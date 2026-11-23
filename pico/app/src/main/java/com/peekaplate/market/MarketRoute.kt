package com.peekaplate.market

internal data class MarketRoute(val model: String? = null, val portion: Boolean = false) {
    fun open(dishModel: String) = MarketRoute(dishModel)
    fun preview(loaded: Boolean) = copy(portion = model != null && loaded)
    fun back() = if (portion) copy(portion = false) else MarketRoute()
}
