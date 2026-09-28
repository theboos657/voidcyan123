package com.voidcyan.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Renders active OBJ crit-effect models in world space during AFTER_ENTITIES.
 * Models are drawn as flat-colored triangles (theme color, fade-out over
 * duration). Fill uses debugQuads; a translucent ring of particles can be
 * layered via the same effect JSON.
 */
public final class CritModelRenderer {

   private CritModelRenderer() {
   }

   public static void render(WorldRenderContext context) {
      if (!CritEffectsManager.enabled) return;

      // NOTE: billboard sparks are rendered by VoidCyanClient's AFTER_ENTITIES
      // hook. This used to call CritBillboardFX.render(context) as well, so
      // every spark was emitted TWICE per frame — one of the causes of entity
      // flicker/disappearing players.

      List<CritEffectsManager.ModelInstance> models = CritEffectsManager.getActiveModels();
      if (models.isEmpty()) return;
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null) return;

      MatrixStack matrices = context.matrices();
      VertexConsumerProvider consumers = context.consumers();
      if (matrices == null || consumers == null) return;

      int rgb = VoidCyanClient.getPrimaryColor() & 0x00FFFFFF;

      // AFTER_ENTITIES matrices are camera-relative: world-space model origins
      // must be shifted by the camera position or models render far off-screen.
      Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();

      for (CritEffectsManager.ModelInstance m : models) {
         float fade = m.fade();
         // ease-out fade: opaque at start, transparent at end
         int alpha = (int) ((1.0F - fade) * 200.0F);
         if (alpha <= 4) continue;

         float spin = m.spinAngle();
         float scale = m.effect.modelScale;

         VertexConsumer buffer = consumers.getBuffer(RenderLayers.debugQuads());
         matrices.push();
         matrices.translate(m.x - camPos.x, m.y - camPos.y, m.z - camPos.z);
         if (spin != 0.0F) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
         }
         matrices.scale(scale, scale, scale);
         // OBJ Y-up matches Minecraft Y-up; no axis swap needed for Blender Z-up
         // exports only if the user exports with "Y up" — provide pivot shift:
         matrices.translate(0.0F, -1.0F, 0.0F); // raise pivot: model origin sits at hit feet

         float[] tris = m.mesh.triangles;
         // debugQuads is a QUADS draw mode: OBJ triangles (3 verts) MUST be
         // padded with a degenerate 4th vertex or the whole vertex stream goes
         // misaligned — that corrupted entity rendering (players vanishing).
         for (int i = 0; i + 8 < tris.length; i += 9) {
            for (int v = 0; v < 3; v++) {
               float vx = tris[i + v * 3];
               float vy = tris[i + v * 3 + 1];
               float vz = tris[i + v * 3 + 2];
               buffer.vertex(matrices.peek(), vx, vy, vz);
               buffer.color(alpha << 24 | rgb);
            }
            // degenerate 4th corner = zero-area quad tail, keeps alignment
            buffer.vertex(matrices.peek(), tris[i], tris[i + 1], tris[i + 2]);
            buffer.color(alpha << 24 | rgb);
         }
         matrices.pop();
      }
   }
}
