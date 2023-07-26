package smagellan.test;

import org.jboss.logmanager.EmbeddedConfigurator;
import org.jboss.logmanager.LogContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderLauncher {
    public static void main(String[] args) {
        List<ServiceLoader.Provider<EmbeddedConfigurator>> providers1 = loadServices(EmbeddedConfigurator.class, LogContext.class.getClassLoader());
        for (ServiceLoader.Provider<EmbeddedConfigurator> provider : providers1) {
            System.err.println(provider.get());
        }
    }

    @NotNull
    private static <T> List<ServiceLoader.Provider<T>> loadServices(Class<T> cls, ClassLoader loader) {
        ServiceLoader<T> ldr = ServiceLoader.load(cls, loader);
        return ldr.stream().toList();
    }
}
