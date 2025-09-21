package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ItemHistoryScreen extends GenericScreen {

    protected ScrollList SCROLL_PANE;

    public ItemHistoryScreen() {
        super("Item Editor History");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = ItemHistoryScreen::new;
        this.addBackButton(DebugScreen::new);

        setupScrollPane(false, true);

        ListTag history = FortytwoEdit.getItemHist();
        if(history.isEmpty())
            paneScroll().addRow("No item history to show");
        else
            paneScroll().addRow("Showing recent " + history.size() + " item(s)");

        for(int i=0; i<FortytwoEdit.ITEM_HIST_ROWS; i++) {
            ScrollRow row = paneScroll().addRow();
            for(int slot=0; slot<9; slot++) {
                final ItemStack item;
                int histIndex = i*9+slot;
                if(history.size()>histIndex)
                    item = BlackMagick.itemFromNbt(BlackMagick.validCompound(history.get(i*9+slot)));
                else
                    item = ItemStack.EMPTY;
                ItemSlotButton itemBtn = new ItemSlotButton(ItemSlotButton.SLOT_HEIGHT, item, btn -> btnCopyItemNbt(item));
                itemBtn.showSlot(false);
                itemBtn.active = false;
                if(item != null && !item.isEmpty()) {
                    itemBtn.showSlot(true);
                    itemBtn.active = true;
                    itemBtn.setTooltip(ItemBuilder.makeItemTooltip(item));
                }
                itemBtn.setTooltipDelay(TOOLTIP_DELAY_SHORT);
                row.add(itemBtn, false);
            }
            row.center();
        }

    }

    protected void btnCopyItemNbt(ItemStack stack) {
        if(stack != null && !stack.isEmpty()) {
            String itemData = BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(stack));
            FortytwoEdit.setClipboard(itemData);
            FortytwoEdit.showToast("Item History","Item NBT copied to clipboard");
        }
        unsel();
    }
    
}
