package com.hexidec.ekit.component;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;

public class SwingUtilities {

	public static Point getPointForCentering(Dialog dialog, Window parent) {
		Dimension size = dialog.getSize();
		Dimension parentSize = parent.getSize();
		int centerX = (int) ((parentSize.getWidth() - size.getWidth()) / 2 + parent.getX());
		int centerY = (int) ((parentSize.getHeight() - size.getHeight()) / 2 + parent.getY());
		return new Point(centerX, centerY);
	}

}
