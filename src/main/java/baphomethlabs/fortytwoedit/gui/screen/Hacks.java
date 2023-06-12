package baphomethlabs.fortytwoedit.gui.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
        }));
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
        this.addDrawableChild(ButtonWidget.builder(Text.of("Get Entity"), button -> this.btnGetEntity()).dimensions(x+20,y+22*3+1,80,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Full Data"), button -> this.btnGetEntityFull()).dimensions(x+20+80+5,y+22*3+1,60,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Xray [On]"), Text.literal("Xray [Off]")).initially(FortytwoEdit.seeInvis).omitKeyText().build(x+20,y+22*4+1,100,20, Text.of(""), (button, trackOutput) -> {
            client.worldRenderer.reload();
            FortytwoEdit.seeInvis = !FortytwoEdit.seeInvis;
            FortytwoEdit.xrayEntity = FortytwoEdit.seeInvis;
            this.resize(this.client,this.width,this.height);
        }));
        this.addDrawableChild(ButtonWidget.builder(Text.of("Find Invis Entities"), button -> this.btnFindInvis()).dimensions(x+20+100+5,y+22*4+1,100,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Death Pos"), button -> this.btnDeathPos()).dimensions(x+20,y+22*5+1,100,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Look N"), button -> this.btnLookN()).dimensions(x+20,y+22*7+1,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Rotate"), button -> this.btnLookR()).dimensions(x+20+40+5,y+22*7+1,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Pano"), button -> this.btnPano()).dimensions(x+20+40+5+40+5,y+22*7+1,40,20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("View Pano"), button -> this.btnScreenshots()).dimensions(x+20+120+15,y+22*7+1,60,20).build());
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

    protected void btnGetEntity() {
        if (client.player.getAbilities().creativeMode) {
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
                    nbt.putInt("Count",1);
                    NbtCompound nbtTag = new NbtCompound();
                    nbt.put("tag",nbtTag);
                    NbtCompound nbtEntityTag = new NbtCompound();
                    if((new EntityDataObject(current)).getNbt()!=null)
                        nbtEntityTag = (new EntityDataObject(current)).getNbt();
                    nbtTag.put("EntityTag",nbtEntityTag);
                    if(current.getType() == EntityType.ARMOR_STAND) {
                        nbt.putString("id","armor_stand");
                        nbtEntityTag.remove("Attributes");
                        nbtEntityTag.remove("Brain");
                        nbtEntityTag.remove("FallFlying");
                        nbtEntityTag.remove("Health");
                    }
                    else {
                        nbt.putString("id","ender_dragon_spawn_egg");
                        nbtEntityTag.putString("id",current.getType().toString().replace("entity.minecraft.",""));
                        NbtCompound nbtDisplay = new NbtCompound();
                        nbtTag.put("display",nbtDisplay);
                        NbtList nbtLore = new NbtList();
                        nbtDisplay.put("Lore",nbtLore);
                        nbtLore.add(NbtString.of("{\"text\":\""+
                            current.getType().toString().replace("entity.minecraft.","")+"\",\"color\":\"gray\",\"italic\":false}"));
                    }
                    nbtEntityTag.remove("Air");
                    nbtEntityTag.remove("FallDistance");
                    nbtEntityTag.remove("Fire");
                    nbtEntityTag.remove("Motion");
                    nbtEntityTag.remove("OnGround");
                    nbtEntityTag.remove("PortalCooldown");
                    nbtEntityTag.remove("Pos");
                    nbtEntityTag.remove("Rotation");
                    nbtEntityTag.remove("TicksFrozen");
                    nbtEntityTag.remove("UUID");
                    nbtEntityTag.remove("AbsorptionAmount");
                    nbtEntityTag.remove("DeathTime");
                    nbtEntityTag.remove("HurtByTimestamp");
                    nbtEntityTag.remove("HurtTime");
                    items.add(nbt);
                }
            }
            if(items.size()>0) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","bundle");
                nbt.putInt("Count",1);
                NbtCompound nbtTag = new NbtCompound();
                nbt.put("tag",nbtTag);
                NbtList nbtItems = new NbtList();
                nbtTag.put("Items",nbtItems);
                for(int i=0; i<items.size(); i++) {
                    nbtItems.add(items.get(i));
                }
                ItemStack item = ItemStack.fromNbt(nbt);
                if(items.size()==1)
                    item = ItemStack.fromNbt((NbtCompound)nbtItems.get(0));
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
            }
        }
        this.resize(this.client,this.width,this.height);
    }

    protected void btnGetEntityFull() {
        if (client.player.getAbilities().creativeMode) {
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
                    nbt.putInt("Count",1);
                    NbtCompound nbtTag = new NbtCompound();
                    nbt.put("tag",nbtTag);
                    NbtCompound nbtEntityTag = new NbtCompound();
                    if((new EntityDataObject(current)).getNbt()!=null)
                        nbtEntityTag = (new EntityDataObject(current)).getNbt();
                    nbtTag.put("EntityTag",nbtEntityTag);
                    if(current.getType() == EntityType.ARMOR_STAND) {
                        nbt.putString("id","armor_stand");
                    }
                    else {
                        nbt.putString("id","ender_dragon_spawn_egg");
                        nbtEntityTag.putString("id",current.getType().toString().replace("entity.minecraft.",""));
                        NbtCompound nbtDisplay = new NbtCompound();
                        nbtTag.put("display",nbtDisplay);
                        NbtList nbtLore = new NbtList();
                        nbtDisplay.put("Lore",nbtLore);
                        nbtLore.add(NbtString.of("{\"text\":\""+
                            current.getType().toString().replace("entity.minecraft.","")+"\",\"color\":\"gray\",\"italic\":false}"));
                    }
                    items.add(nbt);
                }
            }
            if(items.size()>0) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","bundle");
                nbt.putInt("Count",1);
                NbtCompound nbtTag = new NbtCompound();
                nbt.put("tag",nbtTag);
                NbtList nbtItems = new NbtList();
                nbtTag.put("Items",nbtItems);
                for(int i=0; i<items.size(); i++) {
                    nbtItems.add(items.get(i));
                }
                ItemStack item = ItemStack.fromNbt(nbt);
                if(items.size()==1)
                    item = ItemStack.fromNbt((NbtCompound)nbtItems.get(0));
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
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
                    if(nbt.contains("Invisible") && nbt.get("Invisible").getType()==NbtElement.BYTE_TYPE
                    && nbt.get("Invisible").asString().equals("1b")) {
                        if(((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(0))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(1))).isEmpty()
                        && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(2))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("ArmorItems"))).get(3))).isEmpty()
                        && ((NbtCompound)(((NbtList)(nbt.get("HandItems"))).get(0))).isEmpty() && ((NbtCompound)(((NbtList)(nbt.get("HandItems"))).get(1))).isEmpty())
                            client.player.sendMessage(Text.of("Invisible armor stand ["+
                                current.getBlockX()+","+current.getBlockY()+","+current.getBlockZ()+"]"),false);
                    }
                }
                else if(current.getType() == EntityType.ITEM_FRAME || current.getType() == EntityType.GLOW_ITEM_FRAME) {
                    NbtCompound nbt = new NbtCompound();
                    if((new EntityDataObject(current)).getNbt()!=null)
                        nbt = (new EntityDataObject(current)).getNbt();
                    if(nbt.contains("Invisible") && nbt.get("Invisible").getType()==NbtElement.BYTE_TYPE
                    && nbt.get("Invisible").asString().equals("1b")) {
                        if(!nbt.contains("Item")) {
                            client.player.sendMessage(Text.of("Invisible item frame ["+
                            current.getBlockX()+","+current.getBlockY()+","+current.getBlockZ()+"]"),false);
                        }
                    }
                }
            }
        }
        this.resize(this.client,this.width,this.height);
    }

    protected void btnDeathPos() {
        if(client.player.getLastDeathPos().isPresent()) {
            GlobalPos pos = client.player.getLastDeathPos().get();
            String coords = "Last death [X: "+pos.getPos().getX()+", Y: "+pos.getPos().getY()+", Z: "+pos.getPos().getZ()+"] in "+pos.getDimension().getValue().toString();
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
        this.renderBackground(context);
        this.drawBackground(context, delta, mouseX, mouseY);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Hacks"), this.width / 2, y+11, 0xFFFFFF);
		context.drawItem(new ItemStack(Items.CRACKED_DEEPSLATE_BRICKS),x+20+2,y+22*2+1+2);
		context.drawItem(new ItemStack(Items.ENDER_DRAGON_SPAWN_EGG),x+20+2,y+22*3+1+2);
		context.drawItem(new ItemStack(Items.BARRIER),x+20+2,y+22*4+1+2);
		context.drawItem(new ItemStack(Items.SKELETON_SKULL),x+20+2,y+22*5+1+2);
        super.render(context, mouseX, mouseY, delta);
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
        if (keyCode == KeyBindingHelper.getBoundKeyOf(FortytwoEdit.magickGuiKey).getCode() || keyCode == KeyBindingHelper.getBoundKeyOf(client.options.inventoryKey).getCode()) {
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
