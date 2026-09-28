package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.accessor.BigItemRenderStateAccess;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ItemEntityRenderer.class},
   priority = 2000
)
public abstract class BigItemsMixin {
   @Inject(
      method = {"updateRenderState"},
      at = {@At("TAIL")}
   )
   private void voidcyan$setBigItemState(ItemEntity entity, ItemEntityRenderState state, float tickProgress, CallbackInfo ci) {
      ((BigItemRenderStateAccess)state).voidcyan$setBigItem(VoidCyanClient.isBigItem(entity.getStack()));
   }

   @Inject(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V",
         shift = Shift.AFTER
      )}
   )
   private void voidcyan$scaleBigItem(
      ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (((BigItemRenderStateAccess)state).voidcyan$isBigItem()) {
         float scale = Math.clamp(VoidCyanClient.bigItemsScale, 1.0F, 20.0F);
         matrices.push();
         matrices.scale(scale, scale, scale);
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void voidcyan$restoreBigItemScale(
      ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      if (((BigItemRenderStateAccess)state).voidcyan$isBigItem()) {
         matrices.pop();
      }
   }
}
