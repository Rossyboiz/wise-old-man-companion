package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import net.runelite.api.Item;
import org.junit.Test;
import static org.junit.Assert.*;

public class AccountItemTotalsTest
{
    @Test
    public void notesAndUnnotedStacksAreCombined()
    {
        JsonArray result = AccountItemTotals.encode(new Item[]{new Item(100001, 7), new Item(100002, 3)},
            id -> 100001, id -> "Fixture item");
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getAsJsonObject().get("quantity").getAsLong());
    }

    @Test
    public void placeholdersAndEmptySlotsDoNotBecomeHoldings()
    {
        JsonArray result = AccountItemTotals.encode(new Item[]{null, new Item(-1, 1), new Item(100001, 0)},
            id -> id, id -> "Fixture item");
        assertEquals(0, result.size());
    }

    @Test
    public void quantitiesDoNotOverflowAnInteger()
    {
        JsonArray result = AccountItemTotals.encode(new Item[]{new Item(100001, Integer.MAX_VALUE), new Item(100001, 1)},
            id -> id, id -> "Fixture item");
        assertEquals(2147483648L, result.get(0).getAsJsonObject().get("quantity").getAsLong());
    }
}
