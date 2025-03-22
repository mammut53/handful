package io.github.mammut53.handful.mixin.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mammut53.handful.client.renderer.entity.state.ArmedEntityRenderStateItemStacks;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {

    @Unique
    private final RandomSource random = RandomSource.create();

    protected ItemInHandLayerMixin(final RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            ),
            cancellable = true
    )
    private void injectItemCluster(final S armedEntityRenderState, final ItemStackRenderState itemStackRenderState, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light, final CallbackInfo ci) {
        this.renderItemCluster(armedEntityRenderState, itemStackRenderState, humanoidArm, poseStack, multiBufferSource, light);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void renderItemCluster(final S armedEntityRenderState, final ItemStackRenderState itemStackRenderState, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        final ItemStack itemStack = switch (humanoidArm) {
            case RIGHT -> ((ArmedEntityRenderStateItemStacks) armedEntityRenderState).getRightHandItemStack();
            case LEFT -> ((ArmedEntityRenderStateItemStacks) armedEntityRenderState).getLeftHandItemStack();
        };

        final boolean isGui3d = itemStackRenderState.isGui3d();
        if (isGui3d) {
            itemStackRenderState.render(poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);
            return;
        }

        final float scaleZ = itemStackRenderState.transform().scale.z();
        poseStack.translate(0.0F, 0.0F, -0.01F);

        this.random.setSeed(ItemClusterRenderState.getSeedForItemStack(itemStack));
        final int renderedAmount = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());
        for (int i = 0; i < renderedAmount; i++) {
            poseStack.pushPose();

            if (i > 0) {
                final float randomTranslateX = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                final float randomTranslateY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                poseStack.translate(randomTranslateX, randomTranslateY, 0.0F);
            }

            itemStackRenderState.render(poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
            poseStack.translate(0.0F, 0.0F, 0.09375F * scaleZ);
        }
    }
}
