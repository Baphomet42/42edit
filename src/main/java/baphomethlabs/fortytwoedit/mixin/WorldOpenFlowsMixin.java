package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import baphomethlabs.fortytwoedit.OptionsUtil;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.WorldData;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Redirect(method = "openWorldCheckWorldStemCompatibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettingsLifecycle()Lcom/mojang/serialization/Lifecycle;"))
    private Lifecycle redirectWorldGenSettingsLifecycle(WorldData data) {
        if (OptionsUtil.ModOptions.SKIP_EXPERIMENTAL_WARNING.getSetting()) {
            return Lifecycle.stable();
        }
        return data.worldGenSettingsLifecycle();
    }

}
