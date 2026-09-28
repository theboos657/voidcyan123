package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class NotificationsSettingsScreen extends BaseSettingsScreen {
   public NotificationsSettingsScreen(Screen parent) {
      super(parent, "Notifications Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Anim Enabled", () -> VoidCyanClient.isNotificationsAnimEnabled, val -> VoidCyanClient.isNotificationsAnimEnabled = val);
      this.addIntSlider("Direction", 0, 100, () -> VoidCyanClient.notificationsAnimDirection, val -> VoidCyanClient.notificationsAnimDirection = val);
   }
}
