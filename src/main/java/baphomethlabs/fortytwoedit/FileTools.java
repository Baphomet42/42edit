package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.compress.utils.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Util;

/**
 * Class containing static methods used for working with files
 */
public class FileTools {

    // .42edit files
    public static final int FILE_FORMAT = 3; // increment for any breaking file format changes
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
        final MinecraftClient client = MinecraftClient.getInstance();
        return buildFilePath(client.runDirectory.getAbsolutePath(),filePath);
    }

    /**
     * Saves a compound to .snbt file
     * 
     * @param filePath path to file relative to .minecraft
     * @param nbt
     * @param display how to format file
     * @return
     */
    public static boolean writeCompoundToFile(String filePath, NbtCompound nbt, FileDisplayType display) {
        String fileContents = "";
        if(nbt == null)
            fileContents = "{}";
        else {
            switch(display) {
                case TREE : {
                    fileContents = BlackMagick.formatSnbtAsTree(nbt, false);
                    break;
                }
                case TREE_CONDITIONAL_COLLAPSE : {
                    fileContents = BlackMagick.formatSnbtAsTree(nbt, true);
                    break;
                }
                default : {
                    fileContents = nbt.asString();
                }
            }
        }
        return writeStringToFile(filePath,fileContents);
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
     * Reads a compound from a .snbt file. Returns empty compound if file contents are invalid or cannot be accessed.
     * 
     * @param filePath path to file relative to .minecraft
     * @return
     */
    public static NbtCompound readCompoundFromFile(String filePath) {
        String fileContents = readStringFromFile(filePath);
        if(fileContents != null && fileContents.length()>0) {
            NbtElement nbt = BlackMagick.nbtFromString(fileContents);
            if(nbt != null && nbt.getType() == NbtElement.COMPOUND_TYPE) {
                return ((NbtCompound)nbt);
            }

            FortytwoEdit.logError("Failed to parse file '" + filePath + "' as compound: " + fileContents);
        }

        return new NbtCompound();
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
            try {
                writer = new FileWriter(pathFromMinecraft(filePath), FILE_CHARSET, false);
                writer.write(text);
                writer.close();

                String newText = readStringFromFile(filePath);
                if(newText != null && newText.equals(text))
                    return true;
            }
            catch(Exception ex) {}
            if(writer != null)
                try {
                    writer.close();
                } catch(Exception ex) {}

            FortytwoEdit.logError("Failed to write to file '" + filePath + "': "+text);
        }

        return false;
    }

    /**
     * Return string contents of a file, or null if failed to read file
     * 
     * @param filePath path to file relative to .minecraft
     * @return
     */
    private static String readStringFromFile(String filePath) {

        if(verifyFileExists(filePath)) {
            try {
                return Files.readString(Paths.get(pathFromMinecraft(filePath)), FILE_CHARSET);
            }
            catch(Exception ex) {}
            FortytwoEdit.logError("Failed to read from file '" + filePath + "'");
        }

        return null;
    }

    /**
     * If file exists, returns true.
     * If file does not exist, create it and return true.
     * If file cannot be verified, return false.
     * 
     * @param filePath path to file relative to .minecraft
     * @param defaultText the file contents to insert when creating a new file
     * @return
     */
    private static boolean verifyFileExists(String filePath) {
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

            } catch(Exception ex) {}
        }

        FortytwoEdit.logError("Failed to access or create file '" + filePath + "'");
        return false;
    }

    public static boolean openModDir() {
        if(openMinecraftDirEntry(FILE_DIRECTORY))
            return true;
        FortytwoEdit.showToast("File Error",".42edit folder could not be opened");
        return false;
    }

    public static boolean openMinecraftScreenshots() {
        if(openMinecraftDirEntry(ScreenshotRecorder.SCREENSHOTS_DIRECTORY))
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
        try {
            File dir = new File(pathFromMinecraft(filePath));
            if(dir.exists() && dir.isDirectory()) {
                Util.getOperatingSystem().open(dir);
                return true;
            }
        } catch(Exception ex) {}
        FortytwoEdit.logError("Failed to open directory: " + filePath);
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