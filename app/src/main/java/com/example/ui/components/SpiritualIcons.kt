package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-Realistic Material Symbols Icon Rendering System
 * Uses authentic Material Rounded vectors combined with physical specular micro-highlights,
 * ambient glow halos, and layered metallic / glass gradients.
 */

@Composable
fun RealisticSymbolBadge(
    symbol: ImageVector,
    tint: Color,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = (size.value * 0.14f).dp,
                shape = CircleShape,
                spotColor = tint.copy(alpha = 0.45f),
                ambientColor = tint.copy(alpha = 0.20f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.24f),
                        tint.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        tint.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size((size.value * 0.58f).dp)
        )
    }
}

@Composable
fun SpiritualBeadsIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
) {
    val drawColor = if (tint != Color.Unspecified) tint else Color(0xFFD4AF37)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f - 1.5.dp.toPx())
            val radius = (this.size.minDimension / 2f) - 3.2.dp.toPx()
            val beadCount = 11
            val beadRadius = 2.0.dp.toPx()

            // Dairesel tesbih taneleri - Layered metallic bead rendering
            for (i in 0 until beadCount) {
                val angle = (2 * PI * i / beadCount) - (PI / 2)
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                
                // Outer bead shadow / ambient glow
                drawCircle(
                    color = drawColor.copy(alpha = 0.35f),
                    radius = beadRadius + 0.8.dp.toPx(),
                    center = Offset(x, y)
                )
                // Bead core
                drawCircle(
                    color = drawColor,
                    radius = beadRadius,
                    center = Offset(x, y)
                )
                // Specular micro-highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = beadRadius * 0.4f,
                    center = Offset(x - beadRadius * 0.3f, y - beadRadius * 0.3f)
                )
            }

            // Alt İmame ve Püskül Çizimi
            val imameTop = Offset(center.x, center.y + radius + 0.5.dp.toPx())
            val imameBottom = Offset(center.x, this.size.height - 1.dp.toPx())
            drawLine(
                color = drawColor,
                start = imameTop,
                end = imameBottom,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // İmame kubbesi
            drawCircle(
                color = drawColor,
                radius = 2.2.dp.toPx(),
                center = Offset(center.x, imameTop.y + 2.dp.toPx())
            )
        }
    }
}

@Composable
fun SpiritualFlameIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFFF9800),
    size: Dp = 22.dp
) {
    RealisticSymbolBadge(
        symbol = Icons.Rounded.LocalFireDepartment,
        tint = tint,
        size = size,
        modifier = modifier
    )
}




@Composable
fun SpiritualLockIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Gray,
    size: Dp = 16.dp,
    contentDescription: String? = null
) {
    Icon(
        imageVector = Icons.Rounded.Lock,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun SpiritualCheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF22C55E),
    size: Dp = 16.dp,
    contentDescription: String? = null
) {
    Icon(
        imageVector = Icons.Rounded.CheckCircle,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}


