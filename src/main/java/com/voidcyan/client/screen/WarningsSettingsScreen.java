package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class WarningsSettingsScreen extends BaseSettingsScreen {
   public WarningsSettingsScreen(Screen parent) {
      super(parent, "Warnings Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Low Health Alarm", () -> VoidCyanClient.isLowHealthAlarmEnabled, val -> VoidCyanClient.isLowHealthAlarmEnabled = val);
      this.addBoolean("LHA: Play Sound", () -> VoidCyanClient.lowHealthAlarmSound, val -> VoidCyanClient.lowHealthAlarmSound = val);
      this.addBoolean("LHA: Display Text", () -> VoidCyanClient.lowHealthAlarmText, val -> VoidCyanClient.lowHealthAlarmText = val);
      this.addIntSlider(
         "LHA: Threshold (Half Hearts)", 1, 20, () -> VoidCyanClient.lowHealthAlarmThreshold, val -> VoidCyanClient.lowHealthAlarmThreshold = val
      );
      this.addIntSlider("LHA: Totem Skip Threshold", 0, 10, () -> VoidCyanClient.lowHealthTotemThreshold, val -> VoidCyanClient.lowHealthTotemThreshold = val);
      this.addBoolean("LHA: Use Theme Color", () -> VoidCyanClient.lowHealthUseThemeColor, val -> VoidCyanClient.lowHealthUseThemeColor = val);
      this.addBoolean("Pot Warning", () -> VoidCyanClient.isPotWarningEnabled, val -> VoidCyanClient.isPotWarningEnabled = val);
      this.addBoolean("PW: Low Pots Warning", () -> VoidCyanClient.potWarnLowPots, val -> VoidCyanClient.potWarnLowPots = val);
      this.addIntSlider("PW: Pot Threshold (<= N)", 0, 20, () -> VoidCyanClient.potWarnPotThreshold, val -> VoidCyanClient.potWarnPotThreshold = val);
      this.addBoolean("PW: Low Effect Warning", () -> VoidCyanClient.potWarnLowEffects, val -> VoidCyanClient.potWarnLowEffects = val);
      this.addIntSlider(
         "PW: Effect Threshold (ticks)", 20, 1200, () -> VoidCyanClient.potWarnEffectThreshold, val -> VoidCyanClient.potWarnEffectThreshold = val
      );
      this.addBoolean("PW: Play Sound", () -> VoidCyanClient.potWarnSound, val -> VoidCyanClient.potWarnSound = val);
      this.addBoolean("PW: Show Text", () -> VoidCyanClient.potWarnShowText, val -> VoidCyanClient.potWarnShowText = val);
      this.addBoolean("PW: Use Theme Color", () -> VoidCyanClient.potWarnUseThemeColor, val -> VoidCyanClient.potWarnUseThemeColor = val);
   }
}
