package com.wiseoldman.companion;

import net.runelite.api.Client;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class TextureColor
{
    private static Client client;

    private static final Map<Integer, Color> colorCache = new HashMap<>();

    // Called by our Wise Old Man plugin to provide RuneLite access.
    public static void setClient(Client runeLiteClient)
    {
        client = runeLiteClient;
        colorCache.clear();
    }

    // Retrieves the average colour of a RuneScape texture.
    public static Color getColor(int textureId)
    {
        if (colorCache.containsKey(textureId))
        {
            return colorCache.get(textureId);
        }

        // Fallback colour if RuneLite is unavailable.
        if (client == null || client.getTextureProvider() == null)
        {
            return Color.WHITE;
        }

        int[] pixels = client.getTextureProvider().load(textureId);

        // Some textures may not be available immediately.
        if (pixels == null || pixels.length == 0)
        {
            return Color.WHITE;
        }

        long r = 0;
        long g = 0;
        long b = 0;
        int n = 0;

        for (int pixel : pixels)
        {
            // Ignore transparent pixels.
            if (pixel == 0)
            {
                continue;
            }

            Color c = new Color(pixel);

            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
            n++;
        }

        // Prevent division by zero if every pixel was transparent.
        if (n == 0)
        {
            return Color.WHITE;
        }

        Color average = new Color(
                (int) (r / n),
                (int) (g / n),
                (int) (b / n)
        );

        colorCache.put(textureId, average);

        return average;
    }
}