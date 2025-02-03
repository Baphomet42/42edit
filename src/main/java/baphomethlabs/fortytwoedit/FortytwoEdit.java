package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import baphomethlabs.fortytwoedit.mixin.HotbarStorageAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;

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
    public static KeyBinding keyAfkClick = new KeyBinding("42edit.key.afk_click", GLFW.GLFW_KEY_MINUS, "42edit.key.categories.42edit");
    public static KeyBinding keyAfkMove = new KeyBinding("42edit.key.afk_move", GLFW.GLFW_KEY_EQUAL, "42edit.key.categories.42edit");
    public static KeyBinding keyFreeLook = new KeyBinding("42edit.key.free_look", GLFW.GLFW_KEY_LEFT_ALT, "42edit.key.categories.42edit");
    public static KeyBinding keyMagickGui = new KeyBinding("42edit.key.open_magick_gui", GLFW.GLFW_KEY_J, "42edit.key.categories.42edit");
    public static KeyBinding keyMod = new KeyBinding("42edit.key.key_mod", InputUtil.UNKNOWN_KEY.getCode(), "42edit.key.categories.42edit");
    public static KeyBinding keySpamClick = new KeyBinding("42edit.key.spam_click", InputUtil.UNKNOWN_KEY.getCode(), "42edit.key.categories.42edit");
    public static KeyBinding keyZoom = new KeyBinding("42edit.key.zoom", GLFW.GLFW_KEY_R, "42edit.key.categories.42edit");

    public static final KeyBinding[] KEYBINDS = new KeyBinding[]{
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
    private static NbtCompound optionsExtra = null;

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
    private static final NbtList itemHistList = new NbtList();
    public static void addItemHist(ItemStack item) {
        if(item != null && !item.isEmpty())
            addItemHist(BlackMagick.itemToNbtStorage(item));
    }
    public static void addItemHist(NbtCompound itemNbt) {
        if(itemNbt != null && !itemNbt.isEmpty() && itemNbt.contains("components",NbtElement.COMPOUND_TYPE)) {
            NbtCompound item = itemNbt.copy();
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
    public static NbtList getItemHist() {
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

    private static ArrayList<String> capeNames = new ArrayList<>(); // all cached names
    private static ArrayList<String> capeNames2 = new ArrayList<>(); // names with capes

    public static int debugCapeNamesSize() {
        return capeNames.size();
    }

    public static int debugCapeNames2Size() {
        return capeNames2.size();
    }

    public static void clearCapes() {
        capeNames.clear();
        final MinecraftClient client = MinecraftClient.getInstance();
        for(String name : capeNames2) {
            client.getTextureManager().destroyTexture(getCapeCacheID(name));
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
        final MinecraftClient client = MinecraftClient.getInstance();
        if(capeNames.isEmpty() && !name.equals(client.getSession().getUsername()))
            tryLoadCape(client.getSession().getUsername());
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
                    cape.setColorArgb(x, y, capeInp.getColorArgb(x, y));

            capeInp.close();
            client.getTextureManager().registerTexture(getCapeCacheID(name), new NativeImageBackedTexture(cape));
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
    private static Identifier getCapeCacheID(String name) {
        return Identifier.of("42edit","cache/cape/"+name.toLowerCase());
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
    public static NbtIntArray UUID = new NbtIntArray(new int[]{0,0,0,0});
    public record CapeTexture(CapeGroup group, String id, String name, String link, String desc, Identifier identifier) {

        public CapeTexture(CapeGroup group, String id, String name) {
            this(group, id, name, null, null);
        }

        public CapeTexture(CapeGroup group, String id, String name, String link) {
            this(group, id, name, link, null);
        }

        public CapeTexture(CapeGroup group, String id, String name, String link, String desc) {
            this(group, id, name, link, desc, Identifier.of("42edit", "textures/cape/"+id+".png"));
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
    public static Identifier customSkinID = Identifier.of("42edit","cache/custom_skin");

    public static boolean setCustomSkin(File file) {
        final MinecraftClient client = MinecraftClient.getInstance();
        customSkinName = "";
        showClientSkin = false;
        client.getTextureManager().destroyTexture(customSkinID);

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
                            skin.setColorArgb(x, y, skinFile.getColorArgb(x, y));

                    skinFile.close();
                    customSkinName = file.getName();
                    client.getTextureManager().registerTexture(customSkinID, new NativeImageBackedTexture(skin));
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
    private static Perspective lastPerspective;
    public static float[] cameraRotation = {0f,0f};

    //see feature items
    public static final FeatureSet FEATURES = FeatureSet.of(FeatureFlags.VANILLA);

    //supersecretsettings
    public static final Identifier[] SUPER_SECRET_SETTING_PROGRAMS = new Identifier[]{/*Identifier.of("42edit","shaders/post/notch.json"), Identifier.of("42edit","shaders/post/fxaa.json"), Identifier.of("42edit","shaders/post/art.json"), Identifier.of("42edit","shaders/post/bumpy.json"), Identifier.of("42edit","shaders/post/blobs2.json"), Identifier.of("42edit","shaders/post/pencil.json"), Identifier.of("42edit","shaders/post/color_convolve.json"), Identifier.of("42edit","shaders/post/deconverge.json"), Identifier.of("42edit","shaders/post/flip.json"),*/ Identifier.ofVanilla("invert"),/* Identifier.of("42edit","shaders/post/ntsc.json"), Identifier.of("42edit","shaders/post/outline.json"), Identifier.of("42edit","shaders/post/phosphor.json"), Identifier.of("42edit","shaders/post/scan_pincushion.json"), Identifier.of("42edit","shaders/post/sobel.json"), Identifier.of("42edit","shaders/post/bits.json"), Identifier.of("42edit","shaders/post/desaturate.json"), Identifier.of("42edit","shaders/post/green.json"), Identifier.of("42edit","shaders/post/blur.json"), Identifier.of("42edit","shaders/post/wobble.json"), Identifier.of("42edit","shaders/post/blobs.json"), Identifier.of("42edit","shaders/post/antialias.json"),*/ Identifier.ofVanilla("creeper"), Identifier.ofVanilla("spider")};
    private static int superSecretSettingIndex = SUPER_SECRET_SETTING_PROGRAMS.length;
    private static final Identifier[] SECRETSOUNDS = getSecretSounds();
    private static Identifier[] getSecretSounds() {
        Set<Identifier> sounds = Registries.SOUND_EVENT.getIds();
        ArrayList<Identifier> valid = new ArrayList<>();
        for(Identifier sound: sounds) {
            if(sound.getPath().contains("entity.") || sound.getPath().contains("block.") || sound.getPath().contains("weather.") || sound.getPath().contains("item."))
                valid.add(sound);
        }
        Identifier[] arr = new Identifier[valid.size()];
        return valid.toArray(arr);
    }
    private static void secretSound() {
        if(SECRETSOUNDS != null && SECRETSOUNDS.length > 0) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int i = (int)(Math.random()*SECRETSOUNDS.length);
            try {
                client.player.playSoundToPlayer(SoundEvent.of(SECRETSOUNDS[i]), SoundCategory.MASTER, 1f, .5f);
            } catch(Exception ex) {}
        }
    }
    public static void cycleSuperSecretSetting() {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.getCameraEntity() instanceof PlayerEntity) {
            if(client.gameRenderer.getPostProcessorId() != null) {
                client.gameRenderer.clearPostProcessor();
            }
            superSecretSettingIndex = (superSecretSettingIndex + 1) % (SUPER_SECRET_SETTING_PROGRAMS.length + 1);
            if(superSecretSettingIndex == SUPER_SECRET_SETTING_PROGRAMS.length) {
                ((GameRendererInvoker)client.gameRenderer).setPostProcessorEnabled(false);
            } else {
                ((GameRendererInvoker)client.gameRenderer).invokeSetPostProcessor(SUPER_SECRET_SETTING_PROGRAMS[superSecretSettingIndex]);
            }
        }
        secretSound();
    }

    //items
    public static final ItemStack HEAD42 = BlackMagick.itemFromNbtStatic((NbtCompound)BlackMagick.nbtFromString("{id:player_head,components:{profile:{name:\"42Richtofen42\","
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlc"
        +"yIgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDliNjliNWU3MzVlYjUyMmIyNGM2OTczNTQ5ZGRhODMzYjE1ZD"
        +"kxYjg3NDM1NjRjZmIxN2QwZjk2MWMwZjU0Ig0KICAgIH0NCiAgfQ0KfQ==\"}]}}}"));
    public static final NbtCompound BANNER42 = (NbtCompound)BlackMagick.nbtFromString("{id:red_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:curly_border\"}"
        +",{color:\"black\",pattern:\"minecraft:bricks\"},{color:\"red\",pattern:\"minecraft:triangle_bottom\"},{color:\"black\",pattern:\"minecraft:triangle_bottom\"}"
        +",{color:\"purple\",pattern:\"minecraft:flower\"},{color:\"black\",pattern:\"minecraft:gradient\"}]}}");
    public static final NbtCompound BANNERBRICK = (NbtCompound)BlackMagick.nbtFromString("{id:orange_banner,components:{banner_patterns:[{color:\"black\",pattern:\"minecraft:gradient\"}"
        +",{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"black\",pattern:\"minecraft:gradient\"},{color:\"orange\",pattern:\"minecraft:triangles_bottom\"}"
        +",{color:\"red\",pattern:\"minecraft:bricks\"},{color:\"green\",pattern:\"minecraft:creeper\"}]}}");
    public static final ItemStack ITEM_ERROR = BlackMagick.itemFromNbtStatic((NbtCompound)BlackMagick.nbtFromString("{id:player_head,components:{profile:{"
        +"properties:[{name:\"textures\",value:\"ew0KICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsDQogICJ0ZXh0dXJlcy"
        +"IgOiB7DQogICAgIlNLSU4iIDogew0KICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZTE4MjM2NzExOTYzMTMxNzY5MjM0Mzc4OGNkNWM4NTRjMTNiNDQ5"
        +"ZDM2ZTYyMmI4NTU0YTU2MzhlZDM4NTkzIg0KICAgIH0NCiAgfQ0KfQ==\"}]}}}"));
    public static final ItemStack ITEM_QUESTION = BlackMagick.itemFromNbtStatic((NbtCompound)BlackMagick.nbtFromString("{components:{\"minecraft:profile\":"
        +"{id:[I;1617833968,-310949822,-1653808685,840726584],name:\"MHF_Question\",properties:[{name:\"textures\",value:\"ewogICJzaWduYXR1cmVSZXF1aXJlZCIg"
        +"OiBmYWxzZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzNGUwNjNjYWZiNDY3Y"
        +"TVjOGRlNDNlYzc4NjE5Mzk5ZjM2OWY0YTUyNDM0ZGE4MDE3YTk4M2NkZDkyNTE2YTAiCiAgICB9CiAgfQp9\"}]}},count:1,id:\"minecraft:player_head\"}"));

    //saved items
    public static final int SAVED_ROWS = 12;

    //web items
    public static boolean webItemsAuto = true;
    public static List<String> webItems = Lists.newArrayList();
    private static final String WEB_ITEMS_URL_DEFAULT = "https://baphomet42.github.io/mc/blackmarket/items.json";
    private static String webItemsUrlOverride = "";

    // itemstack warning
    private static final String[] ITEM_WARNING_MODES = {"vanilla","hide","smart"};
    private static int itemWarningMode = 0;
    public static String getItemWarningMode() {
        return ITEM_WARNING_MODES[itemWarningMode];
    }
    public static void cycleItemWarningMode(boolean right) {
        int newWarn = itemWarningMode;
        if(right) {
            newWarn++;
            if(newWarn>=ITEM_WARNING_MODES.length)
                newWarn = 0;
        }
        else {
            newWarn--;
            if(newWarn<0)
                newWarn = ITEM_WARNING_MODES.length-1;
        }

        readOptions();
        itemWarningMode = newWarn;
        updateOptions();
    }


    @Override
    public void onInitializeClient() {
        logInfo("Loading 42edit client");

        final MinecraftClient client = MinecraftClient.getInstance();

        //options
        readOptions();

        // custom capes
        USERNAME = client.getSession().getUsername();
        if(client.getSession().getUuidOrNull() != null)
            UUID = NbtHelper.fromUuid(client.getSession().getUuidOrNull());
        clearCapes();

        getSavedItems(); // used to show log errors
        refreshWebItems(false);

        FileTools.scanModFiles();

        for(String c : ComponentHelper.LIST_DATA_COMPONENT_TYPE.get()) {
            // this will log warnings if ComponentHelper doesnt include a vanilla component
            ComponentHelper.getPathInfo("components."+c);
        }

        logInfo("Client initialized");
    }

    public static void clientTick(MinecraftClient client) {

        // magickgui
        if(keyMagickGui.wasPressed()) {
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
        if(keyZoom.isPressed() && !zoomed) {
            smooth = client.options.smoothCameraEnabled;
            client.options.smoothCameraEnabled = true;
            zoomed = true;
        }
        else if(!keyZoom.isPressed() && zoomed) {
            client.options.smoothCameraEnabled = smooth;
            zoomed = false;
        }

        // afkMove
        if(keyAfkMove.wasPressed()) {
            autoMove = !autoMove;
            client.options.forwardKey.setPressed(false);
            while(client.options.forwardKey.wasPressed()) {}
        }
        if(autoMove && client.player != null) {
            if(client.options.forwardKey.wasPressed()) {
                autoMove = false;
                client.options.forwardKey.setPressed(false);
                while(client.options.forwardKey.wasPressed()) {}
            }
            else
                client.options.forwardKey.setPressed(true);
        }

        //afkClick
        if(keyAfkClick.wasPressed()) {
            autoClicker = !autoClicker;
            client.options.useKey.setPressed(false);
            client.options.attackKey.setPressed(false);
            while(client.options.useKey.wasPressed()) {}
            while(client.options.attackKey.wasPressed()) {}
        }
        if(autoClicker && client.player != null) {
            if(autoClick) {
                client.options.useKey.setPressed(true);
            }
            if(autoAttack && System.currentTimeMillis()>=lastAttack + attackWait && client.crosshairTarget instanceof EntityHitResult) {
                lastAttack = System.currentTimeMillis();
                suppressKeybind = true;
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.attackKey).getBoundKey());
                suppressKeybind = false;
            }
            if(autoMine) {
                client.options.attackKey.setPressed(true);
            }
        }

        //autoFish
        if(autoFishClickQueue && System.currentTimeMillis()>=(lastFish+fishWait)) {
            if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                    && client.player.getMainHandStack().isOf(Items.FISHING_ROD)) || (client.player.getMainHandStack().isEmpty()
                    && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().isOf(Items.FISHING_ROD))) ) {
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                didFish = true;
            }
            autoFishClickQueue = false;
            lastFish = System.currentTimeMillis() + 100+(int)(RNG.nextDouble()*400);
        }
        if(didFish && System.currentTimeMillis()>=(lastFish+fishWait)) {
            if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                    && client.player.getMainHandStack().isOf(Items.FISHING_ROD)) || (client.player.getMainHandStack().isEmpty()
                    && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().isOf(Items.FISHING_ROD))) ) {
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
            }
            didFish = false;
            lastFish = System.currentTimeMillis() + 100+(int)(RNG.nextDouble()*400);
        }

        //freelook
        if(keyFreeLook.isPressed()) {
            if(!isFreeLooking) {
                lastPerspective = client.options.getPerspective();
                Entity view = client.getCameraEntity() == null ? client.player : client.getCameraEntity();
                cameraRotation[0] = view.getYaw();
                cameraRotation[1] = view.getPitch();

                if(lastPerspective == Perspective.FIRST_PERSON)
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);

                isFreeLooking = true;
            }
        }
        else if(isFreeLooking) {
            isFreeLooking = false;
            client.options.setPerspective(lastPerspective);
        }

        //spam
        if(keySpamClick.isPressed() && System.currentTimeMillis()>=lastSpam + 20) {
            if(keyMod.isPressed())
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.attackKey).getBoundKey());
            else {
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                if(randoMode)
                    changeRandoSlot();
            }
            lastSpam = System.currentTimeMillis();
        }

        // rando
        if(randoMode) {
            if(client.options.useKey.isPressed())
                changeRandoSlot();
        }
    }

    public static void updateAutoClick(boolean click, boolean mine, boolean attack, int wait) {
        final MinecraftClient client = MinecraftClient.getInstance();
        autoClicker = false;

        autoClick = click;
        autoMine = mine;
        autoAttack = attack;
        attackWait = wait;
        if(wait < 1)
            attackWait = 1;
        else if(wait > 9999)
            attackWait = 9999;

        client.options.useKey.setPressed(false);
        client.options.attackKey.setPressed(false);
    }

    private static boolean testRandoSlot() {
        final MinecraftClient client = MinecraftClient.getInstance();
        int selected = client.player.getInventory().selectedSlot + 1;
        for(int i = 0; i < randoSlots.length; i++) {
            if(randoSlots[i] == selected)
                return true;
        }
        return false;
    }

    public static void changeRandoSlot() {
        if(randoSlots != null && testRandoSlot()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int slot = (int) (RNG.nextDouble() * randoSlots.length);
            slot = randoSlots[slot];
            client.player.getInventory().selectedSlot = slot - 1;
        }
    }

    public static ItemStack copyLookAt() {
        final MinecraftClient client = MinecraftClient.getInstance();
        HitResult hitResult = client.crosshairTarget;
        if(hitResult == null) {
            return null;
        }
        if(hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = client.player.getWorld().getBlockState(blockPos);

            if(!blockState.getProperties().isEmpty()) {
                NbtCompound stack = new NbtCompound();
                stack.put("id",NbtString.of(blockState.getBlock().asItem().toString()));
                NbtCompound tag = new NbtCompound();

                String states = "";
                states += "{";
                boolean bl = false;
                for(Map.Entry<Property<?>,Comparable<?>> entry : blockState.getEntries().entrySet()) {
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
        return property.name(state.get(property));
    }

    private static final String LOG_PREFIX = "(42edit) ";
    private static final SystemToast.Type TOAST_TYPE = new SystemToast.Type();
    private static final MutableText TOAST_PREFIX = Text.empty().append("").append(Text.empty().append("(42edit) ").formatted(Formatting.BLACK));

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
        showToast(Text.of(title),Text.of(desc));
    }

    public static void showToast(Text title, Text desc) {
        final MinecraftClient client = MinecraftClient.getInstance();
        try {
            client.getToastManager().add(new SystemToast(TOAST_TYPE, TOAST_PREFIX.copy().append(title), desc));
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

        final MinecraftClient client = MinecraftClient.getInstance();
        ((HotbarStorageAccessor)client.getCreativeHotbarStorage()).setLoaded(false);
        client.getCreativeHotbarStorage().getSavedHotbar(0);

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
            if(!KEYBINDS[i].getBoundKeyTranslationKey().equals(KEYBINDS_CONFIG_CACHE[i])) {
                diff = true;
                break;
            }
        }
        if(diff)
            updateOptions();
    }

    public static void readOptions() {
        NbtCompound options = FileTools.readCompoundFromFile(FileTools.FILE_OPTIONS);
        if(options == null)
            options = new NbtCompound();

        // keep options consistent
        if(options.contains("custom_cape_toggle",NbtElement.BYTE_TYPE))
            showClientCape = options.getByte("custom_cape_toggle") == 1;
        if(options.contains("custom_cape",NbtElement.STRING_TYPE)) {
            clientCape = 0;
            String capeName = options.getString("custom_cape");
            for(int i=0; i<CLIENT_CAPES.length; i++)
                if(capeName.equals(CLIENT_CAPES[i].id()))
                    clientCape = i;
        }
        if(options.contains("item_warning_override",NbtElement.STRING_TYPE)) {
            String warnModeName = options.getString("item_warning_override");
            itemWarningMode = 0;
            for(int i=0; i<ITEM_WARNING_MODES.length; i++)
                if(ITEM_WARNING_MODES[i].equals(warnModeName))
                    itemWarningMode = i;
        }
        if(options.contains("keybinds",NbtElement.COMPOUND_TYPE)) {
            NbtCompound keybindsCompound = options.getCompound("keybinds");
            Set<String> foundKeys = Sets.newHashSet();
            for(String k : keybindsCompound.getKeys()) {
                for(int i=0; i<KEYBINDS.length; i++) {
                    if(KEYBINDS[i].getTranslationKey().equals(k)) {
                        try {
                            KEYBINDS[i].setBoundKey(InputUtil.fromTranslationKey(keybindsCompound.getString(k)));
                        }
                        catch(Exception ex) {
                            logError("Failed to set keybind for binding "+k+" to key "+BlackMagick.nbtToString(keybindsCompound.get(k)));
                        }
                        foundKeys.add(k);
                    }
                }
            }
            if(!foundKeys.isEmpty()) {
                for(String k : foundKeys)
                    keybindsCompound.remove(k);
                KeyBinding.updateKeysByCode();
            }
        }
        if(options.contains("opticapes",NbtElement.BYTE_TYPE))
            opticapesOn = options.getByte("opticapes") == 1;
        if(options.contains("web_items",NbtElement.BYTE_TYPE))
            webItemsAuto = options.getByte("web_items") == 1;
        if(options.contains("web_items_url",NbtElement.STRING_TYPE)) {
            webItemsUrlOverride = options.getString("web_items_url");
            if(webItemsUrlOverride.length()>0 && !webItemsUrlOverride.startsWith("https://") && !webItemsUrlOverride.startsWith("http://")) {
                logError("Invalid web_items_url (expected 'http://' or 'https://'): "+webItemsUrlOverride);
                webItemsUrlOverride = "";
            }
        }

        // keep options consistent
        options.remove("file_format");
        options.remove("custom_cape_toggle");
        options.remove("custom_cape");
        options.remove("item_warning_override");
        if(options.getCompound("keybinds").isEmpty())
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
        NbtCompound options = new NbtCompound();
        if(optionsExtra != null)
            options = optionsExtra.copy();

        // keep options consistent
        options.putInt("file_format",FileTools.FILE_FORMAT);
        options.putBoolean("custom_cape_toggle",showClientCape);
        options.putString("custom_cape",CLIENT_CAPES[clientCape].id());
        options.putString("item_warning_override",getItemWarningMode());
        NbtCompound keysCompound = options.getCompound("keybinds");
        for(int i=0; i<KEYBINDS.length; i++) {
            keysCompound.put(KEYBINDS[i].getTranslationKey(),NbtString.of(KEYBINDS[i].getBoundKeyTranslationKey()));
            KEYBINDS_CONFIG_CACHE[i] = KEYBINDS[i].getBoundKeyTranslationKey();
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
        NbtCompound savedItemsNbt = FileTools.readCompoundFromFile(FileTools.FILE_SAVED_ITEMS);
        if(savedItemsNbt == null) {
            savedItemsNbt = new NbtCompound();
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
        if(savedItemsNbt.contains("items",NbtElement.LIST_TYPE)) {
            NbtList storedItems = (NbtList)savedItemsNbt.get("items");
            foundItems = true;
            if(!storedItems.isEmpty() && storedItems.getHeldType() == NbtElement.COMPOUND_TYPE) {
                boolean itemsOutOfRange = false;
                final int MAX_SAVED_ITEM_SLOT = FortytwoEdit.SAVED_ROWS*9-1;
                int currentDupeSlot = MAX_SAVED_ITEM_SLOT+1;
                NbtList unknownItemHolders = new NbtList();
                for(int i=0; i<storedItems.size(); i++) {
                    NbtCompound itemHolder = storedItems.getCompound(i);
                    if(itemHolder.isEmpty())
                        continue;
                    if((itemHolder.contains("item",NbtElement.COMPOUND_TYPE) || itemHolder.contains("item",NbtElement.STRING_TYPE))
                    && itemHolder.contains("slot",NbtElement.INT_TYPE)) {
                        String itemString = itemHolder.getString("item");
                        if(itemHolder.get("item").getType()==NbtElement.COMPOUND_TYPE)
                            itemString = BlackMagick.nbtToString(itemHolder.getCompound("item"));
                        itemHolder.remove("item");
                        if(itemString.isEmpty() || itemString.equals("{}"))
                            continue;
                        int slot = itemHolder.getInt("slot");
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
        NbtList itemsList = new NbtList();
        for(int slot : BlackMagick.sortIntSet(savedItemsMap.keySet())) {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("slot",slot);
            nbt.putString("item",savedItemsMap.get(slot));
            itemsList.add(nbt);
        }

        NbtCompound savedItemsNbt = new NbtCompound();
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
    public static NbtCompound refreshWebItems(boolean forceWeb) {
        webItems.clear();
        NbtCompound result = new NbtCompound();

        NbtCompound cacheNbt = FileTools.readCompoundFromFile(FileTools.FILE_WEB_CACHE);
        if(cacheNbt == null)
            cacheNbt = new NbtCompound();
        NbtCompound newItems = cacheNbt.copy();

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

            NbtElement parseWebJson = BlackMagick.nbtFromString(webJson);
            if(parseWebJson != null && parseWebJson.getType()==NbtElement.COMPOUND_TYPE) {
                NbtCompound webNbt = (NbtCompound)parseWebJson;
                newItems = webNbt.copy();

                if(BlackMagick.elementsEqual(webNbt,cacheNbt)) {
                    logInfo("Black Market items are up to date");
                    result.put("site_match_catch",new NbtCompound());
                }
                else {
                    logInfo("Updating Black Market items");
                    result.put("site_updated_catch",new NbtCompound());
                    FileTools.writeCompoundToFile(FileTools.FILE_WEB_CACHE, newItems, FileDisplayType.TREE_CONDITIONAL_COLLAPSE);
                }
            }
            else if(!didError)
                logError("Failed to parse BaphomethLabs Black Market ("+webItemsUrlActive+"): "+webJson);
        }

        if(newItems != null && newItems.contains("versions",NbtElement.LIST_TYPE) && !((NbtList)newItems.get("versions")).isEmpty()
                && ((NbtList)newItems.get("versions")).get(0).getType()==NbtElement.COMPOUND_TYPE) {

            NbtList versionsList = (NbtList)newItems.get("versions");
            int itemsVer = -1;
            NbtList jsonItems = null;

            for(int i=0; i<versionsList.size(); i++) {
                NbtCompound versionData = (NbtCompound)versionsList.get(i);
                if(versionData.contains("version",NbtElement.INT_TYPE) && versionData.contains("items",NbtElement.LIST_TYPE)) {
                    int versionNum = versionData.getInt("version");

                    if(itemsVer == -1 || (versionNum > itemsVer && versionNum <= SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA))) {
                        itemsVer = versionNum;
                        jsonItems = versionData.getList("items",NbtElement.COMPOUND_TYPE);
                    }
                }
            }

            if(jsonItems != null && !jsonItems.isEmpty()) {
                for(int i=0; i<jsonItems.size(); i++) {
                    NbtCompound itemHolder = (NbtCompound)jsonItems.get(i);
                    if(itemHolder.contains("item",NbtElement.COMPOUND_TYPE) || itemHolder.contains("item",NbtElement.STRING_TYPE)) {
                        String itemString = itemHolder.getString("item");
                        if(itemHolder.get("item").getType()==NbtElement.COMPOUND_TYPE)
                            itemString = BlackMagick.nbtToString(itemHolder.getCompound("item"));
                        webItems.add(itemString);
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