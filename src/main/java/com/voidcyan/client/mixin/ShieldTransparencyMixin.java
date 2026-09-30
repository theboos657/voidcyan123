package com.voidcyan.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.ShieldModelRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShieldModelRenderer.class)
public class ShieldTransparencyMixin {
   @WrapOperation(
      method = "render(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IIZI)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;ZZILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;I)V"
      )
   )
   private void voidcyan$translucent(
      OrderedRenderCommandQueue queue,
      ModelPart part,
      MatrixStack matrices,
      RenderLayer layer,
      int light,
      int overlay,
      Sprite sprite,
      boolean sheeted,
      boolean hasGlint,
      int color,
      ModelCommandRenderer.CrumblingOverlayCommand crumbling,
      int outline,
      Operation<Void> original,
      @Local(argsOnly = true) ItemDisplayContext context
   ) {
      if (VoidCyanClient.isTransparentShieldEnabled
         && context != ItemDisplayContext.GUI
         && context != ItemDisplayContext.FIXED
         && context != ItemDisplayContext.GROUND) {
         layer = RenderLayers.entityTranslucent(sprite.getAtlasId());
         color = (VoidCyanClient.transparentShieldOpacity * 255 / 100) << 24 | 0xFFFFFF;
      }

      original.call(queue, part, matrices, layer, light, overlay, sprite, sheeted, hasGlint, color, crumbling, outline);
   }
}
