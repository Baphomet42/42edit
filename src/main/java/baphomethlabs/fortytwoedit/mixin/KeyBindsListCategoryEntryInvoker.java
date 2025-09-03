package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList.CategoryEntry;
import net.minecraft.network.chat.Component;

@Mixin(CategoryEntry.class)
public interface KeyBindsListCategoryEntryInvoker {

    @Invoker("<init>")
    static CategoryEntry invokeConstructor(KeyBindsList keyBindsList, final Component component) {
        throw new AssertionError();
    }
    
}
