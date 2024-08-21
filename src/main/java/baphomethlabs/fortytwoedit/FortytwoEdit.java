package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import baphomethlabs.fortytwoedit.gui.screen.AutoClick;
import baphomethlabs.fortytwoedit.gui.screen.Capes;
import baphomethlabs.fortytwoedit.gui.screen.Hacks;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import baphomethlabs.fortytwoedit.gui.screen.LogScreen;
import baphomethlabs.fortytwoedit.gui.screen.MagickGui;
import baphomethlabs.fortytwoedit.gui.screen.SecretScreen;
import baphomethlabs.fortytwoedit.mixin.GameRendererInvoker;
import baphomethlabs.fortytwoedit.mixin.KeyBindingAccessor;
import baphomethlabs.fortytwoedit.mixin.TranslationStorageAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;

public class FortytwoEdit implements ClientModInitializer {

    //log
	public static final Logger LOGGER = LoggerFactory.getLogger("42edit");

    //gui
    public static KeyBinding magickGuiKey;
    public static QuickScreen quickScreen = QuickScreen.NONE;
    public enum QuickScreen {
        NONE,

        AUTO_CLICK,
        CAPES,
        HACKS,
        ITEM_BUILDER,
        LOG_SCREEN,
        SECRET_SCREEN
    }

    //options
    private static final int OPTIONS_FORMAT = 1;
    private static NbtCompound optionsExtra;

    // zoom
    public static boolean zoomed = false;
    private static boolean smooth = false;

