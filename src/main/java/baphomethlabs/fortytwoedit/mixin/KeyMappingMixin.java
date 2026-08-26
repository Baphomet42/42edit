package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Inject(method = "click", at = @At("TAIL"))
    private static void detectKeyPress(InputConstants.Key key, CallbackInfo c) {

        if (OptionsUtil.Keybinds.KEY_KEY_MOD.isDown() && !OptionsUtil.Keybinds.KEY_SPAM_CLICK.isDown()) {
            final Minecraft client = Minecraft.getInstance();

            if (key.equals(((KeyMappingAccessor)client.options.keyAttack).getBoundKey())) {
                KeyMapping.click(((KeyMappingAccessor)client.options.keyUse).getBoundKey());
                client.options.keyAttack.setDown(false);
            }
            else if (key.equals(((KeyMappingAccessor)client.options.keyPickItem).getBoundKey())) {
                ItemStack item = FortytwoEdit.copyLookAt();
                if (item != null && !item.isEmpty() && BlackMagick.isCreative(client)) {
                    BlackMagick.setItemMain(item);
                    while (client.options.keyPickItem.consumeClick()) {}
                    client.options.keyPickItem.setDown(false);
                }
            }

        }
        else if (FortytwoEdit.autoClicker && !FortytwoEdit.suppressKeybind) {
            final Minecraft client = Minecraft.getInstance();
            boolean stopAutoClicker = false;

            if ((FortytwoEdit.autoMine || FortytwoEdit.autoAttack) && key.equals(((KeyMappingAccessor)client.options.keyAttack).getBoundKey()))
                stopAutoClicker = true;
            else if ((FortytwoEdit.autoUse && !FortytwoEdit.autoFish) && key.equals(((KeyMappingAccessor)client.options.keyUse).getBoundKey()))
                stopAutoClicker = true;

            if (stopAutoClicker) {
                FortytwoEdit.autoClicker = false;
                client.options.keyUse.setDown(false);
                client.options.keyAttack.setDown(false);
                while (client.options.keyUse.consumeClick()) {}
                while (client.options.keyAttack.consumeClick()) {}
            }

        }

    }

}
