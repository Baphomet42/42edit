package baphomethlabs.fortytwoedit.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemSlotButtonWidget extends ButtonWidget {

    public static final Identifier SPRITE_SLOT = Identifier.ofVanilla("container/slot");
    public static final Identifier SPRITE_HIGHLIGHT_BACK = Identifier.ofVanilla("container/slot_highlight_back");
    public static final Identifier SPRITE_HIGHLIGHT_FRONT = Identifier.ofVanilla("container/slot_highlight_front");
    public static final Identifier SPRITE_WARNING = Identifier.ofVanilla("world_list/warning_highlighted");
    public static final Identifier SPRITE_ERROR = Identifier.ofVanilla("world_list/error_highlighted");
    public static final Identifier SPRITE_FEET = PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE;
    public static final Identifier SPRITE_LEGS = PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE;
    public static final Identifier SPRITE_CHEST = PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE;
    public static final Identifier SPRITE_HEAD = PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE;
    public static final Identifier SPRITE_OFFHAND = PlayerScreenHandler.EMPTY_OFF_HAND_SLOT_TEXTURE;
    public static final Identifier SPRITE_MAINHAND = Identifier.ofVanilla("container/slot/sword");

    protected ItemStack item;
    protected boolean showSlot = true;
    protected ItemError error = ItemError.NONE;
    protected Identifier overlay = null;
    protected int overlaySize = 16;
    protected Identifier emptySlotSprite = null;
    protected final int size;
    private static final int SIZE_ITEM = 16;
    private static final int SIZE_HIGHLIGHT = 24;
    private static final int SIZE_SLOT = 18;
    private static final int SIZE_ERROR = 20;
    private static final int SIZE_EMPTY_SLOT_SPRITE = 16;

	public ItemSlotButtonWidget(int x, int y, int size, ItemStack item, ButtonWidget.PressAction onPress) {
		super(x, y, size, size, Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.size = size;
        setItem(item);
	}

	public ItemSlotButtonWidget(int x, int y, int size, ButtonWidget.PressAction onPress) {
		this(x, y, size, null, onPress);
	}

    public ItemSlotButtonWidget addEmptySlotSprite(Identifier sprite) {
        this.emptySlotSprite = sprite;
        return this;
    }

    public void setItem(ItemStack item) {
        this.item = item == null ? ItemStack.EMPTY : item;
    }

    public void showSlot(boolean show) {
        this.showSlot = show;
    }

    public void setError(ItemError error) {
        this.error = error == null ? ItemError.NONE : error;
    }

    public void removeOverlay() {
        this.overlay = null;
    }

    public void setOverlay(Identifier sprite, int overlaySize) {
        this.overlay = sprite;
        this.overlaySize = overlaySize;
    }

    protected void drawItem(DrawContext context, ItemStack item, int x, int y) {
        final MinecraftClient client = MinecraftClient.getInstance();
        context.drawItem(item,x,y);
        context.drawStackOverlay(client.textRenderer,item,x,y);
    }

    public enum ItemError {
        NONE,
        WARN,
        ERROR
    }

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);

        if(this.showSlot)
            context.drawGuiTexture(RenderLayer::getGuiTextured,
                SPRITE_SLOT, this.getX()+((size-SIZE_SLOT)/2), this.getY()+((size-SIZE_SLOT)/2), SIZE_SLOT, SIZE_SLOT);

        if(isSelected() && this.showSlot)
            context.drawGuiTexture(RenderLayer::getGuiTextured,
                SPRITE_HIGHLIGHT_BACK, this.getX()+((size-SIZE_HIGHLIGHT)/2), this.getY()+((size-SIZE_HIGHLIGHT)/2), SIZE_HIGHLIGHT, SIZE_HIGHLIGHT);

        drawItem(context,this.item,this.getX()+((size-SIZE_ITEM)/2),this.getY()+((size-SIZE_ITEM)/2));

        if(this.emptySlotSprite != null && this.item.isEmpty())
            context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay,
                this.emptySlotSprite, this.getX()+((size-SIZE_EMPTY_SLOT_SPRITE)/2), this.getY()+((size-SIZE_EMPTY_SLOT_SPRITE)/2),
                SIZE_EMPTY_SLOT_SPRITE, SIZE_EMPTY_SLOT_SPRITE);

        if(isSelected() && this.showSlot)
            context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay,
                SPRITE_HIGHLIGHT_FRONT, this.getX()+((size-SIZE_HIGHLIGHT)/2), this.getY()+((size-SIZE_HIGHLIGHT)/2), SIZE_HIGHLIGHT, SIZE_HIGHLIGHT);

        if(this.overlay != null)
            context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay,
                this.overlay, this.getX()+((size-overlaySize)/2), this.getY()+((size-overlaySize)/2), overlaySize, overlaySize);

        switch(this.error) {
            case WARN : {
                context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay,
                    SPRITE_WARNING, this.getX()+((size-SIZE_ERROR)/2), this.getY()+((size-SIZE_ERROR)/2), SIZE_ERROR, SIZE_ERROR);
                break;
            }
            case ERROR : {
                context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay,
                    SPRITE_ERROR, this.getX()+((size-SIZE_ERROR)/2), this.getY()+((size-SIZE_ERROR)/2), SIZE_ERROR, SIZE_ERROR);
                break;
            }
            case NONE : break;
        }

	}
    
}
