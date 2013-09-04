package mws.apps.greekquiz;

import static android.provider.BaseColumns._ID;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class DeleteWords extends Activity {

	private lexiconDB lexi;
	private SQLiteDatabase db;
	private Cursor cursor;
	public Typeface font;
	private ListView listview;
	private int idOfRowOnBlock;
	private SimpleCursorAdapter listAdapter;
	private String secForCount;
	
	//                                    0               1                  2                    3                 4 
	private static String[] FROM_LIST = {_ID, Constants.LEX_ENG, Constants.LEX_GRK, Constants.LEX_SEC, Constants.LEX_ROOT };
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_words);
    
        font = Typeface.createFromAsset(getAssets(),"IFAOGrecMWS41.ttf");
        
        if (savedInstanceState == null) {idOfRowOnBlock = -1;}
        
	}
	
	@Override
	protected void onStart() {
	    super.onStart(); 
	    lexi = new lexiconDB(this);
		db = lexi.getWritableDatabase();
		
		cursor = db.query(Constants.LEX_TABLE, FROM_LIST, null, null, null, null, Constants.LEX_ROOT);
	    
		
		
		String[] from = new String[] {Constants.LEX_GRK,   Constants.LEX_ENG,   Constants.LEX_SEC};
		int[] to = new int[]         {R.id.dellistgrkword, R.id.dellistengword, R.id.dellistsec};
	    listAdapter = new SimpleCursorAdapter(this, R.layout.delete_word_list_item, cursor, from, to, 2);
		
		listview = (ListView) findViewById(R.id.delwordlist);					
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listview.setAdapter(listAdapter); 
		listview.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("Click", "onItemClick");
			}});
		
		listview.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				cursor.moveToPosition(arg2);
				
				idOfRowOnBlock = cursor.getInt(0);
				
				TextView greekWordForDeletion= (TextView) findViewById(R.id.greekWordForDeletion);
				greekWordForDeletion.setTypeface(font);
				greekWordForDeletion.setText(cursor.getString(2));
				
				TextView secOfGreekWordForDeletion= (TextView) findViewById(R.id.secOfGreekWordForDeletion);
				secOfGreekWordForDeletion.setTypeface(font);
				secOfGreekWordForDeletion.setText(cursor.getString(3));
				secForCount = cursor.getString(3);
				
				TextView englishWordForDeletion= (TextView) findViewById(R.id.englishWordForDeletion);
				englishWordForDeletion.setTypeface(font);
				englishWordForDeletion.setText(cursor.getString(1));			
				
				return false;
			}});
    }
 	public void msgbox(String string) {
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage for you, sir: \n");
    	alertDialog.setMessage(string);
    	//alertDialog.show();
	}
 	public void deleteThisWord(View v) {
 		
	 	if (idOfRowOnBlock != -1) {
	 		String[] deletionRow = {Integer.toString(idOfRowOnBlock)};
 		 	
 		 	
 			int i = db.delete(Constants.LEX_TABLE, _ID + " =?", deletionRow);
 			msgbox("Delete " + idOfRowOnBlock + "\n" + 
 			       "Deleted " + i + " rows.");
 			
 			cursor = db.query(Constants.LEX_TABLE, FROM_LIST, null, null, null, null, Constants.LEX_ROOT);
 			listAdapter.changeCursor(cursor);
 			
 			//update count of the selected section
		    	//1.  Get the new count
		    	long newCount = DatabaseUtils.queryNumEntries(db, Constants.LEX_TABLE, Constants.LEX_SEC + " = ?", new String[] {secForCount});
				
		    	//2.  "put" the new count as a value
		    	ContentValues values = new ContentValues();
		    	values.put(Constants.SEC_FLG, newCount);
		    	
		    	//3.  "update" the row in SECTIONS_TABLE that has secForCount as its SEC_SECTION
		    	db.update(Constants.SECTIONS_TABLE, values, Constants.SEC_SECTION + " = ?", new String[] {secForCount});
 			
			TextView greekWordForDeletion= (TextView) findViewById(R.id.greekWordForDeletion);
			greekWordForDeletion.setText("");
			
			TextView secOfGreekWordForDeletion= (TextView) findViewById(R.id.secOfGreekWordForDeletion);
			secOfGreekWordForDeletion.setText("");
			
			TextView englishWordForDeletion= (TextView) findViewById(R.id.englishWordForDeletion);
			englishWordForDeletion.setText("");	
			
			idOfRowOnBlock = -1;
	 	}
 		
 	}
 	public void passThisWord(View v) {
		TextView greekWordForDeletion= (TextView) findViewById(R.id.greekWordForDeletion);
		CharSequence currentSelectionGreek = greekWordForDeletion.getText();
		TextView secOfGreekWordForDeletion= (TextView) findViewById(R.id.secOfGreekWordForDeletion);
		CharSequence currentSelectionSection = secOfGreekWordForDeletion.getText();
		TextView englishWordForDeletion= (TextView) findViewById(R.id.englishWordForDeletion);
		CharSequence currentSelectionEnglish = englishWordForDeletion.getText();	
		
		Log.d("CurSelGrk", currentSelectionGreek.toString());
		Log.d("CurSelEng:", currentSelectionEnglish.toString());
		Log.d("CurSelSec:", currentSelectionSection.toString());
		
		
			Intent intent = new Intent();
			intent.putExtra("GreekWord", currentSelectionGreek);
			intent.putExtra("EnglishWord", currentSelectionEnglish);
			intent.putExtra("Section", currentSelectionSection);
			
			setResult(Activity.RESULT_OK, intent);
			if (idOfRowOnBlock != -1) {
		 		String[] deletionRow = {Integer.toString(idOfRowOnBlock)};
	 		 	
	 		 	
	 			int i = db.delete(Constants.LEX_TABLE, _ID + " =?", deletionRow);
				Log.d("Deleting:", "Deleted " + i + " rows.");}
			finish();
 	}
 	public void justFinish(View v) {
 		Intent intent = new Intent();
		intent.putExtra("GreekWord", "");
		intent.putExtra("EnglishWord", "");
		intent.putExtra("Section", "");
		
		setResult(Activity.RESULT_OK, intent);
 		finish();
 	}
 	@Override
	protected void onStop() {
	    super.onStop(); 
	    Log.d("DeleteWords","onStop");
	    db.close();
	    lexi.close();
	    
	    
	}

}
