package com.wiseoldman.companion;

/** A positive observed XP change is an estimate unit, not a named game action. */
final class SkillXpProgress
{
    int xp;
    int lastGain;
    String gainAt;

    SkillXpProgress(int xp)
    {
        this.xp = xp;
    }

    void observe(int value, String now)
    {
        if (value > xp)
        {
            lastGain = value - xp;
            gainAt = now;
        }
        else if (value < xp)
        {
            lastGain = 0;
            gainAt = null;
        }
        xp = value;
    }
}
