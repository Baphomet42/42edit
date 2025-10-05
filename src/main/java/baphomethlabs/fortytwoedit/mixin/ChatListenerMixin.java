package baphomethlabs.fortytwoedit.mixin;

import java.time.Instant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {

    @Inject(method = "showMessageToPlayer", at = @At("HEAD"), cancellable = true)
    public void injectShowMessageToPlayer(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        FortytwoEdit.chatIconNew(component, gameProfile);
    }

}
