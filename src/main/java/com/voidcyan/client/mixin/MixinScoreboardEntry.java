package com.voidcyan.client.mixin;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ScoreboardEntry.class})
public class MixinScoreboardEntry {
   @Inject(
      method = {"name"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectNameProtectScoreboardName(CallbackInfoReturnable<Text> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         Text original = (Text)cir.getReturnValue();
         if (original != null) {
            Text processed = NameProtect.processText(original);
            if (processed != null && processed != original) {
               cir.setReturnValue(processed);
            }
         }
      }
   }
}
