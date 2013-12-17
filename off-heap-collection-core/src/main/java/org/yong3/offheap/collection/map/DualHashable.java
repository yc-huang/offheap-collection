package org.yong3.offheap.collection.map;

public interface DualHashable {
	//the two hash method should use different hash impl
	public int longHash();
	public int shortHash();
}
