package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class EntityCounterSettingsScreen extends BaseSettingsScreen {
   public EntityCounterSettingsScreen(Screen parent) {
      super(parent, "EntityCounter Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Current Chunk Only", () -> VoidCyanClient.entityCounterCurrentChunkOnly, val -> VoidCyanClient.entityCounterCurrentChunkOnly = val);
      this.addBoolean("Show Players", () -> VoidCyanClient.entityCounterShowPlayers, val -> VoidCyanClient.entityCounterShowPlayers = val);
      this.addBoolean("Show Hostiles", () -> VoidCyanClient.entityCounterShowHostile, val -> VoidCyanClient.entityCounterShowHostile = val);
      this.addBoolean("Show Animals", () -> VoidCyanClient.entityCounterShowAnimals, val -> VoidCyanClient.entityCounterShowAnimals = val);
      this.addBoolean("Show Items", () -> VoidCyanClient.entityCounterShowItems, val -> VoidCyanClient.entityCounterShowItems = val);
      this.addBoolean("Show Total", () -> VoidCyanClient.entityCounterShowTotal, val -> VoidCyanClient.entityCounterShowTotal = val);
   }
}
