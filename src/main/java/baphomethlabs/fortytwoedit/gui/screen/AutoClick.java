package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoClick extends GenericScreen {

    protected EditBox txtAttackCooldown;
    protected boolean unsaved = false;

    public AutoClick() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.AUTO_CLICK;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new MagickGui())).bounds(x+5,y+5,40,20).build());
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Use [On]"), Component.literal("Use [Off]")).withInitialValue(FortytwoEdit.autoClick).displayOnlyValue().create(x+20,y+22*2+1,100,20, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick((boolean)trackOutput,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            unsel();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle use key in auto click mode\n\nWhen on: auto click mode will hold the use key down")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Mine [On]"), Component.literal("Mine [Off]")).withInitialValue(FortytwoEdit.autoMine).displayOnlyValue().create(x+20,y+22*3+1,100,20, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,(boolean)trackOutput,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            unsel();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle mine key in auto click mode\n\nWhen on: auto click mode will hold the mine key down")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Attack [On]"), Component.literal("Attack [Off]")).withInitialValue(FortytwoEdit.autoAttack).displayOnlyValue().create(x+20,y+22*4+1,100,20, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,(boolean)trackOutput,FortytwoEdit.attackWait);
            unsel();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle auto attack in auto click mode\n\nWhen on: auto click mode will use the attack key once per cooldown (1500ms default)")));
        this.txtAttackCooldown = new EditBox(this.font,x+20+100+5+1,y+22*5+1,40-2,20,Component.nullToEmpty(""));
        this.txtAttackCooldown.setMaxLength(4);
        this.txtAttackCooldown.setValue(""+FortytwoEdit.attackWait);
        this.txtAttackCooldown.setResponder(this::editTxtAttackCooldown);
        this.addRenderableWidget(this.txtAttackCooldown);
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Lock Screen [On]"), Component.literal("Lock Screen [Off]")).withInitialValue(FortytwoEdit.afkScreenLock).displayOnlyValue().create(x+20,y+22*6+1,100,20, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.readOptions();
            FortytwoEdit.afkScreenLock = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            reloadScreen();
        })).setTooltip(Tooltip.create(Component.nullToEmpty("Toggle screen lock in auto click mode\n\nWhen on: mouse movement will be ignored and FPS will be reduced")));
    }

    protected void editTxtAttackCooldown(String text) {
        unsaved = true;
    }

    protected void setTxtAttackCooldown() {
        if(unsaved) {
            String inp = "";
            if(txtAttackCooldown.getValue() != null)
                inp = txtAttackCooldown.getValue();
            int attackWait = 1500;
            inp = inp.replaceAll("[^0-9]","");
            try {
                attackWait=Integer.parseInt(inp);
            } catch(NumberFormatException ex) {}
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,attackWait);
            unsaved = false;
        }
    }

    protected void saveAll() {
        setTxtAttackCooldown();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Auto Clicker"), this.width / 2, y+11, TEXT_COLOR);
		context.renderFakeItem(new ItemStack(Items.FISHING_ROD),x+20+2,y+44+1+2);
		context.renderFakeItem(new ItemStack(Items.NETHERITE_PICKAXE),x+20+2,y+22*3+1+2);
		context.renderFakeItem(new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+22*4+1+2);
        context.drawString(this.font, Component.nullToEmpty("Attack Cooldown:"), x+20+3,y+7+22*5, LABEL_COLOR);
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        saveAll();
        super.resize(client, width, height);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtAttackCooldown.canConsumeInput();
    }

    @Override
    public void onCloseAction() {
        saveAll();
        super.onCloseAction();
    }

    @Override
    public void tick() {
        if(!txtAttackCooldown.canConsumeInput())
            setTxtAttackCooldown();

        super.tick();
    }

}
