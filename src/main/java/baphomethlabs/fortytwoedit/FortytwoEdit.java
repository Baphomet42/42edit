package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.blaze3d.platform.NativeImage;
import baphomethlabs.fortytwoedit.BlackMagick.ParsedText;
import baphomethlabs.fortytwoedit.FileTools.FileDisplayType;
import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilderScreen;
import baphomethlabs.fortytwoedit.gui.screen.LogScreen;
import baphomethlabs.fortytwoedit.gui.screen.MagickScreen;
import baphomethlabs.fortytwoedit.mixin.GameRendererInvoker;
import baphomethlabs.fortytwoedit.mixin.HotbarManagerAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyMappingAccessor;
import baphomethlabs.fortytwoedit.mixin.SkinManagerInvoker;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class FortytwoEdit implements ClientModInitializer {

    // log
    public static final String MOD_ID_JAVA = "fortytwoedit";
    public static final String MOD_ID_MC = "42edit";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID_MC);
    /**
     * Used for creating ProblemReporter.ScopedCollector objects only
     */
    public static final Logger getLogger() {
        return LOGGER;
    }

    // gui
    public static final Supplier<GenericScreen> DEFAULT_SCREEN = MagickScreen::new;
    public static Supplier<GenericScreen> quickScreen = DEFAULT_SCREEN;

    // chat icons
    private static final Set<String> CHAT_ICON_KEY_SET = Sets.newHashSet();
    private static final List<String> CHAT_ICON_KEY_LIST = Lists.newArrayList();
    private static final Map<String,Component> CHAT_ICON_COMPONENT_CACHE = Maps.newHashMap();
    private static final Map<String,GuiMessage> CHAT_ICON_MESSAGE_CACHE = Maps.newHashMap();
    private static void chatCache(String key) {
        boolean added = CHAT_ICON_KEY_SET.add(key);
        if (added) {
            CHAT_ICON_KEY_LIST.add(key);
            if (CHAT_ICON_KEY_LIST.size() > 250) {
                String removed = CHAT_ICON_KEY_LIST.remove(0);
                CHAT_ICON_COMPONENT_CACHE.remove(removed);
                CHAT_ICON_MESSAGE_CACHE.remove(removed);
                CHAT_ICON_KEY_SET.remove(removed);
            }
        }
    }
    public static void chatIconNew(Component text, UUID uuid) {
        try {
            final Minecraft minecraft = Minecraft.getInstance();
            PlayerInfo playerInfo = minecraft.player.connection.getPlayerInfo(uuid);
            if (playerInfo != null) {
                String mapKey = ""+minecraft.gui.hud.getGuiTicks()+"_"+BlackMagick.textComponentToSnbt(text);
                MutableComponent newComponent = Component.empty();

                ParsedText parsedText = BlackMagick.textComponentFromSnbt("{object:'player',player:{id:"
                    + BlackMagick.nbtToSnbt(new IntArrayTag(UUIDUtil.uuidToIntArray(uuid))) + "},hat:"
                    + (playerInfo.showHat() ? "true" : "false") + ",shadow_color:0}");

                if (parsedText.isValid()) {
                    newComponent.append(parsedText.text());
                    newComponent.append(" ");
                    newComponent.append(text);
                    CHAT_ICON_COMPONENT_CACHE.put(mapKey,newComponent);
                    chatCache(mapKey);
                }
            }
        } catch (Exception ex) {}
    }
    public static void chatIconNew(Component text, String name) {
        try {
            final Minecraft minecraft = Minecraft.getInstance();
            String mapKey = "" + minecraft.gui.hud.getGuiTicks() + "_"+BlackMagick.textComponentToSnbt(text);
            MutableComponent newComponent = Component.empty();

            String hat = ",hat:true";
            PlayerInfo playerInfo = minecraft.player.connection.getPlayerInfo(name);
            if (playerInfo != null && !playerInfo.showHat()) {
                hat = ",hat:false";
            }

            ParsedText parsedText = BlackMagick.textComponentFromSnbt("{object:'player',player:{name:"+BlackMagick.nbtToSnbt(StringTag.valueOf(name))+"}"+hat+",shadow_color:0}");

            if (parsedText.isValid()) {
                newComponent.append(parsedText.text());
                newComponent.append(" ");
                newComponent.append(text);
                CHAT_ICON_COMPONENT_CACHE.put(mapKey,newComponent);
                chatCache(mapKey);
            }
        } catch (Exception ex) {}
    }
    public static GuiMessage chatIconGet(GuiMessage guiMessage) {
        String mapKey = ""+guiMessage.addedTime()+"_"+BlackMagick.textComponentToSnbt(guiMessage.content());

        GuiMessage testCache = CHAT_ICON_MESSAGE_CACHE.get(mapKey);
        if (testCache != null)
            return testCache;

        Component testComponent = CHAT_ICON_COMPONENT_CACHE.get(mapKey);
        if (testComponent != null) {
            GuiMessage newMessage = new GuiMessage(guiMessage.addedTime(), testComponent, guiMessage.signature(), guiMessage.source(), guiMessage.tag());
            CHAT_ICON_MESSAGE_CACHE.put(mapKey,newMessage);
            chatCache(mapKey);
            return newMessage;
        }

        try {
            String name = null;
            UUID uuid = null;
            Tag nbt = BlackMagick.textComponentToNbt(guiMessage.content());
            if (nbt.getId() == Tag.TAG_COMPOUND) {
                CompoundTag compound = (CompoundTag)nbt;
                if (!compound.contains("extra") && compound.getString("translate").isPresent()) {
                    switch (compound.getString("translate").get()) {
                        case "multiplayer.player.joined" :
                        case "multiplayer.player.left" :
                        case "chat.type.text" :
                        case "chat.type.emote" :
                        case "chat.type.announcement" :
                        case "commands.message.display.incoming" :
                        {
                            Tag hoverUUID = BlackMagick.getNbtPath(compound,"with[0].hover_event.uuid");
                            if (hoverUUID != null && hoverUUID.getId() == Tag.TAG_INT_ARRAY) {
                                uuid = UUIDUtil.uuidFromIntArray(((IntArrayTag)hoverUUID).getAsIntArray());
                            }
                            else {
                                Tag textTag = BlackMagick.getNbtPath(compound,"with[0]");
                                if (textTag != null && textTag.getId() == Tag.TAG_STRING) {
                                    name = BlackMagick.nbtToSnbtOrString(textTag);
                                }
                                else {
                                    textTag = BlackMagick.getNbtPath(compound,"with[0].text");
                                    if (textTag != null && textTag.getId() == Tag.TAG_STRING) {
                                        name = BlackMagick.nbtToSnbtOrString(textTag);
                                    }
                                }
                            }
                            break;
                        }
                        case "chat.type.team.text" :
                        case "chat.type.team.sent" :
                        {
                            Tag hoverUUID = BlackMagick.getNbtPath(compound,"with[1].hover_event.uuid");
                            if (hoverUUID != null && hoverUUID.getId() == Tag.TAG_INT_ARRAY) {
                                uuid = UUIDUtil.uuidFromIntArray(((IntArrayTag)hoverUUID).getAsIntArray());
                            }
                            else {
                                Tag textTag = BlackMagick.getNbtPath(compound,"with[1]");
                                if (textTag != null && textTag.getId() == Tag.TAG_STRING) {
                                    name = BlackMagick.nbtToSnbtOrString(textTag);
                                }
                                else {
                                    textTag = BlackMagick.getNbtPath(compound,"with[1].text");
                                    if (textTag != null && textTag.getId() == Tag.TAG_STRING) {
                                        name = BlackMagick.nbtToSnbtOrString(textTag);
                                    }
                                }
                            }
                            break;
                        }
                        case "commands.message.display.outgoing" :
                        {
                            uuid = UUIDUtil.uuidFromIntArray(FortytwoEdit.UUID.getAsIntArray());
                            break;
                        }
                        default : break;
                    }
                }
            }
            if (uuid != null)
                chatIconNew(guiMessage.content(), uuid);
            else if (name != null)
                chatIconNew(guiMessage.content(), name);

            testComponent = CHAT_ICON_COMPONENT_CACHE.get(mapKey);
            if (testComponent != null) {
                GuiMessage newMessage = new GuiMessage(guiMessage.addedTime(), testComponent, guiMessage.signature(), guiMessage.source(), guiMessage.tag());
                CHAT_ICON_MESSAGE_CACHE.put(mapKey,newMessage);
                chatCache(mapKey);
                return newMessage;
            }
        } catch (Exception ex) {}

        CHAT_ICON_MESSAGE_CACHE.put(mapKey,guiMessage);
        chatCache(mapKey);
        return guiMessage;
    }


    // locator bar
    public static final String MIXIN_LOCATOR_BAR_OPTION_NEVER = "never";
    private static final String MIXIN_LOCATOR_BAR_OPTION_OVERRIDE_DEFAULT = "override_default";
    private static final String MIXIN_LOCATOR_BAR_OPTION_ALWAYS = "always";
    private static final String[] MIXIN_LOCATOR_BAR_PROFILE_OPTIONS = {
        MIXIN_LOCATOR_BAR_OPTION_NEVER,
        MIXIN_LOCATOR_BAR_OPTION_OVERRIDE_DEFAULT,
        MIXIN_LOCATOR_BAR_OPTION_ALWAYS
    };
    public static final OptionsUtil.StringChoices LOCATOR_BAR_PROFILE_CHOICES = new OptionsUtil.StringChoicesList(
        OptionsUtil.StringOption.of(MIXIN_LOCATOR_BAR_OPTION_NEVER, "Never")
            .withDesc("Icon is not modified."),
        OptionsUtil.StringOption.of(MIXIN_LOCATOR_BAR_OPTION_OVERRIDE_DEFAULT, "Override Default")
            .withDesc("Icon is modified only when existing icon is default."),
        OptionsUtil.StringOption.of(MIXIN_LOCATOR_BAR_OPTION_ALWAYS, "Always")
            .withDesc("Icon is always modified.")
    );
    public static boolean mixinLocatorBar = false;
    public static boolean mixinLocatorBarAlways = false;
    public static boolean mixinLocatorBarModeDefault() {
        return OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting().equals(MIXIN_LOCATOR_BAR_OPTION_NEVER);
    }
    public static boolean mixinLocatorBarModeUnknown() {
        for (String mode : MIXIN_LOCATOR_BAR_PROFILE_OPTIONS)
            if (OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting().equals(mode))
                return false;
        return true;
    }
    public static void mixinLocatorBarCycle() {
        int index=-1;
        for (int i=0; i < MIXIN_LOCATOR_BAR_PROFILE_OPTIONS.length; i++) {
            if (MIXIN_LOCATOR_BAR_PROFILE_OPTIONS[i].equals(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting())) {
                index = i;
                break;
            }
        }
        index++;
        if (index >= MIXIN_LOCATOR_BAR_PROFILE_OPTIONS.length)
            index = 0;
        OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.setSetting(MIXIN_LOCATOR_BAR_PROFILE_OPTIONS[index]);
    }
    public static void mixinLocatorBarRefresh() {
        mixinLocatorBar = false;
        mixinLocatorBarAlways = false;
        boolean found = false;
        for (String option : MIXIN_LOCATOR_BAR_PROFILE_OPTIONS) {
            if (option.equals(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting())) {
                found = true;
                break;
            }
        }
        if (!found) {
            String err = "Unknown option locator_bar_profile:"
                + BlackMagick.nbtToSnbt(StringTag.valueOf(OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting()))
                + " (expected one of: ";
            boolean first = true;
            for (String o : MIXIN_LOCATOR_BAR_PROFILE_OPTIONS) {
                if (!first) {
                    err += ", ";
                }
                else
                    first = false;
                err += BlackMagick.nbtToSnbt(StringTag.valueOf(o));
            }
            err += ")";
            FortytwoEdit.logWarn(err);
        }
        else if (OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting().equals(MIXIN_LOCATOR_BAR_OPTION_OVERRIDE_DEFAULT)) {
            mixinLocatorBar = true;
        }
        else if (OptionsUtil.ModOptions.LOCATOR_BAR_PROFILE.getSetting().equals(MIXIN_LOCATOR_BAR_OPTION_ALWAYS)) {
            mixinLocatorBar = true;
            mixinLocatorBarAlways = true;
        }
    }

    // zoom
    public static boolean zoomed = false;
    private static boolean smooth = false;

    // hacks
    private static boolean inWorld = false;
    private static final SecureRandom RNG = new SecureRandom();
    public static boolean autoMove = false;
    public static boolean autoClicker = false;
    public static boolean autoClick = true;
    public static boolean autoAttack = false;
    public static boolean autoMine = false;
    public static int attackWait = 1500;
    private static long afkReduceFramerateTime = 0;
    public static void toggleAutoClicker() {
        afkReduceFramerateTime = System.currentTimeMillis();
        autoClicker = !autoClicker;
    }
    public static boolean shouldReduceFramerate() {
        return autoClicker && OptionsUtil.ModOptions.AUTO_CLICK_LOCK.getSetting() && (System.currentTimeMillis() - afkReduceFramerateTime > 5000);
    }
    private static long lastAttack = 0;
    private static long lastSpam = 0;
    public static boolean xrayEntity = false;
    public static boolean autoFish = false;
    private static boolean autoFishClickQueue = false;
    private static long lastFish = 0;
    private static boolean didFish = false;
    private static final int fishWait = 1000;
    public static boolean suppressKeybind = false;
    public static void queueAutoFish() {
        autoFishClickQueue = true;
        lastFish = Math.max(lastFish, System.currentTimeMillis() + 100+randomInt(400) - fishWait);
    }
    private static double random() {
        return RNG.nextDouble();
    }
    public static double randomDouble(int range) {
        return random() * range;
    }
    public static int randomInt(int range) {
        return (int)randomDouble(range);
    }

    // item history
    private static final ListTag itemHistList = new ListTag();
    public static final int ITEM_HIST_ROWS = 6;
    public static void addItemHist(ItemStack item) {
        if (item != null && !item.isEmpty())
            addItemHist(BlackMagick.itemToNbtStorage(item));
    }
    public static void addItemHist(CompoundTag itemNbt) {
        if (itemNbt != null && !itemNbt.isEmpty() && itemNbt.getCompound("components").isPresent()) {
            CompoundTag item = itemNbt.copy();
            item.remove("count");

            int found = -1;

            for (int i = 0; i < itemHistList.size(); i++) {
                if (BlackMagick.elementsEqual(itemHistList.get(i), item)) {
                    found = i;
                    break;
                }
            }

            if (found < 0) {
                itemHistList.add(0, item.copy());
                if (itemHistList.size() > ITEM_HIST_ROWS * 9)
                    itemHistList.remove(itemHistList.size() - 1);
            }
            else {
                itemHistList.add(0,itemHistList.remove(found));
            }
        }
    }
    public static ListTag getItemHist() {
        return itemHistList.copy();
    }

    // xray mode
    public static boolean seeInvis = false;

    // randomizer mode
    public static int[] randoSlots;
    private static boolean randoMode = false;
    private static boolean shouldRandomizeSlot = false;
    public static boolean isRandoModeActive() {
        return randoMode && randoSlots != null;
    }
    public static boolean isRandoModeEnabled() {
        return randoMode;
    }
    public static void setRandoModeEnabled(boolean enabled) {
        randoMode = enabled;
    }
    public static void toggleRandoModeEnabled() {
        setRandoModeEnabled(!randoMode);
    }
    public static void randomizeSlot() {
        shouldRandomizeSlot = true;
    }

    // opticapes
    private static long lastCapeLoaded = System.currentTimeMillis();

    public static boolean capeTimeCheck() {
        if (System.currentTimeMillis() - lastCapeLoaded > 50) {
            lastCapeLoaded = System.currentTimeMillis();
            return true;
        }
        else
            return false;
    }

    public static boolean opticapesWorking = true; // if optifine connection is working

    private static void checkCapesEnabled() {
        opticapesWorking = true;

        if (OptionsUtil.ModOptions.OPTICAPES.getSetting()) {
            boolean connect = false;

            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection)(new URI("http://s.optifine.net/capes/42Richtofen42.png")).toURL().openConnection();
                con.setConnectTimeout(2000);
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
                    connect = true;
                con.disconnect();
            } catch (Exception ex) {}
            if (con != null)
                try {
                    con.disconnect();
                } catch (Exception ex) {}

            if (!connect) {
                opticapesWorking = false;
                logWarn("Failed connection to OptiFine capes");
            }
        }
    }

    private static List<String> capeNames = Lists.newArrayList(); // all cached names
    private static List<String> capeNames2 = Lists.newArrayList(); // names with capes

    public static int debugCapeNamesSize() {
        return capeNames.size();
    }

    public static int debugCapeNames2Size() {
        return capeNames2.size();
    }

    public static void clearOptiCapes() {
        capeNames.clear();
        final Minecraft client = Minecraft.getInstance();
        for (String name : capeNames2) {
            client.getTextureManager().release(getCapeCacheID(name, true));
        }
        capeNames2.clear();
        checkCapesEnabled();
    }

    public static boolean nameCached(String name) {
        return capeNames.contains(name);
    }

    public static boolean capeCached(String name) {
        return capeNames2.contains(name);
    }

    public static boolean tryLoadCape(String name) {
        if (name == null || name.isEmpty())
            return false;

        final Minecraft client = Minecraft.getInstance();
        if (capeNames.isEmpty() && !name.equals(client.getUser().getName()))
            tryLoadCape(client.getUser().getName());
        capeNames.add(name);

        HttpURLConnection con = null;
        InputStream stream = null;
        NativeImage capeInp = null;
        NativeImage cape = null;
        try {
            URL link = new URI("http://s.optifine.net/capes/" + name + ".png").toURL();
            con = (HttpURLConnection)link.openConnection();
            con.setUseCaches(false);
            con.setConnectTimeout(500);
            con.setReadTimeout(500);
            stream = con.getInputStream();
            capeInp = NativeImage.read(stream);
            stream.close();
            con.disconnect();
            cape = new NativeImage(128, 64, true);

            for (int x = 0; x < capeInp.getWidth(); x++)
                for (int y = 0; y < capeInp.getHeight(); y++)
                    cape.setPixel(x, y, capeInp.getPixel(x, y));

            capeInp.close();
            client.getTextureManager().register(getCapeCacheID(name, true), new DynamicTexture(getCapeCacheID(name, false)::toString, cape));
            cape.close();
            capeNames2.add(name);
            return true;
        } catch (Exception ex) {}
        if (con != null)
            try {
                con.disconnect();
            } catch (Exception ex) {}
        if (stream != null)
            try {
                stream.close();
            } catch (Exception ex) {}
        if (capeInp != null)
            capeInp.close();
        if (cape != null)
            cape.close();

        return false;
    }
    private static Identifier getCapeCacheID(String name, boolean full) {
        String path = "cache/cape/" + name.toLowerCase();
        if (full)
            path = getTexturesFullPath(path);
        return Identifier.fromNamespaceAndPath(MOD_ID_MC, path);
    }

    // custom capes
    public static final List<CapeTexture> CLIENT_CAPES = Lists.newArrayList();
    private static void resetClientCapes() {
        CLIENT_CAPES.clear();
        CAPE_URLS.clear();
        CAPE_REGISTERED_IDS.clear();
        CAPE_MAP.clear();
        warnedCapeCache = null;

        registerCape(new CapeTexture(CapeTextureStatus.NONE, "none", "No Cape", null), null);

        registerCustomCape("spartan", "Spartan", "From the Battle & Beasts Skin Pack");
        registerCustomCape("christmas", "Christmas", "Temporarily shown around Christmas of 2010");
        registerCustomCape("42banner", "42cape", "OptiFine cape of 42Richtofen42"); // edit http://s.optifine.net/capes/42Richtofen42.png to 128x64
    };
    public record CapeTexture(CapeTextureStatus status, String id, String name, String desc) {
        public static CapeTexture newCustom(String id, String name, String desc) {
            return new CapeTexture(CapeTextureStatus.CUSTOM, id, name, desc);
        }
        public static CapeTexture newUrl(String id, String name, String desc) {
            return new CapeTexture(CapeTextureStatus.URL, id, name, desc);
        }
    }
    public enum CapeTextureStatus {
        NONE,
        CUSTOM,
        URL,
        UNKNOWN
    }
    private static String warnedCapeCache = null;
    private static final Set<String> CAPE_URLS = Sets.newHashSet();
    private static final Set<String> CAPE_URLS_QUEUE = Sets.newHashSet();
    private static final Map<String,String> CAPE_URLS_QUEUE_MAP = Maps.newHashMap();
    private static final Set<String> CAPE_REGISTERED_IDS = Sets.newHashSet();
    public static Identifier capeIdentifier(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID_MC, "cape/"+id);
    }
    private static void registerCustomCape(String id, String name, String desc) {
        registerCape(CapeTexture.newCustom(id, name, desc), new ClientAsset.ResourceTexture(capeIdentifier(id)));
    }
    private static void registerCape(CapeTexture cape, ClientAsset.Texture texture) {
        CLIENT_CAPES.add(cape);
        CAPE_MAP.put(cape.id(), texture);
        CAPE_ID_TO_TEXTURE.put(cape.id(), getTexturesFullPath(capeIdentifier(cape.id())));
    }
    /**
     * see {@link net.minecraft.client.resources.SkinManager#registerTextures}
     */
    private static void registerCapeUrl(String url, String id, String name, String desc) {
        if (url == null)
            return;
        if (!CAPE_URLS.contains(url)) {
            CAPE_URLS.add(url);
            CAPE_URLS_QUEUE.add(url);
            CAPE_URLS_QUEUE_MAP.put(url, id);
            CAPE_REGISTERED_IDS.add(id);
            CLIENT_CAPES.add(CapeTexture.newUrl(id, name, desc));
        }
    }
    public static void resolveCapeUrlQueue() {
        if (!CAPE_URLS_QUEUE.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.getSkinManager() != null && client.getUser() != null && client.getUser().getProfileId() != null) {
                for (String url : CAPE_URLS_QUEUE) {
                    try {
                        CompletableFuture<PlayerSkin> futurePlayerSkin = ((SkinManagerInvoker)client.getSkinManager())
                            .invokeRegisterTextures(new UUID(0,0), new MinecraftProfileTextures(null,
                            new MinecraftProfileTexture(url, null), null, SignatureState.SIGNED));
                            futurePlayerSkin.thenAccept(playerSkin -> {
                                if (CAPE_URLS_QUEUE_MAP.containsKey(url) && playerSkin != null && playerSkin.cape() != null) {
                                    String capeId = CAPE_URLS_QUEUE_MAP.get(url);
                                    CAPE_MAP.put(capeId, playerSkin.cape());
                                    CAPE_ID_TO_TEXTURE.put(capeId, playerSkin.cape().texturePath());
                                }
                                else
                                    FortytwoEdit.logWarn("Failed to load cape id for: "+url);
                            }
                        );
                    }
                    catch (Exception ex) {
                        FortytwoEdit.logWarn("Failed to load cape texture for: "+url);
                    }
                }
                CAPE_URLS_QUEUE.clear();
            }
        }
        else if (!CAPE_REGISTERED_IDS.contains(OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting())
        && (warnedCapeCache == null || !warnedCapeCache.equals(OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting()))) {
            warnedCapeCache = OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting();
            FortytwoEdit.logWarn("Failed to find custom cape with ID: "+OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting());
        }
    }
    private static final Map<String,ClientAsset.Texture> CAPE_MAP = Maps.newHashMap();
    public static final Map<String,Identifier> CAPE_ID_TO_TEXTURE = Maps.newHashMap();
    public static ClientAsset.Texture getClientCapeTexture() {
        if (CAPE_MAP.containsKey(OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting()))
            return CAPE_MAP.get(OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting());
        resolveCapeUrlQueue();
        return null;
    }
    public static CapeTexture getCurrentClientCape() {
        for (CapeTexture c : CLIENT_CAPES)
            if (c.id().equals(OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting())) {
                return c;
            }
        return new CapeTexture(CapeTextureStatus.UNKNOWN,
            OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting(),
            OptionsUtil.ModOptions.CUSTOM_CAPE.getSetting(),
            null);
    }

    public static String USERNAME = "";
    public static IntArrayTag UUID = new IntArrayTag(new int[]{0,0,0,0});
    public static String[] PROFILE_SUGGS = null;

    // skin testing
    public static boolean showClientSkin = false;
    public static boolean clientSkinSlim = false;
    public static String customSkinName = "";
    private static final String CUSTOM_SKIN_PATH = "cache/custom_skin";
    private static final Identifier CUSTOM_SKIN_ID = Identifier.fromNamespaceAndPath(MOD_ID_MC,CUSTOM_SKIN_PATH);
    private static final Identifier CUSTOM_SKIN_ID_FULL = Identifier.fromNamespaceAndPath(MOD_ID_MC,getTexturesFullPath(CUSTOM_SKIN_PATH));
    public static final ClientAsset.Texture CUSTOM_SKIN_TEXTURE = new ClientAsset.ResourceTexture(CUSTOM_SKIN_ID);

    public static String getTexturesFullPath(String path) {
        return "textures/" + path + ".png";
    }
    public static Identifier getTexturesFullPath(Identifier identifier) {
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), getTexturesFullPath(identifier.getPath()));
    }

    public static boolean setCustomSkin(File file) {
        final Minecraft client = Minecraft.getInstance();
        customSkinName = "";
        showClientSkin = false;
        client.getTextureManager().release(CUSTOM_SKIN_ID_FULL);

        if (file != null) {
            FileInputStream inp = null;
            NativeImage skinFile = null;
            NativeImage skin = null;
            if (file.isFile() && file.getName().endsWith(".png")) {
                try {
                    inp = new FileInputStream(file);
                    skinFile = NativeImage.read(inp);
                    inp.close();
                    skin = new NativeImage(skinFile.getWidth(),skinFile.getHeight(),true);

                    for (int x = 0; x < skinFile.getWidth(); x++)
                        for (int y = 0; y < skinFile.getHeight(); y++)
                            skin.setPixel(x, y, skinFile.getPixel(x, y));

                    skinFile.close();
                    customSkinName = file.getName();
                    client.getTextureManager().register(CUSTOM_SKIN_ID_FULL, new DynamicTexture(CUSTOM_SKIN_ID::toString, skin));
                    skin.close();
                    showClientSkin = true;
                    return true;
                } catch (Exception ex) {}
            }
            if (inp != null)
                try {
                    inp.close();
                } catch (Exception ex) {}
            if (skinFile != null)
                skinFile.close();
            if (skin != null)
                skin.close();

            logWarn("Failed to set custom skin file");
        }

        return false;
    }

    private static final String[] MOD_ASSETS_TEXTURES = new String[]{ // to_do replace with real solution
        "cape/42banner",
        "cape/christmas",
        "cape/spartan",

        "gui/generic",
        "gui/menu_bar",

        "icon/mod",
        "icon/mycelium"
    };
    private static final String[] MOD_LANGUAGES = new String[]{
        "en_us",
        "en_pt",
        "en_ud"
    };

    public static void loadAllModAssets() {
        final Minecraft client = Minecraft.getInstance();
        for (String path : MOD_ASSETS_TEXTURES)
            loadAssetsTexture(client, path);
    }

    public static void loadAssetsTexture(Minecraft client, String path) {
        try {
            NativeImage texture = NativeImage.read(client.getClass().getClassLoader().getResourceAsStream("assets/"+MOD_ID_MC+"/textures/"+path+".png"));
            Identifier id = Identifier.fromNamespaceAndPath(MOD_ID_MC,path);
            Identifier idFull = Identifier.fromNamespaceAndPath(MOD_ID_MC,getTexturesFullPath(path));
            client.getTextureManager().release(idFull);
            client.getTextureManager().register(idFull,new DynamicTexture(id::toString,texture));
        }
        catch (Exception ex) {
            FortytwoEdit.logError("Failed to load mod texture: "+path);
        }
    }

    public static InputStream getAssetsLang(String lang) {
        boolean found = false;
        for (String s : MOD_LANGUAGES) {
            if (s.equals(lang))
                found = true;
        }
        if (found) {
            final Minecraft client = Minecraft.getInstance();
            try {
                return client.getClass().getClassLoader().getResourceAsStream("assets/"+MOD_ID_MC+"/lang/"+lang+".json");
            }
            catch (Exception ex) {
                FortytwoEdit.logError("Failed to load mod language: "+lang);
            }
        }
        return null;
    }

    // freelook
    public static boolean isFreeLooking = false;
    private static CameraType lastPerspective;
    public static float[] cameraRotation = {0f,0f};

    // see feature items
    public static final FeatureFlagSet FEATURES = FeatureFlagSet.of(FeatureFlags.VANILLA);

    // supersecretsettings
    public static final Identifier[] SUPER_SECRET_SETTING_PROGRAMS = new Identifier[]{/*Identifier.of("42edit","shaders/post/notch.json"), Identifier.of("42edit","shaders/post/fxaa.json"), Identifier.of("42edit","shaders/post/art.json"), Identifier.of("42edit","shaders/post/bumpy.json"), Identifier.of("42edit","shaders/post/blobs2.json"), Identifier.of("42edit","shaders/post/pencil.json"), Identifier.of("42edit","shaders/post/color_convolve.json"), Identifier.of("42edit","shaders/post/deconverge.json"), Identifier.of("42edit","shaders/post/flip.json"),*/ Identifier.withDefaultNamespace("invert"),/* Identifier.of("42edit","shaders/post/ntsc.json"), Identifier.of("42edit","shaders/post/outline.json"), Identifier.of("42edit","shaders/post/phosphor.json"), Identifier.of("42edit","shaders/post/scan_pincushion.json"), Identifier.of("42edit","shaders/post/sobel.json"), Identifier.of("42edit","shaders/post/bits.json"), Identifier.of("42edit","shaders/post/desaturate.json"), Identifier.of("42edit","shaders/post/green.json"), Identifier.of("42edit","shaders/post/blur.json"), Identifier.of("42edit","shaders/post/wobble.json"), Identifier.of("42edit","shaders/post/blobs.json"), Identifier.of("42edit","shaders/post/antialias.json"),*/ Identifier.withDefaultNamespace("creeper"), Identifier.withDefaultNamespace("spider")};
    private static int superSecretSettingIndex = SUPER_SECRET_SETTING_PROGRAMS.length;
    private static final Identifier[] SECRETSOUNDS = getSecretSounds();
    private static Identifier[] getSecretSounds() {
        Set<Identifier> sounds = BuiltInRegistries.SOUND_EVENT.keySet();
        List<Identifier> valid = Lists.newArrayList();
        for (Identifier sound: sounds) {
            if (sound.getPath().contains("entity.") || sound.getPath().contains("block.") || sound.getPath().contains("weather.") || sound.getPath().contains("item."))
                valid.add(sound);
        }
        return valid.toArray(new Identifier[0]);
    }
    private static void secretSound() {
        if (SECRETSOUNDS != null && SECRETSOUNDS.length > 0) {
            int i = randomInt(SECRETSOUNDS.length);
            BlackMagick.playClientSound(SECRETSOUNDS[i], 1f, .5f);
        }
    }
    public static void cycleSuperSecretSetting() {
        final Minecraft client = Minecraft.getInstance();
        if (client.getCameraEntity() instanceof Player) {
            if (client.gameRenderer.currentPostEffect() != null) {
                client.gameRenderer.clearPostEffect();
            }
            superSecretSettingIndex = (superSecretSettingIndex + 1) % (SUPER_SECRET_SETTING_PROGRAMS.length + 1);
            if (superSecretSettingIndex == SUPER_SECRET_SETTING_PROGRAMS.length) {
                ((GameRendererInvoker)client.gameRenderer).setEffectActive(false);
            } else {
                ((GameRendererInvoker)client.gameRenderer).invokeSetPostEffect(SUPER_SECRET_SETTING_PROGRAMS[superSecretSettingIndex]);
            }
        }
        secretSound();
    }

    // common
    public static final ItemStackPreset HEAD42 = ItemStackPreset.set("{id:player_head,components:{profile:{name:\"42Richtofen42\","
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlc"
        +"yIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDliNjliNWU3MzVlYjUyMmIyNGM2OTczNTQ5ZGRhODMzYjE1ZD"
        +"kxYjg3NDM1NjRjZmIxN2QwZjk2MWMwZjU0Ig0KICAgIH0NCiAgfQ0KfQ==\"}]}}}");
    public static final ItemStackPreset BANNER42 = ItemStackPreset.set("{id:red_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:curly_border\"}"
        +",{color:\"black\",pattern:\"minecraft:bricks\"},{color:\"red\",pattern:\"minecraft:triangle_bottom\"},{color:\"black\",pattern:\"minecraft:triangle_bottom\"}"
        +",{color:\"purple\",pattern:\"minecraft:flower\"},{color:\"black\",pattern:\"minecraft:gradient\"}]}}");
    public static final ItemStackPreset BANNERBRICK = ItemStackPreset.set("{id:orange_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:gradient\"}"
        +",{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"orange\",pattern:\"minecraft:triangles_bottom\"}"
        +",{color:\"red\",pattern:\"minecraft:bricks\"},{color:\"green\",pattern:\"minecraft:creeper\"}]}}");
    public static final ItemStackPreset ITEM_ERROR = ItemStackPreset.set("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcy"
        +"IgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZTE4MjM2NzExOTYzMTMxNzY5MjM0Mzc4OGNkNWM4NTRjMTNiNDQ5"
        +"ZDM2ZTYyMmI4NTU0YTU2MzhlZDM4NTkzIg0KICAgIH0NCiAgfQ0KfQ==\"}]}}}");
    public static final ItemStackPreset ITEM_QUESTION = ItemStackPreset.set("{components:{\"minecraft:profile\":"
        +"{id:[I;1617833968,-310949822,-1653808685,840726584],name:\"MHF_Question\",properties:[{name:\"textures\",value:\"ewogICJzaWduYXR1cmVSZXF1aXJlZCIg"
        +"OiBmYWxzZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzNGUwNjNjYWZiNDY3Y"
        +"TVjOGRlNDNlYzc4NjE5Mzk5ZjM2OWY0YTUyNDM0ZGE4MDE3YTk4M2NkZDkyNTE2YTAiCiAgICB9CiAgfQp9\"}]}},count:1,id:\"minecraft:player_head\"}");

    public static final CompoundTag LORE_BAPHOMETHLABS = BlackMagick.validCompoundFromString("{color:\"gold\",\"text\":\"BaphomethLabs\"}");
    public static final CompoundTag LORE_BAPHOMETHLABS_BOTTLE = BlackMagick.validCompoundFromString("{color:\"gold\",\"text\":\"Bottled by BaphomethLabs\"}");

    // saved items
    public static final int SAVED_ROWS = 12;

    // web items
    public static List<String> webItems = Lists.newArrayList();
    private static final String WEB_ITEMS_URL_DEFAULT = "https://baphomet42.github.io/mc/blackmarket/items.json";
    private static String webItemsUrlOverride = "";

    @Override
    public void onInitializeClient() {}

    public static void onMinecraftInit() {
        logInfo("42edit init started");

        final Minecraft client = Minecraft.getInstance();

        OptionsUtil.readOptions();

        USERNAME = client.getUser().getName();
        if (client.getUser().getProfileId() != null)
            UUID = new IntArrayTag(UUIDUtil.uuidToIntArray(client.getUser().getProfileId()));
        PROFILE_SUGGS = new String[]{USERNAME,BlackMagick.nbtToSnbt(UUID)};
        clearOptiCapes();

        getSavedItems(); // used to show log errors
        refreshWebItems();

        FileTools.scanModFiles();

        loadAllModAssets();

        logInfo("42edit init finished");
    }

    public static void clientTick(Minecraft client) {

        if (client.player == null || (client.gui != null && client.gui.screen() != null)) {
            autoClicker = false;
            autoMove = false;
        }

        if (inWorld && client.player == null) {
            inWorld = false;
            CHAT_ICON_COMPONENT_CACHE.clear();
            CHAT_ICON_MESSAGE_CACHE.clear();
            CHAT_ICON_KEY_LIST.clear();
            CHAT_ICON_KEY_SET.clear();
        }
        else if (!inWorld && client.player != null) {
            inWorld = true;
        }

        if (shouldRandomizeSlot) {
            shouldRandomizeSlot = false;
            if (randoSlots != null) {
                boolean testRandoSlot = false;
                int selected = client.player.getInventory().getSelectedSlot() + 1;
                for (int i = 0; i < randoSlots.length; i++) {
                    if (randoSlots[i] == selected) {
                        testRandoSlot = true;
                        break;
                    }
                }
                if (testRandoSlot) {
                    int slot = randomInt(randoSlots.length);
                    slot = randoSlots[slot];
                    client.player.getInventory().setSelectedSlot(slot - 1);
                }
            }
        }

        // magickgui
        if (OptionsUtil.Keybinds.KEY_OPEN_MAGICK_GUI.consumeClick())
            client.gui.setScreen(quickScreen.get());

        // zoom
        if (OptionsUtil.Keybinds.KEY_ZOOM.isDown() && !zoomed) {
            smooth = client.options.smoothCamera;
            client.options.smoothCamera = true;
            zoomed = true;
        }
        else if (!OptionsUtil.Keybinds.KEY_ZOOM.isDown() && zoomed) {
            client.options.smoothCamera = smooth;
            zoomed = false;
        }

        // afkMove
        if (OptionsUtil.Keybinds.KEY_AFK_MOVE.consumeClick()) {
            autoMove = !autoMove;
            client.options.keyUp.setDown(false);
            while (client.options.keyUp.consumeClick()) {}
        }
        if (autoMove && client.player != null) {
            if (client.options.keyUp.consumeClick()) {
                autoMove = false;
                client.options.keyUp.setDown(false);
                while (client.options.keyUp.consumeClick()) {}
            }
            else
                client.options.keyUp.setDown(true);
        }

        // afkClick
        if (OptionsUtil.Keybinds.KEY_AFK_CLICK.consumeClick()) {
            toggleAutoClicker();
            client.options.keyUse.setDown(false);
            client.options.keyAttack.setDown(false);
            while (client.options.keyUse.consumeClick()) {}
            while (client.options.keyAttack.consumeClick()) {}
        }
        if (autoClicker && client.player != null) {
            if (autoClick) {
                client.options.keyUse.setDown(true);
            }
            if (autoAttack && System.currentTimeMillis() >= lastAttack + attackWait && client.hitResult instanceof EntityHitResult) {
                lastAttack = System.currentTimeMillis();
                suppressKeybind = true;
                KeyMapping.click(((KeyMappingAccessor)client.options.keyAttack).getBoundKey());
                suppressKeybind = false;
            }
            if (autoMine) {
                client.options.keyAttack.setDown(true);
            }
        }
        if (autoFishClickQueue && System.currentTimeMillis() >= (lastFish + fishWait)) {
            if (autoClicker && autoFish && ((!client.player.getMainHandItem().isEmpty()
                    && client.player.getMainHandItem().is(Items.FISHING_ROD)) || (client.player.getMainHandItem().isEmpty()
                    && !client.player.getOffhandItem().isEmpty() && client.player.getOffhandItem().is(Items.FISHING_ROD))) ) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
                didFish = true;
            }
            autoFishClickQueue = false;
            lastFish = System.currentTimeMillis() + 100+randomInt(400);
        }
        if (didFish && System.currentTimeMillis() >= (lastFish + fishWait)) {
            if (autoClicker && autoFish && ((!client.player.getMainHandItem().isEmpty()
                    && client.player.getMainHandItem().is(Items.FISHING_ROD)) || (client.player.getMainHandItem().isEmpty()
                    && !client.player.getOffhandItem().isEmpty() && client.player.getOffhandItem().is(Items.FISHING_ROD))) ) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
            }
            didFish = false;
            lastFish = System.currentTimeMillis() + 100 + randomInt(400);
        }

        // freelook
        if (OptionsUtil.Keybinds.KEY_FREE_LOOK.isDown()) {
            if (!isFreeLooking) {
                lastPerspective = client.options.getCameraType();
                Entity view = client.getCameraEntity() == null ? client.player : client.getCameraEntity();
                cameraRotation[0] = view.getYRot();
                cameraRotation[1] = view.getXRot();

                if (lastPerspective == CameraType.FIRST_PERSON)
                    client.options.setCameraType(CameraType.THIRD_PERSON_BACK);

                isFreeLooking = true;
            }
        }
        else if (isFreeLooking) {
            isFreeLooking = false;
            client.options.setCameraType(lastPerspective);
        }

        // spam
        if (OptionsUtil.Keybinds.KEY_SPAM_CLICK.isDown() && System.currentTimeMillis() >= lastSpam + 20) {
            if (OptionsUtil.Keybinds.KEY_KEY_MOD.isDown())
                KeyMapping.click(((KeyMappingAccessor)client.options.keyAttack).getBoundKey());
            else {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
            }
            lastSpam = System.currentTimeMillis();
        }

    }

    public static void updateAutoClick(boolean click, boolean mine, boolean attack, boolean fish, int wait) {
        final Minecraft client = Minecraft.getInstance();
        autoClicker = false;

        autoClick = click;
        autoMine = mine;
        autoAttack = attack;
        attackWait = wait;
        autoFish = fish;
        if (wait < 1)
            attackWait = 1;
        else if (wait > 9999)
            attackWait = 9999;

        client.options.keyUse.setDown(false);
        client.options.keyAttack.setDown(false);
    }

    public static ItemStack copyLookAt() {
        final Minecraft client = Minecraft.getInstance();
        HitResult hitResult = client.hitResult;
        if (hitResult == null) {
            return null;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = client.player.level().getBlockState(blockPos);

            if (!blockState.getProperties().isEmpty()) {
                CompoundTag stack = new CompoundTag();
                stack.put("id",StringTag.valueOf(BlackMagick.itemToStringId(blockState.getBlock().asItem())));
                CompoundTag tag = new CompoundTag();

                String states = "";
                states += "{";
                boolean bl = false;
                for (Property<?> entry : blockState.getProperties()) {
                    if (bl) {
                        states += ",";
                    }
                    states += entry.getName();
                    states += ":";
                    states += "\""+getValueString(blockState,entry)+"\"";
                    bl = true;
                }
                states += "}";

                tag.put("block_state",BlackMagick.nbtFromSnbt(states));
                stack.put("components",tag);
                return BlackMagick.itemFromNbt(stack);
            }
        }
        return null;
    }

    private static <T extends Comparable<T>> String getValueString(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    public static void setClipboard(String text) {
        final Minecraft client = Minecraft.getInstance();
        client.keyboardHandler.setClipboard(text);
    }

    public static String getClipboard() {
        final Minecraft client = Minecraft.getInstance();
        return client.keyboardHandler.getClipboard();
    }

    private static final String LOG_PREFIX = "("+MOD_ID_MC+") ";
    private static final SystemToast.SystemToastId TOAST_TYPE = new SystemToast.SystemToastId();
    private static final MutableComponent TOAST_PREFIX = Component.empty().append("").append(Component.empty().append(LOG_PREFIX).withStyle(ChatFormatting.BLACK));

    public static void logInfo(String info) {
        LOGGER.info(LOG_PREFIX + info);
        LogScreen.logModLog(LogScreen.LogType.INFO, info);
    }

    public static void logWarn(String warn) {
        LOGGER.warn(LOG_PREFIX + warn);
        LogScreen.logModLog(LogScreen.LogType.WARN, warn);
    }

    public static void logError(String error) {
        LOGGER.error(LOG_PREFIX + error);
        LogScreen.logModLog(LogScreen.LogType.ERROR, error);
    }

    public static void showToast(String title, String desc) {
        showToast(Component.nullToEmpty(title),Component.nullToEmpty(desc));
    }

    public static void showToast(Component title, Component desc) {
        final Minecraft client = Minecraft.getInstance();
        try {
            client.gui.toastManager().addToast(new SystemToast(TOAST_TYPE, TOAST_PREFIX.copy().append(title), desc));
        }
        catch (Exception ex) {
            logError("Failed to show toast ("+BlackMagick.textComponentToStringLiteral(title)+") ("+BlackMagick.textComponentToStringLiteral(desc)+"): "+ex.getMessage());
        }
    }

    public static void showToastCreativeError() {
        showToast("Invalid Permissions", "Creative mode required");
    }

    /**
     * Refresh a wide variety of features. Can be used for debug reasons, to fetch new web items, etc.
     */
    public static void debugTryRefreshVarious() {
        logInfo("Starting debug...");

        OptionsUtil.readOptions();
        refreshWebItems();

        final Minecraft client = Minecraft.getInstance();
        ((HotbarManagerAccessor)client.getHotbarManager()).setLoaded(false);
        client.getHotbarManager().get(0);

        getSavedItems(); // used to show log errors

        SuggestionHelper.runAllListMethods();

        clearOptiCapes();
        setCustomSkin(null);

        itemHistList.clear();

        // CHAT_ICON_COMPONENT_CACHE.clear(); do not clear here
        CHAT_ICON_MESSAGE_CACHE.clear();
        // CHAT_ICON_KEY_LIST.clear(); do not clear here
        // CHAT_ICON_KEY_SET.clear(); do not clear here

        FileTools.scanModFiles();

        LogScreen.debugTryRefreshVarious();

        logInfo("Debug complete");
    }

    public static Map<Integer,String> getSavedItems() {
        Map<Integer,String> itemsMap = Maps.newHashMap();
        ItemBuilderScreen.savedItemsError = false;
        CompoundTag savedItemsNbt = FileTools.readCompoundFromFile(FileTools.FILE_SAVED_ITEMS);
        if (savedItemsNbt == null) {
            savedItemsNbt = new CompoundTag();
            if (FileTools.testFileExists(FileTools.FILE_SAVED_ITEMS)) {
                String fileString = FileTools.readStringFromFile(FileTools.FILE_SAVED_ITEMS);
                if (fileString != null && !fileString.isEmpty()) {
                    ItemBuilderScreen.savedItemsError = true;
                }
            }
            else {
                setSavedItems(itemsMap);
            }
        }

        boolean foundItems = false;
        if (savedItemsNbt.getList("items").isPresent()) {
            ListTag storedItems = savedItemsNbt.getList("items").get();
            foundItems = true;
            if (!storedItems.isEmpty()) {
                boolean itemsOutOfRange = false;
                final int MAX_SAVED_ITEM_SLOT = FortytwoEdit.SAVED_ROWS * 9 - 1;
                int currentDupeSlot = MAX_SAVED_ITEM_SLOT+1;
                ListTag unknownItemHolders = new ListTag();
                for (int i = 0; i < storedItems.size(); i++) {
                    if (storedItems.get(i).getId() == Tag.TAG_COMPOUND) {
                        CompoundTag itemHolder = storedItems.getCompound(i).get();
                        if (itemHolder.isEmpty())
                            continue;
                        if ((itemHolder.getCompound("item").isPresent() || itemHolder.getString("item").isPresent())
                        && itemHolder.getInt("slot").isPresent()) {
                            String itemString = itemHolder.getStringOr("item","");
                            if (itemHolder.get("item").getId()==Tag.TAG_COMPOUND)
                                itemString = BlackMagick.nbtToSnbt(itemHolder.getCompound("item").get());
                            itemHolder.remove("item");
                            if (itemString.isEmpty() || itemString.equals("{}"))
                                continue;
                            int slot = itemHolder.getInt("slot").get();
                            itemHolder.remove("slot");
                            if (!itemHolder.isEmpty()) {
                                FortytwoEdit.logError("Saved item contains unknown keys: "+BlackMagick.nbtToSnbt(itemHolder));
                            }
                            if (slot < 0 || slot > MAX_SAVED_ITEM_SLOT) {
                                itemsOutOfRange = true;
                            }
                            if (itemsMap.containsKey(slot)) {
                                int newSlot = currentDupeSlot;
                                while (itemsMap.containsKey(newSlot)) {
                                    newSlot++;
                                }
                                currentDupeSlot = newSlot+1;
                                FortytwoEdit.logError("Saved items file contains duplicate slot " + slot + ". Item will move to slot " + newSlot + " after saving.");
                                slot = newSlot;
                            }
                            itemsMap.put(slot,itemString);
                        }
                        else
                            unknownItemHolders.add(itemHolder);
                    }
                    else
                        unknownItemHolders.add(storedItems.get(i));
                }
                if (itemsOutOfRange) {
                    FortytwoEdit.logWarn("Saved items file contains slots outside of range 0-"+MAX_SAVED_ITEM_SLOT);
                }
                if (!unknownItemHolders.isEmpty()) {
                    FortytwoEdit.logError("Saved items file contains invalid entries. After saving, the following will be deleted: "+BlackMagick.nbtToSnbt(unknownItemHolders));
                }
            }
        }
        if (!foundItems && !savedItemsNbt.isEmpty()) {
            logError("Failed to read saved items: " + BlackMagick.nbtToSnbt(savedItemsNbt));
            ItemBuilderScreen.savedItemsError = true;
        }

        return itemsMap;
    }

    public static boolean setSavedItems(Map<Integer,String> savedItemsMap) {
        ListTag itemsList = new ListTag();
        for (int slot : BlackMagick.sortIntSet(savedItemsMap.keySet())) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("slot",slot);
            nbt.putString("item",savedItemsMap.get(slot));
            itemsList.add(nbt);
        }

        CompoundTag savedItemsNbt = new CompoundTag();
        savedItemsNbt.put("items",itemsList);
        savedItemsNbt.putInt(FileTools.FILE_FORMAT_LABEL,FileTools.FILE_FORMAT);
        if (FileTools.writeCompoundToFile(FileTools.FILE_SAVED_ITEMS, savedItemsNbt, FileDisplayType.TREE_CONDITIONAL_COLLAPSE)) {
            getSavedItems(); // used to show log errors
            return true;
        }
        return false;
    }

    public static boolean testSavedItems(Map<Integer,String> oldMap) {
        Map<Integer,String> newMap = getSavedItems();
        if (newMap.keySet().size()==oldMap.keySet().size()) {
            for (int slot : oldMap.keySet()) {
                if (!(newMap.containsKey(slot) && newMap.get(slot).equals(oldMap.get(slot))))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @return compound with keys to mark results (site_match_catch, site_updated_catch)
     */
    public static CompoundTag refreshWebItems() {
        webItems.clear();
        CompoundTag result = new CompoundTag();

        CompoundTag cacheNbt = FileTools.readCompoundFromFile(FileTools.FILE_WEB_CACHE);
        if (cacheNbt == null)
            cacheNbt = new CompoundTag();
        CompoundTag newItems = cacheNbt.copy();

        {
            String webItemsUrlActive = webItemsUrlOverride.length() > 0 ? webItemsUrlOverride : WEB_ITEMS_URL_DEFAULT;
            String webJson = "";
            boolean didError = false;

            HttpURLConnection con = null;
            InputStream stream = null;
            try {
                con = (HttpURLConnection)(new URI(webItemsUrlActive)).toURL().openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(500);
                con.setUseCaches(false);
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = con.getInputStream();
                    webJson = new String(stream.readAllBytes(), FileTools.FILE_CHARSET);
                    stream.close();
                    con.disconnect();
                }
            }
            catch (Exception ex) {
                logWarn("Failed connection to BaphomethLabs Black Market ("+webItemsUrlActive+")");
                didError = true;
            }
            if (con != null)
                try {
                    con.disconnect();
                } catch (Exception ex) {}
            if (stream != null)
                try {
                    stream.close();
                } catch (Exception ex) {}

            Tag parseWebJson = BlackMagick.nbtFromSnbt(webJson);
            if (parseWebJson != null && parseWebJson.getId()==Tag.TAG_COMPOUND) {
                CompoundTag webNbt = (CompoundTag)parseWebJson;
                newItems = webNbt.copy();

                if (BlackMagick.elementsEqual(webNbt,cacheNbt)) {
                    logInfo("Black Market items are up to date");
                    result.put("site_match_catch",new CompoundTag());
                }
                else {
                    logInfo("Updating Black Market items");
                    result.put("site_updated_catch",new CompoundTag());
                    FileTools.writeCompoundToFile(FileTools.FILE_WEB_CACHE, newItems, FileDisplayType.TREE_CONDITIONAL_COLLAPSE);
                }
            }
            else if (!didError)
                logError("Failed to parse BaphomethLabs Black Market ("+webItemsUrlActive+"): "+webJson);
        }

        if (newItems != null && newItems.getList("versions").isPresent() && !newItems.getList("versions").get().isEmpty()) {

            ListTag versionsList = newItems.getList("versions").get();
            int itemsVer = -1;
            int itemsVerMinor = 0;
            ListTag jsonItems = null;

            for (int i = 0; i < versionsList.size(); i++) {
                if (versionsList.get(i).getId() == Tag.TAG_COMPOUND) {
                    CompoundTag versionData = versionsList.getCompound(i).get();
                    if (versionData.getInt("version").isPresent() && versionData.getList("items").isPresent()) {
                        int versionNum = versionData.getInt("version").get();
                        int versionMinor = versionData.getIntOr("version_minor", 0);
                        if (itemsVer == -1 ||
                            (
                                (versionNum > itemsVer || (versionNum == itemsVer && versionMinor > itemsVerMinor))
                                && versionNum <= SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major()
                                && versionMinor <= SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minor()
                            )
                        ) {
                            itemsVer = versionNum;
                            itemsVerMinor = versionMinor;
                            jsonItems = versionData.getList("items").get();
                        }
                    }
                }
            }

            if (jsonItems != null && !jsonItems.isEmpty()) {
                for (int i = 0; i < jsonItems.size(); i++) {
                    if (jsonItems.get(i).getId() == Tag.TAG_COMPOUND) {
                        CompoundTag itemHolder = jsonItems.getCompound(i).get();
                        if (itemHolder.getCompound("item").isPresent() || itemHolder.getString("item").isPresent()) {
                            String itemString = itemHolder.getStringOr("item","");
                            if (itemHolder.get("item").getId()==Tag.TAG_COMPOUND)
                                itemString = BlackMagick.nbtToSnbt(itemHolder.getCompound("item").get());
                            webItems.add(itemString);
                        }
                    }
                }
            }
        }
        resetClientCapes();
        if (newItems != null && newItems.getList("capes").isPresent() && !newItems.getList("capes").get().isEmpty()) {

            for (Tag thisTag : newItems.getList("capes").get()) {
                if (thisTag.getId() == Tag.TAG_COMPOUND) {
                    CompoundTag thisCompound = (CompoundTag)thisTag;
                    if (thisCompound.getString("name").isPresent() && thisCompound.getString("url").isPresent()) {
                        String name = thisCompound.getString("name").get();
                        String url = thisCompound.getString("url").get();
                        String id = thisCompound.getString("id").orElse(
                            name.replace(" ","_").toLowerCase().replaceAll("[^a-z0-9_:./-]",""));
                        String desc = thisCompound.getString("description").orElse(null);
                        registerCapeUrl(url, id, name, desc);
                    }
                }
            }

        }

        if (webItems.isEmpty())
            logWarn("No source of Black Market items available");
        else if (webItems.size() > SAVED_ROWS * 9)
            FortytwoEdit.logWarn("Web items list contains more than " + (SAVED_ROWS * 9) + " items (" + webItems.size() + ")");

        return result;
    }

}
