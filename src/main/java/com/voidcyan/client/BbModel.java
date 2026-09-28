package com.voidcyan.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Blockbench .bbmodel loader (generic-model format).
 *
 * Blockbench stores cubes in 16 units = 1 block with Y pointing DOWN, so this
 * loader converts to world space: divide by 16 and flip Y. Per-cube rotations
 * ("rotation": [x, y, z] degrees with an optional "origin") and nested group
 * transforms are baked into the vertices at load time. The result is a flat
 * triangle list, same as ObjModel.
 */
public final class BbModel {
   /** Flat triangle list: 9 floats per triangle (x,y,z per corner), Y-up world units. */
   public final float[] triangles;

   private BbModel(float[] triangles) {
      this.triangles = triangles;
   }

   private static final java.util.Map<String, Cached> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

   private record Cached(long mtime, BbModel model) {
   }

   public static BbModel get(File file) {
      if (file == null || !file.isFile()) return null;
      long mtime;
      try {
         mtime = file.lastModified();
      } catch (Exception e) {
         return null;
      }
      Cached c = CACHE.get(file.getAbsolutePath());
      if (c != null && c.mtime() == mtime) return c.model();
      BbModel parsed = parse(file);
      if (parsed != null) {
         CACHE.put(file.getAbsolutePath(), new Cached(mtime, parsed));
      }
      return parsed;
   }

   public static void clearCache() {
      CACHE.clear();
   }

   private static BbModel parse(File file) {
      List<Float> out = new ArrayList<>();
      try {
         String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
         JsonObject root = JsonParser.parseString(text).getAsJsonObject();
         JsonElement modelsEl = root.get("elements");
         if (modelsEl == null || !modelsEl.isJsonArray()) return null;

         for (JsonElement el : modelsEl.getAsJsonArray()) {
            if (!el.isJsonObject()) continue;
            JsonObject cube = el.getAsJsonObject();
            JsonArray from = cube.getAsJsonArray("from");
            JsonArray to = cube.getAsJsonArray("to");
            if (from == null || to == null || from.size() < 3 || to.size() < 3) continue;

            float x0 = from.get(0).getAsFloat() / 16.0F;
            float y0 = from.get(1).getAsFloat() / 16.0F;
            float z0 = from.get(2).getAsFloat() / 16.0F;
            float x1 = to.get(0).getAsFloat() / 16.0F;
            float y1 = to.get(1).getAsFloat() / 16.0F;
            float z1 = to.get(2).getAsFloat() / 16.0F;

            // World space: Y flips (Blockbench is y-down).
            float ax = x0, ay = -y0, az = z0; // (x0,y0,z0)
            float bx = x1, by = -y0, bz = z0; // (x1,y0,z0)
            float cx = x1, cy = -y1, cz = z0; // (x1,y1,z0)
            float dx = x0, dy = -y1, dz = z0; // (x0,y1,z0)
            float ex = x0, ey = -y0, ez = z1; // (x0,y0,z1)
            float fx = x1, fy = -y0, fz = z1; // (x1,y0,z1)
            float gx = x1, gy = -y1, gz = z1; // (x1,y1,z1)
            float hx = x0, hy = -y1, hz = z1; // (x0,y1,z1)

            // Per-cube rotation (Blockbench stores degrees, pivot "origin" in model units).
            Matrix4f transform = null;
            JsonObject rot = cube.has("rotation") && cube.get("rotation").isJsonObject()
               ? cube.getAsJsonObject("rotation") : null;
            JsonArray groupChain = null;
            if (rot != null && rot.has("angle") == false) {
               // full [x, y, z] rotation object
            }
            if (rot != null) {
               float rx = rot.has("x") ? rot.get("x").getAsFloat() : 0.0F;
               float ry = rot.has("y") ? rot.get("y").getAsFloat() : 0.0F;
               float rz = rot.has("z") ? rot.get("z").getAsFloat() : 0.0F;
               float[] origin = new float[]{0.0F, 0.0F, 0.0F};
               if (rot.has("origin") && rot.get("origin").isJsonArray()) {
                  JsonArray o = rot.getAsJsonArray("origin");
                  origin[0] = o.get(0).getAsFloat() / 16.0F;
                  origin[1] = -o.get(1).getAsFloat() / 16.0F;
                  origin[2] = o.get(2).getAsFloat() / 16.0F;
               }
               transform = new Matrix4f();
               transform.translate((float) origin[0], (float) origin[1], (float) origin[2]);
               // Blockbench applies Z, then Y, then X.
               transform.rotate((float) Math.toRadians(rz), 0.0F, 0.0F, 1.0F);
               transform.rotate((float) Math.toRadians(ry), 0.0F, 1.0F, 0.0F);
               transform.rotate((float) Math.toRadians(rx), 1.0F, 0.0F, 0.0F);
               transform.translate((float) -origin[0], (float) -origin[1], (float) -origin[2]);
            }

            float[][] corners = new float[][]{
               {ax, ay, az}, {bx, by, bz}, {cx, cy, cz}, {dx, dy, dz},
               {ex, ey, ez}, {fx, fy, fz}, {gx, gy, gz}, {hx, hy, hz}
            };
            if (transform != null) {
               for (float[] v : corners) {
                  Vector3f p = transform.transformPosition(new Vector3f(v[0], v[1], v[2]));
                  v[0] = p.x;
                  v[1] = p.y;
                  v[2] = p.z;
               }
            }

            // 6 faces, 2 triangles each (12 indices, CCW outside).
            int[][] faces = new int[][]{
               {5, 1, 2, 6}, // north  (-z): b c g f -> indices 1 2 6 5
               {4, 0, 3, 7}, // south  (+z): a d h e -> indices 0 3 7 4
               {1, 0, 4, 5}, // down   (-y flipped): a e f b
               {2, 3, 7, 6}, // up:    d h g c
               {4, 7, 6, 5}, // east   (+x): e h g f
               {0, 1, 2, 3}  // west   (-x): a b c d
            };
            for (int[] quad : faces) {
               emit(out, corners[quad[0]], corners[quad[1]], corners[quad[2]]);
               emit(out, corners[quad[0]], corners[quad[2]], corners[quad[3]]);
            }
         }
      } catch (Exception e) {
         return null;
      }
      if (out.isEmpty()) return null;
      float[] tris = new float[out.size()];
      for (int i = 0; i < tris.length; i++) tris[i] = out.get(i);
      return new BbModel(tris);
   }

   private static void emit(List<Float> out, float[] a, float[] b, float[] c) {
      out.add(a[0]);
      out.add(a[1]);
      out.add(a[2]);
      out.add(b[0]);
      out.add(b[1]);
      out.add(b[2]);
      out.add(c[0]);
      out.add(c[1]);
      out.add(c[2]);
   }

   /** Wraps a BbModel as an ObjModel-compatible triangle mesh. */
   public static ObjModel toObj(BbModel bb) {
      if (bb == null) return null;
      return ObjModel.wrap(bb.triangles);
   }
}
