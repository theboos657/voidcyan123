package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.AlarmSoundManager;
import net.minecraft.client.gui.screen.Screen;

public class KeyboardSoundsSettingsScreen extends BaseSettingsScreen {
   public KeyboardSoundsSettingsScreen(Screen parent) {
      super(parent, "Keyboard Sounds Settings");
   }

   @Override
   protected void buildSettings() {
      this.addDropdown("Sound", AlarmSoundManager::getAlarmOptions, () -> VoidCyanClient.keyboardSoundsFile, val -> VoidCyanClient.keyboardSoundsFile = val);
      this.addButton("Sounds Folder", "Open Folder", AlarmSoundManager::openFolder);
      this.addKeybind(
         "Play Sound Key", () -> VoidCyanClient.getModuleKey("Play Keyboard Sound"), val -> VoidCyanClient.setModuleKey("Play Keyboard Sound", val)
      );
   }
}
