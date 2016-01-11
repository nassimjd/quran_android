package com.quran.labs.androidquran.data;

import com.quran.labs.androidquran.database.DatabaseUtils;
import com.quran.labs.androidquran.util.QuranFileUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AyahInfoDatabaseHandler {

	private SQLiteDatabase database = null;
	public static String COL_PAGE = "page_number";
	public static String COL_LINE = "line_number";
	public static String COL_SURA = "sura_number";
	public static String COL_AYAH = "ayah_number";
	public static String COL_POSITION = "position";
	public static String MIN_X = "min_x";
	public static String MIN_Y = "min_y";
	public static String MAX_X = "max_x";
	public static String MAX_Y = "max_y";
	public static String GLYPHS_TABLE = "glyphs";

  private static Map<String, AyahInfoDatabaseHandler> sHandlerMap = new HashMap<>();

  public static synchronized AyahInfoDatabaseHandler getDatabaseHandler(
      Context context, String databaseName) {
    AyahInfoDatabaseHandler handler = sHandlerMap.get(databaseName);
    if (handler == null) {
      handler = new AyahInfoDatabaseHandler(context.getApplicationContext(), databaseName);
      sHandlerMap.put(databaseName, handler);
    }
    return handler;
  }
	
	private AyahInfoDatabaseHandler(Context context, String databaseName) throws SQLException {
		String base = QuranFileUtils.getQuranAyahDatabaseDirectory(context);
		if (base == null) return;
		String path = base + File.separator + databaseName;
		database = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}
	
	public boolean validDatabase(){
		return database != null && database.isOpen();
	}

   public Cursor getVersesBoundsForPage(int page){
      if (!validDatabase()) return null;
      return database.query(GLYPHS_TABLE,
              new String[]{ COL_PAGE, COL_LINE, COL_SURA, COL_AYAH,
                      COL_POSITION, MIN_X, MIN_Y, MAX_X, MAX_Y },
              COL_PAGE + "=" + page,
              null, null, null,
              COL_SURA + "," + COL_AYAH + "," + COL_POSITION);
   }
	
	public RectF getPageBounds(int page) {
		if (!validDatabase()){ return null; }

      Cursor c = null;
      try {
         String[] colNames = new String[] {
               "MIN(" + MIN_X + ")", "MIN(" + MIN_Y + ")",
               "MAX(" + MAX_X + ")", "MAX(" + MAX_Y + ")"};
         c = database.query(GLYPHS_TABLE,
                 colNames, COL_PAGE + "=" + page, null, null, null, null);
         if (!c.moveToFirst()){ return null; }
         return new RectF(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3));
      }
      catch (Exception e){
         return null;
      } finally {
        DatabaseUtils.closeCursor(c);
      }
   }
}
