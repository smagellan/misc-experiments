package smagellan.quarkus;

import org.jboss.logmanager.LogContextInitializer;
import org.jboss.logmanager.LogContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderLauncher {
    public static void main(String[] args) {
        //io.quarkus.bootstrap.logging.InitialConfigurator conf;
        List<ServiceLoader.Provider<LogContextInitializer>> providers1 = loadServices(LogContextInitializer.class, LogContext.class.getClassLoader());
        for (ServiceLoader.Provider<LogContextInitializer> provider : providers1) {
            System.err.println(provider.get());
        }
    }

    @NotNull
    private static <T> List<ServiceLoader.Provider<T>> loadServices(Class<T> cls, ClassLoader loader) {
        ServiceLoader<T> ldr = ServiceLoader.load(cls, loader);
        return ldr.stream().toList();
    }
}
