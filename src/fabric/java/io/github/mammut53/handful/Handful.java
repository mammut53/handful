package io.github.mammut53.handful;

import net.fabricmc.api.ModInitializer;

public class Handful implements ModInitializer {

    @Override
    public void onInitialize() {
        HandfulCommon.initialize();
    }

}
