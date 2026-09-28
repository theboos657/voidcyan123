package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ScreenshotManagerScreen extends Screen {
   private final Screen parent;
   private long openTime;
   private List<File> screenshots = new ArrayList<>();
   private int selectedIndex = -1;
   private float scrollY = 0.0F;
   private int panelW;
   private int panelH;
   private int panelX;
   private int panelY;
   private int listX;
   private int listY;
   private int listW;
   private int listH;
   private int previewX;
   private int previewY;
   private int previewW;
   private int previewH;
   private File previewFile;
   private Identifier previewTextureId;
   private NativeImageBackedTexture previewTexture;
   private int previewImgW;
   private int previewImgH;
   private TextFieldWidget renameField;
   private static final int RENAME_BTN_W = 55;
   private static final int RENAME_BTN_H = 18;

   public ScreenshotManagerScreen(Screen parent) {
      super(Text.literal("Screenshot Manager"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.openTime = System.currentTimeMillis();
      this.panelW = Math.max(Math.min(660, this.width - 10), (int)(this.width * 0.9F));
      this.panelH = Math.max(Math.min(380, this.height - 10), (int)(this.height * 0.9F));
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      this.listW = 260;
      this.listX = this.panelX + 10;
      this.listY = this.panelY + 35;
      this.listH = this.panelH - 75;
      this.previewW = this.panelW - this.listW - 30;
      this.previewX = this.panelX + this.listW + 20;
      this.previewY = this.panelY + 35;
      this.previewH = this.panelH - 110;
      this.renameField = new TextFieldWidget(
         this.textRenderer, this.previewX, this.previewY + this.previewH + 6, this.previewW - 55 - 5, 18, Text.literal("Filename")
      );
      this.renameField.setMaxLength(120);
      this.renameField.setDrawsBackground(false);
      this.renameField.setVisible(false);
      this.addDrawableChild(this.renameField);
      this.refreshScreenshots();
   }

   private void refreshScreenshots() {
      this.screenshots.clear();
      this.selectedIndex = -1;
      this.clearPreview();
      if (this.renameField != null) {
         this.renameField.setVisible(false);
      }

      Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots");
      if (Files.exists(dir) && Files.isDirectory(dir)) {
         File[] files = dir.toFile().listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
         if (files != null) {
            this.screenshots.addAll(Arrays.asList(files));
            this.screenshots.sort(Comparator.comparingLong(File::lastModified).reversed());
         }
      }
   }

   private void selectScreenshot(int idx) {
      if (idx >= 0 && idx < this.screenshots.size()) {
         this.selectedIndex = idx;
         this.loadPreview(this.screenshots.get(idx));
      }
   }

   private void loadPreview(File file) {
      this.clearPreview();
      this.previewFile = file;

      try {
         NativeImage img = NativeImage.read(new FileInputStream(file));
         this.previewImgW = img.getWidth();
         this.previewImgH = img.getHeight();
         this.previewTexture = new NativeImageBackedTexture(null, img);
         this.previewTextureId = Identifier.of("voidcyan", "ss_prev_" + System.currentTimeMillis());
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.previewTextureId, this.previewTexture);
         this.renameField.setText(file.getName().replace(".png", ""));
         this.renameField.setVisible(true);
      } catch (Exception var3) {
         var3.printStackTrace();
         this.previewFile = null;
         this.renameField.setVisible(false);
      }
   }

   private void clearPreview() {
      if (this.previewTextureId != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(this.previewTextureId);
         this.previewTextureId = null;
      }

      if (this.previewTexture != null) {
         this.previewTexture.close();
         this.previewTexture = null;
      }

      this.previewFile = null;
      this.previewImgW = 0;
      this.previewImgH = 0;
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      float progress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / 250.0F);
      float scale = 1.0F - (float)Math.pow(1.0F - progress, 3.0);
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(this.width / 2.0F, this.height / 2.0F);
      context.getMatrices().scale(scale, scale);
      context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F);
      context.fill(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, Integer.MIN_VALUE);
      this.drawBorderCyan(context, this.panelX, this.panelY, this.panelW, this.panelH);
      context.fill(this.panelX + 1, this.panelY + 1, this.panelX + this.panelW - 1, this.panelY + 25, 1610612736);
      context.drawTextWithShadow(
         this.textRenderer, Text.literal("Screenshot Manager"), this.panelX + 10, this.panelY + 8, VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000
      );
      String countStr = this.screenshots.size() + " Total";
      context.drawTextWithShadow(
         this.textRenderer, Text.literal(countStr), this.panelX + this.panelW - 10 - this.textRenderer.getWidth(countStr), this.panelY + 8, -7829368
      );
      context.fill(this.panelX + 1, this.panelY + 25, this.panelX + this.panelW - 1, this.panelY + 26, VoidCyanClient.getPrimaryColor() & 16777215 | 1342177280);
      context.fill(
         this.panelX + this.listW + 15,
         this.listY,
         this.panelX + this.listW + 16,
         this.listY + this.listH + 5,
         VoidCyanClient.getPrimaryColor() & 16777215 | 805306368
      );
      context.fill(this.listX, this.listY, this.listX + this.listW, this.listY + this.listH, 805306368);
      context.enableScissor(this.listX, this.listY, this.listX + this.listW, this.listY + this.listH);
      int itemH = 26;
      int iy = this.listY + 4 - (int)this.scrollY;

      for (int i = 0; i < this.screenshots.size(); i++) {
         File ss = this.screenshots.get(i);
         if (iy + itemH > this.listY && iy < this.listY + this.listH) {
            boolean hov = mouseX >= this.listX && mouseX <= this.listX + this.listW && mouseY >= iy && mouseY < iy + itemH;
            int bg = i == this.selectedIndex ? VoidCyanClient.getPrimaryColor() & 16777215 | 1610612736 : (hov ? 1090519039 : 0);
            context.fill(this.listX + 3, iy + 1, this.listX + this.listW - 3, iy + itemH - 1, bg);
            int nameColor = i == this.selectedIndex ? VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000 : -1;
            String name = ss.getName();
            int maxW = this.listW - 65;
            if (this.textRenderer.getWidth(name) > maxW) {
               name = this.textRenderer.trimToWidth(name, maxW - 6) + "â€¦";
            }

            context.drawTextWithShadow(this.textRenderer, Text.literal(name), this.listX + 10, iy + 9, nameColor);
            String sz = String.format("%.1fM", (float)ss.length() / 1048576.0F);
            context.drawTextWithShadow(this.textRenderer, Text.literal(sz), this.listX + this.listW - 8 - this.textRenderer.getWidth(sz), iy + 9, -10066330);
         }

         iy += itemH;
      }

      context.disableScissor();
      if (this.previewTextureId != null) {
         context.fill(this.previewX, this.previewY, this.previewX + this.previewW, this.previewY + this.previewH, -16777216);
         this.drawBorderCyan(context, this.previewX - 1, this.previewY - 1, this.previewW + 2, this.previewH + 2);
         float aspect = this.previewImgH > 0 ? (float)this.previewImgW / this.previewImgH : 1.0F;
         int drawW = this.previewW;
         int drawH = this.previewH;
         if ((float)this.previewW / this.previewH > aspect) {
            drawW = (int)(this.previewH * aspect);
         } else {
            drawH = (int)(this.previewW / aspect);
         }

         int dx = this.previewX + (this.previewW - drawW) / 2;
         int dy = this.previewY + (this.previewH - drawH) / 2;
         context.drawTexturedQuad(this.previewTextureId, dx, dy, dx + drawW, dy + drawH, 0.0F, 1.0F, 0.0F, 1.0F);
         int renBtnX = this.renameField.getX() + this.renameField.getWidth() + 5;
         int renBtnY = this.renameField.getY();
         boolean hRen = this.isHov(mouseX, mouseY, renBtnX, renBtnY, 55, 18);
         context.fill(renBtnX, renBtnY, renBtnX + 55, renBtnY + 18, hRen ? VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000 : -12303292);
         this.drawCenter(context, "Rename", renBtnX + 27, renBtnY + 5, -1);
      } else {
         String hint = this.selectedIndex == -1 ? "Select a screenshot to preview" : "Loading previewâ€¦";
         this.drawCenter(context, hint, this.previewX + this.previewW / 2, this.previewY + this.previewH / 2, -10066330);
      }

      int btnY = this.panelY + this.panelH - 30;
      int btnW = 95;
      int btnH = 18;
      boolean hFolder = this.isHov(mouseX, mouseY, this.listX, btnY, btnW, btnH);
      context.fill(this.listX, btnY, this.listX + btnW, btnY + btnH, hFolder ? VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000 : -12303292);
      this.drawCenter(context, "Open Folder", this.listX + btnW / 2, btnY + 5, -1);
      if (this.previewTextureId != null) {
         boolean hView = this.isHov(mouseX, mouseY, this.previewX, btnY, btnW, btnH);
         context.fill(this.previewX, btnY, this.previewX + btnW, btnY + btnH, hView ? VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000 : -12303292);
         this.drawCenter(context, "View Image", this.previewX + btnW / 2, btnY + 5, -1);
         int delX = this.previewX + this.previewW - btnW;
         boolean hDel = this.isHov(mouseX, mouseY, delX, btnY, btnW, btnH);
         context.fill(delX, btnY, delX + btnW, btnY + btnH, hDel ? -52429 : -7864320);
         this.drawCenter(context, "Delete", delX + btnW / 2, btnY + 5, -1);
      }

      super.render(context, mouseX, mouseY, deltaTicks);
      context.getMatrices().popMatrix();
   }

   private void drawBorderCyan(DrawContext ctx, int x, int y, int w, int h) {
      ctx.fill(x, y, x + w, y + 1, VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000);
      ctx.fill(x, y + h - 1, x + w, y + h, VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000);
      ctx.fill(x, y + 1, x + 1, y + h - 1, VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000);
      ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, VoidCyanClient.getPrimaryColor() & 16777215 | 0xFF000000);
   }

   private void drawCenter(DrawContext ctx, String txt, int cx, int ty, int col) {
      ctx.drawTextWithShadow(this.textRenderer, Text.literal(txt), cx - this.textRenderer.getWidth(txt) / 2, ty, col);
   }

   private boolean isHov(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      if (super.mouseClicked(click, doubled)) {
         return true;
      } else {
         if (mx >= this.listX && mx <= this.listX + this.listW && my >= this.listY && my <= this.listY + this.listH) {
            int itemH = 26;
            int iy = this.listY + 4 - (int)this.scrollY;

            for (int i = 0; i < this.screenshots.size(); i++) {
               if (my >= iy && my < iy + itemH) {
                  if (this.selectedIndex != i) {
                     this.selectScreenshot(i);
                  }

                  return true;
               }

               iy += itemH;
            }
         }

         int btnY = this.panelY + this.panelH - 30;
         int btnW = 95;
         int btnH = 18;
         if (this.isHov((int)mx, (int)my, this.listX, btnY, btnW, btnH)) {
            Util.getOperatingSystem().open(MinecraftClient.getInstance().runDirectory.toPath().resolve("screenshots").toUri());
            return true;
         } else {
            if (this.previewTextureId != null) {
               int viewBtnW = (int)(this.previewW * 0.48F);
               int delBtnW = this.previewW - viewBtnW;
               if (this.isHov((int)mx, (int)my, this.previewX, btnY, viewBtnW - 2, btnH)) {
                  Util.getOperatingSystem().open(this.screenshots.get(this.selectedIndex).toURI());
                  return true;
               }

               int delX = this.previewX + viewBtnW;
               if (this.isHov((int)mx, (int)my, delX, btnY, delBtnW, btnH)) {
                  File f = this.screenshots.get(this.selectedIndex);
                  if (f.delete()) {
                     this.refreshScreenshots();
                  }

                  return true;
               }

               int renBtnX = this.renameField.getX() + this.renameField.getWidth() + 5;
               int renBtnY = this.renameField.getY();
               if (this.isHov((int)mx, (int)my, renBtnX, renBtnY, 55, 18)) {
                  this.doRename();
                  return true;
               }
            }

            return false;
         }
      }
   }

   private void doRename() {
      if (this.selectedIndex >= 0 && this.selectedIndex < this.screenshots.size()) {
         String newName = this.renameField.getText().trim();
         if (!newName.isEmpty()) {
            if (!newName.toLowerCase().endsWith(".png")) {
               newName = newName + ".png";
            }

            File original = this.screenshots.get(this.selectedIndex);
            if (!newName.equals(original.getName())) {
               File newFile = new File(original.getParentFile(), newName);
               if (original.renameTo(newFile)) {
                  String renamed = newName;
                  this.refreshScreenshots();

                  for (int i = 0; i < this.screenshots.size(); i++) {
                     if (this.screenshots.get(i).getName().equals(renamed)) {
                        this.selectScreenshot(i);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (mouseX >= this.listX && mouseX <= this.listX + this.listW && mouseY >= this.listY && mouseY <= this.listY + this.listH) {
         this.scrollY = (float)(this.scrollY - verticalAmount * 20.0);
         if (this.scrollY < 0.0F) {
            this.scrollY = 0.0F;
         }

         int maxS = Math.max(0, this.screenshots.size() * 26 - this.listH + 8);
         if (this.scrollY > maxS) {
            this.scrollY = maxS;
         }

         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (this.renameField != null && this.renameField.isFocused() && input.key() == 257) {
         this.doRename();
         return true;
      } else if (input.key() == 256) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public void close() {
      this.clearPreview();
      this.client.setScreen(this.parent);
   }
}
