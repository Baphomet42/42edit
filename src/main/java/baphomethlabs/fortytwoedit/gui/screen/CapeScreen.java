package baphomethlabs.fortytwoedit.gui.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.Blaze3D;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;

public class CapeScreen extends GenericScreen {

    protected SmartEditBox txtCustomSkin;
    protected final int playerX = backgroundWidth;
    protected final int playerY = 0;
    protected final int playerWidth = 100;
    protected final int playerHeight = backgroundHeight;
    private static final String CUSTOM_SKIN_ERROR_TITLE = "Failed to load skin";

    public CapeScreen() {
        super("Client Capes & Skins");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = CapeScreen::new;
        this.addBackButton();

        FortytwoEdit.resolveCapeUrlQueue();

        setupScrollPane();
        paneScroll().addRow("Cape", false);
        paneScroll().addRow(
            WIDGET_UTIL.newButton("OptiCapes", btn -> {
                    OptionsUtil.ModOptions.OPTICAPES.toggleSetting();
                    FortytwoEdit.clearOptiCapes();
                    rebuildWidgets();
                }).setBoolName(OptionsUtil.ModOptions.OPTICAPES.getSetting()).setSize(80)
                .setTooltip(OptionsUtil.ModOptions.OPTICAPES.getButtonTooltip()).build(),
            WIDGET_UTIL.newButton("Refresh", btn -> this.btnReloadCapes())
                .setTooltip("Refresh all OptiCapes").build(),
            WIDGET_UTIL.newButton("Edit", btn -> this.btnEditCape()).setSize(40)
                .setTooltip("Edit your OptiFine cape (if you have one)").build()
        ).stretchPrev(2);
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Custom", btn -> {
                    OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.toggleSetting();
                    rebuildWidgets();
                }).setBoolName(OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.getSetting()).setSize(80)
                .setTooltip(OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.getButtonTooltip()).build(),
            WIDGET_UTIL.newButton("Cape: [" + FortytwoEdit.getCurrentClientCape().name() + "]", btn -> {
                    OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.setSetting(true);
                    changeScreen(new CapeSelectorScreen());
                }).setTooltip("Select custom cape").build()
        ).stretchPrev();

        paneScroll().addRow("Skin", false);
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Custom", btn -> {
                    FortytwoEdit.showClientSkin = !FortytwoEdit.showClientSkin;
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.showClientSkin).setSize(80)
                .setTooltip("Toggle custom skin mode\n\nWhen on: change your skin (only you can see this)").build(),
            WIDGET_UTIL.newButton(FortytwoEdit.clientSkinSlim ? "3px" : "4px", btn -> {
                    FortytwoEdit.clientSkinSlim = !FortytwoEdit.clientSkinSlim;
                    rebuildWidgets();
                }).setSize(40).setTooltip("Toggle skin model between wide/slim (requires custom skin mode)").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newEditBox().fullWidth()
                .setValue(FortytwoEdit.customSkinName.equals("") ? "<Drag and drop skin into this window>" : FortytwoEdit.customSkinName)
                .moveCursorToStart(false).setTooltip("Drag and drop a skin into this window to set a custom skin")
                .setEditable(false).runWithSelf(w -> this.txtCustomSkin = w).build()
        );
        finalizeScrollPane();
    }

    protected void btnReloadCapes() {
        FortytwoEdit.showToast("OptiCapes cache cleared", FortytwoEdit.debugCapeNamesSize() + " name(s) and " + FortytwoEdit.debugCapeNames2Size() + " cape(s) deleted");
        FortytwoEdit.clearOptiCapes();
    }

    protected void btnEditCape() {
        try {
            String randomId = (new BigInteger(128, new Random())).xor(new BigInteger(128, new Random(System.identityHashCode(new Object())))).toString(16);
            minecraft.services().sessionService().joinServer(minecraft.getUser().getProfileId(), minecraft.getUser().getAccessToken(), randomId);
            Blaze3D.openUri(URI.create(
                "https://optifine.net/capeChange"
                + "?u=" + minecraft.getUser().getProfileId().toString().replace("-", "")
                + "&n=" + minecraft.getUser().getName()
                + "&s=" + randomId));
        }
        catch (Exception ex) {
            FortytwoEdit.showToast("Failed to edit cape", "Could not open OptiFine cape editor webpage");
        }
    }

    public static PlayerSkin injectSkinLogic(String name, PlayerSkin current) {
        ClientAsset.Texture body = current.body();
        ClientAsset.Texture cape = current.cape();
        ClientAsset.Texture elytra = current.elytra();
        PlayerModelType model = current.model();
        boolean changed = false;

        // cape
        if (FortytwoEdit.opticapesWorking && OptionsUtil.ModOptions.OPTICAPES.getSetting()) {
            if (FortytwoEdit.capeCached(name)) {
                Identifier id = Identifier.fromNamespaceAndPath("42edit", "cache/cape/" + name.toLowerCase());
                cape = new ClientAsset.ResourceTexture(id);
                elytra = cape;
                changed = true;
            }
            else if (!FortytwoEdit.nameCached(name) && FortytwoEdit.capeTimeCheck()) {
                FortytwoEdit.tryLoadCape(name);
            }
        }
        if (OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.getSetting() && name.equals(FortytwoEdit.USERNAME)) {
            cape = FortytwoEdit.getClientCapeTexture();
            elytra = cape;
            changed = true;
        }

        // skin
        if (FortytwoEdit.showClientSkin && !FortytwoEdit.customSkinName.equals("") && name.equals(FortytwoEdit.USERNAME)) {
            body = FortytwoEdit.CUSTOM_SKIN_TEXTURE;
            changed = true;
        }


        // model
        if (FortytwoEdit.showClientSkin && name.equals(FortytwoEdit.USERNAME)) {
            if (FortytwoEdit.clientSkinSlim)
                model = PlayerModelType.SLIM;
            else
                model = PlayerModelType.WIDE;

            changed = true;
        }

        if (changed) {
            return new PlayerSkin(body, cape, elytra, model, false);
        }

        return null;
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        try {
            File file = paths.get(0).toFile();
            if (file.isFile() && file.getName().endsWith(".png")) {
                BufferedImage skin = ImageIO.read(file);
                if ((skin.getWidth() == 64 && skin.getHeight() == 64) || (skin.getWidth() == 128 && skin.getHeight() == 128)) {
                    if (FortytwoEdit.setCustomSkin(file)) {
                        FortytwoEdit.showToast("Custom skin loaded", file.getName());
                        rebuildWidgets();
                        return;
                    }
                    rebuildWidgets();
                }
                else {
                    FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE, "File is not a valid 64x64 or 128x128 skin");
                    return;
                }
            }
            else {
                FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE, "File type must be a PNG");
                return;
            }
        } catch (Exception ex) {}
        FortytwoEdit.showToast(CUSTOM_SKIN_ERROR_TITLE, "File could not be read");
    }

    /**
     * Modified from {@link net.minecraft.client.gui.screens.inventory.InventoryScreen#renderEntityInInventoryFollowsMouse}
     * Replace y body rot 180.0F occurances with 0.0F to flip player backwards. Do not replace 180.0 in quaternion
     * Copy referenced `extractRenderState` method here
     */
    public static void drawPlayer(GuiGraphicsExtractor guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity) {
        float n = (i + k) / 2.0F;
        float o = (j + l) / 2.0F;
        float p = (float)Math.atan((n - g) / 40.0F);
        float q = (float)Math.atan((o - h) / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(q * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        EntityRenderState entityRenderState = extractRenderState(livingEntity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyRot = 0.0F + p * 20.0F;
            livingEntityRenderState.yRot = p * 20.0F;
            if (livingEntityRenderState.pose != Pose.FALL_FLYING) {
                livingEntityRenderState.xRot = -q * 20.0F;
            } else {
                livingEntityRenderState.xRot = 0.0F;
            }

            livingEntityRenderState.boundingBoxWidth = livingEntityRenderState.boundingBoxWidth / livingEntityRenderState.scale;
            livingEntityRenderState.boundingBoxHeight = livingEntityRenderState.boundingBoxHeight / livingEntityRenderState.scale;
            livingEntityRenderState.scale = 1.0F;
        }

        Vector3f vector3f = new Vector3f(0.0F, entityRenderState.boundingBoxHeight / 2.0F + f, 0.0F);
        guiGraphics.entity(entityRenderState, m, vector3f, quaternionf, quaternionf2, i, j, k, l);
    }
    private static EntityRenderState extractRenderState(LivingEntity livingEntity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(livingEntity);
        EntityRenderState entityRenderState = entityRenderer.createRenderState(livingEntity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        drawPlayer(context, x + playerX, y + playerY, x + playerX + playerWidth, y + playerY + playerHeight, 60, 0.0F, mouseX, mouseY, (LivingEntity)this.minecraft.player);
    }

}
