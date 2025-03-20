package io.github.mammut53.handful.mixin.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {

    @Shadow @Final
    private ItemRenderer itemRenderer;
    @Unique
    private final RandomSource random = RandomSource.create();

    protected ItemInHandLayerMixin(final RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
            ),
            cancellable = true
    )
    private void injectItemCluster(final S livingEntityRenderState, final @Nullable BakedModel bakedModel, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light, final CallbackInfo ci) {
        this.renderItemCluster(livingEntityRenderState, bakedModel, itemStack, itemDisplayContext, humanoidArm, poseStack, multiBufferSource, light);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void renderItemCluster(final S livingEntityRenderState, final @Nullable BakedModel model, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        final boolean isLeft = humanoidArm == HumanoidArm.LEFT;

        final boolean isGui3d = model != null && model.isGui3d();
        if (isGui3d) {
            this.itemRenderer.render(itemStack, itemDisplayContext, isLeft, poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY, model);
            return;
        }

        this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(itemStack));

        final int renderedAmount = ItemEntityRenderer.getRenderedAmount(itemStack.getCount());

        final float scaleZ = model != null ? switch (humanoidArm) {
            case LEFT -> model.getTransforms().thirdPersonLeftHand.scale.z();
            case RIGHT -> model.getTransforms().thirdPersonRightHand.scale.z();
        } : 1.0F;

        poseStack.translate(0.0F, 0.0F, -0.01F);

        for (int i = 0; i < renderedAmount; i++) {
            poseStack.pushPose();

            if (i > 0) {
                final float randomTranslateX = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                final float randomTranslateY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                poseStack.translate(randomTranslateX, randomTranslateY, 0.0F);
            }

            this.itemRenderer.render(itemStack, itemDisplayContext, isLeft, poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY, model);

            poseStack.popPose();
            poseStack.translate(0.0F, 0.0F, 0.09375F * scaleZ);
        }
    }
}
