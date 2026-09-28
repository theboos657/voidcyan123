package com.voidcyan.client.mixin;

import com.mojang.authlib.GameProfile;
import com.voidcyan.client.util.NameProtect;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListHud.class})
public class MixinPlayerListHud {
   @Inject(
      method = {"getPlayerName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void injectNameProtectPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
      if (NameProtect.INSTANCE.isEnabled()) {
         Text original = (Text)cir.getReturnValue();
         if (original != null) {
            Text processed = NameProtect.processText(original);
            cir.setReturnValue(processed);
         } else {
            GameProfile profile = entry.getProfile();
            if (profile != null) {
               String profileName = profile.name();
               if (profileName != null) {
                  Text nameText = Text.literal(profileName);
                  AbstractTeam team = entry.getScoreboardTeam();
                  Text decorated;
                  if (team != null) {
                     decorated = team.decorateName(nameText);
                  } else {
                     decorated = nameText;
                  }

                  Text processed = NameProtect.processText(decorated);
                  cir.setReturnValue(processed);
               }
            }
         }
      }
   }
}
