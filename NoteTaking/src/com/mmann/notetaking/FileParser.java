package com.mmann.notetaking;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;

public class FileParser {

	/**
	 * Format for an option line is
	 *      option, option_arg, begin, end
	 * @param string
	 * @param delegate
	 * @return
	 */
	public SpannableString getSpannableStringFromRawFileText(final String string, final NoteDelegate delegate) {
		if (string == null || string.equalsIgnoreCase("")) {
			return new SpannableString("");
		}
		
		String newLineString = System.getProperty("line.separator");
		String[] optionAndRest = string.split(newLineString, 1);
		int numberOfSpannables = 0;
		try {
			numberOfSpannables = Integer.parseInt(optionAndRest[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		SpannableStringBuilder sb = new SpannableStringBuilder("");
		if (numberOfSpannables > 0) {
			String[] optionsWithRest = optionAndRest[1].split(newLineString, numberOfSpannables);
			String rawText = optionsWithRest[numberOfSpannables - 1];
			sb.append(rawText);
			for (int i = 0; i < numberOfSpannables; i++) {
				String[] options = optionsWithRest[i].split(",");
				if (options[0].equalsIgnoreCase("subnote")) {
					final String filename = options[1];
					final int begin = Integer.parseInt(options[2]);
					final int end = Integer.parseInt(options[3]);
					sb.setSpan(new FileLoadingSpan(filename, delegate), begin, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		} else {
			sb.append(optionAndRest[1]);
		}
		return new SpannableString(sb);
	}
	
	/**
	 * Creates a copy of the given sequence replacing spans with a textual representation.
	 * @param sequence The sequence to remove the spans from.
	 * @return
	 */
	public CharSequence rawTextForSequence(final SpannableString sequence) {
		SpannableStringBuilder sb = new SpannableStringBuilder(sequence);
		final String newLine = System.getProperty("line.separator");
		
		FileLoadingSpan[] spans = sequence.getSpans(0, sequence.length(), FileLoadingSpan.class);
		int start, end;
		for (int i = 0; i < spans.length; i++) {
			start = sb.getSpanStart(spans[i]);
			end = sb.getSpanEnd(spans[i]);
			String str = "@subnote:" + spans[i].getFilename() + "@<" + sb.subSequence(start, end) + ">";
			sb.replace(start, end, str);
		}
		return sb;
	}
	
	class FileLoadingSpan extends ClickableSpan {

		private String filename;
		private NoteDelegate delegate;
		
		public FileLoadingSpan(String filename, NoteDelegate delegate) {
			super();
			this.filename = filename;
			this.delegate = delegate;
		}
		
		@Override
		public void onClick(View widget) {
			this.delegate.loadNoteNamed(this.filename);
		}
		
		public String toString() {
			return this.filename;
		}
		
		public String getFilename() {
			return this.filename;
		}
	}
}
