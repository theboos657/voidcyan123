package com.voidcyan.client.mixin;

import com.voidcyan.client.FeatureModules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bossbar customizer: hide, scale and move the boss bars. */
@Mixin(BossBarHud.class)
public class BossBarHudMixin {
   @Unique
   private boolean voidcyan$pushed = false;

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void voidcyan$bossHead(DrawContext context, CallbackInfo ci) {
      this.voidcyan$pushed = false;
      if (!FeatureModules.on[FeatureModules.BOSS]) return;
      if (FeatureModules.bossHide) {
         ci.cancel();
         return;
      }

      float cx = context.getScaledWindowWidth() / 2.0F;
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(cx + FeatureModules.bossOffsetX, FeatureModules.bossOffsetY);
      context.getMatrices().scale(FeatureModules.bossScale, FeatureModules.bossScale);
      context.getMatrices().translate(-cx, 0.0F);
      this.voidcyan$pushed = true;
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void voidcyan$bossReturn(DrawContext context, CallbackInfo ci) {
      if (this.voidcyan$pushed) {
         context.getMatrices().popMatrix();
         this.voidcyan$pushed = false;
      }
   }
}
