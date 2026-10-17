package ir.maamay.roozaneh

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import org.json.JSONArray

private val Cream = Color(0xFFFFF8EE)
private val Navy = Color(0xFF173B66)
private val Teal = Color(0xFF238F8D)
private val Gold = Color(0xFFF4BE4F)
private val Ink = Color(0xFF17304B)
private val Mint = Color(0xFFDDF2E8)
private val PaleGold = Color(0xFFFFEFCB)
private val Rose = Color(0xFFFBE4E7)
private val Lavender = Color(0xFFE8E4F3)
private val Muted = Color(0xFF7F8994)

private data class Puzzle(
    val id: Int, val category: String, val difficulty: String, val question: String,
    val options: List<String>, val answerIndex: Int, val explanation: String,
    val imageName: String, val chapter: String, val hint: String, val reward: Int
)

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(255,248,238)
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        MobileAds.initialize(this) {}
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                GanjnamehApp()
            }
        }
    }
}

@Composable
private fun GanjnamehApp() {
    val context = LocalContext.current
    val puzzles = remember { loadPuzzles(context) }
    val prefs = remember { context.getSharedPreferences("ganjnameh_progress", Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf("home") }
    var selected by remember { mutableIntStateOf((prefs.getInt("last_stage_id", 1)-1).coerceIn(0, (puzzles.size-1).coerceAtLeast(0))) }
    var chosen by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(prefs.getInt("score", 0)) }
    var completed by remember { mutableStateOf(prefs.getStringSet("completed_ids", emptySet<String>())?.toSet() ?: emptySet()) }
    var hintShown by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    val current = puzzles.getOrNull(selected)

    MaterialTheme(colorScheme = lightColorScheme(primary=Navy, secondary=Teal, tertiary=Gold, background= Cream, surface=Color.White, onSurface=Ink, onBackground=Ink)) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(containerColor=Color.White, tonalElevation=0.dp) {
                    NavigationBarItem(selected=page=="home", onClick={page="home"}, icon={Text("🏛️")}, label={Text("خانه")})
                    NavigationBarItem(selected=page=="levels" || page=="play", onClick={page="levels"}, icon={Text("🧩")}, label={Text("مراحل")})
                    NavigationBarItem(selected=page=="profile", onClick={page="profile"}, icon={Text("🏆")}, label={Text("پیشرفت")})
                }
            }
        ) { pad ->
            when(page) {
                "home" -> HomePage(
                    modifier=Modifier.padding(pad), total=puzzles.size, score=score, completed=completed.size,
                    onStart={ selected=(prefs.getInt("last_stage_id",1)-1).coerceIn(0,(puzzles.size-1).coerceAtLeast(0)); chosen=-1; hintShown=false; page="play" },
                    onLevels={categoryFilter=null;page="levels"},
                    onCategory={ categoryFilter=it; page="levels" }
                )
                "levels" -> LevelsPage(
                    modifier=Modifier.padding(pad), puzzles=puzzles, completed=completed, filter=categoryFilter,
                    onFilterClear={categoryFilter=null},
                    onPick={ p -> selected=p.id-1; chosen=-1; hintShown=false; prefs.edit().putInt("last_stage_id",p.id).apply(); page="play" }
                )
                "play" -> if(current!=null) PlayPage(
                    modifier=Modifier.padding(pad), puzzle=current, index=selected, total=puzzles.size,
                    chosen=chosen, hintShown=hintShown,
                    onChoose={ option ->
                        if(chosen==-1) {
                            chosen=option
                            if(option==current.answerIndex) {
                                score += current.reward
                                completed = completed + current.id.toString()
                                prefs.edit().putInt("score",score).putStringSet("completed_ids",completed).putInt("last_stage_id",current.id).apply()
                            }
                        }
                    },
                    onHint={hintShown=true},
                    onNext={
                        if(selected < puzzles.lastIndex) selected += 1 else page="home"
                        chosen=-1; hintShown=false
                        prefs.edit().putInt("last_stage_id",selected+1).apply()
                    },
                    onLevels={page="levels"}
                )
                else -> ProfilePage(Modifier.padding(pad), score, completed.size, puzzles.size, onLevels={page="levels"}, onHome={page="home"})
            }
        }
    }
}

