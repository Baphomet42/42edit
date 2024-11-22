package baphomethlabs.fortytwoedit.mixin;

import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.BlackMagick.ParsedText;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKeys;

@Mixin (BlockItem.class)
public abstract class BlockItemMixin {

    /** 
     * See {@link net.minecraft.block.entity.BlockEntityType} and {@link net.minecraft.entity.EntityType} `POTENTIALLY_EXECUTES_COMMANDS`
     */
    @Inject(method="shouldShowOperatorBlockWarnings", at=@At(value = "RETURN"), cancellable = true)
    private void overrideWarningMessage(ItemStack stack, @Nullable PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue().booleanValue()) {
            switch(FortytwoEdit.itemWarningMode) {
                case "hide": {
                    cir.setReturnValue(false);
                    break;
                }
                case "smart": {
                    NbtComponent data = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
                    if(data != null) {
                        BlockEntityType<?> blockEntityType = data.getRegistryValueOfId(player.getWorld().getRegistryManager(), RegistryKeys.BLOCK_ENTITY_TYPE);
                        NbtCompound stackNbt = BlackMagick.itemToNbtStorage(stack);

                        if(blockEntityType.equals(BlockEntityType.SIGN) || blockEntityType.equals(BlockEntityType.HANGING_SIGN)) {
                            Set<String> textComponents = Sets.newHashSet();
                            tryAddStringPath(textComponents,BlackMagick.getNbtPath(stackNbt,"components.minecraft:block_entity_data.front_text.messages"));
                            tryAddStringPath(textComponents,BlackMagick.getNbtPath(stackNbt,"components.minecraft:block_entity_data.back_text.messages"));
                            cir.setReturnValue(warnForText(textComponents));
                        }
                        else if(blockEntityType.equals(BlockEntityType.LECTERN)) {
                            Set<String> textComponents = Sets.newHashSet();
                            NbtElement pages = BlackMagick.getNbtPath(stackNbt,"components.minecraft:block_entity_data.Book.components.minecraft:written_book_content.pages");
                            if(pages != null && pages.getType() == NbtElement.LIST_TYPE && !((NbtList)pages).isEmpty()) {
                                NbtList pagesList = (NbtList)pages;
                                if(pagesList.getHeldType() == NbtElement.STRING_TYPE) {
                                    tryAddStringPath(textComponents,BlackMagick.getNbtPath(stackNbt,"components.minecraft:block_entity_data.Book.components.minecraft:written_book_content.pages"));
                                }
                                else if(pagesList.getHeldType() == NbtElement.COMPOUND_TYPE) {
                                    for(int i=0; i<pagesList.size(); i++) {
                                        NbtCompound page = (NbtCompound)pagesList.get(i);
                                        tryAddStringPath(textComponents,BlackMagick.getNbtPath(page,"raw"));
                                        tryAddStringPath(textComponents,BlackMagick.getNbtPath(page,"filtered"));
                                    }
                                }
                            }
                            cir.setReturnValue(warnForText(textComponents));
                        }
                    }
                    break;
                }
                default: break;
            }
        }
    }

    private boolean warnForText(Set<String> textComponents) {
        for (String s : textComponents) {
            ParsedText pt = BlackMagick.jsonFromString(s);
            if(pt.isValid()) {
                if(s.contains("clickEvent") && s.contains("run_command"))
                    return true;
            }
        }
        return false;
    }

    private void tryAddStringPath(Set<String> set, NbtElement el) {
        if(el != null) {
            if(el.getType()==NbtElement.LIST_TYPE) {
                NbtList l = (NbtList)el;
                if(!l.isEmpty() && l.getHeldType()==NbtElement.STRING_TYPE) {
                    for(int i=0; i<l.size(); i++)
                        tryAddString(set, l.get(i));
                }
            }
            else if(el.getType()==NbtElement.STRING_TYPE)
                tryAddString(set, el);
        }
    }

    private void tryAddString(Set<String> set, NbtElement el) {
        if(el != null && el.getType()==NbtElement.STRING_TYPE)
            set.add(((NbtString)el).asString());
    }

}
