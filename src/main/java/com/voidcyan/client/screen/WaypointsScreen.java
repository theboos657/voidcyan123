package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.WaypointManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class WaypointsScreen extends Screen {
   private final Screen parent;
   private int guiLeft;
   private int guiTop;
   private int guiWidth;
   private int guiHeight;
   private int leftPaneWidth;
   private int rightPaneWidth;
   private String searchQuery = "";
   private int scrollOffset = 0;
   private boolean editing = false;
   private int editIndex = -1;
   private String nameInput = "";
   private String xInput = "";
   private String yInput = "";
   private String zInput = "";
   private String iconInput = "minecraft:compass";
   private int selectedColor = 16777215;
   private boolean showLabel = true;
   private boolean showBlockDisplay = true;
   private boolean renderThroughWalls = true;
   private int fillOpacity = 10;
   private int outlineOpacity = 100;
   private String activeField = "";
   private String draggingSlider = null;
   private static final int[] COLORS = new int[]{16777215, 0, 16711680, 16746496, 16776960, 65280, 65535, 35071, 11141375, 16711935};

   private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y + 1, x + 1, y + h - 1, color);
      context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
   }

   public WaypointsScreen(Screen parent) {
      super(Text.literal("Waypoints"));
      this.parent = parent;
      this.resetForm();
   }

   private void resetForm() {
      this.editIndex = -1;
      this.nameInput = "";
      this.xInput = "";
      this.yInput = "";
      this.zInput = "";
      this.iconInput = "minecraft:compass";
      this.selectedColor = 16777215;
      this.showLabel = true;
      this.showBlockDisplay = true;
      this.renderThroughWalls = true;
      this.fillOpacity = 10;
      this.outlineOpacity = 100;
      this.activeField = "";
   }

   private void loadWaypointIntoForm(int index) {
      WaypointManager.Waypoint wp = WaypointManager.waypoints.get(index);
      this.editIndex = index;
      this.nameInput = wp.name;
      this.xInput = String.valueOf(wp.x);
      this.yInput = String.valueOf(wp.y);
      this.zInput = String.valueOf(wp.z);
      this.iconInput = wp.iconItem;
      this.selectedColor = wp.color;
      this.showLabel = wp.showLabel;
      this.showBlockDisplay = wp.showBlockDisplay;
      this.renderThroughWalls = wp.renderThroughWalls;
      this.fillOpacity = wp.fillOpacity;
      this.outlineOpacity = wp.outlineOpacity;
      this.editing = true;
   }

   protected void init() {
      super.init();
      this.guiWidth = Math.min(this.width - 40, 800);
      this.guiHeight = Math.min(this.height - 40, 500);
      this.guiLeft = (this.width - this.guiWidth) / 2;
      this.guiTop = (this.height - this.guiHeight) / 2;
      this.leftPaneWidth = (int)(this.guiWidth * 0.35);
      this.rightPaneWidth = this.guiWidth - this.leftPaneWidth - 10;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.fill(0, 0, this.width, this.height, -1442840576);
      super.render(context, mouseX, mouseY, deltaTicks);
      context.drawTextWithShadow(this.textRenderer, Text.literal("wWaypoints"), this.guiLeft, this.guiTop - 15, -1);
      String serverLabel = WaypointManager.getCurrentServerLabel(this.client);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Server: " + serverLabel), this.guiLeft, this.guiTop - 3, -5592406);
      this.renderLeftPane(context, mouseX, mouseY);
      this.renderRightPane(context, mouseX, mouseY);
      if (this.draggingSlider != null) {
         this.handleDrag(mouseX);
      }
   }

   private void renderLeftPane(DrawContext context, int mouseX, int mouseY) {
      int lx = this.guiLeft;
      int ly = this.guiTop;
      int lw = this.leftPaneWidth;
      int lh = this.guiHeight;
      context.fill(lx, ly, lx + lw, ly + lh, 1073741824);
      int sy = ly + 10;
      context.fill(lx + 10, sy, lx + lw - 10, sy + 20, Integer.MIN_VALUE);
      this.drawBorder(context, lx + 10, sy, lw - 20, 20, this.activeField.equals("search") ? VoidCyanClient.getPrimaryColor() : -11184811);
      String sText = this.searchQuery.isEmpty() && !this.activeField.equals("search")
         ? "Search..."
         : this.searchQuery + (this.activeField.equals("search") && System.currentTimeMillis() % 1000L < 500L ? "_" : "");
      context.drawTextWithShadow(this.textRenderer, Text.literal(sText), lx + 15, sy + 6, this.searchQuery.isEmpty() ? -5592406 : -1);
      int listY = sy + 30;
      int listH = lh - 40;
      List<WaypointManager.Waypoint> filtered = new ArrayList<>();

      for (WaypointManager.Waypoint wp : WaypointManager.waypoints) {
         if (this.searchQuery.isEmpty() || wp.name.toLowerCase().contains(this.searchQuery.toLowerCase())) {
            filtered.add(wp);
         }
      }

      context.enableScissor(lx, listY, lx + lw, listY + listH);
      if (filtered.isEmpty()) {
         context.drawTextWithShadow(this.textRenderer, Text.literal("No waypoints yet"), lx + 15, listY + 10, -5592406);
      } else {
         for (int i = 0; i < filtered.size(); i++) {
            WaypointManager.Waypoint wpx = filtered.get(i);
            int ry = listY + i * 30 - this.scrollOffset;
            if (ry + 30 >= listY && ry <= listY + listH) {
               boolean hovered = mouseX >= lx + 5 && mouseX <= lx + lw - 5 && mouseY >= ry && mouseY <= ry + 30 - 2;
               context.fill(lx + 5, ry, lx + lw - 5, ry + 30 - 2, hovered ? 1610634666 : 1073741824);
               context.fill(lx + 10, ry + 6, lx + 22, ry + 18, 0xFF000000 | wpx.color);
               context.drawTextWithShadow(this.textRenderer, Text.literal(wpx.name), lx + 30, ry + 8, -1);
               int tx = lx + lw - 30;
               int ty = ry + 6;
               context.fill(tx, ty, tx + 20, ty + 12, wpx.enabled ? -16733696 : -11184811);
               context.fill(tx + (wpx.enabled ? 10 : 2), ty + 2, tx + (wpx.enabled ? 18 : 10), ty + 10, -1);
            }
         }
      }

      context.disableScissor();
      context.fill(lx + 5, ly + lh - 25, lx + lw - 5, ly + lh - 5, 1610612736);
      this.drawBorder(context, lx + 5, ly + lh - 25, lw - 10, 20, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("+ New Waypoint"), lx + lw / 2 - 40, ly + lh - 19, VoidCyanClient.getPrimaryColor());
   }

   private void renderRightPane(DrawContext context, int mouseX, int mouseY) {
      int rx = this.guiLeft + this.leftPaneWidth + 10;
      int ry = this.guiTop;
      int rw = this.rightPaneWidth;
      int rh = this.guiHeight;
      context.fill(rx, ry, rx + rw, ry + rh, 1073741824);
      context.drawTextWithShadow(
         this.textRenderer, Text.literal(this.editIndex == -1 ? "Create waypoint" : "Edit waypoint"), rx + 10, ry + 10, VoidCyanClient.getPrimaryColor()
      );
      context.drawTextWithShadow(this.textRenderer, Text.literal("Fill details then press Create"), rx + 10, ry + 22, -5592406);
      context.fill(rx + 10, ry + 35, rx + rw - 10, ry + 36, -11184811);
      int fy = ry + 45;
      this.drawInput(context, "Name", this.nameInput, this.activeField.equals("name"), rx + 10, fy, rw - 220);
      this.drawInput(context, "X", this.xInput, this.activeField.equals("x"), rx + rw - 200, fy, 40);
      this.drawInput(context, "Y", this.yInput, this.activeField.equals("y"), rx + rw - 150, fy, 40);
      this.drawInput(context, "Z", this.zInput, this.activeField.equals("z"), rx + rw - 100, fy, 40);
      context.fill(rx + rw - 50, fy + 12, rx + rw - 10, fy + 32, 1610612736);
      this.drawBorder(context, rx + rw - 50, fy + 12, 40, 20, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Copy"), rx + rw - 42, fy + 18, -1);
      int sy = fy + 50;
      int sectionW = (rw - 30) / 2;
      this.drawSectionHeader(context, "Label", this.showLabel, rx + 10, sy, sectionW);
      this.drawBorder(context, rx + 10, sy + 20, sectionW, 120, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Waypoint Color"), rx + 20, sy + 30, -5592406);
      int cx = rx + 20;

      for (int i = 0; i < COLORS.length; i++) {
         context.fill(cx + i * 16, sy + 45, cx + i * 16 + 14, sy + 59, 0xFF000000 | COLORS[i]);
         if (COLORS[i] == (this.selectedColor & 16777215)) {
            this.drawBorder(context, cx + i * 16 - 1, sy + 44, 16, 16, VoidCyanClient.getPrimaryColor());
         }
      }

      context.drawTextWithShadow(this.textRenderer, Text.literal("Icon Item"), rx + 20, sy + 70, -5592406);
      this.drawInput(context, "minecraft:item", this.iconInput, this.activeField.equals("icon"), rx + 20, sy + 82, sectionW - 30);
      ItemStack previewIcon = WaypointManager.getIconStack(this.iconInput);
      context.drawItemWithoutEntity(previewIcon, rx + sectionW - 24, sy + 86);
      int rx2 = rx + 20 + sectionW;
      this.drawSectionHeader(context, "Block Display", this.showBlockDisplay, rx2, sy, sectionW);
      this.drawBorder(context, rx2, sy + 20, sectionW, 120, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Render Through Walls"), rx2 + 10, sy + 30, -1);
      this.drawToggle(context, rx2 + sectionW - 30, sy + 30, this.renderThroughWalls);
      this.drawSlider(context, "Fill Opacity", this.fillOpacity, rx2 + 10, sy + 60, sectionW - 20);
      this.drawSlider(context, "Outline Opacity", this.outlineOpacity, rx2 + 10, sy + 100, sectionW - 20);
      int by = ry + rh - 35;
      if (this.editIndex != -1) {
         context.fill(rx + 10, by, rx + 100, by + 25, -2136342528);
         context.drawTextWithShadow(this.textRenderer, Text.literal("Delete"), rx + 40, by + 8, -1);
      } else {
         context.fill(rx + 10, by, rx + 100, by + 25, 1610612736);
         this.drawBorder(context, rx + 10, by, 90, 25, -11184811);
         context.drawTextWithShadow(this.textRenderer, Text.literal("Clear"), rx + 40, by + 8, -1);
      }

      context.fill(rx + 110, by, rx + rw - 90, by + 25, -2147461718);
      context.drawTextWithShadow(
         this.textRenderer, Text.literal(this.editIndex == -1 ? "Create Waypoint" : "Save Waypoint"), rx + 110 + (rw - 200) / 2 - 30, by + 8, -1
      );
      context.fill(rx + rw - 80, by, rx + rw - 10, by + 25, 1610612736);
      this.drawBorder(context, rx + rw - 80, by, 70, 25, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("Cancel"), rx + rw - 60, by + 8, -1);
   }

   private void drawInput(DrawContext context, String label, String value, boolean active, int x, int y, int w) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y, -5592406);
      context.fill(x, y + 12, x + w, y + 32, Integer.MIN_VALUE);
      this.drawBorder(context, x, y + 12, w, 20, active ? VoidCyanClient.getPrimaryColor() : -11184811);
      String t = value + (active && System.currentTimeMillis() % 1000L < 500L ? "_" : "");
      context.drawTextWithShadow(this.textRenderer, Text.literal(t), x + 5, y + 18, -1);
   }

   private void drawSectionHeader(DrawContext context, String title, boolean toggled, int x, int y, int w) {
      context.fill(x, y, x + w, y + 20, 1610612736);
      context.drawTextWithShadow(this.textRenderer, Text.literal(title), x + 10, y + 6, -1);
      this.drawToggle(context, x + w - 30, y + 4, toggled);
   }

   private void drawToggle(DrawContext context, int x, int y, boolean toggled) {
      context.fill(x, y, x + 20, y + 12, toggled ? -16733696 : -11184811);
      context.fill(x + (toggled ? 10 : 2), y + 2, x + (toggled ? 18 : 10), y + 10, -1);
   }

   private void drawSlider(DrawContext context, String label, int value, int x, int y, int w) {
      context.drawTextWithShadow(this.textRenderer, Text.literal(label), x, y, -1);
      context.fill(x, y + 15, x + w - 30, y + 17, -11184811);
      int handleX = x + (int)((w - 30) * (value / 100.0F));
      context.fill(handleX - 3, y + 12, handleX + 3, y + 20, -1);
      context.drawTextWithShadow(this.textRenderer, Text.literal(value + "%"), x + w - 25, y + 10, VoidCyanClient.getPrimaryColor());
   }

   private void handleDrag(int mouseX) {
      int rx2 = this.guiLeft + this.leftPaneWidth + 10 + (this.rightPaneWidth - 30) / 2 + 20;
      int sectionW = (this.rightPaneWidth - 30) / 2;
      int sw = sectionW - 20 - 30;
      float val = Math.max(0.0F, Math.min(1.0F, (float)(mouseX - rx2 - 10) / sw));
      if (this.draggingSlider.equals("fill")) {
         this.fillOpacity = (int)(val * 100.0F);
      }

      if (this.draggingSlider.equals("outline")) {
         this.outlineOpacity = (int)(val * 100.0F);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return false;
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      double mouseX = click.x();
      double mouseY = click.y();
      int button = click.button();
      int lx = this.guiLeft;
      int ly = this.guiTop;
      int lw = this.leftPaneWidth;
      int lh = this.guiHeight;
      if (mouseX >= lx + 10 && mouseX <= lx + lw - 10 && mouseY >= ly + 10 && mouseY <= ly + 30) {
         this.activeField = "search";
         return true;
      } else if (mouseX >= lx + 5 && mouseX <= lx + lw - 5 && mouseY >= ly + lh - 25 && mouseY <= ly + lh - 5) {
         this.resetForm();
         return true;
      } else {
         int listY = ly + 40;
         List<WaypointManager.Waypoint> filtered = new ArrayList<>();
         List<Integer> actualIndices = new ArrayList<>();

         for (int i = 0; i < WaypointManager.waypoints.size(); i++) {
            WaypointManager.Waypoint wp = WaypointManager.waypoints.get(i);
            if (this.searchQuery.isEmpty() || wp.name.toLowerCase().contains(this.searchQuery.toLowerCase())) {
               filtered.add(wp);
               actualIndices.add(i);
            }
         }

         for (int ix = 0; ix < filtered.size(); ix++) {
            int ry = listY + ix * 30 - this.scrollOffset;
            if (ry >= listY && ry <= listY + lh - 40 && mouseX >= lx + 5 && mouseX <= lx + lw - 5 && mouseY >= ry && mouseY <= ry + 30 - 2) {
               int tx = lx + lw - 30;
               if (mouseX >= tx && mouseX <= tx + 20) {
                  filtered.get(ix).enabled = !filtered.get(ix).enabled;
                  WaypointManager.saveWaypoints();
                  return true;
               }

               this.loadWaypointIntoForm(actualIndices.get(ix));
               return true;
            }
         }

         int rx = this.guiLeft + this.leftPaneWidth + 10;
         int ry = this.guiTop;
         int rw = this.rightPaneWidth;
         int rh = this.guiHeight;
         int fy = ry + 45;
         if (mouseY >= fy + 12 && mouseY <= fy + 32) {
            if (mouseX >= rx + 10 && mouseX <= rx + rw - 220) {
               this.activeField = "name";
               return true;
            }

            if (mouseX >= rx + rw - 200 && mouseX <= rx + rw - 160) {
               this.activeField = "x";
               return true;
            }

            if (mouseX >= rx + rw - 150 && mouseX <= rx + rw - 110) {
               this.activeField = "y";
               return true;
            }

            if (mouseX >= rx + rw - 100 && mouseX <= rx + rw - 60) {
               this.activeField = "z";
               return true;
            }
         }

         if (mouseX >= rx + rw - 50 && mouseX <= rx + rw - 10 && mouseY >= fy + 12 && mouseY <= fy + 32) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.xInput + ", " + this.yInput + ", " + this.zInput);
            return true;
         } else {
            int sy = fy + 50;
            int sectionW = (rw - 30) / 2;
            if (mouseX >= rx + sectionW - 20 && mouseX <= rx + sectionW && mouseY >= sy && mouseY <= sy + 20) {
               this.showLabel = !this.showLabel;
               return true;
            } else {
               int cx = rx + 20;
               if (mouseY >= sy + 45 && mouseY <= sy + 59) {
                  for (int ixx = 0; ixx < COLORS.length; ixx++) {
                     if (mouseX >= cx + ixx * 16 && mouseX <= cx + ixx * 16 + 14) {
                        this.selectedColor = COLORS[ixx];
                        return true;
                     }
                  }
               }

               if (mouseY >= sy + 94 && mouseY <= sy + 114 && mouseX >= rx + 20 && mouseX <= rx + 20 + sectionW - 30) {
                  this.activeField = "icon";
                  return true;
               } else {
                  int rx2 = rx + 20 + sectionW;
                  if (mouseX >= rx2 + sectionW - 20 && mouseX <= rx2 + sectionW && mouseY >= sy && mouseY <= sy + 20) {
                     this.showBlockDisplay = !this.showBlockDisplay;
                     return true;
                  } else if (mouseX >= rx2 + sectionW - 30 && mouseX <= rx2 + sectionW - 10 && mouseY >= sy + 30 && mouseY <= sy + 42) {
                     this.renderThroughWalls = !this.renderThroughWalls;
                     return true;
                  } else if (mouseY >= sy + 70 && mouseY <= sy + 80 && mouseX >= rx2 + 10 && mouseX <= rx2 + sectionW - 10) {
                     this.draggingSlider = "fill";
                     return true;
                  } else if (mouseY >= sy + 110 && mouseY <= sy + 120 && mouseX >= rx2 + 10 && mouseX <= rx2 + sectionW - 10) {
                     this.draggingSlider = "outline";
                     return true;
                  } else {
                     int by = ry + rh - 35;
                     if (mouseX >= rx + 10 && mouseX <= rx + 100 && mouseY >= by && mouseY <= by + 25) {
                        if (this.editIndex != -1) {
                           WaypointManager.removeWaypoint(this.editIndex);
                           this.resetForm();
                        } else {
                           this.resetForm();
                        }

                        return true;
                     } else if (mouseX >= rx + 110 && mouseX <= rx + rw - 90 && mouseY >= by && mouseY <= by + 25) {
                        this.saveWaypoint();
                        return true;
                     } else if (mouseX >= rx + rw - 80 && mouseX <= rx + rw - 10 && mouseY >= by && mouseY <= by + 25) {
                        this.resetForm();
                        return true;
                     } else {
                        return super.mouseClicked(click, doubled);
                     }
                  }
               }
            }
         }
      }
   }

   public boolean mouseReleased(Click click) {
      if (this.draggingSlider != null) {
         this.draggingSlider = null;
         return true;
      } else {
         return super.mouseReleased(click);
      }
   }

   private void saveWaypoint() {
      if (!this.nameInput.trim().isEmpty()) {
         try {
            int x = this.xInput.isEmpty() ? 0 : Integer.parseInt(this.xInput.trim());
            int y = this.yInput.isEmpty() ? 64 : Integer.parseInt(this.yInput.trim());
            int z = this.zInput.isEmpty() ? 0 : Integer.parseInt(this.zInput.trim());
            String dim = this.client != null && this.client.world != null ? this.client.world.getRegistryKey().getValue().getPath() : "overworld";
            if (this.editIndex == -1) {
               String icon = this.iconInput.trim().isEmpty() ? "minecraft:compass" : this.iconInput.trim();
               WaypointManager.addWaypoint(
                  this.nameInput.trim(),
                  x,
                  y,
                  z,
                  dim,
                  this.selectedColor,
                  this.renderThroughWalls,
                  this.fillOpacity,
                  this.outlineOpacity,
                  this.showLabel,
                  this.showBlockDisplay,
                  icon
               );
            } else {
               WaypointManager.Waypoint wp = WaypointManager.waypoints.get(this.editIndex);
               wp.name = this.nameInput.trim();
               wp.x = x;
               wp.y = y;
               wp.z = z;
               wp.color = this.selectedColor;
               wp.showLabel = this.showLabel;
               wp.showBlockDisplay = this.showBlockDisplay;
               wp.renderThroughWalls = this.renderThroughWalls;
               wp.fillOpacity = this.fillOpacity;
               wp.outlineOpacity = this.outlineOpacity;
               wp.iconItem = this.iconInput.trim().isEmpty() ? "minecraft:compass" : this.iconInput.trim();
               WaypointManager.saveWaypoints();
            }

            this.resetForm();
         } catch (NumberFormatException var6) {
         }
      }
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         this.client.setScreen(this.parent);
         return true;
      } else if (input.key() == 259) {
         String var7 = this.activeField;
         switch (var7) {
            case "search":
               if (!this.searchQuery.isEmpty()) {
                  this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
               }
               break;
            case "name":
               if (!this.nameInput.isEmpty()) {
                  this.nameInput = this.nameInput.substring(0, this.nameInput.length() - 1);
               }
               break;
            case "x":
               if (!this.xInput.isEmpty()) {
                  this.xInput = this.xInput.substring(0, this.xInput.length() - 1);
               }
               break;
            case "y":
               if (!this.yInput.isEmpty()) {
                  this.yInput = this.yInput.substring(0, this.yInput.length() - 1);
               }
               break;
            case "z":
               if (!this.zInput.isEmpty()) {
                  this.zInput = this.zInput.substring(0, this.zInput.length() - 1);
               }
               break;
            case "icon":
               if (!this.iconInput.isEmpty()) {
                  this.iconInput = this.iconInput.substring(0, this.iconInput.length() - 1);
               }
         }

         return true;
      } else {
         int key = input.key();
         char chr = 0;
         if (key >= 65 && key <= 90) {
            chr = (char)(97 + (key - 65));
            long window = this.client.getWindow().getHandle();
            boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
            if (shift) {
               chr = Character.toUpperCase(chr);
            }
         } else if (key >= 48 && key <= 57) {
            chr = (char)(48 + (key - 48));
         } else if (key == 45 || key == 333) {
            chr = '-';
         } else if (key == 32) {
            chr = ' ';
         } else if (key == 59) {
            long window = this.client.getWindow().getHandle();
            boolean shift = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
            chr = (char)(shift ? 58 : 59);
         } else if (key == 47 || key == 46) {
            if (key == 47) {
               chr = '/';
            } else {
               chr = '.';
            }
         }

         if (chr != 0) {
            String var10 = this.activeField;
            switch (var10) {
               case "search":
                  this.searchQuery = this.searchQuery + chr;
                  return true;
               case "name":
                  if (this.nameInput.length() < 30) {
                     this.nameInput = this.nameInput + chr;
                  }

                  return true;
               case "x":
                  if ((Character.isDigit(chr) || chr == '-') && this.xInput.length() < 8) {
                     this.xInput = this.xInput + chr;
                  }

                  return true;
               case "y":
                  if ((Character.isDigit(chr) || chr == '-') && this.yInput.length() < 8) {
                     this.yInput = this.yInput + chr;
                  }

                  return true;
               case "z":
                  if ((Character.isDigit(chr) || chr == '-') && this.zInput.length() < 8) {
                     this.zInput = this.zInput + chr;
                  }

                  return true;
               case "icon":
                  if (this.iconInput.length() < 64) {
                     this.iconInput = this.iconInput + Character.toLowerCase(chr);
                  }

                  return true;
            }
         }

         return super.keyPressed(input);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int maxScroll = Math.max(0, WaypointManager.waypoints.size() * 30 - (this.guiHeight - 40));
      this.scrollOffset -= (int)(verticalAmount * 20.0);
      this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset));
      return true;
   }

   public boolean shouldPause() {
      return false;
   }
}
