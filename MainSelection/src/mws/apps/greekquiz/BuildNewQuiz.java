package mws.apps.greekquiz;

import static android.provider.BaseColumns._ID;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class BuildNewQuiz extends Activity {
	//public class BuildNewQuiz extends Activity implements OnItemClickListener {

	private lexiconDB lexi;
	private SQLiteDatabase db;
	private Cursor cursor;
	private Cursor secIdListCursor;
	
	private static String[] FROM_BUILD = {_ID, Constants.LEX_ENG, Constants.LEX_GRK, Constants.LEX_SEC, Constants.LEX_SEQ, Constants.LEX_FLG, };
	private static String[] FROM_LIST  = {_ID, Constants.SEC_SECTION, Constants.SEC_FLG};
	
 
	
	

@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_build_new_quiz);
	
		    lexi = new lexiconDB(this);
			db = lexi.getWritableDatabase();
			cursor = db.query(Constants.SECTIONS_TABLE, FROM_LIST, null, null, null, null, Constants.SEC_SECTION);
	        String[] from = new String[] {Constants.SEC_SECTION, };
			int[] to = new int[] {android.R.id.text1, };
	        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, cursor, from, to, 2);
			
			ListView listview = (ListView) findViewById(R.id.selectSectionsForQuiz);					
			listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listview.setAdapter(listAdapter);
			//listview.setOnItemClickListener(this);
			
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


    CheckedTextView check = (CheckedTextView)view;
    check.setChecked(!check.isChecked());
    boolean click = !check.isChecked();
    check.setChecked(click);
    if (click) {
            Toast.makeText(this, "Not Selected", Toast.LENGTH_SHORT).show();
    	} else {
    		Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show();
    	} 
	}

	public void onRadioButtonClicked(View v) {
		
	}
	public void startNewQuiz(View v) {
		ListView listview = (ListView) findViewById(R.id.selectSectionsForQuiz);
		Log.d("getCheckedItemIds()", "getCheckedItemIds().length = " + listview.getCheckedItemIds().length);		
		if (listview.getCheckedItemIds().length > 0) {
			
			
			StringBuilder whereClauseString = new StringBuilder();
			StringBuilder selectedSectionsIds = new StringBuilder();
			
			
		
			long[] checkedIds = listview.getCheckedItemIds();	//a string of longs, one for each selected position from the listview
			
			for (int i = 0; i < checkedIds.length; ++i) {
				selectedSectionsIds.append(_ID + " = " + Long.toString(checkedIds[i]) + " OR " );
			}
			selectedSectionsIds.delete(selectedSectionsIds.length() - 3, selectedSectionsIds.length());
			Log.d("selectedSectionsIds:  ", selectedSectionsIds.toString() );
			secIdListCursor = db.query(Constants.SECTIONS_TABLE, FROM_LIST  , selectedSectionsIds.toString(), null , null, null, null);
			Log.d("secIdListCursor", "secIdListCursor.getCount() is " + secIdListCursor.getCount());
			
			for (int i = 0; i < secIdListCursor.getCount(); ++i) {
				secIdListCursor.moveToPosition(i);
				Log.d("secIdListCursor", "Row " + i + " is _id " + secIdListCursor.getInt(0) + ", named " + secIdListCursor.getString(1));
			}

			for (int i = 0; i < checkedIds.length; ++i) {
				secIdListCursor.moveToPosition(i);
				whereClauseString.append(Constants.LEX_SEC + " = '" + secIdListCursor.getString(1) + "' OR ");  //builds the array of selected section names	
			}
			
			whereClauseString.delete(whereClauseString.length() - 3, whereClauseString.length());  //delete the last " OR "
			
			Log.d("LEXwhereclause: ", whereClauseString.toString());
			
			//Build cursor of TBL_LEX entries corresponding to the selected sections:
			Log.d("buildCursor:", "Starting to build the quiz Cursor.");
			Cursor buildCursor = db.query(Constants.LEX_TABLE, FROM_BUILD, whereClauseString.toString(), null, null, null, null); 
			Log.d("buildCursor:", "Finished building the quiz Cursor.");
			
			
			msgbox("num of quiz entries: " + buildCursor.getCount());
		
			if (buildCursor.getCount() == 0) {
				msgbox("No words have been entered for the selected section.");
				
			}
			
			else {   
				
				//First clear flags from entire db:
				ContentValues clearflagvalues = new ContentValues();
				clearflagvalues.put(Constants.LEX_FLG, 0);
				clearflagvalues.put(Constants.LEX_SEQ, 0);
				db.update(Constants.LEX_TABLE, clearflagvalues, null, null);
				
				
				
				
				ContentValues values = new ContentValues();
				String[] args = new String[1];
				
				Log.d("buildCursor:", "Starting to build mixUp array.");
				int[] mixArray = mixUp(buildCursor.getCount());
				Log.d("buildCursor:", "Finished building mixUp array.");
				
				Log.d("buildCursor:", "Starting to enter the random sequence into the table.");
				for (int j = 0; j < buildCursor.getCount(); ++j) {
					
				
					buildCursor.moveToNext();
					args[0] = "" + buildCursor.getInt(0);
					
					values.clear();
					values.put(Constants.LEX_SEQ, mixArray[j]);
					
					db.update(Constants.LEX_TABLE, values, "_id = ?", args);
					
					
				}
				Log.d("buildCursor:", "Finished entering the random sequence into the table.");
			db.close();
			lexi.close();
			savePreferences();
			Intent i = new Intent(this,QuizScreen.class);
			startActivity(i);
			finish();	
			
			}
				
			 
		
		}
		
		
		
		
		
		
		
		 
		
		
	}
	
	
	public int[] mixUp(int count) {
		
			
			int[] array = new int[count];
			
			for (int i = 0; i < count; ++i ) {
				array[i] = i + 1;
			}
			
			
			Random rng = new Random();   // i.e., java.util.Random.
	   int n = array.length;        // The number of items left to shuffle (loop invariant).
	   while (n > 1)
		   {
		      int k = rng.nextInt(n);  // 0 <= k < n.
		      n--;                     // n is now the last pertinent index;
		      int temp = array[n];     // swap array[n] with array[k] (does nothing if k == n).
		      array[n] = array[k];
		      array[k] = temp;
		   }
	   while (n > 1)
		   {
		      int k = rng.nextInt(n);  // 0 <= k < n.
		      n--;                     // n is now the last pertinent index;
		      int temp = array[n];     // swap array[n] with array[k] (does nothing if k == n).
		      array[n] = array[k];
		      array[k] = temp;
		   }
		   
	   return array;
	   
	}
	public void msgbox(String string) {
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage for you, sir: \n");
    	alertDialog.setMessage(string);
    	//alertDialog.show();
	}
	public void returnToMain(View v) {
		db.close();
		lexi.close();
		finish();
	}
