package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import baphomethlabs.fortytwoedit.SuggestionHelper.KeyGetter;
import baphomethlabs.fortytwoedit.SuggestionHelper.SuggestionGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * <p> Class containing static methods related to nbt data structures </p>
 * <p> These often change every update and must be kept up to date manually </p>
 * <p> Check the following for help: </p>
 * <ul>
 *  <li> {@link net.minecraft.core.component.DataComponents} </li>
 *  <li> https://minecraft.wiki/w/Item_format </li>
 *  <li> https://minecraft.wiki/w/Entity_format </li>
 *  <li> https://minecraft.wiki/w/Chunk_format#Block_entity_format </li>
 * </ul>
 */
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

    private static void buildPathInfos() {
        PATH_INFO_REF_MAP.clear();

        registerPathInfo("item_stack", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_ITEM)).setIcon(Items.STONE).getter()
            ),Map.of(
            "count", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).setIcon(Items.STONE).getter(),
            "components", PathInfoGetter.of("components")
        ))).setIcon(Items.STONE));

        registerPathInfo("components", PathInfo.create(DataType.CompoundStructured.allOptional(Map.ofEntries(

            Map.entry("minecraft:attribute_modifiers", registerPathInfo("components/attribute_modifiers", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "type", PathInfo.create().getter(),
                    "id", PathInfo.create().getter(),
                    "amount", PathInfo.create().getter(),
                    "operation", PathInfo.create().getter()
                    ),Map.of(
                    "slot", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.DIAMOND_SWORD))),

            Map.entry("minecraft:banner_patterns", registerPathInfo("components/banner_patterns", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                    "color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DYE_COLOR)).getter(),
                    "pattern", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_BANNER_PATTERN)).getter()
                ))).getter()
            )).setIcon(Items.WHITE_BANNER))),

            Map.entry("minecraft:base_color", registerPathInfo("components/base_color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DYE_COLOR)).setIcon(Items.SHIELD))),

            Map.entry("minecraft:bees", registerPathInfo("components/bees", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "min_ticks_in_hive", PathInfo.create().getter(),
                    "ticks_in_hive", PathInfo.create().getter()
                    ),Map.of(
                    "entity_data", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.BEE_NEST))),

            Map.entry("minecraft:block_entity_data", registerPathInfo("components/block_entity_data", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_BLOCK_ENTITY_TYPE)).getter()
                ),Map.of(
                "front_text", PathInfo.create().getter()
            ))).setIcon(Items.SPAWNER))),

            Map.entry("minecraft:block_state", registerPathInfo("components/block_state", PathInfo.create().setIcon(Items.PALE_OAK_STAIRS))),

            Map.entry("minecraft:blocks_attacks", registerPathInfo("components/blocks_attacks", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "block_delay_seconds", PathInfo.create().getter(),
                "disable_cooldown_scale", PathInfo.create().getter(),
                "damage_reductions", PathInfo.create().getter(),
                "item_damage", PathInfo.create().getter(),
                "block_sound", PathInfo.create().getter(),
                "disable_sound", PathInfo.create().getter(),
                "bypassed_by", PathInfo.create().getter()
            ))).setIcon(Items.SHIELD))),

            Map.entry("minecraft:break_sound", PathInfoGetter.of("sound_event_or_definition")),

            Map.entry("minecraft:bucket_entity_data", registerPathInfo("components/bucket_entity_data", PathInfo.create(DataType.CompoundStructured.allOptional(Map.ofEntries(
                Map.entry("NoAI", PathInfo.create().getter()),
                Map.entry("Silent", PathInfo.create().getter()),
                Map.entry("NoGravity", PathInfo.create().getter()),
                Map.entry("Glowing", PathInfo.create().getter()),
                Map.entry("Invulnerable", PathInfo.create().getter()),
                Map.entry("Health", PathInfo.create().getter()),
                Map.entry("Age", PathInfo.create().getter()),
                Map.entry("Variant", PathInfo.create().getter()),
                Map.entry("HuntingCooldown", PathInfo.create().getter()),
                Map.entry("BucketVariantTag", PathInfo.create().getter()),
                Map.entry("type", PathInfo.create().getter())
            ))).setIcon(Items.TROPICAL_FISH_BUCKET))),

            Map.entry("minecraft:bundle_contents", registerPathInfo("components/bundle_contents", PathInfo.create(DataType.ListUnordered.of(PathInfoGetter.of("item_stack"))).setIcon(Items.BUNDLE))),

            Map.entry("minecraft:can_break", PathInfoGetter.of("block_predicate_or_list")),

            Map.entry("minecraft:can_place_on", PathInfoGetter.of("block_predicate_or_list")),

            Map.entry("minecraft:charged_projectiles", registerPathInfo("components/charged_projectiles", PathInfo.create(DataType.ListUnordered.of(PathInfoGetter.of("item_stack"))).setIcon(Items.CROSSBOW))),

            Map.entry("minecraft:consumable", registerPathInfo("components/consumable", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "consume_seconds", PathInfo.create().getter(),
                "animation", PathInfo.create().getter(),
                "sound", PathInfoGetter.of("sound_event_or_definition"),
                "has_consume_particles", PathInfo.create().getter(),
                "on_consume_effects", PathInfo.create().getter()
            ))).setIcon(Items.GOLDEN_APPLE))),

            Map.entry("minecraft:container", registerPathInfo("components/container", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                    "item", PathInfoGetter.of("item_stack"),
                    "slot", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.SHULKER_BOX))),

            Map.entry("minecraft:container_loot", registerPathInfo("components/container_loot", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "loot_table", PathInfo.create().getter()
                ),Map.of(
                "seed", PathInfo.create().getter()
            ))).setIcon(Items.CHEST))),

            Map.entry("minecraft:custom_data", registerPathInfo("components/custom_data", PathInfo.create().setIcon(Items.COMMAND_BLOCK))),

            Map.entry("minecraft:custom_model_data", registerPathInfo("components/custom_model_data", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
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
                    PathInfo.create().getter()
                )).getter()
            ))).setIcon(Items.COMMAND_BLOCK))),

            Map.entry("minecraft:custom_name", PathInfo.copyOf("text_component").setFlag(PathFlag.TEXT_COMPONENT_ITALIC).getter()),

            Map.entry("minecraft:damage", registerPathInfo("components/damage", PathInfo.create().setIcon(Items.DIAMOND_PICKAXE))),

            Map.entry("minecraft:damage_resistant", registerPathInfo("components/damage_resistant", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "types", PathInfo.create().getter()
            ))).setIcon(Items.NETHERITE_INGOT))),

            Map.entry("minecraft:debug_stick_state", registerPathInfo("components/debug_stick_state", PathInfo.create().setIcon(Items.DEBUG_STICK))),

            Map.entry("minecraft:death_protection", registerPathInfo("components/death_protection", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "death_effects", PathInfo.create().getter()
            ))).setIcon(Items.TOTEM_OF_UNDYING))),

            Map.entry("minecraft:dyed_color", registerPathInfo("components/dyed_color", PathInfo.create().setIcon(Items.LEATHER_CHESTPLATE))),

            Map.entry("minecraft:enchantable", registerPathInfo("components/enchantable", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "value", PathInfo.create().getter()
            ))).setIcon(Items.ENCHANTED_BOOK))),

            Map.entry("minecraft:enchantment_glint_override", registerPathInfo("components/enchantment_glint_override", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).setIcon(Items.ENCHANTED_BOOK))),

            Map.entry("minecraft:enchantments", registerPathInfo("components/enchantments", PathInfo.create().setIcon(Items.ENCHANTED_BOOK))),

            Map.entry("minecraft:entity_data", registerPathInfo("components/entity_data", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_ENTITY_TYPE)).getter()
                ),Map.of(
                "Tags", PathInfo.create().getter()
            ))).setIcon(Items.ARMOR_STAND))),

            Map.entry("minecraft:equippable", registerPathInfo("components/equippable", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "slot", PathInfo.create().getter()
                ),Map.of(
                "equip_sound", PathInfoGetter.of("sound_event_or_definition"),
                "asset_id", PathInfo.create().getter(),
                "allowed_entities", PathInfo.create().getter(),
                "dispensable", PathInfo.create().getter(),
                "swappable", PathInfo.create().getter(),
                "damage_on_hurt", PathInfo.create().getter(),
                "equip_on_interact", PathInfo.create().getter(),
                "camera_overlay", PathInfo.create().getter()
            ))).setIcon(Items.DIAMOND_CHESTPLATE))),

            Map.entry("minecraft:firework_explosion", registerPathInfo("components/firework_explosion", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "shape", PathInfo.create().getter()
                ),Map.of(
                "colors", PathInfo.create().getter(),
                "fade_colors", PathInfo.create().getter(),
                "has_trail", PathInfo.create().getter(),
                "has_twinkle", PathInfo.create().getter()
            ))).setIcon(Items.FIREWORK_STAR))),

            Map.entry("minecraft:fireworks", registerPathInfo("components/fireworks", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "explosions", PathInfo.create(DataType.ListUnordered.of(
                    PathInfoGetter.of("components/firework_explosion")
                )).getter(),
                "flight_duration", PathInfo.create().getter()
            ))).setIcon(Items.FIREWORK_ROCKET))),

            Map.entry("minecraft:food", registerPathInfo("components/food", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "nutrition", PathInfo.create().getter(),
                "saturation", PathInfo.create().getter()
                ),Map.of(
                "can_always_eat", PathInfo.create().getter()
            ))).setIcon(Items.APPLE))),

            Map.entry("minecraft:glider", registerPathInfo("components/glider", PathInfo.create(DataType.Unit.create()).setIcon(Items.ELYTRA))),

            Map.entry("minecraft:instrument", registerPathInfo("components/instrument", PathInfo.create().setIcon(Items.GOAT_HORN))),

            Map.entry("minecraft:intangible_projectile", registerPathInfo("components/intangible_projectile", PathInfo.create().setIcon(Items.ARROW))),

            Map.entry("minecraft:item_model", registerPathInfo("components/item_model", PathInfo.create().setIcon(Items.STONE))),

            Map.entry("minecraft:item_name", PathInfoGetter.of("text_component")),

            Map.entry("minecraft:jukebox_playable", registerPathInfo("components/jukebox_playable", PathInfo.create().setIcon(Items.MUSIC_DISC_13))),

            Map.entry("minecraft:lock", registerPathInfo("components/lock", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "items", PathInfo.create().getter(),
                "count", PathInfo.create().getter(),
                "components", PathInfo.create().getter(),
                "predicates", PathInfo.create().getter()
            ))).setIcon(Items.CHEST))),

            Map.entry("minecraft:lodestone_tracker", registerPathInfo("components/lodestone_tracker", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "target", PathInfo.create().getter(),
                "tracked", PathInfo.create().getter()
            ))).setIcon(Items.COMPASS))),

            Map.entry("minecraft:lore", registerPathInfo("components/lore", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.copyOf("text_component").setFlag(PathFlag.TEXT_COMPONENT_LORE).getter()
            )).setIcon(Items.NAME_TAG))),

            Map.entry("minecraft:map_color", registerPathInfo("components/map_color", PathInfo.create().setIcon(Items.FILLED_MAP))),

            Map.entry("minecraft:map_decorations", registerPathInfo("components/map_decorations", PathInfo.create().setIcon(Items.FILLED_MAP))),

            Map.entry("minecraft:map_id", registerPathInfo("components/map_id", PathInfo.create().setIcon(Items.FILLED_MAP))),

            Map.entry("minecraft:max_damage", registerPathInfo("components/max_damage", PathInfo.create().setIcon(Items.DIAMOND_PICKAXE))),

            Map.entry("minecraft:max_stack_size", registerPathInfo("components/max_stack_size", PathInfo.create().setIcon(Items.STONE))),

            Map.entry("minecraft:note_block_sound", registerPathInfo("components/note_block_sound", PathInfo.create().setIcon(Items.PLAYER_HEAD))),

            Map.entry("minecraft:ominous_bottle_amplifier", registerPathInfo("components/ominous_bottle_amplifier", PathInfo.create().setIcon(Items.OMINOUS_BOTTLE))),

            Map.entry("minecraft:pot_decorations", registerPathInfo("components/pot_decorations", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create().getter()
            )).setIcon(Items.DECORATED_POT))),

            Map.entry("minecraft:potion_contents", registerPathInfo("components/potion_contents", PathInfo.create(
                DataType.CompoundStructured.allOptional(Map.of(
                    "potion", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter(),
                    "custom_color", PathInfo.create().getter(),
                    "custom_name", PathInfo.create().getter(),
                    "custom_effects", PathInfo.create().getter()
                )),
                DataType.ElementLiteral.of(NbtType.STRING)
            ).setIcon(Items.POTION))),

            Map.entry("minecraft:potion_duration_scale", registerPathInfo("components/potion_duration_scale", PathInfo.create().setIcon(Items.SPLASH_POTION))),

            Map.entry("minecraft:profile", registerPathInfo("components/profile", PathInfo.create(
                DataType.CompoundStructured.allOptional(Map.of(
                    "name", PathInfo.create().getter(),
                    "id", PathInfo.create().getter(),
                    "properties", PathInfo.create().getter()
                )),
                DataType.ElementLiteral.of(NbtType.STRING)
            ).setIcon(Items.PLAYER_HEAD))),

            Map.entry("minecraft:provides_banner_patterns", registerPathInfo("components/provides_banner_patterns", PathInfo.create().setIcon(Items.CREEPER_BANNER_PATTERN))),

            Map.entry("minecraft:provides_trim_material", registerPathInfo("components/provides_trim_material", PathInfo.create().setIcon(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE))),

            Map.entry("minecraft:rarity", registerPathInfo("components/rarity", PathInfo.create().setIcon(Items.STONE))),

            Map.entry("minecraft:recipes", registerPathInfo("components/recipes", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create().getter()
            )).setIcon(Items.KNOWLEDGE_BOOK))),

            Map.entry("minecraft:repairable", registerPathInfo("components/repairable", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "items", PathInfo.create().getter()
            ))).setIcon(Items.ANVIL))),

            Map.entry("minecraft:repair_cost", registerPathInfo("components/repair_cost", PathInfo.create().setIcon(Items.ANVIL))),

            Map.entry("minecraft:stored_enchantments", registerPathInfo("components/stored_enchantments", PathInfo.create().setIcon(Items.ENCHANTED_BOOK))),

            Map.entry("minecraft:suspicious_stew_effects", registerPathInfo("components/suspicious_stew_effects", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "id", PathInfo.create().getter()
                    ),Map.of(
                    "duration", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.SUSPICIOUS_STEW))),

            Map.entry("minecraft:tool", registerPathInfo("components/tool", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "rules", PathInfo.create().getter()
                    ),Map.of(
                    "default_mining_speed", PathInfo.create().getter(),
                    "damage_per_block", PathInfo.create().getter(),
                    "can_destroy_blocks_in_creative", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.DIAMOND_PICKAXE))),

            Map.entry("minecraft:tooltip_display", registerPathInfo("components/tooltip_display", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "hide_tooltip", PathInfo.create().getter(),
                "hidden_components", PathInfo.create(DataType.ListUnordered.of(
                    PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                )).getter()
            ))).setIcon(Items.COMMAND_BLOCK))),

            Map.entry("minecraft:tooltip_style", registerPathInfo("components/tooltip_style", PathInfo.create().setIcon(Items.COMMAND_BLOCK))),

            Map.entry("minecraft:trim", registerPathInfo("components/trim", PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                "pattern", PathInfo.create().getter(),
                "material", PathInfo.create().getter()
            ))).setIcon(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE))),

            Map.entry("minecraft:unbreakable", registerPathInfo("components/unbreakable", PathInfo.create(DataType.Unit.create()).setIcon(Items.DIAMOND_PICKAXE))),

            Map.entry("minecraft:use_cooldown", registerPathInfo("components/use_cooldown", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "seconds", PathInfo.create().getter()
                ),Map.of(
                "cooldown_group", PathInfo.create().getter()
            ))).setIcon(Items.ENDER_PEARL))),

            Map.entry("minecraft:use_remainder", PathInfoGetter.of("item_stack")),

            Map.entry("minecraft:weapon", registerPathInfo("components/weapon", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "item_damage_per_attack", PathInfo.create().getter(),
                "disable_blocking_for_seconds", PathInfo.create().getter()
            ))).setIcon(Items.GOLDEN_SWORD))),

            Map.entry("minecraft:writable_book_content", registerPathInfo("components/writable_book_content", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "pages", PathInfo.create(DataType.ListUnordered.of(
                    PathInfo.create(
                        DataType.ElementLiteral.of(NbtType.STRING),
                        DataType.CompoundStructured.of(Map.of(
                            "raw", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                            ),Map.of(
                            "filtered", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                        ))
                    ).getter()
                )).getter()
            ))).setIcon(Items.WRITABLE_BOOK))),

            Map.entry("minecraft:written_book_content", registerPathInfo("components/written_book_content", PathInfo.create(DataType.CompoundStructured.of(Map.of(
                "author", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter(),
                "title", PathInfo.create(
                    DataType.ElementLiteral.of(NbtType.STRING),
                    DataType.CompoundStructured.of(Map.of(
                        "raw", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                        ),Map.of(
                        "filtered", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                    ))
                ).getter()
                ),Map.of(
                "pages", PathInfo.create(DataType.ListUnordered.of(//to_do allow list of text components or list of objects with raw/filtered
                    PathInfo.create(
                        DataType.CompoundStructured.of(Map.of(
                            "raw", PathInfoGetter.of("text_component")
                            ),Map.of(
                            "filtered", PathInfoGetter.of("text_component")
                        ))
                    ).getter()
                )).getter(),
                "generation", PathInfo.create().getter(),
                "resolved", PathInfo.create().getter()
            ))).setIcon(Items.WRITTEN_BOOK))),


            Map.entry("minecraft:axolotl/variant", registerPathInfo("components/axolotl/variant", PathInfo.create().setIcon(Items.AXOLOTL_SPAWN_EGG))),

            Map.entry("minecraft:cat/collar", registerPathInfo("components/cat/collar", PathInfo.create().setIcon(Items.CAT_SPAWN_EGG))),

            Map.entry("minecraft:cat/variant", registerPathInfo("components/cat/variant", PathInfo.create().setIcon(Items.CAT_SPAWN_EGG))),

            Map.entry("minecraft:chicken/variant", registerPathInfo("components/chicken/variant", PathInfo.create().setIcon(Items.CHICKEN_SPAWN_EGG))),

            Map.entry("minecraft:cow/variant", registerPathInfo("components/cow/variant", PathInfo.create().setIcon(Items.COW_SPAWN_EGG))),

            Map.entry("minecraft:fox/variant", registerPathInfo("components/fox/variant", PathInfo.create().setIcon(Items.FOX_SPAWN_EGG))),

            Map.entry("minecraft:frog/variant", registerPathInfo("components/frog/variant", PathInfo.create().setIcon(Items.FROG_SPAWN_EGG))),

            Map.entry("minecraft:horse/variant", registerPathInfo("components/horse/variant", PathInfo.create().setIcon(Items.HORSE_SPAWN_EGG))),

            Map.entry("minecraft:llama/variant", registerPathInfo("components/llama/variant", PathInfo.create().setIcon(Items.LLAMA_SPAWN_EGG))),

            Map.entry("minecraft:mooshroom/variant", registerPathInfo("components/mooshroom/variant", PathInfo.create().setIcon(Items.MOOSHROOM_SPAWN_EGG))),

            Map.entry("minecraft:parrot/variant", registerPathInfo("components/parrot/variant", PathInfo.create().setIcon(Items.PARROT_SPAWN_EGG))),

            Map.entry("minecraft:painting/variant", registerPathInfo("components/painting/variant", PathInfo.create(
                DataType.CompoundStructured.of(Map.of(
                    "asset_id", PathInfo.create().getter(),
                    "width", PathInfo.create().getter(),
                    "height", PathInfo.create().getter()
                    ),Map.of(
                    "title", PathInfoGetter.of("text_component"),
                    "author", PathInfoGetter.of("text_component")
                )),
                DataType.ElementLiteral.of(NbtType.STRING)
            ).setIcon(Items.PAINTING))),

            Map.entry("minecraft:pig/variant", registerPathInfo("components/pig/variant", PathInfo.create().setIcon(Items.PIG_SPAWN_EGG))),

            Map.entry("minecraft:rabbit/variant", registerPathInfo("components/rabbit/variant", PathInfo.create().setIcon(Items.RABBIT_SPAWN_EGG))),

            Map.entry("minecraft:salmon/size", registerPathInfo("components/salmon/size", PathInfo.create().setIcon(Items.SALMON_SPAWN_EGG))),

            Map.entry("minecraft:sheep/color", registerPathInfo("components/sheep/color", PathInfo.create().setIcon(Items.SHEEP_SPAWN_EGG))),

            Map.entry("minecraft:shulker/color", registerPathInfo("components/shulker/color", PathInfo.create().setIcon(Items.SHULKER_SPAWN_EGG))),

            Map.entry("minecraft:tropical_fish/base_color", registerPathInfo("components/tropical_fish/base_color", PathInfo.create().setIcon(Items.TROPICAL_FISH_SPAWN_EGG))),

            Map.entry("minecraft:tropical_fish/pattern", registerPathInfo("components/tropical_fish/pattern", PathInfo.create().setIcon(Items.TROPICAL_FISH_SPAWN_EGG))),

            Map.entry("minecraft:tropical_fish/pattern_color", registerPathInfo("components/tropical_fish/pattern_color", PathInfo.create().setIcon(Items.TROPICAL_FISH_SPAWN_EGG))),

            Map.entry("minecraft:villager/variant", registerPathInfo("components/villager/variant", PathInfo.create().setIcon(Items.VILLAGER_SPAWN_EGG))),

            Map.entry("minecraft:wolf/collar", registerPathInfo("components/wolf/collar", PathInfo.create().setIcon(Items.WOLF_SPAWN_EGG))),

            Map.entry("minecraft:wolf/sound_variant", registerPathInfo("components/wolf/sound_variant", PathInfo.create().setIcon(Items.WOLF_SPAWN_EGG))),

            Map.entry("minecraft:wolf/variant", registerPathInfo("components/wolf/variant", PathInfo.create().setIcon(Items.WOLF_SPAWN_EGG)))

        ))).setIcon(Items.STONE));

        registerPathInfo("text_component", PathInfo.create(
            DataType.CompoundStructured.of(Map.of(
                "text", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                ),Map.of(
                "color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.LIST_FORMATTING_COLOR)).getter(),
                "font", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.ASSETS_FONT)).getter(),
                "bold", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
                "italic", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
                "underlined", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
                "strikethrough", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
                "obfuscated", PathInfo.create(DataType.ElementLiteral.of(NbtType.BOOLEAN)).getter(),
                "extra", PathInfo.create(DataType.ListUnordered.of(PathInfoGetter.of("text_component"))).getter()
            )),
            DataType.ElementLiteral.of(NbtType.STRING),
            DataType.ListUnordered.of(PathInfoGetter.of("text_component"))
        ).setIcon(Items.NAME_TAG).setFlag(PathFlag.TEXT_COMPONENT));
        
        registerPathInfo("sound_event_or_definition", PathInfo.create(
            DataType.ElementLiteral.of(NbtType.STRING),
            DataType.CompoundStructured.of(Map.of(
                "sound_id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING)).getter()
                ),Map.of(
                "range", PathInfo.create(DataType.ElementLiteral.of(NbtType.FLOAT)).getter()
            ))
        ).setIcon(Items.NOTE_BLOCK));

        registerPathInfo("block_predicate_or_list", PathInfo.create(//to_do reference static final compound supplier
            DataType.ListUnordered.of(PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
                "blocks", PathInfo.create().getter(),
                "nbt", PathInfo.create().getter(),
                "state", PathInfo.create().getter()
            ))).getter()),
            DataType.CompoundStructured.allOptional(Map.of(
                "blocks", PathInfo.create().getter(),
                "nbt", PathInfo.create().getter(),
                "state", PathInfo.create().getter()
            ))
        ).setIcon(Items.CHAIN_COMMAND_BLOCK));

    }

    private static final Map<String, PathInfo> PATH_INFO_REF_MAP = Maps.newHashMap();

    protected static PathInfo getRegisteredPathInfo(String refKey) {
        if(PATH_INFO_REF_MAP.isEmpty())
            buildPathInfos();
        if(PATH_INFO_REF_MAP.containsKey(refKey))
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
        TEXT_COMPONENT_LORE

    }

    public static abstract class PathInfo {

        protected ItemStack icon = null;
        protected Component info = null;
        protected PathFlag flag = PathFlag.NONE;
        protected boolean isEmpty = true;

        private static final PathInfo EMPTY = PathInfoDefinition.create();

        private PathInfo() {}

        public Component getInfo() {
            return info;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public PathFlag getFlag() {
            return flag;
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

        public PathInfo setFlag(PathFlag flag) {
            if(flag != null)
                this.flag = flag;
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

        public abstract KeyGetter getCompoundKeys(CompoundTag compound);

        public abstract PathInfo getCompoundKeyInfo(CompoundTag compound, String key);

        public abstract PathInfo getListIndexInfo(int i);

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

        public KeyGetter getCompoundKeys(CompoundTag compound) {
            return pi.get().getCompoundKeys(compound);
        }

        public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
            return pi.get().getCompoundKeyInfo(compound, key);
        }

        public PathInfo getListIndexInfo(int i) {
            return pi.get().getListIndexInfo(i);
        }

        @Override
        public Component getInfo() {
            return this.info != null ? this.info : pi.get().getInfo();
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

        public static PathInfoDefinition create() {
            return new PathInfoDefinition();
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
                if(compoundSupplier != null)
                    allNbtTypes.add(NbtType.COMPOUND);
                if(listSupplier != null)
                    allNbtTypes.add(NbtType.LIST);
                if(elementSupplier != null) {
                    allNbtTypes.remove(elementSupplier.getNbtType());
                    allNbtTypes.add(elementSupplier.getNbtType());
                }

            }
            return allNbtTypes;
        }

        public PathType getDefaultPathType() {
            return this.defaultPathType;
        }

        public boolean hasPathType(PathType type) {
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

        public KeyGetter getCompoundKeys(CompoundTag compound) {
            if(compoundSupplier != null)
                return compoundSupplier.getCompoundKeys(compound);
            return KeyGetter.create();
        }

        public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
            if(compoundSupplier != null)
                return compoundSupplier.getCompoundKeyInfo(compound, key);
            return PathInfo.EMPTY;
        }

        public PathInfo getListIndexInfo(int i) {
            if(listSupplier != null)
                return listSupplier.getListIndexInfo(i);
            return PathInfo.EMPTY;
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

        public abstract KeyGetter getCompoundKeys(CompoundTag compound);

        public abstract PathInfo getCompoundKeyInfo(CompoundTag compound, String key);

    }

    private static abstract class PathInfoSupplierList implements PathInfoSupplier {

        private static final SuggestionGetter SUGGS = SuggestionGetter.newInlineSnbt("[]");

        public SuggestionGetter getSuggs() {
            return SUGGS;
        }

        public PathType getPathType() {
            return PathType.LIST;
        }

        public abstract PathInfo getListIndexInfo(int i);

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
    
            private final KeyGetter keyGetter;
            private final Map<String, PathInfoGetter> keyInfo;
    
            private CompoundStructured(Map<String, PathInfoGetter> required, Map<String, PathInfoGetter> optional) {
                this.keyInfo = Maps.newHashMap();
                Set<String> requiredKeys = Sets.newHashSet();
                Set<String> optionalKeys = Sets.newHashSet();
                if(optional != null)
                    for(String s : optional.keySet()) {
                        this.keyInfo.put(s, optional.get(s));
                        optionalKeys.add(s);
                    }
                if(required != null)
                    for(String s : required.keySet()) {
                        this.keyInfo.put(s, required.get(s));
                        requiredKeys.add(s);
                        if(optionalKeys.contains(s)) {
                            FortytwoEdit.logWarn("PathInfoComoundStructured tried to create 2 PathInfo's for key: "+s);
                            optionalKeys.remove(s);
                        }
                    }
                this.keyGetter = KeyGetter.create().withRequired(requiredKeys.toArray(new String[0])).withOptional(optionalKeys.toArray(new String[0]));
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
    
            public KeyGetter getCompoundKeys(CompoundTag compound) {
                return this.keyGetter;
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                if(this.keyInfo.containsKey(key))
                    return this.keyInfo.get(key).get();
                return PathInfo.EMPTY;
            }
    
        }
    
        public static class Unit extends PathInfoSupplierCompound {
    
            private Unit() {}
    
            public static Unit create() {
                return new Unit();
            }
    
            @Override
            public PathType getPathType() {
                return PathType.UNIT;
            }
    
            public KeyGetter getCompoundKeys(CompoundTag compound) {
                return KeyGetter.create();
            }
    
            public PathInfo getCompoundKeyInfo(CompoundTag compound, String key) {
                return PathInfo.EMPTY;
            }
    
        }
    
        public static class ListUnordered extends PathInfoSupplierList {
    
            public final PathInfoGetter entryInfo;
    
            private ListUnordered(PathInfoGetter entry) {
                this.entryInfo = entry;
            }
    
            public static ListUnordered of(PathInfoGetter entry) {
                return new ListUnordered(entry);
            }
    
            public PathInfo getListIndexInfo(int i) {
                return entryInfo.get();
            }
    
        }
    
        public static class ElementLiteral extends PathInfoSupplierElement {
    
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
