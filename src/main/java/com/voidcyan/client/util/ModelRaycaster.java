package com.voidcyan.client.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Raycasts mouse clicks into the entity model as displayed in the texture editor's 3D viewport.
 *
 * The transform chain here mirrors exactly what the vanilla GUI entity pipeline does
 * (SpecialGuiElementRenderer -> EntityGuiElementRenderer -> EntityRenderManager -> LivingEntityRenderer):
 *
 *   screen = T(elementCenter) . S(s, s, -s) . T(addEntityTranslation) . R(orbit)
 *            . Ry(180 - bodyYaw) . S(-1,-1,1) . T(0, -1.501, 0) . modelPos
 *
 * Critical details (verified against decompiled 1.21.11 bytecode):
 *  - MatrixStack ops post-multiply, so translate(0,-1.501,0) lands INSIDE the mirror:
 *    the effective vertex transform is (-x, -y + 1.501, z).
 *  - The addEntity translation (pan offsets + height/2) is added AFTER the orbit rotation,
 *    then the GUI scale (s, s, -s) applies the final Z flip.
 *  - Because of that Z flip, faces closer to the viewer have LARGER view Z, so picking
 *    must choose the maximum depth, not the minimum.
 */
public class ModelRaycaster {

   public static class RayHit {
      public final int pixelX;
      public final int pixelY;
      public final float depth;
      public final String partName;
      public final Vector3f[] screenQuad;

      public RayHit(int pixelX, int pixelY, float depth, String partName, Vector3f[] screenQuad) {
         this.pixelX = pixelX;
         this.pixelY = pixelY;
         this.depth = depth;
         this.partName = partName;
         this.screenQuad = screenQuad;
      }
   }

   public static class QuadData {
      public final Vector3f[] screenVerts;
      public final float[] u;
      public final float[] v;
      public final String partName;
      public final boolean facingCamera;

      public QuadData(Vector3f[] screenVerts, float[] u, float[] v, String partName, boolean facingCamera) {
         this.screenVerts = screenVerts;
         this.u = u;
         this.v = v;
         this.partName = partName;
         this.facingCamera = facingCamera;
      }
   }

   /**
    * Collects all projected screen-space quads for an entity model.
    */
   @SuppressWarnings("rawtypes")
   public static List<QuadData> getProjectedQuads(
      LivingEntity entity,
      float centerX,
      float centerY,
      float scale,
      float cameraYaw,
      float cameraPitch
   ) {
      List<QuadData> quads = new ArrayList<>();
      if (entity == null) return quads;

      MinecraftClient client = MinecraftClient.getInstance();
      EntityRenderer renderer = client.getEntityRenderDispatcher().getRenderer(entity);
      if (!(renderer instanceof LivingEntityRenderer lr)) return quads;

      EntityModel model = lr.getModel();
      if (model == null) return quads;

      ModelPart root = model.getRootPart();
      if (root == null) return quads;

      // Orbit rotation — must match the screen's quaternion exactly:
      // new Quaternionf().rotateZ(PI).rotateY(yaw).rotateX(pitch)  (JOML local-space chaining)
      Quaternionf orbitRot = new Quaternionf()
         .rotateZ((float) Math.PI)
         .rotateY((float) Math.toRadians(cameraYaw))
         .rotateX((float) Math.toRadians(cameraPitch));

      // LivingEntityRenderer.setupTransforms rotates by (180 - bodyYaw).
      // The editor's render state sets bodyYaw = 180, so this is a no-op — kept general.
      float bodyYawDegrees = 180.0f - 180.0f; // = 0
      float yawRad = (float) Math.toRadians(bodyYawDegrees);
      float cosY = (float) Math.cos(yawRad);
      float sinY = (float) Math.sin(yawRad);

      // The screen passes translation = (panX/scale, height/2 + panY/scale, 0) to addEntity.
      // centerX/centerY already contain the pan + element-center terms, so only the
      // height/2 part survives as an extra additive term after the scale.
      float halfHeight = entity.getHeight() / 2.0f;

      projectModel(root, halfHeight, centerX, centerY, scale, orbitRot, cosY, sinY, quads);

      return quads;
   }

