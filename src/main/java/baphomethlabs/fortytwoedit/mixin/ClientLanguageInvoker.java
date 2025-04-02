package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLanguage.class)
public interface ClientLanguageInvoker {

    @Invoker("<init>")
    public static ClientLanguage invokeClientLanguage(Map<String, String> map, boolean bl) {
        throw new AssertionError();
    }

}
