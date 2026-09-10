package com.example.data.model

data class ZikirDefinition(
    val id: Int,
    val defaultTarget: Long,
    val arabicText: String,
    val names: Map<String, String>,
    val details: Map<String, String>
)

object ZikirContent {

    val INITIAL_DEFINITIONS: List<ZikirDefinition> = listOf(
        ZikirDefinition(
            id = 1,
            defaultTarget = 100000L,
            arabicText = "لَا إِلٰهَ إِلَّا الله",
            names = mapOf(
                "tr" to "Kelime-i Tevhid",
                "ar" to "لَا إِلٰهَ إِلَّا الله",
                "en" to "Kalimah Tawheed",
                "de" to "Kalimah Tawheed",
                "fr" to "Kalima Tawheed"
            ),
            details = mapOf(
                "tr" to "• İman Sütunu: Tüm amellerin köküdür. Müminin kalbinde ve semalarda sarsılmaz bir tevhid sütunu dikerek şirk ve şüpheleri yok eder.\n\n• Menzil ve Bereket: Ömrü ve rızkı bereketlendirir; ahiret yolunda kulu hakiki menzile ulaştıran can kurtarıcı bir nurdur.\n\n• Huşu ve Denge: Özellikle seher vakitlerinde nefes dengesiyle çekildiğinde, bedene ve ruha derin bir sükûnet bahşeder.\n\n• Ümmete Kalkan: Sadece şahsa değil, tüm Ümmet-i Muhammed’e rahmet indirir; belaların ve musibetlerin defedilmesine vesiledir.\n\n• Namaz ve Muhabbet: Tevhid namaz ile kemale erer; kalpte ilahi muhabbeti coşturup basiret gözünü açar.\n\nHülasa: Kalpten masivayı silip nefsi dirilten, başı ve sonu ihata eden zikirlerin en büyüğüdür.",
                "ar" to "• عماد الإيمان: أصل كافة العبادات. يرفع عماد التوحيد في القلب والسماء ويدحض الشرك والشكوك.\n\n• البركة وبلوغ الغاية: يبارك في العمر والرزق، ونور منجٍ يبلغ العبد منازل الفلاح في الآخرة.\n\n• الخشوع والسكينة: يمنح الروح والجسد سكينة عميقة خاصة في أوقات الأسحار.\n\n• رحمة للأمة: يدفع البلايا والشدائد عن أمة محمد ﷺ جمعاء.\n\n• كمال الصلاة: يبلغ التوحيد تمامه بالصلاة ويفيض القلب بمحبة الله وبصيرته.",
                "en" to "• Pillar of Faith: The root of all deeds. Erects an unshakable pillar of Tawheed, eradicating doubts and hidden idolatry.\n\n• Blessing & Destination: A lifesaving light that blesses one’s lifespan and guides the soul to its eternal home.\n\n• Serenity & Balance: Best recited at dawn with rhythmic breath, bestowing profound tranquility upon body and soul.\n\n• Shield for the Ummah: Brings divine mercy and repels calamities across the entire Muslim community.\n\n• Perfected in Prayer: Tawheed reaches perfection through prayer, opening fountains of divine love and spiritual insight.",
                "de" to "• Säule des Glaubens: Wurzel aller Taten. Vernichtet Zweifel und richtet eine unerschütterliche Säule des Monotheismus im Herzen auf.\n\n• Segen & Ziel: Ein rettendes Licht, das Lebenszeit und Versorgung segnet.\n\n• Ruhe & Balance: Verleiht besonders zur Morgendämmerung tiefe innere Ruhe.\n\n• Schutz für die Ummah: Bringt göttliche Barmherzigkeit und wehrt Prüfungen ab.",
                "fr" to "• Pilier de la Foi: La racine de toutes les actions. Érige un pilier inébranlable dans le cœur et dissipe les doutes.\n\n• Bénédiction: Une lumière salvatrice qui bénit la vie et la subsistance.\n\n• Sérénité: Procure une quiétude profonde au corps et à l'esprit à l'aube.\n\n• Bouclier pour la communauté: Attire la miséricorde divine et repousse les épreuves."
            )
        ),
        ZikirDefinition(
            id = 2,
            defaultTarget = 100000L,
            arabicText = "الله",
            names = mapOf(
                "tr" to "Allah",
                "ar" to "الله",
                "en" to "Allah",
                "de" to "Allah",
                "fr" to "Allah"
            ),
            details = mapOf(
                "tr" to "• Nasrullah (İlahi Yardım): Darlık ve tıkanıklıkları açar; kula Cenab-ı Hakk’ın hususi yardım ve inayet kapılarını aralar.\n\n• Kalbi İtminan ve Doyum: Dünyevi tatminsizlikleri giderir; kalbe hiçbir fani şeyin veremeyeceği hakiki huzur ve doyumu yerleştirir.\n\n• Arınma ve Emniyet: Ruhu günah kirlerinden temizler; meleklerin şahitliğinde affa mazhar edip bedbahtlıktan (şekavetten) korur.\n\n• Hikmet ve Basiret: Kişiyi dar fikrinden kurtarır; kalpteki basiret aynasını parlatarak olayların ardındaki hikmeti gösterir.\n\n• Şifa ve Rahmet: Manevi yaralara şifadır; müslümanı hem kendisi hem tüm ümmet için bir rahmet ve nur vesilesi kılar.\n\nHülasa: Kulu nefsin zindanından çıkarıp kalbi doğrudan Zat-ı Akdes’e bağlayan tecellilerin membaıdır.",
                "ar" to "• النصرة والمدد الإلهي: يفتح أبواب المعونة الخاصة والتأييد الرباني في الشدائد.\n\n• طمأنينة القلب: يملأ الفؤاد بسكينة وشبع روحي لا تبلغه المتاع الفانية.\n\n• التطهير والأمان: يطهر النفس من الذنوب ويحفظ العبد من الشقاوة بشهادة الملائكة.\n\n• الحكمة ونور البصيرة: يحرر الفكر من الضيق ويجلو مرآة البصيرة في القلب.\n\n• شفاء ورحمة للأمة: دواء للأدواء القلبية ونور يدفع المكاره عن الأمة.",
                "en" to "• Divine Aid (Nasrullah): Clears spiritual and worldly obstacles, opening doors of special divine assistance.\n\n• Inner Fulfillment: Eliminates worldly dissatisfaction, anchoring true serenity and spiritual peace in the heart.\n\n• Purification & Safety: Cleanses the soul from sins and grants safety from despair under angelic witness.\n\n• Wisdom & Insight: Frees the mind from narrow thoughts, polishing the heart’s mirror to perceive divine realities.\n\n• Healing & Mercy: A cure for spiritual wounds, turning the believer into a source of mercy for the whole Ummah.",
                "de" to "• Göttlicher Beistand (Nasrullah): Löst Engpässe und öffnet Tore göttlicher Unterstützung.\n\n• Seelenfrieden: Verleiht dem Herzen wahre Erfüllung und tiefste Geborgenheit.\n\n• Reinigung & Schutz: Reinigt die Seele und schützt vor Verzweiflung.",
                "fr" to "• Secours Divin: Ouvre les portes de l'assistance et dissipe les tourments.\n\n• Paix Intérieure: Remplit le cœur d'une sérénité que nul bien éphémère ne peut apporter.\n\n• Purification: Purifie l'âme et accorde la sécurité sous le regard des anges."
            )
        ),
        ZikirDefinition(
            id = 3,
            defaultTarget = 100000L,
            arabicText = "هُوَ",
            names = mapOf(
                "tr" to "Hu",
                "ar" to "هُوَ",
                "en" to "Hu",
                "de" to "Hu",
                "fr" to "Hou"
            ),
            details = mapOf(
                "tr" to "• Ruhun Fıtri Arayışı: Kalp sadece biyolojik bir et parçası değildir; her atışı ruhun Rabbine olan fıtri zikridir. \"Hû\" zikri, dil sussa dahi kalbin bu vuruşlarını ilahi bir şuura dönüştürür.\n\n• İlahi Sinyal ve Kapı Çalma: Tasavvufta kalbin her atışı, vuslat kapısını çalan manevi bir tıkırtıdır. Ruh bu zikirle, gaflet perdelerini aşıp Cenab-ı Hakk’a doğru manevi sinyaller gönderir.\n\n• Vuslat Kapısının Açılması: Kalbin her bir \"Hû\" nidasıyla o kapıyı ısrarla çalması, bir gün vuslat kapısının aralanmasına ve Rabbü'l-Âlemîn’in marifetine ermeye vesiledir.\n\n• Benliğin Erimesi: Nefsin \"ben\" iddiasını yok eder; kulun her şeyde ve her nefeste yalnızca \"O\" (Hû) hakikatini müşahede etmesini sağlar.\n\nHülasa: Benliği eritip kalbin her atışını \"Hû\" sedasıyla vuslat kapısını çalan bir niyaza dönüştüren zattır.",
                "ar" to "• نداء الروح الفطري: كل نبضة قلب هي تسبيح باطني يبحث عن مولاه، وذكر \"هو\" يجعل هذا النبض حضوراً دائماً.\n\n• طرق باب الوصال: ضربات القلب طرق مستمر على أبواب الرحمة والفيوضات الإلهية.\n\n• فتح أبواب المعرفة: المداومة على هذا النداء تفتح مغاليق البصيرة وتورث العبد معرفة بالله.\n\n• فناء الأنانية: يذيب وهم \"الأنا\" ليشهد العبد حقيقة \"هو\" المطلقة في كل نفس.",
                "en" to "• Innate Seeking of the Soul: The heartbeat is the soul’s natural longing for its Creator; \"Hu\" transforms it into conscious devotion.\n\n• Knocking on the Door of Union: Every pulse acts as a spiritual knock on the door of divine intimacy.\n\n• Attaining Divine Proximity: Continuous yearning through \"Hu\" opens the gates to spiritual insight and divine gnosis.\n\n• Dissolution of the Ego: Eradicates selfish pride, allowing the servant to witness \"He\" (Hu) in all existence.",
                "de" to "• Ruf der Seele: Macht jeden Herzschlag zu einem bewussten Ruf nach der göttlichen Gegenwart.\n\n• Klopfen an die Pforte der Nähe: Beständiges Streben nach geistiger Erkenntnis und Hingabe.",
                "fr" to "• Appel Originel de l'Âme: Transforme les battements du cœur en conscience permanente.\n\n• Frapper à la Porte de l'Union: Dissout l'illusion de l'égo pour ne contempler que 'Lui'."
            )
        ),
        ZikirDefinition(
            id = 4,
            defaultTarget = 70000L,
            arabicText = "يَا حَقُّ",
            names = mapOf(
                "tr" to "Ya Hak",
                "ar" to "يَا حَقُّ",
                "en" to "Ya Haqq",
                "de" to "Ya Haqq",
                "fr" to "Ya Haqq"
            ),
            details = mapOf(
                "tr" to "• Hakkın İzharı ve İstikamet: Kalpteki bâtıl düşünceleri, vehimleri ve şüpheleri yok eder; mümine hakkı bâtıldan ayırma feraseti ve hakikat üzere sarsılmaz bir sebat verir.\n\n• Nasrullah ve İmkân Kapıları: Hak yolda yürüyen kulun önündeki engelleri kaldırır; Cenab-ı Hakk’ın \"Nasrullah\" denilen hususi yardım ve manevi fetih kapılarını açar.\n\n• Hikmet ve Fikri Hürriyet: Kişiyi kendi nefsinin dar zannından ve peşin hükümlerinden kurtarır; olayların perde arkasındaki ilahi hikmetleri idrak etmesini sağlar.\n\n• Arınma ve Hakikat Ehli Olma: Zikreden kulun kalbini günah kirlerinden arındırıp rahmet-i ilahiyeye kavuşturur.\n\nHülasa: Bâtılı zail edip kalpte Hakk’ın mutlak tecellisini izhar eden, kulu hakikat ve istikamet eri kılan celalli bir zikirdir.",
                "ar" to "• إظهار الحق والاستقامة: يمحو الباطل والأوهام، ويمنح المؤمن بصيرة تفرق بين الحق والباطل وثباتاً على الهدى.\n\n• النصرة والفتوح: يزيل العقبات من طريق السالكين ويفتح أبواب التأييد الإلهي.\n\n• الحكمة وانشراح الفكر: يحرر العقل من أسر الظنون الضيقة ليدرك الحكمة الإلهية في تصاريف الأمور.",
                "en" to "• Manifestation of Truth & Firmness: Eradicates falsehood and doubts, granting sharp spiritual discernment and steadfastness.\n\n• Divine Aid (Nasrullah): Clears hurdles on the path of righteousness and opens doors of divine support.\n\n• Wisdom & Freedom of Thought: Liberates the intellect from narrow personal prejudices to perceive divine wisdom.",
                "de" to "• Offenbarung der Wahrheit: Befreit von Illusionen und Zweifeln, schenkt spirituelle Klarheit und Standhaftigkeit.",
                "fr" to "• Manifestation de la Vérité: Élimine le faux et les doutes, accordant discernement et rectitude inébranlable."
            )
        ),
        ZikirDefinition(
            id = 5,
            defaultTarget = 70000L,
            arabicText = "يَا حَيُّ",
            names = mapOf(
                "tr" to "Ya Hay",
                "ar" to "يَا حَيُّ",
                "en" to "Ya Hayy",
                "de" to "Ya Hayy",
                "fr" to "Ya Hayy"
            ),
            details = mapOf(
                "tr" to "• Ruhun İhyası ve İlahi Dirilik: Gafletle katılaşmış veya pörsümüş kalpleri diriltir; ruha Cenab-ı Hakk’ın nefhasından gelen ebedi ve manevi bir canlılık bağışlar.\n\n• Hayrın Fethi ve Şerrin Def’i: Müslümanın hayrı anlayıp hayra hizmet etmesini sağlar; hayatındaki şer kapılarını kapatıp hayır kapılarını açar.\n\n• İbadette Aşk ve Zindelik: İbadet ve zikirlerdeki gafleti, tembelliği ve yorgunluğu siler; mümine daimi bir şuur, aşk ve ruhi uyanıklık hali verir.\n\nHülasa: Ölü kalpleri nefha-i ilahiye ile ihya eden, ruhu beden kafesinden kurtarıp ebedi hayat ve hayır kaynağına bağlayan diriliş ismidir.",
                "ar" to "• إحياء القلوب والنفوس: يحيي القلوب التي أثقلتها الغفلة، ويهب الروح حياة ونضارة معنوية دائمة.\n\n• فتح أبواب الخير ودفع الشر: يوفق العبد لفعل الخيرات وخدمتها ويغلق عنه أبواب السوء.\n\n• النشاط والشوق في العبادة: يطرد الكسل والفتور ويملأ الصدر حباً ويقظة في الطاعات.",
                "en" to "• Revival of the Soul: Restores hearts hardened by heedlessness, breathing divine vitality into the spirit.\n\n• Unlocking Goodness: Empowers one to live and serve goodness, closing paths of harm while opening doors of grace.\n\n• Zeal & Alertness in Worship: Banishes lethargy and spiritual fatigue, granting constant joy and alertness in devotion.",
                "de" to "• Wiederbelebung des Herzens: Schenkt dem Geist ewige Lebendigkeit und vertreibt geistige Trägheit.",
                "fr" to "• Revivification des Cœurs: Insuffle une vitalité spirituelle durable et dissipe la léthargie."
            )
        ),
        ZikirDefinition(
            id = 6,
            defaultTarget = 70000L,
            arabicText = "يَا قَيُّومُ",
            names = mapOf(
                "tr" to "Ya Kayyum",
                "ar" to "يَا قَيُّومُ",
                "en" to "Ya Qayyum",
                "de" to "Ya Qayyum",
                "fr" to "Ya Qayyum"
            ),
            details = mapOf(
                "tr" to "• Kayyumiyet ve Manevi Sebat: Her şeyi ayakta tutan ilahi kudret tecelli ederek, müslümanın iç dünyasındaki dağınıklığı toplar; kula sarsılmaz bir nizam ve irade gücü verir.\n\n• İhlas ve Benliği Terk: Amelleri gösterişten arındırıp sırf Allah rızası için yapma edebini kazandırır.\n\n• Mutlak Tevekkül ve İtminan: Müslümanı kendi dar aklının vesveselerinden kurtarır; bütün işlerini Kayyum-u Zülcelâl’e bırakmanın sonsuz huzurunu yaşatır.",
                "ar" to "• استقامة الباطن والثبات: يجمع شتات النفس ويمنح العبد ثباتاً ورسوخاً في مواجهة تقلبات الدنيا.\n\n• الإخلاص وتجريد النية: يربي العبد على عبادة الله طلباً لرضاه دون التطلع للمصالح الذاتية.\n\n• التوكل الحق: يريح القلب من هم التدبير ويلقي بأحمال الروح بين يدي القيوم.",
                "en" to "• Spiritual Order & Steadfastness: Restores order to inner turmoil, granting unshakable perseverance and spiritual willpower.\n\n• Pure Sincerity: Cleanses worship from self-serving desires, dedicating all acts purely for Allah.\n\n• Absolute Reliance: Relieves the burden of overthinking by anchoring trust completely in the Eternal Sustainer.",
                "de" to "• Ordnung und Standhaftigkeit: Sammelt die innere Zerstreutheit und stärkt den Willen zur Beständigkeit.",
                "fr" to "• Ordre et Fermeté: Rassemble l'esprit dispersé et ancre une confiance inébranlable dans le Soutien Universel."
            )
        ),
        ZikirDefinition(
            id = 7,
            defaultTarget = 70000L,
            arabicText = "يَا قَهَّارُ",
            names = mapOf(
                "tr" to "Ya Kahhar",
                "ar" to "يَا قَهَّارُ",
                "en" to "Ya Qahhar",
                "de" to "Ya Qahhar",
                "fr" to "Ya Qahhar"
            ),
            details = mapOf(
                "tr" to "• Nefs-i Emmârenin Kahrı: Nefsin kibir, gurur, şehvet ve gazap gibi azgın sıfatlarını kahreder; içteki en büyük düşmana galebe çalmayı sağlar.\n\n• Vesvese ve Şerlerin İptali: Kalbe musallat olan şeytani vesveseleri bertaraf ederek gönül hanesini temizler.\n\n• Ümmetin Mazlumlarına Kalkan: Şer odaklarının, fitne ve zulümlerin kahrolup defedilmesine manevi bir kalkan olur.",
                "ar" to "• قهر سطوة النفس: يقهر أهواء النفس الأمارة من كبر وغضب وشهوة، ويعين الروح على الانتصار عليها.\n\n• إبطال كيد الوساوس: يدحر وساوس الشياطين ويزيل الظلمات عن القلب.\n\n• نصرة المظلومين: يدفع كيد الظلم والفتن عن أمة الإسلام.",
                "en" to "• Subduing the Lower Self: Overcomes the rebellious vices of the ego (arrogance, wrath), granting inner spiritual mastery.\n\n• Eradicating Satanic Whispers: Annihilates dark thoughts and spiritual sabotage, cleansing the inner sanctum.\n\n• Shield for the Oppressed: Serves as a fortress against deception and tribulations.",
                "de" to "• Bezwingung des niederen Egos: Bricht Hochmut und Zorn und reinigt das Herz von Einflüsterungen.",
                "fr" to "• Domptage de l'Égo: Éradique l'orgueil et les passions néfastes, purifiant le sanctuaire du cœur."
            )
        ),
        ZikirDefinition(
            id = 8,
            defaultTarget = 70000L,
            arabicText = "يَا وَاحِدُ",
            names = mapOf(
                "tr" to "Ya Vahid",
                "ar" to "يَا وَاحِدُ",
                "en" to "Ya Wahid",
                "de" to "Ya Wahid",
                "fr" to "Ya Wahid"
            ),
            details = mapOf(
                "tr" to "• Vahdet ve Kalbi Birlik: Kalpteki dünyevi dağınıklığı ve kafa karışıklığını giderir; müslümanın gönlünü tek bir gayeye, Allah’ın rızasına odaklar.\n\n• Şirkten ve Şüphelerden Arınma: Sebeplere aşırı takılmayı ve gizli şirki siler; sarsılmaz bir tevhid inancı kazandırır.\n\n• Ümmetin Birliğine Maya: Tefrika ve fitneleri dağıtacak manevi bir birlik ve vahdet şuuru aşılar.",
                "ar" to "• جمع الهم في الوحدة: يزيل تشتت القلب بين المخلوقات، ويوجه الوجهة كلها إلى الله الواحد.\n\n• نقاء العقيدة والإخلاص: يطهر الباطن من التعلق بالأسباب ومن شوائب الرياء والشك.\n\n• تأليف قلوب الأمة: ينشر نور التوحيد ليجمع شمل الأمة ويدفع التنازع.",
                "en" to "• Unity & Focus of Heart: Dissolves mental chaos and worldly distractions, centering the heart entirely on Allah’s pleasure.\n\n• Purity from Hidden Shirk: Cleanses reliance on secondary causes, bestowing pure monotheism.\n\n• Unity for the Community: Radiates monotheistic light that heals division across the faithful.",
                "de" to "• Einheit des Herzens: Löst geistige Zerstreuung und richtet das Streben ganz auf den Einen Schöpfer aus.",
                "fr" to "• Unité du Cœur: Dissipe la dispersion mentale et oriente l'intention uniquement vers l'Agrément divin."
            )
        ),
        ZikirDefinition(
            id = 9,
            defaultTarget = 70000L,
            arabicText = "يَا عَزِيزُ",
            names = mapOf(
                "tr" to "Ya Aziz",
                "ar" to "يَا عَزِيزُ",
                "en" to "Ya Aziz",
                "de" to "Ya Aziz",
                "fr" to "Ya Aziz"
            ),
            details = mapOf(
                "tr" to "• İzzet ve Manevi Vakar: Müslümana hakiki bir vakar ihsan eder; fani insanlara boyun büküp zelil olmaktan korur.\n\n• Mağlup Edilemez Manevi Güç: Mutlak galip olan el-Azîz’e bağlanan kalbe sarsılmaz bir cesaret aşılar; haksızlıklara karşı eğilmeyen bir duruş verir.\n\n• Benliksiz Asalet: İzzeti nefiste değil, yalnızca Allah’a hakiki kullukta arama edebini yerleştirir.",
                "ar" to "• العزة الإيمانية والوقار: يمنح المؤمن هيبة ومهابة ويحميه من ذل التبعية للمخلوقين.\n\n• الثبات واليقين: يورث شجاعة ويقيناً لأن العزة لله جميعاً.\n\n• عزة العبودية: يعلم العبد أن الشرف الحقيقي في التذلل لله وحده.",
                "en" to "• Spiritual Dignity & Majesty: Bestows noble composure, protecting the soul from degrading itself before creation.\n\n• Invincible Fortitude: Instills unwavering spiritual courage against falsehood and adversity through connection with the All-Mighty.\n\n• Noble Servitude: Teaches that true honor lies in humble servitude to God.",
                "de" to "• Würde und Stärke: Schützt vor Erniedrigung vor Geschöpfen und verleiht echten seelischen Mut.",
                "fr" to "• Noblesse et Dignité: Préserve l'âme de l'avilissement et inspire une force spirituelle invincible."
            )
        ),
        ZikirDefinition(
            id = 10,
            defaultTarget = 70000L,
            arabicText = "يَا وَدُودُ",
            names = mapOf(
                "tr" to "Ya Vedud",
                "ar" to "يَا وَدُودُ",
                "en" to "Ya Wadud",
                "de" to "Ya Wadud",
                "fr" to "Ya Wadoud"
            ),
            details = mapOf(
                "tr" to "• İlahi Muhabbet ve Gönül İmarı: Kalbe Cenab-ı Hakk’ın sevgisini nakşeder; müslümanı hem Allah’ı seven hem de Rabbi ve salih kullar tarafından sevilen bir muhabbet ehli kılar.\n\n• Gelecek Endişesi ve Korkulara Şifa: Yarın ne olacağına dair içsel kaygıları dindirir; kalbi ilahi sevginin mutlak güvenine teslim eder.\n\n• Mahlukata Şefkat ve Hilm: Gönüldeki katılığı, kini ve nefreti eritir; tüm mahlukata derin bir şefkat ve nezaketle bakmayı sağlar.",
                "ar" to "• محبة الله وسكينة الروح: يفيض على القلب وداداً وقرباً من الله، ويجعل العبد مقبولاً ومحبوباً.\n\n• طرد القلق والخوف: يزيل الخوف من المجهول ويملأ الفؤاد ثقة برحمة الله وعنايته.\n\n• الرحمة والشفقة بالخلق: ينزع الغل والقسوة ويجعل المؤمن هيناً ليناً رفيقاً بالجميع.",
                "en" to "• Divine Love & Heart’s Solace: Engraves deep divine love in the heart, making one beloved to God and the righteous.\n\n• Relief from Future Anxiety: Dissolves fear of the unknown, surrendering worries to the boundless love and care of God.\n\n• Compassion & Mercy: Softens inner hardness, inspiring gentle empathy and kindness toward all beings.",
                "de" to "• Göttliche Liebe: Erfüllt das Herz mit Hingabe und Zuneigung, löst Zukunftsängste und Bitterkeit.",
                "fr" to "• Amour Divin et Douceur: Adoucit le cœur, dissipe les angoisses du lendemain et inspire une bienveillance universelle."
            )
        ),
        ZikirDefinition(
            id = 11,
            defaultTarget = 60000L,
            arabicText = "يَا وَهَّابُ",
            names = mapOf(
                "tr" to "Ya Vahhab",
                "ar" to "يَا وَهَّابُ",
                "en" to "Ya Wahhab",
                "de" to "Ya Wahhab",
                "fr" to "Ya Wahhab"
            ),
            details = mapOf(
                "tr" to "• Karşılıksız İhsan ve Lütuf: Cenab-ı Hakk’ın hesapsız bağışlayan tecellisini celbeder; gayb hazinelerinden umulmadık manevi lütuflar ve rızık genişliği kapıları açar.\n\n• Ledünni İlim ve Gönül Fethi: Çaba ve aklın ötesinde kalbe hikmet, feyiz ve manevi ilhamlar akıtır.\n\n• Gönül Zenginliği ve Cömertlik: Nefsteki cimrilik ve tamahı eritir; karşılıksız veren, cömert ve gani bir ahlak kazandırır.",
                "ar" to "• العطاء الإلهي بلا حساب: يجلب الفتوحات الإلهية والمنح الربانية من خزائن الغيب الواسعة.\n\n• العلم اللدني والحكمة: يفيض على القلب بنور الفهم الصادق والإلهام الرباني.\n\n• غنى النفس والجود: ينفي الشح والحرص ويجعل المؤمن جواداً كريماً باذلاً للخير.",
                "en" to "• Unconditional Divine Gifts: Attracts limitless divine bestowals, opening unseen doors of spiritual illumination and provision.\n\n• Spiritual Insight & Knowledge: Pours divine wisdom and intuitive understanding into the receptive heart.\n\n• Generosity & Contentment: Eradicates stinginess, cultivating noble altruism and richness of soul.",
                "de" to "• Bedingungslose göttliche Gaben: Öffnet Tore unbegrenzter Wohltaten, Weisheit und Großzügigkeit der Seele.",
                "fr" to "• Dons Divins Inconditionnels: Ouvre les trésors de la sagesse et de la générosité sans limites."
            )
        ),
        ZikirDefinition(
            id = 12,
            defaultTarget = 50000L,
            arabicText = "يَا مُهَيْمِنُ",
            names = mapOf(
                "tr" to "Ya Müheymin",
                "ar" to "يَا مُهَيْمِنُ",
                "en" to "Ya Muhaymin",
                "de" to "Ya Muhaymin",
                "fr" to "Ya Mouhaymin"
            ),
            details = mapOf(
                "tr" to "• Murakabe ve İhsan Şuuru: Kalbe Cenab-ı Hakk’ın her an kendisini görüp gözettiği idrakini yerleştirir; daimi bir haşyet, edep ve haya hali verir.\n\n• Manevi Himaye ve Emniyet: Kulu görünen ve görünmeyen tehlikelerden korur; kalbe sarsılmaz bir emniyet ve ilahi sığınak bahşeder.\n\n• Kalbi Sükûnet: Yalnızlık hissini ve korkuları dindirir; kulun her an Rabbinin şefkatli muhafazası altında olduğunu hissettirir.",
                "ar" to "• مقام المراقبة والحياء: يرسخ في القلب يقين المعية الإلهية فيورث الخشية والحياء من المعاصي.\n\n• الحصن والأمان من المخاوف: يحفظ الذاكر من كيد الشياطين والوساوس ويبث الطمأنينة في صدره.\n\n• السكينة الباطنية: يزيل وحشة النفس ويشعر العبد برعاية الله الدائمة له.",
                "en" to "• Station of Watchfulness (Muraqabah): Instills deep consciousness of divine observation, fostering reverent modesty and piety.\n\n• Divine Sanctuary & Protection: Shields the believer from unseen perils, doubts, and inner fears.\n\n• Inner Security: Banishes loneliness, reassuring the soul of God’s constant compassionate protection.",
                "de" to "• Achtsamkeit und Schutz (Muraqabah): Vertieft das Bewusstsein der steten göttlichen Aufsicht und Geborgenheit.",
                "fr" to "• Vigilance et Protection: Ancre la conscience de la présence divine et procure une protection totale contre les craintes."
            )
        ),
        ZikirDefinition(
            id = 13,
            defaultTarget = 40000L,
            arabicText = "يَا بَاسِطُ",
            names = mapOf(
                "tr" to "Ya Basit",
                "ar" to "يَا بَاسِطُ",
                "en" to "Ya Basit",
                "de" to "Ya Basit",
                "fr" to "Ya Basit"
            ),
            details = mapOf(
                "tr" to "• İnşirah ve Ruhi Genişlik (Bast Hali): Gönüldeki darlıkları, kasveti ve kederleri dağıtır; kalbe tarif edilmez bir ferahlık ve sevinç tecelli ettirir.\n\n• Maddi ve Manevi Rızık Bolluğu: Kulun rızkındaki ve kalbindeki feyiz ve ilim kapılarını genişletir.\n\n• Geniş Gönüllülük ve Rıza: Nefsi dar görüşlülükten kurtarır; her halde Rabbine teslim olan geniş yürekli bir ahlaka kavuşturur.",
                "ar" to "• انشراح الصدر وبسط الروح: يكشف ضيق الصدر والهموم ويبدلها بسعة وفرح وسرور باطني.\n\n• بركة الأرزاق والعلوم: يوسع في الأرزاق الظاهرة ويفجر ينابيع الفهم والمعارف في القلب.\n\n• انشراح الفؤاد: يريح النفس من الحزن ويمنحها رضا وتسليماً كاملاً لله.",
                "en" to "• Spiritual Expansion & Joy (Bast): Dispels sorrow and spiritual constriction, filling the heart with vast joy and inner liberation.\n\n• Abundance in Provision & Wisdom: Multiplies outward blessings and inward fountains of wisdom.\n\n• Generous Soul: Frees the self from narrow greed, cultivating contentment and broad-minded surrender.",
                "de" to "• Weite und Freude der Seele (Bast): Löst Beklemmung und Sorgen und bringt seelische Erleichterung.",
                "fr" to "• Expansion et Joie Spirituelle: Dissipe les angoisses et la tristesse, apportant une immense délivrance intérieure."
            )
        ),
        ZikirDefinition(
            id = 14,
            defaultTarget = 100000L,
            arabicText = "يَا رَحْمٰنُ",
            names = mapOf(
                "tr" to "Ya Rahman",
                "ar" to "يَا رَحْمٰنُ",
                "en" to "Ya Rahman",
                "de" to "Ya Rahman",
                "fr" to "Ya Rahman"
            ),
            details = mapOf(
                "tr" to "• İlmin Şehri ve Hikmet Pınarı: Kalbi hakikatlere açar; Resûl-i Kibriyâ Efendimiz’in (s.a.v.) ilahi ilminden ve feyzinden kalbe pencereler aralar.\n\n• İmanı ve Dini Muhafaza: Müslümanın gönlündeki imanı korur; fitnelerden ve hilelerden muhafaza eder.\n\n• Kuşatıcı Şefkat ve Hilm: Kulun içindeki öfke ve katılıktan eser bırakmaz; bütün mahlukatı kucaklayan evrensel bir merhamet ahlakı kazandırır.",
                "ar" to "• مدينة العلم والحقائق: يفتح القلب على معارف النبوة وأسرار العلوم الإلهية اللدنية.\n\n• صيانة الدين والإيمان: يثبت الإيمان في الصدور ويحفظ الشريعة وأهلها من الفتن.\n\n• الرحمة العامة بالحلم: يطرد الغضب والفظاظة، ويورث المسلم خُلق الرفق والرحمة بالجميع.",
                "en" to "• City of Knowledge & Wisdom: Connects the soul to prophetic knowledge and profound divine insights.\n\n• Guardian of Faith & Truth: Preserves personal faith and protects the sacred truth from corruption and trials.\n\n• Universal Compassion: Dispels anger, bestowing gentle forbearance and mercy toward all of creation.",
                "de" to "• Quelle des Wissens und der universellen Barmherzigkeit: Schützt den Glauben und verwandelt Härte in Sanftmut.",
                "fr" to "• Source de Connaissance et Miséricorde Universelle: Préserve la foi et transforme la rigueur en douceur envers toute créature."
            )
        ),
        ZikirDefinition(
            id = 15,
            defaultTarget = 100000L,
            arabicText = "يَا رَحِيمُ",
            names = mapOf(
                "tr" to "Ya Rahim",
                "ar" to "يَا رَحِيمُ",
                "en" to "Ya Raheem",
                "de" to "Ya Raheem",
                "fr" to "Ya Rahim"
            ),
            details = mapOf(
                "tr" to "• Acziyet İdraki ve Tevazu: Kulun kendi noksanlığını fark ettirir; kibri kırarak hakiki teslimiyet ve tevazu kazandırır.\n\n• Peygamberî Şefkat ve Ahlak: Resûl-i Ekrem Efendimiz’in (s.a.v.) \"Rauf ve Rahim\" ahlakından hisse bahşeder; mümin kardeşlerine karşı derin bir merhamet ve koruyuculuk kazandırır.\n\n• Müminlere Has Hususi Lütuf: Ahirette ve dünyada vaat edilen hususi mağfiret ve ebedi selamet kapılarını açar.\n\n• Manevi Mescit ve Ev Şuuru: Haneyi ve kalbi sekine, bereket ve meleklerin indiği manevi sığınaklara dönüştürür.",
                "ar" to "• الانكسار والافتقار المحمود: يكسر كبرياء النفس ويذيق القلب حلاوة التواضع والافتقار إلى الله.\n\n• التخلق بالرحمة المحمدية: يورث القلب نصيباً من خلق النبي ﷺ الرؤوف الرحيم بالمؤمنين.\n\n• اللطف الخاص بالمؤمنين: يفتح خزائن المغفرة الخاصة لأهل الإيمان.\n\n• بركة البيوت والمساجد: يجعل بيوت الذاكرين وقلوبهم مهبطاً للسكينة والملائكة والرضوان.",
                "en" to "• Humility & Awareness of Need: Breaks the ego’s pride, elevating the servant to sincere humility and complete surrender to Allah.\n\n• Prophetic Compassion: Bestows a share of the Prophet’s ﷺ merciful nature (Rauf & Rahim), fostering gentle love for believers.\n\n• Special Grace for the Faithful: Unlocks the special salvation and forgiveness reserved for sincere believers.\n\n• Sanctuary in Homes & Hearts: Transforms one’s home and soul into serene sanctuaries visited by angels and divine peace.",
                "de" to "• Besondere göttliche Gnade und Demut: Bricht den Stolz des Egos und füllt das Haus mit Segen und Engeln.",
                "fr" to "• Grâce Spéciale et Humilité: Brise l'orgueil de l'égo et transforme le foyer et le cœur en sanctuaires de sérénité et de bénédictions."
            )
        )
    )

