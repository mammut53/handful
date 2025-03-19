package io.github.mammut53.handful;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HandfulConstants {

    public static final String MOD_ID = "handful";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private HandfulConstants() {
        throw new IllegalStateException("Utility class");
    }

}