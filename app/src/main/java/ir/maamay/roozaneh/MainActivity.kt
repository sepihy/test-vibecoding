package ir.maamay.roozaneh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

private val purple=Color(0xFF6C4CF1); private val pink=Color(0xFFFF5C93); private val yellow=Color(0xFFFFC857); private val teal=Color(0xFF21C7A8); private val ink=Color(0xFF202044)
data class Puzzle(val category:String,val question:String,val options:List<String>,val answer:Int,val explanation:String)
private val base=listOf(
Puzzle("عدد","عدد بعدی چیست؟ ۲، ۴، ۶، ۸، ؟",listOf("۹","۱۰","۱۲","۱۴"),1,"هر بار ۲ واحد اضافه می‌شود."),
Puzzle("منطق","در مسابقه از نفر دوم سبقت می‌گیری. حالا چندمی؟",listOf("اول","دوم","سوم","چهارم"),1,"جای نفر دوم را می‌گیری، پس دوم می‌شوی."),
Puzzle("کلمه","کدام مورد متفاوت است؟",listOf("قرمز","آبی","سبز","هفت"),3,"سه گزینه رنگ‌اند؛ هفت یک عدد است."),
Puzzle("منطق","دو پدر و دو پسر سه سیب دارند؛ هر نفر یک سیب می‌گیرد. چند نفرند؟",listOf("۲","۳","۴","۵"),1,"سه نفرند: پدربزرگ، پدر و پسر."),
Puzzle("عدد","۵ + ۳ × ۲ چند می‌شود؟",listOf("۱۶","۱۱","۱۳","۱۰"),1,"ابتدا ضرب: ۵ + ۶ = ۱۱."),
Puzzle("کلمه","کدام یک جانور نیست؟",listOf("گنجشک","لاک‌پشت","پروانه","کاج"),3,"کاج یک درخت است."),
Puzzle("منطق","اگر امروز دوشنبه باشد، سه روز بعد چه روزی است؟",listOf("چهارشنبه","پنجشنبه","جمعه","شنبه"),1,"سه روز بعد پنجشنبه است."),
Puzzle("عدد","نصف ۱۸ به‌علاوه ۲ چند است؟",listOf("۹","۱۱","۲۰","۷"),1,"۹ + ۲ = ۱۱."),
Puzzle("کلمه","کدام مورد وسیله نقلیه نیست؟",listOf("قطار","دوچرخه","هواپیما","پنجره"),3,"پنجره وسیله نقلیه نیست."),
Puzzle("منطق","مریم از سارا بلندتر و سارا از ندا بلندتر است. کوتاه‌ترین کیست؟",listOf("مریم","سارا","ندا","مشخص نیست"),2,"ندا کوتاه‌ترین است.")
)
private val puzzles=(1..100).map { base[(it-1)%base.size] }
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App()}} }

@Composable private fun App(){
 var page by remember{mutableStateOf("home")}; var idx by remember{mutableIntStateOf(0)}; var picked by remember{mutableIntStateOf(-1)}; var score by remember{mutableIntStateOf(0)}; var hint by remember{mutableStateOf(false)}
 MaterialTheme(colorScheme=lightColorScheme(primary=purple,secondary=pink,background=Color(0xFFFFF9F2))){
 Scaffold(containerColor=Color(0xFFFFF9F2),bottomBar={NavigationBar(containerColor=Color.White){
 NavigationBarItem(page=="home",{page="home"},icon={Text("🏠")},label={Text("خانه")})
 NavigationBarItem(page=="levels",{page="levels"},icon={Text("🧩")},label={Text("مراحل")})
 NavigationBarItem(page=="profile",{page="profile"},icon={Text("🏆")},label={Text("پیشرفت")})
 }}){ pad -> when(page){
 "home"->Home(pad,score,{idx=0;picked=-1;hint=false;page="play"},{page="levels"})
 "play"->Play(pad,puzzles[idx],idx,picked,score,hint,{n->if(picked==-1){picked=n;if(n==puzzles[idx].answer)score+=10}},{hint=true},{idx=(idx+1)%100;picked=-1;hint=false})
 "levels"->Levels(pad){idx=it;picked=-1;hint=false;page="play"}
 else->Column(Modifier.fillMaxSize().padding(pad).padding(22.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){Text("پیشرفت من 🏆",fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=ink);Stat("⭐ امتیاز",score.toString(),yellow,Modifier.fillMaxWidth());Stat("🧩 آخرین معما","${idx+1} از ۱۰۰",teal,Modifier.fillMaxWidth());Text("نسخه آزمایشی؛ ورود آنلاین در مرحله بعد اضافه می‌شود.",color=Color.Gray)}
 } }
 }
}
@Composable private fun Home(pad:PaddingValues,score:Int,start:()->Unit,all:()->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(pad).padding(18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
 item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("سلام، ذهن کنجکاو! 👋",fontSize=22.sp,fontWeight=FontWeight.ExtraBold,color=ink);Text("هر روز یک قدم باهوش‌تر شو",color=Color.Gray)};Text("🧠",fontSize=38.sp)}}
 item{Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(purple,pink)),RoundedCornerShape(26.dp)).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("چالش امروز",color=Color.White);Text("آماده‌ای ذهنت رو قلقلک بدی؟",fontSize=23.sp,fontWeight=FontWeight.ExtraBold,color=Color.White);Text("۱۰۰ مرحله سرگرم‌کننده",color=Color.White);Button(onClick=start,colors=ButtonDefaults.buttonColors(containerColor=yellow,contentColor=ink),shape=RoundedCornerShape(15.dp)){Text("شروع معما ←",fontWeight=FontWeight.Bold)}}}
 item{Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Stat("⭐ امتیاز",score.toString(),yellow,Modifier.weight(1f));Stat("🧩 معماها","۱۰۰",teal,Modifier.weight(1f))}}
 item{Text("دسته‌بندی‌ها",fontSize=19.sp,fontWeight=FontWeight.Bold,color=ink)}
 item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Cat("🔢","عدد",purple,Modifier.weight(1f),all);Cat("🔤","کلمه",pink,Modifier.weight(1f),all);Cat("💡","منطق",teal,Modifier.weight(1f),all)}}
 item{Text("رایگان بازی کن؛ راهنمایی و تبلیغات اختیاری در نسخه بعد اضافه می‌شوند.",fontSize=12.sp,color=Color.Gray,textAlign=TextAlign.Center)}
 }}
