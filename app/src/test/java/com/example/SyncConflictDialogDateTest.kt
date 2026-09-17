package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.UiText
import com.example.ui.components.SyncConflictDialog
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Recomposition denetimi (Etap 12) sirasinda `SyncConflictDialog` icindeki tarih
 * metni `remember(remoteBackupTimestamp) { ... }` icine alindi; daha once her
 * recomposition'da SimpleDateFormat + metin yeniden uretiliyordu.
 *
 * Bu test o kod yolunu GERCEKTEN calistirir ve iki seyi kilitler:
 *  1) Ekranda gorunen metin degismedi (ayni desen, aynı locale).
 *  2) `remember` anahtari dogru: zaman damgasi degisince metin GUNCELLENMELI.
 *     Anahtar unutulsa (orn. `remember { }`) diyalog eski tarihi gosterirdi ve
 *     bu test kirmiziya donerdi.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class SyncConflictDialogDateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun formattedDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    private fun expectedMessage(timestamp: Long): String =
        UiText.cloudBackupFoundMessage.format("tr", formattedDate(timestamp))

    @Test
    fun `yedek zamani diyalogda dogru bicimde gorunur`() {
        val timestamp = 1_700_000_000_000L
        composeTestRule.setContent {
            SyncConflictDialog(
                lang = "tr",
                remoteBackupTimestamp = timestamp,
                onDismissRequest = {},
                onKeepLocal = {},
                onUseRemote = {},
                onMerge = {}
            )
        }
        composeTestRule.onNodeWithText(expectedMessage(timestamp)).assertExists()
    }

    @Test
    fun `zaman damgasi degisince metin guncellenir`() {
        val first = 1_700_000_000_000L
        val second = 1_800_000_000_000L
        var timestamp by mutableStateOf(first)

        composeTestRule.setContent {
            SyncConflictDialog(
                lang = "tr",
                remoteBackupTimestamp = timestamp,
                onDismissRequest = {},
                onKeepLocal = {},
                onUseRemote = {},
                onMerge = {}
            )
        }
        composeTestRule.onNodeWithText(expectedMessage(first)).assertExists()

        timestamp = second
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(expectedMessage(second)).assertExists()
        composeTestRule.onNodeWithText(expectedMessage(first)).assertDoesNotExist()
    }

    @Test
    fun `zaman damgasi yoksa tire gosterilir`() {
        composeTestRule.setContent {
            SyncConflictDialog(
                lang = "tr",
                remoteBackupTimestamp = 0L,
                onDismissRequest = {},
                onKeepLocal = {},
                onUseRemote = {},
                onMerge = {}
            )
        }
        composeTestRule
            .onNodeWithText(UiText.cloudBackupFoundMessage.format("tr", "-"))
            .assertExists()
    }
}
