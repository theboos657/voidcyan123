package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

/**
 * Modern color picker modal (styled after the reference design):
 *  - SV gradient square with draggable cursor
 *  - Vertical hue strip
 *  - HEX / R / G / B / A editable fields
 *  - Alpha slider with % readout
 *  - Preset swatches row
 *  - Reset + Apply buttons
 * The host screen forwards mouse/key/char events while the modal is open and
 * closes it when {@link #isOpen()} returns false.
 */
public class ColorPickerModal {
   public static final int[] PRESETS = new int[]{
      0xFF6C5CE7, 0xFF00CEC9, 0xFFA29BFE, 0xFFFD79A8, 0xFFE17055, 0xFFFDCB6E, 0xFFE84393, 0xFF00B894, 0xFF2D3436, 0xFFFFFFFF
   };

   /** Callback receives the final ARGB color when Apply is pressed. */
   public interface ApplyCallback {
      void onApply(int argb);
   }

   private final int width = 300;
   private final int height = 336;
   private final String title;
   private final String subtitle;
   private final int initialArgb;
   private final ApplyCallback onApply;
   private boolean open = true;

   // HSV state (h 0-1, s 0-1, v 0-1) + alpha 0-255.
   private float hue = 0.0F;
   private float sat = 1.0F;
   private float val = 1.0F;
   private int alpha = 255;

   // Field editing.
   private static final int F_NONE = -1;
   private static final int F_HEX = 0;
   private static final int F_R = 1;
   private static final int F_G = 2;
   private static final int F_B = 3;
   private static final int F_A = 4;
   private int focusedField = F_NONE;
   private String fieldText = "";
   private boolean draggingSv = false;
   private boolean draggingHue = false;
   private boolean draggingAlpha = false;

   // Computed geometry (refreshed each render).
   private int x;
   private int y;
   private int svX;
   private int svY;
   private int svS = 190;
   private int hueX;
   private int hueStripW = 14;
   private int alphaTrackX;
   private int alphaTrackY;
   private int alphaTrackW;
   private int resetY;

   public ColorPickerModal(int centerX, int centerY, int initialArgb, String title, String subtitle, ApplyCallback onApply) {
      this.x = centerX - this.width / 2;
      this.y = centerY - this.height / 2;
      this.initialArgb = initialArgb;
      this.title = title;
      this.subtitle = subtitle;
      this.onApply = onApply;
      this.setArgb(initialArgb, false);
   }

   // ================= Color conversions =================

   private static int hsvToRgb(float h, float s, float v) {
      return java.awt.Color.HSBtoRGB(h, s, v) & 0x00FFFFFF;
   }

