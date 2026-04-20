package baphomethlabs.fortytwoedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList.KeyEntry;
import net.minecraft.network.chat.Component;

@Mixin(KeyEntry.class)
public interface KeyBindsListKeyEntryInvoker {

    @Invoker("<init>")
    static KeyEntry invokeConstructor(KeyBindsList keyBindsList, final KeyMapping keyMapping, final Component component) {
        throw new AssertionError();
    }

}
