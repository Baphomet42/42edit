package baphomethlabs.fortytwoedit.gui.screen;

import java.util.Iterator;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class MagickGui extends GenericScreen {

    ButtonWidget btnWgtAutoClick;
    ButtonWidget btnWgtHat;
    
    public MagickGui() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("42edit..."), button -> this.btnItem()).dimensions(x+20,y+44+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Hacks..."), button -> this.btnHacks()).dimensions(x+20,y+66+1,80,20).build());
        btnWgtHat = this.addDrawableChild(ButtonWidget.builder(Text.of("Hat"), button -> this.btnHat()).dimensions(x+20,y+22*4+1,60,20).build());
        if(!client.player.getAbilities().creativeMode)
            btnWgtHat.active = false;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Super Secret Settings..."), button -> this.btnSuperSecretSettings()).dimensions(x+20,y+22*5+1,165,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Capes..."), button -> this.btnCapes()).dimensions(x+20,y+22*6+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("AutoClick..."), button -> this.btnAutoClickSettings()).dimensions(x+20,y+22*7+1,90,20).build());
        btnWgtAutoClick = this.addDrawableChild(ButtonWidget.builder(Text.of(""), button -> this.btnAutoClick()).dimensions(x+20+90+5,y+22*7+1,70,20).build());
        setAutoClickMessage();
    }

    protected void btnItem() {
        client.setScreen(new ItemBuilder());
    }

    protected void btnHacks() {
        client.setScreen(new Hacks());
    }

    protected void btnHat() {
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
        this.resize(this.client,this.width,this.height);
    }

    protected void btnSuperSecretSettings() {
        client.gameRenderer.cycleSuperSecretSetting();
        FortytwoEdit.secretSound();
        this.resize(this.client,this.width,this.height);
    }

    protected void btnCapes() {
        client.setScreen(new Capes());
    }

    protected void btnAutoClickSettings() {
        client.setScreen(new AutoClick());
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
        this.resize(this.client,this.width,this.height);
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawItem(new ItemStack(Items.JIGSAW), x+6, y+6);
        context.drawCenteredTextWithShadow(this.textRenderer, BlackMagick.jsonFromString("{\"text\":\"Black Magick by BaphomethLabs\",\"color\":\"#420666\",\"bold\":true}").text(), this.width / 2, y+11, 0xFFFFFF);
		context.drawItem(new ItemStack(Items.SPONGE),x+20+2,y+44+1+2);
		context.drawItem(new ItemStack(Items.REPEATING_COMMAND_BLOCK),x+20+2,y+22*3+1+2);
		context.drawItem(new ItemStack(Items.DIAMOND_HELMET),x+20+2,y+22*4+1+2);
		context.drawItem(new ItemStack(Items.STRUCTURE_BLOCK),x+20+2,y+22*5+1+2);
		context.drawItem(new ItemStack(Items.ELYTRA),x+20+2,y+22*6+1+2);
		context.drawItem(new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+22*7+1+2);
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
