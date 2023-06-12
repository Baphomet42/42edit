package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

@Mixin(Tooltip.class)
public abstract class TooltipMixin {

    @Inject(method="wrapLines(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/text/Text;)Ljava/util/List;", at=@At("RETURN"), cancellable = true)
    private static void modWrapLines(MinecraftClient client, Text text, CallbackInfoReturnable<List<OrderedText>> cir) {
        if(client.currentScreen != null && client.currentScreen instanceof ItemBuilder && client.textRenderer.wrapLines(text, 170).size()>7) {
            if(client.textRenderer.wrapLines(text, 285).size()>7)
                cir.setReturnValue(client.textRenderer.wrapLines(text, 400));
            else
                cir.setReturnValue(client.textRenderer.wrapLines(text, 285));
        }
    }
    
}
