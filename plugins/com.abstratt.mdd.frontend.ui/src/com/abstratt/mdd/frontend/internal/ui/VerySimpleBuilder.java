package com.abstratt.mdd.frontend.internal.ui;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.abstratt.mdd.frontend.core.FrontEnd;
import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.IProblem;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.core.IProblem.Severity;
import com.abstratt.mdd.ui.UIConstants;

/**
 * A temporary builder implementation.
 */
public class VerySimpleBuilder extends IncrementalProjectBuilder {

	private static int getLineNumber(IProblem problem) {
		final Integer lineNumberAttribute = (Integer) problem.getAttribute(IProblem.LINE_NUMBER);
		return lineNumberAttribute == null ? 1 : lineNumberAttribute.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IProject toBuild = getProject();

		// mark all referencing as needing rebuild
		for (IProject referencing : toBuild.getReferencingProjects())
			referencing.touch(monitor);

		// build location context
		IFileStore storeToBuild = EFS.getStore(toBuild.getLocationURI());
		LocationContext context = new LocationContext(storeToBuild);
		context.addSourcePath(storeToBuild, null);
		for (IProject referenced : toBuild.getReferencedProjects()) {
			URI referencedLocation = referenced.getLocationURI();
			if (referencedLocation != null) {
				IFileStore modelPathEntry = EFS.getStore(referencedLocation);
				context.addRelatedPath(modelPathEntry);
			}
		}
		
		removeMarkers(toBuild);
		IProblem[] problems =
						FrontEnd.getCompilationDirector().compile(null, null, context,
										ICompilationDirector.FULL_BUILD, monitor);
		toBuild.refreshLocal(IResource.DEPTH_INFINITE, null);
		Arrays.sort(problems, new Comparator<IProblem>() {
			public int compare(IProblem o1, IProblem o2) {
				if ((o1 instanceof InternalProblem) || (o2 instanceof InternalProblem)) {
					if (!(o1 instanceof InternalProblem))
						return 1;
					if (!(o2 instanceof InternalProblem))
						return -1;
					return 0;
				}
				int fileNameDelta =
								((IFileStore) o1.getAttribute(IProblem.FILE_NAME)).toURI().compareTo(
												((IFileStore) o2.getAttribute(IProblem.FILE_NAME)).toURI());
				if (fileNameDelta != 0)
					return fileNameDelta;
				int lineNo1 = getLineNumber(o1);
				int lineNo2 = getLineNumber(o2);
				return lineNo1 - lineNo2;
			}

		});
		createMarkers(toBuild, problems);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IProject toBuild = getProject();
		IFileStore storeToBuild = EFS.getStore(toBuild.getLocationURI());
		LocationContext context = new LocationContext(storeToBuild);
		FrontEnd.getCompilationDirector().compile(null, null, context, ICompilationDirector.CLEAN, monitor);
		toBuild.refreshLocal(IResource.DEPTH_INFINITE, null);
		removeMarkers(toBuild);
	}

	protected void createMarkers(IProject project, IProblem[] problems) throws CoreException {
		URI projectURI = project.getLocationURI();
		for (int i = 0; i < problems.length; i++) {
			IFileStore source = (IFileStore) problems[i].getAttribute(IProblem.FILE_NAME);
			IResource target = project;
			if (source != null) {
				final URI sourceURI = source.toURI();
				if (sourceURI != null) {
					URI relativeURI = projectURI.relativize(sourceURI);
					if (!relativeURI.isAbsolute())
						target = project.getFile(new Path(relativeURI.getPath()));
				}
			}
			IMarker marker = target.createMarker(UIConstants.MARKER_TYPE);
			marker.setAttribute(IMarker.SEVERITY, getMarkerSeverity(problems[i].getSeverity()));
			marker.setAttribute(IMarker.MESSAGE, problems[i].getMessage());
			marker.setAttribute(IMarker.LINE_NUMBER, getLineNumber(problems[i]));
		}
	}

	private int getMarkerSeverity(Severity problemSeverity) {
		switch (problemSeverity) {
		case ERROR:
			return IMarker.SEVERITY_ERROR;
		case WARNING:
			return IMarker.SEVERITY_WARNING;
		}
		// not supported yet
		return IMarker.SEVERITY_INFO;
	}

	private void removeMarkers(IResource resource) throws CoreException {
		resource.deleteMarkers(UIConstants.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}
/*	
	protected String memory(String... messages) {
		StringBuffer sb = new StringBuffer();
		for (String current : messages) {
			sb.append(current);
			sb.append(' ');
		}
		sb.append(toMB(Runtime.getRuntime().freeMemory()) + " / " + toMB(Runtime.getRuntime().totalMemory()));
		return sb.toString();
	}

	private static String toMB(long byteCount) {
		return byteCount / (1024 * 1024) + "MB";
	}
*/
}
