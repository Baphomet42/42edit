package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ItemBuilder extends GenericScreen {
    
    public ItemBuilder() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
    }

    protected void btnBack() {
        client.setScreen(new MagickGui());
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
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

}
