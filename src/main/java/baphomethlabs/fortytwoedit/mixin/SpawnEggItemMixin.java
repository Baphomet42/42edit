package baphomethlabs.fortytwoedit.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;

@Mixin (SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    /**
     * See {@link net.minecraft.block.entity.BlockEntityType} and {@link net.minecraft.entity.EntityType} `POTENTIALLY_EXECUTES_COMMANDS`
     */
    @Inject(method="shouldShowOperatorBlockWarnings", at=@At(value = "RETURN"), cancellable = true)
    private void overrideWarningMessage(ItemStack stack, @Nullable PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue().booleanValue()) {
            switch(FortytwoEdit.getItemWarningMode()) {
                case "hide": {
                    cir.setReturnValue(false);
                    break;
                }
                // case "smart": {
                //     NbtComponent data = stack.get(DataComponentTypes.ENTITY_DATA);
                //     if(data != null) {
                //         EntityType<?> entityType = data.getRegistryValueOfId(player.getWorld().getRegistryManager(), RegistryKeys.ENTITY_TYPE);
                //         NbtCompound stackNbt = BlackMagick.itemToNbtStorage(stack);
                //     }
                //     break;
                // }
                default: break;
            }
        }
    }

}
