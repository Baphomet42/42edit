package baphomethlabs.fortytwoedit.gui.legacy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import baphomethlabs.fortytwoedit.BlackMagick;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WItem;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.WTabPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemBuilder extends LightweightGuiDescription {

    public static WItem handItem;
    public static ItemStack[][] saved;
    private static WButton[][] tabSavedBtnGrid;
    private static boolean tabSavedMode;

    public ItemBuilder() {
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(12*20,9*22);

        //itembuilder main
        WLabel lblMenu = new WLabel(Text.of("42edit"));
        lblMenu.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WButton btnBack = new WButton(Text.of("Back"));
        btnBack.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new baphomethlabs.fortytwoedit.gui.MagickGui());
        });
        handItem = new WItem(new ItemStack(Items.AIR)){
            public void addTooltip(TooltipBuilder tooltip) {
                MinecraftClient client = MinecraftClient.getInstance();
                String itemData = "";
                if(client.player.getMainHandStack().hasNbt()) {
                    itemData += client.player.getMainHandStack().getNbt().asString();
                }
                //remove SkullOwner Properties
                while(itemData.contains("Properties:{textures:")) {
                    int propertiesIndex = itemData.indexOf("Properties:{textures:");
                    String firstHalf = itemData.substring(0,propertiesIndex)+"Properties:{...}";
                    String secondHalf = itemData.substring(propertiesIndex+21);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('{');
                        nextCloseBracket = secondHalf.indexOf('}');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                //remove SkullOwner UUID
                while(itemData.contains("Id:[I;")) {
                    int idIndex = itemData.indexOf("Id:[I;");
                    String firstHalf = itemData.substring(0,idIndex)+"Id:[...]";
                    String secondHalf = itemData.substring(idIndex+6);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('[');
                        nextCloseBracket = secondHalf.indexOf(']');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                //remove SkullOwnerOrig
                while(itemData.contains("SkullOwnerOrig:[I;")) {
                    int origIndex = itemData.indexOf("SkullOwnerOrig:[I;");
                    String firstHalf = itemData.substring(0,origIndex)+"\u00a74\u26a0\u00a7rSkullOwnerOrig:[...]";
                    String secondHalf = itemData.substring(origIndex+18);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('[');
                        nextCloseBracket = secondHalf.indexOf(']');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                int tooltipLines = 0;
                while(itemData.length()>0 && tooltipLines<42) {
                    int endIndex = 48;
                    if(itemData.length()<endIndex)
                        endIndex = itemData.length();
                    tooltip.add(Text.of(itemData.substring(0,endIndex)));
                    tooltipLines++;
                    if(itemData.length()>endIndex) {
                        itemData = itemData.substring(endIndex);
                        if(tooltipLines==42)
                            tooltip.add(Text.of("..."));
                    }
                    else
                        itemData = "";
                }
                updateItem();
            }
        };
        updateItem();
        //
        WButton btnSlotLeft = new WButton(Text.of("<"));
        btnSlotLeft.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            int slot = client.player.getInventory().selectedSlot;
            slot--;
            if(slot<0)
                slot = 8;
            client.player.getInventory().selectedSlot = slot;
            updateItem();
        });
        WButton btnSlotRight = new WButton(Text.of(">"));
        btnSlotRight.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            int slot = client.player.getInventory().selectedSlot;
            slot++;
            if(slot>8)
                slot = 0;
            client.player.getInventory().selectedSlot = slot;
            updateItem();
        });
        WButton btnSlotOff = new WButton(Text.of("c"));
        btnSlotOff.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getAbilities().creativeMode) {
                ItemStack mainhand = null;
                ItemStack offhand = null;
                if(!client.player.getMainHandStack().isEmpty())
                    mainhand=client.player.getMainHandStack().copy();
                if(!client.player.getOffHandStack().isEmpty())
                    offhand=client.player.getOffHandStack().copy();
                if(mainhand!=null && offhand!=null) {
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    updateItem(offhand);
                }
                else if(mainhand!=null && offhand==null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(mainhand, 45);
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    updateItem(ItemStack.fromNbt(nbt));
                }
                else if(mainhand==null && offhand!=null) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id","air");
                    client.interactionManager.clickCreativeStack(ItemStack.fromNbt(nbt), 45);
                    client.interactionManager.clickCreativeStack(offhand, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    updateItem(offhand);
                }
            }
        });
        WButton btnSlotCopyToOff = new WButton(Text.of("c*"));
        btnSlotCopyToOff.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getAbilities().creativeMode) {
                if(!client.player.getMainHandStack().isEmpty()) {
                    ItemStack item=client.player.getMainHandStack().copy();
                    client.interactionManager.clickCreativeStack(item, 45);
                    client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        });
        WButton btnSlotThrow = new WButton(Text.of("Q"));
        btnSlotThrow.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            client.player.dropSelectedItem(true);
            client.player.playerScreenHandler.sendContentUpdates();
            updateItem();
        });
        WButton btnSlotThrowCopy = new WButton(Text.of("Q*"));
        btnSlotThrowCopy.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getAbilities().creativeMode) {
                ItemStack item = client.player.getMainHandStack().copy();
                client.player.dropSelectedItem(true);
                client.player.playerScreenHandler.sendContentUpdates();
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
                updateItem(item);
            }
        });

        //tabs
        WTabPanel tabs = new WTabPanel();
        WPlainPanel tabGeneral = new WPlainPanel();
        tabGeneral.setSize(12*20,9*22-30-30);
        WPlainPanel tabDisplay = new WPlainPanel();
        WPlainPanel tabEnch = new WPlainPanel();
        WPlainPanel tabBlock = new WPlainPanel();
        WPlainPanel tabEntity = new WPlainPanel();
        WPlainPanel tabMisc = new WPlainPanel();
        WPlainPanel tabCustom = new WPlainPanel();
        WPlainPanel tabSaved = new WPlainPanel();

        //tabGeneral
        WPlainPanel tabGeneralScroll = new WPlainPanel();
        WLabel tabGeneralLblBlank = new WLabel(Text.of(""));
        //
        WButton tabGeneralBtnId = new WButton(Text.of("id"));
        WTextField tabGeneralTxtId = new WTextField();
        tabGeneralTxtId.setMaxLength(64);
        tabGeneralBtnId.setOnClick(() -> {
            BlackMagick.setId(tabGeneralTxtId.getText());
        });
        //
        WButton tabGeneralBtnCount = new WButton(Text.of("Count"));
        WTextField tabGeneralTxtCount = new WTextField();
        tabGeneralTxtCount.setMaxLength(2);
        tabGeneralBtnCount.setOnClick(() -> {
            BlackMagick.setCount(tabGeneralTxtCount.getText());
        });
        //
        WButton tabGeneralBtnName = new WButton(Text.of("Name"));
        WTextField tabGeneralTxtName = new WTextField();
        tabGeneralTxtName.setMaxLength(1024);
        tabGeneralBtnName.setOnClick(() -> {
            String inp = tabGeneralTxtName.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                BlackMagick.setNbt(null,"display/Name",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"display/Name");
        });
        //
        WButton tabGeneralBtnHideFlags = new WButton(Text.of("HideFlags"));
        WLabel tabGeneralLblHideFlags = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("1-Ench     2-Attribute     4-Unbreakable"),
                    Text.of("8-CanDestroy     16-CanPlaceOn     32-Various"),
                    Text.of("64-Armor Dye     128-Armor Trims"),
                    Text.of("255-Everything"));
            }
        };
        tabGeneralLblHideFlags.setHorizontalAlignment(HorizontalAlignment.CENTER);
        tabGeneralLblHideFlags.setVerticalAlignment(VerticalAlignment.CENTER);
        WTextField tabGeneralTxtHideFlags = new WTextField();
        tabGeneralTxtHideFlags.setMaxLength(3);
        tabGeneralBtnHideFlags.setOnClick(() -> {
            String inp = tabGeneralTxtHideFlags.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"HideFlags",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"HideFlags");
        });
        //
        WLabel tabGeneralLblTool = new WLabel(Text.of("Tools / Adventure Mode"));
        tabGeneralLblTool.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabGeneralBtnUnbreakable = new WButton(Text.of("Unbreakable"));
        WTextField tabGeneralTxtUnbreakable = new WTextField();
        tabGeneralTxtUnbreakable.setMaxLength(5);
        tabGeneralBtnUnbreakable.setOnClick(() -> {
            String inp = tabGeneralTxtUnbreakable.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"Unbreakable",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"Unbreakable");
        });
        //
        WButton tabGeneralBtnDamage = new WButton(Text.of("Damage"));
        WLabel tabGeneralLblDamage = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("Tools-"),
                Text.of("Netherite-2031  Diamond-1561  Iron-250"),
                Text.of("Stone-131  Wood-59  Gold-32"),Text.of(""),Text.of("Armor (H/C/L/B)-"),
                Text.of("Netherite-407,592,555,481  Diamond-363,528,495,429"),
                Text.of("Iron/Chain-165,240,225,195  Gold-77,112,105,91"),
                Text.of("Leather-55,80,75,65  Turtle-275  Elytra-432"),Text.of(""),Text.of("Other-"),
                Text.of("Bow-384  Crossbow-326  Trident-250  Shield-336"),
                Text.of("Flint-64  Shears-238  Fishing-64"),
                Text.of("CarrotStick-25  FungusStick-100"));
            }
        };
        tabGeneralLblDamage.setHorizontalAlignment(HorizontalAlignment.CENTER);
        tabGeneralLblDamage.setVerticalAlignment(VerticalAlignment.CENTER);
        WTextField tabGeneralTxtDamage = new WTextField();
        tabGeneralTxtDamage.setMaxLength(5);
        tabGeneralBtnDamage.setOnClick(() -> {
            String inp = tabGeneralTxtDamage.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"Damage",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"Damage");
        });
        //
        WButton tabGeneralBtnModel = new WButton(Text.of("Custom Model"));
        WTextField tabGeneralTxtModel = new WTextField();
        tabGeneralTxtModel.setMaxLength(10);
        tabGeneralBtnModel.setOnClick(() -> {
            String inp = tabGeneralTxtModel.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"CustomModelData",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"CustomModelData");
        });
        //
        WLabel tabGeneralLblBlock = new WLabel(Text.of("Block:"));
        WButton tabGeneralBtnCanPlaceOn = new WButton(Text.of("CanPlaceOn"));
        WButton tabGeneralBtnCanDestroy = new WButton(Text.of("CanDestroy"));
        WTextField tabGeneralTxtBlock = new WTextField();
        tabGeneralTxtBlock.setMaxLength(64);
        tabGeneralBtnCanPlaceOn.setOnClick(() -> {
            String inp = tabGeneralTxtBlock.getText();
            NbtElement inpEl;
            NbtList list;
            if(!inp.equals("")) {
                inp = "\""+inp+"\"";
                inpEl = BlackMagick.elementFromString(inp);
                if(BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn")!=null &&
                    BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn").getType()==NbtElement.LIST_TYPE) {
                        list = (NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/CanPlaceOn");
                        if(list.size()==0) {
                            BlackMagick.setNbt(null,"CanPlaceOn/0:",inpEl,NbtElement.STRING_TYPE);
                        }
                        else {
                            if(list.contains(inpEl)) {
                                int index = list.indexOf(inpEl);
                                BlackMagick.removeNbt(null,"CanPlaceOn/"+index+":");
                            }
                            else {
                                int index = list.size();
                                BlackMagick.setNbt(null,"CanPlaceOn/"+index+":",inpEl);
                            }
                        }
                }
                else
                    BlackMagick.setNbt(null,"CanPlaceOn/0:",inpEl,NbtElement.STRING_TYPE);
            }
            else if(inp.equals(""))
                BlackMagick.removeNbt(null,"CanPlaceOn");
        });
        tabGeneralBtnCanDestroy.setOnClick(() -> {
            String inp = tabGeneralTxtBlock.getText();
            NbtElement inpEl;
            NbtList list;
            if(!inp.equals("")) {
                inp = "\""+inp+"\"";
                inpEl = BlackMagick.elementFromString(inp);
                if(BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy")!=null &&
                    BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy").getType()==NbtElement.LIST_TYPE) {
                        list = (NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/CanDestroy");
                        if(list.size()==0) {
                            BlackMagick.setNbt(null,"CanDestroy/0:",inpEl,NbtElement.STRING_TYPE);
                        }
                        else {
                            if(list.contains(inpEl)) {
                                int index = list.indexOf(inpEl);
                                BlackMagick.removeNbt(null,"CanDestroy/"+index+":");
                            }
                            else {
                                int index = list.size();
                                BlackMagick.setNbt(null,"CanDestroy/"+index+":",inpEl);
                            }
                        }
                }
                else
                    BlackMagick.setNbt(null,"CanDestroy/0:",inpEl,NbtElement.STRING_TYPE);
            }
            else if(inp.equals(""))
                BlackMagick.removeNbt(null,"CanDestroy");
        });
        //
        tabGeneralScroll.add(tabGeneralBtnId,5,5+1,40,20);
        tabGeneralScroll.add(tabGeneralTxtId,50,5+1,230-8-50-13-10,22);
        tabGeneralScroll.add(tabGeneralBtnCount,5,5+1+22,40,20);
        tabGeneralScroll.add(tabGeneralTxtCount,50,5+1+22,230-8-50-13-10,22);
        tabGeneralScroll.add(tabGeneralBtnName,5,5+1+22*2,40,20);
        tabGeneralScroll.add(tabGeneralTxtName,50,5+1+22*2,230-8-50-13-10,22);
        tabGeneralScroll.add(tabGeneralBtnHideFlags,5,5+1+22*3,60,20);
        tabGeneralScroll.add(tabGeneralTxtHideFlags,50+20,5+1+22*3,230-8-50-13-10-20,22);
        tabGeneralScroll.add(tabGeneralLblHideFlags,50+20+230-8-50-13-10-20+5,5+1+22*3+6,13,7);
        tabGeneralScroll.add(tabGeneralLblTool,120,5+7+22*4,0,0);
        tabGeneralScroll.add(tabGeneralBtnUnbreakable,5,5+1+22*5,80,20);
        tabGeneralScroll.add(tabGeneralTxtUnbreakable,50+40,5+1+22*5,230-8-50-13-10-40,22);
        tabGeneralScroll.add(tabGeneralBtnDamage,5,5+1+22*6,60,20);
        tabGeneralScroll.add(tabGeneralTxtDamage,50+20,5+1+22*6,230-8-50-13-10-20,22);
        tabGeneralScroll.add(tabGeneralLblDamage,50+20+230-8-50-13-10-20+5,5+1+22*6+6,13,7);
        tabGeneralScroll.add(tabGeneralBtnModel,5,5+1+22*7,80,20);
        tabGeneralScroll.add(tabGeneralTxtModel,50+40,5+1+22*7,230-8-50-13-10-40,22);
        tabGeneralScroll.add(tabGeneralLblBlock,5+3,5+7+22*9,40,20);
        tabGeneralScroll.add(tabGeneralTxtBlock,50,5+1+22*9,230-8-50-13-10,22);
        tabGeneralScroll.add(tabGeneralBtnCanPlaceOn,5,5+1+22*10,80,20);
        tabGeneralScroll.add(tabGeneralBtnCanDestroy,5+80+5,5+1+22*10,80,20);
        tabGeneralScroll.add(tabGeneralLblBlank,0,5+1+22*10,0,22+5+1-2);
        WScrollPanel tabGeneralScrollPanel = new WScrollPanel(tabGeneralScroll);
        tabGeneralScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabGeneralScrollPanel.setScrollingVertically(TriState.TRUE);
        tabGeneral.add(tabGeneralScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tabDisplay
        WPlainPanel tabDisplayScroll = new WPlainPanel();
        WLabel tabDisplayLblBlank = new WLabel(Text.of(""));
        //
        WLabel tabDisplayLblJsonTitle = new WLabel(Text.of("JSON"));
        tabDisplayLblJsonTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabDisplayBtnSetJson = new WButton(Text.of("Set"));
        WButton tabDisplayBtnGetJson = new WButton(Text.of("Get"));
        WTextField tabDisplayTxtJsonIndex = new WTextField();
        tabDisplayTxtJsonIndex.setMaxLength(2);
        WTextField tabDisplayTxtJsonPath = new WTextField();
        tabDisplayTxtJsonPath.setMaxLength(1024);
        WLabel tabDisplayLblJsonHelp = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                    Text.of("font"),
                    Text.of("default  uniform  alt  illageralt"),
                    Text.of(""),
                    Text.of("clickEvent (action | value)"),
                    Text.of("  action"),
                    Text.of("    open_url  open_file  run_command"),
                    Text.of("    suggest_command change_page copy_to_clipboard"),
                    Text.of(""),
                    Text.of("hoverEvent (action | contents)"),
                    Text.of("  action"),
                    Text.of("    show_text  show_item  show_entity"),
                    Text.of("  contents"),
                    Text.of("    (show_text) json"),
                    Text.of("    (show_item) {id: ,count: ,tag: }"),
                    Text.of("    (show_entity) {name: ,type: ,id:(UUID)}"));
            }
        };
        WTextField tabDisplayTxtJson = new WTextField();
        tabDisplayTxtJson.setMaxLength(4096);
        WLabel tabDisplayLblJson = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                String json = tabDisplayTxtJson.getText();
                while(json.length()>0) {
                    int endIndex = 48;
                    if(json.length()<endIndex)
                        endIndex = json.length();
                    tooltip.add(Text.of(json.substring(0,endIndex)));
                    if(json.length()>endIndex)
                        json = json.substring(endIndex);
                    else
                        json = "";
                }
            }
        };
        tabDisplayBtnSetJson.setOnClick(() -> {
            String path = tabDisplayTxtJsonPath.getText();
            String inpIndex = tabDisplayTxtJsonIndex.getText();
            String json = tabDisplayTxtJson.getText();
            if(!path.equals("") && !inpIndex.equals("")) {
                int index;
                try {
                    index = Integer.parseInt(inpIndex);
                    if(index<0 || json.equals(""))
                        return;
                } catch(NumberFormatException e) {
                    return;
                }
                NbtElement pathElement = BlackMagick.getNbtFromPath(null,"0:/tag/"+path);
                if(pathElement!=null && pathElement.getType()==NbtElement.LIST_TYPE) {
                    NbtList jsonList = (NbtList)pathElement;
                    if(jsonList.size()>0 && jsonList.get(0).getType()==NbtElement.STRING_TYPE) {
                        while(index>jsonList.size()) {
                            jsonList.add(NbtString.of("{\"text\":\"\"}"));
                        }
                        jsonList.add(index,NbtString.of(json));
                        BlackMagick.setNbt(null,path,jsonList);
                    }
                    else {
                        jsonList = new NbtList();
                        while(index>jsonList.size())
                            jsonList.add(NbtString.of("{\"text\":\"\"}"));
                        jsonList.add(index,NbtString.of(json));
                        BlackMagick.setNbt(null,path,jsonList);
                    }
                }
                else {
                    NbtList jsonList = new NbtList();
                    while(index>jsonList.size())
                        jsonList.add(NbtString.of("{\"text\":\"\"}"));
                    jsonList.add(index,NbtString.of(json));
                    BlackMagick.setNbt(null,path,jsonList);
                }
            }
            else if(!path.equals("")) {
                if(json.equals(""))
                    BlackMagick.removeNbt(null,path);
                else
                    BlackMagick.setNbt(null,path,NbtString.of(json));
            }
        });
        tabDisplayBtnGetJson.setOnClick(() -> {
            NbtElement jsonElement = BlackMagick.getNbtFromPath(null,"0:/tag/"+tabDisplayTxtJsonPath.getText());
            if(jsonElement!=null && jsonElement.getType()==NbtElement.STRING_TYPE)
                tabDisplayTxtJson.setText(((NbtString)jsonElement).asString());
        });
        //
        WLabel tabDisplayLblColors = new WLabel(Text.of("Colors"));
        tabDisplayLblColors.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WButton tabDisplayBtnColor = new WButton(Text.of("Convert RGB"));
        WLabel tabDisplayLblColor = new WLabel(Text.of("\u2588"));
        WLabel tabDisplayLblColorHelp = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("\u00a7f0 black #000000   \u00a711 dark_blue #0000AA"),
                Text.of("\u00a722 dark_green #00AA00   \u00a733 dark_aqua #00AAAA"),
                Text.of("\u00a744 dark_red #AA0000   \u00a755 dark_purple #AA00AA"),
                Text.of("\u00a766 gold #FFAA00   \u00a777 gray #AAAAAA"),
                Text.of("\u00a788 dark_gray #555555   \u00a799 blue #5555FF"),
                Text.of("\u00a7aa green #55FF55   \u00a7bb aqua #55FFFF"),
                Text.of("\u00a7cc red #FF5555   \u00a7dd light_purple #FF55FF"),
                Text.of("\u00a7ee yellow #FFFF55   \u00a7ff white #FFFFFF"),
                Text.of("\u00a7fr reset"),
                Text.of(""),
                Text.of("\u00a77k obfuscated \u00a7kobfuscated"),
                Text.of("\u00a77l bold \u00a7lbold"),
                Text.of("\u00a77m strikethrough \u00a7mstrikethrough"),
                Text.of("\u00a77n underlined \u00a7nunderlined"),
                Text.of("\u00a77o italic \u00a7oitalic"));
            }
        };
        WTextField tabDisplayTxtColorRGB = new WTextField();
        tabDisplayTxtColorRGB.setMaxLength(11);
        WTextField tabDisplayTxtColorHex = new WTextField();
        tabDisplayTxtColorHex.setMaxLength(7);
        WTextField tabDisplayTxtColorDec = new WTextField();
        tabDisplayTxtColorDec.setMaxLength(8);
        tabDisplayBtnColor.setOnClick(() -> {
            String inp = tabDisplayTxtColorRGB.getText();
            if(inp.equals("")) {
                tabDisplayTxtColorHex.setText("");
                tabDisplayTxtColorDec.setText("");
            }
            else {
                int[] rgb = new int[3];
                inp += ",,,,";
                try {
                    for(int i=0; i<3; i++) {
                        if(!inp.contains(","))
                            return;
                        rgb[i]=Integer.parseInt(inp.substring(0,inp.indexOf(",")));
                        if(rgb[i]<0 || rgb[i]>255)
                            return;
                        inp = inp.substring(inp.indexOf(",")+1);
                    }
                } catch(NumberFormatException e) {
                    return;
                }
                String hex = "";
                for(int i=0;i<3;i++) {
                    String current = Integer.toHexString(rgb[i]);
                    if(current.length()==1)
                        current= "0"+current;
                    hex+=current;
                }
                int dec = 256*256*rgb[0] + 256*rgb[1] + rgb[2];
                tabDisplayTxtColorHex.setText("#"+hex);
                tabDisplayTxtColorDec.setText(""+dec);
                tabDisplayLblColor.setColor(dec);
            }
        });
        //
        WButton tabDisplayBtnArmor = new WButton(Text.of("Armor Color"));
        tabDisplayBtnArmor.setOnClick(() -> {
            if(tabDisplayTxtColorDec.getText().equals(""))
                BlackMagick.removeNbt(null,"display/color");
            else
                try {
                    BlackMagick.setNbt(null,"display/color",NbtInt.of(Integer.parseInt(tabDisplayTxtColorDec.getText())));
                } catch(NumberFormatException e) {}
        });
        WButton tabDisplayBtnMap = new WButton(Text.of("Map Color"));
        tabDisplayBtnMap.setOnClick(() -> {
            if(tabDisplayTxtColorDec.getText().equals(""))
                BlackMagick.removeNbt(null,"display/MapColor");
            else
                try {
                    BlackMagick.setNbt(null,"display/MapColor",NbtInt.of(Integer.parseInt(tabDisplayTxtColorDec.getText())));
                } catch(NumberFormatException e) {}
        });
        WButton tabDisplayBtnPotion = new WButton(Text.of("Potion Color"));
        tabDisplayBtnPotion.setOnClick(() -> {
            if(tabDisplayTxtColorDec.getText().equals(""))
                BlackMagick.removeNbt(null,"CustomPotionColor");
            else
                try {
                    BlackMagick.setNbt(null,"CustomPotionColor",NbtInt.of(Integer.parseInt(tabDisplayTxtColorDec.getText())));
                } catch(NumberFormatException e) {}
        });
        //
        WButton tabDisplayBtnLabs = new WButton(Text.of("BaphomethLabs"));
        tabDisplayBtnLabs.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(!client.player.getMainHandStack().isEmpty()) {
                NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/display/Lore");
                if(element!=null && element.getType()==NbtElement.LIST_TYPE) {
                    NbtList jsonList = (NbtList)element;
                    if(jsonList.size()>0 && jsonList.get(0).getType()==NbtElement.STRING_TYPE) {
                        jsonList.add(NbtString.of("{\"text\":\"\"}"));
                        jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                        BlackMagick.setNbt(null,"display/Lore",jsonList);
                    }
                    else {
                        jsonList = new NbtList();
                        jsonList.add(NbtString.of("{\"text\":\"\"}"));
                        jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                        BlackMagick.setNbt(null,"display/Lore",jsonList);
                    }
                }
                else {
                    NbtList jsonList = new NbtList();
                    jsonList.add(NbtString.of("{\"text\":\"\"}"));
                    jsonList.add(NbtString.of("{\"text\":\"BaphomethLabs\",\"color\":\"gold\"}"));
                    BlackMagick.setNbt(null,"display/Lore",jsonList);
                }
            }
        });
        WButton tabDisplayBtnWatermark = new WButton(Text.of("Watermark"));
        tabDisplayBtnWatermark.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/\u00a76\u00a7oBaphomethLabs\u00a7r")!=null)
                BlackMagick.removeNbt(null,"\u00a76\u00a7oBaphomethLabs\u00a7r");
            else
                BlackMagick.setNbt(null,"\u00a76\u00a7oBaphomethLabs\u00a7r",NbtInt.of(42));
        });
        //
        tabDisplayScroll.add(tabDisplayLblJsonTitle,120,5+7,0,0);
        tabDisplayScroll.add(tabDisplayBtnSetJson,5,5+1+22,40,20);
        tabDisplayScroll.add(tabDisplayBtnGetJson,5+42,5+1+22,40,20);
        tabDisplayScroll.add(tabDisplayTxtJsonPath,50+40+2,5+1+22,230-8-50-13-10-40-2-20-2,22);
        tabDisplayScroll.add(tabDisplayTxtJsonIndex,50+40+2+230-8-50-13-10-40-2-20,5+1+22,20,22);
        tabDisplayScroll.add(tabDisplayLblJsonHelp,50+20+230-8-50-13-10-20+5,5+1+22+6,13,7);
        tabDisplayScroll.add(tabDisplayTxtJson,5+1,5+1+22*2,230-8-50-13-10+44,22);
        tabDisplayScroll.add(tabDisplayLblJson,50+20+230-8-50-13-10-20+5,5+1+22*2+6,13,7);
        tabDisplayScroll.add(tabDisplayLblColors,120,5+7+22*3,0,0);
        tabDisplayScroll.add(tabDisplayBtnColor,5,5+1+22*4,80,20);
        tabDisplayScroll.add(tabDisplayTxtColorRGB,50+40,5+1+22*4,230-8-50-13-10-40,20);
        tabDisplayScroll.add(tabDisplayLblColorHelp,50+20+230-8-50-13-10-20+5,5+1+22*4+6,13,7);
        tabDisplayScroll.add(tabDisplayTxtColorHex,5+1,5+1+22*5,(230-8-50-13-10+44)/2-1,22);
        tabDisplayScroll.add(tabDisplayTxtColorDec,5+1+(230-8-50-13-10+44)/2+1,5+1+22*5,(230-8-50-13-10+44)/2,22);
        tabDisplayScroll.add(tabDisplayLblColor,50+20+230-8-50-13-10-20+5+2,5+1+22*5+6,13,7);
        tabDisplayScroll.add(tabDisplayBtnArmor,5,5+1+22*6,80,20);
        tabDisplayScroll.add(tabDisplayBtnMap,5+80+5,5+1+22*6,60,20);
        tabDisplayScroll.add(tabDisplayBtnPotion,5,5+1+22*7,80,20);
        tabDisplayScroll.add(tabDisplayBtnLabs,5,5+1+22*9,100,20);
        tabDisplayScroll.add(tabDisplayBtnWatermark,5+100+5,5+1+22*9,60,20);
        tabDisplayScroll.add(tabDisplayLblBlank,0,5+1+22*9,0,22+5+1-2);
        WScrollPanel tabDisplayScrollPanel = new WScrollPanel(tabDisplayScroll);
        tabDisplayScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabDisplayScrollPanel.setScrollingVertically(TriState.TRUE);
        tabDisplay.add(tabDisplayScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tabEnch
        WPlainPanel tabEnchScroll = new WPlainPanel();
        WLabel tabEnchLblBlank = new WLabel(Text.of(""));
        //
        WLabel tabEnchLblEnch = new WLabel(Text.of("Enchanting"));
        tabEnchLblEnch.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabEnchBtnEnch = new WButton(Text.of("Ench"));
        WTextField tabEnchTxtEnchId = new WTextField();
        tabEnchTxtEnchId.setMaxLength(64);
        WTextField tabEnchTxtEnchLvl = new WTextField();
        tabEnchTxtEnchLvl.setMaxLength(4);
        tabEnchBtnEnch.setOnClick(() -> {
            short lvl;
            String id = tabEnchTxtEnchId.getText();
            if(!tabEnchTxtEnchLvl.getText().equals("")) {
                try{
                    lvl = Short.parseShort(tabEnchTxtEnchLvl.getText());
                } catch(NumberFormatException ex) { return; }
                if(!id.equals("")) {
                    if(id.equals("*")) {
                        NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/Enchantments");
                        int i = 0;
                        if(element != null && element.getType() == NbtElement.LIST_TYPE)
                            i = ((NbtList)element).size();
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("aqua_affinity"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("bane_of_arthropods"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("blast_protection"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("channeling"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("depth_strider"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("efficiency"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("feather_falling"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("fire_aspect"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("fire_protection"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("flame"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("fortune"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("frost_walker"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("impaling"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("infinity"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("knockback"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("looting"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("loyalty"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("luck_of_the_sea"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("lure"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("mending"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("multishot"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("piercing"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("power"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("projectile_protection"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("protection"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("punch"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("quick_charge"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("respiration"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("riptide"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("sharpness"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("silk_touch"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("smite"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("soul_speed"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("sweeping"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("thorns"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                        i++;
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/id",NbtString.of("unbreaking"));
                        BlackMagick.setNbt(null,"Enchantments/"+i+":/lvl",NbtShort.of(lvl));
                    }
                    else {
                        NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/Enchantments");
                        if(!id.contains("minecraft:"))
                            id = "minecraft:"+id;
                        if(element != null && element.getType() == NbtElement.LIST_TYPE) {
                            NbtList nbtEnchantments = (NbtList)element;
                            NbtCompound nbt = new NbtCompound();
                            nbt.putString("id",id);
                            nbt.putShort("lvl",lvl);
                            nbtEnchantments.add(nbt);
                            BlackMagick.setNbt(null,"Enchantments",nbtEnchantments);
                        }
                        else {
                            BlackMagick.setNbt(null,"Enchantments/0:/id",NbtString.of(id));
                            BlackMagick.setNbt(null,"Enchantments/0:/lvl",NbtShort.of(lvl));
                        }
                    }
                }
            }
            else if(id.equals("")) {
                BlackMagick.removeNbt(null,"Enchantments");
            }
            else {
                NbtElement element = BlackMagick.getNbtFromPath(null,"0:/tag/Enchantments");
                if(element !=null && element.getType() == NbtElement.LIST_TYPE) {
                    NbtList nbtEnchantments = (NbtList)element;
                    boolean changedEnch = false;
                    if(!id.contains("minecraft:"))
                        id = "minecraft:"+id;
                    for(int i=0; i<nbtEnchantments.size(); i++) {
                        if(nbtEnchantments.get(i).getType() == NbtElement.COMPOUND_TYPE
                            && ((NbtCompound)nbtEnchantments.get(i)).contains("id")
                            && ((NbtCompound)nbtEnchantments.get(i)).get("id").getType() == NbtElement.STRING_TYPE
                            && ((NbtCompound)nbtEnchantments.get(i)).get("id").asString().equals(id)) {
                                nbtEnchantments.remove(i);
                                i--;
                                changedEnch = true;
                            }
                    }
                    if(changedEnch) {
                        ItemStack item = BlackMagick.setNbt(null,"Enchantments",nbtEnchantments);
                        if(!item.hasEnchantments())
                            BlackMagick.removeNbt(null,"Enchantments");
                    }
                }
            }
        });
        //
        WButton tabEnchBtnRepair = new WButton(Text.of("Repair Cost"));
        WButton tabEnchBtnRepairMax = new WButton(Text.of("?")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("Max 2,147,483,647"));
            }
        };
        WTextField tabEnchTxtRepair = new WTextField();
        tabEnchTxtRepair.setMaxLength(10);
        tabEnchBtnRepair.setOnClick(() -> {
            String inp = tabEnchTxtRepair.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"RepairCost",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"RepairCost");
        });
        tabEnchBtnRepairMax.setOnClick(() -> {
            tabEnchTxtRepair.setText("2147483647");
        });
        //
        tabEnchScroll.add(tabEnchLblEnch,120,5+7,0,0);
        tabEnchScroll.add(tabEnchBtnEnch,5,5+1+22,40,20);
        tabEnchScroll.add(tabEnchTxtEnchId,50,5+1+22,230-8-50-13-10-42,22);
        tabEnchScroll.add(tabEnchTxtEnchLvl,50+230-8-50-13-10-40,5+1+22,40,22);
        tabEnchScroll.add(tabEnchBtnRepair,5,5+1+22*2,80,20);
        tabEnchScroll.add(tabEnchTxtRepair,50+40,5+1+22*2,230-8-50-13-10-40,22);
        tabEnchScroll.add(tabEnchBtnRepairMax,50+230-8-50-13-10+5,5+1+22*2,15,20);
        tabEnchScroll.add(tabEnchLblBlank,0,5+1+22*2,0,22+5+1-2);
        WScrollPanel tabEnchScrollPanel = new WScrollPanel(tabEnchScroll);
        tabEnchScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabEnchScrollPanel.setScrollingVertically(TriState.TRUE);
        tabEnch.add(tabEnchScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tabBlock
        WPlainPanel tabBlockScroll = new WPlainPanel();
        WLabel tabBlockLblBlank = new WLabel(Text.of(""));
        //
        WButton tabBlockBtnName = new WButton(Text.of("Name"));
        WTextField tabBlockTxtName = new WTextField();
        tabBlockTxtName.setMaxLength(1024);
        tabBlockBtnName.setOnClick(() -> {
            String inp = tabBlockTxtName.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inp = "\'"+inp+"\'";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"BlockEntityTag/CustomName",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"BlockEntityTag/CustomName");
        });
        //
        WButton tabBlockBtnLock = new WButton(Text.of("Lock"));
        WTextField tabBlockTxtLock = new WTextField();
        tabBlockTxtLock.setMaxLength(256);
        tabBlockBtnLock.setOnClick(() -> {
            String inp = tabBlockTxtLock.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inp = "\'"+inp+"\'";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"BlockEntityTag/Lock",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"BlockEntityTag/Lock");
        });
        //
        WButton tabBlockBtnState = new WButton(Text.of("BlockState"));
        WLabel tabBlockLblState = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("Key | (Value)"),
                Text.of(""),
                Text.of("facing (n/s/e/w/u/d)   n/s/e/w/u/d (t/f)   axis (x/y/z)"),
                Text.of("rotation (15)   n/s/e/w/u/d (t/f)   waterlogged (t/f)"),
                Text.of(""),
                Text.of("walls- n/s/e/w (none/low/tall)  up (t/f)"),
                Text.of("slabs- type (bottom/top/double)"),
                Text.of("stairs- half (bottom/top)"),
                Text.of("           shape (inner_[left/right]/outer_[left/right])"),
                Text.of("doors/gates/trapdoors/barrel- open (t/f)"),
                Text.of("furnaces/campfires/lamp- lit (t/f)  farmland- moisture (7)"),
                Text.of("bamboo- leaves (none/small/large)"),
                Text.of("pistons- extended (t/f)  tnt- unstable (t/f)"),
                Text.of("double plants- half (lower/upper)"),
                Text.of("chests- type (single/left/right)"),
                Text.of("beds- part (foot/head)  campfires- signal_fire (t/f)"),
                Text.of("grass/podzol/mycelium- snowy (t/f)"),
                Text.of(""),
                Text.of("age"),
                Text.of("bamboo 0-1  cocoa_beans 0-2"),
                Text.of("nether_wart/beetroot_seeds/sweet_berries 0-3"),
                Text.of("[wheat/pumpkin/melon]_seeds/carrot/potato 0-7"),
                Text.of("Stop growth at:  chorus_flower 5"),
                Text.of("kelp/glow_berries/[twisting/weeping]_vines 25"));
            }
        };
        WTextField tabBlockTxtStateKey = new WTextField();
        tabBlockTxtStateKey.setMaxLength(64);
        WTextField tabBlockTxtStateValue = new WTextField();
        tabBlockTxtStateValue.setMaxLength(64);
        tabBlockBtnState.setOnClick(() -> {
            String inp = tabBlockTxtStateValue.getText();
            NbtElement inpEl;
            if(!inp.equals("") && !tabBlockTxtStateKey.getText().equals("")) {
                inp = "\""+inp+"\"";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"BlockStateTag/"+tabBlockTxtStateKey.getText(),inpEl,NbtElement.STRING_TYPE);
            }
            else if(inp.equals(""))
                BlackMagick.removeNbt(null,"BlockStateTag/"+tabBlockTxtStateKey.getText());
        });
        //
        WLabel tabBlockLblBanner = new WLabel(Text.of("Banners"));
        tabBlockLblBanner.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabBlockBtnBannerBase = new WButton(Text.of("Base Color"));
        WButton tabBlockBtnBannerPattern = new WButton(Text.of("Pattern"));
        WButton tabBlockBtnBannerReplaceMode = new WButton(Text.of("Add"));
        WTextField tabBlockTxtBannerBase = new WTextField();
        tabBlockTxtBannerBase.setMaxLength(16);
        WLabel tabBlockLblBannerHelp = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("Color | Pattern | Index (0-5/15)"),Text.of(""),
                Text.of("Pattern-"),
                Text.of("bs-Base   ts-Chief   ls-Pale Dexter"),
                Text.of("rs-Pale S.   cs-Pale   ms-Fess"),
                Text.of("drs-Bend   dls-Bend S.   ss-Paly"),
                Text.of("cr-Saltire   sc-Cross   ld-Per Bend S."),
                Text.of("rud-Per Bend   lud-Per Bend I.   rd-Per Bend S.I."),
                Text.of("vh-Per Pale   vhr-Per Pale I.   hh-Per Fess"),
                Text.of("hhb-Per Fess I.   bl-Base Dexter Canton   br-Base S. Canton"),
                Text.of("tl-Chief Dexter Canton   tr-Chief S. Canton   bt-Chevron"),
                Text.of("tt-I. Chevron   bts-Base Indented   tts-Chief Indented"),
                Text.of("mc-Roundel   mr-Lozenge   bo-Bordure"),
                Text.of("cbo-Bordure Indented   bri-Field Masoned   gra-Gradient"),
                Text.of("gru-Base Gradient"),
                Text.of("cre-Creeper Charge   sku-Skull Charge   flo-Flower Charge"),
                Text.of("moj-Thing   glb-Globe   pig-Snout"),
                Text.of(""),
                Text.of("Color-"),
                Text.of("0-White   1-Orange   2-Magenta   3-Light Blue"),
                Text.of("4-Yellow   5-Lime   6-Pink   7-Gray"),
                Text.of("8-Light Gray   9-Cyan   10-Purple   11-Blue"),
                Text.of("12-Brown   13-Green   14-Red   15-Black"));
            }
        };
        WTextField tabBlockTxtBannerIndex = new WTextField();
        tabBlockTxtBannerIndex.setMaxLength(2);
        WTextField tabBlockTxtPatternColor = new WTextField();
        tabBlockTxtPatternColor.setMaxLength(32);
        WTextField tabBlockTxtPattern = new WTextField();
        tabBlockTxtPattern.setMaxLength(3);
        tabBlockBtnBannerReplaceMode.setOnClick(() -> {
            if(tabBlockBtnBannerReplaceMode.getLabel().getString().equals("Add"))
                tabBlockBtnBannerReplaceMode.setLabel(Text.of("Set"));
            else
                tabBlockBtnBannerReplaceMode.setLabel(Text.of("Add"));
        });
        tabBlockBtnBannerBase.setOnClick(() -> {
            if(!tabBlockTxtBannerBase.getText().equals(""))
                BlackMagick.setId(tabBlockTxtBannerBase.getText()+"_banner");
            else
                BlackMagick.setId("white_banner");
        });
        tabBlockBtnBannerPattern.setOnClick(() -> {
            CLICK: {
                int index;
                try {
                    if(tabBlockTxtBannerIndex.getText().equals(""))
                        index = -1;
                    else {
                        index = Integer.parseInt(tabBlockTxtBannerIndex.getText());
                        if(index<0)
                            break CLICK;
                    }
                } catch(NumberFormatException e) {
                    break CLICK;
                }
                String inpColor = tabBlockTxtPatternColor.getText();
                int color=0;
                switch(inpColor) {
                    case "": color=-1; break;
                    case "white": color=0; break;
                    case "orange": color=1; break;
                    case "magenta": color=2; break;
                    case "light_blue": color=3; break;
                    case "yellow": color=4; break;
                    case "lime": color=5; break;
                    case "pink": color=6; break;
                    case "gray": color=7; break;
                    case "light_gray": color=8; break;
                    case "cyan": color=9; break;
                    case "purple": color=10; break;
                    case "blue": color=11; break;
                    case "brown": color=12; break;
                    case "green": color=13; break;
                    case "red": color=14; break;
                    case "black": color=15; break;
                    default: {
                        try{
                            color = Integer.parseInt(inpColor);
                            if(color<0 || color >15)
                                break CLICK;
                        } catch(NumberFormatException e) {
                            break CLICK;
                        }
                    }
                }
                String pattern = tabBlockTxtPattern.getText();
                switch(pattern) {
                    case "": break;
                    case "bs": break;
                    case "ts": break;
                    case "ls": break;
                    case "rs": break;
                    case "cs": break;
                    case "ms": break;
                    case "drs": break;
                    case "dls": break;
                    case "ss": break;
                    case "cr": break;
                    case "sc": break;
                    case "ld": break;
                    case "rud": break;
                    case "lud": break;
                    case "rd": break;
                    case "vh": break;
                    case "vhr": break;
                    case "hh": break;
                    case "hhb": break;
                    case "bl": break;
                    case "br": break;
                    case "tl": break;
                    case "tr": break;
                    case "bt": break;
                    case "tt": break;
                    case "bts": break;
                    case "tts": break;
                    case "mc": break;
                    case "mr": break;
                    case "bo": break;
                    case "cbo": break;
                    case "bri": break;
                    case "gra": break;
                    case "gru": break;
                    case "cre": break;
                    case "sku": break;
                    case "flo": break;
                    case "moj": break;
                    case "glb": break;
                    case "pig": break;
                    default: {
                        break CLICK;
                    }
                }
                if(index == -1 && color==-1 && pattern.equals("")) {
                    BlackMagick.removeNbt(null,"BlockEntityTag/Patterns");
                    break CLICK;
                }
                else if (color==-1 && pattern.equals("")) {
                    BlackMagick.removeNbt(null,"BlockEntityTag/Patterns/"+index+":");
                    break CLICK;
                }
                boolean replace = (tabBlockBtnBannerReplaceMode.getLabel().getString().equals("Set"));
                NbtList nbtPatterns = (NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/BlockEntityTag/Patterns");
                if(replace && nbtPatterns!=null) {
                    if(index==-1)
                        index = nbtPatterns.size()-1;
                    else if(index>nbtPatterns.size()-1)
                        break CLICK;
                    if(color!=-1)
                        BlackMagick.setNbt(null,"BlockEntityTag/Patterns/"+index+":/Color",NbtInt.of(color));
                    if(!pattern.equals(""))
                        BlackMagick.setNbt(null,"BlockEntityTag/Patterns/"+index+":/Pattern",NbtString.of(pattern));
                }
                else if(!replace) {
                    if(index==-1 && color!=-1 && !pattern.equals("")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putInt("Color",color);
                        nbt.putString("Pattern",pattern);
                        if(nbtPatterns==null)
                            BlackMagick.setNbt(null,"BlockEntityTag/Patterns/0:",nbt);
                        else {
                            nbtPatterns.add(nbt);
                            BlackMagick.setNbt(null,"BlockEntityTag/Patterns",nbtPatterns);
                        }
                    }
                    else if(index!=-1 && color!=-1 && !pattern.equals("")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putInt("Color",color);
                        nbt.putString("Pattern",pattern);
                        if(nbtPatterns==null)
                            BlackMagick.setNbt(null,"BlockEntityTag/Patterns/0:",nbt);
                        else {
                            if(index>nbtPatterns.size())
                                index=nbtPatterns.size();
                            nbtPatterns.add(index,nbt);
                            BlackMagick.setNbt(null,"BlockEntityTag/Patterns",nbtPatterns);
                        }
                    }
                }
            }
        });
        //
        WButton tabBlockBtnBannerPreset = new WButton(Text.of("Preset"));
        String allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        WLabel tabBlockLblBannerPreset = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("Char Color | Char | Base Color"),Text.of(""),
                Text.of(allChars));
            }
        };
        WTextField tabBlockTxtBannerPresetPatternColor = new WTextField();
        tabBlockTxtBannerPresetPatternColor.setMaxLength(16);
        WTextField tabBlockTxtBannerPresetChar = new WTextField();
        tabBlockTxtBannerPresetChar.setMaxLength(128);
        WTextField tabBlockTxtBannerPresetBaseColor = new WTextField();
        tabBlockTxtBannerPresetBaseColor.setMaxLength(16);
        tabBlockBtnBannerPreset.setOnClick(() -> {
            CLICK:{
                int baseColor = 0;
                int charColor = 0;
                switch(tabBlockTxtBannerPresetBaseColor.getText()) {
                    case "white": baseColor=0; break;
                    case "orange": baseColor=1; break;
                    case "magenta": baseColor=2; break;
                    case "light_blue": baseColor=3; break;
                    case "yellow": baseColor=4; break;
                    case "lime": baseColor=5; break;
                    case "pink": baseColor=6; break;
                    case "gray": baseColor=7; break;
                    case "light_gray": baseColor=8; break;
                    case "cyan": baseColor=9; break;
                    case "purple": baseColor=10; break;
                    case "blue": baseColor=11; break;
                    case "brown": baseColor=12; break;
                    case "green": baseColor=13; break;
                    case "red": baseColor=14; break;
                    case "black": baseColor=15; break;
                    default: break CLICK;
                }
                switch(tabBlockTxtBannerPresetPatternColor.getText()) {
                    case "white": charColor=0; break;
                    case "orange": charColor=1; break;
                    case "magenta": charColor=2; break;
                    case "light_blue": charColor=3; break;
                    case "yellow": charColor=4; break;
                    case "lime": charColor=5; break;
                    case "pink": charColor=6; break;
                    case "gray": charColor=7; break;
                    case "light_gray": charColor=8; break;
                    case "cyan": charColor=9; break;
                    case "purple": charColor=10; break;
                    case "blue": charColor=11; break;
                    case "brown": charColor=12; break;
                    case "green": charColor=13; break;
                    case "red": charColor=14; break;
                    case "black": charColor=15; break;
                    default: break CLICK;
                }
                String chars = tabBlockTxtBannerPresetChar.getText();
                if(chars.equals("*"))
                    chars = allChars;
                if(chars.length()==1)
                    createBanner(baseColor,charColor,chars,
                        tabBlockTxtBannerPresetBaseColor.getText(),tabBlockTxtBannerPresetPatternColor.getText());
                else if(chars.length()>1) {
                    NbtList Items = new NbtList();
                    while(chars.length()>0) {
                        ItemStack bannerItem = createBanner(baseColor,charColor,chars.substring(0,1),
                            tabBlockTxtBannerPresetBaseColor.getText(),tabBlockTxtBannerPresetPatternColor.getText());
                        if(bannerItem!=null)
                            Items.add((NbtCompound)BlackMagick.getNbtFromPath(bannerItem,"0:"));
                        if(chars.length()==1)
                            chars = "";
                        else
                            chars = chars.substring(1);
                    }
                    ItemStack override = BlackMagick.setId("bundle");
                    BlackMagick.removeNbt(override,"");
                    BlackMagick.setNbt(override,"Items",Items);
                }
            }
        });
        //
        WButton tabBlockBtnMaxStack = new WButton(Text.of("Fill Container Stacks"));
        tabBlockBtnMaxStack.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getAbilities().creativeMode) {
                if(!client.player.getMainHandStack().isEmpty()) {
                    ItemStack item = client.player.getMainHandStack();
                    if(item.hasNbt()) {
                        NbtCompound nbt = item.getNbt().copy();
                        if(nbt.contains("BlockEntityTag") && nbt.get("BlockEntityTag").getType()==NbtElement.COMPOUND_TYPE
                        && ((NbtCompound)nbt.get("BlockEntityTag")).contains("Items")
                        && ((NbtCompound)nbt.get("BlockEntityTag")).get("Items").getType()==NbtElement.LIST_TYPE
                        && ((NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items")).size()>0
                        && ((NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items")).get(0).getType()==NbtElement.COMPOUND_TYPE) {
                            NbtList nbtItems = (NbtList)((NbtCompound)nbt.get("BlockEntityTag")).get("Items");
                            for(int i=0; i<nbtItems.size(); i++) {
                                BlackMagick.setNbt(null,"BlockEntityTag/Items/"+i+":/Count",NbtInt.of(64));
                            }
                        }
                    }
                }
            }
        });
        //
        WButton tabBlockBtnBundle = new WButton(Text.of("Container to Bundle"));
        tabBlockBtnBundle.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.player.getAbilities().creativeMode) {
                ItemStack item0 = BlackMagick.setNbt(null,"Items",BlackMagick.getNbtFromPath(null,"0:/tag/BlockEntityTag/Items"));
                NbtCompound tag = new NbtCompound();
                if(item0.hasNbt())
                    tag = item0.getNbt();
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","bundle");
                nbt.putInt("Count",1);
                nbt.put("tag",tag);
                ItemStack item = ItemStack.fromNbt(nbt);
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
                ItemBuilder.updateItem(item);
            }
        });
        //
        tabBlockScroll.add(tabBlockBtnName,5,5+1,40,20);
        tabBlockScroll.add(tabBlockTxtName,50,5+1,230-8-50-13-10,22);
        tabBlockScroll.add(tabBlockBtnLock,5,5+1+22,40,20);
        tabBlockScroll.add(tabBlockTxtLock,50,5+1+22,230-8-50-13-10,22);
        tabBlockScroll.add(tabBlockBtnState,5,5+1+22*2,60,20);
        tabBlockScroll.add(tabBlockTxtStateKey,50+20,5+1+22*2,(230-8-50-13-10-20)/2-1,22);
        tabBlockScroll.add(tabBlockTxtStateValue,50+20+(230-8-50-13-10-20)/2+1,5+1+22*2,(230-8-50-13-10-20)/2,22);
        tabBlockScroll.add(tabBlockLblState,50+20+230-8-50-13-10-20+5,5+1+22*2+6,13,7);
        tabBlockScroll.add(tabBlockLblBanner,120,5+7+22*3,0,0);
        tabBlockScroll.add(tabBlockBtnBannerBase,5,5+1+22*4,60,20);
        tabBlockScroll.add(tabBlockTxtBannerBase,50+20,5+1+22*4,230-8-50-13-10-20,22);
        tabBlockScroll.add(tabBlockBtnBannerPattern,5,5+1+22*5,50,20);
        tabBlockScroll.add(tabBlockBtnBannerReplaceMode,5+50+5,5+1+22*5,25,20);
        tabBlockScroll.add(tabBlockTxtPatternColor,50+40,5+1+22*5,(230-8-50-13-10-40)*5/8-5-3,22);
        tabBlockScroll.add(tabBlockTxtPattern,50+40+(230-8-50-13-10-40)*5/8-6,5+1+22*5,(230-8-50-13-10-40)*2/8,22);
        tabBlockScroll.add(tabBlockTxtBannerIndex,50+40+(230-8-50-13-10-40)*7/8-5+1,5+1+22*5,(230-8-50-13-10-40)/8+5,22);
        tabBlockScroll.add(tabBlockLblBannerHelp,50+20+230-8-50-13-10-20+5,5+1+22*5+6,13,7);
        tabBlockScroll.add(tabBlockBtnBannerPreset,5,5+1+22*6,40,20);
        tabBlockScroll.add(tabBlockTxtBannerPresetPatternColor,50,5+1+22*6,60,22);
        tabBlockScroll.add(tabBlockTxtBannerPresetChar,50+60+2,5+1+22*6,230-8-50-13-10-120-2-2,22);
        tabBlockScroll.add(tabBlockTxtBannerPresetBaseColor,50+60+1+230-8-50-13-10-120-1,5+1+22*6,60,22);
        tabBlockScroll.add(tabBlockLblBannerPreset,50+20+230-8-50-13-10-20+5,5+1+22*6+6,13,7);
        tabBlockScroll.add(tabBlockBtnMaxStack,5,5+1+22*8,120,20);
        tabBlockScroll.add(tabBlockBtnBundle,5,5+1+22*9,120,20);
        tabBlockScroll.add(tabBlockLblBlank,0,5+1+22*9,0,22+5+1-2);
        WScrollPanel tabBlockScrollPanel = new WScrollPanel(tabBlockScroll);
        tabBlockScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabBlockScrollPanel.setScrollingVertically(TriState.TRUE);
        tabBlock.add(tabBlockScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tabEntity
        WPlainPanel tabEntityScroll = new WPlainPanel();
        WLabel tabEntityLblBlank = new WLabel(Text.of(""));
        //
        WLabel tabEntityLblArmorStand = new WLabel(Text.of("Armor Stands"));
        tabEntityLblArmorStand.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabEntityBtnBasePlate = new WButton(Text.of("BasePlate"));
        tabEntityBtnBasePlate.setOnClick(() -> {
            ItemStack item = BlackMagick.setId("armor_stand");
            if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate")!=null &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/NoBasePlate").asString().equals("1b")) {
                BlackMagick.removeNbt(item,"EntityTag/NoBasePlate");
            }
            else
                BlackMagick.setNbt(item,"EntityTag/NoBasePlate",NbtByte.of(true));
        });
        WButton tabEntityBtnShowArms = new WButton(Text.of("ShowArms"));
        tabEntityBtnShowArms.setOnClick(() -> {
            ItemStack item = BlackMagick.setId("armor_stand");
            if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms")!=null &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/ShowArms").asString().equals("1b")) {
                BlackMagick.removeNbt(item,"EntityTag/ShowArms");
            }
            else
                BlackMagick.setNbt(item,"EntityTag/ShowArms",NbtByte.of(true));
        });
        WButton tabEntityBtnSmall = new WButton(Text.of("Small"));
        tabEntityBtnSmall.setOnClick(() -> {
            ItemStack item = BlackMagick.setId("armor_stand");
            if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small")!=null &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Small").asString().equals("1b")) {
                BlackMagick.removeNbt(item,"EntityTag/Small");
            }
            else
                BlackMagick.setNbt(item,"EntityTag/Small",NbtByte.of(true));
        });
        WButton tabEntityBtnMarker = new WButton(Text.of("Marker"));
        tabEntityBtnMarker.setOnClick(() -> {
            ItemStack item = BlackMagick.setId("armor_stand");
            if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker")!=null &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Marker").asString().equals("1b")) {
                BlackMagick.removeNbt(item,"EntityTag/Marker");
            }
            else
                BlackMagick.setNbt(item,"EntityTag/Marker",NbtByte.of(true));
        });
        WButton tabEntityBtnInvisible = new WButton(Text.of("Invisible"));
        tabEntityBtnInvisible.setOnClick(() -> {
            ItemStack item = BlackMagick.setId("armor_stand");
            if(BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible")!=null &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(item,"0:/tag/EntityTag/Invisible").asString().equals("1b")) {
                BlackMagick.removeNbt(item,"EntityTag/Invisible");
            }
            else
                BlackMagick.setNbt(item,"EntityTag/Invisible",NbtByte.of(true));
        });
        //
        WButton tabEntityBtnDisabledSlots = new WButton(Text.of("DisabledSlots"));
        WButton tabEntityBtnDisableAll = new WButton(Text.of("?")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("Disable All Slots (16,191)"));
            }
        };
        WTextField tabEntityTxtDisabledSlots = new WTextField();
        tabEntityTxtDisabledSlots.setMaxLength(7);
        tabEntityBtnDisabledSlots.setOnClick(() -> {
            String inp = tabEntityTxtDisabledSlots.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"EntityTag/DisabledSlots",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"EntityTag/DisabledSlots");
        });
        tabEntityBtnDisableAll.setOnClick(() -> {
            tabEntityTxtDisabledSlots.setText("16191");
        });
        //
        WButton tabEntityBtnPose = new WButton(Text.of("Pose"));
        WLabel tabEntityLblPose = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("Part | Rotation (x,y,z)"),
                Text.of(""),
                Text.of("Head"),
                Text.of("+x Look Down    +y Look Right   +z Tilt Left"),
                Text.of(""),
                Text.of("Body"),
                Text.of("+x Lean Forward    +y Aim Right   +z Lean Left"),
                Text.of(""),
                Text.of("LeftArm"),
                Text.of("+x Swing Back    +y Tilt Right    +z Swing Right"),
                Text.of(""),
                Text.of("RightArm"),
                Text.of("+x Swing Back    +y Tilt Right   +z Swing Right"),
                Text.of(""),
                Text.of("LeftLeg"),
                Text.of("+x Swing Back    +y Tilt Right    +z Swing Right"),
                Text.of(""),
                Text.of("RightLeg"),
                Text.of("+x Swing Back    +y Tilt Right    +z Swing Right"));
            }
        };
        WTextField tabEntityTxtPosePart = new WTextField();
        tabEntityTxtPosePart.setMaxLength(16);
        WTextField tabEntityTxtPose = new WTextField();
        tabEntityTxtPose.setMaxLength(64);
        tabEntityBtnPose.setOnClick(() -> {
            CLICK:{
                String inpPart = tabEntityTxtPosePart.getText();
                String inpPose = tabEntityTxtPose.getText();
                if(inpPart.equals("") && inpPose.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Pose");
                else if(!inpPart.equals("") && inpPose.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Pose/"+inpPart);
                else if(!inpPart.equals("") && !inpPose.equals("")) {
                    switch(inpPart) {
                        case "Head": break;
                        case "Body": break;
                        case "LeftArm": break;
                        case "RightArm": break;
                        case "LeftLeg": break;
                        case "RightLeg": break;
                        default: break CLICK;
                    }
                    float[] rot = {0f,0f,0f};
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pose/"+inpPart)!=null
                    && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pose/"+inpPart).getType()==NbtElement.LIST_TYPE &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pose/"+inpPart)).size()==3 &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pose/"+inpPart)).getHeldType()==NbtElement.FLOAT_TYPE)
                        for(int i=0; i<3; i++)
                            rot[i]=Float.parseFloat(
                                ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pose/"+inpPart)).get(i).asString());
                    inpPose = inpPose.replace(" ","");
                    inpPose = inpPose.replace("f","");
                    inpPose = inpPose.replace("F","");
                    inpPose = inpPose+",X";
                    try {
                        for(int i=0; i<3; i++) {
                            if(!inpPose.contains(","))
                                break CLICK;
                            if(!inpPose.substring(0,inpPose.indexOf(",")).equals(""))
                                rot[i]=Float.parseFloat(inpPose.substring(0,inpPose.indexOf(",")));
                            inpPose = inpPose.substring(inpPose.indexOf(",")+1);
                        }
                    } catch(NumberFormatException e) {
                        break CLICK;
                    }
                    for(int i=0; i<3; i++)
                        BlackMagick.setNbt(null,"EntityTag/Pose/"+inpPart+"/"+i+":",NbtFloat.of(rot[i]));
                }
            }
        });
        //
        WButton tabEntityBtnId = new WButton(Text.of("Entity"));
        WTextField tabEntityTxtId = new WTextField();
        tabEntityTxtId.setMaxLength(128);
        tabEntityBtnId.setOnClick(() -> {
            String inp = tabEntityTxtId.getText();
            NbtElement inpEl;
            final MinecraftClient client = MinecraftClient.getInstance();
            ItemStack item;
            if(client.player.getMainHandStack().isEmpty())
                item = BlackMagick.setId("bat_spawn_egg");
            else
                item = client.player.getMainHandStack();
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString("\""+inp+"\"");
                BlackMagick.setNbt(item,"EntityTag/id",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(item,"EntityTag/id");
        });
        //
        WButton tabEntityBtnName = new WButton(Text.of("Name"));
        WTextField tabEntityTxtName = new WTextField();
        tabEntityTxtName.setMaxLength(1024);
        tabEntityBtnName.setOnClick(() -> {
            String inp = tabEntityTxtName.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString("\'"+inp+"\'");
                BlackMagick.setNbt(null,"EntityTag/CustomName",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"EntityTag/CustomName");
        });
        //
        WButton tabEntityBtnNameVisible = new WButton(Text.of("ShowName"));
        tabEntityBtnNameVisible.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/CustomNameVisible").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/CustomNameVisible");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/CustomNameVisible",NbtByte.of(true));
        });
        WButton tabEntityBtnGravity = new WButton(Text.of("NoGravity"));
        tabEntityBtnGravity.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoGravity").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/NoGravity");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/NoGravity",NbtByte.of(true));
        });
        WButton tabEntityBtnGlowing = new WButton(Text.of("Glowing"));
        tabEntityBtnGlowing.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Glowing").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/Glowing");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Glowing",NbtByte.of(true));
        });
        WButton tabEntityBtnFire = new WButton(Text.of("VisualFire"));
        tabEntityBtnFire.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/HasVisualFire").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/HasVisualFire");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/HasVisualFire",NbtByte.of(true));
        });
        WButton tabEntityBtnSilent = new WButton(Text.of("Silent"));
        tabEntityBtnSilent.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Silent").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/Silent");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Silent",NbtByte.of(true));
        });
        WButton tabEntityBtnAI = new WButton(Text.of("NoAI"));
        tabEntityBtnAI.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/NoAI").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/NoAI");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/NoAI",NbtByte.of(true));
        });
        WButton tabEntityBtnInvulnerable = new WButton(Text.of("Invulnerable"));
        tabEntityBtnInvulnerable.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invulnerable").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/Invulnerable");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Invulnerable",NbtByte.of(true));
        });
        WButton tabEntityBtnDespawn = new WButton(Text.of("Persistence"));
        tabEntityBtnDespawn.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/PersistenceRequired").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/PersistenceRequired");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/PersistenceRequired",NbtByte.of(true));
        });
        //
        WButton tabEntityBtnItems = new WButton(Text.of("Items"));
        WLabel tabEntityLblItems = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("0 Mainhand    1 Offhand"),
                Text.of("2 Boots    3 Leggings"),
                Text.of("4 Chestplate    5 Helmet"),
                Text.of(""),
                Text.of("6 Armor    7 Armor/Mainhand"),
                Text.of(""),
                Text.of("8 Remove Armor    9 Remove Hand"));
            }
        };
        WTextField tabEntityTxtItems = new WTextField();
        tabEntityTxtItems.setMaxLength(1);
        tabEntityBtnItems.setOnClick(() -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            switch(tabEntityTxtItems.getText()) {
                case "0":
                    if(client.player.getOffHandStack().isEmpty())
                        BlackMagick.setNbt(null,"EntityTag/HandItems/0:",new NbtCompound());
                    else {
                        ItemStack item = client.player.getOffHandStack();
                        NbtCompound nbt = new NbtCompound();
                        nbt.putInt("Count",item.getCount());
                        nbt.putString("id",item.getItem().toString());
                        if(item.hasNbt())
                            nbt.put("tag",item.getNbt());
                        BlackMagick.setNbt(null,"EntityTag/HandItems/0:",nbt);
                    }
                    break;
                case "1":
                    if(client.player.getOffHandStack().isEmpty())
                        BlackMagick.setNbt(null,"EntityTag/HandItems/1:",new NbtCompound());
                    else {
                        ItemStack item = client.player.getOffHandStack();
                        NbtCompound nbt = new NbtCompound();
                        nbt.putInt("Count",item.getCount());
                        nbt.putString("id",item.getItem().toString());
                        if(item.hasNbt())
                            nbt.put("tag",item.getNbt());
                        BlackMagick.setNbt(null,"EntityTag/HandItems/1:",nbt);
                    }
                    break;
                case "2":
                    if(client.player.getArmorItems()!=null) {
                        Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                        ItemStack item = null;
                        if(items.hasNext())
                            item = items.next();
                        if(item == null || item.isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",new NbtCompound());
                        else {
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",nbt);
                        }
                    }
                    break;
                case "3":
                    if(client.player.getArmorItems()!=null) {
                        Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                        ItemStack item = null;
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        else item = null;
                        if(item == null || item.isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",new NbtCompound());
                        else {
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",nbt);
                        }
                    }
                    break;
                case "4":
                    if(client.player.getArmorItems()!=null) {
                        Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                        ItemStack item = null;
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        else item = null;
                        if(item == null || item.isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",new NbtCompound());
                        else {
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",nbt);
                        }
                    }
                    break;
                case "5":
                    if(client.player.getArmorItems()!=null) {
                        Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                        ItemStack item = null;
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        if(items.hasNext())
                            item = items.next();
                        else item = null;
                        if(item == null || item.isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",new NbtCompound());
                        else {
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",nbt);
                        }
                    }
                    break;
                case "6":
                    {//boots
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",nbt);
                            }
                        }
                    }
                    {//leggings
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",nbt);
                            }
                        }
                    }
                    {//chestplate
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",nbt);
                            }
                        }
                    }
                    {//helmet
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",nbt);
                            }
                        }
                    }
                    break;
                case "7":
                    {//mainhand
                        if(client.player.getOffHandStack().isEmpty())
                            BlackMagick.setNbt(null,"EntityTag/HandItems/0:",new NbtCompound());
                        else {
                            ItemStack item = client.player.getOffHandStack();
                            NbtCompound nbt = new NbtCompound();
                            nbt.putInt("Count",item.getCount());
                            nbt.putString("id",item.getItem().toString());
                            if(item.hasNbt())
                                nbt.put("tag",item.getNbt());
                            BlackMagick.setNbt(null,"EntityTag/HandItems/0:",nbt);
                        }
                    }
                    {//boots
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/0:",nbt);
                            }
                        }
                    }
                    {//leggings
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/1:",nbt);
                            }
                        }
                    }
                    {//chestplate
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/2:",nbt);
                            }
                        }
                    }
                    {//helmet
                        if(client.player.getArmorItems()!=null) {
                            Iterator<ItemStack> items = client.player.getArmorItems().iterator();
                            ItemStack item = null;
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            if(items.hasNext())
                                item = items.next();
                            else item = null;
                            if(item == null || item.isEmpty())
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",new NbtCompound());
                            else {
                                NbtCompound nbt = new NbtCompound();
                                nbt.putInt("Count",item.getCount());
                                nbt.putString("id",item.getItem().toString());
                                if(item.hasNbt())
                                    nbt.put("tag",item.getNbt());
                                BlackMagick.setNbt(null,"EntityTag/ArmorItems/3:",nbt);
                            }
                        }
                    }
                    break;
                case "8":
                    BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                    break;
                case "9":
                    BlackMagick.removeNbt(null,"EntityTag/HandItems");
                    break;
                case "":
                    BlackMagick.removeNbt(null,"EntityTag/ArmorItems");
                    BlackMagick.removeNbt(null,"EntityTag/HandItems");
                    break;
                default: break;
            }
        });
        //
        WButton tabEntityBtnPos = new WButton(Text.of("Pos"));
        WTextField tabEntityTxtPos = new WTextField();
        tabEntityTxtPos.setMaxLength(64);
        tabEntityBtnPos.setOnClick(() -> {
            CLICK:{
                String inp = tabEntityTxtPos.getText();
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Pos");
                else {
                    double[] pos = {0d,0d,0d};
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")!=null
                    && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos").getType()==NbtElement.LIST_TYPE &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).size()==3 &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).getHeldType()==NbtElement.DOUBLE_TYPE)
                        for(int i=0; i<3; i++)
                            pos[i]=Double.parseDouble(
                                ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Pos")).get(i).asString());
                    inp = inp.replace(" ","");
                    inp = inp.replace("d","");
                    inp = inp.replace("D","");
                    inp = inp+",X";
                    try {
                        for(int i=0; i<3; i++) {
                            if(!inp.contains(","))
                                break CLICK;
                            if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                pos[i]=Double.parseDouble(inp.substring(0,inp.indexOf(",")));
                            inp = inp.substring(inp.indexOf(",")+1);
                        }
                    } catch(NumberFormatException e) {
                        break CLICK;
                    }
                    for(int i=0; i<3; i++)
                        BlackMagick.setNbt(null,"EntityTag/Pos/"+i+":",NbtDouble.of(pos[i]));
                }
            }
        });
        //
        WButton tabEntityBtnMot = new WButton(Text.of("Motion"));
        WTextField tabEntityTxtMot = new WTextField();
        tabEntityTxtMot.setMaxLength(64);
        tabEntityBtnMot.setOnClick(() -> {
            CLICK:{
                String inp = tabEntityTxtMot.getText();
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Motion");
                else {
                    double[] mot = {0d,0d,0d};
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")!=null
                    && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion").getType()==NbtElement.LIST_TYPE &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).size()==3 &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).getHeldType()==NbtElement.DOUBLE_TYPE)
                        for(int i=0; i<3; i++)
                            mot[i]=Double.parseDouble(
                                ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Motion")).get(i).asString());
                    inp = inp.replace(" ","");
                    inp = inp.replace("d","");
                    inp = inp.replace("D","");
                    inp = inp+",X";
                    try {
                        for(int i=0; i<3; i++) {
                            if(!inp.contains(","))
                                break CLICK;
                            if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                mot[i]=Double.parseDouble(inp.substring(0,inp.indexOf(",")));
                            inp = inp.substring(inp.indexOf(",")+1);
                        }
                    } catch(NumberFormatException e) {
                        break CLICK;
                    }
                    for(int i=0; i<3; i++)
                        BlackMagick.setNbt(null,"EntityTag/Motion/"+i+":",NbtDouble.of(mot[i]));
                }
            }
        });
        //
        WButton tabEntityBtnRot = new WButton(Text.of("Rotation"));
        WLabel tabEntityLblRot = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(
                Text.of("(Yaw,Pitch)"),
                Text.of(""),
                Text.of("Yaw 0 - 360 (South=0,West=90)"),
                Text.of("Pitch -90 - 90 (Down=90)"));
            }
        };
        WTextField tabEntityTxtRot = new WTextField();
        tabEntityTxtRot.setMaxLength(64);
        tabEntityBtnRot.setOnClick(() -> {
            CLICK:{
                String inp = tabEntityTxtRot.getText();
                if(inp.equals(""))
                    BlackMagick.removeNbt(null,"EntityTag/Rotation");
                else {
                    float[] rot = {0f,0f};
                    if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")!=null
                    && BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation").getType()==NbtElement.LIST_TYPE &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).size()==2 &&
                    ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).getHeldType()==NbtElement.FLOAT_TYPE)
                        for(int i=0; i<2; i++)
                            rot[i]=Float.parseFloat(
                                ((NbtList)BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Rotation")).get(i).asString());
                    inp = inp.replace(" ","");
                    inp = inp.replace("f","");
                    inp = inp.replace("F","");
                    inp = inp+",X";
                    try {
                        for(int i=0; i<2; i++) {
                            if(!inp.contains(","))
                                break CLICK;
                            if(!inp.substring(0,inp.indexOf(",")).equals(""))
                                rot[i]=Float.parseFloat(inp.substring(0,inp.indexOf(",")));
                            inp = inp.substring(inp.indexOf(",")+1);
                        }
                    } catch(NumberFormatException e) {
                        break CLICK;
                    }
                    for(int i=0; i<2; i++)
                        BlackMagick.setNbt(null,"EntityTag/Rotation/"+i+":",NbtFloat.of(rot[i]));
                }
            }
        });
        //
        WLabel tabEntityLblItemFrame = new WLabel(Text.of("Item Frames"));
        tabEntityLblItemFrame.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabEntityBtnItemRot = new WButton(Text.of("Rotation"));
        WTextField tabEntityTxtItemRot = new WTextField();
        tabEntityTxtItemRot.setMaxLength(1);
        tabEntityBtnItemRot.setOnClick(() -> {
            String inp = tabEntityTxtItemRot.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"EntityTag/ItemRotation",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"EntityTag/ItemRotation");
        });
        //
        WButton tabEntityBtnItem = new WButton(Text.of("Item"));
        tabEntityBtnItem.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Item")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Item").getType()==NbtElement.COMPOUND_TYPE) {
                BlackMagick.removeNbt(null,"EntityTag/Item");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Item",BlackMagick.getNbtFromPath(null,"1:"));
        });
        WButton tabEntityBtnItemInvis = new WButton(Text.of("Invis"));
        tabEntityBtnItemInvis.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Invisible").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/Invisible");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Invisible",NbtByte.of(true));
        });
        WButton tabEntityBtnItemFix = new WButton(Text.of("Fixed"));
        tabEntityBtnItemFix.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed").getType()==NbtElement.BYTE_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/Fixed").asString().equals("1b")) {
                BlackMagick.removeNbt(null,"EntityTag/Fixed");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/Fixed",NbtByte.of(true));
        });
        WButton tabEntityBtnItemDrop = new WButton(Text.of("Drop"));
        tabEntityBtnItemDrop.setOnClick(() -> {
            if(BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ItemDropChance")!=null &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ItemDropChance").getType()==NbtElement.FLOAT_TYPE &&
            BlackMagick.getNbtFromPath(null,"0:/tag/EntityTag/ItemDropChance").asString().equals("0.0f")) {
                BlackMagick.removeNbt(null,"EntityTag/ItemDropChance");
            }
            else
                BlackMagick.setNbt(null,"EntityTag/ItemDropChance",NbtFloat.of(0f));
        });
        //
        tabEntityScroll.add(tabEntityLblArmorStand,120,5+7,0,0);
        tabEntityScroll.add(tabEntityBtnBasePlate,5,5+1+22,60,20);
        tabEntityScroll.add(tabEntityBtnShowArms,5+60+5,5+1+22,60,20);
        tabEntityScroll.add(tabEntityBtnSmall,5+60+5+60+5,5+1+22,60,20);
        tabEntityScroll.add(tabEntityBtnMarker,5,5+1+22*2,60,20);
        tabEntityScroll.add(tabEntityBtnInvisible,5+60+5,5+1+22*2,60,20);
        tabEntityScroll.add(tabEntityBtnDisabledSlots,5,5+1+22*3,80,20);
        tabEntityScroll.add(tabEntityTxtDisabledSlots,50+40,5+1+22*3,230-8-50-13-10-40,22);
        tabEntityScroll.add(tabEntityBtnDisableAll,50+230-8-50-13-10+5,5+1+22*3,15,20);
        tabEntityScroll.add(tabEntityBtnPose,5,5+1+22*4,60,20);
        tabEntityScroll.add(tabEntityTxtPosePart,50+20,5+1+22*4,(230-8-50-13-10-20)/2-1-8,22);
        tabEntityScroll.add(tabEntityTxtPose,50+20+(230-8-50-13-10-20)/2+1-8,5+1+22*4,(230-8-50-13-10-20)/2+8,22);
        tabEntityScroll.add(tabEntityLblPose,50+20+230-8-50-13-10-20+5,5+1+22*4+6,13,7);
        tabEntityScroll.add(tabEntityBtnId,5,5+1+22*6,40,20);
        tabEntityScroll.add(tabEntityTxtId,50,5+1+22*6,230-8-50-13-10,22);
        tabEntityScroll.add(tabEntityBtnName,5,5+1+22*7,40,20);
        tabEntityScroll.add(tabEntityTxtName,50,5+1+22*7,230-8-50-13-10,22);
        tabEntityScroll.add(tabEntityBtnNameVisible,5,5+1+22*8,60,20);
        tabEntityScroll.add(tabEntityBtnGravity,5+60+5,5+1+22*8,60,20);
        tabEntityScroll.add(tabEntityBtnGlowing,5+60+5+60+5,5+1+22*8,60,20);
        tabEntityScroll.add(tabEntityBtnFire,5,5+1+22*9,60,20);
        tabEntityScroll.add(tabEntityBtnSilent,5+60+5,5+1+22*9,60,20);
        tabEntityScroll.add(tabEntityBtnAI,5+60+5+60+5,5+1+22*9,60,20);
        tabEntityScroll.add(tabEntityBtnInvulnerable,5,5+1+22*10,80,20);
        tabEntityScroll.add(tabEntityBtnDespawn,5+80+5,5+1+22*10,80,20);
        tabEntityScroll.add(tabEntityBtnItems,5,5+1+22*11,40,20);
        tabEntityScroll.add(tabEntityTxtItems,50,5+1+22*11,230-8-50-13-10,22);
        tabEntityScroll.add(tabEntityLblItems,50+20+230-8-50-13-10-20+5,5+1+22*11+6,13,7);
        tabEntityScroll.add(tabEntityBtnPos,5,5+1+22*12,40,20);
        tabEntityScroll.add(tabEntityTxtPos,50,5+1+22*12,230-8-50-13-10,22);
        tabEntityScroll.add(tabEntityBtnMot,5,5+1+22*13,40,20);
        tabEntityScroll.add(tabEntityTxtMot,50,5+1+22*13,230-8-50-13-10,22);
        tabEntityScroll.add(tabEntityBtnRot,5,5+1+22*14,60,20);
        tabEntityScroll.add(tabEntityTxtRot,50+20,5+1+22*14,230-8-50-13-10-20,22);
        tabEntityScroll.add(tabEntityLblRot,50+20+230-8-50-13-10-20+5,5+1+22*14+6,13,7);
        tabEntityScroll.add(tabEntityLblItemFrame,120,5+7+22*15,0,0);
        tabEntityScroll.add(tabEntityBtnItemRot,5,5+1+22*16,60,20);
        tabEntityScroll.add(tabEntityTxtItemRot,50+20,5+1+22*16,230-8-50-13-10-20,22);
        tabEntityScroll.add(tabEntityBtnItem,5,5+1+22*17,60,20);
        tabEntityScroll.add(tabEntityBtnItemInvis,5+60+5,5+1+22*17,60,20);
        tabEntityScroll.add(tabEntityBtnItemFix,5+60+5+60+5,5+1+22*17,60,20);
        tabEntityScroll.add(tabEntityBtnItemDrop,5,5+1+22*18,60,20);
        tabEntityScroll.add(tabEntityLblBlank,0,5+1+22*18,0,22+5+1-2);
        WScrollPanel tabEntityScrollPanel = new WScrollPanel(tabEntityScroll);
        tabEntityScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabEntityScrollPanel.setScrollingVertically(TriState.TRUE);
        tabEntity.add(tabEntityScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tabMisc
        WPlainPanel tabMiscScroll = new WPlainPanel();
        WLabel tabMiscLblBlank = new WLabel(Text.of(""));
        //
        WLabel tabMiscLblSkull = new WLabel(Text.of("Player Heads"));
        tabMiscLblSkull.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabMiscBtnSkullOwner = new WButton(Text.of("Owner"));
        WTextField tabMiscTxtSkullOwner = new WTextField();
        tabMiscTxtSkullOwner.setMaxLength(16);
        tabMiscBtnSkullOwner.setOnClick(() -> {
            String inp = tabMiscTxtSkullOwner.getText();
            NbtElement inpEl;
            final MinecraftClient client = MinecraftClient.getInstance();
            if(!inp.equals("")) {
                inp = "\""+inp+"\"";
                inpEl = BlackMagick.elementFromString(inp);
                if(client.player.getMainHandStack().isEmpty())
                    BlackMagick.setNbt(BlackMagick.setId("player_head"),"SkullOwner",inpEl,NbtElement.STRING_TYPE);
                else
                    BlackMagick.setNbt(null,"SkullOwner",inpEl,NbtElement.STRING_TYPE);
            }
            else {
                if(client.player.getMainHandStack().isEmpty())
                    BlackMagick.setId("player_head");
                else
                    BlackMagick.removeNbt(null,"SkullOwner");
            }
        });
        //
        WButton tabMiscBtnSkullName = new WButton(Text.of("Name"));
        WTextField tabMiscTxtSkullName = new WTextField();
        tabMiscTxtSkullName.setMaxLength(256);
        WButton tabMiscBtnSection = new WButton(Text.of("\u00a7mS"));
        tabMiscBtnSection.setOnClick(() -> {
            tabMiscTxtSkullName.setText(tabMiscTxtSkullName.getText()+"\u00a7");
        });
        tabMiscBtnSkullName.setOnClick(() -> {
            String inp = tabMiscTxtSkullName.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inp = "\'"+inp+"\'";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"SkullOwner/Name",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"SkullOwner/Name");
        });
        //
        WButton tabMiscBtnSkullGive = new WButton(Text.of("Skin"));
        WTextField tabMiscTxtSkullGive = new WTextField();
        tabMiscTxtSkullGive.setMaxLength(1024);
        tabMiscBtnSkullGive.setOnClick(() -> {
            String value = tabMiscTxtSkullGive.getText();
            String id = tabMiscTxtSkullGive.getText();
            final MinecraftClient client = MinecraftClient.getInstance();
            if(!client.player.getAbilities().creativeMode)
                return;
            CLICK:
            if(value.contains("Value:\"") && id.contains("Id:[I;")) {
                value = value.substring(value.indexOf("Value:\"")+7);
                id = id.substring(id.indexOf("Id:[I;")+6);
                if(value.contains("\"") && id.contains("]")) {
                    value = value.substring(0,value.indexOf("\""));
                    value = "\""+value+"\"";
                    int[] idArray = new int[4];
                    try {
                    for(int i=0; i<4; i++) {
                        if(i!=3 && id.contains(",")) {
                            idArray[i] = Integer.parseInt(id.substring(0,id.indexOf(",")));
                            id = id.substring(id.indexOf(",")+1);
                        } else if(i==3 && id.contains("]"))
                            idArray[i] = Integer.parseInt(id.substring(0,id.indexOf("]")));
                        else
                            break CLICK;
                    }} catch(NumberFormatException e) {
                        break CLICK;
                    }
                    ItemStack item;
                    if(client.player.getMainHandStack().isEmpty()) {
                        item = BlackMagick.setId("player_head");
                        BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value))
                        .getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                    }
                    else {
                        item = client.player.getMainHandStack();
                        BlackMagick.setNbt(item,"SkullOwner/Properties/textures/0:/Value",BlackMagick.elementFromString(value))
                        .getNbt().getCompound("SkullOwner").putIntArray("Id",idArray);
                    }
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    ItemBuilder.updateItem(item);
                }
            }
            else if(tabMiscTxtSkullGive.getText().equals("")) {
                BlackMagick.removeNbt(null,"SkullOwner/Id");
                BlackMagick.removeNbt(null,"SkullOwner/Properties");
            }
        });
        //
        WLabel tabMiscLblBook = new WLabel(Text.of("Books"));
        tabMiscLblBook.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabMiscBtnTitle = new WButton(Text.of("title"));
        WTextField tabMiscTxtTitle = new WTextField();
        tabMiscTxtTitle.setMaxLength(256);
        tabMiscBtnTitle.setOnClick(() -> {
            String inp = tabMiscTxtTitle.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inp = "\'"+inp+"\'";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"title",inpEl,NbtElement.STRING_TYPE);
                BlackMagick.setNbt(null,"filtered_title",inpEl,NbtElement.STRING_TYPE);
            }
            else {
                BlackMagick.removeNbt(null,"title");
                BlackMagick.removeNbt(null,"filtered_title");
            }
        });
        //
        WButton tabMiscBtnAuthor = new WButton(Text.of("author"));
        WTextField tabMiscTxtAuthor = new WTextField();
        tabMiscTxtAuthor.setMaxLength(256);
        tabMiscBtnAuthor.setOnClick(() -> {
            String inp = tabMiscTxtAuthor.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inp = "\'"+inp+"\'";
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"author",inpEl,NbtElement.STRING_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"author");
        });
        //
        WButton tabMiscBtnGeneration = new WButton(Text.of("generation"));
        WTextField tabMiscTxtGeneration = new WTextField();
        tabMiscTxtGeneration.setMaxLength(2);
        tabMiscBtnGeneration.setOnClick(() -> {
            String inp = tabMiscTxtGeneration.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,"generation",inpEl,NbtElement.NUMBER_TYPE);
            }
            else
                BlackMagick.removeNbt(null,"generation");
        });
        //
        WLabel tabMiscLblFish = new WLabel(Text.of("Tropical Fish Bucket"));
        tabMiscLblFish.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabMiscBtnFish = new WButton(Text.of("Variant"));
        WTextField tabMiscTxtFishSize = new WTextField();
        tabMiscTxtFishSize.setMaxLength(1);
        WLabel tabMiscLblFishHelp = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("Size | Pattern | Base Color | Pattern Color"),Text.of(""),
                Text.of("Size-"),Text.of("0-Small   1-Large"),Text.of(""),
                Text.of("Pattern-"),Text.of("0-Kob/Flopper   1-Sunstreak/Stripey   2-Snooper/Glitter"),
                Text.of("3-Dasher/Blockfish   4-Brinely/Betty   5-Spotty/Clayfish"),Text.of(""),
                Text.of("Color-"),Text.of("0-White   1-Orange   2-Magenta   3-Light Blue"),
                Text.of("4-Yellow   5-Lime   6-Pink   7-Gray"),
                Text.of("8-Light Gray   9-Cyan   10-Purple   11-Blue"),
                Text.of("12-Brown   13-Green   14-Red   15-Black"));
            }
        };
        WTextField tabMiscTxtFishPattern = new WTextField();
        tabMiscTxtFishPattern.setMaxLength(1);
        WTextField tabMiscTxtFishBaseColor = new WTextField();
        tabMiscTxtFishBaseColor.setMaxLength(2);
        WTextField tabMiscTxtFishPatternColor = new WTextField();
        tabMiscTxtFishPatternColor.setMaxLength(2);
        tabMiscBtnFish.setOnClick(() -> {
            CLICK: {
                String inpSize = tabMiscTxtFishSize.getText();
                String inpPattern = tabMiscTxtFishPattern.getText();
                String inpBaseColor = tabMiscTxtFishBaseColor.getText();
                String inpPatternColor = tabMiscTxtFishPatternColor.getText();
                if(inpSize.equals("") && inpPattern.equals("") && inpBaseColor.equals("") && inpPatternColor.equals("")) {
                    BlackMagick.removeNbt(null,"BucketVariantTag");
                    break CLICK;
                }
                int size,pattern,baseColor,patternColor;
                try {
                    size = Integer.parseInt(inpSize);
                    pattern = Integer.parseInt(inpPattern);
                    baseColor = Integer.parseInt(inpBaseColor);
                    patternColor = Integer.parseInt(inpPatternColor);
                } catch(NumberFormatException e) {
                    break CLICK;
                }
                if( (size<0 || size>1) || (pattern<0 || pattern>5)
                || (baseColor<0 || baseColor>15) || (patternColor<0 || patternColor>15) )
                    break CLICK;
                int inp = size +(((int)Math.pow(2,8))*pattern)+(((int)Math.pow(2,16))*baseColor)+(((int)Math.pow(2,24))*patternColor);
                final MinecraftClient client = MinecraftClient.getInstance();
                if(client.player.getMainHandStack().isEmpty()) {
                    BlackMagick.setNbt(BlackMagick.setId("tropical_fish_bucket"),"BucketVariantTag",
                        BlackMagick.elementFromString(""+inp),NbtElement.NUMBER_TYPE);
                }
                else
                    BlackMagick.setNbt(null,"BucketVariantTag",BlackMagick.elementFromString(""+inp),NbtElement.NUMBER_TYPE);
            }
        });
        //
        WLabel tabMiscLblMap = new WLabel(Text.of("Maps"));
        tabMiscLblMap.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //
        WButton tabMiscBtnMap = new WButton(Text.of("map #"));
        WTextField tabMiscTxtMap = new WTextField();
        tabMiscTxtMap.setMaxLength(10);
        tabMiscBtnMap.setOnClick(() -> {
            String inp = tabMiscTxtMap.getText();
            if(!inp.equals("")) {
                final MinecraftClient client = MinecraftClient.getInstance();
                ItemStack item = null;
                if(client.player.getAbilities().creativeMode && client.player.getMainHandStack().isEmpty())
                    item = BlackMagick.setId("filled_map");
                else if(client.player.getAbilities().creativeMode)
                    item = client.player.getMainHandStack();
                try{
                    BlackMagick.setNbt(item,"map",NbtInt.of(Integer.parseInt(inp)));
                } catch(NumberFormatException e) {
                    return;
                }
            }
            else
                BlackMagick.removeNbt(null,"map");
        });
        //
        WButton tabMiscBtnDecor = new WButton(Text.of("Decor"));
        WTextField tabMiscTxtDecor = new WTextField();
        tabMiscTxtDecor.setMaxLength(256);
        WLabel tabMiscLblDecor = new WLabel(Text.of("[?]")){
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(Text.of("decor index, id (Name), type (0-26), x, z, rot (180 is upright)"),Text.of(""),
                Text.of("type"),
                Text.of("0- player marker  1- frame marker  2- red marker"),
                Text.of("3- blue marker  4- white x  5- red triangle"),
                Text.of("6- large white dot  7- small white dot  8- mansion"),
                Text.of("9- monument 10-25 banners  26- red x"));
            }
        };
        tabMiscBtnDecor.setOnClick(() -> {
            String inp = tabMiscTxtDecor.getText();
            if(inp.equals(""))
                BlackMagick.removeNbt(null,"Decorations");
            else {
                String[] inps = new String[6];
                inp = inp+",,,,,,,";
                for(int i=0; i<6; i++) {
                    inps[i]=inp.substring(0,inp.indexOf(","));
                    inp = inp.substring(inp.indexOf(",")+1);
                }
                if(!inps[0].equals("") && inps[1].equals("") && inps[2].equals("") && inps[3].equals("")
                && inps[4].equals("") && inps[5].equals(""))
                    BlackMagick.removeNbt(null,"Decorations/"+inps[0]+":");
                else if(!inps[0].equals("") && !inps[1].equals("") && !inps[2].equals("") && !inps[3].equals("")
                && !inps[4].equals("") && !inps[5].equals("")) {
                    int i = 0;
                    Byte t = 0;
                    Double x = 0.0;
                    Double z = 0.0;
                    Double r = 0.0;
                    try {
                        i = Integer.parseInt(inps[0]);
                        t = Byte.parseByte(inps[2]);
                        x = Double.parseDouble(inps[3]);
                        z = Double.parseDouble(inps[4]);
                        r = Double.parseDouble(inps[5]);
                        if(i<0)
                            return;
                    } catch(NumberFormatException e) {
                        return;
                    }
                    BlackMagick.setNbt(null,"Decorations/"+i+":/id",NbtString.of(inps[1]));
                    BlackMagick.setNbt(null,"Decorations/"+i+":/type",NbtByte.of(t));
                    BlackMagick.setNbt(null,"Decorations/"+i+":/x",NbtDouble.of(x));
                    BlackMagick.setNbt(null,"Decorations/"+i+":/z",NbtDouble.of(z));
                    BlackMagick.setNbt(null,"Decorations/"+i+":/rot",NbtDouble.of(r));
                }
            }
        });
        //
        WButton tabMiscBtnSound = new WButton(Text.of("Sound"));
        WTextField tabMiscTxtSound = new WTextField();
        tabMiscTxtSound.setMaxLength(512);
        tabMiscBtnSound.setOnClick(() -> {
            if(!tabMiscTxtSound.getText().trim().equals("")) {
                String sound = tabMiscTxtSound.getText().trim();
                sound = sound.replaceAll("[^a-zA-Z0-9.]","");
                if(!sound.equals("")) {
                    final MinecraftClient client = MinecraftClient.getInstance();
                    client.player.playSound(SoundEvent.of(new Identifier(sound)), SoundCategory.MASTER, 1, 1);
                }
            }
        });
        WButton tabMiscBtnHeadSound = new WButton(Text.of("Head Sound"));
        tabMiscBtnHeadSound.setOnClick(() -> {
            if(!tabMiscTxtSound.getText().trim().equals("")) {
                String sound = tabMiscTxtSound.getText().trim();
                sound = sound.replaceAll("[^a-zA-Z0-9.]","");
                if(!sound.equals("")) {
                    final MinecraftClient client = MinecraftClient.getInstance();
                    if(client.player.getMainHandStack().isEmpty()) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putString("id","player_head");
                        nbt.putInt("Count",1);
                        NbtCompound tag = new NbtCompound();
                        NbtCompound BET = new NbtCompound();
                        BET.putString("note_block_sound",sound);
                        tag.put("BlockEntityTag",BET);
                        NbtList lore = new NbtList();
                        lore.add(NbtString.of("{\"text\":\"Note block sound:\",\"italic\":false,\"color\":\"gray\"}"));
                        lore.add(NbtString.of("{\"text\":\""+sound+"\",\"italic\":false,\"color\":\"gray\"}"));
                        NbtCompound display = new NbtCompound();
                        display.put("Lore",lore);
                        tag.put("display",display);
                        nbt.put("tag",tag);
                        ItemStack item = ItemStack.fromNbt(nbt);
                        if(item != null && client.player.getAbilities().creativeMode) {
                            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                            client.player.playerScreenHandler.sendContentUpdates();
                            updateItem(item);
                        }
                    }
                    else {
                        BlackMagick.setNbt(null,"BlockEntityTag/note_block_sound",NbtString.of(sound));
                        NbtList lore = new NbtList();
                        lore.add(NbtString.of("{\"text\":\"Note block sound:\",\"italic\":false,\"color\":\"gray\"}"));
                        lore.add(NbtString.of("{\"text\":\""+sound+"\",\"italic\":false,\"color\":\"gray\"}"));
                        BlackMagick.setNbt(null,"display/Lore",lore);
                    }
                }
            }
            else {
                BlackMagick.removeNbt(null,"BlockEntityTag/note_block_sound");
                BlackMagick.removeNbt(null,"display/Lore");
            }
        });
        //
        tabMiscScroll.add(tabMiscLblSkull,120,5+7,0,0);
        tabMiscScroll.add(tabMiscBtnSkullOwner,5,5+1+22,40,20);
        tabMiscScroll.add(tabMiscTxtSkullOwner,50,5+1+22,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnSkullName,5,5+1+22*2,40,20);
        tabMiscScroll.add(tabMiscTxtSkullName,50,5+1+22*2,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnSection,50+230-8-50-13-10+5,5+1+22*2,15,20);
        tabMiscScroll.add(tabMiscBtnSkullGive,5,5+1+22*3,40,20);
        tabMiscScroll.add(tabMiscTxtSkullGive,50,5+1+22*3,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscLblBook,120,5+7+22*4,0,0);
        tabMiscScroll.add(tabMiscBtnTitle,5,5+1+22*5,40,20);
        tabMiscScroll.add(tabMiscTxtTitle,50,5+1+22*5,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnAuthor,5,5+1+22*6,40,20);
        tabMiscScroll.add(tabMiscTxtAuthor,50,5+1+22*6,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnGeneration,5,5+1+22*7,60,20);
        tabMiscScroll.add(tabMiscTxtGeneration,50+20,5+1+22*7,230-8-50-13-10-20,22);
        tabMiscScroll.add(tabMiscLblFish,120,5+7+22*8,0,0);
        tabMiscScroll.add(tabMiscBtnFish,5,5+1+22*9,60,20);
        tabMiscScroll.add(tabMiscTxtFishSize,50+20,5+1+22*9,(230-8-50-13-10-20)/4-2,22);
        tabMiscScroll.add(tabMiscTxtFishPattern,50+20+(230-8-50-13-10-20)/4,5+1+22*9,(230-8-50-13-10-20)/4-1,22);
        tabMiscScroll.add(tabMiscTxtFishBaseColor,50+20+(230-8-50-13-10-20)/4*2+1,5+1+22*9,(230-8-50-13-10-20)/4-1,22);
        tabMiscScroll.add(tabMiscTxtFishPatternColor,50+20+(230-8-50-13-10-20)/4*3+2,5+1+22*9,(230-8-50-13-10-20)/4-1,22);
        tabMiscScroll.add(tabMiscLblFishHelp,50+20+230-8-50-13-10-20+5,5+1+22*9+6,13,7);
        tabMiscScroll.add(tabMiscLblMap,120,5+7+22*10,0,0);
        tabMiscScroll.add(tabMiscBtnMap,5,5+1+22*11,40,20);
        tabMiscScroll.add(tabMiscTxtMap,50,5+1+22*11,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnDecor,5,5+1+22*12,40,20);
        tabMiscScroll.add(tabMiscTxtDecor,50,5+1+22*12,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscLblDecor,50+20+230-8-50-13-10-20+5,5+1+22*12+6,13,7);
        tabMiscScroll.add(tabMiscBtnSound,5,5+1+22*14,40,20);
        tabMiscScroll.add(tabMiscTxtSound,50,5+1+22*14,230-8-50-13-10,22);
        tabMiscScroll.add(tabMiscBtnHeadSound,5,5+1+22*15,80,20);
        tabMiscScroll.add(tabMiscLblBlank,0,5+1+22*15,0,22+5+1-2);
        WScrollPanel tabMiscScrollPanel = new WScrollPanel(tabMiscScroll);
        tabMiscScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabMiscScrollPanel.setScrollingVertically(TriState.TRUE);
        tabMisc.add(tabMiscScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tab custom
        WPlainPanel tabCustomScroll = new WPlainPanel();
        WLabel tabCustomLblBlank = new WLabel(Text.of(""));
        //
        WButton tabCustomBtnGive = new WButton(Text.of("Give"));
        WTextField tabCustomTxtGive = new WTextField();
        tabCustomTxtGive.setMaxLength(131072);
        tabCustomBtnGive.setOnClick(() -> {
            String inp = tabCustomTxtGive.getText();
            if(inp.contains("/") && inp.indexOf("/")==0)
                inp = inp.substring(1);
            if(inp.contains("give ") && inp.indexOf("give ")==0)
                inp = inp.substring(5);
            if((inp.contains("@s ") && inp.indexOf("@s ")==0) || (inp.contains("@p ") && inp.indexOf("@p ")==0))
                    inp = inp.substring(3);
            if(inp.contains("{")) {
                String id = (inp.substring(0,inp.indexOf("{")));
                String tag = (inp.substring(inp.indexOf("{")));
                while(tag.length()>2 && tag.charAt(tag.length()-1)!='}') {
                    tag = tag.substring(0,tag.length()-1);
                    if(tag.length()==2)
                        tag = "{}";
                }
                if(tag.charAt(tag.length()-1)!='}')
                    tag = "{}";
                int Count = 1;
                try {
                    if(tag.length()<(inp.substring(inp.indexOf("{")).length())) {
                        Count = Integer.parseInt(inp.substring(inp.indexOf("{")+tag.length()+1));
                        if(Count<1)
                            Count=1;
                        if(Count>64)
                            Count=64;
                    }
                } catch(NumberFormatException ex) {}
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id",id);
                nbt.putInt("Count",Count);
                if(BlackMagick.elementFromString(tag) != null)
                    nbt.put("tag",BlackMagick.elementFromString(tag));
                ItemStack item = ItemStack.fromNbt(nbt);
                final MinecraftClient client = MinecraftClient.getInstance();
                if(item != null && client.player.getAbilities().creativeMode) {
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    updateItem(item);
                }
            }
            else if(inp.contains(" ")) {
                BlackMagick.removeNbt(null,"");
                BlackMagick.setId(inp.substring(0,inp.indexOf(" ")));
                try {
                    if(inp.length() > inp.indexOf(" ")+1) {
                        int Count = Integer.parseInt(inp.substring(inp.indexOf(" ")+1));
                        if(Count<1)
                            Count=1;
                        if(Count>64)
                            Count=64;
                        BlackMagick.setCount(""+Count);
                    }
                } catch(NumberFormatException ex) {}
            }
            else {
                BlackMagick.removeNbt(null,"");
                BlackMagick.setId(inp);
                BlackMagick.setCount("1");
            }
        });
        //
        WLabel tabCustomLblKey = new WLabel(Text.of("Key:"));
        WLabel tabCustomLblInp = new WLabel(Text.of("Value:"));
        WButton tabCustomBtnCustom = new WButton(Text.of("Set"));
        WTextField tabCustomTxtKey = new WTextField();
        tabCustomTxtKey.setMaxLength(1024);
        WTextField tabCustomTxtInp = new WTextField();
        tabCustomTxtInp.setMaxLength(1024);
        tabCustomBtnCustom.setOnClick(() -> {
            String inp = tabCustomTxtInp.getText();
            NbtElement inpEl;
            if(!inp.equals("")) {
                inpEl = BlackMagick.elementFromString(inp);
                BlackMagick.setNbt(null,tabCustomTxtKey.getText(),inpEl);
            }
            else
                BlackMagick.removeNbt(null,tabCustomTxtKey.getText());
        });
        //
        WLabel tabCustomLblSetPath = new WLabel(Text.of("Key:"));
        WLabel tabCustomLblGetPath = new WLabel(Text.of("From:"));
        WButton tabCustomBtnCustomFrom = new WButton(Text.of("Set"));
        WTextField tabCustomTxtSetPath = new WTextField();
        tabCustomTxtSetPath.setMaxLength(1024);
        WTextField tabCustomTxtGetPath = new WTextField();
        tabCustomTxtGetPath.setMaxLength(1024);
        tabCustomBtnCustomFrom.setOnClick(() -> {
            NbtElement inp = BlackMagick.getNbtFromPath(null,tabCustomTxtGetPath.getText());
            BlackMagick.setNbt(null,tabCustomTxtSetPath.getText(),inp);
        });
        //
        WButton tabCustomBtnGiveDisabled = new WButton(Text.of("Bundle Item"));
        WTextField tabCustomTxtGiveDisabled = new WTextField();
        tabCustomTxtGiveDisabled.setMaxLength(64);
        tabCustomBtnGiveDisabled.setOnClick(() -> {
            String inp = tabCustomTxtGiveDisabled.getText();
            inp = inp.replaceAll("[^a-zA-Z_]","");
            if(inp.length()>1) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id","bundle");
                nbt.putInt("Count",1);
                NbtCompound tag = new NbtCompound();
                NbtCompound innerItem = new NbtCompound();
                innerItem.putString("id",inp);
                innerItem.putInt("Count",1);
                NbtList items = new NbtList();
                items.add(innerItem);
                tag.put("Items",items);
                nbt.put("tag",tag);
                ItemStack item = ItemStack.fromNbt(nbt);
                final MinecraftClient client = MinecraftClient.getInstance();
                if(item != null && client.player.getAbilities().creativeMode) {
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    updateItem(item);
                }
            }
        });
        //
        tabCustomScroll.add(tabCustomBtnGive,5,5+1,40,20);
        tabCustomScroll.add(tabCustomTxtGive,50,5+1,230-8-50-13-10,22);
        tabCustomScroll.add(tabCustomLblKey,5+3,5+7+22*2,40,20);
        tabCustomScroll.add(tabCustomTxtKey,50,5+1+22*2,230-8-50-13-10,22);
        tabCustomScroll.add(tabCustomLblInp,5+3,5+7+22*3,40,20);
        tabCustomScroll.add(tabCustomTxtInp,50,5+1+22*3,230-8-50-13-10,22);
        tabCustomScroll.add(tabCustomBtnCustom,5,5+1+22*4,40,20);
        tabCustomScroll.add(tabCustomLblSetPath,5+3,5+7+22*6,40,20);
        tabCustomScroll.add(tabCustomTxtSetPath,50,5+1+22*6,230-8-50-13-10,22);
        tabCustomScroll.add(tabCustomLblGetPath,5+3,5+7+22*7,40,20);
        tabCustomScroll.add(tabCustomTxtGetPath,50,5+1+22*7,230-8-50-13-10,22);
        tabCustomScroll.add(tabCustomBtnCustomFrom,5,5+1+22*8,40,20);
        tabCustomScroll.add(tabCustomBtnGiveDisabled,5,5+1+22*10,80,20);
        tabCustomScroll.add(tabCustomTxtGiveDisabled,90,5+1+22*10,230-8-50-40-13-10,22);
        tabCustomScroll.add(tabCustomLblBlank,0,5+1+22*10,0,22+5+1-2);
        WScrollPanel tabCustomScrollPanel = new WScrollPanel(tabCustomScroll);
        tabCustomScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabCustomScrollPanel.setScrollingVertically(TriState.TRUE);
        tabCustom.add(tabCustomScrollPanel,5,5,12*20-10,9*22-30-30-10);

        //tab saved
        WPlainPanel tabSavedScroll = new WPlainPanel();
        WLabel tabSavedLblBlank = new WLabel(Text.of(""));
        //
        final int SAVED_HEIGHT = 15;
        saved = new ItemStack[9][SAVED_HEIGHT];
        //
        tabSavedMode = false;
        WButton tabSavedBtnMode = new WButton(Text.of("V"));
        tabSavedBtnMode.setOnClick(() -> {
            if(tabSavedBtnMode.getLabel().getString().equals("V")) {
                tabSavedBtnMode.setLabel(Text.of("C"));
                tabSavedMode = true;
            }
            else {
                tabSavedBtnMode.setLabel(Text.of("V"));
                tabSavedMode = false;
            }
        });
        //
        tabSavedBtnGrid = new WButton[9][SAVED_HEIGHT];
        for(int x=0;x<tabSavedBtnGrid.length;x++)
            for(int y=0;y<tabSavedBtnGrid[0].length;y++) {
                tabSavedBtnGrid[x][y] = new WButton() {
                    public void addTooltip(TooltipBuilder tooltip) {
                        ItemStack item = null;
                        SEARCH_BUTTONS:
                        for(int xx=0;xx<tabSavedBtnGrid.length;xx++)
                            for(int yy=0;yy<tabSavedBtnGrid[0].length;yy++)
                                if(this.equals(tabSavedBtnGrid[xx][yy])) {
                                    item = saved[xx][yy];
                                    createTooltip(tooltip,item);
                                    break SEARCH_BUTTONS;
                                }
                    }
                };
                {
                    final int XX = x;
                    final int YY = y;
                    tabSavedBtnGrid[x][y].setOnClick(() -> {
                        if(tabSavedMode) {
                            final MinecraftClient client = MinecraftClient.getInstance();
                            if(!client.player.getMainHandStack().isEmpty()) {
                                ItemStack item = client.player.getMainHandStack().copy();
                                saveItemToFile(item,XX,YY);
                            }
                            else {
                                saveItemToFile(null,XX,YY);
                            }
                        }
                        else
                            getSavedItem(XX,YY);
                    });
                }
                tabSavedScroll.add(tabSavedBtnGrid[x][y],20*x,6+20*y,20,20);
            }
        //
        reloadSaved();
        //
        tabSaved.add(tabSavedBtnMode,10,11,20,20);
        tabSavedScroll.add(tabSavedLblBlank,0,5+1+20*tabSavedBtnGrid[0].length-20+2,0,20+5+1-2);
        WScrollPanel tabSavedScrollPanel = new WScrollPanel(tabSavedScroll);
        tabSavedScrollPanel.setScrollingHorizontally(TriState.FALSE);
        tabSavedScrollPanel.setScrollingVertically(TriState.TRUE);
        tabSaved.add(tabSavedScrollPanel,5+33,5,12*20-10-33,9*22-30-30-10);

        //add items
        tabs.add(tabGeneral, tab -> tab.icon(new ItemIcon(new ItemStack(Items.GOLDEN_SWORD))).tooltip(Text.of("General")));
        tabs.add(tabDisplay, tab -> tab.icon(new ItemIcon(new ItemStack(Items.NAME_TAG))).tooltip(Text.of("Display")));
        tabs.add(tabEnch, tab -> tab.icon(new ItemIcon(new ItemStack(Items.ENCHANTED_BOOK))).tooltip(Text.of("Enchantments")));
        tabs.add(tabBlock, tab -> tab.icon(new ItemIcon(new ItemStack(Items.BARREL))).tooltip(Text.of("Blocks")));
        tabs.add(tabEntity, tab -> tab.icon(new ItemIcon(new ItemStack(Items.BAT_SPAWN_EGG))).tooltip(Text.of("Entity Data")));
        tabs.add(tabMisc, tab -> tab.icon(new ItemIcon(new ItemStack(Items.PLAYER_HEAD))).tooltip(Text.of("Misc")));
        tabs.add(tabCustom, tab -> tab.icon(new ItemIcon(new ItemStack(Items.COMMAND_BLOCK))).tooltip(Text.of("Custom NBT")));
        tabs.add(tabSaved, tab -> tab.icon(new ItemIcon(new ItemStack(Items.JIGSAW))).tooltip(Text.of("Saved Items")));
        root.add(btnBack,5,5,40,20);
        //root.add(lblMenu,120,11,0,0);
        root.add(btnSlotCopyToOff,120-15-15-20-1,5,20,20);
        root.add(btnSlotOff,120-15-15,5,15,20);
        root.add(btnSlotLeft,120-15,5,15,20);
        root.add(btnSlotRight,120,5,15,20);
        root.add(btnSlotThrow,120+15,5,15,20);
        root.add(btnSlotThrowCopy,120+15+15,5,20,20);
        root.add(handItem,240-25,7);
        root.add(tabs,0,30,12*20,9*22-30);
        
    }

    //update WItem in corner using current mainhand
    public static void updateItem() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateItem(client.player.getMainHandStack());
    }

    //update WItem in corner using input item
    public static void updateItem(ItemStack i) {
        List<ItemStack> list = List.of(i);
        handItem.setItems(list);
    }

    //return banner item from char and colors (and sets item)
    //if invalid char, returns null and sets item to baseColor banner with no nbt
    public static ItemStack createBanner(int baseColor, int charColor, String text, String baseColorString, String charColorString) {
        ItemStack item = BlackMagick.setId(baseColorString+"_banner");
        BlackMagick.removeNbt(item,"");
        switch(text.substring(0,1)) {
            case "A": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "B": item = BlackMagick.setId(charColorString+"_banner");
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mc"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "C": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "D": item = BlackMagick.setId(charColorString+"_banner");
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("vh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "E": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "F": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "G": item = BlackMagick.setId(charColorString+"_banner");
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("vh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "H": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "I": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "J": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "K": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "L": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("vh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
            case "M": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("tt"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("tts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "N": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "O": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "P": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "Q": item = BlackMagick.setId(charColorString+"_banner");
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("br"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "R": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "S": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "T": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("cs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
            case "U": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "V": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rd"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "W": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bt"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "X": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
            case "Y": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "Z": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
            case "0": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "1": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("tl"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "2": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "3": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("cbo"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Pattern",NbtString.of("bo"));break;
            case "4": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "5": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
            case "6": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Pattern",NbtString.of("bo"));break;
            case "7": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
            case "8": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("dls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            case "9": BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
                BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
            default: return null;
        }
        return item;
    }

    //refreshes saved item list from file, closing file right after caching items
    //fills in buttons and saved[][]
    //creates file if DNE
    //called upon menu opening, after saving items
    public static void reloadSaved() {
        for(int x=0;x<saved.length;x++)
            for(int y=0;y<saved[0].length;y++) {
                saved[x][y]=null;
            }
        final MinecraftClient client = MinecraftClient.getInstance();
        File savedFile = new File(client.runDirectory.getAbsolutePath()+"/42edit_saved.txt");
        if(savedFile.exists()) {
            try {
                Scanner scan = new Scanner(savedFile);
                while(scan.hasNextLine()) {
                    int x = Integer.parseInt(scan.nextLine());
                    int y = Integer.parseInt(scan.nextLine());
                    String id = scan.nextLine();
                    int Count = Integer.parseInt(scan.nextLine());
                    NbtCompound tag = (NbtCompound)BlackMagick.elementFromString(scan.nextLine());
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("id",id);
                    nbt.putInt("Count",Count);
                    if(tag != null)
                        nbt.put("tag",tag);
                    saved[x][y] = ItemStack.fromNbt(nbt);
                }
                scan.close();
            } catch(Exception ex) {}
        }
        else {
            try {
                savedFile.createNewFile();
            } catch(IOException ex) {}
        }
        //
        for(int x=0;x<tabSavedBtnGrid.length;x++)
            for(int y=0;y<tabSavedBtnGrid[0].length;y++)
                if(saved[x][y]!=null)
                    tabSavedBtnGrid[x][y].setIcon(new ItemIcon(saved[x][y]));
                else
                    tabSavedBtnGrid[x][y].setIcon(new ItemIcon(Items.AIR));
    }

    //rewrites file based on saved[][], closing file right after
    //creates file if DNE
    //then reloads
    //called when item is saved
    public static void saveItemToFile(ItemStack item, int x, int y) {
        saved[x][y]=item;
        final MinecraftClient client = MinecraftClient.getInstance();
        File savedFile = new File(client.runDirectory.getAbsolutePath()+"/42edit_saved.txt");
        if(savedFile.exists()) {
            savedFile.delete();
        }
        try {
            savedFile.createNewFile();
            FileWriter file = new FileWriter(savedFile);
            for(int xx=0;xx<saved.length;xx++)
                for(int yy=0;yy<saved[0].length;yy++)
                    if(saved[xx][yy] != null) {
                        ItemStack currentItem = saved[xx][yy];
                        file.write(""+xx+"\n");
                        file.write(""+yy+"\n");
                        file.write(currentItem.getItem().toString()+"\n");
                        file.write(""+currentItem.getCount()+"\n");
                        if(currentItem.hasNbt())
                            file.write(currentItem.getNbt().asString()+"\n");
                        else
                            file.write(" \n");
                    }
            file.close();
        } catch(IOException ex) {}
        reloadSaved();
    }

    //gets item at x,y if it exists
    public static void getSavedItem(int x, int y) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.player.getAbilities().creativeMode) {
            ItemStack item = saved[x][y];
            if(item!=null) {
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
                updateItem(item);
            }
        }
    }

    //creates a basic tooltip to be seen in the saved items menu
    public static void createTooltip(TooltipBuilder tooltip, ItemStack item) {
        if(item != null) {
                tooltip.add(item.getName());
                tooltip.add(Text.of(item.getItem().toString()+" ("+item.getCount()+")"));
                //
                String itemData = "";
                if(item.hasNbt()) {
                    itemData += item.getNbt().asString();
                }
                //remove SkullOwner Properties
                while(itemData.contains("Properties:{textures:")) {
                    int propertiesIndex = itemData.indexOf("Properties:{textures:");
                    String firstHalf = itemData.substring(0,propertiesIndex)+"Properties:{...}";
                    String secondHalf = itemData.substring(propertiesIndex+21);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('{');
                        nextCloseBracket = secondHalf.indexOf('}');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                //remove SkullOwner UUID
                while(itemData.contains("Id:[I;")) {
                    int idIndex = itemData.indexOf("Id:[I;");
                    String firstHalf = itemData.substring(0,idIndex)+"Id:[...]";
                    String secondHalf = itemData.substring(idIndex+6);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('[');
                        nextCloseBracket = secondHalf.indexOf(']');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                //remove SkullOwnerOrig
                while(itemData.contains("SkullOwnerOrig:[I;")) {
                    int origIndex = itemData.indexOf("SkullOwnerOrig:[I;");
                    String firstHalf = itemData.substring(0,origIndex)+"\u00a74\u26a0\u00a7rSkullOwnerOrig:[...]";
                    String secondHalf = itemData.substring(origIndex+18);
                    int bracketCount = 1;
                    int nextOpenBracket;
                    int nextCloseBracket;
                    while(bracketCount!=0 && secondHalf.length()>0) {
                        nextOpenBracket = secondHalf.indexOf('[');
                        nextCloseBracket = secondHalf.indexOf(']');
                        if((nextOpenBracket<nextCloseBracket || nextCloseBracket==-1) && nextOpenBracket!=-1) {
                            bracketCount++;
                            secondHalf = secondHalf.substring(nextOpenBracket+1);
                        }
                        else if((nextOpenBracket>nextCloseBracket || nextOpenBracket==-1) && nextCloseBracket != -1){
                            bracketCount--;
                            secondHalf = secondHalf.substring(nextCloseBracket+1);
                        }
                        else
                            secondHalf = "";
                    }
                    itemData = firstHalf + secondHalf;
                }
                int tooltipLines = 0;
                while(itemData.length()>0 && tooltipLines<42) {
                    int endIndex = 48;
                    if(itemData.length()<endIndex)
                        endIndex = itemData.length();
                    tooltip.add(Text.of(itemData.substring(0,endIndex)));
                    tooltipLines++;
                    if(itemData.length()>endIndex) {
                        itemData = itemData.substring(endIndex);
                        if(tooltipLines==42)
                            tooltip.add(Text.of("..."));
                    }
                    else
                        itemData = "";
                }
        }
    }

}
