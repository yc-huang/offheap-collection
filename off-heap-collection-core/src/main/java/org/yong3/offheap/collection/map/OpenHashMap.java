package org.yong3.offheap.collection.map;

import org.yong3.offheap.serialization.MemoryAllocator;
import org.yong3.offheap.serialization.MemoryAllocatorFactory;
import org.yong3.offheap.serialization.OffheapUtils;
import org.yong3.offheap.serialization.Serializer;
import org.yong3.offheap.serialization.SerializerFactory;

import sun.misc.Unsafe;

/**
 * A simple open address linear probe hashmap.The current impl is not thread-safe, and remove is not implemented.
 * @author yc
 *
 * @param <K>
 * @param <V>
 */

public class OpenHashMap<K, V> implements Map<K, V>{
	public static final byte BYTE_0x80 = (byte) 0x80;

	private static Unsafe unsafe = OffheapUtils.unsafe;
	private Serializer<K> keySerializer;
	private Serializer<V> valueSerializer;

	final long idxAddr;
	final int idxSize;
	final int addressSize;
	final int slotSize;
	final int maxProbe;

	public long get_check = 0;
	public long get = 0;

	public long put_check = 0;
	public long put_find = 0;
	public long put = 0;

	Class<K> keyCls;
	Class<V> valueCls;

	class Address {
		long slotAddr;
		long recordAddr;
		int probe;
	}

	Address currentAddr = new Address();
	MemoryAllocator allocator;

	public OpenHashMap(Class<K> keyCls, Class<V> valueCls, int size) {
		allocator = MemoryAllocatorFactory.get();
		addressSize = unsafe.addressSize();
		System.out.println("Address size:" + addressSize);
		slotSize = 1 + addressSize; // add 1 byte for short hash/digest
		int _idxSize = nextPower2(size, 65536);

		if (_idxSize * 1.0 / size < 1.5) {
			_idxSize *= 2;
		}

		idxSize = _idxSize - 1;

		System.out.printf("idx size:%,d, slot size:%d\n", idxSize, slotSize);
		maxProbe = idxSize >> 10;
		System.out.printf("max probe size:%,d\n", maxProbe);

		idxAddr = allocator.allocate(idxSize * slotSize);
		this.keyCls = keyCls;
		this.valueCls = valueCls;
		keySerializer = SerializerFactory.get(keyCls);
		valueSerializer = SerializerFactory.get(valueCls);
		
	}

	public void put(final K key, final V value) {
		if (key == null || value == null) {
			throw new NullPointerException("key or value should not be null.");
		}

		// System.err.println("put " + key);

		long start = System.nanoTime();
		Address addr = checkExist(key);
		//System.err.println(addr.slotAddr);
		put_check += System.nanoTime() - start;

		start = System.nanoTime();
		int keySize = keySerializer.getOffheapSize(key);
		int valueSize = valueSerializer.getOffheapSize(value);
		long recordAddr = allocator.allocate(keySize + valueSize);
		keySerializer.write(recordAddr, key);
		K rkey = keySerializer.read(recordAddr, keyCls);
		if(!key.equals(rkey)){
			System.err.println("key serializing error:expected " + key + " but get " + rkey);
		}
		valueSerializer.write(recordAddr + keySize, value);
		V rvalue = valueSerializer.read(recordAddr + keySize, valueCls);
		if(!value.equals(rvalue)){
			System.err.println("value serializing error: expected " + value + " but get " + rvalue);
		}

		if (addr.recordAddr != -1) {
			// exist, replace
			allocator.deallocate(addr.recordAddr);
			allocator.putLong(addr.slotAddr + 1, recordAddr);
		} else {
			// insert to current slot
			byte shortHash = (byte) ((key.hashCode()/971 & 0xff) | BYTE_0x80);
			if(shortHash == 0) shortHash = 1;
			allocator.putByte(addr.slotAddr, shortHash);
			allocator.putLong(addr.slotAddr + 1, recordAddr);
		}
		put += System.nanoTime() - start;
	}

	public V remove(K key) {
		if (key == null) {
			throw new NullPointerException("key or value should not be null.");
		}
		V value = null;
		Address addr = checkExist(key);
		if (addr.recordAddr != -1) {
			// exist, remove
			value = valueSerializer.read(addr.recordAddr + keySerializer.getOffheapSize(key), valueCls);
			// clear slot
			allocator.putByte(addr.slotAddr, (byte) 0);//TODO; this is not safe, and might cause some key un-accessable
			// remove record
			allocator.deallocate(addr.recordAddr);
		}
		return value;
	}

	public boolean contains(K key) {
		if (key == null) {
			throw new NullPointerException("key should not be null.");
		}
		return checkExist(key).recordAddr != -1;
	}

	// return the record address if found, else return -1.
	private Address checkExist(K key) {
		int slot = key.hashCode() & idxSize;

		long slotAddr = idxAddr + slot * slotSize;
		byte shortHash = (byte) ((key.hashCode()/971 & 0xff) | BYTE_0x80);
		if(shortHash == 0) shortHash = 1;

		int probe = 0;
		byte shortHashStored;
		boolean empty;
		long recordAddr;
		while (probe < maxProbe) {
			shortHashStored = allocator.getByte(slotAddr);
			empty = (shortHashStored & BYTE_0x80) == 0;
			if (empty) {
				currentAddr.recordAddr = -1;
				currentAddr.slotAddr = slotAddr;
				// currentAddr.probe = probe;
				//System.err.println(slotAddr);
				return currentAddr;
			} else if (shortHashStored == shortHash) {
				// might found
				recordAddr = allocator.getLong(slotAddr + 1);
				K storedKey = keySerializer.read(recordAddr, keyCls);
				if (key.equals(storedKey)) {
					currentAddr.slotAddr = slotAddr;
					currentAddr.recordAddr = recordAddr;
					return currentAddr;
				}
			}
			// probe
			slotAddr += slotSize;
			probe++;
		}

		throw new RuntimeException("max probe exceed for key " + key);

	}

	public V get(K key) {
		if (key == null) {
			throw new NullPointerException("key should not be null.");
		}

		long start = System.nanoTime();

		long recordAddr = checkExist(key).recordAddr;

		put_check += System.nanoTime() - start;

		if (recordAddr == -1)
			return null;
		else {
			start = System.nanoTime();
			V v = valueSerializer.read(recordAddr + keySerializer.getOffheapSize(key), valueCls);
			get += System.nanoTime() - start;
			return v;
		}
	}

	public static int nextPower2(int n, int min) {
		if (n < min)
			return min;
		if ((n & (n - 1)) == 0)
			return n;
		int i = min;
		while (i < n) {
			i *= 2;
			if (i <= 0)
				return 1 << 30;
		}
		return i;
	}

	@Override
	public void destroy() {
		long slotAddr, recordAddr;
		byte shortHashStored;
		boolean empty;
		for(int slot = 0; slot < idxSize; slot++){
			slotAddr = idxAddr + slot * slotSize;
			shortHashStored = allocator.getByte(slotAddr);
			empty = (shortHashStored & BYTE_0x80) == 0;
			if (!empty) {
				recordAddr = allocator.getLong(slotAddr + 1);
				allocator.deallocate(recordAddr);
			}
		}
		allocator.deallocate(idxAddr);
	}

}
