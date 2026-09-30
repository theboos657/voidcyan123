package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Shared visual language for the redesigned VoidCyan click GUI:
 * rounded floating window, pill buttons/chips/badges, pill sliders,
 * thin rounded scrollbar and soft glow accents in the theme color.
 */
public final class GuiStyle {
   private GuiStyle() {}

   // ---- Core geometry ------------------------------------------------------

   public static final int WINDOW_MARGIN = 12;
   public static final int SIDEBAR_W = 150;
   public static final int WINDOW_RADIUS = 12;

   /** Cool profile: real rounded corners, gradients and pill controls. */
   public static boolean cool() {
      return VoidCyanClient.guiType == 1;
   }

   public static boolean square() {
      return VoidCyanClient.guiType == 0 && !orbitSkin;
   }

   /** Square and Cool both draw rounded corners; only the classic Orbit skin stays sharp. */
   public static boolean rounded() {
      return VoidCyanClient.guiType <= 1 && !orbitSkin;
   }

   /** True while the classic window is shown from the Orbit wheel (no sidebar, deep-purple skin). */
   public static boolean orbitSkin = false;

   /** Pulls dark base colors toward the Orbit purple while that skin is active. */
   public static int pal(int rgb) {
      return orbitSkin ? blend(rgb, 0x1E1236, 0.85F) : rgb;
   }

   private static int inset(int r, int row) {
      double d = r - row - 0.5;
      return (int)Math.round(r - Math.sqrt(Math.max(0.0, r * r - d * d)));
   }

