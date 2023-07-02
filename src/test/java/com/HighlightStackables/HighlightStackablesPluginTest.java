package com.HighlightStackables;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HighlightStackablesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HighlightStackablesPlugin.class);
		RuneLite.main(args);
	}
}