package com.hexidec.ekit.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
  * Custom ParserCallback class by Janko Jochimsen
  */

public class EkitStandardParserCallback extends HTMLEditorKit.ParserCallback
{
	private static final Log log = LogFactory.getLog(EkitStandardParserCallback.class);
	
	HashMap<String, Integer> theErrors;
	boolean debug = false;
	
	public EkitStandardParserCallback()
	{
		theErrors = new HashMap<String, Integer>();
	}

	/**
	 * Process all Errors and take node
	 */
	public void handleError(String errorMsg, int pos)
	{
		Integer value = theErrors.get(errorMsg);
		if(value == null)
		{
			Integer one = new Integer(1);
			theErrors.put(errorMsg, one);
		}
		else
		{
			Integer incvalue = value + 1;
			theErrors.put(errorMsg, incvalue);
		}
		if(debug)
		{
			log.debug("ParserCallback " + errorMsg + ", POS: " + pos);
		}
	}

	/**
	 * Get the Errors for futher investigation
	 */
	public HashMap<String, Integer> getTheErrors()
	{
		return theErrors;
	}

	/**
	 * Service Method to output the Errors. Might be used as a Template for
	 * futher methods
	 */
	public void reportCB()
	{
		Set<String> theErrorTypes = theErrors.keySet();
		if(theErrorTypes.size() < 1)
		{
			log.debug("No Errors parsing file ");
		}
		else
		{
			Iterator<String> iter = theErrorTypes.iterator();
			while(iter.hasNext())
			{
				String error = iter.next();
				Integer count = theErrors.get(error);
				log.debug("Error [" + error + "] happend " + count + " times");
			}
		}
	}
}
