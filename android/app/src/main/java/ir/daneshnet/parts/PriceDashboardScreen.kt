package ir.daneshnet.parts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
@Composable fun PriceDashboardScreen(state:PriceDashboardState,modifier:Modifier=Modifier){Column(modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("گزارش قیمت",fontSize=25.sp,fontWeight=FontWeight.Bold);Text(state.partName);Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(state.currentRial?.let{formatToman(it)}?:"—",fontSize=30.sp,fontWeight=FontWeight.Bold);Text("قیمت بازار فعلی");state.changePercent?.let{Text(String.format(Locale.US,"%+.1f%%",it),fontSize=18.sp)};Text("اطمینان: ${state.confidence}%");Text("منابع مستقل: ${state.sourceCount}");Text(state.status,fontWeight=FontWeight.SemiBold)}}}}
private fun formatToman(r:Long)=NumberFormat.getNumberInstance(Locale("fa","IR")).format(r/10)+" تومان"
