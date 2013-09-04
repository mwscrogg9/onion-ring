package mws.apps.greektexts;

import static android.provider.BaseColumns._ID;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import mws.apps.greektexts.R;




public class AddNewWords extends Activity implements TextWatcher  
{
 
 private boolean capsOnce;
 private boolean capsTwice;
 public String idString;
 private static String[] FROM = {_ID, Constants.SEC_SECTION, };
 private lexiconDB lexi;
 private Cursor cursor;
 private SQLiteDatabase db;
 private int[] baseCharacters;
 private int[] cycleAccent;
 private int[] cycleBreath;
 private int[] cycleIota;
 private int[] cycleMacron;
 private int[] cycleCase;
 private int[] rootLetters;
 private int secIndex;
 private boolean backFromDel;
 public Typeface font;
 private static String ORDER_BY = Constants.SEC_SECTION + " ASC";
                                //     0                1                2                     3                  4
 private static String[] editFrom = { _ID, Constants.LEX_ENG, Constants.LEX_GRK, Constants.LEX_SEC, Constants.LEX_EDT  };
 private static String[] editArg = { "1" };
 private Cursor editCursor;
 private ImageButton editButton;
 private Button addButton;
 private boolean thereAreEdits;
 private boolean editing;
 private int currentlyEditing;
 
 
 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_words);
        Log.d("AddNewWords", "onCreate()");
        
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        
        editing = false;
        
    }
    @Override
	protected void onStart() {
	    super.onStart(); 
        Log.d("AddNewWords", "onStart()");
	    lexi = new lexiconDB(this);
		db = lexi.getWritableDatabase();
        
		
        Resources r = getResources();
        baseCharacters = r.getIntArray(R.array.BaseCharacters);
        rootLetters = r.getIntArray(R.array.RootLetter);
        cycleAccent = r.getIntArray(R.array.CycleAccent);
        cycleBreath = r.getIntArray(R.array.CycleBreath);
        cycleIota = r.getIntArray(R.array.CycleIota);
        cycleMacron = r.getIntArray(R.array.CycleMacron);
        cycleCase = r.getIntArray(R.array.CycleCase);
        
        
        font = Typeface.createFromAsset(getAssets(),"IFAOGrecMWS41.ttf");
               
        capsOnce = false;
        capsTwice = false;
        
        idString = "";
        
		
		resetSpinner(backFromDel);
		secIndex = 0;
		backFromDel = false;
		
		EditText tv = (EditText) findViewById(R.id.secondtextview);
		tv.setTypeface(font);
		tv.addTextChangedListener(this);
		
		editCursor = db.query(Constants.LEX_TABLE, editFrom, Constants.LEX_EDT + " = ?", editArg, null, null, null);
		
		addButton = (Button) findViewById(R.id.button_enter);					//get id of the "Add" button...
		if (editing) addButton.setText("Update");
		
		Log.d("editCursor", "" + editCursor.getCount() + " rows with edit flag set.");
		editButton = (ImageButton) findViewById(R.id.button_getedits);
		if (editCursor.getCount() > 0) {
			editCursor.moveToFirst();
			editButton.setImageResource(R.drawable.greenflagmicro);
			thereAreEdits = true;
		}
		else thereAreEdits = false;
		currentlyEditing = -1;
		
		
		
		
    }
	public void clickGetEdits(View v) {
		Log.d("clickGetEdits:", "there are " + editCursor.getCount() + " words for editing. currentlyEditing: " + currentlyEditing);
		if (thereAreEdits) {
			
			if (++currentlyEditing >= (editCursor.getCount())) currentlyEditing = 0;		//if you're currently editing the last item in the cursor, re-initialize.
			
			Log.d("clickGetEdits:", "about to moveToPosition " + currentlyEditing);
			editCursor.moveToPosition(currentlyEditing);     								//move editCursor to the next editable
			
			
			Button addButton = (Button) findViewById(R.id.button_enter);					//get id of the "Add" button...
			addButton.setText("Update");													//and change it to the "Update" button.
			
			//pull the word from the db and load for editing
			editing = true;																	//set the "editing flag" to true so that the "Add/Update" 
																							//button will behave correctly, and onStart() sets up the button
																							//correctly on restart
			
			String greekForEdit = editCursor.getString(2);									//greek text of the editable
			String englishForEdit = editCursor.getString(1);								//english text of the editable
			String sectionForEdit = editCursor.getString(3);								//section text of the editable
			
			EditText englishEditText = (EditText) findViewById(R.id.englishText);
			englishEditText.setText(englishForEdit);										//place the english text in the english textbox
		
			TextView greekTextView = (TextView) findViewById(R.id.secondtextview);
			greekTextView.setText(greekForEdit);											//place greek text in the greek textbox
			
			Spinner sectionSpin = (Spinner) findViewById(R.id.selectSectionSpinner);
			secIndex = 0;																	
			cursor = db.query(Constants.SECTIONS_TABLE, FROM, null, null, null, null, ORDER_BY);      //get a cursor full of section names
			
			for (int i = 0; i < cursor.getCount(); ++i) {									
				cursor.moveToPosition(i);													//starting with "0", advance the cursor through each section...
				String cycleSec = cursor.getString(1);										//grab the section name...
				//Log.d(cycleSec, cursor.getString(1) + "  i: " + i);							//enter in the log which section we're about to evaluate...
				int compInt = cycleSec.compareTo(sectionForEdit);    						//see if "section" from the cursor is the same 
																							//as the one we're looking for...
				//Log.d("comp Int", Integer.toString(compInt));								//log the integer result of the comparison: 0 means they're equal
				if (compInt == 0) {															//if they're equal...
					secIndex = i;															//"secIndex" = the corresponding cursor index position...
					i = cursor.getCount();													//and there's no need to continue the for loop so set i to end the loop.
				}
			}
			
			//now, secIndex holds the desired position of the spinner.  Or, if it wasn't found, it holds 0.
			
			sectionSpin.setSelection(secIndex, true);										//set the spinner to the editable's section
			
			Log.d("clickGetEdits", "Word has been loaded, currentlyEditing = " + currentlyEditing);
			
			
		}
		
		else {																				//if there aren't any editables...
			Button addButton = (Button) findViewById(R.id.button_enter);					//get id of the "Add" button...
			if (editing) addButton.setText("Add");											//make sure it's on "add"
		}	
		
	}
    
	public void clickCommaHyphen(View view) {
	
		
		clickSound(AudioManager.FX_KEYPRESS_STANDARD);
		       
        TextView secText = (TextView) findViewById(R.id.secondtextview);
		Editable textWas = (Editable) secText.getText();
		
        //find position of cursor
        int start = secText.getSelectionStart();
        int end = secText.getSelectionEnd();
        
        textWas.replace(start, end, ", -");
        
	}
    
    public void clickAlpha(View view) {
	
		
		clickSound(AudioManager.FX_KEYPRESS_STANDARD);
		
		Button button = (Button) view;
        CharSequence buttonChar = button.getText();
        char c = buttonChar.charAt(0);
        
        if (capsTwice) {        
        	int cInt = c;		// if greek caps lock is on
        	if (cInt == 962) {++cInt;}  //if "final sigma" then get capital for regular sigma
        	cInt = cInt - 32;
        	c = (char) cInt;        	
        }
         else {
        }
        
        char cArray[] = {c};
        
        String newLetter = new String(cArray);
        TextView secText = (TextView) findViewById(R.id.secondtextview);
		Editable textWas = (Editable) secText.getText();
		
        
        //find position of cursor
        int start = secText.getSelectionStart();
        int end = secText.getSelectionEnd();
        
        textWas.replace(start, end, newLetter);
        
               
	}
	
	public void clickMacron(View view) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		
		EditText secText = (EditText) findViewById(R.id.secondtextview);
		
		int cursorPosition = secText.getSelectionStart();
		CharSequence enteredText = secText.getText().toString();
		if ((cursorPosition > 0) && (enteredText.length() > 0)) {
			CharSequence targetText = enteredText.subSequence(cursorPosition-1, cursorPosition);
			char targetChar = targetText.charAt(0);
			int targetCharCode = targetChar;
		    int i = -1;
			do {
				++i;
			} while ((baseCharacters[i] != targetCharCode) && ((baseCharacters[i] != 0))  );
			if (baseCharacters[i] != cycleMacron[i]) {  //If a change is in order:
				char newLetterCharArray[] = {(char) cycleMacron[i]}; 
				String newLetter = new String(newLetterCharArray);
				int index = cursorPosition - 1;
				SpannableStringBuilder editMe = new SpannableStringBuilder(enteredText);
				editMe.replace(index,  index+1, newLetter);
				secText.setText(editMe);
				secText.setSelection(cursorPosition, cursorPosition);
			}
		}
	}
	public void clickIota(View view) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		
		EditText secText = (EditText) findViewById(R.id.secondtextview);
		
		int cursorPosition = secText.getSelectionStart();
		CharSequence enteredText = secText.getText().toString();
		if ((cursorPosition > 0) && (enteredText.length() > 0)) {
			CharSequence targetText = enteredText.subSequence(cursorPosition-1, cursorPosition);
			char targetChar = targetText.charAt(0);
			int targetCharCode = targetChar;
		    int i = -1;
			do {
				++i;
			} while ((baseCharacters[i] != targetCharCode) && ((baseCharacters[i] != 0))  );
			if (baseCharacters[i] != cycleIota[i]) {  //If a change is in order:
				char newLetterCharArray[] = {(char) cycleIota[i]}; 
				String newLetter = new String(newLetterCharArray);
				int index = cursorPosition - 1;
				SpannableStringBuilder editMe = new SpannableStringBuilder(enteredText);
				editMe.replace(index,  index+1, newLetter);
				secText.setText(editMe);
				secText.setSelection(cursorPosition, cursorPosition);
			}
		}
	}
	public void clickClear(View view) {

		clickSound(AudioManager.FX_KEYPRESS_DELETE);
		
		EditText secText = (EditText) findViewById(R.id.secondtextview);
		EditText english = (EditText) findViewById(R.id.englishText);
		
		english.setText("");
		secText.setText("");
		
		secText.requestFocus();
		
		
		if (editing) {
			Button addButton = (Button) findViewById(R.id.button_enter);					//get id of the "Add" button...
			addButton.setText("Add");
			editing = false;
		}
		
		
	}
	
	public void clickAccent(View view) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		
		EditText secText = (EditText) findViewById(R.id.secondtextview);
		
		int cursorPosition = secText.getSelectionStart();
		CharSequence enteredText = secText.getText().toString();
		if ((cursorPosition > 0) && (enteredText.length() > 0)) {
			CharSequence targetText = enteredText.subSequence(cursorPosition-1, cursorPosition);
			char targetChar = targetText.charAt(0);
			int targetCharCode = targetChar;
		    int i = -1;
			do {
				++i;
			} while ((baseCharacters[i] != targetCharCode) && ((baseCharacters[i] != 0))  );
			if (baseCharacters[i] != cycleAccent[i]) {  //If a change is in order:
				char newLetterCharArray[] = {(char) cycleAccent[i]}; 
				String newLetter = new String(newLetterCharArray);
				int index = cursorPosition - 1;
				SpannableStringBuilder editMe = new SpannableStringBuilder(enteredText);
				editMe.replace(index,  index+1, newLetter);
				secText.setText(editMe);
				secText.setSelection(cursorPosition, cursorPosition);
			}
		}
	}
	public void clickBreath(View view) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		
		EditText secText = (EditText) findViewById(R.id.secondtextview);
		
		int cursorPosition = secText.getSelectionStart();
		CharSequence enteredText = secText.getText().toString();
		if ((cursorPosition > 0) && (enteredText.length() > 0)) {
			CharSequence targetText = enteredText.subSequence(cursorPosition-1, cursorPosition);
			char targetChar = targetText.charAt(0);
			int targetCharCode = targetChar;
		    int i = -1;
			do {
				++i;
			} while ((baseCharacters[i] != targetCharCode) && ((baseCharacters[i] != 0))  );
			if (baseCharacters[i] != cycleBreath[i]) {  //If a change is in order:
				char newLetterCharArray[] = {(char) cycleBreath[i]}; 
				String newLetter = new String(newLetterCharArray);
				int index = cursorPosition - 1;
				SpannableStringBuilder editMe = new SpannableStringBuilder(enteredText);
				editMe.replace(index,  index+1, newLetter);
				secText.setText(editMe);
				secText.setSelection(cursorPosition, cursorPosition);
			}
		}
	}
	public void clickNewSec(View view) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		
		Intent intentToAddSec = new Intent(this,ListAndAddSecs.class);
		intentToAddSec.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		//intentToAddSec.putExtra("likelyName", value);
		startActivity(intentToAddSec);
		
		
	}
	public void goToDeleteWord(View v) {
		clickSound(AudioManager.FX_KEYPRESS_SPACEBAR);
		Intent intentToDelWords = new Intent(this, DeleteWords.class);
		//intentToDelWords.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		//intentToDelWords.putExtra("likelyName", value);
		startActivityForResult(intentToDelWords, 1);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		
		if (data.getCharSequenceExtra("GreekWord") != "") {
			EditText secText = (EditText) findViewById(R.id.secondtextview);
			secText.setText(data.getCharSequenceExtra("GreekWord"));
		}
		if (data.getCharSequenceExtra("EnglishWord") != "") {
			EditText english = (EditText) findViewById(R.id.englishText);
			english.setText(data.getCharSequenceExtra("EnglishWord"));
		}
		if (data.getCharSequenceExtra("Section") != "") {
			secIndex = 0;
			Log.d("extraSec", data.getCharSequenceExtra("Section").toString());
			for (int i = 0; i<cursor.getCount(); ++i) {
				cursor.moveToPosition(i);
				String cycleSec = cursor.getString(1);
				Log.d(cycleSec, cursor.getString(1) + "  i: " + i);
				int compInt = cycleSec.compareTo(data.getCharSequenceExtra("Section").toString());
				Log.d("comp Int", Integer.toString(compInt));
				if (compInt == 0) {
					secIndex = i;
					i = cursor.getCount();
				}
			backFromDel = true;
			}
		}
		
		
	}
	public void resetSpinner(Boolean fromReturn) {
		cursor = db.query(Constants.SECTIONS_TABLE, FROM, null, null, null, null, ORDER_BY);
		String[] from = new String[] {Constants.SEC_SECTION};
		int[] to = new int[] {android.R.id.text1};
		SimpleCursorAdapter spinAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to, 2);
		spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		Spinner sectionSpin = (Spinner) findViewById(R.id.selectSectionSpinner);
		sectionSpin.setAdapter(spinAdapter);
		if (fromReturn) {
			Log.d("resetSpinner()", "Resetting:  backFromDel = " + Boolean.toString(fromReturn) + ", secIndex = " + secIndex);
			sectionSpin.setSelection(secIndex, true);}
		else {Log.d("resetSpinner()", "no spinner reset:  backFromDel = " + Boolean.toString(fromReturn) + ", secIndex = " + secIndex);}
		
		
	}
	
	public void clickAddNewWord(View view) {
		Log.d("clickAddNewWord","entering clickAddNewWord; currentlyEditing" + currentlyEditing);
		clickSound(AudioManager.FX_KEYPRESS_RETURN);
		//grab typed in english word
		EditText englishEditText = (EditText) findViewById(R.id.englishText);
		String englishWord = englishEditText.getText().toString();
		
		
		//grab typed in greek word
		TextView greekTextView = (TextView) findViewById(R.id.secondtextview);
		String greekWord = greekTextView.getText().toString();
		
		
		//grab section name
		Spinner sectionSpin = (Spinner) findViewById(R.id.selectSectionSpinner);
		String selectedSection = ((Cursor) sectionSpin.getSelectedItem()).getString(1);
					
		//form the stripped name
		char[] strippedGreekWordArray;  strippedGreekWordArray = new char[greekWord.length()]; 
        char[] greekLetterArray;  greekLetterArray = new char[greekWord.length()];
        greekWord.getChars(0, greekWord.length() - 0, greekLetterArray, 0); //populate greekLetterArray w/ greekWord characters

        for (int i = 0; i < greekLetterArray.length; ++i) {  //cycle through each letter in greekWord to find non-diacritical root letter
               if (greekLetterArray[i] > 937)  {  //only need to cycle through baseCharacters if the Char is not already a basic capital letter.
                     int j = -1;  //initialize index to cycle through the "baseCharacters" array.
                     do {++j;} while ((baseCharacters[j] != (int) greekLetterArray[i]) && (baseCharacters[j] != 0));
                            //this loop finds the correct index j to use to grab the rootLetter.
                     strippedGreekWordArray[i] = (char) rootLetters[j];  //i-th element of the stripped character array becomes the j-th rootLetter
               }
               else {
                     strippedGreekWordArray[i] = greekLetterArray[i];  //it had been determined to already be stripped, so just use the char as is.
               }
        }
        String strippedGreekWord = new String(strippedGreekWordArray, 0, strippedGreekWordArray.length);
		
		ContentValues values = new ContentValues();
    	
    	//add entry to table
	    	values.put(Constants.LEX_GRK, greekWord);
			values.put(Constants.LEX_ENG, englishWord);
			values.put(Constants.LEX_SEC, selectedSection);
	    	values.put(Constants.LEX_ROOT, strippedGreekWord);
	    	 
			long result = db.insertOrThrow(Constants.LEX_TABLE, null, values);
	    	String string = englishWord + " " + greekWord + " " + selectedSection + "\n" +
	    	                "Result: " + result + "\n" + "Roots: " + strippedGreekWord;
	    
	    //update count of the selected section
	    	//1.  Get the new count
	    	long newCount = DatabaseUtils.queryNumEntries(db, Constants.LEX_TABLE, Constants.LEX_SEC + " = ?", new String[] {selectedSection});
			
	    	//2.  "put" the new count as a value
	    	values.clear();
	    	values.put(Constants.SEC_FLG, newCount);
	    	
	    	//3.  "update" the row in SECTIONS_TABLE that has selectedSection as its SEC_SECTION
	    	db.update(Constants.SECTIONS_TABLE, values, Constants.SEC_SECTION + " = ?", new String[] {selectedSection});
	   
	    	
	    	
	   Log.d("clickAddNew:", "new line added to db.  About to see if old entry needs to be deleted.");
	   Log.d("clickAddNew:", "editing: " + editing);
	    	
	    if (editing) {
	    	Log.d("clickAddNew:", "before deletion: currentlyEditing = " + currentlyEditing);
	    	Log.d("clickAddNew:", "before deletion: editCursor getCount() = " + editCursor.getCount());
	   		String[] deletionRow = {Integer.toString(editCursor.getInt(0))};							//if you had been editing...
 		 	int i = db.delete(Constants.LEX_TABLE, _ID + " =?", deletionRow);							//delete the old entry that was flagged for editing...
 			Log.d("deleted", "Deleted row " + deletionRow[0] + ", deleted " + i + " rows.");				//log it...
 			
 			editCursor = db.query(Constants.LEX_TABLE, editFrom, Constants.LEX_EDT + " = ?", editArg, null, null, null);	//update the editCursor 
 			
 			if (currentlyEditing >= editCursor.getCount()) currentlyEditing = -1;					//if you had been editing the last item in the cursor,
 			else --currentlyEditing;																						//reinitialize currentlyEditing to -1 (it gets incremented to 0 in the
 																									//clickGetEdits() method.
 			
 			if (editCursor.getCount() == 0) {														//if no more editables...
 				editing = false;
 				thereAreEdits = false;
 				editButton.setImageResource(R.drawable.greenflagblackmicro);
 				addButton.setText("Add");
 				
 			}
 			
 			clickGetEdits(null);
 			
	    }
	    
		
	    	((Button) view).setEnabled(false);
		
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage for you, sir: \n");
    	alertDialog.setMessage(string);
    	alertDialog.show();
    	
	}
	
	
	public void clickMenu(View view) {
		clickSound(AudioManager.FX_KEYPRESS_RETURN);
		
		db.close();
	    lexi.close();	
		
		finish();
		
	}
	
	public void clickCaps(View view) {
		// if caps is pressed once, make the selected letter(s) caps.  if caps is pressed twice, set caps lock on and turn button blue.
		if (capsOnce) {   //if caps has already been pressed once
			capsOnce = false;   
			capsTwice = true;    //go into caps lock mode
			((Button) view).getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF00AA00));
			//msgbox("caps lock on");
		}
		else {     //caps either hasn't been pressed at all, or we're already in caps lock
			if (capsTwice) {    //if you're already in capslock 
				capsTwice = !capsTwice;    //turn caps lock off
				((Button) view).getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00000000));
				//msgbox("caps lock off");
			}
			else {   // or else you're going into capsOnce mode
				capsOnce = true;
				//msgbox("caps Once");
				
				//here we will make the selected letters capitalized.
				EditText secText = (EditText) findViewById(R.id.secondtextview);
				
				int cursorPosition = secText.getSelectionStart();
				CharSequence enteredText = secText.getText().toString();
				if ((cursorPosition > 0) && (enteredText.length() > 0)) {
					CharSequence targetText = enteredText.subSequence(cursorPosition-1, cursorPosition);
					char targetChar = targetText.charAt(0);
					int targetCharCode = targetChar;
				    int i = -1;
					do {
						++i;
					} while ((baseCharacters[i] != targetCharCode) && ((baseCharacters[i] != 0))  );
					if (baseCharacters[i] != cycleCase[i]) {  //If a change is in order:
						char newLetterCharArray[] = {(char) cycleCase[i]}; 
						String newLetter = new String(newLetterCharArray);
						int index = cursorPosition - 1;
						SpannableStringBuilder editMe = new SpannableStringBuilder(enteredText);
						editMe.replace(index,  index+1, newLetter);
						secText.setText(editMe);
						secText.setSelection(cursorPosition, cursorPosition);
					}
				}
			}
		}
		
	}
	public void clickSound(int keyType) {
		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
		float vol = (float) 0.5; //This will be half of the default system sound
		am.playSoundEffect(keyType, vol);
	}
	
	public void msgbox(String string) {
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage for you, sir: \n");
    	alertDialog.setMessage(string);
    	alertDialog.show();
	}
	@Override
	protected void onStop() {
	    super.onStop(); 
	    Log.d("AddNewWords:","onStop");
	    
	    //db.close();
	    //lexi.close();	
	}
	public void setPassId(int i){
	}
	@Override
	public void afterTextChanged(Editable arg0) {
		
		Button addButton = (Button) findViewById(R.id.button_enter);
		addButton.setEnabled(true);
	}
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		
	}
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		
	}
}
