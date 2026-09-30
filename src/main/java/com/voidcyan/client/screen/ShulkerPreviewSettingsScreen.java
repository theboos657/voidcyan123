package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ShulkerPreviewSettingsScreen extends BaseSettingsScreen {
   public ShulkerPreviewSettingsScreen(Screen parent) {
      super(parent, "Shulker Preview Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isShulkerPreviewEnabled, val -> VoidCyanClient.isShulkerPreviewEnabled = val);
      this.addKeybind("Preview Key", () -> VoidCyanClient.shulkerPreviewKey, val -> VoidCyanClient.shulkerPreviewKey = val);
      this.addKeybind("Lock Key", () -> VoidCyanClient.shulkerPreviewLockKey, val -> VoidCyanClient.shulkerPreviewLockKey = val);
   }
}
