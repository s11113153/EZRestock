package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


public class DBHelper extends SQLiteOpenHelper {
  //private final String TableName = "Management";
  private final String TABLE_MANAGEMENT = "Management";
  private final String TABLE_PRODUCTS = "Products";
  private final String TABLE_PRODUCTCODE = "ProductCode";

  public DBHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
  }
  @Override
  public void onCreate(SQLiteDatabase db) {
    // create Management table
    final String SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_MANAGEMENT + "( " +
        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "ScanNumber VARCHAR(20), " +
        "Quantity VARCHAR(20), " + // DOUBLE type => if value > 10000 then display : 1.E+
        "Inventory VARCHAR(20), " +
        "createTime DATETIME DEFAULT CURRENT_TIMESTAMP " +
        ");";
    db.execSQL(SQL);

    // create Products table
    final String createProductsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + "( " +
        "ProID    INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "ProCode  INTEGER,     " +
        "BarCode  VARCHAR(20), " +
        "ProDesc  VARCHAR(30), " +
        "ProUnitS VARCHAR(10), " +
        "ProUnitL VARCHAR(10), " +
        "PackageQ INTEGER,     " +
        "SupCode  VARCHAR(10)  " +
        ");";
    db.execSQL(createProductsTable);

    // create ProductCode table
    final String createProductCodeTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTCODE + "( " +
        "PCoID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "ProID INTEGER, " +
        "ProCode VARCHAR(20), " +
        "SupID VARCHAR(30), " +
        "FOREIGN KEY (ProID) REFERENCES " + TABLE_PRODUCTS + "(ProID)" +
        ");";
    db.execSQL(createProductCodeTable);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    final String SQL = "DROP TABLE IF EXISTS " + TABLE_MANAGEMENT;
    db.execSQL(SQL);
    this.onCreate(db);
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

}
