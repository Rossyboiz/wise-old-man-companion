package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import net.runelite.api.Item;

/** Normalises notes/placeholders and sums real quantities without counting empty slots. */
final class AccountItemTotals
{
    static JsonArray encode(Item[] items, IntUnaryOperator canonicalize, IntFunction<String> names)
    {
        Map<Integer, Long> totals = new TreeMap<>();
        if (items != null)
        {
            for (Item item : items)
            {
                if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
                {
                    continue;
                }
                int id = canonicalize.applyAsInt(item.getId());
                if (id >= 0)
                {
                    totals.merge(id, (long) item.getQuantity(), Long::sum);
                }
            }
        }
        JsonArray result = new JsonArray();
        totals.forEach((id, quantity) -> {
            JsonObject value = new JsonObject();
            value.addProperty("id", id);
            String name = names.apply(id);
            value.addProperty("name", name == null ? "Item " + id : name);
            value.addProperty("quantity", quantity);
            result.add(value);
        });
        return result;
    }
}
