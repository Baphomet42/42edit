package baphomethlabs.fortytwoedit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Class containing static methods used for working with NBT, Text components, and more
 */
public class BlackMagick {

    /**
     * Set a copy of the item to the mainhand slot
     * 
     * @param item
     */
    public static void setItemMain(ItemStack item) {
        final Minecraft client = Minecraft.getInstance();
        setItem(item,client.player.getInventory().getSelectedSlot(),36+client.player.getInventory().getSelectedSlot());
    }

    /**
     * Set a copy of the item to the offhand slot
     * 
     * @param item
     */
    public static void setItemOff(ItemStack item) {
        setItem(item,Inventory.SLOT_OFFHAND,45);
    }

    /**
     * Set a copy of the item to the head equipment slot
     * 
     * @param item
     */
    public static void setItemHead(ItemStack item) {
        setItem(item,39,5);
    }

    /**
     * Set a copy of the item to the specified inventory slot
     * 
     * @param item
     * @param slot
     */
    public static void setItem(ItemStack itemInput, int invSlot, int creativeSlot) {
        final Minecraft client = Minecraft.getInstance();
        if(client.player.getAbilities().instabuild) {

            ItemStack item = itemInput == null ? ItemStack.EMPTY : itemInput.copy();

            // If item is not enabled, sets the slot to a bundle containing the item.
            // Removing the bundle item in an inventory may result in a ghost item.
            // Emptying the bundle with the use key ingame will spawn the item, and it will not be a ghost.
            if(!item.isEmpty() && !client.player.connection.isFeatureEnabled(item.getItem().requiredFeatures())) {
                ItemStack newStack = BlackMagick.itemFromString("{id:bundle,components:{bundle_contents:["+BlackMagick.nbtToSnbt(BlackMagick.itemToNbtStorage(item))+"]}}");
                if(!newStack.isEmpty()) {
                    item = newStack;
                }
            }

            FortytwoEdit.addItemHist(client.player.getInventory().getItem(invSlot));
            FortytwoEdit.addItemHist(item);
            client.player.getInventory().setItem(invSlot, item);
            client.gameMode.handleCreativeModeItemAdd(item, creativeSlot);
            client.player.inventoryMenu.broadcastChanges();
        }
    }

    /**
     * Get nbt from snbt.
     * Invalid types may be parsed as a string.
     * 
     * @param inp snbt
     * @return parsed element or null if invalid
     */
    public static Tag nbtFromString(String inp) {
        String nbt = "{temp:"+inp+"}";
        CompoundTag temp;
        try {
            temp = TagParser.parseCompoundFully(nbt);
            if(temp.contains("temp") && temp.size()==1)
                return temp.get("temp");
        } catch(CommandSyntaxException ex) {}
        return null;
    }

    /**
     * Get nbt from snbt of a certain type
     * 
     * @param inp snbt
     * @param type if parsed element not type, returns null
     * @return parsed element or null if invalid
     */
    public static Tag nbtFromString(String inp, byte type) {
        Tag el = nbtFromString(inp);
        if(el != null && el.getId() != type)
            return null;
        return el;
    }

    /**
     * Get nbt compound from snbt (or empty compound if invalid)
     * 
     * @param inp snbt
     * @return parsed compound or empty compound if invalid
     */
    public static CompoundTag validCompoundFromString(String inp) {
        return BlackMagick.validCompound(BlackMagick.nbtFromString(inp));
    }

    /**
     * Get String contents of NbtString or SNBT representation of NBT.
     * A null element will return an empty string.
     * 
     * @param inp
     * @return
     */
    public static String nbtToSnbtOrString(Tag inp) {
        if(inp != null && inp.getId()==Tag.TAG_STRING)
            return ((StringTag)inp).asString().get();
        return nbtToSnbt(inp);
    }

    /**
     * Get SNBT representation of NBT.
     * A null element will return an empty string.
     * 
     * @param inp
     * @return
     */
    public static String nbtToSnbt(Tag inp) {
        if(inp == null)
            return "";
        else if(inp.getId()==Tag.TAG_STRING) {
            CompoundTag temp = new CompoundTag();
            temp.put("temp",inp);
            String parsed = temp.toString();
            if(parsed.startsWith("{temp:") && parsed.endsWith("}")) {
                parsed = parsed.substring(6,parsed.length()-1);
                if(BlackMagick.nbtFromString(parsed)!=null && BlackMagick.nbtFromString(parsed).getId()==Tag.TAG_STRING
                && (BlackMagick.nbtFromString(parsed)).toString().equals(inp.toString())) {
                    return parsed;
                }
            }
            FortytwoEdit.logError("Failed to stringify NbtString: "+inp.toString());
            return inp.toString();
        }
        else
            return inp.toString();
    }

