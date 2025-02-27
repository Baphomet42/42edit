package baphomethlabs.fortytwoedit.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ContainerTooltipComponent implements ClientTooltipComponent {
    private final NonNullList<ItemStack> inventory;
    private final int rowCount;
    private final int columnCount;

    public ContainerTooltipComponent(ContainerTooltipData data) {
        this.inventory = data.getInventory();
        rowCount = data.rowCount;
        columnCount = data.columnCount;
    }

    @Override
    public int getHeight(Font textRenderer) {
        return this.getRowsHeight() + 4;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return this.getColumnsWidth();
    }

    private int getColumnsWidth() {
        return columnCount * 18 + 2;
    }

    private int getRowsHeight() {
        return rowCount * 18 + 2;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
        int i = columnCount;
        int j = rowCount;
        int k = 0;
        for(int l = 0; l < j; ++l) {
            for(int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * 18 + 1;
                this.drawSlot(n, o, k++, context, textRenderer);
            }
        }
    }

    private void drawSlot(int x, int y, int index, GuiGraphics context, Font textRenderer) {
        ItemStack itemStack = this.inventory.get(index);
        this.draw(context, x, y, Sprite.SLOT);
        context.renderItem(itemStack, x + 1, y + 1);
        context.renderItemDecorations(textRenderer, itemStack, x + 1, y + 1);
    }

    private void draw(GuiGraphics context, int x, int y, Sprite sprite) {
        context.blitSprite(RenderType::guiTextured, sprite.texture, x, y, sprite.width, sprite.height);
    }

    static enum Sprite {
        SLOT(ResourceLocation.parse("container/slot"), 18, 18);

        public final ResourceLocation texture;
        public final int width;
        public final int height;

        private Sprite(ResourceLocation texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}
