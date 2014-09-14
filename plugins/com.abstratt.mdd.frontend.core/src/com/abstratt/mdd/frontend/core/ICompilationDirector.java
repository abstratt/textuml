/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/ 
package com.abstratt.mdd.frontend.core;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.frontend.core.spi.ISourceAnalyzer;

/**
 * The compilation director orchestrates the compilation process. It can compile
 * source files written in multiple languages. The end result of the compilation is 
 * that the source files found in the source locations are parsed into UML models,
 * persisted into the appropriate output location.
 * <p>
 * Internally, the compilation director will build a repository from the existing files found
 * under the model and output paths. Compilers participating 
 * in the compilation process will generate model elements into this repository.
 * </p>
 */
public interface ICompilationDirector {
	public static final String MDD_FILE_EXTENSION = "mdd"; 
	public static final int CLEAN = 2;
	public static final int DEBUG = 4;
	public static final int FULL_BUILD = 1;

	/**
	 * Compiles the given source files, or all source files found in the context 
	 * if <code>null</code> is provided.
	 * 
	 * @param toBuild source files to build
	 * @param repository a repository, or <code>null</code>
	 * @param context 
	 * @param mode
	 * @param monitor
	 * @return an array of problems (empty if none)
	 * @throws CoreException 
	 */
	public IProblem[] compile(IFileStore[] toCompile, IRepository repository, LocationContext context, int mode, IProgressMonitor monitor) throws CoreException;

	public String format(String filename, String toFormat) throws CoreException;
	public List<ISourceAnalyzer.SourceElement> analyze(String extension, String toAnalyze) throws CoreException;
}