    // hacks
    public static boolean autoMove = false;
    public static boolean autoClicker = false;
    public static boolean autoClick = true;
    public static boolean autoAttack = false;
    public static boolean autoMine = false;
    public static int attackWait = 1500;
    private static long lastAttack = 0;
    private static long lastSpam = 0;
    public static KeyBinding modKey;
    public static KeyBinding spamClick;
    public static boolean xrayEntity = false;
    public static boolean autoFish = false;
    public static boolean autoFishClick = false;
    private static long lastFish = 0;
    private static boolean didFish = false;
    private static final int fishWait = 1000;
    public static boolean suppressKeybind = false;

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
                if(itemHistList.get(i).asString().equals(item.asString())) {
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
    private static SecureRandom rand = new SecureRandom();
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
            try {
                HttpURLConnection con = (HttpURLConnection)(new URI("http://s.optifine.net/capes/42Richtofen42.png")).toURL().openConnection();
                con.setConnectTimeout(2000);
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
                    connect = true;
                con.disconnect();
            } catch(Exception e) {}
            if(!connect) {
                opticapesWorking = false;
                LOGGER.warn("Failed connection to OptiFine capes");
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
        try {
            URL link = new URI("http://s.optifine.net/capes/" + name + ".png").toURL();
            HttpURLConnection con = (HttpURLConnection)link.openConnection();
            con.setUseCaches(false);
            con.setConnectTimeout(500);
            con.setReadTimeout(500);
            NativeImage capeInp = NativeImage.read(con.getInputStream());
            con.disconnect();
            NativeImage cape = new NativeImage(128, 64, true);

            for (int x = 0; x < capeInp.getWidth(); x++)
                for (int y = 0; y < capeInp.getHeight(); y++)
                    cape.setColor(x, y, capeInp.getColor(x, y));

            capeInp.close();
            client.getTextureManager().registerTexture(Identifier.of("42edit","cache/capes/"+name.toLowerCase()), new NativeImageBackedTexture(cape));
            cape.close();
            capeNames2.add(name);
            return true;
        } catch (Exception e) {
            return false;
        }
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
            this(group, id, name, link, desc, Identifier.of("42edit", "textures/capes/"+id+".png"));
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
        if(file.isFile() && file.getName().endsWith(".png")) {
            try {
                FileInputStream inp = new FileInputStream(file);
                NativeImage skinFile = NativeImage.read(inp);
                inp.close();
                NativeImage skin = new NativeImage(skinFile.getWidth(),skinFile.getHeight(),true);

                for (int x = 0; x < skinFile.getWidth(); x++)
                    for (int y = 0; y < skinFile.getHeight(); y++)
                        skin.setColor(x, y, skinFile.getColor(x, y));

                skinFile.close();
                final MinecraftClient client = MinecraftClient.getInstance();
                customSkinName = file.getName();
                client.getTextureManager().registerTexture(customSkinID, new NativeImageBackedTexture(skin));
                skin.close();
                return true;
            } catch(Exception e) {}
        }
        return false;
    }

    //freelook
    public static boolean isFreeLooking = false;
    private static Perspective lastPerspective;
    public static float[] cameraRotation = {0f,0f};

    //see feature items
    public static final FeatureSet FEATURES = FeatureSet.of(FeatureFlags.VANILLA,FeatureFlags.BUNDLE);

    //format codes
    public static final Text formatTooltip = BlackMagick.jsonFromString("[{\"text\":\"Formatting\n"+
        "0-black§r 1-§1dark_blue§r 2-§2dark_green§r 3-§3dark_aqua§r 4-§4dark_red§r 5-§5dark_purple§r "+
        "6-§6gold§r 7-§7gray§r 8-§8dark_gray§r 9-§9blue§r a-§agreen§r b-§baqua§r "+
        "c-§cred§r d-§dlight_purple§r e-§eyellow§r f-§fwhite§r #420666-\"},{\"text\":\"0xRRGGBB\",\"color\":\"#420666\"},"+
        "{\"text\":\"\nr-§rreset§r k-obfuscated§r l-§lbold§r m-§mstrikethrough§r n-§nunderlined§r o-§oitalic§r\"},"+
        "{\"text\":\"\n\nFonts\ndefault- ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\nuniform- \"},{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n\",\"font\":\"uniform\"},"+
        "{\"text\":\"alt- \"},{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n\",\"font\":\"alt\"},{\"text\":\"illageralt- \"},"+
        "{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\",\"font\":\"illageralt\"}]").text();

    //supersecretsettings
    public static final Identifier[] SUPER_SECRET_SETTING_PROGRAMS = new Identifier[]{/*Identifier.of("42edit","shaders/post/notch.json"), Identifier.of("42edit","shaders/post/fxaa.json"), Identifier.of("42edit","shaders/post/art.json"), Identifier.of("42edit","shaders/post/bumpy.json"), Identifier.of("42edit","shaders/post/blobs2.json"), Identifier.of("42edit","shaders/post/pencil.json"),*/ Identifier.of("42edit","shaders/post/color_convolve.json"),/* Identifier.of("42edit","shaders/post/deconverge.json"), Identifier.of("42edit","shaders/post/flip.json"),*/ Identifier.of("shaders/post/invert.json"),/* Identifier.of("42edit","shaders/post/ntsc.json"), Identifier.of("42edit","shaders/post/outline.json"), Identifier.of("42edit","shaders/post/phosphor.json"), Identifier.of("42edit","shaders/post/scan_pincushion.json"), Identifier.of("42edit","shaders/post/sobel.json"),*/ Identifier.of("42edit","shaders/post/bits.json"), Identifier.of("42edit","shaders/post/desaturate.json"),/* Identifier.of("42edit","shaders/post/green.json"), Identifier.of("42edit","shaders/post/blur.json"), Identifier.of("42edit","shaders/post/wobble.json"), Identifier.of("42edit","shaders/post/blobs.json"), Identifier.of("42edit","shaders/post/antialias.json"),*/ Identifier.of("shaders/post/creeper.json"), Identifier.of("shaders/post/spider.json")};
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
            client.player.playSoundToPlayer(SoundEvent.of(SECRETSOUNDS[i]), SoundCategory.MASTER, 1f, .5f);
        }
    }
    public static void cycleSuperSecretSetting() {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.getCameraEntity() instanceof PlayerEntity) {
            if(client.gameRenderer.getPostProcessor() != null) {
                client.gameRenderer.getPostProcessor().close();
            }
            superSecretSettingIndex = (superSecretSettingIndex + 1) % (SUPER_SECRET_SETTING_PROGRAMS.length + 1);
            if(superSecretSettingIndex == SUPER_SECRET_SETTING_PROGRAMS.length) {
                client.gameRenderer.disablePostProcessor();
            } else {
                ((GameRendererInvoker)client.gameRenderer).invokeLoadPostProcessor(SUPER_SECRET_SETTING_PROGRAMS[superSecretSettingIndex]);
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

    //saved items
    public static final int SAVED_ROWS = 12;

    //registry suggestions
    public static final String[] ATTRIBUTES = getCacheAttributes();
    public static final String[] BLOCKS = getCacheBlocks();
    public static final String[] BLOCKTAGS = getCacheBlockTags();
    public static final String[] COMPONENTS = getCacheComponents();
    public static final String[] ITEMS = getCacheItems();
    public static final String[] EFFECTS = getCacheEffects();
    public static final String[] ENTITIES = getCacheEntities();
    private static String[] KEYBINDS = null;
    public static final String[] LOOT = getCacheLootTables();
    public static final String[] PARTICLES = getCacheParticles();
    public static final String[] SOUNDS = getCacheSounds();
    public static final String[] STRUCTURES = getCacheStructures();
    private static String[] TRANSLATIONS = null;

    //live command suggestions
    public static void setCommandSuggs(String cmd, TextSuggestor suggs, String[] suggsList) {
        if(cmd != null && suggs != null) {
            final MinecraftClient client = MinecraftClient.getInstance();
            CommandDispatcher<CommandSource> commandDispatcher = client.player.networkHandler.getCommandDispatcher();
            ParseResults<CommandSource> cmdSuggsParse = commandDispatcher.parse(cmd, (CommandSource)client.player.networkHandler.getCommandSource());
            CompletableFuture<Suggestions> cmdSuggsPendingSuggestions = commandDispatcher.getCompletionSuggestions(cmdSuggsParse, cmd.length());

            suggs.setSuggestions(new String[]{""});

            cmdSuggsPendingSuggestions.thenRun(() -> {

                List<Suggestion> cmdSuggs = null;
                if(cmdSuggsPendingSuggestions != null && cmdSuggsPendingSuggestions.isDone()) { 
                    Suggestions suggestions = cmdSuggsPendingSuggestions.join();
                    if(!suggestions.isEmpty())
                        cmdSuggs = suggestions.getList();
                }

                String[] suggsArr = joinCommandSuggs(new String[][]{suggsList}, cmdSuggs, null);

                if(suggsArr.length>0)
                    suggs.setSuggestions(suggsArr);
            });
        }
    }

    public static String[] joinCommandSuggs(String[][] joinLists, List<Suggestion> cmdSuggs, String[] startVals) {
        List<String> list = new ArrayList<>();

        if(joinLists != null)
            for(int i=0; i<joinLists.length; i++) {
                if(joinLists[i] != null)
                    for(int j=0; j<joinLists[i].length; j++)
                        list.add(joinLists[i][j]);
            }

        if(cmdSuggs != null)
            for(Suggestion suggestion : cmdSuggs)
                list.add(suggestion.getText().replaceFirst("minecraft:",""));     

        list = new ArrayList<String>((new HashSet<String>(list)));
        Collections.sort(list);

        if(startVals != null)
            for(int i=0; i<startVals.length; i++)
                list.add(0,startVals[startVals.length-1-i]);

        if(!list.isEmpty())
            return list.toArray(new String[0]);
        return null;
    }

    //web items
    public static boolean webItemsAuto = true;
    public static NbtList webItems = null;


    @Override
    public void onInitializeClient() {

        LOGGER.info("Loading 42edit client");

        final MinecraftClient gameClient = MinecraftClient.getInstance();

        //keybinds
        magickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.openMagickGui",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "ftedit.key.categories.ftedit"));
        KeyBinding zoom = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.zoom",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "ftedit.key.categories.ftedit"));
        KeyBinding freeLook = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.freeLook",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "ftedit.key.categories.ftedit"));
        spamClick = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.spamClick",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F13, "ftedit.key.categories.ftedit"));
        modKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.modKey",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F14, "ftedit.key.categories.ftedit"));
        KeyBinding afkMove = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.afkMove",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "ftedit.key.categories.ftedit"));
        KeyBinding afkClick = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.afkClick",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "ftedit.key.categories.ftedit"));

        //options
        readOptions();

        // custom capes
        USERNAME = gameClient.getSession().getUsername();
        if(gameClient.getSession().getUuidOrNull() != null)
            UUID = NbtHelper.fromUuid(gameClient.getSession().getUuidOrNull());
        clearCapes();

        getSavedItems();
        refreshWebItems(false);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // magickgui
            if(magickGuiKey.wasPressed()) {
                switch(quickScreen) {
                    case NONE: client.setScreen(new MagickGui()); break;
                    case ITEM_BUILDER: client.setScreen(new ItemBuilder()); break;
                    case SECRET_SCREEN: client.setScreen(new SecretScreen()); break;
                    case LOG_SCREEN: client.setScreen(new LogScreen()); break;
                    case AUTO_CLICK: client.setScreen(new AutoClick()); break;
                    case CAPES: client.setScreen(new Capes()); break;
                    case HACKS: client.setScreen(new Hacks()); break;
                }
            }

            // zoom
            if(zoom.isPressed() && !zoomed) {
                smooth = client.options.smoothCameraEnabled;
                client.options.smoothCameraEnabled = true;
                zoomed = true;
            }
            else if(!zoom.isPressed() && zoomed) {
                client.options.smoothCameraEnabled = smooth;
                zoomed = false;
            }

            // afkMove
            if(afkMove.wasPressed()) {
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
            if(afkClick.wasPressed()) {
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
            if(autoFishClick && System.currentTimeMillis()>=lastFish + fishWait) {
                if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                        && client.player.getMainHandStack().isOf(Items.FISHING_ROD)) || (client.player.getMainHandStack().isEmpty()
                        && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().isOf(Items.FISHING_ROD))) ) {
                    KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                    didFish = true;
                }
                autoFishClick = false;
                lastFish = System.currentTimeMillis();
            }
            if(didFish && System.currentTimeMillis()>=lastFish + fishWait) {
                if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                        && client.player.getMainHandStack().isOf(Items.FISHING_ROD)) || (client.player.getMainHandStack().isEmpty()
                        && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().isOf(Items.FISHING_ROD))) ) {
                    KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                }
                didFish = false;
                lastFish = System.currentTimeMillis();
            }

            //freelook
            if(freeLook.isPressed()) {
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
            if(spamClick.isPressed() && System.currentTimeMillis()>=lastSpam + 20) {
                if(modKey.isPressed())
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

        });

        ComponentHelper.clearCacheInfo(); // now that registries have been set, clear any caches that may have had empty lists

        LOGGER.info("Client initialized");
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
        for (int i = 0; i < randoSlots.length; i++) {
            if(randoSlots[i] == selected)
                return true;
        }
        return false;
    }

    public static void changeRandoSlot() {
        if(randoSlots != null && testRandoSlot()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int slot = (int) (rand.nextDouble() * randoSlots.length);
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
                for (Map.Entry<Property<?>,Comparable<?>> entry : blockState.getEntries().entrySet()) {
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

    private static String[] getCacheAttributes() {
        List<String> list = new ArrayList<>();

        Registries.ATTRIBUTE.forEach(a -> {
            list.add(Registries.ATTRIBUTE.getId(a).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheBlocks() {
        List<String> list = new ArrayList<>();

        Registries.BLOCK.forEach(b -> {
            list.add(Registries.BLOCK.getId(b).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheBlockTags() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "tags/block", map::putIfAbsent);
        map.keySet().forEach(t -> {
            list.add("#"+t.toString().replaceFirst("tags/block/","").replaceFirst(".json",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheComponents() {
        List<String> list = new ArrayList<>();

        Registries.DATA_COMPONENT_TYPE.forEach(c -> {
            list.add(Registries.DATA_COMPONENT_TYPE.getId(c).toString());
        });

        list.remove("minecraft:creative_slot_lock");
        list.remove("minecraft:map_post_processing");
        
        for(String k : list) {
            // this will log warnings if ComponentHelper doesn't include a vanilla component
            ComponentHelper.getPathInfo("components."+k);
        }

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheItems() {
        List<String> list = new ArrayList<>();

        Registries.ITEM.forEach(i -> {
            list.add(Registries.ITEM.getId(i).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheEffects() {
        List<String> list = new ArrayList<>();

        Registries.STATUS_EFFECT.forEach(e -> {
            list.add(Registries.STATUS_EFFECT.getId(e).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheEntities() {
        List<String> list = new ArrayList<>();

        Registries.ENTITY_TYPE.forEach(e -> {
            list.add(Registries.ENTITY_TYPE.getId(e).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    public static String[] getCacheKeybinds() {
        if(KEYBINDS == null) {
            List<String> list = new ArrayList<>();
            List<String> list2 = new ArrayList<>();

            for(String k: KeyBindingAccessor.getKeysList().keySet()) {
                if(k.contains("ftedit.")) {
                    list2.add(k);
                }
                else
                    list.add(k);
            }

            Collections.sort(list);
            Collections.sort(list2);

            for(String k: list2)
                list.add(k);

            KEYBINDS = list.toArray(new String[0]);
        }
        return KEYBINDS;
    }

    private static String[] getCacheLootTables() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "loot_table", map::putIfAbsent);
        map.keySet().forEach(l -> {
            list.add(l.toString().replaceFirst("loot_table/","").replaceFirst(".json",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheParticles() {
        List<String> list = new ArrayList<>();

        Registries.PARTICLE_TYPE.forEach(p -> {
            list.add(Registries.PARTICLE_TYPE.getId(p).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheSounds() {
        List<String> list = new ArrayList<>();

        Registries.SOUND_EVENT.forEach(s -> {
            list.add(Registries.SOUND_EVENT.getId(s).toString());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheStructures() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "structure", map::putIfAbsent);
        map.keySet().forEach(s -> {
            list.add(s.toString().replaceFirst("structure/","").replaceFirst(".nbt",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    public static String[] getCacheTranslations() {
        if(TRANSLATIONS == null) {
            List<String> list = new ArrayList<>();

            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.getResourceManager() != null) {
                List<String> l = new ArrayList<>();
                l.add("en_us");
                TranslationStorage s = TranslationStorage.load(client.getResourceManager(),l,false);
                for(String t: ((TranslationStorageAccessor)s).getTranslations().keySet()) {
                    list.add(t);
                }
            }

            Collections.sort(list);
            TRANSLATIONS = list.toArray(new String[0]);
        }
        return TRANSLATIONS;
    }

    private static void readOptions() {
        final MinecraftClient client = MinecraftClient.getInstance();
        String optionsString = "";
        try {
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).createNewFile();
                
            Scanner scan = new Scanner(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt"), StandardCharsets.UTF_8);
            if(scan.hasNextLine())
                optionsString = scan.nextLine();
            scan.close();
        } catch (Exception e) {}

        if(BlackMagick.nbtFromString(optionsString) != null && BlackMagick.nbtFromString(optionsString).getType()==NbtElement.COMPOUND_TYPE) {
            NbtCompound json = (NbtCompound)BlackMagick.nbtFromString(optionsString);

            if(json.contains("custom_cape_toggle",NbtElement.BYTE_TYPE))
                showClientCape = ((NbtByte)json.get("custom_cape_toggle")).byteValue() == 1;
            if(json.contains("custom_cape",NbtElement.STRING_TYPE)) {
                clientCape = 0;
                String capeName = ((NbtString)json.get("custom_cape")).asString();
                for(int i=0; i<CLIENT_CAPES.length; i++)
                    if(capeName.equals(CLIENT_CAPES[i].id()))
                        clientCape = i;
            }
            if(json.contains("opticapes",NbtElement.BYTE_TYPE))
                opticapesOn = ((NbtByte)json.get("opticapes")).byteValue() == 1;
            if(json.contains("web_items",NbtElement.BYTE_TYPE))
                webItemsAuto = ((NbtByte)json.get("web_items")).byteValue() == 1;

            json.remove("options_format");
            json.remove("custom_cape_toggle");
            json.remove("custom_cape");
            json.remove("opticapes");
            json.remove("web_items");

            if(!json.isEmpty()) {
                LOGGER.warn("Config file contains unknown keys: "+json.asString());
                optionsExtra = json;
            }
        }
        else
            LOGGER.info("Creating new config file");

        updateOptions();
    }

    public static void updateOptions() {
        try {

            final MinecraftClient client = MinecraftClient.getInstance();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).createNewFile();

            NbtCompound options = new NbtCompound();
            
            if(optionsExtra != null)
                for(String k : optionsExtra.getKeys())
                    options.put(k,optionsExtra.get(k));

            options.putInt("options_format",OPTIONS_FORMAT);
            options.putBoolean("custom_cape_toggle",showClientCape);
            options.putString("custom_cape",CLIENT_CAPES[clientCape].id());
            options.putBoolean("opticapes",opticapesOn);
            options.putBoolean("web_items",webItemsAuto);

            FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt", StandardCharsets.UTF_8, false);
            writer.write(options.asString());
            writer.close();

        } catch (Exception e) {
            LOGGER.warn("Failed to edit config file");
        }
    }

    public static NbtList getSavedItems() {
        final MinecraftClient client = MinecraftClient.getInstance();
        String savedString = "";
        try {
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).exists()) {
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).createNewFile();
                FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt", StandardCharsets.UTF_8, false);
                writer.write("[]");
                writer.close();
            }
                
            Scanner scan = new Scanner(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt"), StandardCharsets.UTF_8);
            if(scan.hasNextLine())
                savedString = scan.nextLine();
            scan.close();
        } catch (Exception e) {}

        if(BlackMagick.nbtFromString(savedString) != null && BlackMagick.nbtFromString(savedString).getType()==NbtElement.LIST_TYPE) {
            NbtList nbt = (NbtList)BlackMagick.nbtFromString(savedString);
            if(!nbt.isEmpty() && nbt.get(0).getType()!=NbtElement.COMPOUND_TYPE) {
                LOGGER.warn("Failed to read saved items");
                return null;
            }

            while(nbt.size()<9*SAVED_ROWS)
                nbt.add(new NbtCompound());
            if(nbt.size()>9*SAVED_ROWS)
                LOGGER.warn("Saved items file contains too many items");

            return nbt;
        }

        LOGGER.warn("Failed to read saved items");
        return null;
    }

    public static NbtList setSavedItems(NbtList nbt) {
        if(nbt == null)
            return null;
        try {

            final MinecraftClient client = MinecraftClient.getInstance();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).createNewFile();

            String items = "[]";
            if(nbt != null)
                items = nbt.asString();

            FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt", StandardCharsets.UTF_8, false);
            writer.write(items);
            writer.close();
            return getSavedItems();

        } catch (Exception e) {}

        LOGGER.warn("Failed to edit saved items file");
        return null;
    }

    public static void refreshWebItems(boolean forceWeb) {
        webItems = null;
        final MinecraftClient client = MinecraftClient.getInstance();
        String cacheString = "";
        try {
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if(!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\web_cache.txt")).exists()) {
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\web_cache.txt")).createNewFile();
                FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\web_cache.txt", StandardCharsets.UTF_8, false);
                writer.write("{}");
                writer.close();
            }
                
            Scanner scan = new Scanner(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\web_cache.txt"), StandardCharsets.UTF_8);
            if(scan.hasNextLine())
                cacheString = scan.nextLine();
            scan.close();
        } catch (Exception e) {}

        NbtCompound newItems = null;

        if(BlackMagick.nbtFromString(cacheString) != null && BlackMagick.nbtFromString(cacheString).getType()==NbtElement.COMPOUND_TYPE)
            newItems = (NbtCompound)BlackMagick.nbtFromString(cacheString);

        if(webItemsAuto || forceWeb) {
            String webJson = "";
            try {
                HttpURLConnection con = (HttpURLConnection)(new URI("https://baphomet42.github.io/blackmarket/items.json")).toURL().openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(500);
                con.setUseCaches(false);
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Scanner scan = new Scanner(con.getInputStream(), StandardCharsets.UTF_8);
                    while(scan.hasNextLine())
                        webJson += scan.nextLine();
                    scan.close();
                }
                con.disconnect();
            } catch(Exception e) {}

            if(BlackMagick.nbtFromString(webJson) != null && BlackMagick.nbtFromString(webJson).getType()==NbtElement.COMPOUND_TYPE) {
                NbtCompound cache = (NbtCompound)BlackMagick.nbtFromString(webJson);
                newItems = cache.copy();
                String items = newItems.asString();

                if(items.equals(cacheString)) {
                    LOGGER.info("Black Market items are up to date");
                }
                else {
                    LOGGER.info("Updating Black Market items");

                    try {
                        FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\web_cache.txt", StandardCharsets.UTF_8, false);
                        writer.write(items);
                        writer.close();
                    } catch(IOException e) {
                        LOGGER.warn("Failed to save Black Market cache");
                    }
                }
            }
            else
                LOGGER.warn("Failed connection to BaphomethLabs Black Market");
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
                webItems = new NbtList();
                for(int i=0; i<jsonItems.size(); i++) {
                    if(((NbtCompound)jsonItems.get(i)).contains("item",NbtElement.STRING_TYPE)) {
                        NbtCompound stack = BlackMagick.validCompound(BlackMagick.nbtFromString(((NbtCompound)jsonItems.get(i)).getString("item")));
                        if(!stack.isEmpty())
                            webItems.add(stack);
                    }
                }
            }
        }

        if(webItems == null || webItems.isEmpty())
            LOGGER.warn("No source of Black Market items available");
    }

}