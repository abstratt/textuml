/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.cli;

import static com.abstratt.mdd.frontend.cli.Helper.buildStatus;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.frontend.core.FrontEnd;
import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.internal.core.CompilationDirector;
import com.abstratt.pluginutils.LogUtils;

public class CompilationDirectorCLI {

	public void doIt(IRepository repository) throws CoreException {
		final String inputPathAsString = System.getProperty("args.input");
		Assert.isLegal(inputPathAsString != null, "No input specified");
		final String outputPathAsString = System.getProperty("args.output", inputPathAsString);
		compile(repository, inputPathAsString, outputPathAsString);
	}

	private void compile(IRepository repository, final String inputPathAsString, final String outputPathAsString)
			throws CoreException {
		IFileSystem localFS = EFS.getLocalFileSystem();
		final IFileStore outputPath = localFS.getStore(new Path(outputPathAsString));
		LocationContext context = new LocationContext(outputPath);
		final IFileStore sourcePath = localFS.getStore(new Path(inputPathAsString));
		if (!sourcePath.fetchInfo().exists()) {
			System.err.println(sourcePath + " does not exist");
			System.exit(1);
		}
		context.addSourcePath(sourcePath, outputPath);
		int mode = ICompilationDirector.CLEAN | ICompilationDirector.FULL_BUILD;
		if (Boolean.getBoolean("args.debug"))
			mode |= ICompilationDirector.DEBUG;
		IProblem[] problems = CompilationDirector.getInstance().compile(null, repository, context, mode, null);
		if (problems.length > 0) {
			MultiStatus parent = new MultiStatus(FrontEnd.PLUGIN_ID, IStatus.OK, "Problems occurred", null);
			for (int i = 0; i < problems.length; i++) {
				String message = problems[i].toString();
				parent.add(buildStatus(message, null));
			}
			LogUtils.log(parent);
		} else
			LogUtils.logInfo(FrontEnd.PLUGIN_ID, "Done", null);
	}
}
