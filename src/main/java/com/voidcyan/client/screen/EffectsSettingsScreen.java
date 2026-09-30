package com.voidcyan.client.screen;

import com.voidcyan.client.CritBillboardFX;
import com.voidcyan.client.CritEffectsManager;
import com.voidcyan.client.VoidCyanClient;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Effects module settings: a full-screen grid with LIVE looping previews of
 * every billboard preset. Click a card to install/uninstall that preset as an
 * effect JSON in config/voidcyan-crit-effects/ (hot-reloads instantly).
 */
public class EffectsSettingsScreen extends Screen {
   private static final int COLS = 3;
   private static final int CELL_W = 132;
   private static final int CELL_H = 110;
   private static final int GAP = 12;

   private final Screen parent;
   private float scrollOffset = 0.0F;
   private float scrollTarget = 0.0F;
   private int maxScroll = 0;
   private boolean dragging = false;
   private String statusMsg = "";
   private long statusUntil = 0L;
   private int primaryColor = 0xFF00F5FF;
   private long openTime = 0L;
   private final Map<String, Long> hoverSince = new HashMap<>();

   /** Shape whose right-click trigger menu is open (null = closed). */
   private String triggerMenuFor = null;
   private int menuX = 0;
   private int menuY = 0;

   /** Every event an effect can be restricted to — anything not chosen stays off. */
   private static final String[] TRIGGERS = {
      "hit", "hit_player", "hit_mob", "hit_armor_stand",
      "crit", "crit_player", "crit_mob",
      "kill", "kill_player", "kill_mob",
      "death", "xp", "totem", "crystal"
   };

   private static String triggerLabel(String t) {
      return switch (t == null ? "hit" : t) {
         case "hit_player" -> "Hit — Player";
         case "hit_mob" -> "Hit — Mob";
         case "hit_armor_stand" -> "Hit — Armor Stand";
         case "crit" -> "Critical Hit (any)";
         case "crit_player" -> "Crit — Player";
         case "crit_mob" -> "Crit — Mob";
         case "kill" -> "Kill (any)";
         case "kill_player" -> "Kill — Player";
         case "kill_mob" -> "Kill — Mob";
         case "death" -> "Death (you die)";
         case "xp" -> "XP Orb Pickup";
         case "totem" -> "Totem Pop";
         case "crystal" -> "Crystal Break";
         default -> "Hit (any)";
      };
   }

   public EffectsSettingsScreen(Screen parent) {
      super(Text.literal("Effects Settings"));
      this.parent = parent;
   }

   private int gridTop() {
      return 54;
   }

   private int gridBottom() {
      return this.height - 44;
   }

   private int gridX() {
      return this.width / 2 - (COLS * CELL_W + (COLS - 1) * GAP) / 2;
   }

   private int contentHeight() {
      return (this.totalRows()) * (CELL_H + GAP) + 8;
   }

   private int totalRows() {
      int n = this.keys().size();
      return (n + COLS - 1) / COLS;
   }

   private void status(String msg) {
      this.statusMsg = msg;
      this.statusUntil = System.currentTimeMillis() + 2600L;
   }

   private List<String> customs = new java.util.ArrayList<>();

