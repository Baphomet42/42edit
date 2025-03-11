package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.ComponentHelper.KeyGetter;
import baphomethlabs.fortytwoedit.ComponentHelper.SuggestionGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PathHelper {

    public static PathInfo getItemPath(Tag element, PathNode[] path) {
        return getPath(element, PathInfoGetter.of("ITEM_STACK").get(), path);
    }

    public static PathInfo getPath(Tag element, PathInfo context, PathNode[] path) {
        if(path != null) {
            List<PathNode> currentPath = Lists.newArrayList();
            currentPath.addAll(List.of(path));
            PathInfo currentContext = context;
            while(!currentPath.isEmpty() && currentContext != null) {
                currentContext = currentContext.getNode(element, currentPath.get(0));
                currentPath.remove(0);
            }
            if(currentContext != null && currentPath.isEmpty())
                return currentContext;
        }
        return PathInfo.EMPTY;
    }

    public static class PathInfo {

        private ItemStack icon = null;
        private Component info = null;
        private PathInfoSupplierCompound compoundSupplier = null;
        private PathInfoSupplierList listSupplier = null;
        private PathInfoSupplierElement elementSupplier = null;
        private SuggestionGetter allSuggs = SuggestionGetter.empty();
        private boolean cacheSuggs = false;
        private boolean isEmpty = true;
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

        public PathType getPathType(byte nbtType) {
            return PathType.ELEMENT;
        }

        public SuggestionGetter getSuggs() {
            if(cacheSuggs)
                return allSuggs;
            cacheSuggs = true;

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

        public PathInfo setInfo(Component info) {
            this.info = info;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setCompoundInfo(PathInfoSupplierCompound info) {
            this.compoundSupplier = info;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setListInfo(PathInfoSupplierList info) {
            this.listSupplier = info;
            this.isEmpty = false;
            return this;
        }

        public PathInfo setElementInfo(PathInfoSupplierElement info) {
            this.elementSupplier = info;
            this.isEmpty = false;
            return this;
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
            return PathType.COMPOUND;
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

    }

    private static class PathInfoSuppliers {

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
    
            private final PathType type;
            private final SuggestionGetter SUGGS;
    
            private ElementLiteral(PathType type, SuggestionGetter suggs) {
                this.type = type;
                this.SUGGS = suggs;
            }
    
            public static ElementLiteral of(byte nbtType) {
                return new ElementLiteral(PathType.ELEMENT, SuggestionGetter.newInline(ComponentHelper.defaultNbtType(nbtType)));
            }
    
            public static ElementLiteral of(PathType type, SuggestionGetter suggs) {
                return new ElementLiteral(type, suggs);
            }
    
            @Override
            public SuggestionGetter getSuggs() {
                return SUGGS;
            }
    
            @Override
            public PathType getPathType() {
                return type;
            }
    
        }
    
        public static class Bool extends ElementLiteral {
    
            private static final SuggestionGetter BOOLEAN_SUGGS = SuggestionGetter.newInline("true","false");
    
            private Bool() {
                super(PathType.BOOLEAN, BOOLEAN_SUGGS);
            }
    
            public static Bool create() {
                return new Bool();
            }
    
        }

    }

    public enum PathType {
        ELEMENT,

        COMPOUND,
        LIST,
        STRING,

        UNIT,
        BOOLEAN
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

    private static void buildPathInfos() {
        PATH_INFO_REF_MAP.clear();

        registerPathInfo("ITEM_STACK", PathInfo.create(PathInfoSuppliers.CompoundStructured.of(Map.of(
            "id", PathInfo.create(PathInfoSuppliers.ElementLiteral.of(PathType.STRING, ComponentHelper.REGISTRY_ITEM)).getter()
        ),Map.of(
            "count", PathInfo.create(PathInfoSuppliers.ElementLiteral.of(Tag.TAG_INT)).getter(),
            "components", PathInfoGetter.of("COMPONENTS")
        ))));

        registerPathInfo("COMPONENTS", PathInfo.create(PathInfoSuppliers.CompoundStructured.allOptional(Map.of(
            "minecraft:custom_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:item_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:lore", PathInfo.create(PathInfoSuppliers.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))).getter(),
            "minecraft:unbreakable", PathInfo.create(PathInfoSuppliers.Unit.create()).getter()
        ))));

        registerPathInfo("TEXT_COMPONENT", PathInfo.create(
            PathInfoSuppliers.ElementLiteral.of(Tag.TAG_STRING),
            PathInfoSuppliers.CompoundStructured.of(Map.of(
                "text", PathInfo.create(PathInfoSuppliers.ElementLiteral.of(Tag.TAG_STRING)).getter()
            ),Map.of(
                "color", PathInfo.create(PathInfoSuppliers.ElementLiteral.of(PathType.STRING, ComponentHelper.LIST_FORMATTING_COLOR)).getter(),
                "font", PathInfo.create(PathInfoSuppliers.ElementLiteral.of(PathType.STRING, ComponentHelper.ASSETS_FONT)).getter(),
                "bold", PathInfo.create(PathInfoSuppliers.Bool.create()).getter(),
                "italic", PathInfo.create(PathInfoSuppliers.Bool.create()).getter(),
                "underlined", PathInfo.create(PathInfoSuppliers.Bool.create()).getter(),
                "strikethrough", PathInfo.create(PathInfoSuppliers.Bool.create()).getter(),
                "obfuscated", PathInfo.create(PathInfoSuppliers.Bool.create()).getter(),
                "extra", PathInfo.create(PathInfoSuppliers.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))).getter()
            )),
            PathInfoSuppliers.ListUnordered.of(PathInfoGetter.of("TEXT_COMPONENT"))
        ));

    }
    
}
