package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp

/**
 * Material-compliant 3-tier and disabled vibration icons.
 * Follows Google Material 24x24dp grid with rounded corners and caps.
 */
object HapticIcons {

    private fun PathBuilder.drawPhoneBody() {
        moveTo(16.5f, 3f)
        horizontalLineTo(7.5f)
        curveTo(6.4f, 3f, 5.5f, 3.9f, 5.5f, 5f)
        verticalLineTo(19f)
        curveTo(5.5f, 20.1f, 6.4f, 21f, 7.5f, 21f)
        horizontalLineTo(16.5f)
        curveTo(17.6f, 21f, 18.5f, 20.1f, 18.5f, 19f)
        verticalLineTo(5f)
        curveTo(18.5f, 3.9f, 17.6f, 3f, 16.5f, 3f)
        close()
        // Ekran Boşluğu
        moveTo(16.5f, 18f)
        horizontalLineTo(7.5f)
        verticalLineTo(6f)
        horizontalLineTo(16.5f)
        verticalLineTo(18f)
        close()
    }

    // 0. Titreşim Kapalı (Titreşimsiz / Sessiz Telefon)
    val VibrationOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rounded.VibrationOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Telefon Gövdesi
            materialPath {
                drawPhoneBody()
            }
            // Çapraz İptal Çizgisi
            materialPath {
                moveTo(3.5f, 4.2f)
                lineTo(19.8f, 20.5f)
                curveTo(20.2f, 20.9f, 20.8f, 20.9f, 21.2f, 20.5f)
                curveTo(21.6f, 20.1f, 21.6f, 19.5f, 21.2f, 19.1f)
                lineTo(4.9f, 2.8f)
                curveTo(4.5f, 2.4f, 3.9f, 2.4f, 3.5f, 2.8f)
                curveTo(3.1f, 3.2f, 3.1f, 3.8f, 3.5f, 4.2f)
                close()
            }
        }.build()
    }

    // 1. Hafif Titreşim (Az zikzaklı / 1 Kademe Dalga)
    val VibrationLight: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rounded.VibrationLight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            materialPath {
                drawPhoneBody()
            }
            // Sol Tek Zikzak (Hafif)
            materialPath {
                moveTo(3f, 8.5f)
                curveTo(2.45f, 8.5f, 2f, 8.95f, 2f, 9.5f)
                verticalLineTo(10f)
                lineTo(3.2f, 12f)
                lineTo(2f, 14f)
                verticalLineTo(14.5f)
                curveTo(2f, 15.05f, 2.45f, 15.5f, 3f, 15.5f)
                curveTo(3.55f, 15.5f, 4f, 15.05f, 4f, 14.5f)
                lineTo(4f, 13.6f)
                lineTo(4.9f, 12f)
                lineTo(4f, 10.4f)
                verticalLineTo(9.5f)
                curveTo(4f, 8.95f, 3.55f, 8.5f, 3f, 8.5f)
                close()
            }
            // Sağ Tek Zikzak (Hafif)
            materialPath {
                moveTo(21f, 8.5f)
                curveTo(20.45f, 8.5f, 20f, 8.95f, 20f, 9.5f)
                verticalLineTo(10.4f)
                lineTo(19.1f, 12f)
                lineTo(20f, 13.6f)
                verticalLineTo(14.5f)
                curveTo(20f, 15.05f, 20.45f, 15.5f, 21f, 15.5f)
                curveTo(21.55f, 15.5f, 22f, 15.05f, 22f, 14.5f)
                verticalLineTo(14f)
                lineTo(20.8f, 12f)
                lineTo(22f, 10f)
                verticalLineTo(9.5f)
                curveTo(22f, 8.95f, 21.55f, 8.5f, 21f, 8.5f)
                close()
            }
        }.build()
    }

    // 2. Orta Titreşim (Orta zikzaklı / 2 Kademe Dalga)
    val VibrationMedium: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rounded.VibrationMedium",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            materialPath {
                drawPhoneBody()
            }
            // Sol Çift Zikzak
            materialPath {
                // İç Zikzak
                moveTo(3.8f, 9.2f)
                lineTo(4.7f, 10.6f)
                lineTo(3.8f, 12f)
                lineTo(4.7f, 13.4f)
                lineTo(3.8f, 14.8f)
                curveTo(3.5f, 15.2f, 4f, 15.7f, 4.4f, 15.4f)
                lineTo(5.5f, 13.7f)
                curveTo(5.7f, 13.4f, 5.7f, 13.1f, 5.5f, 12.8f)
                lineTo(4.8f, 12f)
                lineTo(5.5f, 11.2f)
                curveTo(5.7f, 10.9f, 5.7f, 10.6f, 5.5f, 10.3f)
                lineTo(4.4f, 8.6f)
                curveTo(4f, 8.3f, 3.5f, 8.8f, 3.8f, 9.2f)
                close()
                // Dış Zikzak
                moveTo(1.3f, 8.5f)
                lineTo(2.3f, 10.3f)
                lineTo(1.3f, 12f)
                lineTo(2.3f, 13.7f)
                lineTo(1.3f, 15.5f)
                curveTo(1f, 15.9f, 1.5f, 16.4f, 1.9f, 16.1f)
                lineTo(3.1f, 14.1f)
                curveTo(3.3f, 13.8f, 3.3f, 13.4f, 3.1f, 13.1f)
                lineTo(2.3f, 12f)
                lineTo(3.1f, 10.9f)
                curveTo(3.3f, 10.6f, 3.3f, 10.2f, 3.1f, 9.9f)
                lineTo(1.9f, 7.9f)
                curveTo(1.5f, 7.6f, 1f, 8.1f, 1.3f, 8.5f)
                close()
            }
            // Sağ Çift Zikzak
            materialPath {
                // İç Zikzak
                moveTo(20.2f, 9.2f)
                lineTo(19.3f, 10.6f)
                lineTo(20.2f, 12f)
                lineTo(19.3f, 13.4f)
                lineTo(20.2f, 14.8f)
                curveTo(20.5f, 15.2f, 20f, 15.7f, 19.6f, 15.4f)
                lineTo(18.5f, 13.7f)
                curveTo(18.3f, 13.4f, 18.3f, 13.1f, 18.5f, 12.8f)
                lineTo(19.2f, 12f)
                lineTo(18.5f, 11.2f)
                curveTo(18.3f, 10.9f, 18.3f, 10.6f, 18.5f, 10.3f)
                lineTo(19.6f, 8.6f)
                curveTo(20f, 8.3f, 20.5f, 8.8f, 20.2f, 9.2f)
                close()
                // Dış Zikzak
                moveTo(22.7f, 8.5f)
                lineTo(21.7f, 10.3f)
                lineTo(22.7f, 12f)
                lineTo(21.7f, 13.7f)
                lineTo(22.7f, 15.5f)
                curveTo(23f, 15.9f, 22.5f, 16.4f, 22.1f, 16.1f)
                lineTo(20.9f, 14.1f)
                curveTo(20.7f, 13.8f, 20.7f, 13.4f, 20.9f, 13.1f)
                lineTo(21.7f, 12f)
                lineTo(20.9f, 10.9f)
                curveTo(20.7f, 10.6f, 20.7f, 10.2f, 20.9f, 9.9f)
                lineTo(22.1f, 7.9f)
                curveTo(22.5f, 7.6f, 23f, 8.1f, 22.7f, 8.5f)
                close()
            }
        }.build()
    }

    // 3. Yüksek Titreşim (Yoğun zikzaklı / 3 Kademe Güçlü Dalga)
    val VibrationStrong: ImageVector by lazy {
        ImageVector.Builder(
            name = "Rounded.VibrationStrong",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            materialPath {
                drawPhoneBody()
            }
            // Sol 3'lü Yoğun Zikzak
            materialPath {
                // 1. İç Zikzak
                moveTo(4.4f, 9.6f)
                lineTo(5.2f, 10.8f)
                lineTo(4.4f, 12f)
                lineTo(5.2f, 13.2f)
                lineTo(4.4f, 14.4f)
                curveTo(4.1f, 14.8f, 4.6f, 15.3f, 5f, 15f)
                lineTo(5.9f, 13.6f)
                curveTo(6.1f, 13.3f, 6.1f, 12.9f, 5.9f, 12.6f)
                lineTo(5.2f, 12f)
                lineTo(5.9f, 11.4f)
                curveTo(6.1f, 11.1f, 6.1f, 10.7f, 5.9f, 10.4f)
                lineTo(5f, 9f)
                curveTo(4.6f, 8.7f, 4.1f, 9.2f, 4.4f, 9.6f)
                close()

                // 2. Orta Zikzak
                moveTo(2.4f, 8.8f)
                lineTo(3.4f, 10.4f)
                lineTo(2.4f, 12f)
                lineTo(3.4f, 13.6f)
                lineTo(2.4f, 15.2f)
                curveTo(2.1f, 15.6f, 2.6f, 16.1f, 3f, 15.8f)
                lineTo(4.1f, 14.1f)
                curveTo(4.3f, 13.8f, 4.3f, 13.4f, 4.1f, 13.1f)
                lineTo(3.3f, 12f)
                lineTo(4.1f, 10.9f)
                curveTo(4.3f, 10.6f, 4.3f, 10.2f, 4.1f, 9.9f)
                lineTo(3f, 8.2f)
                curveTo(2.6f, 7.9f, 2.1f, 8.4f, 2.4f, 8.8f)
                close()

                // 3. Dış Güçlü Zikzak
                moveTo(0.5f, 8f)
                lineTo(1.6f, 10f)
                lineTo(0.5f, 12f)
                lineTo(1.6f, 14f)
                lineTo(0.5f, 16f)
                curveTo(0.2f, 16.5f, 0.7f, 17f, 1.1f, 16.6f)
                lineTo(2.4f, 14.3f)
                curveTo(2.6f, 13.9f, 2.6f, 13.5f, 2.4f, 13.1f)
                lineTo(1.6f, 12f)
                lineTo(2.4f, 10.9f)
                curveTo(2.6f, 10.5f, 2.6f, 10.1f, 2.4f, 9.7f)
                lineTo(1.1f, 7.4f)
                curveTo(0.7f, 7f, 0.2f, 7.5f, 0.5f, 8f)
                close()
            }

            // Sağ 3'lü Yoğun Zikzak
            materialPath {
                // 1. İç Zikzak
                moveTo(19.6f, 9.6f)
                lineTo(18.8f, 10.8f)
                lineTo(19.6f, 12f)
                lineTo(18.8f, 13.2f)
                lineTo(19.6f, 14.4f)
                curveTo(19.9f, 14.8f, 19.4f, 15.3f, 19f, 15f)
                lineTo(18.1f, 13.6f)
                curveTo(17.9f, 13.3f, 17.9f, 12.9f, 18.1f, 12.6f)
                lineTo(18.8f, 12f)
                lineTo(18.1f, 11.4f)
                curveTo(17.9f, 11.1f, 17.9f, 10.7f, 18.1f, 10.4f)
                lineTo(19f, 9f)
                curveTo(19.4f, 8.7f, 19.9f, 9.2f, 19.6f, 9.6f)
                close()

                // 2. Orta Zikzak
                moveTo(21.6f, 8.8f)
                lineTo(20.6f, 10.4f)
                lineTo(21.6f, 12f)
                lineTo(20.6f, 13.6f)
                lineTo(21.6f, 15.2f)
                curveTo(21.9f, 15.6f, 21.4f, 16.1f, 21f, 15.8f)
                lineTo(19.9f, 14.1f)
                curveTo(19.7f, 13.8f, 19.7f, 13.4f, 19.9f, 13.1f)
                lineTo(20.7f, 12f)
                lineTo(19.9f, 10.9f)
                curveTo(19.7f, 10.6f, 19.7f, 10.2f, 19.9f, 9.9f)
                lineTo(21f, 8.2f)
                curveTo(21.4f, 7.9f, 21.9f, 8.4f, 21.6f, 8.8f)
                close()

                // 3. Dış Güçlü Zikzak
                moveTo(23.5f, 8f)
                lineTo(22.4f, 10f)
                lineTo(23.5f, 12f)
                lineTo(22.4f, 14f)
                lineTo(23.5f, 16f)
                curveTo(23.8f, 16.5f, 23.3f, 17f, 22.9f, 16.6f)
                lineTo(21.6f, 14.3f)
                curveTo(21.4f, 13.9f, 21.4f, 13.5f, 21.6f, 13.1f)
                lineTo(22.4f, 12f)
                lineTo(21.6f, 10.9f)
                curveTo(21.4f, 10.5f, 21.4f, 10.1f, 21.6f, 9.7f)
                lineTo(22.9f, 7.4f)
                curveTo(23.3f, 7f, 23.8f, 7.5f, 23.5f, 8f)
                close()
            }
        }.build()
    }

    /**
     * Duruma göre uygun 24x24dp Material ImageVector döndürür.
     */
    fun forMode(enabled: Boolean, tapMode: String): ImageVector {
        if (!enabled) return VibrationOff
        return when (tapMode.lowercase()) {
            "light" -> VibrationLight
            "medium" -> VibrationMedium
            "strong" -> VibrationStrong
            else -> VibrationMedium
        }
    }
}
