package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MagickScreen extends GenericScreen {

    private Button btnWgtAutoClick;

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
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.hat"), btn -> this.btnHat())
                .setRenderItem(Items.DIAMOND_HELMET).creativeOnly(Component.translatable("42edit.gui.magick_screen.hat.tooltip")).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.super_secret"), WID_WIDTH_FULL,
                (button, inputWithModifiers) -> this.btnSuperSecretSettings(inputWithModifiers))
                .setRenderItem(Items.STRUCTURE_BLOCK).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.capes"), btn -> changeScreen(new CapeScreen()))
                .setRenderItem(Items.ELYTRA).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.auto_click"), btn -> changeScreen(new AutoClickScreen()))
                .setRenderItem(Items.GOLDEN_SWORD).build(),
            WIDGET_UTIL.newButton(Component.empty(), btn -> this.btnAutoClick()).runWithSelf(w -> this.btnWgtAutoClick = w).build()
        );
        setAutoClickMessage();

    }

    protected void btnHat() {
        if (BlackMagick.isCreative(minecraft)) {
            ItemStack hand = minecraft.player.getMainHandItem().copy();
            ItemStack head = minecraft.player.getItemBySlot(EquipmentSlot.HEAD).copy();
            BlackMagick.setItemHead(hand);
            BlackMagick.setItemMain(head);
        }
        unsel();
    }

    protected void btnSuperSecretSettings(InputWithModifiers inputWithModifiers) {
        if (inputWithModifiers.hasShiftDown()) {
            changeScreen(new SecretScreen());
        }
        else {
            FortytwoEdit.cycleSuperSecretSetting();
            unsel();
        }
    }

    protected void btnAutoClick() {
        if (BlackMagick.textComponentToStringLiteral(btnWgtAutoClick.getMessage()).equals("[Use]")) {
            FortytwoEdit.updateAutoClick(false,true,false,1500);
        }
        else if (BlackMagick.textComponentToStringLiteral(btnWgtAutoClick.getMessage()).equals("[Attack .65]")) {
            FortytwoEdit.updateAutoClick(true,false,false,1500);
        }
        else if (BlackMagick.textComponentToStringLiteral(btnWgtAutoClick.getMessage()).equals("[Attack 1.5]")) {
            FortytwoEdit.updateAutoClick(false,false,true,650);
        }
        else if (BlackMagick.textComponentToStringLiteral(btnWgtAutoClick.getMessage()).equals("[Mine]")) {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        else {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        reloadScreen();
    }

    private void setAutoClickMessage() {
        if (FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.use"));
        else if (!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.mine"));
        else if (!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 1500)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_slow"));
        else if (!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 650)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_fast"));
        else
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.custom"));
        btnWgtAutoClick.setTooltip(Tooltip.create(Component.translatable("42edit.gui.magick_screen.auto_click.cycle_tooltip")));
    }

    @Override
    protected Identifier getBackgroundTexture() {
        return null;
    }

}
