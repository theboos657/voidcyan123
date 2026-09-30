package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * "Orbit" GUI profile: every page sits on a radial wheel. Module categories show their modules in a panel
 * on the right; the other pages (Screenshots, Settings, Extras, ...) open the classic window on that tab.
 * The wheel is drawn per physical pixel with anti-aliased edges so it stays smooth at any GUI scale.
 */
public class OrbitGuiScreen extends Screen {
   private record Seg(String label, int tab, String category) {
   }

   private record Row(String label, Runnable click, BooleanSupplier on, Runnable settings) {
   }

   private static final Seg[] MAIN = {
      new Seg("Modules", -2, null), new Seg("Screenshots", 1, null), new Seg("Backgrounds", 2, null), new Seg("Settings", 3, null),
      new Seg("Friends", 4, null), new Seg("Config", 5, null), new Seg("Stats", 6, null), new Seg("Extras", 7, null)
   };
   private static final Seg[] CATS = {
      new Seg("Combat", -1, "Combat"), new Seg("Visual", -1, "Visual"), new Seg("HUD", -1, "HUD"), new Seg("Player", -1, "Player")
   };
   private Seg[] segs = MAIN;

   private final List<List<Row>> rows = new ArrayList<>();
   private int selected = 0;
   private int scroll = 0;
   private int cx;
   private int cy;
   private int radius;
   private int holeR;
   private float sf = 1.0F;
   private int radiusP;
   private int holeP;
   private int segP;
   private int dim;
   private byte[] cls;
   private byte[] alpha;
   private static final Identifier WHEEL_ID = Identifier.of("voidcyan", "orbit_wheel");
   private NativeImageBackedTexture wheelTex;
   private int lastKey = Integer.MIN_VALUE;

   /** Repaints the cached wheel texture; only runs when the hovered/selected segment changes, so a frame is one textured quad. */
   private void paintWheel(int hover, int primary) {
      int n = this.segs.length;
      int ring = GuiStyle.blend(0x120D1C, primary, 0.22F);
      int rim = GuiStyle.blend(0x120D1C, primary, 0.6F);
      int[] segColor = new int[n];
      for (int i = 0; i < n; i++) {
         boolean sel = this.segs == CATS && i == this.selected;
         segColor[i] = sel ? GuiStyle.blend(0x1A1226, primary, 0.7F) : GuiStyle.blend(0x140E1E, primary, i == hover ? 0.2F : 0.06F);
      }

      NativeImage image = this.wheelTex.getImage();
      for (int y = 0; y < this.dim; y++) {
         for (int x = 0; x < this.dim; x++) {
            int c = this.cls[y * this.dim + x];
            int argb = 0;
            if (c != 0) {
               int rgb = c <= n ? segColor[c - 1] : c == n + 1 ? ring : c == n + 2 ? rim : 0x0C0813;
               argb = (this.alpha[y * this.dim + x] * 255 / 7) << 24 | rgb;
            }

            image.setColorArgb(x, y, argb);
         }
      }

      this.wheelTex.upload();
   }

   @Override
   public void removed() {
      this.client.getTextureManager().destroyTexture(WHEEL_ID);
      this.wheelTex = null;
   }

   public OrbitGuiScreen() {
      super(Text.literal("VoidCyan"));
   }

   @Override
   protected void init() {
      this.rows.clear();
      Map<String, List<DropdownGuiScreen.Module>> cats = DropdownGuiScreen.orbitCategories();
      for (Seg seg : CATS) {
         List<Row> list = new ArrayList<>();
         if (seg.category() != null) {
            for (DropdownGuiScreen.Module m : cats.getOrDefault(seg.category(), new ArrayList<>())) {
               list.add(new Row(m.name, m.toggle, m.enabled, m.openSettings));
            }
         }

         this.rows.add(list);
      }

      this.sf = (float)this.client.getWindow().getScaleFactor();
      this.cx = (int)(this.width * 0.31F);
      this.cy = this.height / 2;
      this.radius = (int)Math.min(this.height * 0.37F, this.width * 0.27F);
      this.holeR = (int)(this.radius * 0.28F);
      this.radiusP = (int)(this.radius * this.sf);
      this.holeP = (int)(this.holeR * this.sf);
      this.segP = (int)(this.radiusP * 0.92F);
      this.buildMap();
   }

