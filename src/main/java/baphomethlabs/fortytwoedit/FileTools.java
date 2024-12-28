package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Class containing static methods used for working with files
 */
public class FileTools {

    // .42edit files
    public static final String FILE_DIRECTORY = ".42edit";
    public static final String FILE_OPTIONS = "options.snbt";
    public static final int OPTIONS_FORMAT = 3;
    public static final String FILE_SAVED_ITEMS = "saved_items.snbt";
    public static final int SAVED_ITEMS_FORMAT = 2;
    public static final String FILE_WEB_CACHE = "web_cache.snbt";

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
                    fileContents = formatSnbtAsTree(nbt, false);
                    break;
                }
                case TREE_CONDITIONAL_COLLAPSE : {
                    fileContents = formatSnbtAsTree(nbt, true);
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
     * Format an nbt element as a tree
     * 
     * @param el
     * @param collapseItems if true, compounds that contain the key `id` will be inlined instead of expanded
     * @return
     */
    public static String formatSnbtAsTree(NbtElement el, boolean collapseItems) {
        return formatSnbtAsTree(el, collapseItems, 0);
    }

    /**
     * Inner logic for method above
     */
    private static String formatSnbtAsTree(NbtElement el, boolean collapseItems, int indents) {
        if(el == null)
            return "null";

        String current = "";
        String indent = "";
        for(int i=0; i<indents; i++)
            indent += "\t";
        
        switch(el.getType()) {
            case NbtElement.COMPOUND_TYPE: {
                NbtCompound nbt = (NbtCompound)el;
                if(nbt.isEmpty())
                    current += "{}";
                else if(collapseItems && nbt.contains("id"))
                    current += nbt.asString();
                else {
                    current += "{\n";

                    boolean firstKey = true;
                    for(String k : nbt.getKeys()) {
                        if(firstKey)
                            firstKey = false;
                        else
                            current += ",\n";
                        
                        String keyString = BlackMagick.validSnbtKey(k);
                        current += indent + "\t" + keyString + ": " + formatSnbtAsTree(nbt.get(k), collapseItems, indents+1);
                    }

                    current += "\n" + indent + "}";
                }
                break;
            }
            case NbtElement.LIST_TYPE: {
                NbtList nbt = (NbtList)el;
                if(nbt.isEmpty())
                    current += "[]";
                else {
                    current += "[\n";

                    for(int i=0; i<nbt.size(); i++) {
                        if(i>0)
                            current += ",\n";

                        current += indent + "\t" + formatSnbtAsTree(nbt.get(i), collapseItems, indents+1);
                    }

                    current += "\n" + indent + "]";
                }
                break;
            }
            default: {
                current += BlackMagick.nbtToString(el);
            }
        }
        return current;
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
            try {
                FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\" + FileTools.FILE_DIRECTORY + "\\" + fileName, StandardCharsets.UTF_8, false);
                writer.write(text);
                writer.close();
                return true;
                //TODO verify new file contents `.equals(text)`
            }
            catch(Exception e) {}
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
                //keep consistent with FortytwoEdit.refreshWebItems
                Scanner scan = new Scanner(new File(client.runDirectory.getAbsolutePath() + "\\" + FileTools.FILE_DIRECTORY + "\\" + fileName), StandardCharsets.UTF_8);
                
                String fileContents = "";
                boolean firstLine = true;
                while(scan.hasNextLine()) {
                    if(!firstLine)
                        fileContents += "\n";
                    else
                        firstLine = false;
                    fileContents += scan.nextLine();
                }

                scan.close();

                return fileContents;
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