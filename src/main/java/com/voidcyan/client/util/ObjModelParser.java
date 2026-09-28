package com.voidcyan.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Wavefront .obj parser for custom player models.
 *
 * Supports the constructs produced by Blockbench/Blender exporters in practice:
 * "v x y z" positions, "vt u v" texture coordinates and "f i/i i/i i/i [i/i]"
 * faces (triangles or quads). Faces are split into triangles, so everything is
 * consumed downstream as triangle lists.
 *
 * Coordinates are converted from OBJ convention (+Y up, +Z toward viewer) into
 * Minecraft model convention (-Y up, +Z behind the player) during parse:
 *   mc.x = -obj.x,  mc.y = -obj.y,  mc.z = -obj.z
 * V is flipped too (mc texture space has V growing downward).
 */
public final class ObjModelParser {

   public static final class Tri {
      public final float[] xyz = new float[9];   // 3 verts, x/y/z in Minecraft model units
      public final float[] uv = new float[6];    // 3 verts, u/v in 0..1
   }

   public static final class Result {
      public final List<Tri> tris;
      public final float minX, minY, minZ, maxX, maxY, maxZ;

      Result(List<Tri> tris, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
         this.tris = tris;
         this.minX = minX; this.minY = minY; this.minZ = minZ;
         this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
      }

      public float widthX()  { return maxX - minX; }
      public float heightY() { return maxY - minY; }
      public float depthZ()  { return maxZ - minZ; }
   }

   /** Hard sanity limits so a pathological file cannot stall or OOM the client. */
   public static final int MAX_FACES = 60000;

   public static Result parse(Path file) throws IOException {
      List<float[]> positions = new ArrayList<>();
      List<float[]> texCoords = new ArrayList<>();
      List<Tri> tris = new ArrayList<>();

      float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

      try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
         String line;
         int lineNo = 0;
         while ((line = reader.readLine()) != null) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("s ")
                  || line.startsWith("o ") || line.startsWith("g ")
                  || line.startsWith("mtllib") || line.startsWith("usemtl")) {
               continue;
            }

            if (line.startsWith("v ")) {
               String[] parts = line.split("\\s+");
               if (parts.length < 4) continue;
               try {
                  float ox = Float.parseFloat(parts[1]);
                  float oy = Float.parseFloat(parts[2]);
                  float oz = Float.parseFloat(parts[3]);
                  float x = -ox, y = -oy, z = -oz; // OBJ -> Minecraft model convention
                  positions.add(new float[]{x, y, z});
                  if (x < minX) minX = x;
                  if (y < minY) minY = y;
                  if (z < minZ) minZ = z;
                  if (x > maxX) maxX = x;
                  if (y > maxY) maxY = y;
                  if (z > maxZ) maxZ = z;
               } catch (NumberFormatException ignored) {}
            } else if (line.startsWith("vt ")) {
               String[] parts = line.split("\\s+");
               if (parts.length < 3) continue;
               try {
                  float u = Float.parseFloat(parts[1]);
                  float v = Float.parseFloat(parts[2]);
                  texCoords.add(new float[]{u, 1.0f - v}); // V flip for MC texture space
               } catch (NumberFormatException ignored) {}
            } else if (line.startsWith("f ")) {
               if (tris.size() >= MAX_FACES) {
                  throw new IOException("Too many faces in .obj (limit " + MAX_FACES + ")");
               }
               String[] parts = line.split("\\s+");
               // parts[0] == "f"; corners are parts[1..]
               int corners = parts.length - 1;
               if (corners < 3) continue;
               if (corners > 4) {
                  throw new IOException("Face with " + corners + " corners at line " + lineNo
                     + " — only triangles and quads are supported");
               }

               int[] vi = new int[corners];
               int[] ti = new int[corners];
               for (int c = 0; c < corners; c++) {
                  String tok = parts[c + 1];
                  String[] slash = tok.split("/");
                  try {
                     int raw = Integer.parseInt(slash[0]);
                     vi[c] = raw > 0 ? raw - 1 : positions.size() + raw; // negative = relative
                  } catch (NumberFormatException e) {
                     throw new IOException("Bad vertex index '" + tok + "' at line " + lineNo);
                  }
                  if (slash.length > 1 && !slash[1].isEmpty()) {
                     try {
                        int tRaw = Integer.parseInt(slash[1]);
                        ti[c] = tRaw > 0 ? tRaw - 1 : texCoords.size() + tRaw;
                     } catch (NumberFormatException e) {
                        ti[c] = -1;
                     }
                  } else {
                     ti[c] = -1;
                  }
               }

               // Quad -> two triangles (0,1,2) + (0,2,3); triangle passes through as-is.
               int[][] triIdx = corners == 4
                  ? new int[][]{{0, 1, 2}, {0, 2, 3}}
                  : new int[][]{{0, 1, 2}};
               for (int[] t : triIdx) {
                  Tri tri = new Tri();
                  for (int k = 0; k < 3; k++) {
                     float[] p = positions.get(vi[t[k]]);
                     tri.xyz[k * 3] = p[0];
                     tri.xyz[k * 3 + 1] = p[1];
                     tri.xyz[k * 3 + 2] = p[2];
                     if (ti[t[k]] >= 0 && ti[t[k]] < texCoords.size()) {
                        float[] tc = texCoords.get(ti[t[k]]);
                        tri.uv[k * 2] = tc[0];
                        tri.uv[k * 2 + 1] = tc[1];
                     }
                  }
                  tris.add(tri);
               }
            }
         }
      }

      if (tris.isEmpty()) {
         throw new IOException("No faces found in .obj file");
      }

      return new Result(tris, minX, minY, minZ, maxX, maxY, maxZ);
   }

   private ObjModelParser() {}
}
