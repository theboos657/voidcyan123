package com.voidcyan.client.util;

import java.awt.Desktop;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class AlarmSoundManager {
   public static final String DEFAULT_SOUND = "Default Beep";
   private static final String[] SUPPORTED_EXTENSIONS = new String[]{".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a"};

   public static File getAlarmsDirectory() {
      File runDir = MinecraftClient.getInstance().runDirectory;
      if (runDir == null) {
         runDir = new File(".");
      }
      File dir = new File(runDir, "voidcyan/alarms");
      if (!dir.exists()) {
         dir.mkdirs();
      }
      return dir;
   }

   public static void openFolder() {
      File dir = getAlarmsDirectory();
      try {
         if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(dir);
            return;
         }
      } catch (Exception ignored) {
      }
      try {
         Util.getOperatingSystem().open(dir.toURI());
      } catch (Exception ignored) {
      }
   }

   private static String[] cachedOptions;
   private static long cachedOptionsAt;

   // Settings dropdowns call this several times per frame; rescan the folders at most once a second.
   public static String[] getAlarmOptions() {
      long now = System.currentTimeMillis();
      if (cachedOptions == null || now - cachedOptionsAt > 1000L) {
         cachedOptions = scanAlarmOptions();
         cachedOptionsAt = now;
      }
      return cachedOptions;
   }

   private static String[] scanAlarmOptions() {
      List<String> list = new ArrayList<>();
      list.add(DEFAULT_SOUND);

      scanDirectory(getAlarmsDirectory(), list);
      return list.toArray(new String[0]);
   }

   private static void scanDirectory(File dir, List<String> result) {
      if (dir == null || !dir.exists() || !dir.isDirectory()) return;
      File[] files = dir.listFiles();
      if (files == null) return;
      for (File f : files) {
         if (f.isFile()) {
            String name = f.getName();
            String lower = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
               if (lower.endsWith(ext)) {
                  if (!result.contains(name)) {
                     result.add(name);
                  }
                  break;
               }
            }
         }
      }
   }

   public static File findAlarmFile(String soundName) {
      if (soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase(DEFAULT_SOUND)) {
         return null;
      }
      File f = new File(getAlarmsDirectory(), soundName);
      return f.exists() && f.isFile() ? f : null;
   }

   public static void playAlarm(String soundName) {
      File file = findAlarmFile(soundName);
      if (file == null) {
         playDefaultBeep();
         return;
      }

      new Thread(() -> {
         String name = file.getName().toLowerCase();
         // Fast path for standard WAV
         if (name.endsWith(".wav")) {
            try {
               AudioInputStream ais = AudioSystem.getAudioInputStream(file);
               Clip clip = AudioSystem.getClip();
               clip.open(ais);
               clip.start();
               return;
            } catch (Exception ignored) {
            }
         }

         // FFmpeg path for MP3, OGG, FLAC, AAC, M4A, etc.
         try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            int sampleRate = grabber.getSampleRate();
            int channels = grabber.getAudioChannels();
            if (sampleRate <= 0) sampleRate = 44100;
            if (channels <= 0) channels = 2;

            AudioFormat format = new AudioFormat((float)sampleRate, 16, channels, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
               line.open(format);
               line.start();

               Frame frame;
               while ((frame = grabber.grabSamples()) != null) {
                  if (frame.samples == null || frame.samples.length == 0) continue;
                  Buffer buf = frame.samples[0];
                  if (buf instanceof ShortBuffer sb) {
                     short[] shorts = new short[sb.remaining()];
                     sb.get(shorts);
                     byte[] bytes = new byte[shorts.length * 2];
                     ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
                     line.write(bytes, 0, bytes.length);
                  } else if (buf instanceof FloatBuffer fb) {
                     float[] floats = new float[fb.remaining()];
                     fb.get(floats);
                     byte[] bytes = new byte[floats.length * 2];
                     ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                     for (float fl : floats) {
                        short s = (short)(Math.max(-1.0F, Math.min(1.0F, fl)) * 32767.0F);
                        bb.putShort(s);
                     }
                     line.write(bytes, 0, bytes.length);
                  } else if (buf instanceof ByteBuffer bb) {
                     byte[] bytes = new byte[bb.remaining()];
                     bb.get(bytes);
                     line.write(bytes, 0, bytes.length);
                  }
               }
               line.drain();
            }
            grabber.stop();
         } catch (Throwable t) {
            // Windows PowerShell Media.MediaPlayer fallback if needed
            try {
               String script = "Add-Type -AssemblyName presentationCore; $p = New-Object System.Windows.Media.MediaPlayer; $p.Open('" 
                  + file.getAbsolutePath().replace("'", "''") + "'); $p.Play(); Start-Sleep -Seconds 3";
               new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script).start();
            } catch (Throwable ignored) {
               playDefaultBeep();
            }
         }
      }, "VoidCyan-AlarmPlayer").start();
   }

   public static void playDefaultBeep() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         SoundEvent sound = (SoundEvent)Registries.SOUND_EVENT.get(Identifier.of("voidcyan", "armor_warning"));
         if (sound != null) {
            client.player.playSound(sound, 1.0F, 1.0F);
         }
      }
   }
}
