package com.abstratt.mdd.core;

import java.net.URI;

import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.resman.ResourceKey;

public class RepositoryKey implements ResourceKey {
	private static final long serialVersionUID = 1L;
	private URI uri;

	public URI getUri() {
		return uri;
	}

	public RepositoryKey(URI uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString() {
		return uri.toString();
	}
	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepositoryKey other = (RepositoryKey) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public static RepositoryKey from(org.eclipse.emf.common.util.URI repoURI) {
		return new RepositoryKey(MDDUtil.fromEMFToJava(repoURI));
	}
}
