package com.voidcyan.client.mixin.network;

import com.voidcyan.client.util.NameProtect;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket.SerializableTeam;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SerializableTeam.class})
public class MixinSerializableTeam {
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

   @Inject(
      method = {"getPrefix"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetPrefix(CallbackInfoReturnable<Text> cir) {
      Text original = (Text)cir.getReturnValue();
      if (original != null) {
         Text modified = NameProtect.processText(original);
         if (modified != null) {
            cir.setReturnValue(modified);
         }
      }
   }

   @Inject(
      method = {"getSuffix"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetSuffix(CallbackInfoReturnable<Text> cir) {
      Text original = (Text)cir.getReturnValue();
      if (original != null) {
         Text modified = NameProtect.processText(original);
         if (modified != null) {
            cir.setReturnValue(modified);
         }
      }
   }
}
