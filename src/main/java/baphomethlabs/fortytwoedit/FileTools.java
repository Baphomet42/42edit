package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.include.com.google.common.collect.Sets;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.Blaze3D;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilderScreen;

/**
 * Class containing static methods used for working with files
 */
public class FileTools {

    // .42edit files
    public static final int FILE_FORMAT = 5; // increment for any breaking file format changes
    public static final String FILE_FORMAT_LABEL = "file_format";
    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;
    public static final String PATH_SEPARATOR = File.separator;
    public static final String FILE_DIRECTORY = ".42edit";
    public static final String CACHE_DIRECTORY = buildFilePath(FILE_DIRECTORY, "cache");
    public static final String FILE_OPTIONS = buildFilePath(FILE_DIRECTORY, "options.snbt");
    public static final String FILE_SAVED_ITEMS = buildFilePath(FILE_DIRECTORY, "saved_items.snbt");
    public static final String FILE_HEADS = buildFilePath(FILE_DIRECTORY, "heads.snbt");
    public static final String FILE_WEB_CACHE = buildFilePath(CACHE_DIRECTORY, "web_items.snbt");
    private static final String[] KNOWN_MOD_FILES = new String[]{
        CACHE_DIRECTORY,
        FILE_OPTIONS,
        FILE_SAVED_ITEMS,
        FILE_HEADS,
        FILE_WEB_CACHE
    };

