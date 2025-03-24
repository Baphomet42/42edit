package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.SuggestionHelper.SuggestionGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.MapItemColor;

public class PathHelper {

    public static PathInfo getItemPath(Tag element, PathNode... path) {
        return getPath(element, PathInfoGetter.of("item_stack").get(), path);
    }

    public static PathInfo getPath(Tag element, PathInfo context, PathNode... path) {
        List<PathNode> currentPath = Lists.newArrayList();
        if(path != null)
            currentPath.addAll(List.of(path));
        PathInfo currentContext = context;
        while(!currentPath.isEmpty() && currentContext != null && !currentContext.isEmpty()) {
            currentContext = currentContext.getNode(element, currentPath.get(0));
            currentPath.remove(0);
        }
        if(currentPath.isEmpty() && currentContext != null && !currentContext.isEmpty())
            return currentContext;
        return PathInfo.EMPTY;
    }

    /**
     * <p> These often change every update and must be kept up to date manually </p>
     * <p> Check the following for help: </p>
     * <ul>
     *  <li> {@link net.minecraft.core.component.DataComponents} </li>
     *  <li> https://minecraft.wiki/w/Item_format </li>
     *  <li> https://minecraft.wiki/w/Entity_format </li>
     *  <li> https://minecraft.wiki/w/Chunk_format#Block_entity_format </li>
     * </ul>
     * <p> Helpful search terms to find hardcoded data: </p>
     * <ul>
     *  <li> `newInline(` </li>
     *  <li> `newInlineSnbt(` </li>
     * </ul>
     */
    private static void buildPathInfos() {
        PATH_INFO_REF_MAP.clear();

        registerPathInfo("item_stack", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_ITEM)).setIcon(Items.STONE).getter()
            ),Map.of(
            "count", PathInfo.copyOf("item_count").setIcon(Items.STONE).getter(),
            "components", PathInfo.create(DataType.CompoundComponentsMap.create()).setIcon(Items.STONE).getter()
        ))).setIcon(Items.STONE));

        registerPathInfo("components.minecraft:attribute_modifiers", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "type", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_ATTRIBUTE)).getter(),
                "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("minecraft:armor.body","minecraft:armor.boots","minecraft:armor.chestplate","minecraft:armor.helmet","minecraft:armor.leggings",Item.BASE_ATTACK_DAMAGE_ID.toString(),Item.BASE_ATTACK_SPEED_ID.toString()))).setInfo("Unique namespaced ID used to update modifiers").getter(),
                "amount", PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).getter(),
                "operation", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("add_value","add_multiplied_base","add_multiplied_total"))).setInfo("add_value: base + amount1 + amount2\n\nadd_multiplied_base: base * (1 + amount1 + amount2)\n\nadd_multiplied_total: base * (1 + amount1) * (1 + amount2)").getter()
                ),Map.of(
                "slot", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_ATTRIBUTE_MODIFIER_SLOT)).setUnsetInfo(StringTag.valueOf("any")).getter()
            ))).setFlag(PathFlag.ATTRIBUTE_MODIFIER).getter()
        )).setIcon(Items.DIAMOND_SWORD));

        registerPathInfo("components.minecraft:banner_patterns", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "color", PathInfoGetter.of("dye_color"),
                "pattern", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_BANNER_PATTERN)).getter()
            ))).setFlag(PathFlag.BANNER_PATTERN).getter()
        )).setIcon(Items.LIGHT_GRAY_BANNER));

        registerPathInfo("components.minecraft:base_color", PathInfo.copyOf("dye_color").setIcon(Items.SHIELD).setInfo("Used for the banner color of a shield"));

        registerPathInfo("components.minecraft:bees", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "min_ticks_in_hive", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter(),
                "ticks_in_hive", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter()
                ),Map.of(
                "entity_data", PathInfo.create().getter()
            ))).getter()
        )).setIcon(Items.BEE_NEST));

        registerPathInfo("components.minecraft:block_entity_data", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_BLOCK_ENTITY_TYPE)).getter()
            ),Map.of(
            "front_text", PathInfo.create().getter()
        ))).setIcon(Items.SPAWNER));

        registerPathInfo("components.minecraft:block_state", PathInfo.create().setIcon(Items.PALE_OAK_STAIRS));

        registerPathInfo("components.minecraft:blocks_attacks", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "block_delay_seconds", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter(),
            "disable_cooldown_scale", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter(),
            "damage_reductions", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "base", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter(),
                    "factor", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
                    ),Map.of(
                    "types", PathInfo.copyOf("damage_type_id_tag_or_list").setUnsetInfo("Applies to all damage types").getter(),
                    "horizontal_blocking_angle", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("90.0f"))).setUnsetInfo(FloatTag.valueOf(90f)).getter()
                ))).getter()
            )).getter(),
            "item_damage", PathInfo.create(DataType.CompoundStructured.allRequired(Map.ofEntries(
                Map.entry("base", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()),
                Map.entry("factor", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()),
                Map.entry("threshold", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter())
            ))).getter(),
            "block_sound", PathInfoGetter.of("sound_event_or_definition"),
            "disable_sound", PathInfoGetter.of("sound_event_or_definition"),
            "bypassed_by", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
        ))).setIcon(Items.SHIELD));

        registerPathInfo("components.minecraft:break_sound", PathInfo.copyOf("sound_event_or_definition"));

        registerPathInfo("components.minecraft:bucket_entity_data", PathInfo.create(DataType.CompoundStructured.allOptional(Map.ofEntries(
            Map.entry("NoAI", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()),
            Map.entry("Silent", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()),
            Map.entry("NoGravity", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()),
            Map.entry("Glowing", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()),
            Map.entry("Invulnerable", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()),
            Map.entry("Health", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()),
            Map.entry("Age", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).setInfo("Age of axolotl or tadpole").getter()),
            Map.entry("HuntingCooldown", PathInfo.create(DataType.ElementLiteral.of(NbtType.LONG)).setInfo("Used by axolotls").getter())
        ))).setIcon(Items.TROPICAL_FISH_BUCKET));

        registerPathInfo("components.minecraft:bundle_contents", PathInfo.create(DataType.ListUnordered.of(
            PathInfoGetter.of("item_stack")
        )).setIcon(Items.BUNDLE));

        registerPathInfo("components.minecraft:can_break", PathInfo.copyOf("block_predicate_or_list"));

        registerPathInfo("components.minecraft:can_place_on", PathInfo.copyOf("block_predicate_or_list"));

        registerPathInfo("components.minecraft:charged_projectiles", PathInfo.create(DataType.ListUnordered.of(
            PathInfoGetter.of("item_stack")
        )).setIcon(Items.CROSSBOW));

        registerPathInfo("components.minecraft:consumable", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "consume_seconds", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("1.6f"))).setUnsetInfo(FloatTag.valueOf(1.6f)).getter(),
            "animation", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_USE_ACTION)).setUnsetInfo(StringTag.valueOf("eat")).getter(),
            "sound", PathInfo.copyOf("sound_event_or_definition").setUnsetInfo(StringTag.valueOf("minecraft:entity.generic.eat")).getter(),
            "has_consume_particles", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter(),
            "on_consume_effects", PathInfo.create(DataType.ListUnordered.of(
                PathInfoGetter.of("consume_effect")
            )).setInfo("List of consume effects to trigger after consuming the item").getter()
        ))).setIcon(Items.GOLDEN_APPLE));

        registerPathInfo("components.minecraft:container", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "item", PathInfoGetter.of("item_stack"),
                "slot", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter()
            ))).getter()
        )).setIcon(Items.SHULKER_BOX));

        registerPathInfo("components.minecraft:container_loot", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "loot_table", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_LOOT_TABLE)).getter()
            ),Map.of(
            "seed", PathInfo.create(DataType.ElementLiteral.of(NbtType.LONG)).getter()
        ))).setIcon(Items.CHEST));

        registerPathInfo("components.minecraft:custom_data", PathInfo.create(//to_do always stores as compound
            DataType.CompoundUnstructured.create(),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("{}"))
        ).setInfo("Unstructured NBT in a compound or stringified compound").setIcon(Items.COMMAND_BLOCK));

        registerPathInfo("components.minecraft:custom_model_data", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "floats", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            )).getter(),
            "flags", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()
            )).getter(),
            "strings", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
            )).getter(),
            "colors", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.copyOf("color_rgb_int_or_list").getter()
            )).getter()
        ))).setIcon(Items.COMMAND_BLOCK));

        registerPathInfo("components.minecraft:custom_name", PathInfo.copyOf("text_component").setInfo("Renamed item text component").setFlag(PathFlag.TEXT_COMPONENT_ITALIC));

        registerPathInfo("components.minecraft:damage", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0"))).setIcon(Items.DIAMOND_PICKAXE));

        registerPathInfo("components.minecraft:damage_resistant", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
            "types", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_TAG_DAMAGE_TYPE)).getter()
        ))).setIcon(Items.NETHERITE_INGOT));

        registerPathInfo("components.minecraft:death_protection", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "death_effects", PathInfo.create(DataType.ListUnordered.of(
                PathInfoGetter.of("consume_effect")
            )).setInfo("List of consume effects to trigger after using the death protection item").getter()
        ))).setIcon(Items.TOTEM_OF_UNDYING));

        registerPathInfo("components.minecraft:debug_stick_state", PathInfo.create().setIcon(Items.DEBUG_STICK));

        registerPathInfo("components.minecraft:dyed_color", PathInfo.copyOf("color_rgb_int_or_list").setIcon(Items.LEATHER_CHESTPLATE));

        registerPathInfo("components.minecraft:enchantable", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
            "value", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("1",""+Integer.MAX_VALUE))).setInfo("Positive integer that allows better enchantments to be picked").getter()
        ))).setIcon(Items.ENCHANTED_BOOK));

        registerPathInfo("components.minecraft:enchantment_glint_override", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setIcon(Items.ENCHANTED_BOOK));

        registerPathInfo("components.minecraft:enchantments", PathInfo.create(DataType.CompoundEnchantmentsMap.create()).setIcon(Items.ENCHANTED_BOOK));

        registerPathInfo("components.minecraft:entity_data", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_ENTITY_TYPE)).getter()
            ),Map.of(
            "Tags", PathInfo.create().getter()
        ))).setIcon(Items.ARMOR_STAND));

        registerPathInfo("components.minecraft:equippable", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "slot", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_EQUIPMENT_SLOT)).getter()
            ),Map.of(
            "equip_sound", PathInfo.copyOf("sound_event_or_definition").setUnsetInfo(StringTag.valueOf("minecraft:item.armor.equip_generic")).getter(),
            "asset_id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.ASSETS_EQUIPMENT)).setInfo("Resource location of an equipment model at `assets/<namespace>/equipment/<id>`").getter(),
            "allowed_entities", PathInfo.copyOf("entity_id_tag_or_list").setUnsetInfo("Applies to all entity types").getter(),
            "dispensable", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter(),
            "swappable", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setInfo("If the item can be equipped when interacting").setUnsetInfo(true).getter(),
            "damage_on_hurt", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter(),
            "equip_on_interact", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setInfo("If the item can be equipped on a mob when interacting").setUnsetInfo(true).getter(),
            "camera_overlay", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.ASSETS_TEXTURES)).setInfo("Resource location of a texture at `assets/<namespace>/textures/<id>`").getter()
        ))).setIcon(Items.DIAMOND_CHESTPLATE));

        registerPathInfo("components.minecraft:firework_explosion", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "shape", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE)).getter()
            ),Map.of(
            "colors", PathInfo.create(DataType.ListUnordered.of(
                PathInfoGetter.of("color_rgb_int")
            )).getter(),
            "fade_colors", PathInfo.create(DataType.ListUnordered.of(
                PathInfoGetter.of("color_rgb_int")
            )).getter(),
            "has_trail", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
            "has_twinkle", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()
        ))).setIcon(Items.FIREWORK_STAR));

        registerPathInfo("components.minecraft:fireworks", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "explosions", PathInfo.create(DataType.ListUnordered.of(
                PathInfoGetter.of("components.minecraft:firework_explosion")
            )).getter(),
            "flight_duration", PathInfo.create(DataType.ElementLiteral.of(NbtType.BYTE,SuggestionGetter.newInlineSnbt("1b"))).setInfo("Byte representing a value of 0-255").setUnsetInfo(ByteTag.valueOf((byte)1)).getter()
        ))).setIcon(Items.FIREWORK_ROCKET));

        registerPathInfo("components.minecraft:food", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "nutrition", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter(),
            "saturation", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            ),Map.of(
            "can_always_eat", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()
        ))).setIcon(Items.APPLE));

        registerPathInfo("components.minecraft:glider", PathInfo.create(DataType.Unit.create()).setIcon(Items.ELYTRA));

        registerPathInfo("components.minecraft:instrument", PathInfo.create(//to_do stores in specified form
            DataType.CompoundStructured.allRequired(Map.of(
                "sound_event", PathInfoGetter.of("sound_event_or_definition"),
                "description", PathInfoGetter.of("text_component"),
                "use_duration", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).setInfo("Positive float of length in seconds").getter(),
                "range", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            )),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_INSTRUMENT)
        ).setIcon(Items.GOAT_HORN));

        registerPathInfo("components.minecraft:intangible_projectile", PathInfo.create(DataType.Unit.create()).setIcon(Items.ARROW));

        registerPathInfo("components.minecraft:item_model", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.ASSETS_ITEMS)).setIcon(Items.STONE));

        registerPathInfo("components.minecraft:item_name", PathInfo.copyOf("text_component"));

        registerPathInfo("components.minecraft:jukebox_playable", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_JUKEBOX_SONG)).setIcon(Items.MUSIC_DISC_13));

        registerPathInfo("components.minecraft:lock", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "items", PathInfoGetter.of("item_id_tag_or_list"),
            "count", PathInfo.create(//to_do stores in specified form
                DataType.ElementLiteral.of(NbtType.INT),
                DataType.CompoundStructured.allOptional(Map.of(
                    "min", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter(),
                    "max", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter()
                ))
            ).getter(),
            "components", PathInfo.create(DataType.CompoundComponentsMap.createOnlyPresent()).setInfo("Exact component matches").getter(),
            "predicates", PathInfo.create().setInfo("Component predicates").getter()
        ))).setIcon(Items.CHEST));

        registerPathInfo("components.minecraft:lodestone_tracker", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "target", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "pos", PathInfoGetter.of("pos_int_array"),
                "dimension", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("overworld","the_nether","the_end"))).getter()
            ))).getter(),
            "tracked", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setInfo("When true, the component is removed if the lodestone is broken").setUnsetInfo(true).getter()
        ))).setIcon(Items.COMPASS));

        registerPathInfo("components.minecraft:lore", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.copyOf("text_component").setFlag(PathFlag.TEXT_COMPONENT_LORE).getter()
        )).setIcon(Items.NAME_TAG));

        registerPathInfo("components.minecraft:map_color", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionHelper.LIST_MAP_COLOR)).setUnsetInfo(IntTag.valueOf(MapItemColor.DEFAULT.rgb())).setFlag(PathFlag.COLOR_RGB_INT).setIcon(Items.FILLED_MAP));

        registerPathInfo("components.minecraft:map_decorations", PathInfo.create(DataType.CompoundMap.of(
            PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "type", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_MAP_DECORATION_TYPE)).getter(),
                "x", PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("World x coordinate of the decoration").getter(),
                "z", PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("World z coordinate of the decoration").getter(),
                "rotation", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("180.0f"))).setInfo("Rotation with 180 being upright").getter()
            ))).getter(),
            Set.of("+")
        )).setIcon(Items.FILLED_MAP));

        registerPathInfo("components.minecraft:map_id", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).setIcon(Items.FILLED_MAP));

        registerPathInfo("components.minecraft:max_damage", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,
            SuggestionGetter.newInlineSnbt(
                ""+ToolMaterial.WOOD.durability(),
                ""+ToolMaterial.STONE.durability(),
                ""+ToolMaterial.GOLD.durability(),
                ""+ToolMaterial.IRON.durability(),
                ""+ToolMaterial.DIAMOND.durability(),
                ""+ToolMaterial.NETHERITE.durability()
            ))).setInfo("Vanilla values for reference:\n  Wood tools - "
                +ToolMaterial.WOOD.durability()+"\n  Stone tools - "
                +ToolMaterial.STONE.durability()+"\n  Gold tools - "
                +ToolMaterial.GOLD.durability()+"\n  Iron tools - "
                +ToolMaterial.IRON.durability()+"\n  Diamond tools - "
                +ToolMaterial.DIAMOND.durability()+"\n  Netherite tools - "
                +ToolMaterial.NETHERITE.durability()
            ).setIcon(Items.DIAMOND_PICKAXE));

        registerPathInfo("components.minecraft:max_stack_size", PathInfo.copyOf("item_count").setInfo("Integer 1-99").setIcon(Items.STONE));

        registerPathInfo("components.minecraft:note_block_sound", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_SOUND_EVENT)).setInfo("Resource location of a sound event").setIcon(Items.PLAYER_HEAD));

        registerPathInfo("components.minecraft:ominous_bottle_amplifier", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0","1","2","3","4"))).setIcon(Items.OMINOUS_BOTTLE));

        registerPathInfo("components.minecraft:pot_decorations", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DECORATED_POT_PATTERN_ITEMS)).getter()
        )).setIcon(Items.DECORATED_POT));

        registerPathInfo("components.minecraft:potion_contents", PathInfo.create(//to_do always stores as compound
            DataType.CompoundStructured.allOptional(Map.of(
                "potion", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_POTION)).setInfo("Potion ID").getter(),
                "custom_color", PathInfo.copyOf("color_rgb_int").getter(),
                "custom_name", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_POTION_CUSTOM_NAME)).setInfo("Corresponds to the translation key\n`<default item translation key>.effect.<custom_name>`\n\nOnly used for the following items:\n  minecraft:potion\n  minecraft:splash_potion\n  minecraft:lingering_potion\n  minecraft:tipped_arrow").getter(),
                "custom_effects", PathInfo.create(DataType.ListUnordered.of(
                    PathInfoGetter.of("effect_instance")
                )).getter()
            )),
            DataType.ElementLiteral.of(NbtType.STRING)
        ).setIcon(Items.POTION));

        registerPathInfo("components.minecraft:potion_duration_scale", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("0.25f","1.0f"))).setInfo("Lingering potions use 0.25f").setUnsetInfo(FloatTag.valueOf(1f)).setIcon(Items.LINGERING_POTION));

        registerPathInfo("components.minecraft:profile", PathInfo.create(//to_do always stores as compound
            DataType.CompoundStructured.allOptional(Map.of(
                "name", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("Player username to resolve skin. Once resolved, only used for the item name.").getter(),
                "id", PathInfo.copyOf("uuid").setInfo("Player UUID to resolve skin. Once resolved, has no use.").getter(),
                "properties", PathInfo.create(DataType.ListUnordered.of(
                    PathInfo.create(DataType.CompoundStructured.of(Map.of(
                        "name", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("textures"))).setInfo("Only used value is \"textures\"").getter(),
                        "value", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("Player skin information encoded in base64").getter()
                        ),Map.of(
                        "signature", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                    ))).getter()
                )).setInfo("List with a single compound entry for resolved skin information").getter()
            )),
            DataType.ElementLiteral.of(NbtType.STRING)
        ).setIcon(Items.PLAYER_HEAD));

        registerPathInfo("components.minecraft:provides_banner_patterns", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_TAG_BANNER_PATTERN)).setInfo("Banner pattern tag").setIcon(Items.CREEPER_BANNER_PATTERN));

        registerPathInfo("components.minecraft:provides_trim_material", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_TRIM_MATERIAL)).setIcon(Items.SMITHING_TABLE));

        registerPathInfo("components.minecraft:rarity", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("common","uncommon","rare","epic")))
            .setInfo(Component.empty().append("Used for item name color:")
            .append(Component.empty().append("\n  common").withStyle(ChatFormatting.WHITE))
            .append(Component.empty().append("\n  uncommon").withStyle(ChatFormatting.YELLOW))
            .append(Component.empty().append("\n  rare").withStyle(ChatFormatting.AQUA))
            .append(Component.empty().append("\n  epic").withStyle(ChatFormatting.LIGHT_PURPLE))
            ).setFlag(PathFlag.RARITY).setIcon(Items.STONE));

        registerPathInfo("components.minecraft:recipes", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_RECIPE)).getter()
        )).setIcon(Items.KNOWLEDGE_BOOK));

        registerPathInfo("components.minecraft:repairable", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "items", PathInfo.copyOf("item_id_tag_or_list").getter()
        ))).setIcon(Items.ANVIL));

        registerPathInfo("components.minecraft:repair_cost", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0",""+Integer.MAX_VALUE))).setIcon(Items.ANVIL));

        registerPathInfo("components.minecraft:stored_enchantments", PathInfo.create(DataType.CompoundEnchantmentsMap.create()).setIcon(Items.ENCHANTED_BOOK));

        registerPathInfo("components.minecraft:suspicious_stew_effects", PathInfo.create(DataType.ListUnordered.of(
            PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_STATUS_EFFECT)).setInfo("Effect ID").getter()
                ),Map.of(
                "duration", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("-1","160"))).setInfo("Duration in ticks, or -1 for infinite").setUnsetInfo(IntTag.valueOf(160)).getter()
            ))).getter()
        )).setIcon(Items.SUSPICIOUS_STEW));

        registerPathInfo("components.minecraft:tool", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "rules", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "blocks", PathInfoGetter.of("block_id_tag_or_list")
                    ),Map.of(
                    "correct_for_drops", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(false).getter(),
                    "speed", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("0.0f","1.0f"))).setInfo("Overrides default_mining_speed when set").getter()
                ))).getter()
            )).getter()
            ),Map.of(
            "default_mining_speed", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("0.0f","1.0f"))).setUnsetInfo(FloatTag.valueOf(1f)).getter(),
            "damage_per_block", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0","1",""+Integer.MAX_VALUE))).setUnsetInfo(IntTag.valueOf(1)).getter(),
            "can_destroy_blocks_in_creative", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter()
        ))).setIcon(Items.DIAMOND_PICKAXE));

        registerPathInfo("components.minecraft:tooltip_display", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "hide_tooltip", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
            "hidden_components", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DATA_COMPONENT_TYPE)).getter()
            )).getter()
        ))).setIcon(Items.COMMAND_BLOCK));

        registerPathInfo("components.minecraft:tooltip_style", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("Resource location to reference tooltip sprites at `assets/<namespace>/textures/gui/sprites/tooltip/<id>_background` and `assets/<namespace>/textures/gui/sprites/tooltip/<id>_frame`").setIcon(Items.COMMAND_BLOCK));

        registerPathInfo("components.minecraft:trim", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
            "pattern", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_TRIM_PATTERN)).getter(),
            "material", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_TRIM_MATERIAL)).getter()
        ))).setIcon(Items.SMITHING_TABLE));

        registerPathInfo("components.minecraft:unbreakable", PathInfo.create(DataType.Unit.create()).setIcon(Items.DIAMOND_PICKAXE));

        registerPathInfo("components.minecraft:use_cooldown", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "seconds", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            ),Map.of(
            "cooldown_group", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("Resource location of an item ID or a custom identifier").setUnsetInfo("Current item ID is used").getter()
        ))).setIcon(Items.ENDER_PEARL));

        registerPathInfo("components.minecraft:use_remainder", PathInfo.copyOf("item_stack"));

        registerPathInfo("components.minecraft:weapon", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "item_damage_per_attack", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("1"))).setUnsetInfo(IntTag.valueOf(1)).getter(),
            "disable_blocking_for_seconds", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("0.0f"))).setUnsetInfo(FloatTag.valueOf(0f)).getter()
        ))).setIcon(Items.GOLDEN_SWORD));

        registerPathInfo("components.minecraft:writable_book_content", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "pages", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(//to_do test storage
                    DataType.ElementLiteral.of(NbtType.STRING),
                    DataType.CompoundStructured.of(Map.of(
                        "raw", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                        ),Map.of(
                        "filtered", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                    ))
                ).getter()
            )).getter()
        ))).setIcon(Items.WRITABLE_BOOK));

        registerPathInfo("components.minecraft:written_book_content", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "author", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter(),
            "title", PathInfo.create(//to_do test storage
                DataType.ElementLiteral.of(NbtType.STRING),
                DataType.CompoundStructured.of(Map.of(
                    "raw", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                    ),Map.of(
                    "filtered", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                ))
            ).getter()
            ),Map.of(
            "pages", PathInfo.create(DataType.ListUnordered.of(//TODO allow list of text components or list of objects with raw/filtered
                PathInfo.create(//to_do test storage
                    DataType.CompoundStructured.of(Map.of(
                        "raw", PathInfoGetter.of("text_component")
                        ),Map.of(
                        "filtered", PathInfoGetter.of("text_component")
                    ))
                ).getter()
            )).getter(),
            "generation", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0","1","2","3")))
                .setInfo(
                    Component.empty()
                    .append("0 - ").append(Component.translatable("book.generation.0"))
                    .append("\n1 - ").append(Component.translatable("book.generation.1"))
                    .append("\n2 - ").append(Component.translatable("book.generation.2"))
                    .append("\n3 - ").append(Component.translatable("book.generation.3"))
                ).getter(),
            "resolved", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter()
        ))).setIcon(Items.WRITTEN_BOOK));

        registerPathInfo("components.minecraft:axolotl/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_AXOLOTL_VARIANT)).setIcon(Items.AXOLOTL_SPAWN_EGG));

        registerPathInfo("components.minecraft:cat/collar", PathInfo.copyOf("dye_color").setIcon(Items.CAT_SPAWN_EGG));

        registerPathInfo("components.minecraft:cat/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_CAT_VARIANT)).setIcon(Items.CAT_SPAWN_EGG));

        registerPathInfo("components.minecraft:chicken/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_CHICKEN_VARIANT)).setIcon(Items.CHICKEN_SPAWN_EGG));

        registerPathInfo("components.minecraft:cow/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_COW_VARIANT)).setIcon(Items.COW_SPAWN_EGG));

        registerPathInfo("components.minecraft:fox/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_FOX_VARIANT)).setIcon(Items.FOX_SPAWN_EGG));

        registerPathInfo("components.minecraft:frog/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_FROG_VARIANT)).setIcon(Items.FROG_SPAWN_EGG));

        registerPathInfo("components.minecraft:horse/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_HORSE_VARIANT)).setIcon(Items.HORSE_SPAWN_EGG));

        registerPathInfo("components.minecraft:llama/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_LLAMA_VARIANT)).setIcon(Items.LLAMA_SPAWN_EGG));

        registerPathInfo("components.minecraft:mooshroom/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_MOOSHROOM_VARIANT)).setIcon(Items.MOOSHROOM_SPAWN_EGG));

        registerPathInfo("components.minecraft:painting/variant", PathInfo.create(//to_do stores in specified form
            DataType.CompoundStructured.of(Map.of(
                "asset_id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.ASSETS_TEXTURES_PAINTING)).setInfo("Resource location of a texture at `assets/<namespace>/textures/painting/<id>`").getter(),
                "width", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter(),
                "height", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter()
                ),Map.of(
                "title", PathInfoGetter.of("text_component"),
                "author", PathInfoGetter.of("text_component")
            )),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_PAINTING_VARIANT)
        ).setIcon(Items.PAINTING));

        registerPathInfo("components.minecraft:parrot/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_PARROT_VARIANT)).setIcon(Items.PARROT_SPAWN_EGG));

        registerPathInfo("components.minecraft:pig/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_PIG_VARIANT)).setIcon(Items.PIG_SPAWN_EGG));

        registerPathInfo("components.minecraft:rabbit/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_RABBIT_VARIANT)).setIcon(Items.RABBIT_SPAWN_EGG));

        registerPathInfo("components.minecraft:salmon/size", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_SALMON_VARIANT)).setIcon(Items.SALMON_SPAWN_EGG));

        registerPathInfo("components.minecraft:sheep/color", PathInfo.copyOf("dye_color").setIcon(Items.SHEEP_SPAWN_EGG));

        registerPathInfo("components.minecraft:shulker/color", PathInfo.copyOf("dye_color").setIcon(Items.SHULKER_SPAWN_EGG));

        registerPathInfo("components.minecraft:tropical_fish/base_color", PathInfo.copyOf("dye_color").setIcon(Items.TROPICAL_FISH_SPAWN_EGG));

        registerPathInfo("components.minecraft:tropical_fish/pattern", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_TROPICAL_FISH_VARIANT)).setIcon(Items.TROPICAL_FISH_SPAWN_EGG));

        registerPathInfo("components.minecraft:tropical_fish/pattern_color", PathInfo.copyOf("dye_color").setIcon(Items.TROPICAL_FISH_SPAWN_EGG));

        registerPathInfo("components.minecraft:villager/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_VILLAGER_TYPE)).setIcon(Items.VILLAGER_SPAWN_EGG));

        registerPathInfo("components.minecraft:wolf/collar", PathInfo.copyOf("dye_color").setIcon(Items.WOLF_SPAWN_EGG));

        registerPathInfo("components.minecraft:wolf/sound_variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_WOLF_SOUND_VARIANT)).setIcon(Items.WOLF_SPAWN_EGG));

        registerPathInfo("components.minecraft:wolf/variant", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_WOLF_VARIANT)).setIcon(Items.WOLF_SPAWN_EGG));

        registerPathInfo("text_component", PathInfo.create(//to_do list storage
            DataType.CompoundStructured.of(Map.of(
                "text", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("For 'text' type - string text").getter(),
                "keybind", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_KEYBIND)).setInfo("For 'keybind' type - string keybinding ID").getter(),
                "translate", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_TRANSLATION_KEY)).setInfo("For 'translatable' type - string translation key").getter(),
                "score", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                    "name", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("@s","*",FortytwoEdit.USERNAME))).setInfo("Name, selector, or * to show each player their own score").getter(),
                    "objective", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("Scoreboard objective").getter()
                ))).setInfo("For 'score' type - scoreboard entry to display (text component must be resolved)").getter(),
                "selector", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("@s","@p","@r","@a","@e"))).setInfo("For 'selector' type - entity selector to display (text component must be resolved)").getter(),
                "nbt", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("foo.bar[0]"))).setInfo("For 'nbt' type - NBT path of the data to display (text component must be resolved)").getter()
                ),Map.ofEntries(
                Map.entry("type", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("text","keybind","translatable","score","selector","nbt")))
                    .setInfo("The text component type to use. If unset, chooses based on the order:\n  text\n  translatable\n  keybind\n  score\n  selector\n  nbt")
                    .getter()),

                Map.entry("color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionGetter.newJoined(SuggestionHelper.LIST_FORMATTING_COLOR,SuggestionGetter.newInline("#000000"))))
                    .setInfo(Component.empty().append("Text color preset or RGB hex like '#000000'\n\nPresets:")
                    .append(Component.empty().append("\n  aqua").withStyle(ChatFormatting.AQUA))
                    .append(Component.empty().append("    black").withStyle(ChatFormatting.BLACK))
                    .append(Component.empty().append("    blue").withStyle(ChatFormatting.BLUE))
                    .append(Component.empty().append("    dark_aqua").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.empty().append("\n  dark_blue").withStyle(ChatFormatting.DARK_BLUE))
                    .append(Component.empty().append("    dark_gray").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.empty().append("    dark_green").withStyle(ChatFormatting.DARK_GREEN))
                    .append(Component.empty().append("    dark_purple").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.empty().append("\n  dark_red").withStyle(ChatFormatting.DARK_RED))
                    .append(Component.empty().append("    gold").withStyle(ChatFormatting.GOLD))
                    .append(Component.empty().append("    gray").withStyle(ChatFormatting.GRAY))
                    .append(Component.empty().append("    green").withStyle(ChatFormatting.GREEN))
                    .append(Component.empty().append("\n  light_purple").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.empty().append("    red").withStyle(ChatFormatting.RED))
                    .append(Component.empty().append("    white").withStyle(ChatFormatting.WHITE))
                    .append(Component.empty().append("    yellow").withStyle(ChatFormatting.YELLOW))
                    ).setUnsetInfo("Inherit from parent text").setFlag(PathFlag.TEXT_COMPONENT_FIELD_COLOR).getter()),
                Map.entry("shadow_color", PathInfo.copyOf("color_argb_int_or_list").getter()),
                Map.entry("font", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.ASSETS_FONT))
                    .setInfo(Component.empty().append("Resource location of a font at `assets/<namespace>/font/<id>`\n\nVanilla fonts:")
                    .append(Component.empty().append("\n  default - ").append(Component.empty().append("ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))))
                    .append(Component.empty().append("\n  uniform - ").append(Component.empty().append("ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("uniform")))))
                    .append(Component.empty().append("\n  alt - ").append(Component.empty().append("ABCDEFGHIJKLMNOPQRSTUVWXYZ").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt")))))
                    .append(Component.empty().append("\n  illageralt - ").append(Component.empty().append("ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789").withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("illageralt")))))
                    ).setUnsetInfo("Inherit from parent text").getter()),
                Map.entry("bold", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN))
                    .setInfo(Component.empty().append(Component.empty().append("true").withStyle(ChatFormatting.BOLD)).append("\nfalse")).setUnsetInfo("Inherit from parent text").getter()),
                Map.entry("italic", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN))
                    .setInfo(Component.empty().append(Component.empty().append("true").withStyle(ChatFormatting.ITALIC)).append("\nfalse")).setUnsetInfo("Inherit from parent text").getter()),
                Map.entry("underlined", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN))
                    .setInfo(Component.empty().append(Component.empty().append("true").withStyle(ChatFormatting.UNDERLINE)).append("\nfalse")).setUnsetInfo("Inherit from parent text").getter()),
                Map.entry("strikethrough", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN))
                    .setInfo(Component.empty().append(Component.empty().append("true").withStyle(ChatFormatting.STRIKETHROUGH)).append("\nfalse")).setUnsetInfo("Inherit from parent text").getter()),
                Map.entry("obfuscated", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN))
                    .setInfo(Component.empty().append(Component.empty().append("true").withStyle(ChatFormatting.OBFUSCATED)).append("\nfalse")).setUnsetInfo("Inherit from parent text").getter()),

                Map.entry("extra", PathInfo.create(DataType.ListUnordered.of(
                    PathInfoGetter.of("text_component")
                )).setInfo("List of text components to append and inherit the style of this one").getter()),
                Map.entry("insertion", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("String to be inserted into chat when shift-clicked (only works in chat)").getter()),
                Map.entry("click_event", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "action", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("run_command","suggest_command","copy_to_clipboard","open_url","change_page"))).setInfo("Action when clicked (only works in chat and written books)").getter() // open_file isn't listed as it is never represented in NBT and only used internally
                    ),Map.of(
                    "command", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("For 'run_command' action - string command with or without a slash\n\nFor 'suggest_command' action - string command starting with a slash, or chat message without a slash").getter(),
                    "value", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("For 'copy_to_clipboard' action - string to copy").getter(),
                    "url", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("For 'open_url' action - url to open").getter(),
                    "page", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).setInfo("For 'change_page' action - written book page to jump to").getter()
                ))).setInfo("Action when clicked (only works in chat and written books)").getter()),
                Map.entry("hover_event", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "action", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("show_text","show_item","show_entity"))).setInfo("Action when hovered (only works in chat and written books)").getter()
                    ),Map.of(
                    "value", PathInfo.copyOf("text_component").setInfo("For 'show_text' action - text component to show").getter(),
                    "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newJoined(SuggestionHelper.REGISTRY_ITEM,SuggestionHelper.REGISTRY_ENTITY_TYPE))).setInfo("For 'show_item' and 'show_entity' actions - ID of an item or entity type").getter(),
                    "count", PathInfo.copyOf("item_count").setInfo("For 'show_item' action - int count of the item").getter(),
                    "components", PathInfo.create(DataType.CompoundComponentsMap.create()).setInfo("For 'show_item' action - components on the item").getter(),
                    "name", PathInfo.copyOf("text_component").setInfo("For 'show_entity' action - custom name of the entity").getter(),
                    "uuid", PathInfo.create().setInfo("For 'show_entity' action - UUID of the entity (either in string hex form or int array form)").getter()
                ))).setInfo("Action when hovered (only works in chat and written books)").getter()),

                Map.entry("fallback", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).setInfo("For 'translatable' type - string to display when translation key not found").getter()),
                Map.entry("with", PathInfo.create(DataType.ListUnordered.of(
                    PathInfoGetter.of("text_component")
                )).setInfo("For 'translatable' type - list of text components for translate arguments").getter()),

                Map.entry("separator", PathInfo.copyOf("text_component").setInfo("For 'selector' and 'nbt' types - text components to display between entries").setUnsetInfo(StringTag.valueOf(", ")).getter()),

                Map.entry("source", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("block","entity","storage"))).setInfo("For 'nbt' type - data source to use. If unset, chooses based on the order:\n  entity\n  block\n  storage").getter()),
                Map.entry("storage", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("foo:bar"))).setInfo("For 'nbt' type - resource location of storage to read data of").getter()),
                Map.entry("block", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("0 0 0","~ ~ ~","^ ^ ^"))).setInfo("For 'nbt' type - coordinates of block to read data of (can be absolute, relative, or local)").getter()),
                Map.entry("entity", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newInline("@s","@p","@r","@a","@e"))).setInfo("For 'nbt' type - entity selector to read data of").getter()),
                Map.entry("interpret", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setInfo("For 'nbt' type - if the data should be interpreted as a text component instead of displaying the NBT directly").getter())

            )),
            DataType.ElementLiteral.of(NbtType.STRING),
            DataType.ListUnordered.of(
                PathInfoGetter.of("text_component")
            )
        ).setIcon(Items.NAME_TAG).setInfo("Text component").setFlag(PathFlag.TEXT_COMPONENT));

        registerPathInfo("effect_instance", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_STATUS_EFFECT)).setInfo("Effect ID").getter()
            ),Map.of(
            "amplifier", PathInfo.create(DataType.ElementLiteral.of(NbtType.BYTE,SuggestionGetter.newInlineSnbt("0b"))).setInfo("Byte representing a value of 0-255").setUnsetInfo(ByteTag.valueOf((byte)0)).getter(),
            "duration", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("-1","1"))).setInfo("Duration in ticks, or -1 for infinite").setUnsetInfo(IntTag.valueOf(1)).getter(),
            "ambient", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(false).getter(),
            "show_particles", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter(),
            "show_icon", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setUnsetInfo(true).getter()
        ))));
        
        registerPathInfo("sound_event_or_definition", PathInfo.create(//to_do stores in specified form
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_SOUND_EVENT),
            DataType.CompoundStructured.of(Map.of(
                "sound_id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                ),Map.of(
                "range", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            ))
        ).setIcon(Items.NOTE_BLOCK).setInfo("Sound event or new sound event definition"));

        registerPathInfo("uuid", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT_ARRAY,SuggestionGetter.newInlineSnbt("[I;0,0,0,0]",BlackMagick.nbtToSnbt(FortytwoEdit.UUID)))).setInfo("UUID int array with 4 integers"));

        registerPathInfo("item_count", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("1","16","64","99"))));

        registerPathInfo("dye_color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DYE_COLOR)).setInfo("Dye color"));

        registerPathInfo("color_rgb_int", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt("0",""+255*256*256+255*256+255))).setFlag(PathFlag.COLOR_RGB_INT).setInfo("RGB color in decimal form"));

        registerPathInfo("color_rgb_int_or_list", PathInfo.create(//to_do test storage
            DataType.ElementLiteral.of(NbtType.INT),
            DataType.ListStructured.of(
                SuggestionGetter.newInlineSnbt("[0.0d,0.0d,0.0d]"),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Red channel from 0.0 to 1.0").getter(),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Green channel from 0.0 to 1.0").getter(),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Blue channel from 0.0 to 1.0").getter()
            )
        ).setFlag(PathFlag.COLOR_RGB_INT_OR_LIST).setInfo("RGB color in decimal form, or a list of R,G,B values from 0.0-1.0"));

        registerPathInfo("color_argb_int_or_list", PathInfo.create(//to_do test storage
            DataType.ElementLiteral.of(NbtType.INT),
            DataType.ListStructured.of(
                SuggestionGetter.newInlineSnbt("[0.0d,0.0d,0.0d,1.0d]"),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Red channel from 0.0 to 1.0").getter(),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Green channel from 0.0 to 1.0").getter(),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE)).setInfo("Blue channel from 0.0 to 1.0").getter(),
                PathInfo.create(DataType.ElementLiteral.of(NbtType.DOUBLE,SuggestionGetter.newInlineSnbt("1.0d"))).setInfo("Alpha channel from 0.0 to 1.0").getter()
            )
        ).setFlag(PathFlag.COLOR_ARGB_INT_OR_LIST).setInfo("ARGB color in decimal form, or a list of R,G,B,A values from 0.0-1.0"));

        registerPathInfo("consume_effect", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "type", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_CONSUME_EFFECT_TYPE)).getter()
            ),Map.of(
            "effects", PathInfo.create().setInfo("For 'apply_effects' type - list of effect instances\n\nFor 'remove_effects' type - effect ID or a list of effect IDs").getter(),//to_do handle apply_effects vs remove_effects
            "probability", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("0.0f","1.0f"))).setInfo("For 'apply_effects' type - chance from 0.0 to 1.0 of all the effects to be applied").setUnsetInfo(FloatTag.valueOf(1f)).getter(),
            "diameter", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT,SuggestionGetter.newInlineSnbt("16.0f"))).setInfo("For 'teleport_randomly' type - diameter of teleportation region").setUnsetInfo(FloatTag.valueOf(16f)).getter(),
            "sound", PathInfo.copyOf("sound_event_or_definition").setInfo("For 'play_sound' type - sound event or new sound event definition").getter()
        ))));

        registerPathInfo("block_predicate_or_list", PathInfo.create(//to_do test storage
            DataType.ListUnordered.of(PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "blocks", PathInfoGetter.of("block_id_tag_or_list"),
                "nbt", PathInfo.create().getter(),//to_do compound or stringified compound
                "state", PathInfo.create().getter()
            ))).getter()),
            DataType.CompoundStructured.allOptional(Map.of(
                "blocks", PathInfoGetter.of("block_id_tag_or_list"),
                "nbt", PathInfo.create().getter(),//to_do compound or stringified compound
                "state", PathInfo.create().getter()
            ))
        ).setIcon(Items.CHAIN_COMMAND_BLOCK).setInfo("Block predicate (blocks/nbt/state) or list of block predicates"));

        registerPathInfo("block_id_tag_or_list", PathInfo.create(//to_do test storage
            DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_BLOCK)).getter()
            ),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newJoined(SuggestionHelper.REGISTRY_BLOCK,SuggestionHelper.DATA_TAG_BLOCK))
        ).setInfo("Block ID or tag, or a list of block IDs"));

        registerPathInfo("damage_type_id_tag_or_list", PathInfo.create(//to_do test storage
            DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_DAMAGE_TYPE)).getter()
            ),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newJoined(SuggestionHelper.DATA_DAMAGE_TYPE,SuggestionHelper.DATA_TAG_DAMAGE_TYPE))
        ).setInfo("Damage type ID or tag, or a list of damage type IDs"));

        registerPathInfo("entity_id_tag_or_list", PathInfo.create(//to_do test storage
            DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_ENTITY_TYPE)).getter()
            ),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newJoined(SuggestionHelper.REGISTRY_ENTITY_TYPE,SuggestionHelper.DATA_TAG_ENTITY_TYPE))
        ).setInfo("Entity type ID or tag, or a list of entity type IDs"));

        registerPathInfo("item_id_tag_or_list", PathInfo.create(//to_do test storage
            DataType.ListUnordered.of(
                PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.REGISTRY_ITEM)).getter()
            ),
            DataType.ElementLiteral.of(NbtType.STRING,SuggestionGetter.newJoined(SuggestionHelper.REGISTRY_ITEM,SuggestionHelper.DATA_TAG_ITEM))
        ).setInfo("Item ID or tag, or a list of item IDs"));

        registerPathInfo("pos_int_array", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT_ARRAY,SuggestionGetter.newInlineSnbt("[I;0,0,0]"))).setInfo("Block position represented by [I; X, Y, Z]"));

    }

    private static final Map<String, PathInfo> PATH_INFO_REF_MAP = Maps.newHashMap();

    protected static boolean testRegisteredPathInfo(String refKey) {
        if(PATH_INFO_REF_MAP.isEmpty())
            buildPathInfos();
        return PATH_INFO_REF_MAP.containsKey(refKey);
    }

    protected static PathInfo getRegisteredPathInfo(String refKey) {
        if(testRegisteredPathInfo(refKey))
            return PATH_INFO_REF_MAP.get(refKey);
        FortytwoEdit.logWarn("PathInfo map is missing a registered key: "+refKey);
        return PathInfo.EMPTY;
    }

    protected static PathInfoGetter registerPathInfo(String refKey, PathInfo pi) {
        if(PATH_INFO_REF_MAP.containsKey(refKey))
            FortytwoEdit.logWarn("Duplicate PathInfo registry key detected: "+refKey);
        PATH_INFO_REF_MAP.put(refKey, pi);
        return PathInfoGetter.of(refKey);
    }

    /**
     * Miscellaneous flags to denote a path as something specific
     */
    public enum PathFlag {

        NONE,

        TEXT_COMPONENT,
        TEXT_COMPONENT_ITALIC,
        TEXT_COMPONENT_LORE,
        TEXT_COMPONENT_FIELD_COLOR,

        COLOR_RGB_INT,
        COLOR_RGB_INT_OR_LIST,
        COLOR_ARGB_INT_OR_LIST,

        ATTRIBUTE_MODIFIER,
        BANNER_PATTERN,
        RARITY

    }

    public static abstract class PathInfo {

        protected ItemStack icon = null;
        protected Component info = null;
        protected Component infoUnset = null;
        protected PathFlag flag = null;
        protected boolean isEmpty = true;
        protected boolean isUnstructured = false;

        private static final PathInfo EMPTY = PathInfoDefinition.create();
        private static final PathInfo ANY = PathInfoDefinition.createUnstructured().setInfo("Unstructured NBT");
        
        protected static final String COMPOUND_KEY_REQUIRED_LABEL = "Required";
        protected static final String COMPOUND_KEY_OPTIONAL_LABEL = "Optional";
        protected static final String COMPOUND_KEY_UNKNOWN_LABEL = "Unknown";
        protected static final String COMPOUND_KEY_MISSING_NAMESPACE_LABEL = "Missing Namespace";
        public static final String[] COMPOUND_KEY_SPECIAL_LABELS = new String[]{
            COMPOUND_KEY_UNKNOWN_LABEL,
            COMPOUND_KEY_MISSING_NAMESPACE_LABEL,
            COMPOUND_KEY_REQUIRED_LABEL,
            COMPOUND_KEY_OPTIONAL_LABEL
        };

        private PathInfo() {}

        public Component getInfo() {
            return info;
        }

        public Component getUnsetInfo() {
            return infoUnset;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public PathFlag getFlag() {
            return flag != null ? flag : PathFlag.NONE;
        }

        public PathInfo setIcon(ItemStack icon) {
            this.icon = icon;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setIcon(Item icon) {
            return setIcon(new ItemStack(icon));
        }

        public PathInfo setInfo(Component info) {
            this.info = info;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setInfo(String info) {
            return setInfo(Component.nullToEmpty(info));
        }

        public PathInfo setFlag(PathFlag flag) {
            this.flag = flag;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setUnsetInfo(Tag defaultElement) {
            if(defaultElement != null) {
                this.infoUnset = Component.empty().append("Defaults to ")
                    .append(BlackMagick.nbtToColorfulText(defaultElement));
            }
            this.isEmpty = false;
            return this;
        }

        public PathInfo setUnsetInfo(String description) {
            this.infoUnset = Component.empty().append(description);
            this.isEmpty = false;
            return this;
        }

        public PathInfo setUnsetInfo(Component description) {
            this.infoUnset = description;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setUnsetInfo(boolean bool) {
            this.infoUnset = Component.empty().append("Defaults to ").append(Component.empty().append(bool ? "true" : "false").withStyle(ChatFormatting.GOLD));
            this.isEmpty = false;
            return this;
        }

        public boolean isEmpty() {
            return this.isEmpty;
        }

        public PathInfoGetter getter() {
            return PathInfoGetter.of(this);
        }

        public abstract PathInfo getNode(Tag element, PathNode node);

        public abstract List<NbtType> getNbtTypes();

        public abstract PathType getDefaultPathType();

        public abstract boolean hasPathType(PathType type);

        public abstract SuggestionGetter getSuggs();

        public abstract Set<String> getCompoundKeys(CompoundTag compound);

        public abstract String getCompoundKeyLabel(CompoundTag compound, String key);

        public abstract PathInfo getCompoundKeyInfo(CompoundTag compound, String key);

        public abstract PathInfo getListIndexInfo(int index);

        public abstract int getListSize();

        public abstract boolean showCompoundNewKeyRow();

        public static PathInfoDefinition create() {
            return PathInfoDefinition.create();
        }

        public static PathInfoDefinition create(PathInfoSupplier... info) {
            return PathInfoDefinition.create(info);
        }

        public static PathInfo copyOf(PathInfoGetter pi) {
            return PathInfoCopy.of(pi);
        }

        public static PathInfo copyOf(String refKey) {
            return PathInfoCopy.of(refKey);
        }

    }

    public static class PathInfoCopy extends PathInfo {

        private final PathInfoGetter pi;

        private PathInfoCopy(PathInfoGetter pi) {
            this.pi = pi;
            this.flag = null;
            this.isEmpty = false;
        }

        public static PathInfo of(PathInfoGetter pi) {
            return new PathInfoCopy(pi);
        }

        public static PathInfo of(String refKey) {
            return new PathInfoCopy(PathInfoGetter.of(refKey));
        }

        public PathInfo getNode(Tag element, PathNode node) {
            return pi.get().getNode(element, node);
        }

        public List<NbtType> getNbtTypes() {
            return pi.get().getNbtTypes();
        }

        public PathType getDefaultPathType() {
            return pi.get().getDefaultPathType();
        }

        public boolean hasPathType(PathType type) {
            return pi.get().hasPathType(type);
        }

        public SuggestionGetter getSuggs() {
            return pi.get().getSuggs();
        }

        public Set<String> getCompoundKeys(CompoundTag compound) {
            return pi.get().getCompoundKeys(compound);
        }

        public String getCompoundKeyLabel(CompoundTag compound, String key) {
            return pi.get().getCompoundKeyLabel(compound, key);
        }

        public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
            return pi.get().getCompoundKeyInfo(compound, key);
        }

        public PathInfo getListIndexInfo(int index) {
            return pi.get().getListIndexInfo(index);
        }

        public int getListSize() {
            return pi.get().getListSize();
        }

        public boolean showCompoundNewKeyRow() {
            return pi.get().showCompoundNewKeyRow();
        }

        @Override
        public Component getInfo() {
            return this.info != null ? this.info : pi.get().getInfo();
        }

        @Override
        public Component getUnsetInfo() {
            return this.infoUnset != null ? this.infoUnset : pi.get().getUnsetInfo();
        }

        @Override
        public ItemStack getIcon() {
            return this.icon != null ? this.icon : pi.get().getIcon();
        }

        @Override
        public PathFlag getFlag() {
            return this.flag != null ? this.flag : pi.get().getFlag();
        }

    }

    public static class PathInfoDefinition extends PathInfo {

        private PathInfoSupplierCompound compoundSupplier = null;
        private PathInfoSupplierList listSupplier = null;
        private PathInfoSupplierElement elementSupplier = null;
        private PathType defaultPathType = PathType.ELEMENT;
        private boolean setDefaultPathType = false;

        private SuggestionGetter allSuggs = SuggestionGetter.empty();
        private boolean cacheAllSuggs = false;
        private List<NbtType> allNbtTypes = Lists.newArrayList();
        private boolean cacheAllNbtTypes = false;

        private PathInfoDefinition() {}

        private PathInfoDefinition(boolean isUnstructured) {
            this.isUnstructured = isUnstructured;
            this.isEmpty = false;
        }

        public static PathInfoDefinition create() {
            return new PathInfoDefinition();
        }

        public static PathInfo createUnstructured() {
            return new PathInfoDefinition(true);
        }

        public static PathInfoDefinition create(PathInfoSupplier... info) {
            PathInfoDefinition newInfo = create();
            boolean foundCompound = false;
            boolean foundList = false;
            boolean foundElement = false;
            for(PathInfoSupplier pi : info) {
                if(pi instanceof PathInfoSupplierCompound) {
                    if(foundCompound)
                        FortytwoEdit.logWarn("Tried to add duplicate PathInfoSupplierCompound");
                    else
                        newInfo.setCompoundInfo((PathInfoSupplierCompound)pi);
                }
                else if(pi instanceof PathInfoSupplierList) {
                    if(foundList)
                        FortytwoEdit.logWarn("Tried to add duplicate PathInfoSupplierList");
                    else
                        newInfo.setListInfo((PathInfoSupplierList)pi);
                }
                else if(pi instanceof PathInfoSupplierElement) {
                    if(foundElement)
                        FortytwoEdit.logWarn("Tried to add duplicate PathInfoSupplierElement");
                    else
                        newInfo.setElementInfo((PathInfoSupplierElement)pi);
                }
                else {
                    FortytwoEdit.logWarn("Tried to add unknown PathInfoSupplier");
                }
            }
            return newInfo;
        }

        private void setCompoundInfo(PathInfoSupplierCompound info) {
            this.compoundSupplier = info;
            setSupplierInfo(info);
        }

        private void setListInfo(PathInfoSupplierList info) {
            this.listSupplier = info;
            setSupplierInfo(info);
        }

        private void setElementInfo(PathInfoSupplierElement info) {
            this.elementSupplier = info;
            setSupplierInfo(info);
        }

        private void setSupplierInfo(PathInfoSupplier info) {
            if(!this.setDefaultPathType) {
                this.defaultPathType = info.getPathType();
                this.setDefaultPathType = true;
            }
            this.isEmpty = false;
        }

        public PathInfo getNode(Tag element, PathNode node) {
            if(node.isKey) {
                if(element != null && element.getId()==Tag.TAG_COMPOUND)
                    return getCompoundKeyInfo((CompoundTag)element, node.key());
                return getCompoundKeyInfo(null, node.key());
            }
            else {
                return getListIndexInfo(node.index());
            }
        }

        public List<NbtType> getNbtTypes() {
            if(!cacheAllNbtTypes) {
                cacheAllNbtTypes = true;
                
                allNbtTypes.clear();
                if(this.isUnstructured || (elementSupplier != null && elementSupplier.getNbtType()==NbtType.ANY)) {
                    allNbtTypes.add(NbtType.ANY);
                }
                else {
                    if(compoundSupplier != null)
                        allNbtTypes.add(NbtType.COMPOUND);
                    if(listSupplier != null)
                        allNbtTypes.add(NbtType.LIST);
                    if(elementSupplier != null) {
                        allNbtTypes.remove(elementSupplier.getNbtType());
                        allNbtTypes.add(elementSupplier.getNbtType());
                    }
                }

            }
            return allNbtTypes;
        }

        public PathType getDefaultPathType() {
            return this.defaultPathType;
        }

        public boolean hasPathType(PathType type) {
            if(this.isUnstructured) {
                switch(type) {
                    case ELEMENT:
                    case STRING:
                    case COMPOUND:
                    case LIST:
                        return true;
                    case UNIT:
                    case BOOLEAN:
                        return false;
                }
            }
            if(compoundSupplier != null && compoundSupplier.getPathType() == type)
                return true;
            if(listSupplier != null && listSupplier.getPathType() == type)
                return true;
            if(elementSupplier != null && elementSupplier.getPathType() == type)
                return true;
            return false;
        }

        public SuggestionGetter getSuggs() {
            if(!cacheAllSuggs) {
                cacheAllSuggs = true;

                List<SuggestionGetter> suggsList = Lists.newArrayList();
                if(compoundSupplier != null) {
                    SuggestionGetter thisSuggs = compoundSupplier.getSuggs();
                    if(thisSuggs != null && !thisSuggs.isEmpty())
                        suggsList.add(thisSuggs);
                }
                if(listSupplier != null) {
                    SuggestionGetter thisSuggs = listSupplier.getSuggs();
                    if(thisSuggs != null && !thisSuggs.isEmpty())
                        suggsList.add(thisSuggs);
                }
                if(elementSupplier != null) {
                    SuggestionGetter thisSuggs = elementSupplier.getSuggs();
                    if(thisSuggs != null && !thisSuggs.isEmpty())
                        suggsList.add(thisSuggs);
                }
                if(!suggsList.isEmpty())
                    allSuggs = SuggestionGetter.newJoined(suggsList.toArray(new SuggestionGetter[0]));
            }
            return allSuggs;
        }

        public Set<String> getCompoundKeys(CompoundTag compound) {
            if(compoundSupplier != null)
                return compoundSupplier.getCompoundKeys(compound);
            return Set.of();
        }

        public String getCompoundKeyLabel(CompoundTag compound, String key) {
            if(compoundSupplier != null)
                return compoundSupplier.getCompoundKeyLabel(compound, key);
            return COMPOUND_KEY_UNKNOWN_LABEL;
        }

        public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
            if(this.isUnstructured)
                return PathInfo.ANY;
            if(compoundSupplier != null)
                return compoundSupplier.getCompoundKeyInfo(compound, key);
            return PathInfo.EMPTY;
        }

        public PathInfo getListIndexInfo(int index) {
            if(this.isUnstructured)
                return PathInfo.ANY;
            if(listSupplier != null)
                return listSupplier.getListIndexInfo(index);
            return PathInfo.EMPTY;
        }

        public int getListSize() {
            if(listSupplier != null)
                return listSupplier.getListSize();
            return -1;
        }

        public boolean showCompoundNewKeyRow() {
            if(this.isUnstructured)
                return true;
            if(compoundSupplier != null)
                return compoundSupplier.showNewKeyRow();
            return false;
        }

    }

    private interface PathInfoSupplier {

        public SuggestionGetter getSuggs();
        public PathType getPathType();

    }

    private static abstract class PathInfoSupplierCompound implements PathInfoSupplier {

        private static final SuggestionGetter SUGGS = SuggestionGetter.newInlineSnbt("{}");

        public SuggestionGetter getSuggs() {
            return SUGGS;
        }

        public PathType getPathType() {
            return PathType.COMPOUND;
        }

        public abstract Set<String> getCompoundKeys(CompoundTag compound);

        public abstract String getCompoundKeyLabel(CompoundTag compound, String key);

        public abstract PathInfo getCompoundKeyInfo(CompoundTag compound, String key);

        public boolean showNewKeyRow() {
            return false;
        }

    }

    private static abstract class PathInfoSupplierList implements PathInfoSupplier {

        private static final SuggestionGetter SUGGS = SuggestionGetter.newInlineSnbt("[]");

        public SuggestionGetter getSuggs() {
            return SUGGS;
        }

        public PathType getPathType() {
            return PathType.LIST;
        }

        public abstract PathInfo getListIndexInfo(int index);

        public int getListSize() {
            return -1;
        }

    }

    private static abstract class PathInfoSupplierElement implements PathInfoSupplier {

        public SuggestionGetter getSuggs() {
            return null;
        }

        public PathType getPathType() {
            return PathType.ELEMENT;
        }

        public abstract NbtType getNbtType();

    }

    private static class DataType {

        protected static class CompoundStructured extends PathInfoSupplierCompound {
    
            private final Set<String> allKeys;
            private final Set<String> requiredKeys;
            private final Set<String> optionalKeys;
            private final Map<String, PathInfoGetter> keyInfo;
    
            private CompoundStructured(Map<String, PathInfoGetter> required, Map<String, PathInfoGetter> optional) {
                this.keyInfo = Maps.newHashMap();
                this.requiredKeys = Sets.newHashSet();
                this.optionalKeys = Sets.newHashSet();
                if(optional != null)
                    for(String s : optional.keySet()) {
                        this.keyInfo.put(s, optional.get(s));
                        this.optionalKeys.add(s);
                    }
                if(required != null)
                    for(String s : required.keySet()) {
                        this.keyInfo.put(s, required.get(s));
                        this.requiredKeys.add(s);
                        if(this.optionalKeys.contains(s)) {
                            FortytwoEdit.logWarn("PathInfoComoundStructured tried to create 2 PathInfo's for key: "+s);
                            this.optionalKeys.remove(s);
                        }
                    }
                this.allKeys = Sets.newHashSet();
                this.allKeys.addAll(this.requiredKeys);
                this.allKeys.addAll(this.optionalKeys);
            }
    
            public static CompoundStructured of(Map<String, PathInfoGetter> required, Map<String, PathInfoGetter> optional) {
                return new CompoundStructured(required, optional);
            }
    
            public static CompoundStructured allOptional(Map<String, PathInfoGetter> optional) {
                return new CompoundStructured(null, optional);
            }
    
            public static CompoundStructured allRequired(Map<String, PathInfoGetter> required) {
                return new CompoundStructured(required, null);
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return this.allKeys;
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                if(requiredKeys.contains(key))
                    return PathInfo.COMPOUND_KEY_REQUIRED_LABEL;
                else if(optionalKeys.contains(key))
                    return PathInfo.COMPOUND_KEY_OPTIONAL_LABEL;
                return PathInfo.COMPOUND_KEY_UNKNOWN_LABEL;
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                if(this.keyInfo.containsKey(key))
                    return this.keyInfo.get(key).get();
                return PathInfo.EMPTY;
            }
    
        }

        protected static class CompoundUnstructured extends PathInfoSupplierCompound {
    
            private CompoundUnstructured() {}
    
            public static CompoundUnstructured create() {
                return new CompoundUnstructured();
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return Set.of();
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                return "Custom Data";
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                return PathInfo.ANY;
            }

            @Override
            public boolean showNewKeyRow() {
                return true;
            }
    
        }

        protected static class CompoundComponentsMap extends PathInfoSupplierCompound {

            boolean allowRemoved;
    
            private CompoundComponentsMap(boolean allowRemoved) {
                this.allowRemoved = allowRemoved;
            }
    
            public static CompoundComponentsMap create() {
                return new CompoundComponentsMap(true);
            }

            public static CompoundComponentsMap createOnlyPresent() {
                return new CompoundComponentsMap(false);
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return allowRemoved ? Set.of(SuggestionHelper.LIST_DATA_COMPONENT_TYPE_OR_REMOVED.getArray()) : Set.of(SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getArray());
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                if(getCompoundKeyInfo(compound,key).isEmpty()) {
                    return PathInfo.COMPOUND_KEY_UNKNOWN_LABEL;
                }
                if(!this.allowRemoved) {
                    if(key.startsWith("!"))
                        return PathInfo.COMPOUND_KEY_UNKNOWN_LABEL;
                    return PathInfo.COMPOUND_KEY_OPTIONAL_LABEL;
                }
                if(!key.contains(":"))
                    return PathInfo.COMPOUND_KEY_MISSING_NAMESPACE_LABEL;
                return key.startsWith("!") ? "Removed Components" : "Components";
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                if(key.startsWith("!")) {
                    if(this.allowRemoved) {
                        ResourceLocation componentId = BlackMagick.identifierOrNull(key.substring(1));
                        if(componentId != null && testRegisteredPathInfo("components."+componentId.toString())) {
                            ItemStack icon = getRegisteredPathInfo("components."+componentId.toString()).getIcon();
                            return PathInfo.create(DataType.Unit.create()).setInfo("Removed default component").setIcon(icon);
                        }
                        if(componentId != null && SuggestionHelper.LIST_DATA_COMPONENT_TYPE.getList().contains(componentId.toString()))
                            return PathInfo.create(DataType.Unit.create()).setInfo("Removed default component");
                    }
                }
                else {
                    ResourceLocation componentId = BlackMagick.identifierOrNull(key);
                    if(componentId != null && testRegisteredPathInfo("components."+componentId.toString()))
                        return getRegisteredPathInfo("components."+componentId.toString());
                }
                return PathInfo.EMPTY;
            }

        }

        protected static class CompoundEnchantmentsMap extends PathInfoSupplierCompound {
    
            private CompoundEnchantmentsMap() {}
    
            public static CompoundEnchantmentsMap create() {
                return new CompoundEnchantmentsMap();
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return Set.of(SuggestionHelper.DATA_ENCHANTMENT.getArray());
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                if(getCompoundKeyInfo(compound,key).isEmpty()) {
                    return PathInfo.COMPOUND_KEY_UNKNOWN_LABEL;
                }
                if(!key.contains(":"))
                    return PathInfo.COMPOUND_KEY_MISSING_NAMESPACE_LABEL;
                return "Enchantments";
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                ResourceLocation componentId = BlackMagick.identifierOrNull(key);
                if(componentId != null && SuggestionHelper.DATA_ENCHANTMENT.getList().contains(componentId.toString())) {
                    int max = SuggestionHelper.getEnchantmentMaxLevel(key);
                    if(max>0)
                        return PathInfo.create(DataType.ElementLiteral.of(NbtType.INT,SuggestionGetter.newInlineSnbt(BlackMagick.getIntRangeArray(1, max))))
                            .setInfo("Max level: "+max);
                }
                return PathInfo.EMPTY;
            }

        }

        protected static class CompoundMap extends PathInfoSupplierCompound {
    
            public final PathInfoGetter entryInfo;
            public final Set<String> keys;
    
            private CompoundMap(PathInfoGetter entry, Set<String> keys) {
                this.entryInfo = entry;
                this.keys = keys;
            }
    
            // public static CompoundMap of(PathInfoGetter entry) { to_do uncomment
            //     return new CompoundMap(entry, Set.of());
            // }
    
            public static CompoundMap of(PathInfoGetter entry, Set<String> keys) {
                return new CompoundMap(entry, keys);
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return keys;
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                return "Entries";
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                return entryInfo.get();
            }

            @Override
            public boolean showNewKeyRow() {
                return true;
            }

        }
    
        protected static class Unit extends PathInfoSupplierCompound {
    
            private Unit() {}
    
            public static Unit create() {
                return new Unit();
            }
    
            @Override
            public PathType getPathType() {
                return PathType.UNIT;
            }
    
            public Set<String> getCompoundKeys(CompoundTag compound) {
                return Set.of();
            }

            public String getCompoundKeyLabel(CompoundTag compound, String key) {
                return PathInfo.COMPOUND_KEY_UNKNOWN_LABEL;
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                return PathInfo.EMPTY;
            }
    
        }
    
        protected static class ListUnordered extends PathInfoSupplierList {
    
            private final PathInfoGetter entryInfo;
    
            private ListUnordered(PathInfoGetter entry) {
                this.entryInfo = entry;
            }
    
            public static ListUnordered of(PathInfoGetter entry) {
                return new ListUnordered(entry);
            }
    
            public PathInfo getListIndexInfo(int index) {
                return entryInfo.get();
            }
    
        }

        protected static class ListStructured extends PathInfoSupplierList {

            private final PathInfoGetter[] entryInfos;
            private final SuggestionGetter listSuggs;

            private ListStructured(SuggestionGetter suggs, PathInfoGetter... entryInfos) {
                this.entryInfos = entryInfos;
                this.listSuggs = SuggestionGetter.newJoined(suggs, super.getSuggs());
            }

            // public static ListStructured of(PathInfoGetter... entries) { to_do uncomment
            //     return new ListStructured(null, entries);
            // }

            public static ListStructured of(SuggestionGetter suggs, PathInfoGetter... entries) {
                return new ListStructured(suggs, entries);
            }

            public PathInfo getListIndexInfo(int index) {
                if(index >= 0 && index < entryInfos.length)
                    return entryInfos[index].get();
                return PathInfo.EMPTY;
            }

            @Override
            public SuggestionGetter getSuggs() {
                if(this.listSuggs != null)
                    return this.listSuggs;
                return super.getSuggs();
            }

            @Override
            public int getListSize() {
                return entryInfos.length;
            }

        }
    
        protected static class ElementLiteral extends PathInfoSupplierElement {
    
            private final NbtType nbtType;
            private final PathType type;
            private final SuggestionGetter SUGGS;
    
            private ElementLiteral(NbtType nbtType, PathType type, SuggestionGetter suggs) {
                this.nbtType = nbtType;
                this.type = type;
                this.SUGGS = suggs;
            }
    
            public static ElementLiteral of(NbtType nbtType) {
                return of(nbtType, SuggestionGetter.newInlineSnbt(nbtType.suggs()));
            }
    
            public static ElementLiteral of(NbtType nbtType, SuggestionGetter suggs) {
                PathType thisPathType = PathType.ELEMENT;
                if(nbtType==NbtType.BOOLEAN)
                    thisPathType = PathType.BOOLEAN;
                else if(nbtType==NbtType.STRING)
                    thisPathType = PathType.STRING;
                return new ElementLiteral(nbtType, thisPathType, suggs);
            }
    
            @Override
            public SuggestionGetter getSuggs() {
                return SUGGS;
            }
    
            @Override
            public PathType getPathType() {
                return type;
            }

            public NbtType getNbtType() {
                return nbtType;
            }
    
        }

    }

    public enum PathType {
        ELEMENT,
        STRING,

        UNIT,
        BOOLEAN,

        COMPOUND(true),
        LIST(true);

        private boolean isComplex;

        PathType() {
            this.isComplex = false;
        }

        PathType(boolean isComplex) {
            this.isComplex = isComplex;
        }

        public boolean isComplex() {
            return isComplex;
        }
    }

    public enum NbtType {

        BYTE("Byte", "0b",""+Byte.MIN_VALUE+"b",""+Byte.MAX_VALUE+"b"),
        SHORT("Short", "0s",""+Short.MIN_VALUE+"s",""+Short.MAX_VALUE+"s"),
        INT("Int", "0",""+Integer.MIN_VALUE,""+Integer.MAX_VALUE),
        LONG("Long", "0l",""+Long.MIN_VALUE+"l",""+Long.MAX_VALUE+"l"),
        DOUBLE("Double", "0.0d"),
        FLOAT("Float", "0.0f"),

        STRING("String", "\"\""),

        BYTE_ARRAY("Byte Array", "[B;]"),
        INT_ARRAY("Int Array", "[I;]"),
        LONG_ARRAY("Long Array", "[L;]"),

        COMPOUND("Compound", "{}"),
        LIST("List", "[]"),

        BOOLEAN("Boolean", "true","false"),
        
        ANY("Any", "0","\"\"","{}","[]");

        private final String label;
        private final String[] suggs;

        NbtType(String label, String... suggs) {
            this.label = label;
            this.suggs = suggs;
        }

        public String label() { return label; }
        public String[] suggs() { return suggs; }
    }

    public record PathNode(boolean isKey, String key, int index) {
        public static PathNode of(String key) {
            return new PathNode(true, key, -1);
        }
        public static PathNode of(int index) {
            return new PathNode(false, null, index);
        }

        public String getCompoundKey() {
            if(isKey) {
                return BlackMagick.validCompoundKey(key);
            }
            return null;
        }
        public String getPathKey() {
            if(isKey) {
                return BlackMagick.validPathKey(key);
            }
            return null;
        }

        public static String resolvePath(PathNode[] path) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(PathNode node : path) {
                if(first)
                    first = false;
                else if(node.isKey())
                    sb.append(".");
                
                if(node.isKey())
                    sb.append(node.getPathKey());
                else
                    sb.append("["+node.index()+"]");
            }
            return sb.toString();
        }

        public static PathNode[] parsePath(String path) {
            if(path == null || path.isEmpty() || path.equals("{}"))
                return new PathNode[0];

            List<PathNode> nodeList = Lists.newArrayList();
            String edit = path;
            boolean valid = true;
            while(!edit.isEmpty() && valid) {
                valid = false;
                if(!nodeList.isEmpty() && edit.charAt(0)=='[') {
                    if(edit.contains("]")) {
                        String thisNode = edit.substring(1,edit.indexOf("]"));
                        edit = edit.substring(edit.indexOf("]")+1);
                        try {
                            int index = Integer.parseInt(thisNode);
                            if(index>=0) {
                                nodeList.add(PathNode.of(index));
                                valid = true;
                                continue;
                            }
                        } catch(Exception ex) {}
                    }
                }
                else if(nodeList.isEmpty() || edit.charAt(0)=='.') {
                    if(nodeList.isEmpty() && edit.charAt(0)=='.') {
                        valid = false;
                        break;
                    }
                    if(edit.charAt(0)=='.') {
                        edit = edit.substring(1);
                        if(edit.isEmpty()) {
                            valid = true;
                            break;
                        }
                    }
                    if(edit.charAt(0)=='"' || edit.charAt(0)=='\'') {
                        char quoteChar = edit.charAt(0);
                        edit = edit.substring(1);
                        StringBuilder thisKey = new StringBuilder();
                        boolean repeatSearch = true;
                        while(!edit.isEmpty() && repeatSearch) {
                            repeatSearch = false;
                            if(edit.charAt(0)=='\\') {
                                if(edit.length()>1) {
                                    thisKey.append(edit.charAt(1));
                                    edit = edit.substring(2);
                                    repeatSearch = true;
                                }
                            }
                            else if(edit.charAt(0)==quoteChar) {
                                if(thisKey.length()>0) {
                                    nodeList.add(PathNode.of(thisKey.toString()));
                                    edit = edit.substring(1);
                                    valid = true;
                                    repeatSearch = false;
                                }
                            }
                            else {
                                thisKey.append(edit.charAt(0));
                                edit = edit.substring(1);
                                repeatSearch = true;
                            }
                        }
                    }
                    else {
                        StringBuilder thisKey = new StringBuilder();
                        while(!edit.isEmpty() && isAllowedInUnquotedName(edit.charAt(0))) {
                            thisKey.append(edit.charAt(0));
                            edit = edit.substring(1);
                        }
                        if(thisKey.length()>0) {
                            nodeList.add(PathNode.of(thisKey.toString()));
                            valid = true;
                        }
                    }
                }
            }

            if(valid && !nodeList.isEmpty())
                return nodeList.toArray(new PathNode[0]);
            
            return null;
        }

        /**
         * Modified from {@link net.minecraft.commands.arguments.NbtPathArgument#isAllowedInUnquotedName}
         */
        private static boolean isAllowedInUnquotedName(char c) {
            return c != ' ' && c != '"' && c != '\'' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
        }
    }

    public record PathInfoGetter(PathInfo inlined, String refKey) {

        public static PathInfoGetter of(PathInfo inlined) {
            return new PathInfoGetter(inlined, null);
        }

        public static PathInfoGetter of(String refKey) {
            return new PathInfoGetter(null, refKey);
        }

        public PathInfo get() {
            if(inlined != null)
                return inlined;
            return getRegisteredPathInfo(refKey);
        }

    }
    
}
