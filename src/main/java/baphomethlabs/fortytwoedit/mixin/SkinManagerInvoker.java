package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;

@Mixin(SkinManager.class)
public interface SkinManagerInvoker {

    @Invoker("registerTextures")
    public CompletableFuture<PlayerSkin> invokeRegisterTextures(UUID uUID, MinecraftProfileTextures minecraftProfileTextures);

}
