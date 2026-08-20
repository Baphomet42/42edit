package baphomethlabs.fortytwoedit.mixin;

import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLanguage.class)
public interface ClientLanguageAccessor {

    @Accessor("storage")
    Map<String, String> getTranslations();

}
