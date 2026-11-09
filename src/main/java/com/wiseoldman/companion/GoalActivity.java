package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.game.ItemStack;

/** Cumulative observations; the receiver merges deltas so uploads never duplicate kills/drops. */
final class GoalActivity
{
    private static final Pattern COUNT = Pattern.compile("Your (.+?) (?:kill|completion) count is: ([\\d,]+)", Pattern.CASE_INSENSITIVE);
    private final String session = UUID.randomUUID().toString();
    private final Map<String, Long> kills = new TreeMap<>();
    private final Map<Integer, Long> drops = new TreeMap<>();
    private final Map<String, Long> confirmed = new TreeMap<>();

    void loot(String source, boolean npc, int amount, Collection<ItemStack> items, IntUnaryOperator canonicalize)
    {
        if (npc && amount > 0)
        {
            kills.merge(source, (long) amount, Long::sum);
        }
        for (ItemStack item : items)
        {
            if (item.getId() > 0 && item.getQuantity() > 0)
            {
                drops.merge(canonicalize.applyAsInt(item.getId()), (long) item.getQuantity(), Long::sum);
            }
        }
    }

    boolean message(String message)
    {
        Matcher match = COUNT.matcher(message);
        if (!match.find())
        {
            return false;
        }
        try
        {
            confirmed.put(match.group(1).trim(), Long.parseLong(match.group(2).replace(",", "")));
            return true;
        }
        catch (NumberFormatException ignored)
        {
            return false;
        }
    }

    JsonObject encode(IntFunction<String> names)
    {
        JsonObject value = new JsonObject();
        value.addProperty("session", session);
        JsonObject killCounts = new JsonObject();
        kills.forEach(killCounts::addProperty);
        value.add("kills", killCounts);
        JsonObject records = new JsonObject();
        confirmed.forEach(records::addProperty);
        value.add("confirmedKills", records);
        JsonArray loot = new JsonArray();
        drops.forEach((id, quantity) -> {
            JsonObject item = new JsonObject();
            item.addProperty("id", id);
            item.addProperty("name", names.apply(id));
            item.addProperty("quantity", quantity);
            loot.add(item);
        });
        value.add("drops", loot);
        return value;
    }
}
