/*
GNU Lesser General Public License

Translatrix - General Access To Language Resource Bundles
Copyright (C) 2002  Howard A Kistler

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

package com.hexidec.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Translatrix {
	
	private static final Log log = LogFactory.getLog(Translatrix.class);

	private static ResourceBundle langResources;

	public static void init(String bundle, Locale locale) {
		
		if(langResources != null) {
			return;
		}
		
		try {
			langResources = ResourceBundle.getBundle(bundle, locale, ClassLoader.getSystemClassLoader());
		} catch (MissingResourceException mre) {
			logException(
					"MissingResourceException while loading language file", mre);
		}
	}

	public static String getTranslationString(String originalText) {
		if (langResources == null) {
			return originalText;
		} else {
			try {
				return langResources.getString(originalText);
			} catch (Exception e) {
				return originalText;
			}
		}
	}

	private static void logException(String internalMessage, Exception e) {
		log.error(internalMessage, e);
	}

}
