package ir.daneshnet.parts
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class PriceDatabase(context: Context):SQLiteOpenHelper(context,"parts_price.db",null,1){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE price_points(id INTEGER PRIMARY KEY AUTOINCREMENT,part_key TEXT NOT NULL,source_id TEXT NOT NULL,price_rial INTEGER NOT NULL,observed_at INTEGER NOT NULL,availability TEXT NOT NULL)");db.execSQL("CREATE INDEX idx_price_part_time ON price_points(part_key,observed_at)")}
 override fun onUpgrade(db:SQLiteDatabase,o:Int,n:Int){}
 fun savePrice(k:String,s:String,p:Long,t:Long,a:String="UNKNOWN"){writableDatabase.execSQL("INSERT INTO price_points(part_key,source_id,price_rial,observed_at,availability) VALUES(?,?,?,?,?)",arrayOf(k,s,p,t,a))}
 fun history(k:String,since:Long):List<DbPricePoint>{val c=readableDatabase.rawQuery("SELECT source_id,price_rial,observed_at,availability FROM price_points WHERE part_key=? AND observed_at>=? ORDER BY observed_at",arrayOf(k,since.toString()));return c.use{val out=mutableListOf<DbPricePoint>();while(it.moveToNext())out+=DbPricePoint(it.getString(0),it.getLong(1),it.getLong(2),it.getString(3));out}}
}
data class DbPricePoint(val sourceId:String,val priceRial:Long,val observedAt:Long,val availability:String)
