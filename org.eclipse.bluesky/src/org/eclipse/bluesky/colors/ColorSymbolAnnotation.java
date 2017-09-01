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

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.RGBA;

/**
 * Color symbol annotation.
 *
 */
class ColorSymbolAnnotation extends Annotation {

	/**
	 * The type of color annotations.
	 */
	public static final String TYPE = "org.eclipse.bluesky.color"; //$NON-NLS-1$

	/**
	 * Color provider
	 *
	 */
	public interface IColorProvider {

		Color getColor(RGBA rgba);
	}

	/**
	 * The rgb color information.
	 */
	private final RGBA rgba;

	/**
	 * The position of the annotation. This information is required since color
	 * symbol takes place with {@link GlyphMetrics} which must be updated.
	 * 
	 * @see explanation at
	 *      {@link ColorSymbolSupport#applyTextPresentation(org.eclipse.jface.text.TextPresentation)}
	 */
	private final Position position;

	/**
	 * The color provider.
	 */
	private final IColorProvider colorProvider;

	public ColorSymbolAnnotation(RGBA rgba, Position position, IColorProvider colorProvider) {
		super(TYPE, false, null);
		this.rgba = rgba;
		this.position = position;
		this.colorProvider = colorProvider;
	}

	/**
	 * Returns the rgb color information.
	 * 
	 * @return the rgb color information.
	 */
	public RGBA getRGBA() {
		return rgba;
	}

	/**
	 * Returns the position of the annotation.
	 * 
	 * @return the position of the annotation
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Returns the SWT Color.
	 * 
	 * @return the SWT Color.
	 */
	public Color getColor() {
		return colorProvider.getColor(getRGBA());
	}
}
