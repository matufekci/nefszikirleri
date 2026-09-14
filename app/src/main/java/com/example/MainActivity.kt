package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainApp
import com.example.ui.viewmodel.ZikirViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ZikirViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bildirim tap UX'i: hatirlatma bildirimine dokunan kullanici
        // dogrudan zikir (sayac) sekmesine iner. Uc bildirim kaynagi da
        // "open_tab" extra'sini gonderiyor (NotificationDeepLinkConsistencyTest
        // bu sozlesmeyi kilitliyor). MainActivity'nin launchMode'u "standard"
        // ve bildirimler FLAG_ACTIVITY_CLEAR_TASK ile geliyor, dolayisiyla
        // dokunusta yeni instance olusur ve bu satir her zaman calisir.
        intent.getStringExtra("open_tab")?.let { viewModel.setTab(it) }
        enableEdgeToEdge()

        setContent {
            MainApp(viewModel = viewModel)
        }
    }
}

