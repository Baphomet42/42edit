package baphomethlabs.fortytwoedit.gui.screen;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FileTools;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;

public class HacksScreen extends GenericScreen {

    protected Button btnWgtFindInvis;
    protected SmartEditBox txtRando;
    protected boolean unsaved = false;

    public HacksScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = HacksScreen::new;
        this.addBackButton();

        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Coord Hud [On]"),
                Component.literal("Coord Hud [Off]"), FortytwoEdit.showCoordHud).displayOnlyValue().create(x+20,y+ROW_HEIGHT*2+1,120,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.readOptions();
            FortytwoEdit.showCoordHud = (boolean)trackOutput;
            FortytwoEdit.updateOptions();
            unsel();
        }));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Mix [On]"),
                Component.literal("Mix [Off]"), FortytwoEdit.randoMode).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty(
                "Toggle mix mode\n\nWhen on: after placing a block, change to a random hotbar slot\n\n"
                +"If numbers are specified, the random slot will be selected from those.\nExample: (1233 will give slots 1 and 2 a 25% chance and slot 3 a 50% chance)"))).create(x+20,y+ROW_HEIGHT*3+1,80,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            setTxtRando();
            if (!(boolean)trackOutput)
                FortytwoEdit.randoMode = false;
            else if ((boolean)trackOutput && FortytwoEdit.randoSlots != null)
                FortytwoEdit.randoMode = true;
            unsel();
        }));
        this.txtRando = new SmartEditBox(this.font,x+20+80+WID_SPACE,y+ROW_HEIGHT*3+1,100-2,WID_HEIGHT,Component.nullToEmpty(""));
        this.txtRando.setMaxLength(15);
        if (FortytwoEdit.randoSlots != null) {
            String keys = "";
            for (int i: FortytwoEdit.randoSlots) {
                keys += i;
            }
            txtRando.setValue(keys);
        }
        this.txtRando.setResponder(this::editTxtRando);
        this.addRenderableWidget(this.txtRando);
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Get Entity"), button -> this.btnGetEntity(1)).bounds(x+20,y+ROW_HEIGHT*4+1,80,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Copy entity data within 2.5 blocks and get a spawn egg for the entities")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Full Data"), button -> this.btnGetEntity(0)).bounds(x+20+80+WID_SPACE,y+ROW_HEIGHT*4+1,60,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Get Entity without removing position, uuid, etc.")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Xray [On]"),
                Component.literal("Xray [Off]"), FortytwoEdit.seeInvis).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Toggle xray mode\n\nWhen on: all entities will glow through blocks"))).create(x+20,y+ROW_HEIGHT*5+1,100,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            //minecraft.levelRenderer.allChanged(); // to_do enable if invis block mixins are reimplemented
            FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
            FortytwoEdit.xrayEntity = FortytwoEdit.seeInvis;
            unsel();
        }));
        btnWgtFindInvis = this.addRenderableWidget(Button.builder(Component.nullToEmpty("Find Invis Entities"),
            button -> this.btnFindInvis()).bounds(x+20+100+WID_SPACE,y+ROW_HEIGHT*5+1,100,WID_HEIGHT).build());
        if (!minecraft.player.getAbilities().instabuild) {
            btnWgtFindInvis.active = false;
            btnWgtFindInvis.setTooltip(TT_CREATIVE);
        }
        else
            btnWgtFindInvis.setTooltip(Tooltip.create(Component.nullToEmpty("Print positions of invisible entities (only you can see this)")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Death Pos"), button -> this.btnDeathPos()).bounds(x+20,y+ROW_HEIGHT*6+1,100,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Print your last position of death (only you can see this)")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("   Auto Fish [On]"),
                Component.literal("   Auto Fish [Off]"), FortytwoEdit.autoFish).displayOnlyValue().withTooltip(val -> Tooltip.create(Component.nullToEmpty("Hold a fishing rod to automatically fish\n\nRequires subtitles to be on"))).create(x+20+100+WID_SPACE,y+ROW_HEIGHT*6+1,100,WID_HEIGHT,
                Component.nullToEmpty(""), (button, trackOutput) -> {
            FortytwoEdit.autoFish = !FortytwoEdit.autoFish;
            unsel();
        }));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Look N"), button -> this.btnLookN()).bounds(x+20,y+ROW_HEIGHT*7+1,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Set your rotation to straight north")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Rotate"), button -> this.btnLookR()).bounds(x+20+40+WID_SPACE,y+ROW_HEIGHT*7+1,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Rotate 90\u00b0 clockwise")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Pano"), button -> this.btnPano()).bounds(x+20+40+WID_SPACE+40+WID_SPACE,y+ROW_HEIGHT*7+1,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Take a panorama screenshot")));
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("View Pano"), button -> this.btnScreenshots()).bounds(x+20+40+WID_SPACE+40+WID_SPACE+40+WID_SPACE,y+ROW_HEIGHT*7+1,60,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Open screenshots folder to view panorama")));
    }

    protected void editTxtRando(String text) {
        unsaved = true;
    }

    protected void setTxtRando() {
        if (unsaved) {
            FortytwoEdit.randoSlots = null;
            if (txtRando.getValue() != null && !txtRando.getValue().equals("")) {
                String inp = txtRando.getValue().replaceAll("[^1-9]","");
                if (inp.length()>0) {
                    int[] slots = new int[inp.length()];
                    for (int i=0; i<slots.length; i++) {
                        try {
                            slots[i]=Integer.parseInt(""+inp.charAt(i));
                        } catch (NumberFormatException ex) {}
                    }
                    FortytwoEdit.randoSlots = slots;
                }
            }

            FortytwoEdit.randoMode = (FortytwoEdit.randoSlots!=null);
            unsaved = false;
            reloadScreen();
        }
    }

    protected void btnGetEntity(int mode) {
        Iterator<Entity> entities = minecraft.level.entitiesForRendering().iterator();
        List<CompoundTag> items = Lists.newArrayList();
        double x = minecraft.player.getX();
        double y = minecraft.player.getY();
        double z = minecraft.player.getZ();
        double range = 2.5;
        while (entities.hasNext()) {
            Entity current = entities.next();
            if (current.getType() != EntityType.PLAYER
            && current.getX()>x-range && current.getX()<x+range
            && current.getY()>y-range && current.getY()<y+range
            && current.getZ()>z-range && current.getZ()<z+range) {
                CompoundTag nbt = new CompoundTag();
                CompoundTag components = new CompoundTag();
                nbt.put("components",components);
                CompoundTag entityData = new CompoundTag();
                if ((new EntityDataAccessor(current)).getData()!=null)
                    entityData = (new EntityDataAccessor(current)).getData();
                components.put("entity_data",entityData);
                if (current.getType() == EntityType.ARMOR_STAND) {
                    nbt.putString("id","minecraft:armor_stand");
                    entityData.putString("id","minecraft:armor_stand");
                    if (mode == 1) {
                        entityData.remove("Brain");
                        entityData.remove("Health");
                    }
                }
                else {
                    nbt.putString("id","minecraft:endermite_spawn_egg");
                    entityData.putString("id",BlackMagick.identifierToString(EntityType.getKey(current.getType())));
                    components.putString("minecraft:item_name","Custom "
                        +BlackMagick.textComponentToStringLiteral(current.getType().getDescription())+" Spawn Egg");
                }
                if (mode == 1) {
                    entityData.remove("Air");
                    entityData.remove("FallDistance");
                    entityData.remove("Fire");
                    entityData.remove("Motion");
                    entityData.remove("OnGround");
                    entityData.remove("PortalCooldown");
                    entityData.remove("Pos");
                    entityData.remove("Rotation");
                    entityData.remove("TicksFrozen");
                    entityData.remove("UUID");
                    entityData.remove("AbsorptionAmount");
                    entityData.remove("DeathTime");
                    entityData.remove("HurtByTimestamp");
                    entityData.remove("HurtTime");

                    entityData.remove("Facing");
                    entityData.remove("TileX");
                    entityData.remove("TileY");
                    entityData.remove("TileZ");
                }
                items.add(nbt);
            }
        }
        if (!items.isEmpty()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("id","bundle");
            CompoundTag components = new CompoundTag();
            nbt.put("components",components);
            ListTag bundle = new ListTag();
            components.put("bundle_contents",bundle);
            for (int i=0; i<items.size(); i++) {
                bundle.add(items.get(i));
            }
            ItemStack item = BlackMagick.itemFromNbt(nbt);
            if (items.size()==1)
                item = BlackMagick.itemFromNbtTag(bundle.get(0));

            FortytwoEdit.setClipboard(BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(item)));
            FortytwoEdit.showToast("Get Entity","Entity data copied");

            if (minecraft.player.getAbilities().instabuild && !item.isEmpty()) {
                BlackMagick.setItemMain(item);
            }
        }
        else {
            FortytwoEdit.showToast("Get Entity","No entities within range");
        }
        unsel();
    }

    protected void btnFindInvis() {
        if (minecraft.player.getAbilities().instabuild) {
            int found = 0;
            Iterator<Entity> entities = minecraft.level.entitiesForRendering().iterator();
            while (entities.hasNext()) {
                Entity current = entities.next();
                if (current.getType() == EntityType.ARMOR_STAND) {
                    CompoundTag nbt = new CompoundTag();
                    if ((new EntityDataAccessor(current)).getData()!=null)
                        nbt = (new EntityDataAccessor(current)).getData();
                    if ((nbt.getByte("Invisible").isPresent() && nbt.getByte("Invisible").get()==1)
                    && !(nbt.getByte("CustomNameVisible").isPresent() && nbt.getByte("CustomNameVisible").get()==1)
                    && !nbt.getCompound("equipment").isPresent()) {
                        reportInvis(current);
                        found++;
                    }
                }
                else if (current.getType() == EntityType.ITEM_FRAME || current.getType() == EntityType.GLOW_ITEM_FRAME) {
                    CompoundTag nbt = new CompoundTag();
                    if ((new EntityDataAccessor(current)).getData()!=null)
                        nbt = (new EntityDataAccessor(current)).getData();
                    if (nbt.getByte("Invisible").isPresent() && nbt.getByte("Invisible").get()==1) {
                        if (!nbt.contains("Item")) {
                            reportInvis(current);
                            found++;
                        }
                    }
                }
            }
            if (found>0)
                FortytwoEdit.showToast("Find Invis","Found "+found+" invisible entities");
            else
                FortytwoEdit.showToast("Find Invis","No invisible entities detected");
        }
        else {
            FortytwoEdit.showToast("Find Invis", "This requires creative mode");
        }
        unsel();
    }

    private void reportInvis(Entity entity) {
        BlackMagick.sendClientChat(
            Component.empty().append(entity.getName()).append(" ["+entity.getBlockX()+", "+entity.getBlockY()+", "+entity.getBlockZ()+"]")
            .withStyle(style -> style.withHoverEvent(
            new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(entity.getType(), entity.getUUID(), entity.getName())))
            .withClickEvent(new ClickEvent.SuggestCommand("/tp @s "+entity.getBlockX() + " " + entity.getBlockY() + " " + entity.getBlockZ()))));
    }

    protected void btnDeathPos() {
        if (minecraft.player.getLastDeathLocation().isPresent()) {
            GlobalPos pos = minecraft.player.getLastDeathLocation().get();
            String coords = "Last death [X: "+pos.pos().getX()+", Y: "+pos.pos().getY()+", Z: "+pos.pos().getZ()+"] in "+BlackMagick.identifierToString(pos.dimension().identifier());
            BlackMagick.sendClientChat(coords);
            FortytwoEdit.showToast("Death Pos", "Death coords sent to chat");
        }
        else {
            FortytwoEdit.showToast("Death Pos", "No death pos recorded");
        }
        unsel();
    }

    protected void btnLookN() {
		minecraft.player.setXRot(0);
		minecraft.player.setYRot(180);
        unsel();
    }

    protected void btnLookR() {
		minecraft.player.setYRot(minecraft.player.getYRot() + 90);
        unsel();
    }

    protected void btnPano() {
        minecraft.grabPanoramixScreenshot(new File(minecraft.gameDirectory.getAbsolutePath()));
        unsel();
    }

    protected void btnScreenshots() {
        FileTools.openMinecraftScreenshots();
        unsel();
    }

    protected void saveAll() {
        setTxtRando();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, Component.nullToEmpty("Hacks"), this.width / 2, y+11, TEXT_COLOR);
		context.fakeItem(new ItemStack(Items.COMPASS),x+20+2,y+ROW_HEIGHT*2+1+2);
		context.fakeItem(new ItemStack(Items.CRACKED_DEEPSLATE_BRICKS),x+20+2,y+ROW_HEIGHT*3+1+2);
		context.fakeItem(new ItemStack(Items.CREEPER_SPAWN_EGG),x+20+2,y+ROW_HEIGHT*4+1+2);
		context.fakeItem(new ItemStack(Items.BARRIER),x+20+2,y+ROW_HEIGHT*5+1+2);
		context.fakeItem(new ItemStack(Items.SKELETON_SKULL),x+20+2,y+ROW_HEIGHT*6+1+2);
		context.fakeItem(new ItemStack(Items.FISHING_ROD),x+20+2+100+WID_SPACE,y+ROW_HEIGHT*6+1+2);
    }

    @Override
    public void resize(int width, int height) {
        saveAll();
        super.resize(width, height);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtRando.canConsumeInput();
    }

    @Override
    public void onCloseAction() {
        saveAll();
        super.onCloseAction();
    }

    @Override
    public void tick() {
        if (!txtRando.canConsumeInput())
            setTxtRando();

        super.tick();
    }

}
