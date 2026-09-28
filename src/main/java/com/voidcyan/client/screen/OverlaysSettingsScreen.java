package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class OverlaysSettingsScreen extends BaseSettingsScreen {
   public OverlaysSettingsScreen(Screen parent) {
      super(parent, "On-Screen Overlays");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Fire Overlay", () -> VoidCyanClient.isFireEnabled, val -> VoidCyanClient.isFireEnabled = val);
      this.addBoolean("Pumpkin Overlay", () -> VoidCyanClient.isPumpkinEnabled, val -> VoidCyanClient.isPumpkinEnabled = val);
      this.addBoolean("Nether Portal Overlay", () -> VoidCyanClient.isPortalEnabled, val -> VoidCyanClient.isPortalEnabled = val);
      this.addBoolean("Hurtcam", () -> VoidCyanClient.isHurtcamEnabled, val -> VoidCyanClient.isHurtcamEnabled = val);
      this.addBoolean("Water Fog", () -> VoidCyanClient.isWaterFogEnabled, val -> VoidCyanClient.isWaterFogEnabled = val);
   }
}
