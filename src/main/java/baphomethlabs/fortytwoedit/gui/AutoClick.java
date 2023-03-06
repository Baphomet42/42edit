package baphomethlabs.fortytwoedit.gui;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class AutoClick extends GenericScreen {

    protected TextFieldWidget txtAttackCooldown;
    
    public AutoClick() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Use [On]"), Text.literal("Use [Off]")).initially(FortytwoEdit.autoClick).omitKeyText().build(x+20,y+44+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick((boolean)trackOutput,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Mine [On]"), Text.literal("Mine [Off]")).initially(FortytwoEdit.autoMine).omitKeyText().build(x+20,y+22*3+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,(boolean)trackOutput,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
        }));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Attack [On]"), Text.literal("Attack [Off]")).initially(FortytwoEdit.autoAttack).omitKeyText().build(x+20,y+22*4+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,(boolean)trackOutput,FortytwoEdit.attackWait);
        }));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Attack Cooldown"), button -> this.btnAttackCooldown()).dimensions(x+20,y+22*6+1,100,20).build());
        this.txtAttackCooldown = new TextFieldWidget(this.textRenderer,x+20+100+5,y+22*6+1,40,20,Text.of(""));
        this.txtAttackCooldown.setMaxLength(4);
        this.txtAttackCooldown.setText(""+FortytwoEdit.attackWait);
        this.addSelectableChild(this.txtAttackCooldown);
    }

    protected void btnBack() {
        MinecraftClient.getInstance().setScreen(new MagickGui());
    }

    protected void btnAttackCooldown() {
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
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        MagickGui.drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of("Auto Clicker"), this.width / 2, y+11, 0xFFFFFF);
        this.txtAttackCooldown.render(matrices, mouseX, mouseY, delta);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.FISHING_ROD),x+20+2,y+44+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.NETHERITE_PICKAXE),x+20+2,y+22*3+1+2);
		this.itemRenderer.renderInGui(matrices, new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+22*4+1+2);
        super.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String txt = this.txtAttackCooldown.getText();
        this.init(client, width, height);
        this.txtAttackCooldown.setText(txt);
    }

}
