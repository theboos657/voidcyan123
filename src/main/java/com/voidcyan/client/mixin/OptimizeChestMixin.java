package com.voidcyan.client.mixin;

import com.voidcyan.client.OptimizeManager;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimize Chests: skips rendering chests (regular, trapped, ender, barrels via
 * their own renderer) whose block position is hidden behind solid blocks.
 */
@Mixin(ChestBlockEntityRenderer.class)
public class OptimizeChestMixin {
   @Inject(
      method = {"render(Lnet/minecraft/client/render/block/entity/state/ChestBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$cullOccludedChests(ChestBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
      if (state != null && state.pos != null && !OptimizeManager.shouldRenderChestAt(state.pos)) {
         ci.cancel();
      }
   }
}
