package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.widget.SmartButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class MagickScreen extends GenericScreen {

    private SmartButton btnWgtAutoClick;
    private int autoClickCycle = -1;

    public MagickScreen() {
        super(Component.translatable("42edit.gui.magick_screen.title").copy().withColor(0x420666).withStyle(ChatFormatting.BOLD),
            Items.JIGSAW);
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = MagickScreen::new;

        setupScrollPane();
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.item_builder"), btn -> changeScreen(new ItemBuilderScreen()))
                .setRenderItem(Items.SPONGE).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.hacks"), btn -> changeScreen(new HacksScreen()))
                .setRenderItem(Items.REPEATING_COMMAND_BLOCK).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.super_secret"),
                (button, inputWithModifiers) -> this.btnSuperSecretSettings(inputWithModifiers)).fullWidth()
                .setRenderItem(Items.STRUCTURE_BLOCK).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.capes"), btn -> changeScreen(new CapeScreen()))
                .setRenderItem(Items.ELYTRA).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.auto_click"), btn -> changeScreen(new AutoClickScreen()))
                .setRenderItem(Items.GOLDEN_SWORD).build(),
            WIDGET_UTIL.newButton(Component.empty(), (btn, inputs) -> this.btnAutoClick(inputs)).runWithSelf(w -> this.btnWgtAutoClick = w).setTooltip(
                Component.translatable("42edit.gui.magick_screen.auto_click.cycle_tooltip")).build()
        );
        setAutoClickMessage();
        finalizeScrollPane();

    }

    protected void btnSuperSecretSettings(InputWithModifiers inputs) {
        if (inputs.hasShiftDown())
            changeScreen(new SecretScreen());
        else
            FortytwoEdit.cycleSuperSecretSetting();
    }

    protected void btnAutoClick(InputWithModifiers inputs) {
        if (autoClickCycle == -1)
            autoClickCycle = 0;
        else {
            autoClickCycle += (inputs.hasShiftDown() ? -1 : 1);
            if (autoClickCycle < 0)
                autoClickCycle = 4;
            else if (autoClickCycle > 4)
                autoClickCycle = 0;
        }

        switch (autoClickCycle) {
            case 0:
                FortytwoEdit.updateAutoClick(true, false, false, false, FortytwoEdit.attackWait);
                break;
            case 1:
                FortytwoEdit.updateAutoClick(false, true, false, false, FortytwoEdit.attackWait);
                break;
            case 2:
                FortytwoEdit.updateAutoClick(false, false, true, false, 1500);
                break;
            case 3:
                FortytwoEdit.updateAutoClick(false, false, true, false, 650);
                break;
            case 4:
                FortytwoEdit.updateAutoClick(false, false, false, true, FortytwoEdit.attackWait);
                break;
            default: break;
        }

        setAutoClickMessage();
    }

    private void setAutoClickMessage() {
        if (FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack && !FortytwoEdit.autoFish) {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.use"));
            autoClickCycle = 0;
        }
        else if (!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack && !FortytwoEdit.autoFish) {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.mine"));
            autoClickCycle = 1;
        }
        else if (!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && !FortytwoEdit.autoFish && FortytwoEdit.attackWait == 1500) {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_slow"));
            autoClickCycle = 2;
        }
        else if (!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && !FortytwoEdit.autoFish && FortytwoEdit.attackWait == 650) {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_fast"));
            autoClickCycle = 3;
        }
        else if (!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack && FortytwoEdit.autoFish) {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.fish"));
            autoClickCycle = 4;
        }
        else {
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.custom"));
            autoClickCycle = -1;
        }
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return null;
    }

}
