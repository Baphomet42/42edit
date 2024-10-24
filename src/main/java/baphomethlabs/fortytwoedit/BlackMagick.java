package baphomethlabs.fortytwoedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo.Map;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * Class containing static methods used for working with NBT, JSON Text, and more
 */
public class BlackMagick {

    /**
     * Set a copy of the item to the mainhand slot
     * 
     * @param item
     */
    public static void setItemMain(ItemStack item) {
        final MinecraftClient client = MinecraftClient.getInstance();
        setItem(item,client.player.getInventory().selectedSlot,36+client.player.getInventory().selectedSlot);
    }

    /**
     * Set a copy of the item to the offhand slot
     * 
     * @param item
     */
    public static void setItemOff(ItemStack item) {
        setItem(item,PlayerInventory.OFF_HAND_SLOT,45);
    }

    /**
     * Set a copy of the item to the specified inventory slot
     * 
     * @param item
     * @param slot
     */
    public static void setItem(ItemStack itemInput, int invSlot, int creativeSlot) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.player.getAbilities().creativeMode) {

            ItemStack item = itemInput == null ? ItemStack.EMPTY : itemInput.copy();

            // If item is not enabled, sets the slot to a bundle containing the item.
            // Removing the bundle item in an inventory may result in a ghost item.
            // Emptying the bundle with the use key ingame will spawn the item, and it will not be a ghost.
            if (!item.isEmpty() && !client.player.networkHandler.hasFeature(item.getItem().getRequiredFeatures())) {
                ItemStack newStack = BlackMagick.itemFromString("{id:bundle,components:{bundle_contents:["+BlackMagick.itemToNbtStorage(item).asString()+"]}}");
                if(!newStack.isEmpty()) {
                    item = newStack;
                }
            }

            FortytwoEdit.addItemHist(item);
            client.player.getInventory().setStack(invSlot, item);
            client.interactionManager.clickCreativeStack(item, creativeSlot);
            client.player.playerScreenHandler.sendContentUpdates();
        }
    }

    /**
     * Get nbt from stringified nbt.
     * Invalid types may be parsed as a string.
     * 
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
     * Get nbt from stringified nbt of a certain type.
     * 
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
     * Get stringified nbt where NbtStrings are quoted.
     * Quotes may be double or single, and some characters may be escaped.
     * A null element will return an empty string.
     * 
     * @param inp
     * @return
     */
    public static String nbtToString(NbtElement inp) {
        if(inp == null)
            return "";
        else if(inp.getType()==NbtElement.STRING_TYPE) {
            NbtCompound temp = new NbtCompound();
            temp.put("temp",inp);
            String parsed = temp.asString();
            if(parsed.startsWith("{temp:") && parsed.endsWith("}")) {
                parsed = parsed.substring(6,parsed.length()-1);
                if(BlackMagick.nbtFromString(parsed)!=null && BlackMagick.nbtFromString(parsed).getType()==NbtElement.STRING_TYPE
                && (BlackMagick.nbtFromString(parsed)).asString().equals(inp.asString())) {
                    return parsed;
                }
            }
            FortytwoEdit.LOGGER.warn("Failed to stringify NbtString: "+inp.asString());
            return inp.asString();
        }
        else
            return inp.asString();
    }

    /**
     * Validates element to be a non-null compound, otherwise returns default compound
     * 
     * @param el any element or null
     * @return non-null compound
     */
    public static NbtCompound validCompound(NbtElement el) {
        if(el == null || el.getType() != NbtElement.COMPOUND_TYPE)
            return new NbtCompound();
        return (NbtCompound)el;
    }

    /**
     * Converts json to Text object. Valid forms include {"text":""} [{"text":""}] ""
     * 
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
        return new ParsedText(false,Text.of("Invalid JSON").copy().formatted(Formatting.RED));
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
     * @param inp stringified item compound with id/count/components
     * @return stack from nbt (or empty stack if invalid)
     */
    public static ItemStack itemFromString(String inp) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && inp != null)
            return ItemStack.fromNbtOrEmpty(client.world.getRegistryManager(),BlackMagick.validCompound(BlackMagick.nbtFromString(inp,NbtElement.COMPOUND_TYPE)));
        return ItemStack.EMPTY;
    }

    /**
     * Get compound representation of an item, or an empty compound if invalid.
     * Compound appears exactly as it would with /data get
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static NbtCompound itemToNbtStorage(ItemStack item) {
        NbtCompound nbt = new NbtCompound();
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && item != null && !item.isEmpty()) {
            nbt = (NbtCompound)item.toNbtAllowEmpty(client.world.getRegistryManager());
        }
        return nbt;
    }

    /**
     * Get compound representation of an item, or an empty compound if invalid.
     * All components are kept, even if they are the default values.
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static NbtCompound itemToNbt(ItemStack item) {
        NbtCompound nbt = new NbtCompound();
        if(item != null && !item.isEmpty()) {
            NbtCompound comps = new NbtCompound();
            String compsString = componentsAsString(item.getComponents());
            if(compsString != null && compsString.length()>0)
                comps = BlackMagick.validCompound(BlackMagick.nbtFromString("{"+compsString+"}"));

            NbtCompound itemComps = BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.itemToNbtStorage(item),"components"));
            for(String k : itemComps.getKeys()) {
                if(k.startsWith("!"))
                    comps.put(k,itemComps.get(k));
            }

            if(!comps.isEmpty())
                nbt.put("components",comps);
            nbt.putInt("count",item.getCount());
            nbt.putString("id",BlackMagick.getItemId(item,true));
        }
        return nbt;
    }

    /**
     * Represent ItemStack as used in give command
     * 
     * @param item
     * @return arguments as used after /give (or empty string if invalid)
     */
    public static String itemToGive(ItemStack item) {
        return itemToGive(BlackMagick.itemToNbtStorage(item));
    }

    /**
     * Represent ItemStack as used in give command
     * 
     * @param item an item in compound form with id/count/components
     * @return arguments as used after /give (or empty string if invalid)
     */
    public static String itemToGive(NbtCompound item) {
        if(item != null && item.contains("id",NbtElement.STRING_TYPE)) {
            String cmd = item.getString("id").replace("minecraft:","");
            if(item.contains("components",NbtElement.COMPOUND_TYPE)) {
                cmd += "[";
                NbtCompound components = item.getCompound("components");
                boolean first = true;
                for(String k : components.getKeys()) {
                    String key = k.replace("minecraft:","");
                    if(!first)
                        cmd += ",";

                    if(k.startsWith("!"))
                        cmd += key;
                    else
                        cmd += key+"="+BlackMagick.nbtToString(components.get(k));

                    first = false;
                }
                cmd += "]";
            }
            if(item.contains("count",NbtElement.INT_TYPE) && item.getInt("count")>1)
                cmd += " "+item.getInt("count");
            return cmd;
        }
        return "";
    }

    // modified from net.minecraft.command.argument.ItemStackArgument
    // change = to : in return line and surround identifier in quotes
    private static String componentsAsString(ComponentMap comps) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && comps != null) {
            RegistryWrapper.WrapperLookup registries = client.world.getRegistryManager();
            RegistryOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);
            return comps.stream().flatMap(component -> {
                ComponentType<?> dataComponentType = component.type();
                Identifier identifier = Registries.DATA_COMPONENT_TYPE.getId(dataComponentType);
                Optional<NbtElement> optional = component.encode(dynamicOps).result();
                if (identifier == null || optional.isEmpty()) {
                    return Stream.empty();
                }
                return Stream.of("\""+identifier.toString() + "\":" + optional.get());
            }).collect(Collectors.joining(String.valueOf(',')));
        }
        return "";
    }


    /**
     * 
     * @param stack
     * @param namespace if id should begin with minecraft:
     * @return item id like stone or minecraft:stone
     */
    public static String getItemId(ItemStack stack, boolean namespace) {
        return getItemId(stack.getItem(), namespace);
    }

    /**
     * 
     * @param item from ItemStack.getItem()
     * @param namespace if id should begin with minecraft:
     * @return item id like stone or minecraft:stone
     */
    public static String getItemId(Item item, boolean namespace) {
        if(namespace)
            return Registries.ITEM.getId(item).toString();
        return Registries.ITEM.getId(item).getPath();
    }

    /**
     * Find invalid components in an item compound.
     * If error is found, changes inpError to the new error message.
     * Otherwise returns previous inpError.
     * 
     * @param item stringified compound with id/count/components
     * @param inpError existing error message, or null
     * @return String describing errors, or null
     */
    public static String getItemCompoundErrors(String item, String inpError) {
        String invalidMsg = "Invalid item";
        if(item==null || item.length()==0)
            return invalidMsg;
        try {
            String giveMsg = "bundle[bundle_contents=["+item+"]]";
            ItemStackArgumentType.itemStack(BlackMagick.getCommandRegistries()).parse(new StringReader(giveMsg));
        } catch(Exception ex) {
            if(ex instanceof CommandSyntaxException) {
                String err = ((CommandSyntaxException)ex).getMessage();
                String bundleErr = "Malformed 'minecraft:bundle_contents' component: ";
                if(err.startsWith(bundleErr))
                    err = err.replaceFirst(bundleErr,"");
                if(err.contains(" at position ")) {
                    err = err.substring(0,err.indexOf(" at position "));
                }
                return err;
            }
            return invalidMsg;
        }
        return inpError;
    }

    /**
     * Returns copy of element at path or null if not found
     * 
     * @param inp compound to search from
     * @param path path like "foo" "foo.bar" or "foo.bar[1]"
     * @return
     */
    public static NbtElement getNbtPath(NbtCompound inp, String path) {
        if(inp == null || inp.getType() != NbtElement.COMPOUND_TYPE)
            return null;

        try {
            NbtPath p = NbtPath.parse(path);
            List<NbtElement> list = p.get(inp);
            if(list.size() == 1) {
                NbtElement el = list.get(0);
                if(el == null)
                    return null;
                else
                    return el.copy();
            }
        } catch(Exception ex) {}
        
        return null;
    }

    /**
     * Returns element at path with matching type, or null if not found
     * 
     * @param inp compound to search from
     * @param path path like "foo" "foo.bar" or "foo.bar[1]"
     * @param type NbtElement type
     * @return
     */
    public static NbtElement getNbtPath(NbtCompound inp, String path, byte type) {
        NbtElement el = getNbtPath(inp, path);
        if(el != null && el.getType() == type)
            return el;
        return null;
    }

    /**
     * Returns a copy of a compound after the path is set to a copy of el (or removed if el == null).
     * This cannot set to an index of a list that does not yet exist, unless the path ends like foo.bar.list[]
     * 
     * @param base compound to edit
     * @param path path like "foo" "foo.bar" or "foo.bar[1]"
     * @param el element to be set (or null to remove)
     * @return copy with changes made
     */
    public static NbtCompound setNbtPath(NbtCompound base, String path, NbtElement el) {
        NbtCompound nbt;
        if(base != null)
            nbt = base.copy();
        else
            nbt = new NbtCompound();

        try {
            NbtPath p = NbtPath.parse(path);
            if(el == null)
                p.remove(nbt);
            else {
                p.put(nbt,el.copy());
                if(!path.contains("!"))
                    nbt = removeComponentLocks(nbt,path);
            }
        } catch(Exception ex) {
            return base.copy();
        }
        
        return nbt;
    }

    /**
     * Create default NbtElement for a given type
     * 
     * @param type from NbtElement.getType()
     * @return a new NbtElement or null if the type is unknown
     */
    public static NbtElement getDefaultNbt(byte type) {
        switch(type) {
            case NbtElement.COMPOUND_TYPE : return new NbtCompound();
            case NbtElement.LIST_TYPE : return new NbtList();
            case NbtElement.BYTE_TYPE : return NbtByte.ZERO;
            case NbtElement.BYTE_ARRAY_TYPE : return new NbtByteArray(new byte[0]);
            case NbtElement.SHORT_TYPE : return NbtShort.of((short)0);
            case NbtElement.INT_TYPE : return NbtInt.of(0);
            case NbtElement.INT_ARRAY_TYPE : return new NbtIntArray(new int[0]);
            case NbtElement.LONG_TYPE : return NbtLong.of((long)0);
            case NbtElement.LONG_ARRAY_TYPE : return new NbtLongArray(new long[0]);
            case NbtElement.DOUBLE_TYPE : return NbtDouble.of((double)0);
            case NbtElement.FLOAT_TYPE : return NbtFloat.of((float)0);
            case NbtElement.STRING_TYPE : return NbtString.of("");
            default: break;
        }
        FortytwoEdit.LOGGER.error("Failed to create default NbtElement for type: "+type);
        return null;
    }

    private static NbtCompound removeComponentLocks(NbtCompound base, String path) {
        if(base == null)
            return null;
        NbtCompound nbt = base.copy();

        if(path.startsWith("components.")) {
            String component = path.substring(11);
            if(component.contains("."))
                component = component.substring(0,component.indexOf("."));
            if(component.contains("["))
                component = component.substring(0,component.indexOf("["));
            
            if(nbtToString(getNbtPath(nbt,"components.!"+component)).equals("{}"))
                nbt = setNbtPath(nbt,"components.!"+component,null);
        }

        return nbt;
    }

    /**
     * Returns a copy of a compound in which a list element at the path is swapped up or down (if possible)
     * 
     * @param base compound to edit
     * @param path path like "foo" "foo.bar" or "foo.bar[1]" ending in an NbtList
     * @param index index in list
     * @param up whether to move the element up or down
     * @return copy with changes made
     */
    public static NbtCompound moveListElement(NbtCompound base, String path, int index, boolean up) {
        if(base == null)
            return null;
        if(path == null || path.length() == 0 || index<0)
            return base.copy();

        NbtCompound nbt = base.copy();

        if(getNbtPath(nbt,path,NbtElement.LIST_TYPE) != null) {
            NbtList list = (NbtList)getNbtPath(nbt,path);
            if(list.size()>index && index>=0 && !((index==0 && up) || (index==list.size()-1 && !up))) {
                NbtElement el = list.remove(index);
                if(up)
                    list.add(index-1,el);
                else
                    list.add(index+1,el);
                nbt = setNbtPath(nbt, path, list);
            }
        }

        return nbt;
    }

    /**
     * Returns a copy of a compound in which a list element at the path is cloned
     * 
     * @param base compound to edit
     * @param path path like "foo" "foo.bar" or "foo.bar[1]" ending in an NbtList
     * @param index index in list
     * @return copy with changes made
     */
    public static NbtCompound cloneListElement(NbtCompound base, String path, int index) {
        if(base == null)
            return null;
        if(path == null || path.length() == 0 || index<0)
            return base.copy();

        NbtCompound nbt = base.copy();

        if(getNbtPath(nbt,path,NbtElement.LIST_TYPE) != null) {
            NbtList list = (NbtList)getNbtPath(nbt,path);
            if(list.size()>index && index>=0) {
                NbtElement el = list.get(index).copy();
                list.add(index,el);
                nbt = setNbtPath(nbt, path, list);
            }
        }

        return nbt;
    }

    /**
     * Get list of all possible block states that can be applied to the item
     * 
     * @param item the ItemStack.getItem()
     * @return list of lists where the first string in each list is the key and the rest are the value options (may be empty but never null)
     */
    public static List<List<String>> getBlockStates(Item item) {
        List<List<String>> states = new ArrayList<>();
        BlockState blockState = Block.getBlockFromItem(item).getDefaultState();
        for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(entry.getKey().getName());
            for(Comparable<?> val : entry.getKey().getValues()) {
                list.add((String)Util.getValueAsString(entry.getKey(), val));
            }
            states.add(list);
        }
        return states;
    }

    public static List<String> getWorldEnchantmentList() {
        List<String> list = new ArrayList<>();

        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null)
                for(Identifier i : client.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).get().getIds())
                    list.add(i.toString());
        } catch(Exception e) {}

        Collections.sort(list);
        return list;
    }

    public static int getWorldEnchantmentMaxLevel(String key) {
        int max = 1;

        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null) {
                Enchantment ench = client.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).get().get(Identifier.of(key));
                if(ench != null)
                    max = ench.getMaxLevel();
            }
        } catch(Exception e) {}

        return max;
    }

    public static List<String> getWorldJukeboxList() {
        List<String> list = new ArrayList<>();

        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null)
                for(Identifier i : client.world.getRegistryManager().getOptional(RegistryKeys.JUKEBOX_SONG).get().getIds())
                    list.add(i.toString());
        } catch(Exception e) {}

        Collections.sort(list);
        return list;
    }

    public static List<String> getWorldPaintingList() {
        List<String> list = new ArrayList<>();

        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null)
                for(Identifier i : client.world.getRegistryManager().getOptional(RegistryKeys.PAINTING_VARIANT).get().getIds())
                    list.add(i.toString());
        } catch(Exception e) {}

        Collections.sort(list);
        return list;
    }

    /**
     * Get the Nbt representation of an item for pre-made banner designs for various characters
     * 
     * @param character 
     * @param baseColor the banner background color
     * @param charColor the color of the character
     * @return compound representation of an itemstack, or null
     */
    public static NbtCompound createBanner(char character, String baseColor, String charColor) {
        NbtElement el = null;
        switch(character) {
            case 'A': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'B': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+baseColor+",pattern:\"minecraft:circle\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'C': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'D': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:half_vertical\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'E': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'F': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'G': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+baseColor+",pattern:\"minecraft:half_vertical\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'H': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'I': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'J': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'K': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'L': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:half_vertical\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'M': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:triangle_top\"},{color:"+baseColor+",pattern:\"minecraft:triangles_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'N': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'O': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'P': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'Q': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:square_bottom_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'R': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'S': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'T': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'U': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'V': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:diagonal_up_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'W': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:triangle_bottom\"},{color:"+baseColor+",pattern:\"minecraft:triangles_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'X': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'Y': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case 'Z': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '0': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '1': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:square_top_left\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '2': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '3': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '4': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '5': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '6': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '7': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '8': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            case '9': el = BlackMagick.nbtFromString("{components:{banner_patterns:[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}",NbtElement.COMPOUND_TYPE); break;
            default: return null;
        }
        if(el != null)
            return (NbtCompound)el;
        return null;
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
    public static int[] containerSize(Item item) {
        int rows = -1;
        int cols = -1;

        // find items by searching net.minecraft.item.Items for DataComponentTypes.CONTAINER
        // manually enter rows/cols based on ingame gui appearance
        // remove ender chest

        String id = item.toString();
        
        if(id.contains("ender_chest")) {

        }
        else if(id.contains("chest") || id.contains("trapped_chest") || id.contains("shulker") || id.contains("barrel")) {
            rows = 3;
            cols = 9;
        }
        else if(id.contains("dispenser") || id.contains("dropper") || id.contains("crafter")) {
            rows = 3;
            cols = 3;
        }
        else if(id.contains("hopper")) {
            rows = 1;
            cols = 5;
        }
        else if(id.contains("furnace") || id.contains("smoker")) {
            rows = 1;
            cols = 3;
        }
        else if(id.contains("brewing_stand")) {
            rows = 1;
            cols = 5;
        }
        else if(id.contains("chiseled_bookshelf")) {
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

    /**
     * Returns a list of strings sorted alphabetically.
     * Treats uppercase and lowercase the same.
     * 
     * @param set
     * @return
     */
    public static List<String> sortSet(Set<String> set) {
        List<String> list = new ArrayList<>();

        for(String s : set)
            list.add(s);

        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /**
     * Returns a list of strings sorted alphabetically.
     * Treats uppercase and lowercase the same.
     * 
     * @param array
     * @return
     */
    public static List<String> sortArray(String[] array) {
        List<String> list = new ArrayList<>();

        for(String s : array)
            list.add(s);

        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /**
     * Format suggs so that they represent an NbtString.
     * Required when string suggs are used in a txt not setup for NbtStrings.
     * 
     * @param suggs
     * @return
     */
    public static String[] formatStringSuggs(String[] suggs) {
        List<String> list = new ArrayList<>();
        
        for(String s : suggs) {
            list.add(nbtToString(NbtString.of(s)));
        }

        return list.toArray(new String[0]);
    }

    /**
     * Format suggs with a prefix and suffix
     * 
     * @param suggs
     * @return
     */
    public static String[] formatSuggs(String[] suggs, String prefix, String suffix) {
        List<String> list = new ArrayList<>();
        
        for(String s : suggs) {
            list.add(prefix+s+suffix);
        }

        return list.toArray(new String[0]);
    }

    public static String[] getIntRangeArray(int min, int max) {
        if(min == max)
            return new String[]{""+min};
        if(min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        String[] arr = new String[max-min+1];
        for(int i=0; i<arr.length; i++) {
            arr[i] = "" + (min + i);
        }
        return arr;
    }

    /**
     * Converts decimal color like 4327014 to hex color like #420666.
     * Returns null if input is not a valid decimal color.
     * 
     * @param dec like 4327014
     * @return hex String like #420666 or null
     */
    public static String colorHexFromDec(String dec) {
        if(dec != null)
            try {
                int col = Integer.parseInt(dec);
                if(col>=0 && col <=16777215) {
                    String hex = Integer.toHexString(col);
                    while(hex.length()<6)
                        hex = "0"+hex;
                    if(hex.length()==6)
                        return "#"+hex;
                }
            } catch(NumberFormatException ex) {}
        return null;
    }

    /**
     * 
     * @return vanilla command registries with all features enabled
     */
    public static CommandRegistryAccess getCommandRegistries() {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null)
            return CommandRegistryAccess.of(client.world.getRegistryManager(), FortytwoEdit.FEATURES);
        return CommandRegistryAccess.of(DynamicRegistryManager.EMPTY, FortytwoEdit.FEATURES);
    }

    /**
     * Display element differences with colored text.
     * 
     * @param left
     * @param right
     * @return formatted Text listing element differences and full content
     */
    public static Text getElementDifferences(NbtElement left, NbtElement right) {
        if(left==null && right==null)
            return Text.of("null").copy().formatted(Formatting.ITALIC);
        if(left==null)
            return Text.of(BlackMagick.nbtToString(right)).copy().formatted(Formatting.GREEN);
        if(right==null)
            return Text.of(BlackMagick.nbtToString(left)).copy().formatted(Formatting.RED);
        
        if(left.getType() == right.getType()) {
            if(BlackMagick.nbtToString(left).equals(BlackMagick.nbtToString(right)))
                return Text.of(BlackMagick.nbtToString(left));
            if(left.getType() == NbtElement.COMPOUND_TYPE) {
                NbtCompound leftCmp = (NbtCompound)left;
                NbtCompound rightCmp = (NbtCompound)right;
                MutableText output = Text.empty().append(Text.of("{"));

                Set<String> allKeys = Sets.newHashSet();
                for(String k : ((NbtCompound)left).getKeys())
                    allKeys.add(k);
                for(String k : ((NbtCompound)right).getKeys())
                    allKeys.add(k);

                boolean first = true;
                for(String k : allKeys) {
                    String k2 = k.contains(":") ? ("\""+k+"\"") : k;

                    if(!first)
                        output.append(Text.of(","));
                    else
                        first = false;

                    if(leftCmp.contains(k) && rightCmp.contains(k)) {
                        output.append(Text.of(k2+":"));
                        output.append(getElementDifferences(leftCmp.get(k),rightCmp.get(k)));
                    }
                    else if(leftCmp.contains(k)) {
                        output.append((Text.of(k2+":").copy().append(Text.of(BlackMagick.nbtToString(leftCmp.get(k))))).formatted(Formatting.RED));
                    }
                    else {
                        output.append((Text.of(k2+":").copy().append(Text.of(BlackMagick.nbtToString(rightCmp.get(k))))).formatted(Formatting.GREEN));
                    }
                }

                output.append(Text.of("}"));
                return output;
            }
            if(left.getType() == NbtElement.LIST_TYPE) {
                NbtList leftList = (NbtList)left;
                NbtList rightList = (NbtList)right;
                MutableText output = Text.empty().append(Text.of("["));

                int maxSize = Math.max(((NbtList)left).size(),((NbtList)right).size());

                boolean first = true;
                for(int i=0; i<maxSize; i++) {
                    if(!first)
                        output.append(Text.of(","));
                    else
                        first = false;

                    if(leftList.size()>i && rightList.size()>i) {
                        output.append(getElementDifferences(leftList.get(i),rightList.get(i)));
                    }
                    else if(leftList.size()>i) {
                        output.append((Text.of(BlackMagick.nbtToString(leftList.get(i))).copy()).formatted(Formatting.RED));
                    }
                    else {
                        output.append((Text.of(BlackMagick.nbtToString(rightList.get(i))).copy()).formatted(Formatting.GREEN));
                    }
                }

                output.append(Text.of("]"));
                return output;
            }
        }
        return Text.empty().append(Text.of(BlackMagick.nbtToString(left)).copy().formatted(Formatting.RED)).append(Text.of(BlackMagick.nbtToString(right)).copy().formatted(Formatting.GREEN));
    }


}