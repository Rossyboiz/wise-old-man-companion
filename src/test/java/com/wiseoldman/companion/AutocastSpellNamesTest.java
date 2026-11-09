package com.wiseoldman.companion;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class AutocastSpellNamesTest
{
    @Test
    public void resolvesObservedNamelessSpellIcons()
    {
        assertEquals("Wind Strike", AutocastSpellNames.resolve(ItemID._01_WIND_STRIKE, "null"));
        assertEquals("Water Strike", AutocastSpellNames.resolve(ItemID._05_WATER_STRIKE, null));
        assertEquals("Fire Strike", AutocastSpellNames.resolve(ItemID._13_FIRE_STRIKE, "null"));
        assertEquals("Wind Bolt", AutocastSpellNames.resolve(ItemID._17_WIND_BOLT, "null"));
    }

    @Test
    public void resolvesAncientsAndSurges()
    {
        assertEquals("Ice Barrage", AutocastSpellNames.resolve(ItemID._94_ICE_BARRAGE, "null"));
        assertEquals("Fire Surge", AutocastSpellNames.resolve(ItemID._95_FIRE_SURGE, "null"));
    }

    @Test
    public void unknownIconsRemainUnknown()
    {
        assertNull(AutocastSpellNames.resolve(-1, "null"));
        assertEquals("New spell", AutocastSpellNames.resolve(-1, "New spell"));
    }
}
