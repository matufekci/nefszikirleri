package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    // NOT: Sabit "com.example" YANLISTI. `namespace` com.example olsa da
    // gercek applicationId `com.aistudio.nefszikir.kdhrmq`; packageName onu
    // dondurur. Bu test hic cihazda kosmadigi icin hata fark edilmemisti;
    // emulator job'i eklenince kesin kirmizi olurdu. Artik BuildConfig'ten
    // okunuyor, yani ikisi de degisse test dogru kalir.
    assertEquals(com.example.BuildConfig.APPLICATION_ID, appContext.packageName)
  }
}
