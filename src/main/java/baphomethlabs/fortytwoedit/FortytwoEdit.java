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
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import baphomethlabs.fortytwoedit.FileTools.FileDisplayType;
import baphomethlabs.fortytwoedit.gui.screen.AutoClick;
import baphomethlabs.fortytwoedit.gui.screen.Capes;
import baphomethlabs.fortytwoedit.gui.screen.DebugScreen;
import baphomethlabs.fortytwoedit.gui.screen.Hacks;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import baphomethlabs.fortytwoedit.gui.screen.LogScreen;
import baphomethlabs.fortytwoedit.gui.screen.MagickGui;
import baphomethlabs.fortytwoedit.gui.screen.SecretScreen;
import baphomethlabs.fortytwoedit.mixin.GameRendererInvoker;
import baphomethlabs.fortytwoedit.mixin.HotbarManagerAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyMappingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
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

    // gui
    public static QuickScreen quickScreen = QuickScreen.NONE;
    public enum QuickScreen {
        NONE,

        AUTO_CLICK,
        CAPES,
        DEBUG_SCREEN,
        HACKS,
        ITEM_BUILDER,
        LOG_SCREEN,
        SECRET_SCREEN
    }

    // keys
    public static KeyMapping keyAfkClick = new KeyMapping("42edit.key.afk_click", GLFW.GLFW_KEY_MINUS, "42edit.key.categories.42edit");
    public static KeyMapping keyAfkMove = new KeyMapping("42edit.key.afk_move", GLFW.GLFW_KEY_EQUAL, "42edit.key.categories.42edit");
    public static KeyMapping keyFreeLook = new KeyMapping("42edit.key.free_look", GLFW.GLFW_KEY_LEFT_ALT, "42edit.key.categories.42edit");
    public static KeyMapping keyMagickGui = new KeyMapping("42edit.key.open_magick_gui", GLFW.GLFW_KEY_J, "42edit.key.categories.42edit");
    public static KeyMapping keyMod = new KeyMapping("42edit.key.key_mod", InputConstants.UNKNOWN.getValue(), "42edit.key.categories.42edit");
    public static KeyMapping keySpamClick = new KeyMapping("42edit.key.spam_click", InputConstants.UNKNOWN.getValue(), "42edit.key.categories.42edit");
    public static KeyMapping keyZoom = new KeyMapping("42edit.key.zoom", GLFW.GLFW_KEY_R, "42edit.key.categories.42edit");

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
        lastFish = Math.max(lastFish, System.currentTimeMillis() + 100+(int)(RNG.nextDouble()*400) - fishWait);
    }

    // item history
    private static final ListTag itemHistList = new ListTag();
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
                if(itemHistList.size()>54)
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

    public static void clearCapes() {
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
    public static int clientCape = 0;
    public static final CapeTexture[] CLIENT_CAPES = { // Update from https://namemc.com/capes
        new CapeTexture(CapeGroup.NONE, "none", "No cape", null, null, null),

        new CapeTexture(CapeGroup.PUBLIC, "migrator", "Migrator", "http://textures.minecraft.net/texture/2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933"),
        new CapeTexture(CapeGroup.PUBLIC, "vanilla", "Vanilla", "http://textures.minecraft.net/texture/f9a76537647989f9a0b6d001e320dac591c359e9e61a31f4ce11c88f207f0ad4"),
        new CapeTexture(CapeGroup.PUBLIC, "cherry_blossom", "Cherry Blossom", "http://textures.minecraft.net/texture/afd553b39358a24edfe3b8a9a939fa5fa4faa4d9a9c3d6af8eafb377fa05c2bb"),
        new CapeTexture(CapeGroup.PUBLIC, "15th_anniversary", "15th Anniversary", "http://textures.minecraft.net/texture/cd9d82ab17fd92022dbd4a86cde4c382a7540e117fae7b9a2853658505a80625"),
        new CapeTexture(CapeGroup.PUBLIC, "purple_heart", "Purple Heart", "http://textures.minecraft.net/texture/cb40a92e32b57fd732a00fc325e7afb00a7ca74936ad50d8e860152e482cfbde"),
        new CapeTexture(CapeGroup.PUBLIC, "followers", "Follower's", "http://textures.minecraft.net/texture/569b7f2a1d00d26f30efe3f9ab9ac817b1e6d35f4f3cfb0324ef2d328223d350"),
        new CapeTexture(CapeGroup.PUBLIC, "mcc_15th_year", "MCC 15th Year", "http://textures.minecraft.net/texture/56c35628fe1c4d59dd52561a3d03bfa4e1a76d397c8b9c476c2f77cb6aebb1df"),
        new CapeTexture(CapeGroup.PUBLIC, "minecraft_experience", "Minecraft Experience", "http://textures.minecraft.net/texture/7658c5025c77cfac7574aab3af94a46a8886e3b7722a895255fbf22ab8652434"),
        new CapeTexture(CapeGroup.PUBLIC, "mojang_office", "Mojang Office", "http://textures.minecraft.net/texture/5c29410057e32abec02d870ecb52ec25fb45ea81e785a7854ae8429d7236ca26"),

        new CapeTexture(CapeGroup.MINECON, "minecon_2011", "MineCon 2011", "http://textures.minecraft.net/texture/953cac8b779fe41383e675ee2b86071a71658f2180f56fbce8aa315ea70e2ed6"),
        new CapeTexture(CapeGroup.MINECON, "minecon_2012", "MineCon 2012", "http://textures.minecraft.net/texture/a2e8d97ec79100e90a75d369d1b3ba81273c4f82bc1b737e934eed4a854be1b6"),
        new CapeTexture(CapeGroup.MINECON, "minecon_2013", "MineCon 2013", "http://textures.minecraft.net/texture/153b1a0dfcbae953cdeb6f2c2bf6bf79943239b1372780da44bcbb29273131da"),
        new CapeTexture(CapeGroup.MINECON, "minecon_2015", "MineCon 2015", "http://textures.minecraft.net/texture/b0cc08840700447322d953a02b965f1d65a13a603bf64b17c803c21446fe1635"),
        new CapeTexture(CapeGroup.MINECON, "minecon_2016", "MineCon 2016", "http://textures.minecraft.net/texture/e7dfea16dc83c97df01a12fabbd1216359c0cd0ea42f9999b6e97c584963e980"),
        new CapeTexture(CapeGroup.OTHER, "founders", "Founder's"),

        new CapeTexture(CapeGroup.PRIVATE, "realms_mapmaker", "Realms Mapmaker", "http://textures.minecraft.net/texture/17912790ff164b93196f08ba71d0e62129304776d0f347334f8a6eae509f8a56"),
        new CapeTexture(CapeGroup.PRIVATE, "mojira_moderator", "Mojira Moderator", "http://textures.minecraft.net/texture/ae677f7d98ac70a533713518416df4452fe5700365c09cf45d0d156ea9396551"),
        new CapeTexture(CapeGroup.PRIVATE, "translator", "Translator", "http://textures.minecraft.net/texture/1bf91499701404e21bd46b0191d63239a4ef76ebde88d27e4d430ac211df681e"),
        new CapeTexture(CapeGroup.PRIVATE, "cobalt", "Cobalt", "http://textures.minecraft.net/texture/ca35c56efe71ed290385f4ab5346a1826b546a54d519e6a3ff01efa01acce81"),
        new CapeTexture(CapeGroup.PRIVATE, "scrolls", "Scrolls", "http://textures.minecraft.net/texture/3efadf6510961830f9fcc077f19b4daf286d502b5f5aafbd807c7bbffcaca245"),

        new CapeTexture(CapeGroup.MOJANG, "mojang_classic", "Mojang (Classic)", "http://textures.minecraft.net/texture/8f120319222a9f4a104e2f5cb97b2cda93199a2ee9e1585cb8d09d6f687cb761"),
        new CapeTexture(CapeGroup.MOJANG, "mojang", "Mojang", "http://textures.minecraft.net/texture/5786fe99be377dfb6858859f926c4dbc995751e91cee373468c5fbf4865e7151"),
        new CapeTexture(CapeGroup.MOJANG, "mojang_studios", "Mojang Studios", "http://textures.minecraft.net/texture/9e507afc56359978a3eb3e32367042b853cddd0995d17d0da995662913fb00f7"),

        new CapeTexture(CapeGroup.OTHER, "spartan", "Spartan"),
        new CapeTexture(CapeGroup.OTHER, "christmas", "Christmas"),
        new CapeTexture(CapeGroup.OTHER, "42", "42", null, "42Richtofen42's OptiFine cape") // edit http://s.optifine.net/capes/42Richtofen42.png to 128x64
    };
    public static String USERNAME = "";
    public static IntArrayTag UUID = new IntArrayTag(new int[]{0,0,0,0});
    public record CapeTexture(CapeGroup group, String id, String name, String link, String desc, ResourceLocation identifier) {

        public CapeTexture(CapeGroup group, String id, String name) {
            this(group, id, name, null, null);
        }

        public CapeTexture(CapeGroup group, String id, String name, String link) {
            this(group, id, name, link, null);
        }

        public CapeTexture(CapeGroup group, String id, String name, String link, String desc) {
            this(group, id, name, link, desc, ResourceLocation.fromNamespaceAndPath("42edit", "textures/cape/"+id+".png"));
        }

    }
    public enum CapeGroup {
        NONE,       // empty cape
        PUBLIC,     // easily available for many players
        PRIVATE,    // very exclusive
        MINECON,    // classic minecon capes
        MOJANG,     // mojang
        OTHER       // bedrock, skin pack, custom, archived, etc
    }

    //skin testing
    public static boolean showClientSkin = false;
    public static boolean clientSkinSlim = false;
    public static String customSkinName = "";
    public static final ResourceLocation CUSTOM_SKIN_ID = ResourceLocation.fromNamespaceAndPath("42edit","cache/custom_skin");

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
            int i = (int)(Math.random()*SECRETSOUNDS.length);
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
    public static final ItemStack HEAD42 = BlackMagick.itemFromNbtStatic(BlackMagick.validCompoundFromString("{id:player_head,components:{profile:{name:\"42Richtofen42\","
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlc"
        +"yIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDliNjliNWU3MzVlYjUyMmIyNGM2OTczNTQ5ZGRhODMzYjE1ZD"
        +"kxYjg3NDM1NjRjZmIxN2QwZjk2MWMwZjU0Ig0KICAgIH0NCiAgfQ0KfQ==\"}]}}}"));
    public static final CompoundTag BANNER42 = BlackMagick.validCompoundFromString("{id:red_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:curly_border\"}"
        +",{color:\"black\",pattern:\"minecraft:bricks\"},{color:\"red\",pattern:\"minecraft:triangle_bottom\"},{color:\"black\",pattern:\"minecraft:triangle_bottom\"}"
        +",{color:\"purple\",pattern:\"minecraft:flower\"},{color:\"black\",pattern:\"minecraft:gradient\"}]}}");
    public static final CompoundTag BANNERBRICK = BlackMagick.validCompoundFromString("{id:orange_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:gradient\"}"
        +",{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"orange\",pattern:\"minecraft:triangles_bottom\"}"
        +",{color:\"red\",pattern:\"minecraft:bricks\"},{color:\"green\",pattern:\"minecraft:creeper\"}]}}");
    public static final ItemStack ITEM_ERROR = BlackMagick.itemFromNbtStatic(BlackMagick.validCompoundFromString("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcy"
        +"IgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZTE4MjM2NzExOTYzMTMxNzY5MjM0Mzc4OGNkNWM4NTRjMTNiNDQ5"
        +"ZDM2ZTYyMmI4NTU0YTU2MzhlZDM4NTkzIg0KICAgIH0NCiAgfQ0KfQ==\"}]}}}"));
    public static final ItemStack ITEM_QUESTION = BlackMagick.itemFromNbtStatic(BlackMagick.validCompoundFromString("{components:{\"minecraft:profile\":"
        +"{id:[I;1617833968,-310949822,-1653808685,840726584],name:\"MHF_Question\",properties:[{name:\"textures\",value:\"ewogICJzaWduYXR1cmVSZXF1aXJlZCIg"
        +"OiBmYWxzZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzNGUwNjNjYWZiNDY3Y"
        +"TVjOGRlNDNlYzc4NjE5Mzk5ZjM2OWY0YTUyNDM0ZGE4MDE3YTk4M2NkZDkyNTE2YTAiCiAgICB9CiAgfQp9\"}]}},count:1,id:\"minecraft:player_head\"}"));

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
        clearCapes();

        getSavedItems(); // used to show log errors
        refreshWebItems(false);

        FileTools.scanModFiles();

        for(String c : ComponentHelper.LIST_DATA_COMPONENT_TYPE.getList()) {
            // this will log warnings if ComponentHelper doesnt include a vanilla component
            ComponentHelper.getPathInfo("components."+c);
        }

        logInfo("Client initialized");
    }

    public static void clientTick(Minecraft client) {

        if(client.player == null || client.screen != null) {
            autoClicker = false;
            autoMove = false;
        }

        // magickgui
        if(keyMagickGui.consumeClick()) {
            switch(quickScreen) {
                case NONE: client.setScreen(new MagickGui()); break;
                case ITEM_BUILDER: client.setScreen(new ItemBuilder()); break;
                case SECRET_SCREEN: client.setScreen(new SecretScreen()); break;
                case LOG_SCREEN: client.setScreen(new LogScreen()); break;
                case AUTO_CLICK: client.setScreen(new AutoClick()); break;
                case CAPES: client.setScreen(new Capes()); break;
                case HACKS: client.setScreen(new Hacks()); break;
                case DEBUG_SCREEN: client.setScreen(new DebugScreen()); break;
            }
        }

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
            lastFish = System.currentTimeMillis() + 100+(int)(RNG.nextDouble()*400);
        }
        if(didFish && System.currentTimeMillis()>=(lastFish+fishWait)) {
            if(autoFish && !autoClicker && client.screen == null && ((!client.player.getMainHandItem().isEmpty()
                    && client.player.getMainHandItem().is(Items.FISHING_ROD)) || (client.player.getMainHandItem().isEmpty()
                    && !client.player.getOffhandItem().isEmpty() && client.player.getOffhandItem().is(Items.FISHING_ROD))) ) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
            }
            didFish = false;
            lastFish = System.currentTimeMillis() + 100+(int)(RNG.nextDouble()*400);
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
            int slot = (int) (RNG.nextDouble() * randoSlots.length);
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

                tag.put("block_state",BlackMagick.nbtFromString(states));
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
        } catch(Exception ex) {
            logError("Failed to show toast ("+title.getString()+") ("+desc.getString()+"): "+ex.getMessage());
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

        ComponentHelper.runAllListMethods();

        clearCapes();
        setCustomSkin(null);

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
        options.getString("custom_cape").ifPresent(s -> {
            clientCape = 0;
            for(int i=0; i<CLIENT_CAPES.length; i++)
                if(CLIENT_CAPES[i].id().equals(s))
                    clientCape = i;
        });
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
                    logError("Failed to set keybind for binding "+k+" to key "+BlackMagick.nbtToString(c.get(k)));
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
        options.remove("item_warning_override");
        if(options.getCompoundOrEmpty("keybinds").isEmpty())
            options.remove("keybinds");
        options.remove("opticapes");
        options.remove("web_items");
        options.remove("web_items_url");

        optionsExtra = null;
        if(!options.isEmpty()) {
            logWarn("Config file contains unknown keys: "+BlackMagick.nbtToString(options));
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
        options.putString("custom_cape",CLIENT_CAPES[clientCape].id());
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
        ItemBuilder.savedItemsError = false;
        CompoundTag savedItemsNbt = FileTools.readCompoundFromFile(FileTools.FILE_SAVED_ITEMS);
        if(savedItemsNbt == null) {
            savedItemsNbt = new CompoundTag();
            if(FileTools.testFileExists(FileTools.FILE_SAVED_ITEMS)) {
                String fileString = FileTools.readStringFromFile(FileTools.FILE_SAVED_ITEMS);
                if(fileString != null && !fileString.isEmpty()) {
                    ItemBuilder.savedItemsError = true;
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
                                itemString = BlackMagick.nbtToString(itemHolder.getCompound("item").get());
                            itemHolder.remove("item");
                            if(itemString.isEmpty() || itemString.equals("{}"))
                                continue;
                            int slot = itemHolder.getInt("slot").get();
                            itemHolder.remove("slot");
                            if(!itemHolder.isEmpty()) {
                                FortytwoEdit.logError("Saved item contains unknown keys: "+BlackMagick.nbtToString(itemHolder));
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
                    FortytwoEdit.logError("Saved items file contains invalid entries. After saving, the following will be deleted: "+BlackMagick.nbtToString(unknownItemHolders));
                }
            }
        }
        if(!foundItems && !savedItemsNbt.isEmpty()) {
            logError("Failed to read saved items: " + BlackMagick.nbtToString(savedItemsNbt));
            ItemBuilder.savedItemsError = true;
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
            } catch(Exception ex) {
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

            Tag parseWebJson = BlackMagick.nbtFromString(webJson);
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
            ListTag jsonItems = null;

            for(int i=0; i<versionsList.size(); i++) {
                if(versionsList.get(i).getId() == Tag.TAG_COMPOUND) {
                    CompoundTag versionData = versionsList.getCompound(i).get();
                    if(versionData.getInt("version").isPresent() && versionData.getList("items").isPresent()) {
                        int versionNum = versionData.getInt("version").get();
    
                        if(itemsVer == -1 || (versionNum > itemsVer && versionNum <= SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA))) {
                            itemsVer = versionNum;
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
                                itemString = BlackMagick.nbtToString(itemHolder.getCompound("item").get());
                            webItems.add(itemString);
                        }
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