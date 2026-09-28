package com.voidcyan.client.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.IntIterator;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.random.RandomKt;
import kotlin.ranges.CharRange;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Metadata(
   mv = {2, 3, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001:\u0003012B\u0007¢\u0006\u0004\b\u0002\u0010\u0003JO\u0010\u000e\u001a\u00020\r2\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00042\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\t2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b¢\u0006\u0004\b\u000e\u0010\u000fJE\u0010\u0011\u001a\u00020\u00102\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00042\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00072\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u0002¢\u0006\u0004\b\u0011\u0010\u0012J'\u0010\u0016\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00150\u00040\t2\u0006\u0010\u0013\u001a\u00020\u0005¢\u0006\u0004\b\u0016\u0010\u0017J\u0015\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u0005¢\u0006\u0004\b\u0018\u0010\u0019J\u0017\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u001a\u001a\u00020\u0005H\u0002¢\u0006\u0004\b\u001c\u0010\u001dJ\u001f\u0010!\u001a\u00020\u00052\u0006\u0010\u001f\u001a\u00020\u001e2\u0006\u0010 \u001a\u00020\u001bH\u0002¢\u0006\u0004\b!\u0010\"R\u001c\u0010%\u001a\n $*\u0004\u0018\u00010#0#8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b%\u0010&R$\u0010'\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u00048\u0002@\u0002X\u0082\u000e¢\u0006\u0006\n\u0004\b'\u0010(R\"\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00078\u0002@\u0002X\u0082\u000e¢\u0006\u0006\n\u0004\b\b\u0010)R\u001c\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00050*8\u0002@\u0002X\u0082\u000e¢\u0006\u0006\n\u0004\b+\u0010,R\u0018\u0010.\u001a\u0004\u0018\u00010-8\u0002@\u0002X\u0082\u000e¢\u0006\u0006\n\u0004\b.\u0010/¨\u00063"},
   d2 = {"Lcom/voidcyan/client/util/NameProtectMappings;", "", "<init>", "()V", "Lkotlin/Pair;", "", "username", "", "friendMappings", "", "otherPlayers", "Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;", "coloringInfo", "", "update", "(Lkotlin/Pair;Ljava/util/Map;Ljava/util/List;Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;)V", "", "shouldUpdate", "(Lkotlin/Pair;Ljava/util/Map;Ljava/util/List;)Z", "text", "Lorg/ahocorasick/trie/Emit;", "Lcom/voidcyan/client/util/NameProtectMappings$MappingData;", "findReplacements", "(Ljava/lang/String;)Ljava/util/List;", "findReplacement", "(Ljava/lang/String;)Ljava/lang/String;", "playerName", "Lkotlin/random/Random;", "getEntropySourceFrom", "(Ljava/lang/String;)Lkotlin/random/Random;", "", "length", "rng", "generateRandomName", "(ILkotlin/random/Random;)Ljava/lang/String;", "Lorg/slf4j/Logger;", "kotlin.jvm.PlatformType", "logger", "Lorg/slf4j/Logger;", "usernameReplacement", "Lkotlin/Pair;", "Ljava/util/Map;", "", "otherPlayerMappings", "Ljava/util/Set;", "Lcom/voidcyan/client/util/NameProtectMappings$ReplacementInstructions;", "replacementInstructions", "Lcom/voidcyan/client/util/NameProtectMappings$ReplacementInstructions;", "ColoringInfo", "MappingData", "ReplacementInstructions", "voidcyan-client"}
)
@SourceDebugExtension({"SMAP\nNameProtectMappings.kt\nKotlin\n*S Kotlin\n*F\n+ 1 NameProtectMappings.kt\ncom/voidcyan/client/util/NameProtectMappings\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 _Maps.kt\nkotlin/collections/MapsKt___MapsKt\n*L\n1#1,157:1\n1915#2,2:158\n1586#2:162\n1661#2,3:163\n1915#2,2:166\n1586#2:168\n1661#2,3:169\n221#3,2:160\n*S KotlinDebug\n*F\n+ 1 NameProtectMappings.kt\ncom/voidcyan/client/util/NameProtectMappings\n*L\n47#1:158,2\n114#1:162\n114#1:163,3\n138#1:166,2\n153#1:168\n153#1:169,3\n62#1:160,2\n*E\n"})
public final class NameProtectMappings {
   private final Logger logger = LoggerFactory.getLogger("NameProtect");
   @Nullable
   private Pair<String, String> usernameReplacement;
   @NotNull
   private Map<String, String> friendMappings = MapsKt.emptyMap();
   @NotNull
   private Set<String> otherPlayerMappings = SetsKt.emptySet();
   @Nullable
   private NameProtectMappings.ReplacementInstructions replacementInstructions;

   public final void update(
      @NotNull Pair<String, String> username,
      @NotNull Map<String, String> friendMappings,
      @NotNull List<String> otherPlayers,
      @Nullable NameProtectMappings.ColoringInfo coloringInfo
   ) {
      Intrinsics.checkNotNullParameter(username, "username");
      Intrinsics.checkNotNullParameter(friendMappings, "friendMappings");
      Intrinsics.checkNotNullParameter(otherPlayers, "otherPlayers");
      if (this.shouldUpdate(username, friendMappings, otherPlayers)) {
         HashMap currentMapping = new HashMap(otherPlayers.size() + friendMappings.size());
         Iterable $this$forEach$iv = otherPlayers.subList(0, RangesKt.coerceAtMost(200, otherPlayers.size()));
         int $i$f$forEach = 0;

         for (Object element$iv : $this$forEach$iv) {
            String playerName = (String)element$iv;
            int name = playerName.length();
            if (3 <= name ? name < 21 : false) {
               Random rng = this.getEntropySourceFrom(playerName);
               String randomName = this.generateRandomName(16, rng);
               currentMapping.put(
                  playerName,
                  coloringInfo != null
                     ? new NameProtectMappings.MappingData(randomName, coloringInfo.getOtherPlayers())
                     : new NameProtectMappings.MappingData(randomName)
               );
            }
         }

         $i$f$forEach = 0;

         for (Entry element$ivx : friendMappings.entrySet()) {
            String name = (String)element$ivx.getKey();
            String replacement = (String)element$ivx.getValue();
            currentMapping.put(
               name,
               coloringInfo != null
                  ? new NameProtectMappings.MappingData(replacement, coloringInfo.getFriends())
                  : new NameProtectMappings.MappingData(replacement)
            );
         }

         this.friendMappings = MapsKt.toMap(friendMappings);
         this.otherPlayerMappings = CollectionsKt.toHashSet(otherPlayers);
         this.usernameReplacement = username;
         currentMapping.put(
            username.getFirst(),
            coloringInfo != null
               ? new NameProtectMappings.MappingData((String)username.getSecond(), coloringInfo.getUsername())
               : new NameProtectMappings.MappingData((String)username.getSecond())
         );
         Trie matcher = Trie.builder().addKeywords(currentMapping.keySet()).ignoreOverlaps().build();
         Intrinsics.checkNotNull(matcher);
         this.replacementInstructions = new NameProtectMappings.ReplacementInstructions(matcher, currentMapping);
      }
   }

   private final boolean shouldUpdate(Pair<String, String> username, Map<String, String> friendMappings, List<String> otherPlayers) {
      if (this.replacementInstructions == null) {
         return true;
      } else if (!Intrinsics.areEqual(this.usernameReplacement, username)) {
         return true;
      } else {
         return !Intrinsics.areEqual(friendMappings, this.friendMappings)
            ? true
            : !Intrinsics.areEqual(CollectionsKt.toHashSet(otherPlayers), this.otherPlayerMappings);
      }
   }

   @NotNull
   public final List<Pair<Emit, NameProtectMappings.MappingData>> findReplacements(@NotNull String text) {
      Intrinsics.checkNotNullParameter(text, "text");
      NameProtectMappings.ReplacementInstructions currentInstructions = this.replacementInstructions;
      if (currentInstructions == null) {
         return CollectionsKt.emptyList();
      } else {
         Collection matches = currentInstructions.getMatcher().parseText(text);
         Intrinsics.checkNotNull(matches);
         Iterable $this$map$iv = matches;
         int $i$f$map = 0;
         ArrayList destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
         int $i$f$mapTo = 0;

         for (Object item$iv$iv : $this$map$iv) {
            Emit emit = (Emit)item$iv$iv;
            String keyword = emit.getKeyword();
            NameProtectMappings.MappingData mapping = currentInstructions.getReplacements().get(keyword);
            destination$iv$iv.add(mapping != null ? TuplesKt.to(emit, mapping) : null);
         }

         return CollectionsKt.filterNotNull((Iterable & List)destination$iv$iv);
      }
   }

   @NotNull
   public final String findReplacement(@NotNull String text) {
      Intrinsics.checkNotNullParameter(text, "text");
      NameProtectMappings.ReplacementInstructions currentInstructions = this.replacementInstructions;
      if (currentInstructions == null) {
         this.logger.info("[NameProtect] No replacement instructions - trie not built");
         return text;
      } else {
         List replacements = this.findReplacements(text);
         if (replacements.isEmpty()) {
            return text;
         } else {
            Object result = null;
            result = text;
            Iterable $this$forEach$iv = replacements;
            int $i$f$forEach = 0;

            for (Object element$iv : $this$forEach$iv) {
               Pair var9 = (Pair)element$iv;
               Emit emit = (Emit)var9.component1();
               NameProtectMappings.MappingData mappingData = (NameProtectMappings.MappingData)var9.component2();
               int var14 = emit.getStart();
               int var15 = emit.getEnd() + 1;
               CharSequence var16 = mappingData.getNewName();
               result = StringsKt.replaceRange((CharSequence)result, var14, var15, var16).toString();
            }

            return (String)result;
         }
      }
   }

   private final Random getEntropySourceFrom(String playerName) {
      try {
         MessageDigest var10000 = MessageDigest.getInstance("MD5");
         byte[] var10001 = playerName.getBytes(Charsets.UTF_8);
         Intrinsics.checkNotNullExpressionValue(var10001, "getBytes(...)");
         byte[] hash = var10000.digest(var10001);
         long l = ByteBuffer.wrap(hash).getLong();
         return RandomKt.Random(l);
      } catch (Exception e) {
         return RandomKt.Random((long)playerName.hashCode());
      }
   }

   private final String generateRandomName(int length, Random rng) {
      List chars = CollectionsKt.plus(
         CollectionsKt.plus((Iterable)(new CharRange('a', 'z')), (Iterable)(new CharRange('A', 'Z'))), (Iterable)(new CharRange('0', '9'))
      );
      Iterable $this$map$iv = (Iterable)(new IntRange(1, length));
      int $i$f$map = 0;
      ArrayList destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
      int $i$f$mapTo = 0;
      Iterator var9 = $this$map$iv.iterator();

      while (var9.hasNext()) {
         int item$iv$iv = ((IntIterator)var9).nextInt();
         destination$iv$iv.add((Character)chars.get(rng.nextInt(chars.size())));
      }

      StringBuilder sb = new StringBuilder(); for (Object c : (java.util.List)destination$iv$iv) sb.append(c); return sb.toString();
   }

   @Metadata(
      mv = {2, 3, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002¢\u0006\u0004\b\u0007\u0010\bJ\u0016\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002HÆ\u0003¢\u0006\u0004\b\t\u0010\nJ\u0016\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002HÆ\u0003¢\u0006\u0004\b\u000b\u0010\nJ\u0016\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002HÆ\u0003¢\u0006\u0004\b\f\u0010\nJ@\u0010\r\u001a\u00020\u00002\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00022\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u00022\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002HÆ\u0001¢\u0006\u0004\b\r\u0010\u000eJ\u001b\u0010\u0011\u001a\u00020\u00102\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004¢\u0006\u0004\b\u0011\u0010\u0012J\u0011\u0010\u0013\u001a\u00020\u0003HÖ\u0081\u0004¢\u0006\u0004\b\u0013\u0010\u0014J\u0011\u0010\u0016\u001a\u00020\u0015HÖ\u0081\u0004¢\u0006\u0004\b\u0016\u0010\u0017R\u001d\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00028\u0006¢\u0006\f\n\u0004\b\u0004\u0010\u0018\u001a\u0004\b\u0019\u0010\nR\u001d\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u00028\u0006¢\u0006\f\n\u0004\b\u0005\u0010\u0018\u001a\u0004\b\u001a\u0010\nR\u001d\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00028\u0006¢\u0006\f\n\u0004\b\u0006\u0010\u0018\u001a\u0004\b\u001b\u0010\n¨\u0006\u001c"},
      d2 = {"Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;", "", "Lkotlin/Function0;", "", "username", "friends", "otherPlayers", "<init>", "(Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;)V", "component1", "()Lkotlin/jvm/functions/Function0;", "component2", "component3", "copy", "(Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;)Lcom/voidcyan/client/util/NameProtectMappings$ColoringInfo;", "other", "", "equals", "(Ljava/lang/Object;)Z", "hashCode", "()I", "", "toString", "()Ljava/lang/String;", "Lkotlin/jvm/functions/Function0;", "getUsername", "getFriends", "getOtherPlayers", "voidcyan-client"}
   )
   public static final class ColoringInfo {
      @NotNull
      private final Function0<Integer> username;
      @NotNull
      private final Function0<Integer> friends;
      @NotNull
      private final Function0<Integer> otherPlayers;

      public ColoringInfo(@NotNull Function0<Integer> username, @NotNull Function0<Integer> friends, @NotNull Function0<Integer> otherPlayers) {
         Intrinsics.checkNotNullParameter(username, "username");
         Intrinsics.checkNotNullParameter(friends, "friends");
         Intrinsics.checkNotNullParameter(otherPlayers, "otherPlayers");
         this.username = username;
         this.friends = friends;
         this.otherPlayers = otherPlayers;
      }

      @NotNull
      public final Function0<Integer> getUsername() {
         return this.username;
      }

      @NotNull
      public final Function0<Integer> getFriends() {
         return this.friends;
      }

      @NotNull
      public final Function0<Integer> getOtherPlayers() {
         return this.otherPlayers;
      }

      @NotNull
      public final Function0<Integer> component1() {
         return this.username;
      }

      @NotNull
      public final Function0<Integer> component2() {
         return this.friends;
      }

      @NotNull
      public final Function0<Integer> component3() {
         return this.otherPlayers;
      }

      @NotNull
      public final NameProtectMappings.ColoringInfo copy(
         @NotNull Function0<Integer> username, @NotNull Function0<Integer> friends, @NotNull Function0<Integer> otherPlayers
      ) {
         Intrinsics.checkNotNullParameter(username, "username");
         Intrinsics.checkNotNullParameter(friends, "friends");
         Intrinsics.checkNotNullParameter(otherPlayers, "otherPlayers");
         return new NameProtectMappings.ColoringInfo(username, friends, otherPlayers);
      }

      @NotNull
      @Override
      public String toString() {
         return "ColoringInfo(username=" + this.username + ", friends=" + this.friends + ", otherPlayers=" + this.otherPlayers + ")";
      }

      @Override
      public int hashCode() {
         int result = this.username.hashCode();
         result = result * 31 + this.friends.hashCode();
         return result * 31 + this.otherPlayers.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof NameProtectMappings.ColoringInfo var2)) {
            return false;
         } else if (!Intrinsics.areEqual(this.username, var2.username)) {
            return false;
         } else {
            return !Intrinsics.areEqual(this.friends, var2.friends) ? false : Intrinsics.areEqual(this.otherPlayers, var2.otherPlayers);
         }
      }
   }

   @Metadata(
      mv = {2, 3, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\n\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004¢\u0006\u0004\b\u0007\u0010\bJ\u0010\u0010\t\u001a\u00020\u0002HÆ\u0003¢\u0006\u0004\b\t\u0010\nJ\u0016\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004HÆ\u0003¢\u0006\u0004\b\u000b\u0010\fJ*\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00022\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004HÆ\u0001¢\u0006\u0004\b\r\u0010\u000eJ\u001b\u0010\u0011\u001a\u00020\u00102\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004¢\u0006\u0004\b\u0011\u0010\u0012J\u0011\u0010\u0013\u001a\u00020\u0005HÖ\u0081\u0004¢\u0006\u0004\b\u0013\u0010\u0014J\u0011\u0010\u0015\u001a\u00020\u0002HÖ\u0081\u0004¢\u0006\u0004\b\u0015\u0010\nR\u0017\u0010\u0003\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b\u0003\u0010\u0016\u001a\u0004\b\u0017\u0010\nR\u001d\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048\u0006¢\u0006\f\n\u0004\b\u0006\u0010\u0018\u001a\u0004\b\u0019\u0010\f¨\u0006\u001a"},
      d2 = {"Lcom/voidcyan/client/util/NameProtectMappings$MappingData;", "", "", "newName", "Lkotlin/Function0;", "", "colorGetter", "<init>", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V", "component1", "()Ljava/lang/String;", "component2", "()Lkotlin/jvm/functions/Function0;", "copy", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;)Lcom/voidcyan/client/util/NameProtectMappings$MappingData;", "other", "", "equals", "(Ljava/lang/Object;)Z", "hashCode", "()I", "toString", "Ljava/lang/String;", "getNewName", "Lkotlin/jvm/functions/Function0;", "getColorGetter", "voidcyan-client"}
   )
   public static final class MappingData {
      @NotNull
      private final String newName;
      @NotNull
      private final Function0<Integer> colorGetter;

      public MappingData(@NotNull String newName) {
         this(newName, MappingData::_init_$lambda$0);
      }

      public MappingData(@NotNull String newName, @NotNull Function0<Integer> colorGetter) {
         Intrinsics.checkNotNullParameter(newName, "newName");
         Intrinsics.checkNotNullParameter(colorGetter, "colorGetter");
         this.newName = newName;
         this.colorGetter = colorGetter;
      }

      @NotNull
      public final String getNewName() {
         return this.newName;
      }

      @NotNull
      public final Function0<Integer> getColorGetter() {
         return this.colorGetter;
      }

      @NotNull
      public final String component1() {
         return this.newName;
      }

      @NotNull
      public final Function0<Integer> component2() {
         return this.colorGetter;
      }

      @NotNull
      public final NameProtectMappings.MappingData copy(@NotNull String newName, @NotNull Function0<Integer> colorGetter) {
         Intrinsics.checkNotNullParameter(newName, "newName");
         Intrinsics.checkNotNullParameter(colorGetter, "colorGetter");
         return new NameProtectMappings.MappingData(newName, colorGetter);
      }

      @NotNull
      @Override
      public String toString() {
         return "MappingData(newName=" + this.newName + ", colorGetter=" + this.colorGetter + ")";
      }

      @Override
      public int hashCode() {
         int result = this.newName.hashCode();
         return result * 31 + this.colorGetter.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof NameProtectMappings.MappingData var2)) {
            return false;
         } else {
            return !Intrinsics.areEqual(this.newName, var2.newName) ? false : Intrinsics.areEqual(this.colorGetter, var2.colorGetter);
         }
      }

      private static final int _init_$lambda$0() {
         return 16777215;
      }
   }

   @Metadata(
      mv = {2, 3, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0003\u001a\u00020\u0002\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004¢\u0006\u0004\b\b\u0010\tJ\u0010\u0010\n\u001a\u00020\u0002HÆ\u0003¢\u0006\u0004\b\n\u0010\u000bJ\u001c\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004HÆ\u0003¢\u0006\u0004\b\f\u0010\rJ0\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00022\u0014\b\u0002\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004HÆ\u0001¢\u0006\u0004\b\u000e\u0010\u000fJ\u001b\u0010\u0012\u001a\u00020\u00112\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004¢\u0006\u0004\b\u0012\u0010\u0013J\u0011\u0010\u0015\u001a\u00020\u0014HÖ\u0081\u0004¢\u0006\u0004\b\u0015\u0010\u0016J\u0011\u0010\u0017\u001a\u00020\u0005HÖ\u0081\u0004¢\u0006\u0004\b\u0017\u0010\u0018R\u0017\u0010\u0003\u001a\u00020\u00028\u0006¢\u0006\f\n\u0004\b\u0003\u0010\u0019\u001a\u0004\b\u001a\u0010\u000bR#\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u00048\u0006¢\u0006\f\n\u0004\b\u0007\u0010\u001b\u001a\u0004\b\u001c\u0010\r¨\u0006\u001d"},
      d2 = {"Lcom/voidcyan/client/util/NameProtectMappings$ReplacementInstructions;", "", "Lorg/ahocorasick/trie/Trie;", "matcher", "", "", "Lcom/voidcyan/client/util/NameProtectMappings$MappingData;", "replacements", "<init>", "(Lorg/ahocorasick/trie/Trie;Ljava/util/Map;)V", "component1", "()Lorg/ahocorasick/trie/Trie;", "component2", "()Ljava/util/Map;", "copy", "(Lorg/ahocorasick/trie/Trie;Ljava/util/Map;)Lcom/voidcyan/client/util/NameProtectMappings$ReplacementInstructions;", "other", "", "equals", "(Ljava/lang/Object;)Z", "", "hashCode", "()I", "toString", "()Ljava/lang/String;", "Lorg/ahocorasick/trie/Trie;", "getMatcher", "Ljava/util/Map;", "getReplacements", "voidcyan-client"}
   )
   public static final class ReplacementInstructions {
      @NotNull
      private final Trie matcher;
      @NotNull
      private final Map<String, NameProtectMappings.MappingData> replacements;

      public ReplacementInstructions(@NotNull Trie matcher, @NotNull Map<String, NameProtectMappings.MappingData> replacements) {
         Intrinsics.checkNotNullParameter(matcher, "matcher");
         Intrinsics.checkNotNullParameter(replacements, "replacements");
         this.matcher = matcher;
         this.replacements = replacements;
      }

      @NotNull
      public final Trie getMatcher() {
         return this.matcher;
      }

      @NotNull
      public final Map<String, NameProtectMappings.MappingData> getReplacements() {
         return this.replacements;
      }

      @NotNull
      public final Trie component1() {
         return this.matcher;
      }

      @NotNull
      public final Map<String, NameProtectMappings.MappingData> component2() {
         return this.replacements;
      }

      @NotNull
      public final NameProtectMappings.ReplacementInstructions copy(@NotNull Trie matcher, @NotNull Map<String, NameProtectMappings.MappingData> replacements) {
         Intrinsics.checkNotNullParameter(matcher, "matcher");
         Intrinsics.checkNotNullParameter(replacements, "replacements");
         return new NameProtectMappings.ReplacementInstructions(matcher, replacements);
      }

      @NotNull
      @Override
      public String toString() {
         return "ReplacementInstructions(matcher=" + this.matcher + ", replacements=" + this.replacements + ")";
      }

      @Override
      public int hashCode() {
         int result = this.matcher.hashCode();
         return result * 31 + this.replacements.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof NameProtectMappings.ReplacementInstructions var2)) {
            return false;
         } else {
            return !Intrinsics.areEqual(this.matcher, var2.matcher) ? false : Intrinsics.areEqual(this.replacements, var2.replacements);
         }
      }
   }
}
