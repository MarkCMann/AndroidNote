package com.mmann.notetaking;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;

public class LinkParser {
	
	private static LinkParser lp;
	
	public static LinkParser sharedParser() {
		if (lp == null) {
			lp = new LinkParser();
		}
		return lp;
	}
	
	public SpannableString parseString(String string, NoteDelegate delegate) {
		if (string == null || string.equalsIgnoreCase("")) {
			return new SpannableString("");
		}
		
		String[] strings = string.split("@subnote:");
		SpannableStringBuilder sb = new SpannableStringBuilder();
		sb.append(strings[0]);
		String filename;
		String[] link, text;
		for (int i = 1; i < strings.length; i++) {
			link = strings[i].split("@<");
			if (link.length <= 1) {
				sb.append(strings[i]);
				continue;
			}
			filename = link[0];
			// We need to catch the trailing > and the following text.
			text = link[1].split(">");
			sb.append(attachFileLinkToSequence(text[0], filename, delegate));
			if (text.length > 1) {
				sb.append(text[1]);
			}
		}
		
		return new SpannableString(sb);
	}
	
	public CharSequence attachFileLinkToSequence(CharSequence string, final String filename, final NoteDelegate delegate) {
		SpannableStringBuilder sb = new SpannableStringBuilder(string);
		sb.setSpan(new FileLoadingSpan(filename, delegate), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sb;
	}
	
	/**Creates a copy of the incoming sequence and then replaces the links with a textual representation of the links.
	 * 
	 * @param sequence The sequence to delinkify.
	 * @return
	 */
	public CharSequence rawTextForSequence(SpannableString sequence) {
		SpannableStringBuilder sb = new SpannableStringBuilder(sequence);
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
}
