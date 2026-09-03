# Change Particle data
sed -i 's/val emoji = listOf("🌟", "✨", "🎉", "💫", "🤲", "🎇", "👑", "🕊️")\[i % 8\]/val pIndex = i/' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/Triple(targetX, targetY, emoji)/Triple(targetX, targetY, pIndex)/' app/src/main/java/com/example/ui/components/ParticleCelebration.kt
sed -i 's/particles.forEach { (tx, ty, emoji) ->/particles.forEach { (tx, ty, pIndex) ->/' app/src/main/java/com/example/ui/components/ParticleCelebration.kt

# Change the loop Text(emoji) to ParticleIcon
sed -i -e '/Text(/,/)/!b' -e '/text = emoji,/{
c\
                ParticleIcon(\
                    index = pIndex as Int,\
                    tint = colors.gold,\
                    modifier = Modifier\
                        .size(32.dp)\
                        .offset { IntOffset(curX, curY) }\
                        .alpha(alpha)\
                        .scale(scale)\
                )
}' app/src/main/java/com/example/ui/components/ParticleCelebration.kt

# Change the "🌟 🤲 🌟" Text to a Row of ParticleIcons
sed -i -e '/Text(/,/)/!b' -e '/text = "🌟 🤲 🌟",/{
c\
                    Row(\
                        verticalAlignment = Alignment.CenterVertically,\
                        modifier = Modifier.padding(bottom = 12.dp)\
                    ) {\
                        ParticleIcon(index = 0, tint = colors.gold, modifier = Modifier.size(24.dp))\
                        Spacer(modifier = Modifier.width(16.dp))\
                        ParticleIcon(index = 2, tint = colors.primary, modifier = Modifier.size(32.dp))\
                        Spacer(modifier = Modifier.width(16.dp))\
                        ParticleIcon(index = 0, tint = colors.gold, modifier = Modifier.size(24.dp))\
                    }
}' app/src/main/java/com/example/ui/components/ParticleCelebration.kt

