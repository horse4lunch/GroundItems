package com.HighlightStackables;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.plugins.grounditems.GroundItemsOverlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@PluginDescriptor(
		name = "Highlight Stackables"
)
@PluginDependency(GroundItemsPlugin.class)
public class HighlightStackablesPlugin extends Plugin {
	private List<String> spawnedItems;
	private List<String> spawnedItemsUnique;
	private Set<String> uniqueSet;
	private String formattedString;
	@Inject
	private HighlightStackablesConfig config;

	@Provides
	HighlightStackablesConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(HighlightStackablesConfig.class);
	}

	@Inject
	private GroundItemsPlugin groundItemsPlugin;

	@Inject
	private GroundItemsConfig groundItemsConfig;

	@Inject
	private GroundItemsOverlay groundItemsOverlay;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp() {
		spawnedItems = new ArrayList<>();
		spawnedItemsUnique = new ArrayList<>();
		uniqueSet = new HashSet<>();

		// Store the original highlighted items from GroundItemsConfig
		config.setOriginalItem(groundItemsConfig.getHighlightItems());
	}

	@Override
	protected void shutDown() {
		// Restore the original highlighted items to GroundItemsConfig
		groundItemsConfig.setHighlightedItem(config.getOrginalItems());
	}
	@Subscribe
	public void onClientShutdown(ClientShutdown event) {
		// Restore the original highlighted items to GroundItemsConfig
		groundItemsConfig.setHighlightedItem(config.getOrginalItems());
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event) {
		// Clear item lists if you leave the area
		if (event.getGameState() == GameState.LOADING) {
			spawnedItems.clear();
			spawnedItemsUnique.clear();
		}
	}
	@Subscribe
	public void onGameTick(GameTick event) {
		// Check if spawnedItems is empty and if Highlighted items from GroundItemsConfig and my config don't match
		if (spawnedItems.isEmpty() && !groundItemsConfig.getHighlightItems().equals(config.getOrginalItems())) {
			// Restore the original highlighted items
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());
		}
	}
	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("highlightStackables")) {
			// Update the highlighted items in GroundItemsConfig
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());
		}
	}

	private void sortItems() {
		// Copy spawnedItems to spawnedItemsUnique removing all duplicates.
		for (String element : spawnedItems) {
			boolean alreadyExists = false;
			for (String uniqueElement : spawnedItemsUnique) {
				if (element.contains(uniqueElement)) {
					alreadyExists = true;
					break;
				}
			}
			if (!alreadyExists) {
				spawnedItemsUnique.add(element);
			}
		}
	}

	private void formatString() {
		// Convert List to String, Remove [ and ]
		formattedString = spawnedItemsUnique.toString();
		formattedString = formattedString.substring(1, formattedString.length() - 1);
		groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formattedString);
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned) {
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final TileItem item = itemSpawned.getItem();
		final int id = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(id);
		String exclusionList = groundItemsConfig.getHiddenItems().toString();

		if (itemComposition.isStackable() && !exclusionList.contains(itemComposition.getName())) {
			if (config.inventoryStackable()) {
				if (inventory.contains(itemComposition.getId())) {
					spawnedItems.add(itemComposition.getName());
					groundItemsConfig.setHighlightedItem(config.getOrginalItems());

					sortItems();

					formatString();
				}
			} else {
				spawnedItems.add(itemComposition.getName());
				groundItemsConfig.setHighlightedItem(config.getOrginalItems());

				sortItems();

				formatString();
			}
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned) {
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final TileItem item = itemDespawned.getItem();
		final int id = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(id);

		spawnedItems.remove(itemComposition.getName());

		//Iterate over the spawnedItemsUnique list and remove any element that is not present in the spawnedItems list
		for(String element : spawnedItemsUnique){
			if(!spawnedItems.contains(element)){
				spawnedItemsUnique.remove(element);
				break;
			}
		}

		sortItems();

		formatString();
	}
}