    /**
     * 
     * @param inp
     * @return true if inp is any of the 4 integer types or any of the 2 decimal types
     */
    public static boolean nbtIsNumber(Tag inp) {
        if(inp == null)
            return false;
        return nbtTypeIsNumber(inp.getId());
    }

    public static boolean nbtTypeIsNumber(byte inp) {
        switch(inp) {
            case Tag.TAG_BYTE:
            case Tag.TAG_SHORT:
            case Tag.TAG_INT:
            case Tag.TAG_LONG:
            case Tag.TAG_FLOAT:
            case Tag.TAG_DOUBLE:
                return true;
            default: return false;
        }
    }

    /**
     * Get SNBT representation of NBT.
     * A null element will return an empty string.
     * 
     * @param inp
     * @return
     */
    public static Component nbtToColorfulText(Tag inp) {
        if(inp == null)
            return Component.empty().append("null").withStyle(ChatFormatting.ITALIC);
        return NbtUtils.toPrettyComponent(inp);
    }

    /**
     * Validates element to be a non-null compound, otherwise returns default compound
     * 
     * @param el any element or null
     * @return non-null compound
     */
    public static CompoundTag validCompound(Tag el) {
        if(el == null || el.getId() != Tag.TAG_COMPOUND)
            return new CompoundTag();
        return (CompoundTag)el;
    }