   private void buildMap() {
      this.dim = this.radiusP * 2 + 1;
      this.cls = new byte[this.dim * this.dim];
      this.alpha = new byte[this.dim * this.dim];
      int n = this.segs.length;
      double span = 360.0 / n;
      double gap = 1.6 * this.sf;
      for (int y = 0; y < this.dim; y++) {
         for (int x = 0; x < this.dim; x++) {
            double dx = x - this.radiusP;
            double dy = y - this.radiusP;
            double r = Math.sqrt(dx * dx + dy * dy);
            int c = 0;
            double a = 1.0;
            if (r <= this.radiusP + 0.5) {
               if (r < this.holeP - 1) {
                  c = n + 3;
               } else if (r < this.holeP + 2.5 * this.sf) {
                  c = n + 2;
               } else if (r > this.segP) {
                  c = n + 1;
                  a = Math.min(1.0, this.radiusP + 0.5 - r);
               } else {
                  double ang = Math.toDegrees(Math.atan2(dy, dx)) + 90.0;
                  if (ang < 0) ang += 360.0;
                  int seg = Math.min(n - 1, (int)(ang / span));
                  double local = ang - seg * span;
                  double dist = r * Math.sin(Math.toRadians(Math.min(local, span - local)));
                  a = Math.max(0.0, Math.min(1.0, (dist - gap) / this.sf));
                  c = a > 0.0 ? seg + 1 : 0;
               }
            }

            this.cls[y * this.dim + x] = (byte)c;
            this.alpha[y * this.dim + x] = (byte)(c == 0 ? 0 : (int)(a * 7.0 + 0.5));
         }
      }

      this.client.getTextureManager().destroyTexture(WHEEL_ID);
      this.wheelTex = new NativeImageBackedTexture(() -> "voidcyan_orbit_wheel", new NativeImage(this.dim, this.dim, true));
      this.client.getTextureManager().registerTexture(WHEEL_ID, this.wheelTex);
      this.lastKey = Integer.MIN_VALUE;
   }

   /** Segment index under a mouse position in GUI coordinates, or -1. */
   private int segmentAt(double mx, double my) {
      int x = (int)((mx - this.cx) * this.sf) + this.radiusP;
      int y = (int)((my - this.cy) * this.sf) + this.radiusP;
      if (x < 0 || y < 0 || x >= this.dim || y >= this.dim) return -1;
      int c = this.cls[y * this.dim + x];
      return c >= 1 && c <= this.segs.length ? c - 1 : -1;
   }

   private int panelX() {
      return Math.min(this.cx + this.radius + 40, this.width - 250);
   }

   private int panelY() {
      return Math.max(24, this.cy - 130);
   }

   private int rowsVisible() {
      return Math.max(3, (this.height - this.panelY() - 30) / 24);
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, 0xB0070510);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      int primary = VoidCyanClient.getPrimaryColor() & 0xFFFFFF;
      int n = this.segs.length;
      int hover = this.segmentAt(mouseX, mouseY);
      int key = hover * 1000 + this.selected * 10 + (this.segs == CATS ? 1 : 0);
      if (this.wheelTex == null || key != this.lastKey) {
         this.lastKey = key;
         this.paintWheel(hover, primary);
      }

      context.getMatrices().pushMatrix();
      context.getMatrices().translate(this.cx, this.cy);
      context.getMatrices().scale(1.0F / this.sf, 1.0F / this.sf);
      context.drawTexture(RenderPipelines.GUI_TEXTURED, WHEEL_ID, -this.radiusP, -this.radiusP, 0.0F, 0.0F, this.dim, this.dim, this.dim, this.dim);
      context.getMatrices().popMatrix();

      GuiStyle.drawLogo(context, this.cx - this.holeR / 2, this.cy - this.holeR / 2, this.holeR, primary, 1.0F);
      double span = Math.PI * 2 / n;
      for (int i = 0; i < n; i++) {
         double mid = -Math.PI / 2 + span * (i + 0.5);
         int lx = this.cx + (int)(Math.cos(mid) * this.radius * 0.64);
         int ly = this.cy + (int)(Math.sin(mid) * this.radius * 0.64);
         boolean active = this.segs == CATS && i == this.selected;
         context.getMatrices().pushMatrix();
         context.getMatrices().translate(lx, ly);
         context.getMatrices().scale(0.8F, 0.8F);
         GuiStyle.textCentered(context, this.textRenderer, this.segs[i].label().toUpperCase(), 0, -4, active ? 0xFFFFFFFF : i == hover ? 0xFFFFFFFF : 0xFFCFC6E0);
         context.getMatrices().popMatrix();
      }

