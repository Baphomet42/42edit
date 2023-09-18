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

    private static final int maxLines = 22;
    private static final int large = 420;
    private static final int medium = 285;
    private static final int small = 170;

    @Inject(method="wrapLines(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/text/Text;)Ljava/util/List;", at=@At("RETURN"), cancellable = true)
    private static void modWrapLines(MinecraftClient client, Text text, CallbackInfoReturnable<List<OrderedText>> cir) {
        if(client.currentScreen != null && client.currentScreen instanceof ItemBuilder && client.textRenderer.wrapLines(text, small).size()>7) {
            int largeSafe = large;
            if(client.currentScreen != null)
                largeSafe = Math.min(largeSafe,client.currentScreen.width-20);
            if(client.textRenderer.wrapLines(text, largeSafe).size()>maxLines) {
                List<OrderedText> linesImmutable = client.textRenderer.wrapLines(text, largeSafe);
                List<OrderedText> lines = new ArrayList<>();
                for(OrderedText t : linesImmutable)
                    lines.add(t);
                int i = 0;
                while(lines.size()>maxLines-1) {
                    lines.remove(lines.size()-1);
                    i++;
                }
                List<OrderedText> extra = client.textRenderer.wrapLines(Text.of("..."+i+" more lines..."), largeSafe);
                lines.add(extra.get(0));
                cir.setReturnValue(lines);
            }
            else if(client.textRenderer.wrapLines(text, medium).size()>7)
                cir.setReturnValue(client.textRenderer.wrapLines(text, largeSafe));
            else
                cir.setReturnValue(client.textRenderer.wrapLines(text, medium));
        }
    }
    
}
