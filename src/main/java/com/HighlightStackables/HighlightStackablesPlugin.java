package com.HighlightStackables;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;


import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;


import java.util.ArrayList;
import java.util.List;



@Slf4j
@PluginDescriptor(
		name = "Highlight Stackables"
)
@PluginDependency(GroundItemsPlugin.class)
public class HighlightStackablesPlugin extends Plugin
{
	private List<String> spawnedItems;



	@Inject
	private HighlightStackablesConfig config;

	@Provides
	HighlightStackablesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HighlightStackablesConfig.class);
	}

	@Inject
	private GroundItemsConfig groundItemsConfig;
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;




	@Override
	protected void startUp()
	{
		spawnedItems = new ArrayList<>();
		config.setOriginalItem(groundItemsConfig.getHighlightItems());

	}

	@Override
	protected void shutDown()
	{

	groundItemsConfig.setHighlightedItem(config.getOrginalItems());
	}
	@Subscribe
	public void onClientTick(ClientTick event)
	{

		if(spawnedItems.isEmpty() && groundItemsConfig.getHighlightItems() != config.getOrginalItems()){
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());

		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("highlightStackables"))
		{
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());

		}
	}


	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned) {
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final TileItem item = itemSpawned.getItem();
		final int id = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(id);



		if (itemComposition.isStackable()) {
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());

			String oldList = groundItemsConfig.getHighlightItems().toString();
			String exclusionList = groundItemsConfig.getHiddenItems().toString();




			if(config.inventoryStackable()) {
				if (!oldList.contains(itemComposition.getName()) && !exclusionList.contains(itemComposition.getName()) && inventory.contains(itemComposition.getId())) {
					spawnedItems.add(itemComposition.getName());


					String formatedString = spawnedItems.toString();
					formatedString = formatedString.substring(1, formatedString.length() - 1);

					groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formatedString);

				}
			}else
			{
				if (!oldList.contains(itemComposition.getName()) && !exclusionList.contains(itemComposition.getName())){
					spawnedItems.add(itemComposition.getName());


					String formatedString = spawnedItems.toString();
					formatedString = formatedString.substring(1, formatedString.length() - 1);


					groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formatedString);

				}
			}



		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned){
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final TileItem item = itemDespawned.getItem();
		final int id = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(id);

		spawnedItems.remove(itemComposition.getName());
		String formatedString = spawnedItems.toString();
		formatedString = formatedString.substring(1, formatedString.length() - 1);
		groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formatedString);

	}
}