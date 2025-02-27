package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;

@Mixin(Tooltip.class)
public abstract class TooltipMixin {

    private static final int lineSwap = 7;
    private static final int large = 400;
    private static final int medium = 285;
    private static final int small = 170;
    private static final int safeZone = 18;

    @Inject(method = "splitTooltip", at = @At("RETURN"), cancellable = true)
    private static void injectSplitTooltip(Minecraft client, Component text, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if(client.screen != null && client.screen instanceof ItemBuilder && client.font.split(text, small).size()>lineSwap) {

            int largeSafe = Math.min(large,client.screen.width-safeZone);
            int mediumSafe = Math.min(medium,client.screen.width-safeZone);

            if(client.font.split(text, largeSafe).size()>lineSwap) {
                List<FormattedCharSequence> linesImmutable = client.font.split(text, client.screen.width-safeZone);
                List<FormattedCharSequence> lines = Lists.newArrayList();
                int maxLines = Math.max(lineSwap,((client.screen.height-safeZone)/10)-1);//10 pixels per line, -1 line gives space to see hotbar
                for(FormattedCharSequence t : linesImmutable)
                    lines.add(t);
                if(lines.size()>maxLines) {
                    int i = 0;
                    while(lines.size()>maxLines-1) {
                        lines.remove(lines.size()-1);
                        i++;
                    }
                    List<FormattedCharSequence> extra = client.font.split(Component.nullToEmpty("..."+i+" more lines..."), client.screen.width-safeZone);
                    lines.add(extra.get(0));
                }
                cir.setReturnValue(lines);
            }
            else if(client.font.split(text, mediumSafe).size()>lineSwap)
                cir.setReturnValue(client.font.split(text, largeSafe));
            else
                cir.setReturnValue(client.font.split(text, mediumSafe));

        }
    }

}
