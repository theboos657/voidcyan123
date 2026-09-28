package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class TargetHudSettingsScreen extends BaseSettingsScreen {
   private static int currentSection = 0; // 0 = Main Target HUD, 1 = Enemy Armor, 2 = Enemy Info, 3 = Show All
   private static final String[] SECTIONS = new String[]{
      "Main Target HUD Settings",
      "Enemy Armor",
      "Enemy Info",
      "Show All"
   };

   public TargetHudSettingsScreen(Screen parent) {
      super(parent, "Target HUD Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Category", SECTIONS, () -> currentSection, val -> {
         currentSection = val;
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null) {
            mc.setScreen(new TargetHudSettingsScreen(this.parent));
         }
      });

      if (currentSection == 0 || currentSection == 3) {
         this.addBoolean("Render Above Enemy Head", () -> VoidCyanClient.targetHudAboveHead, val -> VoidCyanClient.targetHudAboveHead = val);
         this.addSlider("Above Head Scale", 0.2F, 2.0F, () -> VoidCyanClient.targetHudAboveHeadScale, val -> VoidCyanClient.targetHudAboveHeadScale = val);
         this.addBoolean("Stay After Looking Away", () -> VoidCyanClient.targetHudStickyEnabled, val -> VoidCyanClient.targetHudStickyEnabled = val);
         this.addIntSlider("Stay Duration (seconds)", 1, 15, () -> VoidCyanClient.targetHudStickyDuration, val -> VoidCyanClient.targetHudStickyDuration = val);
         this.addSlider("Target HUD Scale", 0.5F, 2.0F, () -> VoidCyanClient.targetHudScale, val -> VoidCyanClient.targetHudScale = val);
      }

      if (currentSection == 1 || currentSection == 3) {
         this.addBoolean("Show Armor", () -> VoidCyanClient.targetHudShowArmor, val -> VoidCyanClient.targetHudShowArmor = val);
         this.addBoolean("Show Armor Durability", () -> VoidCyanClient.targetHudShowArmorDurability, val -> VoidCyanClient.targetHudShowArmorDurability = val);
         this.addBoolean("Mmo Armor Display", () -> VoidCyanClient.targetHudMmoArmorDisplay, val -> VoidCyanClient.targetHudMmoArmorDisplay = val);
         this.addSlider("MMO Armor Scale", 0.5F, 2.0F, () -> VoidCyanClient.targetHudMmoArmorScale, val -> VoidCyanClient.targetHudMmoArmorScale = val);
      }

      if (currentSection == 2 || currentSection == 3) {
         this.addBoolean("Show Enemy Info", () -> VoidCyanClient.targetHudShowEnemyCrucials, val -> VoidCyanClient.targetHudShowEnemyCrucials = val);
         this.addBoolean("Show Enemy Totems", () -> VoidCyanClient.targetHudShowEnemyTotems, val -> VoidCyanClient.targetHudShowEnemyTotems = val);
         this.addBoolean("Show Enemy Pops", () -> VoidCyanClient.targetHudShowEnemyPops, val -> VoidCyanClient.targetHudShowEnemyPops = val);
         this.addBoolean("Show Enemy Gapples", () -> VoidCyanClient.targetHudShowEnemyGapples, val -> VoidCyanClient.targetHudShowEnemyGapples = val);
         this.addBoolean("Show Enemy Pearls", () -> VoidCyanClient.targetHudShowEnemyPearls, val -> VoidCyanClient.targetHudShowEnemyPearls = val);
         this.addBoolean("Show Enemy Wind Charges", () -> VoidCyanClient.targetHudShowEnemyWindCharges, val -> VoidCyanClient.targetHudShowEnemyWindCharges = val);
         this.addBoolean("Show Enemy Winds Used", () -> VoidCyanClient.targetHudShowEnemyWindChargesUsed, val -> VoidCyanClient.targetHudShowEnemyWindChargesUsed = val);
         this.addBoolean("Show Enemy Cobwebs", () -> VoidCyanClient.targetHudShowEnemyCobwebs, val -> VoidCyanClient.targetHudShowEnemyCobwebs = val);
         this.addBoolean("Show Enemy Health Pots", () -> VoidCyanClient.targetHudShowEnemyHealthPots, val -> VoidCyanClient.targetHudShowEnemyHealthPots = val);
         this.addSlider("Enemy Info Scale", 0.5F, 2.0F, () -> VoidCyanClient.enemyCrucialsHudScale, val -> VoidCyanClient.enemyCrucialsHudScale = val);
      }
   }
}
