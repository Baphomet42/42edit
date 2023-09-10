package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Mixin(BrewingStandScreen.class)
public abstract class BrewingStandScreenMixin extends HandledScreen<BrewingStandScreenHandler> {

    public BrewingStandScreenMixin(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    
    private static final Identifier TEXTURE_LEFT = new Identifier("the_pinpal","brewing_guide_left");
    private static final Identifier TEXTURE_RIGHT = new Identifier("the_pinpal","brewing_guide_right");
    
    @Inject(method="drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at=@At("TAIL"), cancellable = true)
    private void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo c) {

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawGuiTexture(TEXTURE_LEFT, 96, 104, 0, 0, i-96, j, 96, 104);
        context.drawGuiTexture(TEXTURE_RIGHT, 143, 193, 0, 0, i+this.backgroundWidth, j, 143, 193);

    }

}
