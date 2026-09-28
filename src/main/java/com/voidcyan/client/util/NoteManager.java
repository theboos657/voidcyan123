package com.voidcyan.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public class NoteManager {
   public static List<NoteManager.Note> notes = new ArrayList<>();
   private static final File NOTES_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "voidcyan_notes.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   public static void loadNotes() {
      if (!NOTES_FILE.exists()) {
         notes.add(
            new NoteManager.Note(
               "Welcome to Notes!",
               "This is the VoidCyan Notes feature.\nYou can add, edit, and copy notes here.\nNotes are saved automatically when you close the game."
            )
         );
         saveNotes();
      } else {
         try (FileReader reader = new FileReader(NOTES_FILE)) {
            Type type = (new TypeToken<List<NoteManager.Note>>() {}).getType();
            List<NoteManager.Note> loaded = (List<NoteManager.Note>)GSON.fromJson(reader, type);
            if (loaded != null) {
               notes = loaded;
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }

   public static void saveNotes() {
      try (FileWriter writer = new FileWriter(NOTES_FILE)) {
         GSON.toJson(notes, writer);
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   public static void addNote(String title, String content) {
      notes.add(new NoteManager.Note(title, content));
   }

   public static class Note {
      public String title;
      public String content;
      public long timestamp;

      public Note(String title, String content) {
         this.title = title;
         this.content = content;
         this.timestamp = System.currentTimeMillis();
      }
   }
}
