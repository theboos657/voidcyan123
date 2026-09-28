package com.voidcyan.client.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.ahocorasick.trie.Emit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Metadata(
   mv = {2, 3, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u001b\u0010\u0006\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0004H\u0007¢\u0006\u0004\b\u0006\u0010\u0007J\u001b\u0010\b\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0004H\u0007¢\u0006\u0004\b\b\u0010\u0007J\r\u0010\n\u001a\u00020\t¢\u0006\u0004\b\n\u0010\u000bJ\u0015\u0010\u000e\u001a\u00020\r2\u0006\u0010\f\u001a\u00020\t¢\u0006\u0004\b\u000e\u0010\u000fJ\u0015\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010¢\u0006\u0004\b\u0012\u0010\u0013J\u0017\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002¢\u0006\u0004\b\u0014\u0010\u0013JO\u0010\u001d\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0016\u001a\u00020\u00102\u0014\b\u0002\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u00172\u000e\b\u0002\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00100\u00192\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001b¢\u0006\u0004\b\u001d\u0010\u001eR\u001c\u0010!\u001a\n  *\u0004\u0018\u00010\u001f0\u001f8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b!\u0010\"R\u0014\u0010$\u001a\u00020#8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b$\u0010%R0\u0010(\u001a\u001e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100&j\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u0010`'8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b(\u0010)R\u0014\u0010+\u001a\u00020*8\u0002X\u0082T¢\u0006\u0006\n\u0004\b+\u0010,R\u0016\u0010\f\u001a\u00020\t8\u0002@\u0002X\u0082\u000e¢\u0006\u0006\n\u0004\b\f\u0010-¨\u0006."},
   d2 = {"Lcom/voidcyan/client/util/NameProtect;", "", "<init>", "()V", "Lnet/minecraft/class_2561;", "text", "processText", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_2561;", "processTextFlat", "", "isEnabled", "()Z", "enabled", "", "setEnabled", "(Z)V", "", "original", "replace", "(Ljava/lang/String;)Ljava/lang/String;", "uncachedReplace", "targetName", "replacementName", "", "extraReplacements", "", "otherPlayers", "Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;", "coloringInfo", "updateMappings", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Ljava/util/List;Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;)V", "Lorg/slf4j/Logger;", "kotlin.jvm.PlatformType", "logger", "Lorg/slf4j/Logger;", "Lcom/voidcyan/client/util/NameProtectMappings;", "mappings", "Lcom/voidcyan/client/util/NameProtectMappings;", "Ljava/util/LinkedHashMap;", "Lkotlin/collections/LinkedHashMap;", "stringMappingCache", "Ljava/util/LinkedHashMap;", "", "CACHE_SIZE", "I", "Z", "voidcyan-client"}
)
@SourceDebugExtension({"SMAP\nNameProtect.kt\nKotlin\n*S Kotlin\n*F\n+ 1 NameProtect.kt\ncom/voidcyan/client/util/NameProtect\n+ 2 Maps.kt\nkotlin/collections/MapsKt__MapsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,181:1\n383#2,7:182\n1#3:189\n*S KotlinDebug\n*F\n+ 1 NameProtect.kt\ncom/voidcyan/client/util/NameProtect\n*L\n93#1:182,7\n*E\n"})
public final class NameProtect {
   @NotNull
   public static final NameProtect INSTANCE = new NameProtect();
   private static final Logger logger = LoggerFactory.getLogger("NameProtect");
   @NotNull
   private static final NameProtectMappings mappings = new NameProtectMappings();
   @NotNull
   private static final LinkedHashMap<String, String> stringMappingCache = new LinkedHashMap<>();
   private static final int CACHE_SIZE = 512;
   private static boolean enabled;

   private NameProtect() {
   }

   @JvmStatic
   @Nullable
   public static final Text processText(@Nullable Text text) {
      if (text == null || !enabled) {
         return text;
      } else {
         MutableText result = null;
         boolean contentChanged = false;
         if (text.getContent() instanceof net.minecraft.text.PlainTextContent plain) {
            String original = plain.string();
            String replaced = INSTANCE.replace(original);
            if (!Intrinsics.areEqual(replaced, original)) {
               result = Text.literal(replaced);
               contentChanged = true;
            } else {
               result = text.copyContentOnly();
            }
         } else {
            result = text.copyContentOnly();
         }

         result.setStyle(text.getStyle());
         boolean siblingsChanged = false;

         for (Text sibling : text.getSiblings()) {
            Text processedSibling = processText(sibling);
            result.append(processedSibling);
            if (processedSibling != sibling) {
               siblingsChanged = true;
            }
         }

         if (!contentChanged && !siblingsChanged) {
            String flatString = text.getString();
            String replaced = INSTANCE.replace(flatString);
            if (!Intrinsics.areEqual(replaced, flatString)) {
               return Text.literal(replaced).setStyle(text.getStyle());
            }
            return text;
         }

         return (Text)result;
      }
   }

