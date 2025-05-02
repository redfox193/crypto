package com.cryptott
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoRatesActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CryptoRatesActivity::class.java)

    @Test
    fun testWalletBalanceDisplayed() {
        // Проверяем, что баланс кошелька отображается
        onView(withId(R.id.walletBalance))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("$"))))
    }

    @Test
    fun testAddMoneyButton() {
        // Проверяем, что кнопка добавления денег отображается и кликабельна
        onView(withId(R.id.addMoneyButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .perform(click())

        // После клика проверяем, что баланс обновился (начальное значение + 0.01)
        onView(withId(R.id.walletBalance))
            .check(matches(withText(containsString("0.01"))))
    }

    @Test
    fun testCryptoRatesListDisplayed() {
        // Проверяем, что список криптовалют отображается
        onView(withId(R.id.cryptoRatesRecyclerView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testCryptoRatesTitleDisplayed() {
        // Проверяем, что заголовок списка криптовалют отображается
        onView(withId(R.id.cryptoRatesTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Cryptocurrency Rates")))
    }

    @Test
    fun testWalletCardDisplayed() {
        // Проверяем, что карточка с балансом отображается
        onView(withId(R.id.walletCard))
            .check(matches(isDisplayed()))
    }
}