private fun loadPuzzles(context: Context): List<Puzzle> = runCatching {
    val array = JSONArray(context.assets.open("puzzles.json").bufferedReader().use { it.readText() })
    (0 until array.length()).map { i ->
        val o=array.getJSONObject(i); val opts=o.getJSONArray("options")
        Puzzle(
            id=o.getInt("id"), category=o.getString("category"), difficulty=o.getString("difficulty"),
            question=o.getString("question"), options=(0 until opts.length()).map { opts.getString(it) },
            answerIndex=o.getInt("answerIndex"), explanation=o.getString("explanation"),
            imageName=o.optString("image","stage_%03d".format(o.getInt("id"))),
            chapter=o.optString("chapter","سفر به سرزمین رازها"),
            hint=o.optString("hint","به سرنخ‌ها دقت کن."), reward=o.optInt("reward",10)
        )
    }
}.getOrDefault(emptyList())

private fun artId(context: Context, name: String): Int {
    val id=context.resources.getIdentifier(name,"drawable",context.packageName)
    return if(id!=0) id else R.drawable.heritage_collage
}

@Composable
private fun HomePage(modifier: Modifier, total: Int, score: Int, completed: Int, onStart:()->Unit, onLevels:()->Unit, onCategory:(String)->Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp, vertical=14.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("سلام، ذهن کنجکاو! 👋", fontSize=25.sp, fontWeight=FontWeight.Bold, color=Navy)
                Text("هر روز یک قدم به هوش‌تر شو", fontSize=15.sp, color=Muted)
                Text("✦ خرد · حماسه · ایران ✦", fontSize=13.sp, color=Teal, fontWeight=FontWeight.SemiBold)
            }
            Text("🪶", fontSize=34.sp)
        }
        Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(28.dp))) {
            Image(painterResource(R.drawable.hero_persian), null, Modifier.fillMaxSize(), contentScale=ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Navy.copy(alpha=.08f), Navy.copy(alpha=.92f)))))
            Column(Modifier.align(Alignment.CenterEnd).fillMaxWidth(.72f).padding(18.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("رازهای شاهنامه", color=Gold, fontWeight=FontWeight.Bold, fontSize=15.sp)
                Text("آماده‌ای، پهلوان؟", color=Color.White, fontWeight=FontWeight.Bold, fontSize=27.sp)
                Text("هوشت را بیازمای و گنج دانایی را پیدا کن.", color=Color.White, fontSize=14.sp)
                Button(onClick=onStart, colors=ButtonDefaults.buttonColors(containerColor=Gold, contentColor=Navy), shape=RoundedCornerShape(18.dp)) {
                    Text("شروع معما  ←", fontWeight=FontWeight.Bold)
                }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp), modifier=Modifier.fillMaxWidth()) {
            StatCard("مراحل بازی", total.toString(), "🧩", Mint, Modifier.weight(1f))
            StatCard("امتیاز من", score.toString(), "⭐", PaleGold, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Text("دسته‌های چالش", fontSize=20.sp, fontWeight=FontWeight.Bold, color=Navy)
            Text("همهٔ مراحل ←", color=Teal, modifier=Modifier.clickable { onLevels() })
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp), modifier=Modifier.fillMaxWidth()) {
            CategoryCard("منطق","💡",Mint,Modifier.weight(1f)){onCategory("منطق")}
            CategoryCard("کلمه","📖",Rose,Modifier.weight(1f)){onCategory("کلمه")}
            CategoryCard("عدد","🔢",Lavender,Modifier.weight(1f)){onCategory("عدد")}
            CategoryCard("فرهنگ","🏛️",PaleGold,Modifier.weight(1f)){onCategory("فرهنگ")}
        }
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("🏺", fontSize=30.sp)
                Column(Modifier.weight(1f)) {
                    Text("سفری در ایران کهن", fontWeight=FontWeight.Bold, color=Navy)
                    Text("از باغ‌های ایرانی تا رازهای شاهنامه", fontSize=13.sp, color=Muted)
                }
                Text("✦", color=Gold, fontSize=24.sp)
            }
        }
        Text("بازی رایگان است؛ راهنمایی‌های اختیاری در نسخه‌های بعدی اضافه می‌شوند.", color=Muted, fontSize=12.sp, textAlign=TextAlign.Center, modifier=Modifier.fillMaxWidth().padding(bottom=8.dp))
    }
}

