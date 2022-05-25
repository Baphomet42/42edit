package baphomethlabs.fortytwoedit;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import baphomethlabs.fortytwoedit.gui.ItemBuilder;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.LiteralText;

public class BlackMagick {
    

    //creates new nbtelement from input
    //returns null if nbtelement cannot be parsed
    public static NbtElement elementFromString(String inp) {
        String nbt = "{temp:"+inp+"}";
        NbtCompound temp;
        try {
            temp = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            return null;
        }
        return temp.get("temp");
    }


    /**
     * Outdated elementFromString
     * caused json reader errors (extra escape characters)
     * couldn't read some unicode characters
     * float precision issues
     * 
    //creates new nbtelement from input
    //values surrounded in "" or '' are strings
    //values surrounded in {} are compounds
    //values starting in [ and ending in ] which have I; B; or L; then a list of
    //2 or more numbers separated by commas will return correct Array (nums in B; end in b, nums in L; end in l)
    //other values surrounded in [] are lists
    //values ending in b, s, l, f, or d will return the element if input is correct type or null if not
    //bytes, shorts, ints, and floats will start as long or double, and will be set within correct range
    //all other inputs will give a double if it contains a . or else an int
    //unless it can't be parsed as a double, in which it will return null
    public static NbtElement elementFromString(String inp) {
        if(inp.length()>=2 && inp.charAt(0)=='"' && inp.charAt(inp.length()-1)=='"') {
            return NbtString.of(inp.substring(1,inp.length()-1).replaceAll("\\\"","\"").replaceAll("\\\'","\'").replaceAll("\\\\","\\"));
        }
        else if(inp.length()>=2 && inp.charAt(0)=='\'' && inp.charAt(inp.length()-1)=='\'') {
            return NbtString.of(inp.substring(1,inp.length()-1));
        }
        else if(inp.length()>=2 && inp.charAt(0)=='{' && inp.charAt(inp.length()-1)=='}') {
            try {
                return StringNbtReader.parse(inp);
            } catch (CommandSyntaxException e) {
                return null;
            } /**
            inp = inp.substring(1,inp.length()-1);
            ArrayList<String[]> parseList = new ArrayList<>();
            while(inp.length()>0) {
                String[] thisPair = new String[2];
                int bracketCount = 0;
                boolean inQuotes = false;
                boolean inSingleQuotes = false;
                if(inp.charAt(0)=='\"') {
                    if(inp.contains("\":")) {
                        thisPair[0] = inp.substring(1,inp.indexOf("\":"));
                        if(inp.length()>inp.indexOf("\":")+2)
                            inp = inp.substring(inp.indexOf("\":")+2);
                        else
                            inp = "";
                    }
                    else
                        continue;
                }
                else if(inp.contains(":")) {
                    thisPair[0] = inp.substring(0,inp.indexOf(":"));
                    if(inp.length()>inp.indexOf(":")+1)
                        inp = inp.substring(inp.indexOf(":")+1);
                    else
                        inp = "";
                }
                else
                    continue;
                if(inp.charAt(0)=='"') {
                    FIND_VALUE:
                    for(int i=1; i<inp.length(); i++) {
                        if(inp.charAt(i)=='"') {
                            thisPair[1] = inp.substring(0,i+1);
                            parseList.add(thisPair);
                            if(inp.length()>i+2)
                                inp = inp.substring(i+2);
                            else
                                inp = "";
                            break FIND_VALUE;
                        }
                        else if(inp.charAt(i)=='\\')
                            i++;
                    }
                }
                else if(inp.charAt(0)=='\'') {
                    FIND_VALUE:
                    for(int i=1; i<inp.length(); i++) {
                        if(inp.charAt(i)=='\'') {
                            thisPair[1] = inp.substring(0,i+1);
                            parseList.add(thisPair);
                            if(inp.length()>i+2)
                                inp = inp.substring(i+2);
                            else
                                inp = "";
                            break FIND_VALUE;
                        }
                        else if(inp.charAt(i)=='\\')
                            i++;
                    }
                }
                else if(inp.charAt(0)=='{') {
                    bracketCount=1;
                    FIND_VALUE:
                    for(int i=1; i<inp.length(); i++) {
                        if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='{')
                            bracketCount++;
                        else if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='}')
                            bracketCount--;
                        else if(inp.charAt(i)=='\\')
                            i++;
                        else if(inp.charAt(i)=='"' && !inSingleQuotes)
                            inQuotes = !inQuotes;
                        else if(inp.charAt(i)=='\'' && !inQuotes)
                            inSingleQuotes = !inSingleQuotes;
                        if(bracketCount==0) {
                            thisPair[1] = inp.substring(0,i+1);
                            parseList.add(thisPair);
                            if(inp.length()>i+2)
                                inp = inp.substring(i+2);
                            else
                                inp = "";
                            break FIND_VALUE;
                        }
                    }
                }
                else if(inp.charAt(0)=='[') {
                    bracketCount=1;
                    FIND_VALUE:
                    for(int i=1; i<inp.length(); i++) {
                        if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='[')
                            bracketCount++;
                        else if(!inQuotes && !inSingleQuotes && inp.charAt(i)==']')
                            bracketCount--;
                        else if(inp.charAt(i)=='\\')
                            i++;
                        else if(inp.charAt(i)=='"' && !inSingleQuotes)
                            inQuotes = !inQuotes;
                        else if(inp.charAt(i)=='\'' && !inQuotes)
                            inSingleQuotes = !inSingleQuotes; 
                        if(bracketCount==0) {
                            thisPair[1] = inp.substring(0,i+1);
                            parseList.add(thisPair);
                            if(inp.length()>i+2)
                                inp = inp.substring(i+2);
                            else
                                inp = "";
                            break FIND_VALUE;
                        }
                    }                    
                }
                else {
                    boolean commaFound = false;
                    FIND_VALUE:
                    for(int i=1; i<inp.length(); i++) {
                        if(inp.charAt(i)==',') {
                            thisPair[1] = inp.substring(0,i);
                            parseList.add(thisPair);
                            if(inp.length()>i+1)
                                inp = inp.substring(i+1);
                            else
                                inp = "";
                            commaFound=true;
                            break FIND_VALUE;
                        }
                    }
                    if(!commaFound) {
                        thisPair[1] = inp;
                        parseList.add(thisPair);
                        inp = "";
                    }
                }
                if(thisPair[0] == null || thisPair[1] == null)
                    inp = "";
            }
            NbtCompound compound = new NbtCompound();
            for(String[] pair: parseList) {
                if(elementFromString(pair[1]) != null)
                    compound.put(pair[0],elementFromString(pair[1]));
            }
            return compound;
        }
        else if(inp.length()>=2 && inp.charAt(0)=='[' && inp.charAt(inp.length()-1)==']') {
            inp = inp.substring(1,inp.length()-1);
            if((inp.toLowerCase().contains("i;") && inp.toLowerCase().indexOf("i;")==0) ||
                (inp.toLowerCase().contains("b;") && inp.toLowerCase().indexOf("b;")==0) ||
                (inp.toLowerCase().contains("l;") && inp.toLowerCase().indexOf("l;")==0)) {//number array
            inp = inp.replace(" ","");
            if(inp.length()<3 || inp.contains("."))
                return null;
            List<String> numList = new ArrayList<>();
            if(inp.toLowerCase().substring(0,2).equals("i;"))
                numList.add("I;");
            else if(inp.toLowerCase().substring(0,2).equals("b;"))
                numList.add("B;");
            else if(inp.toLowerCase().substring(0,2).equals("l;"))
                numList.add("L;");
            else
                return null;
            inp = inp.substring(2);
            if(!inp.contains(","))
                numList.add(inp);
            while(inp.contains(",")) {
                String thisString = inp.substring(0,inp.indexOf(","));
                if(!thisString.equals(""))
                    numList.add(thisString);
                if(inp.length()>inp.indexOf(",")+1) {
                    inp = inp.substring(inp.indexOf(",")+1);
                    if(!inp.contains(",") && !inp.equals(""))
                        numList.add(inp);
                }
                else
                    inp = "";
            }
            if(numList.size()<2)
                return null;
            if(numList.get(0).equals("I;")) {
                int[] array = new int[numList.size()-1];
                for(int i=0; i<array.length; i++) {
                    try {
                        long number = Long.parseLong(numList.get(i+1));
                        if(number<Integer.MIN_VALUE)
                            number=Integer.MIN_VALUE;
                        else if(number>Integer.MAX_VALUE)
                            number=Integer.MAX_VALUE;
                        array[i] = (int)number;
                    } catch(NumberFormatException e) {
                        return null;
                    }
                }
                return new NbtIntArray(array);
            }
            else if(numList.get(0).equals("B;")) {
                byte[] array = new byte[numList.size()-1];
                for(int i=0; i<array.length; i++) {
                    try {
                        String thisNum = numList.get(i+1);
                        if(thisNum.toLowerCase().charAt(thisNum.length()-1)=='b')
                            thisNum = thisNum.substring(0,thisNum.length()-1);
                        long number = Long.parseLong(thisNum);
                        if(number<Byte.MIN_VALUE)
                            number=Byte.MIN_VALUE;
                        else if(number>Byte.MAX_VALUE)
                            number=Byte.MAX_VALUE;
                        array[i] = (byte)number;
                    } catch(NumberFormatException e) {
                        return null;
                    }
                }
                return new NbtByteArray(array);
            }
            else if(numList.get(0).equals("L;")) {
                long[] array = new long[numList.size()-1];
                for(int i=0; i<array.length; i++) {
                    try {
                        String thisNum = numList.get(i+1);
                        if(thisNum.toLowerCase().charAt(thisNum.length()-1)=='l')
                            thisNum = thisNum.substring(0,thisNum.length()-1);
                        long number = Long.parseLong(thisNum);
                        array[i] = number;
                    } catch(NumberFormatException e) {
                        return null;
                    }
                }
                return new NbtLongArray(array);
            }
            else
                return null;
            }
            else {//list
                ArrayList<String> parseList = new ArrayList<>();
                while(inp.length()>0) {
                    boolean addedItem = false;
                    int bracketCount=0;
                    boolean inQuotes = false;
                    boolean inSingleQuotes = false;
                    if(inp.charAt(0)=='"') {
                        FIND_VALUE:
                        for(int i=1; i<inp.length(); i++) {
                            if(inp.charAt(i)=='"') {
                                parseList.add(inp.substring(0,i+1));
                                addedItem = true;
                                if(inp.length()>i+2)
                                    inp = inp.substring(i+2);
                                else
                                    inp = "";
                                break FIND_VALUE;
                            }
                            else if(inp.charAt(i)=='\\')
                                i++;
                        }
                    }
                    else if(inp.charAt(0)=='\'') {
                        FIND_VALUE:
                        for(int i=1; i<inp.length(); i++) {
                            if(inp.charAt(i)=='\'') {
                                parseList.add(inp.substring(0,i+1));
                                addedItem = true;
                                if(inp.length()>i+2)
                                    inp = inp.substring(i+2);
                                else
                                    inp = "";
                                break FIND_VALUE;
                            }
                            else if(inp.charAt(i)=='\\')
                                i++;
                        }
                    }
                    else if(inp.charAt(0)=='{') {
                        bracketCount=1;
                        FIND_VALUE:
                        for(int i=1; i<inp.length(); i++) {
                            if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='{')
                                bracketCount++;
                            else if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='}')
                                bracketCount--;
                            else if(inp.charAt(i)=='\\')
                                i++;
                            else if(inp.charAt(i)=='"' && !inSingleQuotes)
                                inQuotes = !inQuotes;
                            else if(inp.charAt(i)=='\'' && !inQuotes)
                                inSingleQuotes = !inSingleQuotes;
                            if(bracketCount==0) {
                                parseList.add(inp.substring(0,i+1));
                                addedItem = true;
                                if(inp.length()>i+2)
                                    inp = inp.substring(i+2);
                                else
                                    inp = "";
                                break FIND_VALUE;
                            }
                        }
                    }
                    else if(inp.charAt(0)=='[') {
                        bracketCount=1;
                        FIND_VALUE:
                        for(int i=1; i<inp.length(); i++) {
                            if(!inQuotes && !inSingleQuotes && inp.charAt(i)=='[')
                                bracketCount++;
                            else if(!inQuotes && !inSingleQuotes && inp.charAt(i)==']')
                                bracketCount--;
                            else if(inp.charAt(i)=='\\')
                                i++;
                            else if(inp.charAt(i)=='"' && !inSingleQuotes)
                                inQuotes = !inQuotes;
                            else if(inp.charAt(i)=='\'' && !inQuotes)
                                inSingleQuotes = !inSingleQuotes;
                            if(bracketCount==0) {
                                parseList.add(inp.substring(0,i+1));
                                addedItem = true;
                                if(inp.length()>i+2)
                                    inp = inp.substring(i+2);
                                else
                                    inp = "";
                                break FIND_VALUE;
                            }
                        }                    
                    }
                    else {
                        boolean commaFound = false;
                        FIND_VALUE:
                        for(int i=1; i<inp.length(); i++) {
                            if(inp.charAt(i)==',') {
                                parseList.add(inp.substring(0,i));
                                addedItem = true;
                                if(inp.length()>i+1)
                                    inp = inp.substring(i+1);
                                else
                                    inp = "";
                                commaFound=true;
                                break FIND_VALUE;
                            }
                        }
                        if(!commaFound) {
                            parseList.add(inp);
                            addedItem = true;
                            inp = "";
                        }
                    }
                    if(!addedItem)
                        inp = "";
                }
                NbtList list = new NbtList();
                for(String value: parseList) {
                    if(elementFromString(value) != null) {
                        try{
                            list.add(elementFromString(value));
                        } catch(Exception ex) {}
                    }
                }
                return list;
            }
        }
        else if(inp.toLowerCase().equals("true")) {
            return NbtByte.of((byte)1);
        }
        else if(inp.toLowerCase().equals("false")) {
            return NbtByte.of((byte)0);
        }
        else if(inp.length()>1 && inp.toLowerCase().charAt(inp.length()-1)=='b') {
            try {
                long inpNum = Long.parseLong(inp.substring(0,inp.length()-1));
                if(inpNum<Byte.MIN_VALUE)
                    inpNum=Byte.MIN_VALUE;
                else if(inpNum>Byte.MAX_VALUE)
                    inpNum=Byte.MAX_VALUE;
                return NbtByte.of((byte)inpNum);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        else if(inp.length()>1 && inp.toLowerCase().charAt(inp.length()-1)=='s') {
            try {
                long inpNum = Long.parseLong(inp.substring(0,inp.length()-1));
                if(inpNum<Short.MIN_VALUE)
                    inpNum=Short.MIN_VALUE;
                else if(inpNum>Short.MAX_VALUE)
                    inpNum=Short.MAX_VALUE;
                return NbtShort.of((short)inpNum);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        else if(inp.length()>1 && inp.toLowerCase().charAt(inp.length()-1)=='l') {
            try {
                long inpNum = Long.parseLong(inp.substring(0,inp.length()-1));
                return NbtLong.of(inpNum);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        else if(inp.length()>1 && inp.toLowerCase().charAt(inp.length()-1)=='f') {
            try {
                double inpNumD = Double.parseDouble(inp.substring(0,inp.length()-1));
                float inpNum = Float.parseFloat(inp.substring(0,inp.length()-1));
                System.out.println(inpNumD+" "+inpNum);
                if(inpNumD<Float.MIN_VALUE)
                    inpNum=Float.MIN_VALUE;
                else if(inpNumD>Float.MAX_VALUE)
                    inpNum=Float.MAX_VALUE;
                return NbtFloat.of(inpNum);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        else if(inp.length()>1 && inp.toLowerCase().charAt(inp.length()-1)=='d') {
            try {
                double inpNum = Double.parseDouble(inp.substring(0,inp.length()-1));
                return NbtDouble.of(inpNum);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        else {
            try {
                if(inp.contains("."))
                    return NbtDouble.of(Double.parseDouble(inp));
                else {
                    long inpNum = Long.parseLong(inp);
                    if(inpNum<Integer.MIN_VALUE)
                        inpNum=Integer.MIN_VALUE;
                    else if(inpNum>Integer.MAX_VALUE)
                        inpNum=Integer.MAX_VALUE;
                    return NbtInt.of((int)inpNum);
                }
            } catch(NumberFormatException e) {
                return null;
            }
        }
    } */



