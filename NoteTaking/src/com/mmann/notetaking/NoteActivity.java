package com.mmann.notetaking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView.BufferType;

import mmann.sslserver.SocketConnection;
import mmann.sslserver.SocketConnectionListener;

public class NoteActivity extends FragmentActivity implements NoteDelegate, SocketConnectionListener {

	private Note currentNote;
	private EditText textField;
	
	private AndroidSocketConnection conn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_note);
        
        currentNote = null;
        textField = (EditText)findViewById(R.id.noteSpace);
        textField.setLines(20);
        textField.setGravity(Gravity.TOP);
        this.hideTextField();
        
//        conn = new AndroidSocketConnection("10.0.0.4", this, this);
//        conn.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_note, menu);
        return true;
    }
    
    public void createNoteClicked(View v) {
    	DialogFragment dialog = new CreateNoteDialogFragment();
    	dialog.show(getSupportFragmentManager(), "CreateNoteDialog");
    }
    
    public void loadNoteClicked(View v) {
    	DialogFragment dialog = new LoadNoteDialogFragment();
    	dialog.show(getSupportFragmentManager(), "LoadNoteDialog");
    }
    
    public void deleteNoteClicked(View v) {
    	DialogFragment dialog = new DeleteNoteDialogFragment();
    	dialog.show(getSupportFragmentManager(), "DeleteNoteDialog");
    }
    
    class CreateNoteDialogFragment extends DialogFragment {
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View alertView = inflater.inflate(R.layout.dialog_layout, null);
            builder.setView(alertView);
            builder.setPositiveButton(R.string.CreateNoteConfirmation, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // Call the create method.
                    	   createNoteNamed(((EditText)alertView.findViewById(R.id.dialogText)).getText().toString());
                       }
                   });
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // User cancelled so there is no need to do anything.
                       }
                   });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    
    class LoadNoteDialogFragment extends DialogFragment {
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final String[] files = NoteActivity.this.fileList();
            
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // User cancelled so there is no need to do anything.
                       }
                   });
            builder.setItems(files, new Dialog.OnClickListener(){
				public void onClick(DialogInterface dialog, int index) {
					NoteActivity.this.loadNoteNamed(files[index]);
				}
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    
    class DeleteNoteDialogFragment extends DialogFragment {
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final String[] files = NoteActivity.this.fileList();
            
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // User cancelled so there is no need to do anything.
                       }
                   });
            builder.setItems(files, new Dialog.OnClickListener(){
				public void onClick(DialogInterface dialog, int index) {
					NoteActivity.this.deleteNoteNamed(files[index]);
				}
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    
    public void onPause() {
    	saveTextInNote();
    	super.onPause();
    }
    
    private void setTextFieldCharSequence(CharSequence text) {
    	this.textField.setText(text, BufferType.SPANNABLE);
    	this.textField.setMovementMethod(CustomMovementMethod.getInstance());
    	this.textField.setVisibility(View.VISIBLE);
    	this.textField.setEnabled(true);
    	this.textField.setSelection(text.length());
    }
    
    private void hideTextField() {
    	this.textField.setEnabled(false);
        this.textField.setVisibility(View.INVISIBLE);
    }

	@Override
	public void finishedLoadingNote(Note note) {
		setTextFieldCharSequence(note.getContent());
	}

	@Override
	public void finishedSavingNote(Note note) {
		// Currently, we don't really have any interest in doing something when
		// a note is saved.
		Log.e(Constants.DEBUG, String.format("Saved file %s.", note.getFilename()));
	}
	
	@Override 
	public void finishedDeletingNote(Note note) {
		// We don't need to do anything once the note has been deleted.
		Log.e(Constants.DEBUG, String.format("Deleted file %s.", note.getFilename()));
	}
	
	public void saveTextInNote() {
		if (this.currentNote == null) {
			return;
		}
		
		currentNote.setContent(new SpannableString(textField.getText()));
//		conn.writeObject(currentNote.toString());
		currentNote.save();
	}

	@Override
	public void loadNoteNamed(final String filename) {
		if(currentNote != null && currentNote.getFilename().equalsIgnoreCase(filename)) {
			return;
		}
		saveTextInNote();
		
		currentNote = new Note(this, this, filename);
		currentNote.load();
	}
	
	private void createNoteNamed(final String filename) {
		// You can't create a note with the same name as a note already in existence.
		for (String file : fileList()) {
			if (file.equalsIgnoreCase(filename)) {
				return;
			}
		}
		saveTextInNote();
    	currentNote = new Note(this, this, filename);
    	setTextFieldCharSequence("");
    }
	
	public void deleteNoteNamed(final String filename) {
    	if (currentNote.getFilename().equalsIgnoreCase(filename)) {
    		currentNote.delete();
    		currentNote = null;
    		this.hideTextField();
    	} else {
    		new Note(this, this, filename).delete();
    	}
    }

	@Override
	public void socketStartedListening(SocketConnection conn) {
		Log.e(Constants.DEBUG, "Connected");
	}

	@Override
	public void socketReceivedObject(Object obj) {
		Log.e(Constants.DEBUG, obj.toString());
	}

	@Override
	public void socketStoppedListening(SocketConnection conn) {
		Log.e(Constants.DEBUG, "Disconnected");
	}
}
