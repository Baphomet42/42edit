package baphomethlabs.fortytwoedit.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class ItemSlotButton extends Button {

    public static final ResourceLocation SPRITE_SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    public static final ResourceLocation SPRITE_HIGHLIGHT_BACK = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
    public static final ResourceLocation SPRITE_HIGHLIGHT_FRONT = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");
    public static final ResourceLocation SPRITE_WARNING = ResourceLocation.withDefaultNamespace("world_list/warning_highlighted");
    public static final ResourceLocation SPRITE_ERROR = ResourceLocation.withDefaultNamespace("world_list/error_highlighted");
    public static final ResourceLocation SPRITE_FEET = InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
    public static final ResourceLocation SPRITE_LEGS = InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
    public static final ResourceLocation SPRITE_CHEST = InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
    public static final ResourceLocation SPRITE_HEAD = InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
    public static final ResourceLocation SPRITE_BODY = ResourceLocation.withDefaultNamespace("container/slot/horse_armor");
    public static final ResourceLocation SPRITE_SADDLE = ResourceLocation.withDefaultNamespace("container/slot/saddle");
    public static final ResourceLocation SPRITE_OFFHAND = InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
    public static final ResourceLocation SPRITE_MAINHAND = ResourceLocation.withDefaultNamespace("container/slot/sword");

    protected ItemStack item;
    protected boolean showSlot = true;
    protected ItemError error = ItemError.NONE;
    protected ResourceLocation overlay = null;
    protected int overlaySize = 16;
    protected ResourceLocation emptySlotSprite = null;
    protected final int size;
    private static final int SIZE_ITEM = 16;
    private static final int SIZE_HIGHLIGHT = 24;
    private static final int SIZE_SLOT = 18;
    private static final int SIZE_ERROR = 20;
    private static final int SIZE_EMPTY_SLOT_SPRITE = 16;

	public ItemSlotButton(int x, int y, int size, ItemStack item, Button.OnPress onPress) {
		super(x, y, size, size, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.size = size;
        setItem(item);
	}

	public ItemSlotButton(int x, int y, int size, Button.OnPress onPress) {
		this(x, y, size, null, onPress);
	}

    public ItemSlotButton addEmptySlotSprite(ResourceLocation sprite) {
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

    public void setOverlay(ResourceLocation sprite, int overlaySize) {
        this.overlay = sprite;
        this.overlaySize = overlaySize;
    }

    protected void drawItem(GuiGraphics context, ItemStack item, int x, int y) {
        final Minecraft client = Minecraft.getInstance();
        context.renderItem(item,x,y);
        context.renderItemDecorations(client.font,item,x,y);
    }

    public enum ItemError {
        NONE,
        WARN,
        ERROR
    }

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);

        if(this.showSlot)
            context.blitSprite(RenderType::guiTextured,
                SPRITE_SLOT, this.getX()+((size-SIZE_SLOT)/2), this.getY()+((size-SIZE_SLOT)/2), SIZE_SLOT, SIZE_SLOT);

        if(isHoveredOrFocused() && this.showSlot)
            context.blitSprite(RenderType::guiTextured,
                SPRITE_HIGHLIGHT_BACK, this.getX()+((size-SIZE_HIGHLIGHT)/2), this.getY()+((size-SIZE_HIGHLIGHT)/2), SIZE_HIGHLIGHT, SIZE_HIGHLIGHT);

        drawItem(context,this.item,this.getX()+((size-SIZE_ITEM)/2),this.getY()+((size-SIZE_ITEM)/2));

        if(this.emptySlotSprite != null && this.item.isEmpty())
            context.blitSprite(RenderType::guiTexturedOverlay,
                this.emptySlotSprite, this.getX()+((size-SIZE_EMPTY_SLOT_SPRITE)/2), this.getY()+((size-SIZE_EMPTY_SLOT_SPRITE)/2),
                SIZE_EMPTY_SLOT_SPRITE, SIZE_EMPTY_SLOT_SPRITE);

        if(isHoveredOrFocused() && this.showSlot)
            context.blitSprite(RenderType::guiTexturedOverlay,
                SPRITE_HIGHLIGHT_FRONT, this.getX()+((size-SIZE_HIGHLIGHT)/2), this.getY()+((size-SIZE_HIGHLIGHT)/2), SIZE_HIGHLIGHT, SIZE_HIGHLIGHT);

        if(this.overlay != null)
            context.blitSprite(RenderType::guiTexturedOverlay,
                this.overlay, this.getX()+((size-overlaySize)/2), this.getY()+((size-overlaySize)/2), overlaySize, overlaySize);

        switch(this.error) {
            case WARN : {
                context.blitSprite(RenderType::guiTexturedOverlay,
                    SPRITE_WARNING, this.getX()+((size-SIZE_ERROR)/2), this.getY()+((size-SIZE_ERROR)/2), SIZE_ERROR, SIZE_ERROR);
                break;
            }
            case ERROR : {
                context.blitSprite(RenderType::guiTexturedOverlay,
                    SPRITE_ERROR, this.getX()+((size-SIZE_ERROR)/2), this.getY()+((size-SIZE_ERROR)/2), SIZE_ERROR, SIZE_ERROR);
                break;
            }
            case NONE : break;
        }

	}
    
}
