/*******************************************************************************
 * Copyright (c) 2009 Abstratt Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.standalone.protocols.platform;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;

/**
 * A URL stream handler for platform URLs.
 */
public class Handler extends URLStreamHandler {
	private static final String PLATFORM_PROTOCOL = "platform";

	@Override
	protected URLConnection openConnection(URL originalURL) throws IOException {
		Assert.isLegal(PLATFORM_PROTOCOL.equals(originalURL.getProtocol()), originalURL.toString());
		final Path originalPath = new Path(originalURL.getPath());
		final String resourcePath = originalPath.removeFirstSegments(2).toString();
		Enumeration<URL> resolvedURLs = getClass().getClassLoader().getResources(
				resourcePath);
		if (!resolvedURLs.hasMoreElements())
			throw new MalformedURLException(resourcePath);
		//TODO if multiple occurrences found, find the first seemingly defined under a bundle with the proper symbolic name
		return resolvedURLs.nextElement().openConnection();
	}
}
