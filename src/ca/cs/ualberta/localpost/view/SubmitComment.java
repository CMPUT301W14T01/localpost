/**
 * The MIT License (MIT)
 * Copyright (c) 2014 Timotei Albu, David Chau-Tran, Alain Clark, Shawn Anderson, Mickael Zerihoun
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 *	this software and associated documentation files (the "Software"), to deal in
 *	the Software without restriction, including without limitation the rights to
 *	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *	the Software, and to permit persons to whom the Software is furnished to do so,
 *	subject to the following conditions:
 *	
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *	
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *  
 */

package ca.cs.ualberta.localpost.view;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import ca.cs.ualberta.localpost.controller.Serialize;
import ca.cs.ualberta.localpost.model.RootCommentModel;
import ca.cs.ualberta.localpost.model.StandardUserModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

/**
 * This activity allows the user to enter and submit a new comment
 * @author Team 01
 *
 */
public class SubmitComment extends Activity {
	/**Grabs the username from an intent */
	private String intentUsername;
	
	/**Creates a new StandardUserModel object */
	private StandardUserModel user;
	
	/**Button used to submit the comment */
	private Button postButton;
	
	/** Variable for the onClickListener that generates the map view **/
	ImageView image;

	private Address address;
	
	/**Gson writer */
	private Gson gson = new Gson();
	
	LatLng latlng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_comment);

		/**Grabs uername from MainActivity via intent */
		Bundle extras = getIntent().getExtras();
		intentUsername = extras.getString("username");
		
		/**SetText for the button */
		postButton = (Button) findViewById(R.id.postButton);
		postButton.setText("Submit Comment");
		
		/**Set the listener on the Map image **/
		image = (ImageView) findViewById(R.id.mapView);

		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MapsView.class);
				startActivityForResult(intent, 1);
			}

		});
	}
	
	@Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
		if (requestCode == 1){
			if (resultCode == RESULT_OK){
				String intentIndex = data.getStringExtra("address");
				address = gson.fromJson(intentIndex, android.location.Address.class);
			}
			else
				super.onActivityResult(requestCode, resultCode, data);
		}
    }

	/**Adds a new root. Puts all the input data into a RootCommentModel
	 * and writes to a json file.
	 * @param view Takes in view from SubmitComment
	 * @throws InvalidKeyException  Checks for invalid keys
	 * @throws NoSuchAlgorithmException Checks if algorithm used is valid
	 * @throws UnsupportedEncodingException Checks if there is an encoding problem
	 */
	public void add_root(View view) throws InvalidKeyException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		user = new StandardUserModel();
		user.setUsername(intentUsername);

		EditText titleView = (EditText) findViewById(R.id.commentTitle);
		String title = titleView.getText().toString();

		EditText contentView = (EditText) findViewById(R.id.textBody);
		String content = contentView.getText().toString();

		RootCommentModel new_root = new RootCommentModel(content, title);
		new_root.setAuthor(user.getUsername());	
		
		new_root.setAddress(address);
		
		//Log.e("LatLng", String.valueOf(new_root.getLatlng()));

		Serialize.SaveInFile(new_root, SubmitComment.this);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.submit_comment, menu);
		return true;
	}
}
