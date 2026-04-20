package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoClickScreen extends GenericScreen {

    protected SmartEditBox txtAttackCooldown;
    protected boolean unsaved = false;

    public AutoClickScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = AutoClickScreen::new;
        this.addBackButton();

        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Use [On]"), Component.literal("Use [Off]"), FortytwoEdit.autoClick).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle use key in auto click mode\n\nWhen on: auto click mode will hold the use key down"))).create(x+20,y+ROW_HEIGHT*2+1,100,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick((boolean)trackOutput,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            unsel();
        }));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Mine [On]"), Component.literal("Mine [Off]"), FortytwoEdit.autoMine).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle mine key in auto click mode\n\nWhen on: auto click mode will hold the mine key down"))).create(x+20,y+ROW_HEIGHT*3+1,100,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,(boolean)trackOutput,FortytwoEdit.autoAttack,FortytwoEdit.attackWait);
            unsel();
        }));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Attack [On]"), Component.literal("Attack [Off]"), FortytwoEdit.autoAttack).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle auto attack in auto click mode\n\nWhen on: auto click mode will use the attack key once per cooldown (1500ms default)"))).create(x+20,y+ROW_HEIGHT*4+1,100,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,(boolean)trackOutput,FortytwoEdit.attackWait);
            unsel();
        }));
        this.txtAttackCooldown = new SmartEditBox(this.font,x+WID_SPACE+20+100+1,y+ROW_HEIGHT*5+1,40-2,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtAttackCooldown.setMaxLength(4);
        this.txtAttackCooldown.setValue(""+FortytwoEdit.attackWait);
        this.txtAttackCooldown.setResponder(this::editTxtAttackCooldown);
        this.addRenderableWidget(this.txtAttackCooldown);
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Lock Screen [On]"),
                Component.literal("Lock Screen [Off]"),
                OptionsUtil.ModOptions.AFK_SCREEN_LOCK.getSetting()).displayOnlyValue()
                .withTooltip(val -> Tooltip.create(Component.nullToEmpty(
                "Toggle screen lock in auto click mode\n\nWhen on: mouse movement will be ignored and FPS will be reduced")))
                .create(x+20,y+ROW_HEIGHT*6+1,100,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {

            OptionsUtil.ModOptions.AFK_SCREEN_LOCK.setSetting((boolean)trackOutput);
            reloadScreen();
        }));
    }

    protected void editTxtAttackCooldown(String text) {
        unsaved = true;
    }

    protected void setTxtAttackCooldown() {
        if (unsaved) {
            String inp = "";
            if (txtAttackCooldown.getValue() != null)
                inp = txtAttackCooldown.getValue();
            int attackWait = 1500;
            inp = inp.replaceAll("[^0-9]","");
            try {
                attackWait=Integer.parseInt(inp);
            } catch (NumberFormatException ex) {}
            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick,FortytwoEdit.autoMine,FortytwoEdit.autoAttack,attackWait);
            unsaved = false;
        }
    }

    protected void saveAll() {
        setTxtAttackCooldown();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, Component.nullToEmpty("Auto Clicker"), this.width / 2, y+11, TEXT_COLOR);
		context.fakeItem(new ItemStack(Items.FISHING_ROD),x+20+2,y+44+1+2);
		context.fakeItem(new ItemStack(Items.NETHERITE_PICKAXE),x+20+2,y+ROW_HEIGHT*3+1+2);
		context.fakeItem(new ItemStack(Items.GOLDEN_SWORD),x+20+2,y+ROW_HEIGHT*4+1+2);
        context.text(this.font, Component.nullToEmpty("Attack Cooldown:"), x+20+3,y+7+ROW_HEIGHT*5, LABEL_COLOR);
    }

    @Override
    public void resize(int width, int height) {
        saveAll();
        super.resize(width, height);
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
        if (!txtAttackCooldown.canConsumeInput())
            setTxtAttackCooldown();

        super.tick();
    }

}