   @JvmStatic
   @Nullable
   public static final Text processTextFlat(@Nullable Text text) {
      if (text == null || !enabled) {
         return text;
      } else {
         String flatString = text.getString();
         String replaced = INSTANCE.replace(flatString);
         return !Intrinsics.areEqual(replaced, flatString) ? (Text)Text.literal(replaced).setStyle(text.getStyle()) : text;
      }
   }

   public final boolean isEnabled() {
      return enabled;
   }

   public final void setEnabled(boolean enabled) {
      NameProtect.enabled = enabled;
   }

   @NotNull
   public final String replace(@NotNull String original) {
      Intrinsics.checkNotNullParameter(original, "original");
      if (!this.isEnabled()) {
         return original;
      } else {
         Map $this$getOrPut$iv = stringMappingCache;
         int $i$f$getOrPut = 0;
         Object value$iv = $this$getOrPut$iv.get(original);
         Object var10000;
         if (value$iv == null) {
            Object answer$iv = INSTANCE.uncachedReplace(original);
            $this$getOrPut$iv.put(original, answer$iv);
            var10000 = answer$iv;
         } else {
            var10000 = value$iv;
         }

         Object var7 = var10000;
         String it = (String)var7;
         $i$f$getOrPut = 0;
         if (stringMappingCache.size() > 512) {
            stringMappingCache.clear();
         }

         return (String)var7;
      }
   }

   private final String uncachedReplace(String original) {
      StringBuilder stripped = new StringBuilder(original.length());
      int[] indexMap = new int[original.length() + 1];
      int sIdx = 0;
      int oIdx = 0;

      while (oIdx < original.length()) {
         if (original.charAt(oIdx) == 167 && oIdx + 1 < original.length()) {
            oIdx += 2;
         } else {
            indexMap[sIdx] = oIdx;
            stripped.append(original.charAt(oIdx));
            sIdx++;
            oIdx++;
         }
      }

      indexMap[sIdx] = oIdx;
      String var10000 = stripped.toString();
      Intrinsics.checkNotNullExpressionValue(var10000, "toString(...)");
      String strippedStr = var10000;
      List replacements = mappings.findReplacements(strippedStr);
      if (replacements.isEmpty()) {
         return original;
      } else {
         StringBuilder var8 = new StringBuilder();
         StringBuilder $this$uncachedReplace_u24lambda_u240 = var8;
         int currReplacementIndex = 0;
         int currentOriginalIndex = 0;

         while (currentOriginalIndex < original.length()) {
            Pair replacement = (Pair)CollectionsKt.getOrNull(replacements, currReplacementIndex);
            if (replacement != null) {
               int sStart = ((Emit)replacement.getFirst()).getStart();
               int oStart = indexMap[sStart];
               if (oStart == currentOriginalIndex) {
                  $this$uncachedReplace_u24lambda_u240.append(((NameProtectMappings.MappingData)replacement.getSecond()).getNewName());
                  int lastMatchedCharIndex = ((Emit)replacement.getFirst()).getEnd();
                  currentOriginalIndex = indexMap[lastMatchedCharIndex] + 1;
                  currReplacementIndex++;
               } else {
                  $this$uncachedReplace_u24lambda_u240.append(original, currentOriginalIndex, oStart);
                  currentOriginalIndex = oStart;
               }
            } else {
               $this$uncachedReplace_u24lambda_u240.append(original, currentOriginalIndex, original.length());
               currentOriginalIndex = original.length();
            }
         }

         String var17 = var8.toString();
         logger.debug("[NameProtect] Replaced string: " + StringsKt.take(original, 30) + "... -> " + StringsKt.take(var17, 30) + "...");
         return var17;
      }
   }

   public final void updateMappings(
      @NotNull String targetName,
      @NotNull String replacementName,
      @NotNull Map<String, String> extraReplacements,
      @NotNull List<String> otherPlayers,
      @Nullable NameProtectMappings.ColoringInfo coloringInfo
   ) {
      Intrinsics.checkNotNullParameter(targetName, "targetName");
      Intrinsics.checkNotNullParameter(replacementName, "replacementName");
      Intrinsics.checkNotNullParameter(extraReplacements, "extraReplacements");
      Intrinsics.checkNotNullParameter(otherPlayers, "otherPlayers");
      mappings.update(TuplesKt.to(targetName, replacementName), extraReplacements, otherPlayers, coloringInfo);
      stringMappingCache.clear();
   }

   public static void updateMappings$default(NameProtect instance, String targetName, String replacementName, java.util.Map extraReplacements, java.util.List otherPlayers, NameProtectMappings.ColoringInfo coloringInfo, int flags, Object ignored) {
      if ((flags & 4) != 0) { extraReplacements = java.util.Collections.emptyMap(); }
      if ((flags & 8) != 0) { otherPlayers = java.util.Collections.emptyList(); }
      if ((flags & 16) != 0) { coloringInfo = null; }
      instance.updateMappings(targetName, replacementName, extraReplacements, otherPlayers, coloringInfo);
   }
}