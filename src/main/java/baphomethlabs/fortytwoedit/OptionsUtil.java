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
import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import baphomethlabs.fortytwoedit.gui.widget.SmartButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class OptionsUtil {

    public static final Map<String,ModOption<?>> MOD_OPTIONS = Maps.newHashMap();
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

    public static final Identifier[] DEBUG_SCREEN_REARRANGE_MOVE_LAST = {
        Identifier.withDefaultNamespace("looking_at_entity"),
        Identifier.withDefaultNamespace("looking_at_fluid"),
        Identifier.withDefaultNamespace("looking_at_block")
    };
    private static String movedLastDisplay = null;
    private static String getMovedLastDisplay() {
        if (movedLastDisplay == null) {
            movedLastDisplay = "";
            for (Identifier i : DEBUG_SCREEN_REARRANGE_MOVE_LAST)
                movedLastDisplay += "\n  \u2022 " + BlackMagick.identifierToStringSimplified(i);
        }
        return movedLastDisplay;
    }

    public static class ModOptions {
        public static final ModOptionBoolean AUTO_CLICK_LOCK = registerModOption(
            new ModOptionBoolean("auto_click_lock", false, "AutoClick Lock")).setDescription(true,
            "While AutoClick is in use, mouse movement will be ignored and FPS reduced.");
        public static final ModOptionBoolean CHAT_ICONS = registerModOption(
            new ModOptionBoolean("chat_icons", false, "Chat Icons")).setDescription(true,
            "Player icons will render by chat messages.");
        public static final ModOptionBoolean COORD_HUD = registerModOption(
            new ModOptionBoolean("coord_hud", false, "Coord HUD")).setDescription(true,
            "The HUD will render coordinates and facing direction.");
        public static final ModOptionString CUSTOM_CAPE = registerModOption(
            new ModOptionString("custom_cape", "none", "Custom Cape", new StringChoicesClientCape())).setDescription(
            "Determines which cape to render when the Custom Cape Toggle is enabled.");
        public static final ModOptionBoolean CUSTOM_CAPE_TOGGLE = registerModOption(
            new ModOptionBoolean("custom_cape_toggle", false, "Custom Cape Toggle")).setDescription(true,
            "Your player will render with the cape specified in the Custom Cape option.");
        public static final ModOptionBoolean DEBUG_SCREEN_REARRANGE = registerModOption(
            new ModOptionBoolean("debug_screen_rearrange", false, "Debug Screen Rearrange")).setDescription(true,
            "The following debug screen entries are moved to the end of the list:"+getMovedLastDisplay());
        public static final ModOptionBoolean DYNAMIC_PROFILE_TOOLTIP_INFO = registerModOption(
            new ModOptionBoolean("dynamic_profile_tooltip_info", false, "Dynamic Profile Tooltip Info")).setDescription(true,
            "The profile component tooltip will contain extra info for dynamic profiles.");
        public static final ModOptionString LOCATOR_BAR_PROFILE = registerModOption(
            new ModOptionString("locator_bar_profile", FortytwoEdit.MIXIN_LOCATOR_BAR_OPTION_NEVER, "Locator Bar Profile",
            FortytwoEdit.LOCATOR_BAR_PROFILE_CHOICES)).setDescription(
            "Choice whether player icon shows instead of locator bar icon.");
        public static final ModOptionBoolean OPTICAPES = registerModOption(
            new ModOptionBoolean("opticapes", false, "OptiCapes").setDescription(true,
            "Players will render with their OptiFine cape, if they have one. 42edit is not affiliated with OptiFine in any way."));

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
        protected String displayName;

        protected ModOption(String id, T defaultSetting, String displayName) {
            if (id == null || defaultSetting == null)
                throw new IllegalArgumentException("Tried to create ModOption with null param");
            for (String reserved : MOD_OPTION_RESERVED_LABELS) {
                if (reserved.equals(id))
                    throw new IllegalArgumentException("Tried to create ModOption using reserved id: " + id);
            }

            this.id = id;
            this.defaultSetting = defaultSetting;
            this.currentSetting = this.defaultSetting;
            this.displayName = displayName;
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

        public abstract boolean isDefault();

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

        public String getDisplayName() {
            return this.displayName;
        }

        public abstract Component getButtonTooltip();

        public abstract SmartButton getButton(GenericScreen screen);

    }

    public static class ModOptionBoolean extends ModOption<Boolean> {

        private Component description;
        private static final Component LABEL_ON_TRUE = Component.empty()
            .append(Component.empty().append("When on").withStyle(ChatFormatting.GREEN))
            .append(Component.empty().append(" - ").withStyle(ChatFormatting.GRAY));
        private static final Component LABEL_ON_FALSE = Component.empty()
            .append(Component.empty().append("When off").withStyle(ChatFormatting.RED))
            .append(Component.empty().append(" - ").withStyle(ChatFormatting.GRAY));
        private static final Component LABEL_DEFAULT_TRUE = Component.empty().append("On").withStyle(ChatFormatting.GREEN);
        private static final Component LABEL_DEFAULT_FALSE = Component.empty().append("Off").withStyle(ChatFormatting.RED);

        public ModOptionBoolean(String id, boolean defaultSetting, String displayName) {
            super(id, defaultSetting, displayName);
        }

        public Tag getCurrentNbt() {
            return ByteTag.valueOf(currentSetting);
        }

        public Boolean readNbtType(CompoundTag nbt) {
            return nbt.getBooleanOr(this.id, this.defaultSetting);
        }

        public boolean isDefault() {
            return getSetting() == getDefault();
        }

        public boolean toggleSetting() {
            return setSetting(!this.currentSetting);
        }

        public ModOptionBoolean setDescription(boolean displayState, String onState) {
            return setDescription(displayState, Component.empty().append(onState).withStyle(ChatFormatting.GRAY));
        }

        public ModOptionBoolean setDescription(boolean displayState, Component onState) {
            if (displayState)
                return setDescription(onState, null);
            else
                return setDescription(null, onState);
        }

        public ModOptionBoolean setDescription(String onTrue, String onFalse) {
            return setDescription(Component.empty().append(onTrue).withStyle(ChatFormatting.GRAY), Component.empty().append(onFalse).withStyle(ChatFormatting.GRAY));
        }

        public ModOptionBoolean setDescription(Component onTrue, Component onFalse) {
            MutableComponent tempDesc = null;
            if (onTrue != null || onFalse != null) {
                tempDesc = Component.empty();
                if (onTrue != null)
                    tempDesc.append(LABEL_ON_TRUE).append(onTrue);
                if (onTrue != null && onFalse != null)
                    tempDesc.append("\n\n");
                if (onFalse != null)
                    tempDesc.append(LABEL_ON_FALSE).append(onFalse);
            }
            description = tempDesc;
            return this;
        }

        public Component getButtonTooltip() {
            MutableComponent btnTooltip = Component.empty().append("Default: ").append(getDefault() ? LABEL_DEFAULT_TRUE : LABEL_DEFAULT_FALSE);
            if (this.description != null)
                btnTooltip.append("\n\n").append(this.description);
            return btnTooltip;
        }

        public SmartButton getButton(GenericScreen screen) {
            MutableComponent btnTxt = Component.empty().append(this.displayName + ": ");
            if (!getSetting())
                btnTxt.append(Component.empty().append("Off").withStyle(getSetting() == getDefault() ? ChatFormatting.GRAY : ChatFormatting.RED));
            else
                btnTxt.append(Component.empty().append("On").withStyle(getSetting() == getDefault() ? ChatFormatting.GRAY : ChatFormatting.GREEN));

            return screen.WIDGET_UTIL.newButton(btnTxt, btn -> {
                toggleSetting();
                screen.reloadScreen();
            }).fullWidth().setTooltip(getButtonTooltip()).setTooltipDelayStandard().build();
        }

    }

    public static class ModOptionString extends ModOption<String> {

        private Component description;
        private StringChoices choices;

        public ModOptionString(String id, String defaultSetting, String displayName, StringChoices choices) {
            super(id, defaultSetting, displayName);
            this.choices = choices;
        }

        public Tag getCurrentNbt() {
            return StringTag.valueOf(currentSetting);
        }

        public String readNbtType(CompoundTag nbt) {
            return nbt.getStringOr(this.id, this.defaultSetting);
        }

        public boolean isDefault() {
            return getSetting().equals(getDefault());
        }

        public String cycleSetting(boolean forward) {
            if (choices != null) {
                List<StringOption> choiceOptions = choices.getChoices();
                int foundIndex = -1;
                for (int i=0; i<choiceOptions.size(); i++) {
                    if (choiceOptions.get(i).choice().equals(getSetting())) {
                        foundIndex = i;
                        break;
                    }
                }

                if (foundIndex == -1)
                    foundIndex = 0;
                else {
                    if (forward)
                        foundIndex++;
                    else
                        foundIndex--;
                }

                if (foundIndex >= choiceOptions.size())
                    foundIndex = 0;
                if (foundIndex < 0)
                    foundIndex = choiceOptions.size() - 1;

                return setSetting(choiceOptions.get(foundIndex).choice());
            }
            return resetSetting();
        }

        public ModOptionString setDescription(String description) {
            return setDescription(Component.empty().append(description).withStyle(ChatFormatting.GRAY));
        }

        public ModOptionString setDescription(Component description) {
            this.description = description;
            return this;
        }

        public Component getButtonTooltip() {
            StringOptionDetails details = StringOptionDetails.get(choices, getSetting(), getDefault(), isDefault());
            return getButtonTooltip(details.didFindCurrent(), details.foundDefault(), details.choiceOptions());
        }

        public Component getButtonTooltip(boolean didFindCurrent, StringOption foundDefault, List<StringOption> choiceOptions) {
            MutableComponent btnTooltip = Component.empty().append("Default: ").append(Component.empty().append(foundDefault.displayName()).withStyle(ChatFormatting.GRAY));

            if (!didFindCurrent)
                btnTooltip.append(Component.empty().append("\n\nUnknown option '"+getSetting()+"'").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));

            if (description != null)
                btnTooltip.append("\n\n").append(description);

            if (choiceOptions != null && !choiceOptions.isEmpty()) {
                btnTooltip.append("\n\n").append(Component.empty().append("Options\n").withStyle(ChatFormatting.UNDERLINE));
                for (StringOption o : choiceOptions) {
                    btnTooltip.append("\n  \u2022 " + o.displayName());
                    if (o.description() != null) {
                        btnTooltip.append(Component.empty().append(" - ").append(o.description()).withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            return btnTooltip;
        }

        public SmartButton getButton(GenericScreen screen) {
            StringOptionDetails details = StringOptionDetails.get(choices, getSetting(), getDefault(), isDefault());

            MutableComponent btnTxt = Component.empty().append(this.displayName + ": ")
                .append(Component.empty().append(details.foundCurrent().displayName()).withStyle(details.btnColor()));

            return screen.WIDGET_UTIL.newButton(btnTxt, (btn, inputs) -> {
                cycleSetting(!inputs.hasShiftDown());
                screen.reloadScreen();
            }).fullWidth().setTooltip(getButtonTooltip(details.didFindCurrent(), details.foundDefault(), details.choiceOptions())).setTooltipDelayStandard().build();
        }

        private record StringOptionDetails(StringOption foundCurrent, boolean didFindCurrent, StringOption foundDefault, boolean didFindDefault, ChatFormatting btnColor, List<StringOption> choiceOptions) {
            public static StringOptionDetails get(StringChoices choices, String currentSetting, String defaultSetting, boolean isDefault) {
                StringOption foundCurrent = StringOption.of(currentSetting);
                boolean didFindCurrent = false;
                StringOption foundDefault = StringOption.of(defaultSetting);
                boolean didFindDefault = false;
                ChatFormatting btnColor = ChatFormatting.YELLOW;

                List<StringOption> choiceOptions = null;
                if (choices != null) {
                    choiceOptions = choices.getChoices();
                    for (StringOption o : choiceOptions) {
                        if (o.choice().equals(currentSetting)) {
                            foundCurrent = o;
                            btnColor = ChatFormatting.GREEN;
                            didFindCurrent = true;
                        }
                        if (o.choice().equals(defaultSetting)) {
                            foundDefault = o;
                            didFindDefault = true;
                        }
                        if (didFindCurrent && didFindDefault)
                            break;
                    }
                }

                if (isDefault)
                    btnColor = ChatFormatting.GRAY;
                
                return new StringOptionDetails(foundCurrent, didFindCurrent, foundDefault, didFindDefault, btnColor, choiceOptions);
            }
        }

    }

    public static abstract class StringChoices {

        public abstract List<StringOption> getChoices();

    }

    public static class StringChoicesList extends StringChoices {

        public List<StringOption> choices = Lists.newArrayList();

        public StringChoicesList(List<StringOption> choices) {
            this.choices = choices;
        }

        public StringChoicesList(StringOption... choices) {
            this.choices = List.of(choices);
        }

        public List<StringOption> getChoices() {
            return this.choices;
        }

    }

    public static class StringChoicesClientCape extends StringChoices {

        public StringChoicesClientCape() {}

        public List<StringOption> getChoices() {
            List<StringOption> list = Lists.newArrayList();
            for (FortytwoEdit.CapeTexture c : FortytwoEdit.CLIENT_CAPES) {
                StringOption o = StringOption.of(c.id(), c.name() == null ? c.id() : c.name());
                if (c.desc() != null)
                    o = o.withDesc(c.desc());
                list.add(o);
            }
            return list;
        }

    }

    public static record StringOption(String choice, String displayName, Component description) {

        public static StringOption of(String choice) {
            return new StringOption(choice, choice, null);
        }

        public static StringOption of(String choice, String displayName) {
            return new StringOption(choice, displayName, null);
        }

        public StringOption withDesc(String desc) {
            return new StringOption(this.choice, this.displayName, Component.empty().append(desc).withStyle(ChatFormatting.GRAY));
        }

        public StringOption withDesc(Component desc) {
            return new StringOption(this.choice, this.displayName, desc);
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
