package ir.daneshnet.parts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
data class PriceDashboardState(val partName:String="",val currentRial:Long?=null,val previousRial:Long?=null,val changePercent:Double?=null,val confidence:Int=0,val sourceCount:Int=0,val status:String="برای شروع یک قطعه جستجو کن")
class PriceDashboardViewModel(private val db:PriceDatabase):ViewModel(){
 var state=PriceDashboardState();private set
 fun analyze(k:String,name:String,now:Long){viewModelScope.launch(Dispatchers.IO){val p=db.history(k,now-30L*24*60*60*1000);val cur=median(p.filter{it.observedAt>=now-7L*24*60*60*1000}.map{it.priceRial});val prev=median(p.filter{it.observedAt<now-7L*24*60*60*1000}.map{it.priceRial});val pct=if(cur!=null&&prev!=null)(cur-prev)*100.0/prev else null;val conf=(p.map{it.sourceId}.distinct().size*15+p.size*8).coerceAtMost(100);val status=when{cur==null->"داده کافی نیست";conf<75->"نیازمند بررسی";pct!=null&&pct>5->"هشدار: قیمت افزایش یافته";else->"قیمت قابل بررسی"};withContext(Dispatchers.Main){state=PriceDashboardState(name,cur,prev,pct,conf,p.map{it.sourceId}.distinct().size,status)}}}
 private fun median(v:List<Long>):Long?{if(v.isEmpty())return null;val s=v.sorted();return if(s.size%2==1)s[s.size/2] else (s[s.size/2-1]+s[s.size/2])/2}
}
