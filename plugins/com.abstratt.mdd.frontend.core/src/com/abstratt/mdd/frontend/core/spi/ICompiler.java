package com.abstratt.mdd.frontend.core.spi;

import java.io.Reader;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.Problem;

public interface ICompiler {
	/**
	 * @param source
	 *            the source contents to compile
	 * @param repository
	 *            the repository where to create the output
	 * @param refTracker
	 * @param problems
	 *            list where to add problems found (element type:
	 *            {@link Problem})
	 * @throws CoreException
	 */
	public void compile(Reader source, CompilationContext context) throws CoreException;

	/**
	 * Optional. 
	 * @param toParse
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public String findModelName(String toParse) throws UnsupportedOperationException;

	/**
	 * @param source
	 * @param destination
	 */
	public String format(String toFormat);
}
