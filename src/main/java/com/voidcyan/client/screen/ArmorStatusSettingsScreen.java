package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.AlarmSoundManager;
import net.minecraft.client.gui.screen.Screen;

public class ArmorStatusSettingsScreen extends BaseSettingsScreen {
   public ArmorStatusSettingsScreen(Screen parent) {
      super(parent, "Armor Status Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum(
         "Display Mode",
         new String[]{"Percentage", "Raw (Current/Max)", "Both"},
         () -> VoidCyanClient.armorStatusDisplayMode,
         val -> VoidCyanClient.armorStatusDisplayMode = val
      );
      this.addEnum(
         "Orientation", new String[]{"Vertical", "Horizontal"}, () -> VoidCyanClient.armorStatusOrientation, val -> VoidCyanClient.armorStatusOrientation = val
      );
      this.addBoolean("Border Glow", () -> VoidCyanClient.armorStatusGlow, val -> VoidCyanClient.armorStatusGlow = val);
      this.addBoolean("Show Durability Bar", () -> VoidCyanClient.armorStatusShowDurabilityBar, val -> VoidCyanClient.armorStatusShowDurabilityBar = val);
      this.addBoolean("Show Empty Slots", () -> VoidCyanClient.armorStatusShowEmptySlots, val -> VoidCyanClient.armorStatusShowEmptySlots = val);
      this.addBoolean("Warning Sound", () -> VoidCyanClient.armorStatusWarningSound, val -> VoidCyanClient.armorStatusWarningSound = val);
      this.addDropdown("Sound File", AlarmSoundManager::getAlarmOptions, () -> VoidCyanClient.armorStatusWarningSoundFile, val -> VoidCyanClient.armorStatusWarningSoundFile = val);
      this.addButton("Alarms Folder", "Open Folder", AlarmSoundManager::openFolder);
      this.addIntSlider("Warning Threshold", 0, 100, () -> VoidCyanClient.armorStatusWarningThreshold, val -> VoidCyanClient.armorStatusWarningThreshold = val);
      this.addBoolean("Transparent Background", () -> VoidCyanClient.armorStatusTransparentBg, val -> VoidCyanClient.armorStatusTransparentBg = val);
   }
}
