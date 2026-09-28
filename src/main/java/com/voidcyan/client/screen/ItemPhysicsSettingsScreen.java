package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ItemPhysicsSettingsScreen extends BaseSettingsScreen {
   public ItemPhysicsSettingsScreen(Screen parent) {
      super(parent, "Item Physics Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Falling Flip", () -> VoidCyanClient.itemPhysicsFallFlips, val -> {
         VoidCyanClient.itemPhysicsFallFlips = val;
         VoidCyanClient.markConfigDirty();
      });
      // How many full flips an item does per block fallen (only while airborne).
      this.addIntSlider("Flips per Block", 1, 10, () -> VoidCyanClient.itemPhysicsFlipsPerBlock, val -> {
         VoidCyanClient.itemPhysicsFlipsPerBlock = val;
         VoidCyanClient.markConfigDirty();
      }, () -> VoidCyanClient.itemPhysicsFallFlips);
      this.addSlider("X Rotation (deg)", 0.0F, 180.0F, () -> VoidCyanClient.itemPhysicsRotationX, val -> {
         VoidCyanClient.itemPhysicsRotationX = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addSlider("Y Offset", -1.0F, 1.0F, () -> VoidCyanClient.itemPhysicsOffsetY, val -> {
         VoidCyanClient.itemPhysicsOffsetY = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addSlider("Z Offset", -1.0F, 1.0F, () -> VoidCyanClient.itemPhysicsOffsetZ, val -> {
         VoidCyanClient.itemPhysicsOffsetZ = val;
         VoidCyanClient.markConfigDirty();
      });
   }
}
