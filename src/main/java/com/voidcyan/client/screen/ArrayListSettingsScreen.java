package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ArrayListSettingsScreen extends BaseSettingsScreen {
   public ArrayListSettingsScreen(Screen parent) {
      super(parent, "ArrayList Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Show Background", () -> VoidCyanClient.arrayListBackground, val -> VoidCyanClient.arrayListBackground = val);
      this.addEnum(
         "Sort Mode",
         new String[]{"Alphabetical", "Length (longest first)", "Time Applied"},
         () -> VoidCyanClient.arrayListSortMode,
         val -> VoidCyanClient.arrayListSortMode = val
      );
   }
}
