package baphomethlabs.fortytwoedit.mixin;

import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import baphomethlabs.fortytwoedit.gui.ContainerTooltipData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    @Final
    protected T handler;

    @Shadow
    protected Slot focusedSlot;

    @Shadow
    protected abstract List<Text> getTooltipFromItem(ItemStack stack);
    
    @Inject(method="drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V", at=@At("HEAD"), cancellable = true)
    private void drawContainerTooltip(DrawContext context, int x, int y, CallbackInfo c) {
        
        if (((ScreenHandler)this.handler).getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            ItemStack stack = this.focusedSlot.getStack().copy();
            if(stack.hasNbt() && stack.getNbt().contains("BlockEntityTag",NbtElement.COMPOUND_TYPE)) {
                NbtCompound bet = (NbtCompound)(stack.getNbt().get("BlockEntityTag").copy());
                if(bet.contains("Items",NbtElement.LIST_TYPE) && ((NbtList)bet.get("Items")).size()>0
                        && ((NbtList)bet.get("Items")).get(0).getType()==NbtElement.COMPOUND_TYPE) {
                
                    NbtList itemsCompound = ((NbtList)bet.get("Items"));
                    boolean empty = true;
                    int rows = 3;
                    int columns = 9;
                    String itemName = stack.getItem().toString();
                    if(itemName.contains("chest") || itemName.contains("shulker") || itemName.contains("barrel")) {
                    }
                    else if(itemName.contains("dispenser") || itemName.contains("dropper")) {
                        columns = 3;
                    }
                    else if(itemName.contains("hopper")) {
                        rows = 1;
                        columns = 5;
                    }
                    else if(itemName.contains("furnace") || itemName.contains("smoker")) {
                        rows = 3;
                        columns = 1;
                    }
                    else if(itemName.contains("brewing_stand")) {
                        rows = 1;
                        columns = 5;
                    }
                    else if(itemName.contains("chiseled_bookshelf")) {
                        rows = 2;
                        columns = 3;
                    }
                    else if(itemName.contains("campfire")) {
                        rows = 1;
                        columns = 4;
                    }
                    ItemStack[] itemsArray = new ItemStack[rows*columns];

                    for(NbtElement el : itemsCompound) {
                        if(((NbtCompound)el).contains("id",NbtElement.STRING_TYPE) && 
                                (((NbtCompound)el).contains("Count",NbtElement.INT_TYPE) || ((NbtCompound)el).contains("Count",NbtElement.BYTE_TYPE)) && 
                                (((NbtCompound)el).contains("Slot",NbtElement.INT_TYPE) || ((NbtCompound)el).contains("Slot",NbtElement.BYTE_TYPE))) {
                            ItemStack current = ItemStack.fromNbt((NbtCompound)el);
                            int slot = -1;
                            if(((NbtCompound)el).contains("Slot",NbtElement.INT_TYPE))
                                slot = ((NbtInt)((NbtCompound)el).get("Slot")).intValue();
                            else if(((NbtCompound)el).contains("Slot",NbtElement.BYTE_TYPE))
                                slot = 0+((NbtByte)((NbtCompound)el).get("Slot")).byteValue();
                            if(slot>=0 && slot<itemsArray.length) {
                                itemsArray[slot]=current;
                                empty = false;
                            }
                        }
                    }

                    if(!empty) {
                        DefaultedList<ItemStack> items = DefaultedList.of();
                        for(int i=0; i<itemsArray.length; i++) {
                            if(itemsArray[i] != null )
                                items.add(itemsArray[i]);
                            else
                                items.add(new ItemStack(Items.AIR,1));
                        }
                        bet.remove("Items");
                        stack.setSubNbt("BlockEntityTag",bet);
                        Optional<TooltipData> data = Optional.of(new ContainerTooltipData(items,rows,columns));
                        context.drawTooltip(this.textRenderer, this.getTooltipFromItem(stack), data, x, y);
                        c.cancel();
                    }
                }
            }
        }
    }

}
