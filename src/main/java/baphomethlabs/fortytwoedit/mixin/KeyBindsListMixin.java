package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin extends ContainerObjectSelectionList<KeyBindsList.Entry> {

    @Shadow
	private int maxNameWidth;

	protected KeyBindsListMixin(KeyBindsScreen keyBindsScreen, Minecraft minecraft) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), 20);
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void appendConstructor(KeyBindsScreen keyBindsScreen, Minecraft minecraft, CallbackInfo ci) {

        KeyBindsList self = (KeyBindsList)(Object)this;
        this.addEntry(self.new CategoryEntry(FortytwoEdit.KEY_CATEGORY));

        for(KeyMapping keyMapping : FortytwoEdit.KEYBINDS) {
			Component component = Component.translatable(keyMapping.getName());
            int i = minecraft.font.width(component);
            if (i > this.maxNameWidth) {
                this.maxNameWidth = i;
            }
            this.addEntry(KeyBindsListKeyEntryInvoker.invokeConstructor((KeyBindsList)(Object)this, keyMapping, component));
        }

    }
    
}
