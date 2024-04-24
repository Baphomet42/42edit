package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.SignItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * <p> Class containing static methods related to item components </p>
 * <p> These often change every update and must be kept up to date manually </p>
 * <p> Check the following for help: </p>
 * <ul>
 *  <li> {@link net.minecraft.component.DataComponentTypes} </li>
 *  <li> https://minecraft.wiki/w/Item_format </li>
 *  <li> https://minecraft.wiki/w/Entity_format </li>
 * </ul>
 */
public class ComponentHelper {
    
    /**
     * Vanilla dyes as they appear in banner color IDs
     */
    public static final String[] DYES = {"black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"};

    /**
     * Vanilla formatting colors as they appear in JSON "color"
     */
    public static final String[] FORMAT_COLORS = {"reset","aqua","black","blue","dark_aqua","dark_blue","dark_gray","dark_green","dark_purple","dark_red","gold","gray","green","light_purple","red","white","yellow"};

    /**
     * Vanilla and experimental banner pattern ids.
     * Update from {@link net.minecraft.block.entity.BannerPatterns}
     */
    public static final String[] BANNER_PATTERNS = BlackMagick.sortArray(new String[]{
        "base","square_bottom_left","square_bottom_right","square_top_left","square_top_right",
        "stripe_bottom","stripe_top","stripe_left","stripe_right","stripe_center",
        "stripe_middle","stripe_downright","stripe_downleft","small_stripes","cross",
        "straight_cross","triangle_bottom","triangle_top","triangles_bottom","triangles_top",
        "diagonal_left","diagonal_up_right","diagonal_up_left","diagonal_right","circle",
        "rhombus","half_vertical","half_horizontal","half_vertical_right","half_horizontal_bottom",
        "border","curly_border","gradient","gradient_up","bricks",
        "globe","creeper","skull","flower","mojang",
        "piglin","flow","guster"
    }).toArray(new String[0]);

