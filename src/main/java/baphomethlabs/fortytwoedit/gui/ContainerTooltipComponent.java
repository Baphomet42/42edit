package baphomethlabs.fortytwoedit.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ContainerTooltipComponent implements TooltipComponent {
    public static final Identifier TEXTURE = new Identifier("textures/gui/container/bundle.png");
    private static final int TEXTURE_SIZE = 128;
    private static final int WIDTH_PER_COLUMN = 18;
    private static final int HEIGHT_PER_ROW = 18;
    private final DefaultedList<ItemStack> inventory;
    private final int rowCount;
    private final int columnCount;

    public ContainerTooltipComponent(ContainerTooltipData data) {
        this.inventory = data.getInventory();
        rowCount = data.rowCount;
        columnCount = data.columnCount;
    }

    @Override
    public int getHeight() {
        return rowCount * HEIGHT_PER_ROW + 2 + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return columnCount * WIDTH_PER_COLUMN + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int i = columnCount;
        int j = rowCount;
        int k = 0;
        for (int l = 0; l < j; ++l) {
            for (int m = 0; m < i; ++m) {
                int n = x + m * WIDTH_PER_COLUMN + 1;
                int o = y + l * HEIGHT_PER_ROW + 1;
                this.drawSlot(n, o, k++, context, textRenderer);
            }
        }
        this.drawOutline(x, y, i, j, context);
    }

    private void drawSlot(int x, int y, int index, DrawContext context, TextRenderer textRenderer) {
        if (index >= this.inventory.size()) {
            this.draw(context, x, y, Sprite.SLOT);
            return;
        }
        ItemStack itemStack = this.inventory.get(index);
        this.draw(context, x, y, Sprite.SLOT);
        context.drawItem(itemStack, x + 1, y + 1, index);
        context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1);
    }

    private void drawOutline(int x, int y, int columns, int rows, DrawContext context) {
        int i;
        this.draw(context, x, y, Sprite.BORDER_CORNER_TOP);
        this.draw(context, x + columns * WIDTH_PER_COLUMN + 1, y, Sprite.BORDER_CORNER_TOP);
        for (i = 0; i < columns; ++i) {
            this.draw(context, x + 1 + i * WIDTH_PER_COLUMN, y, Sprite.BORDER_HORIZONTAL_TOP);
            this.draw(context, x + 1 + i * WIDTH_PER_COLUMN, y + rows * HEIGHT_PER_ROW+1, Sprite.BORDER_HORIZONTAL_TOP);
        }
        for (i = 0; i < rows; ++i) {
            this.draw(context, x, y + i * HEIGHT_PER_ROW + 1, Sprite.BORDER_VERTICAL);
            this.draw(context, x + columns * WIDTH_PER_COLUMN + 1, y + i * HEIGHT_PER_ROW + 1, Sprite.BORDER_VERTICAL);
        }
        this.draw(context, x, y + rows * HEIGHT_PER_ROW+1, Sprite.BORDER_CORNER_TOP);
        this.draw(context, x + columns * WIDTH_PER_COLUMN + 1, y + rows * HEIGHT_PER_ROW+1, Sprite.BORDER_CORNER_TOP);
    }

    private void draw(DrawContext context, int x, int y, Sprite sprite) {
        context.drawTexture(TEXTURE, x, y, 0, sprite.u, sprite.v, sprite.width, sprite.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    static enum Sprite {
        SLOT(0, 0, WIDTH_PER_COLUMN, HEIGHT_PER_ROW),
        BORDER_VERTICAL(0, 18, 1, HEIGHT_PER_ROW),
        BORDER_HORIZONTAL_TOP(0, 20, WIDTH_PER_COLUMN, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1);

        public final int u;
        public final int v;
        public final int width;
        public final int height;

        private Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }
}
