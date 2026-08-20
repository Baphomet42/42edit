package baphomethlabs.fortytwoedit.gui.screen;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FileTools;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.OptionsUtil;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;

public class HacksScreen extends GenericScreen {

    protected SmartEditBox txtRando;
    protected boolean unsaved = false;
    private static final int MAX_RANDO_SLOTS_LENGTH = 15;

    public HacksScreen() {
        super("Hacks");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = HacksScreen::new;
        this.addBackButton();
        
        setupScrollPane();
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Coord Hud", btn -> {
                    OptionsUtil.ModOptions.COORD_HUD.toggleSetting();
                    rebuildWidgets();
                }).fullWidth().setBoolName(OptionsUtil.ModOptions.COORD_HUD.getSetting()).setRenderItem(Items.COMPASS)
                .setTooltip(OptionsUtil.ModOptions.COORD_HUD.getButtonTooltip()).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Mix", btn -> {
                    setTxtRando();
                    FortytwoEdit.toggleRandoModeEnabled();
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.isRandoModeEnabled()).setRenderItem(Items.CRACKED_DEEPSLATE_BRICKS).setTooltip(
                    "Toggle mix mode\n\nWhen on - after placing a block, change to a random hotbar slot based on the specified slots"
                ).build(),
            WIDGET_UTIL.newEditBox().setMaxLength(MAX_RANDO_SLOTS_LENGTH).setValue(getRandoSlotsValue()).setSmartTooltip(
                    "Mix mode slots\n\nChoose which slots should be randomized.\n\n"
                    + "Example: (1233 will give slots 1 and 2 a 25% chance and slot 3 a 50% chance)"
                )
                .setResponder(this::editTxtRando).runWithSelf(w -> this.txtRando = w).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Get Entity", btn -> this.btnGetEntity(1))
                .setTooltip("Copy entity data within 2.5 blocks and get a spawn egg for the entities").setRenderItem(Items.CREEPER_HEAD).build(),
            WIDGET_UTIL.newButton("Full Data", btn -> this.btnGetEntity(0))
                .setTooltip("Get Entity without removing position, uuid, etc.").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Xray", btn -> {
                    // minecraft.levelRenderer.allChanged(); // to_do enable if invis block mixins are reimplemented
                    FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
                    FortytwoEdit.xrayEntity = FortytwoEdit.seeInvis;
                    rebuildWidgets();
                }).setBoolName(FortytwoEdit.seeInvis).setTooltip("Toggle xray mode\n\nWhen on: all entities will glow through blocks").setRenderItem(Items.BARRIER).build(),
            WIDGET_UTIL.newButton("Find Invis Entities", btn -> this.btnFindInvis()).creativeOnly("Print positions of invisible entities").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Death Pos", btn -> this.btnDeathPos())
                .setTooltip("Print your last position of death (only you can see this)").setRenderItem(Items.SKELETON_SKULL).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton(Component.translatable("42edit.gui.magick_screen.hat"), btn -> this.btnHat())
                .setRenderItem(Items.DIAMOND_HELMET).creativeOnly(Component.translatable("42edit.gui.magick_screen.hat.tooltip")).build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Look North", btn -> this.btnLookN())
                .setTooltip("Set your rotation to straight north").build(),
            WIDGET_UTIL.newButton("Rotate Clockwise", btn -> this.btnLookR())
                .setTooltip("Rotate 90\\u00b0 clockwise").build()
        );
        paneScroll().addRow(
            WIDGET_UTIL.newButton("Take Panorama", btn -> this.btnPano())
                .setTooltip("Take a panorama screenshot").build(),
            WIDGET_UTIL.newButton("Open Screenshots", btn -> this.btnScreenshots())
                .setTooltip("Open screenshots folder to view panorama").build()
        );
        finalizeScrollPane();
    }

    protected void editTxtRando(String text) {
        unsaved = true;
    }

    protected String getRandoSlotsValue() {
        if (FortytwoEdit.randoSlots != null) {
            String keys = "";
            for (int i: FortytwoEdit.randoSlots) {
                keys += i;
            }
            return keys;
        }
        return null;
    }

    protected void setTxtRando() {
        if (unsaved) {
            FortytwoEdit.randoSlots = null;
            if (txtRando.getValue() != null && !txtRando.getValue().equals("")) {
                String inp = txtRando.getValue().replaceAll("[^1-9]", "");
                if (inp.length() > 0) {
                    int[] slots = new int[inp.length()];
                    for (int i = 0; i < slots.length; i++) {
                        try {
                            slots[i] = Integer.parseInt("" + inp.charAt(i));
                        } catch (NumberFormatException ex) {}
                    }
                    if (slots != null && slots.length > 0)
                        FortytwoEdit.randoSlots = slots;
                }
            }

            FortytwoEdit.setRandoModeEnabled(FortytwoEdit.randoSlots != null);
            unsaved = false;
            rebuildWidgets();
        }
    }

    protected void btnHat() {
        if (BlackMagick.isCreative(minecraft)) {
            ItemStack hand = minecraft.player.getMainHandItem().copy();
            ItemStack head = minecraft.player.getItemBySlot(EquipmentSlot.HEAD).copy();
            BlackMagick.setItemHead(hand);
            BlackMagick.setItemMain(head);
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
            if (current.getType() != EntityTypes.PLAYER
            && current.getX() > x - range && current.getX() < x + range
            && current.getY() > y - range && current.getY() < y + range
            && current.getZ() > z - range && current.getZ() < z + range) {
                CompoundTag nbt = new CompoundTag();
                CompoundTag components = new CompoundTag();
                nbt.put("components", components);
                CompoundTag entityData = new CompoundTag();
                if ((new EntityDataAccessor(current)).getData() != null)
                    entityData = (new EntityDataAccessor(current)).getData();
                components.put("entity_data", entityData);
                if (current.getType() == EntityTypes.ARMOR_STAND) {
                    nbt.putString("id", "minecraft:armor_stand");
                    entityData.putString("id", "minecraft:armor_stand");
                    if (mode == 1) {
                        entityData.remove("Brain");
                        entityData.remove("Health");
                    }
                }
                else {
                    nbt.putString("id", "minecraft:endermite_spawn_egg");
                    entityData.putString("id", BlackMagick.identifierToString(EntityType.getKey(current.getType())));
                    components.putString("minecraft:item_name", "Custom "
                        + BlackMagick.textComponentToStringLiteral(current.getType().getDescription()) + " Spawn Egg");
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
            nbt.putString("id", "bundle");
            CompoundTag components = new CompoundTag();
            nbt.put("components", components);
            ListTag bundle = new ListTag();
            components.put("bundle_contents", bundle);
            for (int i = 0; i < items.size(); i++) {
                bundle.add(items.get(i));
            }
            ItemStack item = BlackMagick.itemFromNbt(nbt);
            if (items.size() == 1)
                item = BlackMagick.itemFromNbtTag(bundle.get(0));

            FortytwoEdit.setClipboard(BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(item)));
            FortytwoEdit.showToast("Get Entity", "Entity data copied");

            if (BlackMagick.isCreative(minecraft) && !item.isEmpty()) {
                BlackMagick.setItemMain(item);
            }
        }
        else {
            FortytwoEdit.showToast("Get Entity", "No entities within range");
        }
    }

    protected void btnFindInvis() {
        int found = 0;
        Iterator<Entity> entities = minecraft.level.entitiesForRendering().iterator();
        while (entities.hasNext()) {
            Entity current = entities.next();
            if (current.getType() == EntityTypes.ARMOR_STAND) {
                CompoundTag nbt = new CompoundTag();
                if ((new EntityDataAccessor(current)).getData() != null)
                    nbt = (new EntityDataAccessor(current)).getData();
                if ((nbt.getByte("Invisible").isPresent() && nbt.getByte("Invisible").get() == 1)
                && !(nbt.getByte("CustomNameVisible").isPresent() && nbt.getByte("CustomNameVisible").get() == 1)
                && !nbt.getCompound("equipment").isPresent()) {
                    reportInvis(current);
                    found++;
                }
            }
            else if (current.getType() == EntityTypes.ITEM_FRAME || current.getType() == EntityTypes.GLOW_ITEM_FRAME) {
                CompoundTag nbt = new CompoundTag();
                if ((new EntityDataAccessor(current)).getData() != null)
                    nbt = (new EntityDataAccessor(current)).getData();
                if (nbt.getByte("Invisible").isPresent() && nbt.getByte("Invisible").get() == 1) {
                    if (!nbt.contains("Item")) {
                        reportInvis(current);
                        found++;
                    }
                }
            }
        }
        if (found > 0)
            FortytwoEdit.showToast("Find Invis", "Found " + found + " invisible entities");
        else
            FortytwoEdit.showToast("Find Invis", "No invisible entities detected");
    }

    private void reportInvis(Entity entity) {
        BlackMagick.sendClientChat(
            Component.empty().append(entity.getName()).append(" [" + entity.getBlockX() + ", " + entity.getBlockY() + ", " + entity.getBlockZ() + "]")
            .withStyle(style -> style.withHoverEvent(
            new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(entity.getType(), entity.getUUID(), entity.getName())))
            .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + entity.getBlockX() + " " + entity.getBlockY() + " " + entity.getBlockZ()))));
    }

    protected void btnDeathPos() {
        if (minecraft.player.getLastDeathLocation().isPresent()) {
            GlobalPos pos = minecraft.player.getLastDeathLocation().get();
            String coords = "Last death [X: " + pos.pos().getX() + ", Y: " + pos.pos().getY() + ", Z: " + pos.pos().getZ() + "] in "
                + BlackMagick.identifierToString(pos.dimension().identifier());
            BlackMagick.sendClientChat(coords);
            FortytwoEdit.showToast("Death Pos", "Death coords sent to chat");
        }
        else {
            FortytwoEdit.showToast("Death Pos", "No death pos recorded");
        }
    }

    protected void btnLookN() {
        minecraft.player.setXRot(0);
        minecraft.player.setYRot(180);
    }

    protected void btnLookR() {
        minecraft.player.setYRot(minecraft.player.getYRot() + 90);
    }

    protected void btnPano() {
        minecraft.grabPanoramixScreenshot(new File(minecraft.gameDirectory.getAbsolutePath()));
    }

    protected void btnScreenshots() {
        FileTools.openMinecraftScreenshots();
    }

    protected void saveAll() {
        setTxtRando();
    }

    @Override
    public void rebuildWidgets() {
        saveAll();
        super.rebuildWidgets();
    }

    @Override
    public void onCloseAction() {
        saveAll();
        super.onCloseAction();
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !txtRando.canConsumeInput();
    }

    @Override
    public void tick() {
        if (!txtRando.canConsumeInput())
            setTxtRando();

        super.tick();
    }

}
