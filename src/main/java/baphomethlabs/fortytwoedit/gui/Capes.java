package baphomethlabs.fortytwoedit.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class Capes extends GenericScreen {

    protected TextFieldWidget txtCustom;
    protected TextFieldWidget txtCustomSkin;
    
    public Capes() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("OptiFine [On]"), Text.literal("OptiFine [Off]")).initially(FortytwoEdit.opticapesOn).omitKeyText().build(x+20,y+22*2+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.opticapesOn = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            FortytwoEdit.clearCapes();
        }));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Refresh"), button -> this.btnReloadCapes()).dimensions(x+20+80+5,y+22*2+1,60,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Edit"), button -> this.btnEditCape()).dimensions(x+20+80+5+60+5,y+22*2+1,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Custom [On]"), Text.literal("Custom [Off]")).initially(FortytwoEdit.showClientCape).omitKeyText().build(x+20,y+22*3+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.showClientCape = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
        }));
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnDecCustom()).dimensions(x+20+80+5,y+22*3+1,15,20).build());
        this.txtCustom = new TextFieldWidget(this.textRenderer,x+20+1+80+5+15,y+22*3+1,90-2,20,Text.of(""));
        this.txtCustom.setMaxLength(64);
        this.txtCustom.setEditable(false);
        this.txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
        this.addSelectableChild(this.txtCustom);
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnIncCustom()).dimensions(x+20+80+5+15+90,y+22*3+1,15,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Custom [On]"), Text.literal("Custom [Off]")).initially(FortytwoEdit.showClientSkin).omitKeyText().build(x+20,y+22*5+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.showClientSkin = (boolean)trackOutput;
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("3px"), Text.literal("4px")).initially(FortytwoEdit.clientSkinSlim).omitKeyText().build(x+20+80+5,y+22*5+1,30,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.clientSkinSlim = (boolean)trackOutput;
        }));
        this.txtCustomSkin = new TextFieldWidget(this.textRenderer,x+20+1,y+22*6+1,200-2,20,Text.of(""));
        this.txtCustomSkin.setMaxLength(2048);
        this.txtCustomSkin.setEditable(false);
        this.txtCustomSkin.setText(FortytwoEdit.customSkinName.equals("") ? "<Drag and drop skin into this window>" : FortytwoEdit.customSkinName);
        this.addSelectableChild(this.txtCustomSkin);
    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnReloadCapes() {
        client.player.sendMessage(Text.of("Cleared "+FortytwoEdit.debugCapeNamesSize()+" cached names."),false);
        client.player.sendMessage(Text.of("Deleted "+FortytwoEdit.debugCapeNames2Size()+" cached capes."),false);
        FortytwoEdit.clearCapes();
    }

    protected void btnEditCape() {
        //from https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/mixin/SkinOptionsScreenMixin.java
        try {
            BigInteger random1Bi = new BigInteger(128, new Random());
            BigInteger random2Bi = new BigInteger(128, new Random(System.identityHashCode(new Object())));
            String serverId = random1Bi.xor(random2Bi).toString(16);
            client.getSessionService().joinServer(client.getSession().getProfile(),client.getSession().getAccessToken(),serverId);
            String url = "https://optifine.net/capeChange?u="+client.getSession().getUuid()+"&n="+client.getSession().getUsername()+"&s="+serverId;
            Util.getOperatingSystem().open(url);
        } catch (Exception ex) {}
    }

    protected void btnDecCustom() {
        FortytwoEdit.clientCape--;
        if(FortytwoEdit.clientCape<0)
            FortytwoEdit.clientCape=FortytwoEdit.clientCapeList.length-1;
        txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
        FortytwoEdit.updateOptions();
    }

    protected void btnIncCustom() {
        FortytwoEdit.clientCape++;
        if(FortytwoEdit.clientCape>=FortytwoEdit.clientCapeList.length)
            FortytwoEdit.clientCape=0;
        txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
        FortytwoEdit.updateOptions();
    }

    @Override
    public void filesDragged(List<Path> paths) {
        try {
            File file = paths.get(0).toFile();
            if(file.isFile() && file.getName().endsWith(".png")) {
                BufferedImage skin = ImageIO.read(file);
                if((skin.getWidth()==64 && skin.getHeight()==64) || (skin.getWidth()==128 && skin.getHeight()==128)) {
                    if(FortytwoEdit.setCustomSkin(file)) {
                        txtCustomSkin.setText(FortytwoEdit.customSkinName);
                        SystemToast.add(client.getToastManager(), SystemToast.Type.PACK_COPY_FAILURE, Text.of("Custom skin loaded"), Text.of(file.getName()));
                    }
                }
            }
        } catch (Exception e) {}
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        MagickGui.drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of("Client Capes & Skins"), this.width / 2, y+11, 0xFFFFFF);
        MagickGui.drawTextWithShadow(matrices, this.textRenderer, Text.of("Capes"), x+20,y+5+7+22*1, 0xFFFFFF);
        MagickGui.drawTextWithShadow(matrices, this.textRenderer, Text.of("Skin"), x+20,y+5+7+22*4, 0xFFFFFF);
        this.txtCustom.render(matrices, mouseX, mouseY, delta);
        this.txtCustomSkin.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

}
