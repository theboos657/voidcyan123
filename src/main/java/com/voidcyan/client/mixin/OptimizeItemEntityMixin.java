package com.voidcyan.client.mixin;

import com.voidcyan.client.OptimizeManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimize Items: skips rendering dropped item entities that are hidden behind
 * solid blocks. Cancellation happens before any draw commands are queued.
 */
@Mixin(ItemEntityRenderer.class)
public class OptimizeItemEntityMixin {
   @Inject(
      method = {"render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$cullOccludedItems(ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
      if (state != null && !OptimizeManager.shouldRenderItem(state.x, state.y, state.z)) {
         ci.cancel();
      }
   }
}
