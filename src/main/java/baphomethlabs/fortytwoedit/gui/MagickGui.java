package baphomethlabs.fortytwoedit.gui;

import java.util.Iterator;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WItem;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class MagickGui extends LightweightGuiDescription {
    public MagickGui() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);
        
        //jigsaw logo
        WItem logo = new WItem(new ItemStack(Items.JIGSAW));

        //menu label
        WLabel lblMenu = new WLabel(Text.of("\u00a75\u00a7lBlack Magick by BaphomethLabs"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //item builder button
        WButton btnItem = new WButton(new ItemIcon(new ItemStack(Items.SPONGE)), Text.of("42edit..."));
        btnItem.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new ItemBuilder()));
        });

        //item from world button
        WButton btnFromWorld = new WButton(new ItemIcon(new ItemStack(Items.ARMOR_STAND)), Text.of("Get Data..."));
        btnFromWorld.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new FromWorld()));
        });

        //hacks button
        WButton btnHacks = new WButton(new ItemIcon(new ItemStack(Items.REPEATING_COMMAND_BLOCK)),Text.of("Hacks..."));
        btnHacks.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new Hacks()));
        });

        //hat
        WButton btnHat = new WButton(new ItemIcon(new ItemStack(Items.DIAMOND_HELMET)), Text.of("Hat"));
        btnHat.setOnClick(() -> {
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
        });

        //cape menu
        WButton btnCapes = new WButton(new ItemIcon(new ItemStack(Items.ELYTRA)),Text.of("Capes..."));
        btnCapes.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new Capes()));
        });

        //auto clickers
        WButton btnAutoClickSettings = new WButton(new ItemIcon(new ItemStack(Items.GOLDEN_SWORD)),Text.of("AutoClick..."));
        btnAutoClickSettings.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new AutoClick()));
        });
        WButton btnAutoClick = new WButton(Text.of(""));
        if(FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnAutoClick.setLabel(Text.of("[Use]"));
        else if(!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnAutoClick.setLabel(Text.of("[Mine]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 1500)
            btnAutoClick.setLabel(Text.of("[Attack 1.5]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 650)
            btnAutoClick.setLabel(Text.of("[Attack .65]"));
        else
            btnAutoClick.setLabel(Text.of("[Custom]"));
        btnAutoClick.setOnClick(() -> {
            if(btnAutoClick.getLabel().getString().equals("[Use]")) {
                btnAutoClick.setLabel(Text.of("[Mine]"));
                FortytwoEdit.updateAutoClick(false,true,false,1500);
            }
            else if(btnAutoClick.getLabel().getString().equals("[Attack .65]")) {
                btnAutoClick.setLabel(Text.of("[Use]"));
                FortytwoEdit.updateAutoClick(true,false,false,1500);
            }
            else if(btnAutoClick.getLabel().getString().equals("[Attack 1.5]")) {
                btnAutoClick.setLabel(Text.of("[Attack .65]"));
                FortytwoEdit.updateAutoClick(false,false,true,650);
            }
            else if(btnAutoClick.getLabel().getString().equals("[Mine]")) {
                btnAutoClick.setLabel(Text.of("[Attack 1.5]"));
                FortytwoEdit.updateAutoClick(false,false,true,1500);
            }
            else {
                btnAutoClick.setLabel(Text.of("[Attack 1.5]"));
                FortytwoEdit.updateAutoClick(false,false,true,1500);
            }
        });

        //add items
        root.add(logo,5,5);
        root.add(lblMenu,120,11,0,0);
        root.add(btnItem,20,44+1,80,20);
        root.add(btnFromWorld,20,66+1,80,20);
        root.add(btnHacks,20,88+1,80,20);
        root.add(btnHat,20,22*5+1,60,20);
        root.add(btnCapes,20,22*6+1,80,20);
        root.add(btnAutoClickSettings,20,22*7+1,90,20);
        root.add(btnAutoClick,20+90+5,22*7+1,70,20);
        
    }
}
