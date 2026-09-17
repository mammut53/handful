package io.github.mammut53.handful.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private final RandomSource handful$random = RandomSource.create();

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
            ),
            cancellable = true
    )
    private void injectSubmitArmWithItem(final PlayerRenderState playerState, final FirstPersonHandsAndItemsRenderState state, final float partialTicks, final float xRot, final InteractionHand hand, final float attack, final ItemStack itemStack, final float inverseArmHeight, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final CallbackInfo ci) {
        final boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        final ItemStackRenderState handItemStackRenderState = (isMainHand ? state.mainHandRenderState : state.offHandRenderState);
        this.handful$submitItemCluster(handItemStackRenderState, hand, itemStack, poseStack, submitNodeCollector, lightCoords);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void handful$submitItemCluster(final ItemStackRenderState state, final InteractionHand hand, final ItemStack itemStack, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords) {
        final AABB aabb = state.getModelBoundingBox();
        final float scaleX = (float) aabb.getXsize();
        if (scaleX > 0.0625F) {
            state.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            return;
        }

        final float xOffset = 1.5F * (hand == InteractionHand.OFF_HAND ? -scaleX : scaleX);

        this.handful$random.setSeed(ItemClusterRenderState.getSeedForItemStack(itemStack));
        final int renderedAmount = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());

        state.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.translate(xOffset, 0.0F, 0.0F);
        for (int i = 1; i < renderedAmount; ++i) {
            poseStack.pushPose();

            final float randomTranslateY = (this.handful$random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            final float randomTranslateZ = (this.handful$random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            poseStack.translate(0.0F, randomTranslateY, randomTranslateZ);

            state.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
            poseStack.translate(xOffset, 0.0F, 0.0F);
        }
    }
}
