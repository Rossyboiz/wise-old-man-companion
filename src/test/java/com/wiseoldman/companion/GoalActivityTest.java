package com.wiseoldman.companion;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import net.runelite.client.game.ItemStack;
import org.junit.Test;
import static org.junit.Assert.*;

public class GoalActivityTest
{
    @Test public void creditedNpcKillsAndCanonicalDropsAreCumulative()
    {
        GoalActivity activity = new GoalActivity();
        activity.loot("Fixture monster", true, 1, Arrays.asList(new ItemStack(100001, 2), new ItemStack(100002, 3)), id -> 100001);
        activity.loot("Fixture monster", true, 1, Collections.emptyList(), id -> id);
        activity.loot("Fixture reward", false, 1, Collections.singletonList(new ItemStack(100001, 1)), id -> id);
        JsonObject value = activity.encode(id -> "Fixture item");
        assertEquals(2L, value.getAsJsonObject("kills").get("Fixture monster").getAsLong());
        assertFalse(value.getAsJsonObject("kills").has("Fixture reward"));
        assertEquals(6L, value.getAsJsonArray("drops").get(0).getAsJsonObject().get("quantity").getAsLong());
        assertEquals(value, activity.encode(id -> "Fixture item"));
    }

    @Test public void gameCountsDoNotDuplicateCreditedKillCounters()
    {
        GoalActivity activity = new GoalActivity();
        assertTrue(activity.message("Your Zulrah kill count is: 1,234."));
        assertTrue(activity.message("Your Zulrah kill count is: 1,234."));
        assertFalse(activity.message("Someone else killed Zulrah"));
        JsonObject value = activity.encode(id -> "Fixture");
        assertEquals(0, value.getAsJsonObject("kills").size());
        assertEquals(1234L, value.getAsJsonObject("confirmedKills").get("Zulrah").getAsLong());
    }
}
