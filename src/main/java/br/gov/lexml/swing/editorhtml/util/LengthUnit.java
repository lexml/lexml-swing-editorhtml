package br.gov.lexml.swing.editorhtml.util;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.HashMap;

/**
 * Conversão de valores de comprimento HTML/CSS
 * 
 * Classe retirada de javax.swing.text.html.CSS$LengthUnit
 */
public class LengthUnit {
	
	private static HashMap<String, Float> lengthMapping = new HashMap<String, Float>(6);
	
	private static HashMap<String, Float> w3cLengthMapping = new HashMap<String, Float>(6);
	
	// 0 - value indicates real value
	// 1 - % value, value relative to depends upon key.
	// 50% will have a value = .5
	// 2 - add value to parent value.
	// 3 - em/ex relative to font size of element (except for
	// font-size, which is relative to parent).
	private short type;
	private float value;
	private String units = null;

	private static final short UNINITALIZED_LENGTH = (short) 10;
	
	static {
		lengthMapping.put("pt", new Float(1f));
		// Not sure about 1.3, determined by experiementation.
		lengthMapping.put("px", new Float(1.3f));
		lengthMapping.put("mm", new Float(2.83464f));
		lengthMapping.put("cm", new Float(28.3464f));
		lengthMapping.put("pc", new Float(12f));
		lengthMapping.put("in", new Float(72f));
		int res = 72;
		try {
			res = Toolkit.getDefaultToolkit().getScreenResolution();
		} catch (HeadlessException e) {
		}
		// mapping according to the CSS2 spec
		w3cLengthMapping.put("pt", new Float(res / 72f));
		w3cLengthMapping.put("px", new Float(1f));
		w3cLengthMapping.put("mm", new Float(res / 25.4f));
		w3cLengthMapping.put("cm", new Float(res / 2.54f));
		w3cLengthMapping.put("pc", new Float(res / 6f));
		w3cLengthMapping.put("in", new Float(res));
	}

	public LengthUnit(String value, short defaultType, float defaultValue) {
		parse(value, defaultType, defaultValue);
	}

	private void parse(String value, short defaultType, float defaultValue) {
		type = defaultType;
		this.value = defaultValue;

		int length = value.length();
		if (length > 0 && value.charAt(length - 1) == '%') {
			try {
				this.value = Float.valueOf(value.substring(0, length - 1))
						.floatValue() / 100.0f;
				type = 1;
			} catch (NumberFormatException nfe) {
			}
		}
		if (length >= 2) {
			units = value.substring(length - 2, length);
			Float scale = (Float) lengthMapping.get(units);
			if (scale != null) {
				try {
					this.value = Float.valueOf(value.substring(0, length - 2))
							.floatValue();
					type = 0;
				} catch (NumberFormatException nfe) {
				}
			} else if (units.equals("em") || units.equals("ex")) {
				try {
					this.value = Float.valueOf(value.substring(0, length - 2))
							.floatValue();
					type = 3;
				} catch (NumberFormatException nfe) {
				}
			} else if (value.equals("larger")) {
				this.value = 2f;
				type = 2;
			} else if (value.equals("smaller")) {
				this.value = -2;
				type = 2;
			} else {
				// treat like points.
				try {
					this.value = Float.valueOf(value).floatValue();
					type = 0;
				} catch (NumberFormatException nfe) {
				}
			}
		} else if (length > 0) {
			// treat like points.
			try {
				this.value = Float.valueOf(value).floatValue();
				type = 0;
			} catch (NumberFormatException nfe) {
			}
		}
	}

	public float getValue(boolean w3cLengthUnits) {
		HashMap<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
		float scale = 1;
		if (units != null) {
			Float scaleFloat = (Float) mapping.get(units);
			if (scaleFloat != null) {
				scale = scaleFloat.floatValue();
			}
		}
		return this.value * scale;
	}

	public static float getValue(float value, String units) {
		return getValue(value, units, false);
	}
	
	public static float getValue(float value, String units, Boolean w3cLengthUnits) {
		return value * getScale(units, w3cLengthUnits);
	}
	
	public static String convertTo(float value, String units) {
		return convertTo(value, units, false);
	}

	public static String convertTo(float value, String units, Boolean w3cLengthUnits) {
		return truncateToString(value / getScale(units, w3cLengthUnits)) + units;
	}
	
	private static String truncateToString(float f) {
		if(f == (int)f) {
			return Integer.toString((int)f);
		}
		return Float.toString(Math.round(f * 100f) / 100f);
	}

	private static float getScale(String units, Boolean w3cLengthUnits) {
		HashMap<String, Float> mapping = (w3cLengthUnits) ? w3cLengthMapping : lengthMapping;
		float scale = 1;
		if (units != null) {
			Float scaleFloat = (Float) mapping.get(units);
			if (scaleFloat != null) {
				scale = scaleFloat.floatValue();
			}
		}
		return scale;
	}

	public String toString() {
		return type + " " + value;
	}

}
