package com.voidcyan.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Billboard-based visual FX engine for crit effects.
 *
 * Tracks lightweight spark instances (position, velocity, gravity, drag,
 * lifetime, rotation, size/alpha curves) and renders them as camera-facing
 * quads through the debugQuads (position+color) layer — no mesh files, no
 * texture binds, GPU-cheap.
 *
 * Presets are spawned from CritEffectsManager when an effect JSON uses
 * "shape": "<preset name>".
 */
public final class CritBillboardFX {
   private CritBillboardFX() {
   }

   // ===================== Data class =====================

   public static final class Spark {
      public double x, y, z;
      public double vx, vy, vz;
      public float life;        // seconds lived
      public float maxLife;     // seconds to live
      public float size0, size1; // size curve start -> end
      public int color;         // ARGB (alpha channel = base alpha)
      public float rot;         // radians, billboard-plane rotation
      public float rotSpeed;    // radians per second
      public float gravity;     // blocks/s^2
      public float drag;        // velocity multiplier per second (1 = none)
      public float fadeIn = 0.05F; // seconds of fade-in before fade-out begins
      /** If true the quad is stretched along velocity (streak). */
      public boolean streak;
   }

   // ===================== Registry =====================

   private static final List<Spark> SPARKS = new ArrayList<>();
   private static final Random RNG = new Random();

   // ===================== GUI preview playback =====================

   /**
    * One independent preview playback per preset shape, so every card in the
    * settings grid animates its own effect at the same time (the old single
    * shared list made all cards fight over one burst — nothing animated).
    */
   private static final java.util.Map<String, List<Spark>> PREVIEWS = new java.util.HashMap<>();

   /**
    * Starts/refreshes the looping preview of one preset. Rendered in screen
    * space via previewSparks(shape) — no world needed. Pass retrigger=true to
    * restart the burst (hover re-fire in the preview grid).
    */
   public static final java.util.Map<String, List<FxPart>> CUSTOM_PARTS = new java.util.HashMap<>();

   public static void beginPreview(String shape, boolean retrigger) {
      if (shape == null || shape.isEmpty()) return;
      List<Spark> list = PREVIEWS.get(shape);
      boolean needsNew = list == null || list.isEmpty() || retrigger;
      if (needsNew) {
         if (list == null) {
            list = new ArrayList<>();
            PREVIEWS.put(shape, list);
         }
         list.clear();
         lastPreviewStep.put(shape, System.currentTimeMillis());
         // capture world sparks, spawn the preview burst, snapshot it, restore
         List<Spark> saved = new ArrayList<>(SPARKS);
         SPARKS.clear();
         if (shape.startsWith("custom:")) {
            List<FxPart> cp = CUSTOM_PARTS.get(shape.substring(7));
            if (cp != null) spawnCustom(cp, 0.0, 0.0, 0.0);
         } else {
            spawnPreset(shape, 0.0, 0.0, 0.0, presetColor(shape));
         }
         list.addAll(SPARKS);
         SPARKS.clear();
         SPARKS.addAll(saved);
      }
   }

   /** Advances + returns the live preview sparks of one preset for screen-space rendering. */
   public static List<Spark> previewSparks(String shape) {
      List<Spark> list = PREVIEWS.get(shape);
      if (list == null) return List.of();
      // advance by REAL elapsed time (was a fixed 0.05s per frame — at 60fps
      // that made menu previews run ~5x faster than the in-world effect).
      long now = System.currentTimeMillis();
      Long last = lastPreviewStep.get(shape);
      float dt = last == null ? 0.0F : Math.min(0.1F, (now - last) / 1000.0F);
      lastPreviewStep.put(shape, now);
      if (dt > 0.0F) advance(list, dt);
      return list;
   }

   private static final java.util.Map<String, Long> lastPreviewStep = new java.util.HashMap<>();

   public static void clearPreviews() {
      PREVIEWS.clear();
      lastPreviewStep.clear();
   }

   public static void clear() {
      SPARKS.clear();
   }

   // ===================== Update =====================

   public static void tickSparks(float deltaTicks) {
      advance(SPARKS, Math.min(0.1F, deltaTicks / 20.0F)); // ticks -> seconds, clamp
   }

   /** Shared physics integration: drag, gravity, position, rotation. */
   private static void advance(List<Spark> list, float dt) {
      Iterator<Spark> it = list.iterator();
      while (it.hasNext()) {
         Spark s = it.next();
         s.life += dt;
         if (s.life >= s.maxLife) {
            it.remove();
            continue;
         }
         float dragMul = (float) Math.pow(s.drag, dt);
         s.vx *= dragMul;
         s.vy *= dragMul;
         s.vz *= dragMul;
         s.vy -= s.gravity * dt;
         s.x += s.vx * dt;
         s.y += s.vy * dt;
         s.z += s.vz * dt;
         s.rot += s.rotSpeed * dt;
      }
   }

   // ===================== Rendering =====================

   /** Called from the world render hook (AFTER_ENTITIES). */
   public static void render(WorldRenderContext context) {
      if (SPARKS.isEmpty()) return;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null) return;

      MatrixStack matrices = context.matrices();
      VertexConsumerProvider consumers = context.consumers();
      if (matrices == null || consumers == null) return;

