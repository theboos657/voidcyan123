package com.voidcyan.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.social.FriendManager;
import com.voidcyan.client.util.WaypointManager;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents.Unload;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class LogoutSpotsManager {
   public static final List<LogoutSpotsManager.LogoutSpot> spots = new CopyOnWriteArrayList<>();
   private static File saveFile;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Map<String, Vec3d> recentPlayerPositions = new HashMap<>();
   private static final Map<String, Long> recentPlayerTimes = new HashMap<>();
   private static final List<LogoutSpotsManager.LogoutSpot> pendingSpots = new CopyOnWriteArrayList<>();

   public static void init() {
      MinecraftClient client = MinecraftClient.getInstance();
      saveFile = new File(client.runDirectory, "voidcyan_logout_spots.json");
      loadSpots();
      ClientEntityEvents.ENTITY_UNLOAD.register((Unload)(entity, world) -> {
         if (VoidCyanClient.isLogoutSpotsEnabled) {
            if (VoidCyanClient.logoutSpotsDetectDisappears) {
               if (entity instanceof PlayerEntity player && client.player != null && player != client.player) {
                  String name = player.getName().getString();
                  if (VoidCyanClient.logoutSpotsIgnoreFriends && FriendManager.isFriendOnline(name)) {
                     return;
                  }

                  if (player.getHealth() <= 0.0F) {
                     return;
                  }

                  double distance = player.distanceTo(client.player);
                  int viewDistance = client.options.getClampedViewDistance() * 16;
                  if (distance < viewDistance - 16) {
                     queueSpot(name, player.getX(), player.getY(), player.getZ(), "Disappear");
                  }
               }
            }
         }
      });
      ClientReceiveMessageEvents.ALLOW_GAME.register((AllowGame)(message, overlay) -> {
         if (!VoidCyanClient.isLogoutSpotsEnabled) {
            return true;
         } else if (!VoidCyanClient.logoutSpotsLeaveMessages) {
            return true;
         } else {
            String text = message.getString();
            String lowerText = text.toLowerCase();
            boolean isLeave = lowerText.contains("left the game") || lowerText.contains("disconnected") || lowerText.contains("kicked");
            if (isLeave) {
               String[] words = text.split(" ");
               if (words.length > 0) {
                  String name = words[0];
                  name = name.replaceAll("[^a-zA-Z0-9_]", "");
                  if (VoidCyanClient.logoutSpotsIgnoreFriends && FriendManager.isFriendOnline(name)) {
                     return true;
                  }

                  if (recentPlayerPositions.containsKey(name)) {
                     long timeSinceSeen = System.currentTimeMillis() - recentPlayerTimes.get(name);
                     if (timeSinceSeen < 10000L) {
                        Vec3d pos = recentPlayerPositions.get(name);
                        queueSpot(name, pos.x, pos.y, pos.z, "Leave Message");
                     }
                  }
               }
            }

            return true;
         }
      });
   }

   public static void tick() {
      if (VoidCyanClient.isLogoutSpotsEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null && client.player != null) {
            if (!pendingSpots.isEmpty()) {
               List<LogoutSpotsManager.LogoutSpot> toProcess = new ArrayList<>(pendingSpots);
               pendingSpots.clear();

               for (LogoutSpotsManager.LogoutSpot s : toProcess) {
                  addSpot(s.playerName, s.x, s.y, s.z, s.type);
               }
            }

            for (Entity entity : client.world.getEntities()) {
               if (entity instanceof PlayerEntity player && player != client.player) {
                  String name = player.getName().getString();
                  recentPlayerPositions.put(name, new Vec3d(player.getX(), player.getY(), player.getZ()));
                  recentPlayerTimes.put(name, System.currentTimeMillis());
               }
            }
         }
      }
   }

   private static void queueSpot(String name, double x, double y, double z, String type) {
      long now = System.currentTimeMillis();

      for (LogoutSpotsManager.LogoutSpot s : pendingSpots) {
         if (s.playerName.equals(name)) {
            return;
         }
      }

      for (LogoutSpotsManager.LogoutSpot sx : spots) {
         if (sx.playerName.equals(name) && Math.abs(sx.timestamp - now) < 2000L) {
            return;
         }
      }

      pendingSpots.add(new LogoutSpotsManager.LogoutSpot(name, x, y, z, now, type));
   }

   public static void addSpot(String name, double x, double y, double z, String type) {
      long now = System.currentTimeMillis();

      for (LogoutSpotsManager.LogoutSpot spot : spots) {
         if (spot.playerName.equals(name) && Math.abs(spot.timestamp - now) < 2000L) {
            return;
         }
      }

      LogoutSpotsManager.LogoutSpot spotx = new LogoutSpotsManager.LogoutSpot(name, x, y, z, now, type);
      spots.add(0, spotx);
      saveSpots();
      if (VoidCyanClient.logoutSpotsLeaveMessages || VoidCyanClient.logoutSpotsDetectDisappears) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player != null) {
            String msg = "§c[Logout] §f";
            if (type.equals("Disappear")) {
               msg = "§c[Disappear] §fDisappear logout for " + name;
            } else {
               msg = "§c[Logout] §fLogout for " + name;
            }

            msg = msg + String.format(" at X:%.0f Y:%.0f Z:%.0f", x, y, z);
            mc.player.sendMessage(Text.literal(msg), false);
         }
      }

      if (VoidCyanClient.logoutSpotsCreateWaypoints) {
         MinecraftClient mc = MinecraftClient.getInstance();
         String dimension = "overworld";
         if (mc.world != null) {
            dimension = mc.world.getRegistryKey().getValue().getPath();
         }

         WaypointManager.addWaypoint("Logout: " + name, (int)x, (int)y, (int)z, dimension, -65536);
         WaypointManager.saveWaypoints();
      }
   }

   public static void renderAll(WorldRenderContext ctx) {
      if (VoidCyanClient.isLogoutSpotsEnabled) {
         if (!spots.isEmpty()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world != null) {
               Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
               MatrixStack matrices = ctx.matrices();
               VertexConsumerProvider consumers = ctx.consumers();
               if (consumers != null) {
                  VertexConsumer lineConsumer = consumers.getBuffer(RenderLayers.linesTranslucent());

                  for (LogoutSpotsManager.LogoutSpot spot : spots) {
                     double dx = spot.x - cameraPos.x;
                     double dy = spot.y - cameraPos.y;
                     double dz = spot.z - cameraPos.z;
                     double boxSize = 0.4;
                     double height = 1.8;
                     Box box = new Box(dx - boxSize, dy, dz - boxSize, dx + boxSize, dy + height, dz + boxSize);
                     drawBoxLines(matrices, lineConsumer, box, 1.0F, 0.0F, 0.0F, 255);
                  }
               }
            }
         }
      }
   }

   private static void drawBoxLines(MatrixStack matrices, VertexConsumer consumer, Box box, float r, float g, float b, int a) {
      Matrix4f m = matrices.peek().getPositionMatrix();
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      consumer.vertex(m, minX, minY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, minY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, minY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, minY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, maxY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, maxY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, minY, minZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, maxX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, minY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
      consumer.vertex(m, minX, maxY, maxZ).color(r, g, b, a).normal(0.0F, 0.0F, 0.0F).lineWidth(2.0F);
   }

   public static void clearAll() {
      spots.clear();
      saveSpots();
   }

   public static void deleteSpot(LogoutSpotsManager.LogoutSpot spot) {
      spots.remove(spot);
      saveSpots();
   }

   public static void loadSpots() {
      if (saveFile.exists()) {
         try (FileReader reader = new FileReader(saveFile)) {
            Type listType = (new TypeToken<ArrayList<LogoutSpotsManager.LogoutSpot>>() {}).getType();
            List<LogoutSpotsManager.LogoutSpot> loaded = (List<LogoutSpotsManager.LogoutSpot>)GSON.fromJson(reader, listType);
            if (loaded != null) {
               spots.clear();
               spots.addAll(loaded);
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }

   public static void saveSpots() {
      try (FileWriter writer = new FileWriter(saveFile)) {
         GSON.toJson(spots, writer);
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void recordLogout(PlayerEntity player) {
      if (VoidCyanClient.isLogoutSpotsEnabled) {
         if (VoidCyanClient.logoutSpotsDetectDisappears) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
               String name = player.getName().getString();
               if (!VoidCyanClient.logoutSpotsIgnoreFriends || !FriendManager.isFriendOnline(name)) {
                  if (!(player.getHealth() <= 0.0F)) {
                     double distance = player.distanceTo(client.player);
                     int viewDistance = client.options.getClampedViewDistance() * 16;
                     if (distance < viewDistance - 16) {
                        addSpot(name, player.getX(), player.getY(), player.getZ(), "Disconnect");
                     }
                  }
               }
            }
         }
      }
   }

   public static class LogoutSpot {
      public String playerName;
      public double x;
      public double y;
      public double z;
      public long timestamp;
      public String type;

      public LogoutSpot(String name, double x, double y, double z, long ts, String type) {
         this.playerName = name;
         this.x = x;
         this.y = y;
         this.z = z;
         this.timestamp = ts;
         this.type = type;
      }
   }
}