   private void refreshCustoms() {
      this.customs.clear();
      CritBillboardFX.CUSTOM_PARTS.clear();
      File[] fs = CritEffectsManager.getEffectsDir().listFiles((d, n) -> n.toLowerCase().endsWith(".json"));
      if (fs == null) return;
      com.google.gson.Gson gson = new com.google.gson.Gson();
      for (File f : fs) {
         try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(Files.readString(f.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (!o.has("parts")) continue;
            String n = f.getName().replaceAll("[.]json$", "");
            List<com.voidcyan.client.FxPart> parts = new java.util.ArrayList<>();
            for (var e : o.getAsJsonArray("parts")) parts.add(gson.fromJson(e, com.voidcyan.client.FxPart.class));
            this.customs.add(n);
            CritBillboardFX.CUSTOM_PARTS.put(n, parts);
         } catch (Exception ignored) {
         }
      }
   }

   private List<String> keys() {
      List<String> k = new java.util.ArrayList<>(List.of(CritBillboardFX.PRESET_NAMES));
      for (String c : this.customs) k.add("custom:" + c);
      return k;
   }

   private String label(String key) {
      return key.startsWith("custom:") ? key.substring(7) + " (custom)" : CritBillboardFX.presetDisplayName(key);
   }

   private File presetFile(String shape) {
      if (shape.startsWith("custom:")) return new File(CritEffectsManager.getEffectsDir(), shape.substring(7) + ".json");
      return new File(CritEffectsManager.getEffectsDir(), "preset_" + shape + ".json");
   }

   private boolean isInstalled(String shape) {
      if (shape.startsWith("custom:")) {
         try {
            com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(Files.readString(this.presetFile(shape).toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
            return !o.has("enabled") || o.get("enabled").getAsBoolean();
         } catch (Exception e) {
            return false;
         }
      }

      return this.presetFile(shape).isFile();
   }

   /** Reads the "trigger" field of an installed preset (default "hit"). */
   private String installedTrigger(String shape) {
      File f = this.presetFile(shape);
      if (!f.isFile()) return "hit";
      try {
         com.google.gson.JsonObject obj = com.google.gson.JsonParser
            .parseString(Files.readString(f.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
         return obj.has("trigger") && !obj.get("trigger").isJsonNull() ? obj.get("trigger").getAsString() : "hit";
      } catch (Exception ex) {
         return "hit";
      }
   }

   /** Rewrites the "trigger" field of an installed preset and hot-reloads. */
   private void setInstalledTrigger(String shape, String trigger) {
      File f = this.presetFile(shape);
      if (!f.isFile()) return;
      try {
         com.google.gson.JsonObject obj = com.google.gson.JsonParser
            .parseString(Files.readString(f.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
         obj.addProperty("trigger", trigger);
         Files.writeString(f.toPath(), obj.toString(), StandardCharsets.UTF_8);
         CritEffectsManager.reload();
         this.status(this.label(shape) + " now fires on: " + triggerLabel(trigger));
      } catch (Exception ex) {
         this.status("Failed to update trigger");
      }
   }

   private void setCustomEnabled(String shape, boolean on) {
      try {
         File f = this.presetFile(shape);
         com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(Files.readString(f.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
         o.addProperty("enabled", on);
         Files.writeString(f.toPath(), o.toString(), StandardCharsets.UTF_8);
         CritEffectsManager.reload();
         this.status((on ? "Enabled " : "Disabled ") + this.label(shape));
      } catch (Exception e) {
         this.status("Failed to update effect file");
      }
   }

   private void installPreset(String shape) {
      if (shape.startsWith("custom:")) {
         this.setCustomEnabled(shape, true);
         return;
      }

      try {
         CritEffectsManager.reload();
         File dir = CritEffectsManager.getEffectsDir();
         if (!dir.isDirectory()) {
            dir.mkdirs();
         }
         File f = this.presetFile(shape);
         String color = String.format("#%06X", CritBillboardFX.presetColor(shape) & 0xFFFFFF);
         String json = "{\n"
            + "  \"enabled\": true,\n"
            + "  \"trigger\": \"hit\",\n"
            + "  \"requireCrit\": false,\n"
            + "  \"cooldownMs\": 250,\n"
            + "  \"shape\": \"" + shape + "\",\n"
            + "  \"color\": \"" + color + "\",\n"
            + "  \"sound\": \"\",\n"
            + "  \"volume\": 1.0,\n"
            + "  \"pitch\": 1.0\n"
            + "}";
         Files.writeString(f.toPath(), json, StandardCharsets.UTF_8);
         CritEffectsManager.reload();
         this.status("Enabled " + CritBillboardFX.presetDisplayName(shape));
      } catch (IOException ex) {
         this.status("Failed to write effect file");
      }
   }

   private void uninstallPreset(String shape) {
      if (shape.startsWith("custom:")) {
         this.setCustomEnabled(shape, false);
         return;
      }

      try {
         Files.deleteIfExists(this.presetFile(shape).toPath());
         CritEffectsManager.reload();
         this.status("Disabled " + CritBillboardFX.presetDisplayName(shape));
      } catch (IOException ex) {
         this.status("Failed to remove effect file");
      }
   }

   @Override
   protected void init() {
      this.openTime = System.currentTimeMillis();
      CritBillboardFX.clearPreviews();
      this.hoverSince.clear();
      this.refreshCustoms();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      // NOTE: do NOT call renderBackground() here — vanilla already blurs the
      // world once per frame (1.21.6+), a second call throws
      // IllegalStateException: Can only blur once per frame.
      context.fillGradient(0, 0, this.width, this.height, 0xE0100A18, 0xC0150F20);
      this.primaryColor = VoidCyanClient.getPrimaryColor();
      float anim = Math.min(1.0F, (float) (System.currentTimeMillis() - this.openTime) / 260.0F);
      anim = 1.0F - (float) Math.pow(1.0F - anim, 3.0);

      // Header
      String title = "Effects";
      context.drawTextWithShadow(this.textRenderer, Text.literal(title), 16, 16, this.primaryColor | 0xFF000000 & 0xFFFFFFFF);
      String sub = "Click a card to toggle the effect on your hits \u00b7 " + CritEffectsManager.getEffectCount() + " effect file(s) loaded";
      context.drawTextWithShadow(this.textRenderer, Text.literal(sub), 16, 30, 0xFF8A93A6);

      int gx = this.gridX();
      int top = this.gridTop();
      int bottom = this.gridBottom();

      context.enableScissor(0, top, this.width, bottom);
      int contentH = this.contentHeight();
      this.maxScroll = Math.max(0, contentH - (bottom - top));
      if (this.maxScroll == 0) {
         this.scrollOffset = 0.0F;
         this.scrollTarget = 0.0F;
      } else {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget));
         this.scrollOffset += (this.scrollTarget - this.scrollOffset) * 0.3F;
      }

      List<String> allKeys = this.keys();
      for (int i = 0; i < allKeys.size(); i++) {
         String shape = allKeys.get(i);
         int row = i / COLS;
         int col = i % COLS;
         int cx = gx + col * (CELL_W + GAP);
         int cy = top + 4 + row * (CELL_H + GAP) - (int) this.scrollOffset;
         if (cy + CELL_H < top || cy > bottom) {
            continue;
         }
         this.drawPreviewCell(context, shape, cx, cy, mouseX, mouseY, anim);
      }
      context.disableScissor();

      // Scrollbar
      if (this.maxScroll > 0) {
         int trackX = this.width - 10;
         context.fill(trackX, top, trackX + 3, bottom, 0x30FFFFFF);
         float ratio = (float) (bottom - top) / contentH;
         int barH = Math.max(24, (int) ((bottom - top) * ratio));
         int barY = top + (int) (((bottom - top) - barH) * (this.scrollOffset / (float) this.maxScroll));
         context.fill(trackX, barY, trackX + 3, barY + barH, 0x90FFFFFF);
      }

      // 3D Modeler button (top right)
      boolean mh = mouseX >= this.width - 136 && mouseX <= this.width - 12 && mouseY >= 12 && mouseY <= 32;
      this.fillRounded(context, this.width - 136, 12, 124, 20, 5, 0x66000000 | (mh ? this.primaryColor : 0x2A2333) & 0xFFFFFF);
      this.textCentered(context, "3D Modeler", this.width - 74, 18, 0xFFFFFFFF);

      // Bottom bar
      int by = this.height - 30;
      int bw = 130;
      int bx1 = this.width / 2 - bw - 6;
      int bx2 = this.width / 2 + 6;
      boolean hov1 = mouseX >= bx1 && mouseX <= bx1 + bw && mouseY >= by && mouseY <= by + 20;
      boolean hov2 = mouseX >= bx2 && mouseX <= bx2 + bw && mouseY >= by && mouseY <= by + 20;
      this.fillRounded(context, bx1, by, bw, 20, 5, (hov1 ? 0x60 : 0x38) << 24 | 0xFFFFFF);
      this.textCentered(context, "Open Effects Folder", bx1 + bw / 2, by + 6, 0xFFFFFFFF);
      int accent = hov2 ? this.primaryColor : 0xFF2A2333;
      this.fillRounded(context, bx2, by, bw, 20, 5, 0x66000000 | accent & 0xFFFFFF);
      this.textCentered(context, "Refresh", bx2 + bw / 2, by + 6, 0xFFE8E4F0);

      if (System.currentTimeMillis() < this.statusUntil && !this.statusMsg.isEmpty()) {
         this.textCentered(context, this.statusMsg, this.width / 2, this.height - 38, 0xFF9FE8FF);
      }

      if (this.triggerMenuFor != null) {
         this.drawTriggerMenu(context, mouseX, mouseY);
      }
   }

   // popup menu geometry shared by draw + click handling
   private static final int MENU_COLS = 2;
   private static final int MENU_ROW_H = 17;
   private static final int MENU_COL_W = 128;
   private static final int MENU_W = MENU_COLS * MENU_COL_W + 12;
   private static int menuRows() {
      return (TRIGGERS.length + MENU_COLS - 1) / MENU_COLS;
   }
   private static int menuHeight() {
      return 22 + menuRows() * MENU_ROW_H + 6;
   }

   /** Right-click popup: choose WHEN the effect fires (2-column grid). */
   private void drawTriggerMenu(DrawContext context, double mouseX, double mouseY) {
      int w = MENU_W;
      int h = menuHeight();
      int x = Math.max(4, Math.min(this.width - w - 4, this.menuX));
      int y = Math.max(4, Math.min(this.height - h - 4, this.menuY));
      this.fillRounded(context, x, y, w, h, 8, 0xF2140E1E);
      this.outlineRounded(context, x, y, w, h, 8, this.primaryColor & 0xFFFFFF | 0x90000000);
      this.textCentered(context, "Fires ONLY on:", x + w / 2, y + 7, this.primaryColor & 0xFFFFFF | 0xFF000000);

      String current = this.installedTrigger(this.triggerMenuFor);
      for (int i = 0; i < TRIGGERS.length; i++) {
         String trig = TRIGGERS[i];
         int col = i / menuRows();
         int row = i % menuRows();
         int rx = x + 6 + col * MENU_COL_W;
         int ry = y + 22 + row * MENU_ROW_H;
         boolean hov = mouseX >= rx && mouseX <= rx + MENU_COL_W - 6 && mouseY >= ry && mouseY <= ry + MENU_ROW_H - 2;
         boolean sel = trig.equals(current);
         if (hov || sel) {
            this.fillRounded(context, rx, ry, MENU_COL_W - 6, MENU_ROW_H - 2, 4,
               sel ? (0xB4 << 24 | this.primaryColor & 0xFFFFFF) : (0x30 << 24 | 0xFFFFFF));
         }
         int fg = sel ? 0xFF0E0B14 : hov ? 0xFFFFFFFF : 0xFFB8C2D0;
         this.textCentered(context, triggerLabel(trig), rx + (MENU_COL_W - 6) / 2, ry + 5, fg);
      }
   }

   private void drawPreviewCell(DrawContext context, String shape, int x, int y, double mouseX, double mouseY, float anim) {
      boolean hovered = mouseX >= x && mouseX <= x + CELL_W && mouseY >= y && mouseY <= y + CELL_H;
      boolean on = this.isInstalled(shape);
      int col = shape.startsWith("custom:") ? 0xFF00F5FF : CritBillboardFX.presetColor(shape);

      int bg = on ? 0x66161E2C : 0x44101420;
      int border = on ? 0xFF2FBF71 : (hovered ? 0xFFB8C2D0 : 0x608A93A6);
      if (hovered && !on) {
         border = col | 0xFF000000 & 0xFFFFFFFF;
      }
      this.fillRounded(context, x, y, CELL_W, CELL_H, 9, bg);
      this.outlineRounded(context, x, y, CELL_W, CELL_H, 9, (int) (anim * 255.0F) << 24 | border & 0xFFFFFF);
      if (on) {
         // soft top accent bar for enabled effects
         this.fillRounded(context, x + 8, y + 2, CELL_W - 16, 2, 1, 0xFF2FBF71);
      }

      // ---- LIVE PREVIEW: physics sparks simulated then drawn in screen space ----
      long hStart = this.hoverSince.containsKey(shape) ? this.hoverSince.get(shape) : 0L;
      boolean retrigger = hovered && System.currentTimeMillis() - hStart > 1700L;
      if (hovered && !this.hoverSince.containsKey(shape)) {
         this.hoverSince.put(shape, System.currentTimeMillis());
      } else if (!hovered) {
         this.hoverSince.remove(shape);
      }

      CritBillboardFX.beginPreview(shape, retrigger);
      List<CritBillboardFX.Spark> sparks = CritBillboardFX.previewSparks(shape);
      // auto-reloop: when the burst dies, respawn it after a short pause
      if (sparks.isEmpty() && System.currentTimeMillis() - hStart > 1700L) {
         this.hoverSince.put(shape, 0L);
         CritBillboardFX.beginPreview(shape, true);
         sparks = CritBillboardFX.previewSparks(shape);
      }
      double scale = shape.startsWith("custom:") ? 34.0 : 14.0; // world units -> px (sparks are now 0.5x size = fine pixels)
      int cx = x + CELL_W / 2;
      int cy = y + CELL_H / 2 + 6;
      for (CritBillboardFX.Spark s : sparks) {
         float t = s.life / s.maxLife;
         float alphaMul = t < 0.06F ? t / 0.06F : 1.0F - (t - 0.06F) / 0.94F;
         alphaMul = Math.max(0.0F, Math.min(1.0F, alphaMul));
         int argb = s.color;
         int a = (int) ((argb >>> 24) * alphaMul);
         if (a <= 3) continue;
         int argbA = a << 24 | argb & 0xFFFFFF;
         float size = (s.size0 + (s.size1 - s.size0) * t) * (float) scale;
         float px = (float) (cx + Math.max(-62.0, Math.min(62.0, s.x * scale)));
         float py = (float) (cy - Math.max(-48.0, Math.min(48.0, s.y * scale)));
         // fine pixels: sub-particle squares 1-3px, minimum visible alpha
         int w = Math.max(1, (int) size);
         if (s.streak) {
            double vl = Math.sqrt(s.vx * s.vx + s.vy * s.vy + s.vz * s.vz);
            if (vl > 1e-4) {
               float len = Math.max(3.0F, (float) Math.min(16.0, vl * scale * 0.05));
               float dx = (float) (s.vx / vl * len);
               float dy = (float) (s.vy / vl * len);
               // motion trail: 3 fading dots along velocity
               for (int k = 0; k < 3; k++) {
                  float kx = px - dx * k / 2.0F;
                  float ky = py + dy * k / 2.0F;
                  int ka = a * (3 - k) / 3;
                  if (ka > 4) this.fillRect(context, kx, ky, kx + w, ky + w, ka << 24 | argb & 0xFFFFFF);
               }
               continue;
            }
         }
         this.fillRect(context, px, py, px + w, py + w, argbA);
      }

      String name = this.label(shape);
      this.textCentered(context, name, x + CELL_W / 2, y + 6, 0xFFF0F3F8);
      String state = on ? ("\u2714 " + triggerLabel(this.installedTrigger(shape))) : "Click to enable";
      this.textCentered(context, state, x + CELL_W / 2, y + CELL_H - 12, on ? 0xFF7CFC9B : 0xFF8A93A6);
      if (hovered && on) {
         this.textCentered(context, "Right-click: choose when", x + CELL_W / 2, y + CELL_H - 22, 0xFF8A93A6);
      }
   }

   // ---------------- small draw helpers ----------------

   private void fillRounded(DrawContext context, int x, int y, int w, int h, int r, int argb) {
      GuiStyleHelper.roundedRect(context, x, y, w, h, r, argb);
   }

   private void outlineRounded(DrawContext context, int x, int y, int w, int h, int r, int argb) {
      GuiStyleHelper.roundedOutline(context, x, y, w, h, r, argb);
   }

   private void fillRect(DrawContext context, float x1, float y1, float x2, float y2, int argb) {
      context.fill((int) x1, (int) y1, (int) Math.ceil(x2), (int) Math.ceil(y2), argb);
   }

   private void textCentered(DrawContext context, String s, int cx, int y, int argb) {
      int w = this.textRenderer.getWidth(s);
      context.drawTextWithShadow(this.textRenderer, Text.literal(s), cx - w / 2, y, argb);
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      if (button == 0 && this.triggerMenuFor == null && mouseX >= this.width - 136 && mouseX <= this.width - 12 && mouseY >= 12 && mouseY <= 32) {
         this.client.setScreen(new ParticleModelerScreen(this));
         return true;
      }

      // ---- trigger popup menu handling ----
      if (this.triggerMenuFor != null) {
         int w = MENU_W;
         int h = menuHeight();
         int x = Math.max(4, Math.min(this.width - w - 4, this.menuX));
         int y = Math.max(4, Math.min(this.height - h - 4, this.menuY));
         if (button == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            for (int i = 0; i < TRIGGERS.length; i++) {
               int col = i / menuRows();
               int row = i % menuRows();
               int rx = x + 6 + col * MENU_COL_W;
               int ry = y + 22 + row * MENU_ROW_H;
               if (mouseX >= rx && mouseX <= rx + MENU_COL_W - 6 && mouseY >= ry && mouseY <= ry + MENU_ROW_H - 2) {
                  this.setInstalledTrigger(this.triggerMenuFor, TRIGGERS[i]);
                  this.triggerMenuFor = null;
                  return true;
               }
            }
         }
         // click anywhere else closes the menu
         this.triggerMenuFor = null;
         return true;
      }

      int gx = this.gridX();
      int top = this.gridTop();
      int bottom = this.gridBottom();
      if (mouseY >= top && mouseY <= bottom) {
         List<String> allKeys = this.keys();
         for (int i = 0; i < allKeys.size(); i++) {
            String shape = allKeys.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int cx = gx + col * (CELL_W + GAP);
            int cy = top + 4 + row * (CELL_H + GAP) - (int) this.scrollOffset;
            if (mouseX >= cx && mouseX <= cx + CELL_W && mouseY >= cy && mouseY <= cy + CELL_H) {
               if (button == 0) {
                  if (this.isInstalled(shape)) {
                     this.uninstallPreset(shape);
                  } else {
                     this.installPreset(shape);
                  }
               } else if (button == 1 && this.isInstalled(shape)) {
                  // right-click: open the trigger chooser (hit/crit/kill/death/xp/totem)
                  this.menuX = (int) mouseX + 4;
                  this.menuY = (int) mouseY + 4;
                  this.triggerMenuFor = shape;
               }
               return true;
            }
         }
      }

      int by = this.height - 30;
      int bw = 130;
      int bx1 = this.width / 2 - bw - 6;
      int bx2 = this.width / 2 + 6;
      if (mouseX >= bx1 && mouseX <= bx1 + bw && mouseY >= by && mouseY <= by + 20) {
         CritEffectsManager.reload();
         net.minecraft.util.Util.getOperatingSystem().open(CritEffectsManager.getEffectsDir());
         return true;
      }
      if (mouseX >= bx2 && mouseX <= bx2 + bw && mouseY >= by && mouseY <= by + 20) {
         CritEffectsManager.reload();
         this.status("Reloaded " + CritEffectsManager.getEffectCount() + " effect file(s)");
         return true;
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.dragging && this.maxScroll > 0) {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget - (int) dy));
         return true;
      }
      return super.mouseDragged(click, dx, dy);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.maxScroll > 0) {
         this.scrollTarget = Math.max(0, Math.min(this.maxScroll, this.scrollTarget - (int) (vertical * 34.0)));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
         if (this.parent != null) {
            this.client.setScreen(this.parent);
         } else {
            this.client.setScreen(null);
         }
         return true;
      }
      return super.keyPressed(input);
   }

   @Override
   public void removed() {
      CritBillboardFX.clearPreviews();
      super.removed();
   }
}

/** Minimal rounded-rect helpers (GuiStyle is in the same package). */
final class GuiStyleHelper {
   private GuiStyleHelper() {
   }

   static void roundedRect(DrawContext context, int x, int y, int w, int h, int r, int argb) {
      GuiStyle.roundedRect(context, x, y, w, h, r, argb);
   }

   static void roundedOutline(DrawContext context, int x, int y, int w, int h, int r, int argb) {
      GuiStyle.roundedOutline(context, x, y, w, h, r, argb);
   }
}
