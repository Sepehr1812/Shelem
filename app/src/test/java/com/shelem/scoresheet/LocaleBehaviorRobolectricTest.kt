package com.shelem.scoresheet

import android.content.Context
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.shelem.scoresheet.ui.screens.HomeScreen
import com.shelem.scoresheet.ui.theme.ShelemTheme
import com.shelem.scoresheet.ui.viewmodel.ShelemViewModel
import com.shelem.scoresheet.util.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], qualifiers = "en-rUS")
class LocaleBelowAndroid13RobolectricTest {

    @Test
    fun `below android 13 defaults to persian and disables language switch`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        assertFalse(LocaleHelper.isLanguageSwitchSupported())

        val localizedContext = LocaleHelper.applyLocaleToBaseContext(baseContext)
        LocaleHelper.ensureDefaultPersianLocale(localizedContext)

        assertEquals("fa", LocaleHelper.getCurrentLanguage(localizedContext))
        assertTrue(LocaleHelper.isPersian(localizedContext))
        assertEquals("fa", Locale.getDefault().language)
        assertEquals("شلم!", localizedContext.getString(R.string.app_name))
        assertEquals("بازی جدید", localizedContext.getString(R.string.new_game))

        // toggleLanguage is a no-op below Android 13
        LocaleHelper.toggleLanguage(localizedContext)
        assertEquals("fa", LocaleHelper.getCurrentLanguage(localizedContext))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "en-rUS")
class LocaleAndroid13PlusRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `android 13 plus defaults to persian and shows language switcher`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        assertTrue(LocaleHelper.isLanguageSwitchSupported())
        assertEquals("fa", LocaleHelper.getCurrentLanguage(context))
        assertTrue(LocaleHelper.isPersian(context))

        // Default Persian UI is shown on launch even on en-US device
        assertTrue(composeTestRule.onAllNodesWithText("شلم!").fetchSemanticsNodes().isNotEmpty())
        composeTestRule.onNodeWithText("بازی جدید").assertIsDisplayed()
        composeTestRule.onNodeWithText("بازی‌های ناتمام").assertIsDisplayed()

        // Open navigation drawer
        composeTestRule.onNodeWithTag("open_drawer_button").performClick()
        composeTestRule.waitForIdle()

        // Both theme toggle and language toggle are displayed
        composeTestRule.onNodeWithTag("theme_toggle_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("language_toggle_button").assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "en-rUS")
class DrawerLanguageHiddenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `home drawer hides language toggle when language switch is unsupported`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = ShelemViewModel(app)

        composeTestRule.setContent {
            ShelemTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSetup = {},
                    onNavigateToBoard = {},
                    showLanguageSwitch = false
                )
            }
        }

        composeTestRule.onNodeWithTag("open_drawer_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("theme_toggle_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("language_toggle_button").assertDoesNotExist()
    }
}
