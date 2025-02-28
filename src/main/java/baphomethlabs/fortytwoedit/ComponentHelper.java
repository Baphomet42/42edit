package baphomethlabs.fortytwoedit;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.gui.screen.ItemBuilder;
import baphomethlabs.fortytwoedit.mixin.DecoratedPotPatternsAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyMappingAccessor;
import baphomethlabs.fortytwoedit.mixin.ClientLanguageAccessor;

/**
 * <p> Class containing static methods related to item components </p>
 * <p> These often change every update and must be kept up to date manually </p>
 * <p> Check the following for help: </p>
 * <ul>
 *  <li> {@link net.minecraft.core.component.DataComponents} </li>
 *  <li> https://minecraft.wiki/w/Item_format </li>
 *  <li> https://minecraft.wiki/w/Entity_format </li>
 * </ul>
 */
public class ComponentHelper {

    private static final Map<String,PathInfo> pathInfoCaches = Maps.newHashMap();

    public static void clearCacheInfo() {
        pathInfoCaches.clear();
        for(String key : LIST_CACHES.keySet())
            LIST_CACHES.get(key).clear();
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
                return (new PathInfo(KeyGetter.create().withRequired("modifiers").withOptional("show_in_tooltip"))).withIcon(Items.DIAMOND_SWORD);
            if(path.endsWith("components.attribute_modifiers.modifiers"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.attribute_modifiers.modifiers[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("type","id","amount","operation").withOptional("slot"))).withFlag(PathFlag.ATTRIBUTE);
            if(path.endsWith("components.attribute_modifiers.modifiers[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_ATTRIBUTE));
            if(path.endsWith("components.attribute_modifiers.modifiers[0].slot"))
                return (new PathInfo(PathType.STRING,LIST_ATTRIBUTE_MODIFIER_SLOT));
            if(path.endsWith("components.attribute_modifiers.modifiers[0].id"))
                return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("minecraft:armor.body","minecraft:armor.boots","minecraft:armor.chestplate","minecraft:armor.helmet","minecraft:armor.leggings",Item.BASE_ATTACK_DAMAGE_ID.toString(),Item.BASE_ATTACK_SPEED_ID.toString())).withDesc(Component.nullToEmpty("Unique namespaced ID used to update modifiers"))); // hardcoded list
            if(path.endsWith("components.attribute_modifiers.modifiers[0].amount"))
                return PathInfos.DOUBLE;
            if(path.endsWith("components.attribute_modifiers.modifiers[0].operation"))
                return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("add_value","add_multiplied_base","add_multiplied_total"))).withDesc(
                Component.nullToEmpty("add_value: base + amount1 + amount2\n\nadd_multiplied_base: base * (1 + amount1 + amount2)\n\n"
                +"add_multiplied_total: base * (1 + amount1) * (1 + amount2)")); // hardcoded list
            if(path.endsWith("components.attribute_modifiers.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.banner_patterns")) {
            if(path.endsWith("components.banner_patterns"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.WHITE_BANNER);
            if(path.endsWith("components.banner_patterns[0]"))
                return (new PathInfo(PathType.BANNER));
            //     return (new PathInfo(KeyGetter.create().withRequired("color","pattern"))).withFlag(PathFlag.BANNER);
            // if(path.endsWith("components.banner_patterns[0].color"))
            //     return (new PathInfo(PathType.STRING, DYES));
            // if(path.endsWith("components.banner_patterns[0].pattern"))
            //     return (new PathInfo(PathType.STRING,BANNER_PATTERNS));
        }

        if(path.endsWith("components.base_color"))
            return (new PathInfo(PathType.STRING,LIST_DYE_COLOR)).withDesc(Component.nullToEmpty("Used for the banner color of a shield")).withIcon(Items.SHIELD);

        if(path.contains("components.bees")) {
            if(path.endsWith("components.bees"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.BEE_NEST);
            if(path.endsWith("components.bees[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("min_ticks_in_hive","ticks_in_hive").withOptional("entity_data")));
            // if(path.endsWith("components.bees[0].entity_data"))
            //     see .entity_data
            if(path.endsWith("components.bees[0].min_ticks_in_hive"))
                return PathInfos.INT;
            if(path.endsWith("components.bees[0].ticks_in_hive"))
                return PathInfos.INT;
        }

        if(path.contains("components.block_entity_data")) {
            if(path.endsWith("components.block_entity_data"))
                return (new PathInfo(KeyGetter.create().withRequired("id"))).withIcon(Items.SPAWNER);
            if(path.endsWith("components.block_entity_data.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_BLOCK_ENTITY_TYPE));
        }

        if(path.contains("components.block_state")) {
            if(path.endsWith("components.block_state")) {
                if(!path.equals("components.block_state"))
                    return PathInfos.INLINE_COMPOUND.withIcon(Items.PALE_OAK_STAIRS);
                if(ItemBuilder.getStatesArr() != null)
                    return (new PathInfo(KeyGetter.create().withOptional(ItemBuilder.getStatesArr()))).asDynamic().withIcon(Items.PALE_OAK_STAIRS);
                return PathInfos.INLINE_COMPOUND.asDynamic().withIcon(Items.PALE_OAK_STAIRS);
            }
            if(path.contains("components.block_state.")) {
                if(ItemBuilder.getStatesArr() != null) {
                    for(String s : ItemBuilder.getStatesArr()) {
                        if(path.endsWith("components.block_state."+s)) {
                            String[] states = ItemBuilder.getStateVals(s);
                            if(states != null)
                                return (new PathInfo(PathType.STRING,SuggestionGetter.newInline(states))).asDynamic();
                        }
                    }
                }
                return PathInfos.STRING.asDynamic();
            }
        }

        if(path.contains("components.bucket_entity_data")) {
            if(path.endsWith("components.bucket_entity_data"))
                return (new PathInfo(KeyGetter.create().withOptional("NoAI","Silent","NoGravity","Glowing","Invulnerable","Health","Age","Variant","HuntingCooldown","BucketVariantTag"))).withIcon(Items.TROPICAL_FISH_BUCKET);
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
                return PathInfos.INT.withDesc(Component.nullToEmpty("Age of tadpole or axolotl")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.Variant"))
                return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0","1","2","3","4"))).withDesc(Component.nullToEmpty("Axolotl variant id\n0 - lucy (pink)\n1 - wild (brown)\n2 - gold\n3 - cyan\n4 - blue")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.HuntingCooldown"))
                return PathInfos.LONG.withDesc(Component.nullToEmpty("Axolotl hunting cooldown")).withGroup("Item Specific");
            if(path.endsWith("components.bucket_entity_data.BucketVariantTag"))
                return PathInfos.INT.withDesc(Component.nullToEmpty("Tropical fish variant")).withGroup("Item Specific");
        }

        if(path.contains("components.bundle_contents")) {
            if(path.endsWith("components.bundle_contents"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.BUNDLE);
            if(path.endsWith("components.bundle_contents[0]"))
                return PathInfos.ITEM_NODE;
        }

        if(path.contains("components.can_break")) {
            if(path.endsWith("components.can_break"))
                return (new PathInfo(KeyGetter.create().withOptional("predicates","show_in_tooltip"))).withIcon(Items.DIAMOND_PICKAXE);
            if(path.endsWith("components.can_break.predicates"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.can_break.predicates[0]"))
                return (new PathInfo(KeyGetter.create().withOptional("blocks","nbt","state")));
            if(path.endsWith("components.can_break.predicates[0].blocks"))
                return PathInfos.BLOCK_PREDICATE_BLOCKS;
            if(path.endsWith("components.can_break.predicates[0].nbt"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_break.predicates[0].state"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.can_break.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.can_place_on")) {
            if(path.endsWith("components.can_place_on"))
                return (new PathInfo(KeyGetter.create().withOptional("predicates","show_in_tooltip"))).withIcon(Items.TORCH);
            if(path.endsWith("components.can_place_on.predicates"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.can_place_on.predicates[0]"))
                return (new PathInfo(KeyGetter.create().withOptional("blocks","nbt","state")));
            if(path.endsWith("components.can_place_on.predicates[0].blocks"))
                return PathInfos.BLOCK_PREDICATE_BLOCKS;
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
                return (new PathInfo(KeyGetter.create().withOptional("animation","consume_seconds","has_consume_particles","on_consume_effects","sound"))).withIcon(Items.APPLE);
            if(path.endsWith("components.consumable.animation"))
                return (new PathInfo(PathType.STRING,LIST_USE_ACTION)).withDesc(Component.nullToEmpty("Defaults to \"eat\""));
            if(path.endsWith("components.consumable.consume_seconds"))
                return (new PathInfo(PathType.FLOAT,SuggestionGetter.newInline("1.6f"))).withDesc(Component.nullToEmpty("Defaults to 1.6f")); // hardcoded list
            if(path.endsWith("components.consumable.has_consume_particles"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Defaults to true"));
            if(path.endsWith("components.consumable.on_consume_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.consumable.on_consume_effects[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("type").withOptional("effects","probability","diameter","sound")));
            if(path.endsWith("components.consumable.on_consume_effects[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE));
            if(path.endsWith("components.consumable.on_consume_effects[0].effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0]"))
                return PathInfos.EFFECT_NODE.withDesc(Component.nullToEmpty("Used for \"apply_effects\" or \"remove_effects\""));
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].show_icon"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.consumable.on_consume_effects[0].probability"))
                return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"apply_effects\""));
            if(path.endsWith("components.consumable.on_consume_effects[0].diameter"))
                return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"teleport_randomly\" (Defaults to 16f)"));
            if(path.endsWith("components.consumable.on_consume_effects[0].sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for \"play_sound\""));
            if(path.endsWith("components.consumable.sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Defaults to \"entity.generic.eat\""));
        }

        if(path.contains("components.container")) {
            if(path.endsWith("components.container"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.CHEST);
            if(path.endsWith("components.container[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("item","slot")));
            if(path.endsWith("components.container[0].item"))
                return PathInfos.ITEM_NODE;
            if(path.endsWith("components.container[0].slot"))
                return PathInfos.INT;
        }

        if(path.contains("components.container_loot")) {
            if(path.endsWith("components.container_loot"))
                return (new PathInfo(KeyGetter.create().withRequired("loot_table").withOptional("seed"))).withIcon(Items.CHEST);
            if(path.endsWith("components.container_loot.loot_table"))
                return (new PathInfo(PathType.STRING,DATA_LOOT_TABLE));
            if(path.endsWith("components.container_loot.seed"))
                return PathInfos.LONG;
        }

        if(path.endsWith("components.custom_data"))
            return PathInfos.INLINE_COMPOUND.withDesc(Component.nullToEmpty("Contains unstructured NBT unused ingame")).withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.custom_model_data")) {
            if(path.endsWith("components.custom_model_data"))
                return (new PathInfo(KeyGetter.create().withOptional("floats","flags","strings","colors"))).withIcon(Items.COMMAND_BLOCK);
            if(path.endsWith("components.custom_model_data.floats"))
                return PathInfos.LIST_FLOAT;
            if(path.endsWith("components.custom_model_data.flags"))
                return PathInfos.BYTE_ARRAY.withDesc(Component.nullToEmpty("Array of booleans"));
            if(path.endsWith("components.custom_model_data.strings"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.custom_model_data.colors"))
                return (new PathInfo(PathType.DEFAULT,SuggestionGetter.newInline("[I;]","[[1.0,1.0,1.0]]"))).withDesc(Component.nullToEmpty("Can be either:\na) NbtIntArray with separate decimal colors\nb) NbtList of NbtLists, where each child list contains 3 numbers for RGB"));
        }

        if(path.endsWith("components.custom_name"))
            return PathInfos.TEXT.withDesc(Component.nullToEmpty("This is for renamed items and will appear in italics. See item_name to completely override the vanilla name.")).withIcon(Items.NAME_TAG);

        if(path.endsWith("components.damage"))
            return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0"))).withIcon(Items.DIAMOND_PICKAXE);

        if(path.contains("components.damage_resistant")) {
            if(path.endsWith("components.damage_resistant"))
                return (new PathInfo(KeyGetter.create().withRequired("types"))).withIcon(Items.NETHERITE_INGOT);
            if(path.endsWith("components.damage_resistant.types"))
                return (new PathInfo(PathType.STRING,DATA_TAG_DAMAGE_TYPE)).withDesc(Component.nullToEmpty("Damage type tag of which the item entity is invulnerable"));
        }

        if(path.contains("components.death_protection")) {
            if(path.endsWith("components.death_protection"))
                return (new PathInfo(KeyGetter.create().withOptional("death_effects"))).withIcon(Items.TOTEM_OF_UNDYING);
            if(path.endsWith("components.death_protection.death_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.death_protection.death_effects[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("type").withOptional("effects","probability","diameter","sound")));
            if(path.endsWith("components.death_protection.death_effects[0].type"))
                return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE));
            if(path.endsWith("components.death_protection.death_effects[0].effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.death_protection.death_effects[0].effects[0]"))
                return PathInfos.EFFECT_NODE.withDesc(Component.nullToEmpty("Used for \"apply_effects\" or \"remove_effects\""));
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.death_protection.death_effects[0].effects[0].show_icon"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.death_protection.death_effects[0].probability"))
                return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"apply_effects\""));
            if(path.endsWith("components.death_protection.death_effects[0].diameter"))
                return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"teleport_randomly\" (Defaults to 16f)"));
            if(path.endsWith("components.death_protection.death_effects[0].sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for \"play_sound\""));
        }

        if(path.endsWith("components.debug_stick_state"))
            return PathInfos.DEFAULT.withIcon(Items.DEBUG_STICK);

        if(path.contains("components.dyed_color")) {
            if(path.endsWith("components.dyed_color"))
                return (new PathInfo(KeyGetter.create().withRequired("rgb").withOptional("show_in_tooltip"))).withIcon(Items.LEATHER_CHESTPLATE);
            if(path.endsWith("components.dyed_color.rgb"))
                return PathInfos.DECIMAL_COLOR;
            if(path.endsWith("components.dyed_color.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.enchantable")) {
            if(path.endsWith("components.enchantable"))
                return (new PathInfo(KeyGetter.create().withRequired("value"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.enchantable.value"))
                return (new PathInfo(PathType.INT,SuggestionGetter.newInline("1",""+Integer.MAX_VALUE)));
        }

        if(path.endsWith("components.enchantment_glint_override"))
            return PathInfos.TRINARY.withIcon(Items.ENCHANTED_BOOK);

        if(path.contains("components.enchantments")) {
            if(path.endsWith("components.enchantments"))
                return (new PathInfo(KeyGetter.create().withRequired("levels").withOptional("show_in_tooltip"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.enchantments.levels"))
                return (new PathInfo(KeyGetter.create().withOptional(DATA_ENCHANTMENT)));
            if(path.endsWith("components.enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("enchantments.levels.")) {
            int maxLvl = 1;
            for(String e : DATA_ENCHANTMENT.getList()) {
                if(path.endsWith("enchantments.levels."+e.replace("minecraft:",""))) {
                    maxLvl = getEnchantmentMaxLevel(e);
                }
            }
            return (new PathInfo(PathType.INT,SuggestionGetter.newInline(BlackMagick.getIntRangeArray(1,maxLvl)))).withDesc(Component.nullToEmpty("Max level: "+maxLvl)).asDynamic();
        }

        if(path.contains(".entity_data")) {
            if(path.endsWith(".entity_data"))
                return (new PathInfo(KeyGetter.create().withRequired("id").withOptional(
                "CustomName","CustomNameVisible","Glowing","HasVisualFire","Invulnerable","Motion","NoGravity","Pos","Rotation","Silent","Tags",
                "active_effects","ArmorDropChances","ArmorItems","attributes","CanPickUpLoot","FallFlying","Health","HandDropChances","HandItems","leash","LeftHanded","NoAI","PersistenceRequired","Team",
                "DisabledSlots","Invisible","Marker","NoBasePlate","Pose","ShowArms","Small",
                "Fixed","Invisible","Item","ItemDropChance","ItemRotation",
                "beam_target","ShowBottom",
                "SoundEvent",
                "Duration","DurationOnUse","potion_contents","Particle","Radius","RadiusOnUse","RadiusPerTick","ReapplicationDelay","WaitTime",
                "variant"))).withIcon(Items.CREEPER_SPAWN_EGG);
            if(path.endsWith(".entity_data.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_ENTITY_TYPE));
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
                return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0d,0d,0d]"))).withDesc(Component.nullToEmpty("[x, y, z] motion in each direction\nx - east\ny - up\nz - south"));
            if(path.endsWith(".entity_data.NoGravity"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.OnGround"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.Passengers"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith(".entity_data.PortalCooldown"))
                return PathInfos.INT;
            if(path.endsWith(".entity_data.Pos"))
                return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0d,0d,0d]"))).withDesc(Component.nullToEmpty("[x, y, z]"));
            if(path.endsWith(".entity_data.Rotation"))
                return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0f,0f]"))).withDesc(Component.nullToEmpty("[Yaw, Pitch]\nYaw: -180 to 180 (0 is south, 90 is west)\nPitch: -90 (up) to 90 (down)"));
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
                return (new PathInfo(KeyGetter.create().withOptional("ambient","amplifier","duration","hidden_effect","id","show_icon","show_particles"))).withGroup(lbl).withFlag(PathFlag.EFFECT);
            if(path.endsWith(".entity_data.active_effects[0].ambient") || path.endsWith(".hidden_effect.ambient"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.active_effects[0].amplifier") || path.endsWith(".hidden_effect.amplifier"))
                return PathInfos.EFFECT_AMPLIFIER;
            if(path.endsWith(".entity_data.active_effects[0].duration") || path.endsWith(".hidden_effect.duration"))
                return PathInfos.EFFECT_DURATION;
            if(path.endsWith(".entity_data.active_effects[0].id") || path.endsWith(".hidden_effect.id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
            if(path.endsWith(".entity_data.active_effects[0].show_icon") || path.endsWith(".hidden_effect.show_icon"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.active_effects[0].show_particles") || path.endsWith(".hidden_effect.show_particles"))
                return PathInfos.TRINARY;
            if(path.endsWith(".entity_data.ArmorDropChances"))
                return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0f,0f,0f,0f]","[1f,1f,1f,1f]"))).withDesc(Component.nullToEmpty("[feet, legs, chest, head]")).withGroup(lbl);
            if(path.endsWith(".entity_data.ArmorItems"))
                return PathInfos.LIST_COMPOUND.withDesc(Component.nullToEmpty("[feet, legs, chest, head]")).withGroup(lbl);
            if(path.endsWith(".entity_data.ArmorItems[0]"))
                return PathInfos.ITEM_NODE;
            if(path.endsWith(".entity_data.attributes"))
                return PathInfos.DEFAULT.withGroup(lbl);
            if(path.endsWith(".entity_data.CanPickUpLoot"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.FallFlying"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("If true, mob will glide if wearing an elytra")).withGroup(lbl);
            if(path.endsWith(".entity_data.Health"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.HandDropChances"))
                return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0f,0f]","[1f,1f]"))).withDesc(Component.nullToEmpty("[mainhand, offhand]")).withGroup(lbl);
            if(path.endsWith(".entity_data.HandItems"))
                return PathInfos.LIST_COMPOUND.withDesc(Component.nullToEmpty("[mainhand, offhand]")).withGroup(lbl);
            if(path.endsWith(".entity_data.HandItems[0]"))
                return PathInfos.ITEM_NODE;
            if(path.endsWith(".entity_data.leash"))
                return (new PathInfo(PathType.DEFAULT,SuggestionGetter.newInline("{UUID:[I;0,0,0,0]}","{UUID:"+BlackMagick.nbtToString(FortytwoEdit.UUID)+"}","[I;0,0,0]"))).withDesc(Component.nullToEmpty("Can be either:\na) NbtCompound like {UUID:[I;0,0,0,0]} pointing to an entity UUID\nb) NbtIntArray containing [I; X, Y, Z]")).withGroup(lbl);
            if(path.endsWith(".entity_data.LeftHanded"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.NoAI"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.PersistenceRequired"))
                return PathInfos.TRINARY.withGroup(lbl);
            if(path.endsWith(".entity_data.Team"))
                return PathInfos.STRING.withDesc(Component.nullToEmpty("Name of team for the mob to join when spawning")).withGroup(lbl);

            lbl = "Common";
            if(path.endsWith(".entity_data.Invisible"))
                return PathInfos.TRINARY.withGroup(lbl).withDesc(Component.nullToEmpty("Used by armor stands and item frames"));

            lbl = "Armor Stands";
            if(path.endsWith(".entity_data.DisabledSlots"))
                return (new PathInfo(PathType.INT,SuggestionGetter.newInline("16191"))).withDesc(Component.nullToEmpty("Value of 16191 prevents adding, changing, or removing armor or hand items")).withGroup(lbl);
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
                return (new PathInfo(PathType.BYTE,SuggestionGetter.newInline("0","1","2","3","4","5","6","7"))).withDesc(Component.nullToEmpty("Number of times the item is rotated clockwise")).withGroup(lbl);

            lbl = "End Crystals";
            if(path.endsWith(".entity_data.beam_target"))
                return PathInfos.INT_ARRAY_POS.withGroup(lbl);
            if(path.endsWith(".entity_data.ShowBottom"))
                return PathInfos.TRINARY.withGroup(lbl);

            lbl = "Arrows and Tridents";
            if(path.endsWith(".entity_data.SoundEvent"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Arrows types and tridents will play this sound when hitting something")).withGroup(lbl);

            lbl = "Area Effect Clouds";
            if(path.endsWith(".entity_data.Duration"))
                return PathInfos.INT.withDesc(Component.nullToEmpty("Max age after WaitTime")).withGroup(lbl);
            if(path.endsWith(".entity_data.DurationOnUse"))
                return PathInfos.INT.withGroup(lbl);
            if(path.endsWith(".entity_data.potion_contents"))
                return PathInfos.POTION_CONTENTS.withGroup(lbl);
            if(path.endsWith(".entity_data.Particle"))
                return (new PathInfo(PathType.INLINE_COMPOUND,REGISTRY_PARTICLE_TYPE.withFormat(SuggestionGetter.Format.AEC_PARTICLE_TYPE))).withDesc(Component.nullToEmpty("Format like {type:\"dust\",color:[.5d,0d,1d],scale:2}")).withGroup(lbl);
            if(path.endsWith(".entity_data.Radius"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.RadiusOnUse"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.RadiusPerTick"))
                return PathInfos.FLOAT.withGroup(lbl);
            if(path.endsWith(".entity_data.ReapplicationDelay"))
                return PathInfos.INT.withGroup(lbl);
            if(path.endsWith(".entity_data.WaitTime"))
                return PathInfos.INT.withDesc(Component.nullToEmpty("Time before cloud can have a radius and effect (particles will still appear in the center)")).withGroup(lbl);

            lbl = "Paintings";
            if(path.endsWith(".entity_data.variant"))
                return (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(DATA_PAINTING_VARIANT.withFormat(SuggestionGetter.Format.NBT_STRING),SuggestionGetter.newInline("{asset_id:\"\",width:1,height:1}"))))
                    .withGroup(lbl).withDesc(Component.nullToEmpty("Can be either:\na) NbtString of a painting ID\nb) NbtCompound with {asset_id:\"<variant>\",width:<int>,height:<int>}"));

            // when adding new paths, also add keys to entity_data compound
            // if a key is already used for another entity, move it to common category
        }

        if(path.contains("components.equippable")) {
            if(path.endsWith("components.equippable"))
                return (new PathInfo(KeyGetter.create().withRequired("slot").withOptional("equip_sound","asset_id","allowed_entities","dispensable","swappable","damage_on_hurt","camera_overlay"))).withIcon(Items.DIAMOND_CHESTPLATE);
            if(path.endsWith("components.equippable.slot"))
                return (new PathInfo(PathType.STRING,LIST_EQUIPMENT_SLOT));
            if(path.endsWith("components.equippable.equip_sound"))
                return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Defaults to \"item.armor.equip_generic\""));
            if(path.endsWith("components.equippable.asset_id"))
                return (new PathInfo(PathType.STRING,ASSETS_EQUIPMENT)).withDesc(Component.nullToEmpty("An equipment model at \"assets/<namespace>/equipment/<id>\""));
            if(path.endsWith("components.equippable.allowed_entities"))
                return PathInfos.ENTITY_PREDICATE_ENTITIES;
            if(path.endsWith("components.equippable.dispensable"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Defaults to true"));
            if(path.endsWith("components.equippable.swappable"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Defaults to true"));
            if(path.endsWith("components.equippable.damage_on_hurt"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Defaults to true"));
            if(path.endsWith("components.equippable.camera_overlay"))
                return (new PathInfo(PathType.STRING,ASSETS_TEXTURES)).withDesc(Component.nullToEmpty("A texture at \"assets/<namespace>/textures/<id>\""));
        }

        if(path.contains("components.firework_explosion")) {
            if(path.endsWith("components.firework_explosion"))
                return (new PathInfo(KeyGetter.create().withRequired("shape").withOptional("colors","fade_colors","has_trail","has_twinkle"))).withFlag(PathFlag.FIREWORK).withIcon(Items.FIREWORK_STAR);
            if(path.endsWith("components.firework_explosion.shape"))
                return (new PathInfo(PathType.STRING,LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE));
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
                return (new PathInfo(KeyGetter.create().withOptional("explosions","flight_duration"))).withIcon(Items.FIREWORK_ROCKET);
            if(path.endsWith("components.fireworks.explosions"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.fireworks.explosions[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("shape").withOptional("colors","fade_colors","has_trail","has_twinkle"))).withFlag(PathFlag.FIREWORK);
            if(path.endsWith("components.fireworks.explosions[0].shape"))
                return (new PathInfo(PathType.STRING,LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE));
            if(path.endsWith("components.fireworks.explosions[0].colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].fade_colors"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.fireworks.explosions[0].has_trail"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.explosions[0].has_twinkle"))
                return PathInfos.TRINARY;
            if(path.endsWith("components.fireworks.flight_duration"))
                return (new PathInfo(PathType.BYTE,SuggestionGetter.newInline("1","2","3"))).withDesc(Component.nullToEmpty("Value between 0-255 represented as a signed byte"));
        }

        if(path.contains("components.food")) {
            if(path.endsWith("components.food"))
                return (new PathInfo(KeyGetter.create().withRequired("nutrition","saturation").withOptional("is_meat","can_always_eat"))).withIcon(Items.APPLE);
            if(path.endsWith("components.food.nutrition"))
                return PathInfos.INT.withDesc(Component.nullToEmpty("How many food points to restore (1 nutrition for each half of a food icon)"));
            if(path.endsWith("components.food.saturation"))
                return PathInfos.FLOAT;
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
            return (new PathInfo(PathType.STRING,DATA_INSTRUMENT)).withIcon(Items.GOAT_HORN);

        if(path.endsWith("components.intangible_projectile"))
            return PathInfos.UNIT.withIcon(Items.ARROW);

        if(path.endsWith("components.item_model"))
            return (new PathInfo(PathType.STRING,ASSETS_ITEMS)).withDesc(Component.nullToEmpty("An item model definition at \"assets/<namespace>/items/<id>\"")).withIcon(Items.STONE);

        if(path.endsWith("components.item_name"))
            return PathInfos.TEXT.withIcon(Items.STONE);

        if(path.contains("components.jukebox_playable")) {
            if(path.endsWith("components.jukebox_playable"))
                return (new PathInfo(KeyGetter.create().withRequired("song").withOptional("show_in_tooltip"))).withIcon(Items.MUSIC_DISC_MELLOHI);
            if(path.endsWith("components.jukebox_playable.song"))
                return (new PathInfo(PathType.STRING,DATA_JUKEBOX_SONG));
            if(path.endsWith("components.jukebox_playable.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.lock")) {
            if(path.endsWith("components.lock"))
                return (new PathInfo(KeyGetter.create().withOptional("components","count","items","predicates"))).withIcon(Items.CHEST);
            if(path.endsWith("components.lock.components"))
                return PathInfos.COMPONENTS_NODE.withDesc(Component.nullToEmpty("Exact components to match"));
            if(path.endsWith("components.lock.count"))
                return (new PathInfo(PathType.DEFAULT,SuggestionGetter.newInline("1","{min:1,max:2}"))).withDesc(Component.nullToEmpty("Can be either:\na) NbtInt of exact count\nb) NbtCompound containing min, max, or both to test a range"));
            if(path.endsWith("components.lock.items"))
                return PathInfos.ITEM_PREDICATE_ITEMS;
            if(path.endsWith("components.lock.predicates"))
                return PathInfos.INLINE_COMPOUND.withDesc(Component.nullToEmpty("Item subpredicates to match")); //to_do all subpredicate paths
        }

        if(path.contains("components.lodestone_tracker")) {
            if(path.endsWith("components.lodestone_tracker"))
                return (new PathInfo(KeyGetter.create().withOptional("target","tracked"))).withIcon(Items.COMPASS);
            if(path.endsWith("components.lodestone_tracker.target"))
                return (new PathInfo(KeyGetter.create().withRequired("pos","dimension")));
            if(path.endsWith("components.lodestone_tracker.target.pos"))
                return PathInfos.INT_ARRAY_POS;
            if(path.endsWith("components.lodestone_tracker.target.dimension"))
                return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("overworld","the_nether","the_end")));
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
            return (new PathInfo(PathType.INT,SuggestionGetter.newInline(""+ToolMaterial.WOOD.durability(),""+ToolMaterial.STONE.durability(),
                ""+ToolMaterial.GOLD.durability(),""+ToolMaterial.IRON.durability(),""+ToolMaterial.DIAMOND.durability(),
                ""+ToolMaterial.NETHERITE.durability()))).withDesc(Component.nullToEmpty("Default values for reference:\n  Wood tools - "+ToolMaterial.WOOD.durability()
                +"\n  Stone tools - "+ToolMaterial.STONE.durability()+"\n  Gold tools - "+ToolMaterial.GOLD.durability()
                +"\n  Iron tools - "+ToolMaterial.IRON.durability()+"\n  Diamond tools - "+ToolMaterial.DIAMOND.durability()
                +"\n  Netherite tools - "+ToolMaterial.NETHERITE.durability())).withIcon(Items.DIAMOND_PICKAXE);

        if(path.endsWith("components.max_stack_size"))
            return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

        if(path.endsWith("components.note_block_sound"))
            return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for player heads on a note block")).withIcon(Items.PLAYER_HEAD);

        if(path.endsWith("components.ominous_bottle_amplifier"))
            return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0","1","2","3","4"))).withIcon(Items.OMINOUS_BOTTLE);

        if(path.contains("components.pot_decorations")) {
            if(path.endsWith("components.pot_decorations"))
                return PathInfos.LIST_STRING.withIcon(Items.DECORATED_POT);
            if(path.endsWith("components.pot_decorations[0]"))
                return (new PathInfo(PathType.STRING,LIST_DECORATED_POT_PATTERN_ITEMS));
        }

        if(path.contains(".potion_contents")) {
            if(path.endsWith(".potion_contents"))
                return PathInfos.POTION_CONTENTS.withIcon(Items.SPLASH_POTION);
            if(path.endsWith(".potion_contents.potion"))
                return (new PathInfo(PathType.STRING,REGISTRY_POTION)).withDesc(Component.nullToEmpty("Potion base before custom_color and custom_effects"));
            if(path.endsWith(".potion_contents.custom_color"))
                return PathInfos.DECIMAL_COLOR;
            if(path.endsWith(".potion_contents.custom_effects"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith(".potion_contents.custom_effects[0]"))
                return PathInfos.EFFECT_NODE;
            if(path.endsWith(".potion_contents.custom_effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
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
                return (new PathInfo(PathType.STRING));//to_do set of different strings based on item id translation key plus potion base name, or "empty"
        }

        if(path.contains("components.profile")) {
            if(path.endsWith("components.profile"))
                return (new PathInfo(KeyGetter.create().withOptional("name","id","properties"))).withIcon(Items.PLAYER_HEAD);
            if(path.endsWith("components.profile.name"))
                return PathInfos.STRING.withDesc(Component.nullToEmpty("Player name used to update skin"));
            if(path.endsWith("components.profile.id"))
                return PathInfos.UUID.withDesc(Component.nullToEmpty("Player UUID used to update skin"));
            if(path.endsWith("components.profile.properties"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.profile.properties[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("name","value").withOptional("signature")));
            if(path.endsWith("components.profile.properties[0].name"))
                return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("textures"))).withDesc(Component.nullToEmpty("Currently only used for textures"));
            if(path.endsWith("components.profile.properties[0].value"))
                return PathInfos.STRING;
            if(path.endsWith("components.profile.properties[0].signature"))
                return PathInfos.STRING;
        }

        if(path.endsWith("components.rarity"))
            return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("common","uncommon","rare","epic"))).withDesc(Component.nullToEmpty("Used for item name color:\n  common\n  \u00a7euncommon\n  \u00a7brare\n  \u00a7depic\u00a7r")).withIcon(Items.STONE); // hardcoded list

        if(path.contains("components.recipes")) {
            if(path.endsWith("components.recipes"))
                return PathInfos.LIST_STRING.withIcon(Items.KNOWLEDGE_BOOK);
            if(path.endsWith("components.recipes[0]"))
                return (new PathInfo(PathType.STRING,DATA_RECIPE));
        }

        if(path.endsWith("components.repair_cost"))
            return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0",""+Integer.MAX_VALUE))).withIcon(Items.ENCHANTED_BOOK);

        if(path.contains("components.repairable")) {
            if(path.endsWith("components.repairable"))
                return (new PathInfo(KeyGetter.create().withRequired("items"))).withIcon(Items.ANVIL);
            if(path.endsWith("components.repairable.items"))
                return PathInfos.ITEM_PREDICATE_ITEMS;
        }

        if(path.contains("components.stored_enchantments")) {
            if(path.endsWith("components.stored_enchantments"))
                return (new PathInfo(KeyGetter.create().withRequired("levels").withOptional("show_in_tooltip"))).withIcon(Items.ENCHANTED_BOOK);
            if(path.endsWith("components.stored_enchantments.levels"))
                return (new PathInfo(KeyGetter.create().withOptional(DATA_ENCHANTMENT)));
            if(path.endsWith("components.stored_enchantments.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.contains("components.suspicious_stew_effects")) {
            if(path.endsWith("components.suspicious_stew_effects"))
                return PathInfos.LIST_COMPOUND.withIcon(Items.SUSPICIOUS_STEW);
            if(path.endsWith("components.suspicious_stew_effects[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("id").withOptional("duration"))).withFlag(PathFlag.EFFECT);
            if(path.endsWith("components.suspicious_stew_effects[0].id"))
                return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
            if(path.endsWith("components.suspicious_stew_effects[0].duration"))
                return PathInfos.EFFECT_DURATION;
        }

        if(path.contains("components.tool")) {
            if(path.endsWith("components.tool"))
                return (new PathInfo(KeyGetter.create().withRequired("rules").withOptional("default_mining_speed","damage_per_block"))).withIcon(Items.DIAMOND_PICKAXE);
            if(path.endsWith("components.tool.default_mining_speed"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.tool.damage_per_block"))
                return PathInfos.INT;
            if(path.endsWith("components.tool.rules"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.tool.rules[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("blocks").withOptional("speed","correct_for_drops")));
            if(path.endsWith("components.tool.rules[0].blocks"))
                return PathInfos.LIST_STRING;
            if(path.endsWith("components.tool.rules[0].blocks[0]"))
                return PathInfos.BLOCK_PREDICATE_BLOCKS;
            if(path.endsWith("components.tool.rules[0].speed"))
                return PathInfos.DEFAULT;
            if(path.endsWith("components.tool.rules[0].correct_for_drops"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.tooltip_style"))
            return (new PathInfo(PathType.STRING)).withDesc(Component.nullToEmpty("References tooltip sprites at \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_background\" and \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_frame\"")).withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.trim")) {
            if(path.endsWith("components.trim"))
                return (new PathInfo(KeyGetter.create().withRequired("pattern","material").withOptional("show_in_tooltip"))).withIcon(Items.DIAMOND_CHESTPLATE);
            if(path.endsWith("components.trim.pattern"))
                return (new PathInfo(PathType.STRING,DATA_TRIM_PATTERN));
            if(path.endsWith("components.trim.material"))
                return (new PathInfo(PathType.STRING,DATA_TRIM_MATERIAL));
            if(path.endsWith("components.trim.show_in_tooltip"))
                return PathInfos.TRINARY;
        }

        if(path.endsWith("components.unbreakable"))
            return PathInfos.TOOLTIP_UNIT.withIcon(Items.COMMAND_BLOCK);

        if(path.contains("components.use_cooldown")) {
            if(path.endsWith("components.use_cooldown"))
                return (new PathInfo(KeyGetter.create().withRequired("seconds").withOptional("cooldown_group"))).withIcon(Items.ENDER_PEARL);
            if(path.endsWith("components.use_cooldown.seconds"))
                return (new PathInfo(PathType.FLOAT,SuggestionGetter.newInline("1.0f")));
            if(path.endsWith("components.use_cooldown.cooldown_group"))
                return (new PathInfo(PathType.STRING,REGISTRY_ITEM)).withDesc(Component.nullToEmpty("Custom namespaced ID or namespaced item ID"));
        }

        if(path.endsWith("components.use_remainder"))
            return PathInfos.ITEM_NODE.withIcon(Items.MUSHROOM_STEW);

        if(path.contains("components.writable_book_content")) {
            if(path.endsWith("components.writable_book_content"))
                return (new PathInfo(KeyGetter.create().withOptional("pages"))).withIcon(Items.WRITABLE_BOOK);
            if(path.endsWith("components.writable_book_content.pages"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.writable_book_content.pages[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
            if(path.endsWith("components.writable_book_content.pages[0].raw"))
                return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of page text"));
            if(path.endsWith("components.writable_book_content.pages[0].filtered"))
                return PathInfos.STRING;
        }

        if(path.contains("components.written_book_content")) {
            if(path.endsWith("components.written_book_content"))
                return (new PathInfo(KeyGetter.create().withRequired("author","title").withOptional("pages","generation","resolved"))).withIcon(Items.WRITTEN_BOOK);
            if(path.endsWith("components.written_book_content.pages"))
                return PathInfos.LIST_COMPOUND;
            if(path.endsWith("components.written_book_content.pages[0]"))
                return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
            if(path.endsWith("components.written_book_content.pages[0].raw"))
                return PathInfos.TEXT;
            if(path.endsWith("components.written_book_content.pages[0].filtered"))
                return PathInfos.TEXT;
            if(path.endsWith("components.written_book_content.title"))
                return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
            if(path.endsWith("components.written_book_content.title.raw"))
                return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of title"));
            if(path.endsWith("components.written_book_content.title.filtered"))
                return PathInfos.STRING;
            if(path.endsWith("components.written_book_content.author"))
                return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of author"));
            if(path.endsWith("components.written_book_content.generation"))
                return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0","1","2","3"))).withDesc(Component.nullToEmpty("0 - Original\n1 - Copy of original\n2 - Copy of copy\n3 - Tattered")); // hardcoded list
            if(path.endsWith("components.written_book_content.resolved"))
                return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Whether or not text component is resolved (for selectors/scores/etc)"));
        }

        if(path.contains("components.!")) {
            for(String c : LIST_DATA_COMPONENT_TYPE.getList()) {
                if(path.endsWith("components.!"+c.replace("minecraft:",""))) {
                    return PathInfos.UNIT.withGroup("Inverted Components").withIcon(getPathInfo("components."+c).icon());
                }
            }
        }

        if(path.equals("id") || path.endsWith(".id"))
            return (new PathInfo(PathType.STRING,REGISTRY_ITEM)).withIcon(Items.STONE);

        if(path.equals("count") || path.endsWith(".count"))
            return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

        if(path.equals("components") || path.endsWith(".components"))
            return PathInfos.COMPONENTS_OR_INVERTED;

        FortytwoEdit.logWarn("No PathInfo found for path: "+path);
        return PathInfos.UNKNOWN;
    }

    /**
     * <p> PathType type - to setup widget </p>
     * <p> SuggestionGetter suggs - for textbox suggestions </p>
     * <p> Text description - displays in tooltip </p>
     * <p> KeyGetter keys - keys that can be included in compound (only for PathType.COMPOUND) </p>
     * <p> byte listType - NbtElement.getType() type of list (only for PathType.LIST) </p>
     * <p> String keyGroup - label to sort key within compound </p>
     * <p> boolean dynamic - for PathInfo not to be cached </p>
     * <p> PathFlag flag - for hardcoded effects </p>
     * <p> ItemStack icon - icon to show (only for components on main itembuilder component page) </p>
     */
    public record PathInfo(PathType type, SuggestionGetter suggs, Component description, KeyGetter keys, byte listType, String keyGroup, boolean dynamic, PathFlag flag, ItemStack icon) {

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
        public PathInfo(PathType type, SuggestionGetter suggs) {
            this(type,suggs,null,null,DEFAULT_LIST_TYPE,DEFAULT_GROUP,false,PathFlag.NONE,null);
        }

        /**
         * Create a PathInfo for a compound.
         * 
         * @param keys all keys that may be in the compound
         */
        public PathInfo(KeyGetter keys) {
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
        public PathInfo withDesc(Component desc) {
            return new PathInfo(this.type, this.suggs, desc, this.keys, this.listType, this.keyGroup, this.dynamic, this.flag, this.icon);
        }

        /**
         * Add a custom group label to the PathInfo.
         * Keys in a compound will be grouped in their label.
         * 
         * @param num
         * @return a copy with the group number added.
         */
        public PathInfo withGroup(String group) {
            return new PathInfo(this.type, this.suggs, this.description, this.keys, this.listType, group, this.dynamic, this.flag, this.icon);
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
                case BYTE: return Tag.TAG_BYTE;
                case SHORT: return Tag.TAG_SHORT;
                case INT: return Tag.TAG_INT;
                case LONG: return Tag.TAG_LONG;
                case DOUBLE: return Tag.TAG_DOUBLE;
                case FLOAT: return Tag.TAG_FLOAT;
                case STRING: return Tag.TAG_STRING;

                case BYTE_ARRAY: return Tag.TAG_BYTE_ARRAY;
                case INT_ARRAY: return Tag.TAG_INT_ARRAY;
                case LONG_ARRAY: return Tag.TAG_LONG_ARRAY;

                case COMPOUND: return Tag.TAG_COMPOUND;
                case LIST: return Tag.TAG_LIST;

                case UNKNOWN: return -1;
                case DEFAULT: return -1;
                case TEXT: return -1;
                case DECIMAL_COLOR: return Tag.TAG_INT;
                case UNIT: return Tag.TAG_COMPOUND;
                case TRINARY: return Tag.TAG_BYTE;
                case TOOLTIP_UNIT: return Tag.TAG_COMPOUND;
                case UUID: return Tag.TAG_INT_ARRAY;
                case INLINE_LIST: return Tag.TAG_LIST;
                case INLINE_COMPOUND: return Tag.TAG_COMPOUND;
                case BANNER: return Tag.TAG_COMPOUND;
                case POSE: return Tag.TAG_COMPOUND;
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
            case Tag.TAG_BYTE: return "Byte";
            case Tag.TAG_SHORT: return "Short";
            case Tag.TAG_INT: return "Int";
            case Tag.TAG_LONG: return "Long";
            case Tag.TAG_DOUBLE: return "Double";
            case Tag.TAG_FLOAT: return "Float";
            case Tag.TAG_STRING: return "String";

            case Tag.TAG_BYTE_ARRAY: return "Byte Array";
            case Tag.TAG_INT_ARRAY: return "Int Array";
            case Tag.TAG_LONG_ARRAY: return "Long Array";

            case Tag.TAG_COMPOUND: return "Compound";
            case Tag.TAG_LIST: return "List";

            default: return null;
        }
    }

    private class PathInfos {

        private static final PathInfo UNKNOWN = (new PathInfo(PathType.UNKNOWN));
        private static final PathInfo DEFAULT = (new PathInfo(PathType.DEFAULT));
        private static final PathInfo UNIT = (new PathInfo(PathType.UNIT,SuggestionGetter.newInline("","{}"))).withDesc(Component.nullToEmpty("{} represents true"));
        private static final PathInfo TOOLTIP_UNIT = (new PathInfo(PathType.TOOLTIP_UNIT,SuggestionGetter.newInline("","{show_in_tooltip:0b}","{}"))).withDesc(Component.nullToEmpty("{} or {show_in_tooltip:0b}"));
        private static final PathInfo TRINARY = (new PathInfo(PathType.TRINARY,SuggestionGetter.newInline("","0b","1b"))).withDesc(Component.nullToEmpty("Boolean 0b (false) or 1b (true)"));
        private static final PathInfo SHORT = (new PathInfo(PathType.SHORT,SuggestionGetter.newInline("0s")));
        private static final PathInfo INT = (new PathInfo(PathType.INT,SuggestionGetter.newInline("0")));
        private static final PathInfo LONG = (new PathInfo(PathType.LONG,SuggestionGetter.newInline("0l")));
        private static final PathInfo DOUBLE = (new PathInfo(PathType.DOUBLE,SuggestionGetter.newInline("0.0d")));
        private static final PathInfo FLOAT = (new PathInfo(PathType.FLOAT,SuggestionGetter.newInline("0.0f")));
        private static final PathInfo STRING = (new PathInfo(PathType.STRING));
        private static final PathInfo BYTE_ARRAY = (new PathInfo(PathType.BYTE_ARRAY,SuggestionGetter.newInline("[B;]")));
        private static final PathInfo LIST_COMPOUND = (new PathInfo(Tag.TAG_COMPOUND));
        private static final PathInfo LIST_FLOAT = (new PathInfo(Tag.TAG_FLOAT));
        private static final PathInfo LIST_STRING = (new PathInfo(Tag.TAG_STRING));
        private static final PathInfo INLINE_COMPOUND = (new PathInfo(PathType.INLINE_COMPOUND,SuggestionGetter.newInline("{}")));

        private static final PathInfo TEXT = (new PathInfo(PathType.TEXT,SuggestionGetter.newInline("\"\"","{text:\"\"}","[\"\"]"))).withDesc(Component.nullToEmpty("Text component"));
        private static final PathInfo DECIMAL_COLOR = (new PathInfo(PathType.DECIMAL_COLOR,SuggestionGetter.newInline("0","16777215"))).withDesc(Component.nullToEmpty("0xRRGGBB hex color converted to integer"));
        private static final PathInfo UUID = (new PathInfo(PathType.UUID,SuggestionGetter.newInline("[I;0,0,0,0]")));
        private static final PathInfo INT_ARRAY_POS = (new PathInfo(PathType.INT_ARRAY,SuggestionGetter.newInline("[I;0,0,0]"))).withDesc(Component.nullToEmpty("[I; X, Y, Z] block coordinates"));

        private static final PathInfo ITEM_NODE = (new PathInfo(KeyGetter.create().withRequired("id").withOptional("count","components")));
        private static final PathInfo ITEM_COUNT = (new PathInfo(PathType.INT,SuggestionGetter.newInline("1","16","64","99")));

        private static final PathInfo COMPONENTS_NODE = (new PathInfo(KeyGetter.create().withOptional(LIST_DATA_COMPONENT_TYPE)));
        private static final PathInfo COMPONENTS_OR_INVERTED = (new PathInfo(KeyGetter.create().withOptional(LIST_DATA_COMPONENT_TYPE,LIST_DATA_COMPONENT_TYPE.withFormat(SuggestionGetter.Format.INVERTED))));

        private static final PathInfo POTION_CONTENTS = (new PathInfo(KeyGetter.create().withOptional("potion","custom_color","custom_effects","custom_name")));
        private static final PathInfo EFFECT_NODE = (new PathInfo(KeyGetter.create().withRequired("id").withOptional("amplifier","duration","ambient","show_particles","show_icon"))).withFlag(PathFlag.EFFECT);
        private static final PathInfo EFFECT_DURATION = (new PathInfo(PathType.INT,SuggestionGetter.newInline("-1","1"))).withDesc(Component.nullToEmpty("Duration in ticks or -1 for infinity"));
        private static final PathInfo EFFECT_AMPLIFIER = (new PathInfo(PathType.BYTE,SuggestionGetter.newInline("0","255"))).withDesc(Component.nullToEmpty("Amplifier 0-255 gives effect level 1-256"));

        private static final PathInfo BLOCK_PREDICATE_BLOCKS = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
            REGISTRY_BLOCK.withFormat(SuggestionGetter.Format.NBT_STRING),
            DATA_TAG_BLOCK.withFormat(SuggestionGetter.Format.NBT_STRING),
            SuggestionGetter.newInline("[\"dirt\",\"stone\"]"))))
            .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an block ID or block tag\nb) NbtList of block ID NbtStrings"));
        private static final PathInfo ITEM_PREDICATE_ITEMS = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
            REGISTRY_ITEM.withFormat(SuggestionGetter.Format.NBT_STRING),
            DATA_TAG_ITEM.withFormat(SuggestionGetter.Format.NBT_STRING),
            SuggestionGetter.newInline("[\"diamond\",\"gold_ingot\"]"))))
            .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an item ID or item tag\nb) NbtList of item ID NbtStrings"));
        private static final PathInfo ENTITY_PREDICATE_ENTITIES = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
            REGISTRY_ENTITY_TYPE.withFormat(SuggestionGetter.Format.NBT_STRING),
            DATA_TAG_ENTITY_TYPE.withFormat(SuggestionGetter.Format.NBT_STRING),
            SuggestionGetter.newInline("[\"skeleton\",\"zombie\"]"))))
            .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an entity ID or entity tag\nb) NbtList of entity ID NbtStrings"));

    }

    public record KeyGetter(SuggestionGetter[] requiredKeys, SuggestionGetter[] optionalKeys) {

        public static KeyGetter create() {
            return new KeyGetter(new SuggestionGetter[0], new SuggestionGetter[0]);
        }

        public KeyGetter withRequired(SuggestionGetter... keys) {
            List<SuggestionGetter> newList = Lists.newArrayList();
            newList.addAll(List.of(requiredKeys));
            newList.addAll(List.of(keys));
            return new KeyGetter(newList.toArray(new SuggestionGetter[0]), optionalKeys);
        }

        public KeyGetter withOptional(SuggestionGetter... keys) {
            List<SuggestionGetter> newList = Lists.newArrayList();
            newList.addAll(List.of(optionalKeys));
            newList.addAll(List.of(keys));
            return new KeyGetter(requiredKeys, newList.toArray(new SuggestionGetter[0]));
        }

        public KeyGetter withRequired(String... keys) {
            List<SuggestionGetter> newList = Lists.newArrayList();
            newList.addAll(List.of(requiredKeys));
            newList.add(SuggestionGetter.newInline(keys));
            return new KeyGetter(newList.toArray(new SuggestionGetter[0]), optionalKeys);
        }

        public KeyGetter withOptional(String... keys) {
            List<SuggestionGetter> newList = Lists.newArrayList();
            newList.addAll(List.of(optionalKeys));
            newList.add(SuggestionGetter.newInline(keys));
            return new KeyGetter(requiredKeys, newList.toArray(new SuggestionGetter[0]));
        }

        public Set<String> getRequired() {
            Set<String> set = Sets.newHashSet();
            for(SuggestionGetter suggs : requiredKeys)
                set.addAll(suggs.getList());
            return set;
        }

        public Set<String> getOptional() {
            Set<String> set = Sets.newHashSet();
            for(SuggestionGetter suggs : optionalKeys)
                set.addAll(suggs.getList());
            set.removeAll(getRequired());
            return set;
        }

    }

    public record SuggestionGetter(String[] inlinedSuggs, String listMapKey, Format format, SuggestionGetter[] joinedLists) {

        public static SuggestionGetter newInline(String... suggs) {
            return new SuggestionGetter(suggs, null, Format.NONE, null);
        }
        public static SuggestionGetter newInline(Format format, String... suggs) {
            return new SuggestionGetter(suggs, null, format, null);
        }

        public static SuggestionGetter newRef(String key) {
            return new SuggestionGetter(null, key, Format.NONE, null);
        }

        public static SuggestionGetter newJoined(SuggestionGetter... lists) {
            return new SuggestionGetter(null, null, Format.NONE, lists);
        }

        public SuggestionGetter withFormat(Format format) {
            if(joinedLists != null)
                FortytwoEdit.logWarn("Invalid SuggestionGetter tried to format a 'newJoined' type");
            return new SuggestionGetter(this.inlinedSuggs, this.listMapKey, format, this.joinedLists);
        }

        private void addFormatted(Set<String> set, Format format, String[] toAdd) {
            switch(format) {
                case NONE : {
                    for(String s : toAdd)
                        set.add(s);
                    break;
                }
                case NBT_STRING : {
                    for(String s : toAdd)
                        set.add(BlackMagick.nbtToString(StringTag.valueOf(s)));
                    break;
                }
                case INVERTED : {
                    for(String s : toAdd)
                        set.add("!"+s);
                    break;
                }
                case AEC_PARTICLE_TYPE : {
                    for(String s : toAdd) {
                        CompoundTag nbt = new CompoundTag();
                        nbt.putString("type",s);
                        set.add(BlackMagick.nbtToString(nbt));
                    }
                    break;
                }
            }
        }

        public List<String> getList() {
            Set<String> set = Sets.newHashSet();
            if(inlinedSuggs != null)
                addFormatted(set, format, inlinedSuggs);
            if(listMapKey != null && SUGGS_LIST_METHODS.containsKey(listMapKey))
                addFormatted(set, format, SUGGS_LIST_METHODS.get(listMapKey).get().toArray(new String[0]));
            if(joinedLists != null)
                for(SuggestionGetter s : joinedLists)
                    set.addAll(s.getList());
            List<String> list = Lists.newArrayList();
            list.addAll(set);
            Collections.sort(list);
            return list;
        }

        public String[] getArray() {
            return getList().toArray(new String[0]);
        }

        public enum Format {
            NONE,                   // suggs sent as-is
            NBT_STRING,             // suggs formatted inside valid NBT string (double or single quotes)
            INVERTED,               // suggs prefixed with !
            AEC_PARTICLE_TYPE       // suggs listed inside compound in key `type`
        }

    }


    private static final Map<String,List<String>> LIST_CACHES = Maps.newHashMap();
    private static final Set<String> DYNAMIC_LIST_CACHES = Sets.newHashSet();
    private static final Map<String,Supplier<List<String>>> SUGGS_LIST_METHODS = Maps.newHashMap();

    public static void clearDynamicListCaches() {
        for(String key : DYNAMIC_LIST_CACHES)
            LIST_CACHES.get(key).clear();
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

    protected static List<String> createOrGetCacheList(String name, boolean isDynamic) {
        // helpful regex search `createOrGetCacheList\(.*true\)` or `createOrGetCacheList\(.*false\)`
        if(LIST_CACHES.containsKey(name))
            return LIST_CACHES.get(name);
        List<String> list = Lists.newArrayList();
        LIST_CACHES.put(name,list);
        if(isDynamic)
            DYNAMIC_LIST_CACHES.add(name);
        return list;
    }

    protected static SuggestionGetter registerSuggsList(String listName, Supplier<List<String>> method) {
        SUGGS_LIST_METHODS.put(listName, method);
        return SuggestionGetter.newRef(listName);
    }


    /**
     * Get list of all possible block states that can be applied to the item
     * 
     * @param item the ItemStack.getItem()
     * @return list of lists where the first string in each list is the key and the rest are the value options (may be empty but never null)
     */
    public static List<List<String>> getBlockStates(Item item) {
        List<List<String>> states = Lists.newArrayList();
        BlockState blockState = Block.byItem(item).defaultBlockState();
        for(Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
            List<String> list = Lists.newArrayList();
            list.add(entry.getKey().getName());
            for(Comparable<?> val : entry.getKey().getPossibleValues()) {
                list.add((String)Util.getPropertyName(entry.getKey(), val));
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
            final Minecraft client = Minecraft.getInstance();
            if(client.level != null) {
                Enchantment ench = client.level.registryAccess().lookup(Registries.ENCHANTMENT).get().getValue(ResourceLocation.parse(key));
                if(ench != null)
                    max = ench.getMaxLevel();
            }
        } catch(Exception ex) {}
        return max;
    }

    /**
     * <li> Find items by searching {@link net.minecraft.world.item.Items} for {@link net.minecraft.core.component.DataComponents#CONTAINER} aka (`DataComponents.CONTAINER`) </li>
     * <li> (Search for `DataComponents.CONTAINER`) </li>
     * <li> Manually enter rows/cols based on ingame gui appearance. </li>
     * <li> Remove ender chest. </li>
     * 
     * @param item
     * @return int array with [rows,columns] or [-1,-1] depending on storage size of blockentity for item
     */
    public static int[] getContainerSize(Item item) {

        ResourceLocation identifier = BlackMagick.identifierOrNull(item.toString());
        if(identifier != null) {
            String id = identifier.toString();

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
                default: break;
            }
        }
        return new int[]{-1,-1};
    }


    // static hardcoded lists

    public static final SuggestionGetter LIST_ATTRIBUTE_MODIFIER_SLOT = registerSuggsList("LIST_ATTRIBUTE_MODIFIER_SLOT", () -> {
        List<String> list = createOrGetCacheList("LIST_ATTRIBUTE_MODIFIER_SLOT",false);
        if(list.isEmpty()) {
            for(EquipmentSlotGroup i : EquipmentSlotGroup.values())
                list.add(i.getSerializedName());
            Collections.sort(list);
        }
        return list;
    });

    /**
     * Contains components that can be serialized
     */
    public static final SuggestionGetter LIST_DATA_COMPONENT_TYPE = registerSuggsList("LIST_DATA_COMPONENT_TYPE", () -> {
        List<String> list = createOrGetCacheList("LIST_DATA_COMPONENT_TYPE",false);
        if(list.isEmpty()) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.forEach(i -> {
                if(!i.isTransient())
                    list.add(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(i).toString());
            });
            Collections.sort(list);
        }
        return list;
    });

    /**
     * Contains pottery sherd items and brick item
     */
    public static final SuggestionGetter LIST_DECORATED_POT_PATTERN_ITEMS = registerSuggsList("LIST_DECORATED_POT_PATTERN_ITEMS", () -> {//to_do use item tag `#minecraft:decorated_pot_ingredients` ?
        List<String> list = createOrGetCacheList("LIST_DECORATED_POT_PATTERN_ITEMS",false);
        if(list.isEmpty()) {
            for(Item i : DecoratedPotPatternsAccessor.getItemToPotTexture().keySet())
                list.add(i.toString());
            Collections.sort(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_DYE_COLOR = registerSuggsList("LIST_DYE_COLOR", () -> {
        List<String> list = createOrGetCacheList("LIST_DYE_COLOR",false);
        if(list.isEmpty()) {
            for(DyeColor i : DyeColor.values())
                list.add(i.getSerializedName());
            Collections.sort(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_EQUIPMENT_SLOT = registerSuggsList("LIST_EQUIPMENT_SLOT", () -> {
        List<String> list = createOrGetCacheList("LIST_EQUIPMENT_SLOT",false);
        if(list.isEmpty()) {
            for(EquipmentSlot i : EquipmentSlot.values())
                list.add(i.getSerializedName());
            Collections.sort(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE = registerSuggsList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE", () -> {
        List<String> list = createOrGetCacheList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE",false);
        if(list.isEmpty()) {
            for(FireworkExplosion.Shape i : FireworkExplosion.Shape.values())
                list.add(i.getSerializedName());
            Collections.sort(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_FORMATTING_COLOR = registerSuggsList("LIST_FORMATTING_COLOR", () -> {
        List<String> list = createOrGetCacheList("LIST_FORMATTING_COLOR",false);
        if(list.isEmpty()) {
            for(String i : ChatFormatting.getNames(true, false))
                if(!i.equals("reset"))
                    list.add(i);
            Collections.sort(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_KEYBIND = registerSuggsList("LIST_KEYBIND", () -> {
        List<String> list = createOrGetCacheList("LIST_KEYBIND",false);
        if(list.isEmpty()) {
            for(String i : KeyMappingAccessor.getKeysList().keySet())
                if(!i.startsWith("42edit."))
                    list.add(i);
            Collections.sort(list);
        }
        return list;
    });

    /**
     * Entries for consumable component animation
     */
    public static final SuggestionGetter LIST_USE_ACTION = registerSuggsList("LIST_USE_ACTION", () -> {
        List<String> list = createOrGetCacheList("LIST_USE_ACTION",false);
        if(list.isEmpty()) {
            for(ItemUseAnimation i : ItemUseAnimation.values())
                list.add(i.getSerializedName());
            Collections.sort(list);
        }
        return list;
    });


    // dynamic hardcoded lists

    public static final SuggestionGetter LIST_TRANSLATION_KEY = registerSuggsList("LIST_TRANSLATION_KEY", () -> {
        List<String> list = createOrGetCacheList("LIST_TRANSLATION_KEY",true);
        if(list.isEmpty()) {
            final Language lang = Language.getInstance();
            if(lang instanceof ClientLanguage) {
                for(String i : ((ClientLanguageAccessor)lang).getTranslations().keySet())
                    if(!i.startsWith("42edit."))
                        list.add(i);
                Collections.sort(list);
            }
        }
        return list;
    });


    // static registry lists

    private static List<String> getRegistryIfEmpty(List<String> list, Registry<?> registryRef) {
        if(list.isEmpty()) {
            for(ResourceLocation i : registryRef.keySet())
                list.add(i.toString());
            Collections.sort(list);
        }
        return list;
    }

    public static final SuggestionGetter REGISTRY_ATTRIBUTE = registerSuggsList("REGISTRY_ATTRIBUTE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ATTRIBUTE",false),BuiltInRegistries.ATTRIBUTE));

    public static final SuggestionGetter REGISTRY_BLOCK = registerSuggsList("REGISTRY_BLOCK", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_BLOCK",false),BuiltInRegistries.BLOCK));

    public static final SuggestionGetter REGISTRY_BLOCK_ENTITY_TYPE = registerSuggsList("REGISTRY_BLOCK_ENTITY_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_BLOCK_ENTITY_TYPE",false),BuiltInRegistries.BLOCK_ENTITY_TYPE));

    public static final SuggestionGetter REGISTRY_CONSUME_EFFECT_TYPE = registerSuggsList("REGISTRY_CONSUME_EFFECT_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_CONSUME_EFFECT_TYPE",false),BuiltInRegistries.CONSUME_EFFECT_TYPE));

    public static final SuggestionGetter REGISTRY_ITEM = registerSuggsList("REGISTRY_ITEM", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ITEM",false),BuiltInRegistries.ITEM));

    public static final SuggestionGetter REGISTRY_SOUND_EVENT = registerSuggsList("REGISTRY_SOUND_EVENT", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_SOUND_EVENT",false),BuiltInRegistries.SOUND_EVENT));

    public static final SuggestionGetter REGISTRY_STATUS_EFFECT = registerSuggsList("REGISTRY_STATUS_EFFECT", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_STATUS_EFFECT",false),BuiltInRegistries.MOB_EFFECT));

    public static final SuggestionGetter REGISTRY_ENTITY_TYPE = registerSuggsList("REGISTRY_ENTITY_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ENTITY_TYPE",false),BuiltInRegistries.ENTITY_TYPE));

    public static final SuggestionGetter REGISTRY_PARTICLE_TYPE = registerSuggsList("REGISTRY_PARTICLE_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_PARTICLE_TYPE",false),BuiltInRegistries.PARTICLE_TYPE));

    public static final SuggestionGetter REGISTRY_POTION = registerSuggsList("REGISTRY_POTION", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_POTION",false),BuiltInRegistries.POTION));


    // dynamic data lists

    private static List<String> getDataIfEmpty(List<String> list, ResourceKey<? extends Registry<?>> registryRef) {
        if(list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if(client.level != null)
                client.level.registryAccess().lookup(registryRef).ifPresent(reg -> {
                    for(ResourceLocation i : reg.keySet())
                        list.add(i.toString());
                });
            Collections.sort(list);
        }
        return list;
    }

    public static final SuggestionGetter DATA_BANNER_PATTERN = registerSuggsList("DATA_BANNER_PATTERN", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_BANNER_PATTERN",true),Registries.BANNER_PATTERN));

    public static final SuggestionGetter DATA_DAMAGE_TYPE = registerSuggsList("DATA_DAMAGE_TYPE", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_DAMAGE_TYPE",true),Registries.DAMAGE_TYPE));

    public static final SuggestionGetter DATA_ENCHANTMENT = registerSuggsList("DATA_ENCHANTMENT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_ENCHANTMENT",true),Registries.ENCHANTMENT));

    public static final SuggestionGetter DATA_INSTRUMENT = registerSuggsList("DATA_INSTRUMENT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_INSTRUMENT",true),Registries.INSTRUMENT));

    public static final SuggestionGetter DATA_JUKEBOX_SONG = registerSuggsList("DATA_JUKEBOX_SONG", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_JUKEBOX_SONG",true),Registries.JUKEBOX_SONG));

    public static final SuggestionGetter DATA_PAINTING_VARIANT = registerSuggsList("DATA_PAINTING_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_PAINTING_VARIANT",true),Registries.PAINTING_VARIANT));

    public static final SuggestionGetter DATA_TRIM_MATERIAL = registerSuggsList("DATA_TRIM_MATERIAL", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_TRIM_MATERIAL",true),Registries.TRIM_MATERIAL));

    public static final SuggestionGetter DATA_TRIM_PATTERN = registerSuggsList("DATA_TRIM_PATTERN", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_TRIM_PATTERN",true),Registries.TRIM_PATTERN));

    public static final SuggestionGetter DATA_WOLF_VARIANT = registerSuggsList("DATA_WOLF_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_WOLF_VARIANT",true),Registries.WOLF_VARIANT));


    // dynamic data tags lists

    private static List<String> getTagsIfEmpty(List<String> list, ResourceKey<? extends Registry<?>> registryRef) {
        if(list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if(client.level != null)
                client.level.registryAccess().lookup(registryRef).ifPresent(reg -> {
                    reg.listTagIds().forEach(tag -> {
                        list.add("#"+tag.location().toString());
                    });
                });
            Collections.sort(list);
        }
        return list;
    }

    public static final SuggestionGetter DATA_TAG_BANNER_PATTERN = registerSuggsList("DATA_TAG_BANNER_PATTERN", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_BANNER_PATTERN",true),Registries.BANNER_PATTERN));

    public static final SuggestionGetter DATA_TAG_BLOCK = registerSuggsList("DATA_TAG_BLOCK", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_BLOCK",true),Registries.BLOCK));

    public static final SuggestionGetter DATA_TAG_DAMAGE_TYPE = registerSuggsList("DATA_TAG_DAMAGE_TYPE", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_DAMAGE_TYPE",true),Registries.DAMAGE_TYPE));

    public static final SuggestionGetter DATA_TAG_ENCHANTMENT = registerSuggsList("DATA_TAG_ENCHANTMENT", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ENCHANTMENT",true),Registries.ENCHANTMENT));

    public static final SuggestionGetter DATA_TAG_ENTITY_TYPE = registerSuggsList("DATA_TAG_ENTITY_TYPE", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ENTITY_TYPE",true),Registries.ENTITY_TYPE));

    public static final SuggestionGetter DATA_TAG_ITEM = registerSuggsList("DATA_TAG_ITEM", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ITEM",true),Registries.ITEM));

    public static final SuggestionGetter DATA_TAG_PAINTING_VARIANT = registerSuggsList("DATA_TAG_PAINTING_VARIANT", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_PAINTING_VARIANT",true),Registries.PAINTING_VARIANT));


    // static data lists from vanilla

    private static List<String> getVanillaDataIfEmpty(List<String> list, String path, String suffix) {
        if(list.isEmpty()) {
            try {
                Map<ResourceLocation,IoSupplier<InputStream>> map = Maps.newHashMap();
                final String namespace = "minecraft";
                ServerPacksSource.createVanillaPackSource().listResources(PackType.SERVER_DATA, namespace, path, map::putIfAbsent);
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

    public static final SuggestionGetter DATA_LOOT_TABLE = registerSuggsList("DATA_LOOT_TABLE", () ->
        getVanillaDataIfEmpty(createOrGetCacheList("DATA_LOOT_TABLE",false),"loot_table",JSON_SUFFIX));

    public static final SuggestionGetter DATA_RECIPE = registerSuggsList("DATA_RECIPE", () ->
        getVanillaDataIfEmpty(createOrGetCacheList("DATA_RECIPE",false),"recipe",JSON_SUFFIX));

    public static final SuggestionGetter DATA_STRUCTURE = registerSuggsList("DATA_STRUCTURE", () ->
        getVanillaDataIfEmpty(createOrGetCacheList("DATA_STRUCTURE",false),"structure",NBT_SUFFIX));

    public static final SuggestionGetter DATA_TRIAL_SPAWNER = registerSuggsList("DATA_TRIAL_SPAWNER", () ->
        getVanillaDataIfEmpty(createOrGetCacheList("DATA_TRIAL_SPAWNER",false),"trial_spawner",JSON_SUFFIX));


    // static assets lists from vanilla

    private static List<String> getVanillaAssetsIfEmpty(List<String> list, String path, String suffix) {
        if(list.isEmpty()) {
            try {
                Map<ResourceLocation,IoSupplier<InputStream>> map = Maps.newHashMap();
                final String namespace = "minecraft";
                ServerPacksSource.createVanillaPackSource().listResources(PackType.CLIENT_RESOURCES, namespace, path, map::putIfAbsent);
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

    public static final SuggestionGetter ASSETS_EQUIPMENT = registerSuggsList("ASSETS_EQUIPMENT", () ->
        getVanillaAssetsIfEmpty(createOrGetCacheList("ASSETS_EQUIPMENT",false),"equipment",JSON_SUFFIX));

    public static final SuggestionGetter ASSETS_FONT = registerSuggsList("ASSETS_FONT", () ->
        getVanillaAssetsIfEmpty(createOrGetCacheList("ASSETS_FONT",false),"font",JSON_SUFFIX));

    public static final SuggestionGetter ASSETS_ITEMS = registerSuggsList("ASSETS_ITEMS", () ->
        getVanillaAssetsIfEmpty(createOrGetCacheList("ASSETS_ITEMS",false),"items",JSON_SUFFIX));

    public static final SuggestionGetter ASSETS_TEXTURES = registerSuggsList("ASSETS_TEXTURES", () ->
        getVanillaAssetsIfEmpty(createOrGetCacheList("ASSETS_TEXTURES",false),"textures",PNG_SUFFIX));


}