    /**
     * 
     * @param nodeList array of 0 or more directory names ending with exactly 1 file or directory name
     * @return joined path
     */
    public static String buildFilePath(String... nodeList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeList.length; i++) {
            if (i > 0)
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
        return buildFilePath(client.gameDirectory.getAbsolutePath(), filePath);
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
        if (nbt != null)
            nbtCopy = nbt.copy();

        switch (display) {
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
        if (readCompoundFromFile(filePath) != null) {
            oldFile = readStringFromFile(filePath);
        }
        if (writeStringToFile(filePath, fileContents)) {
            CompoundTag nbtNew = readCompoundFromFile(filePath);
            if (nbtNew != null && BlackMagick.elementsEqual(nbtCopy, nbtNew))
                return true;
            else {
                String errorMsg = "Failed to write compound to file '" + filePath + "'"
                    + "\nTried to save: " + BlackMagick.nbtToSnbt(nbtCopy)
                    + "\nCompound loaded: " + (nbtNew == null ? "null" : BlackMagick.nbtToSnbt(nbtNew));
                if (oldFile == null) {
                    FortytwoEdit.logError(errorMsg + "\nNo file to revert to.");
                }
                else {
                    FortytwoEdit.logError(errorMsg + "\nReverting file.");
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
        if (fileContents != null && fileContents.length() > 0) {
            Tag nbt = BlackMagick.nbtFromSnbt(fileContents);
            if (nbt != null && nbt.getId() == Tag.TAG_COMPOUND) {
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

        if (verifyFileExists(filePath)) {
            FileWriter writer = null;
            String error = null;
            try {
                writer = new FileWriter(pathFromMinecraft(filePath), FILE_CHARSET, false);
                writer.write(text);
                writer.close();

                String newText = readStringFromFile(filePath);
                if (newText != null && newText.equals(text))
                    return true;
            }
            catch (Exception ex) {
                error = ex.getMessage();
            }
            if (writer != null)
                try {
                    writer.close();
                } catch (Exception ex) {}

            String logMsg = "Failed to write to file '" + filePath + "': " + text;
            if (error != null)
                logMsg += "\n\nError: " + error;
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

        if (testFileExists(filePath)) {
            String error = null;
            try {
                return Files.readString(Paths.get(pathFromMinecraft(filePath)), FILE_CHARSET);
            }
            catch (Exception ex) {
                error = ex.getMessage();
            }
            String logMsg = "Failed to read from file '" + filePath + "'";
            if (error != null)
                logMsg += ": " + error;
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
        if (filePath.length() > 0) {
            try {
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());
                File file = new File(pathFromMinecraft(filePath));
                if (!file.exists()) {
                    file.createNewFile();
                    FortytwoEdit.logInfo("Creating file '" + filePath + "'");
                }

                if (file.exists())
                    return true;

            }
            catch (Exception ex) {
                error = ex.getMessage();
            }
        }

        String logMsg = "Failed to access or create file '" + filePath + "'";
        if (error != null)
            logMsg += ": " + error;
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
        if (filePath.length() > 0) {
            try {
                File file = new File(pathFromMinecraft(filePath));
                if (file.exists())
                    return true;
            } catch (Exception ex) {}
        }
        return false;
    }

    public static boolean openModDir() {
        if (openMinecraftDirEntry(FILE_DIRECTORY))
            return true;
        FortytwoEdit.showToast("File Error", ".42edit folder could not be opened");
        return false;
    }

    public static boolean openMinecraftScreenshots() {
        if (openMinecraftDirEntry(Screenshot.SCREENSHOT_DIR))
            return true;
        FortytwoEdit.showToast("File Error", "Screenshots folder could not be opened");
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
            if (dir.exists() && dir.isDirectory()) {
                Blaze3D.openPath(dir.toPath());
                return true;
            }
        }
        catch (Exception ex) {
            error = ex.getMessage();
        }
        String logMsg = "Failed to open directory '" + filePath + "'";
        if (error != null)
            logMsg += ": " + error;
        FortytwoEdit.logError(logMsg);
        return false;
    }

    /**
     * Scan `.minecraft/.42edit/` and log any unknown files
     */
    public static void scanModFiles() {
        File modDir = new File(pathFromMinecraft(FILE_DIRECTORY));
        if (modDir.exists() && modDir.isDirectory()) {
            List<String> unknownFiles = Lists.newArrayList();
            try {
                scanDirectoryFiles(modDir, unknownFiles);
            }
            catch (Exception ex) {
                FortytwoEdit.logError("Error scanning '" + FILE_DIRECTORY + "' files: " + ex.getMessage());
            }
            if (!unknownFiles.isEmpty()) {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("Found " + unknownFiles.size() + " unknown file(s) within .42edit directory:");
                for (int i = 0; i < unknownFiles.size(); i++) {
                    logMsg.append("\n  - ").append(unknownFiles.get(i));
                    if (i == 15) {
                        int remaining = unknownFiles.size() - i - 1;
                        if (remaining > 0)
                            logMsg.append("\n    and " + remaining + " more");
                        break;
                    }
                }
                FortytwoEdit.logWarn(logMsg.toString());
            }
        }
    }

    private static void scanDirectoryFiles(File dir, List<String> unknownFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                String trimmedPath = formatScannedFile(f);
                boolean known = false;
                for (String s : KNOWN_MOD_FILES) {
                    if (trimmedPath.equals(s)) {
                        known = true;
                        break;
                    }
                }
                if (!known) {
                    unknownFiles.add(trimmedPath);
                }
                else if (f.isDirectory()) {
                    scanDirectoryFiles(f, unknownFiles);
                }
            }
        }
    }

    private static final String MOD_DIR_SEARCH_STRING = pathFromMinecraft(FILE_DIRECTORY) + PATH_SEPARATOR;
    private static String formatScannedFile(File file) {
        String path = file.getAbsolutePath();
        if (path.startsWith(MOD_DIR_SEARCH_STRING))
            return buildFilePath(FILE_DIRECTORY, path.substring(MOD_DIR_SEARCH_STRING.length()));
        FortytwoEdit.logError("Failed to trim scanned file path: " + path);
        return path;
    }

    public record SavedItem(String storedString, ItemStack stack, boolean nbtError) {
        public static SavedItem build(String itemString) {
            ItemStack stack = BlackMagick.itemFromString(itemString);
            if (!stack.isEmpty()) {
                String newItemString = BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(stack));
                if (newItemString.equals(itemString)) {
                    return new SavedItem(itemString, stack, false);
                }
                else {
                    return new SavedItem(itemString, stack, true);
                }
            }
            return new SavedItem(itemString, null, true);
        }
        public static SavedItem build(CompoundTag itemNbt) {
            return SavedItem.build(BlackMagick.nbtToSnbt(itemNbt));
        }
    }

    public static record SavedItemEntry(Tag entry, boolean valid, SavedItem item) {

        private static final String ITEM_SNBT_KEY = "item";
        private static final String SLOT_SNBT_KEY = "slot";

        public static SavedItemEntry of(Tag entry) {
            if (entry != null && entry.getId() == Tag.TAG_COMPOUND) {
                CompoundTag entryCompound = (CompoundTag)entry;
                if (entryCompound.getString(ITEM_SNBT_KEY).isPresent()) {
                    return new SavedItemEntry(entry, true, SavedItem.build(entryCompound.getString(ITEM_SNBT_KEY).get()));
                }
            }
            FortytwoEdit.logWarn("Failed to read saved item entry: " + BlackMagick.nbtToSnbt(entry));
            return new SavedItemEntry(entry, false, null);
        }

    }

    public static record SavedItems(CompoundTag fileNbt, Map<Integer,SavedItemEntry> savedItems, ListTag unknownItems) {

        private static final String ITEMS_SNBT_KEY = "items";
        public static final SavedItems EMPTY = new SavedItems(new CompoundTag(), Maps.newHashMap(), new ListTag());

        public static SavedItems fetch() {
            Map<Integer,SavedItemEntry> savedItems = Maps.newHashMap();
            ListTag unknownItems = new ListTag();
            CompoundTag fileNbt = FileTools.readCompoundFromFile(FileTools.FILE_SAVED_ITEMS);
            if (fileNbt == null)
                fileNbt = new CompoundTag();

            if (fileNbt.getList(ITEMS_SNBT_KEY).isPresent()) {
                ListTag storedItems = fileNbt.getList(ITEMS_SNBT_KEY).get();
                ListTag reportSlotItems = new ListTag();
                if (!storedItems.isEmpty()) {
                    for (int i = 0; i < storedItems.size(); i++) {
                        boolean thisEntryAdded = false;
                        if (storedItems.get(i).getId() == Tag.TAG_COMPOUND) {
                            CompoundTag itemHolder = storedItems.getCompound(i).get();
                            if (itemHolder.getInt(SavedItemEntry.SLOT_SNBT_KEY).isPresent()) {
                                int thisSlot = itemHolder.getInt(SavedItemEntry.SLOT_SNBT_KEY).get();
                                if (!savedItems.containsKey(thisSlot)) {
                                    savedItems.put(thisSlot, SavedItemEntry.of(itemHolder));
                                    thisEntryAdded = true;
                                    if (thisSlot < 0 || thisSlot > (FortytwoEdit.SAVED_ROWS * 9 - 1))
                                        reportSlotItems.add(storedItems.get(i));
                                }
                            }
                        }
                        if (!thisEntryAdded) {
                            unknownItems.add(storedItems.get(i));
                            reportSlotItems.add(storedItems.get(i));
                        }
                    }
                }
                if (!reportSlotItems.isEmpty()) {
                    FortytwoEdit.logWarn("Saved items contains invalid or duplicate slots: " + BlackMagick.nbtToSnbt(reportSlotItems));
                }
            }
            else if (fileNbt.contains(ITEMS_SNBT_KEY)) {
                FortytwoEdit.logWarn("Failed to parse saved items: " + BlackMagick.nbtToSnbt(fileNbt.get(ITEMS_SNBT_KEY)));
            }

            return new SavedItems(fileNbt, savedItems, unknownItems);
        }

        public boolean containsSlot(int index) {
            return savedItems.containsKey(index);
        }

        public ItemStack getStackOrEmpty(int index) {
            if (savedItems.containsKey(index) && savedItems.get(index).valid() && savedItems.get(index).item().stack() != null)
                return savedItems.get(index).item().stack();
            return ItemStack.EMPTY;
        }

        public SavedItem getSavedItemOrNull(int index) {
            if (savedItems.containsKey(index) && savedItems.get(index).valid())
                return savedItems.get(index).item();
            return null;
        }

        public Tag getEntryOrNull(int index) {
            if (savedItems.containsKey(index))
                return savedItems.get(index).entry();
            return null;
        }

        public boolean trySetItem(int index, String itemString) {
            SavedItems currentSavedItems = SavedItems.fetch();

            if (BlackMagick.elementsEqual(getEntryOrNull(index), currentSavedItems.getEntryOrNull(index))) {
                CompoundTag newItem = null;
                if (itemString != null && !itemString.isEmpty()) {
                    newItem = new CompoundTag();
                    newItem.putInt(SavedItemEntry.SLOT_SNBT_KEY, index);
                    newItem.putString(SavedItemEntry.ITEM_SNBT_KEY, itemString);
                }
                
                boolean indexInMap = currentSavedItems.savedItems().containsKey(index);
                if (indexInMap && BlackMagick.elementsEqual(newItem, currentSavedItems.savedItems().get(index).entry())) {
                    FortytwoEdit.showToast("No Change", "Item already saved");
                }
                else if (!indexInMap && newItem == null) {
                    FortytwoEdit.showToast("No Change", "Item already removed");
                }
                else {
                    ListTag itemsList = new ListTag();
                    Set<Integer> newKeys = Sets.newHashSet();
                    newKeys.addAll(currentSavedItems.savedItems().keySet());
                    newKeys.add(index);
                    for (int slot : BlackMagick.sortIntSet(newKeys)) {
                        if (slot == index) {
                            if (newItem != null)
                                itemsList.add(newItem);
                        }
                        else {
                            itemsList.add(currentSavedItems.savedItems().get(slot).entry());
                        }
                    }
                    if (currentSavedItems.unknownItems() != null && !currentSavedItems.unknownItems().isEmpty()) {
                        for (int i = 0; i < currentSavedItems.unknownItems().size(); i++) {
                            itemsList.add(currentSavedItems.unknownItems().get(i));
                        }
                    }

                    CompoundTag savedItemsNbt = currentSavedItems.fileNbt().copy();
                    savedItemsNbt.put(ITEMS_SNBT_KEY, itemsList);
                    savedItemsNbt.putInt(FileTools.FILE_FORMAT_LABEL, FileTools.FILE_FORMAT);
                    FileTools.writeCompoundToFile(FileTools.FILE_SAVED_ITEMS, savedItemsNbt, FileDisplayType.TREE_CONDITIONAL_COLLAPSE);
                    return true;
                }
            }
            else {
                FortytwoEdit.showToast("Error Saving Item", "Reopen the screen and try again.");
                FortytwoEdit.logWarn("Failed to save item. The saved items file has changed since the screen has been open. File not changed."
                    + "\nTried to save: " + ((itemString == null || itemString.isEmpty()) ? "air" : itemString));
            }

            return false;
        }

    }

    public static class SavedHeadEntry {

        private static final String SOURCE_SNBT_KEY = "source";
        private static final String PROFILE_SNBT_KEY = "profile";
        private static final String ITEM_TEMPLATE = "{id:'minecraft:player_head',count:1,components:{}}";

        private Tag entry = null;
        private Tag profileTag = null;
        private boolean valid = false;
        private SavedItem item = null;
        private String uuidHex = null;
        private long timestamp = -1;
        private String username = null;
        private boolean dynamic = false;
        private String decodedBase64 = null;

        private SavedHeadEntry(Tag entry) {
            this.entry = entry;

            if (entry != null && entry.getId() == Tag.TAG_COMPOUND) {
                CompoundTag entryCompound = (CompoundTag)entry;
                if (entryCompound.contains(PROFILE_SNBT_KEY)) {
                    this.valid = true;

                    profileTag = entryCompound.get(PROFILE_SNBT_KEY);
                    this.item = SavedItem.build(BlackMagick.setNbtPath(BlackMagick.validCompoundFromString(ITEM_TEMPLATE),
                        "components.minecraft:profile", profileTag));

                    if (profileTag.getId() == Tag.TAG_STRING) {
                        username = BlackMagick.nbtToSnbtOrString(profileTag);
                        dynamic = true;
                    }
                    else if (profileTag.getId() == Tag.TAG_COMPOUND) {
                        CompoundTag profileCompound = (CompoundTag)profileTag;
                        if (profileCompound.getString("name").isPresent())
                            username = profileCompound.getString("name").get();
                        if (profileCompound.getIntArray("id").isPresent()) {
                            try {
                                UUID uuid = UUIDUtil.uuidFromIntArray(profileCompound.getIntArray("id").get());
                                String uuidString = uuid.toString();
                                if (uuidString != null && uuidString.length() == 36)
                                    this.uuidHex = uuidString;
                            } catch (Exception ex) {}
                        }
                        if (profileCompound.getList("properties").isPresent()) {
                            ListTag propertiesList = profileCompound.getList("properties").get();
                            if (propertiesList.size() > 0 && propertiesList.get(0).getId() == Tag.TAG_COMPOUND) {
                                CompoundTag property = (CompoundTag)propertiesList.get(0);
                                if (property.getString("value").isPresent()) {
                                    String base64 = property.getString("value").get();
                                    try {
                                        decodedBase64 = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
                                        String cleanedString = Pattern.compile("(\"?timestamp\"?\\s*:\\s*\\d+)")
                                            .matcher(decodedBase64).replaceAll("$1L");
                                        CompoundTag decodedNbt = BlackMagick.validCompoundFromString(cleanedString);

                                        if (decodedNbt.contains("timestamp")) {
                                            Tag timestampTag = decodedNbt.get("timestamp");
                                            if (timestampTag.getId() == Tag.TAG_INT)
                                                this.timestamp = ((IntTag)timestampTag).intValue();
                                            else if (timestampTag.getId() == Tag.TAG_LONG)
                                                this.timestamp = ((LongTag)timestampTag).longValue();
                                            if (timestamp < 0)
                                                timestamp = -1;
                                        }
                                    } catch (Exception ex) {}
                                }
                            }
                        }
                        if (!profileCompound.contains("properties")
                        && ((!profileCompound.contains("id") && profileCompound.contains("name"))
                        || (profileCompound.contains("id") && !profileCompound.contains("name")))) {
                            dynamic = true;
                        }
                    }
                }
            }
            if (!this.valid)
                FortytwoEdit.logWarn("Failed to read saved head entry: " + BlackMagick.nbtToSnbt(entry));
        }

        public static SavedHeadEntry read(Tag entry) {
            return new SavedHeadEntry(entry);
        }

        public static SavedHeadEntry create(Tag profile) {
            CompoundTag newEntry = new CompoundTag();
            if (profile != null) {
                newEntry.put(PROFILE_SNBT_KEY, profile);
                if (profile.getId() == Tag.TAG_COMPOUND) {
                    CompoundTag profileCompound = (CompoundTag)profile;
                    if (profileCompound.contains("properties")) {
                        newEntry.put(SOURCE_SNBT_KEY, StringTag.valueOf(BlackMagick.nbtToSnbt(profileCompound.get("properties"))));
                    }
                }
            }
            return SavedHeadEntry.read(newEntry);
        }

        public Tag write() {
            return this.entry;
        }

        public boolean isValid() {
            return this.valid;
        }

        public SavedItem getItem() {
            return this.item;
        }

        public Tooltip getButtonTooltip() {
            MutableComponent tooltip = Component.empty();
            if (isValid()) {
                if (dynamic)
                    tooltip.append(ItemBuilderScreen.grayWhiteText("Dynamic profile"));
                else
                    tooltip.append(ItemBuilderScreen.grayWhiteText("Static profile"));
                if (username != null)
                    tooltip.append(ItemBuilderScreen.grayWhiteText("\nName: ", username));
                if (uuidHex != null)
                    tooltip.append(ItemBuilderScreen.grayWhiteText("\nUUID: ", uuidHex));
                if (timestamp >= 0) {
                    try {
                        String timestampString = new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date(timestamp));
                        tooltip.append(ItemBuilderScreen.grayWhiteText("\nTimestamp: ", timestampString));
                    } catch (Exception ex) {}
                }
                if (profileTag != null)
                    tooltip.append("\n\n").append(BlackMagick.nbtToColorfulText(profileTag));
                if (decodedBase64 != null)
                    tooltip.append("\n\n" + decodedBase64);
            }
            else {
                tooltip.append(Component.empty().append("Failed to read entry\n\n" + BlackMagick.nbtToSnbt(this.entry)).withStyle(ChatFormatting.RED));
            }
            return Tooltip.create(tooltip);
        }

    }

    private static class SavedHeadSortComparator implements Comparator<SavedHeadEntry> {
        @Override
        public int compare(SavedHeadEntry s1, SavedHeadEntry s2) {
            if (s2 == null && s1 != null)
                return -1;
            if (s1 == null && s2 != null)
                return 1;
            if (s1 == null && s2 == null)
                return 0;

            if (!s2.isValid() && s1.isValid())
                return -1;
            if (!s1.isValid() && s2.isValid())
                return 1;
            if (s1.isValid() && s2.isValid()) {

                int compareUUID = compareUUID(s1, s2);
                int compareName = compareName(s1, s2);
                int compareTimestamp = compareTimestamp(s1, s2);
                int compareDynamic = compareDynamic(s1, s2);

                if (compareUUID != 0)
                    return compareUUID;

                if (s1.uuidHex == null) { // both UUIDs are null
                    if (compareName != 0)
                        return compareName;
                    if (compareDynamic != 0)
                        return compareDynamic;
                    if (compareTimestamp != 0)
                        return compareTimestamp;
                }
                else { // both UUIDs are present and match
                    if (compareDynamic != 0)
                        return compareDynamic;
                    if (compareTimestamp != 0)
                        return compareTimestamp;
                    if (compareName != 0)
                        return compareName;
                }

            }

            return BlackMagick.nbtToSnbt(s1.entry).compareToIgnoreCase(BlackMagick.nbtToSnbt(s2.entry));
        }
        private int compareUUID(SavedHeadEntry s1, SavedHeadEntry s2) {
            if (s2.uuidHex == null && s1.uuidHex != null)
                return -1;
            if (s1.uuidHex == null && s2.uuidHex != null)
                return 1;
            if (s1.uuidHex != null && s2.uuidHex != null) {
                return s1.uuidHex.compareToIgnoreCase(s2.uuidHex);
            }
            return 0;
        }
        private int compareName(SavedHeadEntry s1, SavedHeadEntry s2) {
            if (s2.username == null && s1.username != null)
                return -1;
            if (s1.username == null && s2.username != null)
                return 1;
            if (s1.username != null && s2.username != null) {
                return s1.username.compareToIgnoreCase(s2.username);
            }
            return 0;
        }
        private int compareTimestamp(SavedHeadEntry s1, SavedHeadEntry s2) {
            if (s2.timestamp < 0 && s1.timestamp >= 0)
                return 1;
            if (s1.timestamp < 0 && s2.timestamp >= 0)
                return -1;
            if (s1.timestamp >= 0 && s2.timestamp >= 0) {
                if (s1.timestamp < s2.timestamp)
                    return -1;
                if (s2.timestamp < s1.timestamp)
                    return 1;
            }
            return 0;
        }
        private int compareDynamic(SavedHeadEntry s1, SavedHeadEntry s2) {
            if (s2.dynamic && !s1.dynamic)
                return 1;
            if (!s2.dynamic && s1.dynamic)
                return -1;
            return 0;
        }
    }

    public static record SavedHeads(CompoundTag fileNbt, List<SavedHeadEntry> savedHeads) {

        private static final String HEADS_SNBT_KEY = "heads";
        public static final SavedHeads EMPTY = new SavedHeads(new CompoundTag(), Lists.newArrayList());

        public static SavedHeads fetch() {
            List<SavedHeadEntry> savedHeads = Lists.newArrayList();
            CompoundTag fileNbt = FileTools.readCompoundFromFile(FileTools.FILE_HEADS);
            if (fileNbt == null)
                fileNbt = new CompoundTag();

            if (fileNbt.getList(HEADS_SNBT_KEY).isPresent()) {
                ListTag storedItems = fileNbt.getList(HEADS_SNBT_KEY).get();
                for (int i = 0; i < storedItems.size(); i++) {
                    savedHeads.add(SavedHeadEntry.read(storedItems.get(i)));
                }
            }
            else if (fileNbt.contains(HEADS_SNBT_KEY)) {
                FortytwoEdit.logWarn("Failed to parse saved heads: " + BlackMagick.nbtToSnbt(fileNbt.get(HEADS_SNBT_KEY)));
            }

            return new SavedHeads(fileNbt, savedHeads);
        }

        public boolean editIndex(int index, SavedHeadEntry entry) {
            if (index < 0)
                return false;

            SavedHeads currentSavedHeads = SavedHeads.fetch();

            if (index >= savedHeads.size() || index >= currentSavedHeads.savedHeads().size()
            || !BlackMagick.elementsEqual(savedHeads.get(index).write(), currentSavedHeads.savedHeads().get(index).write())) {
                FortytwoEdit.showToast("Error Saving Head", "Reopen the screen and try again.");
                FortytwoEdit.logWarn("Failed to save head. The saved heads file has changed since the screen has been open. File not changed."
                    + "\nTried to save: " + (entry == null ? "null" : BlackMagick.nbtToSnbt(entry.write())));
                return false;
            }

            if (entry != null && BlackMagick.elementsEqual(savedHeads.get(index).write(), entry.write())) {
                FortytwoEdit.showToast("No Change", "Head already saved");
                return false;
            }

            if (entry == null)
                currentSavedHeads.savedHeads().remove(index);
            else
                currentSavedHeads.savedHeads().set(index, entry);
            save(currentSavedHeads.fileNbt(), currentSavedHeads.savedHeads());
            return true;
        }

        public boolean addNew(SavedHeadEntry... entries) {
            if (entries == null || entries.length == 0)
                return false;

            SavedHeads currentSavedHeads = SavedHeads.fetch();

            boolean savedAny = false;

            for (SavedHeadEntry newEntry : entries) {
                boolean saveThis = true;
                for (SavedHeadEntry oldEntry : currentSavedHeads.savedHeads()) {
                    if (BlackMagick.elementsEqual(newEntry.write(), oldEntry.write())) {
                        saveThis = false;
                        continue;
                    }
                }
                if (saveThis) {
                    currentSavedHeads.savedHeads().add(newEntry);
                    savedAny = true;
                }
            }

            if (!savedAny) {
                FortytwoEdit.showToast("No Change", "Head(s) already saved");
                return false;
            }

            save(currentSavedHeads.fileNbt(), currentSavedHeads.savedHeads());
            return true;
        }

        private static void save(CompoundTag currentFile, List<SavedHeadEntry> newSavedHeads) {
            Collections.sort(newSavedHeads, new SavedHeadSortComparator());
            ListTag headsList = new ListTag();
            for (SavedHeadEntry s : newSavedHeads) {
                if (!headsList.isEmpty() && BlackMagick.elementsEqual(headsList.getLast(), s.write()))
                    FortytwoEdit.logInfo("Removing duplicate saved head entry: " + BlackMagick.nbtToSnbt(s.write()));
                else
                    headsList.add(s.write());
            }

            CompoundTag savedHeadsNbt = currentFile.copy();
            savedHeadsNbt.put(HEADS_SNBT_KEY, headsList);
            savedHeadsNbt.putInt(FileTools.FILE_FORMAT_LABEL, FileTools.FILE_FORMAT);
            FileTools.writeCompoundToFile(FileTools.FILE_HEADS, savedHeadsNbt, FileDisplayType.TREE_CONDITIONAL_COLLAPSE);
        }

    }

}
