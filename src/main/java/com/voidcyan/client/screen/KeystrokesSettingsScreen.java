package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class KeystrokesSettingsScreen extends BaseSettingsScreen {
   private static final String[] MODES = new String[]{"100% Full", "75% Compact", "60% Minimal", "50% QWERTY", "40% WASD", "30% Mini", "20% Mouse", "10% CPS"};

   public KeystrokesSettingsScreen(Screen parent) {
      super(parent, "Keystrokes Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Mode", MODES, () -> VoidCyanClient.keystrokesMode, val -> VoidCyanClient.keystrokesMode = val);
      this.addBoolean("Show Mouse", () -> VoidCyanClient.keystrokesShowMouse, val -> VoidCyanClient.keystrokesShowMouse = val);
   }
}
