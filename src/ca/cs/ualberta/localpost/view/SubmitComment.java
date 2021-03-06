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
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import ca.cs.ualberta.localpost.controller.ConnectivityCheck;
import ca.cs.ualberta.localpost.controller.ElasticSearchOperations;
import ca.cs.ualberta.localpost.controller.Serialize;
import ca.cs.ualberta.localpost.model.ChildCommentModel;
import ca.cs.ualberta.localpost.model.CommentModel;
import ca.cs.ualberta.localpost.model.RootCommentModel;
import ca.cs.ualberta.localpost.model.StandardUserModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

/**
 * This activity allows the user to submit a new comment
 * or a new reply
 * 
 * @author team01
 * 
 */
public class SubmitComment extends Activity {

	/** Creates a new StandardUserModel object */
	private StandardUserModel user;

	/** Button used to submit the comment */
	private Button postButton;
	/** Constant pic request code */
	public static final int OBTAIN_PIC_REQUEST_CODE = 117;
	public static final int OBTAIN_ADDRESS_REQUEST_CODE = 101;
	private String SUBMIT_VIEW = "submitvew";
	private String MAP_VIEW_TYPE = "mapviewtype";

	/** Carries the currently save picture waiting for submission */
	private Bitmap currentPicture = null;

	/** Variable for the onClickListener that generates the map view **/
	LatLng latlng;
	private Address address;

	/** Gson writer */
	private Gson gson = new Gson();

	/** Gets the ID of replies parent */
	private String parentID = null;

	/** Checks whether its a submit comment or reply */
	private String commentType = null;

	/** Gets root comment model id */
	private String topLevelID = null;

	/** Views */
	private EditText titleView;
	private EditText contentView;
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_comment);

		/** SetText for the button */
		postButton = (Button) findViewById(R.id.postButton);
		postButton.setText("Submit Comment");

		/** Check Bundles */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey("TopLevelID")) {
				topLevelID = extras.getString("TopLevelID");
			}
			if (extras.containsKey("parentID")) {
				parentID = extras.getString("parentID");
			}
			if (extras.containsKey("commentType")) {
				commentType = extras.getString("commentType");
				postButton.setText("Submit Reply");
			}
		} else {
			commentType = "submit";
		}

		contentView = (EditText) findViewById(R.id.textBody);
		titleView = (EditText) findViewById(R.id.commentTitle);

		// Hides the Title Edit Field if reply
		if (commentType.equals("reply")) {
			titleView.setVisibility(View.GONE);
		}

		/** Set the listener on the Map image **/
		image = (ImageView) findViewById(R.id.mapView);
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intentLocation = new Intent(getApplicationContext(),
						MapsView.class);
				intentLocation.putExtra(MAP_VIEW_TYPE, SUBMIT_VIEW);
				startActivityForResult(intentLocation, OBTAIN_ADDRESS_REQUEST_CODE);
			}
		});
	}

	/**
	 * Adds a new root. Puts all the input data into a RootCommentModel and
	 * writes to a json file.
	 * 
	 * @param view
	 *            Takes in view from SubmitComment
	 * @throws InvalidKeyException
	 *             Checks for invalid keys
	 * @throws NoSuchAlgorithmException
	 *             Checks if algorithm used is valid
	 * @throws UnsupportedEncodingException
	 *             Checks if there is an encoding problem
	 */
	public void add_root(View view) throws InvalidKeyException,
	NoSuchAlgorithmException, UnsupportedEncodingException {
		user = Serialize.loaduser(getApplicationContext());

		String title;
		String content;

		ConnectivityCheck conn = new ConnectivityCheck(this);
		if (conn.isConnectingToInternet()) {
			if (commentType.equals("submit")) {
				title = titleView.getText().toString();
				content = contentView.getText().toString();

				RootCommentModel new_root = new RootCommentModel(content, title,
						currentPicture, this);
				new_root.setAuthor(user.getUsername());
				new_root.setAddress(user.getAddress());
				if (address != null)
					new_root.setAddress(address);
				Serialize.SaveComment(new_root, this, "history");
				ElasticSearchOperations es = new ElasticSearchOperations();
				es.execute(1, new_root.getPostId(), new_root, null);

			} else if (commentType.equals("reply")) {
				try {
					//Create new child
					content = contentView.getText().toString();
					ChildCommentModel new_child = new ChildCommentModel(content,null, currentPicture, this);
					new_child.setAddress(user.getAddress());
					if (address != null)
						new_child.setAddress(address);
					new_child.setAuthor(user.getUsername());

					//Find Parent and add to its array
					ElasticSearchOperations es1 = new ElasticSearchOperations();
					ArrayList<CommentModel> array;
					if(topLevelID.equals(parentID)){
						array = es1.execute(3, null, null,parentID).get();
					}
					else
						array = es1.execute(2, null, null,parentID).get();
					CommentModel temp = array.get(0);
					temp.addChild(new_child.getPostId().toString());
					Serialize.SaveComment(temp, this, "history");
					Serialize.update(temp, this, "favoritecomment.json");
					Serialize.update(temp, this, "historycomment.json");


					//Push to ES
					ElasticSearchOperations es2 = new ElasticSearchOperations();
					es2.execute(1, temp.getPostId(), temp, null);

					ElasticSearchOperations es3 = new ElasticSearchOperations();

					es3.execute(1, new_child.getPostId(), new_child, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.onBackPressed();
		} else {
			Toast.makeText(this, "You need to be connected!",Toast.LENGTH_SHORT).show();
		}
	}

	public void obtain_picture(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, OBTAIN_PIC_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO put the resizing code elsewhere + do we want to expand a
		if (requestCode == OBTAIN_PIC_REQUEST_CODE && resultCode == RESULT_OK) {
			this.currentPicture = (Bitmap) data.getExtras().get("data");
			if (currentPicture.getWidth() > 50
					|| currentPicture.getHeight() > 50) {
				double scalingFactor = currentPicture.getWidth() * 1.0 / 50;
				if (currentPicture.getHeight() > currentPicture.getWidth())
					scalingFactor = currentPicture.getHeight() * 1.0 / 50;
				int newWidth = (int) Math.round(currentPicture.getWidth()
						/ scalingFactor);
				int newHeight = (int) Math.round(currentPicture.getHeight()
						/ scalingFactor);

				currentPicture = Bitmap.createScaledBitmap(currentPicture,
						newWidth, newHeight, false);
			}
		}
		if (requestCode == OBTAIN_ADDRESS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String intentIndex = data.getStringExtra("address");
				address = gson.fromJson(intentIndex,
						android.location.Address.class);
			} else
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.submit_comment, menu);
		return true;
	}
}
