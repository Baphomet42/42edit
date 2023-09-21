package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
import baphomethlabs.fortytwoedit.gui.screen.MagickGui;
import baphomethlabs.fortytwoedit.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
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
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;

public class FortytwoEdit implements ClientModInitializer {

    /*------------------------------------------
     *  Incorporated works
     *  
     *  Example Mod from https://github.com/FabricMC/fabric-example-mod Creative Commons Zero v1.0 Universal
     *  Freelook from https://github.com/Celibistrial/freelook (celibistrial.freelook) GNU Affero General Public License v3.0
     *  Visible Barriers from https://github.com/AmyMialeeMods/visible-barriers (xyz.amymialee.visiblebarriers) Copyright (c) 2022 AmyMialee All rights reserved.
     *  Brewing Guide from https://www.curseforge.com/minecraft/texture-packs/in-game-brewing-guide
     *------------------------------------------
     */

	public static final Logger LOGGER = LoggerFactory.getLogger("42edit");

    //gui
    public static KeyBinding magickGuiKey;

    //options
    private static NbtCompound optionsExtra;

    // zoom
    public static boolean zoomed = false;
    private static boolean smooth = false;

    // visiblebarriers
    public static boolean seeInvis = false;

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

    // randomizer mode
    private static SecureRandom rand = new SecureRandom();
    public static int[] randoSlots;
    public static boolean randoMode = false;

