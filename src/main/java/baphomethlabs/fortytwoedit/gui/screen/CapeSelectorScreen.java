package baphomethlabs.fortytwoedit.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.FortytwoEdit.CapeTexture;
import baphomethlabs.fortytwoedit.FortytwoEdit.CapeTextureStatus;
import baphomethlabs.fortytwoedit.gui.widget.SpriteButton;
import baphomethlabs.fortytwoedit.gui.widget.ItemSlotButton.ItemError;

public class CapeSelectorScreen extends GenericScreen {
    protected final int playerX = backgroundWidth;
    protected static final int playerY = 0;
    protected static final int playerWidth = 100;
    protected static final Identifier CAPE_ICON_NONE = Identifier.withDefaultNamespace("spectator/close");
    protected static final int CAPE_ICON_NONE_SIZE = 16;
    protected final int playerHeight = backgroundHeight;
    protected static final int CAPE_WIDTH = 10;
    protected static final int CAPE_HEIGHT = 16;
    protected static final int CAPE_TEXTURE_WIDTH = 64;
    protected static final int CAPE_TEXTURE_HEIGHT = 32;
    protected static final float CAPE_SPRITE_U = 1.0f;
    protected static final float CAPE_SPRITE_V = 1.0f;
    protected static final int CAPE_BUTTON_WIDTH = CAPE_WIDTH * 2 + SpriteButton.MARGIN + SpriteButton.MARGIN;
    protected static final int CAPE_BUTTON_HEIGHT = CAPE_HEIGHT * 2 + SpriteButton.MARGIN + SpriteButton.MARGIN;
    protected static final int CAPE_BUTTONS_PER_ROW = WID_WIDTH_FULL / CAPE_BUTTON_WIDTH;

    protected boolean hasUnloadedCapes = false;

    public CapeSelectorScreen() {
        super("Select Custom Cape");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = CapeSelectorScreen::new;
        this.addBackButton(CapeScreen::new);

        FortytwoEdit.resolveCapeUrlQueue();

        setupScrollPane(true, CAPE_BUTTON_HEIGHT);
        hasUnloadedCapes = false;

        CapeTexture currentCape = FortytwoEdit.getCurrentClientCape();

        addCapeButton(0, FortytwoEdit.CLIENT_CAPES.get(0), currentCape.id());

        int btnNumOffset = 0;
        if (currentCape.status() == CapeTextureStatus.UNKNOWN) {
            btnNumOffset = 1;
            addCapeButton(1, currentCape, currentCape.id());
        }

        for (int i = 1; i < FortytwoEdit.CLIENT_CAPES.size(); i++)
            addCapeButton(i + btnNumOffset, FortytwoEdit.CLIENT_CAPES.get(i), currentCape.id());

        paneScroll().centerAll();

        if (paneScroll().getSize() >= 2) {
            int firstLeft = paneScroll().children().getFirst().getLeft();
            int lastLeft = paneScroll().getRow().getLeft();
            if (firstLeft != lastLeft) {
                paneScroll().getRow().shiftRight(firstLeft - lastLeft);
            }
        }

        if (hasUnloadedCapes) {
            paneScroll().addRowPrepend().add(WIDGET_UTIL.newButton("Refresh Capes", (btn, inputs) -> {
                FortytwoEdit.resolveCapeUrlQueue();
                rebuildWidgets();
            }).setTooltip("Some cape textures have not yet loaded. Click to refresh view.").build());
        }

        finalizeScrollPane();
    }

    private void addCapeButton(int i, CapeTexture cape, String currentCape) {
        if (i % CAPE_BUTTONS_PER_ROW == 0)
            paneScroll().addRow();

        SpriteButton wid = new SpriteButton(this, CAPE_BUTTON_WIDTH, CAPE_BUTTON_HEIGHT, (btn, inputs) -> {
            OptionsUtil.ModOptions.CUSTOM_CAPE_TOGGLE.setSetting(true);
            OptionsUtil.ModOptions.CUSTOM_CAPE.setSetting(cape.id());
            rebuildWidgets();
        });
        
        MutableComponent txtCustomTt = Component.empty().append(cape.name());
        if (cape.status()==CapeTextureStatus.UNKNOWN) {
            txtCustomTt.append(Component.empty().append("\n\nUnknown cape selection").withStyle(ChatFormatting.RED));
            wid.setError(ItemError.WARN);
            wid.missingno();
        }
        else if (cape.status() == CapeTextureStatus.NONE) {
            wid.setSprite(CAPE_ICON_NONE, CAPE_ICON_NONE_SIZE);
        }
        else if (FortytwoEdit.CAPE_ID_TO_TEXTURE.containsKey(cape.id())) {
            wid.setTexture(FortytwoEdit.CAPE_ID_TO_TEXTURE.get(cape.id()), CAPE_WIDTH, CAPE_HEIGHT,
                CAPE_TEXTURE_WIDTH, CAPE_TEXTURE_HEIGHT, CAPE_SPRITE_U, CAPE_SPRITE_V);
        }
        else {
            wid.setError(ItemError.WARN);
            txtCustomTt.append("\n\n").append(Component.empty().append("Texture still loading").withStyle(ChatFormatting.YELLOW));
            this.hasUnloadedCapes = true;
        }

        if (cape.desc() != null)
            txtCustomTt.append("\n\n").append(Component.empty().append(cape.desc()).withStyle(ChatFormatting.GRAY));

        if (cape.id().equals(currentCape)) {
            wid.active = false;
        }

        wid.setTooltip(Tooltip.create(txtCustomTt));
        wid.setTooltipDelay(TOOLTIP_DELAY);

        paneScroll().getRow().addNoPad(wid);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        CapeScreen.drawPlayer(context, x + playerX, y + playerY, x + playerX + playerWidth, y + playerY + playerHeight, 60, 0.0F, mouseX, mouseY, (LivingEntity)this.minecraft.player);
    }

}
