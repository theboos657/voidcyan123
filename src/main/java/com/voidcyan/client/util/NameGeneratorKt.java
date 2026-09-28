package com.voidcyan.client.util;

import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.ranges.RangesKt;
import org.jetbrains.annotations.NotNull;

@Metadata(
   mv = {2, 3, 0},
   k = 2,
   xi = 48,
   d1 = {"\u0000(\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\f\n\u0002\b\u0003\u001a!\u0010\u0005\u001a\u00020\u00042\b\b\u0002\u0010\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u0002¢\u0006\u0004\b\u0005\u0010\u0006\u001a'\u0010\t\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u0000H\u0002¢\u0006\u0004\b\t\u0010\n\"\u001a\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u000b8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\f\u0010\r\"\u001a\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u000b8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\u000e\u0010\r\" \u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000f8\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\u0011\u0010\u0012¨\u0006\u0013"},
   d2 = {"", "maxLength", "Lkotlin/random/Random;", "rng", "", "randomUsername", "(ILkotlin/random/Random;)Ljava/lang/String;", "str", "leetReplacements", "leetRandomly", "(Lkotlin/random/Random;Ljava/lang/String;I)Ljava/lang/String;", "", "ADJECTIVE_LIST", "Ljava/util/List;", "ANIMAL_LIST", "", "", "LEET_MAP", "Ljava/util/Map;", "voidcyan-client"}
)
@SourceDebugExtension({"SMAP\nNameGenerator.kt\nKotlin\n*S Kotlin\n*F\n+ 1 NameGenerator.kt\ncom/voidcyan/client/util/NameGeneratorKt\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,85:1\n777#2:86\n873#2,2:87\n777#2:89\n873#2,2:90\n1915#2,2:93\n1#3:92\n*S KotlinDebug\n*F\n+ 1 NameGenerator.kt\ncom/voidcyan/client/util/NameGeneratorKt\n*L\n35#1:86\n35#1:87,2\n36#1:89\n36#1:90,2\n55#1:93,2\n*E\n"})
public final class NameGeneratorKt {
   @NotNull
   private static final List<String> ADJECTIVE_LIST;
   @NotNull
   private static final List<String> ANIMAL_LIST;
   @NotNull
   private static final Map<Character, Character> LEET_MAP;

   @NotNull
   public static final String randomUsername(int maxLength, @NotNull Random rng) {
      Intrinsics.checkNotNullParameter(rng, "rng");
      Pair var2 = rng.nextBoolean() ? TuplesKt.to(ADJECTIVE_LIST, ANIMAL_LIST) : TuplesKt.to(ANIMAL_LIST, ADJECTIVE_LIST);
      List firstWordList = (List)var2.component1();
      List secondWordList = (List)var2.component2();
      Iterable $this$filter$iv = firstWordList;
      int $i$f$filter = 0;
      var destination$iv$iv = new ArrayList();
      int $i$f$filterTo = 0;

      for (Object element$iv$iv : $this$filter$iv) {
         String it = (String)element$iv$iv;
         if (it.length() <= maxLength - 3) {
            destination$iv$iv.add(element$iv$iv);
         }
      }

      String firstWord = (String)CollectionsKt.random((Collection & List)destination$iv$iv, rng);
      Iterable $this$filter$ivx = secondWordList;
      int $i$f$filterx = 0;
      var destination$iv$ivx = new ArrayList();
      int $i$f$filterTox = 0;

      for (Object element$iv$ivx : $this$filter$ivx) {
         String it = (String)element$iv$ivx;
         if (it.length() <= maxLength - firstWord.length()) {
            destination$iv$ivx.add(element$iv$ivx);
         }
      }

      String secondWord = (String)CollectionsKt.random((Collection & List)destination$iv$ivx, rng);
      String[] var24 = new String[]{firstWord, secondWord};
      List elements = CollectionsKt.mutableListOf(var24);
      Iterable var25 = elements;
      $i$f$filterTo = 0;

      for (Object var39 : var25) {
         String var43 = (String)var39;
         int var19 = var43.length();
         $i$f$filterTo += var19;
      }

      if ($i$f$filterTo + 1 < maxLength && rng.nextInt(20) != 0) {
         int until = RangesKt.coerceAtMost(maxLength - $i$f$filterTo, 3);
         $i$f$filterTo = until <= 2 ? until : rng.nextInt(2, until);
         elements.add(String.valueOf(rng.nextInt(IntMath.pow(10, $i$f$filterTo))));
      }

      Iterable var31 = elements;
      $i$f$filterTox = 0;

      for (Object var44 : var31) {
         String var48 = (String)var44;
         int var20 = var48.length();
         $i$f$filterTox += var20;
      }

      int allowedDelimiters = maxLength - $i$f$filterTox;
      $i$f$filterTo = 0;
      $i$f$filterTo = rng.nextBits(2);

      while (Integer.bitCount($i$f$filterTo) > RangesKt.coerceAtLeast(allowedDelimiters, 0)) {
         $i$f$filterTo = rng.nextBits(2);
      }

      StringBuilder output = new StringBuilder((String)elements.get(0));
      Iterable $this$forEach$iv = elements.subList(1, elements.size());
      int $i$f$forEach = 0;

      for (Object element$iv : $this$forEach$iv) {
         String it = (String)element$iv;
         if (($i$f$filterTo & 1) == 1) {
            output.append('_');
         }

         $i$f$filterTo >>= 1;
         output.append(it);
      }

      String var10001 = output.toString();
      Intrinsics.checkNotNullExpressionValue(var10001, "toString(...)");
      return leetRandomly(rng, var10001, rng.nextInt(3));
   }

