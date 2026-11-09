package com.wiseoldman.companion;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlowpipeDartDataTest
{
    @Test
    public void readsTypedDartsAndScales()
    {
        BlowpipeDartData data = BlowpipeDartData.parse("Darts: Adamant dart x 16,383. Scales: 16,383 (100.0%).");
        assertNotNull(data);
        assertEquals(ItemID.ADAMANT_DART, data.dartId);
        assertEquals(16383, data.quantity);
        assertEquals(Integer.valueOf(16383), data.scales);
    }

    @Test
    public void supportsOtherDartTypesAndNoScales()
    {
        BlowpipeDartData data = BlowpipeDartData.parse("Darts: Amethyst dart x 200.");
        assertEquals(ItemID.AMETHYST_DART, data.dartId);
        assertNull(data.scales);
        assertEquals(ItemID.DRAGON_DART, BlowpipeDartData.parse("Darts: Dragon x 2. Scales: 100 (0.6%).").dartId);
    }

    @Test
    public void emptyDartsAreKnownEmpty()
    {
        BlowpipeDartData data = BlowpipeDartData.parse("Darts: None. Scales: 99 (0.6%).");
        assertNotNull(data);
        assertEquals(0, data.dartId);
        assertEquals(0, data.quantity);
    }

    @Test
    public void unrelatedOrUnknownTextIsNotGuessed()
    {
        assertNull(BlowpipeDartData.parse("You have some darts."));
        assertNull(BlowpipeDartData.parse("Darts: Mystery dart x 10. Scales: 99 (0.6%)."));
    }
}
