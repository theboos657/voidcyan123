package com.voidcyan.client.module;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;

public final class ServerInfoManager {

   private ServerInfoManager() {
   }

   public static ServerInfoManager.ServerDetails getDetails(MinecraftClient client, boolean sample) {
      if (sample || client == null || client.world == null) {
         return new ServerInfoManager.ServerDetails("Example Server", "play.example.com");
      } else if (client.isInSingleplayer()) {
         String worldName = "Singleplayer";
         IntegratedServer server = client.getServer();
         if (server != null) {
            worldName = server.getSaveProperties().getLevelName();
         }

         return new ServerInfoManager.ServerDetails(worldName, "Singleplayer");
      } else {
         ServerInfo info = client.getCurrentServerEntry();
         if (info == null) {
            String fallbackAddress = resolveConnectionAddress(client);
            return new ServerInfoManager.ServerDetails(fallbackAddress, fallbackAddress);
         } else {
            String address = info.address != null && !info.address.isBlank() ? info.address.trim() : "Unknown";
            String name = info.name != null && !info.name.isBlank() ? info.name.trim() : address;
            return new ServerInfoManager.ServerDetails(name, address);
         }
      }
   }

   private static String resolveConnectionAddress(MinecraftClient client) {
      if (client.getNetworkHandler() != null
         && client.getNetworkHandler().getConnection() != null
         && client.getNetworkHandler().getConnection().getAddress() != null) {
         SocketAddress address = client.getNetworkHandler().getConnection().getAddress();
         if (address instanceof InetSocketAddress inet) {
            String host = inet.getHostString();
            int port = inet.getPort();
            if (host != null && !host.isBlank()) {
               if (port > 0 && port != 25565) {
                  return host + ":" + port;
               }

               return host;
            }
         }

         return address.toString();
      } else {
         return "Unknown";
      }
   }

   public static int getWidth(MinecraftClient client) {
      return getWidth(client, false);
   }

   public static int getPreviewWidth(MinecraftClient client) {
      return getWidth(client, true);
   }

   private static int getWidth(MinecraftClient client, boolean sample) {
      if (client != null && client.textRenderer != null) {
         ServerInfoManager.ServerDetails details = getDetails(client, sample);
         int max = client.textRenderer.getWidth(details.name());
         max = Math.max(max, client.textRenderer.getWidth(details.address()));
         return max + 10;
      } else {
         return 140;
      }
   }

   public static int getHeight() {
      return 34;
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.currentScreen == null) {
         drawPanel(context, client, 0, 0, false);
      }
   }

   public static void renderPreview(DrawContext context) {
      drawPanel(context, MinecraftClient.getInstance(), 0, 0, true);
   }

   private static void drawPanel(DrawContext context, MinecraftClient client, int x, int y, boolean sample) {
      if (client != null && client.textRenderer != null) {
         ServerInfoManager.ServerDetails details = getDetails(client, sample);
         int boxW = getWidth(client, sample);
         int boxH = getHeight();
         context.fill(x, y, x + boxW, y + boxH, Integer.MIN_VALUE);
         drawBorder(context, x, y, boxW, boxH, -16711681);
         int textY = y + 5;
         context.drawTextWithShadow(client.textRenderer, Text.literal(details.name()), x + 5, textY, -1);
         textY += 12;
         context.drawTextWithShadow(client.textRenderer, Text.literal(details.address()), x + 5, textY, -5592406);
      }
   }

   private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
      context.fill(x, y, x + w, y + 1, color);
      context.fill(x, y + h - 1, x + w, y + h, color);
      context.fill(x, y, x + 1, y + h, color);
      context.fill(x + w - 1, y, x + w, y + h, color);
   }

   public record ServerDetails(String name, String address) {
   }
}
