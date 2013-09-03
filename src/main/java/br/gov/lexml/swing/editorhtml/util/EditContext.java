package br.gov.lexml.swing.editorhtml.util;

import javax.swing.text.AttributeSet;

public class EditContext {
	
	private int offset;
	private int length;
	private String text;
	private AttributeSet attrs;
	
	public EditContext(int offset, int length, String text, AttributeSet attrs) {
		super();
		this.offset = offset;
		this.length = length;
		this.text = text;
		this.attrs = attrs;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public AttributeSet getAttrs() {
		return attrs;
	}

	public void setAttrs(AttributeSet attrs) {
		this.attrs = attrs;
	}
	
	public int getEndOffset() {
		return offset + length;
	}
	
	@Override
	public String toString() {
		return "offset: " + offset + ", length: " + length + ", text: " + text + ", attrs: " + attrs;
	}

}
