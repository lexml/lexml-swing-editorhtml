package br.gov.lexml.swing.editorhtml.util;

import java.util.Properties;

public class ConversaoFO {
	
//	public static void main(String[] args) {
//		String html = "<p style=\"text-indent: 0cm; margin-left: 3cm; \">" +
//"A medida provis&#243;ria&#160; 595/2012, conhecida como MP dos Portos, estabelece" + 
//"</p>";
//		HTML2FOConverter converter = new HTML2FOConverter();
//		System.out.println(converter.html2fo(html));
//	}
	
	public static void main(String[] args) {
		String str = "<p class=\"\" align=\"justify\">" +
				"      &#8220;Test" +
				"    </p>" +
				"    <p class=\"\" align=\"justify\">" +
				"      Art 1&#186; <span class=\"omissis\">..................................................</span>" +
				"    </p>" +
				"    <p class=\"\" align=\"justify\">" +
				"      &#167; 2 <span class=\"omissis\">..................................................</span>&#8221; (NR)" +
				"    </p>";
//		String str = "xpto<span class=\"omissis\"><b>.......</b></span>";
//		String str = "<p>\n&#8220;<b>Art 1&#186;</b> abc\n <b>xxx</b> .......................</p>";
		Properties config = new Properties();
//		config.put(HTML2FOConverter.CONF_OUTPUT_FORMAT, HTML2FOConverter.OUTPUT_FORMAT_RTF);
		HTML2FOConverter converter = new HTML2FOConverter(config);
		System.out.println(converter.html2fo(str));
	}
	
	

}
