package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    @Final
    protected T handler;

    @Shadow
    protected Slot focusedSlot;

    @Shadow
    protected abstract List<Text> getTooltipFromItem(ItemStack stack);

    @Shadow
    protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);
    
    @Shadow
    private Slot getSlotAt(double x, double y) {return null;}
    
    @Inject(method="drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V", at=@At("HEAD"), cancellable = true)
    private void drawContainerTooltip(DrawContext context, int x, int y, CallbackInfo c) {

        if (((ScreenHandler)this.handler).getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            ItemStack stack = this.focusedSlot.getStack().copy();
            ComponentMap components = stack.getComponents();
            if(components.contains(DataComponentTypes.CONTAINER)) {
                ContainerComponent container = components.get(DataComponentTypes.CONTAINER);
                int rows = 3;
                int columns = 9;
                int[] size = BlackMagick.containerSize(stack.getItem());
                if(size[0]>0)
                    rows = size[0];
                if(size[1]>0)
                    columns = size[1];

                DefaultedList<ItemStack> items = DefaultedList.ofSize(rows*columns,ItemStack.EMPTY);
                container.copyTo(items);

                boolean empty = true;
                for(ItemStack i: items) {
                    if(!i.isEmpty())
                        empty = false;
                }

                if(!empty) {
                    stack.set(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
                    Optional<TooltipData> data = Optional.of(new ContainerTooltipData(items,rows,columns));
                    context.drawTooltip(this.textRenderer, this.getTooltipFromItem(stack), data, x, y);
                    c.cancel();
                }
            }
        }
    }

    @Inject(method="keyPressed(III)Z", at=@At("HEAD"), cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {

        if(FortytwoEdit.spamClick.matchesKey(keyCode,scanCode)) {

            double d = client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
            double e = client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
            Slot slot = this.getSlotAt(d, e);

            if (slot != null && ((ScreenHandler)this.handler).canInsertIntoSlot(ItemStack.EMPTY, slot)) {
                
                if(slot.hasStack()) {
                    ItemStack stack = slot.getStack().copy();
                    for (Slot slot2 : ((ScreenHandler)this.handler).slots) {
                        if (slot2 == null || !slot2.canTakeItems(this.client.player) || !slot2.hasStack() || slot2.inventory != slot.inventory || !ScreenHandler.canInsertItemIntoSlot(slot2, stack, true)) continue;
                        this.onMouseClick(slot2, slot2.id, 0, SlotActionType.QUICK_MOVE);
                    }
                }
                else {
                    for (Slot slot2 : ((ScreenHandler)this.handler).slots) {
                        if (slot2 == null || !slot2.canTakeItems(this.client.player) || !slot2.hasStack() || slot2.inventory != slot.inventory) continue;
                        this.onMouseClick(slot2, slot2.id, 0, SlotActionType.QUICK_MOVE);
                    }
                }

                cir.setReturnValue(true);
            }
        }

    }

}
