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
            description = "Configures specifically highlighted ground items. Format: (item), (item)",
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

}