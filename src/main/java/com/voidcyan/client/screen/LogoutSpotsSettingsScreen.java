package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class LogoutSpotsSettingsScreen extends BaseSettingsScreen {
   public LogoutSpotsSettingsScreen(Screen parent) {
      super(parent, "Logout Spots Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Detect Disappears", () -> VoidCyanClient.logoutSpotsDetectDisappears, val -> VoidCyanClient.logoutSpotsDetectDisappears = val);
      this.addBoolean("Leave Messages", () -> VoidCyanClient.logoutSpotsLeaveMessages, val -> VoidCyanClient.logoutSpotsLeaveMessages = val);
      this.addBoolean("Ignore Friends", () -> VoidCyanClient.logoutSpotsIgnoreFriends, val -> VoidCyanClient.logoutSpotsIgnoreFriends = val);
      this.addBoolean("Create Waypoints", () -> VoidCyanClient.logoutSpotsCreateWaypoints, val -> VoidCyanClient.logoutSpotsCreateWaypoints = val);
      this.addBoolean("Save Rotation", () -> VoidCyanClient.logoutSpotsSaveRotation, val -> VoidCyanClient.logoutSpotsSaveRotation = val);
      this.addBoolean("Save Held Item", () -> VoidCyanClient.logoutSpotsSaveHeldItem, val -> VoidCyanClient.logoutSpotsSaveHeldItem = val);
      this.addButton("Manage Spots", "Open GUI", () -> MinecraftClient.getInstance().setScreen(new LogoutSpotsScreen(this)));
   }
}
