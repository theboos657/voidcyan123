package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.HudScale;
import com.voidcyan.client.module.ArrayListManager;
import com.voidcyan.client.module.DeathInfoManager;
import com.voidcyan.client.module.EnemyCrucialsHudRenderer;
import com.voidcyan.client.module.KeybindsHudRenderer;
import com.voidcyan.client.module.LowHealthAlarmManager;
import com.voidcyan.client.module.MouseStrokesRenderer;
import com.voidcyan.client.module.PotWarningManager;
import com.voidcyan.client.module.PotionStatusRenderer;
import com.voidcyan.client.module.ServerInfoManager;
import com.voidcyan.client.module.TargetHudRenderer;
import com.voidcyan.client.module.TextHudRenderer;
import com.voidcyan.client.module.WatermarkManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EditHudScreen extends Screen {
   private String loadedBackgroundPath = "";
   private final Screen parent;
   private final List<ClickGuiScreen.HudElement> hudElements = new ArrayList<>();
   private ClickGuiScreen.HudElement draggingElement = null;
   private ClickGuiScreen.HudElement resizingElement = null;
   private int dragOffsetX = 0;
   private int dragOffsetY = 0;
   private int resizeAnchorX = 0;
   private int resizeAnchorY = 0;
   private int resizeCorner = 3;
   private float initialScale = 1.0F;
   private int initialX = 0;
   private int initialY = 0;
   private long openTime = 0L;
   private boolean closing = false;
   private long closeStartTime = 0L;
   private static final long ANIM_MS = 180L;
   private float clickGuiBtnHover = 0.0F;
   private long lastRenderTime = 0L;

   public EditHudScreen(Screen parent) {
      super(Text.literal("Edit HUD"));
      this.parent = parent;
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Inventory",
               () -> VoidCyanClient.isInvHudEnabled,
               () -> VoidCyanClient.invHudX,
               () -> VoidCyanClient.invHudY,
               () -> VoidCyanClient.invHudScale,
               x -> VoidCyanClient.invHudX = x,
               y -> VoidCyanClient.invHudY = y,
               s -> VoidCyanClient.invHudScale = s,
               () -> 176 + VoidCyanClient.invHudBorderThickness * 2,
               () -> VoidCyanClient.invHudShowHotbar ? 84 + VoidCyanClient.invHudBorderThickness * 2 : 58 + VoidCyanClient.invHudBorderThickness * 2
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "FPS Counter",
               () -> VoidCyanClient.isFpsCounterEnabled,
               () -> VoidCyanClient.fpsCounterX,
               () -> VoidCyanClient.fpsCounterY,
               () -> VoidCyanClient.fpsCounterScale,
               x -> VoidCyanClient.fpsCounterX = x,
               y -> VoidCyanClient.fpsCounterY = y,
               s -> VoidCyanClient.fpsCounterScale = s,
               () -> 80,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "FPS Graph",
               () -> VoidCyanClient.isFpsGraphEnabled,
               () -> VoidCyanClient.fpsGraphX,
               () -> VoidCyanClient.fpsGraphY,
               () -> VoidCyanClient.fpsGraphScale,
               x -> VoidCyanClient.fpsGraphX = x,
               y -> VoidCyanClient.fpsGraphY = y,
               s -> VoidCyanClient.fpsGraphScale = s,
               () -> 175,
               () -> {
                  int h = 45;
                  if (VoidCyanClient.showFpsAvg) {
                     h += 10;
                  }

                  if (VoidCyanClient.showFpsMinMax) {
                     h += 10;
                  }

                  if (VoidCyanClient.showFps1Percent) {
                     h += 10;
                  }

                  if (VoidCyanClient.showFps01Percent) {
                     h += 10;
                  }

                  return h;
               }
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Coordinates",
               () -> VoidCyanClient.isCoordinatesEnabled,
               () -> VoidCyanClient.coordinatesX,
               () -> VoidCyanClient.coordinatesY,
               () -> VoidCyanClient.coordinatesScale,
               x -> VoidCyanClient.coordinatesX = x,
               y -> VoidCyanClient.coordinatesY = y,
               s -> VoidCyanClient.coordinatesScale = s,
               VoidCyanClient::getCoordinatesHudWidth,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Custom Text",
               () -> VoidCyanClient.isTextHudEnabled,
               () -> VoidCyanClient.textHudX,
               () -> VoidCyanClient.textHudY,
               () -> VoidCyanClient.textHudScale,
               x -> VoidCyanClient.textHudX = x,
               y -> VoidCyanClient.textHudY = y,
               s -> VoidCyanClient.textHudScale = s,
               TextHudRenderer::getWidth,
               TextHudRenderer::getHeight
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "CPS",
               () -> VoidCyanClient.isCpsEnabled,
               () -> VoidCyanClient.cpsX,
               () -> VoidCyanClient.cpsY,
               () -> VoidCyanClient.cpsScale,
               x -> VoidCyanClient.cpsX = x,
               y -> VoidCyanClient.cpsY = y,
               s -> VoidCyanClient.cpsScale = s,
               () -> 60,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Keystrokes",
               () -> VoidCyanClient.isKeystrokesEnabled,
               () -> VoidCyanClient.keystrokesX,
               () -> VoidCyanClient.keystrokesY,
               () -> VoidCyanClient.keystrokesScale,
               x -> VoidCyanClient.keystrokesX = x,
               y -> VoidCyanClient.keystrokesY = y,
               s -> VoidCyanClient.keystrokesScale = s,
               () -> {
                  switch (VoidCyanClient.keystrokesMode) {
                     case 0:
                     case 1:
                     case 2:
                     case 3:
                        return 200;
                     case 4:
                        return 75;
                     case 5:
                        return 90;
                     case 6:
                        return 150;
                     case 7:
                        return 80;
                     default:
                        return 75;
                  }
               },
               () -> {
                  switch (VoidCyanClient.keystrokesMode) {
                     case 0:
                     case 1:
                     case 2:
                     case 3:
                        return 120;
                     case 4:
                        return VoidCyanClient.keystrokesShowMouse ? 80 : 55;
                     case 5:
                        return 65;
                     case 6:
                        return 35;
                     case 7:
                        return 12;
                     default:
                        return VoidCyanClient.keystrokesShowMouse ? 80 : 55;
                  }
               }
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Compass",
               () -> VoidCyanClient.isCompassEnabled,
               () -> VoidCyanClient.compassX,
               () -> VoidCyanClient.compassY,
               () -> VoidCyanClient.compassScale,
               x -> VoidCyanClient.compassX = x,
               y -> VoidCyanClient.compassY = y,
               s -> VoidCyanClient.compassScale = s,
               () -> 200,
               () -> 30
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Armor Status",
               () -> VoidCyanClient.isArmorStatusEnabled,
               () -> VoidCyanClient.armorStatusX,
               () -> VoidCyanClient.armorStatusY,
               () -> VoidCyanClient.armorStatusScale,
               x -> VoidCyanClient.armorStatusX = x,
               y -> VoidCyanClient.armorStatusY = y,
               s -> VoidCyanClient.armorStatusScale = s,
               () -> VoidCyanClient.armorStatusOrientation == 1 ? 95 : 100,
               () -> VoidCyanClient.armorStatusOrientation == 1 ? 29 : 95
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Potion Status",
               () -> VoidCyanClient.isPotionStatusEnabled,
               () -> VoidCyanClient.potionStatusX,
               () -> VoidCyanClient.potionStatusY,
               () -> VoidCyanClient.potionStatusScale,
               x -> VoidCyanClient.potionStatusX = x,
               y -> VoidCyanClient.potionStatusY = y,
               s -> VoidCyanClient.potionStatusScale = s,
               () -> PotionStatusRenderer.getWidth(
                  MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getStatusEffects().size() : 1
               ),
               () -> PotionStatusRenderer.getHeight(
                  MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getStatusEffects().size() : 1
               )
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Target HUD",
               () -> VoidCyanClient.isTargetHudEnabled,
               () -> VoidCyanClient.targetHudX,
               () -> VoidCyanClient.targetHudY,
               () -> VoidCyanClient.targetHudScale,
               x -> VoidCyanClient.targetHudX = x,
               y -> VoidCyanClient.targetHudY = y,
               s -> VoidCyanClient.targetHudScale = s,
               () -> 160,
               TargetHudRenderer::getHudHeight
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Enemy Info",
               () -> VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudShowEnemyCrucials,
               () -> VoidCyanClient.enemyCrucialsHudX,
               () -> VoidCyanClient.enemyCrucialsHudY,
               () -> VoidCyanClient.enemyCrucialsHudScale,
               x -> VoidCyanClient.enemyCrucialsHudX = x,
               y -> VoidCyanClient.enemyCrucialsHudY = y,
               s -> VoidCyanClient.enemyCrucialsHudScale = s,
               () -> 126,
               EnemyCrucialsHudRenderer::getHudHeight
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "MMO Armor Display",
               () -> VoidCyanClient.isTargetHudEnabled && VoidCyanClient.targetHudMmoArmorDisplay,
               () -> VoidCyanClient.targetHudMmoArmorX,
               () -> VoidCyanClient.targetHudMmoArmorY,
               () -> VoidCyanClient.targetHudMmoArmorScale,
               x -> VoidCyanClient.targetHudMmoArmorX = x,
               y -> VoidCyanClient.targetHudMmoArmorY = y,
               s -> VoidCyanClient.targetHudMmoArmorScale = s,
               () -> 62,
               () -> 126
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Stats HUD",
               () -> VoidCyanClient.isStatsHudEnabled,
               () -> VoidCyanClient.statsHudX,
               () -> VoidCyanClient.statsHudY,
               () -> VoidCyanClient.statsHudScale,
               x -> VoidCyanClient.statsHudX = x,
               y -> VoidCyanClient.statsHudY = y,
               s -> VoidCyanClient.statsHudScale = s,
               () -> VoidCyanClient.getStatsHudWidth(),
               () -> VoidCyanClient.getStatsHudHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Combo Counter",
               () -> VoidCyanClient.isComboCounterEnabled,
               () -> VoidCyanClient.comboCounterX,
               () -> VoidCyanClient.comboCounterY,
               () -> VoidCyanClient.comboCounterScale,
               x -> VoidCyanClient.comboCounterX = x,
               y -> VoidCyanClient.comboCounterY = y,
               s -> VoidCyanClient.comboCounterScale = s,
               () -> 80,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Ping Display",
               () -> VoidCyanClient.isPingDisplayEnabled,
               () -> VoidCyanClient.pingDisplayX,
               () -> VoidCyanClient.pingDisplayY,
               () -> VoidCyanClient.pingDisplayScale,
               x -> VoidCyanClient.pingDisplayX = x,
               y -> VoidCyanClient.pingDisplayY = y,
               s -> VoidCyanClient.pingDisplayScale = s,
               () -> 60,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Day Counter",
               () -> com.voidcyan.client.FeatureModules.on[1],
               () -> com.voidcyan.client.FeatureModules.dayX,
               () -> com.voidcyan.client.FeatureModules.dayY,
               () -> com.voidcyan.client.FeatureModules.dayScale,
               x -> com.voidcyan.client.FeatureModules.dayX = x,
               y -> com.voidcyan.client.FeatureModules.dayY = y,
               s -> com.voidcyan.client.FeatureModules.dayScale = s,
               () -> com.voidcyan.client.FeatureModules.textBoxWidth(com.voidcyan.client.FeatureModules.dayText()),
               () -> 18
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Direction HUD",
               () -> com.voidcyan.client.FeatureModules.on[2],
               () -> com.voidcyan.client.FeatureModules.dirX,
               () -> com.voidcyan.client.FeatureModules.dirY,
               () -> com.voidcyan.client.FeatureModules.dirScale,
               x -> com.voidcyan.client.FeatureModules.dirX = x,
               y -> com.voidcyan.client.FeatureModules.dirY = y,
               s -> com.voidcyan.client.FeatureModules.dirScale = s,
               () -> com.voidcyan.client.FeatureModules.textBoxWidth(com.voidcyan.client.FeatureModules.dirText()),
               () -> 18
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Height Limit",
               () -> com.voidcyan.client.FeatureModules.on[3],
               () -> com.voidcyan.client.FeatureModules.hlX,
               () -> com.voidcyan.client.FeatureModules.hlY,
               () -> com.voidcyan.client.FeatureModules.hlScale,
               x -> com.voidcyan.client.FeatureModules.hlX = x,
               y -> com.voidcyan.client.FeatureModules.hlY = y,
               s -> com.voidcyan.client.FeatureModules.hlScale = s,
               () -> com.voidcyan.client.FeatureModules.textBoxWidth(com.voidcyan.client.FeatureModules.heightText()),
               () -> 18
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Pixel Look",
               () -> com.voidcyan.client.FeatureModules.on[16],
               () -> com.voidcyan.client.FeatureModules.plX,
               () -> com.voidcyan.client.FeatureModules.plY,
               () -> com.voidcyan.client.FeatureModules.plScale,
               x -> com.voidcyan.client.FeatureModules.plX = x,
               y -> com.voidcyan.client.FeatureModules.plY = y,
               s -> com.voidcyan.client.FeatureModules.plScale = s,
               () -> com.voidcyan.client.FeatureModules.textBoxWidth(com.voidcyan.client.FeatureModules.pixelText()),
               () -> 18
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Action Bar",
               () -> VoidCyanClient.isActionBarEnabled,
               () -> VoidCyanClient.actionBarX,
               () -> VoidCyanClient.actionBarY,
               () -> VoidCyanClient.actionBarScale,
               x -> VoidCyanClient.actionBarX = x,
               y -> VoidCyanClient.actionBarY = y,
               s -> VoidCyanClient.actionBarScale = s,
               () -> VoidCyanClient.actionBarWidth,
               () -> 12
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Scoreboard",
               () -> VoidCyanClient.isScoreboardEnabled,
               () -> Math.round(((VoidCyanClient.sbKnown ? VoidCyanClient.sbMinX : MinecraftClient.getInstance().getWindow().getScaledWidth() - 103) + VoidCyanClient.scoreboardOffsetX) / HudScale.k()),
               () -> Math.round(((VoidCyanClient.sbKnown ? VoidCyanClient.sbMinY : MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 45) + VoidCyanClient.scoreboardOffsetY) / HudScale.k()),
               () -> 1.0F,
               x -> VoidCyanClient.scoreboardOffsetX = Math.round(x * HudScale.k()) - (VoidCyanClient.sbKnown ? VoidCyanClient.sbMinX : MinecraftClient.getInstance().getWindow().getScaledWidth() - 103),
               y -> VoidCyanClient.scoreboardOffsetY = Math.round(y * HudScale.k()) - (VoidCyanClient.sbKnown ? VoidCyanClient.sbMinY : MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 45),
               s -> {},
               () -> Math.round((VoidCyanClient.sbKnown ? VoidCyanClient.sbMaxX - VoidCyanClient.sbMinX : 100) / HudScale.k()),
               () -> Math.round((VoidCyanClient.sbKnown ? VoidCyanClient.sbMaxY - VoidCyanClient.sbMinY : 90) / HudScale.k())
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Keybinds Display",
               () -> VoidCyanClient.isKeybindsDisplayEnabled,
               () -> VoidCyanClient.keybindsHudX,
               () -> VoidCyanClient.keybindsHudY,
               () -> VoidCyanClient.keybindsHudScale,
               x -> VoidCyanClient.keybindsHudX = x,
               y -> VoidCyanClient.keybindsHudY = y,
               s -> VoidCyanClient.keybindsHudScale = s,
               KeybindsHudRenderer::getHudWidth,
               KeybindsHudRenderer::getHudHeight
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Server Info",
               () -> VoidCyanClient.isServerInfoEnabled,
               () -> VoidCyanClient.serverInfoX,
               () -> VoidCyanClient.serverInfoY,
               () -> VoidCyanClient.serverInfoScale,
               x -> VoidCyanClient.serverInfoX = x,
               y -> VoidCyanClient.serverInfoY = y,
               s -> VoidCyanClient.serverInfoScale = s,
               () -> ServerInfoManager.getWidth(MinecraftClient.getInstance()),
               () -> ServerInfoManager.getHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Watermark",
               () -> VoidCyanClient.isWatermarkEnabled,
               () -> VoidCyanClient.watermarkX,
               () -> VoidCyanClient.watermarkY,
               () -> VoidCyanClient.watermarkScale,
               x -> VoidCyanClient.watermarkX = x,
               y -> VoidCyanClient.watermarkY = y,
               s -> VoidCyanClient.watermarkScale = s,
               () -> WatermarkManager.getWidth(MinecraftClient.getInstance()),
               () -> 12
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Array List",
               () -> VoidCyanClient.isArrayListEnabled,
               () -> VoidCyanClient.arrayListX,
               () -> VoidCyanClient.arrayListY,
               () -> VoidCyanClient.arrayListScale,
               x -> VoidCyanClient.arrayListX = x,
               y -> VoidCyanClient.arrayListY = y,
               s -> VoidCyanClient.arrayListScale = s,
               () -> ArrayListManager.getWidth(MinecraftClient.getInstance()),
               () -> ArrayListManager.getHeight(MinecraftClient.getInstance())
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Low Health",
               () -> VoidCyanClient.isLowHealthAlarmEnabled,
               () -> VoidCyanClient.lowHealthAlarmX,
               () -> VoidCyanClient.lowHealthAlarmY,
               () -> VoidCyanClient.lowHealthAlarmScale,
               x -> VoidCyanClient.lowHealthAlarmX = x,
               y -> VoidCyanClient.lowHealthAlarmY = y,
               s -> VoidCyanClient.lowHealthAlarmScale = s,
               () -> LowHealthAlarmManager.getWidth(MinecraftClient.getInstance()),
               () -> LowHealthAlarmManager.getHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Death Info",
               () -> VoidCyanClient.isDeathInfoEnabled,
               () -> VoidCyanClient.deathInfoX,
               () -> VoidCyanClient.deathInfoY,
               () -> VoidCyanClient.deathInfoScale,
               x -> VoidCyanClient.deathInfoX = x,
               y -> VoidCyanClient.deathInfoY = y,
               s -> VoidCyanClient.deathInfoScale = s,
               () -> DeathInfoManager.getWidth(MinecraftClient.getInstance()),
               () -> DeathInfoManager.getHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Pot Warning",
               () -> VoidCyanClient.isPotWarningEnabled,
               () -> VoidCyanClient.potWarningX,
               () -> VoidCyanClient.potWarningY,
               () -> VoidCyanClient.potWarningScale,
               x -> VoidCyanClient.potWarningX = x,
               y -> VoidCyanClient.potWarningY = y,
               s -> VoidCyanClient.potWarningScale = s,
               () -> PotWarningManager.getWidth(MinecraftClient.getInstance()),
               () -> PotWarningManager.getHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "TPS Display",
               () -> VoidCyanClient.isTpsDisplayEnabled,
               () -> VoidCyanClient.tpsDisplayX,
               () -> VoidCyanClient.tpsDisplayY,
               () -> VoidCyanClient.tpsDisplayScale,
               x -> VoidCyanClient.tpsDisplayX = x,
               y -> VoidCyanClient.tpsDisplayY = y,
               s -> VoidCyanClient.tpsDisplayScale = s,
               () -> 60,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "IRL Clock",
               () -> VoidCyanClient.isIrlClockEnabled,
               () -> VoidCyanClient.irlClockX,
               () -> VoidCyanClient.irlClockY,
               () -> VoidCyanClient.irlClockScale,
               x -> VoidCyanClient.irlClockX = x,
               y -> VoidCyanClient.irlClockY = y,
               s -> VoidCyanClient.irlClockScale = s,
               () -> {
                  String text = VoidCyanClient.irlClockText();

                  return MinecraftClient.getInstance().textRenderer.getWidth(text) + 10;
               },
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Notifications",
               () -> VoidCyanClient.isNotificationsEnabled,
               () -> VoidCyanClient.notificationsX,
               () -> VoidCyanClient.notificationsY,
               () -> VoidCyanClient.notificationsScale,
               x -> VoidCyanClient.notificationsX = x,
               y -> VoidCyanClient.notificationsY = y,
               s -> VoidCyanClient.notificationsScale = s,
               () -> 160,
               () -> 30
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Biome Display",
               () -> VoidCyanClient.isBiomeDisplayEnabled,
               () -> VoidCyanClient.biomeDisplayX,
               () -> VoidCyanClient.biomeDisplayY,
               () -> VoidCyanClient.biomeDisplayScale,
               x -> VoidCyanClient.biomeDisplayX = x,
               y -> VoidCyanClient.biomeDisplayY = y,
               s -> VoidCyanClient.biomeDisplayScale = s,
               () -> 100,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Entity Counter",
               () -> VoidCyanClient.isEntityCounterEnabled,
               () -> VoidCyanClient.entityCounterX,
               () -> VoidCyanClient.entityCounterY,
               () -> VoidCyanClient.entityCounterScale,
               x -> VoidCyanClient.entityCounterX = x,
               y -> VoidCyanClient.entityCounterY = y,
               s -> VoidCyanClient.entityCounterScale = s,
               () -> VoidCyanClient.getEntityCounterWidth(),
               () -> VoidCyanClient.getEntityCounterHeight()
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Arrow Counter",
               () -> VoidCyanClient.isArrowCounterEnabled,
               () -> VoidCyanClient.arrowCounterX,
               () -> VoidCyanClient.arrowCounterY,
               () -> VoidCyanClient.arrowCounterScale,
               x -> VoidCyanClient.arrowCounterX = x,
               y -> VoidCyanClient.arrowCounterY = y,
               s -> VoidCyanClient.arrowCounterScale = s,
               () -> 80,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Speed Display",
               () -> VoidCyanClient.isSpeedDisplayEnabled,
               () -> VoidCyanClient.speedDisplayX,
               () -> VoidCyanClient.speedDisplayY,
               () -> VoidCyanClient.speedDisplayScale,
               x -> VoidCyanClient.speedDisplayX = x,
               y -> VoidCyanClient.speedDisplayY = y,
               s -> VoidCyanClient.speedDisplayScale = s,
               () -> 80,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Reach Display",
               () -> VoidCyanClient.isReachDisplayEnabled,
               () -> VoidCyanClient.reachDisplayX,
               () -> VoidCyanClient.reachDisplayY,
               () -> VoidCyanClient.reachDisplayScale,
               x -> VoidCyanClient.reachDisplayX = x,
               y -> VoidCyanClient.reachDisplayY = y,
               s -> VoidCyanClient.reachDisplayScale = s,
               () -> 80,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Totem Counter",
               () -> VoidCyanClient.isTotemCounterEnabled,
               () -> VoidCyanClient.totemCounterX,
               () -> VoidCyanClient.totemCounterY,
               () -> VoidCyanClient.totemCounterScale,
               x -> VoidCyanClient.totemCounterX = x,
               y -> VoidCyanClient.totemCounterY = y,
               s -> VoidCyanClient.totemCounterScale = s,
               () -> 60,
               () -> 20
            )
         );
      this.hudElements
         .add(
            new ClickGuiScreen.HudElement(
               "Mouse Strokes",
               () -> VoidCyanClient.isMouseStrokesEnabled,
               () -> VoidCyanClient.mouseStrokesX,
               () -> VoidCyanClient.mouseStrokesY,
               () -> VoidCyanClient.mouseStrokesScale,
               x -> VoidCyanClient.mouseStrokesX = x,
               y -> VoidCyanClient.mouseStrokesY = y,
               s -> VoidCyanClient.mouseStrokesScale = s,
               () -> 60,
               () -> 60
            )
         );
   }

   protected void init() {
      super.init();
      // Work in the GUI-scale-independent HUD space (same as the in-game HUD), not vanilla scaled pixels.
      this.width = HudScale.logicalWidth();
      this.height = HudScale.logicalHeight();
      this.openTime = System.currentTimeMillis();
      this.closing = false;

      for (ClickGuiScreen.HudElement el : this.hudElements) {
         int w = (int)(el.getWidth.get().intValue() * el.getScale.get());
         int h = (int)(el.getHeight.get().intValue() * el.getScale.get());
         int x = el.getX.get();
         int y = el.getY.get();
         int newX = Math.max(0, Math.min(x, this.width - w));
         int newY = Math.max(0, Math.min(y, this.height - h));
         if (newX != x) {
            el.setX.accept(newX);
         }

         if (newY != y) {
            el.setY.accept(newY);
         }
      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (VoidCyanClient.editGuiBgEnabled && !VoidCyanClient.guiBackgroundImagePath.isEmpty()) {
         if (!VoidCyanClient.guiBackgroundImagePath.equals(this.loadedBackgroundPath)) {
            VoidCyanClient.updateMainBackground();
            this.loadedBackgroundPath = VoidCyanClient.guiBackgroundImagePath;
         }

         Identifier bgId;
         if (VoidCyanClient.mainBackground != null && (bgId = VoidCyanClient.mainBackground.updateAndGetId()) != null) {
            int opacityInt = VoidCyanClient.guiBgOpacity;
            int color = opacityInt << 24 | 16777215;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, bgId, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height, color);
            return;
         }
      }
   }

   private float getAnimProgress() {
      if (this.closing) {
         float p = (float)(System.currentTimeMillis() - this.closeStartTime) / 180.0F;
         return 1.0F - Math.min(1.0F, p);
      } else {
         float p = (float)(System.currentTimeMillis() - this.openTime) / 180.0F;
         return Math.min(1.0F, p);
      }
   }

   private float easeOut(float t) {
      return 1.0F - (float)Math.pow(1.0F - t, 2.5);
   }

   private float easeIn(float t) {
      return (float)Math.pow(t, 2.0);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.client != null) {
         float var10000 = this.client.getWindow().getScaleFactor();
      } else {
         float var67 = 1.0F;
      }

      float targetScale = 3.0F;
      float scaleRatio = HudScale.k();
      context.getMatrices().pushMatrix();
      context.getMatrices().scale(scaleRatio, scaleRatio);
      int scaledMouseX = (int)(mouseX / scaleRatio);
      int scaledMouseY = (int)(mouseY / scaleRatio);
      long now = System.currentTimeMillis();
      if (this.lastRenderTime == 0L) {
         long var68 = 16L;
      } else {
         long var69 = now - this.lastRenderTime;
      }

      this.lastRenderTime = now;
      super.render(context, scaledMouseX, scaledMouseY, deltaTicks);
      float rawProgress = this.getAnimProgress();
      float progress = this.closing ? this.easeIn(rawProgress) : this.easeOut(rawProgress);
      if (this.closing && rawProgress <= 0.0F) {
         context.getMatrices().popMatrix();
         this.client.setScreen(this.parent);
         VoidCyanClient.saveConfig();
      } else {
         float scale = 0.88F + 0.12F * progress;
         int alpha = (int)(255.0F * progress);
         context.getMatrices().pushMatrix();
         context.getMatrices().translate(this.width / 2.0F, this.height / 2.0F);
         context.getMatrices().scale(scale, scale);
         context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F);

         for (ClickGuiScreen.HudElement el : this.hudElements) {
            boolean enabled = el.isEnabled.get();
            if (enabled) {
               int hX = el.getX.get();
               int hY = el.getY.get();
               float hScale = el.getScale.get();
               int hWidth = el.getWidth.get();
               int hHeight = el.getHeight.get();
               int scaledWidth = Math.max(10, (int)(hWidth * hScale));
               int scaledHeight = Math.max(10, (int)(hHeight * hScale));
               int borderAlpha = (int)(255.0F * progress);
               int fillAlpha = (int)(64.0F * progress);
               int borderColor = borderAlpha << 24 | VoidCyanClient.getPrimaryColor() & 16777215;
               context.fill(hX, hY, hX + scaledWidth, hY + 1, borderColor);
               context.fill(hX, hY + scaledHeight - 1, hX + scaledWidth, hY + scaledHeight, borderColor);
               context.fill(hX, hY, hX + 1, hY + scaledHeight, borderColor);
               context.fill(hX + scaledWidth - 1, hY, hX + scaledWidth, hY + scaledHeight, borderColor);
               context.fill(hX, hY, hX + scaledWidth, hY + scaledHeight, fillAlpha << 24 | VoidCyanClient.getPrimaryColor() & 16777215);
               if (el.name.equals("Mouse Strokes") && enabled) {
                  MouseStrokesRenderer.renderAt(context, hX, hY, scaledMouseX, scaledMouseY, this.width, this.height);
               }

               int dotSize = 4;
               int dotAlpha = (int)((enabled ? 255 : 68) * progress);
               context.fill(hX - dotSize, hY - dotSize, hX + dotSize, hY + dotSize, dotAlpha << 24 | 16777215);
               context.fill(hX + scaledWidth - dotSize, hY - dotSize, hX + scaledWidth + dotSize, hY + dotSize, dotAlpha << 24 | 16777215);
               context.fill(hX - dotSize, hY + scaledHeight - dotSize, hX + dotSize, hY + scaledHeight + dotSize, dotAlpha << 24 | 16777215);
               context.fill(
                  hX + scaledWidth - dotSize, hY + scaledHeight - dotSize, hX + scaledWidth + dotSize, hY + scaledHeight + dotSize, dotAlpha << 24 | 16777215
               );
               int textAlpha = (int)((enabled ? 255 : 85) * progress);
               Object label = enabled ? el.name : el.name + " (off)";
               if (el.name.equals("IRL Clock") && enabled) {
                  String text = VoidCyanClient.irlClockText();

                  label = text;
               }

               int labelColor = textAlpha << 24 | VoidCyanClient.getPrimaryColor() & 16777215;
               context.drawTextWithShadow(
                  this.textRenderer,
                  Text.literal((String)label),
                  hX + scaledWidth / 2 - this.textRenderer.getWidth((String)label) / 2,
                  hY + scaledHeight / 2 - 4,
                  labelColor
               );
            }
         }

         int btnW = 140;
         int btnH = 36;
         int btnX = this.width / 2 - btnW / 2;
         int btnY = this.height / 2 - btnH / 2;
         boolean btnHovered = scaledMouseX >= btnX && scaledMouseX <= btnX + btnW && scaledMouseY >= btnY && scaledMouseY <= btnY + btnH;
         float hoverTarget = btnHovered ? 1.0F : 0.0F;
         float hoverSpeed = 0.3F;
         this.clickGuiBtnHover = this.clickGuiBtnHover + (hoverTarget - this.clickGuiBtnHover) * hoverSpeed;
         if (this.clickGuiBtnHover > 0.01F) {
            int glowAlpha = (int)(48.0F * this.clickGuiBtnHover * progress);
            int glowColor = glowAlpha << 24 | VoidCyanClient.getPrimaryColor() & 16777215;
            context.fill(btnX - 4, btnY - 4, btnX + btnW + 4, btnY + btnH + 4, glowColor);
         }

         int bodyAlpha = (int)((96.0F + 48.0F * this.clickGuiBtnHover) * progress);
         context.fill(btnX, btnY, btnX + btnW, btnY + btnH, bodyAlpha << 24 | 0);
         int borderA = (int)(255.0F * progress);
         int borderColorx = borderA << 24 | VoidCyanClient.getPrimaryColor() & 16777215;
         context.fill(btnX, btnY, btnX + btnW, btnY + 1, borderColorx);
         context.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, borderColorx);
         context.fill(btnX, btnY, btnX + 1, btnY + btnH, borderColorx);
         context.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, borderColorx);
         context.fill(btnX, btnY, btnX + 3, btnY + btnH, borderColorx);
         String btnText = "Click GUI";
         int txtColor = (int)((170 + (int)(85.0F * this.clickGuiBtnHover)) * progress);
         context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(btnText),
            btnX + btnW / 2 - this.textRenderer.getWidth(btnText) / 2,
            btnY + btnH / 2 - 4,
            0xFF000000 | txtColor << 16 | 0xFF00 | 0xFF
         );
         String subLabel = "Edit Modules & Settings";
         int subAlpha = (int)(136.0F * progress);
         context.drawTextWithShadow(
            this.textRenderer, Text.literal(subLabel), btnX + btnW / 2 - this.textRenderer.getWidth(subLabel) / 2, btnY + btnH + 6, subAlpha << 24 | 11184810
         );
         String hint1 = "Drag elements to reposition  •  Corner dots to resize";
         int hintAlpha = (int)(187.0F * progress);
         int hintColor = hintAlpha << 24 | VoidCyanClient.getPrimaryColor() & 16777215;
         context.drawTextWithShadow(this.textRenderer, Text.literal(hint1), this.width / 2 - this.textRenderer.getWidth(hint1) / 2, 16, hintColor);
         String hint2 = "Press ESC to close";
         context.drawTextWithShadow(
            this.textRenderer, Text.literal(hint2), this.width / 2 - this.textRenderer.getWidth(hint2) / 2, 28, (int)(136.0F * progress) << 24 | 11184810
         );
         int resetBtnW = 120;
         int resetBtnH = 20;
         int resetBtnX = this.width / 2 - resetBtnW / 2;
         int resetBtnY = this.height - 30;
         boolean resetHovered = scaledMouseX >= resetBtnX && scaledMouseX <= resetBtnX + resetBtnW && scaledMouseY >= resetBtnY && scaledMouseY <= resetBtnY + resetBtnH;
         context.fill(resetBtnX, resetBtnY, resetBtnX + resetBtnW, resetBtnY + resetBtnH, resetHovered ? -1426107051 : 1627346261);
         context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Reset All Positions"),
            resetBtnX + resetBtnW / 2 - this.textRenderer.getWidth("Reset All Positions") / 2,
            resetBtnY + 6,
            -1
         );
         context.getMatrices().popMatrix();
         context.getMatrices().popMatrix();
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      if (this.client != null) {
         float var10000 = this.client.getWindow().getScaleFactor();
      } else {
         float var39 = 1.0F;
      }

      float targetScale = 3.0F;
      float scaleRatio = HudScale.k();
      double mouseX = click.x() / scaleRatio;
      double mouseY = click.y() / scaleRatio;
      int btnW = 140;
      int btnH = 36;
      int btnX = this.width / 2 - btnW / 2;
      int btnY = this.height / 2 - btnH / 2;
      if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
         VoidCyanClient.openMainGui(this.client, this);

         return true;
      } else {
         for (int i = this.hudElements.size() - 1; i >= 0; i--) {
            ClickGuiScreen.HudElement el = this.hudElements.get(i);
            if (el.isEnabled.get()) {
               int hX = el.getX.get();
               int hY = el.getY.get();
               float hScale = el.getScale.get();
               int scaledWidth = (int)(el.getWidth.get().intValue() * hScale);
               int scaledHeight = (int)(el.getHeight.get().intValue() * hScale);
               int dotSize = 6;
               if (this.isHoveringAnyDot(mouseX, mouseY, hX, hY, scaledWidth, scaledHeight, dotSize)) {
                  this.resizingElement = el;
                  this.resizeAnchorX = (int)mouseX;
                  this.resizeAnchorY = (int)mouseY;
                  this.initialScale = hScale;
                  this.initialX = hX;
                  this.initialY = hY;
                  int cornerX = hX + scaledWidth;
                  int cornerY = hY + scaledHeight;
                  boolean isLeftSide = mouseX <= hX + dotSize;
                  boolean isRightSide = mouseX >= cornerX - dotSize;
                  boolean isTopSide = mouseY <= hY + dotSize;
                  boolean isBottomSide = mouseY >= cornerY - dotSize;
                  if (isLeftSide && isTopSide) {
                     this.resizeCorner = 0;
                  } else if (isRightSide && isTopSide) {
                     this.resizeCorner = 1;
                  } else if (isLeftSide && isBottomSide) {
                     this.resizeCorner = 2;
                  } else {
                     this.resizeCorner = 3;
                  }

                  return true;
               }
            }
         }

         for (int var32 = this.hudElements.size() - 1; var32 >= 0; var32--) {
            ClickGuiScreen.HudElement el = this.hudElements.get(var32);
            if (el.isEnabled.get()) {
               int hX = el.getX.get();
               int hY = el.getY.get();
               float hScale = el.getScale.get();
               int hWidth = (int)(el.getWidth.get().intValue() * hScale);
               int hHeight = (int)(el.getHeight.get().intValue() * hScale);
               if (mouseX >= hX && mouseX <= hX + hWidth && mouseY >= hY && mouseY <= hY + hHeight) {
                  this.draggingElement = el;
                  this.dragOffsetX = (int)(mouseX - hX);
                  this.dragOffsetY = (int)(mouseY - hY);
                  return true;
               }
            }
         }

         int resetBtnW = 120;
         int resetBtnH = 20;
         int resetBtnX = this.width / 2 - resetBtnW / 2;
         int resetBtnY = this.height - 30;
         if (mouseX >= resetBtnX && mouseX <= resetBtnX + resetBtnW && mouseY >= resetBtnY && mouseY <= resetBtnY + resetBtnH) {
            VoidCyanClient.resetAllHudPositions(this.client);
            return true;
         } else {
            return super.mouseClicked(click, doubled);
         }
      }
   }

   private boolean isHoveringAnyDot(double mouseX, double mouseY, int hX, int hY, int w, int h, int dotSize) {
      boolean tl = mouseX >= hX - dotSize && mouseX <= hX + dotSize && mouseY >= hY - dotSize && mouseY <= hY + dotSize;
      boolean tr = mouseX >= hX + w - dotSize && mouseX <= hX + w + dotSize && mouseY >= hY - dotSize && mouseY <= hY + dotSize;
      boolean bl = mouseX >= hX - dotSize && mouseX <= hX + dotSize && mouseY >= hY + h - dotSize && mouseY <= hY + h + dotSize;
      boolean br = mouseX >= hX + w - dotSize && mouseX <= hX + w + dotSize && mouseY >= hY + h - dotSize && mouseY <= hY + h + dotSize;
      return tl || tr || bl || br;
   }

   public boolean mouseReleased(Click click) {
      this.draggingElement = null;
      this.resizingElement = null;
      VoidCyanClient.saveConfig();
      return super.mouseReleased(click);
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      double mouseX = click.x() / HudScale.k();
      double mouseY = click.y() / HudScale.k();
      if (this.draggingElement != null) {
         int newX = (int)(mouseX - this.dragOffsetX);
         int newY = (int)(mouseY - this.dragOffsetY);
         int w = (int)(this.draggingElement.getWidth.get().intValue() * this.draggingElement.getScale.get());
         int h = (int)(this.draggingElement.getHeight.get().intValue() * this.draggingElement.getScale.get());
         newX = Math.max(0, Math.min(newX, this.width - w));
         newY = Math.max(0, Math.min(newY, this.height - h));
         this.draggingElement.setX.accept(newX);
         this.draggingElement.setY.accept(newY);
         return true;
      } else if (this.resizingElement != null) {
         float dx = (float)(mouseX - this.resizeAnchorX);
         float dy = (float)(mouseY - this.resizeAnchorY);
         // Factor signs depend on which corner: dragging outward from each corner should increase scale
         float factorX, factorY;
         if (this.resizeCorner == 0) {         // Top-Left: drag left/up expands
            factorX = -dx; factorY = -dy;
         } else if (this.resizeCorner == 1) {  // Top-Right: drag right/up expands
            factorX =  dx; factorY = -dy;
         } else if (this.resizeCorner == 2) {  // Bottom-Left: drag left/down expands
            factorX = -dx; factorY =  dy;
         } else {                               // Bottom-Right: drag right/down expands
            factorX =  dx; factorY =  dy;
         }
         float d = Math.abs(factorX) > Math.abs(factorY) ? factorX : factorY;
         int w = this.resizingElement.getWidth.get();
         int h = this.resizingElement.getHeight.get();
         float newScale = Math.max(0.3F, Math.min(3.0F, this.initialScale + d / Math.max(w, h)));
         this.resizingElement.setScale.accept(newScale);
         float scaleDiff = newScale - this.initialScale;
         int newX = this.initialX;
         int newY = this.initialY;
         if (this.resizeCorner == 0) {         // Top-Left: bottom-right corner is fixed
            newX = this.initialX - (int)(w * scaleDiff);
            newY = this.initialY - (int)(h * scaleDiff);
         } else if (this.resizeCorner == 1) {  // Top-Right: bottom-left corner is fixed
            newY = this.initialY - (int)(h * scaleDiff);
         } else if (this.resizeCorner == 2) {  // Bottom-Left: top-right corner is fixed
            newX = this.initialX - (int)(w * scaleDiff);
         }
         // Corner 3 (Bottom-Right): top-left is fixed, newX/newY stay as initialX/initialY

         this.resizingElement.setX.accept(newX);
         this.resizingElement.setY.accept(newY);
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public void close() {
      if (!this.closing) {
         this.closing = true;
         this.closeStartTime = System.currentTimeMillis();
      }
   }
}
