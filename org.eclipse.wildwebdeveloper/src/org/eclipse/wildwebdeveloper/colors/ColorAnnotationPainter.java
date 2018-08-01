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
package org.eclipse.wildwebdeveloper.colors;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Utilities to register Color annotation type in the {@link AnnotationPainter}
 * used in a {@link ITextViewer}.
 *
 */
public class ColorAnnotationPainter {

	private static final IDrawingStrategy COLOR_SYMBOL_STRATEGY = new ColorSymbolDrawingStrategy();
	private static final Object COLOR = "color";
	private static final Color DUMMY_COLOR = new Color(null, new RGB(0, 0, 0));

	/**
	 * Initialize Color annotation type in the {@link AnnotationPainter} used by the
	 * given {@link ITextViewer}.
	 * 
	 * @param viewer
	 * @return true if annotation painter was initialized and false otherwise.
	 */
	public static boolean initializeColorPainter(ITextViewer viewer) {
		// Get the annotation painter used by the viewer.
		AnnotationPainter painter = ColorAnnotationPainter.getAnnotationPainter(viewer);
		if (painter != null) {
			// Initialize the color annotation type
			painter.addDrawingStrategy(COLOR, COLOR_SYMBOL_STRATEGY);
			painter.addAnnotationType(ColorSymbolAnnotation.TYPE, COLOR);
			// the painter needs a color for an annotation type
			// we must set it with a dummy color even if we don't use it to draw the color
			// symbol.
			painter.setAnnotationTypeColor(ColorSymbolAnnotation.TYPE, DUMMY_COLOR);
			return true;
		}
		return false;
	}

	/**
	 * Retrieve the annotation painter used by the given text viewer.
	 * 
	 * @param viewer
	 * @return
	 */
	private static AnnotationPainter getAnnotationPainter(ITextViewer viewer) {
		// Here reflection is used, because
		// - it doesn't exists API public for get AnnotationPainter used by the viewer.
		// - it doesn't exists extension point to register custom drawing strategy. See
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=51498
		PaintManager paintManager = ColorAnnotationPainter.getFieldValue(viewer, "fPaintManager", TextViewer.class);
		if (paintManager != null) {
			List<IPainter> painters = ColorAnnotationPainter.getFieldValue(paintManager, "fPainters",
					PaintManager.class);
			if (painters != null) {
				for (IPainter painter : painters) {
					if (painter instanceof AnnotationPainter) {
						return (AnnotationPainter) painter;
					}
				}
			}
		}
		return null;
	}

	private static <T> T getFieldValue(Object object, String name, Class clazz) {
		Field f = getDeclaredField(clazz, name);
		if (f != null) {
			try {
				return (T) f.get(object);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private static Field getDeclaredField(Class clazz, String name) {
		if (clazz == null) {
			return null;
		}
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (NoSuchFieldException e) {
			return getDeclaredField(clazz.getSuperclass(), name);
		} catch (SecurityException e) {
			return null;
		}
	}

}
