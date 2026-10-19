package ir.daneshnet.overtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val Navy = Color(0xFF17324D)
private val Blue = Color(0xFF2F5D7C)
private val Surface = Color(0xFFF7F8FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OvertimeApp() }
    }
}

@Composable
fun OvertimeApp() {
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, secondary = Blue, background = Surface, surface = Color.White)) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Surface,
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        listOf("امروز", "سوابق", "گزارش", "تنظیمات").forEachIndexed { i, label ->
                            NavigationBarItem(tab == i, { tab = i },
                                icon = { Text(listOf("⌂","▣","▤","⚙")[i], fontSize = 19.sp) },
                                label = { Text(label) })
                        }
                    }
                }
            ) { padding ->
                when (tab) {
                    0 -> TodayScreen(Modifier.padding(padding))
                    1 -> SimpleScreen("سوابق", "رکوردهای روزانه اینجا نمایش داده می‌شوند.", Modifier.padding(padding))
                    2 -> SimpleScreen("گزارش ماهانه", "مجموع اضافه‌کاری، کسری و خالص ماهانه.", Modifier.padding(padding))
                    else -> SimpleScreen("تنظیمات", "قوانین ساعت کاری و اعلان‌ها.", Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(modifier: Modifier) {
    var entry by remember { mutableStateOf("") }
    var exit by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<WorkResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val dayNames = listOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه")
    val dayName = dayNames[LocalDate.now().dayOfWeek.value % 7]

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(20.dp))
            Text("محاسبه‌گر اضافه‌کاری", color = Navy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("$dayName  •  امروز", color = Color.Gray)
        }
        item {
            ElevatedCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("وضعیت امروز", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TimeField("ساعت ورود", entry) { entry = it }
                        TimeField("ساعت خروج", exit) { exit = it }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = {
                        val e = parse(entry); val x = parse(exit)
                        error = null
                        if (e == null || x == null) error = "ساعت را به شکل ۰۷:۴۵ وارد کنید."
                        else result = runCatching { WorkCalculator.calculate(e, x, dayName == "پنجشنبه") }
                            .onFailure { error = it.message ?: "خطا در محاسبه" }.getOrNull()
                    }, modifier = Modifier.fillMaxWidth()) { Text("محاسبه") }
                }
            }
        }
        result?.let { r -> item { ResultCard(r) } }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("قانون فعلی", fontWeight = FontWeight.Bold)
                    Text("شروع ۰۷:۳۰  |  شناوری ۳۰ دقیقه  |  موظفی ۰۶:۳۰", color = Color.Gray)
                    Text("پنجشنبه: تمام حضور = اضافه‌کاری", color = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, placeholder = { Text("۰۷:۴۵") },
        singleLine = true, modifier = Modifier.weight(1f))
}

private fun parse(value: String): Int? {
    val n = value.trim().map { c ->
        when (c) {
            '۰' -> '0'; '۱' -> '1'; '۲' -> '2'; '۳' -> '3'; '۴' -> '4'
            '۵' -> '5'; '۶' -> '6'; '۷' -> '7'; '۸' -> '8'; '۹' -> '9'; else -> c
        }
    }.joinToString("")
    val p = n.split(":")
    if (p.size != 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h * 60 + m else null
}

@Composable
private fun ResultCard(r: WorkResult) {
    ElevatedCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(r.status, color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            r.requiredEnd?.let { Text("پایان موظفی: ${format(it)}") }
            Metric("کارکرد امروز", r.workedMinutes)
            Metric("اضافه‌کاری", r.overtimeMinutes)
            Metric("کسری", r.shortageMinutes)
        }
    }
}

@Composable
private fun Metric(label: String, minutes: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.DarkGray)
        Text(format(minutes), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SimpleScreen(title: String, subtitle: String, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(28.dp))
        Text(title, color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Color.Gray)
    }
}

private fun format(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
