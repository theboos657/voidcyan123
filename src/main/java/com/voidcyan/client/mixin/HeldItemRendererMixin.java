package com.voidcyan.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.ItemAnimations;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public class HeldItemRendererMixin {
   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
         shift = Shift.AFTER
      )},
      cancellable = false
   )
   private void onAfterMatrixPush(
      AbstractClientPlayerEntity player,
      float tickProgress,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      boolean isMainHand = hand == Hand.MAIN_HAND;
      boolean vmEnabled = isMainHand ? VoidCyanClient.isMainHandViewModelEnabled : VoidCyanClient.isOffHandViewModelEnabled;
      if (vmEnabled) {
         String itemId = Registries.ITEM.getId(item.getItem()).toString();
         VoidCyanClient.ViewModelSettings settings;
         if (isMainHand && VoidCyanClient.mainHandItemOverrides.containsKey(itemId)) {
            settings = VoidCyanClient.mainHandItemOverrides.get(itemId);
         } else if (!isMainHand && VoidCyanClient.offHandItemOverrides.containsKey(itemId)) {
            settings = VoidCyanClient.offHandItemOverrides.get(itemId);
         } else {
            settings = isMainHand ? VoidCyanClient.mainHandGlobal : VoidCyanClient.offHandGlobal;
         }

         if (settings.enabled) {
            matrices.translate(settings.x, settings.y, settings.z);
            if (settings.rotX != 0.0F) {
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(settings.rotX));
            }

            if (settings.rotY != 0.0F) {
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(settings.rotY));
            }

            if (settings.rotZ != 0.0F) {
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(settings.rotZ));
            }

            if (settings.scale != 1.0F) {
               matrices.scale(settings.scale, settings.scale, settings.scale);
            }
         }
      }
   }

   @WrapOperation(
      method = {"renderFirstPersonItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",
         ordinal = 2
      )}
   )
   private void onSwingArm(
      HeldItemRenderer instance,
      float swingProgress,
      MatrixStack matrices,
      int armX,
      Arm arm,
      Operation<Void> original,
      @Local(ordinal = 0,argsOnly = true) AbstractClientPlayerEntity player,
      @Local(ordinal = 0,argsOnly = true) Hand hand,
      @Local(ordinal = 3,argsOnly = true) float equipProgress
   ) {
      if (!VoidCyanClient.isItemAnimationsEnabled || VoidCyanClient.itemAnimationMode == 0) {
         original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
      } else if (hand != Hand.MAIN_HAND) {
         original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
      } else {
         int i = arm == Arm.RIGHT ? 1 : -1;
         float baseY = -0.52F + equipProgress * -0.6F;
         matrices.translate(-i * 0.56F, -baseY, 0.72F);
         boolean animated = ItemAnimations.applyAnimation(matrices, i, swingProgress);
         if (!animated) {
            matrices.translate(i * 0.56F, baseY, -0.72F);
            original.call(new Object[]{instance, swingProgress, matrices, armX, arm});
         }
      }
   }
}
