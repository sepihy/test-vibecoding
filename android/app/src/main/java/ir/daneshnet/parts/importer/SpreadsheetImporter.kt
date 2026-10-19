package ir.daneshnet.parts.importer
import android.content.Context
import android.net.Uri
import java.io.InputStream
data class ColumnMapping(val partName:Int?,val partNumber:Int?,val brand:Int?,val make:Int?,val model:Int?,val trim:Int?,val price:Int?,val availability:Int?,val date:Int?)
class SpreadsheetImporter(private val context:Context){
 fun open(uri:Uri):InputStream=context.contentResolver.openInputStream(uri) ?: error("فایل قابل خواندن نیست")
 fun detect(headers:List<String>):ColumnMapping{
  fun f(vararg keys:String)=headers.indexOfFirst{h->keys.any{k->h.trim().lowercase().contains(k)}}.takeIf{it>=0}
  return ColumnMapping(f("نام قطعه","شرح","part name","description"),f("کد","شماره فنی","part no","partnumber","sku"),f("برند","brand"),f("سازنده","خودرو","make"),f("مدل","model"),f("تیپ","trim"),f("قیمت","فی","price"),f("موجود","stock","availability"),f("تاریخ","date"))
 }
}