package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.EntityRenderStateAccessor;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public class ItemPhysicsMixin {
   @Inject(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V",
         shift = Shift.AFTER
      )}
   )
   private void voidcyan$applyItemPhysics(
      ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (VoidCyanClient.isItemPhysicsEnabled) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(VoidCyanClient.itemPhysicsRotationX));
         matrices.translate(0.0F, VoidCyanClient.itemPhysicsOffsetY, VoidCyanClient.itemPhysicsOffsetZ);

         // Falling spin: items flip while airborne, scaled by fall distance
         // (flips-per-block setting). Zero while resting on the ground.
         if (VoidCyanClient.itemPhysicsFallFlips
               && state instanceof EntityRenderStateAccessor accessor
               && accessor.voidcyan$getFallTumble() != 0.0F) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(accessor.voidcyan$getFallTumble()));
         }
      }
   }
}
