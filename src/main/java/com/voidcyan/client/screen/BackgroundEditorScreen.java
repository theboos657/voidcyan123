package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Small paint editor for making GUI backgrounds: brush, eraser, fill, shapes, text; saves into voidcyan/background. */
public class BackgroundEditorScreen extends Screen {
   private static final int CW = 960;
   private static final int CH = 540;
   private static final String[] TOOLS = {"Brush", "Eraser", "Fill", "Line", "Rect", "Circle", "Text"};

   private final Screen parent;
   private final BufferedImage img = new BufferedImage(CW, CH, BufferedImage.TYPE_INT_ARGB);
   private final ArrayDeque<BufferedImage> undo = new ArrayDeque<>();
   private NativeImageBackedTexture tex;
   private Identifier texId;
   private boolean dirty = true;

   private int tool = 0;
   private int color = 0xFFE94560;
   private int size = 8;
   private boolean filled = false;
   private ColorPickerModal picker;

   private boolean drawing;
   private int startX, startY, lastX, lastY;
   private BufferedImage snapshot;

   private boolean typing;
   private int textX, textY;
   private String textBuf = "";
   private String status = "";

   // Toolbar hit boxes, rebuilt each render: {x, y, w, h, id}
   private final List<int[]> buttons = new ArrayList<>();
   private int canvasX, canvasY, canvasW, canvasH;

   public BackgroundEditorScreen(Screen parent) {
      super(Text.literal("Background Editor"));
      this.parent = parent;
      Graphics2D g = this.img.createGraphics();
      g.setColor(new Color(0x14101C));
      g.fillRect(0, 0, CW, CH);
      g.dispose();
   }

   /** Opens the editor with an existing picture (e.g. a screenshot) fitted onto the canvas. */
   public BackgroundEditorScreen(Screen parent, File source) {
      this(parent);
      try {
         BufferedImage src = javax.imageio.ImageIO.read(source);
         if (src != null) {
            Graphics2D g = this.img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double sc = Math.min((double) CW / src.getWidth(), (double) CH / src.getHeight());
            int w = (int) (src.getWidth() * sc);
            int h = (int) (src.getHeight() * sc);
            g.drawImage(src, (CW - w) / 2, (CH - h) / 2, w, h, null);
            g.dispose();
         }
      } catch (Exception ignored) {
      }
   }

