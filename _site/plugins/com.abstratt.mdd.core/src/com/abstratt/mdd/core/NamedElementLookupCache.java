package com.abstratt.mdd.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.NamedElement;

public class NamedElementLookupCache {
    private Map<String, NamedElement> cached = new HashMap<String, NamedElement>();
    IRepository repository;
    
    public NamedElementLookupCache(IRepository repository) {
    	this.repository = repository;
	}

	public <T extends NamedElement> T find(String qualifiedName, EClass eClass) {
		T found = findInCache(qualifiedName, eClass);
    	if (found != null)
    		return found;
    	found = repository.<T> findNamedElement(qualifiedName , eClass, null);
    	if (found != null)
    		addToCache(qualifiedName, found);
		return (T) found;
    }

	public <T extends NamedElement> T addToCache(String qualifiedName, T found) {
		return (T) cached.put(qualifiedName, found);
	}
	
	public <T extends NamedElement> T findInCache(String qualifiedName, EClass eClass) {
		return (T) cached.get(qualifiedName);
	}
}
