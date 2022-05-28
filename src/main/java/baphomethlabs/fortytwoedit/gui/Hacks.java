package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.GlobalPos;

public class Hacks extends LightweightGuiDescription {

    WTextField txtRando;

    public Hacks() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //labels
        WLabel lblMenu = new WLabel(Text.of("Hacks"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //back button
        WButton btnBack = new WButton(Text.of("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));
        });

        //random place
        WButton btnRando = new WButton(new ItemIcon(new ItemStack(Items.CRACKED_STONE_BRICKS)), Text.of("Mix [Off]"));
        if(FortytwoEdit.randoMode)
            btnRando.setLabel(Text.of("Mix [On]"));
        btnRando.setOnClick(() -> {
            FortytwoEdit.randoMode = !FortytwoEdit.randoMode;
            if(FortytwoEdit.randoMode)
                btnRando.setLabel(Text.of("Mix [On]"));
            else
                btnRando.setLabel(Text.of("Mix [Off]"));
        });
        txtRando = new WTextField();
        txtRando.setMaxLength(15);
        if(FortytwoEdit.randoSlots != null) {
            String keys = "";
            for(int i: FortytwoEdit.randoSlots) {
                keys += i;
            }
            txtRando.setText(keys);
        }
        WButton btnSetRando = new WButton(Text.of("Set"));
        btnSetRando.setOnClick(() -> {
            updateRandoSlots();
            if(FortytwoEdit.randoSlots == null) {
                FortytwoEdit.randoMode = false;
                btnRando.setLabel(Text.of("Mix [Off]"));
            }
            else {
                FortytwoEdit.randoMode = true;
                btnRando.setLabel(Text.of("Mix [On]"));
            }
        });

        //show barrier/void/light button
        WButton btnSeeInvis = new WButton(new ItemIcon(new ItemStack(Items.BARRIER)));
        if(FortytwoEdit.seeInvis)
            btnSeeInvis.setLabel(Text.of("Xray Vision [On]"));
        else
            btnSeeInvis.setLabel(Text.of("Xray Vision [Off]"));
        btnSeeInvis.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.worldRenderer.reload();
            FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
            if(FortytwoEdit.seeInvis)
                btnSeeInvis.setLabel(Text.of("Xray Vision [On]"));
            else
                btnSeeInvis.setLabel(Text.of("Xray Vision [Off]"));
        });

        //get last death pos
        WButton btnDeathPos = new WButton(new ItemIcon(new ItemStack(Items.SKELETON_SKULL)), Text.of("Death Pos"));
        btnDeathPos.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getLastDeathPos().isPresent()) {
                GlobalPos pos = client.player.getLastDeathPos().get();
                String coords = "Last death [X: "+pos.getPos().getX()+", Y: "+pos.getPos().getY()+", Z: "+pos.getPos().getZ()+"] in "+pos.getDimension().getValue().toString();
                client.player.sendMessage(Text.of(coords),false);
            }
            else {
                client.player.sendMessage(Text.of("No death pos recorded"),false);
            }
        });

        //get bee count
        WButton btnBeeCount = new WButton(new ItemIcon(new ItemStack(Items.BEE_NEST)),Text.of("Bee Count"));
        WLabel lblBeeCount = new WLabel(Text.of(""));
        btnBeeCount.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            ItemStack item = client.player.getMainHandStack();
            if(item.hasNbt() && (item.getItem().toString().equals("beehive") || item.getItem().toString().equals("bee_nest"))) {
                NbtCompound nbt = item.getNbt();
                if(nbt.contains("BlockEntityTag") && nbt.get("BlockEntityTag").getType() == NbtType.COMPOUND) {
                    NbtCompound tag = (NbtCompound)nbt.get("BlockEntityTag");
                    if(tag.contains("Bees") && tag.get("Bees").getType() == NbtType.LIST) {
                        int beeCount = ((NbtList)tag.get("Bees")).size();
                        lblBeeCount.setText(Text.of("["+beeCount+"]"));
                    }
                    else
                        lblBeeCount.setText(Text.of("[0]"));
                }
                else
                    lblBeeCount.setText(Text.of("[0]"));
            }
            else
                lblBeeCount.setText(Text.of("[0]"));
        });

        //add items
        root.add(btnBack,5,5,40,20);
        root.add(lblMenu,120,11,0,0);
        root.add(btnRando,20,44+1,80,20);
        root.add(txtRando,105,44+1,100,20);
        root.add(btnSetRando,210,44+1,20,20);
        root.add(btnSeeInvis,20,22*3+1,120,20);
        root.add(btnDeathPos,20,22*4+1,100,20);
        root.add(btnBeeCount,20,22*5+1,100,20);
        root.add(lblBeeCount,20+100+5,22*5+1+6,60,20);

    }

    //update randoSlots
    private void updateRandoSlots() {
        if(txtRando.getText() != null && !txtRando.getText().equals("")) {
            String inp = txtRando.getText();
            inp = inp.replaceAll("[^1-9]","");
            if(inp.length()>0) {
                int[] slots = new int[inp.length()];
                for(int i=0; i<slots.length; i++) {
                    try{
                        slots[i]=Integer.parseInt(""+inp.charAt(i));
                    }
                    catch(NumberFormatException ex) {}
                }
                FortytwoEdit.randoSlots = slots;
            }
            else
                FortytwoEdit.randoSlots = null;
        }
        else
            FortytwoEdit.randoSlots = null;
    }

}
