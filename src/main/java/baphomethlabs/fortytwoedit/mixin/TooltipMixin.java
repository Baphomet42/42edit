package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;

@Mixin(Tooltip.class)
public abstract class TooltipMixin {

    private static final int lineSwap = 7;
    private static final int large = 400;
    private static final int medium = 285;
    private static final int small = 170;
    private static final int safeZone = 18;

    @Shadow
    @Final
    protected Component message;

    @Inject(method = "toCharSequence", at = @At("RETURN"), cancellable = true)
    private void injectToCharSequence(Minecraft client, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if (client.gui != null && client.gui.screen() != null && client.gui.screen() instanceof GenericScreen) {
            List<FormattedCharSequence> cacheTooltip = GenericScreen.setCurrentTooltip(message);
            if (cacheTooltip != null)
                cir.setReturnValue(cacheTooltip);
            else if (client.font.split(message, small).size()>lineSwap) {

                int largeSafe = Math.min(large, client.gui.screen().width - safeZone);
                int mediumSafe = Math.min(medium, client.gui.screen().width - safeZone);

                if (client.font.split(message, largeSafe).size()>lineSwap) {
                    List<FormattedCharSequence> linesImmutable = client.font.split(message, client.gui.screen().width - safeZone);
                    List<FormattedCharSequence> lines = Lists.newArrayList();
                    int maxLines = Math.max(lineSwap, ((client.gui.screen().height - safeZone) / 10) - 1); //10 pixels per line, -1 line gives space to see hotbar
                    for (FormattedCharSequence t : linesImmutable)
                        lines.add(t);
                    if (lines.size()>maxLines) {
                        int originalLines = lines.size();
                        int startLine = 1;
                        int endLine = lines.size();

                        for (int i = 0; i < GenericScreen.getCurrentTooltipScroll(); i++)
                            if (lines.size() > maxLines - 1) {
                                lines.remove(0);
                                startLine++;
                            }
                            else
                                break;
                        GenericScreen.setCurrentTooltipScroll(startLine - 1);
                        while (lines.size() > maxLines - 1) {
                            lines.remove(lines.size() - 1);
                            endLine--;
                        }
                        List<FormattedCharSequence> extra = client.font.split(Component.empty()
                            .append("[Showing lines " + startLine + "-" + endLine + " of " + originalLines + "]")
                            .append("  ")
                            .append(Component.empty().append("Use Ctrl+PGU/PGD to cycle").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
                            client.gui.screen().width - safeZone);
                        lines.add(extra.get(0));
                    }
                    cir.setReturnValue(lines);
                }
                else if (client.font.split(message, mediumSafe).size()>lineSwap)
                    cir.setReturnValue(client.font.split(message, largeSafe));
                else
                    cir.setReturnValue(client.font.split(message, mediumSafe));
            }

        }
    }

}
