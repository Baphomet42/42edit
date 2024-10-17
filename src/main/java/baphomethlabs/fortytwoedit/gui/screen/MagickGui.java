package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MagickGui extends GenericScreen {

    private ButtonWidget btnWgtAutoClick;
    private ButtonWidget btnWgtHat;
    private static final int LEFT_OFFSET = 20;
    private static final int TOP_OFFSET = 1;
    private static final int ITEM_OFFSET = 2;
    
    public MagickGui() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.NONE;

        this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.itemBuilder","42edit..."),
            button -> this.btnItem()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*2+TOP_OFFSET,80,WID_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.hacks","Hacks..."),
            button -> this.btnHacks()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*3+TOP_OFFSET,80,WID_HEIGHT).build());
        btnWgtHat = this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.hat","Hat"),
            button -> this.btnHat()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*4+TOP_OFFSET,60,WID_HEIGHT).build());
        if(!client.player.getAbilities().creativeMode)
            btnWgtHat.active = false;
        else
            btnWgtHat.setTooltip(Tooltip.of(Text.translatableWithFallback("ftedit.gui.magickScreen.hat.tooltip","Swap current item with helmet slot")));
        this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.superSecret","Super Secret Settings..."),
            button -> this.btnSuperSecretSettings()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*5+TOP_OFFSET,165,WID_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.capes","Capes..."),
            button -> this.btnCapes()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*6+TOP_OFFSET,80,WID_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick","AutoClick..."),
            button -> this.btnAutoClickSettings()).dimensions(x+LEFT_OFFSET,y+ROW_HEIGHT*7+TOP_OFFSET,90,WID_HEIGHT).build());
        btnWgtAutoClick = this.addDrawableChild(ButtonWidget.builder(Text.empty(),
            button -> this.btnAutoClick()).dimensions(x+LEFT_OFFSET+90+5,y+ROW_HEIGHT*7+TOP_OFFSET,70,WID_HEIGHT).build());
        setAutoClickMessage();
    }

    protected void btnItem() {
        client.setScreen(new ItemBuilder());
    }

    protected void btnHacks() {
        client.setScreen(new Hacks());
    }

    protected void btnHat() {
        if(client.player.getAbilities().creativeMode) {
            ItemStack hand = client.player.getMainHandStack().copy();
            ItemStack head = client.player.getInventory().getArmorStack(3).copy();
            BlackMagick.setItem(hand,39,5);
            BlackMagick.setItemMain(head);
        }
        unsel();
    }

    protected void btnSuperSecretSettings() {
        if(hasShiftDown()) {
            client.setScreen(new SecretScreen());
        }
        else {
            FortytwoEdit.cycleSuperSecretSetting();
            unsel();
        }
    }

    protected void btnCapes() {
        client.setScreen(new Capes());
    }

    protected void btnAutoClickSettings() {
        client.setScreen(new AutoClick());
    }

    protected void btnAutoClick() {
        if(btnWgtAutoClick.getMessage().getString().equals("[Use]")) {
            FortytwoEdit.updateAutoClick(false,true,false,1500);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Attack .65]")) {
            FortytwoEdit.updateAutoClick(true,false,false,1500);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Attack 1.5]")) {
            FortytwoEdit.updateAutoClick(false,false,true,650);
        }
        else if(btnWgtAutoClick.getMessage().getString().equals("[Mine]")) {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        else {
            FortytwoEdit.updateAutoClick(false,false,true,1500);
        }
        reloadScreen();
    }

    private void setAutoClickMessage() {
        if(FortytwoEdit.autoClick && !FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.use","[Use]"));
        else if(!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.mine","[Mine]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 1500)
            btnWgtAutoClick.setMessage(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.attackSlow","[Attack 1.5]"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 650)
            btnWgtAutoClick.setMessage(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.attackFast","[Attack .65]"));
        else
            btnWgtAutoClick.setMessage(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.custom","[Custom]"));
        btnWgtAutoClick.setTooltip(Tooltip.of(Text.translatableWithFallback("ftedit.gui.magickScreen.autoClick.cycleTooltip","Cycle auto click mode")));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawItemWithoutEntity(new ItemStack(Items.JIGSAW), x+6, y+6);
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatableWithFallback("ftedit.gui.magickScreen.title",
            "Black Magick by BaphomethLabs").copy().withColor(0x420666).formatted(Formatting.BOLD),
            this.width / 2, y+11, TEXT_COLOR);
		context.drawItemWithoutEntity(new ItemStack(Items.SPONGE),x+LEFT_OFFSET+ITEM_OFFSET,y+44+TOP_OFFSET+ITEM_OFFSET);
		context.drawItemWithoutEntity(new ItemStack(Items.REPEATING_COMMAND_BLOCK),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*3+TOP_OFFSET+ITEM_OFFSET);
		context.drawItemWithoutEntity(new ItemStack(Items.DIAMOND_HELMET),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*4+TOP_OFFSET+ITEM_OFFSET);
		context.drawItemWithoutEntity(new ItemStack(Items.STRUCTURE_BLOCK),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*5+TOP_OFFSET+ITEM_OFFSET);
		context.drawItemWithoutEntity(new ItemStack(Items.ELYTRA),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*6+TOP_OFFSET+ITEM_OFFSET);
		context.drawItemWithoutEntity(new ItemStack(Items.GOLDEN_SWORD),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*7+TOP_OFFSET+ITEM_OFFSET);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            this.client.setScreen(null);
            return true;
        }
        if(super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

}
