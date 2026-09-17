package com.example.data.cloud

data class SyncMetadata(
    val revision: Long,
    val updatedAt: Long,
    val deviceId: String
)

class SyncConflictException(
    val remoteRevision: Long,
    message: String = "Sync conflict detected. Remote revision is newer."
) : Exception(message)

class CloudDataCorruptionException(
    message: String = "Cloud data snapshot is inconsistent or corrupted."
) : Exception(message)

/**
 * Bulutta bu kullanicinin hic yedegi olmadigini isaret eder.
 *
 * Neden ayri tip: bu bir ARIZA degil, normal bir durumdur (ilk giris).
 * Eskiden duz `Exception(...)` ile firlatiliyordu ve cagri tarafi bunu
 * ag/yetki hatasindan ayirt edemedigi icin kullaniciya teknik mesaj
 * gosteriliyordu. `CloudErrorMapper` bu tipi tanir ve 5 dilde "bulutta
 * yedek yok" metnini uretir; `message` yalnizca log icindir.
 */
class NoCloudBackupException(
    message: String = "No cloud backup found for this user."
) : Exception(message)
