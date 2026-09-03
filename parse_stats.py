import re

with open('app/src/main/java/com/example/ui/screens/StatisticsScreen.kt', 'r') as f:
    content = f.read()

# Remove Hero Card (Block 1) completely
# It starts at "// 1. MAJESTIC ACTIVE MERTEBE"
# Ends before "// 2. TERKİP MERTEBELERİ YOL HARİTASI"
hero_pattern = r'// 1\. MAJESTIC ACTIVE MERTEBE.*?(?=(?:// 2\. TERKİP MERTEBELERİ YOL HARİTASI))'
content = re.sub(hero_pattern, '', content, flags=re.DOTALL)

# Block 2 (Terkip Mertebeleri Yol Haritası) -> Wrap in StatCollapsibleCard
# Starts at "// 2. TERKİP MERTEBELERİ YOL HARİTASI"
# Ends before "// 3. KPI METRİK KARTLARI"
terkip_pattern = r'(// 2\. TERKİP MERTEBELERİ YOL HARİTASI.*?(?=(?:// 3\. KPI METRİK KARTLARI)))'
terkip_match = re.search(terkip_pattern, content, flags=re.DOTALL)
if terkip_match:
    terkip_code = terkip_match.group(1)
    new_terkip = f"""
        StatCollapsibleCard(
            title = strings.levelFinished ?: "Mertebe Yol Haritası",
            isExpanded = activeMertebeExpanded,
            onToggle = {{ activeMertebeExpanded = !activeMertebeExpanded }}
        ) {{
{terkip_code}
        }}
        Spacer(modifier = Modifier.height(16.dp))
"""
    content = content.replace(terkip_code, new_terkip)

# Combine Progress, Calendar, Time Density
# Starts at "// 4. AYLIK İLERLEME GRAFİĞİ"
# Ends before "// 7. İSTİKRAR NİŞANLARI"
combo_pattern = r'(// 4\. AYLIK İLERLEME GRAFİĞİ.*?(?=(?:// 7\. İSTİKRAR NİŞANLARI)))'
combo_match = re.search(combo_pattern, content, flags=re.DOTALL)
if combo_match:
    combo_code = combo_match.group(1)
    new_combo = f"""
        StatCollapsibleCard(
            title = strings.timeDensity ?: "İlerleme & Analizler",
            isExpanded = progressExpanded,
            onToggle = {{ progressExpanded = !progressExpanded }}
        ) {{
{combo_code}
        }}
        Spacer(modifier = Modifier.height(16.dp))
"""
    content = content.replace(combo_code, new_combo)

# Badges (Block 7)
# Starts at "// 7. İSTİKRAR NİŞANLARI"
# Ends before "// 8. SON İŞLEMLER"
badges_pattern = r'(// 7\. İSTİKRAR NİŞANLARI.*?(?=(?:// 8\. SON İŞLEMLER \(LOGS\))))'
badges_match = re.search(badges_pattern, content, flags=re.DOTALL)
if badges_match:
    badges_code = badges_match.group(1)
    new_badges = f"""
        StatCollapsibleCard(
            title = strings.badgeTitle ?: "İstikrar Nişanları",
            isExpanded = badgesExpanded,
            onToggle = {{ badgesExpanded = !badgesExpanded }}
        ) {{
{badges_code}
        }}
        Spacer(modifier = Modifier.height(16.dp))
"""
    content = content.replace(badges_code, new_badges)

# Logs (Block 8)
# Starts at "// 8. SON İŞLEMLER (LOGS)"
# Ends before "Spacer(modifier = Modifier.height(20.dp))" or similar end of column
logs_pattern = r'(// 8\. SON İŞLEMLER \(LOGS\).*?(?=Spacer\(modifier = Modifier\.height\(20\.dp\)\)))'
logs_match = re.search(logs_pattern, content, flags=re.DOTALL)
if logs_match:
    logs_code = logs_match.group(1)
    new_logs = f"""
        StatCollapsibleCard(
            title = strings.recordCount ?: "Son İşlemler",
            isExpanded = logsExpanded,
            onToggle = {{ logsExpanded = !logsExpanded }}
        ) {{
{logs_code}
        }}
"""
    content = content.replace(logs_code, new_logs)

with open('app/src/main/java/com/example/ui/screens/StatisticsScreen.kt', 'w') as f:
    f.write(content)
