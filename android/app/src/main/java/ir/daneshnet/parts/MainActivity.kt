package ir.daneshnet.parts

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  setContent { MaterialTheme { AppHome() } }
 }
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
 var selectedFile by remember{mutableStateOf("")}
 val scope= rememberCoroutineScope()
 val context= androidx.compose.ui.platform.LocalContext.current

 val mediaPicker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri: Uri? ->
  if(uri!=null){
   selectedFile=uri.lastPathSegment ?: uri.toString()
   report="فایل انتخاب شد: $selectedFile\nانتخاب فایل موفق بود؛ استخراج متن و قیمت از این فایل هنوز در مرحله بعدی پیاده‌سازی است."
  }
 }
 val spreadsheetPicker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){ uri: Uri? ->
  if(uri!=null){
   selectedFile=uri.lastPathSegment ?: uri.toString()
   report="فایل انتخاب شد: $selectedFile\nفایل دریافت شد؛ خواندن کامل Excel و CSV هنوز به پردازشگر فایل نیاز دارد."
  }
 }

 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("قیمت‌یار قطعات",style=MaterialTheme.typography.headlineMedium)
  Text("قیمت فقط پس از تطبیق قطعه و اعتبارسنجی منبع قابل استفاده است.")
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("نام قطعه یا شماره فنی")})
  Button(onClick={
   report=if(query.isBlank()) "ابتدا نام قطعه یا شماره فنی را وارد کن." else "جستجوی «$query» ثبت شد؛ اتصال جستجو به داده‌های واقعی هنوز تکمیل نشده است."
  },modifier=Modifier.fillMaxWidth()){Text("جستجوی قیمت روز")}
  OutlinedButton(onClick={
   mediaPicker.launch(arrayOf("image/*","application/pdf"))
  },modifier=Modifier.fillMaxWidth()){Text("افزودن PDF یا عکس")}
  OutlinedButton(onClick={
   spreadsheetPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/vnd.ms-excel","text/csv","text/comma-separated-values","application/octet-stream"))
  },modifier=Modifier.fillMaxWidth()){Text("وارد کردن Excel / CSV")}
  if(selectedFile.isNotBlank()){
   Text("فایل انتخاب‌شده: $selectedFile",style=MaterialTheme.typography.bodyMedium)
  }
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
