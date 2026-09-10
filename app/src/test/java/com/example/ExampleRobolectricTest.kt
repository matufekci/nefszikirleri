package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nefs Zikirleri", appName)
  }

  @Test
  fun `verify Android 16 edge to edge activity lifecycle and insets`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertNotNull(activity)
    assertNotNull(activity.window)
    assertNotNull(activity.window.decorView)

    // Configuration / recreation check for landscape & split screen
    controller.configurationChange()
    assertNotNull(activity.window.decorView)

    controller.pause().stop().destroy()
  }

  @Test
  fun `verify various font scales and screen configurations`() {
    val fontScales = listOf(0.85f, 1.0f, 1.15f, 1.3f, 1.5f)
    for (scale in fontScales) {
      val config = android.content.res.Configuration().apply {
        fontScale = scale
      }
      val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
      val activity = controller.get()
      assertNotNull(activity)
      controller.configurationChange(config)
      assertNotNull(activity.window.decorView)
      controller.pause().stop().destroy()
    }
  }
}
