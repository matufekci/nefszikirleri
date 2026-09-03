import re

with open("/tmp/settings.txt", "r") as f:
    text = f.read()

# We want to replace the `viewModel.setLanguage(code)` with `viewModel.incrementSettingUsage("language"); viewModel.setLanguage(code)`
text = re.sub(r'viewModel\.setLanguage\((.*?)\)', r'viewModel.incrementSettingUsage("language"); viewModel.setLanguage(\1)', text)
text = re.sub(r'viewModel\.setTheme\((.*?)\)', r'viewModel.incrementSettingUsage("theme"); viewModel.setTheme(\1)', text)
text = re.sub(r'viewModel\.setFontScale\((.*?)\)', r'viewModel.incrementSettingUsage("fontScale"); viewModel.setFontScale(\1)', text)
text = re.sub(r'viewModel\.setCounterTexture\((.*?)\)', r'viewModel.incrementSettingUsage("texture"); viewModel.setCounterTexture(\1)', text)
text = re.sub(r'viewModel\.toggleCountdownMode\((.*?)\)', r'viewModel.incrementSettingUsage("target"); viewModel.toggleCountdownMode(\1)', text)
text = re.sub(r'viewModel\.setDailyTarget\((.*?)\)', r'viewModel.incrementSettingUsage("target"); viewModel.setDailyTarget(\1)', text)
text = re.sub(r'viewModel\.toggleHaptic\(\)', r'viewModel.incrementSettingUsage("haptic"); viewModel.toggleHaptic()', text)
text = re.sub(r'viewModel\.setHapticTapMode\((.*?)\)', r'viewModel.incrementSettingUsage("haptic"); viewModel.setHapticTapMode(\1)', text)
text = re.sub(r'viewModel\.setHapticMilestoneMode\((.*?)\)', r'viewModel.incrementSettingUsage("haptic"); viewModel.setHapticMilestoneMode(\1)', text)
text = re.sub(r'viewModel\.toggleFullScreenTap\(\)', r'viewModel.incrementSettingUsage("haptic"); viewModel.toggleFullScreenTap()', text)
text = re.sub(r'viewModel\.toggleKeepAwake\(\)', r'viewModel.incrementSettingUsage("habits"); viewModel.toggleKeepAwake()', text)
text = re.sub(r'viewModel\.toggleReminder\((.*?)\)', r'viewModel.incrementSettingUsage("habits"); viewModel.toggleReminder(\1)', text)
text = re.sub(r'viewModel\.toggleInactivityAlert\((.*?)\)', r'viewModel.incrementSettingUsage("habits"); viewModel.toggleInactivityAlert(\1)', text)
text = re.sub(r'viewModel\.toggleTargetReminder\((.*?)\)', r'viewModel.incrementSettingUsage("habits"); viewModel.toggleTargetReminder(\1)', text)
text = re.sub(r'viewModel\.setShowRoundModal\((.*?)\)', r'viewModel.incrementSettingUsage("rounds"); viewModel.setShowRoundModal(\1)', text)

# Write the modified content to /tmp/settings_patched.txt
with open("/tmp/settings_patched.txt", "w") as f:
    f.write(text)

