package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ItemBuilder extends GenericScreen {

    protected boolean unsel = false;
    protected int tab = 0;
    protected final int TAB_OFFSET = 5;
    protected final int TAB_SIZE = 24;
    protected final int TAB_SPACING = 2;
    protected String[] tabsLbl = {"General","Display","Blocks","Entity Data","Misc","Custom NBT","Saved Items"};
    protected Item[] tabsItem = {Items.GOLDEN_SWORD,Items.NAME_TAG,Items.BARREL,Items.ENDER_DRAGON_SPAWN_EGG,
        Items.PLAYER_HEAD,Items.COMMAND_BLOCK,Items.JIGSAW};
    protected ButtonWidget[] tabs = new ButtonWidget[tabsLbl.length];
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        
        //tabs
        for(int i = 0; i<tabs.length; i++) {
            int tabNum = i;
            tabs[i] = ButtonWidget.builder(Text.of(""), button -> this.btnTab(tabNum)).dimensions(x-TAB_SIZE,y+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i,TAB_SIZE,TAB_SIZE).build();
            tabs[i].setTooltip(Tooltip.of(Text.of(tabsLbl[i])));
            if(tab==i)
                tabs[i].active = false;
            this.addDrawableChild(tabs[i]);
        }

        //main
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("c*"), button -> this.btnSwapOff(true)).dimensions(width/2 - 50,y+5,20,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("c"), button -> this.btnSwapOff(false)).dimensions(width/2 - 30,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("<"), button -> this.btnChangeSlot(true)).dimensions(width/2 - 15,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(">"), button -> this.btnChangeSlot(false)).dimensions(width/2,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Q"), button -> this.btnThrow(false)).dimensions(width/2 + 15,y+5,15,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Q*"), button -> this.btnThrow(true)).dimensions(width/2 + 30,y+5,20,20).build());

        //general
        if(tab==0) {
        }
        //display
        else if(tab==1) {
        }
        //blocks
        else if(tab==2) {
        }
        //entity data
        else if(tab==3) {
        }
        //misc
        else if(tab==4) {
        }
        //custom nbt
        else if(tab==5) {
        }
        //saved items
        else if(tab==6) {
        }

    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }

    protected void btnSwapOff(boolean copy) {
        if(client.player.getAbilities().creativeMode) {
            if(!copy) {
                ItemStack mainhand = null;
                ItemStack offhand = null;
                if(!client.player.getMainHandStack().isEmpty())
                    mainhand=client.player.getMainHandStack().copy();
                if(!client.player.getOffHandStack().isEmpty())
                    offhand=client.player.getOffHandStack().copy();
                if(mainhand!=null && offhand!=null) {
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
                else if(mainhand!=null && offhand==null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
                else if(mainhand==null && offhand!=null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
            }
            else if(!client.player.getMainHandStack().isEmpty()) {
                ItemStack item=client.player.getMainHandStack().copy();
                client.interactionManager.clickCreativeStack(item, 45);
                client.player.playerScreenHandler.sendContentUpdates();
            }
        }
        unsel = true;
    }

    protected void btnChangeSlot(boolean left) {
        int slot = client.player.getInventory().selectedSlot;
        if(left)
            slot--;
        else
            slot++;
        if(slot<0)
            slot = 8;
        else if(slot>8)
            slot = 0;
        client.player.getInventory().selectedSlot = slot;
        unsel = true;
    }

    protected void btnThrow(boolean copy) {
        if(!copy) {
            client.player.dropSelectedItem(true);
            client.player.playerScreenHandler.sendContentUpdates();
        }
        else if(client.player.getAbilities().creativeMode) {
            ItemStack item = client.player.getMainHandStack().copy();
            client.player.dropSelectedItem(true);
            client.player.playerScreenHandler.sendContentUpdates();
            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
            client.player.playerScreenHandler.sendContentUpdates();
        }
        unsel = true;
    }

    protected void btnTab(int i) {
        tab = i;
        this.resize(this.client,this.width,this.height);
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        for(int i=0; i<tabsItem.length; i++)
		    this.itemRenderer.renderInGui(matrices, new ItemStack(tabsItem[i]),x-TAB_SIZE+(TAB_SIZE/2-8),y+TAB_OFFSET+(TAB_SIZE+TAB_SPACING)*i+(TAB_SIZE/2-8));
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(FortytwoEdit.magickGuiKey).getCode() || keyCode == KeyBindingHelper.getBoundKeyOf(client.options.inventoryKey).getCode()) {
            this.client.setScreen(null);
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if(unsel) {
            GuiNavigationPath guiNavigationPath = this.getFocusedPath();
            if (guiNavigationPath != null) {
                guiNavigationPath.setFocused(false);
            }
            unsel = false;
        }
    }

}
