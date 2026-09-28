package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ZoomSettingsScreen extends BaseSettingsScreen {
   public ZoomSettingsScreen(Screen parent) {
      super(parent, "Zoom Settings");
   }

   @Override
   protected boolean includeAutoToggleKeybind() {
      return false;
   }

   @Override
   protected void buildSettings() {
      this.addSlider("Starter Zoom Level", 1.0F, 50.0F, () -> (float)VoidCyanClient.zoomLevel, val -> {
         VoidCyanClient.zoomLevel = val.floatValue();
         VoidCyanClient.markConfigDirty();
      });
      this.addSlider("Scroll Sensitivity", 0.1F, 10.0F, () -> (float)VoidCyanClient.zoomScrollSensitivity, val -> {
         VoidCyanClient.zoomScrollSensitivity = val.floatValue();
         VoidCyanClient.markConfigDirty();
      });
      this.addBoolean("Smooth Animation", () -> VoidCyanClient.zoomSmoothAnimation, val -> VoidCyanClient.zoomSmoothAnimation = val);
      this.addSlider("Animation Speed", 0.02F, 0.50F, () -> VoidCyanClient.zoomAnimationSpeed, val -> VoidCyanClient.zoomAnimationSpeed = val);
      this.addBoolean("Toggle Mode", () -> VoidCyanClient.zoomToggleMode, val -> VoidCyanClient.zoomToggleMode = val);
      this.addKeybind("Zoom Key", () -> VoidCyanClient.zoomKey, val -> VoidCyanClient.zoomKey = val);
   }
}
