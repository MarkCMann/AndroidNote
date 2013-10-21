package com.mmann.notetaking;

import java.io.DataInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.util.Log;

public class Note {
	
	private SpannableString content;
	private String filename;
	private Context context;
	private NoteDelegate delegate;
	
	private static final String NOTE_ERROR_STRING = "NOTE";
	
	public Note(Context context, NoteDelegate delegate) {
		this(context, delegate, null);
	}
	
	public Note(Context context, NoteDelegate delegate, String filename) {
		this.context = context;
		this.delegate = delegate;
		this.filename = filename;
		this.content = null;
	}
	
	/** Saves the note and then calls the delegate's finishedSavingNote(Note) method, if
	 * the delegate exists.
	 */
	public void save() {
		new NoteSaveAsyncTask().execute();
	}
	
	/**Loads the note and then calls the delegate's finishedLoadingNote(Note) method, if
	 * the delegate exists.
	 */
	public void load() {
    	new NoteLoadAsyncTask().execute();
    }
	
	/**Deletes the note whose filename matches the filename stored in this note.
	 * 
	 */
	public void delete() {
		new NoteDeleteAsyncTask().execute();
	}
	
	public SpannableString getContent() {
		// We need to return a copy of the spannable string.
		return SpannableString.valueOf(this.content);
	}
	
	public void setContent(SpannableString content) {
		// We set our values to a copy of the string. We use a copy so that
		// we don't have to worry about mutating the string on a background thread when we save.
		this.content = SpannableString.valueOf(content);
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public String toString() {
		return LinkParser.sharedParser().rawTextForSequence(this.getContent()).toString();
	}

	class NoteSaveAsyncTask extends AsyncTask<NoteDelegate, Integer, Integer> {
		
		@Override
		// We never use the parameter.
		protected Integer doInBackground(NoteDelegate... callBack) {
			
			if(Note.this.filename == null) {
				Log.e(NOTE_ERROR_STRING, "Filename cannot be null");
				return null;
			}

			boolean saved = false;

			try {
				FileOutputStream stream = Note.this.context.openFileOutput(Note.this.filename, Context.MODE_PRIVATE);
				String rawString = LinkParser.sharedParser().rawTextForSequence(Note.this.getContent()).toString();
				stream.write(rawString.getBytes());
				stream.flush();
				stream.close();
				stream = null;
				saved = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(!saved) {
				// Alert the user that the note failed to save.
				Log.e(NOTE_ERROR_STRING, String.format("Could not save file %s.", Note.this.filename));
				return 0;
			}
			
			return 1;
		}
		
		protected void onPostExecute(Integer result) {
			if (result == 1 && Note.this.delegate != null) {
				Note.this.delegate.finishedSavingNote(Note.this);
			}
		}
	}
	
	class NoteLoadAsyncTask extends AsyncTask<NoteDelegate, Integer, Integer> {
		
		@Override
		// No need to use the parameter since our enveloping class already has
		// access to its delegate.
		protected Integer doInBackground(NoteDelegate... callBack) {
			
			if (Note.this.filename == null) {
	    		Log.e(NOTE_ERROR_STRING, "Cannot load a null file.");
	    		return null;
	    	}
	    	if (context == null) {
	    		Log.e(NOTE_ERROR_STRING, "Cannot have a null context when loading a note.");
	    		return null;
	    	}
	    	
	    	String text = null;
	    	StringBuilder ret = new StringBuilder();
	    	
	    	try {
				DataInputStream stream = new DataInputStream(Note.this.context.openFileInput(Note.this.filename));
				while((text = stream.readLine()) != null) {
					ret.append(text);
					ret.append(Constants.LINE_SEPARATOR);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	Note.this.content = LinkParser.sharedParser().parseString(ret.toString(), Note.this.delegate);
	    	if (Note.this.content == null || Note.this.content.length() == 0) {
	    		Log.e(NOTE_ERROR_STRING, String.format("Empty note loaded, with filename %s.", Note.this.filename));
	    	}
			
			return 1;
		}
		
		protected void onPostExecute(Integer result) {
			if (result == 1 && Note.this.delegate != null) {
				Note.this.delegate.finishedLoadingNote(Note.this);
			}
		}
	}
		
	class NoteDeleteAsyncTask extends AsyncTask<NoteDelegate, Integer, Integer> {

		@Override
		protected Integer doInBackground(NoteDelegate... callBack) {

			if (Note.this.filename == null) {
				Log.e(NOTE_ERROR_STRING, "Cannot delete a null file.");
				return null;
			}
			if (context == null) {
				Log.e(NOTE_ERROR_STRING, "Cannot have a null context when deleting a note.");
				return null;
			}

			try {
				Note.this.context.deleteFile(Note.this.filename);
			} catch(Exception e) {
				e.printStackTrace();
				return 0;
			}

			return 1;
		}

		protected void onPostExecute(Integer result) {
			if (result == 1 && Note.this.delegate != null) {
				Note.this.delegate.finishedDeletingNote(Note.this);
			}
		}
	}
}
