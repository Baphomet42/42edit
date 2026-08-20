package baphomethlabs.fortytwoedit;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.scores.TeamColor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.mixin.KeyMappingAccessor;
import baphomethlabs.fortytwoedit.mixin.TextureAtlasAccessor;
import baphomethlabs.fortytwoedit.mixin.ClientLanguageAccessor;

public class SuggestionHelper {

    public record SuggestionGetter(String[] inlinedSuggs, String listMapKey, boolean isSnbt, SuggestionGetter[] joinedLists, boolean isEmpty) {

        public static SuggestionGetter newInline(String... suggs) {
            return new SuggestionGetter(suggs, null, false, null, false);
        }
        public static SuggestionGetter newInlineSnbt(String... suggs) {
            return new SuggestionGetter(suggs, null, true, null, false);
        }

        public static SuggestionGetter newRef(String key) {
            return newRef(key, false);
        }

        public static SuggestionGetter newRef(String key, boolean isSnbt) {
            return new SuggestionGetter(null, key, isSnbt, null, false);
        }

        public static SuggestionGetter newJoined(SuggestionGetter... lists) {
            return new SuggestionGetter(null, null, false, lists, false);
        }

        public static SuggestionGetter empty() {
            return new SuggestionGetter(null, null, false, null, true);
        }

