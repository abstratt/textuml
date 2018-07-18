package com.abstratt.mdd.target.base

class GeneratorUtils {
    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper) {
        items.generateMany(mapper, '\n')
    }

    def static <I> CharSequence generateMany(Iterable<I> items, (I)=>CharSequence mapper, String separator) {
        return items.generateMany(separator, mapper)
    }
    def static <I> CharSequence generateMany(Iterable<I> items, String separator, (I)=>CharSequence mapper) {
        return items.generateMany(true, separator, mapper)
    }
    def static <I> CharSequence generateMany(Iterable<I> items, boolean trim, String separator, (I)=>CharSequence mapper) {
    	val toString = if (trim) [ it.toString.trim ] else [ it.toString.trim ] 
        return items.map[toString.apply(mapper.apply(it))].join(separator)
    }
    
}