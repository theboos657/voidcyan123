package com.voidcyan.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.voidcyan.client.CritBillboardFX;
import com.voidcyan.client.CritEffectsManager;
import com.voidcyan.client.FxPart;
import com.voidcyan.client.VoidCyanClient;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** 3D modeler for particle effects: compose shapes of particles, tune motion, preview live, save as an effect JSON. */
public class ParticleModelerScreen extends Screen {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String[] TRIGGERS = {"hit", "crit", "kill", "death", "totem", "crystal", "xp"};
   private static final String[] PROPS = {"X", "Y", "Z", "Size X", "Size Y", "Size Z", "Count", "Particle size", "Outward", "Rise", "Spin", "Life", "Gravity", "Rot X", "Rot Y", "Rot Z"};
   private static final float[] MIN = {-2, -2, -2, 0.05F, 0.05F, 0.05F, 4, 0.03F, -4, -3, -12, 0.2F, -3, -180, -180, -180};
   private static final float[] MAX = {2, 2, 2, 2, 2, 2, 300, 0.5F, 6, 5, 12, 3, 3, 180, 180, 180};
   private static final int LEFT_W = 172;
   private static final int RIGHT_W = 196;

   private final Screen parent;
   private final List<FxPart> parts = new ArrayList<>();
   private int sel = 0;
   private String name = "my_effect";
   private boolean nameFocus = false;
   private int trigger = 0;
   private String status = "";
   private long statusUntil = 0L;

   private float yaw = 35.0F;
   private float pitch = 22.0F;
   private float zoom = 1.0F;
   private boolean rotating = false;
   private boolean panning = false;
   private float panX = 0.0F;
   private float panY = 0.0F;
   private int tool = 0; // 0 move, 1 rotate, 2 scale
   private int dragAxis = -1;
   private boolean animated = true;
   private List<CritBillboardFX.Spark> sim = new ArrayList<>();
   private long lastStep = 0L;
   private long simEmptySince = 0L;

   private int dragProp = -1;
   private int propScroll = 0;
   private ColorPickerModal picker;
   private List<String> files = new ArrayList<>();
   private final List<int[]> hits = new ArrayList<>();

   public ParticleModelerScreen(Screen parent) {
      super(Text.literal("Particle Modeler"));
      this.parent = parent;
      this.parts.add(new FxPart("ring"));
      this.refreshFiles();
      this.restart();
   }

   private FxPart cur() {
      return this.parts.isEmpty() ? null : this.parts.get(Math.max(0, Math.min(this.sel, this.parts.size() - 1)));
   }

   private void status(String s) {
      this.status = s;
      this.statusUntil = System.currentTimeMillis() + 2600L;
   }

   private void restart() {
      this.sim = CritBillboardFX.buildCustom(this.parts);
      this.lastStep = System.currentTimeMillis();
      this.simEmptySince = 0L;
   }

   private float get(FxPart p, int i) {
      return switch (i) {
         case 0 -> p.x; case 1 -> p.y; case 2 -> p.z;
         case 3 -> p.sx; case 4 -> p.sy; case 5 -> p.sz;
         case 6 -> p.count; case 7 -> p.size; case 8 -> p.outward;
         case 9 -> p.rise; case 10 -> p.spin; case 11 -> p.life;
         case 12 -> p.gravity;
         case 13 -> p.rx; case 14 -> p.ry;
         default -> p.rz;
      };
   }

   private void set(FxPart p, int i, float v) {
      switch (i) {
         case 0 -> p.x = v; case 1 -> p.y = v; case 2 -> p.z = v;
         case 3 -> p.sx = v; case 4 -> p.sy = v; case 5 -> p.sz = v;
         case 6 -> p.count = Math.round(v); case 7 -> p.size = v; case 8 -> p.outward = v;
         case 9 -> p.rise = v; case 10 -> p.spin = v; case 11 -> p.life = v;
         case 12 -> p.gravity = v;
         case 13 -> p.rx = v; case 14 -> p.ry = v;
         default -> p.rz = v;
      }
   }

   // ---------- files ----------

