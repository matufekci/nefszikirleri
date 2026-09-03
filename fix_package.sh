sed -i 's/^import androidx.compose.foundation.layout.width//' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/^import androidx.compose.foundation.layout.Row//' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/^import androidx.compose.foundation.layout.size//' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/^import androidx.compose.ui.geometry.Offset//' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt

sed -i '/^package /a import androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.width' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i '/^package /a import androidx.compose.ui.geometry.Offset' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt
