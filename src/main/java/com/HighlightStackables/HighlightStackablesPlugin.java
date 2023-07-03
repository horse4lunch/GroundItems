package com.HighlightStackables;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@PluginDescriptor(
		name = "Highlight Stackables"
)
@PluginDependency(GroundItemsPlugin.class)
public class HighlightStackablesPlugin extends Plugin
{
	private List<String> spawnedItems;

	private List<String> spawnedItemsUnique;

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
		spawnedItemsUnique = new ArrayList<>();
		config.setOriginalItem(groundItemsConfig.getHighlightItems());

	}

	@Override
	protected void shutDown()
	{

	groundItemsConfig.setHighlightedItem(config.getOrginalItems());
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			spawnedItems.clear();

		}
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
		String exclusionList = groundItemsConfig.getHiddenItems().toString();


		if (itemComposition.isStackable()&& !exclusionList.contains(itemComposition.getName()) ) {


			String oldList = groundItemsConfig.getHighlightItems().toString();




			if(config.inventoryStackable()) {
				if (!oldList.contains(itemComposition.getName()) && inventory.contains(itemComposition.getId())) {
					spawnedItems.add(itemComposition.getName());
					groundItemsConfig.setHighlightedItem(config.getOrginalItems());

					HashSet<String> uniqueSet = new HashSet<>();

					// Create a new ArrayList to store unique elements
					ArrayList<String> spawnedItemsUnique = new ArrayList<>();

					for (String element : spawnedItems) {
						// Add the element to the uniqueSet if it doesn't exist already
						if (uniqueSet.add(element)) {
							// If the element is successfully added to the uniqueSet,
							// add it to the spawnedItemsUnique list as well
							spawnedItemsUnique.add(element);
						}
					}




					String formatedString = spawnedItemsUnique.toString();
					formatedString = formatedString.substring(1, formatedString.length() - 1);

					groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formatedString);

				}
			}else
			{
				if (!oldList.contains(itemComposition.getName()) && !exclusionList.contains(itemComposition.getName())){
					spawnedItems.add(itemComposition.getName());
					groundItemsConfig.setHighlightedItem(config.getOrginalItems());



					HashSet<String> uniqueSet = new HashSet<>();


					ArrayList<String> spawnedItemsUnique = new ArrayList<>();

					for (String element : spawnedItems) {

						if (uniqueSet.add(element)) {

							spawnedItemsUnique.add(element);
						}
					}


					String formatedString = spawnedItemsUnique.toString();
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

		HashSet<String> uniqueSet = new HashSet<>();

		// Create a new ArrayList to store unique elements
		ArrayList<String> spawnedItemsUnique = new ArrayList<>();

		for (String element : spawnedItems) {

			if (uniqueSet.add(element)) {

				spawnedItemsUnique.add(element);
			}
		}
		String formatedString = spawnedItemsUnique.toString();
		formatedString = formatedString.substring(1, formatedString.length() - 1);
		groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formatedString);

	}
}