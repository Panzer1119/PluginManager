package de.panzercraft.plugin.impl;

import de.panzercraft.plugin.PluginFilter;
import de.panzercraft.plugin.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.JarEntry;

/**
 * StandardPluginFilter
 *
 * @author Panzer1119
 */
public class StandardPluginFilter implements PluginFilter {

    private static final String JAR = ".jar";
    private static final String CLASS = ".class";

    private final ArrayList<Class> classReferences = new ArrayList<>();
    private final ArrayList<String> pluginNames = new ArrayList<>();
    private boolean blacklist = true;

    public StandardPluginFilter(Class... classReferences) {
        addClassReferences(classReferences);
    }

    public final StandardPluginFilter addClassReferences(Class... classReferences) {
        if (classReferences == null || classReferences.length == 0) {
            return this;
        }
        this.classReferences.addAll(Arrays.asList(classReferences));
        return this;
    }

    public final StandardPluginFilter removeClassReferences(Class... classReferences) {
        if (classReferences == null || classReferences.length == 0) {
            return this;
        }
        this.classReferences.removeAll(Arrays.asList(classReferences));
        return this;
    }

    public final StandardPluginFilter addPluginNames(String... pluginNames) {
        if (pluginNames == null || pluginNames.length == 0) {
            return this;
        }
        this.pluginNames.addAll(Arrays.asList(pluginNames));
        return this;
    }

    public final StandardPluginFilter removePluginNames(String... pluginNames) {
        if (pluginNames == null || pluginNames.length == 0) {
            return this;
        }
        this.pluginNames.removeAll(Arrays.asList(pluginNames));
        return this;
    }

    @Override
    public boolean acceptPlugin(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (pluginNames.contains(file.getName()) == blacklist) {
            return false;
        }
        return file.getName().endsWith(JAR) || file.getName().toLowerCase().endsWith(JAR) || file.getName().toUpperCase().equals(JAR.toUpperCase());
    }

    @Override
    public boolean acceptClass(JarEntry jarEntry) {
        if (jarEntry == null) {
            return false;
        }
        return jarEntry.getName().endsWith(CLASS) || jarEntry.getName().toLowerCase().endsWith(CLASS) || jarEntry.getName().toUpperCase().endsWith(CLASS.toUpperCase());
    }

    @Override
    public String getClassPath(JarEntry jarEntry) {
        if (jarEntry == null) {
            return null;
        }
        return jarEntry.getName().substring(0, jarEntry.getName().length() - CLASS.length()).replace('/', '.');
    }

    @Override
    public String getClassName(JarEntry jarEntry) {
        if (jarEntry == null) {
            return null;
        }
        String name = getClassPath(jarEntry);
        final int index = name.indexOf("\\.");
        if (index != -1) {
            name = name.substring(index + 1);
        }
        return name;
    }

    @Override
    public boolean isPluggableClass(Class<Class> classLoaded) {
        if (classLoaded == null) {
            return false;
        }
        for (Class<?> i : classLoaded.getInterfaces()) {
            for (Class classReference : classReferences) { //FIXME AAAAEEEHM soll das gleich beim ersten finding true returnen?! oder wenn alle interfaces implemented sind??!!
                if (Util.isInterfaceSame(i, classReference)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static final StandardPluginFilter createInstance() {
        return new StandardPluginFilter();
    }
    
    public static final StandardPluginFilter createInstance(Class... classReferences) {
        return new StandardPluginFilter(classReferences);
    }

}
