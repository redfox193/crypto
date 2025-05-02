package com.cryptott

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.StringContains.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyDetailActivityTest {

    @Before
    fun setup() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), CurrencyDetailActivity::class.java)
            .putExtra("symbol", "BTC")
        ActivityScenario.launch<CurrencyDetailActivity>(intent)
    }

    @Test
    fun testToolbarDisplayed() {
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testCurrencyNameDisplayed() {
        onView(withId(R.id.currencyName))
            .check(matches(isDisplayed()))
            .check(matches(withText("BTC")))
    }

    @Test
    fun testCurrentPriceDisplayed() {
        onView(withId(R.id.currentPrice))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("$"))))
    }

    @Test
    fun testPeriodChipsDisplayed() {
        onView(withId(R.id.chip1h))
            .check(matches(isDisplayed()))
            .check(matches(withText("1H")))

        onView(withId(R.id.chip24h))
            .check(matches(isDisplayed()))
            .check(matches(withText("24H")))

        onView(withId(R.id.chip7d))
            .check(matches(isDisplayed()))
            .check(matches(withText("7D")))
    }

    @Test
    fun testPriceChartDisplayed() {
        onView(withId(R.id.priceChart))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testWalletBalancesDisplayed() {
        onView(withId(R.id.walletTradeBalance))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("$"))))

        onView(withId(R.id.walletCryptoBalance))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testBuySectionFunctionality() {
        onView(withId(R.id.intputBuyValue))
            .perform(typeText("100"), closeSoftKeyboard())

        onView(withId(R.id.buyHint))
            .check(matches(withText(containsString("BTC"))))

        onView(withId(R.id.buyButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())
    }

    @Test
    fun testSellSectionFunctionality() {
        onView(withId(R.id.inputSellValue))
            .perform(typeText("1"), closeSoftKeyboard())

        onView(withId(R.id.sellHint))
            .check(matches(withText(containsString("$"))))

        onView(withId(R.id.sellButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())
    }

    @Test
    fun testPeriodSelection() {
        onView(withId(R.id.chip1h))
            .perform(click())

        onView(withId(R.id.chip24h))
            .perform(click())

        onView(withId(R.id.chip7d))
            .perform(click())
    }
}