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
      this.addBoolean("Show Zoom Amount (x)", () -> com.voidcyan.client.FeatureModules.zoomIndicator, val -> {
         com.voidcyan.client.FeatureModules.zoomIndicator = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addBoolean("Auto Zoom-in Ramp (up to 1000x in 30s)", () -> com.voidcyan.client.FeatureModules.zoomRamp, val -> {
         com.voidcyan.client.FeatureModules.zoomRamp = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addSlider("Starter Zoom Level", 2.0F, 100.0F, () -> (float)VoidCyanClient.zoomLevel, val -> {
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
