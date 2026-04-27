package baphomethlabs.fortytwoedit.gui.widget;

import baphomethlabs.fortytwoedit.gui.screen.GenericScreen;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton.ItemError;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SpriteButton extends SmartButton {
    
    public static final int MARGIN = 4;
    private static final int SIZE_ERROR = 20;
    
    protected boolean isSprite = false;
    protected boolean canStretch = false;
    protected Identifier spriteId = null;
    protected int spritePixelWidth = 0;
    protected int spritePixelHeight = 0;
    protected int spriteTextureWidth = 0;
    protected int spriteTextureHeight = 0;
    protected float spriteU = 0.0f;
    protected float spriteV = 0.0f;
    protected ItemError error = ItemError.NONE;
    
    public SpriteButton(GenericScreen screen, int width, int height, OnPressInput onPressInput) {
        super(screen, Component.empty(), onPressInput);
        this.setSize(width, height);
    }

    public SpriteButton setSprite(Identifier sprite, int spritePixelSize) {
        return setSprite(sprite, spritePixelSize, spritePixelSize);
    }

    public SpriteButton setSprite(Identifier sprite, int spritePixelWidth, int spritePixelHeight) {
        return setTexture(true, sprite, spritePixelWidth, spritePixelHeight, spritePixelWidth, spritePixelHeight, 0.0f, 0.0f);
    }

    public SpriteButton setTexture(Identifier sprite, int spritePixelWidth, int spritePixelHeight) {
        return setTexture(sprite, spritePixelWidth, spritePixelHeight, spritePixelWidth, spritePixelHeight, 0.0f, 0.0f);
    }

    public SpriteButton setTexture(Identifier sprite, int spritePixelWidth, int spritePixelHeight, int spriteTextureWidth, int spriteTextureHeight, float spriteU, float spriteV) {
        return setTexture(false, sprite, spritePixelWidth, spritePixelHeight, spriteTextureWidth, spriteTextureHeight, spriteU, spriteV);
    }

    private SpriteButton setTexture(boolean isSprite, Identifier sprite, int spritePixelWidth, int spritePixelHeight, int spriteTextureWidth, int spriteTextureHeight, float spriteU, float spriteV) {
        this.isSprite = isSprite;
        this.spriteId = sprite;
        this.spritePixelWidth = spritePixelWidth;
        this.spritePixelHeight = spritePixelHeight;
        this.spriteTextureWidth = spriteTextureWidth;
        this.spriteTextureHeight = spriteTextureHeight;
        this.spriteU = spriteU;
        this.spriteV = spriteV;
        return this;
    }

    public SpriteButton canStretch() {
        this.canStretch = true;
        return this;
    }

    public SpriteButton missingno() {
        return setSprite(TextureManager.INTENTIONAL_MISSING_TEXTURE, 16, 16).canStretch();
    }

    public void setError(ItemError error) {
        this.error = error == null ? ItemError.NONE : error;
    }

	@Override
	protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractContents(context, mouseX, mouseY, delta);

        if (spriteId != null) {
            int safeButtonWidth = this.getWidth() - MARGIN - MARGIN;
            int safeButtonHeight = this.getHeight() - MARGIN - MARGIN;
            int widthStretch = safeButtonWidth / this.spritePixelWidth;
            int heightStretch = safeButtonHeight / this.spritePixelHeight;
            int stretch = Math.min(widthStretch, heightStretch);
            int leftOffset = (int)((safeButtonWidth - (stretch * this.spritePixelWidth)) / 2);
            int topOffset = (int)((safeButtonHeight - (stretch * this.spritePixelHeight)) / 2);
            int left = this.getX() + MARGIN + (this.canStretch ? 0 : leftOffset);
            int top = this.getY() + MARGIN + (this.canStretch ? 0 : topOffset);

            if (isSprite)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, left, top,
                    this.canStretch ? safeButtonWidth : (stretch * this.spritePixelWidth),
                    this.canStretch ? safeButtonHeight : (stretch * this.spritePixelHeight));
            else
                context.blit(RenderPipelines.GUI_TEXTURED, spriteId, left, top, this.spriteU, this.spriteV,
                    this.canStretch ? safeButtonWidth : (stretch * this.spritePixelWidth),
                    this.canStretch ? safeButtonHeight : (stretch * this.spritePixelHeight),
                    this.spritePixelWidth, this.spritePixelHeight, this.spriteTextureWidth, this.spriteTextureHeight);
        }

        if (this.error != ItemError.NONE) {
            Identifier sprite = null;
            switch (this.error) {
                case WARN: sprite = ItemSlotButton.SPRITE_WARNING; break;
                case ERROR: sprite = ItemSlotButton.SPRITE_ERROR; break;
                case NONE: break;
            }
            if (sprite != null) {
                context.blitSprite(RenderPipelines.GUI_TEXTURED,
                    sprite, this.getX()+((getWidth()-SIZE_ERROR)/2), this.getY()+((getHeight()-SIZE_ERROR)/2), SIZE_ERROR, SIZE_ERROR);
            }
        }

	}

}
