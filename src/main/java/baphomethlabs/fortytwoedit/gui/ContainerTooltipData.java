package baphomethlabs.fortytwoedit.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.collection.DefaultedList;

public class ContainerTooltipData implements TooltipData {
    private final DefaultedList<ItemStack> inventory;
    public final int rowCount;
    public final int columnCount;

    public ContainerTooltipData(DefaultedList<ItemStack> inventory, int rows, int columns) {
        this.inventory = inventory;
        this.rowCount = rows;
        this.columnCount = columns;
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }
}
