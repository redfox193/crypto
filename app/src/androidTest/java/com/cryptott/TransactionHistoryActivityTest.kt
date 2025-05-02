package com.cryptott

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionHistoryActivityTest {

    private val testSymbol = "BTC"

    @get:Rule
    val activityRule = ActivityScenarioRule<TransactionHistoryActivity>(
        Intent(ApplicationProvider.getApplicationContext(), TransactionHistoryActivity::class.java)
            .putExtra("symbol", testSymbol)
    )

    @Test
    fun testToolbarDisplayed() {
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testCurrencyHeaderDisplayed() {
        onView(withId(R.id.tvCurrency))
            .check(matches(isDisplayed()))
            .check(matches(withText(testSymbol)))
    }

    @Test
    fun testStatsDisplayed() {
        onView(withId(R.id.tvBought))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Bought:"))))

        onView(withId(R.id.tvSold))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Sold:"))))

        onView(withId(R.id.tvProfit))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Profit:"))))
    }

    @Test
    fun testRecyclerViewDisplayed() {
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }
}