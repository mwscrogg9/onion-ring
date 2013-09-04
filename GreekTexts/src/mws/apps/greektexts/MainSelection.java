package mws.apps.greektexts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import mws.apps.greektexts.R;

public class MainSelection extends Activity {

	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_selection);
	
		
		
	}
	
	public void startAddNewWords(View v) {
		
		Intent i = new Intent(this,AddNewWords.class);
		startActivity(i);
			
	}
	public void startBuildNewQuiz(View v) {
		
		Intent i = new Intent(this,BuildNewQuiz.class);
		startActivity(i);
			
	}
	public void resumeQuiz(View v) {
		
		Intent i = new Intent(this,QuizScreen.class);
		startActivity(i);
			
	}
	public void exportDB(View v) {
		
	       lexiconDB lexi = new lexiconDB(this); //"/data/mws.greek.quiz/databases/lexicondata.db";
	       msgbox(lexi.exportDB(this));
	       lexi.close();
	    
	}
	
	public void importDB(View v) {
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to replace the existing lexicon?")
	           .setTitle("Import Database...");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				      doTheImport();	
				}
		});
		
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
		
		
		
		
		
		
	
		
		
	}
	public void doTheImport() {
		//A file called NEWlexicondata.db needs to be in the SDCARD directory.
		lexiconDB lexi = new lexiconDB(this); //"/data/mws.greek.quiz/databases/lexicondata.db";
	       
		 msgbox(lexi.importDatabase(this));
	       lexi.close();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_selection, menu);
		return true;
	}
	public void msgbox(String string) {
		AlertDialog alertDialog;
    	alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle("Mesage: \n");
    	alertDialog.setMessage(string);
    	alertDialog.show();
    } 
}
