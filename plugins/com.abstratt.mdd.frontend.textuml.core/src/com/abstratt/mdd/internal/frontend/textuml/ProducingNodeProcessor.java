package com.abstratt.mdd.internal.frontend.textuml;

public interface ProducingNodeProcessor<P, T> extends NodeProcessor<T>{
	P processAndProduce(T node);
	default void process(T node) {
		// do nothing with product
		processAndProduce(node);
	}
}
