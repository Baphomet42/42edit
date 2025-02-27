package baphomethlabs.fortytwoedit.gui.screen;

import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MagickGui extends GenericScreen {

    private Button btnWgtAutoClick;
    private Button btnWgtHat;
    private static final int LEFT_OFFSET = 20;
    private static final int TOP_OFFSET = 1;
    private static final int ITEM_OFFSET = 2;
    private static final Component TITLE_TEXT = Component.translatable("42edit.gui.magick_screen.title").copy().withColor(0x420666).withStyle(ChatFormatting.BOLD);

    public MagickGui() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.NONE;

        this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.item_builder"),
            button -> changeScreen(new ItemBuilder())).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*2+TOP_OFFSET,80,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.hacks"),
            button -> changeScreen(new Hacks())).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*3+TOP_OFFSET,80,WID_HEIGHT).build());
        btnWgtHat = this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.hat"),
            button -> this.btnHat()).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*4+TOP_OFFSET,60,WID_HEIGHT).build());
        if(!minecraft.player.getAbilities().instabuild) {
            btnWgtHat.active = false;
            btnWgtHat.setTooltip(TT_CREATIVE);
        }
        else
            btnWgtHat.setTooltip(Tooltip.create(Component.translatable("42edit.gui.magick_screen.hat.tooltip")));
        this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.super_secret"),
            button -> this.btnSuperSecretSettings()).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*5+TOP_OFFSET,165,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.capes"),
            button -> changeScreen(new Capes())).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*6+TOP_OFFSET,80,WID_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("42edit.gui.magick_screen.auto_click"),
            button -> changeScreen(new AutoClick())).bounds(x+LEFT_OFFSET,y+ROW_HEIGHT*7+TOP_OFFSET,90,WID_HEIGHT).build());
        btnWgtAutoClick = this.addRenderableWidget(Button.builder(Component.empty(),
            button -> this.btnAutoClick()).bounds(x+LEFT_OFFSET+90+5,y+ROW_HEIGHT*7+TOP_OFFSET,70,WID_HEIGHT).build());
        setAutoClickMessage();
    }

    protected void btnHat() {
        if(minecraft.player.getAbilities().instabuild) {
            ItemStack hand = minecraft.player.getMainHandItem().copy();
            ItemStack head = minecraft.player.getItemBySlot(EquipmentSlot.HEAD).copy();
            BlackMagick.setItem(hand,39,5);
            BlackMagick.setItemMain(head);
        }
        unsel();
    }

    protected void btnSuperSecretSettings() {
        if(hasShiftDown()) {
            changeScreen(new SecretScreen());
        }
        else {
            FortytwoEdit.cycleSuperSecretSetting();
            unsel();
        }
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
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.use"));
        else if(!FortytwoEdit.autoClick && FortytwoEdit.autoMine && !FortytwoEdit.autoAttack)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.mine"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 1500)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_slow"));
        else if(!FortytwoEdit.autoClick && !FortytwoEdit.autoMine && FortytwoEdit.autoAttack && FortytwoEdit.attackWait == 650)
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.attack_fast"));
        else
            btnWgtAutoClick.setMessage(Component.translatable("42edit.gui.magick_screen.auto_click.custom"));
        btnWgtAutoClick.setTooltip(Tooltip.create(Component.translatable("42edit.gui.magick_screen.auto_click.cycle_tooltip")));
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return null;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.renderFakeItem(new ItemStack(Items.JIGSAW), x+6, y+6);
        context.drawCenteredString(this.font, TITLE_TEXT, this.width / 2, y+11, TEXT_COLOR);
		context.renderFakeItem(new ItemStack(Items.SPONGE),x+LEFT_OFFSET+ITEM_OFFSET,y+44+TOP_OFFSET+ITEM_OFFSET);
		context.renderFakeItem(new ItemStack(Items.REPEATING_COMMAND_BLOCK),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*3+TOP_OFFSET+ITEM_OFFSET);
		context.renderFakeItem(new ItemStack(Items.DIAMOND_HELMET),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*4+TOP_OFFSET+ITEM_OFFSET);
		context.renderFakeItem(new ItemStack(Items.STRUCTURE_BLOCK),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*5+TOP_OFFSET+ITEM_OFFSET);
		context.renderFakeItem(new ItemStack(Items.ELYTRA),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*6+TOP_OFFSET+ITEM_OFFSET);
		context.renderFakeItem(new ItemStack(Items.GOLDEN_SWORD),x+LEFT_OFFSET+ITEM_OFFSET,y+ROW_HEIGHT*7+TOP_OFFSET+ITEM_OFFSET);
    }

}
