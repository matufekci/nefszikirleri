sed -i '/import Icons.Default.ArrowForward/d' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/import androidx.compose.material.icons.filled.ArrowForward//g' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/import androidx.compose.material.icons.Icons//g' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/import androidx.compose.foundation.layout.size//g' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/import androidx.compose.foundation.layout.Row//g' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/import androidx.compose.foundation.layout.width//g' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i '/^package /a import androidx.compose.material.icons.filled.ArrowForward\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.width' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i '/^$/d' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
