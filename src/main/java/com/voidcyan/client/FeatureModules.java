package com.voidcyan.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Batch of newer modules: Bossbar, Day Counter, Direction HUD, Height Limit, Chunk Borders, Block Overlay,
 * Block Hit (1.7), Crit Multiplier, Hit Sounds, Trajectories, Fog Changer, Motion Blur, Menu Blur,
 * Tooltip+, Auto Sprint and Auto Reconnect. World-space drawing lives in {@link FeatureWorld}.
 */
public final class FeatureModules {
   private FeatureModules() {
   }

   public static final String[] NAMES = {
      "Bossbar", "Day Counter", "Direction HUD", "Height Limit", "Chunk Borders", "Block Overlay", "Block Hit",
      "Crit Multiplier", "Hit Sounds", "Trajectories", "Fog Changer", "Motion Blur", "Menu Blur", "Tooltip+",
      "Auto Sprint", "Auto Reconnect", "Pixel Look"
   };
   public static final int BOSS = 0, DAY = 1, DIR = 2, HEIGHT = 3, CHUNK = 4, OVERLAY = 5, BLOCKHIT = 6, CRIT = 7, HITSND = 8,
      TRAJ = 9, FOG = 10, MBLUR = 11, MENUBLUR = 12, TOOLTIP = 13, SPRINT = 14, RECON = 15, PIXEL = 16;

   public static final boolean[] on = new boolean[NAMES.length];

   static {
      on[MENUBLUR] = true;
   }

   public static int indexOf(String name) {
      for (int i = 0; i < NAMES.length; i++) if (NAMES[i].equals(name)) return i;
      return -1;
   }

   public static boolean enabled(int i) {
      return on[i];
   }

   /** Toggles by module name; returns true when the name belongs to this batch. */
   public static boolean toggle(String name) {
      int i = indexOf(name);
      if (i < 0) return false;
      on[i] = !on[i];
      VoidCyanClient.markConfigDirty();
      return true;
   }

   // ---- Bossbar
   public static boolean bossHide = false;
   public static float bossScale = 1.0F;
   public static int bossOffsetX = 0;
   public static int bossOffsetY = 0;

   // ---- HUD text modules
   public static boolean dayShowClock = true;
   public static boolean dayClock24h = true;
   public static int dayX = 10, dayY = 60;
   public static float dayScale = 1.0F;
   public static boolean dirShowAngles = true;
   public static int dirX = 10, dirY = 84;
   public static float dirScale = 1.0F;
   public static int hlX = 10, hlY = 108;
   public static float hlScale = 1.0F;

   // ---- Pixel look
   public static boolean pixelShowBlock = true;
   public static boolean pixelHighlight = true;
   public static int plX = 10, plY = 132;
   public static float plScale = 1.0F;

