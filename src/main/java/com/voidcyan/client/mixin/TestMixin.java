package com.voidcyan.client.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({HeldItemRenderer.class})
public class TestMixin {
   @ModifyVariable(
      method = {"renderFirstPersonItem"},
      at = @At("HEAD"),
      ordinal = 1,
      argsOnly = true
   )
   private float modifySwing(float swing) {
      return 0.0F;
   }
}
