package baphomethlabs.fortytwoedit;

import org.jetbrains.annotations.Nullable;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.SignItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

/**
 * <p> Class containing static methods related to item components </p>
 * <p> These often change every update and must be kept up to date manually (search for HARDCODED) </p>
 * <p> Check the following for help: </p>
 * <ul>
 *  <li> net.minecraft.DataComponentTypes </li>
 *  <li> https://minecraft.wiki/w/Item_format </li>
 *  <li> https://minecraft.wiki/w/Entity_format </li>
 * </ul>
 */
public class ComponentHelper {
    
    /**
     * vanilla dyes as they appear in banner color IDs
     */
    public static final String[] DYES = {"black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"};

    /**
     * 
     * @param comps from ItemStack.getComponents() or Item.getComponents()
     * @param component component id without minecraft namespace
     * @return true if comps has the specified component
     */
    public static boolean hasComponent(ComponentMap comps, String component) {
        for(Component<?> c : comps) {
            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(c.type());
            if(id != null && id.getPath().equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to determine if a component is used by the game for an Item type (HARDCODED).
     * Also returns true if the component is used because of other components on the stack.
     * If the component shows up in the item tooltip but otherwise has no use, returns false.
     * The default return is true, unless the method was told to return false based on the item and comps.
     * 
     * @param stack
     * @param component component id without minecraft namespace
     * @return false if the component probably isn't used in a meaningful way on the item
     */
    public static boolean componentRead(ItemStack stack, String component) {
        if(stack==null || component == null)
            return false;
        Item item = stack.getItem();
        String itemId = item.toString();
        if(itemId.equals("ender_chest") && (component.equals("container") || component.equals("container_loot")))
            return false;
        if(hasComponent(item.getComponents(),component))
            return true;
        switch(component) {
            case "banner_patterns":
            case "bees":
            case "bundle_contents":
            case "charged_projectiles":
            case "container":
            case "debug_stick_state":
            case "fireworks":
            case "map_color":
            case "map_decorations":
            case "map_id":
            case "max_damage":
            case "pot_decorations":
            case "potion_contents":
            case "recipes":
            case "stored_enchantments":
            case "suspicious_stew_effects":
            case "writable_book_content":
                return hasComponent(item.getComponents(),component);
            case "base_color": return itemId.equals("shield");
            case "block_entity_data": return (BlackMagick.stringEquals(itemId,"spawner","trial_spawner","command_block","chain_command_block","repeating_command_block","jukebox","lectern")
                || item instanceof SignItem || item instanceof HangingSignItem);
            case "block_state": return !BlackMagick.getBlockStates(stack.getItem()).isEmpty();
            case "bucket_entity_data": return item instanceof EntityBucketItem;
            case "dyed_color": return (new ItemStack(item)).isIn(ItemTags.DYEABLE);
            case "entity_data": return (item instanceof DecorationItem || itemId.equals("armor_stand") || item instanceof BoatItem || 
                item instanceof MinecartItem || itemId.contains("spawn_egg"));
            case "container_loot": return BlackMagick.stringContains(itemId,"chest","barrel","dispenser","dropper","hopper","crafter","shulker_box");
            case "damage": return componentRead(stack,"max_damage");
            case "firework_explosion": return itemId.equals("firework_star");
            case "instrument": return itemId.equals("goat_horn");
            case "intangible_projectile": return itemId.contains("arrow");
            case "lock": return (itemId.equals("beacon") || (componentRead(stack,"container") && !itemId.contains("campfire") && !itemId.equals("chiseled_bookshelf")));
            case "lodestone_tracker": return itemId.equals("compass");
            case "note_block_sound": return itemId.equals("player_head");
            case "profile": return itemId.equals("player_head");
            case "trim": return item instanceof ArmorItem;
            case "unbreakable": return componentRead(stack,"max_damage");
            case "written_book_content": return itemId.equals("written_book");
            default: return true;
        }
    }

    /**
     * Get nickname for component that fits in a small-medium button (HARDCODED).
     * If nickname is set, it will be at least two words to show that it is a nickname.
     * 
     * @param path path from base item tag with id/count/components
     * @return nickname or null
     */
    public static String getCompNickname(String path) {
        switch(path) {
            case "components.enchantment_glint_override" : return "glint override";
            case "components.suspicious_stew_effects" : return "stew effects";
            case "components.writable_book_content" : return "book text";
            case "components.written_book_content" : return "book content";
            default : return null;
        }
    }

    /**
     * Used to pick which ItemBuilder.RowWidget should be used for a given path (HARDCODED)
     * 
     * @param path path from base item tag with id/count/components
     * @return enum CompType
     */
    public static CompType getCompType(String path) {
        switch(path) {
            case "components.enchantment_glint_override" :
                return CompType.TRINARY;

            case "components.unbreakable" :
                return CompType.TOOLTIP_UNIT;

            case "components.hide_additional_tooltip" :
            case "components.hide_tooltip" :
            case "components.intangible_projectile" :
            case "components.fire_resistant" :
                return CompType.UNIT;

            case "components.attribute_modifiers" :
            case "components.banner_patterns" :
            case "components.bees" :
            case "components.block_entity_data" :
            case "components.block_state" :
            case "components.bucket_entity_data" :
            case "components.bundle_contents" :
            case "components.can_break" :
            case "components.can_place_on" :
            case "components.charged_projectiles" :
            case "components.container" :
            case "components.container_loot" :
            case "components.custom_data" :
            case "components.custom_model_data" :
            case "components.custom_name" :
            case "components.debug_stick_state" :
            case "components.dyed_color" :
            case "components.enchantments" :
            case "components.entity_data" :
            case "components.firework_explosion" :
            case "components.fireworks" :
            case "components.food" :
            case "components.lodestone_tracker" :
            case "components.lore" :
            case "components.map_decorations" :
            case "components.pot_decorations" :
            case "components.potion_contents" :
            case "components.profile" :
            case "components.recipes" :
            case "components.stored_enchantments" :
            case "components.suspicious_stew_effects" :
            case "components.tool" :
            case "components.trim" :
            case "components.writable_book_content" :
            case "components.written_book_content" :
            case "components.map_color" :
                return CompType.COMPLEX;

            default : return CompType.VALUE;
        }
    }

    /**
     * Get suggestions for nbt at path (HARDCODED)
     * 
     * @param path like components.foo.bar
     * @param stack optional, can help with suggs
     * @return suggestions or null
     */
    public static String[] getPathSuggs(String path, @Nullable ItemStack stack) {
        switch(path) {
            case "id" : return FortytwoEdit.ITEMS ;
            case "count" :
                if(stack != null) {
                    if(stack.getMaxCount()==1)
                        return new String[]{"1"};
                    else
                        return new String[]{"1",""+stack.getMaxCount()};
                }
                else
                    return new String[]{"1","16","64"};

            case "components.base_color" : return DYES;
            case "components.damage" :
                if(stack != null)
                    return new String[]{"0",""+stack.getMaxDamage()};
                else
                    return new String[]{"0"};
            case "components.instrument" : return new String[]{"ponder_goat_horn","sing_goat_horn","seek_goat_horn",
                "feel_goat_horn","admire_goat_horn","call_goat_horn","yearn_goat_horn","dream_goat_horn"};
            case "components.max_damage" : return new String[]{""};
            case "components.max_stack_size" : return new String[]{"1","16","64","99"};
            case "components.note_block_sound" : return FortytwoEdit.SOUNDS;
            case "components.rarity" : return new String[]{"common","uncommon","rare","epic"};
            case "components.repair_cost" : return new String[]{"0",""+Integer.MAX_VALUE};
            default : return null;//TODO get more from subcomponents, entity_data, block_data, etc (test contains entity_tag, block_tag, etc)
        }
    }

    /**
     * used in getCompType()
     * 
     * <p> VALUE - default, component probably refers to a single value or doesn't need a custom screen </p>
     * <p> COMPLEX - component refers to a NbtCompound, List, Text, decimal color, etc </p>
     * <p> UNIT - component is either unset or {} </p>
     * <p> TRINARY - component is unset, false, or true </p>
     * <p> TOOLTIP_UNIT - component is unset, {}, or {show_in_tooltip:0b} </p>
     */
    public enum CompType {
        VALUE,
        COMPLEX,
        UNIT,
        TRINARY,
        TOOLTIP_UNIT
    }
    
    /**
     * Detect if path likely corresponds to a JSON Text element (HARDCODED).
     * 
     * @param path like foo.bar.list[2]
     * @return true if NBT at path should be JSON Text
     */
    public static boolean isText(String path) {
        if(path.endsWith("custom_name") || path.endsWith("CustomName") || path.contains("lore[")
        || (path.contains("pages[") && path.contains("written_book_content")))
            return true;

        return false;
    }
    
    /**
     * Detect if path likely corresponds to a decimal color (HARDCODED).
     * 
     * @param path like foo.bar.list[2]
     * @return true if NBT at path should be a decimal color
     */
    public static boolean isDecimalColor(String path) {
        if(path.endsWith("rgb") || path.endsWith("map_color") || path.contains("custom_color"))
            return true;

        return false;
    }

}
