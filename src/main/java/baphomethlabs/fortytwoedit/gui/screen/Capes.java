package baphomethlabs.fortytwoedit.gui.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public class Capes extends GenericScreen {

    protected EditBox txtCustom;
    protected EditBox txtCustomSkin;
    protected int playerX;
    protected int playerY;
    private static final Vector3f vec = new Vector3f();
    private static final String CUSTOM_SKIN_ERROR_TITLE = "Failed to load skin";

    public Capes() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.CAPES;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new MagickGui())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("OptiFine [On]"),
                Component.literal("OptiFine [Off]")).withInitialValue(FortytwoEdit.opticapesOn).displayOnlyValue().create(x+20,y+ROW_HEIGHT*3+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {

            FortytwoEdit.readOptions();
            FortytwoEdit.opticapesOn = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            FortytwoEdit.clearOptiCapes();
            reloadScreen();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle OptiFine capes mode\n\nWhen on: you can see players' OptiFine capes")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Refresh"), button -> this.btnReloadCapes()).bounds(x+20+80+WID_SPACE,y+ROW_HEIGHT*3+1,60,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Refresh all OptiFine capes")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Edit"), button -> this.btnEditCape()).bounds(x+20+80+WID_SPACE+60+WID_SPACE,y+ROW_HEIGHT*3+1,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Edit your OptiFine cape (requires donation to OptiFine)")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Custom [On]"),
                Component.literal("Custom [Off]")).withInitialValue(FortytwoEdit.showClientCape).displayOnlyValue().create(x+20,y+ROW_HEIGHT*4+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {

            FortytwoEdit.readOptions();
            FortytwoEdit.showClientCape = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            reloadScreen();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle custom capes mode\n\nWhen on: change your cape (only you can see this)")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("<"), button -> this.btnDecCustom()).bounds(x+20+80+WID_SPACE,y+ROW_HEIGHT*4+1,15,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Cycle custom cape left")));
        this.txtCustom = new EditBox(this.font,x+20+1+80+WID_SPACE+15,y+ROW_HEIGHT*4+1,90-2,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtCustom.setMaxLength(MAX_TEXT_LENGTH);
        this.txtCustom.setValue(FortytwoEdit.getClientCapeTextboxName());
        this.txtCustom.moveCursorToStart(false);
        this.txtCustom.setTooltip(FortytwoEdit.getClientCapeTextboxTooltip());
        this.txtCustom.setEditable(false);
        this.addRenderableWidget(this.txtCustom);
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(">"), button -> this.btnIncCustom()).bounds(x+20+80+WID_SPACE+15+90,y+ROW_HEIGHT*4+1,15,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Cycle custom cape right")));

        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Custom [On]"),
                Component.literal("Custom [Off]")).withInitialValue(FortytwoEdit.showClientSkin).displayOnlyValue().create(x+20,y+ROW_HEIGHT*6+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.showClientSkin = (boolean)trackOutput;
            unsel();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle custom skin mode\n\nWhen on: change your skin (only you can see this)")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("3px"),
                Component.literal("4px")).withInitialValue(FortytwoEdit.clientSkinSlim).displayOnlyValue().create(x+20+80+WID_SPACE,y+ROW_HEIGHT*6+1,30,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.clientSkinSlim = (boolean)trackOutput;
            unsel();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle skin model between wide/slim (requires custom skin mode)")));
        this.txtCustomSkin = new EditBox(this.font,x+20,y+ROW_HEIGHT*7+1,200,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtCustomSkin.setMaxLength(MAX_TEXT_LENGTH);
        this.txtCustomSkin.setValue(FortytwoEdit.customSkinName.equals("") ? "<Drag and drop skin into this window>" : FortytwoEdit.customSkinName);
        this.txtCustomSkin.moveCursorToStart(false);
        this.txtCustomSkin.setTooltip(Tooltip.create(Component.nullToEmpty("Drag and drop a skin into this window to set a custom skin")));
        this.txtCustomSkin.setEditable(false);
        this.addRenderableWidget(this.txtCustomSkin);
        playerX = x + 240+40;
        playerY = this.height/2 + 30;
    }

    protected void btnReloadCapes() {
        FortytwoEdit.showToast("OptiCapes cache cleared",FortytwoEdit.debugCapeNamesSize()+" name(s) and "+FortytwoEdit.debugCapeNames2Size()+" cape(s) deleted");
        FortytwoEdit.clearOptiCapes();
        unsel();
    }

    protected void btnEditCape() {
        //from https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/mixin/SkinOptionsScreenMixin.java
        try {
            BigInteger random1Bi = new BigInteger(128, new Random());
            BigInteger random2Bi = new BigInteger(128, new Random(System.identityHashCode(new Object())));
            String serverId = random1Bi.xor(random2Bi).toString(16);
            minecraft.getMinecraftSessionService().joinServer(minecraft.getUser().getProfileId(),minecraft.getUser().getAccessToken(),serverId);
            String url = "https://optifine.net/capeChange?u=" +
                minecraft.getUser().getProfileId().toString().replace("-","") + "&n=" + minecraft.getUser().getName() + "&s=" + serverId;
            Util.getPlatform().openUri(url);
        } catch(Exception ex) {
            FortytwoEdit.showToast("Failed to edit cape","Could not open OptiFine cape editor webpage");
        }
        unsel();
    }

    protected void btnDecCustom() {
        FortytwoEdit.cycleClientCape(false);
        reloadScreen();
    }

    protected void btnIncCustom() {
        FortytwoEdit.cycleClientCape(true);
        reloadScreen();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        try {
            File file = paths.get(0).toFile();
            if(file.isFile() && file.getName().endsWith(".png")) {
                BufferedImage skin = ImageIO.read(file);
                if((skin.getWidth()==64 && skin.getHeight()==64) || (skin.getWidth()==128 && skin.getHeight()==128)) {
                    if(FortytwoEdit.setCustomSkin(file)) {
                        FortytwoEdit.showToast("Custom skin loaded",file.getName());
                        reloadScreen();
                        return;
                    }
                    reloadScreen();
                }
                else {
                    FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE,"File is not a valid 64x64 or 128x128 skin");
                    return;
                }
            }
            else {
                FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE,"File type must be a PNG");
                return;
            }
        } catch(Exception ex) {}
        FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE,"File could not be read");
    }

    /**
     * Modified from {@link net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse}
     */
    private static void drawPlayer(GuiGraphics context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)Math.atan(mouseX / 40.0f);
        float g = (float)Math.atan(mouseY / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0f * ((float)Math.PI / 180));
        quaternionf.mul(quaternionf2);
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;
        entity.yBodyRot = 0.0f + f * 20.0f;
        entity.setYRot(0.0f + f * 40.0f);
        entity.setXRot(-g * 20.0f);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        InventoryScreen.renderEntityInInventory(context, x, y, size, vec, quaternionf, quaternionf2, entity);
        entity.yBodyRot = h;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Client Capes & Skins"), this.width / 2, y+11, TEXT_COLOR);
        context.drawString(this.font, Component.nullToEmpty("Capes"), x+20,y+7+ROW_HEIGHT*2, LABEL_COLOR);
        context.drawString(this.font, Component.nullToEmpty("Skin"), x+20,y+7+ROW_HEIGHT*5, LABEL_COLOR);
        drawPlayer(context, playerX, playerY, 60, (float)(playerX) - mouseX, (float)(playerY - 50) - mouseY, (LivingEntity)this.minecraft.player);
    }

}
