package ir.maamay.roozaneh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ivory = Color(0xFFFFF8ED)
private val lapis = Color(0xFF183B68)
private val lapisLight = Color(0xFF315F91)
private val turquoise = Color(0xFF168F91)
private val saffron = Color(0xFFF2BC55)
private val pomegranate = Color(0xFFC74752)
private val ink = Color(0xFF1F2C45)
private val muted = Color(0xFF748094)
private val paleTurquoise = Color(0xFFDDF2EC)
private val paleGold = Color(0xFFFFEFCB)
private val paleRose = Color(0xFFFBE4E3)
private val paleBlue = Color(0xFFE4EAF5)

data class Puzzle(val category: String, val question: String, val options: List<String>, val answer: Int, val explanation: String)

private val base = listOf(
    Puzzle("عدد", "عدد بعدی چیست؟ ۲، ۴، ۶، ۸، ؟", listOf("۹", "۱۰", "۱۲", "۱۴"), 1, "هر بار ۲ واحد اضافه می‌شود."),
    Puzzle("منطق", "در مسابقه از نفر دوم سبقت می‌گیری. حالا چندمی؟", listOf("اول", "دوم", "سوم", "چهارم"), 1, "جای نفر دوم را می‌گیری، پس دوم می‌شوی."),
    Puzzle("کلمه", "کدام مورد متفاوت است؟", listOf("قرمز", "آبی", "سبز", "هفت"), 3, "سه گزینه رنگ‌اند؛ هفت یک عدد است."),
    Puzzle("منطق", "دو پدر و دو پسر سه سیب دارند؛ هر نفر یک سیب می‌گیرد. چند نفرند؟", listOf("۲", "۳", "۴", "۵"), 1, "سه نفرند: پدربزرگ، پدر و پسر."),
    Puzzle("عدد", "۵ + ۳ × ۲ چند می‌شود؟", listOf("۱۶", "۱۱", "۱۳", "۱۰"), 1, "ابتدا ضرب: ۵ + ۶ = ۱۱."),
    Puzzle("کلمه", "کدام یک جانور نیست؟", listOf("گنجشک", "لاک‌پشت", "پروانه", "کاج"), 3, "کاج یک درخت است."),
    Puzzle("منطق", "اگر امروز دوشنبه باشد، سه روز بعد چه روزی است؟", listOf("چهارشنبه", "پنجشنبه", "جمعه", "شنبه"), 1, "سه روز بعد پنجشنبه است."),
    Puzzle("عدد", "نصف ۱۸ به‌علاوه ۲ چند است؟", listOf("۹", "۱۱", "۲۰", "۷"), 1, "۹ + ۲ = ۱۱."),
    Puzzle("کلمه", "کدام مورد وسیله نقلیه نیست؟", listOf("قطار", "دوچرخه", "هواپیما", "پنجره"), 3, "پنجره وسیله نقلیه نیست."),
    Puzzle("منطق", "مریم از سارا بلندتر و سارا از ندا بلندتر است. کوتاه‌ترین کیست؟", listOf("مریم", "سارا", "ندا", "مشخص نیست"), 2, "ندا کوتاه‌ترین است.")
)
private val puzzles = (1..100).map { base[(it - 1) % base.size] }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    var page by remember { mutableStateOf("home") }
    var idx by remember { mutableIntStateOf(0) }
    var picked by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var hint by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = lapis, secondary = turquoise, tertiary = saffron,
            background = ivory, surface = Color.White, onBackground = ink, onSurface = ink
        )
    ) {
        Scaffold(
            containerColor = ivory,
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    NavigationBarItem(page == "home", { page = "home" }, icon = { Text("🏛️", fontSize = 20.sp) }, label = { Text("خانه") })
                    NavigationBarItem(page == "levels" || page == "play", { page = "levels" }, icon = { Text("🧩", fontSize = 20.sp) }, label = { Text("مراحل") })
                    NavigationBarItem(page == "profile", { page = "profile" }, icon = { Text("🏆", fontSize = 20.sp) }, label = { Text("پیشرفت") })
                }
            }
        ) { pad ->
            when (page) {
                "home" -> Home(pad, score, { idx = 0; picked = -1; hint = false; page = "play" }, { page = "levels" })
                "play" -> Play(pad, puzzles[idx], idx, picked, score, hint,
                    { n -> if (picked == -1) { picked = n; if (n == puzzles[idx].answer) score += 10 } },
                    { hint = true },
                    { idx = (idx + 1) % puzzles.size; picked = -1; hint = false }
                )
                "levels" -> Levels(pad) { idx = it; picked = -1; hint = false; page = "play" }
                else -> Profile(pad, score, idx)
            }
        }
    }
}

