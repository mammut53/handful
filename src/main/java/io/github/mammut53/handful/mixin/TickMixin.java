package io.github.mammut53.handful.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class TickMixin {

    @Inject(
        method = "tickServer",
        at = @At("HEAD")
    )
    public void tickServer(final CallbackInfo ci) {
        System.out.println("Server ticked!");
    }

}
