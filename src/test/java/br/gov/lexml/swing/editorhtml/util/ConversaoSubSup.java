package br.gov.lexml.swing.editorhtml.util;

public class ConversaoSubSup {

	public static void main(String[] args) {
		String html = "<p>Sobrescrito<sup>2</sup> e subscrito<sub>2</sub></p>";
		HTML2FOConverter converter = new HTML2FOConverter();
		String fo = converter.html2fo(html);
		System.out.println(fo);
	}
	
}
