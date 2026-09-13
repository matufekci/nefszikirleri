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
        // dogrudan zikirc (sayac) sekmesine iner.
        intent.getStringExtra("open_tab")?.let { viewModel.setTab(it) }
        enableEdgeToEdge()

        setContent {
            MainApp(viewModel = viewModel)
        }
    }
}

