package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow
    private InputUtil.Key boundKey;

    @Inject(method="onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V", at=@At("TAIL"))
    private static void detectKeyPress(InputUtil.Key key, CallbackInfo c) {

        if(FortytwoEdit.modKey.isPressed() && !FortytwoEdit.spamClick.isPressed()) {
            final MinecraftClient client = MinecraftClient.getInstance();

            if(key.equals(((KeyBindingAccessor)client.options.attackKey).getBoundKey())) {
                KeyBinding.onKeyPressed(((KeyBindingAccessor)client.options.useKey).getBoundKey());
                if(FortytwoEdit.randoMode)
                    FortytwoEdit.changeRandoSlot();
                client.options.attackKey.setPressed(false);
            }
            else if(key.equals(((KeyBindingAccessor)client.options.pickItemKey).getBoundKey())) {
                ItemStack item = FortytwoEdit.copyLookAt();
                if(item != null && !item.isEmpty() && client.player.getAbilities().creativeMode) {
                    BlackMagick.setItemMain(item);
                    while(client.options.pickItemKey.wasPressed()) {}
                    client.options.pickItemKey.setPressed(false);
                }
            }

        }
        else if(FortytwoEdit.autoClicker && !FortytwoEdit.suppressKeybind) {
            final MinecraftClient client = MinecraftClient.getInstance();
            boolean stopAutoClicker = false;

            if((FortytwoEdit.autoMine || FortytwoEdit.autoAttack) && key.equals(((KeyBindingAccessor)client.options.attackKey).getBoundKey()))
                stopAutoClicker = true;
            else if(FortytwoEdit.autoClick && key.equals(((KeyBindingAccessor)client.options.useKey).getBoundKey()))
                stopAutoClicker = true;
            
            if(stopAutoClicker) {
                FortytwoEdit.autoClicker = false;
                client.options.useKey.setPressed(false);
                client.options.attackKey.setPressed(false);
                while(client.options.useKey.wasPressed()) {}
                while(client.options.attackKey.wasPressed()) {}
            }

        }

    }

}