   /**
    * Builds a private armor LAYER model — the exact biped vanilla renders for the
    * given equipment slot on armor stands (EntityModelLayers.ARMOR_STAND_EQUIPMENT).
    * Head/chest/feet use the outer humanoid model, legs the inner humanoid_leggings
    * model: each has its own vanilla dilation and the humanoid/humanoid_leggings
    * 64x32 UV layout, NOT the armor stand's base skin UVs. Returns null when the
    * layers are unavailable (callers fall back to the base-model raycast).
    */
   public static BipedEntityModel<?> createArmorLayerModel(EquipmentSlot slot) {
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.getLoadedEntityModels() == null) return null;
         EntityModelLayer layer = EntityModelLayers.ARMOR_STAND_EQUIPMENT.getModelData(slot);
         ModelPart root = client.getLoadedEntityModels().getModelPart(layer);
         return new ArmorStandArmorEntityModel(root);
      } catch (Exception e) {
         return null;
      }
   }

   /**
    * Projects a custom model (e.g. an armor LAYER model) through the exact same
    * GUI entity transform chain as {@link #getProjectedQuads}. The model is first
    * posed with vanilla's biped posing driven by the entity's render state — the
    * same thing ArmorFeatureRenderer does to equipment models at render time.
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public static List<QuadData> getProjectedQuadsForModel(
      LivingEntity entity,
      EntityRenderState poseSource,
      BipedEntityModel model,
      float centerX,
      float centerY,
      float scale,
      float cameraYaw,
      float cameraPitch
   ) {
      List<QuadData> quads = new ArrayList<>();
      if (entity == null || model == null) return quads;

      ModelPart root = model.getRootPart();
      if (root == null) return quads;

      // Pose the layer model exactly like vanilla poses armor: biped setAngles on
      // the entity's state. For a fresh standing armor stand this is the neutral
      // biped pose, matching what is displayed in the viewport.
      if (poseSource instanceof BipedEntityRenderState bipedState) {
         model.setAngles(bipedState);
      }

      Quaternionf orbitRot = new Quaternionf()
         .rotateZ((float) Math.PI)
         .rotateY((float) Math.toRadians(cameraYaw))
         .rotateX((float) Math.toRadians(cameraPitch));

      float yawRad = 0.0f;
      float cosY = (float) Math.cos(yawRad);
      float sinY = (float) Math.sin(yawRad);
      float halfHeight = entity.getHeight() / 2.0f;

      projectModel(root, halfHeight, centerX, centerY, scale, orbitRot, cosY, sinY, quads);

      return quads;
   }

   /**
    * Raycast overload for armor editing: picks against a dedicated armor LAYER
    * model instead of the entity's base model, so click->UV mapping uses the
    * humanoid/humanoid_leggings texture layout of the texture being edited.
    */
   public static RayHit raycastWithModel(
      LivingEntity entity,
      EntityRenderState poseSource,
      BipedEntityModel model,
      float mouseX,
      float mouseY,
      float centerX,
      float centerY,
      float scale,
      float cameraYaw,
      float cameraPitch,
      int canvasWidth,
      int canvasHeight
   ) {
      List<QuadData> quads = getProjectedQuadsForModel(entity, poseSource, model, centerX, centerY, scale, cameraYaw, cameraPitch);
      return pickBestHit(quads, mouseX, mouseY, canvasWidth, canvasHeight);
   }

   private static void projectModel(
      ModelPart root,
      float halfHeight,
      float centerX,
      float centerY,
      float scale,
      Quaternionf orbitRot,
      float cosY,
      float sinY,
      List<QuadData> quads
   ) {
      MatrixStack matrices = new MatrixStack();
      root.forEachCuboid(matrices, (entry, partName, cuboidIndex, cuboid) -> {
         Matrix4f posMatrix = entry.getPositionMatrix();
         for (ModelPart.Quad quad : cuboid.sides) {
            ModelPart.Vertex[] qv = quad.vertices();
            if (qv == null || qv.length != 4) continue;

            Vector3f[] screenVerts = new Vector3f[4];
            float[] us = new float[4];
            float[] vs = new float[4];

            for (int i = 0; i < 4; i++) {
               Vector4f local = new Vector4f(qv[i].x(), qv[i].y(), qv[i].z(), 1.0f);
               local.mul(posMatrix);

               // --- LivingEntityRenderer chain ---
               // scale(-1,-1,1) then translate(0,-1.501,0), post-multiplied:
               // v' = (-x, -(y - 1.501), z) = (-x, -y + 1.501, z)
               float vx = -local.x();
               float vy = -local.y() + 1.501f;
               float vz = local.z();

               // setupTransforms: rotateY(180 - bodyYaw) — identity for our pose (0 deg)
               float tx = vx * cosY + vz * sinY;
               float tz = -vx * sinY + vz * cosY;

               // Orbit rotation (addEntity's rotation quaternion)
               Vector3f p = new Vector3f(tx, vy, tz);
               orbitRot.transform(p);

               // addEntity translation, added AFTER the orbit rotation:
               // (panX/scale, height/2 + panY/scale, 0). The pan terms are already
               // folded into centerX/centerY; only height/2 remains.
               // Then the GUI scale (s, s, -s) — note the Z flip happens HERE.
               float ex = p.x() * scale;
               float ey = (p.y() + halfHeight) * scale;
               float ez = -p.z() * scale;

               screenVerts[i] = new Vector3f(centerX + ex, centerY + ey, ez);
               us[i] = qv[i].u();
               vs[i] = qv[i].v();
            }

            // Normal through the same chain: mirror -> yaw -> orbit -> Z flip.
            // (Mirror is its own inverse-transpose, so it applies directly.)
            Vector4f normLocal = new Vector4f(quad.direction().x(), quad.direction().y(), quad.direction().z(), 0.0f);
            normLocal.mul(posMatrix);
            float nx = -normLocal.x();
            float ny = -normLocal.y();
            float nz = normLocal.z();
            float ntx = nx * cosY + nz * sinY;
            float ntz = -nx * sinY + nz * cosY;
            Vector3f n = new Vector3f(ntx, ny, ntz);
            orbitRot.transform(n);

            // After the final Z flip, a camera-facing normal has positive flipped Z.
            // At neutral orbit this reduces to raw-model nz < 0 (the empirically correct test).
            boolean facingCamera = -n.z() > -0.05f;

            quads.add(new QuadData(screenVerts, us, vs, partName, facingCamera));
         }
      });
   }

   /**
    * Casts a ray from (mouseX, mouseY) into the model and returns the hit pixel and quad, or null.
    */
   public static RayHit raycast(
      LivingEntity entity,
      float mouseX,
      float mouseY,
      float centerX,
      float centerY,
      float scale,
      float cameraYaw,
      float cameraPitch,
      int canvasWidth,
      int canvasHeight
   ) {
      List<QuadData> quads = getProjectedQuads(entity, centerX, centerY, scale, cameraYaw, cameraPitch);
      return pickBestHit(quads, mouseX, mouseY, canvasWidth, canvasHeight);
   }

   private static RayHit pickBestHit(List<QuadData> quads, float mouseX, float mouseY, int canvasWidth, int canvasHeight) {
      RayHit bestHit = null;
      float bestDepth = Float.NEGATIVE_INFINITY;

      for (QuadData q : quads) {
         if (!q.facingCamera) continue;

         Vector3f p0 = q.screenVerts[0];
         Vector3f p1 = q.screenVerts[1];
         Vector3f p2 = q.screenVerts[2];
         Vector3f p3 = q.screenVerts[3];

         // Test Triangle 1: (0, 1, 2)
         float[] hit1 = testTriangle(mouseX, mouseY, p0, p1, p2, q.u[0], q.u[1], q.u[2], q.v[0], q.v[1], q.v[2]);
         if (hit1 != null && hit1[2] > bestDepth) {
            bestDepth = hit1[2];
            int px = Math.clamp((int) Math.floor(hit1[0] * canvasWidth), 0, canvasWidth - 1);
            int py = Math.clamp((int) Math.floor(hit1[1] * canvasHeight), 0, canvasHeight - 1);
            bestHit = new RayHit(px, py, bestDepth, q.partName, q.screenVerts);
         }

         // Test Triangle 2: (0, 2, 3)
         float[] hit2 = testTriangle(mouseX, mouseY, p0, p2, p3, q.u[0], q.u[2], q.u[3], q.v[0], q.v[2], q.v[3]);
         if (hit2 != null && hit2[2] > bestDepth) {
            bestDepth = hit2[2];
            int px = Math.clamp((int) Math.floor(hit2[0] * canvasWidth), 0, canvasWidth - 1);
            int py = Math.clamp((int) Math.floor(hit2[1] * canvasHeight), 0, canvasHeight - 1);
            bestHit = new RayHit(px, py, bestDepth, q.partName, q.screenVerts);
         }
      }

      return bestHit;
   }

   private static float[] testTriangle(
      float mx, float my,
      Vector3f p0, Vector3f p1, Vector3f p2,
      float u0, float u1, float u2,
      float v0, float v1, float v2
   ) {
      float dX = mx - p2.x();
      float dY = my - p2.y();
      float dX21 = p1.x() - p2.x();
      float dY12 = p2.y() - p1.y();
      float d = dY12 * (p0.x() - p2.x()) + dX21 * (p0.y() - p2.y());

      if (Math.abs(d) < 1e-6f) return null;

      float s = (dY12 * dX + dX21 * dY) / d;
      float t = ((p2.y() - p0.y()) * dX + (p0.x() - p2.x()) * dY) / d;
      float w = 1.0f - s - t;

      if (s >= -0.01f && t >= -0.01f && w >= -0.01f) {
         float u = s * u0 + t * u1 + w * u2;
         float v = s * v0 + t * v1 + w * v2;
         float depth = s * p0.z() + t * p1.z() + w * p2.z();
         return new float[]{u, v, depth};
      }
      return null;
   }
}
