package com.wiseoldman.companion;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

/** Reads only the game's charge-check response; never guesses from the ammo slot. */
final class BlowpipeDartData
{
    private static final Pattern DARTS = Pattern.compile(
        "\\bDarts:\\s*(Bronze|Iron|Steel|Black|Mithril|Adamant|Rune|Runite|Amethyst|Dragon)(?: dart)?\\s+x\\s+([\\d,]+)\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY = Pattern.compile("\\bDarts:\\s*None\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCALES = Pattern.compile("\\bScales:\\s*([\\d,]+)", Pattern.CASE_INSENSITIVE);

    final int dartId;
    final int quantity;
    final Integer scales;

    private BlowpipeDartData(int dartId, int quantity, Integer scales)
    {
        this.dartId = dartId;
        this.quantity = quantity;
        this.scales = scales;
    }

    static BlowpipeDartData parse(String message)
    {
        Matcher darts = DARTS.matcher(message);
        Matcher scaleMatch = SCALES.matcher(message);
        Integer scales = scaleMatch.find() ? Integer.valueOf(scaleMatch.group(1).replace(",", "")) : null;
        if (EMPTY.matcher(message).find())
        {
            return new BlowpipeDartData(0, 0, scales);
        }
        if (!darts.find())
        {
            return null;
        }
        int id;
        switch (darts.group(1).toLowerCase(Locale.ROOT))
        {
            case "bronze": id = ItemID.BRONZE_DART; break;
            case "iron": id = ItemID.IRON_DART; break;
            case "steel": id = ItemID.STEEL_DART; break;
            case "black": id = ItemID.BLACK_DART; break;
            case "mithril": id = ItemID.MITHRIL_DART; break;
            case "adamant": id = ItemID.ADAMANT_DART; break;
            case "rune": case "runite": id = ItemID.RUNE_DART; break;
            case "amethyst": id = ItemID.AMETHYST_DART; break;
            case "dragon": id = ItemID.DRAGON_DART; break;
            default: return null;
        }
        return new BlowpipeDartData(id, Integer.parseInt(darts.group(2).replace(",", "")), scales);
    }

    static boolean isBlowpipe(int id)
    {
        switch (id)
        {
            case ItemID.TOXIC_BLOWPIPE_LOADED:
            case ItemID.TOXIC_BLOWPIPE_LOADED_ORNAMENT:
            case ItemID.CAMPHOR_BLOWPIPE:
            case ItemID.IRONWOOD_BLOWPIPE:
            case ItemID.ROSEWOOD_BLOWPIPE:
                return true;
            default: return false;
        }
    }
}
