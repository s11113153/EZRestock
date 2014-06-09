package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private final String TableName = "Management";

    public DBHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
        SQLiteDatabase db = this.getWritableDatabase();

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "ScanNumber VARCHAR(20), " +
        "Quantity VARCHAR(20), " + // DOUBLE type => if value > 10000 then display : 1.E+
        "Inventory VARCHAR(20), " +
        "createTime DATETIME DEFAULT CURRENT_TIMESTAMP " +
        ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE IF EXISTS " + TableName;
        db.execSQL(SQL);
        this.onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
