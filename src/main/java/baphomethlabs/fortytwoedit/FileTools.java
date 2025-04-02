package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.compress.utils.Lists;

/**
 * Class containing static methods used for working with files
 */
public class FileTools {

    // .42edit files
    public static final int FILE_FORMAT = 4; // increment for any breaking file format changes
    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;
    public static final String PATH_SEPARATOR = File.separator;
    public static final String FILE_DIRECTORY = ".42edit";
    public static final String CACHE_DIRECTORY = buildFilePath(FILE_DIRECTORY, "cache");
    public static final String FILE_OPTIONS = buildFilePath(FILE_DIRECTORY, "options.snbt");
    public static final String FILE_SAVED_ITEMS = buildFilePath(FILE_DIRECTORY, "saved_items.snbt");
    public static final String FILE_WEB_CACHE = buildFilePath(CACHE_DIRECTORY, "web_items.snbt");
    private static final String[] KNOWN_MOD_FILES = new String[]{
        CACHE_DIRECTORY,
        FILE_OPTIONS,
        FILE_SAVED_ITEMS,
        FILE_WEB_CACHE
    };

    /**
     * 
     * @param nodeList array of 0 or more directory names ending with exactly 1 file or directory name
     * @return joined path
     */
    public static String buildFilePath(String... nodeList) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<nodeList.length; i++) {
            if(i>0)
                sb.append(PATH_SEPARATOR);
            sb.append(nodeList[i]);
        }
        return sb.toString();
    }

    /**
     * 
     * @param filePath path to file relative to .minecraft
     * @return absolute path
     */
    public static String pathFromMinecraft(String filePath) {
        final Minecraft client = Minecraft.getInstance();
        return buildFilePath(client.gameDirectory.getAbsolutePath(),filePath);
    }

    /**
     * Saves a compound to .snbt file
     * 
     * @param filePath path to file relative to .minecraft
     * @param nbt
     * @param display how to format file
     * @return true if compound was set successfully
     */
    public static boolean writeCompoundToFile(String filePath, CompoundTag nbt, FileDisplayType display) {
        String fileContents = "";
        CompoundTag nbtCopy = new CompoundTag();
        if(nbt != null)
            nbtCopy = nbt.copy();

        switch(display) {
            case TREE : {
                fileContents = BlackMagick.formatSnbtAsTree(nbtCopy, false);
                break;
            }
            case TREE_CONDITIONAL_COLLAPSE : {
                fileContents = BlackMagick.formatSnbtAsTree(nbtCopy, true);
                break;
            }
            default : {
                fileContents = BlackMagick.nbtToSnbt(nbtCopy);
            }
        }

        String oldFile = null;
        if(readCompoundFromFile(filePath) != null) {
            oldFile = readStringFromFile(filePath);
        }
        if(writeStringToFile(filePath,fileContents)) {
            CompoundTag nbtNew = readCompoundFromFile(filePath);
            if(nbtNew != null && BlackMagick.elementsEqual(nbtCopy,nbtNew))
                return true;
            else {
                String errorMsg = "Failed to write compound to file '" + filePath + "'"
                    + "\nTried to save: " + BlackMagick.nbtToSnbt(nbtCopy)
                    + "\nCompound loaded: " + (nbtNew==null ? "null" : BlackMagick.nbtToSnbt(nbtNew));
                if(oldFile == null) {
                    FortytwoEdit.logError(errorMsg+"\nNo file to revert to.");
                }
                else {
                    FortytwoEdit.logError(errorMsg+"\nReverting file.");
                    writeStringToFile(filePath, oldFile);
                }
            }
        }
        return false;
    }

    /**
     * How to format .snbt file
     */
    public enum FileDisplayType {
        DEFAULT,
        TREE,
        TREE_CONDITIONAL_COLLAPSE
    }

    /**
     * Reads a compound from a .snbt file
     * 
     * @param filePath path to file relative to .minecraft
     * @return compound from file or null if compound could not be parsed
     */
    public static CompoundTag readCompoundFromFile(String filePath) {
        String fileContents = readStringFromFile(filePath);
        if(fileContents != null && fileContents.length()>0) {
            Tag nbt = BlackMagick.nbtFromString(fileContents);
            if(nbt != null && nbt.getId() == Tag.TAG_COMPOUND) {
                return ((CompoundTag)nbt);
            }

            FortytwoEdit.logError("Failed to parse file '" + filePath + "' as compound: " + fileContents);
        }

        return null;
    }

    /**
     * Overrides the contents of a text file with a new String
     * 
     * @param filePath path to file relative to .minecraft
     * @param text
     * @return true if text was set successfully
     */
    private static boolean writeStringToFile(String filePath, String text) {

        if(verifyFileExists(filePath)) {
            FileWriter writer = null;
            String error = null;
            try {
                writer = new FileWriter(pathFromMinecraft(filePath), FILE_CHARSET, false);
                writer.write(text);
                writer.close();

                String newText = readStringFromFile(filePath);
                if(newText != null && newText.equals(text))
                    return true;
            }
            catch(Exception ex) {
                error = ex.getMessage();
            }
            if(writer != null)
                try {
                    writer.close();
                } catch(Exception ex) {}

            String logMsg = "Failed to write to file '" + filePath + "': "+text;
            if(error != null)
                logMsg += "\n\nError: "+error;
            FortytwoEdit.logError(logMsg);
        }

        return false;
    }

    /**
     * Return string contents of a file, or null if failed to read file
     * 
     * @param filePath path to file relative to .minecraft
     * @return
     */
    public static String readStringFromFile(String filePath) {

        if(testFileExists(filePath)) {
            String error = null;
            try {
                return Files.readString(Paths.get(pathFromMinecraft(filePath)), FILE_CHARSET);
            }
            catch(Exception ex) {
                error = ex.getMessage();
            }
            String logMsg = "Failed to read from file '" + filePath + "'";
            if(error != null)
                logMsg += ": "+error;
            FortytwoEdit.logError(logMsg);
        }

        return null;
    }

    /**
     * If file exists, returns true.
     * If file does not exist, create it and return true.
     * If file cannot be verified, return false.
     * 
     * @param filePath path to file relative to .minecraft
     * @return
     */
    private static boolean verifyFileExists(String filePath) {
        String error = null;
        if(filePath.length()>0) {
            try {
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                File file = new File(pathFromMinecraft(filePath));
                if(!file.exists()) {
                    file.createNewFile();
                    FortytwoEdit.logInfo("Creating file '" + filePath + "'");
                }

                if(file.exists())
                    return true;

            }
            catch(Exception ex) {
                error = ex.getMessage();
            }
        }

        String logMsg = "Failed to access or create file '" + filePath + "'";
        if(error != null)
            logMsg += ": "+error;
        FortytwoEdit.logError(logMsg);
        return false;
    }

    /**
     * If file exists, returns true.
     * If file does not exist or cannot be verified, return false.
     * 
     * @param filePath path to file relative to .minecraft
     * @return
     */
    public static boolean testFileExists(String filePath) {
        if(filePath.length()>0) {
            try {
                File file = new File(pathFromMinecraft(filePath));
                if(file.exists())
                    return true;
            } catch(Exception ex) {}
        }
        return false;
    }

    public static boolean openModDir() {
        if(openMinecraftDirEntry(FILE_DIRECTORY))
            return true;
        FortytwoEdit.showToast("File Error",".42edit folder could not be opened");
        return false;
    }

    public static boolean openMinecraftScreenshots() {
        if(openMinecraftDirEntry(Screenshot.SCREENSHOT_DIR))
            return true;
        FortytwoEdit.showToast("File Error","Screenshots folder could not be opened");
        return false;
    }

    /**
     * 
     * @param filePath path to file relative to .minecraft
     * @return true if file was opened
     */
    private static boolean openMinecraftDirEntry(String filePath) {
        String error = null;
        try {
            File dir = new File(pathFromMinecraft(filePath));
            if(dir.exists() && dir.isDirectory()) {
                Util.getPlatform().openFile(dir);
                return true;
            }
        }
        catch(Exception ex) {
            error = ex.getMessage();
        }
        String logMsg = "Failed to open directory '" + filePath + "'";
        if(error != null)
            logMsg += ": "+error;
        FortytwoEdit.logError(logMsg);
        return false;
    }

    /**
     * Scan `.minecraft/.42edit/` and log any unknown files
     */
    public static void scanModFiles() {
        File modDir = new File(pathFromMinecraft(FILE_DIRECTORY));
        if(modDir.exists() && modDir.isDirectory()) {
            List<String> unknownFiles = Lists.newArrayList();
            try {
                scanDirectoryFiles(modDir, unknownFiles);
            }
            catch(Exception ex) {
                FortytwoEdit.logError("Error scanning '"+FILE_DIRECTORY+"' files: "+ex.getMessage());
            }
            if(!unknownFiles.isEmpty()) {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("Found "+unknownFiles.size()+" unknown file(s) within .42edit directory:");
                for(int i=0; i<unknownFiles.size(); i++) {
                    logMsg.append("\n  - ").append(unknownFiles.get(i));
                    if(i==15) {
                        int remaining = unknownFiles.size()-i-1;
                        if(remaining > 0)
                            logMsg.append("\n    and "+remaining+" more");
                        break;
                    }
                }
                FortytwoEdit.logWarn(logMsg.toString());
            }
        }
    }

    private static void scanDirectoryFiles(File dir, List<String> unknownFiles) {
        File[] files = dir.listFiles();
        if(files != null) {
            for(File f : files) {
                String trimmedPath = formatScannedFile(f);
                boolean known = false;
                for(String s : KNOWN_MOD_FILES) {
                    if(trimmedPath.equals(s)) {
                        known = true;
                        break;
                    }
                }
                if(!known) {
                    unknownFiles.add(trimmedPath);
                }
                else if(f.isDirectory()) {
                    scanDirectoryFiles(f, unknownFiles);
                }
            }
        }
    }

    private static final String MOD_DIR_SEARCH_STRING = pathFromMinecraft(FILE_DIRECTORY)+PATH_SEPARATOR;
    private static String formatScannedFile(File file) {
        String path = file.getAbsolutePath();
        if(path.startsWith(MOD_DIR_SEARCH_STRING))
            return buildFilePath(FILE_DIRECTORY, path.substring(MOD_DIR_SEARCH_STRING.length()));
        FortytwoEdit.logError("Failed to trim scanned file path: "+path);
        return path;
    }

}