package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel

/**
 * İstatistikler ve Manevi Gelişim Ana Ekranı (Orchestrator Composable)
 */
@Composable
fun StatisticsScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    modifier: Modifier = Modifier
) {
    var chartRange by rememberSaveable { mutableStateOf(0) } // 0: 7 Gün, 1: 30 Gün, 2: 6 Ay
    var chartExpanded by rememberSaveable { mutableStateOf(false) }
    var heatmapExpanded by rememberSaveable { mutableStateOf(false) }
    var badgesExpanded by rememberSaveable { mutableStateOf(false) }
    var logsExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. KPI METRİK KARTLARI (2x2 Grid)
        // Satır 1: Toplam Zikir & Seri (Streak)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TotalRecitedCard(
                totalDone = state.totalDone,
                lang = state.settings.lang,
                modifier = Modifier.weight(1f)
            )

            StreakSection(
                streak = state.streak,
                bestStreak = state.bestStreak,
                lang = state.settings.lang,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Satır 2: Genel Günlük Hız & Terkip Bitiş Projeksiyonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverallSpeedCard(
                overallAveragePerDay = state.overallAveragePerDay,
                overallRemaining = state.overallRemaining,
                lang = state.settings.lang,
                modifier = Modifier.weight(1f)
            )

            CompletionProjectionCard(
                overallEstimatedDate = state.overallEstimatedDate,
                completedCount = state.completedCount,
                lang = state.settings.lang,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. İNTERAKTİF ÇOK DÖNEMLİ GRAFİK (7 Gün / 30 Gün / 6 Ay)
        WeeklyChartSection(
            state = state,
            chartRange = chartRange,
            isExpanded = chartExpanded,
            onToggle = { chartExpanded = !chartExpanded },
            onRangeChange = { chartRange = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. GÜNLÜK GAYRET ISI HARİTASI (30-Day Activity Heatmap)
        MonthlyChartSection(
            last30Days = state.last30Days,
            lang = state.settings.lang,
            isExpanded = heatmapExpanded,
            onToggle = { heatmapExpanded = !heatmapExpanded }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. MANEVİ İSTİKRAR ROZETLERİ, GEÇMİŞ LOGLARI VE SIFIRLAMA
        OverallStatsSection(
            state = state,
            badgesExpanded = badgesExpanded,
            logsExpanded = logsExpanded,
            onToggleBadges = { badgesExpanded = !badgesExpanded },
            onToggleLogs = { logsExpanded = !logsExpanded },
            onResetAllZikirs = { viewModel.resetAllZikirs() }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}
