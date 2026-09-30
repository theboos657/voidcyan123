package com.voidcyan.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom crit effects. Effect definitions live as JSON files in
 * config/voidcyan-crit-effects/ and are hot-reloaded when the folder changes.
 */
public final class CritEffectsManager {
   public static boolean enabled = false;

   private static final File EFFECTS_DIR = new File("config/voidcyan-crit-effects");
   private static final Gson GSON = new Gson();
   private static final Map<String, Effect> EFFECTS = new ConcurrentHashMap<>();
   private static long lastDirStamp = 0L;
   private static long lastCheck = 0L;
   private static long lastPlay = 0L;

   private CritEffectsManager() {
   }

   /** Public entry point — call when the local player lands a melee hit. */
   public static void onAttack(PlayerEntity attacker, World world, Entity target) {
      if (!enabled) return;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null || world == null || !world.isClient()) return;
      if (attacker != mc.player) return;
      checkReload();
      if (EFFECTS.isEmpty()) return;

      long now = System.currentTimeMillis();
      if (now - lastPlay < 50) return; // global throttle
      lastPlay = now;

      double px = target != null ? target.getX() : attacker.getX();
      double py = target != null ? target.getY() : attacker.getY();
      double pz = target != null ? target.getZ() : attacker.getZ();
      // Effects anchor at the target's chest (feet + half height) so ring/
      // swirl/vortex presets wrap the body instead of hugging the floor.
      double anchor = target != null ? target.getHeight() * 0.5 : 1.0;
      py += anchor;
      int played = 0;
      boolean crit = isCrit(attacker);
      if (crit) flashAt = now;

      // Build the set of trigger keys this attack satisfies. An effect fires
      // ONLY if its chosen trigger is in this set — so "armor stand hit"
      // effects stay silent on players, "player crit" stays silent on mobs, etc.
      String kind = target instanceof net.minecraft.entity.decoration.ArmorStandEntity ? "armor_stand"
         : target instanceof PlayerEntity ? "player" : "mob";
      java.util.Set<String> allowed = new java.util.HashSet<>();
      allowed.add("hit");                      // generic: any hit
      allowed.add("hit_" + kind);              // e.g. hit_player
      if (crit) {
         allowed.add("crit");                  // generic: any crit
         allowed.add("crit_" + kind);          // e.g. crit_player
      }

