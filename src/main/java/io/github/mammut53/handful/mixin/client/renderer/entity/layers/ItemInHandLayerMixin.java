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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    @Shadow @Final
    private ItemInHandRenderer itemInHandRenderer;
    @Unique
    private final RandomSource random = RandomSource.create();

    protected ItemInHandLayerMixin(final RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            cancellable = true
    )
    private void injectItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light, final CallbackInfo ci) {
        this.renderItemCluster(livingEntity, itemStack, itemDisplayContext, humanoidArm, poseStack, multiBufferSource, light);
        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void renderItemCluster(final LivingEntity livingEntity, final ItemStack itemStack, final ItemDisplayContext itemDisplayContext, final HumanoidArm humanoidArm, final PoseStack poseStack, final MultiBufferSource multiBufferSource, final int light) {
        final boolean isLeft = humanoidArm == HumanoidArm.LEFT;

        final Minecraft minecraft = Minecraft.getInstance();
        final ItemRenderer itemRenderer = minecraft.getItemRenderer();
        final BakedModel model = itemRenderer.getModel(itemStack, livingEntity.level(), livingEntity, light);

        final boolean isGui3d = model.isGui3d();
        if (isGui3d) {
            this.itemInHandRenderer.renderItem(livingEntity, itemStack, itemDisplayContext, isLeft, poseStack, multiBufferSource, light);
            return;
        }

        // Workaround for rendering just one item each step and being able to control the directions,
        // because Minecraft originally, and now we are calling itemInHandRenderer which we also
        // modified to render multiple items.
        final ItemStack itemStackWithCountOne = itemStack.copy();
        itemStackWithCountOne.setCount(1);

        this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(itemStack));

        final int renderedAmount = ItemEntityRenderer.getRenderedAmount(itemStack.getCount());

        final float scaleZ = switch (humanoidArm) {
            case LEFT -> model.getTransforms().thirdPersonLeftHand.scale.z();
            case RIGHT -> model.getTransforms().thirdPersonRightHand.scale.z();
        };

        poseStack.translate(0.0F, 0.0F, -0.01F);

        for (int i = 0; i < renderedAmount; i++) {
            poseStack.pushPose();

            if (i > 0) {
                final float randomTranslateX = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                final float randomTranslateY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                poseStack.translate(randomTranslateX, randomTranslateY, 0.0F);
            }

            this.itemInHandRenderer.renderItem(livingEntity, itemStackWithCountOne, itemDisplayContext, isLeft, poseStack, multiBufferSource, light);

            poseStack.popPose();
            poseStack.translate(0.0F, 0.0F, 0.09375F * scaleZ);
        }
    }
}
