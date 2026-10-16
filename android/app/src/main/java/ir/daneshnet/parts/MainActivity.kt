package ir.daneshnet.parts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF17324D)
private val Bg = Color(0xFFF5F7F9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PartsPriceApp() }
    }
}

@Composable
fun PartsPriceApp() {
    var query by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var searched by remember { mutableStateOf(false) }
    var sources by remember { mutableStateOf(listOf("ایساکو / منبع رسمی", "منابع فروشگاهی", "تأمین‌کننده‌های من")) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, background = Bg)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Bg,
                bottomBar = {
                    NavigationBar {
                        listOf("قیمت‌یابی", "منابع", "تاریخچه").forEachIndexed { i, label ->
                            NavigationBarItem(
                                selected = tab == i, onClick = { tab = i },
                                icon = { Text(listOf("⌕", "◉", "▣")[i]) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            ) { pad ->
                when (tab) {
                    0 -> SearchScreen(query, { query = it }, searched, { searched = true }, pad)
                    1 -> SourcesScreen(link, { link = it }, sources, { sources = sources + it }, pad)
                    else -> HistoryScreen(pad)
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    query: String, onQuery: (String) -> Unit, searched: Boolean,
    onSearch: () -> Unit, pad: PaddingValues
) {
    LazyColumn(Modifier.padding(pad).fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("دستیار قیمت لوازم یدکی", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("قیمت روز را از منابع متصل بررسی کن", color = Color.Gray)
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("نام قطعه، خودرو یا شماره فنی") },
                placeholder = { Text("مثلاً لنت جلو پژو ۲۰۶ تیپ ۵") }
            )
        }
        item {
            Button(onClick = onSearch, enabled = query.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("جستجوی هوشمند قیمت")
            }
        }
        if (searched) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("نتیجه بررسی", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text(query)
                        HorizontalDivider()
                        Text("در حال اتصال به منابع قیمت...")
                        Text("هیچ قیمت حدسی نمایش داده نمی‌شود.", color = Color.Gray)
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("منطق قیمت‌گذاری", fontWeight = FontWeight.Bold)
                    Text("تطبیق قطعه، تازگی قیمت، اعتبار منبع و قیمت‌های پرت بررسی می‌شوند.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SourcesScreen(
    link: String, onLink: (String) -> Unit, sources: List<String>, add: (String) -> Unit, pad: PaddingValues
) {
    LazyColumn(Modifier.padding(pad).fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("منابع قیمت", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("لینک یک منبع عمومی را اضافه کن؛ نوع منبع خودکار تشخیص داده می‌شود.", color = Color.Gray)
        }
        item {
            OutlinedTextField(link, onLink, Modifier.fillMaxWidth(), singleLine = true, label = { Text("لینک کانال / سایت") })
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (link.isNotBlank()) {
                    val type = when {
                        link.contains("t.me/") -> "تلگرام"
                        link.contains("rubika.ir/") -> "روبیکا"
                        link.contains("whatsapp.com/channel/") -> "واتساپ"
                        else -> "وب"
                    }
                    add("$type • $link")
                    onLink("")
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = link.isNotBlank()) { Text("افزودن منبع") }
        }
        items(sources) { source ->
            ListItem(headlineContent = { Text(source) }, supportingContent = { Text("فعال • بروزرسانی خودکار") })
        }
    }
}

@Composable
private fun HistoryScreen(pad: PaddingValues) {
    Column(Modifier.padding(pad).fillMaxSize().padding(20.dp)) {
        Text("تاریخچه قیمت", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(Modifier.height(8.dp))
        Text("جست‌وجوهای انجام‌شده و تغییرات قیمت اینجا ذخیره خواهند شد.", color = Color.Gray)
    }
}
