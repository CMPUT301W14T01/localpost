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

package ca.cs.ualberta.localpost.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import ca.cs.ualberta.localpost.model.CommentModel;

	/**
	 * Sorts comment array by Radish 
	 * @author Team01
	 *
	 */
public class SortGreatestComments implements SortComments {
	
	@Override
	public ArrayList<CommentModel> sortComments(ArrayList<CommentModel> comments) {
		Collections.sort(comments, new Comparator<CommentModel>() {
	        @SuppressLint("NewApi")
			@Override
	        public int compare(CommentModel  comment1, CommentModel  comment2)
	        {

	            return  Integer.compare(comment2.getRadish(), comment1.getRadish());
	        }
	    });
		return comments;
	}

	
}
