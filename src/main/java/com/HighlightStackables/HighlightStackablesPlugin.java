package com.HighlightStackables;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
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
	private Item[] previousInventory = new Item[28];
	private List<String> spawnedItems;
	private List<String> spawnedItemsUnique;
	private List<String> removedItems;
	private Set<String> uniqueSet;
	private String formattedString;

	@Provides
	HighlightStackablesConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(HighlightStackablesConfig.class);
	}

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean hotKeyPressed;

	@Inject
	private HighlightStackablesConfig config;
	@Inject
	private HighlightStackablesHotkeyListener hotkeyListener;
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
	@Inject
	private KeyManager keyManager;

	@Override
	protected void startUp() {
		spawnedItems = new ArrayList<>();
		spawnedItemsUnique = new ArrayList<>();
		removedItems = new ArrayList<>();
		uniqueSet = new HashSet<>();
		keyManager.registerKeyListener(hotkeyListener);
		// Store the original highlighted items from GroundItemsConfig
		config.setOriginalItem(groundItemsConfig.getHighlightItems());
	}
	@Override
	protected void shutDown() {
		// Restore the original highlighted items to GroundItemsConfig
		groundItemsConfig.setHighlightedItem(config.getOrginalItems());
		keyManager.unregisterKeyListener(hotkeyListener);
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
		if (hotKeyPressed) {
			return; // hotKeyPressed is true, skip the rest of the function
		}
		// Check if spawnedItems is empty and if Highlighted items from GroundItemsConfig and my config don't match
		if (spawnedItems.isEmpty() && !groundItemsConfig.getHighlightItems().equals(config.getOrginalItems())) {

			// Restore the original highlighted items
			groundItemsConfig.setHighlightedItem(config.getOrginalItems());
		}
		//check if spawned items isnt empty
		if (!spawnedItems.isEmpty()) {
			formatString();

			// Create the string that should be in groundItems Config
			String finishedString = new String();
			finishedString = config.getOrginalItems() + "," + formattedString;

			//Check if finished string = groundItems Config
			if(finishedString != groundItemsConfig.getHighlightItems()){

				//Set groundItems Config
				sortItems();
				groundItemsConfig.setHighlightedItem(config.getOrginalItems() + "," + formattedString);
			}
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
			if (config.inventoryMode()) {
				if (inventory != null && inventory.contains(itemComposition.getId())) {
					spawnedItems.add(itemComposition.getName());

					//Check if hotkey is pressed after adding item to spawned item list, But before sort/format
					if (hotKeyPressed) {
						return; // hotKeyPressed is true, skip the rest of the function
					}
					groundItemsConfig.setHighlightedItem(config.getOrginalItems());
					if(removedItems.contains(itemComposition.getName())){
						for(String element : removedItems){
							if(element.equals((itemComposition.getName()))){
								spawnedItems.add(element);
							}
						}
					}
					sortItems();
					formatString();
				}
			} else {
				spawnedItems.add(itemComposition.getName());

				//Check if hotkey is pressed after adding item to spawned item list, But before sort/format
				if (hotKeyPressed) {
					return; // hotKeyPressed is true, skip the rest of the function
				}
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
		removedItems.remove(itemComposition.getName());

		//Iterate over the spawnedItemsUnique list and remove any element that is not present in the spawnedItems list
		spawnedItemsUnique.removeIf(element -> !spawnedItems.contains(element));

		//Check if hotkey is pressed after removing item from spawned item list, But before sort/format
		if (hotKeyPressed) {
			return; // hotKeyPressed is true, skip the rest of the function
		}
		sortItems();
		formatString();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (event.getItemContainer() == inventory) {
			final Item[] currentItems = event.getItemContainer().getItems();

			//compare the players current inventory to the previous inventory.
			for (int i = 0; i < currentItems.length; i++) {
				Item currentItem = currentItems[i];
				Item previousItem = previousInventory[i];

				// Check if the item was removed from the inventory
				if (previousItem != null && currentItem.getId() == -1 && previousItem.getId() != currentItem.getId()) {
					ItemComposition removedItemComposition = itemManager.getItemComposition(previousItem.getId());

					// An item was dropped by the player
					if (inventory != null && !inventory.contains(removedItemComposition.getId())) {

						//Add item to removedItems List
						for (String item : spawnedItems) {
							if (item.equals(removedItemComposition.getName())) {
								removedItems.add(item);
							}
						}
						//remove all cases of dropped item from spawnedItems list
						spawnedItems.removeIf(item -> item.equals(removedItemComposition.getName()));

						// Iterate over the spawnedItemsUnique list and remove any element that is not present in the spawnedItems list
						spawnedItemsUnique.removeIf(element -> !spawnedItems.contains(element));
						sortItems();
						formatString();
					}
				}
			}
			previousInventory = currentItems.clone(); // Update the previous inventory state
		}
	}

}