   @Override
   protected void init() {
      if (this.tex == null) {
         NativeImage ni = new NativeImage(CW, CH, true);
         this.tex = new NativeImageBackedTexture(null, ni);
         this.texId = Identifier.of("voidcyan", "bg_editor");
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.texId, this.tex);
      }
   }

   @Override
   public void removed() {
      if (this.tex != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(this.texId);
         this.tex = null;
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   private void upload() {
      NativeImage ni = this.tex.getImage();
      if (ni == null) return;
      for (int y = 0; y < CH; y++) {
         for (int x = 0; x < CW; x++) {
            ni.setColorArgb(x, y, this.img.getRGB(x, y));
         }
      }

      this.tex.upload();
      this.dirty = false;
   }

   private static BufferedImage copy(BufferedImage src) {
      BufferedImage c = new BufferedImage(CW, CH, BufferedImage.TYPE_INT_ARGB);
      c.getGraphics().drawImage(src, 0, 0, null);
      return c;
   }

   private void pushUndo(BufferedImage snap) {
      this.undo.push(snap);
      while (this.undo.size() > 20) this.undo.removeLast();
   }

   private Graphics2D gfx() {
      Graphics2D g = this.img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setStroke(new BasicStroke(this.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.setColor(new Color(this.color, true));
      return g;
   }

   private int addButton(int x, int y, int h, String label, int id, boolean active, int mx, int my, DrawContext ctx) {
      int w = this.textRenderer.getWidth(label) + 14;
      boolean hov = GuiStyle.inRect(mx, my, x, y, w, h);
      int primary = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;
      GuiStyle.roundedBordered(ctx, x, y, w, h, 6, active ? 0x66000000 | primary : hov ? 0x44FFFFFF : 0x26FFFFFF, active ? 0xFF000000 | primary : 0x33FFFFFF);
      GuiStyle.text(ctx, this.textRenderer, label, x + 7, y + (h - 8) / 2, active ? 0xFFFFFFFF : 0xE0E6EAF2);
      this.buttons.add(new int[]{x, y, w, h, id});
      return w + 4;
   }

   @Override
   public void render(DrawContext ctx, int mx, int my, float delta) {
      ctx.fill(0, 0, this.width, this.height, 0xFF0B0D12);
      this.buttons.clear();
      int primary = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;
      int bx = 10;
      int by = 8;
      int bh = 18;
      bx += this.addButton(bx, by, bh, "< Back", 100, false, mx, my, ctx);
      bx += 8;
      for (int i = 0; i < TOOLS.length; i++) {
         bx += this.addButton(bx, by, bh, TOOLS[i], i, this.tool == i, mx, my, ctx);
      }

      bx += 8;
      GuiStyle.roundedBordered(ctx, bx, by, 26, bh, 6, this.color, 0xAAFFFFFF);
      this.buttons.add(new int[]{bx, by, 26, bh, 110});
      bx += 30;
      bx += this.addButton(bx, by, bh, "-", 111, false, mx, my, ctx);
      String sz = String.valueOf(this.size);
      GuiStyle.text(ctx, this.textRenderer, sz, bx + 2, by + 5, 0xFFFFFFFF);
      bx += this.textRenderer.getWidth(sz) + 10;
      bx += this.addButton(bx, by, bh, "+", 112, false, mx, my, ctx);
      bx += this.addButton(bx, by, bh, "Fill shape", 113, this.filled, mx, my, ctx);
      bx += 8;
      bx += this.addButton(bx, by, bh, "Undo", 120, false, mx, my, ctx);
      bx += this.addButton(bx, by, bh, "Clear", 121, false, mx, my, ctx);
      bx += this.addButton(bx, by, bh, "Save", 122, false, mx, my, ctx);

      // Canvas fit.
      int availW = this.width - 20;
      int availH = this.height - 60;
      int cw = availW;
      int ch = cw * CH / CW;
      if (ch > availH) {
         ch = availH;
         cw = ch * CW / CH;
      }

      this.canvasW = cw;
      this.canvasH = ch;
      this.canvasX = (this.width - cw) / 2;
      this.canvasY = 34 + (availH - ch) / 2;
      GuiStyle.roundedBordered(ctx, this.canvasX - 3, this.canvasY - 3, cw + 6, ch + 6, 6, 0xFF0F1118, 0xFF000000 | primary);
      if (this.dirty && this.tex != null) this.upload();
      if (this.tex != null) {
         ctx.drawTexture(RenderPipelines.GUI_TEXTURED, this.texId, this.canvasX, this.canvasY, 0.0F, 0.0F, cw, ch, cw, ch, 0xFFFFFFFF);
      }

      if (this.typing) {
         float sc = (float)cw / CW;
         int px = this.canvasX + (int)(this.textX * sc);
         int py = this.canvasY + (int)(this.textY * sc);
         String shown = this.textBuf + (System.currentTimeMillis() / 500L % 2L == 0L ? "|" : "");
         GuiStyle.text(ctx, this.textRenderer, shown, px, py, this.color | 0xFF000000);
      }

      String hint = this.typing ? "Type text, Enter to place, Esc to cancel" : this.status;
      GuiStyle.text(ctx, this.textRenderer, hint, 12, this.height - 14, 0xA0FFFFFF);
      if (this.picker != null) {
         if (!this.picker.isOpen()) this.picker = null;
         else this.picker.render(ctx, mx, my, this.width, this.height);
      }
   }

   private boolean onCanvas(double mx, double my) {
      return mx >= this.canvasX && my >= this.canvasY && mx < this.canvasX + this.canvasW && my < this.canvasY + this.canvasH;
   }

   private int cx(double mx) {
      return (int)Math.max(0, Math.min(CW - 1, (mx - this.canvasX) * CW / this.canvasW));
   }

   private int cy(double my) {
      return (int)Math.max(0, Math.min(CH - 1, (my - this.canvasY) * CH / this.canvasH));
   }

   private void commitText() {
      if (!this.textBuf.isEmpty()) {
         this.pushUndo(copy(this.img));
         Graphics2D g = this.gfx();
         g.setFont(new Font("SansSerif", Font.BOLD, this.size * 3 + 12));
         g.drawString(this.textBuf, this.textX, this.textY + g.getFontMetrics().getAscent() / 2 + 8);
         g.dispose();
         this.dirty = true;
      }

      this.typing = false;
      this.textBuf = "";
   }

   private void floodFill(int sx, int sy) {
      int target = this.img.getRGB(sx, sy);
      int repl = this.color;
      if (target == repl) return;
      this.pushUndo(copy(this.img));
      ArrayDeque<int[]> stack = new ArrayDeque<>();
      stack.push(new int[]{sx, sy});
      while (!stack.isEmpty()) {
         int[] p = stack.pop();
         int x = p[0];
         int y = p[1];
         if (this.img.getRGB(x, y) != target) continue;
         int l = x;
         int r = x;
         while (l > 0 && this.img.getRGB(l - 1, y) == target) l--;
         while (r < CW - 1 && this.img.getRGB(r + 1, y) == target) r++;
         for (int i = l; i <= r; i++) {
            this.img.setRGB(i, y, repl);
            if (y > 0 && this.img.getRGB(i, y - 1) == target) stack.push(new int[]{i, y - 1});
            if (y < CH - 1 && this.img.getRGB(i, y + 1) == target) stack.push(new int[]{i, y + 1});
         }
      }

      this.dirty = true;
   }

   private void save() {
      try {
         File dir = new File(MinecraftClient.getInstance().runDirectory, "voidcyan/background");
         dir.mkdirs();
         File f = new File(dir, "painted_" + System.currentTimeMillis() + ".png");
         javax.imageio.ImageIO.write(this.img, "png", f);
         this.status = "Saved " + f.getName() + " (press Refresh in Backgrounds)";
      } catch (Exception e) {
         this.status = "Save failed: " + e.getMessage();
      }
   }

   private void button(int id) {
      if (id < TOOLS.length) {
         if (this.typing) this.commitText();
         this.tool = id;
      } else switch (id) {
         case 100 -> MinecraftClient.getInstance().setScreen(this.parent);
         case 110 -> this.picker = new ColorPickerModal(this.width / 2, this.height / 2, this.color, "Brush color", "", argb -> this.color = argb | 0xFF000000);
         case 111 -> this.size = Math.max(1, this.size - 2);
         case 112 -> this.size = Math.min(80, this.size + 2);
         case 113 -> this.filled = !this.filled;
         case 120 -> {
            if (!this.undo.isEmpty()) {
               Graphics2D g = this.img.createGraphics();
               g.setComposite(java.awt.AlphaComposite.Src);
               g.drawImage(this.undo.pop(), 0, 0, null);
               g.dispose();
               this.dirty = true;
            }
         }
         case 121 -> {
            this.pushUndo(copy(this.img));
            Graphics2D g = this.img.createGraphics();
            g.setColor(new Color(this.color, true));
            g.fillRect(0, 0, CW, CH);
            g.dispose();
            this.dirty = true;
         }
         case 122 -> this.save();
         default -> {
         }
      }
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      if (this.picker != null) {
         boolean c = this.picker.mouseClicked(click);
         if (!this.picker.isOpen()) this.picker = null;
         if (c) return true;
      }

      double mx = click.x();
      double my = click.y();
      for (int[] b : this.buttons) {
         if (GuiStyle.inRect(mx, my, b[0], b[1], b[2], b[3])) {
            this.button(b[4]);
            return true;
         }
      }

      if (click.button() != 0 || !this.onCanvas(mx, my)) return super.mouseClicked(click, doubled);
      int x = this.cx(mx);
      int y = this.cy(my);
      if (this.typing) this.commitText();
      switch (this.tool) {
         case 2 -> this.floodFill(x, y);
         case 6 -> {
            this.typing = true;
            this.textX = x;
            this.textY = y;
            this.textBuf = "";
         }
         default -> {
            this.pushUndo(copy(this.img));
            this.snapshot = copy(this.img);
            this.drawing = true;
            this.startX = this.lastX = x;
            this.startY = this.lastY = y;
            this.stroke(x, y);
         }
      }

      return true;
   }

   private void stroke(int x, int y) {
      Graphics2D g = this.gfx();
      if (this.tool == 1) {
         g.setComposite(java.awt.AlphaComposite.Src);
         g.setColor(new Color(0x14101C));
      }

      if (this.tool <= 1) {
         g.drawLine(this.lastX, this.lastY, x, y);
      } else {
         g.setComposite(java.awt.AlphaComposite.Src);
         g.drawImage(this.snapshot, 0, 0, null);
         g.setComposite(java.awt.AlphaComposite.SrcOver);
         int rx = Math.min(this.startX, x);
         int ry = Math.min(this.startY, y);
         int rw = Math.abs(x - this.startX);
         int rh = Math.abs(y - this.startY);
         switch (this.tool) {
            case 3 -> g.drawLine(this.startX, this.startY, x, y);
            case 4 -> {
               if (this.filled) g.fillRect(rx, ry, rw, rh);
               else g.drawRect(rx, ry, rw, rh);
            }
            case 5 -> {
               if (this.filled) g.fillOval(rx, ry, rw, rh);
               else g.drawOval(rx, ry, rw, rh);
            }
            default -> {
            }
         }
      }

      g.dispose();
      this.lastX = x;
      this.lastY = y;
      this.dirty = true;
   }

   @Override
   public boolean mouseDragged(Click click, double dx, double dy) {
      if (this.picker != null && this.picker.mouseDragged(click)) return true;
      if (this.drawing) {
         this.stroke(this.cx(click.x()), this.cy(click.y()));
         return true;
      }

      return super.mouseDragged(click, dx, dy);
   }

   @Override
   public boolean mouseReleased(Click click) {
      if (this.picker != null) this.picker.mouseReleased();
      this.drawing = false;
      return super.mouseReleased(click);
   }

   @Override
   public boolean charTyped(CharInput input) {
      if (this.picker != null && this.picker.charTyped(input)) return true;
      if (this.typing) {
         this.textBuf += new String(Character.toChars(input.codepoint()));
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
      if (this.typing) {
         if (k == 256) {
            this.typing = false;
            this.textBuf = "";
         } else if (k == 257 || k == 335) {
            this.commitText();
         } else if (k == 259 && !this.textBuf.isEmpty()) {
            this.textBuf = this.textBuf.substring(0, this.textBuf.length() - 1);
         }

         return true;
      }

      if (k == 256) {
         MinecraftClient.getInstance().setScreen(this.parent);
         return true;
      }

      return super.keyPressed(input);
   }
}
