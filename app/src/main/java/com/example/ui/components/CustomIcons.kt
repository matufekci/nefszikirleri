package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/**
 * Ultra-Realistic Vector Badge Rendering Pipeline
 * Uses layered metallic gradients, ambient depth backdrops, and official Material vector glyphs.
 */
@Composable
fun BadgeHeroIcon(
    badgeId: String,
    tint: Color,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val isLocked = tint == colors.textMuted || tint.alpha < 0.7f

    val (vectorIcon, emblemColor) = when (badgeId) {
        "terkip_1" -> Pair(Icons.Rounded.AutoAwesome, if (isLocked) colors.textMuted else tint)
        "terkip_3" -> Pair(Icons.Rounded.Stars, if (isLocked) colors.textMuted else tint)
        "terkip_5" -> Pair(Icons.Rounded.MilitaryTech, if (isLocked) colors.textMuted else tint)
        "terkip_7" -> Pair(Icons.Rounded.WorkspacePremium, if (isLocked) colors.textMuted else tint)
        "terkip_10" -> Pair(Icons.Rounded.Diamond, if (isLocked) colors.textMuted else tint)
        "terkip_hatmi" -> Pair(Icons.Rounded.EmojiEvents, if (isLocked) colors.textMuted else tint)
        "streak_7" -> Pair(Icons.Rounded.LocalFireDepartment, if (isLocked) colors.textMuted else Color(0xFFFF9800))
        "streak_21" -> Pair(Icons.Rounded.EmojiEvents, if (isLocked) colors.textMuted else Color(0xFFFFB300))
        "streak_40" -> Pair(Icons.Rounded.WorkspacePremium, if (isLocked) colors.textMuted else Color(0xFFFFD700))
        else -> Pair(Icons.Rounded.AutoAwesome, if (isLocked) colors.textMuted else tint)
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isLocked) 0.dp else 6.dp,
                shape = CircleShape,
                spotColor = emblemColor.copy(alpha = 0.5f),
                ambientColor = emblemColor.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(
                if (isLocked) {
                    Brush.radialGradient(
                        colors = listOf(
                            colors.textMuted.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            emblemColor.copy(alpha = 0.28f),
                            emblemColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                }
            )
            .border(
                width = if (isLocked) 1.dp else 1.5.dp,
                brush = if (isLocked) {
                    Brush.linearGradient(listOf(colors.border.copy(alpha = 0.5f), colors.border.copy(alpha = 0.2f)))
                } else {
                    Brush.sweepGradient(
                        listOf(
                            emblemColor.copy(alpha = 0.9f),
                            emblemColor.copy(alpha = 0.3f),
                            emblemColor,
                            emblemColor.copy(alpha = 0.4f),
                            emblemColor.copy(alpha = 0.9f)
                        )
                    )
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Center crisp vector glyph
        Icon(
            imageVector = vectorIcon,
            contentDescription = badgeId,
            tint = emblemColor,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

@Composable
fun ParticleIcon(
    index: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (index % 4) {
        0 -> Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
        1 -> Icon(
            imageVector = Icons.Rounded.Stars,
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
        2 -> Icon(
            imageVector = Icons.Rounded.Diamond,
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
        3 -> Icon(
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
    }
}

