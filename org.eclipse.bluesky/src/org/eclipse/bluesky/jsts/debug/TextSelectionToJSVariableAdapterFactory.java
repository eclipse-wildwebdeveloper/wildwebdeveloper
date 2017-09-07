package org.eclipse.bluesky.jsts.debug;

import java.lang.reflect.Field;

import org.eclipse.bluesky.Activator;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.wst.jsdt.debug.core.model.IJavaScriptStackFrame;

public class TextSelectionToJSVariableAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!(adaptableObject instanceof TextSelection)) {
			return null;
		}
		if (!adapterType.isAssignableFrom(IVariable.class)) {
			return null;
		}
		TextSelection selection = (TextSelection) adaptableObject;
		return (T)getVariableFor(selection);
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IVariable.class };
	}

	private IVariable getVariableFor(TextSelection selection) {
		IDocument document = getDocument(selection);
		if (document == null) {
			return null;
		}
		IJavaScriptStackFrame frame = getFrame();
	    if (frame == null) {
	    	return null;
	    }
	    try {
            String variableName = document.get(selection.getOffset(), selection.getLength());
            if (variableName.isEmpty()) {
            	variableName = findVariableName(document, selection.getOffset());
            }
            IVariable var = findLocalVariable(frame, variableName);
            if(var != null) {
            	return var;
            }
            
            //might be in 'this'
            var = frame.getThisObject();
            try {
                IValue val = var == null ? null : var.getValue();
                if(val != null) {
                	IVariable[] vars = val.getVariables();
                	for (int i = 0; i < vars.length; i++) {
                		if(vars[i].getName().equals(variableName)) {
                			return vars[i];
                		}
					}
                }
            }
            catch(DebugException de) {
            	return null;
            }
            	
        } catch (BadLocationException e) {
            return null;
        }
	    return null;
	}


	
	private String findVariableName(IDocument document, int offset) {
		try {
			if (!Character.isJavaIdentifierPart(document.getChar(offset))) {
				return null;
			}
			int startOffset = offset;
			while (startOffset - 1 >= 0 && Character.isJavaIdentifierPart(document.getChar(startOffset - 1))) startOffset--;
			int endOffset = offset;
			while (endOffset + 1 < document.getLength() && Character.isJavaIdentifierPart(document.getChar(endOffset + 1))) endOffset++;
			return document.get(startOffset, endOffset - startOffset + 1);
		} catch (BadLocationException ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex));
			return null;
		}
	}

	/**
	 * Returns a local variable in the given frame based on the the given name
	 * or <code>null</code> if none.
	 * 
	 * @return local variable or <code>null</code>
	 */
	private IVariable findLocalVariable(IJavaScriptStackFrame frame, String variableName) {
		if (frame != null) {
			try {
				IVariable[] vars = frame.getVariables();
	        	for (int i = 0; i < vars.length; i++) {
					if(vars[i].getName().equals(variableName)) {
						return vars[i];
					}
				}
			} catch (DebugException x) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, x.getMessage(), x));
			}
		}
		return null;
	}	

	protected IJavaScriptStackFrame getFrame() {
	    IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable != null) {
			return Adapters.adapt(adaptable, IJavaScriptStackFrame.class);
		}
		return null;
	}

	private IDocument getDocument(TextSelection sel) {
		try {
			Field documentField = TextSelection.class.getDeclaredField("fDocument"); //$NON-NLS-1$
			documentField.setAccessible(true);
			return (Document) documentField.get(sel);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			return null;
		}
	}
}
