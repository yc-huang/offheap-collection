package org.yong3.offheap.collection.array;

public interface Array<T> {
	
	public void set(long idx, T obj);
	
	public T get(long idx);
}
