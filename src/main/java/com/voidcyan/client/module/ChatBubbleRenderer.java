package com.voidcyan.client.module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class ChatBubbleRenderer {
   private record Bubble(String text, long expireAt) {
   }

   private static final Map<String, Bubble> BUBBLES = new ConcurrentHashMap<>();

   public static void addMessage(String playerName, String text) {
      BUBBLES.put(playerName, new Bubble(text, System.currentTimeMillis() + 5000L));
   }

   public static void renderAll3D(WorldRenderContext ctx) {
      if (BUBBLES.isEmpty()) return;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.world == null) return;
      Camera camera = client.gameRenderer.getCamera();
      MatrixStack matrices = ctx.matrices();
      VertexConsumerProvider consumers = ctx.consumers();
      if (camera == null || matrices == null || consumers == null) return;
      Vec3d cameraPos = camera.getCameraPos();
      TextRenderer tr = client.textRenderer;
      long now = System.currentTimeMillis();

      for (PlayerEntity player : client.world.getPlayers()) {
         Bubble bubble = BUBBLES.get(player.getName().getString());
         if (bubble == null || bubble.expireAt() < now) continue;
         matrices.push();
         matrices.translate(
            player.getX() - cameraPos.x, player.getY() - cameraPos.y + player.getHeight() + 0.7, player.getZ() - cameraPos.z
         );
         matrices.multiply(camera.getRotation());
         matrices.scale(-0.025F, -0.025F, 0.025F);
         Text text = Text.literal(bubble.text());
         int width = tr.getWidth(text);
         tr.draw(
            text, -width / 2.0F, 0.0F, -1, false, matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.NORMAL, 0x80000000, 0xF000F0
         );
         matrices.pop();
      }

      BUBBLES.entrySet().removeIf(e -> e.getValue().expireAt() < now);
   }
}
