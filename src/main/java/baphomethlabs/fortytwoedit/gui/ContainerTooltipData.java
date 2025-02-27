package baphomethlabs.fortytwoedit.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ContainerTooltipData implements TooltipComponent {
    private final NonNullList<ItemStack> inventory;
    public final int rowCount;
    public final int columnCount;

    public ContainerTooltipData(NonNullList<ItemStack> inventory, int rows, int columns) {
        this.inventory = inventory;
        this.rowCount = rows;
        this.columnCount = columns;
    }

    public NonNullList<ItemStack> getInventory() {
        return this.inventory;
    }
}
