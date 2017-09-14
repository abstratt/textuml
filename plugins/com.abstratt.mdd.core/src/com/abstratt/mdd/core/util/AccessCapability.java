package com.abstratt.mdd.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;

/** Matches the enumeration of same name in the mdd_extensions profile */
public enum AccessCapability {
	Create(null, null, false), 
	Delete("Create", null, true), 
	List("Read", "Extent", false), 
	Read(null, null, true), 
	Update("Read", null, true), 
	StaticCall(null, null, false), 
	Call(null, null, true),
	// helper value to represent the existence of a constraint that gives no capabilities
	None(null, null, false);
	private Collection<String> implied;
	private Collection<String> aliases;
	private boolean instance;
	private AccessCapability(String implied, String alias, boolean instance) {
		this.implied = implied == null ? Collections.emptyList() : Arrays.asList(implied);
		this.aliases = alias == null ? Collections.emptyList() : Arrays.asList(alias);
		this.instance = instance;
	}
	
	public static Set<AccessCapability> impliedByName(String name) {
		String normalizedName = WordUtils.capitalizeFully(name).replace(" ", "");
		Stream<AccessCapability> selected = Arrays.stream(values()).filter(it -> (it.name().equals(normalizedName) || it.aliases.contains(normalizedName)));
		Set<AccessCapability> allImplied = selected.map(it -> it.getImplied(true)).reduce(new LinkedHashSet<AccessCapability>(), 
			(a, b) -> { a.addAll(b); return a; }
		);
		return allImplied;
	}
	public static AccessCapability byName(String name) {
		String normalizedName = WordUtils.capitalizeFully(name).replace(" ", "");
		Stream<AccessCapability> selected = Arrays.stream(values()).filter(it -> (it.name().equals(normalizedName) || it.aliases.contains(normalizedName)));
		return selected.findAny().orElseThrow(() -> new IllegalArgumentException(name));
	}
	
	public boolean isInstance() {
		return instance;
	}
	
	public Set<AccessCapability> getImplied(boolean includeSelf) {
		Set<AccessCapability> allImplied = new LinkedHashSet<>();
		if (includeSelf)
			allImplied.add(this);
		implied.forEach(it -> allImplied.addAll(valueOf(it).getImplied(true)));
		return allImplied;
	}
	public static Set<AccessCapability> getDenied(Set<AccessCapability> allowed) {
		Set<AccessCapability> result = new LinkedHashSet<>(Arrays.asList(values()));
		result.removeAll(allowed);
		return result;
	}
}