   private static final String leetRandomly(Random rng, String str, int leetReplacements) {
      char[] var10000 = str.toCharArray();
      Intrinsics.checkNotNullExpressionValue(var10000, "toCharArray(...)");
      char[] charArray = var10000;
      ArrayList list = new ArrayList();
      int i = 0;

      for (int var6 = charArray.length; i < var6; i++) {
         Character leetChar = LEET_MAP.get(charArray[i]);
         if (leetChar != null) {
            list.add(new Pair(i, leetChar));
         }
      }

      CollectionsKt.shuffle(list, rng);

      for (Object _var10raw : CollectionsKt.take(list, RangesKt.coerceAtMost(leetReplacements, list.size()))) { Pair var10 = (Pair)_var10raw;
         int ix = ((Number)var10.component1()).intValue();
         char leetC = (Character)var10.component2();
         charArray[ix] = leetC;
      }

      return new String(charArray);
   }

   static {
      String[] var0 = new String[]{
         "Angry",
         "Brave",
         "Calm",
         "Dark",
         "Epic",
         "Fast",
         "Giant",
         "Happy",
         "Icy",
         "Jolly",
         "Kind",
         "Loud",
         "Mad",
         "Nice",
         "Old",
         "Proud",
         "Quick",
         "Red",
         "Sad",
         "Tall",
         "Wild",
         "Young",
         "Zealous",
         "Bold",
         "Cold",
         "Dull",
         "Fair",
         "Good",
         "Holy",
         "Icy",
         "Just",
         "Keen",
         "Long",
         "Mild",
         "New",
         "Pale",
         "Rare",
         "Sharp",
         "Tame",
         "Vast",
         "Warm",
         "Xenial",
         "Young",
         "Zany"
      };
      ADJECTIVE_LIST = CollectionsKt.listOf(var0);
      var0 = new String[]{
         "Bear",
         "Cat",
         "Dog",
         "Eagle",
         "Fox",
         "Goat",
         "Hawk",
         "Iguana",
         "Jackal",
         "Koala",
         "Lion",
         "Monkey",
         "Newt",
         "Owl",
         "Panda",
         "Quail",
         "Rabbit",
         "Snake",
         "Tiger",
         "Urchin",
         "Viper",
         "Wolf",
         "Xenops",
         "Yak",
         "Zebra"
      };
      ANIMAL_LIST = CollectionsKt.listOf(var0);
      Pair[] var2 = new Pair[]{
         TuplesKt.to('a', '4'),
         TuplesKt.to('b', '8'),
         TuplesKt.to('e', '3'),
         TuplesKt.to('g', '6'),
         TuplesKt.to('i', '1'),
         TuplesKt.to('o', '0'),
         TuplesKt.to('s', '5'),
         TuplesKt.to('t', '7'),
         TuplesKt.to('z', '2')
      };
      LEET_MAP = MapsKt.mapOf(var2);
   }
}
