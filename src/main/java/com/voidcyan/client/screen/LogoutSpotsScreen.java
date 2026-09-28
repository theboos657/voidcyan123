package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.LogoutSpotsManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LogoutSpotsScreen extends Screen {
   private final Screen parent;
   private int guiWidth = 400;
   private int guiHeight = 300;
   private int guiLeft;
   private int guiTop;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private LogoutSpotsManager.LogoutSpot selectedSpot = null;
   private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

   public LogoutSpotsScreen(Screen parent) {
      super(Text.literal("Logout Spots"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.guiWidth = Math.min(this.width - 40, 500);
      this.guiHeight = Math.min(this.height - 40, 400);
      this.guiLeft = (this.width - this.guiWidth) / 2;
      this.guiTop = (this.height - this.guiHeight) / 2;
      this.updateScroll();
   }

   private void updateScroll() {
      int listHeight = LogoutSpotsManager.spots.size() * 30;
      int viewHeight = this.guiHeight - 60;
      this.maxScroll = Math.max(0, listHeight - viewHeight);
      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll));
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
      super.render(context, mouseX, mouseY, deltaTicks);
      context.fill(this.guiLeft, this.guiTop, this.guiLeft + this.guiWidth, this.guiTop + this.guiHeight, -1442840576);
      context.fill(this.guiLeft, this.guiTop, this.guiLeft + this.guiWidth, this.guiTop + 30, -14540254);
      context.drawTextWithShadow(
         this.textRenderer,
         Text.literal("Logout Spots (" + LogoutSpotsManager.spots.size() + ")"),
         this.guiLeft + 10,
         this.guiTop + 10,
         VoidCyanClient.getPrimaryColor()
      );
      int closeBtnX = this.guiLeft + this.guiWidth - 60;
      int closeBtnY = this.guiTop + 5;
      boolean hoverClose = this.isHovered(mouseX, mouseY, closeBtnX, closeBtnY, 50, 20);
      context.fill(closeBtnX, closeBtnY, closeBtnX + 50, closeBtnY + 20, hoverClose ? -11184811 : -13421773);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Close"), closeBtnX + 10, closeBtnY + 6, -1);
      int clearBtnX = this.guiLeft + this.guiWidth - 120;
      int clearBtnY = this.guiTop + 5;
      boolean hoverClear = this.isHovered(mouseX, mouseY, clearBtnX, clearBtnY, 50, 20);
      context.fill(clearBtnX, clearBtnY, clearBtnX + 50, clearBtnY + 20, hoverClear ? -7855582 : -11197918);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Clear"), clearBtnX + 10, clearBtnY + 6, -1);
      int listX = this.guiLeft + 10;
      int listY = this.guiTop + 40;
      int listW = this.guiWidth - 20;
      int listH = this.guiHeight - 50;
      context.enableScissor(listX, listY, listX + listW, listY + listH);
      int yOff = listY - this.scrollOffset;

      for (LogoutSpotsManager.LogoutSpot spot : LogoutSpotsManager.spots) {
         boolean isSelected = spot == this.selectedSpot;
         boolean hoverItem = this.isHovered(mouseX, mouseY, listX, yOff, listW, 28) && mouseY >= listY && mouseY <= listY + listH;
         int bgColor = isSelected ? -2143009537 : (hoverItem ? 1627389951 : 553648127);
         context.fill(listX, yOff, listX + listW, yOff + 28, bgColor);
         String timeStr = this.formatter.format(Instant.ofEpochMilli(spot.timestamp));
         String info = String.format("%s [%s] - X:%.0f Y:%.0f Z:%.0f (%s)", spot.playerName, timeStr, spot.x, spot.y, spot.z, spot.type);
         context.drawTextWithShadow(this.textRenderer, Text.literal(info), listX + 5, yOff + 10, -1);
         if (isSelected) {
            int btnW = 40;
            int delX = listX + listW - btnW - 5;
            int copyX = delX - btnW - 5;
            boolean hoverDel = hoverItem && mouseX >= delX && mouseX <= delX + btnW;
            context.fill(delX, yOff + 4, delX + btnW, yOff + 24, hoverDel ? -3394765 : -5627358);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Del"), delX + 10, yOff + 10, -1);
            boolean hoverCopy = hoverItem && mouseX >= copyX && mouseX <= copyX + btnW;
            context.fill(copyX, yOff + 4, copyX + btnW, yOff + 24, hoverCopy ? -12277180 : -14514142);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Copy"), copyX + 8, yOff + 10, -1);
         }

         yOff += 30;
      }

      context.disableScissor();
      if (this.maxScroll > 0) {
         int sbX = listX + listW + 2;
         context.fill(sbX, listY, sbX + 4, listY + listH, 1073741824);
         float thumbRatio = (float)listH / (LogoutSpotsManager.spots.size() * 30);
         int thumbH = Math.max(20, (int)(listH * thumbRatio));
         int thumbY = listY + (int)((listH - thumbH) * ((float)this.scrollOffset / this.maxScroll));
         context.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, -1431655766);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         int closeBtnX = this.guiLeft + this.guiWidth - 60;
         int closeBtnY = this.guiTop + 5;
         if (this.isHovered((int)mouseX, (int)mouseY, closeBtnX, closeBtnY, 50, 20)) {
            this.close();
            return true;
         } else {
            int clearBtnX = this.guiLeft + this.guiWidth - 120;
            int clearBtnY = this.guiTop + 5;
            if (this.isHovered((int)mouseX, (int)mouseY, clearBtnX, clearBtnY, 50, 20)) {
               LogoutSpotsManager.clearAll();
               this.selectedSpot = null;
               this.updateScroll();
               return true;
            } else {
               int listX = this.guiLeft + 10;
               int listY = this.guiTop + 40;
               int listW = this.guiWidth - 20;
               int listH = this.guiHeight - 50;
               if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                  int yOff = listY - this.scrollOffset;

                  for (int i = 0; i < LogoutSpotsManager.spots.size(); i++) {
                     LogoutSpotsManager.LogoutSpot spot = LogoutSpotsManager.spots.get(i);
                     if (mouseY >= yOff && mouseY <= yOff + 28) {
                        if (this.selectedSpot == spot) {
                           int btnW = 40;
                           int delX = listX + listW - btnW - 5;
                           int copyX = delX - btnW - 5;
                           if (mouseX >= delX && mouseX <= delX + btnW) {
                              LogoutSpotsManager.deleteSpot(spot);
                              this.selectedSpot = null;
                              this.updateScroll();
                              return true;
                           }

                           if (mouseX >= copyX && mouseX <= copyX + btnW) {
                              String coords = String.format("%.0f %.0f %.0f", spot.x, spot.y, spot.z);
                              MinecraftClient.getInstance().keyboard.setClipboard(coords);
                              return true;
                           }
                        }

                        this.selectedSpot = spot;
                        return true;
                     }

                     yOff += 30;
                  }

                  this.selectedSpot = null;
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.scrollOffset = (int)(this.scrollOffset - verticalAmount * 20.0);
      this.updateScroll();
      return true;
   }

   public void close() {
      MinecraftClient.getInstance().setScreen(this.parent);
   }

   private boolean isHovered(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }
}
