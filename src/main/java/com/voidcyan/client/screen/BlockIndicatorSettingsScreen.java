package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.BlockIndicatorManager;
import com.voidcyan.client.util.WaypointManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class BlockIndicatorSettingsScreen extends Screen {
   private static final String[] ICON_PRESETS = new String[]{
      "minecraft:grass_block", "minecraft:diamond", "minecraft:compass", "minecraft:pickaxe", "minecraft:ender_pearl", "minecraft:tnt"
   };
   private final Screen parent;
   private long openTime;
   private boolean draggingWidth = false;
   private boolean draggingFillAlpha = false;
   private boolean draggingBorderR = false;
   private boolean draggingBorderG = false;
   private boolean draggingBorderB = false;
   private String iconInput = "";
   private boolean editingIcon = false;

   public BlockIndicatorSettingsScreen(Screen parent) {
      super(Text.literal("Block Info HUD Settings"));
      this.parent = parent;
      this.iconInput = BlockIndicatorManager.customIconItem;
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
   }

   protected void init() {
      super.init();
      this.openTime = System.currentTimeMillis();
   }

   private int panelWidth() {
      return Math.max(Math.min(440, this.width - 10), (int)(this.width * 0.9F));
   }

   private int panelHeight() {
      return Math.max(Math.min(480, this.height - 10), (int)(this.height * 0.9F));
   }

   private int panelX() {
      return (this.width - this.panelWidth()) / 2;
   }

   private int panelY() {
      return (this.height - this.panelHeight()) / 2;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.renderBackground(context, mouseX, mouseY, deltaTicks);
      float progress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / 250.0F);
      float scale = 1.0F - (float)Math.pow(1.0F - progress, 3.0);
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(this.width / 2.0F, this.height / 2.0F);
      context.getMatrices().scale(scale, scale);
      context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F);
      super.render(context, mouseX, mouseY, deltaTicks);
      int pw = this.panelWidth();
      int ph = this.panelHeight();
      int x = this.panelX();
      int y = this.panelY();
      context.fill(x, y, x + pw, y + ph, Integer.MIN_VALUE);
      this.drawBorder(context, x, y, pw, ph, VoidCyanClient.getPrimaryColor());
      context.fill(x + 1, y + 1, x + pw - 1, y + 25, 1610612736);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Block Info HUD Settings"), x + 10, y + 8, VoidCyanClient.getPrimaryColor());
      int rowY = y + 40;
      int sliderX = x + pw - 150;
      int sliderW = 140;
      int sliderH = 14;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Panel Width"), x + 10, rowY + 2, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Text trims to fit"), x + 10, rowY + 14, -5592406);
      context.fill(sliderX, rowY + 4, sliderX + sliderW, rowY + 4 + sliderH, VoidCyanClient.getPrimaryColor() & 16777215 | 1073741824);
      float widthT = (BlockIndicatorManager.maxWidth - 80.0F) / 240.0F;
      widthT = Math.max(0.0F, Math.min(1.0F, widthT));
      context.fill(sliderX, rowY + 4, sliderX + (int)(sliderW * widthT), rowY + 4 + sliderH, VoidCyanClient.getPrimaryColor());
      context.drawTextWithShadow(this.textRenderer, Text.literal(BlockIndicatorManager.maxWidth + "px"), sliderX + sliderW + 6, rowY + 6, -1);
      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Fill Opacity"), x + 10, rowY + 2, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Background transparency"), x + 10, rowY + 14, -5592406);
      int fillAlpha = BlockIndicatorManager.fillColor >> 24 & 0xFF;
      int fillRGB = BlockIndicatorManager.fillColor & 16777215;
      context.fill(sliderX, rowY + 4, sliderX + sliderW, rowY + 4 + sliderH, 1090519039);
      context.fill(sliderX, rowY + 4, sliderX + (int)(sliderW * fillAlpha / 255.0F), rowY + 4 + sliderH, -7829368 | fillRGB);
      context.drawTextWithShadow(this.textRenderer, Text.literal(fillAlpha + "/255"), sliderX + sliderW + 6, rowY + 6, -1);
      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Fill Color"), x + 10, rowY + 2, -1);
      context.fill(sliderX - 28, rowY + 2, sliderX - 8, rowY + 16, 0xFF000000 | fillRGB);
      this.drawBorder(context, sliderX - 28, rowY + 2, 20, 14, -7829368);
      int r = fillRGB >> 16 & 0xFF;
      int g = fillRGB >> 8 & 0xFF;
      int b = fillRGB & 0xFF;
      context.fill(sliderX, rowY + 2, sliderX + sliderW, rowY + 6, -1057030144);
      context.fill(sliderX, rowY + 2, sliderX + (int)(sliderW * r / 255.0F), rowY + 6, -52429);
      context.fill(sliderX, rowY + 9, sliderX + sliderW, rowY + 13, 1077149491);
      context.fill(sliderX, rowY + 9, sliderX + (int)(sliderW * g / 255.0F), rowY + 13, -13369549);
      context.fill(sliderX, rowY + 16, sliderX + sliderW, rowY + 20, 1077097471);
      context.fill(sliderX, rowY + 16, sliderX + (int)(sliderW * b / 255.0F), rowY + 20, -13395457);
      context.drawTextWithShadow(this.textRenderer, Text.literal("R:" + r + " G:" + g + " B:" + b), sliderX + sliderW + 6, rowY + 2, -3355444);
      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Blur Fill"), x + 10, rowY + 2, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Frosted glass effect"), x + 10, rowY + 14, -5592406);
      int togX = sliderX + sliderW - 60;
      this.drawToggle(context, togX, rowY + 4, BlockIndicatorManager.blurFill, mouseX, mouseY);
      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Border Color"), x + 10, rowY + 2, -1);
      int bc = BlockIndicatorManager.borderColor;
      int br = bc >> 16 & 0xFF;
      int bg2 = bc >> 8 & 0xFF;
      int bb = bc & 0xFF;
      context.fill(sliderX - 28, rowY + 2, sliderX - 8, rowY + 16, bc | 0xFF000000);
      this.drawBorder(context, sliderX - 28, rowY + 2, 20, 14, -7829368);
      context.fill(sliderX, rowY + 2, sliderX + sliderW, rowY + 6, 1077084160);
      context.fill(sliderX, rowY + 2, sliderX + (int)(sliderW * br / 255.0F), rowY + 6, -52429);
      context.fill(sliderX, rowY + 9, sliderX + sliderW, rowY + 13, 1077149491);
      context.fill(sliderX, rowY + 9, sliderX + (int)(sliderW * bg2 / 255.0F), rowY + 13, -13369549);
      context.fill(sliderX, rowY + 16, sliderX + sliderW, rowY + 20, 1077097471);
      context.fill(sliderX, rowY + 16, sliderX + (int)(sliderW * bb / 255.0F), rowY + 20, -13395457);
      context.drawTextWithShadow(this.textRenderer, Text.literal("R:" + br + " G:" + bg2 + " B:" + bb), sliderX + sliderW + 6, rowY + 2, -3355444);
      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Icon"), x + 10, rowY + 2, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Off, looked-at block, or custom item"), x + 10, rowY + 14, -5592406);
      int offBtnX = x + pw - 280;
      int blockBtnX = x + pw - 185;
      int customBtnX = x + pw - 90;
      this.drawModeButton(context, offBtnX, rowY, 85, "Off", BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.OFF, mouseX, mouseY);
      this.drawModeButton(context, blockBtnX, rowY, 90, "Block", BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.BLOCK, mouseX, mouseY);
      this.drawModeButton(context, customBtnX, rowY, 80, "Custom", BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.CUSTOM, mouseX, mouseY);
      rowY += 40;
      boolean customActive = BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.CUSTOM;
      int labelColor = customActive ? -1 : -10066330;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Custom Icon Item"), x + 10, rowY, labelColor);
      rowY += 14;
      int inputW = pw - 60;
      int inputBorder = customActive ? (this.editingIcon ? VoidCyanClient.getPrimaryColor() : -11184811) : -13421773;
      context.fill(x + 10, rowY, x + 10 + inputW, rowY + 20, Integer.MIN_VALUE);
      this.drawBorder(context, x + 10, rowY, inputW, 20, inputBorder);
      String shown = this.iconInput + (this.editingIcon && customActive && System.currentTimeMillis() % 1000L < 500L ? "_" : "");
      context.drawTextWithShadow(this.textRenderer, Text.literal(shown.isEmpty() ? "minecraft:item" : shown), x + 15, rowY + 6, customActive ? -1 : -7829368);
      ItemStack preview = WaypointManager.getIconStack(this.iconInput);
      context.drawItemWithoutEntity(preview, x + pw - 38, rowY + 2);
      rowY += 30;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Quick presets:"), x + 10, rowY, customActive ? -5592406 : -11184811);
      rowY += 14;
      int presetX = x + 10;

      for (int i = 0; i < ICON_PRESETS.length; i++) {
         int px = presetX + i * 28;
         ItemStack presetStack = WaypointManager.getIconStack(ICON_PRESETS[i]);
         context.fill(px, rowY, px + 24, rowY + 24, 1610612736);
         boolean selected = customActive && ICON_PRESETS[i].equals(this.iconInput.trim());
         if (selected) {
            this.drawBorder(context, px, rowY, 24, 24, VoidCyanClient.getPrimaryColor());
         }

         context.drawItemWithoutEntity(presetStack, px + 4, rowY + 4);
      }

      rowY += 36;
      context.drawTextWithShadow(this.textRenderer, Text.literal("Preview"), x + 10, rowY, VoidCyanClient.getPrimaryColor());
      rowY += 14;
      int previewW = BlockIndicatorManager.getPreviewWidth(this.client);
      context.getMatrices().pushMatrix();
      context.getMatrices().translate(x + 10, rowY);
      BlockIndicatorManager.renderPreview(context);
      context.getMatrices().popMatrix();
      context.drawTextWithShadow(
         this.textRenderer, Text.literal(previewW + " x " + BlockIndicatorManager.getHeight() + " px"), x + 10 + previewW + 8, rowY + 4, -5592406
      );
      context.drawTextWithShadow(this.textRenderer, Text.literal("Use Edit HUD to drag position · ESC to return"), x + 10, y + ph - 18, -5592406);
      context.getMatrices().popMatrix();
      int pw2 = this.panelWidth();
      int x2 = this.panelX();
      int y2 = this.panelY();
      int sx = x2 + pw2 - 150;
      int baseRow = y2 + 40;
      if (this.draggingWidth) {
         this.updateWidthFromMouse(mouseX, sx, sliderW);
      }

      if (this.draggingFillAlpha) {
         this.updateFillAlphaFromMouse(mouseX, sx, sliderW);
      }

      if (this.draggingBorderR) {
         this.updateBorderChannelFromMouse(mouseX, sx, sliderW, 0);
      }

      if (this.draggingBorderG) {
         this.updateBorderChannelFromMouse(mouseX, sx, sliderW, 1);
      }

      if (this.draggingBorderB) {
         this.updateBorderChannelFromMouse(mouseX, sx, sliderW, 2);
      }
   }

   private void drawModeButton(DrawContext context, int x, int y, int w, String label, boolean selected, int mouseX, int mouseY) {
      boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 20;
      int bg = selected ? VoidCyanClient.getPrimaryColor() & 16777215 | -1442840576 : (hover ? -2013222230 : 1610612736);
      context.fill(x, y, x + w, y + 20, bg);
      this.drawBorder(context, x, y, w, 20, selected ? VoidCyanClient.getPrimaryColor() : -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x + w / 2 - this.textRenderer.getWidth(label) / 2, y + 6, -1);
   }

   private void drawToggle(DrawContext context, int x, int y, boolean on, int mouseX, int mouseY) {
      int w = 44;
      int h = 18;
      int bg = on ? VoidCyanClient.getPrimaryColor() & 16777215 | -872415232 : -2144128205;
      context.fill(x, y, x + w, y + h, bg);
      this.drawBorder(context, x, y, w, h, on ? VoidCyanClient.getPrimaryColor() : -11184811);
      int knobX = on ? x + w - 16 : x + 2;
      context.fill(knobX, y + 2, knobX + 14, y + h - 2, on ? VoidCyanClient.getPrimaryColor() : -7829368);
      context.drawTextWithShadow(this.textRenderer, Text.literal(on ? "ON" : "OFF"), x + w / 2 - this.textRenderer.getWidth(on ? "ON" : "OFF") / 2, y + 5, -1);
   }

   private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }

   private void updateWidthFromMouse(double mouseX, int sliderX, int sliderW) {
      float t = (float)((mouseX - sliderX) / sliderW);
      t = Math.max(0.0F, Math.min(1.0F, t));
      BlockIndicatorManager.maxWidth = 80 + Math.round(t * 240.0F);
   }

   private void updateFillAlphaFromMouse(double mouseX, int sliderX, int sliderW) {
      float t = (float)((mouseX - sliderX) / sliderW);
      t = Math.max(0.0F, Math.min(1.0F, t));
      int alpha = Math.round(t * 255.0F);
      BlockIndicatorManager.fillColor = alpha << 24 | BlockIndicatorManager.fillColor & 16777215;
   }

   private void updateFillRGBChannelFromMouse(double mouseX, int sliderX, int sliderW, int channel) {
      float t = (float)((mouseX - sliderX) / sliderW);
      t = Math.max(0.0F, Math.min(1.0F, t));
      int val = Math.round(t * 255.0F);
      int fc = BlockIndicatorManager.fillColor;
      int r = fc >> 16 & 0xFF;
      int g = fc >> 8 & 0xFF;
      int b = fc & 0xFF;
      if (channel == 0) {
         r = val;
      } else if (channel == 1) {
         g = val;
      } else {
         b = val;
      }

      BlockIndicatorManager.fillColor = BlockIndicatorManager.fillColor & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private void updateBorderChannelFromMouse(double mouseX, int sliderX, int sliderW, int channel) {
      float t = (float)((mouseX - sliderX) / sliderW);
      t = Math.max(0.0F, Math.min(1.0F, t));
      int val = Math.round(t * 255.0F);
      int bc = BlockIndicatorManager.borderColor;
      int r = bc >> 16 & 0xFF;
      int g = bc >> 8 & 0xFF;
      int b = bc & 0xFF;
      if (channel == 0) {
         r = val;
      } else if (channel == 1) {
         g = val;
      } else {
         b = val;
      }

      BlockIndicatorManager.borderColor = 0xFF000000 | r << 16 | g << 8 | b;
   }

   private boolean handleClick(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         int pw = this.panelWidth();
         int x = this.panelX();
         int y = this.panelY();
         int sliderX = x + pw - 150;
         int sliderW = 140;
         int sliderH = 14;
         int rowY = y + 40;
         if (mouseY >= rowY + 4 && mouseY <= rowY + 18 && mouseX >= sliderX && mouseX <= sliderX + sliderW) {
            this.draggingWidth = true;
            this.updateWidthFromMouse(mouseX, sliderX, sliderW);
            VoidCyanClient.saveConfig();
            return true;
         } else {
            rowY += 36;
            if (mouseY >= rowY + 4 && mouseY <= rowY + 18 && mouseX >= sliderX && mouseX <= sliderX + sliderW) {
               this.draggingFillAlpha = true;
               this.updateFillAlphaFromMouse(mouseX, sliderX, sliderW);
               VoidCyanClient.saveConfig();
               return true;
            } else {
               rowY += 36;
               if (mouseX >= sliderX && mouseX <= sliderX + sliderW) {
                  if (mouseY >= rowY + 2 && mouseY <= rowY + 6) {
                     this.draggingBorderR = false;
                     this.draggingBorderG = false;
                     this.draggingBorderB = false;
                     this.updateFillRGBChannelFromMouse(mouseX, sliderX, sliderW, 0);
                     VoidCyanClient.saveConfig();
                     return true;
                  }

                  if (mouseY >= rowY + 9 && mouseY <= rowY + 13) {
                     this.updateFillRGBChannelFromMouse(mouseX, sliderX, sliderW, 1);
                     VoidCyanClient.saveConfig();
                     return true;
                  }

                  if (mouseY >= rowY + 16 && mouseY <= rowY + 20) {
                     this.updateFillRGBChannelFromMouse(mouseX, sliderX, sliderW, 2);
                     VoidCyanClient.saveConfig();
                     return true;
                  }
               }

               rowY += 36;
               int togX = sliderX + sliderW - 60;
               if (mouseX >= togX && mouseX <= togX + 44 && mouseY >= rowY + 4 && mouseY <= rowY + 22) {
                  BlockIndicatorManager.blurFill = !BlockIndicatorManager.blurFill;
                  VoidCyanClient.saveConfig();
                  return true;
               } else {
                  rowY += 36;
                  if (mouseX >= sliderX && mouseX <= sliderX + sliderW) {
                     if (mouseY >= rowY + 2 && mouseY <= rowY + 6) {
                        this.draggingBorderR = true;
                        this.updateBorderChannelFromMouse(mouseX, sliderX, sliderW, 0);
                        VoidCyanClient.saveConfig();
                        return true;
                     }

                     if (mouseY >= rowY + 9 && mouseY <= rowY + 13) {
                        this.draggingBorderG = true;
                        this.updateBorderChannelFromMouse(mouseX, sliderX, sliderW, 1);
                        VoidCyanClient.saveConfig();
                        return true;
                     }

                     if (mouseY >= rowY + 16 && mouseY <= rowY + 20) {
                        this.draggingBorderB = true;
                        this.updateBorderChannelFromMouse(mouseX, sliderX, sliderW, 2);
                        VoidCyanClient.saveConfig();
                        return true;
                     }
                  }

                  rowY += 36;
                  int offBtnX = x + pw - 280;
                  int blockBtnX = x + pw - 185;
                  int customBtnX = x + pw - 90;
                  if (mouseY >= rowY && mouseY <= rowY + 20) {
                     if (mouseX >= offBtnX && mouseX < offBtnX + 85) {
                        BlockIndicatorManager.iconMode = BlockIndicatorManager.IconMode.OFF;
                        VoidCyanClient.saveConfig();
                        return true;
                     }

                     if (mouseX >= blockBtnX && mouseX < blockBtnX + 90) {
                        BlockIndicatorManager.iconMode = BlockIndicatorManager.IconMode.BLOCK;
                        VoidCyanClient.saveConfig();
                        return true;
                     }

                     if (mouseX >= customBtnX && mouseX < customBtnX + 80) {
                        BlockIndicatorManager.iconMode = BlockIndicatorManager.IconMode.CUSTOM;
                        VoidCyanClient.saveConfig();
                        return true;
                     }
                  }

                  if (BlockIndicatorManager.iconMode != BlockIndicatorManager.IconMode.CUSTOM) {
                     this.editingIcon = false;
                     return false;
                  } else {
                     rowY += 40;
                     int inputW = pw - 60;
                     if (mouseX >= x + 10 && mouseX <= x + 10 + inputW && mouseY >= rowY && mouseY <= rowY + 20) {
                        this.editingIcon = true;
                        return true;
                     } else {
                        this.editingIcon = false;
                        rowY += 44;
                        int presetX = x + 10;

                        for (int i = 0; i < ICON_PRESETS.length; i++) {
                           int px = presetX + i * 28;
                           if (mouseX >= px && mouseX < px + 24 && mouseY >= rowY && mouseY < rowY + 24) {
                              this.iconInput = ICON_PRESETS[i];
                              BlockIndicatorManager.customIconItem = this.iconInput;
                              BlockIndicatorManager.iconMode = BlockIndicatorManager.IconMode.CUSTOM;
                              VoidCyanClient.saveConfig();
                              return true;
                           }
                        }

                        return false;
                     }
                  }
               }
            }
         }
      }
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      return this.handleClick(click.x(), click.y(), click.button()) ? true : super.mouseClicked(click, doubled);
   }

   public boolean mouseReleased(Click click) {
      if (!this.draggingWidth && !this.draggingFillAlpha && !this.draggingBorderR && !this.draggingBorderG && !this.draggingBorderB) {
         return super.mouseReleased(click);
      } else {
         this.draggingWidth = false;
         this.draggingFillAlpha = false;
         this.draggingBorderR = false;
         this.draggingBorderG = false;
         this.draggingBorderB = false;
         VoidCyanClient.saveConfig();
         return true;
      }
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      int sliderX = this.panelX() + this.panelWidth() - 150;
      int sliderW = 140;
      double mx = click.x();
      if (this.draggingWidth) {
         this.updateWidthFromMouse(mx, sliderX, sliderW);
         return true;
      } else if (this.draggingFillAlpha) {
         this.updateFillAlphaFromMouse(mx, sliderX, sliderW);
         return true;
      } else if (this.draggingBorderR) {
         this.updateBorderChannelFromMouse(mx, sliderX, sliderW, 0);
         return true;
      } else if (this.draggingBorderG) {
         this.updateBorderChannelFromMouse(mx, sliderX, sliderW, 1);
         return true;
      } else if (this.draggingBorderB) {
         this.updateBorderChannelFromMouse(mx, sliderX, sliderW, 2);
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   public boolean charTyped(CharInput input) {
      if (this.editingIcon && BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.CUSTOM) {
         char c = (char)input.codepoint();
         if (c >= ' ' && c != 127) {
            this.iconInput = this.iconInput + c;
            BlockIndicatorManager.customIconItem = this.iconInput.trim();
            VoidCyanClient.saveConfig();
         }

         return true;
      } else {
         return super.charTyped(input);
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         if (this.editingIcon) {
            this.editingIcon = false;
            return true;
         } else {
            this.client.setScreen(this.parent);
            return true;
         }
      } else {
         if (this.editingIcon && BlockIndicatorManager.iconMode == BlockIndicatorManager.IconMode.CUSTOM) {
            if (input.key() == 259 && !this.iconInput.isEmpty()) {
               this.iconInput = this.iconInput.substring(0, this.iconInput.length() - 1);
               BlockIndicatorManager.customIconItem = this.iconInput.trim();
               VoidCyanClient.saveConfig();
               return true;
            }

            if (input.key() == 257) {
               this.editingIcon = false;
               BlockIndicatorManager.customIconItem = this.iconInput.trim().isEmpty() ? "minecraft:grass_block" : this.iconInput.trim();
               this.iconInput = BlockIndicatorManager.customIconItem;
               VoidCyanClient.saveConfig();
               return true;
            }
         }

         return super.keyPressed(input);
      }
   }

   public boolean shouldPause() {
      return false;
   }
}
