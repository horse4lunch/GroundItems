package com.example;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup("highlightStackables")
public interface HighlightStackablesConfig extends Config
{
    @ConfigSection(
            name = "Item Lists",
            description = "The highlighted and hidden item lists",
            position = 0
    )
    String itemLists = "itemLists";

    @ConfigItem(
            keyName = "highlightedItems",
            name = "Highlighted Items",
            description = "<html>Configures specifically highlighted ground items. Format: (item), (item)<br>Use this for manually highlighting items rather than the highlighted items list built into ground items.<html>",
            position = 0,
            section = itemLists
    )
    default String getOrginalItems()
    {
        return "";
    }

    @ConfigItem(
            keyName = "highlightedItems",
            name = "",
            description = ""
    )
    void setOriginalItem(String key);

    @ConfigItem(
            keyName = "InventoryStackable",
            name = "Highlight stackables in inventory",
            description = "<html>Highlight stackable ground items already in inventory.<br>If this is disabled, all stackable items will be highlighted.<br>Unless they are on the hidden item list.</html>",

            position = 32
    )
    default boolean inventoryStackable()
    {
        return false;
    }

}