package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;

public class Hacks extends LightweightGuiDescription {

    WTextField txtRando;

    public Hacks() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //labels
        WLabel lblMenu = new WLabel("Hacks");
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //back button
        WButton btnBack = new WButton(new LiteralText("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));
        });

        //random place
        WButton btnRando = new WButton(new ItemIcon(new ItemStack(Items.CRACKED_STONE_BRICKS)), new LiteralText("Mix [Off]"));
        if(FortytwoEdit.randoMode)
            btnRando.setLabel(new LiteralText("Mix [On]"));
        btnRando.setOnClick(() -> {
            FortytwoEdit.randoMode = !FortytwoEdit.randoMode;
            if(FortytwoEdit.randoMode)
                btnRando.setLabel(new LiteralText("Mix [On]"));
            else
                btnRando.setLabel(new LiteralText("Mix [Off]"));
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
        WButton btnSetRando = new WButton(new LiteralText("Set"));
        btnSetRando.setOnClick(() -> {
            updateRandoSlots();
            if(FortytwoEdit.randoSlots == null) {
                FortytwoEdit.randoMode = false;
                btnRando.setLabel(new LiteralText("Mix [Off]"));
            }
            else {
                FortytwoEdit.randoMode = true;
                btnRando.setLabel(new LiteralText("Mix [On]"));
            }
        });

        //show barrier/void/light button
        WButton btnSeeInvis = new WButton(new ItemIcon(new ItemStack(Items.BARRIER)));
        if(FortytwoEdit.seeInvis)
            btnSeeInvis.setLabel(new LiteralText("Xray Vision [On]"));
        else
            btnSeeInvis.setLabel(new LiteralText("Xray Vision [Off]"));
        btnSeeInvis.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.worldRenderer.reload();
            FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
            if(FortytwoEdit.seeInvis)
                btnSeeInvis.setLabel(new LiteralText("Xray Vision [On]"));
            else
                btnSeeInvis.setLabel(new LiteralText("Xray Vision [Off]"));
        });

        //add items
        root.add(btnBack,5,5,40,20);
        root.add(lblMenu,120,11,0,0);
        root.add(btnRando,20,44+1,80,20);
        root.add(txtRando,105,44+1,100,20);
        root.add(btnSetRando,210,44+1,20,20);
        root.add(btnSeeInvis,20,22*3+1,120,20);

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
