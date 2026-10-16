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
import ir.daneshnet.parts.sources.ChannelAccessState
import ir.daneshnet.parts.sources.WhatsAppChannelAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{AppHome()}}}
}
@Composable fun AppHome(){
 var query by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("هنوز اتصال آزمایش نشده است.")}
 var detail by remember{mutableStateOf("")}
 var busy by remember{mutableStateOf(false)}
 val scope= rememberCoroutineScope()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("قیمت‌یار قطعات",style=MaterialTheme.typography.headlineMedium)
  Text("قیمت‌ها فقط پس از اعتبارسنجی منبع و تطبیق قطعه قابل استفاده‌اند.")
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("نام قطعه یا شماره فنی")})
  Button(onClick={},Modifier.fillMaxWidth()){Text("جستجوی قیمت روز")}
  OutlinedButton(onClick={},Modifier.fillMaxWidth()){Text("افزودن عکس یا PDF")}
  OutlinedButton(onClick={},Modifier.fillMaxWidth()){Text("وارد کردن Excel / CSV")}
  HorizontalDivider()
  Text("اتصال مستقیم واتساپ",style=MaterialTheme.typography.titleLarge)
  Text("https://whatsapp.com/channel/0029Vb3Xp1CInlqTzXlogg0h",fontSize=12.sp)
  Button(enabled=!busy,onClick={
   busy=true;status="در حال بررسی دسترسی عمومی...";detail=""
   scope.launch{
    val result=withContext(Dispatchers.IO){WhatsAppChannelAdapter().probe()}
    status=when(result.state){
     ChannelAccessState.PUBLIC_CONTENT_FOUND->"صفحه عمومی باز شد؛ خطوط احتمالی قیمت پیدا شد"
     ChannelAccessState.PUBLIC_PAGE_ONLY->"صفحه باز شد، اما پست‌های قابل استخراج در HTML عمومی پیدا نشدند"
     ChannelAccessState.BLOCKED_OR_UNAVAILABLE->"دسترسی عمومی از این محیط ناموفق بود"
     ChannelAccessState.INVALID_RESPONSE->"پاسخ نامعتبر"
    }
    detail=buildString{
     appendLine(result.note);appendLine("HTTP: ${result.httpCode ?: "نامشخص"}")
     result.title?.let{appendLine("عنوان: $it")}
     result.description?.let{appendLine("توضیح: $it")}
     if(result.candidatePriceLines.isNotEmpty()){appendLine("خطوط کاندیدا (نیازمند بررسی):");result.candidatePriceLines.take(10).forEach{appendLine("• $it")}}
    }
    busy=false
   }
  },Modifier.fillMaxWidth()){Text(if(busy)"در حال بررسی..." else "آزمایش اتصال مستقیم")}
  Text(status,style=MaterialTheme.typography.titleMedium)
  if(detail.isNotBlank())Card(Modifier.fillMaxWidth()){Text(detail,Modifier.padding(14.dp))}
  Text("این آزمایش فقط صفحه عمومی را می‌خواند؛ ورود، دورزدن محدودیت یا استخراج محتوای غیرقابل‌دسترسی انجام نمی‌شود.",style=MaterialTheme.typography.bodySmall)
  HorizontalDivider()
  Text("منابع هدف",style=MaterialTheme.typography.titleMedium)
  Text("Telegram • Rubika • WhatsApp • سایت‌ها • فایل‌های تأمین‌کننده")
 }
}
