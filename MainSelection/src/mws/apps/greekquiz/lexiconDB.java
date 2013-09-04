package mws.apps.greekquiz;
import static android.provider.BaseColumns._ID;
import static mws.apps.greekquiz.Constants.LEX_ENG;
import static mws.apps.greekquiz.Constants.LEX_FLG;
import static mws.apps.greekquiz.Constants.LEX_GRK;
import static mws.apps.greekquiz.Constants.LEX_ROOT;
import static mws.apps.greekquiz.Constants.LEX_SEC;
import static mws.apps.greekquiz.Constants.LEX_SEQ;
import static mws.apps.greekquiz.Constants.LEX_TABLE;
import static mws.apps.greekquiz.Constants.SECTIONS_TABLE;
import static mws.apps.greekquiz.Constants.SEC_FLG;
import static mws.apps.greekquiz.Constants.SEC_SECTION;
import static mws.apps.greekquiz.Constants.LEX_EDT;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


public class lexiconDB extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "lexicondata.db";
	private static final int DATABASE_VERSION = 2;
	public static String DB_FILEPATH = "/data/data/mws.greek.quiz/databases/lexicondata.db";
	private Cursor tempCursor;
	
	//                                     0                1                  2                  3                4                     5                  6
	private static String[] FROM_LIST = {_ID, Constants.LEX_ENG, Constants.LEX_GRK, Constants.LEX_SEC, Constants.LEX_ROOT, Constants.LEX_SEQ, Constants.LEX_FLG };
	
	public lexiconDB(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + LEX_TABLE + " (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				LEX_GRK + " TEXT NOT NULL, " + 
				LEX_ENG + " TEXT NOT NULL, " + 
			   	LEX_SEC + " TEXT NOT NULL, " +
				LEX_FLG + " INTEGER, " + 
			   	LEX_SEQ + " INTEGER, " + 
			   	LEX_ROOT + " TEXT NOT NULL, " +
			   	LEX_EDT + " INTEGER);");

		db.execSQL("CREATE TABLE " + SECTIONS_TABLE + " (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				SEC_SECTION + " TEXT NOT NULL, " + 
				SEC_FLG + " INTEGER);");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Log.d("upgrader", "Upgrader has been called.");
		if (oldVersion < 2) {
            final String ALTER_TBL = "ALTER TABLE " + LEX_TABLE + " ADD COLUMN " + LEX_EDT + " INTEGER";
            db.execSQL(ALTER_TBL);
		}
		
		//if (oldVersion < 3) {
		//	db.execSQL("ALTER TABLE " + LEX_TABLE + " ADD COLUMN GRKTEMP INTEGER");
		//	db.execSQL("ALTER TABLE " + LEX_TABLE + " ADD COLUMN ROOTTEMP INTEGER");
        // 
        //    
		//}
		
		
	}
	

	
	public String importDatabase(Activity a) {
		String outString; 
		try {
        InputStream myInput = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/"+"NEW"+DATABASE_NAME);  // path to source

        File file = new File("/data/data/mws.apps.greekquiz/databases/"+DATABASE_NAME);  //path to destination
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.i("FO","File creation failed for " + file);
            }
        }
        												//path to destination
        OutputStream myOutput = new FileOutputStream("/data/data/mws.apps.greekquiz/databases/"+DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        outString = "File Copied.";

    } catch (Exception e) {
        outString = "Copy Failed.";
    	Log.i("FO","exception="+e);
    }



		return outString;
}

	 
	public String exportDB(Activity a) {
		String outString; 
			try {
	        InputStream myInput = new FileInputStream("/data/data/mws.apps.greekquiz/databases/"+DATABASE_NAME);

	        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+"BACKUP"+DATABASE_NAME);
	        if (!file.exists()){
	            try {
	                file.createNewFile();
	            } catch (IOException e) {
	                Log.i("FO","File creation failed for " + file);
	            }
	        }

	        OutputStream myOutput = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+"BACKUP" + DATABASE_NAME);

	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = myInput.read(buffer))>0){
	            myOutput.write(buffer, 0, length);
	        }

	        //Close the streams
	        myOutput.flush();
	        myOutput.close();
	        myInput.close();
	        outString = "File Copied.";

	    } catch (Exception e) {
	        outString = "Copy Failed.";
	    	Log.i("FO","exception="+e);
	    }



			return outString;
	}
	

}

