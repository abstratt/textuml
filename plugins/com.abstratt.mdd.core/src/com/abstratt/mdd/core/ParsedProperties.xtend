package com.abstratt.mdd.core

import java.util.Map
import java.util.Properties

class ParsedProperties {
	
	Properties repositoryProperties
	
	new(Properties properties) {
		this.repositoryProperties = properties
	}
	
	def Map<String, String> filterProperties(String prefix) {
		val result = newLinkedHashMap()
		val keys = repositoryProperties.keySet().map[toString].filter[startsWith(prefix)]
		keys.forEach [
			result.put(it.replace(prefix, ''), repositoryProperties.get(it).toString())
		]
		return result
	}
}