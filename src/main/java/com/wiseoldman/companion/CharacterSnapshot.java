package com.wiseoldman.companion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Color;
import net.runelite.api.Model;

import static com.wiseoldman.companion.JagexColor.createPalette;

/** Copies the live model on the client thread; no Model escapes to a worker. */
final class CharacterSnapshot
{
    private static final int[] PALETTE = createPalette(JagexColor.BRIGHTNESS_MIN);

    static JsonObject capture(Model model)
    {
        JsonObject snapshot = new JsonObject();
        JsonArray positions = new JsonArray();
        for (int i = 0; i < model.getVerticesCount(); i++)
        {
            positions.add((int) model.getVerticesX()[i]);
            positions.add((int) -model.getVerticesY()[i]);
            positions.add((int) -model.getVerticesZ()[i]);
        }
        JsonArray faces = new JsonArray();
        for (int i = 0; i < model.getFaceCount(); i++)
        {
            if (model.getFaceColors3()[i] == -2)
            {
                continue;
            }
            int texture = model.getFaceTextures() == null ? -1 : model.getFaceTextures()[i];
            Color color;
            if (texture != -1)
            {
                color = TextureColor.getColor(texture);
            }
            else if (model.getFaceColors3()[i] == -1)
            {
                color = new Color(PALETTE[model.getFaceColors1()[i]]);
            }
            else
            {
                color = JagexColor.HSLtoRGBAvg(model.getFaceColors1()[i],
                    model.getFaceColors2()[i], model.getFaceColors3()[i]);
            }
            faces.add(model.getFaceIndices1()[i]);
            faces.add(model.getFaceIndices2()[i]);
            faces.add(model.getFaceIndices3()[i]);
            faces.add(color.getRGB() & 0xffffff);
        }
        snapshot.add("positions", positions);
        snapshot.add("faces", faces);
        return snapshot;
    }
}
