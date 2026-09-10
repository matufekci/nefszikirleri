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
