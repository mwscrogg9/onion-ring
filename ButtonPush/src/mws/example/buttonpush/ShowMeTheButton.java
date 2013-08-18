package mws.example.buttonpush;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ShowMeTheButton extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_me_the_button);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_me_the_button, menu);
		return true;
	}

}
