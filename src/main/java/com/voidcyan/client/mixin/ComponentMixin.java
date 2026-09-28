package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameProtect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net/minecraft/text/StringVisitable$Lazy"}
)
public class ComponentMixin {
   @Inject(
      method = {"getString()Ljava/lang/String;"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0,
      remap = false
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
