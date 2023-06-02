package baphomethlabs.fortytwoedit.mixin;

import net.minecraft.client.gui.screen.ChatInputSuggestor;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import baphomethlabs.fortytwoedit.FortytwoEdit;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    CompletableFuture<Suggestions> pendingSuggestions;
    
    @Inject(method="showCommandSuggestions()V", at=@At("TAIL"))
    private void getSuggestions(CallbackInfo c) {
        if(FortytwoEdit.cacheToolsSuggs) {
            Suggestions suggestions = pendingSuggestions.join();
            String list = "{";
            int i = 0;
            for(Suggestion s : suggestions.getList()) {
                if(i>0)
                    list += ",";
                list += "\""+s.getText()+"\"";
                i++;
            }
            list += "}";
            System.out.println("Size "+i+":");
            System.out.println(list);
        }
    }

}