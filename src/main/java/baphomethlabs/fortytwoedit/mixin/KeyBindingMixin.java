package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Inject(method="onKeyPressed(Lnet/minecraft/client/util/InputUtil/Key;)V", at=@At("TAIL"))
    private static void detectKeyPress(InputUtil.Key key, CallbackInfo c) {
        if(FortytwoEdit.modKey.isPressed() && !FortytwoEdit.spamClick.isPressed()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(key.equals(KeyBindingHelper.getBoundKeyOf(client.options.attackKey))) {
                KeyBinding.onKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.useKey));
            }
        }
    }

}
