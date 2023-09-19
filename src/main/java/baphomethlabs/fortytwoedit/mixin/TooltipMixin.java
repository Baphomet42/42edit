package baphomethlabs.fortytwoedit.mixin;

import java.util.ArrayList;
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

    private static final int lineSwap = 7;
    private static final int large = 400;
    private static final int medium = 285;
    private static final int small = 170;
    private static final int safeZone = 18;

    @Inject(method="wrapLines(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/text/Text;)Ljava/util/List;", at=@At("RETURN"), cancellable = true)
    private static void modWrapLines(MinecraftClient client, Text text, CallbackInfoReturnable<List<OrderedText>> cir) {
        if(client.currentScreen != null && client.currentScreen instanceof ItemBuilder && client.textRenderer.wrapLines(text, small).size()>lineSwap) {

            int largeSafe = Math.min(large,client.currentScreen.width-safeZone);
            int mediumSafe = Math.min(medium,client.currentScreen.width-safeZone);

            if(client.textRenderer.wrapLines(text, largeSafe).size()>lineSwap) {
                List<OrderedText> linesImmutable = client.textRenderer.wrapLines(text, client.currentScreen.width-safeZone);
                List<OrderedText> lines = new ArrayList<>();
                int maxLines = Math.max(lineSwap,(client.currentScreen.height-safeZone)/10);
                for(OrderedText t : linesImmutable)
                    lines.add(t);
                if(lines.size()>maxLines) {
                    int i = 0;
                    while(lines.size()>maxLines-1) {
                        lines.remove(lines.size()-1);
                        i++;
                    }
                    List<OrderedText> extra = client.textRenderer.wrapLines(Text.of("..."+i+" more lines..."), client.currentScreen.width-safeZone);
                    lines.add(extra.get(0));
                }
                cir.setReturnValue(lines);
            }
            else if(client.textRenderer.wrapLines(text, mediumSafe).size()>lineSwap)
                cir.setReturnValue(client.textRenderer.wrapLines(text, largeSafe));
            else
                cir.setReturnValue(client.textRenderer.wrapLines(text, mediumSafe));

        }
    }
    
}
