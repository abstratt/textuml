package com.abstratt.mdd.frontend.cli;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.abstratt.mdd.frontend.core.FrontEnd;

public class Helper {

	public static Status buildStatus(String message, Throwable exception) {
		return buildStatus(IStatus.ERROR, message, exception);
	}
	
	public static Status buildStatus(int severity, String message, Throwable exception) {
		return new Status(severity, FrontEnd.PLUGIN_ID, message, exception);
	}
}
