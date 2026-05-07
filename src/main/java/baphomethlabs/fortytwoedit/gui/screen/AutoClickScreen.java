package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;
import net.minecraft.world.item.Items;

public class AutoClickScreen extends GenericScreen {

    protected SmartEditBox txtAttackCooldown;
    protected boolean unsaved = false;

    public AutoClickScreen() {
        super("Auto Clicker");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = AutoClickScreen::new;
        this.addBackButton();

        setupScrollPane();
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Use", btn -> {
                    FortytwoEdit.updateAutoClick(!FortytwoEdit.autoClick, FortytwoEdit.autoMine, FortytwoEdit.autoAttack, FortytwoEdit.autoFish, FortytwoEdit.attackWait);
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.autoClick).setRenderItem(Items.GOLDEN_APPLE).setTooltip(
                "Toggle use key in auto click mode\n\nWhen on: auto click mode will hold the use key down").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Mine", btn -> {
                    FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick, !FortytwoEdit.autoMine, FortytwoEdit.autoAttack, FortytwoEdit.autoFish, FortytwoEdit.attackWait);
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.autoMine).setRenderItem(Items.NETHERITE_PICKAXE).setTooltip(
                "Toggle mine key in auto click mode\n\nWhen on: auto click mode will hold the mine key down").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Attack", btn -> {
                    FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick, FortytwoEdit.autoMine, !FortytwoEdit.autoAttack, FortytwoEdit.autoFish, FortytwoEdit.attackWait);
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.autoAttack).setRenderItem(Items.GOLDEN_SWORD).setTooltip(
                "Toggle auto attack in auto click mode\n\nWhen on: auto click mode will use the attack key based on the specified timer").build(),
            WIDGET_UTIL.newEditBox().setMaxLength(4).setValue("" + FortytwoEdit.attackWait).setResponder(this::editTxtAttackCooldown)
                .runWithSelf(w -> this.txtAttackCooldown = w).setSmartTooltip(
                "Number of milliseconds between attacks during auto click mode (defaults to 1500)").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Fish", btn -> {
                    FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick, FortytwoEdit.autoMine, FortytwoEdit.autoAttack, !FortytwoEdit.autoFish, FortytwoEdit.attackWait);
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.autoFish).setRenderItem(Items.FISHING_ROD).setTooltip(
                "Toggle auto fish in auto click mode\n\nWhen on: auto click mode will attempt to reel in fish then cast again\n\nRequires closed captions to detect fish").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Lock Screen", btn -> {
                    OptionsUtil.ModOptions.AUTO_CLICK_LOCK.toggleSetting();
                    rebuildWidgets();
                }).setBoolName(OptionsUtil.ModOptions.AUTO_CLICK_LOCK.getSetting()).setTooltip(OptionsUtil.ModOptions.AUTO_CLICK_LOCK.getButtonTooltip()).build()
        );
        finalizeScrollPane();
    }

    protected void editTxtAttackCooldown(String text) {
        unsaved = true;
    }

    protected void setTxtAttackCooldown() {
        if (unsaved) {
            String inp = "";
            if (txtAttackCooldown.getValue() != null)
                inp = txtAttackCooldown.getValue();
            String originalInp = inp;
            int attackWait = 1500;
            inp = inp.replaceAll("[^0-9]", "");
            try {
                attackWait = Integer.parseInt(inp);
            } catch (NumberFormatException ex) {}

            if (attackWait < 1)
                attackWait = 1;
            else if (attackWait > 9999)
                attackWait = 9999;

            FortytwoEdit.updateAutoClick(FortytwoEdit.autoClick, FortytwoEdit.autoMine, FortytwoEdit.autoAttack, FortytwoEdit.autoFish, attackWait);
            if (!originalInp.equals("" + attackWait))
                txtAttackCooldown.setValue("" + attackWait);
            unsaved = false;
        }
    }

    protected void saveAll() {
        setTxtAttackCooldown();
    }

    @Override
    public void rebuildWidgets() {
        saveAll();
        super.rebuildWidgets();
    }

    @Override
    public void onCloseAction() {
        saveAll();
        super.onCloseAction();
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtAttackCooldown.canConsumeInput();
    }

    @Override
    public void tick() {
        if (!txtAttackCooldown.canConsumeInput())
            setTxtAttackCooldown();

        super.tick();
    }

}
