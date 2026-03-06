package io.github.mammut53.handful.mixin.client.renderer.entity.state;

import io.github.mammut53.handful.client.renderer.entity.state.ArmedEntityRenderStateItemStacks;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmedEntityRenderState.class)
public abstract class ArmedEntityRenderStateMixin implements ArmedEntityRenderStateItemStacks {

    @Unique
    private ItemStack handful$rightHandItemStack = ItemStack.EMPTY;
    @Unique
    private ItemStack handful$leftHandItemStack = ItemStack.EMPTY;

    public ItemStack handful$getRightHandItemStack() {
        return handful$rightHandItemStack;
    }

    public ItemStack handful$getLeftHandItemStack() {
        return handful$leftHandItemStack;
    }

    @Inject(
        method = "extractArmedEntityRenderState",
        at = @At("HEAD")
    )
    private static void injectExtractItemStacks(final LivingEntity livingEntity, final ArmedEntityRenderState armedEntityRenderState, final ItemModelResolver itemModelResolver, final CallbackInfo ci) {
        ((ArmedEntityRenderStateMixin)(Object) armedEntityRenderState).handful$rightHandItemStack = livingEntity.getItemHeldByArm(HumanoidArm.RIGHT);
        ((ArmedEntityRenderStateMixin)(Object) armedEntityRenderState).handful$leftHandItemStack = livingEntity.getItemHeldByArm(HumanoidArm.LEFT);
    }

}
