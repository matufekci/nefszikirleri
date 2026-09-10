import re

with open("app/src/test/java/com/example/BackupManagerDataIntegrityTest.kt", "r") as f:
    content = f.read()

replacement = """        val weirdValuesJson = \"\"\"
            {
                "appName": "com.example.nefs_zikir",
                "schemaVersion": 1,
                "exportedAt": 123456789,
                "zikirs": [
                    {"id": 1, "target": -500, "count": -9999, "startedAt": null, "completedAt": null}
                ],
                "history": [
                    {"id": 0, "zikirId": 1, "amount": -10, "type": "unknown", "timestamp": -5, "dateKey": ""}
                ],
                "reminderSlots": [
                    {"id": 0, "hour": 99, "minute": -10, "isEnabled": true}
                ],
                "settings": {
                    "dailyTarget": 5000,
                    "themeName": "emerald",
                    "lang": "tr",
                    "hapticEnabled": true,
                    "hapticTapMode": "light",
                    "hapticMilestoneMode": "double",
                    "countdownMode": false,
                    "fullScreenTap": false,
                    "reminderEnabled": false,
                    "inactivityAlertEnabled": false,
                    "completedRounds": -5,
                    "fontScale": 10.0,
                    "keepAwakeEnabled": true,
                    "selectedZikirId": 1,
                    "counterTexture": "geometric",
                    "targetReminderEnabled": false,
                    "acknowledgedBadges": "",
                    "autoReorderSettings": false,
                    "settingsUsageStats": "{}"
                }
            }
        \"\"\".trimIndent()"""

new_content = re.sub(r'val weirdValuesJson = """[\s\S]*?""".trimIndent\(\)', replacement, content)

with open("app/src/test/java/com/example/BackupManagerDataIntegrityTest.kt", "w") as f:
    f.write(new_content)
