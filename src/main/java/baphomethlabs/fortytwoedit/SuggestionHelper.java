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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.mixin.DecoratedPotPatternsAccessor;
import baphomethlabs.fortytwoedit.mixin.KeyMappingAccessor;
import baphomethlabs.fortytwoedit.mixin.ClientLanguageAccessor;

public class SuggestionHelper {

    // private static PathInfo getNewPathInfo(String path) {

    //     if(path.contains("components.consumable")) {
    //         if(path.endsWith("components.consumable.on_consume_effects"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.consumable.on_consume_effects[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("type").withOptional("effects","probability","diameter","sound")));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].type"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0]"))
    //             return PathInfos.EFFECT_NODE.withDesc(Component.nullToEmpty("Used for \"apply_effects\" or \"remove_effects\""));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].amplifier"))
    //             return PathInfos.EFFECT_AMPLIFIER;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].duration"))
    //             return PathInfos.EFFECT_DURATION;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].ambient"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].show_particles"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].effects[0].show_icon"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.consumable.on_consume_effects[0].probability"))
    //             return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"apply_effects\""));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].diameter"))
    //             return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"teleport_randomly\" (Defaults to 16f)"));
    //         if(path.endsWith("components.consumable.on_consume_effects[0].sound"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for \"play_sound\""));
    //     }

