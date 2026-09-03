sed -i 's/state.timeSlots.forEach { slot ->/state.timeSlots.forEachIndexed { idx, slot ->/g' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt
sed -i 's/Text(text = slot.icon, fontSize = 16.sp)/TimeSlotCustomIcon(idx = idx, tint = colors.primary)/g' app/src/main/java/com/example/ui/screens/StatisticsScreen.kt
