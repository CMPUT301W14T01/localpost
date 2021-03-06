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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import ca.cs.ualberta.localpost.controller.ConnectivityCheck;
import ca.cs.ualberta.localpost.controller.ElasticSearchOperations;
import ca.cs.ualberta.localpost.controller.Serialize;
import ca.cs.ualberta.localpost.model.ChildCommentModel;
import ca.cs.ualberta.localpost.model.CommentModel;
import ca.cs.ualberta.localpost.model.RootCommentModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Displays a comment and all the replies associated with the comment
 * 
 * @author team01
 */
public class ThreadView extends Activity {
	private TableLayout table;
	private final int marginBase = 10;
	private final int depthTolerance = 8;
	private RootCommentModel topLevel;
	private String parentID = null;
	ArrayList<String> mapThreadView; 
	private String THREAD_VIEW = "threadview";
	private String MAP_VIEW_TYPE = "mapviewtype";
	private String THREAD_COMMENT_MODEL = "threadcommentmodel";

	Gson gson = new Gson();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);

		// Creates a Table
		table = (TableLayout) findViewById(R.id.table_layout);

		final Bundle extras = getIntent().getExtras();
		String temp = extras.getString("CommentModel");
		topLevel = gson.fromJson(temp, RootCommentModel.class);


		mapThreadView = new ArrayList<String>(); 

		String passToJson = gson.toJson(topLevel);
		mapThreadView.add(passToJson);

		threadExpand(topLevel, 0);
	}
	/**
	 * Overrides onResume. Clears TableView and
	 * repopulates table.
	 */
	@Override
	public void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				table.removeAllViews();
				table.invalidate();
				threadExpand(topLevel, 0);
			}}, 250);
	}

	/**
	 * Recursively iterates through nested arraylist of comments
	 * @param comment TopLevel Comment
	 * @param level level of identation
	 */
	public void threadExpand(CommentModel comment, int level) {
		String passToJson;

		draw(comment, level);
		++level;
		ArrayList<String> commentChildren = comment.getChildren(); // List of
		// UUIDS

		if (level < depthTolerance && !commentChildren.isEmpty()) {
			ConnectivityCheck conn = new ConnectivityCheck(this);
			if (conn.isConnectingToInternet()) {
				for (String c : commentChildren) {
					ElasticSearchOperations es = new ElasticSearchOperations();
					try {
						ArrayList<CommentModel> model;
						model= es.execute(2, null,null, c).get();

						Serialize.check_if_exist(topLevel.getPostId().toString(), this);
						Serialize.SaveComment(model.get(0), this, topLevel.getPostId().toString());
						threadExpand(model.get(0), level);

						passToJson = gson.toJson(model.get(0));
						mapThreadView.add(passToJson);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				HashMap<String, ChildCommentModel> childlist = Serialize.loadchildFromFile(topLevel.getPostId().toString(),this);
				for (String c : commentChildren) {
					threadExpand(childlist.get(c), level);
				}
			}
		}
	}

	/**
	 * Creates a table row and and appends it to TableView
	 * @param comment Comment that is being drawn
	 * @param level	level of indentation
	 */
	public void draw(final CommentModel comment, int level) {
		TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.row, null);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

		// Pad root comments vertically and child comments horizontally
		if (level < depthTolerance) {
			params.setMargins(level * marginBase, 5, 0, 0);
		}

		row.setLayoutParams(params);

		// Register onclick
		registerForContextMenu(row);

		// Set Tag for use in threadview context menu
		row.setTag(comment.getPostId().toString());

		TextView author = (TextView) row.findViewById(R.id.rowAuthor);
		TextView content = (TextView) row.findViewById(R.id.rowContent);
		TextView location = (TextView) row.findViewById(R.id.rowLocation);
		TextView timestamp = (TextView) row.findViewById(R.id.rowDate);
		ImageView picture = (ImageView) row.findViewById(R.id.rowPicture);

		// Set Sizes
		author.setTextSize(4 * getApplicationContext().getResources()
				.getDisplayMetrics().density);
		content.setTextSize(5 * getApplicationContext().getResources()
				.getDisplayMetrics().density);
		timestamp.setTextSize(4 * getApplicationContext().getResources()
				.getDisplayMetrics().density);
		location.setTextSize(4 * getApplicationContext().getResources()
				.getDisplayMetrics().density);


		// Set text and images
		SimpleDateFormat format = new SimpleDateFormat("c HH:mm MMM/dd/yyyy");

		author.setText(comment.getAuthor() + " - ");
		content.setText(comment.getContent());

		if (comment.getAddress() == null) {
			location.setText(" - @ No location - ");
		} else {
			if (comment.getAddress().getAddressLine(0).length() > 27) {
				String temp = comment.getAddress().getAddressLine(0)
						.substring(0, 27)
						+ "...";
				location.setText(" - @ " + temp + " - ");
			} else {
				location.setText(" - @ "
						+ comment.getAddress().getAddressLine(0) + " - ");
			}
		}

		timestamp.setText(format.format(new Date(comment.getTimestamp())));
		picture.setImageBitmap(comment.getPicture());

		table.addView(row);
		row = null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		parentID = v.getTag().toString();
		menu.add(0, Menu.FIRST, 0, "Reply");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case Menu.FIRST:
			Intent newIntent = new Intent(getApplicationContext(),SubmitComment.class);
			newIntent.putExtra("parentID", parentID);
			newIntent.putExtra("TopLevelID",topLevel.getPostId().toString());
			newIntent.putExtra("commentType", "reply");
			startActivity(newIntent);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.thread_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ConnectivityCheck conn = new ConnectivityCheck(this);
		ElasticSearchOperations es = new ElasticSearchOperations();
		switch (item.getItemId()) {

		case R.id.readLater:
			Toast.makeText(getApplicationContext(), "Added to Read Later", Toast.LENGTH_SHORT).show();
			if(conn.isConnectingToInternet()){
				es.execute(1, topLevel.getPostId(), topLevel,null);
				Serialize.SaveComment(topLevel, this, "readlater");
				Serialize.update(topLevel, this, "readlater.json");
				return true;	
			}
			else{
				Toast.makeText(this, "You require connectivity to cache a thread for later read",Toast.LENGTH_SHORT).show();
				return true;
			}
		case R.id.plotThread:
			if(conn.isConnectingToInternet()){
				Intent intentMapThread = new Intent(getApplicationContext(), MapsView.class);
				intentMapThread.putExtra(MAP_VIEW_TYPE, THREAD_VIEW);
				String passArrayComment = gson.toJson(mapThreadView, new TypeToken<ArrayList<String>>(){}.getType());
				intentMapThread.putExtra(THREAD_COMMENT_MODEL, passArrayComment);
				startActivity(intentMapThread);
				return true;	
			}
			else{
				Toast.makeText(getApplicationContext(), "Yo you require connectivity to view a map thread",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
