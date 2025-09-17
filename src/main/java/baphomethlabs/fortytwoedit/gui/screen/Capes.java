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
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public class Capes extends GenericScreen {

    protected EditBox txtCustom;
    protected EditBox txtCustomSkin;
    protected final int playerX = backgroundWidth;
    protected final int playerY = 0;
    protected final int playerWidth = 100;
    protected final int playerHeight = backgroundHeight;
    private static final String CUSTOM_SKIN_ERROR_TITLE = "Failed to load skin";

    public Capes() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.CAPES;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new MagickGui())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("OptiFine [On]"),
                Component.literal("OptiFine [Off]")).withInitialValue(FortytwoEdit.opticapesOn).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle OptiFine capes mode\n\nWhen on: you can see players' OptiFine capes"))).create(x+20,y+ROW_HEIGHT*3+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {

            FortytwoEdit.readOptions();
            FortytwoEdit.opticapesOn = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            FortytwoEdit.clearOptiCapes();
            reloadScreen();
        }));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Refresh"), button -> this.btnReloadCapes()).bounds(x+20+80+WID_SPACE,y+ROW_HEIGHT*3+1,60,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Refresh all OptiFine capes")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Edit"), button -> this.btnEditCape()).bounds(x+20+80+WID_SPACE+60+WID_SPACE,y+ROW_HEIGHT*3+1,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Edit your OptiFine cape (requires donation to OptiFine)")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Custom [On]"),
                Component.literal("Custom [Off]")).withInitialValue(FortytwoEdit.showClientCape).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle custom capes mode\n\nWhen on: change your cape (only you can see this)"))).create(x+20,y+ROW_HEIGHT*4+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {

            FortytwoEdit.readOptions();
            FortytwoEdit.showClientCape = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            reloadScreen();
        }));
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
                Component.literal("Custom [Off]")).withInitialValue(FortytwoEdit.showClientSkin).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle custom skin mode\n\nWhen on: change your skin (only you can see this)"))).create(x+20,y+ROW_HEIGHT*6+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.showClientSkin = (boolean)trackOutput;
            unsel();
        }));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("3px"),
                Component.literal("4px")).withInitialValue(FortytwoEdit.clientSkinSlim).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle skin model between wide/slim (requires custom skin mode)"))).create(x+20+80+WID_SPACE,y+ROW_HEIGHT*6+1,30,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.clientSkinSlim = (boolean)trackOutput;
            unsel();
        }));
        this.txtCustomSkin = new EditBox(this.font,x+20,y+ROW_HEIGHT*7+1,200,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtCustomSkin.setMaxLength(MAX_TEXT_LENGTH);
        this.txtCustomSkin.setValue(FortytwoEdit.customSkinName.equals("") ? "<Drag and drop skin into this window>" : FortytwoEdit.customSkinName);
        this.txtCustomSkin.moveCursorToStart(false);
        this.txtCustomSkin.setTooltip(Tooltip.create(Component.nullToEmpty("Drag and drop a skin into this window to set a custom skin")));
        this.txtCustomSkin.setEditable(false);
        this.addRenderableWidget(this.txtCustomSkin);
    }

    protected void btnReloadCapes() {
        FortytwoEdit.showToast("OptiCapes cache cleared",FortytwoEdit.debugCapeNamesSize()+" name(s) and "+FortytwoEdit.debugCapeNames2Size()+" cape(s) deleted");
        FortytwoEdit.clearOptiCapes();
        unsel();
    }

    protected void btnEditCape() {
        try {
            String randomId = (new BigInteger(128, new Random())).xor(new BigInteger(128, new Random(System.identityHashCode(new Object())))).toString(16);
            minecraft.services().sessionService().joinServer(minecraft.getUser().getProfileId(),minecraft.getUser().getAccessToken(),randomId);
            Util.getPlatform().openUri(
                "https://optifine.net/capeChange"
                + "?u=" + minecraft.getUser().getProfileId().toString().replace("-","")
                + "&n=" + minecraft.getUser().getName()
                + "&s=" + randomId);
        }
        catch(Exception ex) {
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

    public static PlayerSkin injectSkinLogic(String name, PlayerSkin current) {
        ClientAsset.Texture body = current.body();
        ClientAsset.Texture cape = current.cape();
        ClientAsset.Texture elytra = current.elytra();
        PlayerModelType model = current.model();
        boolean changed = false;

        //cape
        if(FortytwoEdit.opticapesWorking && FortytwoEdit.opticapesOn) {
            if(FortytwoEdit.capeCached(name)) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath("42edit","cache/cape/"+name.toLowerCase());
                cape = new ClientAsset.ResourceTexture(id, id);
                elytra = cape;
                changed = true;
            }
            else if(!FortytwoEdit.nameCached(name) && FortytwoEdit.capeTimeCheck()) {
                FortytwoEdit.tryLoadCape(name);
            }
        }
        if(FortytwoEdit.showClientCape && name.equals(FortytwoEdit.USERNAME)) {
            cape = FortytwoEdit.getClientCape();
            elytra = cape;
            changed = true;
        }

        //skin
        if(FortytwoEdit.showClientSkin && !FortytwoEdit.customSkinName.equals("") && name.equals(FortytwoEdit.USERNAME)) {
            body = FortytwoEdit.CUSTOM_SKIN_TEXTURE;
            changed = true;
        }


        //model
        if(FortytwoEdit.showClientSkin && name.equals(FortytwoEdit.USERNAME)) {
            if(FortytwoEdit.clientSkinSlim)
                model = PlayerModelType.SLIM;
            else
                model = PlayerModelType.WIDE;

            changed = true;
        }

        if(changed) {
            return new PlayerSkin(body, cape, elytra, model, false);
        }

        return null;
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
     * Replace both 180.0F occurances with 0.0F to flip player backwards.
     * Change `renderEntityInInventory(` to `InventoryScreen.renderEntityInInventory(`
     */
    private static void drawPlayer(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity) {
		float n = (i + k) / 2.0F;
		float o = (j + l) / 2.0F;
		guiGraphics.enableScissor(i, j, k, l);
		float p = (float)Math.atan((n - g) / 40.0F);
		float q = (float)Math.atan((o - h) / 40.0F);
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(q * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float r = livingEntity.yBodyRot;
		float s = livingEntity.getYRot();
		float t = livingEntity.getXRot();
		float u = livingEntity.yHeadRotO;
		float v = livingEntity.yHeadRot;
		livingEntity.yBodyRot = 0.0F + p * 20.0F;
		livingEntity.setYRot(0.0F + p * 40.0F);
		livingEntity.setXRot(-q * 20.0F);
		livingEntity.yHeadRot = livingEntity.getYRot();
		livingEntity.yHeadRotO = livingEntity.getYRot();
		float w = livingEntity.getScale();
		Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + f * w, 0.0F);
		float x = m / w;
		InventoryScreen.renderEntityInInventory(guiGraphics, i, j, k, l, x, vector3f, quaternionf, quaternionf2, livingEntity);
		livingEntity.yBodyRot = r;
		livingEntity.setYRot(s);
		livingEntity.setXRot(t);
		livingEntity.yHeadRotO = u;
		livingEntity.yHeadRot = v;
		guiGraphics.disableScissor();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Client Capes & Skins"), this.width / 2, y+11, TEXT_COLOR);
        context.drawString(this.font, Component.nullToEmpty("Capes"), x+20,y+7+ROW_HEIGHT*2, LABEL_COLOR);
        context.drawString(this.font, Component.nullToEmpty("Skin"), x+20,y+7+ROW_HEIGHT*5, LABEL_COLOR);
        drawPlayer(context, x + playerX, y + playerY, x + playerX + playerWidth, y + playerY + playerHeight, 60, 0.0F, mouseX, mouseY, (LivingEntity)this.minecraft.player);
    }

}
