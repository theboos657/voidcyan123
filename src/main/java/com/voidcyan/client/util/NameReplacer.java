package com.voidcyan.client.util;

import com.voidcyan.client.VoidCyanClient;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class NameReplacer {
   private record CompiledTarget(String target, Pattern pattern) {}

   private static volatile CompiledTarget compiled = new CompiledTarget("", Pattern.compile(""));

   private static Pattern targetPattern(String target) {
      CompiledTarget current = compiled;
      if (!current.target().equals(target)) {
         current = new CompiledTarget(target, Pattern.compile("(?i)\\b" + Pattern.quote(target) + "\\b"));
         compiled = current;
      }
      return current.pattern();
   }

   public static String getTargetName() {
      if (VoidCyanClient.nickHiderTargetName != null && !VoidCyanClient.nickHiderTargetName.trim().isEmpty()) {
         return VoidCyanClient.nickHiderTargetName.trim();
      }
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.getSession() != null && client.getSession().getUsername() != null && !client.getSession().getUsername().isEmpty()) {
         return client.getSession().getUsername();
      }
      if (client.player != null) {
         return client.player.getName().getString();
      }
      return "";
   }

   public static String getReplacementName() {
      if (VoidCyanClient.nickHiderReplacementName != null && !VoidCyanClient.nickHiderReplacementName.isEmpty()) {
         return VoidCyanClient.nickHiderReplacementName;
      }
      return "Hidden";
   }

   public static String getDisplayName(String original) {
      if (!VoidCyanClient.isNickHiderEnabled || original == null) {
         return original;
      }
      String targetName = getTargetName();
      String replacementName = getReplacementName();
      if (targetName.isEmpty()) {
         return original;
      }
      if (shouldHide(original, targetName)) {
         return replacementName;
      }
      if (original.contains(targetName)) {
         return targetPattern(targetName).matcher(original).replaceAll(Matcher.quoteReplacement(replacementName));
      }
      return original;
   }

   private static boolean shouldHide(String name, String targetName) {
      return name.equalsIgnoreCase(targetName);
   }

   public static Text replaceInText(Text text) {
      if (!VoidCyanClient.isNickHiderEnabled || text == null) {
         return text;
      }
      String targetName = getTargetName();
      String replacementName = getReplacementName();
      if (targetName.isEmpty()) {
         return text;
      }
      return rebuildText(text, targetName, replacementName);
   }

   private static Text rebuildText(Text text, String target, String replacement) {
      String quotedReplacement = Matcher.quoteReplacement(replacement);
      Pattern pattern = targetPattern(target);
      MutableText newText;
      if (text.getContent() instanceof PlainTextContent plain) {
         String originalStr = plain.string();
         String replacedStr = pattern.matcher(originalStr).replaceAll(quotedReplacement);
         newText = Text.literal(replacedStr);
      } else if (text.getContent() instanceof TranslatableTextContent trans) {
         Object[] args = trans.getArgs();
         Object[] newArgs = new Object[args.length];

         for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Text t) {
               newArgs[i] = rebuildText(t, target, replacement);
            } else if (args[i] instanceof String s) {
               newArgs[i] = pattern.matcher(s).replaceAll(quotedReplacement);
            } else {
               newArgs[i] = args[i];
            }
         }

         newText = Text.translatable(trans.getKey(), newArgs);
      } else {
         newText = MutableText.of(text.getContent());
      }

      newText.setStyle(text.getStyle());

      for (Text sibling : text.getSiblings()) {
         newText.append(rebuildText(sibling, target, replacement));
      }

      return newText;
   }
}