    public static String randomUUID() {
        String uuid = "[I;";
        for(int i=0; i<4; i++) {
            if(i!=0)
                uuid+=",";
            uuid += rand.nextInt(Integer.MIN_VALUE,Integer.MAX_VALUE);
        }
        uuid+="]";
        return uuid;
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

    public static boolean opticapes = true;
    public static boolean opticapesOn = true;

    private static void checkCapesEnabled() {
        opticapes = false;
        if(opticapesOn)
            opticapes = true;

        if(opticapes) {
            boolean connect = false;
            try {
                HttpURLConnection con = (HttpURLConnection)(new URL("http://s.optifine.net/capes/42Richtofen42.png")).openConnection();
                con.setConnectTimeout(2000);
                if(con.getResponseCode() == HttpURLConnection.HTTP_OK)
                    connect = true;
            } catch(IOException e) {}
            if(!connect) {
                opticapes = false;
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
        if (capeNames.size() == 0 && !name.equals(client.getSession().getUsername()))
            tryLoadCape(client.getSession().getUsername());
        capeNames.add(name);
        try {
            URL link = new URL("http://s.optifine.net/capes/" + name + ".png");
            HttpURLConnection con = (HttpURLConnection)link.openConnection();
            con.setConnectTimeout(500);
            con.setReadTimeout(500);
            NativeImage capeInp = NativeImage.read(con.getInputStream());
            con.disconnect();
            NativeImage cape = new NativeImage(128, 64, true);

            for (int x = 0; x < capeInp.getWidth(); x++)
                for (int y = 0; y < capeInp.getHeight(); y++)
                    cape.setColor(x, y, capeInp.getColor(x, y));

            capeInp.close();
            client.getTextureManager().registerTexture(new Identifier("42edit:cache/capes/"+name.toLowerCase()), new NativeImageBackedTexture(cape));
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
    public static String[] clientCapeList = {"minecon2011","minecon2013","minecon2016","mojang-old","mojang","spartan","christmas"};
    public static String clientUsername = "";

    //skin testing
    public static boolean showClientSkin = false;
    public static boolean clientSkinSlim = false;
    public static String customSkinName = "";
    public static Identifier customSkinID = new Identifier("42edit:cache/custom_skin");

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

    //see feature items
    public static final FeatureSet FEATURES = FeatureSet.of(FeatureFlags.VANILLA,FeatureFlags.BUNDLE);

    //format codes
    public static final Text formatTooltip = Text.Serializer.fromJson("[{\"text\":\"Formatting\n"+
        "0-black§r 1-§1dark_blue§r 2-§2dark_green§r 3-§3dark_aqua§r 4-§4dark_red§r 5-§5dark_purple§r "+
        "6-§6gold§r 7-§7gray§r 8-§8dark_gray§r 9-§9blue§r a-§agreen§r b-§baqua§r "+
        "c-§cred§r d-§dlight_purple§r e-§eyellow§r f-§fwhite§r #420666-\"},{\"text\":\"0xRRGGBB\",\"color\":\"#420666\"},"+
        "{\"text\":\"\nr-§rreset§r k-obfuscated§r l-§lbold§r m-§mstrikethrough§r n-§nunderlined§r o-§oitalic§r\"},"+
        "{\"text\":\"\n\nFonts\ndefault- ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\nuniform- \"},{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n\",\"font\":\"uniform\"},"+
        "{\"text\":\"alt- \"},{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n\",\"font\":\"alt\"},{\"text\":\"illageralt- \"},"+
        "{\"text\":\"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\",\"font\":\"illageralt\"}]");

    //supersecretsettings
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
    public static void secretSound() {
        if(SECRETSOUNDS != null && SECRETSOUNDS.length > 0) {
            final MinecraftClient client = MinecraftClient.getInstance();
            int i = (int)(Math.random()*SECRETSOUNDS.length);
            client.player.playSound(SoundEvent.of(SECRETSOUNDS[i]), SoundCategory.MASTER, 1f, .5f);
        }
    }
    
    //items
    public static final ItemStack HEAD42 =
        ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:player_head,Count:1,tag:{SkullOwner:{Id:[I;456917768,-717075144,-1332545803,-1234799884],"+
        "Name:\"42Richtofen42\",Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTY3MzkxMzQ0ODkzNCwKICAicHJvZmlsZUlkIiA6ICIxYjNjMDMwOGQ1NDI0ZDM4YjA5MmY2"+
        "ZjViNjY2NzJmNCIsCiAgInByb2ZpbGVOYW1lIiA6ICI0MlJpY2h0b2ZlbjQyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogIC"+
        "AgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U0OWI2OWI1ZTczNWViNTIyYjI0YzY5NzM1NDlkZGE4MzNiMTVkOTFiODc0MzU2NGNmYjE3ZDBmOTYxYzBm"+
        "NTQiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzNDBjMGUwM2RkMjRhMTFiMTVhOGIzM2MyYTdlOWUzMm"+
        "FiYjIwNTFiMjQ4MWQwYmE3ZGVmZDYzNWNhN2E5MzMiCiAgICB9CiAgfQp9\"}]}}}}"));
    public static final ItemStack BANNER42 =
        ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:red_banner,Count:1,tag:{BlockEntityTag:{Patterns:[{Color:15,Pattern:\"cbo\"},"+
        "{Color:15,Pattern:\"bri\"},{Color:14,Pattern:\"bt\"},{Color:15,Pattern:\"bt\"},{Color:10,Pattern:\"flo\"},{Color:15,Pattern:\"gra\"}],id:\"minecraft:banner\"}}}"));
    public static final ItemStack UNKNOWN_ITEM =
        ItemStack.fromNbt((NbtCompound)BlackMagick.elementFromString("{id:player_head,Count:1,tag:{SkullOwner:{Id:[I;456917768,-717075144,-1332545803,-1234799884],"+
        "Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTY3OTM4NTI5MzU1NywKICAicHJvZmlsZUlkIiA6ICIxYjNjMDMwOGQ1NDI0ZDM4YjA5MmY2ZjViNjY2NzJmNCIsCiAgInB"+
        "yb2ZpbGVOYW1lIiA6ICI0MlJpY2h0b2ZlbjQyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwO"+
        "i8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FlMTgyMzY3MTE5NjMxMzE3NjkyMzQzNzg4Y2Q1Yzg1NGMxM2I0NDlkMzZlNjIyYjg1NTRhNTYzOGVkMzg1OTMiCiAgICB9CiAgfQp9\"}]}}}}"));

    //saved items
    public static final int SAVED_ROWS = 9;

    //registry suggestions
    public static final String[] ATTRIBUTES = getCacheAttributes();
    public static final String[] BLOCKS = getCacheBlocks();
    public static final String[] BLOCKTAGS = getCacheBlockTags();
    public static final String[] ITEMS = getCacheItems();
    public static final String[] EFFECTS = getCacheEffects();
    public static final String[] ENCHANTS = getCacheEnchants();
    public static final String[] ENTITIES = getCacheEntities();
    public static final String[] LOOT = getCacheLootTables();
    public static final String[] PARTICLES = getCacheParticles();
    public static final String[] SOUNDS = getCacheSounds();
    public static final String[] STRUCTURES = getCacheStructures();

    //live command suggestions
    public static void setCommandSuggs(String cmd, TextSuggestor suggs, String[][] joinLists) {
        final MinecraftClient client = MinecraftClient.getInstance();
        CommandDispatcher<CommandSource> commandDispatcher = client.player.networkHandler.getCommandDispatcher();
        ParseResults<CommandSource> cmdSuggsParse = commandDispatcher.parse(cmd, (CommandSource)client.player.networkHandler.getCommandSource());
        CompletableFuture<Suggestions> cmdSuggsPendingSuggestions = commandDispatcher.getCompletionSuggestions(cmdSuggsParse, cmd.length());

        suggs.setSuggestions(new String[]{""});

        cmdSuggsPendingSuggestions.thenRun(() -> {
            List<String> list = new ArrayList<>();

            if(joinLists != null)
                for(int i=0; i<joinLists.length; i++)
                    for(int j=0; j<joinLists[i].length; j++)
                        list.add(joinLists[i][j]);

            Suggestions suggestions;
            if (cmdSuggsPendingSuggestions != null && cmdSuggsPendingSuggestions.isDone() && !(suggestions = cmdSuggsPendingSuggestions.join()).isEmpty()) {
                for (Suggestion suggestion : suggestions.getList())
                    list.add(suggestion.getText().replaceFirst("minecraft:",""));                
            }

            list = new ArrayList<String>((new HashSet<String>(list)));
            Collections.sort(list);
            if(list.size()>0)
                suggs.setSuggestions(list.toArray(new String[0]));
        });
    }


    @Override
    public void onInitializeClient() {

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
                
        // visiblebarriers
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());

        // custom capes
        clientUsername = gameClient.getSession().getUsername();
        clearCapes();

        //options
        readOptions();

        //saved items
        getSavedItems();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // magickgui
            if (magickGuiKey.wasPressed())
                client.setScreen(new MagickGui());

            // zoom
            if (zoom.isPressed() && !zoomed) {
                smooth = client.options.smoothCameraEnabled;
                client.options.smoothCameraEnabled = true;
                zoomed = true;
            } else if (!zoom.isPressed() && zoomed) {
                client.options.smoothCameraEnabled = smooth;
                zoomed = false;
            }

            // afkMove
            if (afkMove.wasPressed()) {
                autoMove = !autoMove;
                if (!autoMove)
                    KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.forwardKey).getBoundKey(), false);
            }
            if (autoMove && client.player != null) {
                KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.forwardKey).getBoundKey(), true);
            }

            //afkClick
            if(afkClick.wasPressed()) {
                autoClicker = !autoClicker;

                if (!autoClicker && autoClick)
                    KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey(), false);
                if (!autoClicker && autoMine)
                    KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.attackKey).getBoundKey(), false);
            }
            if(autoClicker && client.player != null) {
                if (autoClick) {
                    KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey(), true);
                }
                if (autoAttack && System.currentTimeMillis()>=lastAttack + attackWait && client.crosshairTarget instanceof EntityHitResult) {
                    lastAttack = System.currentTimeMillis();
                    client.interactionManager.attackEntity(client.player, ((EntityHitResult)client.crosshairTarget).getEntity());
                    client.player.resetLastAttackedTicks();
                    client.player.swingHand(Hand.MAIN_HAND);
                }
                if (autoMine) {
                    KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.attackKey).getBoundKey(), true);
                }
            }

            //autoFish
            if(autoFishClick && System.currentTimeMillis()>=lastFish + fishWait) {
                if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                        && client.player.getMainHandStack().getItem().toString().equals("fishing_rod")) || (client.player.getMainHandStack().isEmpty()
                        && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().getItem().toString().equals("fishing_rod"))) ) {
                    KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                    didFish = true;
                }
                autoFishClick = false;
                lastFish = System.currentTimeMillis();
            }
            if(didFish && System.currentTimeMillis()>=lastFish + fishWait) {
                if(autoFish && !autoClicker && client.currentScreen == null && ((!client.player.getMainHandStack().isEmpty()
                        && client.player.getMainHandStack().getItem().toString().equals("fishing_rod")) || (client.player.getMainHandStack().isEmpty()
                        && !client.player.getOffHandStack().isEmpty() && client.player.getOffHandStack().getItem().toString().equals("fishing_rod"))) ) {
                    KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                }
                didFish = false;
                lastFish = System.currentTimeMillis();
            }

            //freelook
            if (freeLook.isPressed()) {
                if (!isFreeLooking) { // Only execute when enabling FreeLook.
                    lastPerspective = client.options.getPerspective();

                    // Switch from first to third person.
                    if (lastPerspective == Perspective.FIRST_PERSON) {
                        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    }

                    isFreeLooking = true;
                }
            } else if (isFreeLooking) { // Only execute when disabling FreeLook.
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

            //capes
            if (capeNames.size() > 0 && MinecraftClient.getInstance().player == null) {
                clearCapes();
            }

            // rando
            if (randoMode) {
                if (client.options.useKey.isPressed())
                    changeRandoSlot();
            }

        });
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


        KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey(), false);
        KeyBinding.setKeyPressed(((KeyBindingAccessor)client.options.attackKey).getBoundKey(), false);
    }

    private static boolean testRandoSlot() {
        final MinecraftClient client = MinecraftClient.getInstance();
        int selected = client.player.getInventory().selectedSlot + 1;
        for (int i = 0; i < randoSlots.length; i++) {
            if (randoSlots[i] == selected)
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
        if (hitResult == null) {
            return null;
        }
        if(hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = client.player.getWorld().getBlockState(blockPos);
            
            if (!blockState.getProperties().isEmpty()) {
                NbtCompound stack = new NbtCompound();
                stack.put("id",NbtString.of(blockState.getBlock().asItem().toString()));
                stack.put("Count",NbtInt.of(1));
                NbtCompound tag = new NbtCompound();

                String states = "";
                states += "{";
                boolean bl = false;
                for (Map.Entry<Property<?>,Comparable<?>> entry : blockState.getEntries().entrySet()) {
                    if (bl) {
                        states += ",";
                    }
                    states += entry.getKey().getName();
                    states += ":";
                    states += "\""+getValueString(blockState,entry.getKey())+"\"";
                    bl = true;
                }
                states += "}";

                tag.put("display",BlackMagick.elementFromString("{Lore:['{\"text\":\"(+BlockStateTag)\"}']}"));
                tag.put("BlockStateTag",BlackMagick.elementFromString(states));
                stack.put("tag",tag);
                return ItemStack.fromNbt(stack);
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
            list.add(Registries.ATTRIBUTE.getId(a).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheBlocks() {
        List<String> list = new ArrayList<>();

        Registries.BLOCK.forEach(b -> {
            list.add(Registries.BLOCK.getId(b).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheBlockTags() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "tags/blocks", map::putIfAbsent);
        map.keySet().forEach(t -> {
            list.add(t.getPath().replaceFirst("tags/blocks/","").replaceFirst(".json",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheItems() {
        List<String> list = new ArrayList<>();

        Registries.ITEM.forEach(i -> {
            list.add(Registries.ITEM.getId(i).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheEffects() {
        List<String> list = new ArrayList<>();

        Registries.STATUS_EFFECT.forEach(e -> {
            list.add(Registries.STATUS_EFFECT.getId(e).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheEnchants() {
        List<String> list = new ArrayList<>();

        Registries.ENCHANTMENT.forEach(e -> {
            list.add(Registries.ENCHANTMENT.getId(e).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheEntities() {
        List<String> list = new ArrayList<>();

        Registries.ENTITY_TYPE.forEach(e -> {
            list.add(Registries.ENTITY_TYPE.getId(e).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheLootTables() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "loot_tables", map::putIfAbsent);
        map.keySet().forEach(l -> {
            list.add(l.getPath().replaceFirst("loot_tables/","").replaceFirst(".json",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheParticles() {
        List<String> list = new ArrayList<>();

        Registries.PARTICLE_TYPE.forEach(p -> {
            list.add(Registries.PARTICLE_TYPE.getId(p).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheSounds() {
        List<String> list = new ArrayList<>();

        Registries.SOUND_EVENT.forEach(s -> {
            list.add(Registries.SOUND_EVENT.getId(s).getPath());
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static String[] getCacheStructures() {
        List<String> list = new ArrayList<>();

        HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
        VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, "minecraft", "structures", map::putIfAbsent);
        map.keySet().forEach(s -> {
            list.add(s.getPath().replaceFirst("structures/","").replaceFirst(".nbt",""));
        });

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    private static void readOptions() {
        final MinecraftClient client = MinecraftClient.getInstance();
        String optionsString = "";
        try {
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).createNewFile();
                
            Scanner scan = new Scanner(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt"), StandardCharsets.UTF_8);
            if(scan.hasNextLine())
                optionsString = scan.nextLine();
            scan.close();
        } catch (Exception e) {}

        if(BlackMagick.elementFromString(optionsString) != null && BlackMagick.elementFromString(optionsString).getType()==NbtElement.COMPOUND_TYPE) {
            NbtCompound json = (NbtCompound)BlackMagick.elementFromString(optionsString);

            if(json.contains("CustomCapeToggle",NbtElement.BYTE_TYPE))
                showClientCape = ((NbtByte)json.get("CustomCapeToggle")).byteValue() == 1;
            if(json.contains("CustomCape",NbtElement.INT_TYPE))
                clientCape = ((NbtInt)json.get("CustomCape")).intValue();
            if(clientCape<0)
                clientCape = 0;
            else if(clientCape>=clientCapeList.length)
                clientCape = 0;
            if(json.contains("OptiCapeToggle",NbtElement.BYTE_TYPE))
                opticapesOn = ((NbtByte)json.get("OptiCapeToggle")).byteValue() == 1;

            json.remove("CustomCapeToggle");
            json.remove("CustomCape");
            json.remove("OptiCapeToggle");

            if(json.asString().length()>2) {
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
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt")).createNewFile();

            String options = "{CustomCapeToggle:"+ (showClientCape ? "1b" : "0b")
                +",CustomCape:"+ clientCape
                +",OptiCapeToggle:"+ (opticapesOn ? "1b" : "0b");
            
            if(optionsExtra != null && optionsExtra.asString().length()>2)
                options += "," + optionsExtra.asString().substring(1,optionsExtra.asString().length()-1);

            options += "}";

            FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\.42edit\\options.txt", StandardCharsets.UTF_8, false);
            writer.write(options);
            writer.close();

        } catch (Exception e) {
            LOGGER.warn("Failed to edit config file");
        }
    }

    public static NbtList getSavedItems() {
        final MinecraftClient client = MinecraftClient.getInstance();
        String savedString = "";
        try {
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).exists()) {
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

        if(BlackMagick.elementFromString(savedString) != null && BlackMagick.elementFromString(savedString).getType()==NbtElement.LIST_TYPE) {
            NbtList nbt = (NbtList)BlackMagick.elementFromString(savedString);
            if(nbt.size()>0 && nbt.get(0).getType()!=NbtElement.COMPOUND_TYPE) {
                LOGGER.warn("Failed to read saved items");
                return null;
            }
            while(nbt.size()<9*SAVED_ROWS) {
                NbtCompound air = new NbtCompound();
                air.putString("id","air");
                air.putInt("Count",0);
                nbt.add(air);
            }
            if(nbt.size()>9*SAVED_ROWS)
                LOGGER.warn("Saved items file outdated");
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
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath() + "\\.42edit")).mkdir();
            if (!(new File(client.runDirectory.getAbsolutePath() + "\\.42edit\\saved_items.txt")).exists())
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

}