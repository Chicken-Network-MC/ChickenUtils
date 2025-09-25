package com.chickennw.utils.managers;

import com.chickennw.utils.models.hooks.AbstractPluginHook;
import com.chickennw.utils.models.hooks.PluginHook;
import com.chickennw.utils.models.hooks.impl.other.PlaceholderApiHook;
import com.chickennw.utils.models.hooks.types.OtherHook;
import org.bukkit.Bukkit;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HookManager {

    private static HookManager instance;
    private final List<AbstractPluginHook> loadedHooks = new ArrayList<>();
    private PlaceholderApiHook placeholderApiHook;

    public static synchronized HookManager getInstance() {
        if (instance == null) {
            instance = new HookManager();
        }

        return instance;
    }

    private HookManager() {
        loadAllHooks("com.chickennw.utils.models.hooks");
    }

    public void loadHook(AbstractPluginHook hook) {
        if (!hook.isEnabled()) return;
        if (hook.isRequirePlugin() && !Bukkit.getPluginManager().isPluginEnabled(hook.getRequiredPlugin())) return;

        hook.load();
        Bukkit.getLogger().info("Loaded new hook: " + hook.getName());
        loadedHooks.add(hook);
    }

    public void loadAllHooks(String packageName) {
        loadedHooks.clear();

        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends AbstractPluginHook>> hookClasses = reflections.getSubTypesOf(AbstractPluginHook.class);
        for (Class<? extends AbstractPluginHook> hookClass : hookClasses) {
            if (Modifier.isAbstract(hookClass.getModifiers())) continue;
            if (!canRegister(hookClass)) continue;

            try {
                AbstractPluginHook hook = hookClass.newInstance();
                loadHook(hook);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError ignored) {
                Bukkit.getLogger().info("NoClassDefFoundError on " + hookClass.getName());
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderApiHook = new PlaceholderApiHook();
            placeholderApiHook.load();
        }
    }

    public void unloadAllHooks() {
        for (AbstractPluginHook hook : loadedHooks) {
            hook.unload();
        }

        if (placeholderApiHook != null) {
            placeholderApiHook.unload();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PluginHook> T getHookWithType(Class<T> hookClass) {
        return (T) loadedHooks.stream().filter(hook -> hook.getClass().getInterfaces()[0].equals(hookClass))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends PluginHook> T getHookWithExact(Class<T> hookClass) {
        return (T) loadedHooks.stream().filter(hook -> hook.getClass().equals(hookClass))
                .findFirst()
                .orElse(null);
    }

    private boolean canRegister(Class<?> clazz) {
        Class<?> superClass = clazz.getInterfaces()[0];

        for (AbstractPluginHook hook : loadedHooks) {
            Class<?> hookClass = hook.getClass();
            if (superClass.equals(hookClass)) return false;

            Class<?> hookSuperClass = hookClass.getInterfaces()[0];
            if (hookSuperClass != OtherHook.class && superClass.equals(hookSuperClass)) return false;
        }

        return true;
    }
}
