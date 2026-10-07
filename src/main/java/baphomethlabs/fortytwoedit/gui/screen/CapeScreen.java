package baphomethlabs.fortytwoedit.gui.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        drawEntity(context, x + playerX, y + playerY, x + playerX + playerWidth, y + playerY + playerHeight, 60, 0.0F, mouseX, mouseY, (LivingEntity)this.minecraft.player, true);
    }

}
