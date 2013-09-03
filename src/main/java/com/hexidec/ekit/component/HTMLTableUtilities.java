/*
GNU Lesser General Public License

HTMLUtilities - Special Utility Functions For Ekit
Copyright (C) 2003 Rafael Cieplinski & Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.hexidec.ekit.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTMLTableUtilities {
	
	private static final Log log = LogFactory.getLog(HTMLTableUtilities.class);
	
//	Pattern tagPattern = Pattern.compile("</?(p|table)\\b.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern pTabelaEmParagrafo = Pattern.compile(
			"(<p\\b[^>]*>)([^(?:</p)]*?)(<table.*?>)(.+?)</table>(.*?)</p\\b.*?>", 
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	private static final Pattern pTH = Pattern.compile(
			"(</?)th(.*?>)", 
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	
	
	private static String converteTHemTD(String str) {
		
		Matcher m = pTH.matcher(str);
		if(!m.find()) {
			return str;
		}
	
		StringBuffer sb = new StringBuffer();
		String antes, depois;
		do {
			antes = m.group(1);
			depois = m.group(2);
			
			m.appendReplacement(sb, 
					Matcher.quoteReplacement(antes + "td" + depois));

		} while(m.find());
		
		m.appendTail(sb);
		
		return sb.toString();
	}

	public static String separaTabelasDeParagrafos(String str) {

		Matcher m = pTabelaEmParagrafo.matcher(str);
		if(!m.find()) {
			return str;
		}
	
		StringBuffer sb = new StringBuffer();
		StringBuffer trecho;
		String pOpen, strBefore, tOpen, tBody, strAfter;
		do {
			trecho = new StringBuffer();
			pOpen = m.group(1);
			strBefore = m.group(2);
			tOpen = m.group(3);
			tBody = m.group(4);
			strAfter = m.group(5);
			
			if(!StringUtils.isEmpty(strBefore.trim())) {
				trecho.append(pOpen);
				trecho.append(strBefore);
				trecho.append("</p>");
			}
			trecho.append(tOpen);
			trecho.append(tBody);
			trecho.append("</table>");
			if(!StringUtils.isEmpty(strAfter.trim())) {
				trecho.append(pOpen);
				trecho.append(strAfter);
				trecho.append("</p>");
			}
			
			m.appendReplacement(sb, Matcher.quoteReplacement(trecho.toString()));

		} while(m.find());
		
		m.appendTail(sb);
		
		return sb.toString();
	}

	public static String corrigeTabelas(String str) {
		
		// Converte TH em TD
		str = HTMLTableUtilities.converteTHemTD(str);
		
		return str;
	}
	
//	public static void main(String[] args) {
////		System.out.println(converteBlocosEmParagrafos("<meta http-equiv='content-type' content='text/html; charset=utf-8'><h2 style='font-family: verdana, helvetica, arial, sans-serif; font-size: 22px; margin-top: 10px; margin-bottom: 10px; font-weight: normal; background-color: transparent; color: rgb(0, 0, 0); font-style: normal; font-variant: normal; letter-spacing: normal; line-height: normal; orphans: auto; text-align: start; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px;'>HTML Table Example:</h2><table border='1' cellpadding='3' style='font-family: verdana, helvetica, arial, sans-serif; font-size: 12px; color: rgb(0, 0, 0); font-style: normal; font-variant: normal; font-weight: normal; letter-spacing: normal; line-height: normal; orphans: auto; text-align: start; text-indent: 0px; text-transform: none; white-space: normal; widows: auto; word-spacing: 0px; -webkit-text-stroke-width: 0px;'><tbody><tr><th style='font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>First Name</th><th style='font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Last Name</th><th style='font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Points</th></tr><tr><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Jill</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Smith</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>50</td></tr><tr><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Eve</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Jackson</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>94</td></tr><tr><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>John</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Doe</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>80</td></tr><tr><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Adam</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>Johnson</td><td style='line-height: 16px; font-family: verdana, helvetica, arial, sans-serif; font-size: 12px;'>67</td></tr></tbody></table><br class='Apple-interchange-newline'>"));
//		
////		String str = "<p>1</p><p>2<table>3</table>4</p><p>5</p>";
//		String str = "<p>1</p><table>2</table><p>3</p>";
//		Matcher m = pTabelaEmParagrafo.matcher(str);
//		if(m.find()) {
//			System.out.println(m.group());
//			System.out.println(m.group(1));
//			System.out.println(m.group(2));
//			System.out.println(m.group(3));
//			System.out.println(m.group(4));
//			System.out.println(m.group(5));
//		}
//		else {
//			System.out.println("Não encontrado.");
//		}
//	}
	
}

