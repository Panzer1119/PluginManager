package de.codemakers.plugin;

import java.io.File;
import java.util.jar.JarEntry;

/**
 * Generic Filter
 *
 * @author Panzer1119
 */
public interface PluginFilter {

    public boolean acceptPlugin(File file);

    public boolean acceptClass(JarEntry jarEntry);

    public String getClassPath(JarEntry jarEntry);

    public String getClassName(JarEntry jarEntry);

    public boolean isPluggableClass(Class<Class> classLoaded);

}
