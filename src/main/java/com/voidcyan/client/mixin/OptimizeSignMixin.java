package com.voidcyan.client.mixin;

import com.voidcyan.client.OptimizeManager;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.block.entity.state.SignBlockEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimize Signs: sign blocks themselves still render, but the text on them is
 * skipped beyond the configured distance (1-128 blocks), which removes the bulk
 * of the sign rendering cost in item-frame/sign-heavy farms.
 */
@Mixin(AbstractSignBlockEntityRenderer.class)
public class OptimizeSignMixin {
   @Inject(
      method = {"renderText(Lnet/minecraft/client/render/block/entity/state/SignBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$cullDistantSignText(SignBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean front, CallbackInfo ci) {
      if (state != null && state.pos != null && !OptimizeManager.shouldRenderSignText(state.pos)) {
         ci.cancel();
      }
   }
}
