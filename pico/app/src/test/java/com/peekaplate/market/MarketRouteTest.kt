package com.peekaplate.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketRouteTest {
    @Test fun opensEachPortionAndReturnsToMarket() {
        for (model in listOf("noka-steak-ramen.glb", "thai-chicken-bowl.glb")) {
            val market = MarketRoute()
            val dish = market.open(model)
            val preview = dish.preview(loaded = true)
            assertTrue(preview.portion)
            assertEquals(model, preview.model)
            assertEquals(dish, preview.back())
            assertEquals(market, dish.back())
        }
    }

    @Test fun missingSelectionOrModelKeepsPreviewClosed() {
        assertFalse(MarketRoute().preview(loaded = true).portion)
        assertFalse(MarketRoute().open("missing.glb").preview(loaded = false).portion)
    }
}
