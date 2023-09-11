package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.TextSuggestor;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Mixin(StructureBlockScreen.class)
public abstract class StructureBlockScreenMixin extends Screen {

    public StructureBlockScreenMixin() {
        super(Text.translatable(Blocks.STRUCTURE_BLOCK.getTranslationKey()));
    }

    private TextSuggestor suggs = null;

    @Shadow
    private TextFieldWidget inputName;

    @Shadow
    private ButtonWidget buttonLoad;
    
    @Inject(method="init()V", at=@At("TAIL"), cancellable = true)
    private void init(CallbackInfo c) {
        inputName.setChangedListener(value -> {
            
            if(buttonLoad.visible) {

                if(suggs != null)
                    suggs.refresh();
                else {
                    suggs = new TextSuggestor(client, inputName, textRenderer);
                    suggs.setSuggestions(FortytwoEdit.STRUCTURES);
                }

            }
            else if(suggs != null)
                suggs = null;

        });
    }
    
    @Inject(method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at=@At("TAIL"), cancellable = true)
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo c) {
        if(suggs != null)
            suggs.render(context, mouseX, mouseY);
    }
    
    @Inject(method="keyPressed(III)Z", at=@At("HEAD"), cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (suggs != null && suggs.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        suggs = null;
        super.resize(client, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (suggs != null && suggs.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (suggs != null && suggs.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        suggs = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