    /**
     * Converts json string to Text object. Valid forms include {text:""} [{text:""}] ""
     * 
     * @param inp raw json string
     * @return parsed Text or error message
     */
    public static ParsedText textComponentFromString(String inp) {
        Tag textComponent = BlackMagick.nbtFromString(inp);
        if(textComponent != null) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("id","stone");
            CompoundTag components = new CompoundTag();
            components.put("minecraft:custom_name",textComponent);
            nbt.put("components",components);
            ItemStack stack = BlackMagick.itemFromNbt(nbt);
            if(!stack.isEmpty() && BlackMagick.getNbtPath(BlackMagick.itemToNbtStorage(stack),"components.minecraft:custom_name") != null)
                return new ParsedText(true,stack.getHoverName().copy());
        }
        return new ParsedText(false,Component.nullToEmpty("Invalid Text Component").copy().withStyle(ChatFormatting.RED));
    }

    /**
     * @param inp item compound with id/count/components
     * @return stack from nbt without world registries (or empty stack if invalid)
     */
    public static ItemStack itemFromNbtStatic(CompoundTag inp) {
        if(inp != null) {
            try {
                return ItemStack.parse(RegistryAccess.EMPTY, inp).orElse(ItemStack.EMPTY);
            } catch(Exception ex) {}
        }
        return ItemStack.EMPTY;
    }

    /**
     * @param inp item compound with id/count/components
     * @return stack from nbt (or empty stack if invalid)
     */
    public static ItemStack itemFromNbt(CompoundTag inp) {
        final Minecraft client = Minecraft.getInstance();
        if(client.level != null && inp != null) {
            try {
                Optional<ItemStack> optionalStack = ItemStack.parse(client.level.registryAccess(),inp);
                if(optionalStack.isPresent())
                    return optionalStack.get();
            } catch(Exception ex) {}
        }
        return itemFromNbtStatic(inp);
    }

    /**
     * @param inp stringified item compound with id/count/components
     * @return stack from nbt (or empty stack if invalid)
     */
    public static ItemStack itemFromString(String inp) {
        return itemFromNbt(validCompoundFromString(inp));
    }

    /**
     * Get compound representation of an item, or an empty compound if invalid.
     * Compound appears exactly as it would with /data get
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static CompoundTag itemToNbtStorage(ItemStack item) {
        CompoundTag nbt = new CompoundTag();
        final Minecraft client = Minecraft.getInstance();
        try {
            if(client.level != null && item != null && !item.isEmpty()) {
                nbt = (CompoundTag)item.save(client.level.registryAccess());
            }
        } catch(Exception ex) {}
        return nbt;
    }

    /**
     * Get compound representation of an item, or an empty compound if invalid.
     * All components are kept, even if they are the default values.
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static CompoundTag itemToNbt(ItemStack item) {
        CompoundTag nbt = new CompoundTag();
        if(item != null && !item.isEmpty()) {
            CompoundTag comps = new CompoundTag();
            String compsString = componentsAsString(item.getComponents());
            if(compsString != null && compsString.length()>0)
                comps = validCompoundFromString("{"+compsString+"}");

            CompoundTag itemComps = BlackMagick.validCompound(BlackMagick.getNbtPath(BlackMagick.itemToNbtStorage(item),"components"));
            for(String k : itemComps.keySet()) {
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
     * Get compound representation of an item, or an empty compound if invalid.
     * All components are kept, even if they are the default values.
     * All components that are not on the item are added as negations.
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static CompoundTag itemToNbtExclusive(ItemStack item) {
        CompoundTag nbt = new CompoundTag();
        if(item != null && !item.isEmpty()) {
            CompoundTag comps = new CompoundTag();
            String compsString = componentsAsString(item.getComponents());
            if(compsString != null && compsString.length()>0)
                comps = validCompoundFromString("{"+compsString+"}");

            Set<String> unusedComps = Sets.newHashSet();
            for(String comp : SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList())
                unusedComps.add(comp);
            for(String comp : comps.keySet()) {
                unusedComps.remove(comp);
            }
            for(String comp : unusedComps)
                comps.put("!"+comp,new CompoundTag());

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
    public static String itemToGive(CompoundTag item) {
        if(item != null && item.getString("id").isPresent()) {
            String cmd = item.getString("id").get().replace("minecraft:","");
            if(item.getCompound("components").isPresent()) {
                cmd += "[";
                CompoundTag components = item.getCompound("components").get();
                boolean first = true;
                for(String k : components.keySet()) {
                    String key = k.replace("minecraft:","");
                    if(!first)
                        cmd += ",";

                    if(k.startsWith("!"))
                        cmd += key;
                    else
                        cmd += key+"="+BlackMagick.nbtToSnbt(components.get(k));

                    first = false;
                }
                cmd += "]";
            }
            if(item.getInt("count").isPresent() && item.getInt("count").get()>1)
                cmd += " "+item.getInt("count").get();
            return cmd;
        }
        return "";
    }

    /**
     * Modified from {@link net.minecraft.commands.arguments.item.ItemInput#serializeComponents}
     */
    private static String componentsAsString(DataComponentMap comps) {
        final Minecraft client = Minecraft.getInstance();
        if(client.level != null && comps != null) {
            HolderLookup.Provider provider = client.level.registryAccess();

            DynamicOps<Tag> dynamicOps = provider.createSerializationContext(NbtOps.INSTANCE);
            return comps.stream().flatMap(component -> {
                DataComponentType<?> dataComponentType = component.type();
                ResourceLocation resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(dataComponentType);
                Optional<Tag> optional = component.encodeValue(dynamicOps).result();
                if (resourceLocation == null || !optional.isPresent())
                    return Stream.empty();
                return Stream.of("\"" + resourceLocation.toString() + "\":" + BlackMagick.nbtToSnbt(optional.get()));
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
            return BuiltInRegistries.ITEM.getKey(item).toString();
        return BuiltInRegistries.ITEM.getKey(item).getPath();
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
        if(item==null || item.isEmpty())
            return invalidMsg;
        try {
            String giveMsg = "bundle[bundle_contents=["+item+"]]";
            ItemArgument.item(BlackMagick.getCommandRegistries()).parse(new StringReader(giveMsg));
        }
        catch(Exception ex) {
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
    public static Tag getNbtPath(CompoundTag inp, String path) {
        if(inp == null || inp.getId() != Tag.TAG_COMPOUND)
            return null;

        try {
            NbtPath p = NbtPath.of(path);
            List<Tag> list = p.get(inp);
            if(list.size() == 1) {
                Tag el = list.get(0);
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
    public static Tag getNbtPath(CompoundTag inp, String path, byte type) {
        Tag el = getNbtPath(inp, path);
        if(el != null && el.getId() == type)
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
    public static CompoundTag setNbtPath(CompoundTag base, String path, Tag el) {
        CompoundTag nbt;
        if(base != null)
            nbt = base.copy();
        else
            nbt = new CompoundTag();

        try {
            NbtPath p = NbtPath.of(path);
            if(el == null)
                p.remove(nbt);
            else {
                p.set(nbt,el.copy());
                if(!path.contains("!"))
                    nbt = removeComponentLocks(nbt,path);
            }
        } catch(Exception ex) {}

        return nbt;
    }

    private static CompoundTag removeComponentLocks(CompoundTag base, String path) {
        if(base == null)
            return null;
        CompoundTag nbt = base.copy();

        if(path.startsWith("components.")) {
            String component = path.substring(11);
            if(component.contains("."))
                component = component.substring(0,component.indexOf("."));
            if(component.contains("["))
                component = component.substring(0,component.indexOf("["));

            if(nbtToSnbt(getNbtPath(nbt,"components.!"+component)).equals("{}"))
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
    public static CompoundTag moveListElement(CompoundTag base, String path, int index, boolean up) {
        if(base == null)
            return null;
        if(path == null || path.isEmpty() || index<0)
            return base.copy();

        CompoundTag nbt = base.copy();

        if(getNbtPath(nbt,path,Tag.TAG_LIST) != null) {
            ListTag list = (ListTag)getNbtPath(nbt,path);
            if(list.size()>index && index>=0 && !((index==0 && up) || (index==list.size()-1 && !up))) {
                Tag el = list.remove(index);
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
    public static CompoundTag cloneListElement(CompoundTag base, String path, int index) {
        if(base == null)
            return null;
        if(path == null || path.isEmpty() || index<0)
            return base.copy();

        CompoundTag nbt = base.copy();

        if(getNbtPath(nbt,path,Tag.TAG_LIST) != null) {
            ListTag list = (ListTag)getNbtPath(nbt,path);
            if(list.size()>index && index>=0) {
                Tag el = list.get(index).copy();
                list.add(index,el);
                nbt = setNbtPath(nbt, path, list);
            }
        }

        return nbt;
    }

    /**
     * Returns a copy of a compound in which a list element at the path has a new element appended
     * 
     * @param base compound to edit
     * @param path path like "foo" "foo.bar" or "foo.bar[1]" ending in an NbtList
     * @param element alement to append to end of list
     * @return copy with changes made
     */
    public static CompoundTag appendListElement(CompoundTag base, String path, Tag element) {
        if(base == null)
            return null;
        if(path == null || path.isEmpty() || element == null)
            return base.copy();

        CompoundTag nbt = base.copy();

        if(getNbtPath(nbt,path,Tag.TAG_LIST) != null) {
            ListTag list = (ListTag)getNbtPath(nbt,path);
            list.add(element);
            nbt = setNbtPath(nbt, path, list);
        }

        return nbt;
    }

    /**
     * 
     * @param left
     * @param right
     * @return true if left and right elements are identical
     */
    public static boolean elementsEqual(Tag left, Tag right) {
        if(left == null && right == null)
            return true;
        else if(left == null || right == null)
            return false;

        return nbtToSnbt(left).equals(nbtToSnbt(right));
    }

    public static String validCompoundKey(String key) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(key,0);
        String nbtString = BlackMagick.nbtToSnbt(nbt);
        if(nbtString.startsWith("{") && nbtString.endsWith(":0}")) {
            return nbtString.substring(1,nbtString.length()-3);
        }
        FortytwoEdit.logError("Failed to convert key to valid SNBT key: "+key);
        return nbtToSnbt(StringTag.valueOf(key));
    }

    public static String validPathKey(String key) {
        if(key.matches("^[a-zA-Z0-9:!/_-]*$"))
            return key;
        String newKey = validCompoundKey(key);
        if(key.contains(".") && !newKey.startsWith("\"") && !newKey.startsWith("'"))
            return nbtToSnbt(StringTag.valueOf(key));
        return newKey;
    }

    public static ResourceLocation identifierOrNull(String id) {
        try {
            return ResourceLocation.parse(id);
        }
        catch(Exception ex) {}
        return null;
    }

    /**
     * Get the Nbt representation of an item for pre-made banner designs for various characters
     * 
     * @param character
     * @param baseColor the banner background color
     * @param charColor the color of the character
     * @return compound representation of an itemstack, or null
     */
    public static CompoundTag createBanner(char character, String baseColor, String charColor) {
        switch(character) {
            case 'A': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'B': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+baseColor+",pattern:\"minecraft:circle\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}");
            case 'C': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'D': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:half_vertical\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}");
            case 'E': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'F': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'G': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+baseColor+",pattern:\"minecraft:half_vertical\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}");
            case 'H': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'I': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'J': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'K': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'L': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:half_vertical\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'M': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:triangle_top\"},{color:"+baseColor+",pattern:\"minecraft:triangles_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'N': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'O': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'P': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'Q': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:square_bottom_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+charColor+"_banner\"}");
            case 'R': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'S': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'T': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'U': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'V': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:diagonal_up_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'W': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:triangle_bottom\"},{color:"+baseColor+",pattern:\"minecraft:triangles_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'X': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'Y': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case 'Z': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '0': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '1': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:square_top_left\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_center\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '2': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '3': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:curly_border\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '4': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '5': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '6': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '7': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '8': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_bottom\"},{color:"+baseColor+",pattern:\"minecraft:rhombus\"},{color:"+charColor+",pattern:\"minecraft:stripe_downright\"},{color:"+charColor+",pattern:\"minecraft:stripe_downleft\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            case '9': return validCompoundFromString("{components:{\"minecraft:banner_patterns\":[{color:"+charColor+",pattern:\"minecraft:stripe_left\"},{color:"+baseColor+",pattern:\"minecraft:half_horizontal_bottom\"},{color:"+charColor+",pattern:\"minecraft:stripe_top\"},{color:"+charColor+",pattern:\"minecraft:stripe_right\"},{color:"+charColor+",pattern:\"minecraft:stripe_middle\"},{color:"+baseColor+",pattern:\"minecraft:border\"}]},id:\"minecraft:"+baseColor+"_banner\"}");
            default: return null;
        }
    }


    /**
     * stores parsed text component or an error message if isValid is false
     */
    public record ParsedText(boolean isValid, Component text) {}

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
     * Returns a list of ints in ascending order.
     * 
     * @param set
     * @return
     */
    public static List<Integer> sortIntSet(Set<Integer> set) {
        List<Integer> list = Lists.newArrayList();

        for(Integer i : set)
            list.add(i);

        Collections.sort(list);
        return list;
    }

    /**
     * Returns a list of strings sorted alphabetically.
     * Treats uppercase and lowercase the same.
     * 
     * @param set
     * @return
     */
    public static List<String> sortSet(Set<String> set) {
        List<String> list = Lists.newArrayList();

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
        List<String> list = Lists.newArrayList();

        for(String s : array)
            list.add(s);

        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /**
     * Returns a list of strings sorted alphabetically.
     * Treats uppercase and lowercase the same.
     * 
     * @param list
     * @return
     */
    public static List<String> sortList(List<String> list) {
        List<String> newList = Lists.newArrayList();

        newList.addAll(list);

        Collections.sort(newList, String.CASE_INSENSITIVE_ORDER);
        return newList;
    }

    /**
     * Format suggs so that they represent an NbtString (without modifying original list).
     * Required when string suggs are used in a txt not setup for NbtStrings.
     * 
     * @param suggs
     * @return
     */
    public static List<String> formatStringSuggs(List<String> suggs) {
        List<String> list = Lists.newArrayList();

        for(String s : suggs)
            list.add(nbtToSnbt(StringTag.valueOf(s)));

        return list;
    }

    /**
     * Format suggs with a prefix and suffix (without modifying original list)
     * 
     * @param suggs
     * @return
     */
    public static List<String> formatSuggs(List<String> suggs, String prefix, String suffix) {
        List<String> list = Lists.newArrayList();

        for(String s : suggs)
            list.add(prefix+s+suffix);

        return list;
    }

    public static List<String> joinCommandSuggs(List<String> suggsList, List<String> startVals) {
        Set<String> set = Sets.newHashSet();
        List<String> list = Lists.newArrayList();

        if(suggsList != null)
            for(String s : suggsList)
                if(!s.isEmpty())
                    set.add(s);

        if(startVals != null)
            set.removeAll(startVals);

        list.addAll(set);
        Collections.sort(list, new SnbtSortComparator());

        if(startVals != null)
            for(int i=0; i<startVals.size(); i++) {
                String current = startVals.get(startVals.size()-1-i);
                if(!current.isEmpty())
                    list.add(0,current);
            }

        return list;
    }

    private static class SnbtSortComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {

            Tag el1 = BlackMagick.nbtFromString(s1);
            if(el1 == null)
                el1 = StringTag.valueOf(s1);

            Tag el2 = BlackMagick.nbtFromString(s2);
            if(el2 == null)
                el2 = StringTag.valueOf(s2);

            if(el1.getId() != el2.getId()) {
                if(nbtTypeInt(el1.getId()) < nbtTypeInt(el2.getId()))
                    return -1;
                return 1;
            }

            switch(el1.getId()) {
                case Tag.TAG_BYTE: {
                    if(((ByteTag)el1).byteValue() < ((ByteTag)el2).byteValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_SHORT: {
                    if(((ShortTag)el1).shortValue() < ((ShortTag)el2).shortValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_INT: {
                    if(((IntTag)el1).intValue() < ((IntTag)el2).intValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_LONG: {
                    if(((LongTag)el1).longValue() < ((LongTag)el2).longValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_FLOAT: {
                    if(((FloatTag)el1).floatValue() < ((FloatTag)el2).floatValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_DOUBLE: {
                    if(((DoubleTag)el1).doubleValue() < ((DoubleTag)el2).doubleValue())
                        return -1;
                    return 1;
                }
                case Tag.TAG_STRING: {
                    boolean removed1 = BlackMagick.nbtToSnbtOrString(el1).startsWith("!");
                    boolean removed2 = BlackMagick.nbtToSnbtOrString(el2).startsWith("!");
                    if(!removed1 && removed2)
                        return -1;
                    if(removed1 && !removed2)
                        return 1;
                }
                default: break;
            }

            return s1.compareToIgnoreCase(s2);

        }

        private int nbtTypeInt(byte type) {
            switch(type) {
                case Tag.TAG_COMPOUND: return 1;
                case Tag.TAG_LIST: return 2;

                case Tag.TAG_BYTE_ARRAY: return 3;
                case Tag.TAG_INT_ARRAY: return 4;
                case Tag.TAG_LONG_ARRAY: return 5;
                
                case Tag.TAG_BYTE: return 6;
                case Tag.TAG_SHORT: return 7;
                case Tag.TAG_INT: return 8;
                case Tag.TAG_LONG: return 9;
                case Tag.TAG_FLOAT: return 10;
                case Tag.TAG_DOUBLE: return 11;

                case Tag.TAG_STRING: return 12;

                default: return 0;
            }
        }
        
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
     * @param dec int like 4327014
     * @return hex String like 0x420666
     */
    public static String hexFromInt(int dec) {
        return "0x"+Integer.toHexString(dec);
    }

    /**
     * 
     * @return vanilla command registries with all features enabled
     */
    public static CommandBuildContext getCommandRegistries() {
        final Minecraft client = Minecraft.getInstance();
        if(client.level != null)
            return CommandBuildContext.simple(client.level.registryAccess(), FortytwoEdit.FEATURES);
        return CommandBuildContext.simple(RegistryAccess.EMPTY, FortytwoEdit.FEATURES);
    }

    public static Component getElementDifferencesOrColorfulText(Tag left, Tag right) {
        if(left!=null && right!=null && elementsEqual(left,right))
            return nbtToColorfulText(left);
        return getElementDifferences(left, right);
    }

    /**
     * Display element differences with colored text.
     * 
     * @param left
     * @param right
     * @return formatted Text listing element differences and full content
     */
    public static Component getElementDifferences(Tag left, Tag right) {
        if(left==null && right==null)
            return Component.empty().append("null").withStyle(ChatFormatting.ITALIC);
        if(left==null)
            return Component.nullToEmpty(BlackMagick.nbtToSnbt(right)).copy().withStyle(ChatFormatting.GREEN);
        if(right==null)
            return Component.nullToEmpty(BlackMagick.nbtToSnbt(left)).copy().withStyle(ChatFormatting.RED);

        if(left.getId() == right.getId()) {
            if(BlackMagick.nbtToSnbt(left).equals(BlackMagick.nbtToSnbt(right)))
                return Component.nullToEmpty(BlackMagick.nbtToSnbt(left));
            if(left.getId() == Tag.TAG_COMPOUND) {
                CompoundTag leftCmp = (CompoundTag)left;
                CompoundTag rightCmp = (CompoundTag)right;
                MutableComponent output = Component.empty().append(Component.nullToEmpty("{"));

                Set<String> allKeys = Sets.newHashSet();
                for(String k : ((CompoundTag)left).keySet())
                    allKeys.add(k);
                for(String k : ((CompoundTag)right).keySet())
                    allKeys.add(k);

                boolean first = true;
                for(String k : allKeys) {
                    String k2 = validCompoundKey(k);

                    if(!first)
                        output.append(Component.nullToEmpty(","));
                    else
                        first = false;

                    if(leftCmp.contains(k) && rightCmp.contains(k)) {
                        output.append(Component.nullToEmpty(k2+":"));
                        output.append(getElementDifferences(leftCmp.get(k),rightCmp.get(k)));
                    }
                    else if(leftCmp.contains(k)) {
                        output.append((Component.nullToEmpty(k2+":").copy().append(Component.nullToEmpty(BlackMagick.nbtToSnbt(leftCmp.get(k))))).withStyle(ChatFormatting.RED));
                    }
                    else {
                        output.append((Component.nullToEmpty(k2+":").copy().append(Component.nullToEmpty(BlackMagick.nbtToSnbt(rightCmp.get(k))))).withStyle(ChatFormatting.GREEN));
                    }
                }

                output.append(Component.nullToEmpty("}"));
                return output;
            }
            if(left.getId() == Tag.TAG_LIST) {
                ListTag leftList = (ListTag)left;
                ListTag rightList = (ListTag)right;
                MutableComponent output = Component.empty().append(Component.nullToEmpty("["));

                int maxSize = Math.max(((ListTag)left).size(),((ListTag)right).size());

                boolean first = true;
                for(int i=0; i<maxSize; i++) {
                    if(!first)
                        output.append(Component.nullToEmpty(","));
                    else
                        first = false;

                    if(leftList.size()>i && rightList.size()>i) {
                        output.append(getElementDifferences(leftList.get(i),rightList.get(i)));
                    }
                    else if(leftList.size()>i) {
                        output.append((Component.nullToEmpty(BlackMagick.nbtToSnbt(leftList.get(i))).copy()).withStyle(ChatFormatting.RED));
                    }
                    else {
                        output.append((Component.nullToEmpty(BlackMagick.nbtToSnbt(rightList.get(i))).copy()).withStyle(ChatFormatting.GREEN));
                    }
                }

                output.append(Component.nullToEmpty("]"));
                return output;
            }
        }
        return Component.empty().append(Component.nullToEmpty(BlackMagick.nbtToSnbt(left)).copy().withStyle(ChatFormatting.RED)).append(Component.nullToEmpty(BlackMagick.nbtToSnbt(right)).copy().withStyle(ChatFormatting.GREEN));
    }

    /**
     * Format an nbt element as a tree
     * 
     * @param el
     * @param collapseItems if true, compounds that contain the key `id` will be inlined instead of expanded
     * @return
     */
    public static String formatSnbtAsTree(Tag el, boolean collapseItems) {
        return formatSnbtAsTree(el, collapseItems, 0);
    }

    /**
     * Inner logic for method above
     */
    private static String formatSnbtAsTree(Tag el, boolean collapseItems, int indents) {
        if(el == null)
            return "null";

        StringBuilder current = new StringBuilder(128);
        StringBuilder indentBuilder = new StringBuilder();
        for(int i=0; i<indents; i++)
            indentBuilder.append("\t");
        String indent = indentBuilder.toString();

        switch(el.getId()) {
            case Tag.TAG_COMPOUND: {
                CompoundTag nbt = (CompoundTag)el;
                if(nbt.isEmpty())
                    current.append("{}");
                else if(collapseItems && nbt.contains("id"))
                    current.append(BlackMagick.nbtToSnbt(nbt));
                else {
                    current.append("{\n");

                    boolean firstKey = true;
                    for(String k : sortSet(nbt.keySet())) {
                        if(firstKey)
                            firstKey = false;
                        else
                            current.append(",\n");

                        String keyString = validCompoundKey(k);
                        current.append(indent + "\t" + keyString + ": " + formatSnbtAsTree(nbt.get(k), collapseItems, indents+1));
                    }

                    current.append("\n" + indent + "}");
                }
                break;
            }
            case Tag.TAG_LIST: {
                ListTag nbt = (ListTag)el;
                if(nbt.isEmpty())
                    current.append("[]");
                else {
                    current.append("[\n");

                    for(int i=0; i<nbt.size(); i++) {
                        if(i>0)
                            current.append(",\n");

                        current.append(indent + "\t" + formatSnbtAsTree(nbt.get(i), collapseItems, indents+1));
                    }

                    current.append("\n" + indent + "]");
                }
                break;
            }
            default: {
                current.append(BlackMagick.nbtToSnbt(el));
            }
        }
        return current.toString();
    }


}