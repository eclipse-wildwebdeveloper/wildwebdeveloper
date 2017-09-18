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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.bluesky.colors.ColorSymbolAnnotation.IColorProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4j.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Color symbol support used to:
 * 
 * <ul>
 * <li>draw in the viewer colorized squares be according a list of color range
 * to update.</li>
 * <li>click on colorized square to open a color picker and choose a color.</li>
 * </ul>
 *
 */
public class ColorSymbolSupport implements IColorProvider, ITextPresentationListener, IAnnotationModelListener,
		IAnnotationModelListenerExtension {

	class MouseTracker implements MouseTrackListener, MouseListener, MouseMoveListener {

		private boolean hovered;

		@Override
		public void mouseHover(MouseEvent e) {
			// Mouse hover is done
			StyledText styledText = viewer.getTextWidget();
			if (getColorAnnotationAtPoint(viewer, e.x, e.y) != null) {
				// A colorized square is hovered, update the cursor with cursor hand.
				styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				this.hovered = true;
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if (e.button != 1) {
				return;
			}
			// Mouse click is done
			ColorSymbolAnnotation annotation = getColorAnnotationAtPoint(viewer, e.x, e.y);
			if (annotation != null) {
				// Colorized square was clicked, open the color dialog
				// Compute location of Color dialog
				StyledText styledText = viewer.getTextWidget();
				Rectangle location = Geometry.toDisplay(styledText, new Rectangle(e.x, e.y, 1, 1));
				Shell shell = new Shell(styledText.getDisplay());
				shell.setLocation(location.x, location.y);
				// Open color dialog
				ColorDialog dialog = new ColorDialog(shell);
				dialog.setRGB(annotation.getRGBA().rgb);
				RGB color = dialog.open();
				if (color != null) {
					// Color was selected, update the viewer
					try {
						String rgb = ColorHelper.formatToRGB(color);
						viewer.getDocument().replace(annotation.getPosition().getOffset(),
								annotation.getPosition().getLength(), rgb);
					} catch (BadLocationException e1) {

					}
				}
			}
		}

		@Override
		public void mouseMove(MouseEvent e) {
			if (this.hovered) {
				// Update hand cursor with orignal cursor.
				StyledText styledText = viewer.getTextWidget();
				styledText.setCursor(null);
				this.hovered = false;
			}
		}

		@Override
		public void mouseEnter(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseExit(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// Do nothing
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// Do nothing
		}
	}

	class FontPropertyChangeListener implements IPropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (viewer == null)
				return;

			String property = event.getProperty();
			// FIXME: filter property name with
			// AbstractTextEditor#getFontPropertyPreferenceKey which is protected
			StyledText styledText = viewer.getTextWidget();
			// Font changes was done (ex: zoom), GlyphMetrics width must be recomputed
			IAnnotationModel annotationModel = viewer.getAnnotationModel();
			Iterator<Annotation> annotations = annotationModel.getAnnotationIterator();
			if (annotations.hasNext()) {
				try {
					styledText.setRedraw(false);
					annotations.forEachRemaining(annotation -> {
						if (isIncluded(annotation)) {
							// Annotation is a color symbol
							ColorSymbolAnnotation ann = (ColorSymbolAnnotation) annotation;
							Position position = ann.getPosition();
							if (position != null && !ann.isMarkedDeleted()) {
								StyleRange style = styledText.getStyleRangeAtOffset(position.getOffset());
								if (style != null && style.metrics != null) {
									// Update the width metrics
									style.metrics.width = getSquareWith(styledText);
									styledText.setStyleRange(style);
								}
							}
						}
					});
				} finally {
					styledText.setRedraw(true);
				}
			}
		}
	}

	/**
	 * Viewer to update.
	 */
	private ISourceViewer viewer;

	/**
	 * Holds the current color symbol annotations.
	 */
	private List<Annotation> colorSymbolAnnotations = null;

	/**
	 * True when {@link AnnotationPainter} is initialized with annotation color type
	 * and false otherwise.
	 */
	private boolean painterInitialized;

	/**
	 * Color cache
	 */
	private Map<RGBA, Color> colorsMap;

	/**
	 * Mouse tracker used to open color dialog when user click on colorized square.
	 */
	private MouseTracker mouseTracker;

	/**
	 * Font listener used to recompute width of GlyphMetrics when Zoom In/Out is
	 * done.
	 */
	private IPropertyChangeListener fFontPropertyChangeListener = new FontPropertyChangeListener();

	/**
	 * Install color support for the given viewer.o
	 * 
	 * @param viewer
	 */
	public void install(ISourceViewer viewer) {
		this.viewer = viewer;
		colorsMap = new HashMap<>();
		// Install mouse tracker to open color picker when colorized square is clicked.
		this.mouseTracker = new MouseTracker();
		StyledText styledText = viewer.getTextWidget();
		JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
		styledText.addMouseListener(mouseTracker);
		styledText.addMouseTrackListener(mouseTracker);
		styledText.addMouseMoveListener(mouseTracker);
	}

	/**
	 * Uninstall color support.
	 */
	public void uninstall() {
		if (viewer != null) {
			((ITextViewerExtension4) viewer).removeTextPresentationListener(this);
			IAnnotationModel annotationModel = viewer.getAnnotationModel();
			annotationModel.removeAnnotationModelListener(this);
			// Uninstall mouse tracker
			StyledText styledText = viewer.getTextWidget();
			JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
			styledText.removeMouseListener(mouseTracker);
			styledText.removeMouseTrackListener(mouseTracker);
			styledText.removeMouseMoveListener(mouseTracker);
		}
		colorsMap.values().forEach(Color::dispose);
		this.viewer = null;
		this.mouseTracker = null;
		this.colorsMap = null;
	}

	/**
	 * Update UI with the given list of color range.
	 * 
	 * @param ranges
	 */
	public void colorize(List<Range> ranges) {
		initializePainter();
		updateAnnotations(ranges);
	}

	/**
	 * Initialize {@link AnnotationPainter} with annotation color type if needed.
	 */
	private void initializePainter() {
		if (painterInitialized) {
			return;
		}
		painterInitialized = ColorAnnotationPainter.initializeColorPainter(viewer);
		if (painterInitialized) {
			((ITextViewerExtension4) viewer).addTextPresentationListener(this);
			IAnnotationModel annotationModel = viewer.getAnnotationModel();
			annotationModel.addAnnotationModelListener(this);
		}
	}

	/**
	 * Update the UI annotations with the given list of Color range.
	 *
	 * @param highlights
	 *            list of DocumentHighlight
	 * @param annotationModel
	 *            annotation model to update.
	 */
	private void updateAnnotations(List<? extends Range> ranges) {
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		IDocument document = viewer.getDocument();
		Map<Annotation, Position> annotationsToAdd = new HashMap<>(ranges.size());
		// Initialize annotations to delete with last annotations
		List<Annotation> annotationsToRemove = colorSymbolAnnotations != null ? new ArrayList<>(colorSymbolAnnotations)
				: Collections.emptyList();
		List<Annotation> currentAnnotations = new ArrayList<>();
		// Loop for color ranges
		for (Range range : ranges) {
			try {
				int startOffset = LSPEclipseUtils.toOffset(range.getStart(), document);
				int endOffset = LSPEclipseUtils.toOffset(range.getEnd(), document);
				int length = endOffset - startOffset;
				String text = document.get(startOffset, length);
				RGBA rgba = ColorHelper.getRGBColor(text);
				if (rgba != null) {
					Position pos = new Position(startOffset, length);
					// Try to find existing annotation
					ColorSymbolAnnotation ann = findExistingAnnotation(pos, rgba);
					if (ann == null) {
						// The annotation doesn't exists, create it.
						ann = new ColorSymbolAnnotation(rgba, pos, this);
						annotationsToAdd.put(ann, pos);
					} else {
						// The annotation exists, remove it from the list to delete.
						annotationsToRemove.remove(ann);
					}
					currentAnnotations.add(ann);
				}
			} catch (BadLocationException e) {
			}
		}

		synchronized (getLockObject(annotationModel)) {
			colorSymbolAnnotations = currentAnnotations;
			if (annotationsToAdd.size() == 0 && annotationsToRemove.size() == 0) {
				// None change, do nothing. Here the user could change position of color range
				// (ex: user key press
				// "Enter"), but we don't need to redraw the viewer because change of position
				// is done by AnnotationPainter.
				return;
			}
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]), annotationsToAdd);
			} else {
				removeColorSymbolAnnotations();
				Iterator<Entry<Annotation, Position>> iter = annotationsToAdd.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Annotation, Position> mapEntry = iter.next();
					annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
				}
			}

			// Compute start, end offset range used to invalidate text presentation
			// by using added and deleted annotations
			List<Annotation> allAnnotations = new ArrayList<>(annotationsToAdd.keySet());
			allAnnotations.addAll(annotationsToRemove);
			Collections.sort(allAnnotations, (a1, a2) -> {
				return ((ColorSymbolAnnotation) a1).getPosition().offset
						- ((ColorSymbolAnnotation) a2).getPosition().offset;
			});

			Position first = ((ColorSymbolAnnotation) allAnnotations.get(0)).getPosition();
			Position end = allAnnotations.size() > 1
					? ((ColorSymbolAnnotation) allAnnotations.get(allAnnotations.size() - 1)).getPosition()
					: null;
			IRegion region = new Region(first.getOffset(), end == null ? first.getLength() > 0 ? first.getLength() : 1
					: end.getOffset() + end.getLength() - first.getOffset());

			// invalidate only changed
			viewer.getTextWidget().getDisplay().asyncExec(() -> {
				if (viewer instanceof ITextViewerExtension2)
					((ITextViewerExtension2) viewer).invalidateTextPresentation(region.getOffset(), region.getLength());
				else
					viewer.invalidateTextPresentation();
			});
		}
	}

	/**
	 * Returns existing color annotation with the given position and rgb color
	 * information and null otherwise.
	 * 
	 * @param pos
	 * @param rgba
	 * @return
	 */
	private ColorSymbolAnnotation findExistingAnnotation(Position pos, RGBA rgba) {
		if (colorSymbolAnnotations == null) {
			return null;
		}
		for (Annotation annotation : colorSymbolAnnotations) {
			ColorSymbolAnnotation ann = (ColorSymbolAnnotation) annotation;
			if (ann.getPosition().offset == pos.offset && ann.getRGBA().equals(rgba)) {
				return ann;
			}
		}
		return null;
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel
	 *            the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	void removeColorSymbolAnnotations() {

		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		if (annotationModel == null || colorSymbolAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						colorSymbolAnnotations.toArray(new Annotation[colorSymbolAnnotations.size()]), null);
			} else {
				for (Annotation fColorSymbolAnnotation : colorSymbolAnnotations)
					annotationModel.removeAnnotation(fColorSymbolAnnotation);
			}
			colorSymbolAnnotations = null;
		}
	}

	@Override
	public void modelChanged(IAnnotationModel model) {
		// Do nothing
	}

	@Override
	public void modelChanged(AnnotationModelEvent event) {
		Annotation[] removed = event.getRemovedAnnotations();
		if (removed != null) {
			for (Annotation annotation : removed) {
				// Mark color symbol annotation as deleted
				if (ColorSymbolAnnotation.TYPE.equals(annotation.getType())) {
					((ColorSymbolAnnotation) annotation).markDeleted(true);
				}
			}
		}
	}

	@Override
	public Color getColor(RGBA rgba) {
		Color color = colorsMap.get(rgba);
		if (color != null) {
			return color;
		}
		color = new Color(viewer.getTextWidget().getDisplay(), rgba);
		colorsMap.put(rgba, color);
		return color;
	}

	@Override
	public void applyTextPresentation(TextPresentation textPresentation) {
		// Color symbol annotation is drawn with a colorized square which takes place.
		// SWT StyledText doesn't provide the capability to draw a square which takes
		// place without modifying
		// the StyledText content (by using \uFFFC).
		// To fix this problem, we use the following idea:
		// - set with of colorized square with GlyphMetrics in the start offset of color
		// range
		// - redraw the character replaced by GlyphMetrics
		IAnnotationModel annotationModel = viewer.getAnnotationModel();
		IRegion region = textPresentation.getExtent();
		((IAnnotationModelExtension2) annotationModel)
				.getAnnotationIterator(region.getOffset(), region.getLength(), true, true)
				.forEachRemaining(annotation -> {
					if (isIncluded(annotation)) {
						// Annotation is a color symbol
						ColorSymbolAnnotation ann = (ColorSymbolAnnotation) annotation;
						// Get the position of the annotation.
						// We cannot use "annotationModel.getPosition(annotation);" because when
						// annotation is removed, the position returned by annotation model is null.
						// To fix that, position is stored inside ColorSymbolAnnotation
						Position position = ann.getPosition();
						if (position != null) {
							StyleRange s = new StyleRange();
							s.start = position.getOffset();
							s.length = 1;
							// if annotation is removed, update metrics to null otherwise, set a
							// GlyphMetrics with a width
							s.metrics = ann.isMarkedDeleted() ? null
									: new GlyphMetrics(0, 0, getSquareWith(viewer.getTextWidget()));
							textPresentation.mergeStyleRange(s);
						}
					}
				});
	}

	private static boolean isIncluded(Annotation annotation) {
		return (annotation instanceof ColorSymbolAnnotation);
	}

	/**
	 * Returns the {@link ColorSymbolAnnotation} at the given point of the given
	 * text viewer and null otherwise.
	 * 
	 * @param textViewer
	 * @param x
	 * @param y
	 * @return the {@link ColorSymbolAnnotation} at the given point of the given
	 *         text viewer and null otherwise.
	 */
	private static ColorSymbolAnnotation getColorAnnotationAtPoint(ITextViewer textViewer, int x, int y) {
		try {
			StyledText styledText = textViewer.getTextWidget();
			// Try to retrieve ColorSymbolAnnotation of hovered region
			int offset = styledText.getOffsetAtLocation(new Point(x, y));
			ColorSymbolAnnotation annotation = getColorAnnotationAt(textViewer, offset);
			if (annotation != null) {
				// It exists a color symbol annotation, check if it's only the colorized square
				// which is hovered.
				// Compute size of colorized square
				GC gc = new GC(styledText);
				FontMetrics fontMetrics = gc.getFontMetrics();
				int size = getSquareSize(fontMetrics);
				gc.dispose();
				// Get the point of the start of annotation
				Point p = styledText.getLocationAtOffset(annotation.getPosition().getOffset());
				if (x - p.x <= size) {
					// Click was done inside the colorized square.
					return annotation;
				}
			}
		} catch (IllegalArgumentException ex) {
			// Thrown when mouse hover is outside StyledText lines content.
		}
		return null;
	}

	/**
	 * Returns the {@link ColorSymbolAnnotation} at the given offset of the given
	 * text viewer and null otherwise.
	 * 
	 * @param textViewer
	 *            the viewer
	 * @param offset
	 *            the offset
	 * @return {@link ColorSymbolAnnotation} at the given offset of th egiven text
	 *         viewer and null otherwise.
	 */
	private static ColorSymbolAnnotation getColorAnnotationAt(ITextViewer textViewer, int offset) {
		IAnnotationModel annotationModel = ((ISourceViewer) textViewer).getAnnotationModel();
		Iterator<Annotation> iter = ((IAnnotationModelExtension2) annotationModel).getAnnotationIterator(offset, 1,
				true, true);
		while (iter.hasNext()) {
			Annotation ann = iter.next();
			if (ann instanceof ColorSymbolAnnotation) {
				return (ColorSymbolAnnotation) ann;
			}
		}
		return null;
	}

	/**
	 * Returns the colorized square size.
	 * 
	 * @param fontMetrics
	 * @return the colorized square size.
	 */
	public static int getSquareSize(FontMetrics fontMetrics) {
		return fontMetrics.getHeight() - 2 * fontMetrics.getDescent();
	}

	/**
	 * Compute width of square
	 * 
	 * @param styledText
	 * @return the width of square
	 */
	private static int getSquareWith(StyledText styledText) {
		GC gc = new GC(styledText);
		FontMetrics fontMetrics = gc.getFontMetrics();
		// width = 2 spaces + size width of square
		int width = 2 * fontMetrics.getAverageCharWidth() + getSquareSize(fontMetrics);
		gc.dispose();
		return width;
	}
}
