package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.ComponentHelper.SuggestionGetter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PathHelper {

    public static PathInfoGetter getItemPath(PathNode[] path) {
        return getPath(PathInfos.ITEM_NODE, path);
    }

    public static PathInfoGetter getPath(PathInfoGetter context, PathNode[] path) {
        if(PATH_INFO_REF_MAP.isEmpty())
            buildPathInfos();

        if(path != null) {
            List<PathNode> currentPath = Lists.newArrayList();
            currentPath.addAll(List.of(path));
            PathInfoGetter currentContext = context;
            while(!currentPath.isEmpty() && currentContext != null) {
                PathInfo[] pies = currentContext.getAllInfo();
                currentContext = null;
                for(PathInfo pi : pies) {
                    PathInfoGetter pig = pi.getPath(currentPath.get(0));
                    if(pig != null) {
                        currentContext = pig;
                        break;
                    }
                }
                currentPath.remove(0);
            }
            if(currentContext != null)
                return currentContext;
        }
        return PathInfoGetter.empty();
    }

    public static abstract class PathInfo {

        public final PathType type;
        public final SuggestionGetter suggs;

        private PathInfo(PathType type) {
            this(type, null);
        }

        private PathInfo(PathType type, SuggestionGetter suggs) {
            this.type = type;
            this.suggs = suggs;
        }

        public PathInfoGetter getter() {
            return PathInfoGetter.of(this);
        }

        public PathInfoGetter getPath(PathNode path) {
            return null;
        }

        public abstract Optional<Byte> getNbtType();

    }

    public static abstract class PathInfoCompound extends PathInfo {

        public abstract Set<String> getRequired();
        public abstract Set<String> getOptional();
        public abstract PathInfoGetter getKeyInfo(String key);

        private PathInfoCompound() {
            super(PathType.COMPOUND);
        }

        @Override
        public PathInfoGetter getPath(PathNode path) {
            if(path.isKey())
                return getKeyInfo(path.key());
            return null;
        }

        public Optional<Byte> getNbtType() {
            return Optional.of(Tag.TAG_COMPOUND);
        }

    }

    public static class PathInfoCompoundStructured extends PathInfoCompound {

        public final Map<String, PathInfoGetter> keys;
        public final Set<String> requiredKeys;
        public final Set<String> optionalKeys;

        private PathInfoCompoundStructured(Map<String, PathInfoGetter> required, Map<String, PathInfoGetter> optional) {
            super();
            this.keys = Maps.newHashMap();
            this.requiredKeys = Sets.newHashSet();
            this.optionalKeys = Sets.newHashSet();
            if(optional != null)
                for(String s : optional.keySet()) {
                    this.keys.put(s, optional.get(s));
                    this.optionalKeys.add(s);
                }
            if(required != null)
                for(String s : required.keySet()) {
                    this.keys.put(s, required.get(s));
                    this.requiredKeys.add(s);
                    this.optionalKeys.remove(s);
                }
        }

        public static PathInfoCompoundStructured of(Map<String, PathInfoGetter> required, Map<String, PathInfoGetter> optional) {
            return new PathInfoCompoundStructured(required, optional);
        }

        public static PathInfoCompoundStructured allOptional(Map<String, PathInfoGetter> optional) {
            return new PathInfoCompoundStructured(null, optional);
        }

        public static PathInfoCompoundStructured allRequired(Map<String, PathInfoGetter> required) {
            return new PathInfoCompoundStructured(required, null);
        }

        public Set<String> getRequired() {
            return requiredKeys;
        }

        public Set<String> getOptional() {
            return optionalKeys;
        }

        public PathInfoGetter getKeyInfo(String key) {
            if(keys.containsKey(key))
                return keys.get(key);
            else
                return null;
        }

    }

    public static class PathInfoList extends PathInfo {

        public final PathInfoGetter entryInfo;

        private PathInfoList(PathInfoGetter entry) {
            super(PathType.LIST);
            this.entryInfo = entry;
        }

        public static PathInfoList of(PathInfoGetter entry) {
            return new PathInfoList(entry);
        }

        @Override
        public PathInfoGetter getPath(PathNode path) {
            if(!path.isKey())
                return entryInfo;
            return null;
        }

        public Optional<Byte> getNbtType() {
            return Optional.of(Tag.TAG_LIST);
        }

    }

    public static class PathInfoLiteral extends PathInfo {

        public final byte nbtType;

        private PathInfoLiteral(byte type, SuggestionGetter suggs) {
            super(PathType.ELEMENT, suggs);
            this.nbtType = type;
        }

        public static PathInfoLiteral of(byte type) {
            return new PathInfoLiteral(type, null);
        }

        public static PathInfoLiteral of(byte type, SuggestionGetter suggs) {
            return new PathInfoLiteral(type, suggs);
        }

        public Optional<Byte> getNbtType() {
            return Optional.of(nbtType);
        }

    }

    public static class PathInfoSpecial extends PathInfo {

        public final Optional<Byte> nbtType;

        private PathInfoSpecial(PathType type, SuggestionGetter suggs, Optional<Byte> nbtType) {
            super(type, suggs);
            this.nbtType = nbtType;
        }

        public static PathInfoSpecial unknown() {
            return new PathInfoSpecial(PathType.ELEMENT, null, Optional.empty());
        }

        public static PathInfoSpecial unit() {
            return new PathInfoSpecial(PathType.UNIT, SuggestionGetter.newInline("{}"), Optional.of(Tag.TAG_COMPOUND));
        }

        public static PathInfoSpecial bool() {
            return new PathInfoSpecial(PathType.BOOLEAN, SuggestionGetter.newInline("true","false"), Optional.of(Tag.TAG_BYTE));
        }

        public Optional<Byte> getNbtType() {
            return nbtType;
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

    private static final Map<String, PathInfoGetter> PATH_INFO_REF_MAP = Maps.newHashMap();

    protected static PathInfoGetter registerPathInfo(String refKey, PathInfo pi) {
        PATH_INFO_REF_MAP.put(refKey, PathInfoGetter.of(pi));
        return PathInfoGetter.of(refKey);
    }

    protected static PathInfoGetter registerPathInfo(String refKey, PathInfoGetter pi) {
        PATH_INFO_REF_MAP.put(refKey, pi);
        return PathInfoGetter.of(refKey);
    }

    public record PathInfoGetter(PathInfo[] inlined, String[] refKeys, Component desc, ItemStack icon) {

        public static PathInfoGetter of(PathInfo... options) {
            return new PathInfoGetter(options, null, null, null);
        }

        public static PathInfoGetter of(String... options) {
            return new PathInfoGetter(null, options, null, null);
        }

        public PathInfoGetter withDesc(Component description) {
            return new PathInfoGetter(this.inlined, this.refKeys, description, this.icon);
        }

        public PathInfoGetter withIcon(ItemStack icon) {
            return new PathInfoGetter(this.inlined, this.refKeys, this.desc, icon);
        }

        public PathInfo getInfo(byte nbtType) {
            PathInfo[] pies = getAllInfo();
            for(PathInfo pi : pies) {
                if(pi.getNbtType().isPresent() && pi.getNbtType().get() == nbtType)
                    return pi;
            }
            return null;
        }

        public PathInfoCompound getInfoCompound() {
            PathInfo pi = getInfo(Tag.TAG_COMPOUND);
            if(pi instanceof PathInfoCompound)
                return (PathInfoCompound)pi;
            return null;
        }

        public PathInfoList getInfoList() {
            PathInfo pi = getInfo(Tag.TAG_LIST);
            if(pi instanceof PathInfoList)
                return (PathInfoList)pi;
            return null;
        }

        public PathInfo getInfo() {
            PathInfo[] pies = getAllInfo();
            if(pies.length>0)
                return pies[0];
            return null;
        }

        public PathInfo[] getAllInfo() {
            List<PathInfo> pie = Lists.newArrayList();
            if(inlined != null)
                for(PathInfo pi : inlined)
                    pie.add(pi);
            if(refKeys != null)
                for(String k : refKeys)
                    for(PathInfo pi : PATH_INFO_REF_MAP.get(k).getAllInfo())
                        pie.add(pi);
            return pie.toArray(new PathInfo[0]);
        }

        public static PathInfoGetter empty() {
            return new PathInfoGetter(null, null, null, null);
        }

    }

    private static class PathInfos {
        private static final PathInfoGetter UNIT = PathInfoSpecial.unit().getter().withDesc(Component.nullToEmpty("{} represents true"));
        private static final PathInfoGetter ITEM_NODE = PathInfoCompoundStructured.of(Map.of(
            "id", PathInfoLiteral.of(Tag.TAG_STRING, ComponentHelper.REGISTRY_ITEM).getter()
        ),Map.of(
            "count", PathInfoLiteral.of(Tag.TAG_INT).getter(),
            "components", (PathInfoGetter.of("COMPONENTS"))
        )).getter();
    }

    private static void buildPathInfos() {

        PATH_INFO_REF_MAP.clear();

        registerPathInfo("COMPONENTS", PathInfoCompoundStructured.allOptional(Map.of(
            "minecraft:custom_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:item_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:lore", PathInfoList.of(PathInfoGetter.of("TEXT_COMPONENT")).getter(),
            "minecraft:unbreakable", PathInfos.UNIT
        )));

        registerPathInfo("TEXT_COMPONENT", PathInfoGetter.of(
            PathInfoLiteral.of(Tag.TAG_STRING),
            PathInfoCompoundStructured.of(Map.of(
                "text", PathInfoLiteral.of(Tag.TAG_STRING).getter()
            ),Map.of(
                "color", PathInfoLiteral.of(Tag.TAG_STRING).getter()
            )),
            PathInfoList.of(PathInfoGetter.of("TEXT_COMPONENT"))
        ));

    }
    
}
