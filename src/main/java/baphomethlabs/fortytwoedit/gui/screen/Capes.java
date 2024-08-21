package baphomethlabs.fortytwoedit.gui.screen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class Capes extends GenericScreen {

    protected TextFieldWidget txtCustom;
    protected TextFieldWidget txtCustomSkin;
    protected int playerX;
    protected int playerY;
    private static final Vector3f vec = new Vector3f();
    
    public Capes() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.CAPES;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("OptiFine [On]"), Text.literal("OptiFine [Off]")).initially(FortytwoEdit.opticapesOn).omitKeyText().build(x+20,y+22*3+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.opticapesOn = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            FortytwoEdit.clearCapes();
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Toggle OptiFine capes mode\n\nWhen on: you can see players' OptiFine capes")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Refresh"), button -> this.btnReloadCapes()).dimensions(x+20+80+5,y+22*3+1,60,20).build())
            .setTooltip(Tooltip.of(Text.of("Refresh all OptiFine capes")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Edit"), button -> this.btnEditCape()).dimensions(x+20+80+5+60+5,y+22*3+1,40,20).build())
            .setTooltip(Tooltip.of(Text.of("Edit your OptiFine cape (requires donation to OptiFine)")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Custom [On]"), Text.literal("Custom [Off]")).initially(FortytwoEdit.showClientCape).omitKeyText().build(x+20,y+22*4+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.showClientCape = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Toggle custom capes mode\n\nWhen on: change your cape (only you can see this)")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnDecCustom()).dimensions(x+20+80+5,y+22*4+1,15,20).build())
            .setTooltip(Tooltip.of(Text.of("Cycle custom cape left")));
        this.txtCustom = new TextFieldWidget(this.textRenderer,x+20+1+80+5+15,y+22*4+1,90-2,20,Text.of(""));
        this.txtCustom.setMaxLength(256);
        this.txtCustom.setText(FortytwoEdit.CLIENT_CAPES[FortytwoEdit.clientCape].name());
        this.txtCustom.setCursorToStart(false);
        this.txtCustom.setTooltip(buildCapeTooltip());
        this.txtCustom.setEditable(false);
        this.addDrawableChild(this.txtCustom);
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnIncCustom()).dimensions(x+20+80+5+15+90,y+22*4+1,15,20).build())
            .setTooltip(Tooltip.of(Text.of("Cycle custom cape right")));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Custom [On]"), Text.literal("Custom [Off]")).initially(FortytwoEdit.showClientSkin).omitKeyText().build(x+20,y+22*6+1,80,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.showClientSkin = (boolean)trackOutput;
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Toggle custom skin mode\n\nWhen on: change your skin (only you can see this)")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("3px"), Text.literal("4px")).initially(FortytwoEdit.clientSkinSlim).omitKeyText().build(x+20+80+5,y+22*6+1,30,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.clientSkinSlim = (boolean)trackOutput;
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Toggle skin model between wide/slim (requires custom skin mode)")));
        this.txtCustomSkin = new TextFieldWidget(this.textRenderer,x+20+1,y+22*7+1,200-2,20,Text.of(""));
        this.txtCustomSkin.setMaxLength(2048);
        this.txtCustomSkin.setText(FortytwoEdit.customSkinName.equals("") ? "<Drag and drop skin into this window>" : FortytwoEdit.customSkinName);
        this.txtCustomSkin.setCursorToStart(false);
        this.txtCustomSkin.setTooltip(Tooltip.of(Text.of("Drag and drop a skin into this window to set a custom skin")));
        this.txtCustomSkin.setEditable(false);
        this.addDrawableChild(this.txtCustomSkin);
        playerX = x + 240+40;
        playerY = this.height/2 + 30;
    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnReloadCapes() {
        client.player.sendMessage(Text.of("Cleared "+FortytwoEdit.debugCapeNamesSize()+" cached names."),false);
        client.player.sendMessage(Text.of("Deleted "+FortytwoEdit.debugCapeNames2Size()+" cached capes."),false);
        FortytwoEdit.clearCapes();
        unsel();
    }

    protected void btnEditCape() {
        //from https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/mixin/SkinOptionsScreenMixin.java
        try {
            BigInteger random1Bi = new BigInteger(128, new Random());
            BigInteger random2Bi = new BigInteger(128, new Random(System.identityHashCode(new Object())));
            String serverId = random1Bi.xor(random2Bi).toString(16);
            client.getSessionService().joinServer(client.getSession().getUuidOrNull(),client.getSession().getAccessToken(),serverId);
            String url = "https://optifine.net/capeChange?u="+client.getSession().getUuidOrNull().toString().replaceAll("-","")+"&n="+client.getSession().getUsername()+"&s="+serverId;
            Util.getOperatingSystem().open(url);
        } catch (Exception ex) {}
        unsel();
    }

    protected void btnDecCustom() {
        FortytwoEdit.clientCape--;
        if(FortytwoEdit.clientCape<0)
            FortytwoEdit.clientCape=FortytwoEdit.CLIENT_CAPES.length-1;
        FortytwoEdit.updateOptions();
        this.resize(this.client,this.width,this.height);
    }

    protected void btnIncCustom() {
        FortytwoEdit.clientCape++;
        if(FortytwoEdit.clientCape>=FortytwoEdit.CLIENT_CAPES.length)
            FortytwoEdit.clientCape=0;
        FortytwoEdit.updateOptions();
        this.resize(this.client,this.width,this.height);
    }

    private Tooltip buildCapeTooltip() {
        MutableText tt = Text.of(FortytwoEdit.CLIENT_CAPES[FortytwoEdit.clientCape].name()).copy();
        if(FortytwoEdit.CLIENT_CAPES[FortytwoEdit.clientCape].desc() != null)
            tt.append("\n\n").append(Text.of(FortytwoEdit.CLIENT_CAPES[FortytwoEdit.clientCape].desc()).copy().formatted(Formatting.GRAY));
        return Tooltip.of(tt);
    }

    @Override
    public void filesDragged(List<Path> paths) {
        try {
            File file = paths.get(0).toFile();
            if(file.isFile() && file.getName().endsWith(".png")) {
                BufferedImage skin = ImageIO.read(file);
                if((skin.getWidth()==64 && skin.getHeight()==64) || (skin.getWidth()==128 && skin.getHeight()==128)) {
                    if(FortytwoEdit.setCustomSkin(file)) {
                        FortytwoEdit.showClientSkin = true;
                        SystemToast.add(client.getToastManager(), SystemToast.Type.PACK_COPY_FAILURE, Text.of("Custom skin loaded"), Text.of(file.getName()));
                        this.resize(this.client,this.width,this.height);
                    }
                }
            }
        } catch (Exception e) {}
    }

    private static void drawPlayer(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)Math.atan(mouseX / 40.0f);
        float g = (float)Math.atan(mouseY / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0f * ((float)Math.PI / 180));
        quaternionf.mul(quaternionf2);
        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        entity.bodyYaw = 0.0f + f * 20.0f;
        entity.setYaw(0.0f + f * 40.0f);
        entity.setPitch(-g * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        InventoryScreen.drawEntity(context, x, y, size, vec, quaternionf, quaternionf2, entity);
        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Client Capes & Skins"), this.width / 2, y+11, TEXT_COLOR);
        context.drawTextWithShadow(this.textRenderer, Text.of("Capes"), x+20,y+7+22*2, LABEL_COLOR);
        context.drawTextWithShadow(this.textRenderer, Text.of("Skin"), x+20,y+7+22*5, LABEL_COLOR);
        drawPlayer(context, playerX, playerY, 60, (float)(playerX) - mouseX, (float)(playerY - 50) - mouseY, (LivingEntity)this.client.player);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context, delta, mouseX, mouseY, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            this.client.setScreen(null);
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

}
