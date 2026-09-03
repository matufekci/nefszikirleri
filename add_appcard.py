import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

appcard_str = """
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    when (colors.themeStyle) {
        ThemeStyle.NEON_CYBER -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = modifier.shadow(12.dp, shape, spotColor = colors.glowColor, ambientColor = colors.glowColor),
                content = content
            )
        }
        ThemeStyle.GLASS_LIGHT -> {
            Surface(
                shape = shape,
                color = colors.card.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                modifier = modifier.shadow(16.dp, shape, spotColor = Color(0x1A000000)),
                content = content
            )
        }
        ThemeStyle.DEEP_TECH -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.8f), colors.card.copy(alpha=0f)))),
                modifier = modifier,
                content = content
            )
        }
        ThemeStyle.SOFT_MINIMAL -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = modifier,
                content = content
            )
        }
        ThemeStyle.OLED_GLOW -> {
             Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = modifier.shadow(6.dp, shape, spotColor = colors.primary, ambientColor = colors.primary),
                content = content
            )
        }
    }
}
"""
if "fun AppCard" not in content:
    content = content.replace('object AppPalettes', appcard_str + '\nobject AppPalettes')

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)

