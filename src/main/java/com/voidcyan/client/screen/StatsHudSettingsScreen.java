package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class StatsHudSettingsScreen extends BaseSettingsScreen {
   public StatsHudSettingsScreen(Screen parent) {
      super(parent, "Stats HUD Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Stats Mode", new String[]{"Session", "All-Time"}, () -> VoidCyanClient.statsHudStatMode, val -> VoidCyanClient.statsHudStatMode = val);
      this.addEnum(
         "Enable in Multiplayer",
         new String[]{"Disabled", "Enabled"},
         () -> VoidCyanClient.statsHudShowInMultiplayer ? 1 : 0,
         val -> VoidCyanClient.statsHudShowInMultiplayer = val == 1
      );
      String[] statsOptions = new String[]{
         "Accuracy",
         "Anchors",
         "Anchors Placed",
         "Anchors Blown",
         "Glowstone Used",
         "Blocks Broken",
         "Blocks Placed",
         "Clicks",
         "Crystals Placed",
         "Crystals Broken",
         "Crystal Deaths",
         "Crystal Kills",
         "Damage",
         "Deaths",
         "Hits",
         "Attr Swap %",
         "K/D",
         "Kills",
         "Pops",
         "Time"
      };
      this.addMultiSelect(
         "Displayed Stats",
         statsOptions,
         () -> new boolean[]{
            VoidCyanClient.statsHudShowAccuracy,
            VoidCyanClient.statsHudShowAnchors,
            VoidCyanClient.statsHudShowAnchorsPlaced,
            VoidCyanClient.statsHudShowAnchorsBlown,
            VoidCyanClient.statsHudShowAnchorsCharged,
            VoidCyanClient.statsHudShowBlocksBroken,
            VoidCyanClient.statsHudShowBlocksPlaced,
            VoidCyanClient.statsHudShowClicks,
            VoidCyanClient.statsHudShowCrystalsPlaced,
            VoidCyanClient.statsHudShowCrystalsBroken,
            VoidCyanClient.statsHudShowCrystalDeaths,
            VoidCyanClient.statsHudShowCrystalKills,
            VoidCyanClient.statsHudShowDamage,
            VoidCyanClient.statsHudShowDeaths,
            VoidCyanClient.statsHudShowHits,
            VoidCyanClient.statsHudShowAttributeSwap,
            VoidCyanClient.statsHudShowKD,
            VoidCyanClient.statsHudShowKills,
            VoidCyanClient.statsHudShowPops,
            VoidCyanClient.statsHudShowTime
         },
         val -> {
            VoidCyanClient.statsHudShowAccuracy = val[0];
            VoidCyanClient.statsHudShowAnchors = val[1];
            VoidCyanClient.statsHudShowAnchorsPlaced = val[2];
            VoidCyanClient.statsHudShowAnchorsBlown = val[3];
            VoidCyanClient.statsHudShowAnchorsCharged = val[4];
            VoidCyanClient.statsHudShowBlocksBroken = val[5];
            VoidCyanClient.statsHudShowBlocksPlaced = val[6];
            VoidCyanClient.statsHudShowClicks = val[7];
            VoidCyanClient.statsHudShowCrystalsPlaced = val[8];
            VoidCyanClient.statsHudShowCrystalsBroken = val[9];
            VoidCyanClient.statsHudShowCrystalDeaths = val[10];
            VoidCyanClient.statsHudShowCrystalKills = val[11];
            VoidCyanClient.statsHudShowDamage = val[12];
            VoidCyanClient.statsHudShowDeaths = val[13];
            VoidCyanClient.statsHudShowHits = val[14];
            VoidCyanClient.statsHudShowAttributeSwap = val[15];
            VoidCyanClient.statsHudShowKD = val[16];
            VoidCyanClient.statsHudShowKills = val[17];
            VoidCyanClient.statsHudShowPops = val[18];
            VoidCyanClient.statsHudShowTime = val[19];
         }
      );
   }
}
