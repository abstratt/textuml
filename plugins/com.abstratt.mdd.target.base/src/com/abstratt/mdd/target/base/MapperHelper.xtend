package com.abstratt.mdd.target.base

import java.io.IOException
import java.io.InputStream

class MapperHelper {
	def static InputStream getTemplateContents(Class<?> anchor, String path) {
        val templatePath = '''/templates/«path»'''
        val templateContents = anchor.getResourceAsStream(templatePath)
        if (templateContents == null)
            throw new IOException("Resource not found: " + templatePath)
        return templateContents
    } 
}