   /** Rectangle; corners are rounded only in the Cool profile. */
   public static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int argb) {
      if (width <= 0 || height <= 0 || ((argb >>> 24) & 0xFF) <= 0) return;
      int r = rounded() ? Math.min(radius, Math.min(width, height) / 2) : 0;
      if (r <= 0) {
         context.fill(x, y, x + width, y + height, argb);
         return;
      }
      for (int i = 0; i < r; i++) {
         int in = inset(r, i);
         context.fill(x + in, y + i, x + width - in, y + i + 1, argb);
         context.fill(x + in, y + height - 1 - i, x + width - in, y + height - i, argb);
      }
      context.fill(x, y + r, x + width, y + height - r, argb);
   }

   /** 1px outline; rounded only in the Cool profile. */
   public static void roundedOutline(DrawContext context, int x, int y, int width, int height, int radius, int argb) {
      if (width <= 0 || height <= 0 || ((argb >>> 24) & 0xFF) <= 0) return;
      int r = rounded() ? Math.min(radius, Math.min(width, height) / 2) : 0;
      for (int i = 0; i < r; i++) {
         int in = inset(r, i);
         int span = Math.max(1, (i + 1 < r ? inset(r, i + 1) : 0) - in);
         if (i == 0) span = width - in * 2;
         for (int yy : new int[]{y + i, y + height - 1 - i}) {
            context.fill(x + in, yy, x + in + span, yy + 1, argb);
            context.fill(x + width - in - span, yy, x + width - in, yy + 1, argb);
         }
      }
      context.fill(x + r, y, x + width - r, y + 1, argb);
      context.fill(x + r, y + height - 1, x + width - r, y + height, argb);
      context.fill(x, y + Math.max(r, 1), x + 1, y + height - Math.max(r, 1), argb);
      context.fill(x + width - 1, y + Math.max(r, 1), x + width, y + height - Math.max(r, 1), argb);
   }

   /** Solid rounded rect with a 1px border of a different color. */
   public static void roundedBordered(
      DrawContext context, int x, int y, int width, int height, int radius, int fillArgb, int borderArgb
   ) {
      roundedRect(context, x, y, width, height, radius, borderArgb);
      roundedRect(context, x + 1, y + 1, width - 2, height - 2, Math.max(0, radius - 1), fillArgb);
   }

   /** Softer 2px border variant. */
   public static void roundedBordered2(
      DrawContext context, int x, int y, int width, int height, int radius, int fillArgb, int borderArgb
   ) {
      roundedRect(context, x, y, width, height, radius, borderArgb);
      roundedRect(context, x + 2, y + 2, width - 4, height - 4, Math.max(0, radius - 2), fillArgb);
   }

   // ---- Window --------------------------------------------------------------

   /**
    * Draws the full GUI window: soft outer glow, dark rounded body.
    * Returns nothing; caller draws content inside.
    */
   public static void drawWindow(DrawContext context, int x, int y, int width, int height, int primaryRgb, float openProgress) {
      int a = (int)(openProgress * 255.0F);
      if (a <= 4) return;
      // Outer glow: layered rounded rects expanding outwards with fading alpha.
      int[] glowAlphas = new int[]{(int)(a * 0.045F), (int)(a * 0.075F), (int)(a * 0.11F)};
      for (int layer = glowAlphas.length; layer >= 1; layer--) {
         int pad = layer * 4;
         int ga = glowAlphas[layer - 1];
         if (ga > 3) {
            roundedRect(context, x - pad, y - pad, width + pad * 2, height + pad * 2, WINDOW_RADIUS + pad, ga << 24 | (primaryRgb & 0xFFFFFF));
         }
      }

      // Body: dark base + faint top sheen.
      int body = (int)(a * 0.93F) << 24 | pal(0x0C0A12);
      roundedRect(context, x, y, width, height, WINDOW_RADIUS, body);
      if (cool() || square()) {
         int tint = (int)(a * (square() ? 0.10F : 0.16F)) << 24 | (primaryRgb & 0xFFFFFF);
         context.fillGradient(x + WINDOW_RADIUS, y + 1, x + width - WINDOW_RADIUS, y + height / 3, tint, 0);
         if (cool()) context.fill(x + WINDOW_RADIUS * 2, y, x + width - WINDOW_RADIUS * 2, y + 2, a << 24 | (primaryRgb & 0xFFFFFF));
      }

      // Hairline window outline, tinted by theme.
      int outline = (int)(a * 0.22F) << 24 | (primaryRgb & 0xFFFFFF);
      roundedOutline(context, x, y, width, height, WINDOW_RADIUS, outline);
      roundedOutline(context, x + 1, y + 1, width - 2, height - 2, WINDOW_RADIUS - 1, ((int)(a * 0.10F)) << 24);

      // Inner content well behind the content area (slightly lighter than sidebar).
   }

   public static void drawContentWell(DrawContext context, int x, int y, int width, int height, int primaryRgb, float openProgress) {
      int a = (int)(openProgress * 255.0F);
      if (a <= 4) return;
      if (square()) return;

      int well = (int)(a * 0.55F) << 24 | pal(0x120E1A);
      roundedRect(context, x, y, width, height, 10, well);
      int edge = (int)(a * 0.10F) << 24 | (primaryRgb & 0xFFFFFF);
      roundedOutline(context, x, y, width, height, 10, edge);
   }

   // ---- Text helpers ---------------------------------------------------------

   public static void text(DrawContext context, TextRenderer tr, String s, int x, int y, int argb) {
      context.drawTextWithShadow(tr, Text.literal(s), x, y, argb);
   }

   public static void textCentered(DrawContext context, TextRenderer tr, String s, int cx, int y, int argb) {
      context.drawTextWithShadow(tr, Text.literal(s), cx - tr.getWidth(s) / 2, y, argb);
   }

   // ---- Pills / chips ----------------------------------------------------------

   /** Pill-shaped toggle chip; returns true when hovered. */
   public static boolean chip(
      DrawContext context,
      TextRenderer tr,
      String label,
      int x,
      int y,
      boolean active,
      float hover,
      int primaryRgb,
      float alpha
   ) {
      int w = tr.getWidth(label) + 22;
      int h = 18;
      int a = (int)(alpha * 255.0F);
      if (a <= 4) return false;

      int bg;
      int border;
      int fg;
      if (square()) {
         if (active) {
            bg = (int)(a * 0.26F) << 24 | (primaryRgb & 0xFFFFFF);
            border = (int)(a * 0.85F) << 24 | (primaryRgb & 0xFFFFFF);
            fg = a << 24 | 0xFFFFFF;
         } else {
            bg = (int)(a * (0.10F + hover * 0.08F)) << 24 | 0xFFFFFF;
            border = (int)(a * (0.10F + hover * 0.14F)) << 24 | 0xFFFFFF;
            fg = (int)(a * (0.65F + hover * 0.3F)) << 24 | 0xFFFFFF;
         }

         roundedBordered(context, x, y, w, h, 4, bg, border);
         text(context, tr, label, x + 11, y + 5, fg);
         return true;
      }

      if (active) {
         // Active chips fill completely with the theme color; text stays dark but soft, never harsh black.
         bg = a * 95 / 100 << 24 | (primaryRgb & 0xFFFFFF);
         border = a << 24 | (primaryRgb & 0xFFFFFF);
         fg = a << 24 | 0x3A3544;
      } else {
         bg = a * (45 + (int)(hover * 25)) / 100 << 24 | 0x1C1724;
         border = a * (18 + (int)(hover * 30)) / 100 << 24 | 0xFFFFFF;
         fg = a * (60 + (int)(hover * 35)) / 100 << 24 | 0xFFFFFF;
      }

      roundedBordered(context, x, y, w, h, 4, bg, border);
      text(context, tr, label, x + 11, y + 5, fg);
      return true;
   }

   public static int chipWidth(TextRenderer tr, String label) {
      return tr.getWidth(label) + 22;
   }

   /** Small rounded badge (e.g. ON/OFF). The fixed width keeps both states readable. */
   public static void badge(DrawContext context, TextRenderer tr, String label, int x, int y, int colorRgb, float alpha) {
      int w = 34;
      int h = 16;
      int a = (int)(alpha * 255.0F);
      if (a <= 4) return;
      boolean on = label.equals("ON");
      int bg = on ? a * 95 / 100 << 24 | (colorRgb & 0xFFFFFF) : a * 14 / 100 << 24 | 0xFFFFFF;
      int border = on ? a << 24 | (colorRgb & 0xFFFFFF) : a * 30 / 100 << 24 | 0xFFFFFF;
      int fg = on ? a << 24 | 0x0E0B14 : a * 75 / 100 << 24 | 0xFFFFFF;
      roundedBordered(context, x, y, w, h, 4, bg, border);
      textCentered(context, tr, label, x + w / 2, y + 4, fg);
   }

   // ---- Controls ---------------------------------------------------------------

   private static boolean bright(int rgb) {
      return ((rgb >> 16 & 255) * 3 + (rgb >> 8 & 255) * 6 + (rgb & 255)) / 10 > 170;
   }

   /** Squared toggle switch: ON = full theme track, light knob on the right; OFF = dark track, knob left. */
   public static void toggleSwitch(DrawContext context, int x, int y, int width, int height, boolean enabled, float anim, int primaryRgb, float alpha) {
      int a = (int)(alpha * 255.0F);
      if (a <= 4) return;

      int radius = square() ? 4 : cool() ? height / 2 : 5;

      int track = enabled ? a * 95 / 100 << 24 | (primaryRgb & 0xFFFFFF) : square() ? a << 24 | 0x232630 : rounded() ? a << 24 | 0x3A3448 : a * 70 / 100 << 24 | 0x2A2533;
      roundedRect(context, x, y, width, height, radius, track);

      int knobD = height - 4;
      int knobMinX = x + 2;
      int knobMaxX = x + width - knobD - 2;
      float t = Math.max(0.0F, Math.min(1.0F, anim));
      int knobX = (int)(knobMinX + (knobMaxX - knobMinX) * t);
      // Light knob is clearly visible on both the dark OFF track and the theme-colored ON track.
      roundedRect(context, knobX, y + 2, knobD, knobD, cool() ? knobD / 2 : 3, a << 24 | (enabled ? (bright(primaryRgb) ? 0x232733 : 0xF5F2F8) : 0x8E93A3));
   }

   /**
    * Pill slider with gradient-free fill and glowing handle.
    * trackWidth is the interactive width; returns the computed handle x for reuse.
    */
   public static int slider(
      DrawContext context,
      int x,
      int y,
      int trackWidth,
      float ratio,
      int primaryRgb,
      float alpha,
      boolean hover
   ) {
      ratio = Math.max(0.0F, Math.min(1.0F, ratio));
      int a = (int)(alpha * 255.0F);
      if (a <= 4) return x;
      int trackH = 6;
      int trackY = y;
      roundedRect(context, x, trackY, trackWidth, trackH, trackH / 2, (int)(a * 0.22F) << 24 | 0xFFFFFF);

      int fillW = (int)(trackWidth * ratio);
      if (fillW > 0) {
         int fillCol = (int)(a * 0.95F) << 24 | (primaryRgb & 0xFFFFFF);
         roundedRect(context, x, trackY, Math.max(fillW, trackH), trackH, trackH / 2, fillCol);
      }

      int handleD = 10 + (hover ? 3 : 0);
      int handleX = x + fillW - handleD / 2;
      int glowA = (int)(a * (hover ? 0.35F : 0.18F));
      if (glowA > 3) {
         roundedRect(context, handleX - 3, trackY + trackH / 2 - handleD / 2 - 3, handleD + 6, handleD + 6, (handleD + 6) / 2, glowA << 24 | (primaryRgb & 0xFFFFFF));
      }

      roundedRect(context, handleX, trackY + trackH / 2 - handleD / 2, handleD, handleD, 3, (int)(a * 255.0F) << 24 | 0xF4F1F8);
      return handleX;
   }

   /** Thin rounded scrollbar; thumb position derived from scroll offsets. */
   public static void scrollbar(DrawContext context, int x, int y, int trackH, int scroll, int maxScroll, int primaryRgb, float alpha) {
      int a = (int)(alpha * 255.0F);
      if (a <= 4 || maxScroll <= 0 || trackH < 24) return;
      roundedRect(context, x, y, 4, trackH, 2, (int)(a * 0.10F) << 24 | 0xFFFFFF);

      int thumbH = Math.max(24, trackH * trackH / (trackH + maxScroll));
      int thumbY = y + (int)((float)scroll / maxScroll * (trackH - thumbH));
      roundedRect(context, x, thumbY, 4, thumbH, 2, (int)(a * 0.75F) << 24 | (primaryRgb & 0xFFFFFF));
   }

   public static boolean inRect(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   /** Linear blend of two RGB colors. */
   public static int blend(int rgbA, int rgbB, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = (rgbA >> 16) & 0xFF;
      int ag = (rgbA >> 8) & 0xFF;
      int ab = rgbA & 0xFF;
      int br = (rgbB >> 16) & 0xFF;
      int bg = (rgbB >> 8) & 0xFF;
      int bb = rgbB & 0xFF;
      return (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   // ---- Logo -------------------------------------------------------------------


   /** Draws the bundled mod logo with a vector fallback so it remains visible at every GUI scale. */
   public static void drawLogo(DrawContext context, int x, int y, int size, int primaryRgb, float alpha) {
      int a = (int)(alpha * 255.0F);
      if (a <= 4) return;
      int theme = a << 24 | (primaryRgb & 0xFFFFFF);
      int radius = Math.max(4, size / 4);
      roundedRect(context, x, y, size, size, radius, a * 60 / 100 << 24 | 0x0C0A14);
      roundedOutline(context, x, y, size, size, radius, a * 70 / 100 << 24 | (primaryRgb & 0xFFFFFF));
      // Crisp theme-colored "V" mark: two diagonal strokes meeting at the bottom center.
      int stroke = Math.max(2, size / 5);
      int top = y + size / 4;
      int bottom = y + size * 3 / 4;
      int leftX = x + size / 4;
      int rightX = x + size * 3 / 4 - stroke;
      for (int i = 0; i <= bottom - top; i++) {
         int yy = top + i;
         int lx = leftX + i / 2;
         int rx = rightX - i / 2;
         context.fill(lx, yy, lx + stroke, yy + 1, theme);
         context.fill(rx, yy, rx + stroke, yy + 1, theme);
      }
   }

   /** Draws crisp, small vector icons for the sidebar; avoids scaling the 64px source sprites. */
   public static void sidebarIcon(DrawContext context, int index, int x, int y, int size, int argb) {
      int c = argb;
      int s6 = Math.max(2, size / 6);
      switch (index) {
         case 10: { // player model: humanoid silhouette
            int mid = x + size / 2;
            // head
            roundedRect(context, mid - 2, y + 1, 4, 3, 1, c);
            // torso
            context.fill(mid - 2, y + 5, mid + 2, y + 9, c);
            // arms
            context.fill(mid - 4, y + 5, mid - 2, y + 9, c);
            context.fill(mid + 2, y + 5, mid + 4, y + 9, c);
            // legs
            context.fill(mid - 2, y + 9, mid - 1, y + size, c);
            context.fill(mid + 1, y + 9, mid + 2, y + size, c);
            break;
         }
         case 0: { // modules: 2x2 grid of rounded squares
            int cell = size / 2 - 1;
            roundedRect(context, x + 1, y + 1, cell, cell, 2, c);
            roundedRect(context, x + size - 1 - cell, y + 1, cell, cell, 2, c);
            roundedRect(context, x + 1, y + size - 1 - cell, cell, cell, 2, c);
            roundedRect(context, x + size - 1 - cell, y + size - 1 - cell, cell, cell, 2, c);
            break;
         }
         case 1: { // screenshots: camera body + lens ring
            roundedRect(context, x + 1, y + 3, size - 2, size - 5, 2, c);
            int lens = Math.max(3, size / 3);
            int lx = x + (size - lens) / 2;
            int ly = y + (size - lens) / 2 + 1;
            // Lens drawn as a contrasting ring: invert brightness of the icon color.
            int ringCol = (((c >> 16 & 0xFF) + 128) & 0xFF) << 16 | (((c >> 8 & 0xFF) + 128) & 0xFF) << 8 | ((c & 0xFF) + 128 & 0xFF) | c & 0xFF000000;
            roundedOutline(context, lx, ly, lens, lens, 1, ringCol);
            // small viewfinder bump
            context.fill(x + size / 2 - 2, y + 1, x + size / 2 + 2, y + 3, c);
            break;
         }
         case 2: { // backgrounds: picture frame with mountain + sun
            roundedOutline(context, x + 1, y + 2, size - 2, size - 4, 2, c);
            // sun
            context.fill(x + size - 5, y + 4, x + size - 3, y + 6, c);
            // mountain slope
            for (int r = 0; r < 3; r++) {
               context.fill(x + 3 + r, y + size - 5 - r, x + 5 + r * 2, y + size - 4 - r, c);
            }
            context.fill(x + 3, y + size - 5, x + size - 4, y + size - 4, c);
            break;
         }
         case 3: { // settings: gear = ring + 4 teeth + center hub
            roundedOutline(context, x + 2, y + 2, size - 4, size - 4, 3, c);
            context.fill(x + size / 2 - 1, y, x + size / 2 + 1, y + 3, c);           // top tooth
            context.fill(x + size / 2 - 1, y + size - 3, x + size / 2 + 1, y + size, c); // bottom
            context.fill(x, y + size / 2 - 1, x + 3, y + size / 2 + 1, c);           // left
            context.fill(x + size - 3, y + size / 2 - 1, x + size, y + size / 2 + 1, c); // right
            context.fill(x + size / 2 - 1, y + size / 2 - 1, x + size / 2 + 1, y + size / 2 + 1, c); // hub
            break;
         }
         case 4: // friends
            roundedRect(context, x + size / 2 - 2, y + 2, 4, 4, 2, c);
            roundedRect(context, x + 2, y + 7, size - 4, 4, 2, c);
            break;
         case 5: // config sliders
            context.fill(x + 2, y + 2, x + size - 2, y + 3, c);
            context.fill(x + 2, y + size / 2, x + size - 2, y + size / 2 + 1, c);
            context.fill(x + 2, y + size - 3, x + size - 2, y + size - 2, c);
            context.fill(x + size / 3, y + 1, x + size / 3 + 2, y + 4, c);
            context.fill(x + size * 2 / 3, y + size / 2 - 1, x + size * 2 / 3 + 2, y + size / 2 + 2, c);
            break;
         case 6: // statistics
            context.fill(x + 2, y + size - 3, x + 4, y + size - 2, c);
            context.fill(x + size / 2 - 1, y + size / 2, x + size / 2 + 1, y + size - 2, c);
            context.fill(x + size - 4, y + 2, x + size - 2, y + size - 2, c);
            break;
         case 7: // notes
            roundedOutline(context, x + 2, y + 1, size - 4, size - 2, 1, c);
            context.fill(x + 4, y + 5, x + size - 4, y + 6, c);
            context.fill(x + 4, y + 8, x + size - 4, y + 9, c);
            break;
         case 8: { // calculator: body + display + 2x2 buttons
            roundedOutline(context, x + 2, y + 1, size - 4, size - 2, 2, c);
            context.fill(x + 4, y + 3, x + size - 4, y + 5, c); // display
            int bs = 2;
            int gap = 2;
            int bx0 = x + 4;
            int by0 = y + 7;
            for (int r = 0; r < 2; r++) {
               for (int q = 0; q < 2; q++) {
                  context.fill(bx0 + q * (bs + gap), by0 + r * (bs + gap), bx0 + q * (bs + gap) + bs, by0 + r * (bs + gap) + bs, c);
               }
            }
            break;
         }
         default: { // textures: frame + checkerboard quadrants
            roundedOutline(context, x + 2, y + 2, size - 4, size - 4, 2, c);
            int half = (size - 4) / 2;
            int ix = x + 3;
            int iy = y + 3;
            context.fill(ix, iy, ix + half, iy + half, c);
            context.fill(ix + half + 1, iy + half + 1, ix + half * 2 + 1, iy + half * 2 + 1, c);
            break;
         }
      }
   }

   private static final java.util.Map<String, Identifier> ICONS = new java.util.HashMap<>();

   private static double coverage(NativeImage src, int x, int y) {
      int c = src.getColorArgb(x, y);
      double al = (c >>> 24) / 255.0;
      double lum = (((c >> 16) & 255) + ((c >> 8) & 255) + (c & 255)) / 765.0;
      return Math.max(0.0, Math.min(1.0, (al * (1.0 - lum) - 0.25) / 0.5));
   }

   /** Loads an icon image, drops its background, crops to the glyph and downsizes to a white 64px mask (cached). */
   private static Identifier loadIcon(String key) {
      if (ICONS.containsKey(key)) return ICONS.get(key);
      ICONS.put(key, null);
      try (java.io.InputStream in = GuiStyle.class.getResourceAsStream("/assets/voidcyan/textures/gui/tabicons/" + key + ".png")) {
         NativeImage src = NativeImage.read(in);
         int sw = src.getWidth();
         int sh = src.getHeight();
         double[] col = new double[sw];
         double[] row = new double[sh];
         double maxCol = 0.0, maxRow = 0.0;
         for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
               double cv = coverage(src, x, y);
               col[x] += cv;
               row[y] += cv;
            }
         }

         for (double v : col) maxCol = Math.max(maxCol, v);
         for (double v : row) maxRow = Math.max(maxRow, v);
         int minX = sw, minY = sh, maxX = -1, maxY = -1;
         // Ignore stray specks / watermark captions: only rows and columns with real ink count.
         for (int x = 0; x < sw; x++) {
            if (col[x] >= Math.max(2.0, maxCol * 0.12)) {
               minX = Math.min(minX, x);
               maxX = Math.max(maxX, x);
            }
         }

         for (int y = 0; y < sh; y++) {
            if (row[y] >= Math.max(2.0, maxRow * 0.12)) {
               minY = Math.min(minY, y);
               maxY = Math.max(maxY, y);
            }
         }

         if (maxX < 0 || maxY < 0) {
            minX = 0;
            minY = 0;
            maxX = sw - 1;
            maxY = sh - 1;
         }

         int bw = maxX - minX + 1;
         int bh = maxY - minY + 1;
         int side = Math.max(bw, bh);
         int offX = minX - (side - bw) / 2;
         int offY = minY - (side - bh) / 2;
         int n = 64;
         NativeImage dst = new NativeImage(n, n, true);
         for (int py = 0; py < n; py++) {
            for (int px = 0; px < n; px++) {
               int x0 = offX + px * side / n;
               int x1 = Math.max(x0 + 1, offX + (px + 1) * side / n);
               int y0 = offY + py * side / n;
               int y1 = Math.max(y0 + 1, offY + (py + 1) * side / n);
               double sum = 0.0;
               int cnt = 0;
               for (int yy = y0; yy < y1; yy++) {
                  for (int xx = x0; xx < x1; xx++) {
                     cnt++;
                     if (xx >= 0 && yy >= 0 && xx < sw && yy < sh) sum += coverage(src, xx, yy);
                  }
               }

               dst.setColorArgb(px, py, (int)(sum / cnt * 255.0) << 24 | 0xFFFFFF);
            }
         }

         src.close();
         Identifier id = Identifier.of("voidcyan", "icon_" + key);
         net.minecraft.client.MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(null, dst));
         ICONS.put(key, id);
      } catch (Exception e) {
         e.printStackTrace();
      }

      return ICONS.get(key);
   }

   private static boolean drawIcon(DrawContext context, String key, int x, int y, int size, int argb) {
      Identifier id = loadIcon(key);
      if (id == null) return false;
      context.drawTexture(RenderPipelines.GUI_TEXTURED, id, x, y, 0.0F, 0.0F, size, size, 64, 64, 64, 64, argb);
      return true;
   }

   /** Draws a user-supplied tab icon (white mask tinted by argb). Returns false when the tab has none. */
   public static boolean tabIcon(DrawContext context, int tab, int x, int y, int size, int argb) {
      return tab >= 1 && tab <= 7 && drawIcon(context, "tab" + tab, x, y, size, argb);
   }

   /** Small icons for the Square sidebar category rows. */
   public static void categoryIcon(DrawContext context, int index, int x, int y, int size, int argb) {
      if (drawIcon(context, "cat" + index, x, y, size, argb)) return;
      switch (index) {
         case 1 -> {
            for (int i = 0; i < size; i++) {
               context.fill(x + i, y + i, x + i + 2, y + i + 1, argb);
               context.fill(x + size - 2 - i, y + i, x + size - i, y + i + 1, argb);
            }

            context.fill(x, y + size - 3, x + 4, y + size - 2, argb);
            context.fill(x + size - 4, y + size - 3, x + size, y + size - 2, argb);
         }
         case 2 -> {
            roundedOutline(context, x + 1, y + 2, size - 2, size - 3, 2, argb);
            context.fill(x + 1, y + 5, x + size - 1, y + 6, argb);
            context.fill(x + size / 2 - 1, y + 2, x + size / 2 + 1, y + 6, argb);
         }
         case 3 -> {
            roundedOutline(context, x, y, size, size, size / 2, argb);
            context.fill(x + size / 2, y + 1, x + size / 2 + 1, y + size - 1, argb);
            context.fill(x + 1, y + size / 2, x + size - 1, y + size / 2 + 1, argb);
         }
         case 4 -> {
            roundedOutline(context, x, y + 1, size, size - 4, 2, argb);
            context.fill(x + size / 2 - 3, y + size - 2, x + size / 2 + 3, y + size - 1, argb);
            context.fill(x + 3, y + 4, x + size - 3, y + 5, argb);
         }
         case 5 -> sidebarIcon(context, 4, x, y, size, argb);
         default -> sidebarIcon(context, 0, x, y, size, argb);
      }
   }

   /** Draws a simple white glyph from a 5x5 char map, used for sidebar icon buttons. */
   public static void glyph(DrawContext context, int x, int y, int scale, char[][] map, int argb) {
      for (int row = 0; row < map.length; row++) {
         for (int col = 0; col < map[row].length; col++) {
            if (map[row][col] == '#') {
               context.fill(x + col * scale, y + row * scale, x + (col + 1) * scale, y + (row + 1) * scale, argb);
            }
         }
      }
   }

   // Glyph maps (5 wide)
   public static final char[][] GLYPH_SEARCH = {
      {'.', '#', '#', '#', '.'},
      {'#', '.', '.', '.', '#'},
      {'#', '.', '.', '.', '#'},
      {'#', '.', '.', '.', '#'},
      {'.', '#', '.', '.', '.'}
   };
}