   /** Returns {pixelU, pixelV, faceIndex} for the block face under the crosshair, or null. */
   public static int[] pixelTarget() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || !(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit) || hit.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) return null;
      var d = hit.getSide();
      var local = hit.getPos().subtract(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
      double u;
      double v;
      switch (d) {
         case UP, DOWN -> {
            u = local.x;
            v = local.z;
         }
         case NORTH, SOUTH -> {
            u = local.x;
            v = 1.0 - local.y;
         }
         default -> {
            u = local.z;
            v = 1.0 - local.y;
         }
      }

      int pu = Math.max(0, Math.min(15, (int) Math.floor(u * 16.0)));
      int pv = Math.max(0, Math.min(15, (int) Math.floor(v * 16.0)));
      return new int[]{pu, pv, d.ordinal()};
   }

   public static String pixelText() {
      int[] t = pixelTarget();
      if (t == null) return "Pixel: not looking at a block";
      MinecraftClient mc = MinecraftClient.getInstance();
      var hit = (net.minecraft.util.hit.BlockHitResult) mc.crosshairTarget;
      String face = net.minecraft.util.math.Direction.values()[t[2]].asString();
      String s = "Pixel: " + t[0] + ", " + t[1] + "  (" + face + ")";
      if (pixelShowBlock) s += "  " + mc.world.getBlockState(hit.getBlockPos()).getBlock().getName().getString();
      return s;
   }

   // ---- Chunk borders
   public static int chunkRadius = 1;
   public static boolean chunkSubchunks = true;
   public static int chunkColor = 0x00F5FF;

   // ---- Block overlay
   /** 0 = all edges, 1 = only air-exposed edges/faces. */
   public static int overlayMode = 1;
   public static boolean overlayOutline = true;
   public static boolean overlayFill = true;
   public static int overlayOutlineColor = 0xFFFFFF;
   public static int overlayFillColor = 0xFFFFFF;
   public static int overlayFillAlpha = 40;
   public static float overlayThickness = 2.0F;
   public static boolean overlayGlow = true;

   // ---- Block hit
   public static float blockHitAmount = 1.0F;

   // ---- Crit multiplier / hit sounds
   public static int critMultiplier = 3;
   public static final String[] HIT_SOUND_NAMES = {"Pling", "XP Orb", "Chime", "Level Up", "Arrow Hit", "Crit", "Bell"};
   public static final String[] HIT_SOUND_IDS = {
      "block.note_block.pling", "entity.experience_orb.pickup", "block.amethyst_block.chime", "entity.player.levelup",
      "entity.arrow.hit_player", "entity.player.attack.crit", "block.bell.use"
   };
   public static int hitSound = 0;
   public static float hitVolume = 1.0F;
   public static float hitPitch = 1.2F;

   // ---- Trajectories
   public static boolean trajBow = true, trajPearl = true, trajTrident = true, trajThrowables = true;
   public static int trajColor = 0x55FF99;

   // ---- Fog changer
   /** 0 = vanilla, 1 = no fog, 2 = custom distance. */
   public static int fogMode = 1;
   public static int fogDistance = 160;

   // ---- Motion blur
   public static float blurStrength = 5.0F;

   // ---- Tooltip+
   public static boolean tipDurability = true;
   public static boolean tipComponents = true;

   // ---- Auto reconnect
   public static boolean reconnectAuto = false;
   public static int reconnectDelay = 5;

   // ---- Zoom rework
   public static boolean zoomRamp = false;
   public static boolean zoomIndicator = true;
   private static boolean zoomWasActive = false;
   public static double zoomRuntime = 2.0;
   public static double zoomHeldSeconds = 0.0;
   public static boolean zoomScrolled = false;

   private static boolean b(Properties p, String k, boolean d) {
      return Boolean.parseBoolean(p.getProperty(k, String.valueOf(d)));
   }

   private static int i(Properties p, String k, int d) {
      try {
         return Integer.parseInt(p.getProperty(k, String.valueOf(d)));
      } catch (NumberFormatException e) {
         return d;
      }
   }

   private static float f(Properties p, String k, float d) {
      try {
         return Float.parseFloat(p.getProperty(k, String.valueOf(d)));
      } catch (NumberFormatException e) {
         return d;
      }
   }

   public static void load(Properties p) {
      for (int n = 0; n < NAMES.length; n++) on[n] = b(p, "fm.on." + n, n == MENUBLUR);
      bossHide = b(p, "fm.bossHide", false);
      bossScale = f(p, "fm.bossScale", 1.0F);
      bossOffsetX = i(p, "fm.bossOffsetX", 0);
      bossOffsetY = i(p, "fm.bossOffsetY", 0);
      dayShowClock = b(p, "fm.dayClock", true);
      dayClock24h = b(p, "fm.day24h", true);
      dayX = i(p, "fm.dayX", 10);
      dayY = i(p, "fm.dayY", 60);
      dayScale = f(p, "fm.dayScale", 1.0F);
      dirShowAngles = b(p, "fm.dirAngles", true);
      dirX = i(p, "fm.dirX", 10);
      dirY = i(p, "fm.dirY", 84);
      dirScale = f(p, "fm.dirScale", 1.0F);
      hlX = i(p, "fm.hlX", 10);
      hlY = i(p, "fm.hlY", 108);
      hlScale = f(p, "fm.hlScale", 1.0F);
      pixelShowBlock = b(p, "fm.plBlock", true);
      pixelHighlight = b(p, "fm.plHighlight", true);
      plX = i(p, "fm.plX", 10);
      plY = i(p, "fm.plY", 132);
      plScale = f(p, "fm.plScale", 1.0F);
      chunkRadius = i(p, "fm.chunkRadius", 1);
      chunkSubchunks = b(p, "fm.chunkSub", true);
      chunkColor = i(p, "fm.chunkColor", 0x00F5FF);
      overlayMode = i(p, "fm.ovMode", 1);
      overlayOutline = b(p, "fm.ovOutline", true);
      overlayFill = b(p, "fm.ovFill", true);
      overlayOutlineColor = i(p, "fm.ovOutlineColor", 0xFFFFFF);
      overlayFillColor = i(p, "fm.ovFillColor", 0xFFFFFF);
      overlayFillAlpha = i(p, "fm.ovFillAlpha", 40);
      overlayThickness = f(p, "fm.ovThickness", 2.0F);
      overlayGlow = b(p, "fm.ovGlow", true);
      blockHitAmount = f(p, "fm.blockHit", 1.0F);
      critMultiplier = i(p, "fm.critMult", 3);
      hitSound = i(p, "fm.hitSound", 0);
      hitVolume = f(p, "fm.hitVolume", 1.0F);
      hitPitch = f(p, "fm.hitPitch", 1.2F);
      trajBow = b(p, "fm.trajBow", true);
      trajPearl = b(p, "fm.trajPearl", true);
      trajTrident = b(p, "fm.trajTrident", true);
      trajThrowables = b(p, "fm.trajThrow", true);
      trajColor = i(p, "fm.trajColor", 0x55FF99);
      fogMode = i(p, "fm.fogMode", 1);
      fogDistance = i(p, "fm.fogDist", 160);
      blurStrength = f(p, "fm.blur", 5.0F);
      tipDurability = b(p, "fm.tipDur", true);
      tipComponents = b(p, "fm.tipComp", true);
      reconnectAuto = b(p, "fm.recAuto", false);
      reconnectDelay = i(p, "fm.recDelay", 5);
      zoomRamp = b(p, "fm.zoomRamp", false);
      zoomIndicator = b(p, "fm.zoomIndicator", true);
   }

   public static void save(Properties p) {
      for (int n = 0; n < NAMES.length; n++) p.setProperty("fm.on." + n, String.valueOf(on[n]));
      p.setProperty("fm.bossHide", String.valueOf(bossHide));
      p.setProperty("fm.bossScale", String.valueOf(bossScale));
      p.setProperty("fm.bossOffsetX", String.valueOf(bossOffsetX));
      p.setProperty("fm.bossOffsetY", String.valueOf(bossOffsetY));
      p.setProperty("fm.dayClock", String.valueOf(dayShowClock));
      p.setProperty("fm.day24h", String.valueOf(dayClock24h));
      p.setProperty("fm.dayX", String.valueOf(dayX));
      p.setProperty("fm.dayY", String.valueOf(dayY));
      p.setProperty("fm.dayScale", String.valueOf(dayScale));
      p.setProperty("fm.dirAngles", String.valueOf(dirShowAngles));
      p.setProperty("fm.dirX", String.valueOf(dirX));
      p.setProperty("fm.dirY", String.valueOf(dirY));
      p.setProperty("fm.dirScale", String.valueOf(dirScale));
      p.setProperty("fm.hlX", String.valueOf(hlX));
      p.setProperty("fm.hlY", String.valueOf(hlY));
      p.setProperty("fm.hlScale", String.valueOf(hlScale));
      p.setProperty("fm.plBlock", String.valueOf(pixelShowBlock));
      p.setProperty("fm.plHighlight", String.valueOf(pixelHighlight));
      p.setProperty("fm.plX", String.valueOf(plX));
      p.setProperty("fm.plY", String.valueOf(plY));
      p.setProperty("fm.plScale", String.valueOf(plScale));
      p.setProperty("fm.chunkRadius", String.valueOf(chunkRadius));
      p.setProperty("fm.chunkSub", String.valueOf(chunkSubchunks));
      p.setProperty("fm.chunkColor", String.valueOf(chunkColor));
      p.setProperty("fm.ovMode", String.valueOf(overlayMode));
      p.setProperty("fm.ovOutline", String.valueOf(overlayOutline));
      p.setProperty("fm.ovFill", String.valueOf(overlayFill));
      p.setProperty("fm.ovOutlineColor", String.valueOf(overlayOutlineColor));
      p.setProperty("fm.ovFillColor", String.valueOf(overlayFillColor));
      p.setProperty("fm.ovFillAlpha", String.valueOf(overlayFillAlpha));
      p.setProperty("fm.ovThickness", String.valueOf(overlayThickness));
      p.setProperty("fm.ovGlow", String.valueOf(overlayGlow));
      p.setProperty("fm.blockHit", String.valueOf(blockHitAmount));
      p.setProperty("fm.critMult", String.valueOf(critMultiplier));
      p.setProperty("fm.hitSound", String.valueOf(hitSound));
      p.setProperty("fm.hitVolume", String.valueOf(hitVolume));
      p.setProperty("fm.hitPitch", String.valueOf(hitPitch));
      p.setProperty("fm.trajBow", String.valueOf(trajBow));
      p.setProperty("fm.trajPearl", String.valueOf(trajPearl));
      p.setProperty("fm.trajTrident", String.valueOf(trajTrident));
      p.setProperty("fm.trajThrow", String.valueOf(trajThrowables));
      p.setProperty("fm.trajColor", String.valueOf(trajColor));
      p.setProperty("fm.fogMode", String.valueOf(fogMode));
      p.setProperty("fm.fogDist", String.valueOf(fogDistance));
      p.setProperty("fm.blur", String.valueOf(blurStrength));
      p.setProperty("fm.tipDur", String.valueOf(tipDurability));
      p.setProperty("fm.tipComp", String.valueOf(tipComponents));
      p.setProperty("fm.recAuto", String.valueOf(reconnectAuto));
      p.setProperty("fm.recDelay", String.valueOf(reconnectDelay));
      p.setProperty("fm.zoomRamp", String.valueOf(zoomRamp));
      p.setProperty("fm.zoomIndicator", String.valueOf(zoomIndicator));
   }

   // =================== HUD ===================

   public static String dayText() {
      MinecraftClient mc = MinecraftClient.getInstance();
      long t = mc.world != null ? mc.world.getTimeOfDay() : 0L;
      long day = t / 24000L + 1L;
      String s = "Day " + day;
      if (dayShowClock) {
         int ticks = (int) (t % 24000L);
         int hours = (ticks / 1000 + 6) % 24;
         int mins = (ticks % 1000) * 60 / 1000;
         if (dayClock24h) {
            s += String.format("  %02d:%02d", hours, mins);
         } else {
            s += String.format("  %d:%02d %s", hours % 12 == 0 ? 12 : hours % 12, mins, hours < 12 ? "AM" : "PM");
         }
      }

      return s;
   }

   public static String dirText() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null) return "Facing: -";
      float yaw = net.minecraft.util.math.MathHelper.wrapDegrees(mc.player.getYaw());
      String[] names = {"S (+Z)", "SW", "W (-X)", "NW", "N (-Z)", "NE", "E (+X)", "SE"};
      int idx = Math.floorMod(Math.round((yaw + 180.0F) / 45.0F) + 4, 8);
      // yaw 0 = south, 90 = west, 180/-180 = north, -90 = east
      String[] byIndex = {"N (-Z)", "NE", "E (+X)", "SE", "S (+Z)", "SW", "W (-X)", "NW"};
      String dir = byIndex[Math.floorMod(Math.round((yaw + 180.0F) / 45.0F), 8)];
      if (dirShowAngles) {
         return String.format("%s  Yaw %.1f  Pitch %.1f", dir, yaw, mc.player.getPitch());
      }

      return dir;
   }

   public static String heightText() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null) return "Height limit: -";
      int y = (int) Math.floor(mc.player.getY());
      int up = mc.world.getTopYInclusive() - y;
      int down = y - mc.world.getBottomY();
      return "Build limit: " + up + " up  |  " + down + " down";
   }

   private static void box(DrawContext ctx, MinecraftClient mc, String text) {
      int w = mc.textRenderer.getWidth(text) + 10;
      int col = VoidCyanClient.getPrimaryColor();
      ctx.fill(0, 0, w, 18, Integer.MIN_VALUE);
      ctx.fill(0, 0, w, 1, col);
      ctx.fill(0, 17, w, 18, col);
      ctx.fill(0, 0, 1, 18, col);
      ctx.fill(w - 1, 0, w, 18, col);
      ctx.drawTextWithShadow(mc.textRenderer, text, 5, 5, -1);
   }

   public static int textBoxWidth(String text) {
      return MinecraftClient.getInstance().textRenderer.getWidth(text) + 10;
   }

   public static void renderHud(DrawContext ctx) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || mc.world == null) return;
      if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen)) return;
      if (VoidCyanClient.isZoomEnabled && zoomIndicator && VoidCyanClient.zoomActive && VoidCyanClient.currentZoomMultiplier > 1.01) {
         String zt = String.format("Zoom %.1fx", VoidCyanClient.currentZoomMultiplier);
         int zw = mc.textRenderer.getWidth(zt);
         int zx = ctx.getScaledWindowWidth() / 2 - zw / 2;
         int zy = ctx.getScaledWindowHeight() / 2 + 14;
         ctx.fill(zx - 4, zy - 3, zx + zw + 4, zy + 11, 0x90000000);
         ctx.drawTextWithShadow(mc.textRenderer, zt, zx, zy, 0xFFFFFFFF);
      }

      if (on[DAY]) {
         VoidCyanClient.applyScale(ctx, dayX, dayY, dayScale);
         box(ctx, mc, dayText());
         VoidCyanClient.resetScale(ctx);
      }

      if (on[DIR]) {
         VoidCyanClient.applyScale(ctx, dirX, dirY, dirScale);
         box(ctx, mc, dirText());
         VoidCyanClient.resetScale(ctx);
      }

      if (on[PIXEL]) {
         VoidCyanClient.applyScale(ctx, plX, plY, plScale);
         box(ctx, mc, pixelText());
         VoidCyanClient.resetScale(ctx);
      }

      if (on[HEIGHT]) {
         VoidCyanClient.applyScale(ctx, hlX, hlY, hlScale);
         box(ctx, mc, heightText());
         VoidCyanClient.resetScale(ctx);
      }
   }

   // =================== Tick ===================

   private static float lastYaw = 0.0F;
   private static float lastPitch = 0.0F;
   public static float rotationSpeed = 0.0F;

   public static void tick(MinecraftClient mc) {
      if (mc.player == null) return;
      if (on[SPRINT] && mc.currentScreen == null && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()) {
         mc.options.sprintKey.setPressed(true);
      }

      float yaw = mc.player.getYaw();
      float pitch = mc.player.getPitch();
      float d = Math.abs(net.minecraft.util.math.MathHelper.wrapDegrees(yaw - lastYaw)) + Math.abs(pitch - lastPitch);
      rotationSpeed = rotationSpeed * 0.6F + d * 0.4F;
      lastYaw = yaw;
      lastPitch = pitch;
   }

   /** Advances the zoom ramp; returns the zoom multiplier target while zoom is active. */
   public static double zoomTarget(double starter, boolean active, double dt) {
      if (!active) {
         zoomWasActive = false;
         zoomHeldSeconds = 0.0;
         zoomScrolled = false;
         zoomRuntime = starter;
         return 1.0;
      }

      if (!zoomWasActive) {
         // Every new zoom starts fresh from the starter level (no leftover scroll / ramp state).
         zoomWasActive = true;
         zoomHeldSeconds = 0.0;
         zoomScrolled = false;
         zoomRuntime = starter;
      }

      if (zoomRamp && !zoomScrolled) {
         zoomHeldSeconds += dt;
         zoomRuntime = Math.max(starter, rampAt(zoomHeldSeconds));
      }

      return Math.max(1.0, Math.min(1000.0, zoomRuntime));
   }

   /** Zoom level reached after {@code t} seconds of holding: 0-2x in 1s, to 10x by 4s, 50x by 12s, 100x by 18s, 1000x by 30s. */
   public static double rampAt(double t) {
      double[][] pts = {{0, 1}, {1, 2}, {4, 10}, {12, 50}, {18, 100}, {30, 1000}};
      if (t >= pts[pts.length - 1][0]) return pts[pts.length - 1][1];
      for (int k = 1; k < pts.length; k++) {
         if (t <= pts[k][0]) {
            double u = (t - pts[k - 1][0]) / (pts[k][0] - pts[k - 1][0]);
            return pts[k - 1][1] + (pts[k][1] - pts[k - 1][1]) * u;
         }
      }

      return 1000.0;
   }

   // =================== Attack hooks ===================

   private static boolean isCrit(PlayerEntity p) {
      return p.fallDistance > 0.0F && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater() && !p.hasVehicle() && !p.isSprinting();
   }

   public static void onAttack(PlayerEntity attacker, World world, Entity target) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null || attacker != mc.player || world == null || !world.isClient() || target == null) return;
      if (on[HITSND]) {
         SoundEvent se = Registries.SOUND_EVENT.get(Identifier.of("minecraft", HIT_SOUND_IDS[Math.max(0, Math.min(HIT_SOUND_IDS.length - 1, hitSound))]));
         if (se != null) mc.getSoundManager().play(PositionedSoundInstance.ui(se, hitPitch, hitVolume));
      }

      if (on[CRIT] && critMultiplier > 1 && isCrit(attacker)) {
         java.util.Random r = new java.util.Random();
         int n = (critMultiplier - 1) * 8;
         for (int k = 0; k < n; k++) {
            double x = target.getX() + (r.nextDouble() - 0.5) * target.getWidth() * 1.5;
            double y = target.getY() + r.nextDouble() * target.getHeight();
            double z = target.getZ() + (r.nextDouble() - 0.5) * target.getWidth() * 1.5;
            world.addParticleClient(ParticleTypes.CRIT, x, y, z, (r.nextDouble() - 0.5) * 0.6, r.nextDouble() * 0.4, (r.nextDouble() - 0.5) * 0.6);
         }
      }
   }

   // =================== Tooltips ===================

   private static int tooltipScroll = 0;
   private static int tooltipTotal = 0;
   private static ItemStack lastTipStack = ItemStack.EMPTY;

   private static boolean ctrlDown() {
      long h = MinecraftClient.getInstance().getWindow().getHandle();
      return org.lwjgl.glfw.GLFW.glfwGetKey(h, 341) == 1 || org.lwjgl.glfw.GLFW.glfwGetKey(h, 345) == 1;
   }

   public static int tooltipMaxLines() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return Math.max(6, mc.getWindow().getScaledHeight() / 10 - 3);
   }

   public static boolean scrollTooltip(double amount) {
      if (!on[TOOLTIP] || tooltipTotal <= tooltipMaxLines()) return false;
      int max = tooltipTotal - tooltipMaxLines();
      tooltipScroll = Math.max(0, Math.min(max, tooltipScroll - (int) Math.signum(amount) * 2));
      return true;
   }

   public static void registerTooltip() {
      ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
         if (!on[TOOLTIP]) return;
         if (tipDurability && stack.isDamageable() && stack.getMaxDamage() > 0) {
            int left = stack.getMaxDamage() - stack.getDamage();
            int pct = left * 100 / stack.getMaxDamage();
            Formatting col = pct > 60 ? Formatting.GREEN : pct > 30 ? Formatting.YELLOW : Formatting.RED;
            lines.add(Text.literal("Durability: " + pct + "% (" + left + "/" + stack.getMaxDamage() + ")").formatted(col));
         }

         if (tipComponents && ctrlDown()) {
            List<String> names = new ArrayList<>();
            stack.getComponents().forEach(c -> names.add(Registries.DATA_COMPONENT_TYPE.getId(c.type()) == null ? "?" : Registries.DATA_COMPONENT_TYPE.getId(c.type()).getPath()));
            lines.add(Text.literal("Components (" + names.size() + "):").formatted(Formatting.DARK_GRAY));
            for (int k = 0; k < names.size() && k < 24; k++) lines.add(Text.literal(" " + names.get(k)).formatted(Formatting.DARK_GRAY));
         }

         if (!ItemStack.areEqual(stack, lastTipStack)) {
            tooltipScroll = 0;
            lastTipStack = stack.copy();
         }

         int max = tooltipMaxLines();
         tooltipTotal = lines.size();
         if (lines.size() > max) {
            List<Text> all = new ArrayList<>(lines);
            int bodyMax = max - 2;
            int start = Math.max(0, Math.min(tooltipScroll, all.size() - 1 - bodyMax));
            tooltipScroll = start;
            lines.clear();
            lines.add(all.get(0));
            for (int k = 0; k < bodyMax; k++) lines.add(all.get(1 + start + k));
            lines.add(Text.literal("[scroll] " + (start + 1) + "-" + (start + bodyMax) + " / " + (all.size() - 1)).formatted(Formatting.AQUA));
         }
      });
   }

   // =================== Block hit (1.7 style) ===================

   /** Tilts the sword while right-click is held, on top of the normal swing (approximation of the 1.7 block pose). */
   public static void applyBlockHit(AbstractClientPlayerEntity player, Hand hand, ItemStack item, MatrixStack m) {
      if (!on[BLOCKHIT] || hand != Hand.MAIN_HAND || !item.isIn(ItemTags.SWORDS)) return;
      if (!MinecraftClient.getInstance().options.useKey.isPressed()) return;
      float a = blockHitAmount;
      m.translate(-0.12F * a, 0.04F * a, 0.0F);
      m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-25.0F * a));
      m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0F * a));
      m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F * a));
   }

   // =================== Events: reconnect button, tooltip scroll ===================

   private static ServerInfo lastServer = null;

   public static void initEvents() {
      registerTooltip();
      ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
         ServerInfo si = client.getCurrentServerEntry();
         if (si != null) lastServer = si;
      });
      ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
         if (screen instanceof HandledScreen<?>) {
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hx, vy) -> !scrollTooltip(vy));
         }

         if (screen instanceof DisconnectedScreen && on[RECON] && lastServer != null) {
            final ServerInfo target = lastServer;
            final long shown = System.currentTimeMillis();
            ButtonWidget btn = ButtonWidget.builder(Text.literal("Reconnect"), b -> reconnect(client, target))
               .dimensions(screen.width / 2 - 100, Math.min(screen.height - 28, screen.height / 2 + 96), 200, 20)
               .build();
            Screens.getButtons(screen).add(btn);
            ScreenEvents.afterTick(screen).register(s -> {
               if (!reconnectAuto) return;
               int left = reconnectDelay - (int) ((System.currentTimeMillis() - shown) / 1000L);
               if (left <= 0) {
                  reconnect(client, target);
               } else {
                  btn.setMessage(Text.literal("Reconnect (" + left + ")"));
               }
            });
         }
      });
   }

   private static void reconnect(MinecraftClient client, ServerInfo info) {
      ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(info.address), info, false, null);
   }
}
