package io.github.mammut53.handful.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private final RandomSource random = RandomSource.create();

    @Mutable
    @Final
    @Shadow
    private final ItemRenderer itemRenderer;

    public ItemInHandRendererMixin(final ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    @Inject(
            method = "renderItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light, final CallbackInfo ci) {
        if (!itemStack.isEmpty()) {
            renderItemCluster(livingEntity, itemStack, itemDisplayContext, poseStack, multiBufferSource, light);
        }
        ci.cancel();
    }

    @Unique
    private void renderItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        // Update scratchItemStackRenderState to the item model, so we can get scaleX
        final ItemStackRenderState itemStackRenderState = this.itemRenderer.scratchItemStackRenderState;
        this.itemRenderer.resolver.updateForTopItem(itemStackRenderState, itemStack, itemDisplayContext, livingEntity.level(), livingEntity, livingEntity.getId() + itemDisplayContext.ordinal());

        final AABB aabb = ItemEntityRenderer.calculateModelBoundingBox(itemStackRenderState);
        final float scaleX = (float) aabb.getXsize();
        if (scaleX > 0.0625F) {
            this.renderStatic(livingEntity, itemStack, itemDisplayContext, poseStack, multiBufferSource, light);
            return;
        }

        final float xOffset = 1.5F * (itemDisplayContext.leftHand() ? -scaleX : scaleX);

        this.random.setSeed(ItemClusterRenderState.getSeedForItemStack(itemStack));
        final int renderedAmount = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());

        this.renderStatic(livingEntity, itemStack, itemDisplayContext, poseStack, multiBufferSource, light);
        poseStack.translate(xOffset, 0.0F, 0.0F);
        for (int i = 1; i < renderedAmount; ++i) {
            poseStack.pushPose();

            final float randomTranslateY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            final float randomTranslateZ = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            poseStack.translate(0.0F, randomTranslateY, randomTranslateZ);

            this.renderStatic(livingEntity, itemStack, itemDisplayContext, poseStack, multiBufferSource, light);

            poseStack.popPose();
            poseStack.translate(xOffset, 0.0F, 0.0F);
        }
    }

    @Unique
    private void renderStatic(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        /*
        Call this.itemRenderer.renderStatic instead of this.itemRenderer.scratchItemStackRenderState.render
        to be a bit more compatible with other mods doing stuff to itemRenderer
        */
        this.itemRenderer.renderStatic(
                livingEntity,
                itemStack,
                itemDisplayContext,
                poseStack,
                multiBufferSource,
                livingEntity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                livingEntity.getId() + itemDisplayContext.ordinal()
        );
    }
}