      for (Effect e : EFFECTS.values()) {
         if (!e.enabled) continue;
         String trig = e.trigger == null ? "hit" : e.trigger;
         if (!allowed.contains(trig)) continue;
         if (e.requireCrit && !crit) continue;
         if (now - e.lastFired < e.cooldownMs) continue;
         e.lastFired = now;
         e.play(mc.world, px, py, pz);
         played++;
      }
   }

   /** Generic trigger fire — used for kill / death / xp / totem / crystal triggers. */
   public static void fire(String trigger, World world, double x, double y, double z) {
      fire(new String[]{trigger}, world, x, y, z);
   }

   /** Fires effects whose trigger matches ANY of the given keys. */
   public static void fire(String[] triggers, World world, double x, double y, double z) {
      if (!enabled || world == null || triggers == null || triggers.length == 0) return;
      checkReload();
      if (EFFECTS.isEmpty()) return;
      java.util.Set<String> keys = new java.util.HashSet<>(java.util.Arrays.asList(triggers));
      long now = System.currentTimeMillis();
      for (Effect e : EFFECTS.values()) {
         if (!e.enabled) continue;
         String trig = e.trigger == null ? "hit" : e.trigger;
         if (!keys.contains(trig)) continue;
         if (now - e.lastFired < e.cooldownMs) continue;
         e.lastFired = now;
         e.play(world, x, y, z);
      }
   }

   /** Fired when the local player kills another player (or mob). */
   public static void onKill(MinecraftClient mc) {
      if (!enabled || mc == null || mc.world == null || mc.player == null) return;
      Entity victim = VoidCyanClient.lastAttackedEntity;
      double x;
      double y;
      double z;
      if (victim != null && System.currentTimeMillis() - VoidCyanClient.lastAttackTime < 5000L) {
         x = victim.getX();
         y = victim.getY() + victim.getHeight() * 0.5;
         z = victim.getZ();
      } else {
         x = mc.player.getX();
         y = mc.player.getY() + 1.0;
         z = mc.player.getZ();
      }
      killAt = System.currentTimeMillis();
      flashAt = killAt;
      boolean playerVictim = victim instanceof PlayerEntity;
      fire(playerVictim ? new String[]{"kill_player", "kill"} : new String[]{"kill_mob", "kill"},
         mc.world, x, y, z);
   }

   /** Fired when the local player dies (death animation). */
   public static void onDeath(MinecraftClient mc) {
      if (!enabled || mc == null || mc.world == null || mc.player == null) return;
      fire("death", mc.world, mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ());
   }

   /** Fired when the local player breaks an end crystal (crystal trigger). */
   public static void onCrystalBreak(World world, double x, double y, double z) {
      fire("crystal", world, x, y, z);
   }

   /** Fired when any player pops a totem. */
   public static void onTotemPop(PlayerEntity player) {
      if (!enabled || player == null) return;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null) return;
      fire("totem", mc.world, player.getX(), player.getY() + 1.0, player.getZ());
   }

   private static boolean isCrit(PlayerEntity p) {
      return p.fallDistance > 0.0F && !p.isOnGround() && !p.isTouchingWater() && !p.hasVehicle() && !p.isSprinting() && !p.isUsingSpyglass();
   }

   // ================= Screen effects (no particles) =================

   private static long flashAt = 0L;
   private static long killAt = 0L;

   /** Edge flash on crit/kill plus a punchy KILL banner; drawn every HUD frame. */
   public static void renderOverlay(net.minecraft.client.gui.DrawContext ctx) {
      if (!enabled) return;
      long now = System.currentTimeMillis();
      int w = ctx.getScaledWindowWidth();
      int h = ctx.getScaledWindowHeight();
      int rgb = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;
      float ft = (now - flashAt) / 380.0F;
      if (ft >= 0.0F && ft < 1.0F) {
         float k = (1.0F - ft) * (1.0F - ft);
         for (int i = 0; i < 24; i++) {
            int a = (int) (k * 120.0F * (1.0F - i / 24.0F));
            if (a <= 0) break;
            int c = a << 24 | rgb;
            ctx.fill(i, i, w - i, i + 1, c);
            ctx.fill(i, h - i - 1, w - i, h - i, c);
            ctx.fill(i, i + 1, i + 1, h - i - 1, c);
            ctx.fill(w - i - 1, i + 1, w - i, h - i - 1, c);
         }
      }

      float kt = (now - killAt) / 1300.0F;
      if (kt >= 0.0F && kt < 1.0F) {
         net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
         float pop = kt < 0.12F ? 1.0F + (0.12F - kt) * 8.0F : 1.0F;
         int a = (int) (Math.min(1.0F, (1.0F - kt) * 2.5F) * 255.0F);
         if (a > 4) {
            String txt = "KILL";
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(w / 2.0F, h / 3.0F);
            ctx.getMatrices().scale(2.6F * pop, 2.6F * pop);
            ctx.drawCenteredTextWithShadow(tr, txt, 0, -4, a << 24 | rgb);
            ctx.getMatrices().popMatrix();
         }
      }
   }

   // ================= Loading =================

   public static int getEffectCount() {
      checkReload();
      return EFFECTS.size();
   }

   public static File getEffectsDir() {
      return EFFECTS_DIR;
   }

   private static void checkReload() {
      long now = System.currentTimeMillis();
      if (now - lastCheck < 1000L) return;
      lastCheck = now;
      long stamp = EFFECTS_DIR.isDirectory() ? EFFECTS_DIR.lastModified() : -1L;
      if (stamp == lastDirStamp) return;
      lastDirStamp = stamp;
      reload();
   }

   public static void reload() {
      EFFECTS.clear();
      if (!EFFECTS_DIR.exists()) {
         EFFECTS_DIR.mkdirs();
         writeSample("ring_example.json", sampleRing());
         writeSample("burst_example.json", sampleBurst());
         writeSample("model_example.json", sampleModel());
         writeSample("bbmodel_example.json", sampleBbModel());
         writeSample("_PRESETS_README.txt", presetList());
      }

      File[] files = EFFECTS_DIR.listFiles((d, n) -> n.toLowerCase().endsWith(".json"));
      if (files == null) return;
      for (File f : files) {
         try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(text).getAsJsonObject();
            EFFECTS.put(f.getName(), Effect.fromJson(obj));
         } catch (Exception e) {
            System.out.println("[VoidCyan] Bad crit effect file " + f.getName() + ": " + e.getMessage());
         }
      }
   }

   private static void writeSample(String name, String json) {
      try {
         Files.writeString(new File(EFFECTS_DIR, name).toPath(), json);
      } catch (IOException ignored) {
      }
   }

   // ================= Effects =================

   public static class Effect {
      boolean enabled = true;
      boolean requireCrit = false;
      /** When the effect fires: hit, crit, kill, death, xp, totem, crystal. */
      String trigger = "hit";
      int cooldownMs = 150;
      String shape = "ring";
      String particle = "dust";
      String color = "#00F5FF";
      float scale = 1.0F;
      int count = 24;
      float radius = 1.1F;
      float height = 1.8F;
      float speed = 0.12F;
      String sound = "";
      float volume = 1.0F;
      float pitch = 1.0F;
      java.util.List<FxPart> parts; // custom particle model built in the 3D modeler
      String model = "";      // OBJ file name in config/voidcyan-crit-effects/
      float modelScale = 1.0F;
      float spinSpeed = 0.0F; // degrees per tick around Y
      float durationMs = 500.0F; // how long the model stays visible
      int color2 = -1;        // optional second tint (unused by flat fill, reserved)
      boolean solidFill = true; // false = wireframe-style translucent fill
      transient long lastFired = 0L;
      transient boolean dustMode = true;
      transient DustParticleEffect dust;
      transient ParticleEffect resolved;

      static Effect fromJson(JsonObject o) {
         Effect e = GSON.fromJson(o, Effect.class);
         if (e.shape == null) e.shape = "ring";
         e.dustMode = e.particle == null || e.particle.isEmpty() || e.particle.equals("dust");
         if (e.dustMode) {
            int rgb = parseColor(e.color);
            e.dust = new DustParticleEffect(rgb, Math.max(0.1F, e.scale));
         } else {
            try {
               ParticleType<?> type = Registries.PARTICLE_TYPE.get(Identifier.of(e.particle.contains(":") ? e.particle : "minecraft:" + e.particle));
               e.resolved = type instanceof ParticleEffect pe ? pe : null;
            } catch (Exception ex) {
               e.resolved = null;
            }
            if (e.resolved == null) {
               e.dustMode = true;
               int rgb = parseColor(e.color);
               e.dust = new DustParticleEffect(rgb, Math.max(0.1F, e.scale));
            }
         }
         return e;
      }

      void play(World world, double cx, double cy, double cz) {
         if (this.model != null && !this.model.isEmpty()) {
            ModelInstance.spawn(world, cx, cy, cz, this);
         }

         if (this.parts != null && !this.parts.isEmpty()) {
            CritBillboardFX.spawnCustom(this.parts, cx, cy, cz);
            if (this.sound != null && !this.sound.isEmpty()) {
               try {
                  SoundEvent se = Registries.SOUND_EVENT.get(Identifier.of(this.sound.contains(":") ? this.sound : "minecraft:" + this.sound));
                  if (se != null) {
                     MinecraftClient.getInstance().getSoundManager()
                        .play(new PositionedSoundInstance(se, SoundCategory.PLAYERS, this.volume, this.pitch, net.minecraft.util.math.random.Random.create(), cx, cy, cz));
                  }
               } catch (Exception ignored) {
               }
            }

            return;
         }

         // Billboard FX presets (blue_comet, golden_ring, ...) skip the particle path.
         // cy already arrives chest-anchored; don't push it back toward the feet.
         if (CritBillboardFX.spawnPreset(this.shape, cx, cy, cz,
            0xFF000000 | (parseColor(this.color) & 0x00FFFFFF))) {
            return;
         }

         ParticleEffect pe = this.dustMode ? this.dust : this.resolved;
         if (pe == null) return;
         cy = cy + this.height * 0.5;
         java.util.Random r = new java.util.Random();
         final boolean rainbow = this.dustMode && "rainbow".equalsIgnoreCase(this.color);
         final ParticleEffect fixedPe = pe;
         final float dustScale = Math.max(0.1F, this.scale);
         java.util.function.IntFunction<ParticleEffect> P = i -> rainbow
            ? new DustParticleEffect(java.awt.Color.HSBtoRGB((i % 24) / 24.0F, 0.85F, 1.0F) & 0xFFFFFF, dustScale)
            : fixedPe;

         switch (this.shape == null ? "ring" : this.shape) {
            case "burst" -> {
               for (int i = 0; i < this.count; i++) {
                  double a = r.nextDouble() * Math.PI * 2.0;
                  double el = r.nextDouble() * Math.PI - Math.PI / 2.0;
                  double vx = Math.cos(a) * Math.cos(el) * this.speed;
                  double vy = Math.sin(el) * this.speed;
                  double vz = Math.sin(a) * Math.cos(el) * this.speed;
                  world.addParticleClient(P.apply(i), cx, cy, cz, vx, vy, vz);
               }
            }
            case "column" -> {
               for (int i = 0; i < this.count; i++) {
                  double a = r.nextDouble() * Math.PI * 2.0;
                  double y = cy - this.height * 0.5 + r.nextDouble() * this.height;
                  double rr = 0.25 + r.nextDouble() * 0.15;
                  world.addParticleClient(P.apply(i), cx + Math.cos(a) * rr, y, cz + Math.sin(a) * rr, 0.0, 0.02 + r.nextDouble() * 0.05, 0.0);
               }
            }
            case "spiral" -> {
               for (int i = 0; i < this.count; i++) {
                  double t = (double) i / this.count;
                  double a = t * Math.PI * 6.0;
                  double y = cy - this.height * 0.5 + t * this.height;
                  world.addParticleClient(P.apply(i), cx + Math.cos(a) * this.radius, y, cz + Math.sin(a) * this.radius, 0.0, 0.03, 0.0);
               }
            }
            case "halo" -> {
               double hy = cy + this.height * 0.5;
               for (int i = 0; i < this.count; i++) {
                  double a = (double) i / this.count * Math.PI * 2.0;
                  world.addParticleClient(P.apply(i), cx + Math.cos(a) * this.radius, hy, cz + Math.sin(a) * this.radius, 0.0, 0.01, 0.0);
               }
            }
            case "lightning" -> {
               double y = cy - this.height * 0.5;
               double ox = 0.0;
               double oz = 0.0;
               int segs = Math.max(4, this.count / 3);
               for (int i = 0; i < segs; i++) {
                  ox += (r.nextDouble() - 0.5) * 0.5;
                  oz += (r.nextDouble() - 0.5) * 0.5;
                  y += this.height / segs;
                  world.addParticleClient(P.apply(i), cx + ox, y, cz + oz, 0.0, 0.0, 0.0);
                  world.addParticleClient(P.apply(i), cx + ox * 0.6, y - 0.15, cz + oz * 0.6, 0.0, 0.0, 0.0);
               }
            }
            case "helix" -> {
               for (int i = 0; i < this.count; i++) {
                  double t = (double) i / this.count;
                  double a = t * Math.PI * 8.0;
                  double y = cy - this.height * 0.5 + t * this.height;
                  world.addParticleClient(P.apply(i), cx + Math.cos(a) * this.radius, y, cz + Math.sin(a) * this.radius, 0.0, 0.02, 0.0);
                  world.addParticleClient(P.apply(i + 1), cx + Math.cos(a + Math.PI) * this.radius, y, cz + Math.sin(a + Math.PI) * this.radius, 0.0, 0.02, 0.0);
               }
            }
            case "shockwave" -> {
               for (int k = 0; k < 3; k++) {
                  for (int i = 0; i < this.count; i++) {
                     double a = (double) i / this.count * Math.PI * 2.0;
                     double sp = this.speed * (1.0 + k * 0.7);
                     double rr = this.radius * (0.3 + 0.25 * k);
                     world.addParticleClient(P.apply(i + k * 5), cx + Math.cos(a) * rr, cy - this.height * 0.4, cz + Math.sin(a) * rr, Math.cos(a) * sp, 0.005, Math.sin(a) * sp);
                  }
               }
            }
            case "fountain" -> {
               for (int i = 0; i < this.count; i++) {
                  double a = r.nextDouble() * Math.PI * 2.0;
                  double out = r.nextDouble() * this.speed;
                  world.addParticleClient(P.apply(i), cx, cy - this.height * 0.3, cz, Math.cos(a) * out, 0.18 + r.nextDouble() * 0.2, Math.sin(a) * out);
               }
            }
            default -> { // ring
               for (int i = 0; i < this.count; i++) {
                  double a = (double) i / this.count * Math.PI * 2.0;
                  double vx = Math.cos(a) * this.speed;
                  double vz = Math.sin(a) * this.speed;
                  world.addParticleClient(P.apply(i), cx + Math.cos(a) * this.radius, cy, cz + Math.sin(a) * this.radius, vx, 0.01, vz);
               }
            }
         }

         if (this.sound != null && !this.sound.isEmpty()) {
            try {
               SoundEvent se = Registries.SOUND_EVENT.get(Identifier.of(this.sound.contains(":") ? this.sound : "minecraft:" + this.sound));
               if (se != null) {
                  MinecraftClient.getInstance().getSoundManager()
                     .play(new PositionedSoundInstance(se, SoundCategory.PLAYERS, this.volume, this.pitch, net.minecraft.util.math.random.Random.create(), cx, cy, cz));
               }
            } catch (Exception ignored) {
            }
         }
      }
   }

   private static int parseColor(String hex) {
      try {
         String t = hex == null ? "" : hex.replace("#", "").trim();
         if (t.length() == 6) return (int) Long.parseLong(t, 16) & 0xFFFFFF;
      } catch (NumberFormatException ignored) {
      }
      return 0x00F5FF;
   }

   private static String sampleRing() {
      return """
         {
           "enabled": true,
           "requireCrit": false,
           "cooldownMs": 150,
           "shape": "ring",
           "particle": "dust",
           "color": "#00F5FF",
           "scale": 1.0,
           "count": 24,
           "radius": 1.1,
           "height": 1.8,
           "speed": 0.12,
           "sound": "",
           "volume": 1.0,
           "pitch": 1.0
         }
         """;
   }

   private static String presetList() {
      return """
         VOIDCYAN CRIT EFFECT PRESETS (use as "shape" in a .json effect)
         ================================================================
         blue_comet      fast cyan streak + glowing stardust trail
         blue_cross      glowing cross burst with expanding light rays
         blue_star       2D star flash that expands then dissolves
         blue_swirl      rotating energy vortex around the target
         cyan_droplet    sparkling energy droplets scattering outward
         golden_ring     expanding fire/energy ring at the enemy's feet
         shadow_slash    purple/black claw-scratch streaks
         hellfire_burst  fire sparks jumping out + light smoke
         electric_spark  short zig-zag static bolts
         emerald_shard   sharp green fragments flying out
         thunder_bolt    sky lightning bolt strikes the target + flash
         frost_nova      icy ring expands + rising frost shards
         blood_impact    red spray + falling drips + mist
         void_rift       dark vortex implodes then collapses
         holy_flash      golden pillar of light + halo + flash
         toxic_burst     green gas cloud + dripping acid
         crystal_shatter glass fragments explode outward

         Also still supported:
         ring / burst / column / spiral / halo / lightning  (vanilla-particle shapes)
         helix / shockwave / fountain                        (new vanilla-particle shapes)
         "color": "rainbow"                                   (cycles hues per particle)
         model + "model": "file.obj" or "file.bbmodel"       (your 3D models)

         All presets use "color" from the JSON and the global throttle.
         "trigger" chooses EXACTLY when it fires — the effect is disabled
         for everything else:
           hit / hit_player / hit_mob / hit_armor_stand
           crit / crit_player / crit_mob
           kill / kill_player / kill_mob
           death (you die), xp (orb pickup), totem (pop), crystal (break)
         Stack multiple .json files to combine effects on one event.
         """;
   }

   private static String sampleBbModel() {
      return """
         {
           \"enabled\": true,
           \"requireCrit\": false,
           \"cooldownMs\": 300,
           \"shape\": \"model\",
           \"model\": \"mymodel.bbmodel\",
           \"modelScale\": 1.0,
           \"spinSpeed\": 0.0,
           \"durationMs\": 600,
           \"color\": \"#00F5FF\",
           \"sound\": \"\",
           \"volume\": 1.0,
           \"pitch\": 1.0
         }
         """;
   }

   private static String sampleModel() {
      return """
         {
           \"enabled\": true,
           \"requireCrit\": false,
           \"cooldownMs\": 300,
           \"shape\": \"model\",
           \"model\": \"mysword.obj\",
           \"modelScale\": 1.0,
           \"spinSpeed\": 0.0,
           \"durationMs\": 600,
           \"color\": \"#00F5FF\",
           \"sound\": \"minecraft:entity.player.attack.crit\",
           \"volume\": 0.9,
           \"pitch\": 1.1
         }
         """;
   }

   private static String sampleBurst() {
      return """
         {
           "enabled": true,
           "requireCrit": false,
           "cooldownMs": 200,
           "shape": "burst",
           "particle": "dust",
           "color": "#FF2600",
           "scale": 1.3,
           "count": 32,
           "radius": 1.0,
           "height": 1.5,
           "speed": 0.2,
           "sound": "minecraft:entity.player.attack.crit",
           "volume": 0.8,
           "pitch": 1.2
         }
         """;
   }

   // ================= Model playback (OBJ crit models) =================

   private static final List<ModelInstance> ACTIVE = new ArrayList<>();

   public static final class ModelInstance {
      public final double x;
      public final double y;
      public final double z;
      public final Effect effect;
      public final ObjModel mesh;
      public final long startTime;

      private ModelInstance(World world, double x, double y, double z, Effect effect) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.effect = effect;
         this.startTime = System.currentTimeMillis();
         String name = effect.model.toLowerCase();
         File f = new File(EFFECTS_DIR, effect.model);
         if (!f.isFile()) {
            f = new File(EFFECTS_DIR, effect.model + (name.endsWith(".obj") || name.endsWith(".bbmodel") ? "" : name.endsWith(".") ? "obj" : ".obj"));
         }
         if (f.isFile() && f.getName().toLowerCase().endsWith(".bbmodel")) {
            this.mesh = BbModel.toObj(BbModel.get(f));
         } else {
            this.mesh = ObjModel.get(f);
         }
      }

      private static void spawn(World world, double x, double y, double z, Effect effect) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world == null) return;
         ModelInstance inst = new ModelInstance(world, x, y, z, effect);
         if (inst.mesh == null) {
            System.out.println("[VoidCyan] Crit model not found/empty: " + effect.model);
            return;
         }
         ACTIVE.add(inst);
      }

      public boolean isAlive() {
         return System.currentTimeMillis() - this.startTime < (long) this.effect.durationMs;
      }

      /** 0..1 fade progress. */
      public float fade() {
         float t = (float) (System.currentTimeMillis() - this.startTime) / Math.max(1.0F, this.effect.durationMs);
         return Math.max(0.0F, Math.min(1.0F, t));
      }

      public float spinAngle() {
         return this.effect.spinSpeed * (System.currentTimeMillis() - this.startTime) / 50.0F;
      }
   }

   /** Called every frame from the world renderer. Prunes expired instances. */
   public static List<ModelInstance> getActiveModels() {
      ACTIVE.removeIf(m -> !m.isAlive());
      return ACTIVE;
   }

}