@Composable
private fun StatCard(title:String, value:String, icon:String, tint:Color, modifier:Modifier=Modifier) {
    Card(modifier, colors=CardDefaults.cardColors(containerColor=tint), shape=RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(icon+"  "+title, color=Navy, fontSize=14.sp)
            Text(value, color=Navy, fontWeight=FontWeight.Bold, fontSize=27.sp)
        }
    }
}

@Composable
private fun CategoryCard(title:String, emoji:String, tint:Color, modifier:Modifier=Modifier, onClick:()->Unit) {
    Card(modifier.clickable(onClick=onClick), colors=CardDefaults.cardColors(containerColor=tint), shape=RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical=14.dp, horizontal=4.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text(emoji, fontSize=25.sp)
            Text(title, color=Navy, fontWeight=FontWeight.SemiBold, fontSize=13.sp)
        }
    }
}

@Composable
private fun LevelsPage(modifier:Modifier, puzzles:List<Puzzle>, completed:Set<String>, filter:String?, onFilterClear:()->Unit, onPick:(Puzzle)->Unit) {
    Column(modifier.fillMaxSize().padding(horizontal=14.dp, vertical=10.dp)) {
        Text("نقشهٔ سفر", fontSize=26.sp, fontWeight=FontWeight.Bold, color=Navy, modifier=Modifier.padding(vertical=8.dp))
        Text(if(filter==null) "۱۰۰ مرحله در ۱۰ فصل" else "دسته: "+filter, color=Teal, modifier=Modifier.padding(bottom=8.dp))
        if(filter!=null) OutlinedButton(onClick=onFilterClear, modifier=Modifier.fillMaxWidth()) { Text("نمایش همهٔ مراحل") }
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp), contentPadding=PaddingValues(bottom=12.dp)) {
            items(puzzles.filter { filter==null || it.category==filter }) { p ->
                Card(modifier=Modifier.fillMaxWidth().clickable { onPick(p) }, colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(18.dp), elevation=CardDefaults.cardElevation(defaultElevation=1.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        Image(painterResource(artId(LocalContext.current,p.imageName)), null, Modifier.size(width=88.dp,height=68.dp).clip(RoundedCornerShape(12.dp)), contentScale=ContentScale.Crop)
                        Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Text("مرحلهٔ "+p.id.toString()+" · "+p.category, color=Teal, fontSize=12.sp, fontWeight=FontWeight.SemiBold)
                            Text(p.question, color=Navy, fontSize=14.sp, fontWeight=FontWeight.Medium, maxLines=2)
                            Text(p.chapter+" · "+p.difficulty, color=Muted, fontSize=11.sp)
                        }
                        Text(if(completed.contains(p.id.toString())) "✓" else "›", color=if(completed.contains(p.id.toString())) Teal else Gold, fontSize=25.sp, fontWeight=FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayPage(modifier:Modifier, puzzle:Puzzle, index:Int, total:Int, chosen:Int, hintShown:Boolean, onChoose:(Int)->Unit, onHint:()->Unit, onNext:()->Unit, onLevels:()->Unit) {
    val context=LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp, vertical=12.dp), verticalArrangement=Arrangement.spacedBy(13.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Column {
                Text("آزمون پهلوانی", fontSize=24.sp, fontWeight=FontWeight.Bold, color=Navy)
                Text("معمای "+puzzle.id.toString()+" از "+total.toString(), color=Teal, fontSize=14.sp)
            }
            Surface(color=PaleGold, shape=RoundedCornerShape(14.dp)) { Text("⭐ "+puzzle.reward.toString(), Modifier.padding(horizontal=12.dp, vertical=8.dp), color=Navy, fontWeight=FontWeight.Bold) }
        }
        LinearProgressIndicator(progress={ (index+1).toFloat()/total.coerceAtLeast(1) }, modifier=Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(8.dp)), color=Teal, trackColor=Mint)
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.End) {
            Surface(color=Lavender, shape=RoundedCornerShape(14.dp)) { Text("دسته: "+puzzle.category+"  ·  "+puzzle.difficulty, Modifier.padding(horizontal=12.dp, vertical=9.dp), color=Navy, fontWeight=FontWeight.SemiBold, fontSize=13.sp) }
        }
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(24.dp), elevation=CardDefaults.cardElevation(defaultElevation=2.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Image(painterResource(artId(context,puzzle.imageName)), null, Modifier.fillMaxWidth().height(175.dp).clip(RoundedCornerShape(18.dp)), contentScale=ContentScale.Crop)
                Text("✦  معمای امروز  ✦", color=Color(0xFFC64F61), fontWeight=FontWeight.Bold)
                Text(puzzle.question, color=Navy, fontWeight=FontWeight.Bold, fontSize=21.sp, textAlign=TextAlign.Start)
                puzzle.options.forEachIndexed { i, option ->
                    val correct = chosen!=-1 && i==puzzle.answerIndex
                    val wrong = chosen==i && i!=puzzle.answerIndex
                    val tint = when { correct -> Mint; wrong -> Rose; else -> Cream }
                    Surface(modifier=Modifier.fillMaxWidth().clickable(enabled=chosen==-1){onChoose(i)}, color=tint, shape=RoundedCornerShape(15.dp), border=if(correct) androidx.compose.foundation.BorderStroke(1.dp,Teal) else null) {
                        Row(Modifier.fillMaxWidth().padding(horizontal=14.dp, vertical=14.dp), verticalAlignment=Alignment.CenterVertically) {
                            Text(listOf("الف","ب","ج","د")[i]+"  ·  "+option, color=Navy, fontSize=17.sp, fontWeight=if(correct) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                if(chosen!=-1) {
                    Text(if(chosen==puzzle.answerIndex) "آفرین! پاسخ درست است." else "این بار نشد؛ پاسخ درست را ببین و ادامه بده.", color=if(chosen==puzzle.answerIndex) Teal else Color(0xFFC64F61), fontWeight=FontWeight.Bold, fontSize=17.sp)
                    Text(puzzle.explanation, color=Ink, fontSize=14.sp)
                }
                if(hintShown) Text("💡 "+puzzle.hint, color=Teal, fontSize=14.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick=onHint, modifier=Modifier.weight(1f)) { Text("💡 راهنمایی") }
            Button(onClick=if(chosen==-1) onLevels else onNext, modifier=Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=Navy)) {
                Text(if(chosen==-1) "فهرست مراحل" else if(index==total-1) "پایان سفر ←" else "معمای بعدی ←")
            }
        }
    }
}

@Composable
private fun ProfilePage(modifier:Modifier, score:Int, completed:Int, total:Int, onLevels:()->Unit, onHome:()->Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("پیشرفت من", fontSize=27.sp, fontWeight=FontWeight.Bold, color=Navy)
        Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement=Arrangement.spacedBy(12.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                Text("🏆", fontSize=48.sp)
                Text("پهلوان خرد", fontSize=23.sp, fontWeight=FontWeight.Bold, color=Navy)
                Text("امتیاز: "+score.toString(), color=Teal, fontSize=18.sp, fontWeight=FontWeight.Bold)
                Text("مراحل تکمیل‌شده: "+completed.toString()+" از "+total.toString(), color=Muted)
                LinearProgressIndicator(progress={ completed.toFloat()/total.coerceAtLeast(1) }, modifier=Modifier.fillMaxWidth().height(8.dp), color=Teal, trackColor=Mint)
            }
        }
        Button(onClick=onLevels, modifier=Modifier.fillMaxWidth()) { Text("ادامهٔ سفر") }
        OutlinedButton(onClick=onHome, modifier=Modifier.fillMaxWidth()) { Text("بازگشت به خانه") }
        Text("پیشرفت روی همین دستگاه ذخیره می‌شود. همگام‌سازی آنلاین در مرحلهٔ بعدی توسعه تکمیل می‌شود.", color=Muted, fontSize=12.sp)
    }
}
