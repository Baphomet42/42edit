package baphomethlabs.fortytwoedit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;

public class BlackMagick {

    /**
     * Set a copy of the item to the mainhand slot
     * 
     * @param item
     */
    public static void setItemMain(ItemStack item) {
        final MinecraftClient client = MinecraftClient.getInstance();
        setItem(item,36+client.player.getInventory().selectedSlot);
    }

    /**
     * Set a copy of the item to the offhand slot
     * 
     * @param item
     */
    public static void setItemOff(ItemStack item) {
        setItem(item,45);
    }

    /**
     * Set a copy of the item to the specified inventory slot
     * 
     * @param item
     * @param slot
     */
    public static void setItem(ItemStack item, int slot) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.player.getAbilities().creativeMode) {
            client.interactionManager.clickCreativeStack(item.copy(), slot);
            client.player.playerScreenHandler.sendContentUpdates();
        }
    }

    /**
     * @param inp stringified nbt element
     * @return parsed element or null if invalid
     */
    public static NbtElement nbtFromString(String inp) {
        String nbt = "{temp:"+inp+"}";
        NbtCompound temp;
        try {
            temp = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            return null;
        }
        return temp.get("temp");
    }

    /**
     * @param inp stringified nbt element
     * @param type if parsed element not type, returns null
     * @return parsed element or null if invalid
     */
    public static NbtElement nbtFromString(String inp, byte type) {
        NbtElement el = nbtFromString(inp);
        if(el != null && el.getType() != type)
            return null;
        return el;
    }

    /**
     * @param inp raw json
     * @return parsed Text or error message
     */
    public static ParsedText jsonFromString(String inp) {
        RegistryWrapper.WrapperLookup reg = DynamicRegistryManager.EMPTY;
        try {
            Text temp = Text.Serialization.fromJson(inp,reg);
            if(temp != null)
                return new ParsedText(true,temp.copy());
        } catch(Exception ex) {}
        return new ParsedText(false,Text.Serialization.fromJson("{\"text\":\"Invalid JSON\",\"color\":\"red\"}",reg).copy());
    }

    /**
     * @param inp item compound with id/count/components
     * @return stack from nbt without world registries (or empty stack if invalid)
     */
    public static ItemStack itemFromNbtStatic(NbtCompound inp) {
        return ItemStack.fromNbtOrEmpty(DynamicRegistryManager.EMPTY, inp);
    }

    /**
     * @param inp item compound with id/count/components
     * @return stack from nbt (or empty stack if invalid)
     */
    public static ItemStack itemFromNbt(NbtCompound inp) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && inp != null)
            return ItemStack.fromNbtOrEmpty(client.world.getRegistryManager(),inp);
        return ItemStack.EMPTY;
    }

    /**
     * Get compound representation of an item, or an empty compound if invalid.
     * The minecraft namespace is removed from id and components.
     * Count is removed if only 1.
     * 
     * @return compound with id/count/components (or empty compound)
     */
    public static NbtCompound itemToNbt(ItemStack item) {
        NbtCompound nbt = new NbtCompound();
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && item != null && !item.isEmpty()) {
            nbt = (NbtCompound)item.encodeAllowEmpty(client.world.getRegistryManager());
        }
        if(nbt.contains("id",NbtElement.STRING_TYPE) && nbt.getString("id").startsWith("minecraft:") && nbt.getString("id").length()>10)
            nbt.putString("id",nbt.getString("id").substring(10));
        if(nbt.contains("count",NbtElement.INT_TYPE) && nbt.getInt("count")<2)
            nbt.remove("count");
        if(nbt.contains("components",NbtElement.COMPOUND_TYPE)) {
            NbtCompound comps = nbt.getCompound("components");
            NbtCompound newComps = new NbtCompound();
            for(String k : comps.getKeys()) {
                if(k.startsWith("minecraft:") && k.length()>10) {
                    newComps.put(k.substring(10),comps.get(k));
                }
                else
                    newComps.put(k,comps.get(k));
            }
            nbt.put("components",newComps);
        }
        return nbt;
    }

    /**
     * Returns the item argument that can be used in the give command to get the item.
     * Count will appear at the end if it's higher than 1.
     * The minecraft namespace is removed from id and components.
     * 
     * @return item argument in form: stone[custom_data={foo:bar}] 4
     */
    public static String itemToGive(ItemStack item) {
        NbtCompound nbt = itemToNbt(item);
        if(nbt == null || !nbt.contains("id",NbtElement.STRING_TYPE) || nbt.getString("id").equals(""))
            return "";
        String cmd = nbt.getString("id");

        if(nbt.contains("components",NbtElement.COMPOUND_TYPE)) {
            NbtCompound comps = nbt.getCompound("components");
            cmd += "[";
            boolean first = true;
            for(String k : comps.getKeys()) {
                if(!first)
                    cmd += ",";
                else
                    first = false;

                String key = k;
                String value = comps.get(k).asString();

                if(comps.get(k).getType() == NbtElement.STRING_TYPE)
                    value = "\""+value+"\"";
                cmd += key+"="+value;
            }
            cmd += "]";
        }

        if(nbt.contains("count",NbtElement.INT_TYPE) && nbt.getInt("count") > 1)
            cmd += " " + nbt.getInt("count");
        return cmd;
    }

    /**
     * Returns components with minecraft namespace removed
     * 
     * @return copy of components compound (or empty compound)
     */
    public static NbtCompound getComps(ItemStack inp) {
        NbtCompound nbt = itemToNbt(inp);
        return(nbt.getCompound("components").copy());
    }






    //returns NbtElement from path or null if not found
    // public static NbtElement getNbtPath(NbtElement inp, String path) {
    //     //parse path into list of keys
    //     //key examples: foo "foo" foo[0]
    //     List<String> keys = new ArrayList<>();
        

    //     return getNbtPath(inp,keys);
    // }

    //returns NbtElement from path or null if not found
    // public static NbtElement getNbtPath(NbtElement inp, List<String> keys) { //TODO getNbt
    //     if(inp == null)
    //         return null;
    //     if(keys == null || keys.size() == 0)
    //         return inp;

    //     String nextPath;
    //     if(path.contains(".")) {
    //         if(path.length() > path.indexOf(".") + 1)
    //             nextPath = path.substring(path.indexOf(".")+1);
    //         else
    //             return null;
    //         path = path.substring(0,path.indexOf("."));
    //     }
    // }

        





        // if(path.contains("/") || path.contains(":")) {
        //     List<String> keyList = new ArrayList<>();
        //     if(!path.contains("/") && ( path.equals("0:") || path.equals("1:") ))
        //         keyList.add(path);
        //     while(path.contains("/")) {
        //         String thisString = path.substring(0,path.indexOf("/"));
        //         if(!thisString.equals(""))
        //             keyList.add(thisString);
        //         if(path.length()>path.indexOf("/")+1) {
        //             path = path.substring(path.indexOf("/")+1);
        //             if(!path.contains("/") && !path.equals(""))
        //                 keyList.add(path);
        //         }
        //         else
        //             path = "";
        //     }
        //     if(keyList.size()==0)
        //         return null;
        //     NbtCompound nbtBase = new NbtCompound();
        //     if(keyList.get(0).equals("0:")) {
        //         ItemStack item;
        //         if(overrideItem!=null)
        //             item = overrideItem.copy();
        //         else if(!client.player.getMainHandStack().isEmpty())
        //             item = client.player.getMainHandStack().copy();
        //         else
        //             return null;
        //         NbtCompound nbtTag = new NbtCompound();
        //         if(item.hasNbt()) {
        //             nbtTag= item.getNbt().copy();
        //             nbtBase.put("tag",nbtTag);
        //         }
        //         nbtBase.putInt("Count",item.getCount());
        //         nbtBase.putString("id",item.getItem().toString());
        //         ItemStack newItem = ItemStack.fromNbt(nbtBase);
        //         if(newItem.isEmpty()) {
        //             client.player.sendMessage(Text.of("Item id error (id not equal to Item.toString() "+
        //             item.getItem().toString()+")"),false);
        //             return null;
        //         }
        //     }
        //     else if(keyList.get(0).equals("1:")) {
        //         NbtCompound nbtTag = new NbtCompound();
        //         if(client.player.getOffHandStack().isEmpty())
        //             return null;
        //         ItemStack offItem = client.player.getOffHandStack().copy();
        //         if(offItem.hasNbt()) {
        //             nbtTag=offItem.getNbt().copy();
        //             nbtBase.put("tag",nbtTag);
        //         }
        //         nbtBase.putInt("Count",offItem.getCount());
        //         nbtBase.putString("id",offItem.getItem().toString());
        //         ItemStack newItem = ItemStack.fromNbt(nbtBase);
        //         if(newItem.isEmpty()) {
        //             client.player.sendMessage(Text.of("Item id error (id not equal to Item.toString() "+
        //             offItem.getItem().toString()+")"),false);
        //             return null;
        //         }
        //     }
        //     else
        //         return null;
        //     if(keyList.size()==1)
        //         return nbtBase.copy();
        //     if(keyList.size()==2) {
        //         path = keyList.get(keyList.size()-1);
        //         if(nbtBase.contains(path)) {
        //             return nbtBase.get(path);
        //         }
        //         else
        //             return null;
        //     }
        //     if(!keyList.get(1).equals("tag"))
        //         return null;
        //     NbtCompound nbt = nbtBase.getCompound("tag");
        //     keyList.remove(0);
        //     keyList.remove(0);
        //     if(keyList.size()==1) {
        //         path = keyList.get(keyList.size()-1);
        //         if(nbt.contains(path)) {
        //             return nbt.get(path);
        //         }
        //         else
        //             return null;
        //     }
        //     int[] type = new int[keyList.size()];
        //     for(int i=0; i<keyList.size(); i++) {
        //         if(keyList.get(i).contains(":")) {
        //             if(keyList.get(i).length()<2 || i==0)
        //                 return null;
        //             try {
        //                 type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
        //             } catch(NumberFormatException e) {
        //                 return null;
        //             }
        //         }
        //         type[i]=-1;
        //     }
        //     List<NbtElement> nbtList = new ArrayList<NbtElement>();
        //     nbtList.add(nbt);
        //     for(int i=0; i<keyList.size(); i++) {
        //         if(nbtList.get(i).getType()==NbtElement.COMPOUND_TYPE && ((NbtCompound)nbtList.get(i)).contains(keyList.get(i))) {
        //                 nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //         }
        //         else if(nbtList.get(i).getType()==NbtElement.LIST_TYPE && ((NbtList)nbtList.get(i)).size()>(type[i-1])) {
        //             nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
        //         }
        //         else
        //             return null;
        //     }
        //     if(type[type.length-2]==-1) {
        //         path = keyList.get(keyList.size()-1);
        //         if(((NbtCompound)nbtList.get(nbtList.size()-2)).contains(path)) {
        //             return ((NbtCompound)nbtList.get(nbtList.size()-2)).get(path);
        //         }
        //         else
        //             return null;
        //     }
        //     else {
        //         int inpIndex = type[type.length-2];
        //         if(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex)
        //             return null;
        //         return ((NbtList)nbtList.get(nbtList.size()-2)).get(inpIndex);
        //     }
        // }
        // else
        //     return null;






    //set nbt at specified path to compound/list/array given
    //adds new Compounds and Lists along the way, if none exist
    //overwrites existing keys if they have the same name
    //if itemstack input is null, gets item from user mainhand
    //public static ItemStack setNbt(ItemStack overrideItem, String inpKey, NbtElement inp) { //TODO setNbt and removeNbt
        // MinecraftClient client = MinecraftClient.getInstance();
        // if (client.player.getAbilities().creativeMode) {
        //     ItemStack item;
        //     if(overrideItem!=null)
        //         item = overrideItem.copy();
        //     else if(!client.player.getMainHandStack().isEmpty())
        //         item = client.player.getMainHandStack().copy();
        //     else
        //         return null;
        //     if(inp !=null && inpKey.equals("")) {
        //         if(inp.getType()==NbtElement.COMPOUND_TYPE) {
        //             item.setNbt((NbtCompound)inp);
        //             client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //             client.player.playerScreenHandler.sendContentUpdates();
        //             return item;
        //         }
        //     }
        //     if(inp == null)
        //         return item;
        //     else if(inp.getType()==NbtElement.COMPOUND_TYPE || inp.getType()==NbtElement.LIST_TYPE || inp.getType()==NbtElement.BYTE_ARRAY_TYPE
        //     || inp.getType()==NbtElement.INT_ARRAY_TYPE || inp.getType()==NbtElement.LONG_ARRAY_TYPE || inp.getType()==NbtElement.STRING_TYPE
        //     || inp.getType()==NbtElement.BYTE_TYPE || inp.getType()==NbtElement.SHORT_TYPE || inp.getType()==NbtElement.INT_TYPE
        //     || inp.getType()==NbtElement.LONG_TYPE || inp.getType()==NbtElement.FLOAT_TYPE || inp.getType()==NbtElement.DOUBLE_TYPE ) {
        //         if(inpKey.contains("/") || inpKey.contains(":")) {
        //             NbtCompound nbt = new NbtCompound();
        //             if(item.hasNbt())
        //                 nbt = item.getNbt().copy();
        //             List<String> keyList = new ArrayList<>();
        //             if(!inpKey.contains("/"))
        //                 return item;
        //             while(inpKey.contains("/")) {
        //                 String thisString = inpKey.substring(0,inpKey.indexOf("/"));
        //                 if(!thisString.equals(""))
        //                     keyList.add(thisString);
        //                 if(inpKey.length()>inpKey.indexOf("/")+1) {
        //                     inpKey = inpKey.substring(inpKey.indexOf("/")+1);
        //                     if(!inpKey.contains("/") && !inpKey.equals(""))
        //                         keyList.add(inpKey);
        //                 }
        //                 else
        //                     inpKey = "";
        //             }
        //             if(keyList.size()==0)
        //                 return item;
        //             else if(keyList.size()==1) {
        //                 inpKey = keyList.get(keyList.size()-1);
        //                 nbt.put(inpKey,inp);
        //                 item.setNbt(nbt);
        //                 client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //                 client.player.playerScreenHandler.sendContentUpdates();
        //                 return item;
        //             }
        //             int[] type = new int[keyList.size()];
        //             for(int i=0; i<keyList.size(); i++) {
        //                 if(keyList.get(i).contains(":")) {
        //                     if(keyList.get(i).length()<2 || i==0)
        //                         return item;
        //                     try {
        //                         type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
        //                     } catch(NumberFormatException e) {
        //                         return item;
        //                     }
        //                 }
        //                 type[i]=-1;
        //             }
        //             List<NbtElement> nbtList = new ArrayList<NbtElement>();
        //             nbtList.add(nbt);
        //             for(int i=0; i<keyList.size(); i++) {
        //                 if(nbtList.get(i).getType()==NbtElement.COMPOUND_TYPE) {
        //                     if(((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null
        //                     && ((((NbtCompound)nbtList.get(i)).get(keyList.get(i)).getType()==NbtElement.COMPOUND_TYPE
        //                     && type[i]==-1))) {
        //                         nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //                     }
        //                     else if(((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null
        //                     && ((((NbtCompound)nbtList.get(i)).get(keyList.get(i)).getType()==NbtElement.LIST_TYPE
        //                     && type[i]!=-1))) {
        //                         nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //                     }
        //                     else if(type[i]==-1) {
        //                         ((NbtCompound)nbtList.get(i)).put(keyList.get(i),new NbtCompound());
        //                         nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //                     }
        //                     else {
        //                         ((NbtCompound)nbtList.get(i)).put(keyList.get(i),new NbtList());
        //                         nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //                     }
        //                 }
        //                 else if(nbtList.get(i).getType()==NbtElement.LIST_TYPE) {
        //                     if(type[i]==-1 && i<keyList.size()-1 && ((NbtList)nbtList.get(i)).size()>type[i-1]
        //                     && ((NbtList)nbtList.get(i)).get(type[i-1])!=null
        //                     && ((NbtList)nbtList.get(i)).get(type[i-1]).getType()==NbtElement.COMPOUND_TYPE) {
        //                         nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
        //                     }
        //                     else if(type[i]==-1 && i<keyList.size()-1 && ((NbtList)nbtList.get(i)).size()>0
        //                     && ((NbtList)nbtList.get(i)).get(0)!=null
        //                     && ((NbtList)nbtList.get(i)).get(0).getType()==NbtElement.COMPOUND_TYPE) {
        //                         while(((NbtList)nbtList.get(i)).size()<=type[i-1]) {
        //                            ((NbtList)nbtList.get(i)).add(new NbtCompound());
        //                         }
        //                         nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
        //                     }
        //                     else if(type[i]==-1 && i<keyList.size()-1) {
        //                         ((NbtList)nbtList.get(i)).clear();
        //                         while(((NbtList)nbtList.get(i)).size()<=type[i-1]) {
        //                            ((NbtList)nbtList.get(i)).add(new NbtCompound());
        //                         }
        //                         nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
        //                     }
        //                     else if(i==keyList.size()-1) {
        //                         nbtList.add(new NbtCompound());
        //                     }
        //                     else
        //                         return item;
        //                 }
        //                 else
        //                     return item;
        //             }
        //             if(type[type.length-2]==-1) {
        //                 inpKey = keyList.get(keyList.size()-1);
        //                 ((NbtCompound)nbtList.get(nbtList.size()-2)).put(inpKey,inp);
        //                 item.setNbt(nbt);
        //                 client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //                 client.player.playerScreenHandler.sendContentUpdates();
        //                 return item;
        //             }
        //             else {
        //                 int inpIndex = type[type.length-2];
        //                 if(inp.getType()==NbtElement.LIST_TYPE)
        //                     return item;
        //                 if(inp.getType()==NbtElement.COMPOUND_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.COMPOUND_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtCompound());
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtCompound());
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.BYTE_ARRAY_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.BYTE_ARRAY_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtByteArray(new byte[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtByteArray(new byte[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.INT_ARRAY_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.INT_ARRAY_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtIntArray(new int[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtIntArray(new int[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.LONG_ARRAY_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.LONG_ARRAY_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtLongArray(new long[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtLongArray(new long[0]));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.STRING_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.STRING_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtString.of(""));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtString.of(""));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.DOUBLE_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.DOUBLE_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtDouble.of(0.0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtDouble.of(0.0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.FLOAT_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.FLOAT_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtFloat.of((float)0.0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtFloat.of((float)0.0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.BYTE_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.BYTE_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtByte.of((byte)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtByte.of((byte)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.SHORT_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.SHORT_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtShort.of((short)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtShort.of((short)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.INT_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.INT_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtInt.of(0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtInt.of(0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else if(inp.getType()==NbtElement.LONG_TYPE) {
        //                     if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
        //                     && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtElement.LONG_TYPE) {
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtLong.of((long)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                     else {
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).clear();
        //                         while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
        //                             ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtLong.of((long)0));
        //                         }
        //                         ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
        //                     }
        //                 }
        //                 else
        //                     return item;
        //                 item.setNbt(nbt);
        //                 client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //                 client.player.playerScreenHandler.sendContentUpdates();
        //                 return item;
        //             }
        //         }
        //         NbtCompound nbt = new NbtCompound();
        //         if(item.hasNbt())
        //             nbt = item.getNbt().copy();
        //         nbt.put(inpKey,inp);
        //         item.setNbt(nbt);
        //         client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //         client.player.playerScreenHandler.sendContentUpdates();
        //         return item;
        //     }
        //     else
        //         return item;
        // }
        // else
        //     return null;
    //    return null;
    //}



    


    //deletes nbt at specified path and returns updated item
    //if no path given, deletes all nbt
    //if path not found, nothing changes
    //if itemstack input is null, gets item from user mainhand
    //public static ItemStack removeNbt(ItemStack overrideItem, String inpKey) {
        // final MinecraftClient client = MinecraftClient.getInstance();
        // if (client.player.getAbilities().creativeMode) {
        //     ItemStack item;
        //     if(overrideItem!=null)
        //         item = overrideItem.copy();
        //     else if(!client.player.getMainHandStack().isEmpty())
        //         item = client.player.getMainHandStack().copy();
        //     else
        //         return null;
        //     boolean noInpKey = inpKey.equals("");
        //     if(noInpKey && item.hasNbt()) {
        //         item.setNbt(new NbtCompound());
        //         client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //         client.player.playerScreenHandler.sendContentUpdates();
        //         return item;
        //     }
        //     else if(!noInpKey && item.hasNbt()) {
        //         if(inpKey.contains("/") || inpKey.contains(":")) {
        //             NbtCompound nbt = item.getNbt().copy();
        //             List<String> keyList = new ArrayList<>();
        //             if(!inpKey.contains("/"))
        //                 return item;
        //             while(inpKey.contains("/")) {
        //                 String thisString = inpKey.substring(0,inpKey.indexOf("/"));
        //                 if(!thisString.equals(""))
        //                     keyList.add(thisString);
        //                 if(inpKey.length()>inpKey.indexOf("/")+1) {
        //                     inpKey = inpKey.substring(inpKey.indexOf("/")+1);
        //                     if(!inpKey.contains("/") && !inpKey.equals(""))
        //                         keyList.add(inpKey);
        //                 }
        //                 else
        //                     inpKey = "";
        //             }
        //             if(keyList.size()==0)
        //                 return item;
        //             else if(keyList.size()==1) {
        //                 item.removeSubNbt(keyList.get(keyList.size()-1));
        //                 client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //                 client.player.playerScreenHandler.sendContentUpdates();
        //                 return item;
        //             }
        //             int[] type = new int[keyList.size()];
        //             for(int i=0; i<keyList.size(); i++) {
        //                 if(keyList.get(i).contains(":")) {
        //                     if(keyList.get(i).length()<2 || i==0)
        //                         return item;
        //                     try {
        //                         type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
        //                     } catch(NumberFormatException e) {
        //                         return item;
        //                     }
        //                 }
        //                 type[i]=-1;
        //             }
        //             List<NbtElement> nbtList = new ArrayList<NbtElement>();
        //             nbtList.add(nbt);
        //             for(int i=0; i<keyList.size(); i++) {
        //                 if(nbtList.get(i).getType()==NbtElement.COMPOUND_TYPE
        //                 && ((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null) {
        //                     nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
        //                 }
        //                 else if(nbtList.get(i).getType()==NbtElement.LIST_TYPE
        //                 && ((NbtList)nbtList.get(i)).size()>type[i-1]
        //                 && ((NbtList)nbtList.get(i)).get(type[i-1])!=null) {
        //                     nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
        //                 }
        //                 else
        //                     return item;
        //             }
        //             if(type[type.length-2]==-1)
        //                 ((NbtCompound)nbtList.get(nbtList.size()-2)).remove(keyList.get(keyList.size()-1));
        //             else
        //                 ((NbtList)nbtList.get(nbtList.size()-2)).remove(type[type.length-2]);
        //             CLEAN: {
        //                 for(int i=2;i<type.length;i++) {
        //                     if(type[type.length-i]==-1 && ((NbtCompound)nbtList.get(nbtList.size()-i)).isEmpty()
        //                     && type[type.length-i-1]==-1) {
        //                         ((NbtCompound)nbtList.get(nbtList.size()-i-1)).remove(keyList.get(keyList.size()-i));
        //                     }
        //                     else if(type[type.length-i]!=-1 && ((NbtList)nbtList.get(nbtList.size()-i)).isEmpty()
        //                     && type[type.length-i-1]==-1) {
        //                         ((NbtCompound)nbtList.get(nbtList.size()-i-1)).remove(keyList.get(keyList.size()-i));
        //                     }
        //                     else
        //                         break CLEAN;
        //                 }
        //                 if(nbtList.get(1).getType()==NbtElement.COMPOUND_TYPE && ((NbtCompound)nbtList.get(1)).isEmpty()) {
        //                     ((NbtCompound)nbtList.get(0)).remove(keyList.get(0));
        //                 }
        //                 else if(nbtList.get(1).getType()==NbtElement.LIST_TYPE && ((NbtList)nbtList.get(1)).isEmpty()) {
        //                     ((NbtCompound)nbtList.get(0)).remove(keyList.get(0));
        //                 }
        //             }
        //             item.setNbt(nbt);
        //             client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //             client.player.playerScreenHandler.sendContentUpdates();
        //             return item;
        //         }
        //         item.removeSubNbt(inpKey);
        //         client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
        //         client.player.playerScreenHandler.sendContentUpdates();
        //         return item;
        //     }
        //     else
        //         return item;
        // }
        // else
        //     return null;
    //    return null;
    //}






    //move an element in a list up or down
    // public static ItemStack moveListElement(ItemStack overrideItem, String inpKey, int index, boolean up) { TODO moveListEl
    //     MinecraftClient client = MinecraftClient.getInstance();
    //     if (client.player.getAbilities().creativeMode) {
    //         ItemStack item;
    //         if(overrideItem!=null)
    //             item = overrideItem.copy();
    //         else if(!client.player.getMainHandStack().isEmpty())
    //             item = client.player.getMainHandStack().copy();
    //         else
    //             return null;
    //         if(getNbtFromPath(item, "0:/tag/"+inpKey) != null && getNbtFromPath(item, "0:/tag/"+inpKey).getType()==NbtElement.LIST_TYPE) {
    //             NbtList list = ((NbtList)getNbtFromPath(item, "0:/tag/"+inpKey)).copy();
    //             if(list.size()>index && index>=0 && !((index==0 && up) || (index==list.size()-1 && !up))) {
    //                 NbtElement el = list.remove(index);
    //                 if(up)
    //                     list.add(index-1,el);
    //                 else
    //                     list.add(index+1,el);
    //                 return setNbt(item, inpKey, list.copy());
    //             }
    //         }
    //         return item;
    //     }
    //     return null;
    // }






    //clone an element of a list to the adjacent index
    // public static ItemStack cloneListElement(ItemStack overrideItem, String inpKey, int index) {
    //     MinecraftClient client = MinecraftClient.getInstance();
    //     if (client.player.getAbilities().creativeMode) {
    //         ItemStack item;
    //         if(overrideItem!=null)
    //             item = overrideItem.copy();
    //         else if(!client.player.getMainHandStack().isEmpty())
    //             item = client.player.getMainHandStack().copy();
    //         else
    //             return null;
    //         if(getNbtFromPath(item, "0:/tag/"+inpKey) != null && getNbtFromPath(item, "0:/tag/"+inpKey).getType()==NbtElement.LIST_TYPE) {
    //             NbtList list = ((NbtList)getNbtFromPath(item, "0:/tag/"+inpKey)).copy();
    //             if(list.size()>index && index>=0) {
    //                 NbtElement el = list.get(index).copy();
    //                 list.add(index,el);
    //                 return setNbt(item, inpKey, list.copy());
    //             }
    //         }
    //         return item;
    //     }
    //     return null;
    // }

    /**
     * Get the Nbt representation of an item for pre-made banner designs for various characters
     * 
     * @param character 
     * @param baseColor the banner background color
     * @param charColor the color of the character
     * @return compound representation of an itemstack, or null
     */
    public static NbtCompound createBanner(char character, String baseColor, String charColor) {//TODO banners
        return null;
    //     ItemStack item = BlackMagick.setId(baseColorString+"_banner");
    //     item = BlackMagick.removeNbt(item,"");
    //     switch(text.substring(0,1)) {
    //         case "A": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "B": item = BlackMagick.setId(charColorString+"_banner");
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mc"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "C": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "D": item = BlackMagick.setId(charColorString+"_banner");
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("vh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "E": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "F": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "G": item = BlackMagick.setId(charColorString+"_banner");
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("vh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "H": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "I": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "J": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "K": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "L": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("vh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
    //         case "M": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("tt"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("tts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "N": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "O": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "P": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "Q": item = BlackMagick.setId(charColorString+"_banner");
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("br"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "R": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "S": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "T": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("cs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
    //         case "U": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "V": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("rd"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "W": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bt"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "X": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
    //         case "Y": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "Z": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bo"));break;
    //         case "0": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "1": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("tl"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "2": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "3": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("cbo"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Pattern",NbtString.of("bo"));break;
    //         case "4": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "5": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("bo"));break;
    //         case "6": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hh"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/6:/Pattern",NbtString.of("bo"));break;
    //         case "7": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("bo"));break;
    //         case "8": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("bs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("mr"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("drs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("dls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         case "9": item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/0:/Pattern",NbtString.of("ls"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/1:/Pattern",NbtString.of("hhb"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/2:/Pattern",NbtString.of("ts"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/3:/Pattern",NbtString.of("rs"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Color",NbtInt.of(charColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/4:/Pattern",NbtString.of("ms"));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Color",NbtInt.of(baseColor));
    //             item = BlackMagick.setNbt(item,"BlockEntityTag/Patterns/5:/Pattern",NbtString.of("bo"));break;
    //         default: return null;
    //     }
    //     return item;
    }


    /**
     * stores parsed json text or an error message if isValid is false
     */
    public record ParsedText(boolean isValid, Text text) {
        
    }

    /**
     * 
     * @param id the item id (from ItemStack.getItem().toString())
     * @return int array with [rows,columns] or [-1,-1] depending on storage size of blockentity for item
     */
    public static int[] containerSize(String id) {
        int rows = -1;
        int cols = -1;

        // find items by searching net.minecraft.item.Items for DataComponentTypes.CONTAINER
        // manually enter rows/cols based on ingame gui appearance
        // remove ender chest
        
        if(id.equals("chest") || id.equals("trapped_chest") || id.contains("shulker") || id.equals("barrel")) {
            rows = 3;
            cols = 9;
        }
        else if(id.equals("dispenser") || id.equals("dropper") || id.equals("crafter")) {
            rows = 3;
            cols = 3;
        }
        else if(id.equals("hopper")) {
            rows = 1;
            cols = 5;
        }
        else if(id.contains("furnace") || id.contains("smoker")) {
            rows = 1;
            cols = 3;
        }
        else if(id.equals("brewing_stand")) {
            rows = 1;
            cols = 5;
        }
        else if(id.equals("chiseled_bookshelf")) {
            rows = 2;
            cols = 3;
        }
        else if(id.contains("campfire")) {
            rows = 1;
            cols = 4;
        }

        return new int[]{rows,cols};
    }

    /**
     * Test if query string contains at least one string in the set
     * 
     * @param query String to test set on
     * @param set Strings that may or may not be in query
     * @return true if query contains at least one string in set
     */
    public static boolean stringContains(String query, String... set) {
        for(String s : set)
            if(query.contains(s))
                return true;
        return false;
    }

    /**
     * Test if query string equals at least one string in the set
     * 
     * @param query String to test set on
     * @param set Strings that may or may not be equal to query
     * @return true if query equals at least one string in set
     */
    public static boolean stringEquals(String query, String... set) {
        for(String s : set)
            if(query.equals(s))
                return true;
        return false;
    }


}