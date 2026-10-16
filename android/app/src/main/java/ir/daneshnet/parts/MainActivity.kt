package ir.daneshnet.parts

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Navy = Color(0xFF17324D)
private val Bg = Color(0xFFF5F7F9)

class MainActivity : ComponentActivity() {
    private lateinit var ocr: OcrEngine
    private lateinit var docs: DocumentReader
    private var onExtracted: ((String) -> Unit)? = null

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) processUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocr = OcrEngine(this)
        docs = DocumentReader(contentResolver)
        setContent { PartsPriceApp(onPickDocument = { callback -> onExtracted = callback; picker.launch(arrayOf("image/*", "application/pdf")) }) }
    }

    private fun processUri(uri: Uri) {
        if (contentResolver.getType(uri) == "application/pdf" || uri.toString().lowercase().endsWith(".pdf")) {
            Thread {
                val pages = docs.firstPages(uri)
                val all = StringBuilder()
                fun next(i: Int) {
                    if (i >= pages.size) { runOnUiThread { onExtracted?.invoke(all.toString()) }; return }
                    ocr.read(pages[i], { text -> all.appendLine(text); next(i + 1) }, { next(i + 1) })
                }
                next(0)
            }.start()
        } else {
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            ocr.read(bitmap, { text -> onExtracted?.invoke(text) }, { onExtracted?.invoke("") })
        }
    }
}

@Composable
fun PartsPriceApp(onPickDocument: (( (String) -> Unit) -> Unit)) {
    var query by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var extracted by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var searched by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, background = Bg)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        listOf("قیمت‌یابی", "منابع", "اسناد").forEachIndexed { i, label ->
                            NavigationBarItem(selected = tab == i, onClick = { tab = i },
                                icon = { Text(listOf("⌕","◉","▤")[i]) }, label = { Text(label) })
                        }
                    }
                }
            ) { pad ->
                when(tab) {
                    0 -> SearchScreen(query, { query=it }, searched, { searched=true }, extracted, pad)
                    1 -> SourcesScreen(link, {link=it}, pad)
                    else -> DocumentsScreen(extracted, { onPickDocument { text -> extracted = text } }, pad)
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(query:String,onQuery:(String)->Unit,searched:Boolean,onSearch:()->Unit,extracted:String,pad:PaddingValues){
    LazyColumn(Modifier.padding(pad).fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Spacer(Modifier.height(10.dp));Text("دستیار قیمت لوازم یدکی", fontSize=25.sp, fontWeight=FontWeight.Bold, color=Navy);Text("قیمت روز را از منابع متصل بررسی کن",color=Color.Gray)}
        item{OutlinedTextField(query,onQuery,Modifier.fillMaxWidth(),singleLine=true,label={Text("نام قطعه، خودرو یا شماره فنی")},placeholder={Text("مثلاً لنت جلو پژو ۲۰۶ تیپ ۵")})}
        item{Button(onClick=onSearch,enabled=query.isNotBlank(),Modifier.fillMaxWidth()){Text("جستجوی هوشمند قیمت")}}
        if(extracted.isNotBlank()) item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("متن استخراج‌شده از سند",fontWeight=FontWeight.Bold);Text(extracted.take(2500))}}}
        if(searched)item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("نتیجه بررسی",fontWeight=FontWeight.Bold,fontSize=19.sp);Text(query);HorizontalDivider();Text("در انتظار اتصال منابع واقعی قیمت...");Text("قیمت حدسی نمایش داده نمی‌شود.",color=Color.Gray)}}}
    }
}

@Composable private fun DocumentsScreen(extracted:String,pick:()->Unit,pad:PaddingValues){
    Column(Modifier.padding(pad).fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("خواندن فایل و عکس",25.spEm(),fontWeight=FontWeight.Bold,color=Navy)
        Text("عکس یا PDF را انتخاب کن؛ چند صفحه اول PDF به متن تبدیل می‌شود.",color=Color.Gray)
        Button(onClick=pick ,Modifier.fillMaxWidth()){Text("انتخاب عکس / PDF")}
        if(extracted.isNotBlank())Card(Modifier.fillMaxWidth()){Text(extracted.take(4000),Modifier.padding(14.dp))}
    }
}

@Composable private fun SourcesScreen(link:String,onLink:(String)->Unit,pad:PaddingValues){
    Column(Modifier.padding(pad).fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("منابع قیمت",25.sp,fontWeight=FontWeight.Bold,color=Navy)
        Text("لینک کانال یا سایت را اضافه کن.",color=Color.Gray)
        OutlinedTextField(link,onLink,Modifier.fillMaxWidth(),singleLine=true,label={Text("لینک منبع")})
        Button(onClick={},enabled=link.isNotBlank(),Modifier.fillMaxWidth()){Text("افزودن منبع")}
    }
}

