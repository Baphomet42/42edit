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
    protected Identifier emptySlotSprite = null;

	public ItemSlotButtonWidget(int x, int y, ItemStack item, ButtonWidget.PressAction onPress) {
		super(x, y, 20, 20, Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        setItem(item);
	}

	public ItemSlotButtonWidget(int x, int y, ButtonWidget.PressAction onPress) {
		this(x, y, null, onPress);
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

    public void setOverlay(Identifier sprite) {
        this.overlay = sprite;
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

        drawItem(context,this.item,this.getX()+2,this.getY()+2);
        if(this.showSlot)
            context.drawGuiTexture(RenderLayer::getGuiTextured, SPRITE_SLOT, this.getX()+1, this.getY()+1, 18, 18);
        if(this.emptySlotSprite != null && this.item.isEmpty())
            context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, this.emptySlotSprite, this.getX()+2, this.getY()+2, 16, 16);
        if(this.overlay != null)
            context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, this.overlay, this.getX()+1, this.getY()+1, 18, 18);
        switch(this.error) {
            case WARN :
                context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, SPRITE_WARNING, this.getX(), this.getY(), 20, 20);
                break;
            case ERROR :
                context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, SPRITE_ERROR, this.getX(), this.getY(), 20, 20);
                break;
            case NONE : break;
        }
	}
    
}
