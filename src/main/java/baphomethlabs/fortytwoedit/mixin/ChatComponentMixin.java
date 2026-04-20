package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyVariable(method = "addMessageToDisplayQueue", at = @At("HEAD"), argsOnly = true)
    private static GuiMessage modifyAddMessageToDisplayQueue(GuiMessage guiMessage) {
        GuiMessage modifyMessage = FortytwoEdit.chatIconGet(guiMessage);
        if (OptionsUtil.ModOptions.CHAT_ICONS.getSetting())
            return modifyMessage;
        return guiMessage;
    }

}
