package com.abstratt.mdd.frontend.core.spi;

import java.util.Properties;

import com.abstratt.mdd.core.IRepository;

public class CompilationContext implements Cloneable {
	private boolean debug;
	private IProblemTracker problemTracker;
	private IReferenceTracker referenceTracker;
	private IRepository repository;
	private Properties repositoryProperties;
	private String sourcePath;

	public CompilationContext(IReferenceTracker referenceTracker, IProblemTracker problemTracker, IRepository repository, String sourcePath, boolean debug) {
		this.referenceTracker = referenceTracker;
		this.problemTracker = problemTracker;
		this.repository = repository;
		this.repositoryProperties = repository.getProperties();
		this.debug = debug;
		this.sourcePath = sourcePath;
	}

	public Properties getRepositoryProperties() {
		return repositoryProperties;
	}

	public IProblemTracker getProblemTracker() {
		return problemTracker;
	}

	public IReferenceTracker getReferenceTracker() {
		return referenceTracker;
	}

	public IRepository getRepository() {
		return repository;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public String getSourcePath() {
		return sourcePath;
	}
	
	public CompilationContext newLocalContext(String path) {
		CompilationContext copy = (CompilationContext) this.clone();
		copy.sourcePath = path;
		return copy;
	}
	
	@Override
	protected Object clone()  {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
}
