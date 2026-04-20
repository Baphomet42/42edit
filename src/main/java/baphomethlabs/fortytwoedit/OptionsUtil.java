package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import baphomethlabs.fortytwoedit.FileTools.FileDisplayType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public class OptionsUtil {

    private static final Map<String,ModOption<?>> MOD_OPTIONS = Maps.newHashMap();
    private static final String MOD_OPTION_RESERVED_LABEL_KEYBINDS = "keybinds";
    private static final String[] MOD_OPTION_RESERVED_LABELS = {
        "",
        FileTools.FILE_FORMAT_LABEL,
        MOD_OPTION_RESERVED_LABEL_KEYBINDS
    };
    private static CompoundTag unknownOptions = null;

    public static void readOptions() {
        CompoundTag options = FileTools.readCompoundFromFile(FileTools.FILE_OPTIONS);
        if (options == null)
            options = new CompoundTag();

        options.remove(FileTools.FILE_FORMAT_LABEL);

        options.getCompound(MOD_OPTION_RESERVED_LABEL_KEYBINDS).ifPresent(c -> {
            Set<String> foundKeys = Sets.newHashSet();
            for (String k : c.keySet()) {
                boolean added = false;
                if (c.getString(k).isPresent()) {
                    for (KeyMapping keyMapping : Keybinds.ALL_KEYBINDS) {
                        if (keyMapping.getName().equals(k)) {
                            keyMapping.setKey(keyMapping.getDefaultKey());
                            try {
                                keyMapping.setKey(InputConstants.getKey(c.getString(k).get()));
                            }
                            catch (Exception ex) {}
                            foundKeys.add(k);
                            added = true;
                        }
                    }
                }
                if (!added)
                    FortytwoEdit.logError("Failed to set keybind for binding "+k+" to key "+BlackMagick.nbtToSnbt(c.get(k)));
            }
            if (!foundKeys.isEmpty()) {
                for (String k : foundKeys)
                    c.remove(k);
                KeyMapping.resetMapping();
            }
        });
        if (options.getCompoundOrEmpty(MOD_OPTION_RESERVED_LABEL_KEYBINDS).isEmpty())
            options.remove(MOD_OPTION_RESERVED_LABEL_KEYBINDS);

        for (ModOption<?> option : MOD_OPTIONS.values())
            option.readFromNbt(options);

        unknownOptions = null;
        if (!options.isEmpty()) {
            FortytwoEdit.logWarn("Config file contains unknown keys: "+BlackMagick.nbtToSnbt(options));
            unknownOptions = options.copy();
        }

        writeOptions();
    }

    private static void writeOptions() {
        CompoundTag options = new CompoundTag();
        if (unknownOptions != null)
            options = unknownOptions.copy();

        for (ModOption<?> option : MOD_OPTIONS.values()) {
            options.put(option.id(), option.getCurrentNbt());
        }

        CompoundTag keysCompound = options.getCompoundOrEmpty(MOD_OPTION_RESERVED_LABEL_KEYBINDS);
        if (Keybinds.cachedKeybinds != null)
            Keybinds.cachedKeybinds.clear();
        else
            Keybinds.cachedKeybinds = Maps.newHashMap();
        for (KeyMapping keybind : Keybinds.ALL_KEYBINDS) {
            keysCompound.put(keybind.getName(),StringTag.valueOf(keybind.saveString()));
            Keybinds.cachedKeybinds.put(keybind.getName(),keybind.saveString());
        }
        options.put(MOD_OPTION_RESERVED_LABEL_KEYBINDS, keysCompound);

        options.putInt(FileTools.FILE_FORMAT_LABEL,FileTools.FILE_FORMAT);

        FileTools.writeCompoundToFile(FileTools.FILE_OPTIONS, options, FileDisplayType.TREE);

        onOptionsUpdates();
    }

    private static void onOptionsUpdates() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.debugEntries != null) {
            minecraft.debugEntries.rebuildCurrentList();
        }
        if (minecraft.gui != null && minecraft.gui.hud != null && minecraft.gui.hud.getChat() != null) {
            minecraft.gui.hud.getChat().rescaleChat();
        }
        FortytwoEdit.mixinLocatorBarRefresh();
    }

    public static void saveKeybindOptions() {
        Map<String,String> keybindSettings = Maps.newHashMap();

        for (KeyMapping keyMapping : Keybinds.ALL_KEYBINDS)
            keybindSettings.put(keyMapping.getName(),keyMapping.saveString());

        readOptions();

        for (KeyMapping keyMapping : Keybinds.ALL_KEYBINDS) {
            if (keybindSettings.containsKey(keyMapping.getName())) {
                keyMapping.setKey(keyMapping.getDefaultKey());
                try {
                    keyMapping.setKey(InputConstants.getKey(keybindSettings.get(keyMapping.getName())));
                }
                catch (Exception ex) {}
            }
        }

        writeOptions();
    }

    public static class ModOptions {
        public static final ModOptionBoolean AFK_SCREEN_LOCK = registerModOption(new ModOptionBoolean("afk_screen_lock", false));
        public static final ModOptionBoolean CHAT_ICONS = registerModOption(new ModOptionBoolean("chat_icons", false));
        public static final ModOptionBoolean COORD_HUD = registerModOption(new ModOptionBoolean("coord_hud", false));
        public static final ModOptionString CUSTOM_CAPE = registerModOption(new ModOptionString("custom_cape", "none"));
        public static final ModOptionBoolean CUSTOM_CAPE_TOGGLE = registerModOption(new ModOptionBoolean("custom_cape_toggle", false));
        public static final ModOptionBoolean DEBUG_SCREEN_REARRANGE = registerModOption(new ModOptionBoolean("debug_screen_rearrange", false));
        public static final ModOptionBoolean DYNAMIC_PROFILE_TOOLTIP_INFO = registerModOption(new ModOptionBoolean("dynamic_profile_tooltip_info", false));
        public static final ModOptionString LOCATOR_BAR_PROFILE = registerModOption(new ModOptionString("locator_bar_profile", FortytwoEdit.MIXIN_LOCATOR_BAR_OPTION_NEVER));
        public static final ModOptionBoolean OPTICAPES = registerModOption(new ModOptionBoolean("opticapes", false));

        private static void registerModOptionCommon(ModOption<?> option) {
            if (MOD_OPTIONS.containsKey(option.id())) {
                FortytwoEdit.logError("Duplicate ModOption created with id: "+option.id());
            }
            MOD_OPTIONS.put(option.id(), option);
        }

        public static ModOptionBoolean registerModOption(ModOptionBoolean option) {
            registerModOptionCommon(option);
            return option;
        }

        public static ModOptionString registerModOption(ModOptionString option) {
            registerModOptionCommon(option);
            return option;
        }
    }

    public abstract static class ModOption<T> {

        protected String id;
        protected T defaultSetting;
        protected T currentSetting;

        protected ModOption(String id, T defaultSetting) {
            if (id == null || defaultSetting == null)
                throw new IllegalArgumentException("Tried to create ModOption with null param");
            for (String reserved : MOD_OPTION_RESERVED_LABELS) {
                if (reserved.equals(id))
                    throw new IllegalArgumentException("Tried to create ModOption using reserved id: " + id);
            }

            this.id = id;
            this.defaultSetting = defaultSetting;
            this.currentSetting = this.defaultSetting;
        }

        public String id() {
            return this.id;
        }

        public T getSetting() {
            return this.currentSetting;
        }

        public T getDefault() {
            return this.defaultSetting;
        }

        public T setSetting(T newSetting) {
            readOptions();
            this.currentSetting = newSetting;
            writeOptions();
            return getSetting();
        }

        public T resetSetting() {
            return setSetting(this.defaultSetting);
        }

        public abstract Tag getCurrentNbt();

        public T readFromNbt(CompoundTag nbt) {
            this.currentSetting = this.defaultSetting;
            if (nbt != null) {
                this.currentSetting = readNbtType(nbt);
                nbt.remove(this.id);
            }
            return getSetting();
        }

        protected abstract T readNbtType(CompoundTag nbt);

    }

    public static class ModOptionBoolean extends ModOption<Boolean> {

        public ModOptionBoolean(String id, boolean defaultSetting) {
            super(id, defaultSetting);
        }

        public Tag getCurrentNbt() {
            return ByteTag.valueOf(currentSetting);
        }

        public Boolean readNbtType(CompoundTag nbt) {
            return nbt.getBooleanOr(this.id, this.defaultSetting);
        }

        public boolean toggleSetting() {
            return setSetting(!this.currentSetting);
        }

    }

    public static class ModOptionString extends ModOption<String> {

        public ModOptionString(String id, String defaultSetting) {
            super(id, defaultSetting);
        }

        public Tag getCurrentNbt() {
            return StringTag.valueOf(currentSetting);
        }

        public String readNbtType(CompoundTag nbt) {
            return nbt.getStringOr(this.id, this.defaultSetting);
        }

    }

    public static class Keybinds {

        public static final List<KeyMapping> ALL_KEYBINDS = Lists.newArrayList();

        public static final KeyMapping.Category MOD_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("42edit","keybinds"));

        public static final KeyMapping KEY_AFK_CLICK = registerKeyMapping(new KeyMapping("42edit.key.afk_click", GLFW.GLFW_KEY_MINUS, MOD_CATEGORY));
        public static final KeyMapping KEY_AFK_MOVE = registerKeyMapping(new KeyMapping("42edit.key.afk_move", GLFW.GLFW_KEY_EQUAL, MOD_CATEGORY));
        public static final KeyMapping KEY_FREE_LOOK = registerKeyMapping(new KeyMapping("42edit.key.free_look", GLFW.GLFW_KEY_LEFT_ALT, MOD_CATEGORY));
        public static final KeyMapping KEY_OPEN_MAGICK_GUI = registerKeyMapping(new KeyMapping("42edit.key.open_magick_gui", GLFW.GLFW_KEY_J, MOD_CATEGORY));
        public static final KeyMapping KEY_KEY_MOD = registerKeyMapping(new KeyMapping("42edit.key.key_mod", InputConstants.UNKNOWN.getValue(), MOD_CATEGORY));
        public static final KeyMapping KEY_SPAM_CLICK = registerKeyMapping(new KeyMapping("42edit.key.spam_click", InputConstants.UNKNOWN.getValue(), MOD_CATEGORY));
        public static final KeyMapping KEY_ZOOM = registerKeyMapping(new KeyMapping("42edit.key.zoom", GLFW.GLFW_KEY_R, MOD_CATEGORY));
        public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
            ALL_KEYBINDS.add(keyMapping);
            return keyMapping;
        }

        public static Map<String,String> cachedKeybinds = null;

    }

}
