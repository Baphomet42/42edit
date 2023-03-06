package baphomethlabs.fortytwoedit.gui;

import java.math.BigInteger;
import java.util.Random;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class Capes extends GenericScreen {

    protected TextFieldWidget txtCustom;
    
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
        this.addDrawableChild(ButtonWidget.builder(Text.of("Reload Capes"), button -> this.btnReloadCapes()).dimensions(x+20,y+22*3+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Edit Cape"), button -> this.btnEditCape()).dimensions(x+105,y+22*3+1,60,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Custom [On]"), Text.literal("Custom [Off]")).initially(FortytwoEdit.showClientCape).omitKeyText().build(x+20,y+22*5+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.showClientCape = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
        }));
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnDecCustom()).dimensions(x+20,y+22*6+1,20,20).build());
        this.txtCustom = new TextFieldWidget(this.textRenderer,x+20+1+20+5,y+22*6+1,120,20,Text.of(""));
        this.txtCustom.setMaxLength(64);
        this.txtCustom.setEditable(false);
        this.txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnIncCustom()).dimensions(x+20+20+120+5+5+2,y+22*6+1,20,20).build());
    }

    protected void btnBack() {
        MinecraftClient.getInstance().setScreen(new MagickGui());
    }

    protected void btnReloadCapes() {
        final MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.of("Cleared "+FortytwoEdit.debugCapeNamesSize()+" cached names."),false);
        client.player.sendMessage(Text.of("Deleted "+FortytwoEdit.debugCapeNames2Size()+" cached capes."),false);
        FortytwoEdit.clearCapes();
    }

    protected void btnEditCape() {
        try {
            final MinecraftClient client = MinecraftClient.getInstance();
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        MagickGui.drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of("Capes"), this.width / 2, y+11, 0xFFFFFF);
        MagickGui.drawTextWithShadow(matrices, this.textRenderer, Text.of("OptiFine"), x+20,y+5+7+22*1, 0xFFFFFF);
        MagickGui.drawTextWithShadow(matrices, this.textRenderer, Text.of("Custom"), x+20,y+5+7+22*4, 0xFFFFFF);
        this.txtCustom.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

}
