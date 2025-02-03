package baphomethlabs.fortytwoedit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import baphomethlabs.fortytwoedit.mixin.DecoratedPotPatternsAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyBindingAccessor;
import baphomethlabs.fortytwoedit.mixin.TranslationStorageAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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

    private static final Map<String,PathInfo> pathInfoCaches = Maps.newHashMap();

    public static void clearCacheInfo() {
        pathInfoCaches.clear();
        STATIC_LIST_CACHES.clear();
        clearDynamicListCaches();
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

        if(pathInfoCaches.containsKey(path))
            return pathInfoCaches.get(path);
        PathInfo pi = getNewPathInfo(path);
        if(!pi.dynamic())
            pathInfoCaches.put(path,pi);
        return pi;
    }

    private static PathInfo getNewPathInfo(String path) {

        if(path.contains("components.attribute_modifiers")) {
            if(path.endsWith("components.attribute_modifiers"))
                return (new PathInfo(List.of("modifiers","show_in_tooltip"))).withIcon(Items.DIAMOND_SWORD);
            if(path.endsWith("components.attribute_modifiers.modifiers"))
                return PathInfos.LIST_COMPOUND.asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0]"))
                return (new PathInfo(List.of("type","slot","id","amount","operation"))).withFlag(PathFlag.ATTRIBUTE);
            if(path.endsWith("components.attribute_modifiers.modifiers[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_ATTRIBUTE.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].slot"))
                return (new PathInfo(PathType.STRING,LIST_ATTRIBUTE_MODIFIER_SLOT.get().toArray(new String[0])));
            if(path.endsWith("components.attribute_modifiers.modifiers[0].id"))
                return (new PathInfo(PathType.STRING,new String[]{"minecraft:armor.body","minecraft:armor.boots","minecraft:armor.chestplate","minecraft:armor.helmet","minecraft:armor.leggings",Item.BASE_ATTACK_DAMAGE_MODIFIER_ID.toString(),Item.BASE_ATTACK_SPEED_MODIFIER_ID.toString()}).withDesc(Text.of("Unique namespaced ID used to update modifiers"))).asRequired(); // hardcoded list
            if(path.endsWith("components.attribute_modifiers.modifiers[0].amount"))
                return PathInfos.DOUBLE.asRequired();
            if(path.endsWith("components.attribute_modifiers.modifiers[0].operation"))
                return (new PathInfo(PathType.STRING,new String[]{"add_value","add_multiplied_base","add_multiplied_total"})).withDesc(
                Text.of("add_value: base + amount1 + amount2\n\nadd_multiplied_base: base * (1 + amount1 + amount2)\n\n"
                +"add_multiplied_total: base * (1 + amount1) * (1 + amount2)")).asRequired(); // hardcoded list
            if(path.endsWith("components.attribute_modifiers.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.banner_patterns")) {
            if(path.endsWith("components.banner_patterns"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.WHITE_BANNER);
            if(path.endsWith("components.banner_patterns[0]"))
                return (new PathInfo(PathType.BANNER));
            //     return (new PathInfo(List.of("color","pattern"))).withFlag(PathFlag.BANNER);
            // if(path.endsWith("components.banner_patterns[0].color"))
            //     return (new PathInfo(PathType.STRING, DYES)).asRequired();
            // if(path.endsWith("components.banner_patterns[0].pattern"))
            //     return (new PathInfo(PathType.STRING,BANNER_PATTERNS)).asRequired();
        }

        if(path.endsWith("components.base_color"))
            return (new PathInfo(PathType.STRING,LIST_DYE_COLOR.get().toArray(new String[0]))).withDesc(Text.of("Used for the banner color of a shield")).withIcon(Items.SHIELD);

        if(path.contains("components.bees")) {
            if(path.endsWith("components.bees"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.BEE_NEST);
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
                return (new PathInfo(List.of("id"))).withIcon(Items.SPAWNER);
            if(path.endsWith("components.block_entity_data.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_BLOCK_ENTITY_TYPE.get().toArray(new String[0]))).asRequired();
        }

        if(path.contains("components.block_state")) {
            if(path.endsWith("components.block_state")) {
                if(!path.equals("components.block_state"))
                    return PathInfos.INLINE_COMPOUND.withIcon(Items.PALE_OAK_STAIRS);
                if(ItemBuilder.getStatesArr() != null)
                    return (new PathInfo(List.of(ItemBuilder.getStatesArr()))).asDynamic().withIcon(Items.PALE_OAK_STAIRS);
                return PathInfos.INLINE_COMPOUND.asDynamic().withIcon(Items.PALE_OAK_STAIRS);
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
                return (new PathInfo(List.of("NoAI","Silent","NoGravity","Glowing","Invulnerable","Health","Age","Variant","HuntingCooldown","BucketVariantTag"))).withIcon(Items.TROPICAL_FISH_BUCKET);
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
                return PathInfos.LIST_COMPOUND.withIcon(Items.BUNDLE);
            if(path.endsWith("components.bundle_contents[0]"))
                return PathInfos.ITEM_NODE;
        }

        if(path.contains("components.can_break")) {
            if(path.endsWith("components.can_break"))
                return (new PathInfo(List.of("predicates","show_in_tooltip"))).withIcon(Items.DIAMOND_PICKAXE);
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
                return (new PathInfo(List.of("predicates","show_in_tooltip"))).withIcon(Items.TORCH);
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
                return PathInfos.LIST_COMPOUND.withIcon(Items.CROSSBOW);
            if(path.endsWith("components.charged_projectiles[0]"))
                return PathInfos.ITEM_NODE;
        }

        if(path.contains("components.consumable")) {
            if(path.endsWith("components.consumable"))
                return (new PathInfo(List.of("animation","consume_seconds","has_consume_particles","on_consume_effects","sound"))).withIcon(Items.APPLE);
            if(path.endsWith("components.consumable.animation"))
                return (new PathInfo(PathType.STRING,LIST_USE_ACTION.get().toArray(new String[0]))).withDesc(Text.of("Defaults to \"eat\""));
            if(path.endsWith("components.consumable.consume_seconds"))
                return (new PathInfo(PathType.FLOAT,new String[]{"1.6f"})).withDesc(Text.of("Defaults to 1.6f")); // hardcoded list
            if(path.endsWith("components.consumable.has_consume_particles"))
                return PathInfos.TRINARY.withDesc(Text.of("Defaults to true"));
            if(path.endsWith("components.consumable.on_consume_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.consumable.on_consume_effects[0]"))
                return (new PathInfo(List.of("type","effects","probability","diameter","sound")));
            if(path.endsWith("components.consumable.on_consume_effects[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.consumable.on_consume_effects[0].effects"))
                return PathInfos.DEFAULT.withDesc(Text.of("Used for \"apply_effects\" or \"remove_effects\"")); // to_do
            if(path.endsWith("components.consumable.on_consume_effects[0].probability"))
                return PathInfos.FLOAT.withDesc(Text.of("Used for \"apply_effects\""));
            if(path.endsWith("components.consumable.on_consume_effects[0].diameter"))
                return PathInfos.FLOAT.withDesc(Text.of("Used for \"teleport_randomly\" (Defaults to 16f)"));
            if(path.endsWith("components.consumable.on_consume_effects[0].sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Used for \"play_sound\""));
            if(path.endsWith("components.consumable.sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Defaults to \"entity.generic.eat\""));
        }

        if(path.contains("components.container")) {
            if(path.endsWith("components.container"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.CHEST);
            if(path.endsWith("components.container[0]"))
                return (new PathInfo(List.of("item","slot")));
            if(path.endsWith("components.container[0].item"))
                return PathInfos.ITEM_NODE.asRequired();
            if(path.endsWith("components.container[0].slot"))
                return PathInfos.INT.asRequired();
        }

        if(path.contains("components.container_loot")) {
            if(path.endsWith("components.container_loot"))
                return (new PathInfo(List.of("loot_table","seed"))).withIcon(Items.CHEST);
            if(path.endsWith("components.container_loot.loot_table"))
                return (new PathInfo(PathType.STRING,DATA_LOOT_TABLE.get().toArray(new String[0]))).asRequired().asDynamic();
            if(path.endsWith("components.container_loot.seed"))
                return PathInfos.LONG;
        }

        if(path.endsWith("components.custom_data"))
            return PathInfos.INLINE_COMPOUND.withDesc(Text.of("Contains unstructured NBT unused ingame")).withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.custom_model_data")) {
            if(path.endsWith("components.custom_model_data"))
                return (new PathInfo(List.of("floats","flags","strings","colors"))).withIcon(Items.COMMAND_BLOCK);
            if(path.endsWith("components.custom_model_data.floats"))
                return PathInfos.LIST_FLOAT;
            if(path.endsWith("components.custom_model_data.flags"))
                return PathInfos.BYTE_ARRAY.withDesc(Text.of("Array of booleans"));
            if(path.endsWith("components.custom_model_data.strings"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.custom_model_data.colors"))
                return (new PathInfo(PathType.DEFAULT,new String[]{"[I;]","[[1.0,1.0,1.0]]"})).withDesc(Text.of("Can be either:\na) NbtIntArray with separate decimal colors\nb) NbtList of NbtLists, where each child list contains 3 numbers for RGB"));
        }

        if(path.endsWith("components.custom_name"))
            return PathInfos.TEXT.withDesc(Text.of("This is for renamed items and will appear in italics. See item_name to completely override the vanilla name.")).withIcon(Items.NAME_TAG);

        if(path.endsWith("components.damage"))
            return (new PathInfo(PathType.INT,new String[]{"0"})).withIcon(Items.DIAMOND_PICKAXE);

        if(path.contains("components.damage_resistant")) {
            if(path.endsWith("components.damage_resistant"))
                return (new PathInfo(List.of("types"))).withIcon(Items.NETHERITE_INGOT);
            if(path.endsWith("components.damage_resistant.types"))
                return (new PathInfo(PathType.STRING)).asRequired();// to_do add suggs
        }

        if(path.contains("components.death_protection")) {
            if(path.endsWith("components.death_protection"))
                return (new PathInfo(List.of("death_effects"))).withIcon(Items.TOTEM_OF_UNDYING);
            if(path.endsWith("components.death_protection.death_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.death_protection.death_effects[0]"))
                return (new PathInfo(List.of("type","effects","probability","diameter","sound")));
            if(path.endsWith("components.death_protection.death_effects[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.death_protection.death_effects[0].effects"))
                return PathInfos.DEFAULT.withDesc(Text.of("Used for \"apply_effects\" or \"remove_effects\"")); // to_do
            if(path.endsWith("components.death_protection.death_effects[0].probability"))
                return PathInfos.FLOAT.withDesc(Text.of("Used for \"apply_effects\""));
            if(path.endsWith("components.death_protection.death_effects[0].diameter"))
                return PathInfos.FLOAT.withDesc(Text.of("Used for \"teleport_randomly\" (Defaults to 16f)"));
            if(path.endsWith("components.death_protection.death_effects[0].sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Used for \"play_sound\""));
        }

        if(path.endsWith("components.debug_stick_state"))
            return PathInfos.DEFAULT.withIcon(Items.DEBUG_STICK);

        if(path.contains("components.dyed_color")) {
            if(path.endsWith("components.dyed_color"))
                return (new PathInfo(List.of("rgb","show_in_tooltip"))).withIcon(Items.LEATHER_CHESTPLATE);
            if(path.endsWith("components.dyed_color.rgb"))
                return PathInfos.DECIMAL_COLOR.asRequired();
            if(path.endsWith("components.dyed_color.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.enchantable")) {
            if(path.endsWith("components.enchantable"))
                return (new PathInfo(List.of("value"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.enchantable.value"))
                return (new PathInfo(PathType.INT,new String[]{"1",""+Integer.MAX_VALUE})).asRequired();
        }

        if(path.endsWith("components.enchantment_glint_override"))
            return PathInfos.TRINARY.withIcon(Items.ENCHANTED_BOOK);

        if(path.contains("components.enchantments")) {
            if(path.endsWith("components.enchantments"))
                return (new PathInfo(List.of("levels","show_in_tooltip"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.enchantments.levels"))
                return (new PathInfo(DATA_ENCHANTMENT.get())).asRequired().asDynamic();
            if(path.endsWith("components.enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("enchantments.levels.")) {
            int maxLvl = 1;
            for(String e : DATA_ENCHANTMENT.get()) {
                if(path.endsWith("enchantments.levels."+e.replace("minecraft:",""))) {
                    maxLvl = getEnchantmentMaxLevel(e);
                }
            }
            return (new PathInfo(PathType.INT,BlackMagick.getIntRangeArray(1,maxLvl))).withDesc(Text.of("Max level: "+maxLvl)).asDynamic();
        }

        if(path.contains(".entity_data")) {
            if(path.endsWith(".entity_data"))
                return (new PathInfo(List.of("id",
                "CustomName","CustomNameVisible","Glowing","HasVisualFire","Invulnerable","Motion","NoGravity","Pos","Rotation","Silent","Tags",
                "active_effects","ArmorDropChances","ArmorItems","attributes","CanPickUpLoot","FallFlying","Health","HandDropChances","HandItems","leash","LeftHanded","NoAI","PersistenceRequired","Team",
                "DisabledSlots","Invisible","Marker","NoBasePlate","Pose","ShowArms","Small",
                "Fixed","Invisible","Item","ItemDropChance","ItemRotation",
                "beam_target","ShowBottom",
                "SoundEvent",
                "Duration","DurationOnUse","potion_contents","Particle","Radius","RadiusOnUse","RadiusPerTick","ReapplicationDelay","WaitTime",
                "variant"))).withIcon(Items.BREEZE_SPAWN_EGG);
            if(path.endsWith(".entity_data.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_ENTITY_TYPE.get().toArray(new String[0]))).asRequired();
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
                return (new PathInfo(List.of("ambient","amplifier","duration","hidden_effect","id","show_icon","show_particles"))).withGroup(lbl).withFlag(PathFlag.EFFECT);
            if(path.endsWith(".entity_data.active_effects[0].ambient") || path.endsWith(".hidden_effect.ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.active_effects[0].amplifier") || path.endsWith(".hidden_effect.amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith(".entity_data.active_effects[0].duration") || path.endsWith(".hidden_effect.duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith(".entity_data.active_effects[0].id") || path.endsWith(".hidden_effect.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT.get().toArray(new String[0])));
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
            if(path.endsWith(".entity_data.attributes"))
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
                return (new PathInfo(PathType.DEFAULT,new String[]{"{UUID:[I;0,0,0,0]}","{UUID:"+BlackMagick.nbtToString(FortytwoEdit.UUID)+"}","[I;0,0,0]"})).withDesc(Text.of("Can be either:\na) NbtCompound like {UUID:[I;0,0,0,0]} pointing to an entity UUID\nb) NbtIntArray containing [I; X, Y, Z]")).withGroup(lbl);
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
                return (new PathInfo(PathType.POSE)).withGroup(lbl);
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
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Arrows types and tridents will play this sound when hitting something")).withGroup(lbl);

            lbl = "Area Effect Clouds";
            if(path.endsWith(".entity_data.Duration"))
                return PathInfos.INT.withDesc(Text.of("Max age after WaitTime")).withGroup(lbl);
            if(path.endsWith(".entity_data.DurationOnUse"))
                return PathInfos.INT.withGroup(lbl);
            if(path.endsWith(".entity_data.potion_contents"))
                return PathInfos.POTION_CONTENTS.withGroup(lbl);
            if(path.endsWith(".entity_data.Particle"))
                return (new PathInfo(PathType.INLINE_COMPOUND,BlackMagick.formatSuggs(REGISTRY_PARTICLE_TYPE.get().toArray(new String[0]),"{type:\"","\"}"))).withDesc(Text.of("Format like {type:\"dust\",color:[.5d,0d,1d],scale:2}")).withGroup(lbl);
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

            lbl = "Paintings";
            if(path.endsWith(".entity_data.variant"))
                return (new PathInfo(PathType.DEFAULT,BlackMagick.joinCommandSuggs(new String[][]{BlackMagick.formatStringSuggs(DATA_PAINTING_VARIANT.get().toArray(new String[0]))},
                    new String[]{"{asset_id:\"\",width:1,height:1}"}))).withGroup(lbl).withDesc(Text.of("Can be either:\na) NbtString of a painting ID\nb) NbtCompound with {asset_id:\"<variant>\",width:<int>,height:<int>}")).asDynamic();

            // when adding new paths, also add keys to entity_data compound
            // if a key is already used for another entity, move it to common category
        }

        if(path.contains("components.equippable")) {
            if(path.endsWith("components.equippable"))
                return (new PathInfo(List.of("slot","equip_sound","asset_id","allowed_entities","dispensable","swappable","damage_on_hurt","camera_overlay"))).withIcon(Items.DIAMOND_CHESTPLATE);
            if(path.endsWith("components.equippable.slot"))
                return (new PathInfo(PathType.STRING,LIST_EQUIPMENT_SLOT.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.equippable.equip_sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Defaults to \"item.armor.equip_generic\""));
            if(path.endsWith("components.equippable.asset_id"))
                return (new PathInfo(PathType.STRING,ASSETS_EQUIPMENT.get().toArray(new String[0]))).withDesc(Text.of("An equipment model at \"assets/<namespace>/equipment/<id>\""));
            if(path.endsWith("components.equippable.allowed_entities"))
                return (new PathInfo(PathType.DEFAULT,BlackMagick.joinCommandSuggs(new String[][]{
                    BlackMagick.formatStringSuggs(BlackMagick.joinCommandSuggs(new String[][]{REGISTRY_ENTITY_TYPE.get().toArray(new String[0]),DATA_TAG_ENTITY_TYPE.get().toArray(new String[0])},null))},
                    new String[]{"[\"skeleton\",\"zombie\"]"})))
                    .withDesc(Text.of("Can be either:\na) NbtString of an entity ID or entity tag\nb) NbtList of entity ID NbtStrings")).asDynamic();
            if(path.endsWith("components.equippable.dispensable"))
                return PathInfos.TRINARY.withDesc(Text.of("Defaults to true"));
            if(path.endsWith("components.equippable.swappable"))
                return PathInfos.TRINARY.withDesc(Text.of("Defaults to true"));
            if(path.endsWith("components.equippable.damage_on_hurt"))
                return PathInfos.TRINARY.withDesc(Text.of("Defaults to true"));
            if(path.endsWith("components.equippable.camera_overlay"))
                return (new PathInfo(PathType.STRING,ASSETS_TEXTURES.get().toArray(new String[0]))).withDesc(Text.of("A texture at \"assets/<namespace>/textures/<id>\""));
        }

        if(path.contains("components.firework_explosion")) {
            if(path.endsWith("components.firework_explosion"))
                return (new PathInfo(List.of("shape","colors","fade_colors","has_trail","has_twinkle"))).withFlag(PathFlag.FIREWORK).withIcon(Items.FIREWORK_STAR);
            if(path.endsWith("components.firework_explosion.shape"))
                return (new PathInfo(PathType.STRING,LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE.get().toArray(new String[0]))).asRequired();
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
                return (new PathInfo(List.of("explosions","flight_duration"))).withIcon(Items.FIREWORK_ROCKET);
            if(path.endsWith("components.fireworks.explosions"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.fireworks.explosions[0]"))
                return (new PathInfo(List.of("shape","colors","fade_colors","has_trail","has_twinkle"))).withFlag(PathFlag.FIREWORK);
            if(path.endsWith("components.fireworks.explosions[0].shape"))
                return (new PathInfo(PathType.STRING,LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.fireworks.explosions[0].colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].fade_colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].has_trail"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.explosions[0].has_twinkle"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.flight_duration"))
                return (new PathInfo(PathType.BYTE,new String[]{"1","2","3"})).withDesc(Text.of("Value between 0-255 represented as a signed byte"));
        }

        if(path.contains("components.food")) {
            if(path.endsWith("components.food"))
                return (new PathInfo(List.of("nutrition","saturation","is_meat","can_always_eat"))).withIcon(Items.APPLE);
            if(path.endsWith("components.food.nutrition"))
                return PathInfos.INT.withDesc(Text.of("How many food points to restore (1 nutrition for each half of a food icon)")).asRequired();
            if(path.endsWith("components.food.saturation"))
                return PathInfos.FLOAT.asRequired();
            if(path.endsWith("components.food.is_meat"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.food.can_always_eat"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.glider"))
            return PathInfos.UNIT.withIcon(Items.ELYTRA);

        if(path.endsWith("components.hide_additional_tooltip"))
            return PathInfos.UNIT.withIcon(Items.COMMAND_BLOCK);

        if(path.endsWith("components.hide_tooltip"))
            return PathInfos.UNIT.withIcon(Items.COMMAND_BLOCK);

        if(path.endsWith("components.instrument"))
            return (new PathInfo(PathType.STRING,DATA_INSTRUMENT.get().toArray(new String[0]))).withIcon(Items.GOAT_HORN).asDynamic();

        if(path.endsWith("components.intangible_projectile"))
            return PathInfos.UNIT.withIcon(Items.ARROW);

        if(path.endsWith("components.item_model"))
            return (new PathInfo(PathType.STRING,ASSETS_ITEMS.get().toArray(new String[0]))).withDesc(Text.of("An item model definition at \"assets/<namespace>/items/<id>\"")).withIcon(Items.STONE);

        if(path.endsWith("components.item_name"))
            return PathInfos.TEXT.withIcon(Items.STONE);

        if(path.contains("components.jukebox_playable")) {
            if(path.endsWith("components.jukebox_playable"))
                return (new PathInfo(List.of("song","show_in_tooltip"))).withIcon(Items.MUSIC_DISC_MELLOHI);
            if(path.endsWith("components.jukebox_playable.song"))
                return (new PathInfo(PathType.STRING,DATA_JUKEBOX_SONG.get().toArray(new String[0]))).asRequired().asDynamic();
            if(path.endsWith("components.jukebox_playable.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.lock")) {
            if(path.endsWith("components.lock"))
                return (new PathInfo(List.of("components","count","items","predicates"))).withIcon(Items.CHEST);
            if(path.endsWith("components.lock.components"))
                return (new PathInfo(List.of(LIST_DATA_COMPONENT_TYPE.get().toArray(new String[0])))).withDesc(Text.of("Exact components to match"));
            if(path.endsWith("components.lock.count"))
                return (new PathInfo(PathType.DEFAULT,new String[]{"1","{min:1,max:2}"})).withDesc(Text.of("Can be either:\na) NbtInt of exact count\nb) NbtCompound containing min, max, or both to test a range"));
            if(path.endsWith("components.lock.items"))
                return PathInfos.getItemPredicateItems();
            if(path.endsWith("components.lock.predicates"))
                return PathInfos.INLINE_COMPOUND.withDesc(Text.of("Item subpredicates to match")); // to_do all subpredicate paths
        }

        if(path.contains("components.lodestone_tracker")) {
            if(path.endsWith("components.lodestone_tracker"))
                return (new PathInfo(List.of("target","tracked"))).withIcon(Items.COMPASS);
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
                return PathInfos.LIST_STRING.withIcon(Items.COMMAND_BLOCK);
            if(path.endsWith("components.lore[0]"))
                return PathInfos.TEXT;
        }

        if(path.endsWith("components.map_color"))
            return PathInfos.DECIMAL_COLOR.withIcon(Items.MAP);

        if(path.endsWith("components.map_decorations"))
            return PathInfos.DEFAULT.withIcon(Items.MAP);

        if(path.endsWith("components.map_id"))
            return PathInfos.INT.withIcon(Items.MAP);

        if(path.endsWith("components.max_damage"))
            return (new PathInfo(PathType.INT,new String[]{""+ToolMaterial.WOOD.durability(),""+ToolMaterial.STONE.durability(),
                ""+ToolMaterial.GOLD.durability(),""+ToolMaterial.IRON.durability(),""+ToolMaterial.DIAMOND.durability(),
                ""+ToolMaterial.NETHERITE.durability()})).withDesc(Text.of("Default values for reference:\n  Wood tools - "+ToolMaterial.WOOD.durability()
                +"\n  Stone tools - "+ToolMaterial.STONE.durability()+"\n  Gold tools - "+ToolMaterial.GOLD.durability()
                +"\n  Iron tools - "+ToolMaterial.IRON.durability()+"\n  Diamond tools - "+ToolMaterial.DIAMOND.durability()
                +"\n  Netherite tools - "+ToolMaterial.NETHERITE.durability())).withIcon(Items.DIAMOND_PICKAXE);

        if(path.endsWith("components.max_stack_size"))
            return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

        if(path.endsWith("components.note_block_sound"))
            return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT.get().toArray(new String[0]))).withDesc(Text.of("Used for player heads on a note block")).withIcon(Items.PLAYER_HEAD);

        if(path.endsWith("components.ominous_bottle_amplifier"))
            return (new PathInfo(PathType.INT,new String[]{"0","1","2","3","4"})).withIcon(Items.OMINOUS_BOTTLE);

        if(path.contains("components.pot_decorations")) {
            if(path.endsWith("components.pot_decorations"))
                return PathInfos.LIST_STRING.withIcon(Items.DECORATED_POT);
            if(path.endsWith("components.pot_decorations[0]"))
                return (new PathInfo(PathType.STRING,LIST_DECORATED_POT_PATTERN_ITEMS.get().toArray(new String[0])));
        }

        if(path.contains(".potion_contents")) {
            if(path.endsWith(".potion_contents"))
                return PathInfos.POTION_CONTENTS.withIcon(Items.SPLASH_POTION);
            if(path.endsWith(".potion_contents.potion"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT.get().toArray(new String[0]))).withDesc(Text.of("Potion base before custom_color and custom_effects")); // to_do add potion variants (strong, etc)
            if(path.endsWith(".potion_contents.custom_color"))
                return PathInfos.DECIMAL_COLOR;
            if(path.endsWith(".potion_contents.custom_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith(".potion_contents.custom_effects[0]"))
                return PathInfos.EFFECT_NODE;
            if(path.endsWith(".potion_contents.custom_effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT.get().toArray(new String[0]))).asRequired();
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
            if(path.endsWith(".potion_contents.custom_name"))
                return (new PathInfo(PathType.STRING));// to_do use same list as potion_contents.potion
        }

        if(path.contains("components.profile")) {
            if(path.endsWith("components.profile"))
                return (new PathInfo(List.of("name","id","properties"))).withIcon(Items.PLAYER_HEAD);
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
            return (new PathInfo(PathType.STRING,new String[]{"common","uncommon","rare","epic"})).withDesc(Text.of("Used for item name color:\n  common\n  \u00a7euncommon\n  \u00a7brare\n  \u00a7depic\u00a7r")).withIcon(Items.STONE); // hardcoded list

        if(path.contains("components.recipes")) {
            if(path.endsWith("components.recipes"))
                return PathInfos.LIST_STRING.withIcon(Items.KNOWLEDGE_BOOK);
            if(path.endsWith("components.recipes[0]"))
                return (new PathInfo(PathType.STRING,DATA_RECIPE.get().toArray(new String[0]))).asDynamic();
        }

        if(path.endsWith("components.repair_cost"))
            return (new PathInfo(PathType.INT,new String[]{"0",""+Integer.MAX_VALUE})).withIcon(Items.ENCHANTED_BOOK);

        if(path.contains("components.repairable")) {
            if(path.endsWith("components.repairable"))
                return (new PathInfo(List.of("items"))).withIcon(Items.ANVIL);
            if(path.endsWith("components.repairable.items"))
                return PathInfos.getItemPredicateItems().asRequired();
        }

        if(path.contains("components.stored_enchantments")) {
            if(path.endsWith("components.stored_enchantments"))
                return (new PathInfo(List.of("levels","show_in_tooltip"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.stored_enchantments.levels"))
                return (new PathInfo(DATA_ENCHANTMENT.get())).asRequired().asDynamic();
            if(path.endsWith("components.stored_enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.suspicious_stew_effects")) {
            if(path.endsWith("components.suspicious_stew_effects"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.SUSPICIOUS_STEW);
            if(path.endsWith("components.suspicious_stew_effects[0]"))
                return (new PathInfo(List.of("id","duration"))).withFlag(PathFlag.EFFECT);
            if(path.endsWith("components.suspicious_stew_effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.suspicious_stew_effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
        }

        if(path.contains("components.tool")) {
            if(path.endsWith("components.tool"))
                return (new PathInfo(List.of("default_mining_speed","damage_per_block","rules"))).withIcon(Items.DIAMOND_PICKAXE);
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

        if(path.endsWith("components.tooltip_style"))
            return (new PathInfo(PathType.STRING)).withDesc(Text.of("References tooltip sprites at \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_background\" and \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_frame\"")).withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.trim")) {
            if(path.endsWith("components.trim"))
                return (new PathInfo(List.of("pattern","material","show_in_tooltip"))).withIcon(Items.DIAMOND_CHESTPLATE);
            if(path.endsWith("components.trim.pattern"))
                return (new PathInfo(PathType.STRING,DATA_TRIM_PATTERN.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.trim.material"))
                return (new PathInfo(PathType.STRING,DATA_TRIM_MATERIAL.get().toArray(new String[0]))).asRequired();
            if(path.endsWith("components.trim.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.unbreakable"))
            return PathInfos.TOOLTIP_UNIT.withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.use_cooldown")) {
            if(path.endsWith("components.use_cooldown"))
                return (new PathInfo(List.of("seconds","cooldown_group"))).withIcon(Items.ENDER_PEARL);
            if(path.endsWith("components.use_cooldown.seconds"))
                return (new PathInfo(PathType.FLOAT,new String[]{"1.0f"})).asRequired();
            if(path.endsWith("components.use_cooldown.cooldown_group"))
                return (new PathInfo(PathType.STRING,REGISTRY_ITEM.get().toArray(new String[0]))).withDesc(Text.of("Custom namespaced ID or namespaced item ID"));
        }

        if(path.endsWith("components.use_remainder"))
            return PathInfos.ITEM_NODE.withIcon(Items.MUSHROOM_STEW);

        if(path.contains("components.writable_book_content")) {
            if(path.endsWith("components.writable_book_content"))
                return (new PathInfo(List.of("pages"))).withIcon(Items.WRITABLE_BOOK);
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
                return (new PathInfo(List.of("pages","title","author","generation","resolved"))).withIcon(Items.WRITTEN_BOOK);
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
                return (new PathInfo(PathType.INT,new String[]{"0","1","2","3"})).withDesc(Text.of("0 - Original\n1 - Copy of original\n2 - Copy of copy\n3 - Tattered")); // hardcoded list
            if(path.endsWith("components.written_book_content.resolved"))
                return PathInfos.TRINARY.withDesc(Text.of("Whether or not text component is resolved (for selectors/scores/etc)"));
        }

        if(path.equals("id") || path.endsWith(".id"))
            return (new PathInfo(PathType.STRING,REGISTRY_ITEM.get().toArray(new String[0]))).asRequired().withIcon(Items.STONE);

        if(path.equals("count") || path.endsWith(".count"))
            return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

        if(path.equals("components") || path.endsWith(".components"))
            return (new PathInfo(List.of(LIST_DATA_COMPONENT_TYPE.get().toArray(new String[0]))));

        FortytwoEdit.logWarn("No PathInfo found for path: "+path);
        return PathInfos.UNKNOWN;
    }

    /**
     * <p> PathType type - to setup widget </p>
     * <p> String[] suggs - for textbox suggestions </p>
     * <p> Text description - displays in tooltip </p>
     * <p> List<String> keys - keys that can be included in compound (only for PathType.COMPOUND) </p>
     * <p> byte listType - NbtElement.getType() type of list (only for PathType.LIST) </p>
     */
    public record PathInfo(PathType type, String[] suggs, Text description, List<String> keys, byte listType, String keyGroup, boolean dynamic, PathFlag flag, ItemStack icon) {

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
            this(type,suggs,null,null,DEFAULT_LIST_TYPE,DEFAULT_GROUP,false,PathFlag.NONE,null);
        }

        /**
         * Create a PathInfo for a compound.
         * 
         * @param keys all keys that may be in the compound
         */
        public PathInfo(List<String> keys) {
            this(PathType.COMPOUND,null,null,keys,DEFAULT_LIST_TYPE,DEFAULT_GROUP,false,PathFlag.NONE,null);
        }

        /**
         * Create a PathInfo for a list.
         * 
         * @param listType the NbtElement.getType() that should be in the list
         */
        public PathInfo(byte listType) {
            this(PathType.LIST,null,null,null,listType,DEFAULT_GROUP,false,PathFlag.NONE,null);
        }

        // modifiers

        /**
         * Add a description to the PathInfo.
         * 
         * @param desc
         * @return a copy with the description added
         */
        public PathInfo withDesc(Text desc) {
            return new PathInfo(this.type, this.suggs, desc, this.keys, this.listType, this.keyGroup, this.dynamic, this.flag, this.icon);
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
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, group, this.dynamic, this.flag, this.icon);
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
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, this.keyGroup, true, this.flag, this.icon);
        }

        /**
         * Add a predefined flag
         * 
         * @return
         */
        public PathInfo withFlag(PathFlag flag) {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, this.keyGroup, this.dynamic, flag, this.icon);
        }

        /**
         * Add an ItemStack icon to view next to key name
         * 
         * @return
         */
        public PathInfo withIcon(ItemStack icon) {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, this.keyGroup, this.dynamic, this.flag, icon);
        }

        /**
         * Add an Item icon to view next to key name
         * 
         * @return
         */
        public PathInfo withIcon(Item icon) {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, this.keyGroup, this.dynamic, this.flag, new ItemStack(icon));
        }

    }

    /**
     * Describes the expected value of a path using either NbtElement types
     * or some custom types.
     */
    public enum PathType {

        // edit inline textbox

        BYTE,
        SHORT,
        INT,
        LONG,
        DOUBLE,
        FLOAT,
        STRING,
        BYTE_ARRAY,
        INT_ARRAY,
        LONG_ARRAY,

        UNKNOWN,            // return when no PathInfo is found (treated as any stringified nbt)
        DEFAULT,            // can be used for any stringified nbt
        UUID,
        INLINE_LIST,        // a list that is edited in a text field (such as Motion/Pos/Rotation)
        INLINE_COMPOUND,    // a compound edited in a text field

        // edit with custom row

        UNIT,               // represents nbt that is either absent or {}
        TRINARY,            // represents nbt that is either absent, 0b, or 1b
        TOOLTIP_UNIT,       // represents nbt that is either absent, {}, or {show_in_tooltip:0b}

        // edit complex

        COMPOUND,
        LIST,

        TEXT,               // use for text components
        DECIMAL_COLOR,      // use for integer color fields
        BANNER,
        POSE,

    }

    /**
     * Miscellaneous flags to denote a path as something specific
     */
    public enum PathFlag {

        NONE, // default, says nothing

        // used for ItemBuilder.getButtonText()
        ATTRIBUTE,
        EFFECT,
        PROBABILITY_EFFECT,
        FIREWORK

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
                case BANNER: return true;
                case POSE: return true;
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
                case BANNER: return NbtElement.COMPOUND_TYPE;
                case POSE: return NbtElement.COMPOUND_TYPE;
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
        private static final PathInfo BYTE_ARRAY = (new PathInfo(PathType.BYTE_ARRAY,new String[]{"[B;]"}));
        private static final PathInfo LIST_COMPOUND = (new PathInfo(NbtElement.COMPOUND_TYPE));
        private static final PathInfo LIST_FLOAT = (new PathInfo(NbtElement.FLOAT_TYPE));
        private static final PathInfo LIST_STRING = (new PathInfo(NbtElement.STRING_TYPE));
        private static final PathInfo INLINE_COMPOUND = (new PathInfo(PathType.INLINE_COMPOUND,new String[]{"{}"}));

        private static final PathInfo TEXT = (new PathInfo(PathType.TEXT,new String[]{"'{\"text\":\"\"}'"})).withDesc(Text.of("Text component"));
        private static final PathInfo DECIMAL_COLOR = (new PathInfo(PathType.DECIMAL_COLOR,new String[]{"0","16777215"})).withDesc(Text.of("0xRRGGBB hex color converted to integer"));
        private static final PathInfo UUID = (new PathInfo(PathType.UUID,new String[]{"[I;0,0,0,0]"}));
        private static final PathInfo INT_ARRAY_POS = (new PathInfo(PathType.INT_ARRAY,new String[]{"[I;0,0,0]"})).withDesc(Text.of("[I; X, Y, Z] block coordinates"));

        private static final PathInfo ITEM_NODE = (new PathInfo(List.of("id","count","components")));
        private static final PathInfo ITEM_COUNT = (new PathInfo(PathType.INT,new String[]{"1","16","64","99"}));

        private static final PathInfo POTION_CONTENTS = (new PathInfo(List.of("potion","custom_color","custom_effects","custom_name")));
        private static final PathInfo EFFECT_NODE = (new PathInfo(List.of("id","amplifier","duration","ambient","show_particles","show_icon"))).withFlag(PathFlag.EFFECT);
        private static final PathInfo EFFECT_DURATION = (new PathInfo(PathType.INT,new String[]{"-1","1"})).withDesc(Text.of("Duration in ticks or -1 for infinity"));
        private static final PathInfo EFFECT_AMPLIFIER = (new PathInfo(PathType.BYTE,new String[]{"0","255"})).withDesc(Text.of("Amplifier 0-255 gives effect level 1-256"));

        private static PathInfo getBlockPredicateBlocks() {
            return (new PathInfo(PathType.DEFAULT,BlackMagick.joinCommandSuggs(new String[][]{
                BlackMagick.formatStringSuggs(BlackMagick.joinCommandSuggs(new String[][]{REGISTRY_BLOCK.get().toArray(new String[0]),DATA_TAG_BLOCK.get().toArray(new String[0])},null))},
                new String[]{"[\"dirt\",\"stone\"]"})))
                .withDesc(Text.of("Can be either:\na) NbtString of a block ID or block tag\nb) NbtList of block ID NbtStrings")).asDynamic();
        }

        private static PathInfo getItemPredicateItems() {
            return (new PathInfo(PathType.DEFAULT,BlackMagick.joinCommandSuggs(new String[][]{
                BlackMagick.formatStringSuggs(BlackMagick.joinCommandSuggs(new String[][]{REGISTRY_ITEM.get().toArray(new String[0]),DATA_TAG_ITEM.get().toArray(new String[0])},null))},
                new String[]{"[\"diamond\",\"gold_ingot\"]"})))
                .withDesc(Text.of("Can be either:\na) NbtString of an item ID or item tag\nb) NbtList of item ID NbtStrings")).asDynamic();
        }

    }


    private static final Map<String,List<String>> STATIC_LIST_CACHES = Maps.newHashMap();
    private static final Map<String,List<String>> DYNAMIC_LIST_CACHES = Maps.newHashMap();
    private static final Map<String,Supplier<List<String>>> SUGGS_LIST_METHODS = Maps.newHashMap();

    public static void clearDynamicListCaches() {
        DYNAMIC_LIST_CACHES.clear();
    }

    public static void runAllListMethods() {
        clearCacheInfo();
        int successCount = 0;
        int totalCount = 0;
        for(String list : SUGGS_LIST_METHODS.keySet()) {
            if(SUGGS_LIST_METHODS.get(list).get().isEmpty())
                FortytwoEdit.logWarn("Unable to fetch suggestions list: "+list);
            else
                successCount++;
            totalCount++;
        }
        FortytwoEdit.logInfo("Loaded "+successCount+"/"+totalCount+" suggestions lists");
    }

    protected static List<String> getStaticList(String name) {
        if(STATIC_LIST_CACHES.containsKey(name))
            return STATIC_LIST_CACHES.get(name);
        List<String> list = Lists.newArrayList();
        STATIC_LIST_CACHES.put(name,list);
        return list;
    }
    protected static List<String> getDynamicList(String name) {
        if(DYNAMIC_LIST_CACHES.containsKey(name))
            return DYNAMIC_LIST_CACHES.get(name);
        List<String> list = Lists.newArrayList();
        DYNAMIC_LIST_CACHES.put(name,list);
        return list;
    }

    protected static Supplier<List<String>> registerSuggsList(String list, Supplier<List<String>> method) {
        SUGGS_LIST_METHODS.put(list, method);
        return method;
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
        for(Map.Entry<Property<?>, Comparable<?>> entry : blockState.getEntries().entrySet()) {
            ArrayList<String> list = new ArrayList<>();
            list.add(entry.getKey().getName());
            for(Comparable<?> val : entry.getKey().getValues()) {
                list.add((String)Util.getValueAsString(entry.getKey(), val));
            }
            states.add(list);
        }
        return states;
    }

    /**
     * 
     * @param key an enchantment ID
     * @return the max level for the enchantment, or 1 if unknown
     */
    public static int getEnchantmentMaxLevel(String key) {
        int max = 1;
        try {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null) {
                Enchantment ench = client.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT).get().get(Identifier.of(key));
                if(ench != null)
                    max = ench.getMaxLevel();
            }
        } catch(Exception ex) {}
        return max;
    }

    /**
     * 
     * @param item
     * @return int array with [rows,columns] or [-1,-1] depending on storage size of blockentity for item
     */
    public static int[] getContainerSize(Item item) {

        // find items by searching net.minecraft.item.Items for DataComponentTypes.CONTAINER
        // manually enter rows/cols based on ingame gui appearance
        // remove ender chest

        String id = Identifier.of(item.toString()).toString();

        if(id.startsWith("minecraft:") && id.endsWith("shulker_box"))
            return new int[]{3,9};

        switch(id) {
            case "minecraft:chest":
            case "minecraft:trapped_chest":
            case "minecraft:barrel":
                return new int[]{3,9};
            case "minecraft:dispenser":
            case "minecraft:dropper":
            case "minecraft:crafter":
                return new int[]{3,3};
            case "minecraft:hopper":
            case "minecraft:brewing_stand":
                return new int[]{1,5};
            case "minecraft:furnace":
            case "minecraft:blast_furnace":
            case "minecraft:smoker":
                return new int[]{1,3};
            case "minecraft:chiseled_bookshelf":
                return new int[]{2,3};
            case "minecraft:campfire":
            case "minecraft:soul_campfire":
                return new int[]{1,4};
            case "minecraft:decorated_pot":
                return new int[]{1,1};
            default:
                return new int[]{-1,-1};
        }
    }


    // static hardcoded lists

    public static final Supplier<List<String>> LIST_ATTRIBUTE_MODIFIER_SLOT = registerSuggsList("LIST_ATTRIBUTE_MODIFIER_SLOT", () -> {
        List<String> list = getStaticList("LIST_ATTRIBUTE_MODIFIER_SLOT");
        if(list.isEmpty()) {
            for(AttributeModifierSlot i : AttributeModifierSlot.values())
                list.add(i.asString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_DATA_COMPONENT_TYPE = registerSuggsList("LIST_DATA_COMPONENT_TYPE", () -> {
        List<String> list = getStaticList("LIST_DATA_COMPONENT_TYPE");
        if(list.isEmpty()) {
            Registries.DATA_COMPONENT_TYPE.forEach(i -> {
                if(!i.shouldSkipSerialization())
                    list.add(Registries.DATA_COMPONENT_TYPE.getId(i).toString());
            });
            Collections.sort(list);
        }
        return list;
    });

    /**
     * Contains pottery sherd items and brick item
     */
    public static final Supplier<List<String>> LIST_DECORATED_POT_PATTERN_ITEMS = registerSuggsList("LIST_DECORATED_POT_PATTERN_ITEMS", () -> {
        List<String> list = getStaticList("LIST_DECORATED_POT_PATTERN_ITEMS");
        if(list.isEmpty()) {
            for(Item i : DecoratedPotPatternsAccessor.getSherdToPattern().keySet())
                list.add(i.toString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_DYE_COLOR = registerSuggsList("LIST_DYE_COLOR", () -> {
        List<String> list = getStaticList("LIST_DYE_COLOR");
        if(list.isEmpty()) {
            for(DyeColor i : DyeColor.values())
                list.add(i.asString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_EQUIPMENT_SLOT = registerSuggsList("LIST_EQUIPMENT_SLOT", () -> {
        List<String> list = getStaticList("LIST_EQUIPMENT_SLOT");
        if(list.isEmpty()) {
            for(EquipmentSlot i : EquipmentSlot.values())
                list.add(i.asString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE = registerSuggsList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE", () -> {
        List<String> list = getStaticList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE");
        if(list.isEmpty()) {
            for(FireworkExplosionComponent.Type i : FireworkExplosionComponent.Type.values())
                list.add(i.asString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_FORMATTING_COLOR = registerSuggsList("LIST_FORMATTING_COLOR", () -> {
        List<String> list = getStaticList("LIST_FORMATTING_COLOR");
        if(list.isEmpty()) {
            for(String i : Formatting.getNames(true, false))
                if(!i.equals("reset"))
                    list.add(i);
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_KEYBIND = registerSuggsList("LIST_KEYBIND", () -> {
        List<String> list = getStaticList("LIST_KEYBIND");
        if(list.isEmpty()) {
            for(String i : KeyBindingAccessor.getKeysList().keySet())
                if(!i.startsWith("42edit."))
                    list.add(i);
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_USE_ACTION = registerSuggsList("LIST_USE_ACTION", () -> {
        List<String> list = getStaticList("LIST_USE_ACTION");
        if(list.isEmpty()) {
            for(UseAction i : UseAction.values())
                list.add(i.asString());
            Collections.sort(list);
        }
        return list;
    });

    public static final Supplier<List<String>> LIST_TRANSLATION_KEY = registerSuggsList("LIST_TRANSLATION_KEY", () -> {
        List<String> list = getStaticList("LIST_TRANSLATION_KEY");
        if(list.isEmpty()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            List<String> l = Lists.newArrayList();
            l.add("en_us");
            TranslationStorage s = TranslationStorage.load(client.getResourceManager(),l,false);
            for(String i : ((TranslationStorageAccessor)s).getTranslations().keySet())
                if(!i.startsWith("42edit."))
                    list.add(i);
            Collections.sort(list);
        }
        return list;
    });


    // static registry lists

    private static List<String> getRegistryIfEmpty(List<String> list, Registry<?> registryRef) {
        if(list.isEmpty()) {
            for(Identifier i : registryRef.getIds())
                list.add(i.toString());
            Collections.sort(list);
        }
        return list;
    }

    public static final Supplier<List<String>> REGISTRY_ATTRIBUTE = registerSuggsList("REGISTRY_ATTRIBUTE", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_ATTRIBUTE"),Registries.ATTRIBUTE));

    public static final Supplier<List<String>> REGISTRY_BLOCK = registerSuggsList("REGISTRY_BLOCK", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_BLOCK"),Registries.BLOCK));

    public static final Supplier<List<String>> REGISTRY_BLOCK_ENTITY_TYPE = registerSuggsList("REGISTRY_BLOCK_ENTITY_TYPE", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_BLOCK_ENTITY_TYPE"),Registries.BLOCK_ENTITY_TYPE));

    public static final Supplier<List<String>> REGISTRY_CONSUME_EFFECT_TYPE = registerSuggsList("REGISTRY_CONSUME_EFFECT_TYPE", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_CONSUME_EFFECT_TYPE"),Registries.CONSUME_EFFECT_TYPE));

    public static final Supplier<List<String>> REGISTRY_ITEM = registerSuggsList("REGISTRY_ITEM", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_ITEM"),Registries.ITEM));

    public static final Supplier<List<String>> REGISTRY_SOUND_EVENT = registerSuggsList("REGISTRY_SOUND_EVENT", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_SOUND_EVENT"),Registries.SOUND_EVENT));

    public static final Supplier<List<String>> REGISTRY_STATUS_EFFECT = registerSuggsList("REGISTRY_STATUS_EFFECT", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_STATUS_EFFECT"),Registries.STATUS_EFFECT));

    public static final Supplier<List<String>> REGISTRY_ENTITY_TYPE = registerSuggsList("REGISTRY_ENTITY_TYPE", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_ENTITY_TYPE"),Registries.ENTITY_TYPE));

    public static final Supplier<List<String>> REGISTRY_PARTICLE_TYPE = registerSuggsList("REGISTRY_PARTICLE_TYPE", () ->
        getRegistryIfEmpty(getStaticList("REGISTRY_PARTICLE_TYPE"),Registries.PARTICLE_TYPE));


    // dynamic data lists

    private static List<String> getDataIfEmpty(List<String> list, RegistryKey<? extends Registry<?>> registryRef) {
        if(list.isEmpty()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null)
                client.world.getRegistryManager().getOptional(registryRef).ifPresent(reg -> {
                    for(Identifier i : reg.getIds())
                        list.add(i.toString());
                });
            Collections.sort(list);
        }
        return list;
    }

    public static final Supplier<List<String>> DATA_BANNER_PATTERN = registerSuggsList("DATA_BANNER_PATTERN", () ->
        getDataIfEmpty(getDynamicList("DATA_BANNER_PATTERN"),RegistryKeys.BANNER_PATTERN));

    public static final Supplier<List<String>> DATA_DAMAGE_TYPE = registerSuggsList("DATA_DAMAGE_TYPE", () ->
        getDataIfEmpty(getDynamicList("DATA_DAMAGE_TYPE"),RegistryKeys.DAMAGE_TYPE));

    public static final Supplier<List<String>> DATA_ENCHANTMENT = registerSuggsList("DATA_ENCHANTMENT", () ->
        getDataIfEmpty(getDynamicList("DATA_ENCHANTMENT"),RegistryKeys.ENCHANTMENT));

    public static final Supplier<List<String>> DATA_INSTRUMENT = registerSuggsList("DATA_INSTRUMENT", () ->
        getDataIfEmpty(getDynamicList("DATA_INSTRUMENT"),RegistryKeys.INSTRUMENT));

    public static final Supplier<List<String>> DATA_JUKEBOX_SONG = registerSuggsList("DATA_JUKEBOX_SONG", () ->
        getDataIfEmpty(getDynamicList("DATA_JUKEBOX_SONG"),RegistryKeys.JUKEBOX_SONG));

    public static final Supplier<List<String>> DATA_PAINTING_VARIANT = registerSuggsList("DATA_PAINTING_VARIANT", () ->
        getDataIfEmpty(getDynamicList("DATA_PAINTING_VARIANT"),RegistryKeys.PAINTING_VARIANT));

    public static final Supplier<List<String>> DATA_TRIM_MATERIAL = registerSuggsList("DATA_TRIM_MATERIAL", () ->
        getDataIfEmpty(getDynamicList("DATA_TRIM_MATERIAL"),RegistryKeys.TRIM_MATERIAL));

    public static final Supplier<List<String>> DATA_TRIM_PATTERN = registerSuggsList("DATA_TRIM_PATTERN", () ->
        getDataIfEmpty(getDynamicList("DATA_TRIM_PATTERN"),RegistryKeys.TRIM_PATTERN));

    public static final Supplier<List<String>> DATA_WOLF_VARIANT = registerSuggsList("DATA_WOLF_VARIANT", () ->
        getDataIfEmpty(getDynamicList("DATA_WOLF_VARIANT"),RegistryKeys.WOLF_VARIANT));


    // dynamic data tags lists

    private static List<String> getTagsIfEmpty(List<String> list, RegistryKey<? extends Registry<?>> registryRef) {
        if(list.isEmpty()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if(client.world != null)
                client.world.getRegistryManager().getOptional(registryRef).ifPresent(reg -> {
                    reg.streamTagKeys().forEach(tag -> {
                        list.add("#"+tag.id().toString());
                    });
                });
            Collections.sort(list);
        }
        return list;
    }

    public static final Supplier<List<String>> DATA_TAG_BANNER_PATTERN = registerSuggsList("DATA_TAG_BANNER_PATTERN", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_BANNER_PATTERN"),RegistryKeys.BANNER_PATTERN));

    public static final Supplier<List<String>> DATA_TAG_BLOCK = registerSuggsList("DATA_TAG_BLOCK", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_BLOCK"),RegistryKeys.BLOCK));

    public static final Supplier<List<String>> DATA_TAG_DAMAGE_TYPE = registerSuggsList("DATA_TAG_DAMAGE_TYPE", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_DAMAGE_TYPE"),RegistryKeys.DAMAGE_TYPE));

    public static final Supplier<List<String>> DATA_TAG_ENCHANTMENT = registerSuggsList("DATA_TAG_ENCHANTMENT", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_ENCHANTMENT"),RegistryKeys.ENCHANTMENT));

    public static final Supplier<List<String>> DATA_TAG_ENTITY_TYPE = registerSuggsList("DATA_TAG_ENTITY_TYPE", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_ENTITY_TYPE"),RegistryKeys.ENTITY_TYPE));

    public static final Supplier<List<String>> DATA_TAG_ITEM = registerSuggsList("DATA_TAG_ITEM", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_ITEM"),RegistryKeys.ITEM));

    public static final Supplier<List<String>> DATA_TAG_PAINTING_VARIANT = registerSuggsList("DATA_TAG_PAINTING_VARIANT", () ->
        getTagsIfEmpty(getDynamicList("DATA_TAG_PAINTING_VARIANT"),RegistryKeys.PAINTING_VARIANT));


    // dynamic data lists from vanilla

    private static List<String> getVanillaDataIfEmpty(List<String> list, String path, String suffix) {
        if(list.isEmpty()) {
            try {
                HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
                final String namespace = "minecraft";
                VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.SERVER_DATA, namespace, path, map::putIfAbsent);
                map.keySet().forEach(i -> {
                    String temp = i.toString();
                    if(temp.startsWith(namespace+":"+path+"/") && temp.endsWith(suffix) && temp.length()>(namespace.length()+1+path.length()+1+suffix.length())) {
                        temp = namespace+":"+temp.substring(namespace.length()+1+path.length()+1,temp.length()-suffix.length());
                        if(path.startsWith("tags/"))
                            temp = "#"+temp;
                        list.add(temp);
                    }
                    else {
                        FortytwoEdit.logWarn("Failed to add data path to list: "+i.toString());
                    }
                });
            } catch(Exception ex) {}
            Collections.sort(list);
        }
        return list;
    }

    // private static List<String> setCommandSuggs(List<String> list, String cmd) {
    //     if(list.isEmpty()) {
    //         final MinecraftClient client = MinecraftClient.getInstance();
    //         CommandDispatcher<CommandSource> commandDispatcher = client.player.networkHandler.getCommandDispatcher();
    //         ParseResults<CommandSource> cmdSuggsParse = commandDispatcher.parse(cmd, (CommandSource)client.player.networkHandler.getCommandSource());
    //         CompletableFuture<Suggestions> cmdSuggsPendingSuggestions = commandDispatcher.getCompletionSuggestions(cmdSuggsParse, cmd.length());
    //         cmdSuggsPendingSuggestions.thenRun(() -> {
    //             Suggestions suggestions;
    //             if(cmdSuggsPendingSuggestions.isDone() && !(suggestions = cmdSuggsPendingSuggestions.join()).isEmpty()) {
    //                 list.clear();
    //                 for (Suggestion suggestion : suggestions.getList())
    //                     list.add(suggestion.getText());
    //                 Collections.sort(list);
    //             }
    //         });
    //     }
    //     return list;
    // }
    //
    // `loot give @s loot `
    // `recipe give @s ` (remove `*`)
    // `place template `
    // dimension

    protected final static String JSON_SUFFIX = ".json";
    protected final static String NBT_SUFFIX = ".nbt";
    protected final static String PNG_SUFFIX = ".png";
    protected final static String MCMETA_SUFFIX = ".mcmeta";

    public static final Supplier<List<String>> DATA_LOOT_TABLE = registerSuggsList("DATA_LOOT_TABLE", () ->
        getVanillaDataIfEmpty(getDynamicList("DATA_LOOT_TABLE"),"loot_table",JSON_SUFFIX));

    public static final Supplier<List<String>> DATA_RECIPE = registerSuggsList("DATA_RECIPE", () ->
        getVanillaDataIfEmpty(getDynamicList("DATA_RECIPE"),"recipe",JSON_SUFFIX));

    public static final Supplier<List<String>> DATA_STRUCTURE = registerSuggsList("DATA_STRUCTURE", () ->
        getVanillaDataIfEmpty(getDynamicList("DATA_STRUCTURE"),"structure",NBT_SUFFIX));

    public static final Supplier<List<String>> DATA_TRIAL_SPAWNER = registerSuggsList("DATA_TRIAL_SPAWNER", () ->
        getVanillaDataIfEmpty(getDynamicList("DATA_TRIAL_SPAWNER"),"trial_spawner",JSON_SUFFIX));


    // dynamic assets lists from vanilla

    private static List<String> getVanillaAssetsIfEmpty(List<String> list, String path, String suffix) {
        if(list.isEmpty()) {
            try {
                HashMap<Identifier, InputSupplier<InputStream>> map = new HashMap<Identifier, InputSupplier<InputStream>>();
                final String namespace = "minecraft";
                VanillaDataPackProvider.createDefaultPack().findResources(ResourceType.CLIENT_RESOURCES, namespace, path, map::putIfAbsent);
                map.keySet().forEach(i -> {
                    String temp = i.toString();
                    if(!temp.endsWith(suffix+MCMETA_SUFFIX)) {
                        if(temp.startsWith(namespace+":"+path+"/") && temp.endsWith(suffix) && temp.length()>(namespace.length()+1+path.length()+1+suffix.length())) {
                            temp = namespace+":"+temp.substring(namespace.length()+1+path.length()+1,temp.length()-suffix.length());
                            list.add(temp);
                        }
                        else {
                            FortytwoEdit.logWarn("Failed to add assets path to list: "+i.toString());
                        }
                    }
                });
            } catch(Exception ex) {}
            Collections.sort(list);
        }
        return list;
    }

    public static final Supplier<List<String>> ASSETS_EQUIPMENT = registerSuggsList("ASSETS_EQUIPMENT", () ->
        getVanillaAssetsIfEmpty(getDynamicList("ASSETS_EQUIPMENT"),"equipment",JSON_SUFFIX));

    public static final Supplier<List<String>> ASSETS_FONT = registerSuggsList("ASSETS_FONT", () ->
        getVanillaAssetsIfEmpty(getDynamicList("ASSETS_FONT"),"font",JSON_SUFFIX));

    public static final Supplier<List<String>> ASSETS_ITEMS = registerSuggsList("ASSETS_ITEMS", () ->
        getVanillaAssetsIfEmpty(getDynamicList("ASSETS_ITEMS"),"items",JSON_SUFFIX));

    public static final Supplier<List<String>> ASSETS_TEXTURES = registerSuggsList("ASSETS_TEXTURES", () ->
        getVanillaAssetsIfEmpty(getDynamicList("ASSETS_TEXTURES"),"textures",PNG_SUFFIX));


}
