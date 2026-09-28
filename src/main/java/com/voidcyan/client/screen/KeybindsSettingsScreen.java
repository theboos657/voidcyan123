package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class KeybindsSettingsScreen extends BaseSettingsScreen {
   public KeybindsSettingsScreen(Screen parent) {
      super(parent, "Keybinds Display Settings");
   }

   @Override
   protected void buildSettings() {
      this.addButton("Reset Position", "Reset", () -> {
         VoidCyanClient.keybindsHudX = 10;
         VoidCyanClient.keybindsHudY = 100;
      });
   }
}
