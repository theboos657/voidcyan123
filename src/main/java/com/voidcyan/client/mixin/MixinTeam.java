package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Team.class})
public class MixinTeam {
   @Inject(
      method = "decorateName(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
      at = @At("RETURN"),
      cancellable = true
   )
   private static void injectNameProtectDecorateName(Text name, CallbackInfoReturnable<MutableText> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         MutableText original = (MutableText)cir.getReturnValue();
         if (original != null) {
            Text processed = NameProtect.processText(original);
            if (processed != null) {
               cir.setReturnValue(processed.copy());
            }
         }
      }
   }
}