@Composable private fun Stat(label:String,value:String,c:Color,m:Modifier){Card(m,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=c.copy(alpha=.22f))){Column(Modifier.padding(16.dp)){Text(label,color=ink);Text(value,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=ink)}}}
@Composable private fun Cat(e:String,t:String,c:Color,m:Modifier,click:()->Unit){Card(onClick=click,modifier=m,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=c.copy(alpha=.13f))){Column(Modifier.fillMaxWidth().padding(vertical=16.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(e,fontSize=24.sp);Text(t,fontWeight=FontWeight.Bold,color=ink)}}}
@Composable private fun Play(pad:PaddingValues,p:Puzzle,idx:Int,picked:Int,score:Int,hint:Boolean,choose:(Int)->Unit,showHint:()->Unit,next:()->Unit){
 Column(Modifier.fillMaxSize().padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("🧠 معمای روزانه",fontSize=20.sp,fontWeight=FontWeight.ExtraBold,color=ink);Text("⭐ $score",fontWeight=FontWeight.Bold)}
 LinearProgressIndicator(progress={(idx+1)/100f},modifier=Modifier.fillMaxWidth(),color=purple)
 Text("مرحله ${idx+1} از ۱۰۰ • ${p.category}",color=Color.Gray)
 Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){Text(p.question,fontSize=20.sp,fontWeight=FontWeight.Bold,color=ink)
 p.options.forEachIndexed{n,opt->val bg=when{picked==-1->Color(0xFFF3F0FF);n==p.answer->Color(0xFFDDF8EE);n==picked->Color(0xFFFFE0E8);else->Color(0xFFF5F5F8)}
 Button(onClick={choose(n)},enabled=picked==-1,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),colors=ButtonDefaults.buttonColors(containerColor=bg,contentColor=ink,disabledContainerColor=bg,disabledContentColor=ink)){Text("${listOf("الف","ب","پ","ت")[n]}. $opt",modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Start)}}
 if(picked!=-1){Text(if(picked==p.answer)"آفرین! پاسخ درست بود 🎉" else "اشکالی نداره؛ جواب رو یاد بگیر 🌱",fontWeight=FontWeight.Bold,color=if(picked==p.answer)teal else pink);Text(p.explanation,color=ink)}
 if(hint)Text("راهنمایی: ${p.explanation}",color=purple)
 }}
 Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()){OutlinedButton(onClick=showHint,modifier=Modifier.weight(1f)){Text("💡 راهنمایی")};Button(onClick=next,modifier=Modifier.weight(1f)){Text("بعدی ←")}}
 }}
@Composable private fun Levels(pad:PaddingValues,pick:(Int)->Unit){LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("همه‌ی معماها 🧩",fontSize=24.sp,fontWeight=FontWeight.ExtraBold,color=ink)};itemsIndexed(puzzles){i,p->Card(onClick={pick(i)},shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Row(Modifier.fillMaxWidth().padding(13.dp),verticalAlignment=Alignment.CenterVertically){Text("${i+1}",fontWeight=FontWeight.Bold,color=purple);Spacer(Modifier.width(12.dp));Text(p.question,Modifier.weight(1f),color=ink);Text("‹",fontSize=24.sp,color=purple)}}}}}
