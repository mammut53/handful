package io.github.mammut53.handful.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
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
    private final RandomSource handful$random = RandomSource.create();

    @Mutable
    @Final
    @Shadow
    private final ItemModelResolver itemModelResolver;

    public ItemInHandRendererMixin(final ItemModelResolver itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    @Inject(
            method = "renderItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectItemCluster(final LivingEntity mob, final ItemStack itemStack, final ItemDisplayContext type, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final CallbackInfo ci) {
        if (!itemStack.isEmpty()) {
            final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, type, mob.level(), mob, mob.getId() + type.ordinal());
            this.handful$renderItemCluster(itemStackRenderState, itemStack, type, poseStack, submitNodeCollector, lightCoords);
        }
        ci.cancel();
    }

    @Unique
    private void handful$renderItemCluster(final ItemStackRenderState state, final ItemStack itemStack, final ItemDisplayContext type, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords) {
        final AABB aabb = state.getModelBoundingBox();
        final float scaleX = (float) aabb.getXsize();
        if (scaleX > 0.0625F) {
            state.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            return;
        }

        final float xOffset = 1.5F * (type.leftHand() ? -scaleX : scaleX);

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
