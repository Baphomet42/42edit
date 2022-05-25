package baphomethlabs.fortytwoedit.gui;

import java.util.Iterator;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WItem;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
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

        //look button
        WButton btnLookN = new WButton(Text.of("Look N"));
        btnLookN.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                180f,client.player.getPitch());
        });
        WButton btnLookR = new WButton(Text.of("Rotate"));
        btnLookR.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            float yaw = client.player.getYaw() + 90;
            if(yaw>=360)
                yaw=yaw-360;
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                yaw,client.player.getPitch());
        });
        WButton btnLookH = new WButton(new ItemIcon(new ItemStack(Items.SPYGLASS)), Text.of("Look H"));
        btnLookH.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                client.player.getYaw(),0f);
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

        //add items
        root.add(logo,5,5);
        root.add(lblMenu,120,11,0,0);
        root.add(btnItem,20,44+1,80,20);
        root.add(btnFromWorld,20,66+1,80,20);
        root.add(btnHacks,20,88+1,80,20);
        root.add(btnLookH,20,22*5+1,80,20);
        root.add(btnLookN,20+80+5,22*5+1,40,20);
        root.add(btnLookR,20+80+5+40+5,22*5+1,40,20);
        root.add(btnHat,20,22*6+1,60,20);
        root.add(btnCapes,20,22*7+1,80,20);

        
    }
}