   private static float[] rgbToHsv(int rgb) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
      return new float[]{hsb[0], hsb[1], hsb[2]};
   }

   public int currentArgb() {
      return (this.alpha & 0xFF) << 24 | hsvToRgb(this.hue, this.sat, this.val);
   }

   private void setArgb(int argb, boolean keepAlpha) {
      float[] hsv = rgbToHsv(argb);
      this.hue = hsv[0];
      this.sat = hsv[1];
      this.val = hsv[2];
      if (!keepAlpha) {
         this.alpha = argb >>> 24 & 0xFF;
         if (this.alpha == 0) this.alpha = 255;
      }
   }

   // ================= Layout =================

   private void computeLayout() {
      this.svX = this.x + 14;
      this.svY = this.y + 58;
      this.svS = 190;
      this.hueX = this.svX + this.svS + 10;
      this.alphaTrackX = this.svX;
      this.alphaTrackY = this.svY + this.svS + 34;
      this.alphaTrackW = this.width - 28;
      this.resetY = this.y + this.height - 34;
   }

   private static TextRenderer tr() {
      return MinecraftClient.getInstance().textRenderer;
   }

   // ================= Rendering =================

   public void render(DrawContext context, int mouseX, int mouseY, int screenW, int screenH) {
      this.computeLayout();
      TextRenderer tr = tr();
      int prim = VoidCyanClient.getPrimaryColor() & 0x00FFFFFF;

      // Dim backdrop.
      context.fill(0, 0, screenW, screenH, 140 << 24);

      // Modal panel.
      int OPA = 0xFF000000;
      GuiStyle.roundedRect(context, this.x, this.y, this.width, this.height, 8, OPA | 0x16121E);
      GuiStyle.roundedOutline(context, this.x, this.y, this.width, this.height, 8, (110 << 24) | prim);

      // Title block with palette icon.
      GuiStyle.roundedRect(context, this.x + 14, this.y + 12, 30, 30, 6, (90 << 24) | prim);
      context.fill(this.x + 21, this.y + 20, this.x + 24, this.y + 23, 0xFFFFFFFF);
      context.fill(this.x + 27, this.y + 26, this.x + 30, this.y + 29, 0xFFFFFFFF);
      context.fill(this.x + 21, this.y + 31, this.x + 24, this.y + 34, 0xFFFFFFFF);
      GuiStyle.text(context, tr, this.title, this.x + 52, this.y + 15, OPA | 0xFFFFFF);
      if (this.subtitle != null && !this.subtitle.isEmpty()) {
         GuiStyle.text(context, tr, this.subtitle, this.x + 52, this.y + 26, (150 << 24) | 0xFFFFFF);
      }

      // Close X.
      boolean xHov = mouseX >= this.x + this.width - 22 && mouseX <= this.x + this.width - 8 && mouseY >= this.y + 10 && mouseY <= this.y + 24;
      GuiStyle.text(context, tr, "✕", this.x + this.width - 21, this.y + 13, ((xHov ? 255 : 150) << 24) | 0xFFFFFF);

      // ---- SV square: hue fill + white gradient (left) + black gradient (top).
      int baseHue = OPA | hsvToRgb(this.hue, 1.0F, 1.0F);
      context.fill(this.svX, this.svY, this.svX + this.svS, this.svY + this.svS, baseHue);
      int steps = 24;
      int wStep = this.svS / steps;
      for (int i = 0; i < steps; i++) {
         int w = i * wStep;
         int aw = 255 - i * 255 / steps;
         context.fill(this.svX + w, this.svY, this.svX + w + wStep + 1, this.svY + this.svS, (aw << 24) | 0xFFFFFF);
      }
      for (int i = 0; i < steps; i++) {
         int hh = i * wStep;
         int ab = 255 - i * 255 / steps;
         context.fill(this.svX, this.svY + hh, this.svX + this.svS, this.svY + hh + wStep + 1, ab << 24);
      }
      GuiStyle.roundedOutline(context, this.svX, this.svY, this.svS, this.svS, 2, (150 << 24) | 0xFFFFFF);
      int curX = this.svX + (int)(this.sat * this.svS);
      int curY = this.svY + (int)((1.0F - this.val) * this.svS);
      context.fill(curX - 3, curY - 1, curX + 4, curY + 1, 0xFFFFFFFF);
      context.fill(curX - 1, curY - 3, curX + 1, curY + 4, 0xFFFFFFFF);

      // ---- Hue strip.
      for (int i = 0; i < this.svS; i++) {
         float h = 1.0F - (float)i / this.svS;
         int col = OPA | hsvToRgb(h, 1.0F, 1.0F);
         context.fill(this.hueX, this.svY + i, this.hueX + this.hueStripW, this.svY + i + 1, col);
      }
      GuiStyle.roundedOutline(context, this.hueX, this.svY, this.hueStripW, this.svS, 2, (120 << 24) | 0xFFFFFF);
      int hueCurY = this.svY + (int)((1.0F - this.hue) * this.svS);
      context.fill(this.hueX - 2, hueCurY - 1, this.hueX + this.hueStripW + 2, hueCurY + 1, 0xFFFFFFFF);
      context.fill(this.hueX - 1, hueCurY - 2, this.hueX + this.hueStripW + 1, hueCurY + 2, 0xFF000000);

      // ---- HEX / RGBA fields.
      int rgb = hsvToRgb(this.hue, this.sat, this.val);
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      int fieldX = this.hueX + this.hueStripW + 10;
      int fieldW = this.x + this.width - 14 - fieldX;
      this.renderField(context, F_HEX, String.format("#%02X%02X%02X", r, g, b), fieldX, this.svY, fieldW);
      this.renderField(context, F_R, String.valueOf(r), fieldX, this.svY + 30, fieldW);
      this.renderField(context, F_G, String.valueOf(g), fieldX, this.svY + 60, fieldW);
      this.renderField(context, F_B, String.valueOf(b), fieldX, this.svY + 90, fieldW);
      this.renderField(context, F_A, String.valueOf(this.alpha), fieldX, this.svY + 120, fieldW);

      // ---- Alpha slider row (checker + current color + knob).
      int argbNow = this.currentArgb();
      for (int i = 0; i < this.alphaTrackW; i++) {
         int checker = (i / 6) % 2 == 0 ? 0xFF3A3442 : 0xFF241F2E;
         context.fill(this.alphaTrackX + i, this.alphaTrackY, this.alphaTrackX + i + 1, this.alphaTrackY + 10, checker);
      }
      context.fill(this.alphaTrackX, this.alphaTrackY, this.alphaTrackX + this.alphaTrackW, this.alphaTrackY + 10, (this.alpha << 24) | (argbNow & 0x00FFFFFF));
      GuiStyle.roundedOutline(context, this.alphaTrackX, this.alphaTrackY, this.alphaTrackW, 10, 2, (120 << 24) | 0xFFFFFF);
      int knobX = this.alphaTrackX + (int)((float)this.alpha / 255.0F * (this.alphaTrackW - 6));
      GuiStyle.roundedRect(context, knobX, this.alphaTrackY - 2, 6, 14, 2, OPA | 0xFFFFFF);
      GuiStyle.text(context, tr, "Alpha", this.alphaTrackX, this.alphaTrackY - 11, (170 << 24) | 0xFFFFFF);
      String pct = this.alpha * 100 / 255 + "%";
      GuiStyle.text(context, tr, pct, this.alphaTrackX + this.alphaTrackW + 8, this.alphaTrackY + 1, (200 << 24) | 0xFFFFFF);

      // ---- Preview + presets.
      int prevY = this.alphaTrackY + 22;
      int prevS = 20;
      context.fill(this.alphaTrackX, prevY, this.alphaTrackX + prevS, prevY + prevS, argbNow);
      GuiStyle.roundedOutline(context, this.alphaTrackX, prevY, prevS, prevS, 3, (140 << 24) | 0xFFFFFF);
      GuiStyle.text(context, tr, "Presets", this.alphaTrackX + prevS + 10, prevY + 5, (160 << 24) | 0xFFFFFF);
      int pY = prevY + 26;
      int pS = 16;
      int pGap = (this.alphaTrackW - PRESETS.length * pS) / (PRESETS.length - 1);
      for (int i = 0; i < PRESETS.length; i++) {
         int px = this.alphaTrackX + i * (pS + pGap);
         context.fill(px, pY, px + pS, pY + pS, PRESETS[i]);
         if (PRESETS[i] == argbNow) {
            GuiStyle.roundedOutline(context, px - 1, pY - 1, pS + 2, pS + 2, 3, OPA | 0xFFFFFF);
         }
      }

      // ---- Reset / Apply.
      int resetW = 84;
      boolean rHov = mouseX >= this.x + 14 && mouseX <= this.x + 14 + resetW && mouseY >= this.resetY && mouseY <= this.resetY + 22;
      GuiStyle.roundedRect(context, this.x + 14, this.resetY, resetW, 22, 4, ((rHov ? 70 : 40) << 24) | 0xFFFFFF);
      GuiStyle.textCentered(context, tr, "Reset", this.x + 14 + resetW / 2, this.resetY + 7, OPA | 0xFFFFFF);
      int applyX = this.x + 14 + resetW + 10;
      int applyW = this.x + this.width - 14 - applyX;
      boolean aHov = mouseX >= applyX && mouseX <= applyX + applyW && mouseY >= this.resetY && mouseY <= this.resetY + 22;
      GuiStyle.roundedRect(context, applyX, this.resetY, applyW, 22, 4, ((aHov ? 255 : 225) << 24) | prim);
      GuiStyle.textCentered(context, tr, "Apply", applyX + applyW / 2, this.resetY + 7, OPA | 0x14101A);
   }

   private void renderField(DrawContext context, int fieldId, String value, int x, int y, int width) {
      TextRenderer tr = tr();
      int boxH = 18;
      boolean focused = this.focusedField == fieldId;
      boolean hov = false;
      int border = focused ? (230 << 24) | (VoidCyanClient.getPrimaryColor() & 0x00FFFFFF) : ((hov ? 130 : 60) << 24) | 0xFFFFFF;
      GuiStyle.roundedRect(context, x, y, width, boxH, 4, (200 << 24) | 0x0F0C14);
      GuiStyle.roundedOutline(context, x, y, width, boxH, 4, border);
      String label = fieldId == F_HEX ? "HEX" : fieldId == F_R ? "R" : fieldId == F_G ? "G" : fieldId == F_B ? "B" : "A";
      GuiStyle.text(context, tr, label, x - 4 - tr.getWidth(label) - 2, y + 5, (140 << 24) | 0xFFFFFF);
      String shown = focused ? this.fieldText : value;
      if (focused && System.currentTimeMillis() / 500L % 2L == 0L) shown = shown + "_";
      GuiStyle.text(context, tr, shown, x + 6, y + 5, (240 << 24) | 0xFFFFFF);
   }

   // ================= Input =================

   /** Returns true when the click was consumed by the modal. */
   public boolean mouseClicked(Click click) {
      double mx = click.x();
      double my = click.y();
      int button = click.button();
      boolean inside = mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
      if (button != 0) return inside;
      this.computeLayout();
      TextRenderer tr = tr();

      if (mx >= this.x + this.width - 22 && mx <= this.x + this.width - 8 && my >= this.y + 10 && my <= this.y + 24) {
         this.open = false;
         return true;
      }

      if (mx >= this.svX && mx <= this.svX + this.svS && my >= this.svY && my <= this.svY + this.svS) {
         this.draggingSv = true;
         this.applySv(mx, my);
         this.focusedField = F_NONE;
         return true;
      }

      if (mx >= this.hueX - 2 && mx <= this.hueX + this.hueStripW + 2 && my >= this.svY && my <= this.svY + this.svS) {
         this.draggingHue = true;
         this.applyHue(my);
         this.focusedField = F_NONE;
         return true;
      }

      int rgb = hsvToRgb(this.hue, this.sat, this.val);
      int fieldX = this.hueX + this.hueStripW + 10;
      int fieldW = this.x + this.width - 14 - fieldX;
      for (int i = 0; i < 5; i++) {
         int fy = this.svY + i * 30;
         if (mx >= fieldX - 16 && mx <= fieldX + fieldW && my >= fy && my <= fy + 18) {
            this.focusedField = i;
            if (i == F_HEX) {
               this.fieldText = String.format("#%02X%02X%02X", rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
            } else if (i == F_A) {
               this.fieldText = String.valueOf(this.alpha);
            } else {
               this.fieldText = String.valueOf(i == F_R ? rgb >> 16 & 0xFF : i == F_G ? rgb >> 8 & 0xFF : rgb & 0xFF);
            }
            return true;
         }
      }

      if (mx >= this.alphaTrackX && mx <= this.alphaTrackX + this.alphaTrackW + 40 && my >= this.alphaTrackY - 2 && my <= this.alphaTrackY + 14) {
         this.draggingAlpha = true;
         this.applyAlpha(mx);
         this.focusedField = F_NONE;
         return true;
      }

      int prevY = this.alphaTrackY + 22;
      int pY = prevY + 26;
      int pS = 16;
      int pGap = (this.alphaTrackW - PRESETS.length * pS) / (PRESETS.length - 1);
      for (int i = 0; i < PRESETS.length; i++) {
         int px = this.alphaTrackX + i * (pS + pGap);
         if (mx >= px && mx <= px + pS && my >= pY && my <= pY + pS) {
            this.setArgb(PRESETS[i], false);
            this.focusedField = F_NONE;
            return true;
         }
      }

      int resetW = 84;
      if (mx >= this.x + 14 && mx <= this.x + 14 + resetW && my >= this.resetY && my <= this.resetY + 22) {
         this.setArgb(this.initialArgb, false);
         this.focusedField = F_NONE;
         return true;
      }
      int applyX = this.x + 14 + resetW + 10;
      int applyW = this.x + this.width - 14 - applyX;
      if (mx >= applyX && mx <= applyX + applyW && my >= this.resetY && my <= this.resetY + 22) {
         int argb = this.currentArgb();
         this.open = false;
         if (this.onApply != null) this.onApply.onApply(argb);
         return true;
      }

      return inside;
   }

   public boolean mouseDragged(Click click) {
      double mx = click.x();
      double my = click.y();
      if (this.draggingSv) this.applySv(mx, my);
      if (this.draggingHue) this.applyHue(my);
      if (this.draggingAlpha) this.applyAlpha(mx);
      return this.draggingSv || this.draggingHue || this.draggingAlpha;
   }

   public void mouseReleased() {
      this.draggingSv = false;
      this.draggingHue = false;
      this.draggingAlpha = false;
   }

   public boolean charTyped(CharInput input) {
      if (this.focusedField == F_NONE) return false;
      int cp = input.codepoint();
      if (Character.isISOControl(cp)) return false;
      char ch = Character.toChars(cp)[0];
      boolean valid = this.focusedField == F_HEX
         ? Character.isLetterOrDigit(ch) && this.fieldText.length() < 7
         : Character.isDigit(ch) && this.fieldText.length() < 3;
      if (valid) {
         this.fieldText += ch;
         this.commitField();
      }
      return true;
   }

   public boolean keyPressed(KeyInput input) {
      if (this.focusedField == F_NONE) return false;
      int k = input.key();
      if (k == 256) {
         this.focusedField = F_NONE;
         return true;
      }
      if (k == 259) {
         if (!this.fieldText.isEmpty()) {
            this.fieldText = this.fieldText.substring(0, this.fieldText.length() - 1);
            this.commitField();
         }
         return true;
      }
      if (k == 257 || k == 335) {
         this.commitField();
         this.focusedField = F_NONE;
         return true;
      }
      return true;
   }

   private void commitField() {
      try {
         if (this.focusedField == F_HEX) {
            String t = this.fieldText.replace("#", "");
            if (t.length() == 6) {
               int rgb = (int)Long.parseLong(t, 16) & 0xFFFFFF;
               this.setArgb(0xFF000000 | rgb, true);
            }
         } else {
            int v = Math.max(0, Math.min(255, Integer.parseInt(this.fieldText)));
            int rgb = hsvToRgb(this.hue, this.sat, this.val);
            if (this.focusedField == F_R) rgb = v << 16 | rgb & 0x00FFFF;
            if (this.focusedField == F_G) rgb = rgb & 0xFF00FF00 | v << 8 | rgb & 0x0000FF;
            if (this.focusedField == F_B) rgb = rgb & 0xFFFF0000 | v;
            if (this.focusedField == F_A) {
               this.alpha = v;
            } else {
               float[] hsv = rgbToHsv(rgb);
               this.hue = hsv[0];
               this.sat = hsv[1];
               this.val = hsv[2];
            }
         }
      } catch (NumberFormatException ignored) {
      }
   }

   private void applySv(double mx, double my) {
      this.sat = (float)Math.max(0.0, Math.min(1.0, (mx - this.svX) / this.svS));
      this.val = 1.0F - (float)Math.max(0.0, Math.min(1.0, (my - this.svY) / this.svS));
   }

   private void applyHue(double my) {
      this.hue = 1.0F - (float)Math.max(0.0, Math.min(1.0, (my - this.svY) / this.svS));
   }

   private void applyAlpha(double mx) {
      this.alpha = (int)(Math.max(0.0, Math.min(1.0, (mx - this.alphaTrackX) / this.alphaTrackW)) * 255.0F);
   }

   public boolean isOpen() {
      return this.open;
   }
}
