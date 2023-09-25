package baphomethlabs.fortytwoedit.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(TranslationStorage.class)
public interface TranslationStorageAccessor {

	@Accessor("translations")
	Map<String, String> getTranslations();

}
