package com.abstratt.mdd.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;


enum Target { Instance, Class }

/** Matches the enumeration of same name in the mdd_extensions profile */
public enum AccessCapability {
	
	Create(null, null, EnumSet.of(Target.Class)), 
	Delete("Create", null, EnumSet.of(Target.Instance)), 
	List("Read", "Extent", EnumSet.of(Target.Class)), 
	Read(null, null, EnumSet.of(Target.Instance)), 
	Update("Read", null, EnumSet.of(Target.Instance)), 
	Call(null, null, EnumSet.of(Target.Instance, Target.Class)),
	// helper value to represent the existence of a constraint that gives no capabilities
	None(null, null, EnumSet.noneOf(Target.class));
	private Collection<String> implied;
	private Collection<String> aliases;
	private EnumSet<Target> targets;
	private AccessCapability(String implied, String alias, EnumSet<Target> targets) {
		this.implied = implied == null ? Collections.emptyList() : Arrays.asList(implied);
		this.aliases = alias == null ? Collections.emptyList() : Arrays.asList(alias);
		this.targets = targets;
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
		return targets.contains(Target.Instance);
	}
	
	public boolean isClass() {
		return targets.contains(Target.Class);
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