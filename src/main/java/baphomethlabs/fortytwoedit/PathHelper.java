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
        return getPath(element, PathInfoGetter.of("ITEM_STACK").get(), path);
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

        registerPathInfo("ITEM_STACK", PathInfo.create(DataType.CompoundStructured.of(Map.of(
            "id", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING, SuggestionHelper.REGISTRY_ITEM)).getter()
            ),Map.of(
            "count", PathInfo.create(DataType.ElementLiteral.of(NbtType.INT)).getter(),//to_do suggs
            "components", PathInfoGetter.of("COMPONENTS")
        ))).setIcon(Items.STONE));//to_do info

        registerPathInfo("COMPONENTS", PathInfo.create(DataType.CompoundStructured.allOptional(Map.of(
            "minecraft:attribute_modifiers", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.of(Map.of(
                    "type", PathInfo.create().getter(),
                    "id", PathInfo.create().getter(),
                    "amount", PathInfo.create().getter(),
                    "operation", PathInfo.create().getter()
                    ),Map.of(
                    "slot", PathInfo.create().getter()
                ))).getter()
            )).setIcon(Items.DIAMOND_SWORD).getter(),//to_do info
            "minecraft:banner_patterns", PathInfo.create(DataType.ListUnordered.of(
                PathInfo.create(DataType.CompoundStructured.allRequired(Map.of(
                    "color", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.LIST_DYE_COLOR)).getter(),//to_do info
                    "pattern", PathInfo.create(DataType.ElementLiteral.of(NbtType.STRING,SuggestionHelper.DATA_BANNER_PATTERN)).getter()//to_do info
                ))).getter()
            )).setIcon(Items.WHITE_BANNER).getter(),//to_do info
            "minecraft:custom_name", PathInfoGetter.of("TEXT_COMPONENT"),//to_do info
            "minecraft:item_name", PathInfoGetter.of("TEXT_COMPONENT"),//to_do info
            "minecraft:lore", PathInfo.create(DataType.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))).setIcon(Items.NAME_TAG).getter(),//to_do info
            "minecraft:unbreakable", PathInfo.create(DataType.Unit.create()).setIcon(Items.DIAMOND_PICKAXE).getter()//to_do info
        ))));

        registerPathInfo("TEXT_COMPONENT", PathInfo.create(//to_do add fields, suggs, infos, etc
            DataType.ElementLiteral.of(NbtType.STRING),
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
                "extra", PathInfo.create(DataType.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))).getter()
            )),
            DataType.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))
        ).setIcon(Items.NAME_TAG));

    }

    private static final Map<String, PathInfo> PATH_INFO_REF_MAP = Maps.newHashMap();

    protected static PathInfo getRegisteredPathInfo(String refKey) {
        if(PATH_INFO_REF_MAP.isEmpty())
            buildPathInfos();
        if(PATH_INFO_REF_MAP.containsKey(refKey))
            return PATH_INFO_REF_MAP.get(refKey);
        return PathInfo.EMPTY;
    }

    protected static PathInfoGetter registerPathInfo(String refKey, PathInfo pi) {
        PATH_INFO_REF_MAP.put(refKey, pi);
        return PathInfoGetter.of(refKey);
    }

    /**
     * Miscellaneous flags to denote a path as something specific
     */
    public enum PathFlag {

        NONE, // default, says nothing

        // used for ItemBuilder.getButtonText()
        ARMOR_STAND_POSE,
        ATTRIBUTE,
        BANNER_PATTERNS,
        DECIMAL_COLOR,
        EFFECT,
        FIREWORK,
        PROBABILITY_EFFECT,
        TEXT_COMPONENT

    }

    public static class PathInfo {

        private ItemStack icon = null;
        private Component info = null;
        private PathInfoSupplierCompound compoundSupplier = null;
        private PathInfoSupplierList listSupplier = null;
        private PathInfoSupplierElement elementSupplier = null;
        private boolean isEmpty = true;
        private PathType defaultPathType = PathType.ELEMENT;
        private boolean setDefaultPathType = false;

        private SuggestionGetter allSuggs = SuggestionGetter.empty();
        private boolean cacheAllSuggs = false;
        private List<NbtType> allNbtTypes = Lists.newArrayList();
        private boolean cacheAllNbtTypes = false;

        private static final PathInfo EMPTY = create();

        private PathInfo() {}

        public Component getInfo() {
            return info;
        }

        public ItemStack getIcon() {
            return icon;
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
            return null;
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

        public boolean isEmpty() {
            return isEmpty;
        }

        public static PathInfo create() {
            return new PathInfo();
        }

        public static PathInfo create(PathInfoSupplier... info) {
            PathInfo newInfo = create();
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

        public PathInfo setCompoundInfo(PathInfoSupplierCompound info) {
            this.compoundSupplier = info;
            setSupplierInfo(info);
            return this;
        }

        public PathInfo setListInfo(PathInfoSupplierList info) {
            this.listSupplier = info;
            setSupplierInfo(info);
            return this;
        }

        public PathInfo setElementInfo(PathInfoSupplierElement info) {
            this.elementSupplier = info;
            setSupplierInfo(info);
            return this;
        }

        private void setSupplierInfo(PathInfoSupplier info) {
            if(!this.setDefaultPathType) {
                this.defaultPathType = info.getPathType();
                this.setDefaultPathType = true;
            }
            this.isEmpty = false;
        }

        public PathInfoGetter getter() {
            return PathInfoGetter.of(this);
        }

    }

    private interface PathInfoSupplier {

        public SuggestionGetter getSuggs();
        public PathType getPathType();

    }

    private static abstract class PathInfoSupplierCompound implements PathInfoSupplier {

        private static final SuggestionGetter SUGGS = SuggestionGetter.newInline("{}");

        public SuggestionGetter getSuggs() {
            return SUGGS;
        }

        public PathType getPathType() {
            return PathType.COMPLEX;
        }

        public abstract KeyGetter getCompoundKeys(CompoundTag compound);

        public abstract PathInfo getCompoundKeyInfo(CompoundTag compound, String key);

    }

    private static abstract class PathInfoSupplierList implements PathInfoSupplier {

        private static final SuggestionGetter SUGGS = SuggestionGetter.newInline("[]");

        public SuggestionGetter getSuggs() {
            return SUGGS;
        }

        public PathType getPathType() {
            return PathType.COMPLEX;
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
                return null;
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
                return of(nbtType, SuggestionGetter.newInline(nbtType.suggs()));
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

        COMPLEX
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
