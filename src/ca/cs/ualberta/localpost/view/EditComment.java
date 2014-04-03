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
import ca.cs.ualberta.localpost.controller.ElasticSearchOperations;
import ca.cs.ualberta.localpost.controller.Serialize;
import ca.cs.ualberta.localpost.model.RootCommentModel;
import ca.cs.ualberta.localpost.model.StandardUserModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

/**This Activity allows users to edit a previously posted comments
 * @author Team 01
 *
 */
public class EditComment extends Activity {
	/**Button and TextViews used by the layout */
	private Button editButton;
	private EditText titleView;
	private EditText contentView;
	
	/**Object and index obtained via intent */
	private RootCommentModel intentObj;
	
	/**Gson writer */
	private Gson gson = new Gson();
	
	/** Variable for the onClickListener that generates the map view **/
	ImageView image;
	
	private Address address;
	LatLng latlng;
	private StandardUserModel user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_comment);
		
		//Sets the button text to "Edit Comment"
		editButton = (Button) findViewById(R.id.postButton);
		editButton.setText("Edit Comment");
		
		//Grab data from the intent
		Bundle extras = getIntent().getExtras();
		String temp = extras.getString("ModelObj");
//		intentIndex = extras.getString("Index");
		
		intentObj = gson.fromJson(temp, RootCommentModel.class);

		//Set Previous values back into fields
		titleView = (EditText) findViewById(R.id.commentTitle);
		titleView.setText(intentObj.getTitle());
		
		contentView = (EditText) findViewById(R.id.textBody);
		contentView.setText(intentObj.getContent());
		
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
	
	/**Edits a previous root comment. Puts all the input data into a RootCommentModel
	 * and writes to a .json file.
	 * @param view Takes in view from SubmitComment
	 * @throws InvalidKeyException  Checks for invalid keys
	 * @throws NoSuchAlgorithmException Checks if algorithm used is valid
	 * @throws UnsupportedEncodingException Checks if there is an encoding problem
	 */
	public void add_root(View view){
		try {
			user = StandardUserModel.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String title = titleView.getText().toString();
		String content = contentView.getText().toString();
		
		intentObj.setTitle(title);
		intentObj.setContent(content);
		intentObj.setAuthor(user.getUsername());
		intentObj.setAddress(user.getAddress());
//		Log.e("SelectedAddress", String.valueOf(address));
//		Log.e("DefaultAddress", String.valueOf(user.getAddress()));
		if (address != null)
			intentObj.setAddress(address);
		
		//Send to Server.
		ElasticSearchOperations es = new ElasticSearchOperations();
		es.execute(1,intentObj.getPostId(),intentObj, null);
		
		Serialize.update(intentObj, this, "historycomment.json");
		Serialize.SaveComment(intentObj, EditComment.this, "history");
		
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_comment, menu);
		return true;
	}
}
