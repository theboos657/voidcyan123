package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ItemAnimations {
   public static boolean applyAnimation(MatrixStack matrix, int armIndex, float swingProgress) {
      if (!VoidCyanClient.isItemAnimationsEnabled) {
         return false;
      } else {
         int style = VoidCyanClient.itemAnimationMode;
         if (style == 0) {
            return false;
         } else {
            float squaredProgress = swingProgress * swingProgress;
            float easeInOut = MathHelper.sin(squaredProgress * (float) Math.PI);
            float easeOut = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            float smoothPhase = (float)(Math.sin(swingProgress * Math.PI) * 0.5);
            float sideMultiplier = armIndex;
            switch (style) {
               case 1:
                  transformSwipe(matrix, sideMultiplier, easeInOut, easeOut);
                  break;
               case 2:
                  transformDownward(matrix, sideMultiplier, easeOut);
                  break;
               case 3:
                  transformSmooth(matrix, sideMultiplier, easeInOut, easeOut);
                  break;
               case 4:
                  transformPower(matrix, sideMultiplier, easeInOut, easeOut, smoothPhase);
                  break;
               case 5:
                  transformFeast(matrix, sideMultiplier, easeOut);
                  break;
               case 6:
                  transformCustom(matrix, sideMultiplier, easeOut);
                  break;
               default:
                  return false;
            }

            return true;
         }
      }
   }

   private static void transformSwipe(MatrixStack stack, float side, float easeInOut, float easeOut) {
      stack.translate(0.56F * side, -0.32F, -0.72F);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0F * side));
      stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0F * side));
      float combinedEase = easeOut * easeInOut;
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(combinedEase * -5.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(combinedEase * -120.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
   }

   private static void transformDownward(MatrixStack stack, float side, float easeOut) {
      stack.translate(side * 0.56F, -0.32F, -0.72F);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0F * side));
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(easeOut * -5.0F));
      stack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(easeOut * -100.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(easeOut * -155.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
   }

   private static void transformSmooth(MatrixStack stack, float side, float easeInOut, float easeOut) {
      stack.translate(side * 0.56F, -0.42F, -0.72F);
      float yawOffset = 45.0F + easeInOut * -20.0F;
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * yawOffset));
      stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * easeOut * -20.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(easeOut * -80.0F));
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0F));
      stack.translate(0.0, -0.1, 0.0);
   }

   private static void transformPower(MatrixStack stack, float side, float easeInOut, float easeOut, float smoothPhase) {
      stack.translate(side * 0.56F, -0.32F, -0.72F);
      float shift = -smoothPhase * smoothPhase * easeInOut * side;
      stack.translate(shift, 0.0F, 0.0F);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61.0F * side));
      stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(easeOut));
      float combo = easeOut * easeInOut;
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(combo * -5.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(combo * -30.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(smoothPhase * -60.0F));
   }

   private static void transformFeast(MatrixStack stack, float side, float easeOut) {
      stack.translate(side * 0.56F, -0.32F, -0.72F);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F * side));
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(easeOut * 75.0F * side));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(easeOut * -45.0F));
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F * side));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35.0F * side));
   }

   private static void transformCustom(MatrixStack stack, float side, float progressEase) {
      float pAngle = VoidCyanClient.customSwingPitch * progressEase;
      float yAngle = VoidCyanClient.customSwingYaw * progressEase * side;
      float rAngle = VoidCyanClient.customSwingRoll * progressEase;
      stack.translate(side * 0.56F, -0.32F, -0.72F);
      if (pAngle != 0.0F) {
         stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pAngle));
      }

      if (yAngle != 0.0F) {
         stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yAngle));
      }

      if (rAngle != 0.0F) {
         stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rAngle));
      }
   }
}