    fun getZikirDefinition(id: Int): ZikirDefinition {
        return INITIAL_DEFINITIONS.find { it.id == id } ?: INITIAL_DEFINITIONS[0]
    }

    fun getZikirName(id: Int, lang: String): String {
        val def = getZikirDefinition(id)
        return def.names[lang] ?: def.names["tr"] ?: "Zikir $id"
    }

    fun getZikirDetail(id: Int, lang: String): String {
        val def = getZikirDefinition(id)
        return def.details[lang] ?: def.details["tr"] ?: ""
    }

    fun getArabicText(id: Int): String {
        return getZikirDefinition(id).arabicText
    }

    fun getZikirTransliteration(id: Int, lang: String = "tr"): String {
        return when (lang) {
            "ar" -> when (id) {
                1 -> "لَا إِلٰهَ إِلَّا الله"
                2 -> "الله"
                3 -> "هُوَ"
                4 -> "يَا حَقُّ"
                5 -> "يَا حَيُّ"
                6 -> "يَا قَيُّومُ"
                7 -> "يَا قَهَّارُ"
                8 -> "يَا وَاحِدُ"
                9 -> "يَا عَزِيزُ"
                10 -> "يَا وَدُودُ"
                11 -> "يَا وَهَّابُ"
                12 -> "يَا مُهَيْمِنُ"
                13 -> "يَا بَاسِطُ"
                14 -> "يَا رَحْمٰنُ"
                15 -> "يَا رَحِيمُ"
                else -> ""
            }
            "en", "de" -> when (id) {
                1 -> "La ilaha illallah"
                2 -> "Allah"
                3 -> "Hu"
                4 -> "Ya Haqq"
                5 -> "Ya Hayy"
                6 -> "Ya Qayyum"
                7 -> "Ya Qahhar"
                8 -> "Ya Wahid"
                9 -> "Ya Aziz"
                10 -> "Ya Wadud"
                11 -> "Ya Wahhab"
                12 -> "Ya Muhaymin"
                13 -> "Ya Basit"
                14 -> "Ya Rahman"
                15 -> "Ya Raheem"
                else -> ""
            }
            "fr" -> when (id) {
                1 -> "La ilaha illallah"
                2 -> "Allah"
                3 -> "Hou"
                4 -> "Ya Haqq"
                5 -> "Ya Hayy"
                6 -> "Ya Qayyum"
                7 -> "Ya Qahhar"
                8 -> "Ya Wahid"
                9 -> "Ya Aziz"
                10 -> "Ya Wadoud"
                11 -> "Ya Wahhab"
                12 -> "Ya Mouhaymin"
                13 -> "Ya Basit"
                14 -> "Ya Rahman"
                15 -> "Ya Rahim"
                else -> ""
            }
            else -> when (id) {
                1 -> "Lâ ilâhe illallâh"
                2 -> "Allâh"
                3 -> "Hû"
                4 -> "Yâ Hak"
                5 -> "Yâ Hayy"
                6 -> "Yâ Kayyûm"
                7 -> "Yâ Kahhâr"
                8 -> "Yâ Vâhid"
                9 -> "Yâ Azîz"
                10 -> "Yâ Vedûd"
                11 -> "Yâ Vahhâb"
                12 -> "Yâ Müheymin"
                13 -> "Yâ Bâsıt"
                14 -> "Yâ Rahmân"
                15 -> "Yâ Rahîm"
                else -> ""
            }
        }
    }
}
