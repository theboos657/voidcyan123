package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({StringVisitable.class})
public interface StringVisitableMixin {
   @Inject(
      method = {"getString"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetString(CallbackInfoReturnable<String> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         String original = (String)cir.getReturnValue();
         if (original != null) {
            String replaced = NameProtect.INSTANCE.replace(original);
            if (!replaced.equals(original)) {
               cir.setReturnValue(replaced);
            }
         }
      }
   }
}
