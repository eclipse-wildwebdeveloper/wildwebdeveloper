/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.bluesky.colors;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Color symbol drawing strategy to draw a colorized square.
 *
 */
class ColorSymbolDrawingStrategy implements IDrawingStrategy {

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (gc != null) {
			ColorSymbolAnnotation ann = (ColorSymbolAnnotation) annotation;
			if (ann.isMarkedDeleted()) {
				return;
			}
			FontMetrics fontMetrics = gc.getFontMetrics();

			// Compute position and size of the color square
			Rectangle bounds = textWidget.getTextBounds(offset, offset);
			int x = bounds.x + fontMetrics.getLeading();
			int y = bounds.y + fontMetrics.getDescent();
			int size = fontMetrics.getHeight() - 2 * fontMetrics.getDescent();
			Rectangle rect = new Rectangle(x, y, size, size);

			// Fill square
			gc.setBackground(ann.getColor());
			gc.fillRectangle(rect);

			// Draw square box
			gc.setForeground(textWidget.getForeground());
			gc.drawRectangle(rect);

			// The square replaces the first character of the color by taking a place
			// (COLOR_SQUARE_WITH) by using GlyphMetrics
			// Here we need to redraw this first character because GlyphMetrics clip this
			// color character.
			String s = textWidget.getText(offset, offset);
			StyleRange style = textWidget.getStyleRangeAtOffset(offset);
			if (style != null) {
				if (style.background != null) {
					gc.setBackground(style.background);
				}
				if (style.foreground != null) {
					gc.setForeground(style.foreground);
				}
			}
			gc.drawString(s, bounds.x + bounds.width - gc.stringExtent(s).x, bounds.y, true);
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}

}