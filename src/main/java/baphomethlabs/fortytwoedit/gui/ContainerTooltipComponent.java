package baphomethlabs.fortytwoedit.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ContainerTooltipComponent implements TooltipComponent {
    private final DefaultedList<ItemStack> inventory;
    private final int rowCount;
    private final int columnCount;

    public ContainerTooltipComponent(ContainerTooltipData data) {
        this.inventory = data.getInventory();
        rowCount = data.rowCount;
        columnCount = data.columnCount;
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return this.getRowsHeight() + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.getColumnsWidth();
    }

    private int getColumnsWidth() {
        return columnCount * 18 + 2;
    }

    private int getRowsHeight() {
        return rowCount * 18 + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        int i = columnCount;
        int j = rowCount;
        int k = 0;
        for (int l = 0; l < j; ++l) {
            for (int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * 18 + 1;
                this.drawSlot(n, o, k++, context, textRenderer);
            }
        }
    }

    private void drawSlot(int x, int y, int index, DrawContext context, TextRenderer textRenderer) {
        ItemStack itemStack = this.inventory.get(index);
        this.draw(context, x, y, Sprite.SLOT);
        context.drawItem(itemStack, x + 1, y + 1, index);
        context.drawStackOverlay(textRenderer, itemStack, x + 1, y + 1);
    }

    private void draw(DrawContext context, int x, int y, Sprite sprite) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, sprite.texture, x, y, sprite.width, sprite.height);
    }

    static enum Sprite {
        SLOT(Identifier.of("container/slot"), 18, 18);

        public final Identifier texture;
        public final int width;
        public final int height;

        private Sprite(Identifier texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}
