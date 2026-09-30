package com.voidcyan.client.screen;

import com.voidcyan.client.FeatureModules;
import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

/** Settings page for the newer modules (Bossbar, Block Overlay, Chunk Borders, ...). One class, switched by module name. */
public class FeatureSettingsScreen extends BaseSettingsScreen {
   private final String module;

   public FeatureSettingsScreen(Screen parent, String module) {
      super(parent, module + " Settings");
      this.module = module;
   }

   private void dirty() {
      VoidCyanClient.markConfigDirty();
   }

   @Override
   protected void buildSettings() {
      switch (this.module) {
         case "Bossbar" -> {
            this.addBoolean("Hide Bossbar", () -> FeatureModules.bossHide, v -> {
               FeatureModules.bossHide = v;
               this.dirty();
            });
            this.addSlider("Scale", 0.3F, 2.0F, () -> FeatureModules.bossScale, v -> {
               FeatureModules.bossScale = v;
               this.dirty();
            });
            this.addIntSlider("Offset X", -300, 300, () -> FeatureModules.bossOffsetX, v -> {
               FeatureModules.bossOffsetX = v;
               this.dirty();
            });
            this.addIntSlider("Offset Y", -20, 300, () -> FeatureModules.bossOffsetY, v -> {
               FeatureModules.bossOffsetY = v;
               this.dirty();
            });
         }
         case "Day Counter" -> {
            this.addBoolean("Show Clock", () -> FeatureModules.dayShowClock, v -> {
               FeatureModules.dayShowClock = v;
               this.dirty();
            });
            this.addBoolean("24 Hour Clock", () -> FeatureModules.dayClock24h, v -> {
               FeatureModules.dayClock24h = v;
               this.dirty();
            });
            this.addSlider("Scale", 0.5F, 2.0F, () -> FeatureModules.dayScale, v -> {
               FeatureModules.dayScale = v;
               this.dirty();
            });
         }
         case "Direction HUD" -> {
            this.addBoolean("Show Yaw / Pitch", () -> FeatureModules.dirShowAngles, v -> {
               FeatureModules.dirShowAngles = v;
               this.dirty();
            });
            this.addSlider("Scale", 0.5F, 2.0F, () -> FeatureModules.dirScale, v -> {
               FeatureModules.dirScale = v;
               this.dirty();
            });
         }
         case "Height Limit" -> this.addSlider("Scale", 0.5F, 2.0F, () -> FeatureModules.hlScale, v -> {
            FeatureModules.hlScale = v;
            this.dirty();
         });
         case "Chunk Borders" -> {
            this.addIntSlider("Radius (chunks)", 0, 4, () -> FeatureModules.chunkRadius, v -> {
               FeatureModules.chunkRadius = v;
               this.dirty();
            });
            this.addBoolean("Sub-chunk Lines (every 16 blocks)", () -> FeatureModules.chunkSubchunks, v -> {
               FeatureModules.chunkSubchunks = v;
               this.dirty();
            });
            this.addColorPicker("Line Color", () -> 0xFF000000 | FeatureModules.chunkColor, v -> {
               FeatureModules.chunkColor = v & 0xFFFFFF;
               this.dirty();
            });
         }
         case "Block Overlay" -> {
            this.addEnum("Mode", new String[]{"All edges", "Air-exposed only"}, () -> FeatureModules.overlayMode, v -> {
               FeatureModules.overlayMode = v;
               this.dirty();
            });
            this.addBoolean("Outline", () -> FeatureModules.overlayOutline, v -> {
               FeatureModules.overlayOutline = v;
               this.dirty();
            });
            this.addColorPicker("Outline Color", () -> 0xFF000000 | FeatureModules.overlayOutlineColor, v -> {
               FeatureModules.overlayOutlineColor = v & 0xFFFFFF;
               this.dirty();
            });
            this.addSlider("Outline Thickness", 0.5F, 8.0F, () -> FeatureModules.overlayThickness, v -> {
               FeatureModules.overlayThickness = v;
               this.dirty();
            });
            this.addBoolean("Outline Glow", () -> FeatureModules.overlayGlow, v -> {
               FeatureModules.overlayGlow = v;
               this.dirty();
            });
            this.addBoolean("Fill", () -> FeatureModules.overlayFill, v -> {
               FeatureModules.overlayFill = v;
               this.dirty();
            });
            this.addColorPicker("Fill Color", () -> 0xFF000000 | FeatureModules.overlayFillColor, v -> {
               FeatureModules.overlayFillColor = v & 0xFFFFFF;
               this.dirty();
            });
            this.addIntSlider("Fill Opacity", 0, 255, () -> FeatureModules.overlayFillAlpha, v -> {
               FeatureModules.overlayFillAlpha = v;
               this.dirty();
            });
         }
         case "Block Hit" -> this.addSlider("Tilt Amount", 0.2F, 2.0F, () -> FeatureModules.blockHitAmount, v -> {
            FeatureModules.blockHitAmount = v;
            this.dirty();
         });
         case "Crit Multiplier" -> this.addIntSlider("Crit Particle Multiplier", 1, 10, () -> FeatureModules.critMultiplier, v -> {
            FeatureModules.critMultiplier = v;
            this.dirty();
         });
         case "Hit Sounds" -> {
            this.addEnum("Sound", FeatureModules.HIT_SOUND_NAMES, () -> FeatureModules.hitSound, v -> {
               FeatureModules.hitSound = v;
               this.dirty();
            });
            this.addSlider("Volume", 0.1F, 2.0F, () -> FeatureModules.hitVolume, v -> {
               FeatureModules.hitVolume = v;
               this.dirty();
            });
            this.addSlider("Pitch", 0.5F, 2.0F, () -> FeatureModules.hitPitch, v -> {
               FeatureModules.hitPitch = v;
               this.dirty();
            });
         }
         case "Trajectories" -> {
            this.addBoolean("Bow / Crossbow", () -> FeatureModules.trajBow, v -> {
               FeatureModules.trajBow = v;
               this.dirty();
            });
            this.addBoolean("Ender Pearl", () -> FeatureModules.trajPearl, v -> {
               FeatureModules.trajPearl = v;
               this.dirty();
            });
            this.addBoolean("Trident", () -> FeatureModules.trajTrident, v -> {
               FeatureModules.trajTrident = v;
               this.dirty();
            });
            this.addBoolean("Snowballs / Eggs / Potions", () -> FeatureModules.trajThrowables, v -> {
               FeatureModules.trajThrowables = v;
               this.dirty();
            });
            this.addColorPicker("Line Color", () -> 0xFF000000 | FeatureModules.trajColor, v -> {
               FeatureModules.trajColor = v & 0xFFFFFF;
               this.dirty();
            });
         }
         case "Fog Changer" -> {
            this.addEnum("Mode", new String[]{"Vanilla", "No fog", "Custom distance"}, () -> FeatureModules.fogMode, v -> {
               FeatureModules.fogMode = v;
               this.dirty();
            });
            this.addIntSlider("Custom Distance (blocks)", 16, 512, () -> FeatureModules.fogDistance, v -> {
               FeatureModules.fogDistance = v;
               this.dirty();
            });
         }
         case "Motion Blur" -> this.addSlider("Strength", 1.0F, 10.0F, () -> FeatureModules.blurStrength, v -> {
            FeatureModules.blurStrength = v;
            this.dirty();
         });
         case "Tooltip+" -> {
            this.addBoolean("Durability %", () -> FeatureModules.tipDurability, v -> {
               FeatureModules.tipDurability = v;
               this.dirty();
            });
            this.addBoolean("Show Components (hold Ctrl)", () -> FeatureModules.tipComponents, v -> {
               FeatureModules.tipComponents = v;
               this.dirty();
            });
         }
         case "Auto Reconnect" -> {
            this.addBoolean("Reconnect Automatically", () -> FeatureModules.reconnectAuto, v -> {
               FeatureModules.reconnectAuto = v;
               this.dirty();
            });
            this.addIntSlider("Delay (seconds)", 1, 30, () -> FeatureModules.reconnectDelay, v -> {
               FeatureModules.reconnectDelay = v;
               this.dirty();
            });
         }
         case "Pixel Look" -> {
            this.addBoolean("Show Block Name", () -> FeatureModules.pixelShowBlock, v -> {
               FeatureModules.pixelShowBlock = v;
               this.dirty();
            });
            this.addBoolean("Highlight Pixel In World", () -> FeatureModules.pixelHighlight, v -> {
               FeatureModules.pixelHighlight = v;
               this.dirty();
            });
            this.addSlider("Scale", 0.5F, 2.0F, () -> FeatureModules.plScale, v -> {
               FeatureModules.plScale = v;
               this.dirty();
            });
         }
         default -> {
         }
      }
   }
}
