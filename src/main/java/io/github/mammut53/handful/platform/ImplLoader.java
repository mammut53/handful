package io.github.mammut53.handful.platform;

import java.util.ServiceLoader;

public class ImplLoader {

    public static <T> T load(final Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    private ImplLoader() {
        throw new IllegalStateException("Utility class");
    }

}