package ca.cs.ualberta.localpost.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import android.content.Context;
import android.util.Log;
import ca.cs.ualberta.localpost.model.ChildCommentModel;
import ca.cs.ualberta.localpost.model.CommentModel;
import ca.cs.ualberta.localpost.model.RootCommentModel;

import com.google.gson.Gson;

public class Serialize {
	private static Gson gson = new Gson();

	public static void SaveInFile(CommentModel new_root, Context context) {
		String filename = null;
		if(new_root instanceof RootCommentModel){
			filename = "rootcomment.json";
		}
		else if(new_root instanceof ChildCommentModel){
			filename = "childcomment.json";
		}
		String modelJson = gson.toJson(new_root);
		try {
			OutputStream outputStream = context.getApplicationContext()
					.openFileOutput(filename, Context.MODE_APPEND);
			OutputStreamWriter fileWriter = new OutputStreamWriter(outputStream);
			fileWriter.write(modelJson + "\r\n");
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void loadFromFile(String filename, Context context) {
		FileInputStream FileOpen;
		try {
			FileOpen = context.getApplicationContext().openFileInput(filename);
			InputStreamReader FileReader = new InputStreamReader(FileOpen);
			BufferedReader buffer = new BufferedReader(FileReader);

			String input;
			while ((input = buffer.readLine()) != null) {
				if(filename.equals("rootcomment.json")){
					Log.e("Root", input);
					RootCommentModel obj = gson.fromJson(input,RootCommentModel.class);
					Log.e("ObjRoot", obj.getTitle());
				}
				else if(filename.equals("childcomment.json")){
					Log.e("Child", input);
					ChildCommentModel obj = gson.fromJson(input,ChildCommentModel.class);
					Log.e("ObjChild", obj.getTitle());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// End loadFromFile
}
