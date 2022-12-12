package baphomethlabs.fortytwoedit.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import baphomethlabs.fortytwoedit.gui.framework.MagickScreen;

import java.awt.Desktop;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class FromWorld extends LightweightGuiDescription {
    public FromWorld() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //labels
        WLabel lblMenu = new WLabel(Text.of("Get Data"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);

        //back button
        WButton btnBack = new WButton(Text.of("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MagickScreen(new MagickGui()));
        });

        //print item data
        WButton btnGetItemData = new WButton(new ItemIcon(new ItemStack(Items.NAME_TAG)),Text.of("Copy Item Data"));
        btnGetItemData.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(!client.player.getMainHandStack().isEmpty()) {
                String itemData = client.player.getMainHandStack().getItem().toString();
                if(client.player.getMainHandStack().hasNbt())
                    itemData += client.player.getMainHandStack().getNbt().asString();
                client.player.sendMessage(Text.of(itemData),false);
                client.keyboard.setClipboard(itemData);
            }
        });

        //get entity
        WButton btnGetEntity = new WButton(new ItemIcon(new ItemStack(Items.ENDERMITE_SPAWN_EGG)),Text.of("Get Entity"));
        btnGetEntity.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
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
                            nbt.putString("id","bat_spawn_egg");
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
        });
        WButton btnGetEntityFull = new WButton(Text.of("Full Data"));
        btnGetEntityFull.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
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
                            nbt.putString("id","bat_spawn_egg");
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
        });

        //testfor invis armorstands
        //and invis item frames without an item
        WButton btnFindInvis = new WButton(new ItemIcon(new ItemStack(Items.ARMOR_STAND)),Text.of("Find Invis"));
        btnFindInvis.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if (client.player.getAbilities().creativeMode) {
                Iterator<Entity> entities = client.world.getEntities().iterator();
                while (entities.hasNext()) {
                    Entity current = entities.next();
                    if(current.getType() == EntityType.ARMOR_STAND) {
                        NbtCompound nbt = new NbtCompound();
                        if((new EntityDataObject(current)).getNbt()!=null)
                            nbt = (new EntityDataObject(current)).getNbt();
                        if(nbt.contains("Invisible") && nbt.get("Invisible").getType()==NbtType.BYTE
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
                        if(nbt.contains("Invisible") && nbt.get("Invisible").getType()==NbtType.BYTE
                        && nbt.get("Invisible").asString().equals("1b")) {
                            if(!nbt.contains("Item")) {
                                client.player.sendMessage(Text.of("Invisible item frame ["+
                                current.getBlockX()+","+current.getBlockY()+","+current.getBlockZ()+"]"),false);
                            }
                        }
                    }
                }
            }
        });

        //look button
        WButton btnLookN = new WButton(Text.of("Look N"));
        btnLookN.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                180f,client.player.getPitch());
        });
        WButton btnLookR = new WButton(Text.of("Rotate"));
        btnLookR.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            float yaw = client.player.getYaw() + 90;
            if(yaw>=360)
                yaw=yaw-360;
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                yaw,client.player.getPitch());
        });
        WButton btnLookH = new WButton(new ItemIcon(new ItemStack(Items.SPYGLASS)), Text.of("Look H"));
        btnLookH.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.refreshPositionAndAngles(client.player.getX(),client.player.getY(),client.player.getZ(),
                client.player.getYaw(),0f);
        });

        //panorama
        WButton btnPanorama = new WButton(new ItemIcon(new ItemStack(Items.GRASS_BLOCK)),Text.of("Take Panorama"));
        btnPanorama.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            File location = new File(client.runDirectory.getAbsolutePath());
            for(int i=0;i<6;i++) {
                File oldpan = new File(client.runDirectory.getAbsolutePath()+"/screenshots/panorama_"+i+".png");
                if(oldpan.exists())
                    oldpan.delete();
            }
            client.takePanorama(location,1024,1024);
        });
        WButton btnScreenshots = new WButton(Text.of("View"));
        btnScreenshots.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            File screenshots = new File(client.runDirectory.getAbsolutePath()+"/screenshots");
            if(screenshots.exists() && screenshots.isDirectory())
                try{ Desktop.getDesktop().open(screenshots); } catch(Exception e) {}
        });

        //compare hand items
        WButton btnCompareItems = new WButton(new ItemIcon(new ItemStack(Items.SHULKER_BOX)),Text.of("Compare Items"));
        WLabel lblCompareItems = new WLabel(Text.of(""));
        btnCompareItems.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(!client.player.getMainHandStack().isEmpty() && !client.player.getOffHandStack().isEmpty()) {
                boolean equal = ItemStack.areNbtEqual(client.player.getMainHandStack(),client.player.getOffHandStack());
                if(equal)
                    lblCompareItems.setText(Text.of("\u2611"));
                else
                    lblCompareItems.setText(Text.of("\u2612"));
            }
            else
                lblCompareItems.setText(Text.of(""));
        });

        //add items
        root.add(btnBack,5,5,40,20);
        root.add(lblMenu,120,11,0,0);
        root.add(btnGetItemData,20,44+1,120,20);
        root.add(btnGetEntity,20,66+1,80,20);
        root.add(btnGetEntityFull,20+80+5,66+1,60,20);
        root.add(btnFindInvis,20,88+1,80,20);
        root.add(btnLookH,20,22*5+1,80,20);
        root.add(btnLookN,20+80+5,22*5+1,40,20);
        root.add(btnLookR,20+80+5+40+5,22*5+1,40,20);
        root.add(btnPanorama,20,22*6+1,120,20);
        root.add(btnScreenshots,20+120+5,22*6+1,40,20);
        root.add(btnCompareItems,20,22*7+1,120,20);
        root.add(lblCompareItems,20+120+5,22*7+1+6,60,20);
        
    }
}
