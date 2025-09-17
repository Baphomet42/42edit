package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemHistoryScreen extends GenericScreen {

    public ItemHistoryScreen() {
        super("Item Editor History");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.ITEM_HISTORY;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new DebugScreen())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());

        ScrollList scrollList = new ScrollList(true);
        this.addRenderableWidget(scrollList);

        ListTag history = FortytwoEdit.getItemHist();

        if(history.isEmpty())
            scrollList.addRow(new ScrollRow("No item history to show"));
        else
            scrollList.addRow(new ScrollRow("Showing recent " + history.size() + " item(s)"));

        for(int i=0; i<FortytwoEdit.ITEM_HIST_ROWS; i++) {
            ScrollRow row = scrollList.addRow();
            for(int slot=0; slot<9; slot++) {
                final ItemStack item;
                int histIndex = i*9+slot;
                if(history.size()>histIndex)
                    item = BlackMagick.itemFromNbt(BlackMagick.validCompound(history.get(i*9+slot)));
                else
                    item = ItemStack.EMPTY;
                ItemSlotButton itemBtn = new ItemSlotButton(20, item, btn -> btnCopyItemNbt(item));
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
