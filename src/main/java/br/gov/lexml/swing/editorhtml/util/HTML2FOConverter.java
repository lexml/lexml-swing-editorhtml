package br.gov.lexml.swing.editorhtml.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;

public class HTML2FOConverter {

	private static final Log log = LogFactory.getLog(HTML2FOConverter.class);
	
	// Chaves de configuração
	public static final String CONF_OUTPUT_FORMAT = "conf_output_format";
	public static final String CONF_PARAGRAPH_MARGIN_BOTTOM = "conf_paragraph_margin_bottom";
	
	// Valores de configuração
	public static final String OUTPUT_FORMAT_PDF = "pdf";
	public static final String OUTPUT_FORMAT_RTF = "rtf";
	
	// Valores default de configuração
	private static final String DEFAULT_PARAGRAPH_MARGIN_BOTTOM = "0.6em";
	private static final String DEFAULT_OUTPUT_FORMAT = OUTPUT_FORMAT_PDF;
	
	private String defaultParagraphMarginBottom;
	
	private String outputFormat;
	
	public HTML2FOConverter() {
		configure(new Properties());
	}
	
	public HTML2FOConverter(Properties config) {
		configure(config);
	}
	
	private void configure(Properties config) {
		defaultParagraphMarginBottom = getConfig(config, CONF_PARAGRAPH_MARGIN_BOTTOM, DEFAULT_PARAGRAPH_MARGIN_BOTTOM);
		outputFormat = getConfig(config, CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
	}
	
	private String getConfig(Properties config, String key, String defVal) {
		String val = config.getProperty(key);
		return val == null? defVal: val;
	}

	// HTML to XSL-FO
	public String html2fo(String html) {
		String xhtml = html2xhtml(html);
		return xhtml2fo(xhtml);
	}
	
	private String html2xhtml(String html) {
		if (StringUtils.isEmpty(html)){
			return "";
		}
		
		// Garante tag raiz único para o HtmlCleaner 
		html = "<div>" + html + "</div>";
		
		CleanerProperties props = new CleanerProperties();
		 
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitXmlDeclaration(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitComments(true);
		 
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(html);
		 
		// serialize to xml file
		String ret;
		try {
			ret = new SimpleXmlSerializer(props).getAsString(tagNode);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		// Remove tag raiz <div>
		ret = ret.substring(5, ret.length() - 6);
		
		return ret;
	}

	private String xhtml2fo(String xhtml) {

		if (StringUtils.isEmpty(xhtml)){
			return "";
		}
		
		String ret = "";
		
		try {
	        if (log.isDebugEnabled()) {
	        	log.debug("xhtml2fo: xhtml="+xhtml);
	        }
			
			xhtml = unescapeHtmlKeepingXMLEntities(xhtml);
			
			TransformerFactory factory = new TransformerFactoryImpl();
	        Transformer transformer = factory.newTransformer(
	        		new StreamSource(getClass().getResourceAsStream("/xhtml2fo.xsl")));
	        
	        
	        xhtml = trataMarginBottom(xhtml);
	        
	        // Transofrma class="no_indent" em style="text-indent: 0;"
	        xhtml = trataNoIndent(xhtml);
	        
	        // Transforma <span class='omissis'>... em <omissis/>
	        xhtml = trataOmissis(xhtml);
	        
	        // Coloca div para poder processar conteúdo inline.
	        xhtml = "<div>" + xhtml + "</div>";
	        
	        if (log.isDebugEnabled()) {
	        	log.debug("xhtml2fo: xhtml depois="+xhtml);
	        }
	        
	        ByteArrayInputStream bis = new ByteArrayInputStream(xhtml.getBytes("UTF-8"));
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        
	        transformer.setParameter("formato", outputFormat);
	        
	        transformer.transform(new StreamSource(bis), new StreamResult(bos));
	        
	        ret = new String(bos.toByteArray(), "UTF-8");
	        
	        // Retira o block do div
	        ret = ret.substring(ret.indexOf(">") + 1);
	        ret = ret.substring(0, ret.lastIndexOf("<"));
		}
		catch(Exception e) {
			log.error("Falha na conversão de XHTML para XSL-FO.", e);
			ret = "[Falha na formatação do campo. (" + e.getMessage() + ")]";
		}
		
		return ret;
		
	}

	private static String unescapeHtmlKeepingXMLEntities(String xhtml) {
		xhtml = xhtml.replaceAll("&([gl]t;)", "&amp;$1");
		return StringEscapeUtils.unescapeHtml(xhtml);
	}
	
	/**
	 * Coloca atributo css "margin-bottom: $pMarginBottomDefault" nos parágrafos
	 */
	private String trataMarginBottom(String xhtml) {
		
		StringBuffer sb = new StringBuffer();
		
		Pattern tagPattern = Pattern.compile("</?(p|table)\\b.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = tagPattern.matcher(xhtml);
		
		String style="style=\"";
		
		int i;
		String tag;
		String tagName;
		boolean fecha;
		int tableDepth = 0;
		while(m.find()) {
			tag = m.group();
			tagName = m.group(1).toLowerCase();
			fecha = tag.startsWith("</");
			if(!fecha && tableDepth == 0) {
				i = tag.indexOf(style);
				if(i == -1) {
					tag = tag.replace(">", " style=\"margin-bottom: " + defaultParagraphMarginBottom + ";\">");
				}
				else {
					tag = tag.replace(style, style + "margin-bottom: " + defaultParagraphMarginBottom + "; ");
				}
			}
			if(tagName.equals("table")) {
				tableDepth += (fecha? -1: 1);
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		m.appendTail(sb);
		
		return sb.toString();
	}

	/**
	 * Transofrma class="no_indent" em style="text-indent: 0;"
	 */
	private String trataNoIndent(String xhtml) {
		
		StringBuffer sb = new StringBuffer();
		
		Pattern tagPattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = tagPattern.matcher(xhtml);
		
		String classNoIndent = "class=\"no_indent\"";
		String style="style=\"";
		
		int i; 
		String tag;
		while(m.find()) {
			tag = m.group();
			i = tag.indexOf(classNoIndent);
			if(i != -1) {
				tag = tag.replace(classNoIndent, "");
				i = tag.indexOf(style);
				if(i == -1) {
					tag = tag.replace(">", " style=\"text-indent: 0;\">");
				}
				else {
					tag = tag.replace(style, style + "text-indent: 0; ");
				}
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * Trata omissis
	 */
	private String trataOmissis(String xhtml) {
		
		StringBuffer sb = new StringBuffer();
		
		Pattern omissisPattern = Pattern.compile("<span\\s+class=['\"]omissis['\"]>([^\\.]*)\\.+([^\\.]*)</span>", 
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = omissisPattern.matcher(xhtml);
		
		String tag;
		while(m.find()) {
			tag = m.group(1) + "<omissis/>" + m.group(2);
			m.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
}
