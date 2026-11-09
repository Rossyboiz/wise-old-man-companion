package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.IntFunction;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;

/** Copies observed slot data; completion is only taken from the game's offer state. */
final class ExchangeOfferData
{
    static JsonArray encode(GrandExchangeOffer[] offers, IntFunction<String> names)
    {
        JsonArray result = new JsonArray();
        if (offers == null)
        {
            return result;
        }
        for (int slot = 0; slot < offers.length; slot++)
        {
            GrandExchangeOffer offer = offers[slot];
            if (offer == null || offer.getState() == null || offer.getState() == GrandExchangeOfferState.EMPTY
                || offer.getItemId() <= 0 || offer.getTotalQuantity() <= 0)
            {
                continue;
            }
            JsonObject value = new JsonObject();
            value.addProperty("slot", slot);
            value.addProperty("id", offer.getItemId());
            value.addProperty("name", names.apply(offer.getItemId()));
            value.addProperty("state", offer.getState().name());
            value.addProperty("quantity", offer.getTotalQuantity());
            value.addProperty("filled", offer.getQuantitySold());
            value.addProperty("price", offer.getPrice());
            value.addProperty("traded", offer.getSpent());
            result.add(value);
        }
        return result;
    }
}
