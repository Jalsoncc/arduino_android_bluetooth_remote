package com.arduino.androidtv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartPage extends Activity {
	
	private Intent i;
	private String name;
	private EditText et;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_page);
		
		if( getIntent().getBooleanExtra("Exit me", false)){
	        finish();
	        return; // add this to prevent from doing unnecessary stuffs
	    }
		
		Button send = (Button) findViewById(R.id.btnSend);
		et = (EditText) findViewById(R.id.etBTname);
		
		
		
		i = new Intent(this,MainActivity.class);
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				name = et.getText().toString();
				if(name == null || name.length() == 0){
					Toast.makeText(getBaseContext(), "Enter bluetooth shield name",
							Toast.LENGTH_SHORT).show();
				}else{
					i.putExtra("device_name", name);
					startActivity(i);
				}
				
				
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.exit:
			android.os.Process.killProcess(android.os.Process.myPid());
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
		
		
	}
}
