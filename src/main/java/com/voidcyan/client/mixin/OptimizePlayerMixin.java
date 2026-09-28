package com.voidcyan.client.mixin;

import com.voidcyan.client.OptimizeManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimize Players: skips rendering players whose position is hidden behind
 * solid blocks. Targets the shared living-entity render entry but cancels only
 * for player render states, so mobs and armor stands are unaffected.
 */
@Mixin(LivingEntityRenderer.class)
public class OptimizePlayerMixin {
   @Inject(
      method = {"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$cullOccludedPlayers(
      net.minecraft.client.render.entity.state.LivingEntityRenderState state,
      MatrixStack matrices,
      net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
      net.minecraft.client.render.state.CameraRenderState cameraState,
      CallbackInfo ci
   ) {
      if (state instanceof PlayerEntityRenderState) {
         EntityRenderState base = state;
         if (!OptimizeManager.shouldRenderPlayer(base.x, base.y, base.z)) {
            ci.cancel();
         }
      }
   }
}
