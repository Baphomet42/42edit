package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.OptionsUtil;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    private SlotWidget getHoveredSlot() {return null;}

    @Shadow
    private boolean handleDoublePress(final @Nullable SlotWidget slotWidget, final boolean isDoublePress, final boolean hasShiftDown, final int button) {return false;}

    @Shadow
    private void setQuickCrafting(final boolean isQuickCrafting) {}

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void injectKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {

        if (OptionsUtil.Keybinds.KEY_SPAM_CLICK.matches(event)) {

            SlotWidget slotWidget = this.getHoveredSlot();

            if (this.handleDoublePress(slotWidget, true, true, 0)) {
                this.setQuickCrafting(false);
                cir.setReturnValue(true);
            }

        }

    }

}