      // Billboard basis from the camera rotation.
      // The AFTER_ENTITIES matrix stack is CAMERA-RELATIVE: world sparks must be
      // shifted by the camera position or they render millions of blocks away
      // (this was why effects were invisible in-world).
      Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
      Quaternionf camRot = mc.gameRenderer.getCamera().getRotation();
      Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(camRot);
      Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(camRot);
      MatrixStack.Entry entry = matrices.peek();

      VertexConsumer buffer = consumers.getBuffer(RenderLayers.debugQuads());

      for (Spark s : SPARKS) {
         float t = s.life / s.maxLife; // 0..1
         // alpha: quick fade-in, long fade-out
         float alphaMul = t < s.fadeIn ? t / s.fadeIn : 1.0F - (t - s.fadeIn) / (1.0F - s.fadeIn);
         alphaMul = Math.max(0.0F, Math.min(1.0F, alphaMul));
         alphaMul *= 0.86F + 0.14F * (float) Math.sin(s.life * 38.0F + s.x * 13.0);
         int argb = withAlpha(s.color, alphaMul);
         float size = s.size0 + (s.size1 - s.size0) * t;
         if (size <= 0.001F || (argb >>> 24) == 0) continue;

         // translate to spark pos, camera-relative
         float px = (float) (s.x - camPos.x);
         float py = (float) (s.y - camPos.y);
         float pz = (float) (s.z - camPos.z);

         float cos = (float) Math.cos(s.rot);
         float sin = (float) Math.sin(s.rot);

         // ===== GLOW PASS: 2 layered quads (outer halo + mid ring) =====
         // Halos keep the billboard axis-aligned (no rotation) for a clean bloom look.
         int haloA = (int) ((argb >>> 24) * 0.22F);
         if (haloA > 4) {
            float h1 = size * 2.2F;
            quad(buffer, entry, px, py, pz, right, up, h1, (haloA << 24 | argb & 0xFFFFFF));
            float h2 = size * 1.35F;
            int midA = (int) ((argb >>> 24) * 0.42F);
            quad(buffer, entry, px, py, pz, right, up, h2, (midA << 24 | argb & 0xFFFFFF));
         }

         // ===== MOTION TRAIL: fading afterimages behind fast sparks =====
         double spd0 = Math.sqrt(s.vx * s.vx + s.vy * s.vy + s.vz * s.vz);
         if (spd0 > 1.2 && size > 0.02F) {
            for (int k = 1; k <= 4; k++) {
               float back = 0.03F * k;
               int ga = (int) ((argb >>> 24) * 0.42F / k);
               if (ga <= 3) break;
               quad(buffer, entry, px - (float) s.vx * back, py - (float) s.vy * back, pz - (float) s.vz * back,
                  right, up, size * (1.0F - 0.17F * k), ga << 24 | argb & 0xFFFFFF);
            }
         }

         // ===== CORE PASS (rotated / streaked as before) =====
         float hx, hy; // half extent along rotated axes
         float exX, exY, exZ; // extent axis 1
         float eyX, eyY, eyZ; // extent axis 2
         if (s.streak) {
            // stretch along velocity (projected on billboard plane)
            double spd = Math.sqrt(s.vx * s.vx + s.vy * s.vy + s.vz * s.vz);
            float len = Math.max(0.10F, (float) Math.min(1.8, spd * 0.11));
            float dvx = (float) s.vx, dvy = (float) s.vy, dvz = (float) s.vz;
            float rdot = dvx * right.x + dvy * right.y + dvz * right.z;
            float udot = dvx * up.x + dvy * up.y + dvz * up.z;
            float vlen = (float) Math.sqrt(rdot * rdot + udot * udot);
            if (vlen < 1e-4) {
               exX = right.x;
               exY = right.y;
               exZ = right.z;
            } else {
               exX = (rdot / vlen) * right.x * len + (udot / vlen) * up.x * len;
               exY = (rdot / vlen) * right.y * len + (udot / vlen) * up.y * len;
            }
            exZ = (rdot / vlen) * right.z * len + (udot / vlen) * up.z * len;
            eyX = -exY;
            eyY = exX;
            eyZ = 0.0F;
            if (Math.abs(exX) + Math.abs(exY) + Math.abs(exZ) < 1e-4) {
               exX = right.x * len;
               exY = right.y * len;
               exZ = right.z * len;
               eyX = up.x * size;
               eyY = up.y * size;
               eyZ = up.z * size;
            }
         } else {
            // axis1 = rotated right * size, axis2 = rotated up * size
            exX = (right.x * cos - right.y * sin) * size;
            exY = (right.y * cos - right.x * sin) * size;
            exZ = right.z * size;
            eyX = (up.x * cos + up.y * sin) * size;
            eyY = (up.y * cos - up.x * sin) * size;
            eyZ = up.z * size;
         }

         emitQuad(buffer, entry, px, py, pz, exX, exY, exZ, eyX, eyY, eyZ, argb);
         // bright white-ish core for extra punch
         int coreA = (int) ((argb >>> 24) * 0.75F);
         if (coreA > 6 && size > 0.05F) {
            float csize = size * 0.4F;
            int core = coreA << 24 | 0xFFFFFF;
            quad(buffer, entry, px, py, pz, right, up, csize, core);
         }
      }
   }

   /** Axis-aligned billboard quad helper (halo/core passes). */
   private static void quad(VertexConsumer buffer, MatrixStack.Entry entry, float px, float py, float pz,
                            Vector3f right, Vector3f up, float size, int argb) {
      float exX = right.x * size;
      float exY = right.y * size;
      float exZ = right.z * size;
      float eyX = up.x * size;
      float eyY = up.y * size;
      float eyZ = up.z * size;
      emitQuad(buffer, entry, px, py, pz, exX, exY, exZ, eyX, eyY, eyZ, argb);
   }

   private static void emitQuad(VertexConsumer buffer, MatrixStack.Entry entry, float px, float py, float pz,
                                float exX, float exY, float exZ, float eyX, float eyY, float eyZ, int argb) {
      float ax = px - exX - eyX;
      float ay = py - exY - eyY;
      float az = pz - exZ - eyZ;
      float bx = px + exX - eyX;
      float by = py + exY - eyY;
      float bz = pz + exZ - eyZ;
      float qx = px + exX + eyX;
      float qy = py + exY + eyY;
      float qz = pz + exZ + eyZ;
      float dx2 = px - exX + eyX;
      float dy2 = py - exY + eyY;
      float dz2 = pz - exZ + eyZ;

      buffer.vertex(entry, ax, ay, az);
      buffer.color(argb);
      buffer.vertex(entry, bx, by, bz);
      buffer.color(argb);
      buffer.vertex(entry, qx, qy, qz);
      buffer.color(argb);
      buffer.vertex(entry, dx2, dy2, dz2);
      buffer.color(argb);
   }
   // ===================== Spawner helpers =====================

   private static Spark spawn(double x, double y, double z, double vx, double vy, double vz,
                              double maxLife, double size0, double size1, int argb,
                              double rot, double rotSpeed, double gravity, double drag) {
      if (SPARKS.size() > 6000) return null; // hard cap
      Spark s = new Spark();
      s.x = x;
      s.y = y;
      s.z = z;
      s.vx = vx;
      s.vy = vy;
      s.vz = vz;
      // Fine-detail rendering: 0.5x authored sizes → tiny, dense, pixel-fine sparks.
      s.maxLife = (float) maxLife;
      s.size0 = (float) size0 * 0.5F;
      s.size1 = (float) size1 * 0.5F;
      s.color = argb;
      s.rot = (float) rot;
      s.rotSpeed = (float) rotSpeed;
      s.gravity = (float) gravity;
      s.drag = (float) drag;
      SPARKS.add(s);
      return s;
   }

   private static int withAlpha(int argb, float mul) {
      int a = (int) ((argb >>> 24) * Math.max(0.0F, Math.min(1.0F, mul)));
      return a << 24 | argb & 0x00FFFFFF;
   }

   private static double rnd(double min, double max) {
      return min + RNG.nextDouble() * (max - min);
   }

   // ===================== Presets =====================

   // ===================== Presets =====================

   /** Ordered preset names for GUIs (preview lists, README). */
   public static final String[] PRESET_NAMES = {
      "blue_comet", "blue_cross", "blue_star", "blue_swirl", "cyan_droplet",
      "golden_ring", "shadow_slash", "hellfire_burst", "electric_spark", "emerald_shard",
      "thunder_bolt", "frost_nova", "blood_impact", "void_rift", "holy_flash",
      "toxic_burst", "crystal_shatter"
   };

   /** Signature ARGB color per preset (used for previews and GUI dots). */
   public static int presetColor(String shape) {
      return switch (shape == null ? "" : shape) {
         case "blue_comet" -> 0xFF00E5FF;
         case "blue_cross" -> 0xFF4DA6FF;
         case "blue_star" -> 0xFF7DD3FF;
         case "blue_swirl" -> 0xFF00C3FF;
         case "cyan_droplet" -> 0xFF00F5FF;
         case "golden_ring" -> 0xFFFFB300;
         case "shadow_slash" -> 0xFF9B30FF;
         case "hellfire_burst" -> 0xFFFF6A00;
         case "electric_spark" -> 0xFFBFEFFF;
         case "emerald_shard" -> 0xFF17E86B;
         case "thunder_bolt" -> 0xFFEAF6FF;
         case "frost_nova" -> 0xFF9FDCFF;
         case "blood_impact" -> 0xFFC1121F;
         case "void_rift" -> 0xFF8A2BE2;
         case "holy_flash" -> 0xFFFFD700;
         case "toxic_burst" -> 0xFF39FF14;
         case "crystal_shatter" -> 0xFFCFFAFF;
         default -> 0xFF00F5FF;
      };
   }

   /** Friendly display name for a preset shape. */
   public static String presetDisplayName(String shape) {
      return switch (shape == null ? "" : shape) {
         case "blue_comet" -> "Blue Comet";
         case "blue_cross" -> "Blue Cross";
         case "blue_star" -> "Blue Star";
         case "blue_swirl" -> "Blue Swirl";
         case "cyan_droplet" -> "Cyan Droplet";
         case "golden_ring" -> "Golden Ring";
         case "shadow_slash" -> "Shadow Slash";
         case "hellfire_burst" -> "Hellfire Burst";
         case "electric_spark" -> "Electric Spark";
         case "emerald_shard" -> "Emerald Shard";
         case "thunder_bolt" -> "Thunder Bolt";
         case "frost_nova" -> "Frost Nova";
         case "blood_impact" -> "Blood Impact";
         case "void_rift" -> "Void Rift";
         case "holy_flash" -> "Holy Flash";
         case "toxic_burst" -> "Toxic Burst";
         case "crystal_shatter" -> "Crystal Shatter";
         default -> shape;
      };
   }

   /** Dispatch entry — called from CritEffectsManager.play(). */
   /** Spawns a user-built particle model (list of FxPart) anchored at x,y,z into the world. */
   public static void spawnCustom(List<FxPart> parts, double x, double y, double z) {
      for (FxPart p : parts) {
         int argb = p.argb();
         for (float[] q : p.points()) {
            double sx = q[0], sz = q[2];
            double vx = q[3] * p.outward - sz * p.spin;
            double vy = q[4] * p.outward + p.rise;
            double vz = q[5] * p.outward + sx * p.spin;
            Spark s = spawn(x + q[0], y + q[1], z + q[2], vx, vy, vz,
               p.life * rnd(0.85, 1.15), p.size * 2.0, p.size * 0.5, argb, RNG.nextDouble() * 6.28, rnd(-3, 3), p.gravity, 0.985);
            if (s != null) s.streak = p.streak;
         }
      }
   }

   /** Builds the sparks of a custom model without touching the live world list (editor preview). */
   public static List<Spark> buildCustom(List<FxPart> parts) {
      List<Spark> saved = new ArrayList<>(SPARKS);
      SPARKS.clear();
      spawnCustom(parts, 0.0, 0.0, 0.0);
      List<Spark> out = new ArrayList<>(SPARKS);
      SPARKS.clear();
      SPARKS.addAll(saved);
      return out;
   }

   public static void advanceList(List<Spark> list, float dt) {
      advance(list, dt);
   }

   public static boolean spawnPreset(String shape, double x, double y, double z, int argb) {
      switch (shape == null ? "" : shape.toLowerCase()) {
         case "blue_comet" -> blueComet(x, y, z, argb);
         case "blue_cross" -> blueCross(x, y, z, argb);
         case "blue_star" -> blueStar(x, y, z, argb);
         case "blue_swirl" -> blueSwirl(x, y, z, argb);
         case "cyan_droplet" -> cyanDroplet(x, y, z, argb);
         case "golden_ring" -> goldenRing(x, y, z, argb);
         case "shadow_slash" -> shadowSlash(x, y, z, argb);
         case "hellfire_burst" -> hellfireBurst(x, y, z, argb);
         case "electric_spark" -> electricSpark(x, y, z, argb);
         case "emerald_shard" -> emeraldShard(x, y, z, argb);
         case "thunder_bolt" -> thunderBolt(x, y, z, argb);
         case "frost_nova" -> frostNova(x, y, z, argb);
         case "blood_impact" -> bloodImpact(x, y, z, argb);
         case "void_rift" -> voidRift(x, y, z, argb);
         case "holy_flash" -> holyFlash(x, y, z, argb);
         case "toxic_burst" -> toxicBurst(x, y, z, argb);
         case "crystal_shatter" -> crystalShatter(x, y, z, argb);
         default -> {
            return false;
         }
      }
      return true;
   }

   // ---- Blue Comet: fast cyan streak + stardust trail ----
   private static void blueComet(double x, double y, double z, int argb) {
      double a = RNG.nextDouble() * Math.PI * 2.0;
      double speed = rnd(9.0, 12.0);
      double vx = Math.cos(a) * speed;
      double vz = Math.sin(a) * speed;
      // head
      Spark head = spawn(x, y + 0.4, z, vx, rnd(1.0, 2.0), vz,
         0.35F, 0.45F, 0.05F, withAlpha(argb, 1.0F), 0.0F, 0.0F, 2.0F, 0.85F);
      if (head != null) head.streak = true;
      // stardust trail (spawned over time by the streak head? keep simple: burst behind)
      for (int i = 0; i < 90; i++) {
         double t = i / 90.0;
         double px = x - vx * t * 0.035;
         double py = y + 0.4 - vx * 0.0 + t * rnd(-0.05, 0.05);
         double pz = z - vz * t * 0.035;
         spawn(px, py, pz, rnd(-0.3, 0.3), rnd(0.2, 0.8), rnd(-0.3, 0.3),
            rnd(0.4, 0.9), rnd(0.06, 0.14), 0.0F, withAlpha(argb, (float) rnd(0.5, 0.95)),
            RNG.nextFloat() * 3.0F, rnd(-2.0F, 2.0F), 1.2F, 0.92F);
      }
      // extra far-traveling dust so the trail reads at distance
      for (int i = 0; i < 60; i++) {
         double t = i / 18.0;
         spawn(x - vx * t * 0.075, y + 0.45 + t * rnd(-0.08, 0.12), z - vz * t * 0.075,
            rnd(-0.2, 0.2), rnd(0.1, 0.6), rnd(-0.2, 0.2),
            rnd(0.6, 1.1), rnd(0.05, 0.10), 0.0F, withAlpha(argb, (float) rnd(0.35, 0.8)),
            RNG.nextFloat() * 3.0F, rnd(-2.0F, 2.0F), 0.8F, 0.94F);
      }
   }

   // ---- Blue Cross: expanding glowing cross burst ----
   private static void blueCross(double x, double y, double z, int argb) {
      for (int arm = 0; arm < 4; arm++) {
         double ang = arm * Math.PI / 2.0 + Math.PI / 4.0;
         double c = Math.cos(ang);
         double s = Math.sin(ang);
         for (int i = 0; i < 60; i++) {
            float t = i / 7.0F;
            double px = x + c * (0.15 + t * 1.0);
            double py = y + 1.0 + s * (0.15 + t * 1.0);
            double pz = z;
            spawn(px, py, pz, c * rnd(1.5, 2.5), s * rnd(1.5, 2.5), 0.0,
               rnd(0.35, 0.55), 0.22F - t * 0.12F, 0.0F, withAlpha(argb, 1.0F - t * 0.3F),
               (float) (ang + Math.PI / 2.0), 0.0F, 0.0F, 0.9F);
         }
      }
      // core flash
      spawn(x, y + 1.0, z, 0, 0, 0, 0.2F, 0.5F, 0.7F, withAlpha(argb, 0.9F), 0.0F, 0.0F, 0.0F, 1.0F);
   }

   // ---- Blue Star: expanding 2D star flash, smooth dissolve ----
   private static void blueStar(double x, double y, double z, int argb) {
      // 5-point star drawn with rotated quads; scale pulse then dissolve
      for (int i = 0; i < 5; i++) {
         double ang = i * (Math.PI * 2.0 / 5.0) - Math.PI / 2.0;
         double px = x + Math.cos(ang) * 0.35;
         double py = y + 1.1 + Math.sin(ang) * 0.35;
         // point spikes
         spawn(px, py, z, Math.cos(ang) * 0.8, Math.sin(ang) * 0.8, 0.0,
            0.45F, 0.28F, 0.02F, withAlpha(argb, 1.0F), (float) ang, 0.6F, 0.0F, 0.96F);
         // inner glow dots
         spawn(x + Math.cos(ang) * 0.12, y + 1.1 + Math.sin(ang) * 0.12, z, 0, 0.3, 0,
            0.5F, 0.16F, 0.05F, withAlpha(0xFFFFFFFF, 0.9F), 0.0F, 0.0F, 0.0F, 1.0F);
      }
      // center flash
      Spark core = spawn(x, y + 1.1, z, 0, 0, 0, 0.3F, 0.4F, 0.85F, withAlpha(argb, 0.95F), 0.0F, 0.8F, 0.0F, 1.0F);
   }

   // ---- Blue Swirl: rotating vortex around target ----
   private static void blueSwirl(double x, double y, double z, int argb) {
      int arms = 3;
      for (int arm = 0; arm < arms; arm++) {
         for (int i = 0; i < 34; i++) {
            double t = i / 34.0;
            double ang = arm * (Math.PI * 2.0 / arms) + t * Math.PI * 2.2;
            double r = 0.2 + t * 1.0;
            double px = x + Math.cos(ang) * r;
            double py = y + 0.3 + t * 1.5;
            double pz = z + Math.sin(ang) * r;
            // tangential velocity keeps the vortex spinning
            double tx = -Math.sin(ang) * 2.2;
            double tz = Math.cos(ang) * 2.2;
            spawn(px, py, pz, tx, rnd(0.1, 0.5), tz,
               rnd(0.5, 0.8), rnd(0.08, 0.16), 0.0F, withAlpha(argb, (float) (1.0 - t * 0.4)),
               (float) ang, (float) rnd(2.0, 5.0), 0.3F, 0.94F);
         }
      }
   }
   // ---- Cyan Droplet: sparkling droplets scatter in all directions ----
   private static void cyanDroplet(double x, double y, double z, int argb) {
      for (int i = 0; i < 150; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double el = rnd(-0.9, 1.2);
         double sp = rnd(1.5, 4.5);
         double vx = Math.cos(a) * Math.cos(el) * sp;
         double vy = Math.sin(el) * sp;
         double vz = Math.sin(a) * Math.cos(el) * sp;
         int col = RNG.nextBoolean() ? argb : 0xFFFFFFFF;
         spawn(x, y + 1.0, z, vx, vy, vz,
            rnd(0.5, 1.1), rnd(0.05, 0.11), 0.0F, withAlpha(col, (float) rnd(0.6, 1.0)),
            RNG.nextFloat() * 3.0F, rnd(-4.0F, 4.0F), 6.0F, 0.985F);
      }
   }

   // ---- Golden Ring: expanding energy ring around the enemy ----
   private static void goldenRing(double x, double y, double z, int argb) {
      // caller anchors at chest — drop back to knee height for the ring
      y -= 0.6;
      int count = 130;
      for (int i = 0; i < count; i++) {
         double a = (double) i / count * Math.PI * 2.0;
         double px = x + Math.cos(a) * 0.45;
         double pz = z + Math.sin(a) * 0.45;
         double vx = Math.cos(a) * 3.0;
         double vz = Math.sin(a) * 3.0;
         int col = (i % 3 == 0) ? withAlpha(0xFFFF8C00, 0.95F) : argb;
         spawn(px, y + 0.06, pz, vx, rnd(0.2, 0.7), vz,
            rnd(0.45, 0.7), rnd(0.10, 0.16), 0.0F, col,
            (float) a, rnd(1.0F, 3.0F), 1.5F, 0.9F);
      }
   }

   // ---- Shadow Slash: purple-black claw scratch ----
   private static void shadowSlash(double x, double y, double z, int argb) {
      int claws = 3;
      for (int c = 0; c < claws; c++) {
         double baseAng = -0.5 + c * 0.45; // diagonal slashes
         double offY = 1.5 - c * 0.35;
         for (int i = 0; i < 10; i++) {
            double t = i / 9.0;
            double px = x + (t - 0.5) * 1.6 * Math.cos(baseAng);
            double py = y + offY + (t - 0.5) * 0.5;
            double pz = z + (t - 0.5) * 1.6 * Math.sin(baseAng);
            int col = (i % 2 == 0) ? withAlpha(0xFF2E0A4E, 0.95F) : withAlpha(0xFF9B30FF, 0.9F);
            Spark s = spawn(px, py, pz, rnd(-0.2, 0.2), rnd(0.0, 0.4), rnd(-0.2, 0.2),
               rnd(0.25, 0.4), 0.16F, 0.04F, col,
               (float) baseAng + Math.PI / 2.0F, 0.0F, 0.5F, 0.9F);
            if (s != null) s.streak = true;
         }
         // claw tip flash
         spawn(x + Math.cos(baseAng) * 0.8, y + offY + 0.25, z + Math.sin(baseAng) * 0.8,
            0, 0.5, 0, 0.15F, 0.2F, 0.05F, withAlpha(0xFFDDA0FF, 0.9F), 0.0F, 0.0F, 0.0F, 1.0F);
      }
   }

   // ---- Hellfire Burst: fire sparks + light smoke ----
   private static void hellfireBurst(double x, double y, double z, int argb) {
      for (int i = 0; i < 90; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double el = rnd(-0.2, 1.3);
         double sp = rnd(1.0, 3.5);
         int col = RNG.nextInt(3) == 0 ? withAlpha(0xFFFFD980, 0.95F)
            : RNG.nextBoolean() ? argb : withAlpha(0xFFFF4500, 0.9F);
         spawn(x, y + 0.8, z,
            Math.cos(a) * Math.cos(el) * sp, Math.sin(el) * sp + rnd(0.5, 1.5), Math.sin(a) * Math.cos(el) * sp,
            rnd(0.3, 0.6), rnd(0.09, 0.17), 0.0F, col,
            RNG.nextFloat() * 3.0F, rnd(-3.0F, 3.0F), 2.5F, 0.93F);
      }
      // light smoke puffs (slow, gray, grow)
      for (int i = 0; i < 8; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.3, y + 1.0, z + Math.sin(a) * 0.3,
            Math.cos(a) * 0.4, rnd(0.6, 1.2), Math.sin(a) * 0.4,
            rnd(0.9, 1.4), 0.14F, 0.4F, withAlpha(0xFF555566, 0.45F),
            0.0F, rnd(-0.5F, 0.5F), -0.3F, 0.96F);
      }
   }

   // ---- Electric Spark: zig-zag static burst ----
   private static void electricSpark(double x, double y, double z, int argb) {
      int bolts = 5;
      for (int b = 0; b < bolts; b++) {
         double ang = RNG.nextDouble() * Math.PI * 2.0;
         double px = x;
         double py = y + 1.0;
         double pz = z;
         double dirX = Math.cos(ang);
         double dirZ = Math.sin(ang);
         for (int i = 0; i < 6; i++) {
            double step = 0.16 + i * 0.03;
            px = x + dirX * step + rnd(-0.07, 0.07);
            py += rnd(-0.02, 0.09);
            pz = z + dirZ * step + rnd(-0.07, 0.07);
            Spark s = spawn(px, py, pz, dirX * rnd(0.5, 1.2), rnd(-0.3, 0.6), dirZ * rnd(0.5, 1.2),
               rnd(0.12, 0.22), rnd(0.07, 0.12), 0.0F, withAlpha(0xFFBFEFFF, 0.95F),
               0.0F, 0.0F, 0.0F, 0.88F);
            if (s != null) s.streak = true;
         }
      }
   }

   // ---- Emerald Shard: sharp green fragments ----
   private static void emeraldShard(double x, double y, double z, int argb) {
      for (int i = 0; i < 42; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double el = rnd(-0.4, 1.1);
         double sp = rnd(2.0, 5.0);
         Spark s = spawn(x, y + 1.0, z,
            Math.cos(a) * Math.cos(el) * sp, Math.sin(el) * sp + 1.0, Math.sin(a) * Math.cos(el) * sp,
            rnd(0.6, 1.4), rnd(0.10, 0.2), 0.0F, withAlpha(argb, (float) rnd(0.8, 1.0)),
            RNG.nextFloat() * 6.0F, rnd(-8.0F, 8.0F), 7.0F, 0.99F);
         if (s != null) s.streak = true;
      }
   }   // ---- Thunder Bolt: white flash circle + cascading sparks raining down ----
   private static void thunderBolt(double x, double y, double z, int argb) {
      // Big white impact circle at chest height — the core of the effect.
      spawn(x, y, z, 0, 0, 0, 0.30F, 0.55F, 1.6F, withAlpha(0xFFFFFFFF, 0.95F), 0.0F, 0.0F, 0.0F, 1.0F);
      spawn(x, y, z, 0, 0, 0, 0.22F, 0.9F, 0.2F, withAlpha(0xFFEAF6FF, 0.85F), 0.0F, 0.0F, 0.0F, 1.0F);

      // Expanding white ring (the "circle")
      int ring = 72;
      for (int i = 0; i < ring; i++) {
         double a = (double) i / ring * Math.PI * 2.0;
         Spark s = spawn(x + Math.cos(a) * 0.3, y, z + Math.sin(a) * 0.3,
            Math.cos(a) * 4.8, rnd(-0.1, 0.1), Math.sin(a) * 4.8,
            rnd(0.35, 0.55), rnd(0.05, 0.10), 0.0F, withAlpha(0xFFFFFFFF, (float) rnd(0.8, 1.0)),
            (float) a, rnd(1.0F, 3.0F), 0.0F, 0.93F);
         if (s != null) s.streak = true;
      }

      // Fine sparks raining DOWN from above the circle ("effects down")
      for (int i = 0; i < 110; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double r = rnd(0.0, 1.3);
         Spark s = spawn(x + Math.cos(a) * r, y + rnd(0.8, 2.6), z + Math.sin(a) * r,
            rnd(-0.3, 0.3), rnd(-9.0, -4.0), rnd(-0.3, 0.3),
            rnd(0.35, 0.7), rnd(0.03, 0.08), 0.0F,
            withAlpha(RNG.nextBoolean() ? 0xFFFFFFFF : argb, (float) rnd(0.6, 1.0)),
            RNG.nextFloat() * 3.0F, rnd(-4.0F, 4.0F), 6.0F, 0.99F);
         if (s != null) s.streak = true;
      }

      // Ground ripple below the circle
      double groundY = y - 0.9;
      int gcount = 40;
      for (int i = 0; i < gcount; i++) {
         double a = (double) i / gcount * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.4, groundY, z + Math.sin(a) * 0.4,
            Math.cos(a) * 3.2, rnd(0.1, 0.6), Math.sin(a) * 3.2,
            rnd(0.25, 0.45), rnd(0.04, 0.09), 0.0F, withAlpha(0xFFEAF6FF, 0.7F),
            (float) a, 0.0F, 1.0F, 0.92F);
      }
   }

   // ---- Frost Nova: expanding icy ring + rising shards ----
   private static void frostNova(double x, double y, double z, int argb) {
      y -= 0.6; // ground-level ring, caller is chest-anchored
      int count = 84;
      for (int i = 0; i < count; i++) {
         double a = (double) i / count * Math.PI * 2.0;
         Spark s = spawn(x + Math.cos(a) * 0.3, y + 0.12, z + Math.sin(a) * 0.3,
            Math.cos(a) * 4.5, rnd(0.1, 0.5), Math.sin(a) * 4.5,
            rnd(0.5, 0.8), rnd(0.08, 0.15), 0.0F, withAlpha(argb, (float) rnd(0.7, 1.0)),
            (float) a, rnd(1.0F, 3.0F), 1.0F, 0.93F);
         if (s != null) s.streak = true;
      }
      for (int i = 0; i < 10; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.25, y + 0.3, z + Math.sin(a) * 0.25,
            Math.cos(a) * rnd(0.5, 1.5), rnd(1.5, 3.0), Math.sin(a) * rnd(0.5, 1.5),
            rnd(0.5, 0.9), rnd(0.10, 0.18), 0.02F, withAlpha(0xFFFFFFFF, (float) rnd(0.6, 0.95)),
            RNG.nextFloat() * 6.0F, rnd(-6.0F, 6.0F), 5.0F, 0.98F);
      }
   }

   // ---- Blood Impact: red spray + falling drips ----
   private static void bloodImpact(double x, double y, double z, int argb) {
      for (int i = 0; i < 110; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double el = rnd(-0.3, 1.1);
         double sp = rnd(1.5, 4.0);
         int col = RNG.nextInt(4) == 0 ? withAlpha(0xFF7A0C12, 0.95F) : argb;
         Spark s = spawn(x, y + 1.0, z,
            Math.cos(a) * Math.cos(el) * sp, Math.sin(el) * sp, Math.sin(a) * Math.cos(el) * sp,
            rnd(0.5, 0.9), rnd(0.07, 0.15), 0.02F, col,
            0.0F, 0.0F, 9.0F, 0.97F);
         if (s != null) s.streak = true;
      }
      // mist
      for (int i = 0; i < 6; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.2, y + 1.1, z + Math.sin(a) * 0.2,
            Math.cos(a) * 0.5, rnd(0.2, 0.8), Math.sin(a) * 0.5,
            rnd(0.6, 0.9), 0.12F, 0.34F, withAlpha(0xFF8B0000, 0.35F), 0.0F, rnd(-0.4F, 0.4F), -0.2F, 0.96F);
      }
   }

   // ---- Void Rift: dark vortex implosion then burst ----
   private static void voidRift(double x, double y, double z, int argb) {
      // imploding ring
      for (int i = 0; i < 60; i++) {
         double a = (double) i / 22 * Math.PI * 2.0;
         double r0 = 1.6;
         Spark s = spawn(x + Math.cos(a) * r0, y + 1.0 + rnd(-0.3, 0.3), z + Math.sin(a) * r0,
            -Math.cos(a) * 3.2, 0.0, -Math.sin(a) * 3.2,
            0.45F, rnd(0.08, 0.16), 0.0F, withAlpha(argb, (float) rnd(0.6, 0.95)),
            (float) a, rnd(2.0F, 6.0F), 0.0F, 0.97F);
         if (s != null) s.streak = true;
      }
      // core collapse flash
      spawn(x, y + 1.0, z, 0, 0, 0, 0.35F, 0.15F, 0.8F, withAlpha(0xFF1A0533, 0.9F), 0.0F, 1.5F, 0.0F, 1.0F);
      spawn(x, y + 1.0, z, 0, 0, 0, 0.25F, 0.5F, 0.1F, withAlpha(argb, 0.95F), 0.0F, 0.0F, 0.0F, 1.0F);
   }

   // ---- Holy Flash: golden cross of light + halo ----
   private static void holyFlash(double x, double y, double z, int argb) {
      // halo above
      int hc = 16;
      for (int i = 0; i < hc; i++) {
         double a = (double) i / hc * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.5, y + 2.1, z + Math.sin(a) * 0.5, 0, 0.15, 0,
            rnd(0.5, 0.8), rnd(0.06, 0.11), 0.0F, withAlpha(argb, (float) rnd(0.7, 1.0)),
            0.0F, 0.0F, 0.0F, 0.97F);
      }
      // vertical pillar
      for (int i = 0; i < 34; i++) {
         double t = i / 34.0;
         Spark s = spawn(x, y + 0.1 + t * 2.4, z, 0, 0.4, 0,
            rnd(0.3, 0.5), 0.14F - (float) t * 0.06F, 0.02F, withAlpha(0xFFFFF3B0, (float) (1.0 - t * 0.4)),
            0.0F, 0.0F, 0.0F, 0.98F);
         if (s != null) s.streak = true;
      }
      // flash
      spawn(x, y + 1.1, z, 0, 0, 0, 0.25F, 0.4F, 1.0F, withAlpha(0xFFFFFFFF, 0.85F), 0.0F, 0.0F, 0.0F, 1.0F);
   }

   // ---- Toxic Burst: green gas cloud + dripping acid ----
   private static void toxicBurst(double x, double y, double z, int argb) {
      for (int i = 0; i < 60; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         spawn(x + Math.cos(a) * 0.3, y + 0.6 + rnd(-0.2, 0.4), z + Math.sin(a) * 0.3,
            Math.cos(a) * rnd(0.4, 1.2), rnd(0.2, 0.9), Math.sin(a) * rnd(0.4, 1.2),
            rnd(0.7, 1.2), 0.16F, 0.5F, withAlpha(argb, (float) rnd(0.3, 0.6)),
            0.0F, rnd(-0.8F, 0.8F), -0.4F, 0.95F);
      }
      for (int i = 0; i < 34; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         Spark s = spawn(x, y + 1.2, z, Math.cos(a) * rnd(1.0, 2.5), rnd(-0.5, 0.3), Math.sin(a) * rnd(1.0, 2.5),
            rnd(0.5, 0.8), rnd(0.05, 0.1), 0.0F, withAlpha(0xFFAAFF50, 0.9F),
            0.0F, 0.0F, 8.0F, 0.97F);
         if (s != null) s.streak = true;
      }
   }

   // ---- Crystal Shatter: glass-like fragments explode outward ----
   private static void crystalShatter(double x, double y, double z, int argb) {
      for (int i = 0; i < 96; i++) {
         double a = RNG.nextDouble() * Math.PI * 2.0;
         double el = rnd(-0.6, 1.2);
         double sp = rnd(2.0, 5.5);
         int col = RNG.nextBoolean() ? argb : withAlpha(0xFFFFFFFF, 0.9F);
         Spark s = spawn(x, y + 1.0, z,
            Math.cos(a) * Math.cos(el) * sp, Math.sin(el) * sp + 0.8, Math.sin(a) * Math.cos(el) * sp,
            rnd(0.5, 1.0), rnd(0.08, 0.18), 0.02F, col,
            RNG.nextFloat() * 6.0F, rnd(-10.0F, 10.0F), 7.0F, 0.985F);
         if (s != null) s.streak = true;
      }
      spawn(x, y + 1.0, z, 0, 0, 0, 0.2F, 0.3F, 0.7F, withAlpha(0xFFFFFFFF, 0.9F), 0.0F, 0.0F, 0.0F, 1.0F);
   }
}
