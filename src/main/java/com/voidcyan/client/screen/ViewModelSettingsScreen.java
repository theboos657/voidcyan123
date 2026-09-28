package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ViewModelSettingsScreen extends Screen {
   private static final int GREEN = -14498466;
   private final Screen parent;
   private int tab = 0;
   private float scrollOffset = 0.0F;
   private float scrollTarget = 0.0F;
   private int draggingSlot = -1;
   private String draggingItemId = null;
   private int draggingItemSlotIdx = -1;
   private boolean addingItem = false;
   private String addItemText = "";
   private String expandedItemId = null;
   private long openTime;

   public ViewModelSettingsScreen(Screen parent) {
      super(Text.literal("View Model Settings"));
      this.parent = parent;
   }

   protected void init() {
      this.openTime = System.currentTimeMillis();
      this.scrollOffset = 0.0F;
      this.scrollTarget = 0.0F;
      this.draggingSlot = -1;
      this.addingItem = false;
      this.expandedItemId = null;
   }

   private Map<String, VoidCyanClient.ViewModelSettings> currentOverrides() {
      return this.tab == 0 ? VoidCyanClient.mainHandItemOverrides : VoidCyanClient.offHandItemOverrides;
   }

   private VoidCyanClient.ViewModelSettings currentGlobal() {
      return this.tab == 0 ? VoidCyanClient.mainHandGlobal : VoidCyanClient.offHandGlobal;
   }

   private boolean currentHandEnabled() {
      return this.tab == 0 ? VoidCyanClient.isMainHandViewModelEnabled : VoidCyanClient.isOffHandViewModelEnabled;
   }

   private void setCurrentHandEnabled(boolean v) {
      if (this.tab == 0) {
         VoidCyanClient.isMainHandViewModelEnabled = v;
      } else {
         VoidCyanClient.isOffHandViewModelEnabled = v;
      }

      VoidCyanClient.markConfigDirty();
   }

   private int panelX() {
      return (this.width - 480) / 2;
   }

   private int panelY() {
      return (this.height - this.panelHeight()) / 2;
   }

   private int panelHeight() {
      return Math.min(this.height - 20, 540);
   }

   private int contentStartY() {
      return this.panelY() + 32 + 44;
   }

   private int contentEndY() {
      return this.panelY() + this.panelHeight() - 10;
   }

   private int contentH() {
      return this.contentEndY() - this.contentStartY();
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      float anim = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / 280.0F);
      anim = 1.0F - (float)Math.pow(1.0F - anim, 3.0);
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(this.panelX() + 240.0F, this.panelY() + this.panelHeight() / 2.0F);
      context.getMatrices().scale(anim, anim);
      context.getMatrices().translate(-(this.panelX() + 240.0F), -(this.panelY() + this.panelHeight() / 2.0F));
      int px = this.panelX();
      int py = this.panelY();
      int pw = 480;
      int ph = this.panelHeight();

      for (int i = 1; i <= 6; i++) {
         int alpha = (int)(60.0F * (1.0F - i / 6.0F));
         context.fill(px - i, py - i, px + pw + i, py + ph + i, alpha << 24);
      }

      context.fill(px, py, px + pw, py + ph, -15658216);
      context.fill(px, py, px + pw, py + 3, -16722774);
      context.fill(px, py, px + pw, py + 36, -15065819);
      String title = "☰  View Model";
      context.drawText(this.textRenderer, title, px + 16, py + 12, -1512720, false);
      String back = "← Back";
      int backW = this.textRenderer.getWidth(back);
      context.drawText(this.textRenderer, back, px + pw - backW - 12, py + 12, -7827288, false);
      int tabY = py + 36;
      String[] tabNames = new String[]{"Main Hand", "Off Hand"};
      int tabW = pw / 2;

      for (int i = 0; i < 2; i++) {
         int tx = px + i * tabW;
         boolean active = i == this.tab;
         context.fill(tx, tabY, tx + tabW, tabY + 32, active ? -15065819 : -15658216);
         if (active) {
            context.fill(tx, tabY + 32 - 2, tx + tabW, tabY + 32, -16722774);
         }

         int tc = active ? -16722774 : -7827288;
         int tw = this.textRenderer.getWidth(tabNames[i]);
         context.drawText(this.textRenderer, tabNames[i], tx + (tabW - tw) / 2, tabY + 9, tc, false);
      }

      int cStartY = this.contentStartY();
      int cEndY = this.contentEndY();
      this.scrollOffset = this.scrollOffset + (this.scrollTarget - this.scrollOffset) * 0.25F;
      context.enableScissor(px + 4, cStartY, px + pw - 4, cEndY);
      int cy = cStartY - (int)this.scrollOffset;
      cy = this.drawToggleRow(context, px, cy, pw, "View Model Module", VoidCyanClient.isViewModelEnabled, mouseX, mouseY, 0);
      cy += 4;
      context.fill(px + 12, cy, px + pw - 12, cy + 1, -14012611);
      cy += 6;
      String handLabel = (this.tab == 0 ? "Main Hand" : "Off Hand") + " Enabled";
      cy = this.drawToggleRow(context, px, cy, pw, handLabel, this.currentHandEnabled(), mouseX, mouseY, 1);
      cy += 4;
      context.fill(px + 12, cy, px + pw - 12, cy + 1, -14012611);
      cy += 10;
      VoidCyanClient.ViewModelSettings global = this.currentGlobal();
      context.fill(px + 8, cy, px + pw - 8, cy + 22, -14670802);
      context.drawText(this.textRenderer, "§bGlobal Settings", px + 16, cy + 6, -1512720, false);
      this.drawSmallToggle(context, px + pw - 60, cy + 3, global.enabled, mouseX, mouseY, 10);
      cy += 26;
      cy = this.drawSliderRow(context, px, cy, pw, "X", global.x, -1.0F, 1.0F, mouseX, mouseY, 20);
      cy = this.drawSliderRow(context, px, cy, pw, "Y", global.y, -1.0F, 1.0F, mouseX, mouseY, 21);
      cy = this.drawSliderRow(context, px, cy, pw, "Z", global.z, -1.0F, 1.0F, mouseX, mouseY, 22);
      cy = this.drawSliderRow(context, px, cy, pw, "Rot X", global.rotX, -180.0F, 180.0F, mouseX, mouseY, 23);
      cy = this.drawSliderRow(context, px, cy, pw, "Rot Y", global.rotY, -180.0F, 180.0F, mouseX, mouseY, 24);
      cy = this.drawSliderRow(context, px, cy, pw, "Rot Z", global.rotZ, -180.0F, 180.0F, mouseX, mouseY, 25);
      cy = this.drawSliderRow(context, px, cy, pw, "Scale", global.scale, 0.05F, 3.0F, mouseX, mouseY, 26);
      cy += 8;
      context.fill(px + 12, cy, px + pw - 12, cy + 1, -14012611);
      cy += 10;
      context.fill(px + 8, cy, px + pw - 8, cy + 22, -14670802);
      context.drawText(this.textRenderer, "§aItem Overrides", px + 16, cy + 6, -1512720, false);
      int plusX = px + pw - 36;
      int plusY = cy + 2;
      boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + 28 && mouseY >= plusY && mouseY <= plusY + 18;
      context.fill(plusX, plusY, plusX + 28, plusY + 18, hoverPlus ? -13782175 : -14498466);
      context.drawCenteredTextWithShadow(this.textRenderer, "+", plusX + 14, plusY + 4, -1);
      cy += 26;
      if (this.addingItem) {
         context.fill(px + 12, cy, px + pw - 12, cy + 22, -14342096);
         context.fill(px + 12, cy, px + pw - 12, cy + 1, -16722774);
         context.fill(px + 12, cy + 21, px + pw - 12, cy + 22, -16722774);
         String displayText = this.addItemText + (System.currentTimeMillis() / 500L % 2L == 0L ? "│" : " ");
         context.drawText(this.textRenderer, displayText, px + 16, cy + 7, -1512720, false);
         String hint = "e.g. minecraft:totem_of_undying  [Enter] = add  [Esc] = cancel";
         if (this.addItemText.isEmpty()) {
            context.drawText(this.textRenderer, hint, px + 16, cy + 7, -7827288, false);
         }

         cy += 26;
      }

      Map<String, VoidCyanClient.ViewModelSettings> overrides = this.currentOverrides();
      List<String> keys = new ArrayList<>(overrides.keySet());
      Collections.sort(keys);
      int itemSlotBase = 200;

      for (int ki = 0; ki < keys.size(); ki++) {
         String itemId = keys.get(ki);
         VoidCyanClient.ViewModelSettings ov = overrides.get(itemId);
         boolean expanded = itemId.equals(this.expandedItemId);
         int rowBg = expanded ? -14538950 : -14670802;
         context.fill(px + 8, cy, px + pw - 8, cy + 34, rowBg);
         if (mouseX >= px + 12 && mouseX <= px + 32 && mouseY >= cy + 8 && mouseY <= cy + 24) {
            boolean var76 = true;
         } else {
            boolean var10000 = false;
         }

         int dotColor = ov.enabled ? -14498466 : -1096636;
         context.fill(px + 14, cy + 10, px + 24, cy + 24, dotColor);
         String display = itemId.length() > 34 ? itemId.substring(0, 31) + "..." : itemId;
         context.drawText(this.textRenderer, display, px + 32, cy + 11, -1512720, false);
         String arrow = expanded ? "▾" : "▸";
         context.drawText(this.textRenderer, arrow, px + pw - 54, cy + 11, -16722774, false);
         boolean hovDel = mouseX >= px + pw - 32 && mouseX <= px + pw - 12 && mouseY >= cy + 8 && mouseY <= cy + 26;
         context.fill(px + pw - 32, cy + 8, px + pw - 12, cy + 26, hovDel ? -4513246 : -1096636);
         context.drawCenteredTextWithShadow(this.textRenderer, "×", px + pw - 22, cy + 12, -1);
         cy += 34;
         if (expanded) {
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "X", ov.x, -1.0F, 1.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 0);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Y", ov.y, -1.0F, 1.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 1);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Z", ov.z, -1.0F, 1.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 2);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Rot X", ov.rotX, -180.0F, 180.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 3);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Rot Y", ov.rotY, -180.0F, 180.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 4);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Rot Z", ov.rotZ, -180.0F, 180.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 5);
            cy = this.drawSliderRow(context, px + 8, cy, pw - 16, "Scale", ov.scale, 0.05F, 3.0F, mouseX, mouseY, itemSlotBase + ki * 10 + 6);
            cy += 4;
         }
      }

      cy += 20;
      int totalContent = cy - (cStartY - (int)this.scrollOffset);
      context.disableScissor();
      if (totalContent > this.contentH()) {
         int sbH = Math.max(20, (int)((float)this.contentH() / totalContent * this.contentH()));
         int sbY = cStartY + (int)(this.scrollOffset / (totalContent - this.contentH()) * (this.contentH() - sbH));
         context.fill(px + pw - 5, cStartY, px + pw - 2, cEndY, -14342096);
         context.fill(px + pw - 5, sbY, px + pw - 2, sbY + sbH, -16722774);
      }

      context.getMatrices().popMatrix();
   }

   private int drawToggleRow(DrawContext ctx, int x, int y, int w, String label, boolean val, int mx, int my, int slot) {
      ctx.fill(x + 8, y, x + w - 8, y + 34, -14670802);
      ctx.drawText(this.textRenderer, label, x + 16, y + 11, -1512720, false);
      int tx = x + w - 56;
      int ty = y + 9;
      ctx.fill(tx, ty, tx + 36, ty + 16, val ? -16722774 : -14342096);
      ctx.fill(tx + (val ? 20 : 2), ty + 2, tx + (val ? 34 : 16), ty + 14, -1);
      return y + 34 + 2;
   }

   private void drawSmallToggle(DrawContext ctx, int x, int y, boolean val, int mx, int my, int slot) {
      ctx.fill(x, y, x + 36, y + 16, val ? -16722774 : -14342096);
      ctx.fill(x + (val ? 20 : 2), y + 2, x + (val ? 34 : 16), y + 14, -1);
   }

   private int drawSliderRow(DrawContext ctx, int x, int y, int w, String label, float val, float min, float max, int mx, int my, int slot) {
      ctx.fill(x + 4, y, x + w - 4, y + 28, -15065819);
      ctx.drawText(this.textRenderer, label, x + 10, y + 8, -7827288, false);
      int slW = w - 110;
      int slX = x + 50;
      int slY = y + 14 - 2;
      ctx.fill(slX, slY, slX + slW, slY + 4, -14342096);
      float t = (val - min) / (max - min);
      int fillW = (int)(t * slW);
      ctx.fill(slX, slY, slX + fillW, slY + 4, -16722774);
      int thumbX = slX + fillW - 4;
      boolean hov = mx >= slX && mx <= slX + slW && my >= y && my <= y + 28;
      ctx.fill(thumbX, slY - 3, thumbX + 8, slY + 7, hov ? -1 : -16722774);
      String valStr;
      if (label.startsWith("Scale")) {
         valStr = String.format("%.2f", val);
      } else if (label.startsWith("Rot")) {
         valStr = String.format("%.1f°", val);
      } else {
         valStr = String.format("%.3f", val);
      }

      ctx.drawText(this.textRenderer, valStr, slX + slW + 6, y + 8, -1512720, false);
      return y + 28 + 1;
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      int btn = click.button();
      int px = this.panelX();
      int py = this.panelY();
      int pw = 480;
      int ph = this.panelHeight();
      int back = this.textRenderer.getWidth("← Back");
      if (!(mx >= px + pw - back - 12) || !(mx <= px + pw - 4) || !(my >= py + 4) || !(my <= py + 28)) {
         int tabY = py + 36;
         if (my >= tabY && my <= tabY + 32) {
            int newTab = (int)((mx - px) / (pw / 2));
            if (newTab >= 0 && newTab < 2) {
               this.tab = newTab;
               this.expandedItemId = null;
               this.scrollOffset = this.scrollTarget = 0.0F;
               return true;
            }
         }

         int cStartY = this.contentStartY();
         int cy = cStartY - (int)this.scrollOffset;
         if (this.hitToggle(mx, my, px, cy, pw)) {
            VoidCyanClient.isViewModelEnabled = !VoidCyanClient.isViewModelEnabled;
            VoidCyanClient.markConfigDirty();
            return true;
         } else {
            cy += 47;
            if (this.hitToggle(mx, my, px, cy, pw)) {
               this.setCurrentHandEnabled(!this.currentHandEnabled());
               return true;
            } else {
               cy += 51;
               cy += 22;
               VoidCyanClient.ViewModelSettings global = this.currentGlobal();
               if (mx >= px + pw - 60 && mx <= px + pw - 24 && my >= cy - 22 + 3 && my <= cy - 22 + 3 + 18) {
                  global.enabled = !global.enabled;
                  VoidCyanClient.markConfigDirty();
                  return true;
               } else {
                  for (int si = 0; si < 7; si++) {
                     if (this.hitSlider(mx, my, px, cy, pw, si, global)) {
                        return true;
                     }

                     cy += 29;
                  }

                  cy += 19;
                  int plusX = px + pw - 36;
                  int plusY = cy + 2;
                  cy += 26;
                  if (mx >= plusX && mx <= plusX + 28 && my >= plusY && my <= plusY + 18 && btn == 0) {
                     this.addingItem = !this.addingItem;
                     this.addItemText = "";
                     return true;
                  } else {
                     if (this.addingItem) {
                        cy += 26;
                     }

                     Map<String, VoidCyanClient.ViewModelSettings> overrides = this.currentOverrides();
                     List<String> keys = new ArrayList<>(overrides.keySet());
                     Collections.sort(keys);
                     int itemSlotBase = 200;

                     for (int ki = 0; ki < keys.size(); ki++) {
                        String itemId = keys.get(ki);
                        VoidCyanClient.ViewModelSettings ov = overrides.get(itemId);
                        boolean expanded = itemId.equals(this.expandedItemId);
                        if (mx >= px + pw - 32 && mx <= px + pw - 12 && my >= cy + 8 && my <= cy + 26 && btn == 0) {
                           overrides.remove(itemId);
                           if (itemId.equals(this.expandedItemId)) {
                              this.expandedItemId = null;
                           }

                           VoidCyanClient.markConfigDirty();
                           return true;
                        }

                        if (mx >= px + 12 && mx <= px + 32 && my >= cy + 8 && my <= cy + 24 && btn == 0) {
                           ov.enabled = !ov.enabled;
                           VoidCyanClient.markConfigDirty();
                           return true;
                        }

                        if (mx >= px + 8 && mx <= px + pw - 34 && my >= cy && my <= cy + 34 && btn == 0) {
                           this.expandedItemId = itemId.equals(this.expandedItemId) ? null : itemId;
                           return true;
                        }

                        cy += 34;
                        if (expanded) {
                           for (int si = 0; si < 7; si++) {
                              if (this.hitSlider(mx, my, px + 8, cy, pw - 16, si, ov)) {
                                 return true;
                              }

                              cy += 29;
                           }

                           cy += 4;
                        }
                     }

                     return super.mouseClicked(click, doubled);
                  }
               }
            }
         }
      } else {
         assert this.client != null;

         this.client.setScreen(this.parent);
         return true;
      }
   }

   private boolean hitToggle(double mx, double my, int px, int cy, int pw) {
      int tx = px + pw - 56;
      int ty = cy + 9;
      return mx >= tx && mx <= tx + 36 && my >= cy && my <= cy + 34;
   }

   private boolean hitSlider(double mx, double my, int px, int cy, int pw, int si, VoidCyanClient.ViewModelSettings s) {
      int slX = px + 50;
      int slY = cy + 14 - 2;
      int slW = pw - 110;
      if (mx >= slX && mx <= slX + slW && my >= cy && my <= cy + 28) {
         this.draggingSlot = si;
         this.applySliderDrag(mx, slX, slW, si, s);
         VoidCyanClient.markConfigDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      double mx = click.x();
      double my = click.y();
      int btn = click.button();
      if (this.draggingSlot >= 0) {
         int px = this.panelX();
         int pw = 480;
         int cStartY = this.contentStartY();
         int cy = cStartY - (int)this.scrollOffset;
         cy += 120;
         if (this.draggingSlot >= 0 && this.draggingSlot <= 6) {
            int slX = px + 50;
            int slW = pw - 110;
            this.applySliderDrag(mx, slX, slW, this.draggingSlot, this.currentGlobal());
            VoidCyanClient.markConfigDirty();
            return true;
         }
      }

      if (this.draggingItemId != null && this.draggingItemSlotIdx >= 0) {
         int px = this.panelX();
         int pw = 480;
         VoidCyanClient.ViewModelSettings ov = this.currentOverrides().get(this.draggingItemId);
         if (ov != null) {
            int slX = px + 8 + 50;
            int slW = pw - 16 - 110;
            this.applySliderDrag(mx, slX, slW, this.draggingItemSlotIdx, ov);
            VoidCyanClient.markConfigDirty();
            return true;
         }
      }

      return super.mouseDragged(click, offsetX, offsetY);
   }

   public boolean mouseReleased(Click click) {
      this.draggingSlot = -1;
      this.draggingItemId = null;
      this.draggingItemSlotIdx = -1;
      return super.mouseReleased(click);
   }

   private void applySliderDrag(double mx, int slX, int slW, int slotIdx, VoidCyanClient.ViewModelSettings s) {
      float t = MathHelper.clamp((float)(mx - slX) / slW, 0.0F, 1.0F);
      switch (slotIdx) {
         case 0:
            s.x = this.lerp(t, -1.0F, 1.0F);
            break;
         case 1:
            s.y = this.lerp(t, -1.0F, 1.0F);
            break;
         case 2:
            s.z = this.lerp(t, -1.0F, 1.0F);
            break;
         case 3:
            s.rotX = this.lerp(t, -180.0F, 180.0F);
            break;
         case 4:
            s.rotY = this.lerp(t, -180.0F, 180.0F);
            break;
         case 5:
            s.rotZ = this.lerp(t, -180.0F, 180.0F);
            break;
         case 6:
            s.scale = this.lerp(t, 0.05F, 3.0F);
      }
   }

   private float lerp(float t, float min, float max) {
      return min + t * (max - min);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.scrollTarget -= (float)(verticalAmount * 18.0);
      this.scrollTarget = Math.max(0.0F, this.scrollTarget);
      return true;
   }

   public boolean charTyped(CharInput input) {
      char chr = (char)input.codepoint();
      if (this.addingItem && chr >= ' ') {
         this.addItemText = this.addItemText + chr;
         return true;
      } else {
         return super.charTyped(input);
      }
   }

   public boolean keyPressed(KeyInput input) {
      int keyCode = input.key();
      if (this.addingItem) {
         if (keyCode == 256) {
            this.addingItem = false;
            this.addItemText = "";
            return true;
         } else if (keyCode == 257 || keyCode == 335) {
            String trimmed = this.addItemText.trim();
            if (!trimmed.isEmpty()) {
               if (!trimmed.contains(":")) {
                  trimmed = "minecraft:" + trimmed;
               }

               this.currentOverrides().put(trimmed, new VoidCyanClient.ViewModelSettings());
               this.expandedItemId = trimmed;
               VoidCyanClient.markConfigDirty();
            }

            this.addingItem = false;
            this.addItemText = "";
            return true;
         } else if (keyCode == 259 && !this.addItemText.isEmpty()) {
            this.addItemText = this.addItemText.substring(0, this.addItemText.length() - 1);
            return true;
         } else {
            return true;
         }
      } else if (keyCode == 256) {
         assert this.client != null;

         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean shouldPause() {
      return false;
   }
}
