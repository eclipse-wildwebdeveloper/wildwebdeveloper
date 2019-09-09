package org.eclipse.wildwebdeveloper.debug.chrome;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class RunChromeDebugTab extends RunHTMLDebugTab {
	private Button verboseConsoleOutput;
	public RunChromeDebugTab() {
		super();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		

		verboseConsoleOutput = new Button(resComposite, SWT.CHECK);
		verboseConsoleOutput.setText("Verbose console output");
		verboseConsoleOutput.addSelectionListener(SelectionListener.widgetSelectedAdapter((e) -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}));
	}
	
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(ChromeRunDAPDebugDelegate.VERBOSE, verboseConsoleOutput.getSelection());
	}

}
