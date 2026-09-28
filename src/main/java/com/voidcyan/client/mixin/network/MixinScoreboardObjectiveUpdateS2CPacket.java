package com.voidcyan.client.mixin.network;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ScoreboardObjectiveUpdateS2CPacket.class})
public class MixinScoreboardObjectiveUpdateS2CPacket {
   @Inject(
      method = {"getName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetName(CallbackInfoReturnable<String> cir) {
      String original = (String)cir.getReturnValue();
      if (original != null) {
         cir.setReturnValue(NameProtect.INSTANCE.replace(original));
      }
   }

   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
      Text original = (Text)cir.getReturnValue();
      if (original != null) {
         Text modified = NameProtect.processText(original);
         if (modified != null) {
            cir.setReturnValue(modified);
         }
      }
   }
}
