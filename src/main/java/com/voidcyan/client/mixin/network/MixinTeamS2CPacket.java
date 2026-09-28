package com.voidcyan.client.mixin.network;

import com.voidcyan.client.util.NameProtect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TeamS2CPacket.class})
public class MixinTeamS2CPacket {
   @Inject(
      method = {"getPlayerNames"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetPlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
      Collection<String> original = (Collection<String>)cir.getReturnValue();
      if (original != null && !original.isEmpty()) {
         List<String> modified = new ArrayList<>(original.size());

         for (String s : original) {
            modified.add(NameProtect.INSTANCE.replace(s));
         }

         cir.setReturnValue(modified);
      }
   }

   @Inject(
      method = {"getTeamName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetTeamName(CallbackInfoReturnable<String> cir) {
      String original = (String)cir.getReturnValue();
      if (original != null) {
         cir.setReturnValue(NameProtect.INSTANCE.replace(original));
      }
   }
}
