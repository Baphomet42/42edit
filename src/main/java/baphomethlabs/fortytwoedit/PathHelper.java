package baphomethlabs.fortytwoedit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.utils.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import baphomethlabs.fortytwoedit.ComponentHelper.PathType;
import baphomethlabs.fortytwoedit.ComponentHelper.SuggestionGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PathHelper {

    public static PathInfoGetter getItemPath(PathNode[] path) {
        return getPath(PathInfos.ITEM_NODE, path);
    }

    public static PathInfoGetter getComponentPath(PathNode[] path) {
        return getPath(PathInfos.COMPONENTS, path);
    }

    public static PathInfoGetter getPath(PathInfoGetter context, PathNode[] path) {
        if(path != null) {
            List<PathNode> currentPath = Lists.newArrayList();
            currentPath.addAll(List.of(path));
            PathInfoGetter currentContext = context;
            while(!currentPath.isEmpty() && currentContext != null) {
                PathInfo[] pies = currentContext.getInfo();
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
        return PathInfos.UNKNOWN;
    }

    public static abstract class PathInfo {

        public final PathType type;

        private PathInfo(PathType type) {
            this.type = type;
        }

        public PathInfoGetter getter() {
            return PathInfoGetter.of(this);
        }

        public PathInfoGetter getPath(PathNode path) {
            return null;
        }

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

        public final PathInfoGetter entry;

        private PathInfoList(PathInfoGetter entry) {
            super(PathType.LIST);
            this.entry = entry;
        }

        public static PathInfoList of(PathInfoGetter entry) {
            return new PathInfoList(entry);
        }

        @Override
        public PathInfoGetter getPath(PathNode path) {
            if(!path.isKey())
                return entry;
            return null;
        }

    }

    public static class PathInfoLiteral extends PathInfo {

        public final SuggestionGetter suggs;

        private PathInfoLiteral(PathType type, SuggestionGetter suggs) {
            super(type);
            this.suggs = suggs;
        }

        public static PathInfoLiteral of(PathType type) {
            return new PathInfoLiteral(type, null);
        }

        public static PathInfoLiteral of(PathType type, SuggestionGetter suggs) {
            return new PathInfoLiteral(type, suggs);
        }

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
            for(PathInfo pi : getInfo()) {
                if(ComponentHelper.pathTypeToNbtType(pi.type) == nbtType)
                    return pi;
            }
            return null;
        }

        public PathInfo[] getInfo() {
            List<PathInfo> pie = Lists.newArrayList();
            if(inlined != null)
                for(PathInfo pi : inlined)
                    pie.add(pi);
            if(refKeys != null)
                for(String k : refKeys)
                    for(PathInfo pi : PATH_INFO_REF_MAP.get(k).getInfo())
                        pie.add(pi);
            return pie.toArray(new PathInfo[0]);
        }

    } 

    private static class PathInfos {

        private static final PathInfoGetter UNIT = PathInfoLiteral.of(PathType.UNIT,SuggestionGetter.newInline("","{}")).getter().withDesc(Component.nullToEmpty("{} represents true"));

        private static final PathInfoGetter UNKNOWN = registerPathInfo("UNKNOWN", PathInfoLiteral.of(PathType.UNKNOWN));
        private static final PathInfoGetter DEFAULT = registerPathInfo("DEFAULT", PathInfoLiteral.of(PathType.DEFAULT));

        private static final PathInfoGetter ITEM_NODE = registerPathInfo("ITEM_NODE", PathInfoCompoundStructured.of(Map.of(
            "id", PathInfoLiteral.of(PathType.STRING).getter()
        ),Map.of(
            "count", PathInfoLiteral.of(PathType.INT).getter(),
            "components", (PathInfoGetter.of("COMPONENTS"))
        )));

        private static final PathInfoGetter COMPONENTS = registerPathInfo("COMPONENTS", PathInfoCompoundStructured.allOptional(Map.of(
            "minecraft:custom_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:item_name", PathInfoGetter.of("TEXT_COMPONENT"),
            "minecraft:lore", PathInfoList.of(PathInfoGetter.of("TEXT_COMPONENT")).getter(),
            "minecraft:unbreakable", UNIT
        )));

        private static final PathInfoGetter TEXT_COMPONENT = registerPathInfo("TEXT_COMPONENT", PathInfoGetter.of(
            PathInfoLiteral.of(PathType.STRING),
            PathInfoCompoundStructured.of(Map.of(
                "text", PathInfoLiteral.of(PathType.STRING).getter()
            ),Map.of(
                "color", DEFAULT
            )),
            PathInfoList.of(PathInfoGetter.of("TEXT_COMPONENT"))
        ));

    }
    
}
