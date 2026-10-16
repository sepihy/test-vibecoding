package ir.daneshnet.parts
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{AppHome()}}}
}
@Composable fun AppHome(){
 var query by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("قیمت‌یار قطعات",style=MaterialTheme.typography.headlineMedium)
  Text("قیمت روز را با چند منبع بررسی کن.")
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("نام قطعه یا شماره فنی")})
  Button(onClick={},Modifier.fillMaxWidth()){Text("جستجوی قیمت روز")}
  OutlinedButton(onClick={},Modifier.fillMaxWidth()){Text("افزودن عکس یا PDF")}
  OutlinedButton(onClick={},Modifier.fillMaxWidth()){Text("وارد کردن Excel / CSV")}
  Text("منابع فعال",style=MaterialTheme.typography.titleMedium)
  Text("ایساکو • Telegram • Rubika • WhatsApp • تأمین‌کنندگان")
 }
}