    //if inp is valid id, change current item to inp
    public static ItemStack setId(String inp) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client.player.getAbilities().creativeMode) {
            ItemStack item;
            if(inp.equals(""))
                inp="stone";
            if(!client.player.getMainHandStack().isEmpty())
                item = client.player.getMainHandStack();
            else {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("id",inp);
                nbt.putInt("Count",1);
                item = ItemStack.fromNbt(nbt);
                if(item.isEmpty())
                    return null;
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
                ItemBuilder.updateItem(item);
                return item;
            }
            NbtCompound nbt = new NbtCompound();
            NbtCompound nbtTag = new NbtCompound();
            int count = item.getCount();
            if(item.hasNbt()) {
                nbtTag=item.getNbt().copy();
                nbt.put("tag",nbtTag);
            }
            nbt.putString("id",inp);
            nbt.putInt("Count",count);
            ItemStack newItem = ItemStack.fromNbt(nbt);
            if(newItem.isEmpty())
                return item;
            client.interactionManager.clickCreativeStack(newItem, 36 + client.player.getInventory().selectedSlot);
            client.player.playerScreenHandler.sendContentUpdates();
            ItemBuilder.updateItem(newItem);
            return newItem;
        } else
            return null;
    }






    //change item count if inp is 1-64
    public static ItemStack setCount(String inpString) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client.player.getAbilities().creativeMode) {
            ItemStack item;
            if(!client.player.getMainHandStack().isEmpty())
                item = client.player.getMainHandStack();
            else
                return null;
            int inp = 1;
            boolean noInp = inpString.equals("");
            if(!noInp)
                try {
                    inp = Integer.parseInt(inpString);
                    if(inp <1)
                        inp = 1;
                    else if(inp > 64)
                        inp = 64;
                } catch(NumberFormatException e) {
                    return item;
                }
            if(noInp)
                item.setCount(1);
            else
                item.setCount(inp);
            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
            client.player.playerScreenHandler.sendContentUpdates();
            ItemBuilder.updateItem(item);
            return item;
        } else
            return null;
    }






    //returns NbtElement from path or null if it DNE
    public static NbtElement getNbtFromPath(ItemStack overrideItem, String getKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(getKey.equals(""))
            return null;
        if(getKey.contains("/") || getKey.contains(":")) {
            List<String> keyList = new ArrayList<>();
            if(!getKey.contains("/") && ( getKey.equals("0:") || getKey.equals("1:") ))
                keyList.add(getKey);
            while(getKey.contains("/")) {
                String thisString = getKey.substring(0,getKey.indexOf("/"));
                if(!thisString.equals(""))
                    keyList.add(thisString);
                if(getKey.length()>getKey.indexOf("/")+1) {
                    getKey = getKey.substring(getKey.indexOf("/")+1);
                    if(!getKey.contains("/") && !getKey.equals(""))
                        keyList.add(getKey);
                }
                else
                    getKey = "";
            }
            if(keyList.size()==0)
                return null;
            NbtCompound nbtBase = new NbtCompound();
            if(keyList.get(0).equals("0:")) {
                ItemStack item;
                if(overrideItem!=null)
                    item = overrideItem;
                else if(!client.player.getMainHandStack().isEmpty())
                    item = client.player.getMainHandStack();
                else
                    return null;
                NbtCompound nbtTag = new NbtCompound();
                if(item.hasNbt()) {
                    nbtTag= item.getNbt().copy();
                    nbtBase.put("tag",nbtTag);
                }
                nbtBase.putInt("Count",item.getCount());
                nbtBase.putString("id",item.getItem().toString());
                ItemStack newItem = ItemStack.fromNbt(nbtBase);
                if(newItem.isEmpty()) {
                    client.player.sendMessage(new LiteralText("Item id error (id not equal to Item.toString() "+
                    item.getItem().toString()+")"),false);
                    return null;
                }
            }
            else if(keyList.get(0).equals("1:")) {
                NbtCompound nbtTag = new NbtCompound();
                if(client.player.getOffHandStack().isEmpty())
                    return null;
                ItemStack offItem = client.player.getOffHandStack();
                if(offItem.hasNbt()) {
                    nbtTag=offItem.getNbt().copy();
                    nbtBase.put("tag",nbtTag);
                }
                nbtBase.putInt("Count",offItem.getCount());
                nbtBase.putString("id",offItem.getItem().toString());
                ItemStack newItem = ItemStack.fromNbt(nbtBase);
                if(newItem.isEmpty()) {
                    client.player.sendMessage(new LiteralText("Item id error (id not equal to Item.toString() "+
                    offItem.getItem().toString()+")"),false);
                    return null;
                }
            }
            else
                return null;
            if(keyList.size()==1)
                return nbtBase.copy();
            if(keyList.size()==2) {
                getKey = keyList.get(keyList.size()-1);
                if(nbtBase.contains(getKey)) {
                    return nbtBase.get(getKey);
                }
                else
                    return null;
            }
            if(!keyList.get(1).equals("tag"))
                return null;
            NbtCompound nbt = nbtBase.getCompound("tag");
            keyList.remove(0);
            keyList.remove(0);
            if(keyList.size()==1) {
                getKey = keyList.get(keyList.size()-1);
                if(nbt.contains(getKey)) {
                    return nbt.get(getKey);
                }
                else
                    return null;
            }
            int[] type = new int[keyList.size()];
            for(int i=0; i<keyList.size(); i++) {
                if(keyList.get(i).contains(":")) {
                    if(keyList.get(i).length()<2 || i==0)
                        return null;
                    try {
                        type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
                    } catch(NumberFormatException e) {
                        return null;
                    }
                }
                type[i]=-1;
            }
            List<NbtElement> nbtList = new ArrayList<NbtElement>();
            nbtList.add(nbt);
            for(int i=0; i<keyList.size(); i++) {
                if(nbtList.get(i).getType()==NbtType.COMPOUND && ((NbtCompound)nbtList.get(i)).contains(keyList.get(i))) {
                        nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                }
                else if(nbtList.get(i).getType()==NbtType.LIST && ((NbtList)nbtList.get(i)).size()>(type[i-1])) {
                    nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
                }
                else
                    return null;
            }
            if(type[type.length-2]==-1) {
                getKey = keyList.get(keyList.size()-1);
                if(((NbtCompound)nbtList.get(nbtList.size()-2)).contains(getKey)) {
                    return ((NbtCompound)nbtList.get(nbtList.size()-2)).get(getKey);
                }
                else
                    return null;
            }
            else {
                int inpIndex = type[type.length-2];
                if(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex)
                    return null;
                return ((NbtList)nbtList.get(nbtList.size()-2)).get(inpIndex);
            }
        }
        else
            return null;
    }






    //verify input is type required
    public static ItemStack setNbt(ItemStack overrideItem, String inpKey, NbtElement inp, int forceType) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemStack item;
        if(overrideItem!=null)
            item = overrideItem;
        else if(!client.player.getMainHandStack().isEmpty())
            item = client.player.getMainHandStack();
        else
            return null;
        if(inp == null)
            return item;
        else if(inp.getType()==NbtType.COMPOUND || inp.getType()==NbtType.LIST || inp.getType()==NbtType.BYTE_ARRAY
        || inp.getType()==NbtType.INT_ARRAY || inp.getType()==NbtType.LONG_ARRAY || inp.getType()==NbtType.STRING
        || inp.getType()==NbtType.BYTE || inp.getType()==NbtType.SHORT || inp.getType()==NbtType.INT
        || inp.getType()==NbtType.LONG || inp.getType()==NbtType.FLOAT || inp.getType()==NbtType.DOUBLE ) {
            if(inp.getType()==forceType)
                return setNbt(overrideItem, inpKey, inp);
            else if(forceType==NbtType.NUMBER && ( inp.getType()==NbtType.BYTE || inp.getType()==NbtType.SHORT
            || inp.getType()==NbtType.INT || inp.getType()==NbtType.LONG || inp.getType()==NbtType.FLOAT
            || inp.getType()==NbtType.DOUBLE ))
                return setNbt(overrideItem, inpKey, inp);
        }
        return item;
    }






    //set nbt at specified path to compound/list/array given
    //adds new Compounds and Lists along the way, if none exist
    //overwrites existing keys if they have the same name
    //if itemstack input is null, gets item from user mainhand
    public static ItemStack setNbt(ItemStack overrideItem, String inpKey, NbtElement inp) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player.getAbilities().creativeMode) {
            ItemStack item;
            if(overrideItem!=null)
                item = overrideItem;
            else if(!client.player.getMainHandStack().isEmpty())
                item = client.player.getMainHandStack();
            else
                return null;
            if(inp !=null && inpKey.equals("")) {
                if(inp.getType()==NbtType.COMPOUND) {
                    item.setNbt((NbtCompound)inp);
                    return item;
                }
            }
            if(inp == null)
                return item;
            else if(inp.getType()==NbtType.COMPOUND || inp.getType()==NbtType.LIST || inp.getType()==NbtType.BYTE_ARRAY
            || inp.getType()==NbtType.INT_ARRAY || inp.getType()==NbtType.LONG_ARRAY || inp.getType()==NbtType.STRING
            || inp.getType()==NbtType.BYTE || inp.getType()==NbtType.SHORT || inp.getType()==NbtType.INT
            || inp.getType()==NbtType.LONG || inp.getType()==NbtType.FLOAT || inp.getType()==NbtType.DOUBLE ) {
                if(inpKey.contains("/") || inpKey.contains(":")) {
                    NbtCompound nbt = new NbtCompound();
                    if(item.hasNbt())
                        nbt = item.getNbt().copy();
                    List<String> keyList = new ArrayList<>();
                    if(!inpKey.contains("/"))
                        return item;
                    while(inpKey.contains("/")) {
                        String thisString = inpKey.substring(0,inpKey.indexOf("/"));
                        if(!thisString.equals(""))
                            keyList.add(thisString);
                        if(inpKey.length()>inpKey.indexOf("/")+1) {
                            inpKey = inpKey.substring(inpKey.indexOf("/")+1);
                            if(!inpKey.contains("/") && !inpKey.equals(""))
                                keyList.add(inpKey);
                        }
                        else
                            inpKey = "";
                    }
                    if(keyList.size()==0)
                        return item;
                    else if(keyList.size()==1) {
                        inpKey = keyList.get(keyList.size()-1);
                        nbt.put(inpKey,inp);
                        item.setNbt(nbt);
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                        ItemBuilder.updateItem(item);
                        return item;
                    }
                    int[] type = new int[keyList.size()];
                    for(int i=0; i<keyList.size(); i++) {
                        if(keyList.get(i).contains(":")) {
                            if(keyList.get(i).length()<2 || i==0)
                                return item;
                            try {
                                type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
                            } catch(NumberFormatException e) {
                                return item;
                            }
                        }
                        type[i]=-1;
                    }
                    List<NbtElement> nbtList = new ArrayList<NbtElement>();
                    nbtList.add(nbt);
                    for(int i=0; i<keyList.size(); i++) {
                        if(nbtList.get(i).getType()==NbtType.COMPOUND) {
                            if(((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null
                            && ((((NbtCompound)nbtList.get(i)).get(keyList.get(i)).getType()==NbtType.COMPOUND
                            && type[i]==-1))) {
                                nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                            }
                            else if(((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null
                            && ((((NbtCompound)nbtList.get(i)).get(keyList.get(i)).getType()==NbtType.LIST
                            && type[i]!=-1))) {
                                nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                            }
                            else if(type[i]==-1) {
                                ((NbtCompound)nbtList.get(i)).put(keyList.get(i),new NbtCompound());
                                nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                            }
                            else {
                                ((NbtCompound)nbtList.get(i)).put(keyList.get(i),new NbtList());
                                nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                            }
                        }
                        else if(nbtList.get(i).getType()==NbtType.LIST) {
                            if(type[i]==-1 && i<keyList.size()-1 && ((NbtList)nbtList.get(i)).size()>type[i-1]
                            && ((NbtList)nbtList.get(i)).get(type[i-1])!=null
                            && ((NbtList)nbtList.get(i)).get(type[i-1]).getType()==NbtType.COMPOUND) {
                                nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
                            }
                            else if(type[i]==-1 && i<keyList.size()-1 && ((NbtList)nbtList.get(i)).size()>0
                            && ((NbtList)nbtList.get(i)).get(0)!=null
                            && ((NbtList)nbtList.get(i)).get(0).getType()==NbtType.COMPOUND) {
                                while(((NbtList)nbtList.get(i)).size()<=type[i-1]) {
                                   ((NbtList)nbtList.get(i)).add(new NbtCompound());
                                }
                                nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
                            }
                            else if(type[i]==-1 && i<keyList.size()-1) {
                                ((NbtList)nbtList.get(i)).clear();
                                while(((NbtList)nbtList.get(i)).size()<=type[i-1]) {
                                   ((NbtList)nbtList.get(i)).add(new NbtCompound());
                                }
                                nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
                            }
                            else if(i==keyList.size()-1) {
                                nbtList.add(new NbtCompound());
                            }
                            else
                                return item;
                        }
                        else
                            return item;
                    }
                    if(type[type.length-2]==-1) {
                        inpKey = keyList.get(keyList.size()-1);
                        ((NbtCompound)nbtList.get(nbtList.size()-2)).put(inpKey,inp);
                        item.setNbt(nbt);
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                        ItemBuilder.updateItem(item);
                        return item;
                    }
                    else {
                        int inpIndex = type[type.length-2];
                        if(inp.getType()==NbtType.LIST)
                            return item;
                        if(inp.getType()==NbtType.COMPOUND) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.COMPOUND) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtCompound());
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtCompound());
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.BYTE_ARRAY) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.BYTE_ARRAY) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtByteArray(new byte[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtByteArray(new byte[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.INT_ARRAY) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.INT_ARRAY) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtIntArray(new int[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtIntArray(new int[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.LONG_ARRAY) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.LONG_ARRAY) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtLongArray(new long[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(new NbtLongArray(new long[0]));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.STRING) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.STRING) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtString.of(""));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtString.of(""));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.DOUBLE) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.DOUBLE) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtDouble.of(0.0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtDouble.of(0.0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.FLOAT) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.FLOAT) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtFloat.of((float)0.0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtFloat.of((float)0.0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.BYTE) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.BYTE) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtByte.of((byte)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtByte.of((byte)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.SHORT) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.SHORT) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtShort.of((short)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtShort.of((short)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.INT) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.INT) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtInt.of(0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtInt.of(0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else if(inp.getType()==NbtType.LONG) {
                            if(((NbtList)nbtList.get(nbtList.size()-2)).size()>0
                            && ((NbtList)nbtList.get(nbtList.size()-2)).get(0).getType()==NbtType.LONG) {
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtLong.of((long)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                            else {
                                ((NbtList)nbtList.get(nbtList.size()-2)).clear();
                                while(((NbtList)nbtList.get(nbtList.size()-2)).size()<=inpIndex) {
                                    ((NbtList)nbtList.get(nbtList.size()-2)).add(NbtLong.of((long)0));
                                }
                                ((NbtList)nbtList.get(nbtList.size()-2)).set(inpIndex,inp);
                            }
                        }
                        else
                            return item;
                        item.setNbt(nbt);
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                        ItemBuilder.updateItem(item);
                        return item;
                    }
                }
                NbtCompound nbt = new NbtCompound();
                if(item.hasNbt())
                    nbt = item.getNbt().copy();
                nbt.put(inpKey,inp);
                item.setNbt(nbt);
                client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                client.player.playerScreenHandler.sendContentUpdates();
                ItemBuilder.updateItem(item);
                return item;
            }
            else
                return item;
        }
        else
            return null;
    }


    
    //deletes nbt at specified path and returns updated item
    //if no path given, deletes all nbt
    //if path not found, nothing changes
    //if itemstack input is null, gets item from user mainhand
    public static ItemStack removeNbt(ItemStack overrideItem, String inpKey) {
        //CLICK: {
            final MinecraftClient client = MinecraftClient.getInstance();
            if (client.player.getAbilities().creativeMode) {
                ItemStack item;
                if(overrideItem!=null)
                    item = overrideItem;
                else if(!client.player.getMainHandStack().isEmpty())
                    item = client.player.getMainHandStack();
                else
                    return null;
                boolean noInpKey = inpKey.equals("");
                if(noInpKey && item.hasNbt()) {
                    item.setNbt(new NbtCompound());
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    ItemBuilder.updateItem(item);
                    return item;
                }
                else if(!noInpKey && item.hasNbt()) {
                    if(inpKey.contains("/") || inpKey.contains(":")) {
                        NbtCompound nbt = item.getNbt().copy();
                        List<String> keyList = new ArrayList<>();
                        if(!inpKey.contains("/"))
                            return item;
                        while(inpKey.contains("/")) {
                            String thisString = inpKey.substring(0,inpKey.indexOf("/"));
                            if(!thisString.equals(""))
                                keyList.add(thisString);
                            if(inpKey.length()>inpKey.indexOf("/")+1) {
                                inpKey = inpKey.substring(inpKey.indexOf("/")+1);
                                if(!inpKey.contains("/") && !inpKey.equals(""))
                                    keyList.add(inpKey);
                            }
                            else
                                inpKey = "";
                        }
                        if(keyList.size()==0)
                            return item;
                        else if(keyList.size()==1) {
                            item.removeSubNbt(keyList.get(keyList.size()-1));
                            client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                            client.player.playerScreenHandler.sendContentUpdates();
                            ItemBuilder.updateItem(item);
                            return item;
                        }
                        int[] type = new int[keyList.size()];
                        for(int i=0; i<keyList.size(); i++) {
                            if(keyList.get(i).contains(":")) {
                                if(keyList.get(i).length()<2 || i==0)
                                    return item;
                                try {
                                    type[i-1] = Integer.parseInt(keyList.get(i).substring(0,keyList.get(i).length()-1));
                                } catch(NumberFormatException e) {
                                    return item;
                                }
                            }
                            type[i]=-1;
                        }
                        List<NbtElement> nbtList = new ArrayList<NbtElement>();
                        nbtList.add(nbt);
                        for(int i=0; i<keyList.size(); i++) {
                            if(nbtList.get(i).getType()==NbtType.COMPOUND
                            && ((NbtCompound)nbtList.get(i)).get(keyList.get(i))!=null) {
                                nbtList.add(((NbtCompound)nbtList.get(i)).get(keyList.get(i)));
                            }
                            else if(nbtList.get(i).getType()==NbtType.LIST
                            && ((NbtList)nbtList.get(i)).size()>type[i-1]
                            && ((NbtList)nbtList.get(i)).get(type[i-1])!=null) {
                                nbtList.add(((NbtList)nbtList.get(i)).get(type[i-1]));
                            }
                            else
                                return item;
                        }
                        if(type[type.length-2]==-1)
                            ((NbtCompound)nbtList.get(nbtList.size()-2)).remove(keyList.get(keyList.size()-1));
                        else
                            ((NbtList)nbtList.get(nbtList.size()-2)).remove(type[type.length-2]);
                        CLEAN: {
                            for(int i=2;i<type.length;i++) {
                                if(type[type.length-i]==-1 && ((NbtCompound)nbtList.get(nbtList.size()-i)).isEmpty()
                                && type[type.length-i-1]==-1) {
                                    ((NbtCompound)nbtList.get(nbtList.size()-i-1)).remove(keyList.get(keyList.size()-i));
                                }
                                else if(type[type.length-i]!=-1 && ((NbtList)nbtList.get(nbtList.size()-i)).isEmpty()
                                && type[type.length-i-1]==-1) {
                                    ((NbtCompound)nbtList.get(nbtList.size()-i-1)).remove(keyList.get(keyList.size()-i));
                                }
                                else
                                    break CLEAN;
                            }
                            if(nbtList.get(1).getType()==NbtType.COMPOUND && ((NbtCompound)nbtList.get(1)).isEmpty()) {
                                ((NbtCompound)nbtList.get(0)).remove(keyList.get(0));
                            }
                            else if(nbtList.get(1).getType()==NbtType.LIST && ((NbtList)nbtList.get(1)).isEmpty()) {
                                ((NbtCompound)nbtList.get(0)).remove(keyList.get(0));
                            }
                        }
                        item.setNbt(nbt);
                        client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                        client.player.playerScreenHandler.sendContentUpdates();
                        ItemBuilder.updateItem(item);
                        return item;
                    }
                    item.removeSubNbt(inpKey);
                    client.interactionManager.clickCreativeStack(item, 36 + client.player.getInventory().selectedSlot);
                    client.player.playerScreenHandler.sendContentUpdates();
                    ItemBuilder.updateItem(item);
                    return item;
                }
                else
                    return item;
            }
            else
                return null;
        //}
    }



}