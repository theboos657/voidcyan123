package com.voidcyan.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One shape of particles in a custom effect model (saved as an entry of "parts" in an effect JSON). */
public final class FxPart {
   public static final String[] TYPES = {"sphere", "ring", "disc", "line", "cube", "helix", "cone", "star"};

   public String type = "ring";
   public float x, y, z;
   public float sx = 0.6F, sy = 0.6F, sz = 0.6F;
   public float rx, ry, rz; // rotation in degrees
   public int count = 40;
   public String color = "#00F5FF";
   public float size = 0.12F;
   public float outward = 0.0F;
   public float rise = 0.0F;
   public float spin = 0.0F;
   public float life = 0.8F;
   public float gravity = 0.0F;
   public boolean streak = false;

   public FxPart() {
   }

   public FxPart(String type) {
      this.type = type;
      if (type.equals("line") || type.equals("helix") || type.equals("cone")) this.sy = 0.9F;
      if (type.equals("sphere")) this.outward = 0.6F;
   }

   public FxPart copy() {
      FxPart c = new FxPart(this.type);
      c.x = this.x; c.y = this.y; c.z = this.z;
      c.sx = this.sx; c.sy = this.sy; c.sz = this.sz;
      c.rx = this.rx; c.ry = this.ry; c.rz = this.rz;
      c.count = this.count; c.color = this.color; c.size = this.size;
      c.outward = this.outward; c.rise = this.rise; c.spin = this.spin;
      c.life = this.life; c.gravity = this.gravity; c.streak = this.streak;
      return c;
   }

   public int argb() {
      try {
         String t = this.color.replace("#", "").trim();
         return 0xFF000000 | (int) Long.parseLong(t, 16) & 0xFFFFFF;
      } catch (Exception e) {
         return 0xFF00F5FF;
      }
   }

   /** Points in effect space: {x, y, z, dirX, dirY, dirZ}. Deterministic so the editor shows the same shape every frame. */
   public List<float[]> points() {
      List<float[]> out = new ArrayList<>();
      Random r = new Random(7L + this.type.hashCode());
      int n = Math.max(1, this.count);
      for (int i = 0; i < n; i++) {
         float t = (float) i / n;
         float px = 0.0F, py = 0.0F, pz = 0.0F;
         switch (this.type) {
            case "sphere" -> {
               double a = r.nextDouble() * Math.PI * 2.0;
               double u = r.nextDouble() * 2.0 - 1.0;
               double s = Math.sqrt(1.0 - u * u);
               px = (float) (Math.cos(a) * s) * this.sx;
               py = (float) u * this.sy;
               pz = (float) (Math.sin(a) * s) * this.sz;
            }
            case "disc" -> {
               double a = r.nextDouble() * Math.PI * 2.0;
               double rr = Math.sqrt(r.nextDouble());
               px = (float) (Math.cos(a) * rr) * this.sx;
               pz = (float) (Math.sin(a) * rr) * this.sz;
            }
            case "line" -> py = (t * 2.0F - 1.0F) * this.sy;
            case "cube" -> {
               int face = r.nextInt(6);
               float a = (float) (r.nextDouble() * 2.0 - 1.0);
               float b = (float) (r.nextDouble() * 2.0 - 1.0);
               float s = face % 2 == 0 ? 1.0F : -1.0F;
               switch (face / 2) {
                  case 0 -> { px = s; py = a; pz = b; }
                  case 1 -> { px = a; py = s; pz = b; }
                  default -> { px = a; py = b; pz = s; }
               }
               px *= this.sx;
               py *= this.sy;
               pz *= this.sz;
            }
            case "helix" -> {
               double a = t * Math.PI * 6.0;
               px = (float) Math.cos(a) * this.sx;
               py = (t * 2.0F - 1.0F) * this.sy;
               pz = (float) Math.sin(a) * this.sz;
            }
            case "cone" -> {
               double a = t * Math.PI * 10.0;
               float rr = this.sx * (1.0F - t);
               px = (float) Math.cos(a) * rr;
               py = (t * 2.0F - 1.0F) * this.sy;
               pz = (float) Math.sin(a) * rr * (this.sz / Math.max(0.01F, this.sx));
            }
            case "star" -> {
               float f = t * 10.0F;
               int k = (int) f;
               float w = f - k;
               double a0 = k * Math.PI / 5.0 - Math.PI / 2.0;
               double a1 = (k + 1) * Math.PI / 5.0 - Math.PI / 2.0;
               float r0 = k % 2 == 0 ? 1.0F : 0.42F;
               float r1 = k % 2 == 0 ? 0.42F : 1.0F;
               px = (float) (Math.cos(a0) * r0 * (1 - w) + Math.cos(a1) * r1 * w) * this.sx;
               py = (float) (Math.sin(a0) * r0 * (1 - w) + Math.sin(a1) * r1 * w) * this.sy;
            }
            default -> { // ring
               double a = t * Math.PI * 2.0;
               px = (float) Math.cos(a) * this.sx;
               pz = (float) Math.sin(a) * this.sz;
            }
         }

         if (this.rx != 0.0F || this.ry != 0.0F || this.rz != 0.0F) {
            double ax = Math.toRadians(this.rx), ay = Math.toRadians(this.ry), az = Math.toRadians(this.rz);
            double y1 = py * Math.cos(ax) - pz * Math.sin(ax);
            double z1 = py * Math.sin(ax) + pz * Math.cos(ax);
            double x2 = px * Math.cos(ay) + z1 * Math.sin(ay);
            double z2 = -px * Math.sin(ay) + z1 * Math.cos(ay);
            double x3 = x2 * Math.cos(az) - y1 * Math.sin(az);
            double y3 = x2 * Math.sin(az) + y1 * Math.cos(az);
            px = (float) x3;
            py = (float) y3;
            pz = (float) z2;
         }

         float len = (float) Math.sqrt(px * px + py * py + pz * pz);
         float dx = len < 1e-4F ? 0.0F : px / len;
         float dy = len < 1e-4F ? 1.0F : py / len;
         float dz = len < 1e-4F ? 0.0F : pz / len;
         out.add(new float[]{px + this.x, py + this.y, pz + this.z, dx, dy, dz});
      }

      return out;
   }
}
