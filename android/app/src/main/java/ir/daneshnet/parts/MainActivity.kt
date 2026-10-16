package ir.daneshnet.parts
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.daneshnet.parts.sources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{AppHome()}}}
}
private data class SourceTarget(val name:String,val url:String)
private val targets=listOf(
 SourceTarget("WhatsApp","https://whatsapp.com/channel/0029Vb3Xp1CInlqTzXlogg0h"),
 SourceTarget("Telegram / MellatSt","https://t.me/MellatSt"),
 SourceTarget("Rubika / saipayadakhojatiii","https://rubika.ir/saipayadakhojatiii")
)
@Composable fun AppHome(){
 var query by remember{mutableStateOf("")}
 var busy by remember{mutableStateOf(false)}
 var report by remember{mutableStateOf("هنوز منبعی آزمایش نشده است.")}
 val scope= rememberCoroutineScope()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("قیمت‌یار قطعات",style=MaterialTheme.typography.headlineMedium)
  Text("قیمت فقط پس از تطبیق قطعه و اعتبارسنجی منبع قابل استفاده است.")
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("نام قطعه یا شماره فنی")})
  Button(onClick={},modifier=Modifier.fillMaxWidth()){Text("جستجوی قیمت روز")}
  OutlinedButton(onClick={},modifier=Modifier.fillMaxWidth()){Text("افزودن عکس یا PDF")}
  OutlinedButton(onClick={},modifier=Modifier.fillMaxWidth()){Text("وارد کردن Excel / CSV")}
  HorizontalDivider()
  Text("آزمایش اتصال مستقیم منابع",style=MaterialTheme.typography.titleLarge)
  Text("هر دکمه فقط صفحه عمومی را بررسی می‌کند؛ پیدا شدن صفحه به معنی دسترسی به همه پست‌ها نیست.")
  targets.forEach{target->
   OutlinedButton(enabled=!busy,onClick={
    busy=true;report="در حال بررسی ${target.name} ..."
    scope.launch{
     val r=withContext(Dispatchers.IO){PublicPageSourceAdapter(target.name,target.url).probe()}
     report=buildString{
      appendLine("منبع: ${r.sourceName}");appendLine("وضعیت HTTP: ${r.httpCode ?: "نامشخص"}")
      appendLine("آدرس نهایی: ${r.finalUrl ?: "نامشخص"}")
      r.title?.let{appendLine("عنوان: $it")};r.description?.let{appendLine("توضیح: $it")}
      appendLine(r.note)
      if(r.candidatePriceLines.isNotEmpty()){appendLine("متن‌های کاندیدا (تأییدنشده):");r.candidatePriceLines.take(8).forEach{appendLine("• $it")}}
     }
     busy=false
    }
   },modifier=Modifier.fillMaxWidth()){Text(if(busy)"در حال آزمایش..." else "آزمایش اتصال ${target.name}")}
  }
  if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
  Card(Modifier.fillMaxWidth()){Text(report,Modifier.padding(14.dp),fontSize=14.sp)}
  Text("اگر فقط صفحه معرفی نمایش داده شود، برای پست‌های واقعی به مسیر عمومی/رسمی دیگری نیاز داریم. این آزمایش ورود حساب یا دورزدن محدودیت انجام نمی‌دهد.",style=MaterialTheme.typography.bodySmall)
 }
}
