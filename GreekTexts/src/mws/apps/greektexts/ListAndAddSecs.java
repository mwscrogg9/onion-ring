package mws.apps.greektexts;

import static android.provider.BaseColumns._ID;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import mws.apps.greektexts.R;


public class ListAndAddSecs extends Activity implements OnItemClickListener, OnItemLongClickListener    {
	
	private static String[] FROM = {_ID, Constants.SEC_SECTION, };
	private Cursor cursor;
	private lexiconDB lexi;
	private SQLiteDatabase db;
	private ListView listview;
	private SimpleCursorAdapter listAdapter;

	 //private static String ORDER_BY = Constants.SEC_SECTION + " ASC";
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_and_add_secs);
        EditText E = (EditText) findViewById(R.id.newsectionentry);
        E.setText(this.getIntent().getStringExtra("likelyName"));
        
        lexi = new lexiconDB(this);
		db = lexi.getWritableDatabase();
		cursor = db.query(Constants.SECTIONS_TABLE, FROM, null, null, null, null, Constants.SEC_SECTION);
        String[] from = new String[] {Constants.SEC_SECTION};
		int[] to = new int[] {android.R.id.text1};
        listAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to, 2);
		
		listview = (ListView) findViewById(R.id.listofsections);
		
		//String[] items = new String[] {"item 1", "item 2", "item 3"};
		//ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		
		listview.setAdapter(listAdapter);
        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(this);

    }
    
    public void addNewSectionName(View V) {
    	//Grab the name of the new Section:
    	EditText E = (EditText) findViewById(R.id.newsectionentry);
        String[] eVal = {E.getText().toString().trim()};    	
        //Throw it into the db as a new row in the Sections table:
    	lexiconDB lexi = new lexiconDB(this);
    	SQLiteDatabase db = lexi.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	
    	Cursor countCursor = db.query(Constants.SECTIONS_TABLE, FROM, Constants.SEC_SECTION + "=?", eVal, null, null, null); 
    	int count = countCursor.getCount();
    	
    	if (count == 0) {
    	
	    	values.put(Constants.SEC_SECTION, E.getText().toString());
			values.put(Constants.SEC_FLG, 0);
	    	long result = db.insertOrThrow(Constants.SECTIONS_TABLE, null, values);
	    	
	    	
	    	
	    	AlertDialog alertDialog;
	    	alertDialog = new AlertDialog.Builder(this).create();
	    	alertDialog.setTitle("Row added");
	    	alertDialog.setMessage("New total: " + result + " sections.  Count was " + count + ", eVal was " + eVal[0]);
	    	alertDialog.show();
	
	    	cursor = db.query(Constants.SECTIONS_TABLE, FROM, null, null, null, null, Constants.SEC_SECTION);
	    	
	        listAdapter.changeCursor(cursor);
	        
	        
	        
	        db.close();}
        
    	else {AlertDialog alertDialog;
	    	  alertDialog = new AlertDialog.Builder(this).create();
	    	  alertDialog.setTitle("No.");
	    	  alertDialog.setMessage("Section title already exists.");
	    	  alertDialog.show();
    		
    	}

        
        
        
    	
    }


    
    public void closeListAndAdd(View V) {
    	
    	finish();
    }
    
    
    
   
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		TextView txt1 = (TextView) arg1;
				
		EditText E = (EditText) findViewById(R.id.newsectionentry);
		E.setText(txt1.getText().toString());
		
		
		
	}
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int arg2, long arg3) {
		
		
		Cursor delCursor = db.query(Constants.SECTIONS_TABLE, FROM, Constants._ID + " = " + Long.toString(arg3), null, null, null, 			null);
		delCursor.moveToFirst();
		
		String[] LexCheck_FROM = {_ID, Constants.LEX_SEC,};
		Cursor checkLex = db.query(Constants.LEX_TABLE, LexCheck_FROM, 	Constants.LEX_SEC + " = '" + delCursor.getString(1) + "'", null, null, 						null, null);
		
		if (checkLex.getCount() == 0) {
			
			db.delete(Constants.SECTIONS_TABLE, Constants._ID + " = " + Long.toString(arg3), null);
			
			cursor = db.query(Constants.SECTIONS_TABLE, FROM, null, null, null, null, Constants.SEC_SECTION);
			
			listAdapter.changeCursor(cursor);
			
			
			
			
			msgbox("You are have deleted section ~" + 							delCursor.getString(1) + "~, db _ID " + 						delCursor.getString(0) + ".");
			}
		else {
			msgbox("Section ~" + delCursor.getString(1) + "~, db _ID " 				+ delCursor.getString(0) + ", still has " +
		            checkLex.getCount() + " items assigned to it, and 					cannot be deleted.");
			}
		
		return false;
	}
	public void msgbox(String string) {
		AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Mesage for you, sir: \n");
    		alertDialog.setMessage(string);
    		//alertDialog.show();
	}

	
    
}
