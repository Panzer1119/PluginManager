package de.panzercraft.plugin.util;

/**
 * Util
 * 
 * @author Panzer1119
 */
public class Util {
    
    public static final boolean isInterfaceSame(Class interface_1, Class interface_2) {
        if (interface_1 == null || interface_2 == null) {
            return false;
        }
        if (!interface_1.isInterface() || !interface_2.isInterface()) {
            return false;
        }
        if (!interface_1.getSimpleName().equals(interface_2.getSimpleName())) {
            return false;
        }
        if (!arrayEquals(interface_1.getAnnotations(), interface_2.getAnnotations())) {
            return false;
        }
        if (interface_1.getMethods().length != interface_2.getMethods().length) {
            return false;
        }
        for (int i = 0; i < interface_1.getMethods().length; i++) {
            if (!interface_1.getMethods()[i].getName().equals(interface_2.getMethods()[i].getName())) {
                return false;
            }
            if (!interface_1.getMethods()[i].getReturnType().equals(interface_2.getMethods()[i].getReturnType())) {
                return false;
            }
            if (interface_1.getMethods()[i].getParameterCount() != interface_2.getMethods()[i].getParameterCount()) {
                return false;
            }
            if (!arrayEquals(interface_1.getMethods()[i].getParameters(), interface_2.getMethods()[i].getParameters())) {
                return false;
            }
        }
        return true;
    }
    
    public static final <T> boolean arrayEquals(T[] array_1, T[] array_2) {
        if (array_1 == null || array_2 == null) {
            return array_1 == array_2;
        }
        if (array_1.length != array_2.length) {
            return false;
        }
        if (array_1.length == 0) {
            return true;
        }
        for (int i = 0; i < array_1.length; i++) {
            if (array_1[i] == null || array_2[i] == null) {
                if (array_1[i] != array_2[i]) {
                    return false;
                }
            }
            if (!array_1[i].equals(array_2[i]) && !array_2[i].equals(array_1[i])) {
                return false;
            }
        }
        return true;
    }
    
}
