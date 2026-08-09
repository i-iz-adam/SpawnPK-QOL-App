package com.spk.app

/**
 * Central place for the few things you're likely to want to change.
 */
object AppConfig {


    const val ITEMS_JSON_RAW_URL =
        "https://raw.githubusercontent.com/i-iz-adam/SpawnPk-QOL-App/refs/heads/main/app/src/main/assets/items.json"

    /** How often the background worker checks your watchlist for new sales. */
    const val BACKGROUND_CHECK_INTERVAL_MINUTES = 15L

    /** Prefix in items.json that marks an entry as a real, searchable item. */
    const val VALID_ITEM_PREFIX = "@gre@"

    /**
     * Shown on the Support/Donate page.
     */
    const val DONATE_DISCORD_USERNAME = "dev_wizard._60822"

    /**
     * Optional: a Discord invite link (e.g. "https://discord.gg/xxxxxxx") if you want an
     * "Open Discord" button as well as the copy-username option. Leave blank to hide that button.
     */
    const val DONATE_DISCORD_INVITE_URL = "https://discord.gg/NPEQbSkk9g"
}