        public List<String> getList(boolean snbt) {
            List<String> list = Lists.newArrayList();
            if (snbt) {
                if (inlinedSuggs != null) {
                    if (isSnbt)
                        list.addAll(Set.of(inlinedSuggs));
                    else
                        list.addAll(BlackMagick.formatStringSuggs(List.of(inlinedSuggs)));
                }
                if (listMapKey != null && SUGGS_LIST_METHODS.containsKey(listMapKey)) {
                    if (isSnbt)
                        list.addAll(SUGGS_LIST_METHODS.get(listMapKey).get());
                    else
                        list.addAll(BlackMagick.formatStringSuggs(SUGGS_LIST_METHODS.get(listMapKey).get()));
                }
            }
            else {
                if (inlinedSuggs != null && !isSnbt)
                    list.addAll(Set.of(inlinedSuggs));
                if (listMapKey != null && !isSnbt && SUGGS_LIST_METHODS.containsKey(listMapKey))
                    list.addAll(SUGGS_LIST_METHODS.get(listMapKey).get());
            }
            if (joinedLists != null)
                for (SuggestionGetter s : joinedLists)
                    list.addAll(s.getList(snbt));
            sortUnique(list);
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
    private static final Map<String,Map<String,List<String>>> BLOCK_STATE_CACHE = Maps.newHashMap();

    public static void clearCacheInfo() {
        for (String key : LIST_CACHES.keySet())
            LIST_CACHES.get(key).clear();
        BLOCK_STATE_CACHE.clear();
    }

    public static void clearDynamicListCaches() {
        for (String key : DYNAMIC_LIST_CACHES)
            LIST_CACHES.get(key).clear();
    }

    public static void runAllListMethods() {
        clearCacheInfo();
        int successCount = 0;
        int totalCount = 0;
        for (String list : SUGGS_LIST_METHODS.keySet()) {
            if (SUGGS_LIST_METHODS.get(list).get().isEmpty())
                FortytwoEdit.logWarn("Unable to fetch suggestions list: " + list);
            else
                successCount++;
            totalCount++;
        }
        FortytwoEdit.logInfo("Loaded " + successCount + "/" + totalCount + " suggestions lists");
    }

    protected static List<String> createOrGetCacheList(String name, boolean isDynamic) {
        // helpful regex search `createOrGetCacheList\(.*true\)` or `createOrGetCacheList\(.*false\)`
        if (LIST_CACHES.containsKey(name))
            return LIST_CACHES.get(name);
        List<String> list = Lists.newArrayList();
        LIST_CACHES.put(name, list);
        if (isDynamic)
            DYNAMIC_LIST_CACHES.add(name);
        return list;
    }

    protected static SuggestionGetter registerSuggsList(String listName, Supplier<List<String>> method) {
        SUGGS_LIST_METHODS.put(listName, method);
        return SuggestionGetter.newRef(listName);
    }

    protected static SuggestionGetter registerSuggsListSnbt(String listName, Supplier<List<String>> method) {
        SUGGS_LIST_METHODS.put(listName, method);
        return SuggestionGetter.newRef(listName, true);
    }

    public static void sortUnique(List<String> list) {
        Set<String> tempSet = Sets.newHashSet();
        tempSet.addAll(list);

        list.clear();
        list.addAll(tempSet);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    }


    /**
     * Get list of all possible block states that can be applied to the item
     * 
     * @param item the ItemStack.getItem()
     * @return map of lists
     */
    public static Map<String,List<String>> getBlockStates(Item item) {
        String itemId = BlackMagick.itemToStringId(item);
        if (!BLOCK_STATE_CACHE.containsKey(itemId)) {
            Map<String,List<String>> states = Maps.newHashMap();
            BlockState blockState = Block.byItem(item).defaultBlockState();
            for (Property<?> entry : blockState.getProperties()) {
                List<String> list = Lists.newArrayList();
                for (Comparable<?> val : entry.getPossibleValues()) {
                    list.add((String)Util.getPropertyName(entry, val));
                }
                states.put(entry.getName(), list);
            }
            BLOCK_STATE_CACHE.put(itemId, states);
        }
        return BLOCK_STATE_CACHE.get(itemId);
    }

    /**
     * 
     * @param key an enchantment ID
     * @return the max level for the enchantment, or -1 if unknown
     */
    public static int getEnchantmentMaxLevel(String key) {
        int max = -1;
        try {
            Enchantment ench = BlackMagick.getRegistryAccess().lookup(Registries.ENCHANTMENT).get().getValue(Identifier.parse(key));
            if (ench != null)
                max = ench.getMaxLevel();
        } catch (Exception ex) {}
        return max;
    }

    /**
     * <li> Find items by searching {@link net.minecraft.world.item.Items} for {@link net.minecraft.core.component.DataComponents#CONTAINER} aka (`DataComponents.CONTAINER`) </li>
     * <li> (Search for `DataComponents.CONTAINER`) </li>
     * <li> Manually enter rows/cols based on ingame gui appearance. </li>
     * <li> Remove ender chest. </li>
     * 
     * @param item
     * @return int array with [rows, columns] or [-1, -1] depending on storage size of blockentity for item
     */
    public static int[] getContainerSize(Item item) {

        Identifier identifier = BlackMagick.identifierOrNull(BlackMagick.itemToStringId(item));
        if (identifier != null) {
            String id = BlackMagick.identifierToString(identifier);

            if (id.startsWith("minecraft:") && id.endsWith("shulker_box"))
                return new int[]{3, 9};

            switch (id) {
                case "minecraft:chest":
                case "minecraft:trapped_chest":
                case "minecraft:barrel":
                    return new int[]{3, 9};
                case "minecraft:dispenser":
                case "minecraft:dropper":
                case "minecraft:crafter":
                    return new int[]{3, 3};
                case "minecraft:hopper":
                case "minecraft:brewing_stand":
                    return new int[]{1, 5};
                case "minecraft:furnace":
                case "minecraft:blast_furnace":
                case "minecraft:smoker":
                    return new int[]{1, 3};
                case "minecraft:chiseled_bookshelf":
                    return new int[]{2, 3};
                case "minecraft:campfire":
                case "minecraft:soul_campfire":
                    return new int[]{1, 4};
                case "minecraft:decorated_pot":
                    return new int[]{1, 1};
                default: break;
            }
        }
        return new int[]{-1, -1};
    }


    // static hardcoded lists

    public static final SuggestionGetter LIST_ATTRIBUTE_MODIFIER_SLOT = registerSuggsList("LIST_ATTRIBUTE_MODIFIER_SLOT", () -> {
        List<String> list = createOrGetCacheList("LIST_ATTRIBUTE_MODIFIER_SLOT", false);
        if (list.isEmpty()) {
            for (EquipmentSlotGroup i : EquipmentSlotGroup.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_AXOLOTL_VARIANT = registerSuggsList("LIST_AXOLOTL_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_AXOLOTL_VARIANT", false);
        if (list.isEmpty()) {
            for (Axolotl.Variant i : Axolotl.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    /**
     * Contains components that can be serialized
     */
    public static final SuggestionGetter LIST_DATA_COMPONENT_TYPE = registerSuggsList("LIST_DATA_COMPONENT_TYPE", () -> {
        List<String> list = createOrGetCacheList("LIST_DATA_COMPONENT_TYPE", false);
        if (list.isEmpty()) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.forEach(i -> {
                if (!i.isTransient())
                    list.add(BlackMagick.identifierToString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(i)));
            });
            sortUnique(list);
        }
        return list;
    });

    /**
     * Contains components that can be serialized
     */
    public static final SuggestionGetter LIST_DATA_COMPONENT_TYPE_OR_REMOVED = registerSuggsList("LIST_DATA_COMPONENT_TYPE_OR_REMOVED", () -> {
        List<String> list = createOrGetCacheList("LIST_DATA_COMPONENT_TYPE_OR_REMOVED", false);
        if (list.isEmpty()) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.forEach(i -> {
                if (!i.isTransient()) {
                    list.add(BlackMagick.identifierToString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(i)));
                    list.add("!" + BlackMagick.identifierToString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(i)));
                }
            });
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_DYE_COLOR = registerSuggsList("LIST_DYE_COLOR", () -> {
        List<String> list = createOrGetCacheList("LIST_DYE_COLOR", false);
        if (list.isEmpty()) {
            for (DyeColor i : DyeColor.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_EQUIPMENT_SLOT = registerSuggsList("LIST_EQUIPMENT_SLOT", () -> {
        List<String> list = createOrGetCacheList("LIST_EQUIPMENT_SLOT", false);
        if (list.isEmpty()) {
            for (EquipmentSlot i : EquipmentSlot.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE = registerSuggsList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE", () -> {
        List<String> list = createOrGetCacheList("LIST_FIREWORK_EXPLOSION_COMPONENT_TYPE", false);
        if (list.isEmpty()) {
            for (FireworkExplosion.Shape i : FireworkExplosion.Shape.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_FORMATTING_COLOR = registerSuggsList("LIST_FORMATTING_COLOR", () -> {
        List<String> list = createOrGetCacheList("LIST_FORMATTING_COLOR", false);
        if (list.isEmpty()) {
            for (TeamColor i : TeamColor.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_FOX_VARIANT = registerSuggsList("LIST_FOX_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_FOX_VARIANT", false);
        if (list.isEmpty()) {
            for (Fox.Variant i : Fox.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_HORSE_VARIANT = registerSuggsList("LIST_HORSE_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_HORSE_VARIANT", false);
        if (list.isEmpty()) {
            for (Variant i : Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_KEYBIND = registerSuggsList("LIST_KEYBIND", () -> {
        List<String> list = createOrGetCacheList("LIST_KEYBIND", false);
        if (list.isEmpty()) {
            for (String i : KeyMappingAccessor.getKeysList().keySet())
                if (!i.startsWith("42edit."))
                    list.add(i);
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_LLAMA_VARIANT = registerSuggsList("LIST_LLAMA_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_LLAMA_VARIANT", false);
        if (list.isEmpty()) {
            for (Llama.Variant i : Llama.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_MOOSHROOM_VARIANT = registerSuggsList("LIST_MOOSHROOM_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_MOOSHROOM_VARIANT", false);
        if (list.isEmpty()) {
            for (MushroomCow.Variant i : MushroomCow.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_PARROT_VARIANT = registerSuggsList("LIST_PARROT_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_PARROT_VARIANT", false);
        if (list.isEmpty()) {
            for (Parrot.Variant i : Parrot.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_POTION_CUSTOM_NAME = registerSuggsList("LIST_POTION_CUSTOM_NAME", () -> {
        List<String> list = createOrGetCacheList("LIST_POTION_CUSTOM_NAME", false);
        if (list.isEmpty()) {
            list.add("empty");
            for (Identifier i : BuiltInRegistries.POTION.keySet())
                list.add(BuiltInRegistries.POTION.get(i).get().value().name());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_RABBIT_VARIANT = registerSuggsList("LIST_RABBIT_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_RABBIT_VARIANT", false);
        if (list.isEmpty()) {
            for (Rabbit.Variant i : Rabbit.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_SALMON_VARIANT = registerSuggsList("LIST_SALMON_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_SALMON_VARIANT", false);
        if (list.isEmpty()) {
            for (Salmon.Variant i : Salmon.Variant.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_SWING_ANIMATION_TYPE = registerSuggsList("LIST_SWING_ANIMATION_TYPE", () -> {
        List<String> list = createOrGetCacheList("LIST_SWING_ANIMATION_TYPE", false);
        if (list.isEmpty()) {
            for (SwingAnimationType i : SwingAnimationType.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    public static final SuggestionGetter LIST_TROPICAL_FISH_VARIANT = registerSuggsList("LIST_TROPICAL_FISH_VARIANT", () -> {
        List<String> list = createOrGetCacheList("LIST_TROPICAL_FISH_VARIANT", false);
        if (list.isEmpty()) {
            for (TropicalFish.Pattern i : TropicalFish.Pattern.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });

    /**
     * Entries for consumable component animation
     */
    public static final SuggestionGetter LIST_USE_ACTION = registerSuggsList("LIST_USE_ACTION", () -> {
        List<String> list = createOrGetCacheList("LIST_USE_ACTION", false);
        if (list.isEmpty()) {
            for (ItemUseAnimation i : ItemUseAnimation.values())
                list.add(i.getSerializedName());
            sortUnique(list);
        }
        return list;
    });


    // dynamic hardcoded lists

    public static final SuggestionGetter LIST_ATLAS = registerSuggsList("LIST_ATLAS", () -> {
        List<String> list = createOrGetCacheList("LIST_ATLAS", true);
        if (list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.getAtlasManager() != null) {
                client.getAtlasManager().forEach((identifier, atlasEntry) -> {
                    list.add(BlackMagick.identifierToString(identifier));
                });
                sortUnique(list);
            }
        }
        return list;
    });

    public static final SuggestionGetter LIST_SPRITE = registerSuggsList("LIST_SPRITE", () -> {
        List<String> list = createOrGetCacheList("LIST_SPRITE", true);
        if (list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.getAtlasManager() != null) {
                client.getAtlasManager().forEach((identifier, atlasEntry) -> {
                    ((TextureAtlasAccessor)atlasEntry).getTexturesByName().forEach((textureLocation, textureSprite) -> {
                        list.add(BlackMagick.identifierToString(textureLocation));
                    });
                });
                sortUnique(list);
            }
        }
        return list;
    });

    public static final SuggestionGetter LIST_TRANSLATION_KEY = registerSuggsList("LIST_TRANSLATION_KEY", () -> {
        List<String> list = createOrGetCacheList("LIST_TRANSLATION_KEY", true);
        if (list.isEmpty()) {
            final Language lang = Language.getInstance();
            if (lang instanceof ClientLanguage) {
                for (String i : ((ClientLanguageAccessor)lang).getTranslations().keySet())
                    if (!i.startsWith("42edit."))
                        list.add(i);
                sortUnique(list);
            }
        }
        return list;
    });


    // static registry lists

    private static List<String> getRegistryIfEmpty(List<String> list, Registry<?> registryRef) {
        if (list.isEmpty()) {
            for (Identifier i : registryRef.keySet())
                list.add(BlackMagick.identifierToString(i));
            sortUnique(list);
        }
        return list;
    }

    public static final SuggestionGetter REGISTRY_ATTRIBUTE = registerSuggsList("REGISTRY_ATTRIBUTE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ATTRIBUTE", false), BuiltInRegistries.ATTRIBUTE));

    public static final SuggestionGetter REGISTRY_BLOCK = registerSuggsList("REGISTRY_BLOCK", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_BLOCK", false), BuiltInRegistries.BLOCK));

    public static final SuggestionGetter REGISTRY_BLOCK_ENTITY_TYPE = registerSuggsList("REGISTRY_BLOCK_ENTITY_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_BLOCK_ENTITY_TYPE", false), BuiltInRegistries.BLOCK_ENTITY_TYPE));

    public static final SuggestionGetter REGISTRY_CONSUME_EFFECT_TYPE = registerSuggsList("REGISTRY_CONSUME_EFFECT_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_CONSUME_EFFECT_TYPE", false), BuiltInRegistries.CONSUME_EFFECT_TYPE));

    public static final SuggestionGetter REGISTRY_DIALOG_TYPE = registerSuggsList("REGISTRY_DIALOG_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_DIALOG_TYPE", false), BuiltInRegistries.DIALOG_TYPE));

    public static final SuggestionGetter REGISTRY_ITEM = registerSuggsList("REGISTRY_ITEM", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ITEM", false), BuiltInRegistries.ITEM));

    public static final SuggestionGetter REGISTRY_MAP_DECORATION_TYPE = registerSuggsList("REGISTRY_MAP_DECORATION_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_MAP_DECORATION_TYPE", false), BuiltInRegistries.MAP_DECORATION_TYPE));

    public static final SuggestionGetter REGISTRY_SOUND_EVENT = registerSuggsList("REGISTRY_SOUND_EVENT", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_SOUND_EVENT", false), BuiltInRegistries.SOUND_EVENT));

    public static final SuggestionGetter REGISTRY_STATUS_EFFECT = registerSuggsList("REGISTRY_STATUS_EFFECT", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_STATUS_EFFECT", false), BuiltInRegistries.MOB_EFFECT));

    public static final SuggestionGetter REGISTRY_ENTITY_TYPE = registerSuggsList("REGISTRY_ENTITY_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_ENTITY_TYPE", false), BuiltInRegistries.ENTITY_TYPE));

    public static final SuggestionGetter REGISTRY_PARTICLE_TYPE = registerSuggsList("REGISTRY_PARTICLE_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_PARTICLE_TYPE", false), BuiltInRegistries.PARTICLE_TYPE));

    public static final SuggestionGetter REGISTRY_POTION = registerSuggsList("REGISTRY_POTION", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_POTION", false), BuiltInRegistries.POTION));

    public static final SuggestionGetter REGISTRY_VILLAGER_TYPE = registerSuggsList("REGISTRY_VILLAGER_TYPE", () ->
        getRegistryIfEmpty(createOrGetCacheList("REGISTRY_VILLAGER_TYPE", false), BuiltInRegistries.VILLAGER_TYPE));


    // dynamic data lists

    private static List<String> getDataIfEmpty(List<String> list, ResourceKey<? extends Registry<?>> registryRef) {
        if (list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.level != null)
                BlackMagick.getRegistryAccess(client).lookup(registryRef).ifPresent(reg -> {
                    for (Identifier i : reg.keySet())
                        list.add(BlackMagick.identifierToString(i));
                });
            sortUnique(list);
        }
        return list;
    }

    public static final SuggestionGetter DATA_BANNER_PATTERN = registerSuggsList("DATA_BANNER_PATTERN", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_BANNER_PATTERN", true), Registries.BANNER_PATTERN));

    public static final SuggestionGetter DATA_CAT_SOUND_VARIANT = registerSuggsList("DATA_CAT_SOUND_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_CAT_SOUND_VARIANT", true), Registries.CAT_SOUND_VARIANT));

    public static final SuggestionGetter DATA_CAT_VARIANT = registerSuggsList("DATA_CAT_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_CAT_VARIANT", true), Registries.CAT_VARIANT));

    public static final SuggestionGetter DATA_CHICKEN_SOUND_VARIANT = registerSuggsList("DATA_CHICKEN_SOUND_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_CHICKEN_SOUND_VARIANT", true), Registries.CHICKEN_SOUND_VARIANT));

    public static final SuggestionGetter DATA_CHICKEN_VARIANT = registerSuggsList("DATA_CHICKEN_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_CHICKEN_VARIANT", true), Registries.CHICKEN_VARIANT));

    public static final SuggestionGetter DATA_COW_SOUND_VARIANT = registerSuggsList("DATA_COW_SOUND_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_COW_SOUND_VARIANT", true), Registries.COW_SOUND_VARIANT));

    public static final SuggestionGetter DATA_COW_VARIANT = registerSuggsList("DATA_COW_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_COW_VARIANT", true), Registries.COW_VARIANT));

    public static final SuggestionGetter DATA_DAMAGE_TYPE = registerSuggsList("DATA_DAMAGE_TYPE", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_DAMAGE_TYPE", true), Registries.DAMAGE_TYPE));

    public static final SuggestionGetter DATA_DIALOG = registerSuggsList("DATA_DIALOG", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_DIALOG", true), Registries.DIALOG));

    public static final SuggestionGetter DATA_ENCHANTMENT = registerSuggsList("DATA_ENCHANTMENT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_ENCHANTMENT", true), Registries.ENCHANTMENT));

    public static final SuggestionGetter DATA_FROG_VARIANT = registerSuggsList("DATA_FROG_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_FROG_VARIANT", true), Registries.FROG_VARIANT));

    public static final SuggestionGetter DATA_INSTRUMENT = registerSuggsList("DATA_INSTRUMENT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_INSTRUMENT", true), Registries.INSTRUMENT));

    public static final SuggestionGetter DATA_JUKEBOX_SONG = registerSuggsList("DATA_JUKEBOX_SONG", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_JUKEBOX_SONG", true), Registries.JUKEBOX_SONG));

    public static final SuggestionGetter DATA_PAINTING_VARIANT = registerSuggsList("DATA_PAINTING_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_PAINTING_VARIANT", true), Registries.PAINTING_VARIANT));

    public static final SuggestionGetter DATA_PIG_SOUND_VARIANT = registerSuggsList("DATA_PIG_SOUND_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_PIG_SOUND_VARIANT", true), Registries.PIG_SOUND_VARIANT));

    public static final SuggestionGetter DATA_PIG_VARIANT = registerSuggsList("DATA_PIG_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_PIG_VARIANT", true), Registries.PIG_VARIANT));

    public static final SuggestionGetter DATA_TRIM_MATERIAL = registerSuggsList("DATA_TRIM_MATERIAL", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_TRIM_MATERIAL", true), Registries.TRIM_MATERIAL));

    public static final SuggestionGetter DATA_TRIM_PATTERN = registerSuggsList("DATA_TRIM_PATTERN", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_TRIM_PATTERN", true), Registries.TRIM_PATTERN));

    public static final SuggestionGetter DATA_WOLF_SOUND_VARIANT = registerSuggsList("DATA_WOLF_SOUND_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_WOLF_SOUND_VARIANT", true), Registries.WOLF_SOUND_VARIANT));

    public static final SuggestionGetter DATA_WOLF_VARIANT = registerSuggsList("DATA_WOLF_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_WOLF_VARIANT", true), Registries.WOLF_VARIANT));

    public static final SuggestionGetter DATA_ZOMBIE_NAUTILUS_VARIANT = registerSuggsList("DATA_ZOMBIE_NAUTILUS_VARIANT", () ->
        getDataIfEmpty(createOrGetCacheList("DATA_ZOMBIE_NAUTILUS_VARIANT", true), Registries.ZOMBIE_NAUTILUS_VARIANT));


    // dynamic data tags lists

    private static List<String> getTagsIfEmpty(List<String> list, ResourceKey<? extends Registry<?>> registryRef) {
        if (list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.level != null)
                BlackMagick.getRegistryAccess(client).lookup(registryRef).ifPresent(reg -> {
                    reg.listTagIds().forEach(tag -> {
                        list.add("#" + BlackMagick.identifierToString(tag.location()));
                    });
                });
            sortUnique(list);
        }
        return list;
    }

    public static final SuggestionGetter DATA_TAG_BANNER_PATTERN = registerSuggsList("DATA_TAG_BANNER_PATTERN", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_BANNER_PATTERN", true), Registries.BANNER_PATTERN));

    public static final SuggestionGetter DATA_TAG_BLOCK = registerSuggsList("DATA_TAG_BLOCK", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_BLOCK", true), Registries.BLOCK));

    public static final SuggestionGetter DATA_TAG_DAMAGE_TYPE = registerSuggsList("DATA_TAG_DAMAGE_TYPE", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_DAMAGE_TYPE", true), Registries.DAMAGE_TYPE));

    public static final SuggestionGetter DATA_TAG_DIALOG = registerSuggsList("DATA_TAG_DIALOG", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_DIALOG", true), Registries.DIALOG));

    public static final SuggestionGetter DATA_TAG_ENCHANTMENT = registerSuggsList("DATA_TAG_ENCHANTMENT", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ENCHANTMENT", true), Registries.ENCHANTMENT));

    public static final SuggestionGetter DATA_TAG_ENTITY_TYPE = registerSuggsList("DATA_TAG_ENTITY_TYPE", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ENTITY_TYPE", true), Registries.ENTITY_TYPE));

    public static final SuggestionGetter DATA_TAG_ITEM = registerSuggsList("DATA_TAG_ITEM", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_ITEM", true), Registries.ITEM));

    public static final SuggestionGetter DATA_TAG_PAINTING_VARIANT = registerSuggsList("DATA_TAG_PAINTING_VARIANT", () ->
        getTagsIfEmpty(createOrGetCacheList("DATA_TAG_PAINTING_VARIANT", true), Registries.PAINTING_VARIANT));


    // dynamic data tags entry lists

    private static List<String> getItemsInTag(List<String> list, String tag) {
        if (list.isEmpty()) {
            final Minecraft client = Minecraft.getInstance();
            if (client.level != null)
                BlackMagick.getRegistryAccess(client).lookup(Registries.ITEM).ifPresent(reg -> {
                    for (Holder<Item> itemHolder : reg.getTagOrEmpty(TagKey.create(Registries.ITEM, Identifier.parse(tag)))) {
                        list.add(BlackMagick.itemToStringId(itemHolder.value()));
                    }
                });
            sortUnique(list);
        }
        return list;
    }

    public static final SuggestionGetter DATA_TAG_ENTRY_DECORATED_POT_INGREDIENTS = registerSuggsList("DATA_TAG_ENTRY_DECORATED_POT_INGREDIENTS", () ->
        getItemsInTag(createOrGetCacheList("DATA_TAG_ENTRY_DECORATED_POT_INGREDIENTS", true), "decorated_pot_ingredients"));


    // static vanilla resources lists

    private static List<String> getVanillaResourcesIfEmpty(List<String> list, String path, String suffix, boolean isAssets) {
        if (list.isEmpty()) {
            try {
                PackType packType = isAssets ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA;
                PackResources resources = ServerPacksSource.createVanillaPackSource().fullResources();
                for (String namespace : resources.getNamespaces(packType)) {
                    Map<Identifier,IoSupplier<InputStream>> map = Maps.newHashMap();
                    resources.listResources(packType, namespace, path, map::putIfAbsent);
                    map.keySet().forEach(i -> {
                        String temp = BlackMagick.identifierToString(i);
                        if (!temp.endsWith(suffix + MCMETA_SUFFIX)) {
                            if (temp.startsWith(namespace + ":" + path + "/") && temp.endsWith(suffix) && temp.length() > (namespace.length() + 1 + path.length() + 1 + suffix.length())) {
                                temp = namespace + ":" + temp.substring(namespace.length() + 1 + path.length() + 1, temp.length() - suffix.length());
                                if (!isAssets && path.startsWith("tags/"))
                                    temp = "#" + temp;
                                list.add(temp);
                            }
                            else {
                                FortytwoEdit.logWarn("Failed to add vanilla " + (isAssets ? "assets" : "data") + " path to list: " + BlackMagick.identifierToString(i));
                            }
                        }
                    });
                }
            } catch (Exception ex) {}
            sortUnique(list);
        }
        return list;
    }

    protected final static String JSON_SUFFIX = ".json";
    protected final static String NBT_SUFFIX = ".nbt";
    protected final static String PNG_SUFFIX = ".png";
    protected final static String MCMETA_SUFFIX = ".mcmeta";
    
    // data

    public static final SuggestionGetter DATA_LOOT_TABLE = registerSuggsList("DATA_LOOT_TABLE", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("DATA_LOOT_TABLE", false), "loot_table", JSON_SUFFIX, false));

    public static final SuggestionGetter DATA_RECIPE = registerSuggsList("DATA_RECIPE", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("DATA_RECIPE", false), "recipe", JSON_SUFFIX, false));

    public static final SuggestionGetter DATA_STRUCTURE = registerSuggsList("DATA_STRUCTURE", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("DATA_STRUCTURE", false), "structure", NBT_SUFFIX, false)); // to_do if made dynamic, update structure block screen to refresh dynamic suggs

    public static final SuggestionGetter DATA_TRIAL_SPAWNER = registerSuggsList("DATA_TRIAL_SPAWNER", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("DATA_TRIAL_SPAWNER", false), "trial_spawner", JSON_SUFFIX, false));

    // assets

    public static final SuggestionGetter ASSETS_EQUIPMENT = registerSuggsList("ASSETS_EQUIPMENT", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("ASSETS_EQUIPMENT", false), "equipment", JSON_SUFFIX, true));

    public static final SuggestionGetter ASSETS_FONT = registerSuggsList("ASSETS_FONT", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("ASSETS_FONT", false), "font", JSON_SUFFIX, true));

    public static final SuggestionGetter ASSETS_ITEMS = registerSuggsList("ASSETS_ITEMS", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("ASSETS_ITEMS", false), "items", JSON_SUFFIX, true));

    public static final SuggestionGetter ASSETS_TEXTURES = registerSuggsList("ASSETS_TEXTURES", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("ASSETS_TEXTURES", false), "textures", PNG_SUFFIX, true));

    public static final SuggestionGetter ASSETS_TEXTURES_PAINTING = registerSuggsList("ASSETS_TEXTURES_PAINTING", () ->
        getVanillaResourcesIfEmpty(createOrGetCacheList("ASSETS_TEXTURES_PAINTING", false), "textures/painting", PNG_SUFFIX, true));


}
