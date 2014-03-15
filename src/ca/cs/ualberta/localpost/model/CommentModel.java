package ca.cs.ualberta.localpost.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;
import ca.cs.ualberta.localpost.model.UserModel;

public abstract class CommentModel {
	public String title;
	public String content;
	public android.location.Location location;
	private String author;
	private ArrayList<CommentModel> children = new ArrayList<CommentModel>();
	private long timestamp;
	private java.util.UUID postId;
	private int radish;
	private Bitmap picture;
	long date = new Date().getTime();

	public CommentModel(String content, String title) {
		this.title = title;
		this.content = content;
		this.radish = 0;
		this.setPostId(UUID.randomUUID());
		this.timestamp = date;

		// this.location = location;
		// this.picture = picture;
		// this.author = MainActivity.getModel().getUsername();
	}
	public CommentModel(){
		super();
	}
	public CommentModel(String content, android.location.Location location, Bitmap picture){
		this.content = content;
		this.radish = 0;
		this.setPostId(UUID.randomUUID());
		this.location = location;
		this.timestamp = date;
		this.picture = picture;
		this.author = UserModel.getUsername();
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public android.location.Location getLocation() {
		return location;
	}

	public void setLocation(android.location.Location location) {
		this.location = location;
	}

	public String getTimestamp() {
		return String.valueOf(timestamp);
	}

	public void setTimestamp(String timestamp) {
		long date = new Date().getTime();
		this.timestamp = date;
	}

	public int getRadish() {
		return radish;
	}

	public void incRadish(int radish) {
		this.radish += 1;
	}

	public void decRadish(int radish) {
		this.radish -= 1;
	}

	/**
	 * @return the postId
	 */
	public java.util.UUID getPostId() {
		return postId;
	}

	/**
	 * @param postId
	 *            the postId to set
	 */
	public void setPostId(java.util.UUID postId) {
		this.postId = postId;
	}
}
