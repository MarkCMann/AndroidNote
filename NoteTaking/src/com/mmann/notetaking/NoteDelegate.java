package com.mmann.notetaking;

public interface NoteDelegate {
	
	public void finishedLoadingNote(Note note);
	public void finishedSavingNote(Note note);
	public void finishedDeletingNote(Note note);
	
	public void loadNoteNamed(String filename);

}
