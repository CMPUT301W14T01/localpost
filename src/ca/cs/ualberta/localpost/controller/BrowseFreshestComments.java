package ca.cs.ualberta.localpost.controller;

import java.util.ArrayList;
import ca.cs.ualberta.localpost.model.RootCommentModel;

public class BrowseFreshestComments implements BrowseTopLevelComments {
	ArrayList<RootCommentModel> rootComments;

	@Override
	public void getRootComments() {
		// TODO Auto-generated method stub
		//this.rootComments = RootCommentModelList.getList();
	}

	@Override
	public ArrayList<RootCommentModel> sortRootComments(ArrayList<RootCommentModel> comments) {
		// TODO SortByFreshness
		return comments;
	}

	@Override
	public ArrayList<RootCommentModel> passSortedRootComments() {
		getRootComments();
		sortRootComments(rootComments);
		return rootComments;
	}

}