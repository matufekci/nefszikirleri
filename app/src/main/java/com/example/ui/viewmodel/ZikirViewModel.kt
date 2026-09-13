package com.example.ui.viewmodel

import com.example.ui.UiText

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.cloud.AuthManager
import com.example.data.cloud.SyncManager
import com.google.firebase.auth.FirebaseUser
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.data.model.Badge
import com.example.data.model.BadgeManager
import com.example.data.model.DailyAggregate
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.data.repository.ZikirRepository
import com.example.util.ChildLockPrefs
import com.example.util.HapticHelper
import com.example.util.NotificationScheduler
import com.example.util.MonotonicTime
import com.example.util.NumberFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayChartItem(
    val dateKey: String,
    val label: String,
    val amount: Long,
    val ratio: Float
)

data class MonthChartItem(
    val monthIndex: Int,
    val label: String,
    val amount: Long,
    val ratio: Float
)

data class CelebrationData(
    val zikirId: Int,
    val zikirName: String,
    val nextZikirId: Int?
)

data class UndoSnapshot(
    val zikirId: Int,
    val previousCount: Long,
    val previousStartedAt: Long?,
    val previousCompletedAt: Long?,
    val historyId: Long?
)

data class SequenceWarningData(
    val attemptedZikirId: Int,
    val requiredZikirId: Int
)

data class DhikrUiState(
    val zikirs: List<Zikir> = ZikirContent.INITIAL_DEFINITIONS.map { Zikir(id = it.id, target = it.defaultTarget, count = 0L) },
    val selectedId: Int = 1,
    val history: List<ZikirHistory> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val tab: String = "zikir",
    val todayRecited: Long = 0L,
    val todayPercent: Float = 0f,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val last7Days: List<DayChartItem> = emptyList(),
    val last30Days: List<DayChartItem> = emptyList(),
    val last6Months: List<MonthChartItem> = emptyList(),
    val totalDone: Long = 0L,
    val completedCount: Int = 0,
    val overallRemaining: Long = 0L,
    val overallAveragePerDay: Long = 0L,
    val overallEstimatedDate: Long? = null,
    val currentZikir: Zikir? = Zikir(id = 1, target = 100000L, count = 0L),
    val currentAveragePerDay: Long = 0L,
    val currentEstimatedDate: Long? = null,
    val canUndo: Boolean = false,
    val infoModalZikirId: Int? = null,
    val celebrationData: CelebrationData? = null,
    val badgeCelebrationData: Badge? = null,
    val badges: List<Badge> = emptyList(),
    val sequenceWarning: SequenceWarningData? = null,
    val fastJumpDialogZikirId: Int? = null,
    val showRoundModal: Boolean = false,
    val isSidebarOpen: Boolean = false,
    val isZenMode: Boolean = false,
    val isHydrated: Boolean = false
)

class ZikirViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private companion object {
        const val KEY_FAST_JUMP_COMPLETED_OFFSET = "fast_jump_completed_offset"
        const val KEY_FAST_JUMP_TOTAL_OFFSET = "fast_jump_total_offset"
    }

    /** Çocuk kilidi: kilit açıkken sayaç eylemleri VM düzeyinde yok sayılır. */
    fun isChildLocked(): Boolean = ChildLockPrefs.isEnabled(getApplication())

    private val settingsMutex = Mutex()

    private suspend fun updateSettingsSafely(modifier: (AppSettings) -> AppSettings) {
        settingsMutex.withLock {
            val current = repository.getSettingsDirect() ?: AppSettings()
            val updated = modifier(current)
            repository.updateSettings(updated)
            try {
                val prefs = getApplication<Application>().getSharedPreferences("nefs_app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("lang", updated.lang).apply()
            } catch (_: Exception) {}
        }
    }


    private val repository: ZikirRepository
    private val hapticHelper = HapticHelper(application)
    private val notificationScheduler = NotificationScheduler(application)

    val authManager = AuthManager(application)
    val syncManager = SyncManager()
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _cloudSyncMessage = MutableStateFlow<String?>(null)
    val cloudSyncMessage: StateFlow<String?> = _cloudSyncMessage.asStateFlow()

    private val _lastCloudSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastCloudSyncTimestamp: StateFlow<Long?> = _lastCloudSyncTimestamp.asStateFlow()

    // +1 islemleri artik SENKRON (repository.addDhikrCount) yazilir; toplu
    // kanal + optimistic katman kaldirildi (titremenin kaynagiydi).

    /**
     * Kullanıcının yaptığı son açık seçim (liste -> "Öncekileri Tamamla ve Buradan
     * Başla" ya da kilitli olmayan bir basamağa dokunma). Room akışları hedefle
     * tutarlı hale gelene kadar bu basamağın ilk eksik basamağa düşürülmesini
     * engeller. Bkz. [SelectedZikirResolver].
     */
    @Volatile
    private var pendingSelection: SelectedZikirResolver.Pending? = null

    /**
     * Hızlı intikal ("Öncekileri Tamamla") ile otomatik tamamlanan basamaklar rozet
     * kazandırmamalı. Bu prefs, atlama sırasında eklenen basamak/zikir sayısını biriktirir
     * ve rozet değerlendirmesi gerçek (sırayla) ilerlemeye göre yapılır.
     */
    private val badgeProgressPrefs by lazy {
        getApplication<Application>().getSharedPreferences("badge_progress_prefs", Context.MODE_PRIVATE)
    }

    private fun addFastJumpBadgeOffset(completedZikirs: Int, addedCount: Long) {
        if (completedZikirs <= 0 && addedCount <= 0L) return
        badgeProgressPrefs.edit {
            putInt(
                KEY_FAST_JUMP_COMPLETED_OFFSET,
                badgeProgressPrefs.getInt(KEY_FAST_JUMP_COMPLETED_OFFSET, 0) + completedZikirs
            )
            putLong(
                KEY_FAST_JUMP_TOTAL_OFFSET,
                badgeProgressPrefs.getLong(KEY_FAST_JUMP_TOTAL_OFFSET, 0L) + addedCount
            )
        }
    }

    private val _uiState: MutableStateFlow<DhikrUiState>
    val uiState: StateFlow<DhikrUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ZikirRepository(db)

        val initialTab = savedStateHandle.get<String>("currentTab") ?: "zikir"
        val initialSelectedId = savedStateHandle.get<Int>("selectedZikirId") ?: 1
        _uiState = MutableStateFlow(DhikrUiState(selectedId = initialSelectedId, tab = initialTab))
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            repository.processUnappliedOperations() // eski surumden kalan pending op'lar
            repository.ensureInitialized()

            val sixMonthsAgo = System.currentTimeMillis() - (185L * 24 * 60 * 60 * 1000L)
            val statsFlow = combine(
                repository.recentHistory,
                repository.observeDailyStats(sixMonthsAgo),
                repository.distinctActiveDates
            ) { recent, daily, dates ->
                Triple(recent, daily, dates)
            }

            combine(
                repository.allZikirs,
                repository.settings,
                statsFlow
            ) { dbZikirs, settingsObj, (recentHistory, dailyStats, distinctActiveDates) ->
                val settings = settingsObj ?: AppSettings()
                val zikirs = if (dbZikirs.isNotEmpty()) dbZikirs else _uiState.value.zikirs
                // Tek kaynak settings.selectedZikirId. savedStateHandle echo'su
                // restore sonrasi ilk emission'da 1 yazip secimi ilk zikire
                // kilitliyordu; bu golgeleme kaldirildi.
                val savedId: Int? = null

                // Terkib-i Şerif tertip emniyeti + hızlı intikal yarışının çözümü:
                // kullanıcı açık bir seçim yaptıysa (ör. "Öncekileri Tamamla ve Buradan
                // Başla"), Room'un eski zikir listesiyle gelen ara emission'ı o seçimi
                // ilk eksik basamağa düşürmesin. Bkz. SelectedZikirResolver.
                val resolution = SelectedZikirResolver.resolve(
                    savedId = savedId,
                    settingsSelectedId = settings.selectedZikirId,
                    zikirs = zikirs,
                    pending = pendingSelection,
                    nowMs = MonotonicTime.now()
                )
                val selectedId = resolution.selectedId
                if (resolution.clearPending) {
                    pendingSelection = null
                }

                val savedTab = savedStateHandle.get<String>("currentTab") ?: _uiState.value.tab
                savedStateHandle["currentTab"] = savedTab
                val currentZikir = zikirs.find { it.id == selectedId } ?: zikirs.firstOrNull() ?: Zikir(1, 100000L, 0L)

                // Günlük agregasyon haritası (SQL tarafından gruplanmış)
                val dailyMap = dailyStats.associate { it.dateKey to it.total }

                // Calculate today's recited for selected zikir
                val todayKey = NumberFormatter.getDateKey()
                val todayRecited = (dailyMap[todayKey] ?: 0L).coerceAtLeast(0L)
                val targetDaily = settings.dailyTarget.coerceAtLeast(1000L)
                val todayPercent = ((todayRecited.toFloat() / targetDaily.toFloat()) * 100f).coerceIn(0f, 100f)

                // Total and completed
                val totalDone = zikirs.sumOf { it.count }
                val completedCount = zikirs.count { it.count >= it.target }
                val overallRemaining = zikirs.sumOf { (it.target - it.count).coerceAtLeast(0L) }

                // Overall average & estimation (Eski tüm süreç ortalaması - geriye dönük uyumluluk için saklandı)
                val overallEarliestStarted = zikirs.mapNotNull { it.startedAt }.minOrNull()
                val overallDaysPassed = if (overallEarliestStarted != null && overallEarliestStarted > 0) {
                    val days = ((System.currentTimeMillis() - overallEarliestStarted) / (1000 * 60 * 60 * 24L)) + 1
                    days.coerceAtLeast(1L)
                } else 0L
                val overallAvg = if (overallDaysPassed > 0) totalDone / overallDaysPassed else 0L

                // Current zikir average & estimation (Eski tüm süreç ortalaması - geriye dönük uyumluluk için saklandı)
                val currentStarted = currentZikir.startedAt
                val currentDaysPassed = if (currentStarted != null && currentStarted > 0) {
                    val days = ((System.currentTimeMillis() - currentStarted) / (1000 * 60 * 60 * 24L)) + 1
                    days.coerceAtLeast(1L)
                } else 0L
                val currentAvg = if (currentDaysPassed > 0) currentZikir.count / currentDaysPassed else 0L
                val currentRemaining = (currentZikir.target - currentZikir.count).coerceAtLeast(0L)

                // Streaks calculation (Tüm history yerine SQL distinct dateKey listesi üzerinden)
                val activeDays = distinctActiveDates.toSet()
                var streakCount = 0
                val cal = Calendar.getInstance()
                while (activeDays.contains(NumberFormatter.getDateKey(cal.time))) {
                    streakCount++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                // Best streak calculation from distinct active dates
                var bestStreak = streakCount
                if (distinctActiveDates.isNotEmpty()) {
                    val sortedDates = distinctActiveDates.sorted()
                    var currentRun = 1
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    for (i in 1 until sortedDates.size) {
                        try {
                            val d1 = sdf.parse(sortedDates[i - 1])
                            val d2 = sdf.parse(sortedDates[i])
                            if (d1 != null && d2 != null) {
                                val diffDays = ((d2.time - d1.time) / (1000 * 60 * 60 * 24L))
                                if (diffDays == 1L) {
                                    currentRun++
                                    if (currentRun > bestStreak) bestStreak = currentRun
                                } else {
                                    currentRun = 1
                                }
                            }
                        } catch (e: Exception) {
                            currentRun = 1
                        }
                    }
                }

                // 7 Days Chart (SQL günlük toplam haritasından)
                val sevenDaysList = mutableListOf<DayChartItem>()
                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val k = NumberFormatter.getDateKey(c.time)
                    val label = NumberFormatter.getDayLabel(c.time, settings.lang)
                    val amt = (dailyMap[k] ?: 0L).coerceAtLeast(0L)
                    sevenDaysList.add(DayChartItem(k, label, amt, 0f))
                }
                val max7 = sevenDaysList.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
                val sevenDaysWithRatio = sevenDaysList.map { it.copy(ratio = (it.amount.toFloat() / max7.toFloat()).coerceIn(0.04f, 1f)) }

                // Son 7 güne ait kayıtların toplamını 7'ye bölerek yeni 7 günlük ortalama hesaplaması
                val overall7DayAvg = sevenDaysList.sumOf { it.amount } / 7L
                val current7DayAvg = if (overall7DayAvg > 0) overall7DayAvg else 0L

                // Son 7 günlük ortalamaya dayalı tahmini bitiş süreleri
                val overallEstDays = if (overall7DayAvg > 0) (overallRemaining + overall7DayAvg - 1) / overall7DayAvg else 0L
                val overallEstDate = if (overallEstDays > 0) System.currentTimeMillis() + (overallEstDays * 24 * 60 * 60 * 1000L) else null

                val currentEstDays = if (current7DayAvg > 0) (currentRemaining + current7DayAvg - 1) / current7DayAvg else 0L
                val currentEstDate = if (currentEstDays > 0) System.currentTimeMillis() + (currentEstDays * 24 * 60 * 60 * 1000L) else null

                // 30 Days Chart (SQL günlük toplam haritasından)
                val thirtyDaysList = mutableListOf<DayChartItem>()
                for (i in 29 downTo 0) {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val k = NumberFormatter.getDateKey(c.time)
                    val label = "${c.get(Calendar.DAY_OF_MONTH)}"
                    val amt = (dailyMap[k] ?: 0L).coerceAtLeast(0L)
                    thirtyDaysList.add(DayChartItem(k, label, amt, 0f))
                }
                val max30 = thirtyDaysList.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
                val thirtyDaysWithRatio = thirtyDaysList.map { it.copy(ratio = (it.amount.toFloat() / max30.toFloat()).coerceIn(0.04f, 1f)) }

                // 6 Months Chart (SQL günlük aggregate listesinden ay bazında toplanır)
                val sixMonthsList = mutableListOf<MonthChartItem>()
                for (i in 5 downTo 0) {
                    val mCal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
                    val targetMonth = mCal.get(Calendar.MONTH)
                    val targetYear = mCal.get(Calendar.YEAR)
                    val label = NumberFormatter.getMonthLabel(mCal.time, settings.lang)
                    val prefix = String.format(Locale.US, "%04d-%02d", targetYear, targetMonth + 1)
                    val amt = dailyStats.filter { it.dateKey.startsWith(prefix) }.sumOf { it.total }.coerceAtLeast(0L)
                    sixMonthsList.add(MonthChartItem(targetMonth, label, amt, 0f))
                }
                val max6 = sixMonthsList.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
                val sixMonthsWithRatio = sixMonthsList.map { it.copy(ratio = (it.amount.toFloat() / max6.toFloat()).coerceIn(0.04f, 1f)) }

                // Badges & Unlocked Badge Detection
                // Hızlı intikalle otomatik tamamlanan basamaklar rozet kazandırmaz:
                // offset, güncel değeri aşamaz (sıfırlama/yeni tur sonrası kendini onarır).
                val badgeCompletedCount = BadgeProgress.badgeCompletedCount(
                    completedCount = completedCount,
                    storedOffset = badgeProgressPrefs.getInt(KEY_FAST_JUMP_COMPLETED_OFFSET, 0)
                )
                val badgeTotalDone = BadgeProgress.badgeTotalDone(
                    totalDone = totalDone,
                    storedOffset = badgeProgressPrefs.getLong(KEY_FAST_JUMP_TOTAL_OFFSET, 0L)
                )
                val allBadges = BadgeManager.getAllBadges(badgeTotalDone, badgeCompletedCount, bestStreak, settings.lang)
                val acknowledgedBadges = settings.acknowledgedBadges.split(",").filter { it.isNotBlank() }.toSet()
                val newlyUnlockedBadge = allBadges.firstOrNull { it.isUnlocked && !acknowledgedBadges.contains(it.id) }

                val currentCelebration = _uiState.value.badgeCelebrationData
                val badgeToCelebrate = if (currentCelebration != null) {
                    currentCelebration
                } else if (newlyUnlockedBadge != null && _uiState.value.isHydrated) {
                    if (settings.hapticEnabled) {
                        hapticHelper.celebration()
                    }
                    newlyUnlockedBadge
                } else null

                _uiState.update { current ->
                    current.copy(
                        zikirs = zikirs,
                        selectedId = selectedId,
                        tab = savedTab,
                        history = recentHistory,
                        settings = settings,
                        todayRecited = todayRecited,
                        todayPercent = todayPercent,
                        streak = streakCount,
                        bestStreak = bestStreak,
                        last7Days = sevenDaysWithRatio,
                        last30Days = thirtyDaysWithRatio,
                        last6Months = sixMonthsWithRatio,
                        totalDone = totalDone,
                        completedCount = completedCount,
                        overallRemaining = overallRemaining,
                        overallAveragePerDay = overall7DayAvg,
                        overallEstimatedDate = overallEstDate,
                        currentZikir = currentZikir,
                        currentAveragePerDay = current7DayAvg,
                        currentEstimatedDate = currentEstDate,
                        canUndo = true,
                        badges = allBadges,
                        badgeCelebrationData = badgeToCelebrate,
                        showRoundModal = if (badgeCompletedCount == 15 && !current.showRoundModal) true else current.showRoundModal,
                        isHydrated = true
                    )
                }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
            .collect {}
        }
    }

    private val autoShownZeroInfoZikirIds = mutableSetOf<Int>()
    private val spiritualInfoPrefs by lazy {
        getApplication<Application>().getSharedPreferences("spiritual_virtues_prefs", Context.MODE_PRIVATE)
    }

    fun checkAndShowInitialSpiritualInfo(zikirId: Int) {
        val zikir = _uiState.value.zikirs.find { it.id == zikirId } ?: return
        val todayKey = NumberFormatter.getDateKey()
        val lastShownDate = spiritualInfoPrefs.getString("last_spiritual_virtue_date", null)
        val lastShownZikirId = spiritualInfoPrefs.getInt("last_spiritual_virtue_zikir_id", -1)

        // Gösterim Şartları:
        // 1. Bir zikre yeni başlandığında (sayacı 0 iken) ilk defa
        // 2. VEYA o gün (günde 1 defa) o anda devam eden zikrin manevi tecellisi olarak henüz gösterilmemişse
        val isNewZikirZero = zikir.count == 0L && !autoShownZeroInfoZikirIds.contains(zikirId)
        val isDailyReflectionDue = lastShownDate != todayKey || lastShownZikirId != zikirId

        if (isNewZikirZero) {
            autoShownZeroInfoZikirIds.add(zikirId)
            spiritualInfoPrefs.edit {
                putString("last_spiritual_virtue_date", todayKey)
                putInt("last_spiritual_virtue_zikir_id", zikirId)
            }
            openInfoModal(zikirId)
        } else if (isDailyReflectionDue) {
            spiritualInfoPrefs.edit {
                putString("last_spiritual_virtue_date", todayKey)
                putInt("last_spiritual_virtue_zikir_id", zikirId)
            }
            openInfoModal(zikirId)
        }
    }

    fun incrementCount(amount: Long) {
        if (isChildLocked()) return
        val state = _uiState.value
        val currentZikir = state.currentZikir ?: return
        
        // Emniyet Kontrolü: Eğer aktif zikir kilitliyse (öncekiler bitmemişse) zikir çekilmesini engelle ve uyarı ver
        if (!isZikirUnlocked(currentZikir.id, state.zikirs)) {
            val requiredId = getFirstIncompleteZikirId(state.zikirs)
            _uiState.update {
                it.copy(
                    sequenceWarning = SequenceWarningData(
                        attemptedZikirId = currentZikir.id,
                        requiredZikirId = requiredId
                    )
                )
            }
            return
        }

        val available = (currentZikir.target - currentZikir.count).coerceAtLeast(0L)
        if (available <= 0) return

        val addAmt = amount.coerceAtMost(available)
        val newCount = currentZikir.count + addAmt
        val reachedTarget = newCount >= currentZikir.target

        if (state.settings.hapticEnabled && !reachedTarget) {
            val intensity = state.settings.hapticTapMode
            when (amount) {
                1L -> hapticHelper.tap(intensity)
                1000L -> hapticHelper.quickAdd1000(intensity)
                5000L -> hapticHelper.quickAdd5000(intensity)
                10000L -> hapticHelper.quickAdd10000(intensity)
                else -> hapticHelper.tap(intensity)
            }
        }

        // Her zikirden sonra hareketsizlik sayacını sıfırla (3 gün sonra tekrar kurulsun).
        notificationScheduler.scheduleInactivityAlert(true)

        // SENKRON YAZMA: Room transaction hemen calisir ve allZikirs Flow'u
        // yeni sayiyi TEK emission ile getirir. Optimistic guncelleme + 40ms
        // toplu kanal kaldirildi; boylece ekran once artip sonra dusen
        // titreme yasamaz - sayinin tek kaynagi DB'dir.
        viewModelScope.launch(Dispatchers.IO) {
            val (_, reached) = repository.addDhikrCount(currentZikir.id, addAmt)
            withContext(Dispatchers.Main) {
                if (reached) {
                    hapticHelper.celebration()
                    val nextId = if (currentZikir.id < 15) currentZikir.id + 1 else null
                    val zikirName = ZikirContent.getZikirName(currentZikir.id, state.settings.lang)
                    _uiState.update {
                        it.copy(
                            celebrationData = CelebrationData(currentZikir.id, zikirName, nextId),
                            canUndo = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(canUndo = true) }
                }
            }
        }
    }

    fun decrementCount(amount: Long) {
        val state = _uiState.value
        val currentZikir = state.currentZikir ?: return
        if (currentZikir.count <= 0) return

        val removeAmt = amount.coerceAtMost(currentZikir.count)

        viewModelScope.launch {
            repository.removeDhikrCount(currentZikir.id, removeAmt)
            if (state.settings.hapticEnabled) hapticHelper.tap(state.settings.hapticTapMode)
            _uiState.update { it.copy(canUndo = true) }
        }
    }
    fun undoLastAction() {
        if (isChildLocked()) return
        val state = _uiState.value
        val currentZikirId = state.selectedId
        viewModelScope.launch {
            val newCount = repository.undoLastAction(currentZikirId)
            if (newCount != null) {
                if (state.settings.hapticEnabled) hapticHelper.tap(state.settings.hapticTapMode)
            }
        }
    }


    fun getFirstIncompleteZikirId(zikirs: List<Zikir> = _uiState.value.zikirs): Int =
        SelectedZikirResolver.firstIncompleteId(zikirs)

    fun isZikirUnlocked(id: Int, zikirs: List<Zikir> = _uiState.value.zikirs): Boolean =
        SelectedZikirResolver.isUnlocked(id, zikirs)

    fun selectZikir(id: Int, bypassValidation: Boolean = false) {
        val validId = id.coerceIn(1, 15)
        val zikirs = _uiState.value.zikirs

        if (!bypassValidation && validId > 1) {
            val isUnlocked = isZikirUnlocked(validId, zikirs)
            if (!isUnlocked) {
                val requiredId = getFirstIncompleteZikirId(zikirs)
                _uiState.update {
                    it.copy(
                        sequenceWarning = SequenceWarningData(
                            attemptedZikirId = validId,
                            requiredZikirId = requiredId
                        ),
                        isSidebarOpen = false
                    )
                }
                return
            }
        }

        savedStateHandle["selectedZikirId"] = validId
        pendingSelection = SelectedZikirResolver.Pending(validId, MonotonicTime.now())
        viewModelScope.launch {
            updateSettingsSafely { it.copy(selectedZikirId = validId) }
            _uiState.update {
                it.copy(
                    selectedId = validId,
                    sequenceWarning = null,
                    isSidebarOpen = false
                )
            }
            checkAndShowInitialSpiritualInfo(validId)
        }
    }

    fun dismissSequenceWarning() {
        _uiState.update { it.copy(sequenceWarning = null) }
    }

    fun openFastJumpDialog(zikirId: Int?) {
        _uiState.update { it.copy(fastJumpDialogZikirId = zikirId) }
    }

    fun fastJumpToZikir(targetZikirId: Int) {
        val validId = targetZikirId.coerceIn(1, 15)
        autoShownZeroInfoZikirIds.clear()
        savedStateHandle["selectedZikirId"] = validId
        pendingSelection = SelectedZikirResolver.Pending(validId, MonotonicTime.now())
        savedStateHandle["currentTab"] = "zikir"

        // Atlamanın otomatik tamamlayacağı basamakları DB yazımından önce hesapla ve
        // rozet offset'ini hemen işle; aksi halde ara emission rozet kutlaması tetikler.
        val jumpedZikirs = _uiState.value.zikirs.filter { it.id < validId && it.count < it.target }
        addFastJumpBadgeOffset(
            completedZikirs = jumpedZikirs.size,
            addedCount = jumpedZikirs.sumOf { (it.target - it.count).coerceAtLeast(0L) }
        )

        viewModelScope.launch {
            repository.fastJumpToZikir(validId)
            _uiState.update {
                it.copy(
                    selectedId = validId,
                    sequenceWarning = null,
                    fastJumpDialogZikirId = null,
                    isSidebarOpen = false,
                    tab = "zikir"
                )
            }
            if (_uiState.value.settings.hapticEnabled) {
                hapticHelper.celebration()
            }
            checkAndShowInitialSpiritualInfo(validId)
        }
    }

    fun navigateToRequiredZikir() {
        val reqId = _uiState.value.sequenceWarning?.requiredZikirId ?: getFirstIncompleteZikirId()
        _uiState.update { it.copy(sequenceWarning = null) }
        selectZikir(reqId, bypassValidation = true)
        setTab("zikir")
    }

    fun setTab(tab: String) {
        savedStateHandle["currentTab"] = tab
        _uiState.update { it.copy(tab = tab, isSidebarOpen = false) }
    }

    fun resetCurrentZikir() {
        if (isChildLocked()) return
        val currentId = _uiState.value.selectedId
        autoShownZeroInfoZikirIds.remove(currentId)
        _uiState.update { s ->
            val updated = s.zikirs.map {
                if (it.id == currentId) it.copy(count = 0L, startedAt = null, completedAt = null) else it
            }
            s.copy(zikirs = updated, canUndo = false)
        }
        viewModelScope.launch {
            repository.resetSingleZikir(currentId)
        }
    }

    fun resetAllZikirs() {
        if (isChildLocked()) return
        autoShownZeroInfoZikirIds.clear()
        _uiState.update { s ->
            val updated = s.zikirs.map { it.copy(count = 0L, startedAt = null, completedAt = null) }
            s.copy(zikirs = updated, canUndo = false)
        }
        viewModelScope.launch {
            repository.resetAllZikirs()
        }
    }

    fun startNewRound() {
        autoShownZeroInfoZikirIds.clear()
        _uiState.update { s ->
            val updated = s.zikirs.map { it.copy(count = 0L, startedAt = null, completedAt = null) }
            val currentSettings = s.settings
            s.copy(
                zikirs = updated,
                selectedId = 1,
                settings = currentSettings.copy(
                    completedRounds = currentSettings.completedRounds + 1,
                    selectedZikirId = 1
                ),
                showRoundModal = false,
                canUndo = false
            )
        }
        viewModelScope.launch {
            repository.startNewRound()
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val allowed = setOf("tr", "ar", "en", "de", "fr")
            val safe = if (lang in allowed) lang else "tr"
            updateSettingsSafely { it.copy(lang = safe) }
        }
    }

    fun setTheme(themeName: String) {
        viewModelScope.launch {
            // Canonical + legacy allowed, normalize to canonical for storage
            val normalized = try {
                com.example.ui.theme.AppPalettes.normalizeId(themeName)
            } catch (_: Exception) {
                "hadra_gece"
            }
            updateSettingsSafely { it.copy(themeName = normalized) }
        }
    }

    fun toggleCountdown() {
        viewModelScope.launch {
            updateSettingsSafely { it.copy(countdownMode = !it.countdownMode) }
        }
    }

    fun setDailyTarget(target: Long) {
        viewModelScope.launch {
            updateSettingsSafely { it.copy(dailyTarget = target.coerceIn(500L, 500000L)) }
        }
    }

    fun adjustDailyTarget(delta: Long) {
        val current = _uiState.value.settings.dailyTarget
        setDailyTarget(current + delta)
    }

    fun cycleHapticMode() {
        viewModelScope.launch {
            val currentEnabled = _uiState.value.settings.hapticEnabled
            val currentMode = _uiState.value.settings.hapticTapMode
            
            // 3 Kademeli Döngü: Kapalı -> Hafif -> Orta -> Güçlü -> Kapalı
            val (nextEnabled, nextMode) = when {
                !currentEnabled -> Pair(true, "light")
                currentMode == "light" -> Pair(true, "medium")
                currentMode == "medium" -> Pair(true, "strong")
                else -> Pair(false, "light")
            }
            
            updateSettingsSafely { 
                it.copy(hapticEnabled = nextEnabled, hapticTapMode = nextMode) 
            }
            if (nextEnabled) {
                hapticHelper.tap(nextMode)
            }
        }
    }

    fun toggleFullScreenTap() {
        viewModelScope.launch {
            updateSettingsSafely { it.copy(fullScreenTap = !it.fullScreenTap) }
        }
    }

    fun toggleKeepAwake() {
        viewModelScope.launch {
            updateSettingsSafely { it.copy(keepAwakeEnabled = !it.keepAwakeEnabled) }
        }
    }

    fun openInfoModal(zikirId: Int?) {
        _uiState.update { it.copy(infoModalZikirId = zikirId) }
    }

    fun closeCelebration() {
        _uiState.update { it.copy(celebrationData = null) }
    }

    fun setShowRoundModal(show: Boolean) {
        _uiState.update { it.copy(showRoundModal = show) }
    }

    fun toggleSidebar(open: Boolean) {
        _uiState.update { it.copy(isSidebarOpen = open) }
    }

    fun toggleZenMode(enabled: Boolean? = null) {
        _uiState.update { current ->
            val nextState = enabled ?: !current.isZenMode
            current.copy(isZenMode = nextState)
        }
    }

    fun setCounterTexture(texture: String) {
        viewModelScope.launch {
            val allowed = setOf("none", "geometric", "kaaba", "floral", "tasbih", "stars")
            val safe = if (texture in allowed) texture else "geometric"
            updateSettingsSafely { it.copy(counterTexture = safe) }
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            val clamped = scale.coerceIn(0.7f, 1.5f)
            updateSettingsSafely { it.copy(fontScale = clamped) }
        }
    }

    fun updateZikirTarget(zikirId: Int, newTarget: Long) {
        viewModelScope.launch {
            repository.updateZikirTarget(zikirId, newTarget)
        }
    }

    fun acknowledgeBadge(badgeId: String) {
        viewModelScope.launch {
            updateSettingsSafely { currentSettings ->
                val acknowledged = currentSettings.acknowledgedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
                acknowledged.add(badgeId)
                val newAckString = acknowledged.joinToString(",")
                currentSettings.copy(acknowledgedBadges = newAckString)
            }
            _uiState.update { it.copy(badgeCelebrationData = null) }
        }
    }

    fun dismissBadgeCelebration() {
        val currentBadge = _uiState.value.badgeCelebrationData
        if (currentBadge != null) {
            acknowledgeBadge(currentBadge.id)
        } else {
            _uiState.update { it.copy(badgeCelebrationData = null) }
        }
    }

    fun toggleAutoReorder() {
        viewModelScope.launch {
            updateSettingsSafely { it.copy(autoReorderSettings = !it.autoReorderSettings) }
        }
    }

    fun incrementSettingUsage(category: String) {
        viewModelScope.launch {
            updateSettingsSafely { currentSettings ->
                val currentStatsStr = currentSettings.settingsUsageStats
                val map = mutableMapOf<String, Int>()
                try {
                    val json = JSONObject(currentStatsStr)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        map[k] = json.optInt(k, 0)
                    }
                } catch (e: Exception) {
                    // Fallback to lenient parser if JSON malformed
                    try {
                        val cleanStr = currentStatsStr.removePrefix("{").removeSuffix("}").trim()
                        if (cleanStr.isNotEmpty()) {
                            cleanStr.split(",").forEach { pair ->
                                val parts = pair.split(":")
                                if (parts.size == 2) {
                                    val key = parts[0].trim().removeSurrounding("\"")
                                    val value = parts[1].trim().toIntOrNull() ?: 0
                                    map[key] = value
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                
                map[category] = (map[category] ?: 0) + 1
                
                val newJson = JSONObject()
                for ((k, v) in map) {
                    newJson.put(k, v)
                }
                
                currentSettings.copy(settingsUsageStats = newJson.toString())
            }
        }
    }

    /**
     * Hangi zikrin kaç defa çekildiğine dair tüm istatistikleri detaylı bir metin yedeği dosyası olarak oluşturur
     * ve Android sisteminin dosya paylaşım (Share Chooser) penceresini açar (WhatsApp, Drive, Gmail vb.).
     */

    private val _showExportPasswordDialog = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showExportPasswordDialog = _showExportPasswordDialog.asStateFlow()

    private val _showImportPasswordDialog = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showImportPasswordDialog = _showImportPasswordDialog.asStateFlow()

    private var pendingImportUri: android.net.Uri? = null

    fun requestExportLocalBackup() {
        _showExportPasswordDialog.value = true
    }

    fun dismissExportPasswordDialog() {
        _showExportPasswordDialog.value = false
    }

    fun requestImportLocalBackup(uri: android.net.Uri) {
        pendingImportUri = uri
        _showImportPasswordDialog.value = true
    }

    fun dismissImportPasswordDialog() {
        _showImportPasswordDialog.value = false
        pendingImportUri = null
    }

    fun exportAndShareStatisticsBackup(context: android.content.Context, password: String, onError: (String) -> Unit) {
        _showExportPasswordDialog.value = false
        val strings = AppStrings.get(_uiState.value.settings.lang)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = repository.getAtomicSnapshot()
                val backupManager = com.example.data.backup.BackupManager(context)
                val file = backupManager.createTemporaryBackupFile()
                val outStream = file.outputStream()
                
                val result = backupManager.exportBackup(
                    outputStream = outStream,
                    zikirs = snapshot.zikirs,
                    history = snapshot.history,
                    reminderSlots = snapshot.slots,
                    settings = snapshot.settings,
                    password = password
                )
                
                if (result.isSuccess) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, strings.exportStatsFileHeader)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = android.content.Intent.createChooser(intent, strings.exportStatsChooserTitle).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    withContext(Dispatchers.Main) {
                        context.startActivity(chooser)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError(result.exceptionOrNull()?.localizedMessage ?: "Export Error")
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Export & Share Error")
                }
            }
        }
    }

    fun importStatisticsBackup(
        context: android.content.Context,
        password: String,
        onSuccess: (restoredCount: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val uri = pendingImportUri ?: return
        _showImportPasswordDialog.value = false
        val strings = AppStrings.get(_uiState.value.settings.lang)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backupManager = com.example.data.backup.BackupManager(context)
                context.contentResolver.openInputStream(uri)?.use { moshiInputStream ->
                    val result = backupManager.importBackup(moshiInputStream, password)
                    if (result.isSuccess) {
                        val validatedData = result.getOrThrow()
                        val nextZikirId = validatedData.zikirs.sortedBy { it.id }.firstOrNull { it.count < it.target }?.id ?: 15
                        repository.restoreFullLocalBackup(
                            zikirs = validatedData.zikirs,
                            history = validatedData.history,
                            slots = validatedData.reminderSlots,
                            settings = validatedData.settings,
                            selectedZikirId = nextZikirId
                        )
                        withContext(Dispatchers.Main) { onSuccess(validatedData.zikirs.size) }
                        return@launch
                    } else {
                        val ex = result.exceptionOrNull()
                        if (ex is com.example.data.backup.PasswordRequiredException) {
                            withContext(Dispatchers.Main) { onError(ex.localizedMessage ?: "Parola gerekli") }
                            return@launch
                        }
                        if (ex is com.example.data.backup.WrongPasswordException) {
                            withContext(Dispatchers.Main) { onError(UiText.wrongPassword.get(_uiState.value.settings.lang)) }
                            return@launch
                        }
                        
                        // Fallback logic starts if it wasn't a crypto error
                        // Because stream is closed and read entirely, we need to open again
                        context.contentResolver.openInputStream(uri)?.use { fbStream ->
                            try {
                                val content = fbStream.bufferedReader(Charsets.UTF_8).readText()
                                var jsonString: String? = null
                                val startTag = "--- NEFS_ZIKIR_BACKUP_DATA_START ---"
                                val endTag = "--- NEFS_ZIKIR_BACKUP_DATA_END ---"

                                if (content.contains(startTag) && content.contains(endTag)) {
                                    val start = content.indexOf(startTag) + startTag.length
                                    val end = content.indexOf(endTag)
                                    if (start in 0..end) {
                                        jsonString = content.substring(start, end).trim()
                                    }
                                } else {
                                    val trimmed = content.trim()
                                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                                        jsonString = trimmed
                                    }
                                }

                                val restoredZikirs = mutableListOf<Zikir>()
                                var completedRounds: Int? = null

                                 if (!jsonString.isNullOrBlank()) {
                                    val root = org.json.JSONObject(jsonString)
                                    if (root.has("completedRounds")) {
                                        completedRounds = root.getInt("completedRounds")
                                    }
                                    if (root.has("zikirs")) {
                                        val arr = root.getJSONArray("zikirs")
                                        for (i in 0 until arr.length()) {
                                            val obj = arr.getJSONObject(i)
                                            val id = obj.getInt("id")
                                            val count = obj.getLong("count")
                                            val target = if (obj.has("target")) obj.getLong("target") else 70000L
                                            val startedAt = if (obj.has("startedAt") && !obj.isNull("startedAt")) obj.getLong("startedAt") else null
                                            val completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.getLong("completedAt") else null
                                            restoredZikirs.add(
                                                Zikir(
                                                    id = id,
                                                    target = target,
                                                    count = count,
                                                    startedAt = startedAt,
                                                    completedAt = completedAt
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                com.example.data.model.DhikrDataValidator.validateFullSnapshotStrict(
                                    zikirs = restoredZikirs
                                )
                                
                                val selectedZikirId = restoredZikirs.sortedBy { it.id }.firstOrNull { it.count < it.target }?.id ?: 1
                                repository.restoreBackup(
                                    zikirs = restoredZikirs,
                                    completedRounds = completedRounds
                                )
                                withContext(Dispatchers.Main) { onSuccess(restoredZikirs.size) }
                            } catch (fallbackE: Exception) {
                                withContext(Dispatchers.Main) {
                                    onError(fallbackE.localizedMessage ?: strings.importStatsBackupError)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: strings.importStatsBackupError)
                }
            }
        }
    }

    fun dismissCloudSyncMessage() {
        _cloudSyncMessage.value = null
    }

    fun signInWithGoogle(activityContext: Context, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val result = authManager.signInWithGoogle(activityContext)
            _isCloudSyncing.value = false
            val strings = AppStrings.get(_uiState.value.settings.lang)
            result.fold(
                onSuccess = { user ->
                    val userName = user.displayName ?: user.email ?: ""
                    _cloudSyncMessage.value = strings.cloudWelcomeMessage.replace("{0}", userName)
                    onResult(true, null)
                    // BULUT ONCELIKLI: once oku, gerekiyorsa geri yukle,
                    // ikisi de doluysa kullaniciya sor. Artik yerel veri
                    // giris aninda sessizce buluta YUKLENMIYOR.
                    syncCloudAfterSignIn(user.uid)
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: strings.cloudGenericSignInError
                    _cloudSyncMessage.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            authManager.signOut()
            _isCloudSyncing.value = false
            val strings = AppStrings.get(_uiState.value.settings.lang)
            _cloudSyncMessage.value = strings.cloudSignOutMessage
            onComplete()
        }
    }

    /**
     * Giris sonrasi BULUT ONCELIKLI senkronizasyon.
     *
     * Eski davranis hataliydi: giris basarili olunca backupToCloudSilently()
     * cagriliyor, yani YEREL veri sessizce buluta yukleniyordu. Eski yedek
     * hic cekilmiyor, kullaniciya sorulmuyor ve hata bile gosterilmiyordu
     * (catch (_: Exception) {}). Ustelik taze kurulumda localRevision 0
     * oldugu icin SyncManager'daki koruma (remoteRevision > localRevision)
     * devreye girmiyor ve buluttaki revision 0/eksikse ESKI YEDEK EZILIYORDU.
     *
     * Yeni davranis:
     *  1. Once buluttaki yedek OKUNUR; bu asamada hicbir sey yazilmaz.
     *  2. Bulutta yedek yoksa -> yerel veri yuklenir (bulut ilk kez olusur).
     *  3. Bulutta yedek varsa ve bu cihaz bosa (taze kurulum) -> otomatik
     *     geri yuklenir; kullaniciyi gereksiz soruyla mesgul etmeyiz.
     *  4. Ikisi de dolu -> kullaniciya SORULUR (SyncConflictDialog).
     */
    private fun syncCloudAfterSignIn(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lang = _uiState.value.settings.lang
            try {
                _isCloudSyncing.value = true
                val remote = syncManager.restoreFromCloud(userId)
                val remoteData = remote.getOrNull()

                if (remoteData == null) {
                    // Bulutta yedek yok (veya okunamadi): bu cihazin verisini yukle.
                    uploadLocalAfterSignIn(userId)
                    return@launch
                }

                val localSnapshot = repository.getAtomicSnapshot()
                val localIsEmpty = localSnapshot.history.isEmpty() &&
                    localSnapshot.zikirs.all { it.count == 0L }

                if (localIsEmpty) {
                    repository.restoreFullCloudBackup(
                        zikirs = remoteData.zikirs,
                        settings = remoteData.settings,
                        slots = remoteData.reminderSlots,
                        history = remoteData.history
                    )
                    alignSelectionAfterRestore(
                        zikirs = remoteData.zikirs,
                        history = remoteData.history,
                        backedUpSelection = remoteData.settings.selectedZikirId
                    )
                    setLocalRevision(remoteData.syncMetadata?.revision ?: 0L)
                    _lastCloudSyncTimestamp.value = remoteData.lastSyncedAt
                    _cloudSyncMessage.value = UiText.cloudRestoredOnSignIn.get(lang)
                } else {
                    // Hem yerel hem bulut dolu -> karar kullanicinin.
                    _syncConflictState.value = remoteData
                }
                _isCloudSyncing.value = false
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _isCloudSyncing.value = false
                _cloudSyncMessage.value = e.localizedMessage
                    ?: AppStrings.get(lang).cloudGenericRestoreError
            }
        }
    }

    /**
     * Bulutta hic yedek yokken bu cihazin verisini yukler.
     * Eskiden hatalar sessizce yutuluyordu; artik kullaniciya gosterilir.
     */
    private suspend fun uploadLocalAfterSignIn(userId: String) {
        val lang = _uiState.value.settings.lang
        try {
            val snapshot = repository.getAtomicSnapshot()
            val res = syncManager.backupToCloud(
                userId = userId,
                zikirs = snapshot.zikirs,
                history = snapshot.history,
                slots = snapshot.slots,
                settings = snapshot.settings,
                localRevision = getLocalRevision(),
                deviceId = getDeviceId()
            )
            res.fold(
                onSuccess = { ts ->
                    setLocalRevision(getLocalRevision() + 1)
                    _lastCloudSyncTimestamp.value = ts
                    _cloudSyncMessage.value = UiText.cloudNoBackupUploadedLocal.get(lang)
                },
                onFailure = { e ->
                    _cloudSyncMessage.value = e.localizedMessage
                        ?: AppStrings.get(lang).cloudGenericBackupError
                }
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            _cloudSyncMessage.value = e.localizedMessage
                ?: AppStrings.get(lang).cloudGenericBackupError
        } finally {
            _isCloudSyncing.value = false
        }
    }


    private val syncPrefs: android.content.SharedPreferences = getApplication<android.app.Application>().getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE)

    private fun getDeviceId(): String {
        var id = syncPrefs.getString("device_id", null)
        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            syncPrefs.edit().putString("device_id", id).apply()
        }
        return id
    }

    private fun getLocalRevision(): Long = syncPrefs.getLong("sync_revision", 0L)
    
    private fun setLocalRevision(revision: Long) {
        syncPrefs.edit().putLong("sync_revision", revision).apply()
    }

    private val _syncConflictState = kotlinx.coroutines.flow.MutableStateFlow<com.example.data.cloud.CloudBackupData?>(null)
    val syncConflictState: kotlinx.coroutines.flow.StateFlow<com.example.data.cloud.CloudBackupData?> = _syncConflictState.asStateFlow()

    fun dismissSyncConflict() {
        _syncConflictState.value = null
    }

    fun resolveConflictWithRemote() {
        val backupData = _syncConflictState.value ?: return
        _syncConflictState.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.restoreFullCloudBackup(
                    zikirs = backupData.zikirs,
                    settings = backupData.settings,
                    slots = backupData.reminderSlots,
                    history = backupData.history
                )
                alignSelectionAfterRestore(
                    zikirs = backupData.zikirs,
                    history = backupData.history,
                    backedUpSelection = backupData.settings.selectedZikirId
                )
                setLocalRevision(backupData.syncMetadata?.revision ?: 0L)
                _lastCloudSyncTimestamp.value = backupData.lastSyncedAt
                val strings = AppStrings.get(_uiState.value.settings.lang)
                _cloudSyncMessage.value = strings.cloudRestoreSuccess
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val strings = AppStrings.get(_uiState.value.settings.lang)
                val msg = e.localizedMessage ?: strings.cloudGenericRestoreError
                _cloudSyncMessage.value = msg
            }
        }
    }

    fun resolveConflictWithLocalOverwrite() {
        val backupData = _syncConflictState.value ?: return
        _syncConflictState.value = null
        val remoteRev = backupData.syncMetadata?.revision ?: 0L
        setLocalRevision(remoteRev)
        backupToCloud()
    }

    fun resolveConflictWithMerge() {
        val backupData = _syncConflictState.value ?: return
        _syncConflictState.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val localSnapshot = repository.getAtomicSnapshot()
                val localZikirs = localSnapshot.zikirs
                val localHistory = localSnapshot.history
                val localSlots = localSnapshot.slots
                val localSettings = localSnapshot.settings

                // 1. Birleşik geçmiş kaydını oluştur (yalnızca immutable eventId üzerinden tekil)
                val mergedHistory = (localHistory + backupData.history)
                    .distinctBy { it.eventId }
                    .sortedByDescending { it.timestamp }

                val allZikirIds = (1..15).toList()

                // 2. Sayımları ve durumları doğrudan history add/remove toplamı üzerinden hesapla
                val mergedZikirs = allZikirIds.map { id ->
                    val localZikir = localZikirs.find { it.id == id }
                    val remoteZikir = backupData.zikirs.find { it.id == id }

                    val target = remoteZikir?.target?.takeIf { it > 0 }
                        ?: localZikir?.target?.takeIf { it > 0 }
                        ?: 70000L

                    val zikirHistoryEvents = mergedHistory.filter { it.zikirId == id }
                    val historyNetSum = zikirHistoryEvents.sumOf { if (it.type == "add") it.amount else -it.amount }
                    
                    // Count doğrudan birleşik geçmiş toplamı ile senkronize edilir
                    val mergedCount = historyNetSum.coerceIn(0L, target)

                    val earliestAddEvent = zikirHistoryEvents.filter { it.type == "add" }.minByOrNull { it.timestamp }?.timestamp
                    val latestEvent = zikirHistoryEvents.maxByOrNull { it.timestamp }?.timestamp

                    val startedAt = if (mergedCount > 0) {
                        listOfNotNull(earliestAddEvent, localZikir?.startedAt, remoteZikir?.startedAt)
                            .filter { it > 0L }
                            .minOrNull() ?: System.currentTimeMillis()
                    } else null

                    val completedAt = if (mergedCount >= target) {
                        val candidate = listOfNotNull(localZikir?.completedAt, remoteZikir?.completedAt, latestEvent)
                            .filter { it > 0L }
                            .maxOrNull() ?: System.currentTimeMillis()
                        maxOf(candidate, startedAt ?: candidate)
                    } else null

                    Zikir(
                        id = id,
                        target = target,
                        count = mergedCount,
                        startedAt = startedAt,
                        completedAt = completedAt
                    )
                }

                val mergedCompletedRounds = maxOf(localSettings.completedRounds, backupData.settings.completedRounds)
                val mergedDailyTarget = if (backupData.settings.dailyTarget > 0) backupData.settings.dailyTarget else localSettings.dailyTarget
                val mergedBadges = (localSettings.acknowledgedBadges.split(",") + backupData.settings.acknowledgedBadges.split(","))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .joinToString(",")

                val mergedSettings = localSettings.copy(
                    completedRounds = mergedCompletedRounds,
                    dailyTarget = mergedDailyTarget,
                    acknowledgedBadges = mergedBadges,
                    lastActiveTimestamp = maxOf(localSettings.lastActiveTimestamp, backupData.settings.lastActiveTimestamp)
                )

                val mergedSlots = if (localSlots.isNotEmpty()) localSlots else backupData.reminderSlots

                // 3. Tek Room transaction içerisinde atomik snapshot olarak yaz
                repository.restoreFullCloudBackup(
                    zikirs = mergedZikirs,
                    settings = mergedSettings,
                    slots = mergedSlots,
                    history = mergedHistory
                )
                val remoteRev = backupData.syncMetadata?.revision ?: 0L
                setLocalRevision(remoteRev)
                backupToCloud()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val strings = AppStrings.get(_uiState.value.settings.lang)
                val msg = e.localizedMessage ?: strings.cloudGenericRestoreError
                _cloudSyncMessage.value = msg
            }
        }
    }

    fun backupToCloud(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = currentUser.value
        val strings = AppStrings.get(_uiState.value.settings.lang)
        if (user == null) {
            _cloudSyncMessage.value = strings.cloudSignInRequiredForBackup
            onComplete(false, strings.cloudSignInRequiredForBackup)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudSyncing.value = true
            try {
                val snapshot = repository.getAtomicSnapshot()
                val zikirs = snapshot.zikirs
                val history = snapshot.history
                val slots = snapshot.slots
                val settings = snapshot.settings

                val result = syncManager.backupToCloud(
                    userId = user.uid,
                    zikirs = zikirs,
                    history = history,
                    slots = slots,
                    settings = settings,
                    localRevision = getLocalRevision(),
                    deviceId = getDeviceId()
                )
                
                result.fold(
                    onSuccess = { timestamp ->
                        setLocalRevision(getLocalRevision() + 1)
                        _lastCloudSyncTimestamp.value = timestamp
                        _cloudSyncMessage.value = strings.cloudBackupSuccess
                        _isCloudSyncing.value = false
                        withContext(Dispatchers.Main) { onComplete(true, null) }
                    },
                    onFailure = { error ->
                        if (error is com.example.data.cloud.SyncConflictException) {
                            val remoteDataResult = syncManager.restoreFromCloud(user.uid)
                            remoteDataResult.onSuccess { remoteData ->
                                _syncConflictState.value = remoteData
                            }
                            val msg = UiText.syncConflictDetected.get(_uiState.value.settings.lang)
                            _cloudSyncMessage.value = msg
                            _isCloudSyncing.value = false
                            withContext(Dispatchers.Main) { onComplete(false, msg) }
                        } else {
                            val msg = error.localizedMessage ?: strings.cloudGenericBackupError
                            _cloudSyncMessage.value = msg
                            _isCloudSyncing.value = false
                            withContext(Dispatchers.Main) { onComplete(false, msg) }
                        }
                    }
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _isCloudSyncing.value = false
                val msg = e.localizedMessage ?: strings.cloudGenericBackupError
                _cloudSyncMessage.value = msg
                withContext(Dispatchers.Main) { onComplete(false, msg) }
            }
        }
    }

    fun restoreFromCloud(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = currentUser.value
        val strings = AppStrings.get(_uiState.value.settings.lang)
        if (user == null) {
            _cloudSyncMessage.value = strings.cloudSignInRequiredForRestore
            onComplete(false, strings.cloudSignInRequiredForRestore)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudSyncing.value = true
            try {
                val result = syncManager.restoreFromCloud(user.uid)
                result.fold(
                    onSuccess = { backupData ->
                        val remoteRev = backupData.syncMetadata?.revision ?: 0L
                        val localRev = getLocalRevision()
                        val remoteDeviceId = backupData.syncMetadata?.deviceId ?: ""
                        val isSameDevice = remoteDeviceId == getDeviceId()

                        if (!isSameDevice && remoteRev > 0 && localRev > 0 && remoteRev != localRev) {
                            // ÇAKIŞMA: hiçbir şey yüklenmedi, karar kullanıcıya bırakıldı.
                            // Eskiden burada onComplete(true) dönülüyordu; bu, veriler
                            // yüklenmediği halde "başarılı" demekti ve kullanıcı
                            // neden hiçbir şey olmadığını anlayamıyordu.
                            val conflictMsg = UiText.syncConflictDetected.get(_uiState.value.settings.lang)
                            _syncConflictState.value = backupData
                            _cloudSyncMessage.value = conflictMsg
                            _isCloudSyncing.value = false
                            withContext(Dispatchers.Main) { onComplete(false, conflictMsg) }
                            return@launch
                        }

                        repository.restoreFullCloudBackup(
                            zikirs = backupData.zikirs,
                            settings = backupData.settings,
                            slots = backupData.reminderSlots,
                            history = backupData.history
                        )
                        alignSelectionAfterRestore(
                            zikirs = backupData.zikirs,
                            history = backupData.history,
                            backedUpSelection = backupData.settings.selectedZikirId
                        )
                        setLocalRevision(remoteRev)
                        _lastCloudSyncTimestamp.value = backupData.lastSyncedAt
                        _cloudSyncMessage.value = strings.cloudRestoreSuccess
                        _isCloudSyncing.value = false
                        withContext(Dispatchers.Main) { onComplete(true, null) }
                    },
                    onFailure = { error ->
                        val msg = error.localizedMessage ?: strings.cloudGenericRestoreError
                        _cloudSyncMessage.value = msg
                        _isCloudSyncing.value = false
                        withContext(Dispatchers.Main) { onComplete(false, msg) }
                    }
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _isCloudSyncing.value = false
                val msg = e.localizedMessage ?: strings.cloudGenericRestoreError
                _cloudSyncMessage.value = msg
                withContext(Dispatchers.Main) { onComplete(false, msg) }
            }
        }
    }

    /**
     * Buluttan tam geri yukleme sonrasi ekranda "en son cekilen" zikrin
     * acilmasini saglar.
     *
     * Neden gerekli: combine blogu cozulen id'yi savedStateHandle'a geri
     * yaziyor ve savedId, settings.selectedZikirId'yi GOLGELIYOR. Taze
     * kurulumda ilk emission 1 yazdigi icin restore sonrasi ekran hep ilk
     * zikiri gostermeye basliyordu. Burada hem savedStateHandle hem DB
     * settings guncellenir; boylece hem bu oturumda hem sonraki acilista
     * dogru basamak gorunur.
     */
    private suspend fun alignSelectionAfterRestore(
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        backedUpSelection: Int
    ) {
        val lastRecited = history.maxByOrNull { it.timestamp }?.zikirId
        val frontier = SelectedZikirResolver.firstIncompleteId(zikirs)
        val candidate = lastRecited ?: backedUpSelection
        val finalId = when {
            candidate in 1..SelectedZikirResolver.TOTAL_ZIKIRS &&
                candidate > 1 &&
                SelectedZikirResolver.isUnlocked(candidate, zikirs) -> candidate
            else -> frontier
        }.coerceIn(1, SelectedZikirResolver.TOTAL_ZIKIRS)

        savedStateHandle["selectedZikirId"] = finalId
        updateSettingsSafely { it.copy(selectedZikirId = finalId) }
    }

    override fun onCleared() {
        super.onCleared()
        authManager.cleanup()
    }
}