    //     if(path.contains("components.death_protection")) {
    //         if(path.endsWith("components.death_protection"))
    //             return (new PathInfo(KeyGetter.create().withOptional("death_effects"))).withIcon(Items.TOTEM_OF_UNDYING);
    //         if(path.endsWith("components.death_protection.death_effects"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.death_protection.death_effects[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("type").withOptional("effects","probability","diameter","sound")));
    //         if(path.endsWith("components.death_protection.death_effects[0].type"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_CONSUME_EFFECT_TYPE));
    //         if(path.endsWith("components.death_protection.death_effects[0].effects"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0]"))
    //             return PathInfos.EFFECT_NODE.withDesc(Component.nullToEmpty("Used for \"apply_effects\" or \"remove_effects\""));
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].amplifier"))
    //             return PathInfos.EFFECT_AMPLIFIER;
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].duration"))
    //             return PathInfos.EFFECT_DURATION;
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].ambient"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].show_particles"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.death_protection.death_effects[0].effects[0].show_icon"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith("components.death_protection.death_effects[0].probability"))
    //             return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"apply_effects\""));
    //         if(path.endsWith("components.death_protection.death_effects[0].diameter"))
    //             return PathInfos.FLOAT.withDesc(Component.nullToEmpty("Used for \"teleport_randomly\" (Defaults to 16f)"));
    //         if(path.endsWith("components.death_protection.death_effects[0].sound"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for \"play_sound\""));
    //     }

    //     if(path.contains(".entity_data")) {
    //         if(path.endsWith(".entity_data"))
    //             return (new PathInfo(KeyGetter.create().withRequired("id").withOptional(
    //             "CustomName","CustomNameVisible","Glowing","HasVisualFire","Invulnerable","Motion","NoGravity","Pos","Rotation","Silent","Tags",
    //             "active_effects","attributes","CanPickUpLoot","equipment","FallFlying","Health","leash","LeftHanded","NoAI","PersistenceRequired","Team",
    //             "DisabledSlots","Invisible","Marker","NoBasePlate","Pose","ShowArms","Small",
    //             "Fixed","Invisible","Item","ItemDropChance","ItemRotation",
    //             "beam_target","ShowBottom",
    //             "SoundEvent",
    //             "Duration","DurationOnUse","potion_contents","Particle","Radius","RadiusOnUse","RadiusPerTick","ReapplicationDelay","WaitTime",
    //             "variant"))).withIcon(Items.CREEPER_SPAWN_EGG);
    //         if(path.endsWith(".entity_data.id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_ENTITY_TYPE));
    //         if(path.endsWith(".entity_data.Air"))
    //             return PathInfos.SHORT;
    //         if(path.endsWith(".entity_data.CustomName"))
    //             return PathInfos.TEXT;
    //         if(path.endsWith(".entity_data.CustomNameVisible"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.equipment"))
    //             return (new PathInfo(KeyGetter.create().withOptional("feet","legs","chest","head","body","saddle","mainhand","offhand")));
    //         if(path.endsWith(".entity_data.equipment.feet"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.legs"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.chest"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.head"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.body"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.saddle"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.mainhand"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.equipment.offhand"))
    //             return PathInfos.ITEM_NODE;
    //         if(path.endsWith(".entity_data.FallDistance"))
    //             return PathInfos.FLOAT;
    //         if(path.endsWith(".entity_data.Fire"))
    //             return PathInfos.SHORT;
    //         if(path.endsWith(".entity_data.Glowing"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.HasVisualFire"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.Invulnerable"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.Motion"))
    //             return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0d,0d,0d]"))).withDesc(Component.nullToEmpty("[x, y, z] motion in each direction\nx - east\ny - up\nz - south"));
    //         if(path.endsWith(".entity_data.NoGravity"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.OnGround"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.Passengers"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith(".entity_data.PortalCooldown"))
    //             return PathInfos.INT;
    //         if(path.endsWith(".entity_data.Pos"))
    //             return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0d,0d,0d]"))).withDesc(Component.nullToEmpty("[x, y, z]"));
    //         if(path.endsWith(".entity_data.Rotation"))
    //             return (new PathInfo(PathType.INLINE_LIST,SuggestionGetter.newInline("[0f,0f]"))).withDesc(Component.nullToEmpty("[Yaw, Pitch]\nYaw: -180 to 180 (0 is south, 90 is west)\nPitch: -90 (up) to 90 (down)"));
    //         if(path.endsWith(".entity_data.Silent"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.Tags"))
    //             return PathInfos.LIST_STRING;
    //         if(path.endsWith(".entity_data.Tags[0]"))
    //             return PathInfos.STRING;
    //         if(path.endsWith(".entity_data.TicksFrozen"))
    //             return PathInfos.INT;
    //         if(path.endsWith(".entity_data.UUID"))
    //             return PathInfos.UUID;

    //         String lbl = "Mobs";
    //         if(path.endsWith(".entity_data.active_effects"))
    //             return PathInfos.LIST_COMPOUND.withGroup(lbl);
    //         if(path.endsWith(".entity_data.active_effects[0]") || path.endsWith(".hidden_effect"))
    //             return (new PathInfo(KeyGetter.create().withOptional("ambient","amplifier","duration","hidden_effect","id","show_icon","show_particles"))).withGroup(lbl).withFlag(PathFlag.EFFECT);
    //         if(path.endsWith(".entity_data.active_effects[0].ambient") || path.endsWith(".hidden_effect.ambient"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.active_effects[0].amplifier") || path.endsWith(".hidden_effect.amplifier"))
    //             return PathInfos.EFFECT_AMPLIFIER;
    //         if(path.endsWith(".entity_data.active_effects[0].duration") || path.endsWith(".hidden_effect.duration"))
    //             return PathInfos.EFFECT_DURATION;
    //         if(path.endsWith(".entity_data.active_effects[0].id") || path.endsWith(".hidden_effect.id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
    //         if(path.endsWith(".entity_data.active_effects[0].show_icon") || path.endsWith(".hidden_effect.show_icon"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.active_effects[0].show_particles") || path.endsWith(".hidden_effect.show_particles"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".entity_data.attributes"))
    //             return PathInfos.DEFAULT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.CanPickUpLoot"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.FallFlying"))
    //             return PathInfos.TRINARY.withDesc(Component.nullToEmpty("If true, mob will glide if wearing an elytra")).withGroup(lbl);
    //         if(path.endsWith(".entity_data.Health"))
    //             return PathInfos.FLOAT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.leash"))
    //             return (new PathInfo(PathType.DEFAULT,SuggestionGetter.newInline("{UUID:[I;0,0,0,0]}","{UUID:"+BlackMagick.nbtToSnbt(FortytwoEdit.UUID)+"}","[I;0,0,0]"))).withDesc(Component.nullToEmpty("Can be either:\na) NbtCompound like {UUID:[I;0,0,0,0]} pointing to an entity UUID\nb) NbtIntArray containing [I; X, Y, Z]")).withGroup(lbl);
    //         if(path.endsWith(".entity_data.LeftHanded"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.NoAI"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.PersistenceRequired"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.Team"))
    //             return PathInfos.STRING.withDesc(Component.nullToEmpty("Name of team for the mob to join when spawning")).withGroup(lbl);

    //         lbl = "Common";
    //         if(path.endsWith(".entity_data.Invisible"))
    //             return PathInfos.TRINARY.withGroup(lbl).withDesc(Component.nullToEmpty("Used by armor stands and item frames"));

    //         lbl = "Armor Stands";
    //         if(path.endsWith(".entity_data.DisabledSlots"))
    //             return (new PathInfo(PathType.INT,SuggestionGetter.newInline("16191"))).withDesc(Component.nullToEmpty("Value of 16191 prevents adding, changing, or removing armor or hand items")).withGroup(lbl);
    //         if(path.endsWith(".entity_data.Marker"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.NoBasePlate"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.Pose"))
    //             return (new PathInfo(PathType.POSE)).withGroup(lbl);
    //         if(path.endsWith(".entity_data.ShowArms"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.Small"))
    //             return PathInfos.TRINARY.withGroup(lbl);

    //         lbl = "Item Frames";
    //         if(path.endsWith(".entity_data.Fixed"))
    //             return PathInfos.TRINARY.withGroup(lbl);
    //         if(path.endsWith(".entity_data.Item"))
    //             return PathInfos.ITEM_NODE.withGroup(lbl);
    //         if(path.endsWith(".entity_data.ItemDropChance"))
    //             return PathInfos.FLOAT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.ItemRotation"))
    //             return (new PathInfo(PathType.BYTE,SuggestionGetter.newInline("0","1","2","3","4","5","6","7"))).withDesc(Component.nullToEmpty("Number of times the item is rotated clockwise")).withGroup(lbl);

    //         lbl = "End Crystals";
    //         if(path.endsWith(".entity_data.beam_target"))
    //             return PathInfos.INT_ARRAY_POS.withGroup(lbl);
    //         if(path.endsWith(".entity_data.ShowBottom"))
    //             return PathInfos.TRINARY.withGroup(lbl);

    //         lbl = "Arrows and Tridents";
    //         if(path.endsWith(".entity_data.SoundEvent"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Arrows types and tridents will play this sound when hitting something")).withGroup(lbl);

    //         lbl = "Area Effect Clouds";
    //         if(path.endsWith(".entity_data.Duration"))
    //             return PathInfos.INT.withDesc(Component.nullToEmpty("Max age after WaitTime")).withGroup(lbl);
    //         if(path.endsWith(".entity_data.DurationOnUse"))
    //             return PathInfos.INT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.potion_contents"))
    //             return PathInfos.POTION_CONTENTS.withGroup(lbl);
    //         if(path.endsWith(".entity_data.Particle"))
    //             return (new PathInfo(PathType.INLINE_COMPOUND,REGISTRY_PARTICLE_TYPE.withFormat(SuggestionGetter.Format.AEC_PARTICLE_TYPE))).withDesc(Component.nullToEmpty("Format like {type:\"dust\",color:[.5d,0d,1d],scale:2}")).withGroup(lbl);
    //         if(path.endsWith(".entity_data.Radius"))
    //             return PathInfos.FLOAT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.RadiusOnUse"))
    //             return PathInfos.FLOAT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.RadiusPerTick"))
    //             return PathInfos.FLOAT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.ReapplicationDelay"))
    //             return PathInfos.INT.withGroup(lbl);
    //         if(path.endsWith(".entity_data.WaitTime"))
    //             return PathInfos.INT.withDesc(Component.nullToEmpty("Time before cloud can have a radius and effect (particles will still appear in the center)")).withGroup(lbl);

    //         // when adding new paths, also add keys to entity_data compound
    //         // if a key is already used for another entity, move it to common category
    //     }

    //     if(path.endsWith("components.max_stack_size"))
    //         return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

    //     if(path.endsWith("components.note_block_sound"))
    //         return (new PathInfo(PathType.STRING,REGISTRY_SOUND_EVENT)).withDesc(Component.nullToEmpty("Used for player heads on a note block")).withIcon(Items.PLAYER_HEAD);

    //     if(path.endsWith("components.ominous_bottle_amplifier"))
    //         return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0","1","2","3","4"))).withIcon(Items.OMINOUS_BOTTLE);

    //     if(path.contains("components.pot_decorations")) {
    //         if(path.endsWith("components.pot_decorations"))
    //             return PathInfos.LIST_STRING.withIcon(Items.DECORATED_POT);
    //         if(path.endsWith("components.pot_decorations[0]"))
    //             return (new PathInfo(PathType.STRING,LIST_DECORATED_POT_PATTERN_ITEMS));
    //     }

    //     if(path.contains(".potion_contents")) {
    //         if(path.endsWith(".potion_contents"))
    //             return PathInfos.POTION_CONTENTS.withIcon(Items.SPLASH_POTION);
    //         if(path.endsWith(".potion_contents.potion"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_POTION)).withDesc(Component.nullToEmpty("Potion base before custom_color and custom_effects"));
    //         if(path.endsWith(".potion_contents.custom_color"))
    //             return PathInfos.DECIMAL_COLOR;
    //         if(path.endsWith(".potion_contents.custom_effects"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith(".potion_contents.custom_effects[0]"))
    //             return PathInfos.EFFECT_NODE;
    //         if(path.endsWith(".potion_contents.custom_effects[0].id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
    //         if(path.endsWith(".potion_contents.custom_effects[0].amplifier"))
    //             return PathInfos.EFFECT_AMPLIFIER;
    //         if(path.endsWith(".potion_contents.custom_effects[0].duration"))
    //             return PathInfos.EFFECT_DURATION;
    //         if(path.endsWith(".potion_contents.custom_effects[0].ambient"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".potion_contents.custom_effects[0].show_particles"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".potion_contents.custom_effects[0].show_icon"))
    //             return PathInfos.TRINARY;
    //         if(path.endsWith(".potion_contents.custom_name"))
    //             return (new PathInfo(PathType.STRING));//to_do set of different strings based on item id translation key plus potion base name, or "empty"
    //     }

    //     if(path.contains("components.profile")) {
    //         if(path.endsWith("components.profile"))
    //             return (new PathInfo(KeyGetter.create().withOptional("name","id","properties"))).withIcon(Items.PLAYER_HEAD);
    //         if(path.endsWith("components.profile.name"))
    //             return PathInfos.STRING.withDesc(Component.nullToEmpty("Player name used to update skin"));
    //         if(path.endsWith("components.profile.id"))
    //             return PathInfos.UUID.withDesc(Component.nullToEmpty("Player UUID used to update skin"));
    //         if(path.endsWith("components.profile.properties"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.profile.properties[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("name","value").withOptional("signature")));
    //         if(path.endsWith("components.profile.properties[0].name"))
    //             return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("textures"))).withDesc(Component.nullToEmpty("Currently only used for textures"));
    //         if(path.endsWith("components.profile.properties[0].value"))
    //             return PathInfos.STRING;
    //         if(path.endsWith("components.profile.properties[0].signature"))
    //             return PathInfos.STRING;
    //     }

    //     if(path.endsWith("components.rarity"))
    //         return (new PathInfo(PathType.STRING,SuggestionGetter.newInline("common","uncommon","rare","epic"))).withDesc(Component.nullToEmpty("Used for item name color:\n  \u00a7fcommon\n  \u00a7euncommon\n  \u00a7brare\n  \u00a7depic\u00a7r")).withIcon(Items.STONE); // hardcoded list

    //     if(path.contains("components.recipes")) {
    //         if(path.endsWith("components.recipes"))
    //             return PathInfos.LIST_STRING.withIcon(Items.KNOWLEDGE_BOOK);
    //         if(path.endsWith("components.recipes[0]"))
    //             return (new PathInfo(PathType.STRING,DATA_RECIPE));
    //     }

    //     if(path.endsWith("components.repair_cost"))
    //         return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0",""+Integer.MAX_VALUE))).withIcon(Items.ENCHANTED_BOOK);

    //     if(path.contains("components.repairable")) {
    //         if(path.endsWith("components.repairable"))
    //             return (new PathInfo(KeyGetter.create().withRequired("items"))).withIcon(Items.ANVIL);
    //         if(path.endsWith("components.repairable.items"))
    //             return PathInfos.ITEM_PREDICATE_ITEMS;
    //     }

    //     if(path.endsWith("components.stored_enchantments"))
    //         return (new PathInfo(KeyGetter.create().withOptional(DATA_ENCHANTMENT))).withIcon(Items.ENCHANTED_BOOK);

    //     if(path.contains("components.suspicious_stew_effects")) {
    //         if(path.endsWith("components.suspicious_stew_effects"))
    //             return PathInfos.LIST_COMPOUND.withIcon(Items.SUSPICIOUS_STEW);
    //         if(path.endsWith("components.suspicious_stew_effects[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("id").withOptional("duration"))).withFlag(PathFlag.EFFECT);
    //         if(path.endsWith("components.suspicious_stew_effects[0].id"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_STATUS_EFFECT));
    //         if(path.endsWith("components.suspicious_stew_effects[0].duration"))
    //             return PathInfos.EFFECT_DURATION;
    //     }

    //     if(path.contains("components.tool")) {
    //         if(path.endsWith("components.tool"))
    //             return (new PathInfo(KeyGetter.create().withRequired("rules").withOptional("default_mining_speed","damage_per_block"))).withIcon(Items.DIAMOND_PICKAXE);
    //         if(path.endsWith("components.tool.default_mining_speed"))
    //             return PathInfos.DEFAULT;
    //         if(path.endsWith("components.tool.damage_per_block"))
    //             return PathInfos.INT;
    //         if(path.endsWith("components.tool.rules"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.tool.rules[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("blocks").withOptional("speed","correct_for_drops")));
    //         if(path.endsWith("components.tool.rules[0].blocks"))
    //             return PathInfos.LIST_STRING;
    //         if(path.endsWith("components.tool.rules[0].blocks[0]"))
    //             return PathInfos.BLOCK_PREDICATE_BLOCKS;
    //         if(path.endsWith("components.tool.rules[0].speed"))
    //             return PathInfos.DEFAULT;
    //         if(path.endsWith("components.tool.rules[0].correct_for_drops"))
    //             return PathInfos.TRINARY;
    //     }

    //     if(path.endsWith("components.tooltip_style"))
    //         return (new PathInfo(PathType.STRING)).withDesc(Component.nullToEmpty("References tooltip sprites at \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_background\" and \"assets/<namespace>/textures/gui/sprites/tooltip/<id>_frame\"")).withIcon(Items.COMMAND_BLOCK);

    //     if(path.contains("components.trim")) {
    //         if(path.endsWith("components.trim"))
    //             return (new PathInfo(KeyGetter.create().withRequired("pattern","material"))).withIcon(Items.DIAMOND_CHESTPLATE);
    //         if(path.endsWith("components.trim.pattern"))
    //             return (new PathInfo(PathType.STRING,DATA_TRIM_PATTERN));
    //         if(path.endsWith("components.trim.material"))
    //             return (new PathInfo(PathType.STRING,DATA_TRIM_MATERIAL));
    //     }

    //     if(path.endsWith("components.unbreakable"))
    //         return PathInfos.UNIT.withIcon(Items.COMMAND_BLOCK);

    //     if(path.contains("components.use_cooldown")) {
    //         if(path.endsWith("components.use_cooldown"))
    //             return (new PathInfo(KeyGetter.create().withRequired("seconds").withOptional("cooldown_group"))).withIcon(Items.ENDER_PEARL);
    //         if(path.endsWith("components.use_cooldown.seconds"))
    //             return (new PathInfo(PathType.FLOAT,SuggestionGetter.newInline("1.0f")));
    //         if(path.endsWith("components.use_cooldown.cooldown_group"))
    //             return (new PathInfo(PathType.STRING,REGISTRY_ITEM)).withDesc(Component.nullToEmpty("Custom namespaced ID or namespaced item ID"));
    //     }

    //     if(path.endsWith("components.use_remainder"))
    //         return PathInfos.ITEM_NODE.withIcon(Items.MUSHROOM_STEW);

    //     if(path.contains("components.writable_book_content")) {
    //         if(path.endsWith("components.writable_book_content"))
    //             return (new PathInfo(KeyGetter.create().withOptional("pages"))).withIcon(Items.WRITABLE_BOOK);
    //         if(path.endsWith("components.writable_book_content.pages"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.writable_book_content.pages[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
    //         if(path.endsWith("components.writable_book_content.pages[0].raw"))
    //             return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of page text"));
    //         if(path.endsWith("components.writable_book_content.pages[0].filtered"))
    //             return PathInfos.STRING;
    //     }

    //     if(path.contains("components.written_book_content")) {
    //         if(path.endsWith("components.written_book_content"))
    //             return (new PathInfo(KeyGetter.create().withRequired("author","title").withOptional("pages","generation","resolved"))).withIcon(Items.WRITTEN_BOOK);
    //         if(path.endsWith("components.written_book_content.pages"))
    //             return PathInfos.LIST_COMPOUND;
    //         if(path.endsWith("components.written_book_content.pages[0]"))
    //             return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
    //         if(path.endsWith("components.written_book_content.pages[0].raw"))
    //             return PathInfos.TEXT;
    //         if(path.endsWith("components.written_book_content.pages[0].filtered"))
    //             return PathInfos.TEXT;
    //         if(path.endsWith("components.written_book_content.title"))
    //             return (new PathInfo(KeyGetter.create().withRequired("raw").withOptional("filtered")));
    //         if(path.endsWith("components.written_book_content.title.raw"))
    //             return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of title"));
    //         if(path.endsWith("components.written_book_content.title.filtered"))
    //             return PathInfos.STRING;
    //         if(path.endsWith("components.written_book_content.author"))
    //             return PathInfos.STRING.withDesc(Component.nullToEmpty("Literal string of author"));
    //         if(path.endsWith("components.written_book_content.generation"))
    //             return (new PathInfo(PathType.INT,SuggestionGetter.newInline("0","1","2","3"))).withDesc(Component.nullToEmpty("0 - Original\n1 - Copy of original\n2 - Copy of copy\n3 - Tattered")); // hardcoded list
    //         if(path.endsWith("components.written_book_content.resolved"))
    //             return PathInfos.TRINARY.withDesc(Component.nullToEmpty("Whether or not text component is resolved (for selectors/scores/etc)"));
    //     }

    //     if(path.contains("components.!")) {
    //         for(String c : LIST_DATA_COMPONENT_TYPE.getList()) {
    //             if(path.endsWith("components.!"+c.replace("minecraft:",""))) {
    //                 return PathInfos.UNIT.withGroup("Inverted Components").withIcon(getPathInfo("components."+c).icon());
    //             }
    //         }
    //     }

    //     if(path.equals("id") || path.endsWith(".id"))
    //         return (new PathInfo(PathType.STRING,REGISTRY_ITEM)).withIcon(Items.STONE);

    //     if(path.equals("count") || path.endsWith(".count"))
    //         return PathInfos.ITEM_COUNT.withIcon(Items.STONE);

    //     if(path.equals("components") || path.endsWith(".components"))
    //         return PathInfos.COMPONENTS_OR_INVERTED;

    //     FortytwoEdit.logWarn("No PathInfo found for path: "+path);
    //     return PathInfos.UNKNOWN;
    // }

    // private class PathInfos {

    //     private static final PathInfo UNKNOWN = (new PathInfo(PathType.UNKNOWN));
    //     private static final PathInfo DEFAULT = (new PathInfo(PathType.DEFAULT));
    //     private static final PathInfo UNIT = (new PathInfo(PathType.UNIT,SuggestionGetter.newInline("","{}"))).withDesc(Component.nullToEmpty("{} represents true"));
    //     private static final PathInfo TRINARY = (new PathInfo(PathType.TRINARY,SuggestionGetter.newInline("","false","true"))).withDesc(Component.nullToEmpty("Boolean (true or false)"));
    //     private static final PathInfo SHORT = (new PathInfo(PathType.SHORT,SuggestionGetter.newInline("0s")));
    //     private static final PathInfo INT = (new PathInfo(PathType.INT,SuggestionGetter.newInline("0")));
    //     private static final PathInfo LONG = (new PathInfo(PathType.LONG,SuggestionGetter.newInline("0l")));
    //     private static final PathInfo DOUBLE = (new PathInfo(PathType.DOUBLE,SuggestionGetter.newInline("0.0d")));
    //     private static final PathInfo FLOAT = (new PathInfo(PathType.FLOAT,SuggestionGetter.newInline("0.0f")));
    //     private static final PathInfo STRING = (new PathInfo(PathType.STRING));
    //     private static final PathInfo BYTE_ARRAY = (new PathInfo(PathType.BYTE_ARRAY,SuggestionGetter.newInline("[B;]")));
    //     private static final PathInfo LIST_COMPOUND = (new PathInfo(Tag.TAG_COMPOUND));
    //     private static final PathInfo LIST_FLOAT = (new PathInfo(Tag.TAG_FLOAT));
    //     private static final PathInfo LIST_STRING = (new PathInfo(Tag.TAG_STRING));
    //     private static final PathInfo INLINE_COMPOUND = (new PathInfo(PathType.INLINE_COMPOUND,SuggestionGetter.newInline("{}")));

    //     private static final PathInfo TEXT = (new PathInfo(PathType.TEXT,SuggestionGetter.newInline("\"\"","{text:\"\"}","[\"\"]"))).withDesc(Component.nullToEmpty("Text component"));
    //     private static final PathInfo DECIMAL_COLOR = (new PathInfo(PathType.DECIMAL_COLOR,SuggestionGetter.newInline("0","16777215"))).withDesc(Component.nullToEmpty("0xRRGGBB hex color converted to integer"));
    //     private static final PathInfo UUID = (new PathInfo(PathType.UUID,SuggestionGetter.newInline("[I;0,0,0,0]")));
    //     private static final PathInfo INT_ARRAY_POS = (new PathInfo(PathType.INT_ARRAY,SuggestionGetter.newInline("[I;0,0,0]"))).withDesc(Component.nullToEmpty("[I; X, Y, Z] block coordinates"));

    //     private static final PathInfo ITEM_NODE = (new PathInfo(KeyGetter.create().withRequired("id").withOptional("count","components")));
    //     private static final PathInfo ITEM_COUNT = (new PathInfo(PathType.INT,SuggestionGetter.newInline("1","16","64","99")));

    //     private static final PathInfo COMPONENTS_NODE = (new PathInfo(KeyGetter.create().withOptional(LIST_DATA_COMPONENT_TYPE)));
    //     private static final PathInfo COMPONENTS_OR_INVERTED = (new PathInfo(KeyGetter.create().withOptional(LIST_DATA_COMPONENT_TYPE,LIST_DATA_COMPONENT_TYPE.withFormat(SuggestionGetter.Format.INVERTED))));

    //     private static final PathInfo POTION_CONTENTS = (new PathInfo(KeyGetter.create().withOptional("potion","custom_color","custom_effects","custom_name")));
    //     private static final PathInfo EFFECT_NODE = (new PathInfo(KeyGetter.create().withRequired("id").withOptional("amplifier","duration","ambient","show_particles","show_icon"))).withFlag(PathFlag.EFFECT);
    //     private static final PathInfo EFFECT_DURATION = (new PathInfo(PathType.INT,SuggestionGetter.newInline("-1","1"))).withDesc(Component.nullToEmpty("Duration in ticks or -1 for infinity"));
    //     private static final PathInfo EFFECT_AMPLIFIER = (new PathInfo(PathType.BYTE,SuggestionGetter.newInline("0","255"))).withDesc(Component.nullToEmpty("Amplifier 0-255 gives effect level 1-256"));

    //     private static final PathInfo BLOCK_PREDICATE_BLOCKS = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
    //         REGISTRY_BLOCK.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         DATA_TAG_BLOCK.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         SuggestionGetter.newInline("[\"dirt\",\"stone\"]"))))
    //         .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an block ID or block tag\nb) NbtList of block ID NbtStrings"));
    //     private static final PathInfo ITEM_PREDICATE_ITEMS = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
    //         REGISTRY_ITEM.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         DATA_TAG_ITEM.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         SuggestionGetter.newInline("[\"diamond\",\"gold_ingot\"]"))))
    //         .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an item ID or item tag\nb) NbtList of item ID NbtStrings"));
    //     private static final PathInfo ENTITY_PREDICATE_ENTITIES = (new PathInfo(PathType.DEFAULT,SuggestionGetter.newJoined(
    //         REGISTRY_ENTITY_TYPE.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         DATA_TAG_ENTITY_TYPE.withFormat(SuggestionGetter.Format.NBT_STRING),
    //         SuggestionGetter.newInline("[\"skeleton\",\"zombie\"]"))))
    //         .withDesc(Component.nullToEmpty("Can be either:\na) NbtString of an entity ID or entity tag\nb) NbtList of entity ID NbtStrings"));

    // }

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

        public Set<String> getKeys() {
            Set<String> set = Sets.newHashSet();
            for(SuggestionGetter suggs : requiredKeys)
                set.addAll(suggs.getList());
            for(SuggestionGetter suggs : optionalKeys)
                set.addAll(suggs.getList());
            return set;
        }

    }

    public record SuggestionGetter(String[] inlinedSuggs, String listMapKey, boolean isSnbt, SuggestionGetter[] joinedLists, boolean isEmpty) {

        public static SuggestionGetter newInline(String... suggs) {
            return new SuggestionGetter(suggs, null, false, null, false);
        }
        public static SuggestionGetter newInlineSnbt(String... suggs) {
            return new SuggestionGetter(suggs, null, true, null, false);
        }

        public static SuggestionGetter newRef(String key) {
            return new SuggestionGetter(null, key, false, null, false);
        }

        public static SuggestionGetter newJoined(SuggestionGetter... lists) {
            return new SuggestionGetter(null, null, false, lists, false);
        }

        public static SuggestionGetter empty() {
            return new SuggestionGetter(null, null, false, null, true);
        }

        public List<String> getList(boolean snbt) {
            Set<String> set = Sets.newHashSet();
            if(snbt) {
                if(inlinedSuggs != null) {
                    if(isSnbt)
                        set.addAll(Set.of(inlinedSuggs));
                    else
                        set.addAll(BlackMagick.formatStringSuggs(List.of(inlinedSuggs)));
                }
                if(listMapKey != null && SUGGS_LIST_METHODS.containsKey(listMapKey))
                    set.addAll(BlackMagick.formatStringSuggs(SUGGS_LIST_METHODS.get(listMapKey).get()));
                if(joinedLists != null)
                    for(SuggestionGetter s : joinedLists)
                        set.addAll(s.getList(true));
            }
            else {
                if(inlinedSuggs != null && !isSnbt)
                    set.addAll(Set.of(inlinedSuggs));
                if(listMapKey != null && SUGGS_LIST_METHODS.containsKey(listMapKey))
                    set.addAll(SUGGS_LIST_METHODS.get(listMapKey).get());
                if(joinedLists != null)
                    for(SuggestionGetter s : joinedLists)
                        set.addAll(s.getList());
            }
            List<String> list = Lists.newArrayList();
            list.addAll(set);
            Collections.sort(list);
            return list;
        }

        public List<String> getList() {
            return getList(false);
        }

        public String[] getArray() {
            return getList(false).toArray(new String[0]);
        }

        public String[] getArraySnbt() {
            return getList(true).toArray(new String[0]);
        }

    }


    private static final Map<String,List<String>> LIST_CACHES = Maps.newHashMap();
    private static final Set<String> DYNAMIC_LIST_CACHES = Sets.newHashSet();
    private static final Map<String,Supplier<List<String>>> SUGGS_LIST_METHODS = Maps.newHashMap();

    public static void clearCacheInfo() {
        for(String key : LIST_CACHES.keySet())
            LIST_CACHES.get(key).clear();
    }

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
    public static int[] getContainerSize(Item item) {//to_do move classes?

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
