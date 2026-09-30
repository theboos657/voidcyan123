package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class CoordinatesSettingsScreen extends BaseSettingsScreen {
   public CoordinatesSettingsScreen(Screen parent) {
      super(parent, "Coordinates Settings");
   }

   @Override
   protected void buildSettings() {
      this.addKeybind("Copy Coordinates", () -> VoidCyanClient.getModuleKey("Copy Coordinates"), val -> VoidCyanClient.setModuleKey("Copy Coordinates", val));
   }
}
