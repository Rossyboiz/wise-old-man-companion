package com.wiseoldman.companion;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CompanionDevLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CompanionSyncPlugin.class);
		RuneLite.main(args);
	}
}