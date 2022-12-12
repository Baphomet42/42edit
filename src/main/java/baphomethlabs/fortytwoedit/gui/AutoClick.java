package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.framework.MagickScreen;
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
import net.minecraft.text.Text;

public class AutoClick extends LightweightGuiDescription {

    public AutoClick() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //labels
        WLabel lblMenu = new WLabel(Text.of("Auto Clicker"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //back button
        WButton btnBack = new WButton(Text.of("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));
        });

        //auto buttons

        WButton btnClick = new WButton(new ItemIcon(new ItemStack(Items.FISHING_ROD)),Text.of("Use [Off]"));
        btnClick.setOnClick(() -> {
            if(btnClick.getLabel().getString().equals("Use [Off]")) {
                FortytwoEdit.updateAutoClick(true,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
                btnClick.setLabel(Text.of("Use [On]"));
            }
            else if(btnClick.getLabel().getString().equals("Use [On]")) {
                FortytwoEdit.updateAutoClick(false,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
                btnClick.setLabel(Text.of("Use [Off]"));                
            }
        });
        if(FortytwoEdit.autoClick)
            btnClick.setLabel(Text.of("Use [On]"));
        WButton btnMine = new WButton(new ItemIcon(new ItemStack(Items.DIAMOND_PICKAXE)),Text.of("Mine [Off]"));
        btnMine.setOnClick(() -> {
            if(btnMine.getLabel().getString().equals("Mine [Off]")) {
                FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,true,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
                btnMine.setLabel(Text.of("Mine [On]"));
            }
            else if(btnMine.getLabel().getString().equals("Mine [On]")) {
                FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,false,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
                btnMine.setLabel(Text.of("Mine [Off]"));                
            }
        });
        if(FortytwoEdit.autoMine)
            btnMine.setLabel(Text.of("Mine [On]"));
        WButton btnAttack = new WButton(new ItemIcon(new ItemStack(Items.GOLDEN_SWORD)),Text.of("Attack [Off]"));
        btnAttack.setOnClick(() -> {
            if(btnAttack.getLabel().getString().equals("Attack [Off]")) {
                FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,true,FortytwoEdit.attackWait);
                btnAttack.setLabel(Text.of("Attack [On]"));
            }
            else if(btnAttack.getLabel().getString().equals("Attack [On]")) {
                FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,false,FortytwoEdit.attackWait);
                btnAttack.setLabel(Text.of("Attack [Off]"));                
            }
        });
        if(FortytwoEdit.autoAttack)
            btnAttack.setLabel(Text.of("Attack [On]"));

        //attack cooldown
        WButton btnAttackCooldown = new WButton(Text.of("Attack Cooldown"));
        WTextField txtAttackCooldown = new WTextField();
        txtAttackCooldown.setMaxLength(4);
        btnAttackCooldown.setOnClick(() -> {
            try {
                int inp = Integer.parseInt(txtAttackCooldown.getText());
                if(inp>0) {
                    FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,inp);
                }
                else {
                    txtAttackCooldown.setText(""+FortytwoEdit.attackWait);
                }
            }catch(NumberFormatException e) {
                txtAttackCooldown.setText(""+FortytwoEdit.attackWait);
            }
        });
        txtAttackCooldown.setText(""+FortytwoEdit.attackWait);

        //add items
        root.add(btnBack,5,5,40,20);
        root.add(lblMenu,120,11,0,0);
        root.add(btnClick,20,44+1,100,20);
        root.add(btnMine,20,22*3+1,100,20);
        root.add(btnAttack,20,22*4+1,100,20);
        root.add(btnAttackCooldown,20,22*6+1,100,20);
        root.add(txtAttackCooldown,20+100+5,22*6+1,40,22);

    }

}
