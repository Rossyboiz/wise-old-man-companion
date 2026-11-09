package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExchangeOfferDataTest
{
    private GrandExchangeOffer offer(GrandExchangeOfferState state, int filled)
    {
        return new GrandExchangeOffer()
        {
            public int getQuantitySold() { return filled; }
            public int getItemId() { return 100001; }
            public int getTotalQuantity() { return 10; }
            public long getPrice() { return 4000000000L; }
            public long getSpent() { return 4000000000L * filled; }
            public GrandExchangeOfferState getState() { return state; }
        };
    }

    @Test public void preservesPartialAndCompletedStatesAndLargeAmounts()
    {
        JsonArray result = ExchangeOfferData.encode(new GrandExchangeOffer[]{null,
            offer(GrandExchangeOfferState.BUYING, 3), offer(GrandExchangeOfferState.SOLD, 10)}, id -> "Fixture");
        JsonObject partial = result.get(0).getAsJsonObject();
        assertEquals(1, partial.get("slot").getAsInt());
        assertEquals("BUYING", partial.get("state").getAsString());
        assertEquals(3, partial.get("filled").getAsInt());
        assertEquals(12000000000L, partial.get("traded").getAsLong());
        assertEquals("SOLD", result.get(1).getAsJsonObject().get("state").getAsString());
    }

    @Test public void clearedAndCancelledOffersAreNotReportedAsComplete()
    {
        JsonArray result = ExchangeOfferData.encode(new GrandExchangeOffer[]{
            offer(GrandExchangeOfferState.EMPTY, 0), offer(GrandExchangeOfferState.CANCELLED_BUY, 2)}, id -> "Fixture");
        assertEquals(1, result.size());
        assertEquals("CANCELLED_BUY", result.get(0).getAsJsonObject().get("state").getAsString());
    }
}
