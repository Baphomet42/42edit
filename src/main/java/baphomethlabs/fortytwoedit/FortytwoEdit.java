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
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import baphomethlabs.fortytwoedit.FileTools.FileDisplayType;
import baphomethlabs.fortytwoedit.PathHelper.PathInfo;
import baphomethlabs.fortytwoedit.PathHelper.PathNode;
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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

    // keys
    public static KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("42edit","keybinds"));
    public static KeyMapping keyAfkClick = new KeyMapping("42edit.key.afk_click", GLFW.GLFW_KEY_MINUS, KEY_CATEGORY);
    public static KeyMapping keyAfkMove = new KeyMapping("42edit.key.afk_move", GLFW.GLFW_KEY_EQUAL, KEY_CATEGORY);
    public static KeyMapping keyFreeLook = new KeyMapping("42edit.key.free_look", GLFW.GLFW_KEY_LEFT_ALT, KEY_CATEGORY);
    public static KeyMapping keyMagickGui = new KeyMapping("42edit.key.open_magick_gui", GLFW.GLFW_KEY_J, KEY_CATEGORY);
    public static KeyMapping keyMod = new KeyMapping("42edit.key.key_mod", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static KeyMapping keySpamClick = new KeyMapping("42edit.key.spam_click", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static KeyMapping keyZoom = new KeyMapping("42edit.key.zoom", GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static final KeyMapping[] KEYBINDS = new KeyMapping[]{
        keyAfkClick,
        keyAfkMove,
        keyFreeLook,
        keyMagickGui,
        keyMod,
        keySpamClick,
        keyZoom
    };
    private static final String[] KEYBINDS_CONFIG_CACHE = new String[KEYBINDS.length];

    // options
    private static CompoundTag optionsExtra = null;
    public static boolean mixinProfileDynamicTooltip = true;
    public static boolean debugMixinHideBlockTags = false;
    public static boolean debugMixinRearrange = false;

    // zoom
    public static boolean zoomed = false;
    private static boolean smooth = false;

    // hacks
    private static final SecureRandom RNG = new SecureRandom();
    public static boolean autoMove = false;
    public static boolean autoClicker = false;
    public static boolean autoClick = true;
    public static boolean autoAttack = false;
    public static boolean autoMine = false;
    public static int attackWait = 1500;
    public static boolean afkScreenLock = false;
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
        return random()*range;
    }
    public static int randomInt(int range) {
        return (int)randomDouble(range);
    }

    // item history
    private static final ListTag itemHistList = new ListTag();
    public static final int ITEM_HIST_ROWS = 6;
    public static void addItemHist(ItemStack item) {
        if(item != null && !item.isEmpty())
            addItemHist(BlackMagick.itemToNbtStorage(item));
    }
    public static void addItemHist(CompoundTag itemNbt) {
        if(itemNbt != null && !itemNbt.isEmpty() && itemNbt.getCompound("components").isPresent()) {
            CompoundTag item = itemNbt.copy();
            item.remove("count");

            int found = -1;

            for(int i=0; i<itemHistList.size(); i++) {
                if(BlackMagick.elementsEqual(itemHistList.get(i),item)) {
                    found = i;
                    break;
                }
            }

            if(found<0) {
                itemHistList.add(0,item.copy());
                if(itemHistList.size()>ITEM_HIST_ROWS*9)
                    itemHistList.remove(itemHistList.size()-1);
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
    public static boolean randoMode = false;

    // opticapes
    private static long lastCapeLoaded = System.currentTimeMillis();

    public static boolean capeTimeCheck() {
        if(System.currentTimeMillis() - lastCapeLoaded > 50) {
            lastCapeLoaded = System.currentTimeMillis();
            return true;
        }
        else
            return false;
    }

    public static boolean opticapesWorking = true; //if optifine connection is working
    public static boolean opticapesOn = true; //optifine cape setting

    private static void checkCapesEnabled() {
        opticapesWorking = true;

        if(opticapesOn) {
            boolean connect = false;

            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection)(new URI("http://s.optifine.net/capes/42Richtofen42.png")).toURL().openConnection();
                con.setConnectTimeout(2000);
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
                    connect = true;
                con.disconnect();
            } catch(Exception ex) {}
            if(con != null)
                try {
                    con.disconnect();
                } catch(Exception ex) {}

            if(!connect) {
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
        for(String name : capeNames2) {
            client.getTextureManager().release(getCapeCacheID(name));
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
        if(name == null || name.isEmpty())
            return false;

        final Minecraft client = Minecraft.getInstance();
        if(capeNames.isEmpty() && !name.equals(client.getUser().getName()))
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

            for(int x = 0; x < capeInp.getWidth(); x++)
                for(int y = 0; y < capeInp.getHeight(); y++)
                    cape.setPixel(x, y, capeInp.getPixel(x, y));

            capeInp.close();
            ResourceLocation capeCacheId = getCapeCacheID(name);
            client.getTextureManager().register(capeCacheId, new DynamicTexture(capeCacheId::toString, cape));
            cape.close();
            capeNames2.add(name);
            return true;
        } catch(Exception ex) {}
        if(con != null)
            try {
                con.disconnect();
            } catch(Exception ex) {}
        if(stream != null)
            try {
                stream.close();
            } catch(Exception ex) {}
        if(capeInp != null)
            capeInp.close();
        if(cape != null)
            cape.close();

        return false;
    }
    private static ResourceLocation getCapeCacheID(String name) {
        return ResourceLocation.fromNamespaceAndPath("42edit","cache/cape/"+name.toLowerCase());
    }

    // custom capes
    public static boolean showClientCape = false;
    public static String selectedClientCape = "none";
    private static CapeTexture cacheClientCape = null;
    public static final List<CapeTexture> CLIENT_CAPES = Lists.newArrayList();
    private static void resetClientCapes() {
        CLIENT_CAPES.clear();
        CAPE_URLS.clear();
        CAPE_REGISTERED_IDS.clear();
        CAPE_MAP.clear();
        cacheClientCape = null;
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
    private enum CapeTextureStatus {
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
    private static void registerCustomCape(String id, String name, String desc) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse("42edit:cape/"+id);
        registerCape(CapeTexture.newCustom(id, name, desc), new ClientAsset.ResourceTexture(resourceLocation, resourceLocation));
    }
    private static void registerCape(CapeTexture cape, ClientAsset.Texture texture) {
        CLIENT_CAPES.add(cape);
        CAPE_MAP.put(cape.id(), texture);
    }
    /**
     * see {@link net.minecraft.client.resources.SkinManager#registerTextures}
     */
    private static void registerCapeUrl(String url, String id, String name, String desc) {
        if(url == null)
            return;
        if(!CAPE_URLS.contains(url)) {
            CAPE_URLS.add(url);
            CAPE_URLS_QUEUE.add(url);
            CAPE_URLS_QUEUE_MAP.put(url, id);
            CAPE_REGISTERED_IDS.add(id);
            CLIENT_CAPES.add(CapeTexture.newUrl(id, name, desc));
        }
    }
    public static void resolveCapeUrlQueue() {
        if(!CAPE_URLS_QUEUE.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if(client.getSkinManager() != null && client.getUser() != null && client.getUser().getProfileId() != null) {
                for(String url : CAPE_URLS_QUEUE) {
                    try {
                        CompletableFuture<PlayerSkin> futurePlayerSkin = ((SkinManagerInvoker)client.getSkinManager())
                            .invokeRegisterTextures(new java.util.UUID(0,0), new MinecraftProfileTextures(null,
                            new MinecraftProfileTexture(url, null), null, SignatureState.SIGNED));
                            futurePlayerSkin.thenAccept(playerSkin -> {
                                if(CAPE_URLS_QUEUE_MAP.containsKey(url) && playerSkin != null && playerSkin.cape() != null) {
                                    CAPE_MAP.put(CAPE_URLS_QUEUE_MAP.get(url), playerSkin.cape());
                                }
                                else
                                    FortytwoEdit.logWarn("Failed to load cape id for: "+url);
                            });
                    }
                    catch(Exception ex) {
                        FortytwoEdit.logWarn("Failed to load cape texture for: "+url);
                    }
                }
                CAPE_URLS_QUEUE.clear();
            }
        }
        else if(!CAPE_REGISTERED_IDS.contains(selectedClientCape) && (warnedCapeCache == null || !warnedCapeCache.equals(selectedClientCape))) {
            warnedCapeCache = selectedClientCape;
            FortytwoEdit.logWarn("Failed to find custom cape with ID: "+selectedClientCape);
        }
    }
    private static final Map<String,ClientAsset.Texture> CAPE_MAP = Maps.newHashMap();
    public static ClientAsset.Texture getClientCape() {
        if(CAPE_MAP.containsKey(selectedClientCape))
            return CAPE_MAP.get(selectedClientCape);
        resolveCapeUrlQueue();
        return null;
    }
    private static CapeTexture getCacheClientCape() {
        if(cacheClientCape != null)
            return cacheClientCape;
        for(CapeTexture c : CLIENT_CAPES)
            if(c.id().equals(selectedClientCape)) {
                cacheClientCape = c;
                return cacheClientCape;
            }
        cacheClientCape = new CapeTexture(CapeTextureStatus.UNKNOWN, selectedClientCape, selectedClientCape, selectedClientCape);
        return cacheClientCape;
    }
    public static String getClientCapeTextboxName() {
        return getCacheClientCape().name();
    }
    public static Tooltip getClientCapeTextboxTooltip() {
        CapeTexture cape = getCacheClientCape();
        if(cape.status()==CapeTextureStatus.UNKNOWN) {
            return Tooltip.create(Component.empty().append("Unknown").withStyle(ChatFormatting.RED));
        }
        MutableComponent txtCustomTt = Component.empty().append(cape.name());
        if(cape.desc() != null)
            txtCustomTt.append("\n").append(Component.empty().append(cape.desc()).withStyle(ChatFormatting.GRAY));
        return Tooltip.create(txtCustomTt);
    }
    public static void cycleClientCape(boolean right) {
        if(CLIENT_CAPES.isEmpty())
            return;
        int index = -1;
        for(int i=0; i<CLIENT_CAPES.size(); i++) {
            if(CLIENT_CAPES.get(i).id().equals(selectedClientCape)) {
                index = i;
                break;
            }
        }
        if(index == -1) {
            index = 0;
        }
        else {
            if(right) {
                index++;
                if(index >= CLIENT_CAPES.size())
                    index = 0;
            }
            else {
                index--;
                if(index < 0)
                    index = CLIENT_CAPES.size()-1;
            }
        }

        String newCape = CLIENT_CAPES.get(index).id();

        FortytwoEdit.readOptions();
        FortytwoEdit.selectedClientCape = newCape;
        FortytwoEdit.updateOptions();

        cacheClientCape = null;
    }

    public static String USERNAME = "";
    public static IntArrayTag UUID = new IntArrayTag(new int[]{0,0,0,0});
    public static String[] PROFILE_SUGGS = null;

    //skin testing
    public static boolean showClientSkin = false;
    public static boolean clientSkinSlim = false;
    public static String customSkinName = "";
    private static final ResourceLocation CUSTOM_SKIN_ID = ResourceLocation.fromNamespaceAndPath("42edit","cache/custom_skin");
    public static final ClientAsset.Texture CUSTOM_SKIN_TEXTURE = new ClientAsset.ResourceTexture(CUSTOM_SKIN_ID, CUSTOM_SKIN_ID);

    public static boolean setCustomSkin(File file) {
        final Minecraft client = Minecraft.getInstance();
        customSkinName = "";
        showClientSkin = false;
        client.getTextureManager().release(CUSTOM_SKIN_ID);

        if(file != null) {
            FileInputStream inp = null;
            NativeImage skinFile = null;
            NativeImage skin = null;
            if(file.isFile() && file.getName().endsWith(".png")) {
                try {
                    inp = new FileInputStream(file);
                    skinFile = NativeImage.read(inp);
                    inp.close();
                    skin = new NativeImage(skinFile.getWidth(),skinFile.getHeight(),true);

                    for(int x = 0; x < skinFile.getWidth(); x++)
                        for(int y = 0; y < skinFile.getHeight(); y++)
                            skin.setPixel(x, y, skinFile.getPixel(x, y));

                    skinFile.close();
                    customSkinName = file.getName();
                    client.getTextureManager().register(CUSTOM_SKIN_ID, new DynamicTexture(CUSTOM_SKIN_ID::toString, skin));
                    skin.close();
                    showClientSkin = true;
                    return true;
                } catch(Exception ex) {}
            }
            if(inp != null)
                try {
                    inp.close();
                } catch(Exception ex) {}
            if(skinFile != null)
                skinFile.close();
            if(skin != null)
                skin.close();

            logWarn("Failed to set custom skin file");
        }

        return false;
    }

    private static final String[] MOD_ASSETS_TEXTURES = new String[]{//to_do replace with real solution
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
        for(String path : MOD_ASSETS_TEXTURES)
            loadAssetsTexture(client, path);
    }

    public static void loadAssetsTexture(Minecraft client, String path) {
        try {
            NativeImage texture = NativeImage.read(client.getClass().getClassLoader().getResourceAsStream("assets/"+MOD_ID_MC+"/textures/"+path+".png"));
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID_MC,path);
            client.getTextureManager().release(id);
            client.getTextureManager().register(id,new DynamicTexture(id::toString,texture));
        }
        catch(Exception ex) {
            FortytwoEdit.logError("Failed to load mod texture: "+path);
        }
    }

    public static InputStream getAssetsLang(String lang) {
        boolean found = false;
        for(String s : MOD_LANGUAGES) {
            if(s.equals(lang))
                found = true;
        }
        if(found) {
            final Minecraft client = Minecraft.getInstance();
            try {
                return client.getClass().getClassLoader().getResourceAsStream("assets/"+MOD_ID_MC+"/lang/"+lang+".json");
            }
            catch(Exception ex) {
                FortytwoEdit.logError("Failed to load mod language: "+lang);
            }
        }
        return null;
    }

    //freelook
    public static boolean isFreeLooking = false;
    private static CameraType lastPerspective;
    public static float[] cameraRotation = {0f,0f};

    //see feature items
    public static final FeatureFlagSet FEATURES = FeatureFlagSet.of(FeatureFlags.VANILLA);

    //supersecretsettings
    public static final ResourceLocation[] SUPER_SECRET_SETTING_PROGRAMS = new ResourceLocation[]{/*Identifier.of("42edit","shaders/post/notch.json"), Identifier.of("42edit","shaders/post/fxaa.json"), Identifier.of("42edit","shaders/post/art.json"), Identifier.of("42edit","shaders/post/bumpy.json"), Identifier.of("42edit","shaders/post/blobs2.json"), Identifier.of("42edit","shaders/post/pencil.json"), Identifier.of("42edit","shaders/post/color_convolve.json"), Identifier.of("42edit","shaders/post/deconverge.json"), Identifier.of("42edit","shaders/post/flip.json"),*/ ResourceLocation.withDefaultNamespace("invert"),/* Identifier.of("42edit","shaders/post/ntsc.json"), Identifier.of("42edit","shaders/post/outline.json"), Identifier.of("42edit","shaders/post/phosphor.json"), Identifier.of("42edit","shaders/post/scan_pincushion.json"), Identifier.of("42edit","shaders/post/sobel.json"), Identifier.of("42edit","shaders/post/bits.json"), Identifier.of("42edit","shaders/post/desaturate.json"), Identifier.of("42edit","shaders/post/green.json"), Identifier.of("42edit","shaders/post/blur.json"), Identifier.of("42edit","shaders/post/wobble.json"), Identifier.of("42edit","shaders/post/blobs.json"), Identifier.of("42edit","shaders/post/antialias.json"),*/ ResourceLocation.withDefaultNamespace("creeper"), ResourceLocation.withDefaultNamespace("spider")};
    private static int superSecretSettingIndex = SUPER_SECRET_SETTING_PROGRAMS.length;
    private static final ResourceLocation[] SECRETSOUNDS = getSecretSounds();
    private static ResourceLocation[] getSecretSounds() {
        Set<ResourceLocation> sounds = BuiltInRegistries.SOUND_EVENT.keySet();
        List<ResourceLocation> valid = Lists.newArrayList();
        for(ResourceLocation sound: sounds) {
            if(sound.getPath().contains("entity.") || sound.getPath().contains("block.") || sound.getPath().contains("weather.") || sound.getPath().contains("item."))
                valid.add(sound);
        }
        return valid.toArray(new ResourceLocation[0]);
    }
    private static void secretSound() {
        if(SECRETSOUNDS != null && SECRETSOUNDS.length > 0) {
            final Minecraft client = Minecraft.getInstance();
            int i = randomInt(SECRETSOUNDS.length);
            try {
                client.player.playNotifySound(SoundEvent.createVariableRangeEvent(SECRETSOUNDS[i]), SoundSource.MASTER, 1f, .5f);
            } catch(Exception ex) {}
        }
    }
    public static void cycleSuperSecretSetting() {
        final Minecraft client = Minecraft.getInstance();
        if(client.getCameraEntity() instanceof Player) {
            if(client.gameRenderer.currentPostEffect() != null) {
                client.gameRenderer.clearPostEffect();
            }
            superSecretSettingIndex = (superSecretSettingIndex + 1) % (SUPER_SECRET_SETTING_PROGRAMS.length + 1);
            if(superSecretSettingIndex == SUPER_SECRET_SETTING_PROGRAMS.length) {
                ((GameRendererInvoker)client.gameRenderer).setEffectActive(false);
            } else {
                ((GameRendererInvoker)client.gameRenderer).invokeSetPostEffect(SUPER_SECRET_SETTING_PROGRAMS[superSecretSettingIndex]);
            }
        }
        secretSound();
    }

    // common
    public static final ItemStack HEAD42 = BlackMagick.itemFromString("{id:player_head,components:{profile:{name:\"42Richtofen42\","
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlc"
        +"yIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDliNjliNWU3MzVlYjUyMmIyNGM2OTczNTQ5ZGRhODMzYjE1ZD"
        +"kxYjg3NDM1NjRjZmIxN2QwZjk2MWMwZjU0Ig0KICAgIH0NCiAgfQ0KfQ==\"}]}}}");
    public static final CompoundTag BANNER42 = BlackMagick.validCompoundFromString("{id:red_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:curly_border\"}"
        +",{color:\"black\",pattern:\"minecraft:bricks\"},{color:\"red\",pattern:\"minecraft:triangle_bottom\"},{color:\"black\",pattern:\"minecraft:triangle_bottom\"}"
        +",{color:\"purple\",pattern:\"minecraft:flower\"},{color:\"black\",pattern:\"minecraft:gradient\"}]}}");
    public static final CompoundTag BANNERBRICK = BlackMagick.validCompoundFromString("{id:orange_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:gradient\"}"
        +",{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"orange\",pattern:\"minecraft:triangles_bottom\"}"
        +",{color:\"red\",pattern:\"minecraft:bricks\"},{color:\"green\",pattern:\"minecraft:creeper\"}]}}");
    public static final ItemStack ITEM_ERROR = BlackMagick.itemFromString("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcy"
        +"IgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZTE4MjM2NzExOTYzMTMxNzY5MjM0Mzc4OGNkNWM4NTRjMTNiNDQ5"
        +"ZDM2ZTYyMmI4NTU0YTU2MzhlZDM4NTkzIg0KICAgIH0NCiAgfQ0KfQ==\"}]}}}");
    public static final ItemStack ITEM_QUESTION = BlackMagick.itemFromString("{components:{\"minecraft:profile\":"
        +"{id:[I;1617833968,-310949822,-1653808685,840726584],name:\"MHF_Question\",properties:[{name:\"textures\",value:\"ewogICJzaWduYXR1cmVSZXF1aXJlZCIg"
        +"OiBmYWxzZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzNGUwNjNjYWZiNDY3Y"
        +"TVjOGRlNDNlYzc4NjE5Mzk5ZjM2OWY0YTUyNDM0ZGE4MDE3YTk4M2NkZDkyNTE2YTAiCiAgICB9CiAgfQp9\"}]}},count:1,id:\"minecraft:player_head\"}");

    public static final CompoundTag LORE_BAPHOMETHLABS = BlackMagick.validCompoundFromString("{color:\"gold\",\"text\":\"BaphomethLabs\"}");
    public static final CompoundTag LORE_BAPHOMETHLABS_BOTTLE = BlackMagick.validCompoundFromString("{color:\"gold\",\"text\":\"Bottled by BaphomethLabs\"}");

    //saved items
    public static final int SAVED_ROWS = 12;

    //web items
    public static boolean webItemsAuto = true;
    public static List<String> webItems = Lists.newArrayList();
    private static final String WEB_ITEMS_URL_DEFAULT = "https://baphomet42.github.io/mc/blackmarket/items.json";
    private static String webItemsUrlOverride = "";


    @Override
    public void onInitializeClient() {
        logInfo("Loading 42edit client");

        final Minecraft client = Minecraft.getInstance();

        //options
        readOptions();

        // custom capes
        USERNAME = client.getUser().getName();
        if(client.getUser().getProfileId() != null)
            UUID = new IntArrayTag(UUIDUtil.uuidToIntArray(client.getUser().getProfileId()));
        PROFILE_SUGGS = new String[]{USERNAME,BlackMagick.nbtToSnbt(UUID)};
        clearOptiCapes();

        getSavedItems(); // used to show log errors
        refreshWebItems(false);

        FileTools.scanModFiles();

        int foundComponentPaths = 0;
        for(String c : SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList()) {
            // this will log warnings if PathHelper doesnt include a vanilla component
            PathInfo pi = PathHelper.getItemPath(null, PathNode.of("components"),PathNode.of(c));
            if(!pi.isEmpty())
                foundComponentPaths++;
            else
                FortytwoEdit.logWarn("No PathInfo found for component \""+c+"\"");
        }
        logInfo("Found PathInfo for "+foundComponentPaths+"/"+SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList().size()+" components");

        logInfo("42edit client initialized");
    }

    public static void onMinecraftInit() {
        logInfo("Running 42edit post-init setup");

        loadAllModAssets();

        logInfo("42edit post-init setup finished");
    }

    public static void clientTick(Minecraft client) {

        if(client.player == null || client.screen != null) {
            autoClicker = false;
            autoMove = false;
        }

        // magickgui
        if(keyMagickGui.consumeClick())
            client.setScreen(quickScreen.get());

        // zoom
        if(keyZoom.isDown() && !zoomed) {
            smooth = client.options.smoothCamera;
            client.options.smoothCamera = true;
            zoomed = true;
        }
        else if(!keyZoom.isDown() && zoomed) {
            client.options.smoothCamera = smooth;
            zoomed = false;
        }

        // afkMove
        if(keyAfkMove.consumeClick()) {
            autoMove = !autoMove;
            client.options.keyUp.setDown(false);
            while(client.options.keyUp.consumeClick()) {}
        }
        if(autoMove && client.player != null) {
            if(client.options.keyUp.consumeClick()) {
                autoMove = false;
                client.options.keyUp.setDown(false);
                while(client.options.keyUp.consumeClick()) {}
            }
            else
                client.options.keyUp.setDown(true);
        }

        //afkClick
        if(keyAfkClick.consumeClick()) {
            autoClicker = !autoClicker;
            client.options.keyUse.setDown(false);
            client.options.keyAttack.setDown(false);
            while(client.options.keyUse.consumeClick()) {}
            while(client.options.keyAttack.consumeClick()) {}
        }
        if(autoClicker && client.player != null) {
            if(autoClick) {
                client.options.keyUse.setDown(true);
            }
            if(autoAttack && System.currentTimeMillis()>=lastAttack + attackWait && client.hitResult instanceof EntityHitResult) {
                lastAttack = System.currentTimeMillis();
                suppressKeybind = true;
                KeyMapping.click(((KeyMappingAccessor)client.options.keyAttack).getBoundKey());
                suppressKeybind = false;
            }
            if(autoMine) {
                client.options.keyAttack.setDown(true);
            }
        }

        //autoFish
        if(autoFishClickQueue && System.currentTimeMillis()>=(lastFish+fishWait)) {
            if(autoFish && !autoClicker && client.screen == null && ((!client.player.getMainHandItem().isEmpty()
                    && client.player.getMainHandItem().is(Items.FISHING_ROD)) || (client.player.getMainHandItem().isEmpty()
                    && !client.player.getOffhandItem().isEmpty() && client.player.getOffhandItem().is(Items.FISHING_ROD))) ) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
                didFish = true;
            }
            autoFishClickQueue = false;
            lastFish = System.currentTimeMillis() + 100+randomInt(400);
        }
        if(didFish && System.currentTimeMillis()>=(lastFish+fishWait)) {
            if(autoFish && !autoClicker && client.screen == null && ((!client.player.getMainHandItem().isEmpty()
                    && client.player.getMainHandItem().is(Items.FISHING_ROD)) || (client.player.getMainHandItem().isEmpty()
                    && !client.player.getOffhandItem().isEmpty() && client.player.getOffhandItem().is(Items.FISHING_ROD))) ) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
            }
            didFish = false;
            lastFish = System.currentTimeMillis() + 100+randomInt(400);
        }

        //freelook
        if(keyFreeLook.isDown()) {
            if(!isFreeLooking) {
                lastPerspective = client.options.getCameraType();
                Entity view = client.getCameraEntity() == null ? client.player : client.getCameraEntity();
                cameraRotation[0] = view.getYRot();
                cameraRotation[1] = view.getXRot();

                if(lastPerspective == CameraType.FIRST_PERSON)
                    client.options.setCameraType(CameraType.THIRD_PERSON_BACK);

                isFreeLooking = true;
            }
        }
        else if(isFreeLooking) {
            isFreeLooking = false;
            client.options.setCameraType(lastPerspective);
        }

        //spam
        if(keySpamClick.isDown() && System.currentTimeMillis()>=lastSpam + 20) {
            if(keyMod.isDown())
                KeyMapping.click(((KeyMappingAccessor)client.options.keyAttack).getBoundKey());
            else {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
                if(randoMode)
                    changeRandoSlot();
            }
            lastSpam = System.currentTimeMillis();
        }

        // rando
        if(randoMode) {
            if(client.options.keyUse.isDown())
                changeRandoSlot();
        }
    }

    public static void updateAutoClick(boolean click, boolean mine, boolean attack, int wait) {
        final Minecraft client = Minecraft.getInstance();
        autoClicker = false;

        autoClick = click;
        autoMine = mine;
        autoAttack = attack;
        attackWait = wait;
        if(wait < 1)
            attackWait = 1;
        else if(wait > 9999)
            attackWait = 9999;

        client.options.keyUse.setDown(false);
        client.options.keyAttack.setDown(false);
    }

    private static boolean testRandoSlot() {
        final Minecraft client = Minecraft.getInstance();
        int selected = client.player.getInventory().getSelectedSlot() + 1;
        for(int i = 0; i < randoSlots.length; i++) {
            if(randoSlots[i] == selected)
                return true;
        }
        return false;
    }

    public static void changeRandoSlot() {
        if(randoSlots != null && testRandoSlot()) {
            final Minecraft client = Minecraft.getInstance();
            int slot = randomInt(randoSlots.length);
            slot = randoSlots[slot];
            client.player.getInventory().setSelectedSlot(slot - 1);
        }
    }

    public static ItemStack copyLookAt() {
        final Minecraft client = Minecraft.getInstance();
        HitResult hitResult = client.hitResult;
        if(hitResult == null) {
            return null;
        }
        if(hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = client.player.level().getBlockState(blockPos);

            if(!blockState.getProperties().isEmpty()) {
                CompoundTag stack = new CompoundTag();
                stack.put("id",StringTag.valueOf(blockState.getBlock().asItem().toString()));
                CompoundTag tag = new CompoundTag();

                String states = "";
                states += "{";
                boolean bl = false;
                for(Map.Entry<Property<?>,Comparable<?>> entry : blockState.getValues().entrySet()) {
                    if(bl) {
                        states += ",";
                    }
                    states += entry.getKey().getName();
                    states += ":";
                    states += "\""+getValueString(blockState,entry.getKey())+"\"";
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

    private static final String LOG_PREFIX = "(42edit) ";
    private static final SystemToast.SystemToastId TOAST_TYPE = new SystemToast.SystemToastId();
    private static final MutableComponent TOAST_PREFIX = Component.empty().append("").append(Component.empty().append("(42edit) ").withStyle(ChatFormatting.BLACK));

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
            client.getToastManager().addToast(new SystemToast(TOAST_TYPE, TOAST_PREFIX.copy().append(title), desc));
        }
        catch(Exception ex) {
            logError("Failed to show toast ("+BlackMagick.textComponentToStringLiteral(title)+") ("+BlackMagick.textComponentToStringLiteral(desc)+"): "+ex.getMessage());
        }
    }

    /**
     * Refresh a wide variety of features. Can be used for debug reasons, to fetch new web items, etc.
     */
    public static void debugTryRefreshVarious() {
        logInfo("Starting debug...");

        readOptions();
        refreshWebItems(true);

        final Minecraft client = Minecraft.getInstance();
        ((HotbarManagerAccessor)client.getHotbarManager()).setLoaded(false);
        client.getHotbarManager().get(0);

        getSavedItems(); // used to show log errors

        SuggestionHelper.runAllListMethods();

        clearOptiCapes();
        setCustomSkin(null);

        itemHistList.clear();

        FileTools.scanModFiles();

        LogScreen.debugTryRefreshVarious();

        logInfo("Debug complete");
    }

    public static void saveKeybindOptions() {
        boolean diff = false;
        for(int i=0; i<KEYBINDS.length; i++) {
            if(!KEYBINDS[i].saveString().equals(KEYBINDS_CONFIG_CACHE[i])) {
                diff = true;
                break;
            }
        }
        if(diff)
            updateOptions();
    }

    public static void readOptions() {
        CompoundTag options = FileTools.readCompoundFromFile(FileTools.FILE_OPTIONS);
        if(options == null)
            options = new CompoundTag();

        // keep options consistent
        options.getByte("afk_screen_lock").ifPresent(b -> afkScreenLock = (b == 1));
        options.getByte("custom_cape_toggle").ifPresent(b -> showClientCape = (b == 1));
        options.getString("custom_cape").ifPresent(s -> selectedClientCape = s);
        options.getByte("debug_screen_hide_tags").ifPresent(b -> debugMixinHideBlockTags = (b == 1));
        options.getByte("debug_screen_rearrange").ifPresent(b -> debugMixinRearrange = (b == 1));
        options.getByte("dynamic_profile_tooltip_info").ifPresent(b -> mixinProfileDynamicTooltip = (b == 1));
        options.getCompound("keybinds").ifPresent(c -> {
            Set<String> foundKeys = Sets.newHashSet();
            for(String k : c.keySet()) {
                boolean added = false;
                if(c.getString(k).isPresent()) {
                    for(int i=0; i<KEYBINDS.length; i++) {
                        if(KEYBINDS[i].getName().equals(k)) {
                            try {
                                KEYBINDS[i].setKey(InputConstants.getKey(c.getString(k).get()));
                            }
                            catch(Exception ex) {}
                            foundKeys.add(k);
                            added = true;
                        }
                    }
                }
                if(!added)
                    logError("Failed to set keybind for binding "+k+" to key "+BlackMagick.nbtToSnbt(c.get(k)));
            }
            if(!foundKeys.isEmpty()) {
                for(String k : foundKeys)
                    c.remove(k);
                KeyMapping.resetMapping();
            }
        });
        options.getByte("opticapes").ifPresent(b -> opticapesOn = (b == 1));
        options.getByte("web_items").ifPresent(b -> webItemsAuto = (b == 1));
        options.getString("web_items_url").ifPresent(s -> {
            webItemsUrlOverride = s;
            if(webItemsUrlOverride.length()>0 && !webItemsUrlOverride.startsWith("https://") && !webItemsUrlOverride.startsWith("http://")) {
                logError("Invalid web_items_url (expected 'http://' or 'https://'): "+webItemsUrlOverride);
                webItemsUrlOverride = "";
            }
        });

        // keep options consistent
        options.remove("file_format");
        options.remove("afk_screen_lock");
        options.remove("custom_cape_toggle");
        options.remove("custom_cape");
        options.remove("debug_screen_hide_tags");
        options.remove("debug_screen_rearrange");
        options.remove("dynamic_profile_tooltip_info");
        options.remove("item_warning_override");
        if(options.getCompoundOrEmpty("keybinds").isEmpty())
            options.remove("keybinds");
        options.remove("opticapes");
        options.remove("web_items");
        options.remove("web_items_url");

        optionsExtra = null;
        if(!options.isEmpty()) {
            logWarn("Config file contains unknown keys: "+BlackMagick.nbtToSnbt(options));
            optionsExtra = options.copy();
        }

        updateOptions();
    }

    public static void updateOptions() {
        CompoundTag options = new CompoundTag();
        if(optionsExtra != null)
            options = optionsExtra.copy();

        // keep options consistent
        options.putInt("file_format",FileTools.FILE_FORMAT);
        options.putBoolean("afk_screen_lock",afkScreenLock);
        options.putBoolean("custom_cape_toggle",showClientCape);
        options.putString("custom_cape",selectedClientCape);
        options.putBoolean("debug_screen_hide_tags",debugMixinHideBlockTags);
        options.putBoolean("debug_screen_rearrange",debugMixinRearrange);
        options.putBoolean("dynamic_profile_tooltip_info",mixinProfileDynamicTooltip);
        CompoundTag keysCompound = options.getCompoundOrEmpty("keybinds");
        for(int i=0; i<KEYBINDS.length; i++) {
            keysCompound.put(KEYBINDS[i].getName(),StringTag.valueOf(KEYBINDS[i].saveString()));
            KEYBINDS_CONFIG_CACHE[i] = KEYBINDS[i].saveString();
        }
        options.put("keybinds",keysCompound);
        options.putBoolean("opticapes",opticapesOn);
        options.putBoolean("web_items",webItemsAuto);
        options.putString("web_items_url",webItemsUrlOverride);

        FileTools.writeCompoundToFile(FileTools.FILE_OPTIONS, options, FileDisplayType.TREE);
    }

    public static Map<Integer,String> getSavedItems() {
        Map<Integer,String> itemsMap = Maps.newHashMap();
        ItemBuilderScreen.savedItemsError = false;
        CompoundTag savedItemsNbt = FileTools.readCompoundFromFile(FileTools.FILE_SAVED_ITEMS);
        if(savedItemsNbt == null) {
            savedItemsNbt = new CompoundTag();
            if(FileTools.testFileExists(FileTools.FILE_SAVED_ITEMS)) {
                String fileString = FileTools.readStringFromFile(FileTools.FILE_SAVED_ITEMS);
                if(fileString != null && !fileString.isEmpty()) {
                    ItemBuilderScreen.savedItemsError = true;
                }
            }
            else {
                setSavedItems(itemsMap);
            }
        }

        boolean foundItems = false;
        if(savedItemsNbt.getList("items").isPresent()) {
            ListTag storedItems = savedItemsNbt.getList("items").get();
            foundItems = true;
            if(!storedItems.isEmpty()) {
                boolean itemsOutOfRange = false;
                final int MAX_SAVED_ITEM_SLOT = FortytwoEdit.SAVED_ROWS*9-1;
                int currentDupeSlot = MAX_SAVED_ITEM_SLOT+1;
                ListTag unknownItemHolders = new ListTag();
                for(int i=0; i<storedItems.size(); i++) {
                    if(storedItems.get(i).getId() == Tag.TAG_COMPOUND) {
                        CompoundTag itemHolder = storedItems.getCompound(i).get();
                        if(itemHolder.isEmpty())
                            continue;
                        if((itemHolder.getCompound("item").isPresent() || itemHolder.getString("item").isPresent())
                        && itemHolder.getInt("slot").isPresent()) {
                            String itemString = itemHolder.getStringOr("item","");
                            if(itemHolder.get("item").getId()==Tag.TAG_COMPOUND)
                                itemString = BlackMagick.nbtToSnbt(itemHolder.getCompound("item").get());
                            itemHolder.remove("item");
                            if(itemString.isEmpty() || itemString.equals("{}"))
                                continue;
                            int slot = itemHolder.getInt("slot").get();
                            itemHolder.remove("slot");
                            if(!itemHolder.isEmpty()) {
                                FortytwoEdit.logError("Saved item contains unknown keys: "+BlackMagick.nbtToSnbt(itemHolder));
                            }
                            if(slot<0 || slot>MAX_SAVED_ITEM_SLOT) {
                                itemsOutOfRange = true;
                            }
                            if(itemsMap.containsKey(slot)) {
                                int newSlot = currentDupeSlot;
                                while(itemsMap.containsKey(newSlot)) {
                                    newSlot++;
                                }
                                currentDupeSlot = newSlot+1;
                                FortytwoEdit.logError("Saved items file contains duplicate slot "+slot+". Item will move to slot "+newSlot+" after saving.");
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
                if(itemsOutOfRange) {
                    FortytwoEdit.logWarn("Saved items file contains slots outside of range 0-"+MAX_SAVED_ITEM_SLOT);
                }
                if(!unknownItemHolders.isEmpty()) {
                    FortytwoEdit.logError("Saved items file contains invalid entries. After saving, the following will be deleted: "+BlackMagick.nbtToSnbt(unknownItemHolders));
                }
            }
        }
        if(!foundItems && !savedItemsNbt.isEmpty()) {
            logError("Failed to read saved items: " + BlackMagick.nbtToSnbt(savedItemsNbt));
            ItemBuilderScreen.savedItemsError = true;
        }

        return itemsMap;
    }

    public static boolean setSavedItems(Map<Integer,String> savedItemsMap) {
        ListTag itemsList = new ListTag();
        for(int slot : BlackMagick.sortIntSet(savedItemsMap.keySet())) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("slot",slot);
            nbt.putString("item",savedItemsMap.get(slot));
            itemsList.add(nbt);
        }

        CompoundTag savedItemsNbt = new CompoundTag();
        savedItemsNbt.put("items",itemsList);
        savedItemsNbt.putInt("file_format",FileTools.FILE_FORMAT);
        if(FileTools.writeCompoundToFile(FileTools.FILE_SAVED_ITEMS, savedItemsNbt, FileDisplayType.TREE_CONDITIONAL_COLLAPSE)) {
            getSavedItems(); // used to show log errors
            return true;
        }
        return false;
    }

    public static boolean testSavedItems(Map<Integer,String> oldMap) {
        Map<Integer,String> newMap = getSavedItems();
        if(newMap.keySet().size()==oldMap.keySet().size()) {
            for(int slot : oldMap.keySet()) {
                if(!(newMap.containsKey(slot) && newMap.get(slot).equals(oldMap.get(slot))))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param forceWeb when false, only connect to site if .42edit config web items option set to auto
     * @return compound with keys to mark results (site_match_catch, site_updated_catch)
     */
    public static CompoundTag refreshWebItems(boolean forceWeb) {
        webItems.clear();
        CompoundTag result = new CompoundTag();

        CompoundTag cacheNbt = FileTools.readCompoundFromFile(FileTools.FILE_WEB_CACHE);
        if(cacheNbt == null)
            cacheNbt = new CompoundTag();
        CompoundTag newItems = cacheNbt.copy();

        if(webItemsAuto || forceWeb) {
            String webItemsUrlActive = webItemsUrlOverride.length()>0 ? webItemsUrlOverride : WEB_ITEMS_URL_DEFAULT;
            String webJson = "";
            boolean didError = false;

            HttpURLConnection con = null;
            InputStream stream = null;
            try {
                con = (HttpURLConnection)(new URI(webItemsUrlActive)).toURL().openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(500);
                con.setUseCaches(false);
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = con.getInputStream();
                    webJson = new String(stream.readAllBytes(), FileTools.FILE_CHARSET);
                    stream.close();
                    con.disconnect();
                }
            }
            catch(Exception ex) {
                logWarn("Failed connection to BaphomethLabs Black Market ("+webItemsUrlActive+")");
                didError = true;
            }
            if(con != null)
                try {
                    con.disconnect();
                } catch(Exception ex) {}
            if(stream != null)
                try {
                    stream.close();
                } catch(Exception ex) {}

            Tag parseWebJson = BlackMagick.nbtFromSnbt(webJson);
            if(parseWebJson != null && parseWebJson.getId()==Tag.TAG_COMPOUND) {
                CompoundTag webNbt = (CompoundTag)parseWebJson;
                newItems = webNbt.copy();

                if(BlackMagick.elementsEqual(webNbt,cacheNbt)) {
                    logInfo("Black Market items are up to date");
                    result.put("site_match_catch",new CompoundTag());
                }
                else {
                    logInfo("Updating Black Market items");
                    result.put("site_updated_catch",new CompoundTag());
                    FileTools.writeCompoundToFile(FileTools.FILE_WEB_CACHE, newItems, FileDisplayType.TREE_CONDITIONAL_COLLAPSE);
                }
            }
            else if(!didError)
                logError("Failed to parse BaphomethLabs Black Market ("+webItemsUrlActive+"): "+webJson);
        }

        if(newItems != null && newItems.getList("versions").isPresent() && !newItems.getList("versions").get().isEmpty()) {

            ListTag versionsList = newItems.getList("versions").get();
            int itemsVer = -1;
            int itemsVerMinor = 0;
            ListTag jsonItems = null;

            for(int i=0; i<versionsList.size(); i++) {
                if(versionsList.get(i).getId() == Tag.TAG_COMPOUND) {
                    CompoundTag versionData = versionsList.getCompound(i).get();
                    if(versionData.getInt("version").isPresent() && versionData.getList("items").isPresent()) {
                        int versionNum = versionData.getInt("version").get();
                        int versionMinor = versionData.getIntOr("version_minor", 0);
                        if(itemsVer == -1 ||
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

            if(jsonItems != null && !jsonItems.isEmpty()) {
                for(int i=0; i<jsonItems.size(); i++) {
                    if(jsonItems.get(i).getId() == Tag.TAG_COMPOUND) {
                        CompoundTag itemHolder = jsonItems.getCompound(i).get();
                        if(itemHolder.getCompound("item").isPresent() || itemHolder.getString("item").isPresent()) {
                            String itemString = itemHolder.getStringOr("item","");
                            if(itemHolder.get("item").getId()==Tag.TAG_COMPOUND)
                                itemString = BlackMagick.nbtToSnbt(itemHolder.getCompound("item").get());
                            webItems.add(itemString);
                        }
                    }
                }
            }
        }
        resetClientCapes();
        if(newItems != null && newItems.getList("capes").isPresent() && !newItems.getList("capes").get().isEmpty()) {

            for(Tag thisTag : newItems.getList("capes").get()) {
                if(thisTag.getId() == Tag.TAG_COMPOUND) {
                    CompoundTag thisCompound = (CompoundTag)thisTag;
                    if(thisCompound.getString("name").isPresent() && thisCompound.getString("url").isPresent()) {
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

        if(webItems.isEmpty())
            logWarn("No source of Black Market items available");
        else if(webItems.size()>SAVED_ROWS*9)
            FortytwoEdit.logWarn("Web items list contains more than " + (SAVED_ROWS*9) + " items ("+webItems.size()+")");

        return result;
    }

}