   private File dir() {
      File d = CritEffectsManager.getEffectsDir();
      if (!d.isDirectory()) d.mkdirs();
      return d;
   }

   private void refreshFiles() {
      this.files.clear();
      File[] fs = this.dir().listFiles((d, n) -> n.toLowerCase().endsWith(".json"));
      if (fs == null) return;
      for (File f : fs) {
         try {
            JsonObject o = JsonParser.parseString(Files.readString(f.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (o.has("parts")) this.files.add(f.getName().replaceAll("\\.json$", ""));
         } catch (Exception ignored) {
         }
      }
   }

   private void save() {
      try {
         JsonObject o = new JsonObject();
         o.addProperty("enabled", true);
         o.addProperty("trigger", TRIGGERS[this.trigger]);
         o.addProperty("requireCrit", false);
         o.addProperty("cooldownMs", 250);
         o.addProperty("shape", "custom");
         o.addProperty("color", "#00F5FF");
         o.addProperty("sound", "");
         o.addProperty("volume", 1.0);
         o.addProperty("pitch", 1.0);
         JsonArray arr = new JsonArray();
         for (FxPart p : this.parts) arr.add(GSON.toJsonTree(p));
         o.add("parts", arr);
         Files.writeString(new File(this.dir(), this.name + ".json").toPath(), GSON.toJson(o), StandardCharsets.UTF_8);
         CritEffectsManager.reload();
         this.refreshFiles();
         this.status("Saved " + this.name + " (fires on " + TRIGGERS[this.trigger] + ")");
      } catch (Exception e) {
         this.status("Save failed: " + e.getMessage());
      }
   }

   private void load(String n) {
      try {
         JsonObject o = JsonParser.parseString(Files.readString(new File(this.dir(), n + ".json").toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
         this.parts.clear();
         for (var e : o.getAsJsonArray("parts")) this.parts.add(GSON.fromJson(e, FxPart.class));
         String t = o.has("trigger") ? o.get("trigger").getAsString() : "hit";
         this.trigger = 0;
         for (int i = 0; i < TRIGGERS.length; i++) if (TRIGGERS[i].equals(t)) this.trigger = i;
         this.name = n;
         this.sel = 0;
         this.restart();
         this.status("Loaded " + n);
      } catch (Exception e) {
         this.status("Load failed");
      }
   }

   // ---------- ui helpers ----------

   private void btn(DrawContext c, int x, int y, int w, int h, String label, boolean active, int mx, int my, int id) {
      boolean hov = GuiStyle.inRect(mx, my, x, y, w, h);
      int pr = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;
      GuiStyle.roundedBordered(c, x, y, w, h, 4, active ? 0x66000000 | pr : hov ? 0x44FFFFFF : 0x24FFFFFF, active ? 0xFF000000 | pr : 0x33FFFFFF);
      float sc = this.textRenderer.getWidth(label) > w - 6 ? Math.max(0.55F, (w - 6) / (float) this.textRenderer.getWidth(label)) : 1.0F;
      c.getMatrices().pushMatrix();
      c.getMatrices().translate(x + (w - this.textRenderer.getWidth(label) * sc) / 2.0F, y + (h - 8 * sc) / 2.0F);
      c.getMatrices().scale(sc, sc);
      GuiStyle.text(c, this.textRenderer, label, 0, 0, 0xFFFFFFFF);
      c.getMatrices().popMatrix();
      this.hits.add(new int[]{x, y, w, h, id});
   }

   private int vpX() { return LEFT_W + 14; }
   private int vpW() { return this.width - LEFT_W - RIGHT_W - 28; }
   private int vpH() { return this.height - 50; }

   private float[] proj(float x, float y, float z) {
      double ya = Math.toRadians(this.yaw);
      double pa = Math.toRadians(this.pitch);
      float rx = (float) (x * Math.cos(ya) - z * Math.sin(ya));
      float rz = (float) (x * Math.sin(ya) + z * Math.cos(ya));
      float ry = (float) (y * Math.cos(pa) - rz * Math.sin(pa));
      float rz2 = (float) (y * Math.sin(pa) + rz * Math.cos(pa));
      float persp = 1.0F / (1.0F - rz2 * 0.12F);
      float sc = Math.min(this.vpW(), this.vpH()) * 0.30F * this.zoom * persp;
      return new float[]{this.vpX() + this.vpW() / 2.0F + rx * sc + this.panX, 8 + this.vpH() / 2.0F - ry * sc + this.panY, sc};
   }

   private void line(DrawContext c, float[] a, float[] b, int argb) {
      int steps = (int) Math.max(1, Math.max(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1])));
      steps = Math.min(steps, 400);
      for (int i = 0; i <= steps; i++) {
         float t = (float) i / steps;
         int px = (int) (a[0] + (b[0] - a[0]) * t);
         int py = (int) (a[1] + (b[1] - a[1]) * t);
         c.fill(px, py, px + 1, py + 1, argb);
      }
   }

   private void wireBox(DrawContext c, float x0, float y0, float z0, float x1, float y1, float z1, int argb) {
      float[][] v = new float[8][];
      for (int i = 0; i < 8; i++) v[i] = this.proj((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
      int[][] e = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
      for (int[] ed : e) this.line(c, v[ed[0]], v[ed[1]], argb);
   }

   @Override
   public void render(DrawContext c, int mx, int my, float delta) {
      c.fill(0, 0, this.width, this.height, 0xFF0B0D12);
      this.hits.clear();
      int pr = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;

      // ---- viewport ----
      int vx = this.vpX();
      int vw = this.vpW();
      int vh = this.vpH();
      GuiStyle.roundedBordered(c, vx, 8, vw, vh, 6, 0xFF10131A, 0x33FFFFFF);
      c.enableScissor(vx, 8, vx + vw, 8 + vh);
      for (int i = -4; i <= 4; i++) {
         int g = i == 0 ? 0x40FFFFFF : 0x20FFFFFF;
         this.line(c, this.proj(i * 0.5F, -0.9F, -2.0F), this.proj(i * 0.5F, -0.9F, 2.0F), g);
         this.line(c, this.proj(-2.0F, -0.9F, i * 0.5F), this.proj(2.0F, -0.9F, i * 0.5F), g);
      }

      this.wireBox(c, -0.3F, -0.9F, -0.3F, 0.3F, 0.9F, 0.3F, 0x55FFFFFF);
      this.line(c, this.proj(-0.1F, 0, 0), this.proj(0.1F, 0, 0), 0xFFFF5555);
      this.line(c, this.proj(0, -0.1F, 0), this.proj(0, 0.1F, 0), 0xFF55FF55);
      this.line(c, this.proj(0, 0, -0.1F), this.proj(0, 0, 0.1F), 0xFF5599FF);

      if (this.animated) {
         long now = System.currentTimeMillis();
         float dt = Math.min(0.1F, (now - this.lastStep) / 1000.0F);
         this.lastStep = now;
         CritBillboardFX.advanceList(this.sim, dt);
         if (this.sim.isEmpty()) {
            if (this.simEmptySince == 0L) this.simEmptySince = now;
            if (now - this.simEmptySince > 500L) this.restart();
         }

         for (CritBillboardFX.Spark s : this.sim) {
            float t = s.life / s.maxLife;
            float am = t < s.fadeIn ? t / s.fadeIn : 1.0F - (t - s.fadeIn) / (1.0F - s.fadeIn);
            am = Math.max(0.0F, Math.min(1.0F, am));
            float[] p = this.proj((float) s.x, (float) s.y, (float) s.z);
            float size = (s.size0 + (s.size1 - s.size0) * t) * 2.0F * p[2];
            int a = (int) ((s.color >>> 24) * am);
            if (a < 6) continue;
            int rgb = s.color & 0xFFFFFF;
            int h1 = Math.max(2, (int) (size * 2.2F));
            c.fill((int) p[0] - h1, (int) p[1] - h1, (int) p[0] + h1, (int) p[1] + h1, (int) (a * 0.18F) << 24 | rgb);
            int h2 = Math.max(1, (int) size);
            c.fill((int) p[0] - h2, (int) p[1] - h2, (int) p[0] + h2, (int) p[1] + h2, a << 24 | rgb);
            int h3 = Math.max(1, h2 / 2);
            c.fill((int) p[0] - h3, (int) p[1] - h3, (int) p[0] + h3, (int) p[1] + h3, (int) (a * 0.8F) << 24 | 0xFFFFFF);
         }
      } else {
         for (int pi = 0; pi < this.parts.size(); pi++) {
            FxPart part = this.parts.get(pi);
            int col = part.argb();
            boolean isSel = pi == this.sel;
            for (float[] q : part.points()) {
               float[] p = this.proj(q[0], q[1], q[2]);
               int r = isSel ? 2 : 1;
               c.fill((int) p[0] - r, (int) p[1] - r, (int) p[0] + r, (int) p[1] + r, (isSel ? 0xFF000000 : 0xB0000000) | col & 0xFFFFFF);
            }

            if (isSel) {
               float[] o = this.proj(part.x, part.y, part.z);
               c.fill((int) o[0] - 4, (int) o[1], (int) o[0] + 5, (int) o[1] + 1, 0xFFFFFFFF);
               c.fill((int) o[0], (int) o[1] - 4, (int) o[0] + 1, (int) o[1] + 5, 0xFFFFFFFF);
            }
         }
      }

      this.drawGizmo(c);
      c.disableScissor();
      GuiStyle.text(c, this.textRenderer, "Click part: select | drag: orbit | right/shift-drag: pan | wheel: zoom | G/R/S tools | Ctrl: snap", vx + 8, 8 + vh - 12, 0x80FFFFFF);
      String[] tn = {"Move (G)", "Rotate (R)", "Scale (S)"};
      for (int i = 0; i < 3; i++) this.btn(c, vx + 6 + i * 64, 12, 62, 15, tn[i], this.tool == i, mx, my, 800 + i);
      this.btn(c, vx + vw - 6 - 3 * 42, 12, 40, 15, "Front", false, mx, my, 810);
      this.btn(c, vx + vw - 6 - 2 * 42, 12, 40, 15, "Side", false, mx, my, 811);
      this.btn(c, vx + vw - 6 - 42, 12, 40, 15, "Top", false, mx, my, 812);

      // ---- left panel ----
      int lx = 8;
      GuiStyle.text(c, this.textRenderer, "Particle Modeler", lx, 12, 0xFF000000 | pr);
      GuiStyle.roundedBordered(c, lx, 26, LEFT_W, 16, 4, this.nameFocus ? 0x55000000 | pr : 0x30FFFFFF, this.nameFocus ? 0xFF000000 | pr : 0x40FFFFFF);
      GuiStyle.text(c, this.textRenderer, this.name + (this.nameFocus && System.currentTimeMillis() / 500L % 2L == 0L ? "_" : ""), lx + 5, 30, 0xFFFFFFFF);
      this.hits.add(new int[]{lx, 26, LEFT_W, 16, 200});

      GuiStyle.text(c, this.textRenderer, "PARTS", lx, 50, 0x80FFFFFF);
      for (int i = 0; i < this.parts.size() && i < 9; i++) {
         FxPart p = this.parts.get(i);
         int ry = 61 + i * 15;
         boolean s = i == this.sel;
         GuiStyle.roundedRect(c, lx, ry, LEFT_W, 13, 3, s ? 0x55000000 | pr : GuiStyle.inRect(mx, my, lx, ry, LEFT_W, 13) ? 0x30FFFFFF : 0x18FFFFFF);
         c.fill(lx + 4, ry + 4, lx + 9, ry + 9, p.argb());
         GuiStyle.text(c, this.textRenderer, (i + 1) + ". " + p.type + "  x" + p.count, lx + 14, ry + 3, 0xFFFFFFFF);
         this.hits.add(new int[]{lx, ry, LEFT_W, 13, 300 + i});
      }

      int ay = 61 + Math.min(9, this.parts.size()) * 15 + 6;
      GuiStyle.text(c, this.textRenderer, "ADD SHAPE", lx, ay, 0x80FFFFFF);
      for (int i = 0; i < FxPart.TYPES.length; i++) {
         int bx = lx + (i % 4) * (LEFT_W / 4);
         int by = ay + 11 + (i / 4) * 17;
         this.btn(c, bx, by, LEFT_W / 4 - 3, 15, FxPart.TYPES[i], false, mx, my, 400 + i);
      }

      int cy = ay + 11 + 34 + 4;
      this.btn(c, lx, cy, LEFT_W / 2 - 2, 16, "Duplicate", false, mx, my, 500);
      this.btn(c, lx + LEFT_W / 2 + 2, cy, LEFT_W / 2 - 2, 16, "Delete", false, mx, my, 501);

      int fy = cy + 24;
      GuiStyle.text(c, this.textRenderer, "SAVED MODELS", lx, fy, 0x80FFFFFF);
      for (int i = 0; i < this.files.size() && i < 6; i++) {
         int ry = fy + 11 + i * 14;
         boolean hv = GuiStyle.inRect(mx, my, lx, ry, LEFT_W, 12);
         GuiStyle.roundedRect(c, lx, ry, LEFT_W, 12, 3, hv ? 0x30FFFFFF : 0x14FFFFFF);
         GuiStyle.text(c, this.textRenderer, this.files.get(i), lx + 5, ry + 2, 0xFFDDE6F5);
         this.hits.add(new int[]{lx, ry, LEFT_W, 12, 600 + i});
      }

      // ---- right panel: properties ----
      int rx = this.width - RIGHT_W - 8;
      FxPart p = this.cur();
      if (p != null) {
         GuiStyle.text(c, this.textRenderer, "PART " + (this.sel + 1) + ": " + p.type.toUpperCase(), rx, 12, 0xFF000000 | pr);
         c.enableScissor(rx - 2, 24, rx + RIGHT_W + 2, this.height - 54);
         for (int i = 0; i < PROPS.length; i++) {
            int ry = 26 + i * 19 - this.propScroll;
            float v = this.get(p, i);
            float t = (v - MIN[i]) / (MAX[i] - MIN[i]);
            GuiStyle.text(c, this.textRenderer, PROPS[i], rx, ry, 0xC0FFFFFF);
            String vs = i == 6 ? String.valueOf((int) v) : String.format(java.util.Locale.ROOT, "%.2f", v);
            GuiStyle.text(c, this.textRenderer, vs, rx + RIGHT_W - this.textRenderer.getWidth(vs), ry, 0xFFFFFFFF);
            c.fill(rx, ry + 10, rx + RIGHT_W, ry + 13, 0x40FFFFFF);
            c.fill(rx, ry + 10, rx + (int) (RIGHT_W * t), ry + 13, 0xFF000000 | pr);
            int kx = rx + (int) (RIGHT_W * t);
            GuiStyle.roundedRect(c, kx - 3, ry + 8, 6, 7, 2, 0xFFFFFFFF);
         }

         int ry = 26 + PROPS.length * 19 - this.propScroll;
         this.btn(c, rx, ry, RIGHT_W / 2 - 2, 16, "Color", false, mx, my, 700);
         c.fill(rx + 4, ry + 5, rx + 10, ry + 11, p.argb());
         this.btn(c, rx + RIGHT_W / 2 + 2, ry, RIGHT_W / 2 - 2, 16, "Streak: " + (p.streak ? "ON" : "OFF"), p.streak, mx, my, 701);
         c.disableScissor();
      }

      // ---- bottom bar ----
      int bY = this.height - 26;
      int bx = this.vpX();
      this.btn(c, bx, bY, 64, 18, "< Back", false, mx, my, 100);
      this.btn(c, bx + 68, bY, 84, 18, this.animated ? "View: Live" : "View: Static", this.animated, mx, my, 101);
      this.btn(c, bx + 156, bY, 52, 18, "Replay", false, mx, my, 102);
      this.btn(c, bx + 212, bY, 90, 18, "Fires: " + TRIGGERS[this.trigger], false, mx, my, 103);
      this.btn(c, bx + 306, bY, 54, 18, "Save", false, mx, my, 104);
      this.btn(c, bx + 364, bY, 46, 18, "New", false, mx, my, 105);

      if (System.currentTimeMillis() < this.statusUntil) {
         GuiStyle.text(c, this.textRenderer, this.status, this.vpX() + 4, this.height - 38, 0xFF9FE8FF);
      }

      if (this.picker != null) {
         if (!this.picker.isOpen()) this.picker = null;
         else this.picker.render(c, mx, my, this.width, this.height);
      }
   }

   private boolean keyDown(int a, int b) {
      long h = MinecraftClient.getInstance().getWindow().getHandle();
      return org.lwjgl.glfw.GLFW.glfwGetKey(h, a) == 1 || org.lwjgl.glfw.GLFW.glfwGetKey(h, b) == 1;
   }

   // ---------- gizmo ----------

   private static final int[] AXIS_COL = {0xFFFF4444, 0xFF44FF44, 0xFF4488FF};
   private float dragStart;
   private float dragAccum;

   private float[] axisPoint(FxPart p, int a, float t) {
      return this.proj(p.x + (a == 0 ? t : 0), p.y + (a == 1 ? t : 0), p.z + (a == 2 ? t : 0));
   }

   private float[] ringPoint(FxPart p, int a, double ang) {
      float c = (float) Math.cos(ang) * 0.5F;
      float s = (float) Math.sin(ang) * 0.5F;
      return switch (a) {
         case 0 -> this.proj(p.x, p.y + c, p.z + s);
         case 1 -> this.proj(p.x + c, p.y, p.z + s);
         default -> this.proj(p.x + c, p.y + s, p.z);
      };
   }

   private void drawGizmo(DrawContext c) {
      FxPart p = this.cur();
      if (p == null) return;
      float[] o = this.proj(p.x, p.y, p.z);
      for (int a = 0; a < 3; a++) {
         int col = AXIS_COL[a];
         if (this.tool == 1) {
            float[] prev = this.ringPoint(p, a, 0);
            for (int i = 1; i <= 32; i++) {
               float[] cur = this.ringPoint(p, a, i * Math.PI * 2.0 / 32.0);
               this.line(c, prev, cur, col);
               prev = cur;
            }
         } else {
            float[] e = this.axisPoint(p, a, 0.6F);
            this.line(c, o, e, col);
            int r = this.tool == 2 ? 4 : 3;
            c.fill((int) e[0] - r, (int) e[1] - r, (int) e[0] + r + 1, (int) e[1] + r + 1, col);
         }
      }

      c.fill((int) o[0] - 2, (int) o[1] - 2, (int) o[0] + 3, (int) o[1] + 3, 0xFFFFFFFF);
   }

   private int gizmoHit(double mx, double my) {
      FxPart p = this.cur();
      if (p == null) return -1;
      int best = -1;
      double bd = this.tool == 1 ? 6.0 : 9.0;
      for (int a = 0; a < 3; a++) {
         double d = 1e9;
         if (this.tool == 1) {
            for (int i = 0; i < 48; i++) {
               float[] q = this.ringPoint(p, a, i * Math.PI * 2.0 / 48.0);
               d = Math.min(d, Math.hypot(q[0] - mx, q[1] - my));
            }
         } else {
            float[] e = this.axisPoint(p, a, 0.6F);
            d = Math.hypot(e[0] - mx, e[1] - my);
         }

         if (d < bd) {
            bd = d;
            best = a;
         }
      }

      return best;
   }

   private int pickPart(double mx, double my) {
      int best = -1;
      double bd = 10.0;
      for (int i = 0; i < this.parts.size(); i++) {
         FxPart p = this.parts.get(i);
         float[] o = this.proj(p.x, p.y, p.z);
         double d = Math.hypot(o[0] - mx, o[1] - my);
         for (float[] q : p.points()) {
            float[] s = this.proj(q[0], q[1], q[2]);
            d = Math.min(d, Math.hypot(s[0] - mx, s[1] - my));
         }

         if (d < bd) {
            bd = d;
            best = i;
         }
      }

      return best;
   }

   private void dragGizmo(double dx, double dy) {
      FxPart p = this.cur();
      if (p == null || this.dragAxis < 0) return;
      int a = this.dragAxis;
      float[] o = this.proj(p.x, p.y, p.z);
      float[] e = this.axisPoint(p, a, 1.0F);
      float ax = e[0] - o[0];
      float ay = e[1] - o[1];
      float l2 = ax * ax + ay * ay;
      boolean snap = this.keyDown(341, 345);
      if (this.tool == 1) {
         this.dragAccum += (float) (dx - dy) * 0.7F;
         float v = this.dragStart + this.dragAccum;
         if (snap) v = Math.round(v / 15.0F) * 15.0F;
         v = ((v + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F;
         this.set(p, 13 + a, v);
      } else {
         if (l2 < 1.0F) return;
         this.dragAccum += (float) ((dx * ax + dy * ay) / l2);
         float v = this.dragStart + this.dragAccum;
         if (snap) v = Math.round(v * 10.0F) / 10.0F;
         if (this.tool == 0) this.set(p, a, Math.max(MIN[a], Math.min(MAX[a], v)));
         else this.set(p, 3 + a, Math.max(MIN[3 + a], Math.min(MAX[3 + a], v)));
      }
   }

   // ---------- input ----------

   private void action(int id) {
      FxPart p = this.cur();
      if (id >= 800 && id < 803) {
         this.tool = id - 800;
         return;
      }

      if (id >= 810 && id < 813) {
         this.yaw = id == 811 ? 90.0F : 0.0F;
         this.pitch = id == 812 ? 89.0F : 0.0F;
         this.panX = 0.0F;
         this.panY = 0.0F;
         return;
      }

      if (id >= 300 && id < 310) this.sel = id - 300;
      else if (id >= 400 && id < 410) {
         this.parts.add(new FxPart(FxPart.TYPES[id - 400]));
         this.sel = this.parts.size() - 1;
         this.restart();
      } else if (id >= 600 && id < 610) this.load(this.files.get(id - 600));
      else switch (id) {
         case 100 -> this.client.setScreen(this.parent);
         case 101 -> this.animated = !this.animated;
         case 102 -> this.restart();
         case 103 -> this.trigger = (this.trigger + 1) % TRIGGERS.length;
         case 104 -> this.save();
         case 105 -> {
            this.parts.clear();
            this.parts.add(new FxPart("ring"));
            this.sel = 0;
            this.name = "my_effect";
            this.restart();
         }
         case 200 -> this.nameFocus = true;
         case 500 -> {
            if (p != null && this.parts.size() < 30) {
               FxPart c = p.copy();
               c.y += 0.2F;
               this.parts.add(c);
               this.sel = this.parts.size() - 1;
               this.restart();
            }
         }
         case 501 -> {
            if (p != null && this.parts.size() > 1) {
               this.parts.remove(this.sel);
               this.sel = Math.max(0, this.sel - 1);
               this.restart();
            }
         }
         case 700 -> {
            if (p != null) {
               this.picker = new ColorPickerModal(this.width / 2, this.height / 2, p.argb(), "Part color", "",
                  argb -> {
                     p.color = String.format("#%06X", argb & 0xFFFFFF);
                     this.restart();
                  });
            }
         }
         case 701 -> {
            if (p != null) {
               p.streak = !p.streak;
               this.restart();
            }
         }
         default -> {
         }
      }
   }

   private void dragTo(double mx) {
      FxPart p = this.cur();
      if (p == null || this.dragProp < 0) return;
      int rx = this.width - RIGHT_W - 8;
      float t = (float) Math.max(0.0, Math.min(1.0, (mx - rx) / RIGHT_W));
      this.set(p, this.dragProp, MIN[this.dragProp] + t * (MAX[this.dragProp] - MIN[this.dragProp]));
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      if (this.picker != null) {
         boolean cons = this.picker.mouseClicked(click);
         if (!this.picker.isOpen()) this.picker = null;
         if (cons) return true;
      }

      double mx = click.x();
      double my = click.y();
      this.nameFocus = false;
      for (int[] h : this.hits) {
         if (GuiStyle.inRect(mx, my, h[0], h[1], h[2], h[3])) {
            this.action(h[4]);
            return true;
         }
      }

      int rx = this.width - RIGHT_W - 8;
      if (mx >= rx - 4 && this.cur() != null) {
         for (int i = 0; i < PROPS.length; i++) {
            int ry = 26 + i * 19 - this.propScroll;
            if (my >= ry + 6 && my <= ry + 17) {
               this.dragProp = i;
               this.dragTo(mx);
               return true;
            }
         }
      }

      if (GuiStyle.inRect(mx, my, this.vpX(), 8, this.vpW(), this.vpH())) {
         if (click.button() == 1 || click.button() == 2 || this.keyDown(340, 344)) {
            this.panning = true;
            return true;
         }

         int ax = this.gizmoHit(mx, my);
         if (ax >= 0) {
            FxPart p = this.cur();
            this.dragAxis = ax;
            this.dragAccum = 0.0F;
            this.dragStart = this.tool == 1 ? this.get(p, 13 + ax) : this.tool == 0 ? this.get(p, ax) : this.get(p, 3 + ax);
            return true;
         }

         int pi = this.pickPart(mx, my);
         if (pi >= 0) {
            this.sel = pi;
            return true;
         }

         this.rotating = true;
         return true;
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.picker != null && this.picker.mouseDragged(click)) return true;
      if (this.dragProp >= 0) {
         this.dragTo(click.x());
         return true;
      }

      if (this.dragAxis >= 0) {
         this.dragGizmo(dx, dy);
         return true;
      }

      if (this.panning) {
         this.panX += (float) dx;
         this.panY += (float) dy;
         return true;
      }

      if (this.rotating) {
         this.yaw += (float) dx * 0.6F;
         this.pitch = Math.max(-85.0F, Math.min(85.0F, this.pitch + (float) dy * 0.6F));
         return true;
      }

      return super.mouseDragged(click, dx, dy);
   }

   @Override
   public boolean mouseReleased(Click click) {
      if (this.picker != null) this.picker.mouseReleased();
      if (this.dragProp >= 0 || this.dragAxis >= 0) this.restart();
      this.dragProp = -1;
      this.dragAxis = -1;
      this.panning = false;
      this.rotating = false;
      return super.mouseReleased(click);
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double hx, double vy) {
      if (this.picker != null) return true;
      if (mx >= this.width - RIGHT_W - 12) {
         int max = Math.max(0, 26 + PROPS.length * 19 + 20 - (this.height - 54));
         this.propScroll = Math.max(0, Math.min(max, this.propScroll - (int) (vy * 19)));
         return true;
      }

      this.zoom = Math.max(0.3F, Math.min(3.5F, this.zoom * (1.0F + (float) vy * 0.1F)));
      return true;
   }

   @Override
   public boolean charTyped(CharInput input) {
      if (this.picker != null && this.picker.charTyped(input)) return true;
      if (this.nameFocus) {
         int cp = input.codepoint();
         if ((Character.isLetterOrDigit(cp) || cp == '_' || cp == '-') && this.name.length() < 24) {
            this.name += new String(Character.toChars(cp));
         }

         return true;
      }

      return super.charTyped(input);
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (this.picker != null && this.picker.keyPressed(input)) {
         if (!this.picker.isOpen()) this.picker = null;
         return true;
      }

      int k = input.key();
      if (this.nameFocus) {
         if (k == 259 && !this.name.isEmpty()) this.name = this.name.substring(0, this.name.length() - 1);
         else if (k == 257 || k == 256) this.nameFocus = false;
         return true;
      }

      if (k == 71) {
         this.tool = 0;
         return true;
      }

      if (k == 82) {
         this.tool = 1;
         return true;
      }

      if (k == 83) {
         this.tool = 2;
         return true;
      }

      if (k == 261) {
         this.action(501);
         return true;
      }

      if (k == 68 && this.keyDown(340, 344)) {
         this.action(500);
         return true;
      }

      if (k == 256) {
         this.client.setScreen(this.parent);
         return true;
      }

      return super.keyPressed(input);
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
