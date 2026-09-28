package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.util.Identifier;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AnimatedTexture {
   @NotNull private final String path;
   @NotNull private final String namespace;
   @NotNull private final String idPrefix;
   @Nullable private NativeImageBackedTexture texture;
   @Nullable private Identifier textureId;
   private boolean error;
   @NotNull private final AtomicBoolean running = new AtomicBoolean(false);
   @NotNull private final LinkedBlockingQueue<BufferedImage> frameQueue = new LinkedBlockingQueue<>(5);
   @Nullable private Thread decodeThread;
   private long frameDelayMs = 33L;

   public AnimatedTexture(@NotNull String path, @NotNull String namespace, @NotNull String idPrefix) {
      this.path = path;
      this.namespace = namespace;
      this.idPrefix = idPrefix;

      try {
         File file = new File(this.path);
         if (!file.exists()) {
            System.err.println("[VoidCyan] File not found: " + this.path);
            this.error = true;
         } else {
            System.err.println("[VoidCyan] Loading: " + file.getAbsolutePath());
            String lower = this.path.toLowerCase(Locale.ROOT);
            if (!lower.endsWith(".mp4") && !lower.endsWith(".webm") && !lower.endsWith(".avi") && !lower.endsWith(".mov")) {
               this.loadStatic(file);
            } else {
               this.startVideo(file);
            }
         }
      } catch (Throwable t) {
         System.err.println("[VoidCyan] Error initializing AnimatedTexture:");
         t.printStackTrace();
         this.error = true;
      }
   }

   public AnimatedTexture(@NotNull String path, @NotNull String namespace) {
      this(path, namespace, "anim");
   }

   public AnimatedTexture(@NotNull String path) {
      this(path, "minecraft", "anim");
   }

   private void loadStatic(File file) {
      try {
         NativeImage img = VoidCyanClient.loadAnyImage(this.path);
         this.texture = new NativeImageBackedTexture(null, img);
         this.textureId = Identifier.of(this.namespace, this.idPrefix + "_" + System.currentTimeMillis());
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, this.texture);
      } catch (Exception e) {
         System.err.println("[VoidCyan] Error loading static image:");
         e.printStackTrace();
         this.error = true;
      }
   }

   private void startVideo(File file) {
      System.err.println("[VoidCyan] Starting video playback for: " + file.getAbsolutePath());
      this.running.set(true);
      this.decodeThread = new Thread(() -> decodeVideoLoop(file), "voidcyan-video-decoder");
      this.decodeThread.setDaemon(true);
      this.decodeThread.start();
   }

   private void decodeVideoLoop(File file) {
      FFmpegFrameGrabber grabber = null;
      Java2DFrameConverter converter = null;
      try {
         grabber = new FFmpegFrameGrabber(file);
         grabber.start();
         converter = new Java2DFrameConverter();
         double fps = grabber.getFrameRate();
         this.frameDelayMs = fps > 0.0 ? (long)(1000.0 / fps) : 33L;
         System.err.println("[VoidCyan] Video FPS: " + fps + ", delay: " + this.frameDelayMs + "ms, " + grabber.getImageWidth() + "x" + grabber.getImageHeight());

         while (this.running.get()) {
            if (Thread.currentThread().isInterrupted()) break;
            try (Frame frame = grabber.grabImage()) {
               if (frame == null) {
                  try {
                     grabber.stop();
                     grabber.start();
                  } catch (Exception e) {
                     System.err.println("[VoidCyan] Error looping video:");
                     e.printStackTrace();
                  }
                  continue;
               }
               BufferedImage bimg = converter.convert(frame);
               if (bimg != null) {
                  while (!this.frameQueue.offer(bimg)) {
                     BufferedImage old = this.frameQueue.poll();
                     if (old != null) old.flush();
                  }
               }
            }
            try {
               Thread.sleep(this.frameDelayMs);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               break;
            }
         }
      } catch (Throwable t) {
         System.err.println("[VoidCyan] Error loading/playing video:");
         t.printStackTrace();
         this.error = true;
      } finally {
         System.err.println("[VoidCyan] Video thread stopping");
         if (grabber != null) {
            try { grabber.stop(); } catch (Exception ignored) {}
            try { grabber.release(); } catch (Exception ignored) {}
         }
         if (converter != null) {
            try { converter.close(); } catch (Exception ignored) {}
         }
      }
   }

   private static BufferedImage toARGB(BufferedImage image) {
      if (image.getType() == BufferedImage.TYPE_INT_ARGB) return image;
      BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = argb.createGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
      return argb;
   }

   private static int swapRedBlue(int argb) {
      return (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >> 16) | ((argb & 0x000000FF) << 16);
   }

   private static void copyPixels(BufferedImage src, NativeImage dst, int w, int h) {
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            dst.setColor(x, y, swapRedBlue(src.getRGB(x, y)));
         }
      }
   }

   @Nullable
   public Identifier updateAndGetId() {
      if (this.error) return null;
      BufferedImage bimg = this.frameQueue.poll();
      if (bimg != null) {
         try {
            BufferedImage argb = toARGB(bimg);
            if (this.texture == null) {
               NativeImage img = new NativeImage(Format.RGBA, argb.getWidth(), argb.getHeight(), true);
               copyPixels(argb, img, argb.getWidth(), argb.getHeight());
               this.texture = new NativeImageBackedTexture(null, img);
               this.textureId = Identifier.of(this.namespace, this.idPrefix);
               MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, this.texture);
            } else {
               NativeImage nativeImage = this.texture.getImage();
               if (nativeImage != null) {
                  int w = Math.min(argb.getWidth(), nativeImage.getWidth());
                  int h = Math.min(argb.getHeight(), nativeImage.getHeight());
                  copyPixels(argb, nativeImage, w, h);
                  this.texture.upload();
               }
            }
         } catch (Exception e) {
            System.err.println("[VoidCyan] Error updating texture:");
            e.printStackTrace();
         } finally {
            bimg.flush();
         }
      }
      return this.textureId;
   }

   public void close() {
      System.err.println("[VoidCyan] Closing AnimatedTexture");
      this.running.set(false);
      if (this.decodeThread != null) {
         this.decodeThread.interrupt();
         try {
            this.decodeThread.join(1000L);
         } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
         }
         this.decodeThread = null;
      }

      BufferedImage img;
      while ((img = this.frameQueue.poll()) != null) {
         img.flush();
      }

      if (this.textureId != null) {
         try {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(this.textureId);
         } catch (Exception e) {
            System.err.println("[VoidCyan] Error destroying texture:");
            e.printStackTrace();
         }
         this.textureId = null;
      }

      if (this.texture != null) {
         this.texture.close();
         this.texture = null;
      }
      System.err.println("[VoidCyan] AnimatedTexture closed");
   }
}
