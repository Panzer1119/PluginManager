package de.codemakers.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * PluginLoader
 *
 * @author Panzer1119
 */
public class PluginLoader {

    public static boolean DEBUG_MODE = false;
    public static boolean PRECISE_DEBUG_MODE = false;

    private ClassLoader classLoader = null;
    private PluginFilter filter = null;
    private ArrayList<Plugin> plugins = null;

    public PluginLoader() {
        this(null);
    }

    public PluginLoader(PluginFilter filter, File... files) {
        setPluginFilter(filter);
        loadPlugins(files);
    }

    public final ArrayList<Plugin> getPlugins() {
        return plugins;
    }

    public final PluginFilter getPluginFilter() {
        return filter;
    }

    public final PluginLoader setPluginFilter(PluginFilter filter) {
        this.filter = filter;
        return this;
    }

    public final <T> ArrayList<T> getPluggables(Class<? extends T> type) {
        if (plugins == null) {
            return new ArrayList<>();
        }
        final ArrayList<T> pluggables = new ArrayList<>();
        plugins.stream().filter((plugin) -> plugin.isPlugged()).forEach((plugin) -> {
            pluggables.addAll(plugin.getPluggables(type));
        });
        return pluggables;
    }

    public final <T> ArrayList<T> getPluggables() {
        if (plugins == null) {
            return new ArrayList<>();
        }
        final ArrayList<T> pluggables = new ArrayList<>();
        plugins.stream().filter((plugin) -> plugin.isPlugged()).forEach((plugin) -> {
            pluggables.addAll(plugin.getPluggables());
        });
        return pluggables;
    }

    public final boolean unload() {
        if (plugins == null) {
            return true;
        }
        boolean good = true;
        for (Plugin plugin : plugins) {
            if (!plugin.unload()) {
                good = false;
            }
        }
        return good;
    }

    public final boolean loadPlugins(File... files) {
        if (filter == null || files == null) {
            return false;
        } else if (files.length == 0) {
            return true;
        }
        if (classLoader != null) {
            classLoader.clearAssertionStatus();
            classLoader = null;
        }
        if (plugins != null) { //TODO Maybe add an unloading function?
            plugins.clear();
        }
        plugins = loadFilesInternal(filter, files);
        classLoader = loadPluginsInternal(filter, plugins);
        if (PluginLoader.DEBUG_MODE) {
            System.out.println(String.format("Loaded %d Plugin(s)!", getPluggables().size()));
        }
        return isLoaded();
    }

    public final boolean isLoaded() {
        return plugins != null && classLoader != null;
    }

    protected static final ArrayList<Plugin> loadFilesInternal(PluginFilter filter, File... files) {
        if (filter == null || files == null || files.length == 0) {
            return new ArrayList<>();
        }
        try {
            final ArrayList<Plugin> plugins = new ArrayList<>();
            for (File file : files) {
                try {
                    if (file.exists()) {
                        if (file.isFile()) {
                            if (filter.acceptPlugin(file)) {
                                plugins.add(new Plugin(file));
                            } else if (PluginLoader.PRECISE_DEBUG_MODE) {
                                System.out.println(String.format("Filter doesn't accepted File \"%s\"", file));
                            }
                        } else if (file.isDirectory()) {
                            for (File file_ : file.listFiles()) {
                                plugins.addAll(loadFilesInternal(filter, file_));
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (PluginLoader.DEBUG_MODE) {
                        System.err.println(String.format("Can't load File \"%s\": %s", file, ex));
                    }
                }
            }
            return plugins;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    protected static final ClassLoader loadPluginsInternal(PluginFilter filter, ArrayList<Plugin> plugins) {
        if (filter == null || plugins == null || plugins.isEmpty()) {
            return null;
        }
        final ClassLoader classLoader = createClassLoader(plugins.toArray(new Plugin[plugins.size()]));
        if (classLoader == null) {
            return null;
        }
        plugins.stream().forEach((plugin) -> {
            plugin.setClassLoader(classLoader);
            plugin.loadClasses(filter);
            plugin.createPluggableInstances();
        });
        return null;
    }

    protected static final ClassLoader createClassLoader(Plugin[] plugins) {
        try {
            final ArrayList<URL> urls = new ArrayList<>();
            for (Plugin plugin : plugins) {
                final URL url = plugin.toURL();
                if (url != null) {
                    urls.add(url);
                }
            }
            final URL[] urls_array = urls.toArray(new URL[urls.size()]);
            urls.clear();
            return new URLClassLoader(urls_array);
        } catch (Exception ex) {
            if (PluginLoader.DEBUG_MODE) {
                System.err.println(ex);
            }
            return null;
        }
    }

}
