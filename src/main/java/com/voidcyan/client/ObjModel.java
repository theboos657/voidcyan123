package com.voidcyan.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal OBJ loader for custom crit-effect models (Blender/Blockbench export).
 * Parses positions + faces, triangulates polygons, caches by path+mtime.
 * Coordinates are used as-is: 1 OBJ unit = 1 block, so set "scale" in the
 * effect JSON if your model is huge or tiny.
 */
public final class ObjModel {
   /** Flat triangle list: 9 floats per triangle (x,y,z per corner). */
   public final float[] triangles;

   /** Internal; use parse or the wrap helper. */
   ObjModel(float[] triangles) {
      this.triangles = triangles;
   }

   /** Wraps pre-parsed world-space triangle data (used by BbModel). */
   public static ObjModel wrap(float[] triangles) {
      return triangles == null || triangles.length == 0 ? null : new ObjModel(triangles);
   }

   private static final Map<String, Cached> CACHE = new ConcurrentHashMap<>();

   private record Cached(long mtime, ObjModel model) {
   }

   public static ObjModel get(File file) {
      if (file == null || !file.isFile()) return null;
      long mtime;
      try {
         mtime = file.lastModified();
      } catch (Exception e) {
         return null;
      }
      Cached c = CACHE.get(file.getAbsolutePath());
      if (c != null && c.mtime() == mtime) return c.model();
      ObjModel parsed = parse(file);
      if (parsed != null) {
         CACHE.put(file.getAbsolutePath(), new Cached(mtime, parsed));
      }
      return parsed;
   }

   public static void clearCache() {
      CACHE.clear();
   }

   private static ObjModel parse(File file) {
      List<Float> out = new ArrayList<>();
      try {
         List<float[]> positions = new ArrayList<>();
         String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
         for (String rawLine : text.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("\\s+");
            if (parts[0].equals("v") && parts.length >= 4) {
               try {
                  positions.add(new float[]{
                     Float.parseFloat(parts[1]),
                     Float.parseFloat(parts[2]),
                     Float.parseFloat(parts[3])
                  });
               } catch (NumberFormatException ignored) {
               }
            } else if (parts[0].equals("f") && parts.length >= 4) {
               int[] idx = new int[parts.length - 1];
               boolean ok = true;
               for (int i = 1; i < parts.length; i++) {
                  String tok = parts[i];
                  int slash = tok.indexOf('/');
                  if (slash >= 0) tok = tok.substring(0, slash);
                  try {
                     int v = Integer.parseInt(tok);
                     if (v < 0) v = positions.size() + v; // negative = relative
                     else v = v - 1; // OBJ is 1-based
                     if (v < 0 || v >= positions.size()) {
                        ok = false;
                        break;
                     }
                     idx[i - 1] = v;
                  } catch (NumberFormatException nfe) {
                     ok = false;
                     break;
                  }
               }
               if (!ok) continue;
               // Triangle fan over the polygon.
               for (int i = 1; i + 1 < idx.length; i++) {
                  float[] pa = positions.get(idx[0]);
                  float[] pb = positions.get(idx[i]);
                  float[] pc = positions.get(idx[i + 1]);
                  out.add(pa[0]);
                  out.add(pa[1]);
                  out.add(pa[2]);
                  out.add(pb[0]);
                  out.add(pb[1]);
                  out.add(pb[2]);
                  out.add(pc[0]);
                  out.add(pc[1]);
                  out.add(pc[2]);
               }
            }
         }
      } catch (Exception e) {
         return null;
      }
      if (out.isEmpty()) return null;
      float[] tris = new float[out.size()];
      for (int i = 0; i < tris.length; i++) tris[i] = out.get(i);
      return new ObjModel(tris);
   }
}
