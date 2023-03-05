package baphomethlabs.fortytwoedit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import baphomethlabs.fortytwoedit.gui.MagickGui;
import baphomethlabs.fortytwoedit.gui.framework.MagickScreen;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class FortytwoEdit implements ClientModInitializer {

    /*------------------------------------------
     *  Incorporated works
     *  
     *  Example Mod from https://github.com/FabricMC/fabric-example-mod
     *  LibGui from https://github.com/CottonMC/LibGui
     *  Logical Zoom from https://github.com/LogicalGeekBoy/logical_zoom
     *  Freelook from https://github.com/Celibistrial/freelook
     *  Visible Barriers from https://github.com/AmyMialeeMods/visible-barriers
     *------------------------------------------
     */

    // zoom
    public static boolean zoomed = false;
    private boolean smooth = false;

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

    public static void checkCapesEnabled() {
        opticapes = false;
        if(opticapesOn)
            opticapes = true;
        final MinecraftClient client = MinecraftClient.getInstance();
        if (!client.getResourcePackManager().getEnabledNames().contains("file/§5§lMinekvlt §8- ULTIMATE EDITION"))
            opticapes = false;
        else {
            if (!(new File(client.runDirectory.getAbsolutePath()
                    + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets")).exists())
                (new File(client.runDirectory.getAbsolutePath()
                        + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets")).mkdirs();
            if (!(new File(client.runDirectory.getAbsolutePath()
                    + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit")).exists())
                (new File(client.runDirectory.getAbsolutePath()
                        + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit")).mkdirs();
            if (!(new File(client.runDirectory.getAbsolutePath()
                    + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache")).exists())
                (new File(client.runDirectory.getAbsolutePath()
                        + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache")).mkdirs();
            if (!(new File(client.runDirectory.getAbsolutePath()
                    + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache\\capes")).exists())
                (new File(client.runDirectory.getAbsolutePath()
                        + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache\\capes")).mkdirs();
        }

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
                Log.warn(LogCategory.GENERAL,"[42edit] Failed connection to OptiFine capes");
            }
        }
    }

    private static boolean clearAtLaunch = false;
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
        final MinecraftClient client = MinecraftClient.getInstance();
        File capeCacheFolder = new File(client.runDirectory.getAbsolutePath()
                + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache\\capes");
        if (capeCacheFolder.exists())
            try {
                FileUtils.deleteDirectory(capeCacheFolder);
            } catch (IOException | IllegalArgumentException e) {
            }
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
            NativeImage cape = new NativeImage(128, 64, true);

            for (int x = 0; x < capeInp.getWidth(); x++)
                for (int y = 0; y < capeInp.getHeight(); y++)
                    cape.setColor(x, y, capeInp.getColor(x, y));
            capeInp.close();
            cape.writeTo(Path.of(client.runDirectory.getAbsolutePath()
                    + "\\resourcepacks\\§5§lMinekvlt §8- ULTIMATE EDITION\\assets\\42edit\\cache\\capes\\"
                    + name.toLowerCase()));
            cape.close();
            capeNames2.add(name);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // custom capes
    public static boolean showClientCape = false;
    public static int clientCape = 0;
    public static String[] clientCapeList = {"minecon2011","minecon2013","minecon2016","mojang-old","mojang","spartan","christmas"};
    public static String clientUsername = "";

    //freelook
    public static boolean isFreeLooking = false;
    private static Perspective lastPerspective;

    //see feature items
    public static final FeatureSet FEATURES = FeatureSet.of(FeatureFlags.VANILLA,FeatureFlags.BUNDLE,FeatureFlags.UPDATE_1_20);

    @Override
    public void onInitializeClient() {

        KeyBinding openMagickGui = KeyBindingHelper.registerKeyBinding(new KeyBinding("ftedit.key.openMagickGui",
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

        // hud text
        HudRenderCallback.EVENT.register(FortytwoEdit::drawText);

        // custom capes
        final MinecraftClient gameClient = MinecraftClient.getInstance();
        clientUsername = gameClient.getSession().getUsername();

        // options
        readOptions();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // magickgui
            if (openMagickGui.wasPressed())
                MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));

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

            // capes
            if (!clearAtLaunch) {
                clearCapes();
                clearAtLaunch = true;
            }
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
            attackWait = 1500;

        KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.useKey), false);
        KeyBinding.setKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.attackKey), false);
    }

    private static void drawText(MatrixStack m, float t) {
        final MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client.textRenderer;
        int x = 350;
        int y = 220;
        if (autoMove)
            renderer.draw(m, "[Auto Move]", x, y + 5, 0xffffff);
        if (autoClicker)
            renderer.draw(m, "[Auto Click]", x, y - 5, 0xffffff);
        if (randoMode)
            renderer.draw(m, "[Rando Mode]", x, y - 15, 0xffffff);

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
        if (!(new File(client.runDirectory.getAbsolutePath() + "\\42options.txt")).exists())
            try {
                (new File(client.runDirectory.getAbsolutePath() + "\\42options.txt")).createNewFile();
            } catch (IOException e) {}
        try {
            File optionsFile = new File(client.runDirectory.getAbsolutePath() + "\\42options.txt");
            Scanner options = new Scanner(optionsFile);
            while (options.hasNext()) {
                String line = options.nextLine();
                if (line.length()>11 && line.substring(0, 11).equals("customCape:")) {
                    if (line.substring(11).equals("true"))
                        showClientCape = true;
                } else if (line.length()>5 && line.substring(0, 5).equals("cape:")) {
                    String val = line.substring(5);
                    try{
                        clientCape = Integer.parseInt(val);
                        if(clientCape<0)
                            clientCape=0;
                        else if(clientCape>=clientCapeList.length)
                            clientCape=0;
                    } catch(NumberFormatException e) {
                        clientCape=0;
                    }
                } else if (line.length()>9 && line.substring(0, 9).equals("optiCape:")) {
                    if (line.substring(11).equals("false"))
                        opticapesOn = false;
                }
            }
            options.close();
        } catch (FileNotFoundException e) {}
    }

    public static void updateOptions() {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (!(new File(client.runDirectory.getAbsolutePath() + "\\42options.txt")).exists())
            try {
                (new File(client.runDirectory.getAbsolutePath() + "\\42options.txt")).createNewFile();
            } catch (IOException e) {}
        try {
            FileWriter writer = new FileWriter(client.runDirectory.getAbsolutePath() + "\\42options.txt", false);
            String line = "customCape:";
            if (showClientCape)
                line = line + "true";
            else
                line = line + "false";
            writer.write(line);
            writer.write(System.lineSeparator());
            line = "cape:" + clientCape;
            writer.append(line);
            writer.write(System.lineSeparator());
            line = "optiCape:";
            if (opticapesOn)
                line = line + "true";
            else
                line = line + "false";
            writer.append(line);
            writer.close();
        } catch (IOException e) {}
    }

}