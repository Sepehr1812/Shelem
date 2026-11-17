package com.shelem.scoresheet

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName == "Shelem!" || appName == "شلم!")
    }

    @Test
    fun `english and persian terminology matches trick-taking glossary`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()

        val enConfig = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }
        val enContext = baseContext.createConfigurationContext(enConfig)

        assertEquals("Set", enContext.getString(R.string.status_bid_failed))
        assertEquals("Set", enContext.getString(R.string.outcome_failed_short))
        assertEquals("Made", enContext.getString(R.string.status_bid_accomplished))
        assertEquals("Made", enContext.getString(R.string.outcome_accomplished_short))
        assertEquals("Double Set", enContext.getString(R.string.status_negative_shelem))
        assertEquals("Double Set", enContext.getString(R.string.outcome_neg_shelem_short))
        assertEquals("Declarer (Bidder)", enContext.getString(R.string.select_hakem_player))
        assertEquals("You (Scorekeeper)", enContext.getString(R.string.you_scorer_badge))
        assertEquals("View Scoresheet", enContext.getString(R.string.view_game_results))
        assertEquals("Hand 1", enContext.getString(R.string.round_number, "1"))

        val faConfig = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale("fa"))
        }
        val faContext = baseContext.createConfigurationContext(faConfig)

        assertEquals("خالی", faContext.getString(R.string.status_bid_failed))
        assertEquals("پُر", faContext.getString(R.string.status_bid_accomplished))
        assertEquals("دوبل منفی", faContext.getString(R.string.status_negative_shelem))
        assertEquals("پاسور (برگ‌دهنده)", faContext.getString(R.string.current_round_dealer_title))
        assertEquals("شما (امتیازشمار)", faContext.getString(R.string.you_scorer_badge))
        assertEquals("مشاهده برگه امتیاز", faContext.getString(R.string.view_game_results))
        assertEquals("مزایده و حاکم", faContext.getString(R.string.stat_tab_bidding))
    }

    @Test
    fun `bug report email intent has localized subject and app version in body`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val faIntent = com.shelem.scoresheet.ui.screens.buildBugReportEmailIntent(context, isPersian = true)
        val faSubject = faIntent.getStringExtra(android.content.Intent.EXTRA_SUBJECT) ?: ""
        val faBody = faIntent.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: ""
        assertEquals("گزارش خطا در اپلیکیشن شلم!", faSubject)
        assertTrue(faBody.contains("نسخه اپلیکیشن: ۱.۰"))

        val enIntent = com.shelem.scoresheet.ui.screens.buildBugReportEmailIntent(context, isPersian = false)
        val enSubject = enIntent.getStringExtra(android.content.Intent.EXTRA_SUBJECT) ?: ""
        val enBody = enIntent.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: ""
        assertEquals("Shelem! App - Bug Report", enSubject)
        assertTrue(enBody.contains("App Version: 1.0"))

        assertEquals(
            "https://github.com/Sepehr1812/Shelem",
            com.shelem.scoresheet.ui.screens.GITHUB_REPO_URL
        )
    }
}
