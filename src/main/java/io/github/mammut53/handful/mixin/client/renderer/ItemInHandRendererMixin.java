package io.github.mammut53.handful.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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
    private void injectItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final boolean isLeftHand, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light, final CallbackInfo ci) {
        if (!itemStack.isEmpty()) {
            renderItemCluster(livingEntity, itemStack, itemDisplayContext, isLeftHand, poseStack, multiBufferSource, light);
        }
        ci.cancel();
    }

    @Unique
    private void renderItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final boolean isLeftHand, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        // Do this to get the item model early
        final Minecraft minecraft = Minecraft.getInstance();
        final ItemRenderer itemRenderer = minecraft.getItemRenderer();
        final BakedModel model = itemRenderer.getModel(itemStack, livingEntity.level(), livingEntity, light);

        final boolean isGui3d = model.isGui3d();

        if (isGui3d) {
            this.renderStatic(livingEntity, itemStack, itemDisplayContext, isLeftHand, poseStack, multiBufferSource, light);
            return;
        }

        this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(itemStack));

        final int renderedAmount = ItemEntityRenderer.getRenderedAmount(itemStack.getCount());
        final float scaleX = isLeftHand ? -model.getTransforms().firstPersonLeftHand.scale.x() : model.getTransforms().firstPersonRightHand.scale.x();

        for (int i = 0; i < renderedAmount; i++) {
            poseStack.pushPose();

            if (i > 0) {
                final float randomTranslateY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                final float randomTranslateZ = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                poseStack.translate(0.0F, randomTranslateY, randomTranslateZ);
            }

            this.renderStatic(livingEntity, itemStack, itemDisplayContext, isLeftHand, poseStack, multiBufferSource, light);

            poseStack.popPose();
            poseStack.translate(0.09375F * scaleX, 0.0F, 0.0F);
        }
    }

    @Unique
    private void renderStatic(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final boolean isLeftHand, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        this.itemRenderer.renderStatic(
                livingEntity,
                itemStack,
                itemDisplayContext,
                isLeftHand,
                poseStack,
                multiBufferSource,
                livingEntity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                livingEntity.getId() + itemDisplayContext.ordinal()
        );
    }
}
