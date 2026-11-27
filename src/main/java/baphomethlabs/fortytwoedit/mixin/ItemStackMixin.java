package baphomethlabs.fortytwoedit.mixin;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.SuggestionHelper;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltipImage()Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void injectGetTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (OptionsUtil.ModOptions.CONTAINER_TOOLTIP.getSetting() && cir.getReturnValue().isEmpty()) {
            ItemStack stack = (ItemStack)(Object)this;
            TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            if (stack.has(DataComponents.CONTAINER) && tooltipDisplay.shows(DataComponents.CONTAINER)) {
                ItemContainerContents container = stack.get(DataComponents.CONTAINER);
                int rows = 3;
                int columns = 9;
                int[] size = SuggestionHelper.getContainerSize(stack.getItem());
                if (size[0] > 0)
                    rows = size[0];
                if (size[1] > 0)
                    columns = size[1];

                NonNullList<ItemStack> items = NonNullList.withSize(rows * columns, ItemStack.EMPTY);
                container.copyInto(items);

                boolean empty = true;
                for (ItemStack i: items) {
                    if (!i.isEmpty())
                        empty = false;
                }

                if (!empty) {
                    Optional<TooltipComponent> data = Optional.of(new ContainerTooltipData(items, rows, columns));
                    cir.setReturnValue(data);
                }
            }
        }
    }

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void injectAddToTooltip(final DataComponentType<?> type, final TooltipProvider.Getter<?> tooltipGetter, final Item.TooltipContext context, final TooltipDisplay display, final Consumer<Component> consumer, final TooltipFlag flag, CallbackInfo c) {
        if (OptionsUtil.ModOptions.CONTAINER_TOOLTIP.getSetting() && ((ItemStack)(Object)this).get(type) instanceof ItemContainerContents) {
            Optional<TooltipComponent> tt = ((ItemStack)(Object)this).getTooltipImage();
            if (tt.isPresent() && tt.get() instanceof ContainerTooltipData) {
                c.cancel();
            }
        }
    }

}
