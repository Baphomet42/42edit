package baphomethlabs.fortytwoedit.gui;

import org.lwjgl.glfw.GLFW;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class AutoClick extends GenericScreen {

    protected TextFieldWidget txtAttackCooldown;
    protected boolean unsaved = false;
    
    public AutoClick() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Use [On]"), Text.literal("Use [Off]")).initially(FortytwoEdit.autoClick).omitKeyText().build(x+20,y+44+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick((boolean)trackOutput,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            this.resize(this.client,this.width,this.height);
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Mine [On]"), Text.literal("Mine [Off]")).initially(FortytwoEdit.autoMine).omitKeyText().build(x+20,y+22*3+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,(boolean)trackOutput,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            this.resize(this.client,this.width,this.height);
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Attack [On]"), Text.literal("Attack [Off]")).initially(FortytwoEdit.autoAttack).omitKeyText().build(x+20,y+22*4+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,(boolean)trackOutput,FortytwoEdit.attackWait);
            this.resize(this.client,this.width,this.height);
        }));
        this.txtAttackCooldown = new TextFieldWidget(this.textRenderer,x+20+100+5+1,y+22*6+1,40-2,20,Text.of(""));
        this.txtAttackCooldown.setMaxLength(4);
        this.txtAttackCooldown.setText(""+FortytwoEdit.attackWait);
        this.txtAttackCooldown.setChangedListener(this::editTxtAttackCooldown);
        this.addDrawableChild(this.txtAttackCooldown);
    }

    protected void btnBack() {
        saveAll();
        client.setScreen(new MagickGui());
    }

    protected void editTxtAttackCooldown(String text) {
        unsaved = true;
    }

    protected void setTxtAttackCooldown() {
        if(unsaved) {
            String inp = "";
            if(txtAttackCooldown.getText() != null)
                inp = txtAttackCooldown.getText();
            int attackWait = 1500;
            inp = inp.replaceAll("[^0-9]","");
            try {
                attackWait=Integer.parseInt(inp);
            } catch(NumberFormatException ex) {}
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,attackWait);
            unsaved = false;
            this.resize(this.client,this.width,this.height);
        }
    }

    protected void saveAll() {
        setTxtAttackCooldown();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.drawBackground(context, delta, mouseX, mouseY);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Auto Clicker"), this.width / 2, y+11, 0xFFFFFF);
		context.drawItem(new ItemStack(Items.FISHING_ROD),x+20+2,y+44+1+2);
		context.drawItem(new ItemStack(Items.NETHERITE_PICKAXE),x+20+2,y+22*3+1+2);
		context.drawItem(new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+22*4+1+2);
        context.drawTextWithShadow(this.textRenderer, Text.of("Attack Cooldown:"), x+20+3,y+7+22*6, LABEL_COLOR);
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        saveAll();
        this.init(client, width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            saveAll();
        }
        if (keyCode == KeyBindingHelper.getBoundKeyOf(FortytwoEdit.magickGuiKey).getCode() || keyCode == KeyBindingHelper.getBoundKeyOf(client.options.inventoryKey).getCode()) {
            if(!txtAttackCooldown.isActive()) {
                saveAll();
                this.client.setScreen(null);
                return true;
            }
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if(!txtAttackCooldown.isActive())
            setTxtAttackCooldown();
    }

}
