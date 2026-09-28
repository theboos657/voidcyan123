package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.AlarmSoundManager;
import net.minecraft.client.gui.screen.Screen;

public class LowHealthAlarmSettingsScreen extends BaseSettingsScreen {
   public LowHealthAlarmSettingsScreen(Screen parent) {
      super(parent, "Low Health Alarm Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Play Sound", () -> VoidCyanClient.lowHealthAlarmSound, val -> VoidCyanClient.lowHealthAlarmSound = val);
      this.addDropdown("Alarm Sound", AlarmSoundManager::getAlarmOptions, () -> VoidCyanClient.lowHealthAlarmSoundFile, val -> VoidCyanClient.lowHealthAlarmSoundFile = val);
      this.addButton("Alarms Folder", "Open Folder", AlarmSoundManager::openFolder);
      this.addBoolean("Display Text", () -> VoidCyanClient.lowHealthAlarmText, val -> VoidCyanClient.lowHealthAlarmText = val);
      this.addIntSlider("Threshold (Half Hearts)", 1, 20, () -> VoidCyanClient.lowHealthAlarmThreshold, val -> VoidCyanClient.lowHealthAlarmThreshold = val);
      this.addIntSlider(
         "Totem Threshold (skip if more)", 0, 10, () -> VoidCyanClient.lowHealthTotemThreshold, val -> VoidCyanClient.lowHealthTotemThreshold = val
      );
      this.addBoolean("Use Theme Color", () -> VoidCyanClient.lowHealthUseThemeColor, val -> VoidCyanClient.lowHealthUseThemeColor = val);
   }
}
