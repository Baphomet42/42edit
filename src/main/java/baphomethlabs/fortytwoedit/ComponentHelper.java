package baphomethlabs.fortytwoedit;

import net.minecraft.client.gui.tooltip.Tooltip;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
            case "ominous_bottle_amplifier":
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
            case "components.ominous_bottle_amplifier" : return "ominous lvl";
            case "components.suspicious_stew_effects" : return "stew effects";
            case "components.writable_book_content" : return "book text";
            case "components.written_book_content" : return "book content";
            default : return null;
        }
    }

    /**
     * Get the type of element at a path and suggs (HARDCODED)
     * 
     * @param path path from base item like: components.foo.bar[]
     * @return PathInfo with PathType and suggs
     */
    public static PathInfo getPathInfo(String path) {

        if(path.endsWith("components.hide_additional_tooltip")
            || path.endsWith("components.hide_tooltip")
            || path.endsWith("components.intangible_projectile")
            || path.endsWith("components.fire_resistant"))
            return new PathInfo(PathType.UNIT,new String[]{"","{}"});

        if(path.endsWith("components.unbreakable"))
            return new PathInfo(PathType.TOOLTIP_UNIT,new String[]{"","{show_in_tooltip:0b}","{}"});

        if(path.endsWith("components.enchantment_glint_override"))
            return new PathInfo(PathType.TRINARY,new String[]{"","0b","1b"});

        if(path.endsWith("components.attribute_modifiers")
            || path.endsWith("components.block_entity_data")
            || path.endsWith("components.block_state")
            || path.endsWith("components.bucket_entity_data")
            || path.endsWith("components.can_break")
            || path.endsWith("components.can_place_on")
            || path.endsWith("components.container_loot")
            || path.endsWith("components.debug_stick_state")
            || path.endsWith("components.dyed_color")
            || path.endsWith("components.enchantments")
            || path.endsWith("components.entity_data")
            || path.endsWith("components.firework_explosion")
            || path.endsWith("components.fireworks")
            || path.endsWith("components.food")
            || path.endsWith("components.lodestone_tracker")
            || path.endsWith("components.map_decorations")
            || path.endsWith("components.potion_contents")
            || path.endsWith("components.profile")
            || path.endsWith("components.stored_enchantments")
            || path.endsWith("components.tool")
            || path.endsWith("components.trim")
            || path.endsWith("components.writable_book_content")
            || path.endsWith("components.written_book_content"))
            return new PathInfo(PathType.COMPOUND);

        if(path.endsWith("components.banner_patterns")
            || path.endsWith("components.bees")
            || path.endsWith("components.bundle_contents")
            || path.endsWith("components.charged_projectiles")
            || path.endsWith("components.container")
            || path.endsWith("components.lore")
            || path.endsWith("components.pot_decorations")
            || path.endsWith("components.recipes")
            || path.endsWith("components.suspicious_stew_effects"))
            return new PathInfo(PathType.LIST);

        if(path.endsWith("components.custom_name")
            || path.endsWith("components.item_name")
            || path.endsWith("CustomName")
            || path.contains("components.lore[")
            || (path.contains("pages[") && path.contains("components.written_book_content")))
            return new PathInfo(PathType.TEXT,null);

        if(path.endsWith("rgb")
            || path.endsWith("map_color")
            || path.endsWith("custom_color"))
            return new PathInfo(PathType.DECIMAL_COLOR,null);

        if(path.endsWith("id"))
            return new PathInfo(PathType.STRING,FortytwoEdit.ITEMS);

        if(path.endsWith("count"))
            return new PathInfo(new String[]{"1","16","64","99"});

        if(path.endsWith("components.base_color"))
            return new PathInfo(PathType.STRING,DYES);

        if(path.endsWith("components.damage"))
            return new PathInfo(new String[]{"0"});

        if(path.endsWith("components.instrument"))
            return new PathInfo(PathType.STRING,new String[]{"ponder_goat_horn","sing_goat_horn","seek_goat_horn",
            "feel_goat_horn","admire_goat_horn","call_goat_horn","yearn_goat_horn","dream_goat_horn"});

        if(path.endsWith("components.max_damage"))
            return new PathInfo(new String[]{""});

        if(path.endsWith("components.max_stack_size"))
            return new PathInfo(new String[]{"1","16","64","99"});

        if(path.endsWith("components.note_block_sound"))
            return new PathInfo(PathType.STRING,FortytwoEdit.SOUNDS);

        if(path.endsWith("components.ominous_bottle_amplifier"))
            return new PathInfo(new String[]{"0","1","2","3","4"});

        if(path.endsWith("components.rarity"))
            return new PathInfo(PathType.STRING,new String[]{"common","uncommon","rare","epic"});

        if(path.endsWith("components.repair_cost"))
            return new PathInfo(new String[]{"0",""+Integer.MAX_VALUE});

        return new PathInfo();
    }

    /**
     * PathType type - to setup widget
     * String[] suggs - for textbox suggestions
     * Tooltip description - displays in tooltip
     */
    public record PathInfo(PathType type, String[] suggs, Tooltip description) {

        public PathInfo() {
            this(PathType.DEFAULT,null,null);
        }

        public PathInfo(PathType type) {
            this(type,null,null);
        }

        public PathInfo(String[] suggs) {
            this(PathType.DEFAULT,suggs,null);
        }

        public PathInfo(PathType type, String[] suggs) {
            this(type,suggs,null);
        }

    }

    /**
     * Describes the expected value of a path using either NbtElement types
     * or some custom types.
     */
    public enum PathType {

        // simple types
        BYTE,
        SHORT,
        INT,
        LONG,
        DOUBLE,
        FLOAT,
        STRING,

        // array types
        BYTE_ARRAY,
        INT_ARRAY,
        LONG_ARRAY,

        // complex types
        COMPOUND,
        LIST,

        // custom types
        DEFAULT,        // can be used for any stringified nbt
        TEXT,           // use for Raw JSON
        DECIMAL_COLOR,  // use for integer color fields
        UNIT,           // represents nbt that is either absent or {}
        TRINARY,        // represents nbt that is either absent, 0b, or 1b
        TOOLTIP_UNIT,   // represents nbt that is either absent, {}, or {show_in_tooltip:0b}

    }

    /**
     * Item nbt that contains most nbt keys (HARDCODED).
     * Keys that only exist depending on item type, entity_data.id, or other factors are not included.
     * The nbt types are correct but values are nonsense.
     * Use for referencing what keys should be in compounds, etc.
     */
    public static final NbtCompound SUPER_ITEM = BlackMagick.validCompound(BlackMagick.nbtFromString("{id:stone,count:1,components:{"
        + "attribute_modifiers:{modifiers:[{type:\"\",slot:\"\",uuid:[I;0,0,0,0],name:\"\",amount:0d,operation:\"\"}],show_in_tooltip:1b}"
        +",banner_patterns:[{color:\"\",pattern:\"\"}]"
        +",base_color:\"\""
        +",bees:[{entity_data:{},min_ticks_in_hive:0,ticks_in_hive:0}]"
        +",block_entity_data:{}"
        +",block_state:{}"
        +",bucket_entity_data:{NoAI:0b,Silent:0b,NoGravity:0b,Glowing:0b,Invulnerable:0b,Health:0f}"
        +",bundle_contents:[{id:\"\",count:0,components:{}}]"
        +",can_break:{predicates:[{blocks:[\"\"],nbt:{},state:{}}],show_in_tooltip:1b}"
        +",can_place_on:{predicates:[{blocks:[\"\"],nbt:{},state:{}}],show_in_tooltip:1b}"
        +",charged_projectiles:[{id:\"\",count:0,components:{}}]"
        +",container:[{id:\"\",count:0,components:{}}]"
        +",container_loot:{loot_table:\"\",seed:0L}"
        +",custom_data:{}"
        +",custom_model_data:0"
        +",custom_name:'{\"text\":\"\"}'"
        +",damage:0"
        +",debug_stick_state:{}"
        +",dyed_color:{rgb:0,show_in_tooltip:1b}"
        +",enchantment_glint_override:0b"
        +",enchantments:{levels:{},show_in_tooltip:1b}"
        +",entity_data:{id:\"\",Air:0d,CustomName:'{\"text\":\"\"}',CustomNameVisible:0b,FallDistance:0f,Fire:0s,Glowing:0b,HasVisualFire:0b,Invulnerable:0b,"
            +"Motion:[0d,0d,0d],NoGravity:0b,Pos:[0d,0d,0d],Rotation:[0f,0f],Silent:0b,Tags:[\"\"],TicksFrozen:0,UUID:[I;0,0,0,0]}"
        +",fire_resistant:{}"
        +",firework_explosion:{shape:\"\",colors:[I;0],fade_colors:[I;0],has_trail:0b,has_twinkle:0b}"
        +",fireworks:{explosions:[{shape:\"\",colors:[I;0],fade_colors:[I;0],has_trail:0b,has_twinkle:0b}],flight_duration:2b}"
        +",food:{nutrition:0,saturation_modifier:0f,is_meat:0b,can_always_eat:0b,eat_seconds:0f,effects:["
            +"{effect:{id:\"\",amplifier:0b,duration:0,ambient:0b,show_particles:0b,show_icon:0b},probability:0f}]}"
        +",hide_additional_tooltip:{}"
        +",hide_tooltip:{}"
        +",instrument:\"\""
        +",intangible_projectile:{}"
        +",item_name:'{\"text\":\"\"}'"
        +",lock:\"\""
        +",lodestone_tracker:{target:{pos:[I;0,0,0],dimension:\"\"},tracked:0b}"
        +",lore:['{\"text\":\"\"}']"
        +",map_color:0"
        +",map_decorations:{}"
        +",map_id:0"
        +",max_damage:0"
        +",max_stack_size:0"
        +",note_block_sound:\"\""
        +",ominous_bottle_amplifier:0"
        +",pot_decorations:[\"\"]"
        +",potion_contents:{potion:\"\",custom_color:0,custom_effects:[{id:\"\",amplifier:0b,duration:0,ambient:0b,show_particles:0b,show_icon:0b}]}"
        +",profile:{name:\"\",id:[I;0,0,0,0],properties:[{name:\"textures\",value:\"\",signature:\"\"}]}"
        +",rarity:\"\""
        +",recipes:[\"\"]"
        +",repair_cost:0"
        +",stored_enchantments:{levels:{},show_in_tooltip:0b}"
        +",suspicious_stew_effects:[{id:\"\",duration:0}]"
        +",tool:{default_mining_speed:0f,damage_per_block:0,rules:[{blocks:[\"\"],speed:0f,correct_for_drops:0b}]}"
        +",trim:{pattern:\"\",material:\"\",show_in_tooltip:0b}"
        +",unbreakable:{show_in_tooltip:0b}"
        +",writable_book_content:{pages:[{raw:\"\",filtered:\"\"}]}"
        +",written_book_content:{pages:[{raw:'{\"text\":\"\"}',filtered:'{\"text\":\"\"}'}],title:{raw:\"\",filtered:\"\"},author:\"\",generation:0,resolved:0b}"
        +"}}",NbtElement.COMPOUND_TYPE));

}
