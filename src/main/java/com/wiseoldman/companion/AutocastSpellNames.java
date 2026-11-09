package com.wiseoldman.companion;

import java.util.Map;
import net.runelite.api.gameval.ItemID;
import static java.util.Map.entry;

/** Autocast objects are icon items: their ItemComposition name can be null. */
final class AutocastSpellNames
{
    private static final Map<Integer, String> NAMES = Map.ofEntries(
        entry(ItemID._01_WIND_STRIKE, "Wind Strike"),
        entry(ItemID._05_WATER_STRIKE, "Water Strike"),
        entry(ItemID._09_EARTH_STRIKE, "Earth Strike"),
        entry(ItemID._13_FIRE_STRIKE, "Fire Strike"),
        entry(ItemID._17_WIND_BOLT, "Wind Bolt"),
        entry(ItemID._23_WATER_BOLT, "Water Bolt"),
        entry(ItemID._29_EARTH_BOLT, "Earth Bolt"),
        entry(ItemID._35_FIRE_BOLT, "Fire Bolt"),
        entry(ItemID._39_CRUMBLE_UNDEAD, "Crumble Undead"),
        entry(ItemID._41_WIND_BLAST, "Wind Blast"),
        entry(ItemID._47_WATER_BLAST, "Water Blast"),
        entry(ItemID._50_IBAN_BLAST, "Iban Blast"),
        entry(ItemID._53_EARTH_BLAST, "Earth Blast"),
        entry(ItemID._59_FIRE_BLAST, "Fire Blast"),
        entry(ItemID._60_CLAWS_OF_GUTHIX, "Claws of Guthix"),
        entry(ItemID._60_FLAMES_OF_ZAMORAK, "Flames of Zamorak"),
        entry(ItemID._60_SARADOMIN_STRIKE, "Saradomin Strike"),
        entry(ItemID._62_WIND_WAVE, "Wind Wave"),
        entry(ItemID._65_WATER_WAVE, "Water Wave"),
        entry(ItemID._70_EARTH_WAVE, "Earth Wave"),
        entry(ItemID._75_FIRE_WAVE, "Fire Wave"),
        entry(ItemID._50_MAGIC_DART, "Magic Dart"),
        entry(ItemID._50_SMOKE_RUSH, "Smoke Rush"),
        entry(ItemID._52_SHADOW_RUSH, "Shadow Rush"),
        entry(ItemID._56_BLOOD_RUSH, "Blood Rush"),
        entry(ItemID._58_ICE_RUSH, "Ice Rush"),
        entry(ItemID._62_SMOKE_BURST, "Smoke Burst"),
        entry(ItemID._64_SHADOW_BURST, "Shadow Burst"),
        entry(ItemID._68_BLOOD_BURST, "Blood Burst"),
        entry(ItemID._70_ICE_BURST, "Ice Burst"),
        entry(ItemID._74_SMOKE_BLITZ, "Smoke Blitz"),
        entry(ItemID._76_SHADOW_BLITZ, "Shadow Blitz"),
        entry(ItemID._80_BLOOD_BLITZ, "Blood Blitz"),
        entry(ItemID._82_ICE_BLITZ, "Ice Blitz"),
        entry(ItemID._86_SMOKE_BARRAGE, "Smoke Barrage"),
        entry(ItemID._88_SHADOW_BARRAGE, "Shadow Barrage"),
        entry(ItemID._92_BLOOD_BARRAGE, "Blood Barrage"),
        entry(ItemID._94_ICE_BARRAGE, "Ice Barrage"),
        entry(ItemID._81_WIND_SURGE, "Wind Surge"),
        entry(ItemID._85_WATER_SURGE, "Water Surge"),
        entry(ItemID._90_EARTH_SURGE, "Earth Surge"),
        entry(ItemID._95_FIRE_SURGE, "Fire Surge")
    );

    static String resolve(int objectId, String fallback)
    {
        String known = NAMES.get(objectId);
        if (known != null)
        {
            return known;
        }
        return fallback == null || fallback.isBlank() || fallback.equals("null") ? null : fallback;
    }
}
