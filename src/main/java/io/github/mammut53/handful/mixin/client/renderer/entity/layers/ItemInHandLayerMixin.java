package io.github.mammut53.handful.mixin.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, @NonNull M> {

    @Unique
    private final RandomSource handful$random = RandomSource.create();

    protected ItemInHandLayerMixin(final RenderLayerParent<S, @NonNull M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
            ),
            cancellable = true
    )
    private void injectItemCluster(final S state, final ItemStackRenderState item, final ItemStack itemStack, final HumanoidArm arm, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final CallbackInfo ci) {
        this.handful$renderItemCluster(state, item, itemStack, poseStack, submitNodeCollector, lightCoords);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void handful$renderItemCluster(final S state, final ItemStackRenderState item, final ItemStack itemStack, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords) {
        final AABB aabb = item.getModelBoundingBox();
        final float scaleZ = (float) aabb.getZsize();
        if (scaleZ > 0.0625F) {
            item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            return;
        }

        final float zOffset = 1.5F * scaleZ;

        poseStack.translate(0.0F, 0.0F, -0.01F);

        this.handful$random.setSeed(ItemClusterRenderState.getSeedForItemStack(itemStack));
        final int renderedAmount = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());

        item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.translate(0.0F, 0.0F, zOffset);
        for (int i = 1; i < renderedAmount; ++i) {
            poseStack.pushPose();

            final float randomTranslateX = (this.handful$random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            final float randomTranslateY = (this.handful$random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            poseStack.translate(randomTranslateX, randomTranslateY, 0.0F);

            item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

            poseStack.popPose();
            poseStack.translate(0.0F, 0.0F, zOffset);
        }
    }
}
