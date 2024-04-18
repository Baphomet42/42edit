package baphomethlabs.fortytwoedit.gui.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import baphomethlabs.fortytwoedit.BlackMagick;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.GlobalPos;

public class Hacks extends GenericScreen {

    protected ButtonWidget btnWgtFindInvis;
    protected TextFieldWidget txtRando;
    protected boolean unsaved = false;
    
    public Hacks() {}

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Mix [On]"), Text.literal("Mix [Off]")).initially(FortytwoEdit.randoMode).omitKeyText().build(x+20,y+22*2+1,80,20, Text.of(""), (button, trackOutput) -> {
            setTxtRando();
            if(!(boolean)trackOutput)
                FortytwoEdit.randoMode = false;
            else if((boolean)trackOutput && FortytwoEdit.randoSlots != null)
                FortytwoEdit.randoMode = true;
            this.resize(this.client,this.width,this.height);
        })).setTooltip(Tooltip.of(Text.of("Toggle mix mode\n\nWhen on: after placing a block, change to a random hotbar slot\n\n"
            +"If numbers are specified, the random slot will be selected from those.\nExample: (1233 will give slots 1 and 2 a 25% chance and slot 3 a 50% chance)")));
        this.txtRando = new TextFieldWidget(this.textRenderer,x+105+1,y+44+1,100-2,20,Text.of(""));
        this.txtRando.setMaxLength(15);
        if(FortytwoEdit.randoSlots != null) {
            String keys = "";
            for(int i: FortytwoEdit.randoSlots) {
                keys += i;
            }
            txtRando.setText(keys);
        }
        this.txtRando.setChangedListener(this::editTxtRando);
        this.addDrawableChild(this.txtRando);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Get Entity"), button -> this.btnGetEntity(1)).dimensions(x+20,y+22*3+1,80,20).build())
            .setTooltip(Tooltip.of(Text.of("Copy entity data within 2.5 blocks and get a spawn egg for the entities")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Full Data"), button -> this.btnGetEntity(0)).dimensions(x+20+80+5,y+22*3+1,60,20).build())
            .setTooltip(Tooltip.of(Text.of("Get Entity without removing position, uuid, etc.")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Xray [On]"), Text.literal("Xray [Off]")).initially(FortytwoEdit.seeInvis).omitKeyText().build(x+20,y+22*4+1,100,20, Text.of(""), (button, trackOutput) -> {
            client.worldRenderer.reload();
            FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
            FortytwoEdit.xrayEntity = FortytwoEdit.seeInvis;
            this.resize(this.client,this.width,this.height);
        })).setTooltip(Tooltip.of(Text.of("Toggle xray mode\n\nWhen on: barriers, light blocks, and other invisible blocks will appear as solid blocks")));
        btnWgtFindInvis = this.addDrawableChild(ButtonWidget.builder(Text.of("Find Invis Entities"), button -> this.btnFindInvis()).dimensions(x+20+100+5,y+22*4+1,100,20).build());
        if(!client.player.getAbilities().creativeMode)
            btnWgtFindInvis.active = false;
        else
            btnWgtFindInvis.setTooltip(Tooltip.of(Text.of("Print positions of invisible entities (only you can see this)")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Death Pos"), button -> this.btnDeathPos()).dimensions(x+20,y+22*5+1,100,20).build())
            .setTooltip(Tooltip.of(Text.of("Print your last position of death (only you can see this)")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("   Auto Fish [On]"), Text.literal("   Auto Fish [Off]")).initially(FortytwoEdit.autoFish).omitKeyText().build(x+20+100+5,y+22*5+1,100,20, Text.of(""), (button, trackOutput) -> {
            FortytwoEdit.autoFish = !FortytwoEdit.autoFish;
            this.resize(this.client,this.width,this.height);
        })).setTooltip(Tooltip.of(Text.of("Hold a fishing rod to automatically fish\n\nRequires subtitles to be on")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Look N"), button -> this.btnLookN()).dimensions(x+20,y+22*7+1,40,20).build())
            .setTooltip(Tooltip.of(Text.of("Set your rotation to straight north")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Rotate"), button -> this.btnLookR()).dimensions(x+20+40+5,y+22*7+1,40,20).build())
            .setTooltip(Tooltip.of(Text.of("Rotate 90\u00b0 clockwise")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Pano"), button -> this.btnPano()).dimensions(x+20+40+5+40+5,y+22*7+1,40,20).build())
            .setTooltip(Tooltip.of(Text.of("Take a panorama screenshot")));
        this.addDrawableChild(ButtonWidget.builder(Text.of("View Pano"), button -> this.btnScreenshots()).dimensions(x+20+120+15,y+22*7+1,60,20).build())
            .setTooltip(Tooltip.of(Text.of("Open screenshots folder to view panorama")));
    }

    protected void btnBack() {
        saveAll();
        client.setScreen(new MagickGui());
    }

    protected void editTxtRando(String text) {
        unsaved = true;
    }

    protected void setTxtRando() {
        if(unsaved) {
            FortytwoEdit.randoSlots = null;
            if(txtRando.getText() != null && !txtRando.getText().equals("")) {
                String inp = txtRando.getText().replaceAll("[^1-9]","");
                if(inp.length()>0) {
                    int[] slots = new int[inp.length()];
                    for(int i=0; i<slots.length; i++) {
                        try{
                            slots[i]=Integer.parseInt(""+inp.charAt(i));
                        }
                        catch(NumberFormatException ex) {}
                    }
                    FortytwoEdit.randoSlots = slots;
                }
            }

            FortytwoEdit.randoMode = (FortytwoEdit.randoSlots!=null);
            unsaved = false;
            this.resize(this.client,this.width,this.height);
        }
    }

    protected void btnGetEntity(int mode) {
        Iterator<Entity> entities = client.world.getEntities().iterator();
        List<NbtCompound> items = new ArrayList<>();
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        double range = 2.5;
        while (entities.hasNext()) {
            Entity current = entities.next();
            if(current.getType() != EntityType.PLAYER
            && current.getX()>x-range && current.getX()<x+range
            && current.getY()>y-range && current.getY()<y+range
            && current.getZ()>z-range && current.getZ()<z+range) {
                NbtCompound nbt = new NbtCompound();
                NbtCompound components = new NbtCompound();
                nbt.put("components",components);
                NbtCompound entityData = new NbtCompound();
                if((new EntityDataObject(current)).getNbt()!=null)
                    entityData = (new EntityDataObject(current)).getNbt();
                components.put("entity_data",entityData);
                if(current.getType() == EntityType.ARMOR_STAND) {
                    nbt.putString("id","armor_stand");
                    entityData.putString("id","armor_stand");
                    if(mode == 1) {
                        entityData.remove("Brain");
                        entityData.remove("FallFlying");
                        entityData.remove("Health");
                    }
                }
                else {
                    nbt.putString("id","ender_dragon_spawn_egg");
                    entityData.putString("id",current.getType().toString().replace("entity.minecraft.",""));
                    components.put("item_name",NbtString.of("{\"text\":\"Custom "+
                    current.getType().getName().getString()+" Spawn Egg\",\"italic\":false}"));
                }
                if(mode == 1) {
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
        if(items.size()>0) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("id","bundle");
            NbtCompound components = new NbtCompound();
            nbt.put("components",components);
            NbtList bundle = new NbtList();
            components.put("bundle_contents",bundle);
            for(int i=0; i<items.size(); i++) {
                bundle.add(items.get(i));
            }
            ItemStack item = BlackMagick.itemFromNbt(nbt);
            if(items.size()==1)
                item = BlackMagick.itemFromNbt((NbtCompound)bundle.get(0));

            client.keyboard.setClipboard(BlackMagick.itemToNbtStorage(item).asString());

            if (client.player.getAbilities().creativeMode && !item.isEmpty()) {
                BlackMagick.setItemMain(item);
            }
        }
        this.resize(this.client,this.width,this.height);
    }

    protected void btnFindInvis() {
        if (client.player.getAbilities().creativeMode) {
            Iterator<Entity> entities = client.world.getEntities().iterator();
            while (entities.hasNext()) {
                Entity current = entities.next();
                if(current.getType() == EntityType.ARMOR_STAND) {
                    NbtCompound nbt = new NbtCompound();
                    if((new EntityDataObject(current)).getNbt()!=null)
                        nbt = (new EntityDataObject(current)).getNbt();
                    if(nbt.contains("Invisible",NbtElement.BYTE_TYPE) && nbt.get("Invisible").asString().equals("1b") 
                    && !(nbt.contains("CustomNameVisible",NbtElement.BYTE_TYPE) && nbt.get("CustomNameVisible").asString().equals("1b"))) {
                        if(((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(0))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(1))).isEmpty()
                        && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(2))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(3))).isEmpty()
                        && ((NbtCompound)(((NbtList)(nbt.get("HandItems"))).get(0))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("HandItems"))).get(1))).isEmpty())
                            reportInvis(current);
                    }
                }
                else if(current.getType() == EntityType.ITEM_FRAME || current.getType() == EntityType.GLOW_ITEM_FRAME) {
                    NbtCompound nbt = new NbtCompound();
                    if((new EntityDataObject(current)).getNbt()!=null)
                        nbt = (new EntityDataObject(current)).getNbt();
                    if(nbt.contains("Invisible") && nbt.get("Invisible").getType()==NbtElement.BYTE_TYPE
                    && nbt.get("Invisible").asString().equals("1b")) {
                        if(!nbt.contains("Item")) {
                            reportInvis(current);
                        }
                    }
                }
            }
        }
        this.resize(this.client,this.width,this.height);
    }

    private void reportInvis(Entity entity) {
        String json = "[{\"text\":\""+entity.getName().getString()+"\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":"
            +"{\"type\":\""+entity.getType().toString().replaceFirst("entity.","").replaceFirst("minecraft.","")+"\",\"id\":\""+entity.getUuidAsString()+"\"}},\"clickEvent\":"
            +"{\"action\":\"suggest_command\",\"value\":\"/tp "+entity.getBlockX()+" "+entity.getBlockY()+" "+entity.getBlockZ()+"\"}},{\"text\":\" \"},"
            +"{\"text\":\"["+entity.getBlockX()+", "+entity.getBlockY()+", "+entity.getBlockZ()+"]\"}]";
        if(BlackMagick.jsonFromString(json).isValid())
            client.player.sendMessage(BlackMagick.jsonFromString(json).text(),false);
        else
            client.player.sendMessage(Text.of(entity.getName().getString()+" ["+entity.getBlockX()+", "+entity.getBlockY()+", "+entity.getBlockZ()+"]"),false);
    }

    protected void btnDeathPos() {
        if(client.player.getLastDeathPos().isPresent()) {
            GlobalPos pos = client.player.getLastDeathPos().get();
            String coords = "Last death [X: "+pos.pos().getX()+", Y: "+pos.pos().getY()+", Z: "+pos.pos().getZ()+"] in "+pos.dimension().getValue().toString();
            client.player.sendMessage(Text.of(coords),false);
        }
        else {
            client.player.sendMessage(Text.of("No death pos recorded"),false);
        }
        this.resize(this.client,this.width,this.height);
    }

    protected void btnLookN() {
        client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),180f,0f);
        this.resize(this.client,this.width,this.height);
    }

    protected void btnLookR() {
        float yaw = client.player.getYaw() + 90;
        if(yaw>=360)
            yaw=yaw-360;
        client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),yaw,client.player.getPitch());
        this.resize(this.client,this.width,this.height);
    }

    protected void btnPano() {
        File location = new File(client.runDirectory.getAbsolutePath());
        client.takePanorama(location,1024,1024);
        this.resize(this.client,this.width,this.height);
    }

    protected void btnScreenshots() {
        File screenshots = new File(client.runDirectory.getAbsolutePath()+"\\screenshots");
        if(screenshots.exists() && screenshots.isDirectory())
            try{ Util.getOperatingSystem().open(screenshots); } catch(Exception e) {}
        this.resize(this.client,this.width,this.height);
    }

    protected void saveAll() {
        setTxtRando();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Hacks"), this.width / 2, y+11, 0xFFFFFF);
		context.drawItem(new ItemStack(Items.CRACKED_DEEPSLATE_BRICKS),x+20+2,y+22*2+1+2);
		context.drawItem(new ItemStack(Items.ENDER_DRAGON_SPAWN_EGG),x+20+2,y+22*3+1+2);
		context.drawItem(new ItemStack(Items.BARRIER),x+20+2,y+22*4+1+2);
		context.drawItem(new ItemStack(Items.SKELETON_SKULL),x+20+2,y+22*5+1+2);
		context.drawItem(new ItemStack(Items.FISHING_ROD),x+20+2+100+5,y+22*5+1+2);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context, delta, mouseX, mouseY, 0);
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        saveAll();
        this.init(client, width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            saveAll();
        }
        if (FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            if(!txtRando.isActive()) {
                saveAll();
                this.client.setScreen(null);
                return true;
            }
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if(!txtRando.isActive())
            setTxtRando();
    }

}
