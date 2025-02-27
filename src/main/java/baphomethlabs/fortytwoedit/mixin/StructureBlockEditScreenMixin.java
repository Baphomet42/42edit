package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.ComponentHelper;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

@Mixin(StructureBlockEditScreen.class)
public abstract class StructureBlockEditScreenMixin extends Screen {

    public StructureBlockEditScreenMixin() {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
    }

    private TextSuggestor suggs = null;

    @Shadow
    private EditBox nameEdit;

    @Shadow
    private Button loadButton;

    @Inject(method = "init", at = @At("TAIL"), cancellable = true)
    private void injectInit(CallbackInfo c) {
        nameEdit.setResponder(value -> {

            if(loadButton.visible) {
                //ComponentHelper.clearDynamicListCaches(); uncomment if list made dynamic

                if(suggs != null)
                    suggs.refresh();
                else {
                    suggs = new TextSuggestor(minecraft, nameEdit, font);
                    suggs.setSuggestions(ComponentHelper.DATA_STRUCTURE.getArray());
                }

            }
            else if(suggs != null)
                suggs = null;

        });
    }

    @Inject(method = "render", at = @At("TAIL"), cancellable = true)
    private void injectRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo c) {
        if(suggs != null)
            suggs.render(context, mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void injectKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if(suggs != null && suggs.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "resize", at = @At("RETURN"), cancellable = true)
    private void injectResize(Minecraft client, int width, int height, CallbackInfo ci) {
        suggs = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(suggs != null && suggs.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(suggs != null && suggs.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        suggs = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
