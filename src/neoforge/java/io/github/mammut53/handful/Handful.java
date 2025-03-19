package io.github.mammut53.handful;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(HandfulConstants.MOD_ID)
public class Handful {

    public Handful(final IEventBus modEventBus) {
        HandfulCommon.initialize();
    }

}