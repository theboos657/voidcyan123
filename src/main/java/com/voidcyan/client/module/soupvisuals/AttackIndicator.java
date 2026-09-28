package com.voidcyan.client.module.soupvisuals;

import com.voidcyan.client.VoidCyanClient;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class AttackIndicator {
   private static final long initTime = System.currentTimeMillis();
   private static float rollAngle = 0.0F;
   private static long lastUpdateTime = System.currentTimeMillis();
   private static Entity lastTargetEntity = null;
   private static long lastTargetUpdateTime = 0L;
   public static final Identifier LEGACY = Identifier.of("voidcyan", "textures/target_render/legacy.png");
   private static final Identifier SCIFI = Identifier.of("voidcyan", "textures/target_render/scifi.png");
   private static final Identifier SIMPLE = Identifier.of("voidcyan", "textures/target_render/simple.png");
   private static final Identifier BO = Identifier.of("voidcyan", "textures/target_render/bo.png");
   private static final Identifier MARKER = Identifier.of("voidcyan", "textures/target_render/marker.png");
   private static final Identifier SKULL = Identifier.of("voidcyan", "textures/target_render/skull.png");
   public static final Identifier AMOGUS = Identifier.of("voidcyan", "textures/target_render/amongus.png");
   private static final Identifier VEGAS = Identifier.of("voidcyan", "textures/target_render/vegas.png");
   private static final Identifier JEKA = Identifier.of("voidcyan", "textures/target_render/jeka.png");
   public static final Identifier FIREFLY = Identifier.of("voidcyan", "textures/particle/firefly.png");
   public static final Identifier FIREFLY_ALT = Identifier.of("voidcyan", "textures/particle/firefly_alt.png");
   private static final Identifier WHITE_TEX = Identifier.of("voidcyan", "textures/gui/white.png");
   private static final Color C1 = new Color(6725887);
   private static final Color C2 = new Color(6091007);
   private static final Color C3 = new Color(16757334);
   private static final Color C4 = new Color(16730698);

   public static Color getColor(float position) {
      position %= 1.0F;
      if (position < 0.0F) {
         position++;
      }

      if (position < 0.33333334F) {
         return AttackIndicator.OklabUtils.interpolate(C1, C2, position * 3.0F);
      } else {
         return position < 0.6666667F
            ? AttackIndicator.OklabUtils.interpolate(C2, C3, (position - 0.33333334F) * 3.0F)
            : AttackIndicator.OklabUtils.interpolate(C3, C4, (position - 0.6666667F) * 3.0F);
      }
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
   }

   public static Identifier getTargetRenderTexture() {
      if (VoidCyanClient.attackIndicatorLegacyTexture == null) {
         return LEGACY;
      } else {
         String var0 = VoidCyanClient.attackIndicatorLegacyTexture.toUpperCase();

         return switch (var0) {
            case "MARKER" -> MARKER;
            case "BO" -> BO;
            case "SIMPLE" -> SIMPLE;
            case "SCIFI" -> SCIFI;
            case "JEKA" -> JEKA;
            case "AMONGUS" -> AMOGUS;
            case "SKULL" -> SKULL;
            case "VEGAS" -> VEGAS;
            default -> LEGACY;
         };
      }
   }

   public static Identifier getSoulTexture() {
      if (VoidCyanClient.attackIndicatorSoulTexture == null) {
         return FIREFLY_ALT;
      } else {
         String var0 = VoidCyanClient.attackIndicatorSoulTexture.toUpperCase();
         byte var1 = -1;
         switch (var0.hashCode()) {
            case -131469731:
               if (var0.equals("FIREFLY")) {
                  var1 = 0;
               }
            default:
               return switch (var1) {
                  case 0 -> FIREFLY;
                  default -> FIREFLY_ALT;
               };
         }
      }
   }

   private static boolean updateOrKeepTarget() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && VoidCyanClient.attackIndicatorEnabled) {
         long currentTime = System.currentTimeMillis();
         Entity currentTarget = null;
         double maxReach = VoidCyanClient.targetIndicatorMaxReach;
         if (VoidCyanClient.targetIndicatorHitReachOnly) {
            maxReach = client.player.isCreative() ? 4.5 : 3.0;
         }

         Entity target = getTargetedEntity(client, maxReach);
         if (target != null) {
            currentTarget = target;
         }

         if (VoidCyanClient.attackIndicatorOnlyPlayers && !(currentTarget instanceof PlayerEntity)) {
            currentTarget = null;
         }

         if (currentTarget != null && VoidCyanClient.isBotOrFloatingText(currentTarget)) {
            currentTarget = null;
         }

         boolean visibleNow = false;
         if (currentTarget != null && client.player.canSee(currentTarget)) {
            visibleNow = true;
         }

         if (visibleNow) {
            if (currentTarget != lastTargetEntity) {
               lastTargetEntity = currentTarget;
            }

            lastTargetUpdateTime = currentTime;
         } else if (!VoidCyanClient.attackIndicatorAlwaysActive) {
            lastTargetEntity = null;
            return false;
         }

         if (lastTargetEntity != null) {
            if (currentTime - lastTargetUpdateTime > (long)(VoidCyanClient.attackIndicatorLiveTime * 1000.0F) || lastTargetEntity.isRemoved()) {
               lastTargetEntity = null;
               return false;
            }

            if (!client.player.canSee(lastTargetEntity)) {
               return false;
            }

            if (VoidCyanClient.attackIndicatorOnlyPlayers && !(lastTargetEntity instanceof PlayerEntity)) {
               lastTargetEntity = null;
               return false;
            }
         }

         return lastTargetEntity != null;
      } else {
         return false;
      }
   }

   public static void renderTarget(WorldRenderContext context) {
      if (updateOrKeepTarget()) {
         float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
         MatrixStack matrices = context.matrices();
         VertexConsumerProvider consumers = context.consumers();
         if (matrices != null && consumers != null) {
            if (VoidCyanClient.attackIndicatorStyle != null) {
               String var4 = VoidCyanClient.attackIndicatorStyle.toUpperCase();
               switch (var4) {
                  case "SOUL":
                     renderSoulsEsp(tickDelta, lastTargetEntity, matrices, consumers);
                     break;
                  case "SPIRAL":
                     drawSpiralsEsp(matrices, lastTargetEntity, tickDelta, consumers);
                     break;
                  case "TOPKA":
                     drawScanEsp(matrices, lastTargetEntity, tickDelta, consumers);
                     break;
                  case "PLUS":
                     drawPlusEsp(tickDelta, lastTargetEntity, matrices, consumers);
                     break;
                  default:
                     drawLegacy(tickDelta, lastTargetEntity, matrices, consumers);
               }
            }
         }
      }
   }

   private static Vec3d calculateEntityPositionRelativeToCamera(Camera camera, float tickDelta, Entity targetEntity) {
      double interpolatedX = MathHelper.lerp(tickDelta, targetEntity.lastRenderX, targetEntity.getX());
      double interpolatedY = MathHelper.lerp(tickDelta, targetEntity.lastRenderY, targetEntity.getY()) + targetEntity.getHeight() / 2.0F;
      double interpolatedZ = MathHelper.lerp(tickDelta, targetEntity.lastRenderZ, targetEntity.getZ());
      Vec3d entityPos = new Vec3d(interpolatedX, interpolatedY, interpolatedZ);
      return entityPos.subtract(camera.getCameraPos());
   }

   public static void drawLegacy(float tickDelta, Entity targetEntity, MatrixStack matrices, VertexConsumerProvider consumers) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (targetEntity != null && mc.world != null) {
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d entityPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
         float halfSize = VoidCyanClient.attackIndicatorLegacyScale / 50.0F / 2.0F;
         float alpha = VoidCyanClient.attackIndicatorLegacyAlpha / 100.0F;
         float time = (float)mc.world.getTime() + tickDelta;
         int rotationAngle = 72;
         float rotationOffset = time % rotationAngle / rotationAngle;
         Color color0 = getColor(getWaveInterpolation((float) (Math.PI / 4), rotationOffset));
         Color color1 = getColor(getWaveInterpolation((float) (Math.PI * 3.0 / 4.0), rotationOffset));
         Color color2 = getColor(getWaveInterpolation((float) (Math.PI * 5.0 / 4.0), rotationOffset));
         Color color3 = getColor(getWaveInterpolation((float) (Math.PI * 7.0 / 4.0), rotationOffset));
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - lastUpdateTime) / 1000.0F;
         if (deltaTime > 0.1F) {
            deltaTime = 0.05F;
         }

         lastUpdateTime = currentTime;
         rollAngle = (rollAngle + 90.0F * deltaTime * (VoidCyanClient.attackIndicatorLegacyRollSpeed / 100.0F)) % 360.0F;
         double dx = entityPos.x;
         double dy = entityPos.y;
         double dz = entityPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         float maxOffset = (float)Math.max(0.01, dist - 0.2);
         float offset = Math.min(targetEntity.getWidth() / 2.0F + 0.15F, maxOffset);
         double renderX = dx;
         double renderY = dy;
         double renderZ = dz;
         float scale = 1.0F;
         if (dist > 0.001) {
            renderX = dx - dx / dist * offset;
            renderY = dy - dy / dist * offset;
            renderZ = dz - dz / dist * offset;
            scale = (float)((dist - offset) / dist);
         }

         matrices.push();
         matrices.translate(renderX, renderY, renderZ);
         matrices.multiply(camera.getRotation());
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollAngle));
         matrices.scale(scale, scale, scale);
         Entry entry = matrices.peek();
         VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(getTargetRenderTexture()));
         drawQuad(entry, vc, -halfSize, -halfSize, halfSize, halfSize, color0, color1, color2, color3, alpha);
         matrices.pop();
      }
   }

   public static void drawPlusEsp(float tickDelta, Entity targetEntity, MatrixStack matrices, VertexConsumerProvider consumers) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (targetEntity != null && mc.world != null) {
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d entityPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
         float halfSize = VoidCyanClient.attackIndicatorLegacyScale / 50.0F / 2.0F;
         float alpha = VoidCyanClient.attackIndicatorLegacyAlpha / 100.0F;
         float time = (float)mc.world.getTime() + tickDelta;
         int rotationAngle = 72;
         float rotationOffset = time % rotationAngle / rotationAngle;
         Color color0 = getColor(getWaveInterpolation((float) (Math.PI / 4), rotationOffset));
         Color color1 = getColor(getWaveInterpolation((float) (Math.PI * 3.0 / 4.0), rotationOffset));
         Color color2 = getColor(getWaveInterpolation((float) (Math.PI * 5.0 / 4.0), rotationOffset));
         Color color3 = getColor(getWaveInterpolation((float) (Math.PI * 7.0 / 4.0), rotationOffset));
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - lastUpdateTime) / 1000.0F;
         if (deltaTime > 0.1F) {
            deltaTime = 0.05F;
         }

         lastUpdateTime = currentTime;
         if (VoidCyanClient.targetIndicatorSpinning) {
            rollAngle = (rollAngle + 90.0F * deltaTime * VoidCyanClient.targetIndicatorSpinSpeed * 2.0F) % 360.0F;
         }

         double dx = entityPos.x;
         double dy = entityPos.y;
         double dz = entityPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         float maxOffset = (float)Math.max(0.01, dist - 0.2);
         float offset = Math.min(targetEntity.getWidth() / 2.0F + 0.15F, maxOffset);
         double renderX = dx;
         double renderY = dy;
         double renderZ = dz;
         float scale = 1.0F;
         if (dist > 0.001) {
            renderX = dx - dx / dist * offset;
            renderY = dy - dy / dist * offset;
            renderZ = dz - dz / dist * offset;
            scale = (float)((dist - offset) / dist);
         }

         matrices.push();
         matrices.translate(renderX, renderY, renderZ);
         matrices.multiply(camera.getRotation());
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollAngle));
         matrices.scale(scale, scale, scale);
         Entry entry = matrices.peek();
         VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(WHITE_TEX));
         float thickness = halfSize * 0.2F;
         drawQuad(entry, vc, -halfSize, -thickness / 2.0F, halfSize, thickness / 2.0F, color0, color1, color2, color3, alpha);
         drawQuad(entry, vc, -thickness / 2.0F, -halfSize, thickness / 2.0F, halfSize, color0, color1, color2, color3, alpha);
         matrices.pop();
      }
   }

   public static void renderSoulsEsp(float tickDelta, Entity targetEntity, MatrixStack matrices, VertexConsumerProvider consumers) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Camera camera = mc.gameRenderer.getCamera();
      int espLength = VoidCyanClient.attackIndicatorSoulLength;
      float factor = VoidCyanClient.attackIndicatorSoulFactor;
      float shaking = VoidCyanClient.attackIndicatorSoulShaking;
      float layerSpacing = 2.0F;
      float amplitude = VoidCyanClient.attackIndicatorSoulAmplitude;
      float radius = VoidCyanClient.attackIndicatorSoulRadius / 100.0F;
      float startSize = VoidCyanClient.attackIndicatorSoulStartSize / 100.0F;
      float endSize = VoidCyanClient.attackIndicatorSoulEndSize / 100.0F;
      float scaleModifier = VoidCyanClient.attackIndicatorSoulScale / 100.0F;
      int subdivisions = VoidCyanClient.attackIndicatorSoulSubdivision;
      if (targetEntity != null) {
         Vec3d newPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
         float entityAge = targetEntity.age + tickDelta;
         matrices.push();
         matrices.translate(newPos.getX(), newPos.getY(), newPos.getZ());
         long timeMs = System.currentTimeMillis();
         float colorProgress = (float)(timeMs % 3000L) / 3000.0F;
         Color shaderColor = getColor(colorProgress);
         VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(getSoulTexture()));

         for (int j = 0; j < 3; j++) {
            for (int i = 0; i < espLength; i++) {
               for (int sub = 0; sub < subdivisions; sub++) {
                  float t = (float)sub / subdivisions;
                  float stepIndex = i + t;
                  double radians = Math.toRadians(((stepIndex / 1.5F + entityAge) * factor + j * 120) % (factor * 360.0F));
                  double sinQuad = Math.sin(Math.toRadians(entityAge * 2.5F + stepIndex * (j + 1)) * amplitude) / shaking;
                  float offset = stepIndex / espLength * layerSpacing;
                  float x = (float)(Math.cos(radians) * radius);
                  float z = (float)(Math.sin(radians) * radius);
                  float y = (float)sinQuad;
                  matrices.push();
                  matrices.translate(x, y, z);
                  matrices.multiply(camera.getRotation());
                  float scale = Math.max((endSize + offset * (startSize - endSize)) * scaleModifier, 0.15F * scaleModifier);
                  matrices.scale(scale, scale, scale);
                  Entry entry = matrices.peek();
                  drawQuad(entry, vc, -0.5F, -0.5F, 0.5F, 0.5F, shaderColor, shaderColor, shaderColor, shaderColor, 1.0F);
                  matrices.pop();
               }
            }
         }

         matrices.pop();
      }
   }

   public static void drawSpiralsEsp(MatrixStack stack, Entity target, float tickDelta, VertexConsumerProvider consumers) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Camera camera = mc.gameRenderer.getCamera();
      float radius = 0.75F;
      float heightStep = 0.004F;
      float heightOffset = 0.1F;
      float animationSpeed = 6.0F;
      float alphaDivider = 100.0F;
      Vec3d cameraPos = camera.getCameraPos();
      double x = MathHelper.lerp(tickDelta, target.lastRenderX, target.getX()) - cameraPos.x;
      double y = MathHelper.lerp(tickDelta, target.lastRenderY, target.getY()) - cameraPos.y;
      double z = MathHelper.lerp(tickDelta, target.lastRenderZ, target.getZ()) - cameraPos.z;
      double height = target.getHeight();
      ArrayList<Vec3d>[] spirals = new ArrayList[]{new ArrayList(), new ArrayList(), new ArrayList()};
      generateSpiralVectors(spirals, radius, height, heightStep);
      stack.push();
      stack.translate(x, y - heightOffset, z);
      Entry entry = stack.peek();
      VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(WHITE_TEX));
      renderSpiral(entry, vc, spirals[0], 0.0F, heightOffset, animationSpeed, alphaDivider);
      renderSpiral(entry, vc, spirals[1], 0.33F, heightOffset, animationSpeed, alphaDivider);
      renderSpiral(entry, vc, spirals[2], 0.66F, heightOffset, animationSpeed, alphaDivider);
      stack.pop();
   }

   private static void generateSpiralVectors(ArrayList<Vec3d>[] spirals, float radius, double initialHeight, float heightStep) {
      for (int i = 0; i <= 360; i++) {
         double height = initialHeight - i * heightStep;

         for (int spiralIndex = 0; spiralIndex < 3; spiralIndex++) {
            double angle = Math.toRadians(i + spiralIndex * 120);
            double u = Math.cos(angle);
            double v = Math.sin(angle);
            spirals[spiralIndex].add(new Vec3d((float)(u * radius), (float)height, (float)(v * radius)));
         }
      }
   }

   private static void renderSpiral(
      Entry entry, VertexConsumer vc, ArrayList<Vec3d> vecs, float progressOffset, float heightOffset, float animationSpeed, float alphaDivider
   ) {
      int size = vecs.size();
      Color startColor = getColor(0.0F);
      Color endColor = getColor(1.0F);
      int mixedRed = (startColor.getRed() + endColor.getRed()) / 2;
      int mixedGreen = (startColor.getGreen() + endColor.getGreen()) / 2;
      int mixedBlue = (startColor.getBlue() + endColor.getBlue()) / 2;
      Color mixedColor = injectAlpha(new Color(mixedRed, mixedGreen, mixedBlue), 255);
      float smoothingRange = 0.1F;
      float smoothingEnd = 1.0F - smoothingRange;

      for (int j = 0; j < size - 1; j++) {
         float alpha = 1.0F - (j + (float)(System.currentTimeMillis() - initTime) / animationSpeed) % 360.0F / alphaDivider;
         float progress = ((float)j / size + progressOffset) % 1.0F;
         Color currentColor;
         if (j == 0 || j == size - 1) {
            currentColor = mixedColor;
         } else if (progress <= smoothingRange) {
            float t = progress / smoothingRange;
            Color targetColor = injectAlpha(getColor(smoothingRange), 255);
            int r = (int)(mixedColor.getRed() + t * (targetColor.getRed() - mixedColor.getRed()));
            int g = (int)(mixedColor.getGreen() + t * (targetColor.getGreen() - mixedColor.getGreen()));
            int b = (int)(mixedColor.getBlue() + t * (targetColor.getBlue() - mixedColor.getBlue()));
            currentColor = injectAlpha(new Color(r, g, b), 255);
         } else if (progress >= smoothingEnd) {
            float t = (progress - smoothingEnd) / (1.0F - smoothingEnd);
            Color targetColor = injectAlpha(getColor(smoothingEnd), 255);
            int r = (int)(targetColor.getRed() + t * (mixedColor.getRed() - targetColor.getRed()));
            int g = (int)(targetColor.getGreen() + t * (mixedColor.getGreen() - targetColor.getGreen()));
            int b = (int)(targetColor.getBlue() + t * (mixedColor.getBlue() - targetColor.getBlue()));
            currentColor = injectAlpha(new Color(r, g, b), 255);
         } else {
            currentColor = injectAlpha(getColor(progress), 255);
         }

         Vec3d current = vecs.get(j);
         Vec3d next = vecs.get(j + 1);
         Color finalColor = injectAlpha(currentColor, (int)(alpha * 255.0F));
         float rF = finalColor.getRed() / 255.0F;
         float gF = finalColor.getGreen() / 255.0F;
         float bF = finalColor.getBlue() / 255.0F;
         float aF = finalColor.getAlpha() / 255.0F;
         vc.vertex(entry, (float)current.x, (float)current.y, (float)current.z)
            .color(rF, gF, bF, aF)
            .texture(0.0F, 1.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)next.x, (float)next.y, (float)next.z)
            .color(rF, gF, bF, aF)
            .texture(1.0F, 1.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)next.x, (float)next.y + heightOffset, (float)next.z)
            .color(rF, gF, bF, aF)
            .texture(1.0F, 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)current.x, (float)current.y + heightOffset, (float)current.z)
            .color(rF, gF, bF, aF)
            .texture(0.0F, 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
      }
   }

   public static void drawScanEsp(MatrixStack stack, Entity target, float tickDelta, VertexConsumerProvider consumers) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Camera camera = mc.gameRenderer.getCamera();
      float animationSpeed = VoidCyanClient.attackIndicatorTopkaSpeed / 100.0F;
      float minHeightOffset = 0.01F;
      float maxHeightOffset = 0.5F;
      float radius = VoidCyanClient.attackIndicatorTopkaRadius / 100.0F;
      Vec3d cameraPos = camera.getCameraPos();
      double x = MathHelper.lerp(tickDelta, target.lastRenderX, target.getX()) - cameraPos.x;
      double y = MathHelper.lerp(tickDelta, target.lastRenderY, target.getY()) - cameraPos.y;
      double z = MathHelper.lerp(tickDelta, target.lastRenderZ, target.getZ()) - cameraPos.z;
      double height = target.getHeight();
      float time = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
      float t = time * animationSpeed % 1.0F;
      float triangleWave = t < 0.5F ? 2.0F * t : 2.0F * (1.0F - t);
      float heightStep = (float)(triangleWave * height);
      float direction = t < 0.5F ? 1.0F : -1.0F;
      boolean movingUp = direction > 0.0F;
      float speed = 1.0F - Math.abs(2.0F * triangleWave - 1.0F);
      float heightOffset = minHeightOffset + (maxHeightOffset - minHeightOffset) * speed;
      heightOffset = Math.max(heightOffset, minHeightOffset);
      ArrayList<Vec3d> ring = generateRingVectors(radius, heightStep);
      stack.push();
      stack.translate(x, y, z);
      Entry entry = stack.peek();
      VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(WHITE_TEX));
      renderRing(entry, vc, ring, heightOffset, movingUp);
      stack.pop();
   }

   private static ArrayList<Vec3d> generateRingVectors(float radius, double heightStep) {
      ArrayList<Vec3d> ring = new ArrayList<>();
      int numPoints = 360;

      for (int i = 0; i <= numPoints; i++) {
         double angle = Math.toRadians(i);
         double u = Math.cos(angle);
         double v = Math.sin(angle);
         ring.add(new Vec3d((float)(u * radius), (float)heightStep, (float)(v * radius)));
      }

      return ring;
   }

   private static void renderRing(Entry entry, VertexConsumer vc, ArrayList<Vec3d> ring, float heightOffset, boolean movingUp) {
      int size = ring.size();
      int colorCount = 4;
      float angleStep = 360.0F / colorCount;
      List<Float> angleStops = new ArrayList<>();

      for (int i = 0; i < colorCount; i++) {
         angleStops.add(i * angleStep);
      }

      for (int i = 0; i < size - 1; i++) {
         int nextIndex = i + 1;
         float angleDeg = 360.0F * i / size;
         int leftIndex = 0;

         for (int j = 0; j < angleStops.size(); leftIndex = j++) {
            float stopAngle = angleStops.get(j);
            if (!(angleDeg >= stopAngle)) {
               break;
            }
         }

         int rightIndex = (leftIndex + 1) % colorCount;
         float leftAngle = angleStops.get(leftIndex);
         float rightAngle = angleStops.get(rightIndex);
         float segmentSpan = rightAngle > leftAngle ? rightAngle - leftAngle : 360.0F - leftAngle + rightAngle;
         float t = (angleDeg - leftAngle + 360.0F) % 360.0F / segmentSpan;
         Color leftColor = getColor((float)leftIndex / (colorCount - 1));
         Color rightColor = getColor((float)rightIndex / (colorCount - 1));
         int r = (int)(leftColor.getRed() + t * (rightColor.getRed() - leftColor.getRed()));
         int g = (int)(leftColor.getGreen() + t * (rightColor.getGreen() - leftColor.getGreen()));
         int b = (int)(leftColor.getBlue() + t * (rightColor.getBlue() - leftColor.getBlue()));
         int a = 255;
         Color currentColor = new Color(r, g, b, a);
         float rF = currentColor.getRed() / 255.0F;
         float gF = currentColor.getGreen() / 255.0F;
         float bF = currentColor.getBlue() / 255.0F;
         Vec3d current = ring.get(i);
         Vec3d next = ring.get(nextIndex);
         float sinT = (float)Math.sin(Math.PI / 2);
         int lowerAlpha = movingUp ? (int)(a * (1.0F - sinT)) : a;
         int upperAlpha = movingUp ? a : (int)(a * (1.0F - sinT));
         float lowerAlphaF = lowerAlpha / 255.0F;
         float upperAlphaF = upperAlpha / 255.0F;
         vc.vertex(entry, (float)current.x, (float)current.y, (float)current.z)
            .color(rF, gF, bF, lowerAlphaF)
            .texture(0.0F, 1.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)next.x, (float)next.y, (float)next.z)
            .color(rF, gF, bF, lowerAlphaF)
            .texture(1.0F, 1.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)next.x, (float)next.y + heightOffset, (float)next.z)
            .color(rF, gF, bF, upperAlphaF)
            .texture(1.0F, 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
         vc.vertex(entry, (float)current.x, (float)current.y + heightOffset, (float)current.z)
            .color(rF, gF, bF, upperAlphaF)
            .texture(0.0F, 0.0F)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(0.0F, 1.0F, 0.0F);
      }
   }

   private static void drawQuad(Entry entry, VertexConsumer vc, float x1, float y1, float x2, float y2, Color c0, Color c1, Color c2, Color c3, float alpha) {
      vc.vertex(entry, x1, y1, 0.0F)
         .color(c0.getRed() / 255.0F, c0.getGreen() / 255.0F, c0.getBlue() / 255.0F, alpha)
         .texture(0.0F, 1.0F)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y1, 0.0F)
         .color(c1.getRed() / 255.0F, c1.getGreen() / 255.0F, c1.getBlue() / 255.0F, alpha)
         .texture(1.0F, 1.0F)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y2, 0.0F)
         .color(c2.getRed() / 255.0F, c2.getGreen() / 255.0F, c2.getBlue() / 255.0F, alpha)
         .texture(1.0F, 0.0F)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x1, y2, 0.0F)
         .color(c3.getRed() / 255.0F, c3.getGreen() / 255.0F, c3.getBlue() / 255.0F, alpha)
         .texture(0.0F, 0.0F)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(0.0F, 1.0F, 0.0F);
   }

   static float getWaveInterpolation(double angle, float rotationOffset) {
      float positionOnCircle = (float)(angle / (Math.PI * 2) + rotationOffset) % 1.0F;
      float distanceToGradientPoint = MathHelper.abs(positionOnCircle - 0.5F);
      return MathHelper.clamp(1.0F - distanceToGradientPoint * 2.0F, 0.0F, 1.0F);
   }

   private static Entity getTargetedEntity(MinecraftClient client, double maxDistance) {
      Entity cameraEntity = client.getCameraEntity();
      if (cameraEntity != null && client.world != null) {
         Vec3d start = cameraEntity.getCameraPosVec(1.0F);
         Vec3d rot = cameraEntity.getRotationVec(1.0F);
         Vec3d end = start.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
         Box box = cameraEntity.getBoundingBox().stretch(rot.multiply(maxDistance)).expand(1.0, 1.0, 1.0);
         Entity closestEntity = null;
         double closestDist = maxDistance * maxDistance;

         for (Entity entity : client.world.getOtherEntities(cameraEntity, box, e -> {
            if (e.isSpectator()) {
               return false;
            } else if (e instanceof EndCrystalEntity || e instanceof TntEntity) {
               return true;
            } else {
               return VoidCyanClient.attackIndicatorAllowNonLiving ? true : e instanceof LivingEntity;
            }
         })) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            Optional<Vec3d> optional = entityBox.raycast(start, end);
            if (optional.isPresent()) {
               double dist = start.squaredDistanceTo(optional.get());
               if (dist < closestDist) {
                  closestEntity = entity;
                  closestDist = dist;
               }
            }
         }

         return closestEntity;
      } else {
         return null;
      }
   }

   private static class OklabUtils {
      public static float[] srgbToOklab(Color color) {
         float r = srgbChannelToLinear(color.getRed() / 255.0F);
         float g = srgbChannelToLinear(color.getGreen() / 255.0F);
         float b = srgbChannelToLinear(color.getBlue() / 255.0F);
         float l = 0.41222146F * r + 0.53633255F * g + 0.051445995F * b;
         float m = 0.2119035F * r + 0.6806995F * g + 0.10739696F * b;
         float s = 0.08830246F * r + 0.28171885F * g + 0.6299787F * b;
         float l_ = (float)Math.cbrt(l);
         float m_ = (float)Math.cbrt(m);
         float s_ = (float)Math.cbrt(s);
         float L = 0.21045426F * l_ + 0.7936178F * m_ - 0.004072047F * s_;
         float a = 1.9779985F * l_ - 2.4285922F * m_ + 0.4505937F * s_;
         float b_ = 0.025904037F * l_ + 0.78277177F * m_ - 0.80867577F * s_;
         return new float[]{L, a, b_};
      }

      public static Color oklabToSrgb(float[] oklab) {
         float l_ = oklab[0] + 0.39633778F * oklab[1] + 0.21580376F * oklab[2];
         float m_ = oklab[0] - 0.105561346F * oklab[1] - 0.06385417F * oklab[2];
         float s_ = oklab[0] - 0.08948418F * oklab[1] - 1.2914855F * oklab[2];
         float l = l_ * l_ * l_;
         float m = m_ * m_ * m_;
         float s = s_ * s_ * s_;
         float r = 4.0767417F * l - 3.3077116F * m + 0.23096994F * s;
         float g = -1.268438F * l + 2.6097574F * m - 0.34131938F * s;
         float b = -0.0041960864F * l - 0.7034186F * m + 1.7076147F * s;
         return new Color(clamp(linearToSrgbChannel(r)), clamp(linearToSrgbChannel(g)), clamp(linearToSrgbChannel(b)));
      }

      private static float srgbChannelToLinear(float c) {
         return c <= 0.04045F ? c / 12.92F : (float)Math.pow((c + 0.055F) / 1.055F, 2.4F);
      }

      private static float linearToSrgbChannel(float c) {
         return c <= 0.0031308F ? c * 12.92F : 1.055F * (float)Math.pow(c, 0.4166666666666667) - 0.055F;
      }

      private static int clamp(float v) {
         return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
      }

      public static Color interpolate(Color a, Color b, float t) {
         float[] labA = srgbToOklab(a);
         float[] labB = srgbToOklab(b);
         float[] labResult = new float[3];

         for (int i = 0; i < 3; i++) {
            labResult[i] = labA[i] + (labB[i] - labA[i]) * t;
         }

         return oklabToSrgb(labResult);
      }
   }
}
