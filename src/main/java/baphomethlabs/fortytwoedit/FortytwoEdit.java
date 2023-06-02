package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import baphomethlabs.fortytwoedit.gui.MagickGui;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;

public class FortytwoEdit implements ClientModInitializer {

    /*------------------------------------------
     *  Incorporated works
     *  
     *  Example Mod from https://github.com/FabricMC/fabric-example-mod Creative Commons Zero v1.0 Universal
     *  Logical Zoom from https://github.com/LogicalGeekBoy/logical_zoom (com.logicalgeekboy.logical_zoom) MIT License
     *  Freelook from https://github.com/Celibistrial/freelook (celibistrial.freelook) GNU Affero General Public License v3.0
     *  Visible Barriers from https://github.com/AmyMialeeMods/visible-barriers (xyz.amymialee.visiblebarriers) Copyright (c) 2022 AmyMialee All rights reserved.
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
    public static boolean autoClick = false;
    public static boolean autoAttack = true;
    public static boolean autoMine = false;
    public static int attackWait = 1500;
    private static long lastAttack = 0;

    // randomizer mode
    public static int[] randoSlots;
    public static boolean randoMode = false;

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

    //cache tools
    public static boolean cacheToolsMode = false;    //show gui button
    public static boolean cacheToolsSuggs = false;



    @Override
    public void onInitializeClient() {

        final MinecraftClient gameClient = MinecraftClient.getInstance();

        //keybinds
        magickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.openMagickGui",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "ftedit.key.categories.ftedit"));
        KeyBinding zoom = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.zoom",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "ftedit.key.categories.ftedit"));
        KeyBinding afkMove = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.afkMove",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "ftedit.key.categories.ftedit"));
        KeyBinding afkClick = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.afkClick",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "ftedit.key.categories.ftedit"));
        KeyBinding freeLook = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.freeLook",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "ftedit.key.categories.ftedit"));
                
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
                MinecraftClient.getInstance().setScreen(new MagickGui());

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
                    KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.forwardKey), false);
            }
            if (autoMove && client.player != null) {
                KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.forwardKey), true);
            }

            //afkClick
            if(afkClick.wasPressed()) {
                autoClicker = !autoClicker;

                if (!autoClicker && autoClick)
                    KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.useKey), false);
                if (!autoClicker && autoMine)
                    KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.attackKey), false);
            }
            if(autoClicker && client.player != null) {
                if (autoClick) {
                    KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.useKey), true);
                }
                if (autoAttack && System.currentTimeMillis()>=lastAttack + attackWait && client.crosshairTarget instanceof EntityHitResult) {
                    lastAttack = System.currentTimeMillis();
                    client.interactionManager.attackEntity(client.player, ((EntityHitResult)client.crosshairTarget).getEntity());
                    client.player.resetLastAttackedTicks();
                    client.player.swingHand(Hand.MAIN_HAND);
                }
                if (autoMine) {
                    KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.attackKey), true);
                }
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

            //capes
            if (capeNames.size() > 0 && MinecraftClient.getInstance().player == null) {
                clearCapes();
            }

            // rando
            if (randoMode) {
                if (client.mouse.wasLeftButtonClicked() && randoSlots != null && testRandoSlot())
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


        KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.useKey), false);
        KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.attackKey), false);
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

    private static void changeRandoSlot() {
        final MinecraftClient client = MinecraftClient.getInstance();
        int slot = (int) (Math.random() * randoSlots.length);
        slot = randoSlots[slot];
        client.player.getInventory().selectedSlot = slot - 1;
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