@Composable
private fun Home(pad: PaddingValues, score: Int, start: () -> Unit, all: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("سلام، ذهن کنجکاو! 👋", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = lapis)
                    Text("هر روز یک قدم باهوش‌تر شو", fontSize = 14.sp, color = muted)
                    Text("✦  خرد • حماسه • ایران  ✦", fontSize = 11.sp, color = turquoise, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.padding(start = 10.dp).size(72.dp).clip(CircleShape).border(2.dp, saffron, CircleShape)) {
                    Image(
                        painter = painterResource(R.drawable.hero_persian),
                        contentDescription = "سیمرغ",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.CenterStart
                    )
                }
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(232.dp).clip(RoundedCornerShape(26.dp))) {
                Image(
                    painter = painterResource(R.drawable.hero_persian),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(Color.Transparent, Color(0x33102D54), Color(0xAA102D54)))
                    )
                )
                Column(
                    Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.62f).padding(horizontal = 15.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Surface(color = Color(0xCC315F91), shape = RoundedCornerShape(16.dp)) {
                        Text("✦  چالش امروز  ✦", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("آماده‌ای ذهنت رو قلقلک بدی؟", fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.End)
                    Text("۱۰۰ مرحله سرگرم‌کننده", fontSize = 13.sp, color = Color.White.copy(alpha = 0.94f), fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = start,
                        colors = ButtonDefaults.buttonColors(containerColor = saffron, contentColor = lapis),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 9.dp)
                    ) {
                        Text("←  شروع معما", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HeritageStatCard("معماها", "۱۰۰", HeritageRegion.King, paleTurquoise, Modifier.weight(1f))
                HeritageStatCard("امتیاز", score.toString().map { ch -> if (ch in '0'..'9') ('۰'.code + (ch - '0')).toChar() else ch }.joinToString(""), HeritageRegion.Tower, paleGold, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("✦  ❈  ✦", fontSize = 15.sp, color = saffron)
                Text("دسته‌بندی‌ها", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = lapis)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryCard(HeritageRegion.Numbers, "عدد", paleBlue, lapis, Modifier.weight(1f), all)
                CategoryCard(HeritageRegion.Book, "کلمه", paleRose, pomegranate, Modifier.weight(1f), all)
                CategoryCard(HeritageRegion.Lamp, "منطق", paleTurquoise, turquoise, Modifier.weight(1f), all)
            }
        }
        item {
            Text(
                "رایگان بازی کن؛ راهنمایی و تبلیغات اختیاری در نسخه بعد اضافه می‌شوند.",
                fontSize = 11.sp,
                color = muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Image(
                painter = painterResource(R.drawable.hero_persian),
                contentDescription = "منظره‌ای الهام‌گرفته از ایران کهن",
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            )
        }
    }
}

private enum class HeritageRegion { King, Tower, Lamp, Book, Numbers }

@Composable
private fun HeritageArt(region: HeritageRegion, modifier: Modifier = Modifier) {
    val bitmap = ImageBitmap.imageResource(R.drawable.heritage_collage)
    val source = when (region) {
        HeritageRegion.King -> Pair(IntOffset(0, 0), IntSize(99, 111))
        HeritageRegion.Tower -> Pair(IntOffset(100, 0), IntSize(100, 111))
        HeritageRegion.Lamp -> Pair(IntOffset(0, 112), IntSize(65, 87))
        HeritageRegion.Book -> Pair(IntOffset(66, 112), IntSize(67, 87))
        HeritageRegion.Numbers -> Pair(IntOffset(134, 112), IntSize(66, 87))
    }
    Canvas(modifier) {
        drawImage(
            image = bitmap,
            srcOffset = source.first,
            srcSize = source.second,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Medium
        )
    }
}

@Composable
private fun HeritageStatCard(label: String, value: String, region: HeritageRegion, tint: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxSize().padding(7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(start = 3.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                Text(label, color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(value, color = lapis, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            }
            HeritageArt(region, Modifier.width(74.dp).fillMaxHeight().clip(RoundedCornerShape(15.dp)))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, emblem: String, backgroundColor: Color, accent: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(label, color = ink, fontSize = 12.sp)
                Text(value, color = lapis, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(emblem, color = accent, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun CategoryCard(region: HeritageRegion, title: String, tint: Color, accent: Color, modifier: Modifier, click: () -> Unit) {
    Card(
        onClick = click,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            HeritageArt(region, Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(14.dp)))
            Text(title, color = lapis, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun Play(pad: PaddingValues, p: Puzzle, idx: Int, picked: Int, score: Int, hint: Boolean, choose: (Int) -> Unit, showHint: () -> Unit, next: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("آزمون پهلوانی", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = lapis)
                Text("❈  معمای ${idx + 1} از ۱۰۰  ❈", fontSize = 12.sp, color = turquoise)
            }
            Surface(shape = RoundedCornerShape(14.dp), color = paleGold) {
                Text("★  $score", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = lapis, fontWeight = FontWeight.Bold)
            }
        }
        LinearProgressIndicator(progress = { (idx + 1) / 100f }, modifier = Modifier.fillMaxWidth().height(7.dp), color = turquoise, trackColor = paleTurquoise)
        Surface(shape = RoundedCornerShape(12.dp), color = paleBlue) {
            Text("دسته: ${p.category}   •   خرد و دانایی", modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = lapis, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("✦  معمای امروز  ✦", color = pomegranate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(p.question, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = lapis, lineHeight = 30.sp)
                p.options.forEachIndexed { n, opt ->
                    val bg = when {
                        picked == -1 -> ivory
                        n == p.answer -> paleTurquoise
                        n == picked -> paleRose
                        else -> Color(0xFFF4F5F8)
                    }
                    Button(onClick = { choose(n) }, enabled = picked == -1, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = ink, disabledContainerColor = bg, disabledContentColor = ink)) {
                        Text("${listOf("الف", "ب", "پ", "ت")[n]}.  $opt", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 15.sp)
                    }
                }
                if (picked != -1) {
                    Text(if (picked == p.answer) "آفرین، پهلوان! پاسخ درست بود 🎉" else "اشکالی ندارد؛ از هر معما چیزی یاد می‌گیری 🌱", fontWeight = FontWeight.Bold, color = if (picked == p.answer) turquoise else pomegranate)
                    Text(p.explanation, color = ink, fontSize = 14.sp, lineHeight = 22.sp)
                }
                if (hint) Text("راهنمایی: ${p.explanation}", color = turquoise, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = showHint, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("💡 راهنمایی") }
            Button(onClick = next, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = lapis)) { Text("معمای بعدی  ←") }
        }
    }
}

@Composable
private fun Levels(pad: PaddingValues, pick: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Text("گنجینه‌ی معماها", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = lapis)
            Text("هر مرحله، یک گام به سوی خرد", fontSize = 13.sp, color = muted)
        }
        itemsIndexed(puzzles) { i, p ->
            Card(onClick = { pick(i) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.size(39.dp).background(paleGold, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("${i + 1}", color = lapis, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(p.question, color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp, maxLines = 2)
                        Text("دسته: ${p.category}", color = turquoise, fontSize = 11.sp)
                    }
                    Text("‹", fontSize = 25.sp, color = pomegranate)
                }
            }
        }
    }
}

@Composable
private fun Profile(pad: PaddingValues, score: Int, idx: Int) {
    Column(Modifier.fillMaxSize().padding(pad).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("دفتر افتخار من", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = lapis)
        Text("ردّ پای پیشرفتت در سرزمین معماها", fontSize = 13.sp, color = muted)
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(lapis, lapisLight)), RoundedCornerShape(24.dp)).padding(22.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🏆", fontSize = 42.sp)
                Text("پهلوان خرد", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("هر پاسخ، یک نشان افتخار", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                Text("✦  ❈  ✦  ❈  ✦", fontSize = 15.sp, color = saffron)
            }
        }
        StatCard("امتیاز به‌دست‌آمده", score.toString(), "★", paleGold, saffron, Modifier.fillMaxWidth())
        StatCard("آخرین مرحله", "${idx + 1} از ۱۰۰", "❖", paleTurquoise, turquoise, Modifier.fillMaxWidth())
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Text("نسخه آزمایشی: ذخیره آنلاین و حساب کاربری در مرحله بعد اضافه می‌شود.", modifier = Modifier.fillMaxWidth().padding(16.dp), color = muted, fontSize = 12.sp, lineHeight = 19.sp)
        }
    }
}
