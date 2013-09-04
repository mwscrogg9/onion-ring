package mws.apps.greektexts;

import static android.provider.BaseColumns._ID;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import mws.apps.greektexts.R;

public class QuizScreen extends Activity {

	private lexiconDB lexi;
	private SQLiteDatabase db;
	private Cursor cursor;           //    0               1                  2                  3                  4                  5                  6
	private static String[] FROM_PLAY = {_ID, Constants.LEX_ENG, Constants.LEX_GRK, Constants.LEX_SEC, Constants.LEX_SEQ, Constants.LEX_FLG, Constants.LEX_EDT};
	private Boolean showGreek;
	private int nextFlag;
	private Boolean autoRepeat;
	private int currentPos;
	private Boolean firstClick;  //firstClick being true means first click has occurred.
	private Boolean onFlags;
	private Boolean isDay;
	private int numFlagged;
	private int answerColumn;
	private int hintColumn;
	private Typeface font;
	private Boolean editThis;
	
	
	@Override
	public void onPause() {
	    super.onPause();
	    storeSavedPreferences();
	    
	}
	@Override
	protected void onStop() {
	    super.onStop(); 
	    db.close();
	    lexi.close();	
	}
	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_screen);
        //setLightDark();
                
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        
        editThis = false;
        
        if (!getSavedPreferences()) {
        	msgbox("There was a problem with the quiz.  Build another quiz.");
        	terminateQuiz();
        }
		
        if (onFlags) {
        	TextView currentPosText = (TextView) findViewById(R.id.quizprogress);
        	currentPosText.setTextColor(Color.RED);
        }
        
    	if (firstClick) {  //i.e., first click has occurred
    		turnLightsOn();  //no need to shade answer
    	}
    	else {				//i.e., still waiting for first click (this is default)
    		turnLightsOff();  //shad the answer text
    	}
    	
    	    	
    	if (showGreek) {    //i.e., need to mentally come up with english word
    		answerColumn = 1;   //(col 1 is ENG, this will be shaded)
    		hintColumn = 2;		//(col 2 is GRK, this will always be illuminated)
    		
    	}
    	else {              //i.e., need to mentally come up with greek word
    		answerColumn = 2;   //(col 2 is GRK, this will be shaded)
    		hintColumn = 1;      //(col 1 is ENG, this will always be illuminated)
    	}
    	
    	font = Typeface.createFromAsset(getAssets(),"IFAOGrecMWS41.ttf");
    	
    	//place custom fonts in the question and answer TextViews:
    	TextView hintText = (TextView) findViewById(R.id.quizhint);
    	hintText.setTypeface(font);
    	hintText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                clicknext(v);
            }
        });
    	
    	TextView answerText = (TextView) findViewById(R.id.quizanswer);
    	answerText.setTypeface(font); 
    	LinearLayout masterlayout = (LinearLayout) findViewById(R.id.masterlayout);
    	masterlayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                clicknext(v);
            }
        }); 
    	
    	setLightDark(false);	
    }
    
	@Override
	protected void onStart() {
	    super.onStart(); 
	    
        lexi = new lexiconDB(this);
		db = lexi.getWritableDatabase();
		String[] args = new String[1];
		args[0] = "0";
		if (onFlags) {     //i.e., you're now on the flagged items.
    		TextView currentPosText = (TextView) findViewById(R.id.quizprogress);
    		currentPosText.setTextColor(Color.RED);
    		queryByFlags();   //only grab the ones with flags.
    	}
    	else {
    		Log.d("buildQuiz:", "Starting to query by SEQ.");
    		queryBySeq();     //grab all of them, regardless of flag status.
    		Log.d("buildQuiz:", "Finished the query by SEQ.");
    	}
    	
		if (editThis) { //i.e., if true, green flag needs to be raised
    		Log.d("editThis", "onStart() found editThis to be TRUE");
    		ImageView editFlag = (ImageView) findViewById(R.id.editFlag);
    		editFlag.setImageResource(R.drawable.greenflag);
    	}
    	else Log.d("editThis", "onCreate() found editThis to be " + Boolean.toString(editThis));		
		
    	if (cursor.getCount() == 0) {
    		msgbox("The selected quiz has no quizzable words.  Try building a new quiz.");
    		terminateQuiz();
    	}
    	
    	cursor.moveToPosition(currentPos);
    	
    	if (!setWord()) {
    		msgbox("There was a problem loading the current word.  Try rebuilding the quiz.");
    	}
    	
    	 msgbox( "showGreek: " + showGreek.toString() + "\n" +
	        		"nextFlag: " + nextFlag + "\n" +
	        		"autoRepeat: " + autoRepeat.toString() + "\n" +
	        		"currentPos: " + currentPos + "\n" +
	        		"firstClick: " + firstClick.toString() + "\n" +
	        		"onFlags: " + onFlags.toString() + "\n" +
	        		"numberOfItems: " + cursor.getCount()); 
	}
	
	public void setEditFlag(View v) {
	//this is called by pressing the edit flag on the screen.  
	//It will check if the flag is already set, and then set or clear it as necessary.
	//then it will adjust the database row as necessary.
		
		if (editThis) { //already flagged for editing, so clear the flag
			//set gray flag
			ImageView flagImage = (ImageView) v;
			
			flagImage.setImageResource(R.drawable.greenflaggray);
			
			
			//turn off editThis
			editThis = false;
			
			//change the database to "0" for LEX_EDT
			ContentValues values = new ContentValues();
    		String[] args = new String[1];
    		args[0] = "" + cursor.getInt(0); 
			values.clear();
			values.put(Constants.LEX_EDT, 0);  
			Log.d("LEX_EDT:", "edit flag cleared, " + db.update(Constants.LEX_TABLE, values, "_id = ?", args) + " row(s) affected.");
			
			
		}
		else {
			//not flagged for editing, so it needs to be flagged
			//set green flag
			ImageView flagImage = (ImageView) v;
			
			flagImage.setImageResource(R.drawable.greenflag);
			
			
			//turn on editThis
			editThis = true;
			
			//change the database to "1" for LEX_EDT
			ContentValues values = new ContentValues();
    		String[] args = new String[1];
    		args[0] = "" + cursor.getInt(0);
			values.clear();
			values.put(Constants.LEX_EDT, 1);
			db.update(Constants.LEX_TABLE, values, "_id = ?", args);
			Log.d("LEX_EDT:", "edit flag set, " + db.update(Constants.LEX_TABLE, values, "_id = ?", args) + " row(s) affected.");
		}
	}
	
	private Boolean setWord() {
    	
    	firstClick = false;
    	
    	//place the hint:
    	TextView hintText = (TextView) findViewById(R.id.quizhint);
    	if (cursor.getString(hintColumn).length() < 20) {
    		hintText.setTextSize(92);
    		}
    	else {
    		hintText.setTextSize(56);
    	}
    	hintText.setText(cursor.getString(hintColumn));
    	
    	//place the answer:
    	TextView answerText = (TextView) findViewById(R.id.quizanswer);
    	if (cursor.getString(answerColumn).length() < 20) {
    		answerText.setTextSize(92);
    		}
    	else {
    		answerText.setTextSize(56);
    	}
    	answerText.setText(cursor.getString(answerColumn));
    	
    	int thisWordFlagStatus = cursor.getInt(5);
    	if(thisWordFlagStatus > 0) {
    		raiseFlag();
    	}
    	else {
    		lowerFlag();
    	}
    	
    	TextView currentPosText = (TextView) findViewById(R.id.quizprogress);
    	currentPosText.setText("Progress: " + (currentPos + 1) + " of " + cursor.getCount());
    	
    	TextView currentSecText = (TextView) findViewById(R.id.quizsection);
    	currentSecText.setText("Section: " + cursor.getString(3));
    	
    	TextView numFlagsText = (TextView) findViewById(R.id.quizflags);
    	numFlagsText.setText("Flags: " + numFlagged);
    	
    	int thisWordEditStatus = cursor.getInt(6);
    	Log.d("editThis:", "thisWordEditStuatus for this word = " + thisWordEditStatus);
    	if (thisWordEditStatus == 1) {
    		ImageView editFlag = (ImageView) findViewById(R.id.editFlag);
    		editFlag.setImageResource(R.drawable.greenflag);
    		editThis = true;
    	} else {
    		ImageView editFlag = (ImageView) findViewById(R.id.editFlag);
    		editFlag.setImageResource(R.drawable.greenflaggray);
    		editThis = false;
    	}
    	
    	return true;
    }
    
    public void setAsFlagged(View V) {
    	if (cursor.getInt(5) > 0) { //i.e., it's currently flagged and is to be de-flagged
    		lowerFlag();
    		
    		//write "0" for FLG:
    		ContentValues values = new ContentValues();
    		String[] args = new String[1];
    		args[0] = "" + cursor.getInt(0);
			values.clear();
			values.put(Constants.LEX_FLG, 0);
			db.update(Constants.LEX_TABLE, values, "_id = ?", args);
			
			--numFlagged;
			
			TextView numFlagsText = (TextView) findViewById(R.id.quizflags);
	    	numFlagsText.setText("Flags: " + numFlagged);
	    	
	    	if (onFlags) {     //i.e., you're now on the flagged items.
	    		queryByFlags();   //only grab the ones with flags.
	    		--currentPos;
	    		cursor.moveToPosition(currentPos);
	    		turnLightsOff();
	    		advanceCursor();
	    		storeSavedPreferences();
	    		setWord();
	    	}
	    	else {
	    		queryBySeq();     //grab all of them, regardless of flag status.
	    		cursor.moveToPosition(currentPos);
	    	}
	    	
    	}
    	else {   //i.e., it's currently not flagged but needs to be
    		
    		if ((nextFlag > 0) && !onFlags) { //if we're even doing flags to begin with, nextFlag will be at least 1...
    			
	    		
	    		raiseFlag();
	    		
	    		//write nextFlag for FLG:
	    		ContentValues values = new ContentValues();
	    		String[] args = new String[1];
	    		args[0] = "" + cursor.getInt(0);
				values.clear();
				values.put(Constants.LEX_FLG, nextFlag);
				db.update(Constants.LEX_TABLE, values, "_id = ?", args);
				
				++nextFlag;
				++numFlagged;
				
				queryBySeq();
				cursor.moveToPosition(currentPos);
				
				TextView numFlagsText = (TextView) findViewById(R.id.quizflags);
		    	numFlagsText.setText("Flags: " + numFlagged);
    		}
    	}
    	
    	
    	
    	storeSavedPreferences();
    }
    
    private void raiseFlag() {
    	ImageView leftFlag = (ImageView) findViewById(R.id.quizleftflag);
    	ImageView rightFlag = (ImageView) findViewById(R.id.quizrightflag);
    	leftFlag.setImageResource(R.drawable.flagcolor);
    	rightFlag.setImageResource(R.drawable.flagcolor);
    	}
    
    private void lowerFlag() {
    	ImageView leftFlag = (ImageView) findViewById(R.id.quizleftflag);
    	ImageView rightFlag = (ImageView) findViewById(R.id.quizrightflag);
    	
    	if (isDay) {
	    	leftFlag.setImageResource(R.drawable.flaggray);
	    	rightFlag.setImageResource(R.drawable.flaggray);}
    	else {
    		leftFlag.setImageResource(R.drawable.flagdarkgray);
	    	rightFlag.setImageResource(R.drawable.flagdarkgray);
    	}
    }
    
    
    private void turnLightsOff() {
		TextView answerText = (TextView) findViewById(R.id.quizanswer);
		answerText.setVisibility(View.INVISIBLE);
	}

	private void turnLightsOn() {
		TextView answerText = (TextView) findViewById(R.id.quizanswer);
		answerText.setVisibility(View.VISIBLE);
	}

	public void msgbox(String string) {
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage for you, sir: \n");
    	alertDialog.setMessage(string);
    	 
    	//alertDialog.show();
    } 
	
    private Boolean getSavedPreferences() {
    	
    	Boolean returnTest = true;
    	
    	SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
    	
        showGreek = settings.getBoolean("SHOWGREEK", true);
        nextFlag = settings.getInt("NEXTFLAG", 9999);
        autoRepeat = settings.getBoolean("AUTOREPEAT", true);
        currentPos = settings.getInt("CURRENTPOS", 9999);
        firstClick = settings.getBoolean("FIRSTCLICK", false);
        onFlags = settings.getBoolean("ONFLAGS", false);
        isDay = settings.getBoolean("ISDAY", true);
        numFlagged = settings.getInt("NUMFLAGGED", 0);
    	
        /*    
        msgbox( "showGreek: " + showGreek.toString() + "\n" +
        		"nextFlag: " + nextFlag + "\n" +
        		"autoRepeat: " + autoRepeat.toString() + "\n" +
        		"currentPos: " + currentPos + "\n" +
        		"firstClick: " + firstClick.toString() + "\n" +
        		"onFlags: " + onFlags.toString()  );  
        */ 
        
        if (currentPos == 9999) {
        	returnTest = false;
        }
        
    	return returnTest;
    	
    }
    private void storeSavedPreferences() {
    	SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putBoolean("SHOWGREEK", showGreek);
    	editor.putInt("NEXTFLAG", nextFlag);
    	editor.putBoolean("AUTOREPEAT", autoRepeat);
    	editor.putInt("CURRENTPOS", currentPos);
    	editor.putBoolean("FIRSTCLICK", firstClick);
    	editor.putBoolean("ONFLAGS", onFlags);
    	editor.putBoolean("ISDAY", isDay);
    	editor.putInt("NUMFLAGGED", numFlagged);
    	editor.commit();
    	
    }
    public void clicknext(View V) {
    	if (!firstClick) {
    		turnLightsOn();
    		firstClick = true;
    	}
    	else {
    		turnLightsOff();
    		advanceCursor();
    		storeSavedPreferences();
    		setWord();
    	}
    }
    public void clickback(View V) {
    	if (cursor.getCount() != 0) {
    	turnLightsOff();
    	backCursor();
    	storeSavedPreferences();
    	setWord();}
    }
    public void backCursor() {
    	if (!cursor.isFirst()) {
    		cursor.moveToPrevious();
    		currentPos = cursor.getPosition();
    	}
    }
    public void advanceCursor() {
    	
    	int advanceCase = 8;
    	
    	if (cursor.getCount() == 0) { 
    		
    		advanceCase = autoRepeat ? 0 : 1 ;
    	}
    	
    	else {
    		
    		if (cursor.isLast() && (nextFlag == 0)) {
    			advanceCase = autoRepeat ? 2 : 3 ;
    		}
    		
    		if (cursor.isLast() && (nextFlag > 0) && (numFlagged == 0) && !onFlags) {
    			advanceCase = autoRepeat ? 4 : 5 ;
    		}
    		
    		if (cursor.isLast() && (nextFlag > 0) && (numFlagged >  0)) {
    			advanceCase = onFlags ? 7 : 6 ;
    		}
    	}
    	
    	switch (advanceCase) {
    		case 0:		  
    			msgbox("All flagged items have been cleared.  Restarting quiz.");
    			resetQuiz();
    			break;
    		case 1:
    			msgbox("All flagged items have been cleared.  Returning to main menu.");
    			resetQuiz();
    			terminateQuiz();
    			break;
    		case 2:
    			msgbox("Quiz complete. Restarting quiz.");
    			resetQuiz();
    			break;
    		case 3:
    			msgbox("Quiz has been completed.  Returning to main menu.");
    			resetQuiz();
    			terminateQuiz();
    			break;
    		case 4:
    			msgbox("Quiz has been completed.  No items have been flagged for review.  Restarting quiz.");
    			resetQuiz();
    			break;
    		case 5:
    			msgbox("Quiz has been completed.  No items have been flagged for review.  Returning to main menu.");
    			resetQuiz();
    			terminateQuiz();
    			break;
    		case 6:
    			msgbox("First pass complete.  Commencing review of flagged items.");
    			onFlags = true;
	    		queryByFlags();
	    		currentPos = 0;
    			cursor.moveToFirst();
	    		TextView currentPosText = (TextView) findViewById(R.id.quizprogress);
	    		currentPosText.setTextColor(Color.RED);
	    		break;
    		case 7:
    			msgbox("Review of flagged items complete, but some items remain flagged.  Recommencing review of flagged items.");
    			queryByFlags();
    			currentPos = 0;
    			cursor.moveToFirst();
    			break;
    		case 8:
    			cursor.moveToNext();
        		currentPos = cursor.getPosition();
    	}
    }
     
    private void resetQuiz() {
    	clearAllFlags();
    	queryBySeq();
		cursor.moveToFirst();
		currentPos = 0;
		onFlags = false;
		numFlagged = 0;
		firstClick = false;
		TextView currentPosText = (TextView) findViewById(R.id.quizprogress);
    	currentPosText.setTextColor(Color.DKGRAY);
    }
    private void clearAllFlags() {
    	ContentValues values = new ContentValues();
		values.clear();
		values.put(Constants.LEX_FLG, 0);
		db.update(Constants.LEX_TABLE, values, null, null);
    }
    
    public void clickstop(View V) {
    	if (cursor.getCount() == 0) {
    		resetQuiz();
    	}
    	db.close();
    	lexi.close();
    	storeSavedPreferences();
    	finish();
    }
    private void terminateQuiz() {
    	clearAllFlags();
    	queryBySeq();
		cursor.moveToFirst();
		currentPos = 0;
		onFlags = false;
		numFlagged = 0;
		db.close();
    	lexi.close();
    	storeSavedPreferences();
    	finish();
    }
	private void queryByFlags() {
		cursor = db.query(Constants.LEX_TABLE, FROM_PLAY, Constants.LEX_FLG + " > 0", null, null, null, Constants.LEX_SEQ);
	}
	private void queryBySeq() {
		cursor = db.query(Constants.LEX_TABLE, FROM_PLAY, Constants.LEX_SEQ + " > 0", null, null, null, Constants.LEX_SEQ);
	}
	public void testbyflag(View v) {
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
			currentPos = 0;
			turnLightsOff();
			setWord();
			
		}
	}
	public void onClickLightDark(View v) {
		
		isDay = !isDay;
		setLightDark(true);
		
		
	}
	private void setLightDark(boolean fromButton) {
		
		LinearLayout bkgrdLinearLayout = (LinearLayout) findViewById(R.id.masterlayout); 
		TextView quizHintTV = (TextView) findViewById(R.id.quizhint);
		TextView quizAnswerTV = (TextView) findViewById(R.id.quizanswer);
    	ImageView leftFlag = (ImageView) findViewById(R.id.quizleftflag);
    	ImageView rightFlag = (ImageView) findViewById(R.id.quizrightflag);
				
		int textColor;
		int backColor;
		Log.d("in setLightDark()", "isDay: " + Boolean.toString(isDay) );
		if (isDay == true) {
			textColor = Color.BLACK;
			backColor = Color.WHITE;
			if (fromButton && (cursor.getInt(5) == 0)) { //i.e., it's currently not flagged in daylight 
				leftFlag.setImageResource(R.drawable.flaggray);
		    	rightFlag.setImageResource(R.drawable.flaggray);
			}
			
			
		}
		else {
			textColor = Color.RED;
			backColor = Color.BLACK;
			if (fromButton && (cursor.getInt(5) == 0)) { //i.e., it's currently not flagged in darkness
				leftFlag.setImageResource(R.drawable.flagdarkgray);
		    	rightFlag.setImageResource(R.drawable.flagdarkgray);
				}
		}
	
		bkgrdLinearLayout.setBackgroundColor(backColor);
		quizHintTV.setTextColor(textColor);
		quizAnswerTV.setTextColor(textColor);
		
	
		
	}
		
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {        
		case 88:     				//Left on clicker       
			clickback(null);            
			return true;        
		case 87:     				//Right on clicker                   
			clicknext(null);            
			return true;        
		case 85:     				//Center on clicker                   
			clicknext(null);            
			return true;        
		case 24:     				//Up on clicker                   
			setEditFlag((ImageView) findViewById(R.id.editFlag));            
			return true;     
		case 25:     				//Down on clicker                   
			setAsFlagged(null);            
			return true;   
		case 129:     				//KEYBD on clicker                   
			onClickLightDark(null);          
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
