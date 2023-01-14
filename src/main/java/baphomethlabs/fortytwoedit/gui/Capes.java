package baphomethlabs.fortytwoedit.gui;

import java.math.BigInteger;
import java.util.Random;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.framework.MagickScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class Capes extends LightweightGuiDescription {

    public Capes() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //labels
        WLabel lblMenu = new WLabel(Text.of("Capes"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //back button
        WButton btnBack = new WButton(Text.of("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));
        });

        //optifine capes
        WLabel lblOptifine = new WLabel(Text.of("Optifine"));
        WButton btnOptiCape = new WButton(Text.of("Show Optifine Capes [On]"));
        if(!FortytwoEdit.opticapesOn)
            btnOptiCape.setLabel(Text.of("Show Optifine Capes [Off]"));
        btnOptiCape.setOnClick(() -> {
            FortytwoEdit.opticapesOn = !FortytwoEdit.opticapesOn;
            FortytwoEdit.updateOptions();
            FortytwoEdit.clearCapes();
            if(FortytwoEdit.opticapesOn)
                btnOptiCape.setLabel(Text.of("Show Optifine Capes [On]"));
            else
                btnOptiCape.setLabel(Text.of("Show Optifine Capes [Off]"));
        });
        WButton btnReloadCapes = new WButton(Text.of("Reload Capes"));
        btnReloadCapes.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.sendMessage(Text.of("Cleared "+FortytwoEdit.debugCapeNamesSize()+" cached names."),false);
            client.player.sendMessage(Text.of("Deleted "+FortytwoEdit.debugCapeNames2Size()+" cached capes."),false);
            FortytwoEdit.clearCapes();
        });
        WButton btnEditCape = new WButton(Text.of("Edit Cape"));
        btnEditCape.setOnClick(() -> {
            try {
                //partly from https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/mixin/SkinOptionsScreenMixin.java
                final MinecraftClient client = MinecraftClient.getInstance();
                BigInteger random1Bi = new BigInteger(128, new Random());
                BigInteger random2Bi = new BigInteger(128, new Random(System.identityHashCode(new Object())));
                String serverId = random1Bi.xor(random2Bi).toString(16);
                client.getSessionService().joinServer(client.getSession().getProfile(),client.getSession().getAccessToken(),serverId);
                String url = "https://optifine.net/capeChange?u="+client.getSession().getUuid()+"&n="+client.getSession().getUsername()+"&s="+serverId;
                Util.getOperatingSystem().open(url);
            } catch (Exception ex) {}
        });

        //custom capes
        WLabel lblCustom = new WLabel(Text.of("Custom"));
        WButton btnCustom = new WButton(Text.of("Custom [Off]"));
        if(FortytwoEdit.showClientCape)
            btnCustom.setLabel(Text.of("Custom [On]"));
        btnCustom.setOnClick(() -> {
            FortytwoEdit.showClientCape = !FortytwoEdit.showClientCape;
            FortytwoEdit.updateOptions();
            if(FortytwoEdit.showClientCape)
                btnCustom.setLabel(Text.of("Custom [On]"));
            else
                btnCustom.setLabel(Text.of("Custom [Off]"));
        });
        WTextField txtCustom = new WTextField();
        txtCustom.setMaxLength(64);
        txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
        txtCustom.setEditable(false);
        WButton btnIncCustom = new WButton(Text.of(">"));
        btnIncCustom.setOnClick(() -> {
            FortytwoEdit.clientCape++;
            if(FortytwoEdit.clientCape>=FortytwoEdit.clientCapeList.length)
                FortytwoEdit.clientCape=0;
            txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
            FortytwoEdit.updateOptions();
        });
        WButton btnDecCustom = new WButton(Text.of("<"));
        btnDecCustom.setOnClick(() -> {
            FortytwoEdit.clientCape--;
            if(FortytwoEdit.clientCape<0)
                FortytwoEdit.clientCape=FortytwoEdit.clientCapeList.length-1;
            txtCustom.setText(FortytwoEdit.clientCapeList[FortytwoEdit.clientCape]);
            FortytwoEdit.updateOptions();
        });

        //add items
        root.add(btnBack,5,5,40,20);
        root.add(lblMenu,120,11,0,0);
        root.add(lblOptifine,20,5+7+22*1,0,0);
        root.add(btnOptiCape,20,22*2+1,160,20);
        root.add(btnReloadCapes,20,22*3+1,80,20);
        root.add(btnEditCape,105,22*3+1,60,20);
        root.add(lblCustom,20,5+7+22*4,0,0);
        root.add(btnCustom,20,22*5+1,80,20);
        root.add(btnDecCustom,20,22*6+1,20,20);
        root.add(txtCustom,20+1+20+5,22*6+1,120,22);
        root.add(btnIncCustom,20+20+120+5+5+2,22*6+1,20,20);

    }

}