      if (this.segs == CATS) {
         this.renderPanel(context, mouseX, mouseY, primary);
         int bw = 70;
         int bx = this.cx - bw / 2;
         int by = this.cy + this.radius + 8;
         boolean hovBack = GuiStyle.inRect(mouseX, mouseY, bx, by, bw, 18);
         context.fill(bx, by, bx + bw, by + 18, hovBack ? 0xFF000000 | GuiStyle.blend(0x140E1E, primary, 0.5F) : 0xFF000000 | GuiStyle.blend(0x140E1E, primary, 0.2F));
         GuiStyle.textCentered(context, this.textRenderer, "< BACK", this.cx, by + 5, 0xFFFFFFFF);
      }

      GuiStyle.text(context, this.textRenderer, "ESC to close", 10, this.height - 14, 0x88FFFFFF);
   }

   private void renderPanel(DrawContext context, int mouseX, int mouseY, int primary) {
      int px = this.panelX();
      int py = this.panelY();
      int pw = 230;
      List<Row> list = this.rows.get(this.selected);
      context.fill(px, py, px + pw, py + 20, 0xCC000000 | GuiStyle.blend(0x120C1A, primary, 0.14F));
      context.fill(px, py, px + 3, py + 20, 0xFF000000 | primary);
      GuiStyle.text(context, this.textRenderer, this.segs[this.selected].label().toUpperCase(), px + 10, py + 6, 0xFF000000 | primary);
      int y = py + 26;
      int end = Math.min(list.size(), this.scroll + this.rowsVisible());
      for (int i = this.scroll; i < end; i++) {
         Row row = list.get(i);
         boolean hov = GuiStyle.inRect(mouseX, mouseY, px, y, pw, 20);
         context.fill(px, y, px + pw, y + 20, hov ? 0xDD1C1428 : 0xCC120C1A);
         GuiStyle.text(context, this.textRenderer, row.label().toUpperCase(), px + 10, y + 6, 0xFFF2ECFF);
         boolean on = row.on().getAsBoolean();
         int tx = px + pw - 36;
         context.fill(tx, y + 4, tx + 26, y + 16, on ? 0xFF000000 | primary : 0xFF2A2433);
         int kx = on ? tx + 15 : tx + 1;
         context.fill(kx, y + 5, kx + 10, y + 15, 0xFFF5F2F8);
         if (row.settings() != null) {
            GuiStyle.text(context, this.textRenderer, "»", tx - 14, y + 6, hov ? 0xFFFFFFFF : 0x88FFFFFF);
         }

         y += 24;
      }
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      if (this.segs == CATS && GuiStyle.inRect(mx, my, this.cx - 35, this.cy + this.radius + 8, 70, 18)) {
         this.segs = MAIN;
         this.buildMap();
         return true;
      }

      int seg = this.segmentAt(mx, my);
      if (seg >= 0) {
         if (this.segs == CATS) {
            this.selected = seg;
            this.scroll = 0;
         } else if (this.segs[seg].tab() == -2) {
            this.segs = CATS;
            this.selected = 0;
            this.scroll = 0;
            this.buildMap();
         } else {
            ClickGuiScreen.startTab = this.segs[seg].tab();
            this.client.setScreen(new ClickGuiScreen(this));
         }

         return true;
      }

      if (this.segs == CATS) {
         int px = this.panelX();
         int y = this.panelY() + 26;
         List<Row> list = this.rows.get(this.selected);
         int end = Math.min(list.size(), this.scroll + this.rowsVisible());
         for (int i = this.scroll; i < end; i++) {
            if (GuiStyle.inRect(mx, my, px, y, 230, 20)) {
               Row row = list.get(i);
               if (click.button() == 1 || (row.settings() != null && mx >= px + 230 - 56 && mx < px + 230 - 40)) {
                  if (row.settings() != null) row.settings().run();
               } else {
                  row.click().run();
                  VoidCyanClient.saveConfig();
               }

               return true;
            }

            y += 24;
         }
      }

      return super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.segs != CATS) return true;
      int max = Math.max(0, this.rows.get(this.selected).size() - this.rowsVisible());
      this.scroll = Math.max(0, Math.min(max, this.scroll - (int)Math.signum(verticalAmount)));
      return true;
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         this.close();
         return true;
      }

      return super.keyPressed(input);
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