    /**
     * 
     * @param comps from ItemStack.getComponents() or Item.getComponents()
     * @param component component id with minecraft namespace
     * @return true if comps has the specified component
     */
    public static boolean hasComponent(ComponentMap comps, String component) {
        for(Component<?> c : comps) {
            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(c.type());
            if(id != null && id.toString().equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to determine if a component is used by the game for an Item type.
     * Also returns true if the component is used because of other components on the stack.
     * If the component shows up in the item tooltip but otherwise has no use, returns false.
     * The default return is true, unless the method was told to return false based on the item and comps.
     * 
     * @param stack
     * @param component component id with minecraft namespace
     * @return false if the component probably isn't used in a meaningful way on the item
     */
    public static boolean componentRead(ItemStack stack, String component) {
        return componentReadRecursiveLogic(stack,component,false);
    }

    /**
     * Only used for componentRead logic. Never call this method.
     */
    private static boolean componentReadRecursiveLogic(ItemStack stack, String component, boolean recursive) {
        if(stack==null || component == null)
            return false;
        Item item = stack.getItem();
        String itemId = item.toString();
        if(itemId.equals("ender_chest") && (component.equals("container") || component.equals("container_loot")))
            return false;
        if(hasComponent(item.getComponents(),component) || (recursive && hasComponent(stack.getComponents(),component)))
            return true;
        switch(component) {
            case "minecraft:banner_patterns":
            case "minecraft:bees":
            case "minecraft:bundle_contents":
            case "minecraft:charged_projectiles":
            case "minecraft:container":
            case "minecraft:debug_stick_state":
            case "minecraft:fireworks":
            case "minecraft:map_color":
            case "minecraft:map_decorations":
            case "minecraft:map_id":
            case "minecraft:max_damage":
            case "minecraft:ominous_bottle_amplifier":
            case "minecraft:pot_decorations":
            case "minecraft:potion_contents":
            case "minecraft:recipes":
            case "minecraft:stored_enchantments":
            case "minecraft:suspicious_stew_effects":
            case "minecraft:writable_book_content":
                return hasComponent(item.getComponents(),component) || (recursive && hasComponent(stack.getComponents(),component));
            case "minecraft:base_color": return itemId.equals("shield");
            case "minecraft:block_entity_data": return (BlackMagick.stringEquals(itemId,"beacon","beehive","bee_nest","blast_furnace","brewing_stand","campfire","chiseled_bookshelf","command_block","chain_command_block","repeating_command_block","crafter","furnace","smoker","soul_campfire","spawner","trial_spawner","jukebox","lectern")
                || item instanceof SignItem || item instanceof HangingSignItem); // see https://minecraft.wiki/w/Chunk_format
            case "minecraft:block_state": return !BlackMagick.getBlockStates(stack.getItem()).isEmpty();
            case "minecraft:bucket_entity_data": return item instanceof EntityBucketItem;
            case "minecraft:dyed_color": return (new ItemStack(item)).isIn(ItemTags.DYEABLE);
            case "minecraft:entity_data": return (item instanceof DecorationItem || itemId.equals("armor_stand") || item instanceof BoatItem || 
                item instanceof MinecartItem || itemId.contains("spawn_egg"));
            case "minecraft:container_loot": return BlackMagick.stringContains(itemId,"chest","barrel","dispenser","dropper","hopper","crafter","shulker_box");
            case "minecraft:damage": return componentReadRecursiveLogic(stack,"minecraft:max_damage",true);
            case "minecraft:firework_explosion": return itemId.equals("firework_star");
            case "minecraft:instrument": return itemId.equals("goat_horn");
            case "minecraft:intangible_projectile": return itemId.contains("arrow");
            case "minecraft:lock": return (itemId.equals("beacon") || (componentReadRecursiveLogic(stack,"minecraft:container",true) && !itemId.contains("campfire") && !itemId.equals("chiseled_bookshelf")));
            case "minecraft:lodestone_tracker": return itemId.equals("compass");
            case "minecraft:note_block_sound": return itemId.equals("player_head");
            case "minecraft:profile": return itemId.equals("player_head");
            case "minecraft:trim": return item instanceof ArmorItem;
            case "minecraft:unbreakable": return componentReadRecursiveLogic(stack,"minecraft:max_damage",true);
            case "minecraft:written_book_content": return itemId.equals("written_book");
            default: return true;
        }
    }

    private static final Map<String,PathInfo> cacheInfo = Maps.newHashMap();

    public static void clearCacheInfo() {
        cacheInfo.clear();
    }

    /**
     * <p> Get the type of element at a path and suggs. </p>
     * <p> Based on: </p>
     * <ul>
     *  <li> https://minecraft.wiki/w/Item_format </li>
     *  <li> https://minecraft.wiki/w/Entity_format </li>
     *  <li> https://minecraft.wiki/w/Chunk_format </li>
     * </ul>
     * 
     * @param path path from base item like: components.foo.bar[]
     * @return PathInfo with PathType and suggs
     */
    public static PathInfo getPathInfo(String path) {
        {
            String superPath = "";
            while(path.contains("[") && path.contains("]") && (path.indexOf("[")<path.indexOf("]")-1)) {
                superPath += path.substring(0,path.indexOf("["));
                superPath += "[0]";
                path = path.substring(path.indexOf("]"));
                if(path.length()>1)
                    path = path.substring(1);
                else
                    path = "";
            }
            if(path.length()>0)
                superPath += path;

            path = superPath.replace("minecraft:","");
        }

        if(cacheInfo.containsKey(path))
            return cacheInfo.get(path);
        PathInfo pi = getNewPathInfo(path);
        if(!pi.dynamic())
            cacheInfo.put(path,pi);
        return pi;
    }

    private static PathInfo getNewPathInfo(String path) {

        if(path.contains("components.attribute_modifiers")) {
            if(path.endsWith("components.attribute_modifiers"))
                return (new PathInfo(List.of("modifiers","show_in_tooltip")));
            if(path.endsWith("components.attribute_modifiers.modifiers"))
                return PathInfos.LIST_COMPOUND.asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0]"))
                return (new PathInfo(List.of("type","slot","uuid","name","amount","operation")));
            if(path.endsWith("components.attribute_modifiers.modifiers[0].type"))
                return (new PathInfo(PathType.STRING, FortytwoEdit.ATTRIBUTES)).asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].slot"))
                return (new PathInfo(PathType.STRING, new String[]{"any","hand","mainhand","offhand","armor","head","chest","legs","feet"}));
            if(path.endsWith("components.attribute_modifiers.modifiers[0].uuid"))
                return (new PathInfo(PathType.UUID, new String[]{"[I;0,0,0,0]",FortytwoEdit.randomUUID().asString()})).withDesc(Text.of("Keep all attributes different so they update correctly")).asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].name"))
                return PathInfos.STRING.withDesc(Text.of("Name of attribute (has no effect ingame)")).asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].amount"))
                return PathInfos.DOUBLE.asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].operation"))
                return (new PathInfo(PathType.STRING, new String[]{"add_value","add_multiplied_base","add_multiplied_total"})).withDesc(
                Text.of("add_value: base + amount1 + amount2\n\nadd_multiplied_base: base * (1 + amount1 + amount2)\n\n"
                +"add_multiplied_total: base * (1 + amount1) * (1 + amount2)")).asRequired();
            if(path.endsWith("components.attribute_modifiers.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.banner_patterns")) {
            if(path.endsWith("components.banner_patterns"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.banner_patterns[0]"))
                return (new PathInfo(List.of("color","pattern")));
            if(path.endsWith("components.banner_patterns[0].color"))
                return (new PathInfo(PathType.STRING, DYES)).asRequired();
            if(path.endsWith("components.banner_patterns[0].pattern"))
                return (new PathInfo(PathType.STRING,BANNER_PATTERNS)).asRequired();
        }

        if(path.endsWith("components.base_color"))
            return (new PathInfo(PathType.STRING,DYES)).withDesc(Text.of("Used for the banner color of a shield"));

        if(path.contains("components.bees")) {
            if(path.endsWith("components.bees"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.bees[0]"))
                return (new PathInfo(List.of("entity_data","min_ticks_in_hive","ticks_in_hive")));
            // if(path.endsWith("components.bees[0].entity_data"))
            //     see .entity_data
            if(path.endsWith("components.bees[0].min_ticks_in_hive"))
                return PathInfos.INT.asRequired();
            if(path.endsWith("components.bees[0].ticks_in_hive"))
                return PathInfos.INT.asRequired();
        }

        if(path.contains("components.block_entity_data")) {
            if(path.endsWith("components.block_entity_data"))
                return (new PathInfo(List.of("id")));
            if(path.endsWith("components.block_entity_data.id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.ENTITIES)).asRequired();
        }

        if(path.contains("components.block_state")) {
            if(path.endsWith("components.block_state")) {
                if(!path.equals("components.block_state"))
                    return PathInfos.INLINE_COMPOUND;
                if(ItemBuilder.getStatesArr() != null)
                    return (new PathInfo(List.of(ItemBuilder.getStatesArr()))).asDynamic();
                return PathInfos.INLINE_COMPOUND.asDynamic();
            }
            if(path.contains("components.block_state.")) {
                if(ItemBuilder.getStatesArr() != null) {
                    for(String s : ItemBuilder.getStatesArr()) {
                        if(path.endsWith("components.block_state."+s)) {
                            String[] states = ItemBuilder.getStateVals(s);
                            if(states != null)
                                return (new PathInfo(PathType.STRING,states)).asDynamic();
                        }
                    }
                }
                return PathInfos.STRING.asDynamic();
            }
        }

        if(path.contains("components.bucket_entity_data")) {
            if(path.endsWith("components.bucket_entity_data"))
                return (new PathInfo(List.of("NoAI","Silent","NoGravity","Glowing","Invulnerable","Health","Age","Variant","HuntingCooldown","BucketVariantTag")));
            if(path.endsWith("components.bucket_entity_data.NoAI"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.bucket_entity_data.Silent"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.bucket_entity_data.NoGravity"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.bucket_entity_data.Glowing"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.bucket_entity_data.Invulnerable"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.bucket_entity_data.Health"))
                return PathInfos.FLOAT;
            if(path.endsWith("components.bucket_entity_data.Age"))
                return PathInfos.INT.withDesc(Text.of("Age of tadpole or axolotl")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.Variant"))
                return (new PathInfo(PathType.INT,new String[]{"0","1","2","3","4"})).withDesc(Text.of("Axolotl variant id\n0 - lucy (pink)\n1 - wild (brown)\n2 - gold\n3 - cyan\n4 - blue")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.HuntingCooldown"))
                return PathInfos.LONG.withDesc(Text.of("Axolotl hunting cooldown")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.BucketVariantTag"))
                return PathInfos.INT.withDesc(Text.of("Tropical fish variant")).withGroup("Item Specific");
        }

        if(path.contains("components.bundle_contents")) {
            if(path.endsWith("components.bundle_contents"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.bundle_contents[0]"))
                return PathInfos.ITEM_NODE;
        }

        if(path.contains("components.can_break")) {
            if(path.endsWith("components.can_break"))
                return (new PathInfo(List.of("predicates","show_in_tooltip")));
            if(path.endsWith("components.can_break.predicates"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.can_break.predicates[0]"))
                return (new PathInfo(List.of("blocks","nbt","state")));
            if(path.endsWith("components.can_break.predicates[0].blocks"))
                return PathInfos.getBlockPredicateBlocks();
            if(path.endsWith("components.can_break.predicates[0].nbt"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_break.predicates[0].state"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_break.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.can_place_on")) {
            if(path.endsWith("components.can_place_on"))
                return (new PathInfo(List.of("predicates","show_in_tooltip")));
            if(path.endsWith("components.can_place_on.predicates"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.can_place_on.predicates[0]"))
                return (new PathInfo(List.of("blocks","nbt","state")));
            if(path.endsWith("components.can_place_on.predicates[0].blocks"))
                return PathInfos.getBlockPredicateBlocks();
            if(path.endsWith("components.can_place_on.predicates[0].nbt"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_place_on.predicates[0].state"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_place_on.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.charged_projectiles")) {
            if(path.endsWith("components.charged_projectiles"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.charged_projectiles[0]"))
                return PathInfos.ITEM_NODE;
        }

        if(path.contains("components.container")) {
            if(path.endsWith("components.container"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.container[0]"))
                return (new PathInfo(List.of("item","slot")));
            if(path.endsWith("components.container[0].item"))
                return PathInfos.ITEM_NODE.asRequired();
            if(path.endsWith("components.container[0].slot"))
                return PathInfos.INT.asRequired();
        }

        if(path.contains("components.container_loot")) {
            if(path.endsWith("components.container_loot"))
                return (new PathInfo(List.of("loot_table","seed")));
            if(path.endsWith("components.container_loot.loot_table"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.LOOT)).asRequired();
            if(path.endsWith("components.container_loot.seed"))
                return PathInfos.LONG;
        }

        if(path.endsWith("components.custom_data"))
            return PathInfos.DEFAULT.withDesc(Text.of("Unstructured NBT unused ingame (any NBT type)"));

        if(path.endsWith("components.custom_model_data"))
            return PathInfos.INT;

        if(path.endsWith("components.custom_name"))
            return PathInfos.TEXT.withDesc(Text.of("This is for renamed items and will appear in italics. See item_name to completely override the vanilla name."));

        if(path.endsWith("components.damage"))
            return (new PathInfo(PathType.INT,new String[]{"0"}));

        if(path.endsWith("components.debug_stick_state"))
            return PathInfos.DEFAULT;

        if(path.contains("components.dyed_color")) {
            if(path.endsWith("components.dyed_color"))
                return (new PathInfo(List.of("rgb","show_in_tooltip")));
            if(path.endsWith("components.dyed_color.rgb"))
                return PathInfos.DECIMAL_COLOR.asRequired();
            if(path.endsWith("components.dyed_color.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.enchantment_glint_override"))
            return PathInfos.TRINARY;

        if(path.contains("components.enchantments")) {
            if(path.endsWith("components.enchantments"))
                return (new PathInfo(List.of("levels","show_in_tooltip")));
            if(path.endsWith("components.enchantments.levels"))
                return (new PathInfo(List.of(FortytwoEdit.ENCHANTS))).asRequired();
            if(path.endsWith("components.enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }
        
        if(path.contains("enchantments.levels.")) {
            int maxLvl = 1;
            for(String e : FortytwoEdit.ENCHANTS) {
                if(path.endsWith("enchantments.levels."+e.replace("minecraft:",""))) {
                    Enchantment ench = Registries.ENCHANTMENT.get(new Identifier(e));
                    if(ench != null)
                        maxLvl = ench.getMaxLevel();
                }
            }
            return (new PathInfo(PathType.INT,BlackMagick.getIntRangeArray(1,maxLvl))).withDesc(Text.of("Max vanilla level: "+maxLvl));
        }

        if(path.contains(".entity_data")) {
            if(path.endsWith(".entity_data"))
                return (new PathInfo(List.of("id",
                "CustomName","CustomNameVisible","Glowing","HasVisualFire","Invulnerable","Motion","NoGravity","Pos","Rotation","Silent","Tags",
                "active_effects","ArmorDropChances","ArmorItems","Attributes","CanPickUpLoot","FallFlying","Health","HandDropChances","HandItems","leash","LeftHanded","NoAI","PersistenceRequired","Team",
                "DisabledSlots","Invisible","Marker","NoBasePlate","Pose","ShowArms","Small",
                "Fixed","Invisible","Item","ItemDropChance","ItemRotation",
                "beam_target","ShowBottom",
                "SoundEvent",
                "Duration","DurationOnUse","potion_contents","Particle","Radius","RadiusOnUse","RadiusPerTick","ReapplicationDelay","WaitTime")));
            if(path.endsWith(".entity_data.id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.ENTITIES)).asRequired();
            if(path.endsWith(".entity_data.Air"))
                return PathInfos.SHORT;
            if(path.endsWith(".entity_data.CustomName"))
                return PathInfos.TEXT;
            if(path.endsWith(".entity_data.CustomNameVisible"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.FallDistance"))
                return PathInfos.FLOAT;
            if(path.endsWith(".entity_data.Fire"))
                return PathInfos.SHORT;
            if(path.endsWith(".entity_data.Glowing"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.HasVisualFire"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.Invulnerable"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.Motion"))
                return (new PathInfo(PathType.INLINE_LIST,new String[]{"[0d,0d,0d]"})).withDesc(Text.of("[x, y, z] motion in each direction\nx - east\ny - up\nz - south"));
            if(path.endsWith(".entity_data.NoGravity"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.OnGround"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.Passengers"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith(".entity_data.PortalCooldown"))
                return PathInfos.INT;
            if(path.endsWith(".entity_data.Pos"))
                return (new PathInfo(PathType.INLINE_LIST,new String[]{"[0d,0d,0d]"})).withDesc(Text.of("[x, y, z]"));
            if(path.endsWith(".entity_data.Rotation"))
                return (new PathInfo(PathType.INLINE_LIST,new String[]{"[0f,0f]"})).withDesc(Text.of("[Yaw, Pitch]\nYaw: -180 to 180 (0 is south, 90 is west)\nPitch: -90 (up) to 90 (down)"));
            if(path.endsWith(".entity_data.Silent"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.Tags"))
                return PathInfos.LIST_STRING;
            if(path.endsWith(".entity_data.Tags[0]"))
                return PathInfos.STRING;
            if(path.endsWith(".entity_data.TicksFrozen"))
                return PathInfos.INT;
            if(path.endsWith(".entity_data.UUID"))
                return PathInfos.UUID;
            
            String lbl = "Mobs";
            if(path.endsWith(".entity_data.active_effects"))
                return PathInfos.LIST_COMPOUND.withGroup(lbl);
            if(path.endsWith(".entity_data.active_effects[0]") || path.endsWith(".hidden_effect"))
                return (new PathInfo(List.of("ambient","amplifier","duration","hidden_effect","id","show_icon","show_particles"))).withGroup(lbl);
            if(path.endsWith(".entity_data.active_effects[0].ambient") || path.endsWith(".hidden_effect.ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.active_effects[0].amplifier") || path.endsWith(".hidden_effect.amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith(".entity_data.active_effects[0].duration") || path.endsWith(".hidden_effect.duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith(".entity_data.active_effects[0].id") || path.endsWith(".hidden_effect.id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.EFFECTS));
            if(path.endsWith(".entity_data.active_effects[0].show_icon") || path.endsWith(".hidden_effect.show_icon"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.active_effects[0].show_particles") || path.endsWith(".hidden_effect.show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.ArmorDropChances"))
                return (new PathInfo(PathType.INLINE_LIST,new String[]{"[0f,0f,0f,0f]","[1f,1f,1f,1f]"})).withDesc(Text.of("[feet, legs, chest, head]")).withGroup(lbl);
            if(path.endsWith(".entity_data.ArmorItems"))
                return PathInfos.LIST_COMPOUND.withDesc(Text.of("[feet, legs, chest, head]")).withGroup(lbl);
            if(path.endsWith(".entity_data.ArmorItems[0]"))
                return PathInfos.ITEM_NODE;
            if(path.endsWith(".entity_data.Attributes"))
                return PathInfos.DEFAULT.withGroup(lbl);
            if(path.endsWith(".entity_data.CanPickUpLoot"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.FallFlying"))
                return PathInfos.TRINARY.withDesc(Text.of("If true, mob will glide if wearing an elytra")).withGroup(lbl);
            if(path.endsWith(".entity_data.Health"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.HandDropChances"))
                return (new PathInfo(PathType.INLINE_LIST,new String[]{"[0f,0f]","[1f,1f]"})).withDesc(Text.of("[mainhand, offhand]")).withGroup(lbl);
            if(path.endsWith(".entity_data.HandItems"))
                return PathInfos.LIST_COMPOUND.withDesc(Text.of("[mainhand, offhand]")).withGroup(lbl);
            if(path.endsWith(".entity_data.HandItems[0]"))
                return PathInfos.ITEM_NODE;
            if(path.endsWith(".entity_data.leash"))
                return (new PathInfo(PathType.INLINE_COMPOUND,new String[]{"{UUID:[I;0,0,0,0]}","{X:0,Y:0,Z:0}"})).withDesc(Text.of("Compound with int array UUID or int X, Y, and Z values")).withGroup(lbl);
            if(path.endsWith(".entity_data.LeftHanded"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.NoAI"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.PersistenceRequired"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.Team"))
                return PathInfos.STRING.withDesc(Text.of("Name of team for the mob to join when spawning")).withGroup(lbl);

            lbl = "Common";
            if(path.endsWith(".entity_data.Invisible"))
                return PathInfos.TRINARY.withGroup(lbl).withDesc(Text.of("Used by armor stands and item frames"));

            lbl = "Armor Stands";
            if(path.endsWith(".entity_data.DisabledSlots"))
                return (new PathInfo(PathType.INT,new String[]{"16191"})).withDesc(Text.of("Value of 16191 prevents adding, changing, or removing armor or hand items")).withGroup(lbl);
            if(path.endsWith(".entity_data.Marker"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.NoBasePlate"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.Pose"))
                return PathInfos.DEFAULT.withGroup(lbl);
            if(path.endsWith(".entity_data.ShowArms"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.Small"))
                return PathInfos.TRINARY.withGroup(lbl);

            lbl = "Item Frames";
            if(path.endsWith(".entity_data.Fixed"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.Item"))
                return PathInfos.ITEM_NODE.withGroup(lbl);
            if(path.endsWith(".entity_data.ItemDropChance"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.ItemRotation"))
                return (new PathInfo(PathType.BYTE,new String[]{"0","1","2","3","4","5","6","7"})).withDesc(Text.of("Number of times the item is rotated clockwise")).withGroup(lbl);

            lbl = "End Crystals";
            if(path.endsWith(".entity_data.beam_target"))
                return PathInfos.INT_ARRAY_POS.withGroup(lbl);
            if(path.endsWith(".entity_data.ShowBottom"))
                return PathInfos.TRINARY.withGroup(lbl);

            lbl = "Arrows and Tridents";
            if(path.endsWith(".entity_data.SoundEvent"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.SOUNDS)).withDesc(Text.of("Arrows types and tridents will play this sound when hitting something")).withGroup(lbl);

            lbl = "Area Effect Clouds";
            if(path.endsWith(".entity_data.Duration"))
                return PathInfos.INT.withDesc(Text.of("Max age after WaitTime")).withGroup(lbl);
            if(path.endsWith(".entity_data.DurationOnUse"))
                return PathInfos.INT.withGroup(lbl);
            if(path.endsWith(".entity_data.potion_contents"))
                return PathInfos.POTION_CONTENTS.withGroup(lbl);
            if(path.endsWith(".entity_data.Particle"))
                return (new PathInfo(PathType.INLINE_COMPOUND,BlackMagick.formatSuggs(FortytwoEdit.PARTICLES,"{type:\"","\"}"))).withDesc(Text.of("Format like {type:\"dust\",color:[.5d,0d,1d],scale:2}")).withGroup(lbl);
            if(path.endsWith(".entity_data.Radius"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.RadiusOnUse"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.RadiusPerTick"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.ReapplicationDelay"))
                return PathInfos.INT.withGroup(lbl);
            if(path.endsWith(".entity_data.WaitTime"))
                return PathInfos.INT.withDesc(Text.of("Time before cloud can have a radius and effect (particles will still appear in the center)")).withGroup(lbl);

            // when adding new paths, also add keys to entity_data compound
            // if a key is already used for another entity, move it to common category
        }

        if(path.endsWith("components.fire_resistant"))
            return PathInfos.UNIT;

        if(path.contains("components.firework_explosion")) {
            if(path.endsWith("components.firework_explosion"))
                return (new PathInfo(List.of("shape","colors","fade_colors","has_trail","has_twinkle")));
            if(path.endsWith("components.firework_explosion.shape"))
                return (new PathInfo(PathType.STRING,new String[]{"small_ball","large_ball","star","creeper","burst"})).asRequired();
            if(path.endsWith("components.firework_explosion.colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.firework_explosion.fade_colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.firework_explosion.has_trail"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.firework_explosion.has_twinkle"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.fireworks")) {
            if(path.endsWith("components.fireworks"))
                return (new PathInfo(List.of("explosions","flight_duration")));
            if(path.endsWith("components.fireworks.explosions"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.fireworks.explosions[0]"))
                return (new PathInfo(List.of("shape","colors","fade_colors","has_trail","has_twinkle")));
            if(path.endsWith("components.fireworks.explosions[0].shape"))
                return (new PathInfo(PathType.STRING,new String[]{"small_ball","large_ball","star","creeper","burst"})).asRequired();
            if(path.endsWith("components.fireworks.explosions[0].colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].fade_colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].has_trail"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.explosions[0].has_twinkle"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.flight_duration"))
                return (new PathInfo(PathType.BYTE,new String[]{"1","2","3"}));
        }

        if(path.contains("components.food")) {
            if(path.endsWith("components.food"))
                return (new PathInfo(List.of("nutrition","saturation","is_meat","can_always_eat","eat_seconds","effects")));
            if(path.endsWith("components.food.nutrition"))
                return PathInfos.INT.withDesc(Text.of("How many food points to restore (1 nutrition for each half of a food icon)")).asRequired();
            if(path.endsWith("components.food.saturation"))
                return PathInfos.FLOAT.asRequired();
            if(path.endsWith("components.food.is_meat"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.can_always_eat"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.eat_seconds"))
                return (new PathInfo(PathType.FLOAT,new String[]{"1.6f"}));
            if(path.endsWith("components.food.effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.food.effects[0]"))
                return (new PathInfo(List.of("effect","probability")));
            if(path.endsWith("components.food.effects[0].effect"))
                return PathInfos.EFFECT_NODE;
            if(path.endsWith("components.food.effects[0].effect.id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.EFFECTS)).asRequired();
            if(path.endsWith("components.food.effects[0].effect.amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith("components.food.effects[0].effect.duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith("components.food.effects[0].effect.ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.effects[0].effect.show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.effects[0].effect.show_icon"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.effects[0].probability"))
                return (new PathInfo(PathType.FLOAT,new String[]{"1f"})).withDesc(Text.of("Chance for effect to be applied when food is eaten from 0f to 1f"));
        }

        if(path.endsWith("components.hide_additional_tooltip"))
            return PathInfos.UNIT;

        if(path.endsWith("components.hide_tooltip"))
            return PathInfos.UNIT;

        if(path.endsWith("components.instrument"))
            return (new PathInfo(PathType.STRING,new String[]{"ponder_goat_horn","sing_goat_horn","seek_goat_horn",
                "feel_goat_horn","admire_goat_horn","call_goat_horn","yearn_goat_horn","dream_goat_horn"}));

        if(path.endsWith("components.intangible_projectile"))
            return PathInfos.UNIT;

        if(path.endsWith("components.item_name"))
            return PathInfos.TEXT;

        if(path.endsWith("components.lock"))
            return PathInfos.STRING.withDesc(Text.of("Literal string of item name needed to unlock"));

        if(path.contains("components.lodestone_tracker")) {
            if(path.endsWith("components.lodestone_tracker"))
                return (new PathInfo(List.of("target","tracked")));
            if(path.endsWith("components.lodestone_tracker.target"))
                return (new PathInfo(List.of("pos","dimension")));
            if(path.endsWith("components.lodestone_tracker.target.pos"))
                return PathInfos.INT_ARRAY_POS.asRequired();
            if(path.endsWith("components.lodestone_tracker.target.dimension"))
                return (new PathInfo(PathType.STRING,new String[]{"overworld","the_nether","the_end"})).asRequired();
            if(path.endsWith("components.lodestone_tracker.tracked"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.lore")) {
            if(path.endsWith("components.lore"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.lore[0]"))
                return PathInfos.TEXT;
        }

        if(path.endsWith("components.map_color"))
            return PathInfos.DECIMAL_COLOR;

        if(path.endsWith("components.map_decorations"))
            return PathInfos.DEFAULT;

        if(path.endsWith("components.map_id"))
            return PathInfos.INT;

        if(path.endsWith("components.max_damage"))
            return (new PathInfo(PathType.INT,new String[]{""+ToolMaterials.WOOD.getDurability(),""+ToolMaterials.STONE.getDurability(),
                ""+ToolMaterials.GOLD.getDurability(),""+ToolMaterials.IRON.getDurability(),""+ToolMaterials.DIAMOND.getDurability(),
                ""+ToolMaterials.NETHERITE.getDurability()})).withDesc(Text.of("Default values for reference:\n  Wood tools - "+ToolMaterials.WOOD.getDurability()
                +"\n  Stone tools - "+ToolMaterials.STONE.getDurability()+"\n  Gold tools - "+ToolMaterials.GOLD.getDurability()
                +"\n  Iron tools - "+ToolMaterials.IRON.getDurability()+"\n  Diamond tools - "+ToolMaterials.DIAMOND.getDurability()
                +"\n  Netherite tools - "+ToolMaterials.NETHERITE.getDurability()));

        if(path.endsWith("components.max_stack_size"))
            return PathInfos.ITEM_COUNT;

        if(path.endsWith("components.note_block_sound"))
            return (new PathInfo(PathType.STRING,FortytwoEdit.SOUNDS)).withDesc(Text.of("Used for player heads on a note block"));

        if(path.endsWith("components.ominous_bottle_amplifier"))
            return (new PathInfo(PathType.INT,new String[]{"0","1","2","3","4"}));

        if(path.contains("components.pot_decorations")) {
            if(path.endsWith("components.pot_decorations"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.pot_decorations[0]"))
                return (new PathInfo(PathType.STRING,new String[]{"brick","angler_pottery_sherd","archer_pottery_sherd","arms_up_pottery_sherd","blade_pottery_sherd","brewer_pottery_sherd","burn_pottery_sherd","danger_pottery_sherd","explorer_pottery_sherd","flow_pottery_sherd","friend_pottery_sherd","guster_pottery_sherd","heart_pottery_sherd","heartbreak_pottery_sherd","howl_pottery_sherd","miner_pottery_sherd","mourner_pottery_sherd","plenty_pottery_sherd","prize_pottery_sherd","scrape_pottery_sherd","sheaf_pottery_sherd","shelter_pottery_sherd","skull_pottery_sherd","snort_pottery_sherd"}));
        }

        if(path.contains(".potion_contents")) {
            if(path.endsWith(".potion_contents"))
                return PathInfos.POTION_CONTENTS;
            if(path.endsWith(".potion_contents.potion"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.EFFECTS)).withDesc(Text.of("Potion base before custom_color and custom_effects"));
            if(path.endsWith(".potion_contents.custom_color"))
                return PathInfos.DECIMAL_COLOR;
            if(path.endsWith(".potion_contents.custom_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith(".potion_contents.custom_effects[0]"))
                return PathInfos.EFFECT_NODE;
            if(path.endsWith(".potion_contents.custom_effects[0].id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.EFFECTS)).asRequired();
            if(path.endsWith(".potion_contents.custom_effects[0].amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith(".potion_contents.custom_effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith(".potion_contents.custom_effects[0].ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith(".potion_contents.custom_effects[0].show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith(".potion_contents.custom_effects[0].show_icon"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.profile")) {
            if(path.endsWith("components.profile"))
                return (new PathInfo(List.of("name","id","properties")));
            if(path.endsWith("components.profile.name"))
                return PathInfos.STRING.withDesc(Text.of("Player name used to update skin"));
            if(path.endsWith("components.profile.id"))
                return PathInfos.UUID.withDesc(Text.of("Player UUID used to update skin"));
            if(path.endsWith("components.profile.properties"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.profile.properties[0]"))
                return (new PathInfo(List.of("name","value","signature")));
            if(path.endsWith("components.profile.properties[0].name"))
                return (new PathInfo(PathType.STRING,new String[]{"textures"})).withDesc(Text.of("Currently only used for textures")).asRequired();
            if(path.endsWith("components.profile.properties[0].value"))
                return PathInfos.STRING.asRequired();
            if(path.endsWith("components.profile.properties[0].signature"))
                return PathInfos.STRING;
        }

        if(path.endsWith("components.rarity"))
            return (new PathInfo(PathType.STRING,new String[]{"common","uncommon","rare","epic"})).withDesc(Text.of("Used for item name color:\n  common\n  \u00a7euncommon\n  \u00a7brare\n  \u00a7depic\u00a7r"));

        if(path.contains("components.recipes")) {
            if(path.endsWith("components.recipes"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.recipes[0]"))
                return PathInfos.DEFAULT;
        }

        if(path.endsWith("components.repair_cost"))
            return (new PathInfo(PathType.INT,new String[]{"0",""+Integer.MAX_VALUE}));

        if(path.contains("components.stored_enchantments")) {
            if(path.endsWith("components.stored_enchantments"))
                return (new PathInfo(List.of("levels","show_in_tooltip")));
            if(path.endsWith("components.stored_enchantments.levels"))
                return (new PathInfo(List.of(FortytwoEdit.ENCHANTS))).asRequired();
            if(path.endsWith("components.stored_enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.suspicious_stew_effects")) {
            if(path.endsWith("components.suspicious_stew_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.suspicious_stew_effects[0]"))
                return (new PathInfo(List.of("id","duration")));
            if(path.endsWith("components.suspicious_stew_effects[0].id"))
                return (new PathInfo(PathType.STRING,FortytwoEdit.EFFECTS)).asRequired();
            if(path.endsWith("components.suspicious_stew_effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
        }

        if(path.contains("components.tool")) {
            if(path.endsWith("components.tool"))
                return (new PathInfo(List.of("default_mining_speed","damage_per_block","rules")));
            if(path.endsWith("components.tool.default_mining_speed"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.tool.damage_per_block"))
                return PathInfos.INT;
            if(path.endsWith("components.tool.rules"))
                return PathInfos.LIST_COMPOUND.asRequired();
            if(path.endsWith("components.tool.rules[0]"))
                return (new PathInfo(List.of("blocks","speed","correct_for_drops")));
            if(path.endsWith("components.tool.rules[0].blocks"))
                return PathInfos.LIST_STRING.asRequired();
            if(path.endsWith("components.tool.rules[0].blocks[0]"))
                return PathInfos.getBlockPredicateBlocks();
            if(path.endsWith("components.tool.rules[0].speed"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.tool.rules[0].correct_for_drops"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.trim")) {
            if(path.endsWith("components.trim"))
                return (new PathInfo(List.of("pattern","material","show_in_tooltip")));
            if(path.endsWith("components.trim.pattern"))
                return (new PathInfo(PathType.STRING,new String[]{"bolt","coast","dune","eye","flow","host","raiser","rib","sentry","shaper","silence","snout","spire","tide","vex","ward","wayfinder","wild"})).asRequired();
            if(path.endsWith("components.trim.material"))
                return (new PathInfo(PathType.STRING,new String[]{"amethyst","copper","diamond","emerald","gold","iron","lapis","netherite","quartz","redstone"})).asRequired();
            if(path.endsWith("components.trim.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.unbreakable"))
            return PathInfos.TOOLTIP_UNIT;

        if(path.contains("components.writable_book_content")) {
            if(path.endsWith("components.writable_book_content"))
                return (new PathInfo(List.of("pages")));
            if(path.endsWith("components.writable_book_content.pages"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.writable_book_content.pages[0]"))
                return (new PathInfo(List.of("raw","filtered")));
            if(path.endsWith("components.writable_book_content.pages[0].raw"))
                return PathInfos.STRING.withDesc(Text.of("Literal string of page text")).asRequired();
            if(path.endsWith("components.writable_book_content.pages[0].filtered"))
                return PathInfos.STRING;
        }

        if(path.contains("components.written_book_content")) {
            if(path.endsWith("components.written_book_content"))
                return (new PathInfo(List.of("pages","title","author","generation","resolved")));
            if(path.endsWith("components.written_book_content.pages"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.written_book_content.pages[0]"))
                return (new PathInfo(List.of("raw","filtered")));
            if(path.endsWith("components.written_book_content.pages[0].raw"))
                return PathInfos.TEXT.asRequired();
            if(path.endsWith("components.written_book_content.pages[0].filtered"))
                return PathInfos.TEXT;
            if(path.endsWith("components.written_book_content.title"))
                return (new PathInfo(List.of("raw","filtered"))).asRequired();
            if(path.endsWith("components.written_book_content.title.raw"))
                return PathInfos.STRING.withDesc(Text.of("Literal string of title")).asRequired();
            if(path.endsWith("components.written_book_content.title.filtered"))
                return PathInfos.STRING;
            if(path.endsWith("components.written_book_content.author"))
                return PathInfos.STRING.withDesc(Text.of("Literal string of author")).asRequired();
            if(path.endsWith("components.written_book_content.generation"))
                return (new PathInfo(PathType.INT,new String[]{"0","1","2","3"})).withDesc(Text.of("0 - Original\n1 - Copy of original\n2 - Copy of copy\n3 - Tattered"));
            if(path.endsWith("components.written_book_content.resolved"))
                return PathInfos.TRINARY.withDesc(Text.of("Whether or not JSON is resolved (for selectors/scores/etc)"));
        }

        if(path.endsWith("id"))
            return (new PathInfo(PathType.STRING,FortytwoEdit.ITEMS)).asRequired();

        if(path.endsWith("count"))
            return PathInfos.ITEM_COUNT;

        if(path.endsWith("components"))
            return (new PathInfo(List.of(FortytwoEdit.COMPONENTS)));

        FortytwoEdit.LOGGER.warn("No PathInfo found for path: "+path);
        return PathInfos.UNKNOWN;
    }

    /**
     * <p> PathType type - to setup widget </p>
     * <p> String[] suggs - for textbox suggestions </p>
     * <p> Text description - displays in tooltip </p>
     * <p> List<String> keys - keys that can be included in compound (only for PathType.COMPOUND) </p>
     * <p> byte listType - NbtElement.getType() type of list (only for PathType.LIST) </p>
     */
    public record PathInfo(PathType type, String[] suggs, Text description, List<String> keys, byte listType, String keyGroup, boolean dynamic) {

        private static final byte DEFAULT_LIST_TYPE = (byte)(-1);
        private static final String DEFAULT_GROUP = null;

        /**
         * Create a PathInfo with only the type specified.
         * This should be used very rarely.
         * 
         * @param type
         */
        public PathInfo(PathType type) {
            this(type,null);
        }

        /**
         * Create a PathInfo with type and suggs.
         * Commonly used for simple NBT types.
         * 
         * @param type
         * @param suggs
         */
        public PathInfo(PathType type, String[] suggs) {
            this(type,suggs,null,null,DEFAULT_LIST_TYPE,DEFAULT_GROUP,false);
        }

        /**
         * Create a PathInfo for a compound.
         * 
         * @param keys all keys that may be in the compound
         */
        public PathInfo(List<String> keys) {
            this(PathType.COMPOUND,null,null,keys,DEFAULT_LIST_TYPE,DEFAULT_GROUP,false);
        }

        /**
         * Create a PathInfo for a list.
         * 
         * @param listType the NbtElement.getType() that should be in the list
         */
        public PathInfo(byte listType) {
            this(PathType.LIST,null,null,null,listType,DEFAULT_GROUP,false);
        }

        /**
         * Add a description to the PathInfo.
         * 
         * @param desc
         * @return a copy with the description added
         */
        public PathInfo withDesc(Text desc) {
            return new PathInfo(this.type, this.suggs, desc, this.keys, this.listType, this.keyGroup, this.dynamic);
        }

        /**
         * Add a custom group label to the PathInfo.
         * Keys in a compound will be grouped in their label.
         * Do not use with required()
         * 
         * @param num
         * @return a copy with the group number added.
         */
        public PathInfo withGroup(String group) {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, group, this.dynamic);
        }

        /**
         * Specify path as required in its parent compound.
         * Do not use with withGroup()
         * 
         * @return
         */
        public PathInfo asRequired() {
            return this.withGroup("Required");
        }

        /**
         * Specify that the PathInfo for this path should not be cached.
         * Do this if the PathInfo depends on other factors (such as the current item).
         * 
         * @return
         */
        public PathInfo asDynamic() {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, this.keyGroup, true);
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
        UNKNOWN,        // return when no PathInfo is found (treated as any stringified nbt)
        DEFAULT,        // can be used for any stringified nbt
        TEXT,           // use for Raw JSON
        DECIMAL_COLOR,  // use for integer color fields
        UNIT,           // represents nbt that is either absent or {}
        TRINARY,        // represents nbt that is either absent, 0b, or 1b
        TOOLTIP_UNIT,   // represents nbt that is either absent, {}, or {show_in_tooltip:0b}
        UUID,           // an int array with 4 integers
        INLINE_LIST,    // a list that is edited in a text field (such as Motion/Pos/Rotation)
        INLINE_COMPOUND // a compound edited in a text field

    }

    /**
     * Detect if PathType has a dedicated screen or if it is edited inline.
     * Trinary/binary is considered inline.
     * 
     * @param type
     * @return true if PathType has a dedicated screen
     */
    public static boolean isComplex(PathType type) {
        if(type != null) {
            switch(type) {
                case BYTE: return false;
                case SHORT: return false;
                case INT: return false;
                case LONG: return false;
                case DOUBLE: return false;
                case FLOAT: return false;
                case STRING: return false;

                case BYTE_ARRAY: return false;
                case INT_ARRAY: return false;
                case LONG_ARRAY: return false;

                case COMPOUND: return true;
                case LIST: return true;

                case UNKNOWN: return false;
                case DEFAULT: return false;
                case TEXT: return true;
                case DECIMAL_COLOR: return true;
                case UNIT: return false;
                case TRINARY: return false;
                case TOOLTIP_UNIT: return false;
                case UUID: return false;
                case INLINE_LIST: return false;
                case INLINE_COMPOUND: return false;
            }
        }
        return false;
    }

    /**
     * Returns NbtElement type or -1
     * 
     * @param type
     * @return -1 or NbtElement type
     */
    public static byte pathTypeToNbtType(PathType type) {
        if(type != null) {
            switch(type) {
                case BYTE: return NbtElement.BYTE_TYPE;
                case SHORT: return NbtElement.SHORT_TYPE;
                case INT: return NbtElement.INT_TYPE;
                case LONG: return NbtElement.LONG_TYPE;
                case DOUBLE: return NbtElement.DOUBLE_TYPE;
                case FLOAT: return NbtElement.FLOAT_TYPE;
                case STRING: return NbtElement.STRING_TYPE;

                case BYTE_ARRAY: return NbtElement.BYTE_ARRAY_TYPE;
                case INT_ARRAY: return NbtElement.INT_ARRAY_TYPE;
                case LONG_ARRAY: return NbtElement.LONG_ARRAY_TYPE;

                case COMPOUND: return NbtElement.COMPOUND_TYPE;
                case LIST: return NbtElement.LIST_TYPE;

                case UNKNOWN: return -1;
                case DEFAULT: return -1;
                case TEXT: return NbtElement.STRING_TYPE;
                case DECIMAL_COLOR: return NbtElement.INT_TYPE;
                case UNIT: return NbtElement.COMPOUND_TYPE;
                case TRINARY: return NbtElement.BYTE_TYPE;
                case TOOLTIP_UNIT: return NbtElement.COMPOUND_TYPE;
                case UUID: return NbtElement.INT_ARRAY_TYPE;
                case INLINE_LIST: return NbtElement.LIST_TYPE;
                case INLINE_COMPOUND: return NbtElement.COMPOUND_TYPE;
            }
        }
        return -1;
    }

    /**
     * Returns string label for NBT type, or null.
     * 
     * @param type from NbtElement.getType()
     * @return null or String label
     */
    public static String formatNbtType(byte type) {
        switch(type) {
            case NbtElement.BYTE_TYPE: return "Byte";
            case NbtElement.SHORT_TYPE: return "Short";
            case NbtElement.INT_TYPE: return "Int";
            case NbtElement.LONG_TYPE: return "Long";
            case NbtElement.DOUBLE_TYPE: return "Double";
            case NbtElement.FLOAT_TYPE: return "Float";
            case NbtElement.STRING_TYPE: return "String";

            case NbtElement.BYTE_ARRAY_TYPE: return "Byte Array";
            case NbtElement.INT_ARRAY_TYPE: return "Int Array";
            case NbtElement.LONG_ARRAY_TYPE: return "Long Array";

            case NbtElement.COMPOUND_TYPE: return "Compound";
            case NbtElement.LIST_TYPE: return "List";

            default: return null;
        }
    }

    private class PathInfos {

        private static final PathInfo UNKNOWN = (new PathInfo(PathType.UNKNOWN));
        private static final PathInfo DEFAULT = (new PathInfo(PathType.DEFAULT));
        private static final PathInfo UNIT = (new PathInfo(PathType.UNIT,new String[]{"","{}"})).withDesc(Text.of("{} represents true"));
        private static final PathInfo TOOLTIP_UNIT = (new PathInfo(PathType.TOOLTIP_UNIT,new String[]{"","{show_in_tooltip:0b}","{}"})).withDesc(Text.of("{} or {show_in_tooltip:0b}"));
        private static final PathInfo TRINARY = (new PathInfo(PathType.TRINARY,new String[]{"","0b","1b"})).withDesc(Text.of("Boolean 0b (false) or 1b (true)"));
        private static final PathInfo SHORT = (new PathInfo(PathType.SHORT,new String[]{"0s"}));
        private static final PathInfo INT = (new PathInfo(PathType.INT,new String[]{"0"}));
        private static final PathInfo LONG = (new PathInfo(PathType.LONG,new String[]{"0l"}));
        private static final PathInfo DOUBLE = (new PathInfo(PathType.DOUBLE,new String[]{"0.0d"}));
        private static final PathInfo FLOAT = (new PathInfo(PathType.FLOAT,new String[]{"0.0f"}));
        private static final PathInfo STRING = (new PathInfo(PathType.STRING));
        private static final PathInfo LIST_COMPOUND = (new PathInfo(NbtElement.COMPOUND_TYPE));
        private static final PathInfo LIST_STRING = (new PathInfo(NbtElement.STRING_TYPE));
        private static final PathInfo INLINE_COMPOUND = (new PathInfo(PathType.INLINE_COMPOUND,new String[]{"{}"}));

        private static final PathInfo TEXT = (new PathInfo(PathType.TEXT,new String[]{"'{\"text\":\"\"}'"})).withDesc(Text.of("Raw JSON text"));
        private static final PathInfo DECIMAL_COLOR = (new PathInfo(PathType.DECIMAL_COLOR,new String[]{"0","16777215"})).withDesc(Text.of("0xRRGGBB hex color converted to integer"));
        private static final PathInfo UUID = (new PathInfo(PathType.UUID,new String[]{"[I;0,0,0,0]"}));
        private static final PathInfo INT_ARRAY_POS = (new PathInfo(PathType.INT_ARRAY,new String[]{"[I;0,0,0]"})).withDesc(Text.of("[I; X, Y, Z] block coordinates"));

        private static final PathInfo ITEM_NODE = (new PathInfo(List.of("id","count","components")));
        private static final PathInfo ITEM_COUNT = (new PathInfo(PathType.INT,new String[]{"1","16","64","99"}));

        private static final PathInfo POTION_CONTENTS = (new PathInfo(List.of("potion","custom_color","custom_effects")));
        private static final PathInfo EFFECT_NODE = (new PathInfo(List.of("id","amplifier","duration","ambient","show_particles","show_icon")));
        private static final PathInfo EFFECT_DURATION = (new PathInfo(PathType.INT,new String[]{"-1","1"})).withDesc(Text.of("Duration in ticks or -1 for infinity"));
        private static final PathInfo EFFECT_AMPLIFIER = (new PathInfo(PathType.BYTE,new String[]{"0","255"})).withDesc(Text.of("Amplifier 0-255 gives effect level 1-256"));

        private static PathInfo CACHE_BLOCK_PREDICATE_BLOCKS = null;
        private static PathInfo getBlockPredicateBlocks() {
            if(CACHE_BLOCK_PREDICATE_BLOCKS == null)
                CACHE_BLOCK_PREDICATE_BLOCKS = (new PathInfo(PathType.DEFAULT,FortytwoEdit.joinCommandSuggs(new String[][]{
                    BlackMagick.formatStringSuggs(FortytwoEdit.joinCommandSuggs(new String[][]{FortytwoEdit.BLOCKS,FortytwoEdit.BLOCKTAGS},null,null))},
                    null,new String[]{"[\"stone\",\"dirt\"]"})))
                    .withDesc(Text.of("Can be either:\na) NbtString of a block ID or block tag\nb) NbtList of block ID NbtStrings"));
            return CACHE_BLOCK_PREDICATE_BLOCKS;
        }
    
    }

}
