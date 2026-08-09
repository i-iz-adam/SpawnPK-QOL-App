package com.spk.app.ui.nav

object Routes {
    const val SPLASH = "splash"
    const val SEARCH = "search"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val ITEM_DETAIL = "item/{itemName}"
    const val FAQ = "faq"
    const val DONATE = "donate"

    fun itemDetail(itemName: String): String {
        val encoded = java.net.URLEncoder.encode(itemName, "UTF-8")
        return "item/$encoded"
    }
}
