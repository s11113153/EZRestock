package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class dbHelper extends SQLiteOpenHelper {
    private final String TableName = "Management";
    private SQLiteDatabase db;

    public dbHelper(Context context, String DbName, int dbVersion) {
        super(context, DbName, null, dbVersion);
        db = this.getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "ScanNumber VARCHAR(20), " +
        "Quantity DOUBLE, " +
        "Inventory DOUBLE, " +
        "createTime DATETIME DEFAULT CURRENT_TIMESTAMP " +
        ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + TableName;
        db.execSQL(SQL);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


}
