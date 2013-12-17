package org.yong3.offheap.collection.map;

public interface Map<K, V> {

	public void put(K key, V value);
	
	public V get(K key);
	
	public V remove(K key);
	
	public boolean contains(K key);
	
	public void destroy();
}
