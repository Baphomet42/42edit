package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyVariable(method = "addMessageToDisplayQueue", at = @At("HEAD"), argsOnly = true)
    private static GuiMessage modifyAddMessageToDisplayQueue(GuiMessage guiMessage) {
        if(FortytwoEdit.mixinChatProfileIcon) {
            return FortytwoEdit.chatIconGet(guiMessage);
        }
        return guiMessage;
    }

}
