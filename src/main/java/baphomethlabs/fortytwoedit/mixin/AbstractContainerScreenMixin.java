package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.SuggestionHelper;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    @Final
    protected T menu;

    @Shadow
    protected Slot hoveredSlot;

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(ItemStack stack);

    @Shadow
    protected abstract boolean showTooltipWithItemInHand(ItemStack stack);

    @Shadow
    protected abstract void slotClicked(final Slot slot, int slotId, final int buttonNum, final ContainerInput containerInput);

    @Shadow
    private Slot getHoveredSlot(double x, double y) {return null;}

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void injectRenderTooltip(GuiGraphics context, int x, int y, CallbackInfo c) {

        if(this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem().copy();
            if(((AbstractContainerMenu)this.menu).getCarried().isEmpty() || this.showTooltipWithItemInHand(stack)) {
                DataComponentMap components = stack.getComponents();
		        TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
                if(components.has(DataComponents.CONTAINER) && tooltipDisplay.shows(DataComponents.CONTAINER)) {
                    ItemContainerContents container = components.get(DataComponents.CONTAINER);
                    int rows = 3;
                    int columns = 9;
                    int[] size = SuggestionHelper.getContainerSize(stack.getItem());
                    if(size[0]>0)
                        rows = size[0];
                    if(size[1]>0)
                        columns = size[1];

                    NonNullList<ItemStack> items = NonNullList.withSize(rows*columns,ItemStack.EMPTY);
                    container.copyInto(items);

                    boolean empty = true;
                    for(ItemStack i: items) {
                        if(!i.isEmpty())
                            empty = false;
                    }

                    if(!empty) {
                        stack.set(DataComponents.CONTAINER,ItemContainerContents.EMPTY);
                        Optional<TooltipComponent> data = Optional.of(new ContainerTooltipData(items,rows,columns));
                        context.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(stack), data, x, y, stack.get(DataComponents.TOOLTIP_STYLE));
                        c.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void injectKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

        if(FortytwoEdit.keySpamClick.matches(keyEvent)) {

            double d = minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            double e = minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
            Slot slot = this.getHoveredSlot(d, e);

            if(slot != null && ((AbstractContainerMenu)this.menu).canTakeItemForPickAll(ItemStack.EMPTY, slot)) {

                if(slot.hasItem()) {
                    ItemStack quickMove = slot.getItem().copy();
                    if (!quickMove.isEmpty()) {
                        for (Slot target : this.menu.slots) {
                            if (target != null
                                && target.mayPickup(this.minecraft.player)
                                && target.hasItem()
                                && target.container == slot.container
                                && AbstractContainerMenu.canItemQuickReplace(target, quickMove, true)) {
                                this.slotClicked(target, target.index, 0, ContainerInput.QUICK_MOVE);
                            }
                        }
                    }
                }

                cir.setReturnValue(true);
            }
        }

    }

}
