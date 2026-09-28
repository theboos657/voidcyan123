package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.AlarmSoundManager;
import net.minecraft.client.gui.screen.Screen;

public class PotWarningSettingsScreen extends BaseSettingsScreen {
   public PotWarningSettingsScreen(Screen parent) {
      super(parent, "Pot Warning Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Low Pots Warning", () -> VoidCyanClient.potWarnLowPots, val -> VoidCyanClient.potWarnLowPots = val);
      this.addIntSlider("Pot Threshold (warn if <= N)", 0, 20, () -> VoidCyanClient.potWarnPotThreshold, val -> VoidCyanClient.potWarnPotThreshold = val);
      this.addBoolean("Low Effect Warning", () -> VoidCyanClient.potWarnLowEffects, val -> VoidCyanClient.potWarnLowEffects = val);
      this.addIntSlider(
         "Effect Duration Threshold (ticks)", 20, 1200, () -> VoidCyanClient.potWarnEffectThreshold, val -> VoidCyanClient.potWarnEffectThreshold = val
      );
      this.addBoolean("Play Sound", () -> VoidCyanClient.potWarnSound, val -> VoidCyanClient.potWarnSound = val);
      this.addDropdown("Alarm Sound", AlarmSoundManager::getAlarmOptions, () -> VoidCyanClient.potWarnSoundFile, val -> VoidCyanClient.potWarnSoundFile = val);
      this.addButton("Alarms Folder", "Open Folder", AlarmSoundManager::openFolder);
      this.addBoolean("Show Text", () -> VoidCyanClient.potWarnShowText, val -> VoidCyanClient.potWarnShowText = val);
      this.addBoolean("Use Theme Color", () -> VoidCyanClient.potWarnUseThemeColor, val -> VoidCyanClient.potWarnUseThemeColor = val);
      this.addInputField(
         "Ignored Effects (comma-separated, e.g. night_vision)", () -> VoidCyanClient.potWarnIgnoredEffects, val -> VoidCyanClient.potWarnIgnoredEffects = val
      );
   }
}
