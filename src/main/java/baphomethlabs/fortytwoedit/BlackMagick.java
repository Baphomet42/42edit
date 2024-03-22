package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
     * @param item
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
     * Get compound representation of an item, or an empty compound if invalid.
     * The minecraft namespace is removed from id and components.
     * All components are kept, even if they are the default values.
     * 
     * @param item
     * @return compound with id/count/components (or empty compound)
     */
    public static NbtCompound itemToNbtAll(ItemStack item) {
        NbtCompound nbt = new NbtCompound();
        if(item != null && !item.isEmpty()) {
            NbtElement comps = new NbtCompound();
            String compsString = componentsAsString(item.getComponents());
            if(compsString != null && compsString.length()>0)
                comps = BlackMagick.nbtFromString("{"+compsString+"}",NbtElement.COMPOUND_TYPE);
            if(comps != null && !((NbtCompound)comps).isEmpty())
                nbt.put("components",comps);
            nbt.putInt("count",item.getCount());
            nbt.putString("id",item.getItem().toString());
        }
        if(nbt.contains("id",NbtElement.STRING_TYPE) && nbt.getString("id").startsWith("minecraft:") && nbt.getString("id").length()>10)
            nbt.putString("id",nbt.getString("id").substring(10));
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

    // modified from net.minecraft.command.argument.ItemStackArgument
    // change = to : in return line and surround identifier in quotes
    private static String componentsAsString(ComponentMap comps) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && comps != null) {
            RegistryWrapper.WrapperLookup registries = client.world.getRegistryManager();
            RegistryOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);
            return comps.stream().flatMap(component -> {
                DataComponentType<?> dataComponentType = component.type();
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
     * Returns the item argument that can be used in the give command to get the item.
     * Count will appear at the end if it's higher than 1.
     * The minecraft namespace is removed from id and components.
     * 
     * @param item
     * @param keepAll whether or not default components should be listed (and count if 1)
     * @return item argument in form: stone[custom_data={foo:bar}] 4
     */
    public static String itemToGive(ItemStack item, boolean keepAll) {
        NbtCompound nbt;
        if(!keepAll)
            nbt = itemToNbt(item);
        else
            nbt = itemToNbtAll(item);
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

        if(nbt.contains("count",NbtElement.INT_TYPE) && (nbt.getInt("count") > 1 || keepAll))
            cmd += " " + nbt.getInt("count");
        return cmd;
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
            NbtPath p = NbtPath.method_58472(path);
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
        NbtCompound nbt = new NbtCompound();
        if(base != null)
            nbt = base.copy();

        try {
            NbtPath p = NbtPath.method_58472(path);
            if(el == null)
                p.remove(nbt);
            else
                p.put(nbt,el.copy());
        } catch(Exception ex) {
            return base.copy();
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
            if(list.size()>index && !((index==0 && up) || (index==list.size()-1 && !up))) {
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
            if(list.size()>index) {
                NbtElement el = list.get(index).copy();
                list.add(index,el);
                nbt = setNbtPath(nbt, path, list);
            }
        }

        return nbt;
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

    public static CommandRegistryAccess getCommandRegistries() {//TODO get from world, javadoc
        final MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null)
            return CommandRegistryAccess.of(client.world.getRegistryManager(), FortytwoEdit.FEATURES);
        return CommandRegistryAccess.of(DynamicRegistryManager.EMPTY, FortytwoEdit.FEATURES);
    }


}