private void savePreferences(){
		
		
		Boolean showGreek;
		int nextFlag;
		Boolean autoRepeat;
		
		
		RadioGroup greekRadio = (RadioGroup) findViewById(R.id.greekorenglishbuttongroup);
		RadioGroup flagRadio = (RadioGroup) findViewById(R.id.flagbuttongroup);
		RadioGroup repeatRadio = (RadioGroup) findViewById(R.id.repeatbuttongroup);
		
		if(greekRadio.getCheckedRadioButtonId()==R.id.radioShowGreek) {
				showGreek = true; 	}
		else {
				showGreek = false;  }
		
		if(flagRadio.getCheckedRadioButtonId()==R.id.RadioUseFlags) {
			    nextFlag = 1; 	}
		else {
				nextFlag = 0;  }
		
		if(repeatRadio.getCheckedRadioButtonId()==R.id.yesAutoRepeat) {
				autoRepeat = true; 	}
		else {
				autoRepeat = false;  }
				
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("SHOWGREEK", showGreek);
    	editor.putInt("NEXTFLAG", nextFlag);
    	editor.putBoolean("AUTOREPEAT", autoRepeat);
    	editor.putInt("CURRENTPOS", 0);
    	editor.putBoolean("FIRSTCLICK", false);
    	editor.putBoolean("ONFLAGS", false);
    	editor.putInt("NUMFLAGGED", 0);
    	editor.commit();
	}
	public void selectAll(View v) {
		ListView listview = (ListView) findViewById(R.id.selectSectionsForQuiz);
		for (int i = 0; i < listview.getCount(); ++i) {
			listview.setItemChecked(i, true);
		}
	}
	public void clearAll(View v) {
		ListView listview = (ListView) findViewById(R.id.selectSectionsForQuiz);
		for (int i = 0; i < listview.getCount(); ++i) {
			listview.setItemChecked(i, false);
		}
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View currentView = getCurrentFocus();
		msgbox("key up, view = " + currentView.getId());
		
		switch (keyCode) {        
		case 88:     				//Left on clicker       
			//do nothing            
			return true;        
		case 87:     				//Right on clicker                   
			//do nothing;            
			return true;        
		case 85:     				//Center on clicker                   
			if (currentView != null) {
				currentView.performClick();
			}
			return true;        
		case 24:     				//Up on clicker                   
			if (currentView == null) {
				findViewById(R.id.selectSectionsForQuiz).requestFocus();
			}
			else currentView.requestFocus(View.FOCUS_DOWN);            
			return true;     
		case 25:     				//Down on clicker                   
			if (currentView == null) {
				findViewById(R.id.radioShowGreek).requestFocus();
			}
			else currentView.requestFocus(View.FOCUS_DOWN);            
			return true;   
		case 129:     				//KEYBD on clicker                   
			//do nothing         
			return true;   
		case 164:     				//MUTE on clicker                   
			//do nothing          
			return true;   
		case 66:     				//ENTER on clicker                   
			//do nothing
			return true;
		default:            
			return super.onKeyUp(keyCode, event);    }
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {        
		case 88:     				//Left on clicker                
			//do nothing          
			return true;        
		case 87:     				//Right on clicker                      
			//do nothing          
			return true;        
		case 85:     				//Center on clicker                      
			//do nothing           
			return true;        
		case 24:     				//Up on clicker                       
			//do nothing          
			return true;     
		case 25:     				//Down on clicker                       
			//do nothing                   
			return true;   
		case 129:     				//KEYBD on clicker                   
			//do nothing          
			return true;   
		case 164:     				//MUTE on clicker                   
			//do nothing          
			return true;   
		case 66:     				//ENTER on clicker                   
			//do nothing
			return true;
		default:            
			return super.onKeyUp(keyCode, event);    }
	}
}
