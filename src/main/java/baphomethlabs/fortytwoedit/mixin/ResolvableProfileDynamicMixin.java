package baphomethlabs.fortytwoedit.mixin;

import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.OptionsUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;

@Mixin(ResolvableProfile.Dynamic.class)
public abstract class ResolvableProfileDynamicMixin {

    @Shadow
    @Final
    protected static Component DYNAMIC_TOOLTIP;

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    public void injectAddToTooltip(
        Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter, CallbackInfo ci
    ) {
        if (OptionsUtil.ModOptions.DYNAMIC_PROFILE_TOOLTIP_INFO.getSetting()) {
            String dynamicName = BlackMagick.getDynamicUUIDProfileName((ResolvableProfile.Dynamic)(Object)this);
            if (dynamicName != null && !dynamicName.isEmpty()) {
                consumer.accept(Component.empty().append(DYNAMIC_TOOLTIP).append(Component.empty().append(" UUID ("+dynamicName+")").setStyle(DYNAMIC_TOOLTIP.getStyle())));
                ci.cancel();
            }
            else {
                dynamicName = BlackMagick.getDynamicProfileName((ResolvableProfile.Dynamic)(Object)this);
                if (dynamicName != null && !dynamicName.isEmpty()) {
                    consumer.accept(Component.empty().append(DYNAMIC_TOOLTIP).append(Component.empty().append(" name ("+dynamicName+")").setStyle(DYNAMIC_TOOLTIP.getStyle())));
                    ci.cancel();
                }
            }
        }
    }

}
