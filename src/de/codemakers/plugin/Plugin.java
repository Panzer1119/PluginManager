package de.codemakers.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Plugin
 *
 * @author Panzer1119
 */
public class Plugin {

    private final File file;
    private ClassLoader classLoader = null;
    private ArrayList<Class<Class>> classesLoaded = null;
    private ArrayList<Class> classesPlugged = null;

    public Plugin(File file) {
        this.file = file;
    }

    public final ArrayList<Class<Class>> getClassesLoaded() {
        return classesLoaded;
    }

    protected final Plugin setClasses(ArrayList<Class<Class>> classes) {
        this.classesLoaded = classes;
        return this;
    }

    public final ArrayList<Class> getClassesPlugged() {
        return classesPlugged;
    }

    protected final Plugin setClassesPlugged(ArrayList<Class> classesPlugged) {
        this.classesPlugged = classesPlugged;
        return this;
    }

    public final <T> ArrayList<T> getPluggables(Class<? extends T> type) {
        if (!isPlugged()) {
            return new ArrayList<>();
        }
        return (ArrayList<T>) (ArrayList<?>) classesPlugged;
    }

    public final <T> ArrayList<T> getPluggables() {
        if (!isPlugged()) {
            return new ArrayList<>();
        }
        return (ArrayList<T>) (ArrayList<?>) classesPlugged;
    }

    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    protected final Plugin setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    protected final URL toURL() {
        try {
            return file.toURI().toURL();
        } catch (Exception ex) {
            if (PluginLoader.DEBUG_MODE) {
                System.err.println(ex);
            }
            return null;
        }
    }

    public final boolean isLoaded() {
        return classesLoaded != null;
    }

    public final boolean isPlugged() {
        return classesPlugged != null;
    }

    public final boolean unload() {
        return false;
    }

    public final Plugin loadClasses(PluginFilter filter) {
        if (isLoaded()) {
            classesLoaded.clear();
        }
        if (isPlugged()) {
            classesPlugged.clear();
            classesPlugged = null;
        }
        return setClasses(loadClasses(file, filter, classLoader));
    }

    public final Plugin createPluggableInstances() {
        if (isPlugged()) {
            classesPlugged.clear();
        }
        return setClassesPlugged(createPluggableInstances(classesLoaded));
    }

    public static final ArrayList<Class<Class>> loadClasses(File jarFile, PluginFilter filter, ClassLoader classLoader) {
        if (jarFile == null || !jarFile.exists() || !jarFile.isFile() || filter == null || classLoader == null) {
            return new ArrayList<>();
        }
        final ArrayList<Class<Class>> classes = new ArrayList<>();
        try {
            final JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
            JarEntry jarEntry = null;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                try {
                    if (filter.acceptClass(jarEntry)) {
                        if (PluginLoader.PRECISE_DEBUG_MODE) {
                            System.out.println(String.format("Filter accepted JarEntry \"%s\"", jarEntry));
                        }
                        final String classPath = filter.getClassPath(jarEntry);
                        if (classPath == null) {
                            continue;
                        }
                        final Class<Class> classLoaded = (Class<Class>) classLoader.loadClass(classPath);
                        if (filter.isPluggableClass(classLoaded)) {
                            classes.add(classLoaded);
                        } else if (PluginLoader.PRECISE_DEBUG_MODE) {
                            System.out.println(String.format("Filter doesn't accepted Pluggable Class \"%s\"", jarEntry));
                        }
                    } else if (PluginLoader.PRECISE_DEBUG_MODE) {
                        System.out.println(String.format("Filter doesn't accepted JarEntry \"%s\"", jarEntry));
                    }
                } catch (ClassNotFoundException ex) {
                    if (PluginLoader.DEBUG_MODE) {
                        System.err.println(String.format("Can't load Class \"%s\": %s", jarEntry.getName(), ex));
                        if (PluginLoader.PRECISE_DEBUG_MODE) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            jarInputStream.close();
        } catch (Exception ex) {
            if (PluginLoader.DEBUG_MODE) {
                System.err.println(ex);
            }
        }
        return classes;
    }

    public static final ArrayList<Class> createPluggableInstances(ArrayList<Class<Class>> classesLoaded) {
        if (classesLoaded == null) {
            return new ArrayList<>();
        }
        final ArrayList<Class> classesPlugged = new ArrayList<>();
        classesLoaded.stream().forEach((classLoaded) -> {
            if (classLoaded != null) {
                try {
                    classesPlugged.add(classLoaded.newInstance());
                } catch (InstantiationException ex) {
                    if (PluginLoader.DEBUG_MODE) {
                        System.err.println(String.format("Can't instantiate Class \"%s\": %s", classLoaded.getName(), ex));
                        if (PluginLoader.PRECISE_DEBUG_MODE) {
                            ex.printStackTrace();
                        }
                    }
                } catch (IllegalAccessException ex) {
                    if (PluginLoader.DEBUG_MODE) {
                        System.err.println(String.format("Can't access Class \"%s\": %s", classLoaded.getName(), ex));
                        if (PluginLoader.PRECISE_DEBUG_MODE) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        return classesPlugged;
    }

    @Override
    public String toString() {
        return "Plugin{" + "file=" + file + ", classLoader=" + classLoader + ", classesLoaded=" + classesLoaded + ", classesPlugged=" + classesPlugged + '}';
    }

}
