package com.wiseoldman.companion;

import org.junit.Test;
import static org.junit.Assert.*;

public class SkillXpProgressTest
{
    @Test public void baselineAndUnchangedUpdatesAreNotActions()
    {
        SkillXpProgress progress = new SkillXpProgress(50000);
        progress.observe(50000, "baseline");
        assertEquals(0, progress.lastGain);
        assertNull(progress.gainAt);
        progress.observe(50035, "gain");
        progress.observe(50035, "unchanged");
        assertEquals(35, progress.lastGain);
        assertEquals("gain", progress.gainAt);
    }

    @Test public void newGainReplacesEstimateAndLowerXpClearsIt()
    {
        SkillXpProgress progress = new SkillXpProgress(100);
        progress.observe(135, "first");
        progress.observe(205, "second");
        assertEquals(70, progress.lastGain);
        progress.observe(10, "reset");
        assertEquals(0, progress.lastGain);
        assertNull(progress.gainAt);
    }
}
