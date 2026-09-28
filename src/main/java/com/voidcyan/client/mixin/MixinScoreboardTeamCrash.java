package com.voidcyan.client.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Scoreboard.class})
public class MixinScoreboardTeamCrash {
   @Inject(
      method = {"removeScoreHolderFromTeam"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$suppressRemovePlayerFromTeamCrash(String playerName, Team team, CallbackInfo ci) {
      Scoreboard scoreboard = (Scoreboard)(Object)this;
      Team currentTeam = scoreboard.getScoreHolderTeam(playerName);
      if (currentTeam == null || !currentTeam.equals(team)) {
         ci.cancel();
      }
   }
}
