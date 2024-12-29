package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Class containing static methods used for working with files
 */
public class FileTools {

    // .42edit files
    public static final int FILE_FORMAT = 3; // increment for any breaking file format changes
    public static final String FILE_DIRECTORY = ".42edit";
    public static final String FILE_OPTIONS = "options.snbt";
    public static final String FILE_SAVED_ITEMS = "saved_items.snbt";
    public static final String FILE_WEB_CACHE = "web_cache.snbt";
    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    /**
     * Saves a compound to .snbt file
     * 
     * @param fileName name of file within .42edit folder
     * @param nbt
     * @param display how to format file
     * @return
     */
    public static boolean writeCompoundToFile(String fileName, NbtCompound nbt, FileDisplayType display) {
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
        return writeStringToFile(fileName,fileContents);
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
     * @param fileName
     * @return
     */
    public static NbtCompound readCompoundFromFile(String fileName) {
        String fileContents = readStringFromFile(fileName,"{}");
        if(fileContents != null) {
            NbtElement nbt = BlackMagick.nbtFromString(fileContents);
            if(nbt != null && nbt.getType() == NbtElement.COMPOUND_TYPE) {
                return ((NbtCompound)nbt);
            }

            FortytwoEdit.LOGGER.error("Failed to parse file '" + FILE_DIRECTORY + "/" + fileName + "' as compound: " + fileContents);
        }

        return new NbtCompound();
    }

    /**
     * Overrides the contents of a text file with a new String
     * 
     * @param fileName name of file within .42edit folder
     * @param text
     * @return true if text was set successfully
     */
    private static boolean writeStringToFile(String fileName, String text) {

        if(verifyTextFileExists(fileName, "")) {
            final MinecraftClient client = MinecraftClient.getInstance();

            FileWriter writer = null;
            try {
                writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\" + FileTools.FILE_DIRECTORY + "\\" + fileName, FILE_CHARSET, false);
                writer.write(text);
                writer.close();

                String newText = readStringFromFile(fileName,null);
                if(newText != null && newText.equals(text))
                    return true;
            }
            catch(Exception e) {}
            if(writer != null)
                try {
                    writer.close();
                } catch(Exception e) {}

            FortytwoEdit.LOGGER.error("Failed to write to file '" + FILE_DIRECTORY + "/" + fileName + "': "+text);
        }

        return false;
    }

    /**
     * Return string contents of a file with `\n` characters inserted on linebreaks, or null if failed to read file
     * 
     * @param fileName name of file within .42edit folder
     * @return
     */
    private static String readStringFromFile(String fileName, String defaultText) {

        if(verifyTextFileExists(fileName, defaultText)) {
            final MinecraftClient client = MinecraftClient.getInstance();
            try {
                return Files.readString(new File(client.runDirectory.getAbsolutePath() + "\\" + FileTools.FILE_DIRECTORY + "\\" + fileName).toPath(), FILE_CHARSET);
            }
            catch(Exception e) {}
            FortytwoEdit.LOGGER.error("Failed to read from file '" + FILE_DIRECTORY + "/" + fileName + "'");
        }

        return null;
    }

    /**
     * If file exists, returns true.
     * If file does not exist, create it and return true.
     * If file cannot be verified, return false.
     * 
     * @param fileName name of file within .42edit folder
     * @param defaultText the file contents to insert when creating a new file
     * @return
     */
    private static boolean verifyTextFileExists(String fileName, String defaultText) {

        final MinecraftClient client = MinecraftClient.getInstance();
        try {
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\" + FILE_DIRECTORY)).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\" + FILE_DIRECTORY)).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\" + FILE_DIRECTORY + "\\" + fileName)).exists()) {
                (new File(client.runDirectory.getAbsolutePath() + "\\" + FILE_DIRECTORY + "\\" + fileName)).createNewFile();
                writeStringToFile(fileName,(defaultText == null ? "" : defaultText));
                FortytwoEdit.LOGGER.info("Creating file '" + FILE_DIRECTORY + "/" + fileName + "'");
            }

            if((new File(client.runDirectory.getAbsolutePath() + "\\" + FILE_DIRECTORY + "\\" + fileName)).exists())
                return true;

        } catch (Exception e) {}

        FortytwoEdit.LOGGER.error("Failed to access or create file '" + FILE_DIRECTORY + "/" + fileName + "'");
        return false;
    }
    
}