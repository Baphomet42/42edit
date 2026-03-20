package baphomethlabs.fortytwoedit.mixin;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.Maps;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {

    @Inject(method = "loadFrom", at = @At(value = "RETURN"), cancellable = true)
    private static void injectLoadFrom(ResourceManager resourceManager, List<String> list, boolean bl, CallbackInfoReturnable<ClientLanguage> cir) {
        ClientLanguage clientLanguage = cir.getReturnValue();
        Map<String,String> newTranslations = Maps.newHashMap();
        newTranslations.putAll(((ClientLanguageAccessor)clientLanguage).getTranslations());

        Map<String,String> modTranslations = Maps.newHashMap();
        for (String lang : list) {
            InputStream stream = FortytwoEdit.getAssetsLang(lang);
            if (stream != null)
                Language.loadFromJson(stream,modTranslations::put);
        }

        for (String key : modTranslations.keySet()) {
            if (!newTranslations.containsKey(key))
                newTranslations.put(key,modTranslations.get(key));
        }

        cir.setReturnValue(ClientLanguageInvoker.invokeClientLanguage(Map.copyOf(newTranslations), clientLanguage.isDefaultRightToLeft()));
        
    }

}
