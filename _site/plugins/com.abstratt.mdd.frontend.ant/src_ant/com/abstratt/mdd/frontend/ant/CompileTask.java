/*******************************************************************************
 * Copyright (c) 2009 Vladimir Sosnin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Sosnin - initial API and implementation
 *******************************************************************************/

package com.abstratt.mdd.frontend.ant;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.IProblem;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.internal.core.CompilationDirector;

/**
 * Executes <code>CompilationDirector</code>.
 * Passes all provided resources to it.
 * @author vas
 *
 */
public class CompileTask extends Task {

	private Resources resources;
	private File dest;
	private boolean fullBuild = true;
	private boolean clean = true;
	private boolean debug = false;

	public synchronized void add(ResourceCollection c) {
		if (c == null) {
			return;
		}
		resources = (resources == null) ? new Resources() : resources;
		resources.add(c);
	}

	public synchronized void setFile(File file) {
		add(new FileResource(file));
	}

	/**
	 * Add a FileSet.
	 * 
	 * @param fs
	 *            the <code>FileSet</code> to add.
	 */
	public synchronized void add(FileSet fs) {
		add((ResourceCollection) fs);
	}

	@SuppressWarnings("unchecked")
	public void execute() throws BuildException {
		IProgressMonitor monitor = null;
		Hashtable references = getProject().getReferences();
		if (references != null)
			monitor = (IProgressMonitor) references
					.get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
		IFileSystem localFS = EFS.getLocalFileSystem();
		if (dest == null)
			dest = new File(".");
		final IFileStore outputPath = localFS.getStore(new Path(getDest()
				.getAbsolutePath()));

		LocationContext context = new LocationContext(outputPath);

		if (resources.isFilesystemOnly()) {
			for (Iterator iter = resources.iterator(); iter.hasNext();) {
				FileResource res = (FileResource) iter.next();
				if (res.isExists()) {
					context.addSourcePath(localFS.getStore(new Path(res
							.getFile().getAbsolutePath())), outputPath);
					log("Resource added: " + res.getFile());
				} else
					log("Resource doesn't not exist: " + res.getFile(),
							Project.MSG_ERR);
			}
		} else
			throw new BuildException("Only filesystem resources supported.");
		log("Output is: " + outputPath.toString());
		int mode = 0;
		if (clean)
			mode |= ICompilationDirector.CLEAN;
		if (fullBuild)
			mode |= ICompilationDirector.FULL_BUILD;
		if (debug)
			mode |= ICompilationDirector.DEBUG;
		log("Mode = " + mode + " :" + (clean ? " CLEAN" : "")
				+ (fullBuild ? " FULL_BUILD" : "") + (debug ? " DEBUG" : ""));
		try {
			IProblem[] problems = CompilationDirector.getInstance().compile(
					null, null, context, mode, monitor);
			for (IProblem problem : problems) {
				log(problem.getMessage(), Project.MSG_ERR);
			}
			if (problems.length != 0)
				throw new BuildException("Compilation failed.");
		} catch (CoreException e) {
			throw new BuildException(e);
		}

	}

	public void setFullBuild(boolean fullBuild) {
		this.fullBuild = fullBuild;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public File getDest() {
		return dest;
	}

	public void setDest(File dest) {
		this.dest = dest;
	}

	public void setSrc(File src) {
		add(new FileResource(src));
	}
}
