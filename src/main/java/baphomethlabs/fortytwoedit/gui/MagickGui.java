package baphomethlabs.fortytwoedit.gui;

import java.util.Iterator;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class MagickGui extends GenericScreen {

    ButtonWidget btnWgtAutoClick;
    
    public MagickGui() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("42edit..."), button -> this.btnItem()).dimensions(x+20,y+44+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Elder..."), button -> this.btnItemOld()).dimensions(x+20+80+5,y+44+1,80,20).build());//TODO
        this.addDrawableChild(ButtonWidget.builder(Text.of("Hacks..."), button -> this.btnHacks()).dimensions(x+20,y+66+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Hat"), button -> this.btnHat()).dimensions(x+20,y+22*4+1,60,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Super Secret Settings..."), button -> this.btnSuperSecretSettings()).dimensions(x+20,y+22*5+1,165,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Capes..."), button -> this.btnCapes()).dimensions(x+20,y+22*6+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("AutoClick..."), button -> this.btnAutoClickSettings()).dimensions(x+20,y+22*7+1,90,20).build());
        btnWgtAutoClick = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> this.btnAutoClick()).dimensions(x+20+90+5,y+22*7+1,70,20).build());
        setAutoClickMessage();
    }

    protected void btnItem() {
        MinecraftClient.getInstance().setScreen(new ItemBuilder());
    }

    protected void btnItemOld() {//TODO
        MinecraftClient.getInstance().setScreen(new baphomethlabs.fortytwoedit.gui.legacy.framework.MagickScreen(new baphomethlabs.fortytwoedit.gui.legacy.ItemBuilder()));
    }

    protected void btnHacks() {
        MinecraftClient.getInstance().setScreen(new Hacks());
    }

    protected void btnHat() {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.player.getAbilities().creativeMode) {
            ItemStack hand = null;
            ItemStack head = null;
            Iterator<ItemStack> armor = null;
            if(!client.player.getMainHandStack().isEmpty())
                hand=client.player.getMainHandStack().copy();
            if(client.player.getArmorItems()!=null)
                armor=client.player.getArmorItems().iterator();
            for(int i=0;i<4;i++) {
                if(armor!=null && armor.hasNext())
                    head=armor.next().copy();
                else
                    head=null;
            }
            if(hand!=null && head!=null) {
                client.interactionManager.clickCreativeStack(hand, 5);
                client.interactionManager.clickCreativeStack(head, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
            }
            else if(hand!=null && head==null) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","air");
                client.interactionManager.clickCreativeStack(hand, 5);
                client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
            }
            else if(hand==null && head!=null) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","air");
                client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 5);
                client.interactionManager.clickCreativeStack(head, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
            }
        }
    }

    protected void btnSuperSecretSettings() {
        final MinecraftClient client = MinecraftClient.getInstance();
        client.gameRenderer.cycleSuperSecretSetting();
        FortytwoEdit.secretSound();
    }

    protected void btnCapes() {
        MinecraftClient.getInstance().setScreen(new Capes());
    }

    protected void btnAutoClickSettings() {
        MinecraftClient.getInstance().setScreen(new AutoClick());
    }

    protected void btnAutoClick() {
        if(btnWgtAutoClick.getMessage().getString().equals("[Use]")) {
            FortytwoEdit.updateAutoClick(false,true,false,1500);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Attack .65]")) {
            FortytwoEdit.updateAutoClick(true,false,false,1500);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Attack 1.5]")) {
            FortytwoEdit.updateAutoClick(false,false,true,650);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Mine]")) {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        else {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        setAutoClickMessage();
    }

    private void setAutoClickMessage() {
        if(FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Text.of("[Use]"));
        else if(!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Text.of("[Mine]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 1500)
            btnWgtAutoClick.setMessage(Text.of("[Attack 1.5]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 650)
            btnWgtAutoClick.setMessage(Text.of("[Attack .65]"));
        else
            btnWgtAutoClick.setMessage(Text.of("[Custom]"));
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.JIGSAW), x+6, y+6);
        MagickGui.drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of("\u00a75\u00a7lBlack Magick by BaphomethLabs"), this.width / 2, y+11, 0xFFFFFF);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.SPONGE),x+20+2,y+44+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.REPEATING_COMMAND_BLOCK),x+20+2,y+22*3+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.DIAMOND_HELMET),x+20+2,y+22*4+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.STRUCTURE_BLOCK),x+20+2,y+22*5+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.ELYTRA),x+20+2,y+22*6+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+22*7+1+2);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == KeyBindingHelper.getBoundKeyOf(FortytwoEdit.magickGuiKey).getCode() || keyCode == KeyBindingHelper.getBoundKeyOf(client.options.inventoryKey).getCode()) {
            this.client.setScreen(null);
            return true;
        }
        return false;
    }

}
