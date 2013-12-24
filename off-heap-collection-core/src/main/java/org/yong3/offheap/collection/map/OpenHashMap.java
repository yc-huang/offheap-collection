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

	long idxAddr;
	int idxSize;
	int addressSize;
	int slotSize;
	int maxProbe;

//	public long get_check = 0;
//	public long get = 0;

//	public long put_check = 0;
//	public long put_find = 0;
//	public long put = 0;

	Class<K> keyCls;
	Class<V> valueCls;

//	class Address {
//		long slotAddr;
//		long recordAddr;
//		int probe;
//	}

	long[] currentAddr = new long[]{-1, 0};//first one is recordAddr, second one is slotAddr
	MemoryAllocator allocator;

	public OpenHashMap(Class<K> keyCls, Class<V> valueCls, int size){
		int _idxSize = getIndexSize(size);
		int _maxProbe = _idxSize >> 10;
		init(keyCls, valueCls, _idxSize, Math.min(Math.max(_maxProbe, 128), 16 * 1024));
	}
	
	public OpenHashMap(Class<K> keyCls, Class<V> valueCls, int size, int maxProbe) {
		init(keyCls, valueCls, getIndexSize(size), maxProbe);
	}
	
	private void init(Class<K> keyCls, Class<V> valueCls, int idxSize, int maxProbe) {
		this.idxSize = idxSize;
		this.maxProbe = Math.max(maxProbe, 0);//at least be 0
		
		allocator = MemoryAllocatorFactory.get();
		addressSize = unsafe.addressSize();
		System.out.println("Address size:" + addressSize);
		slotSize = 1 + addressSize; // add 1 byte for short hash/digest

		System.out.printf("idx size:%,d, slot size:%d\n", idxSize, slotSize);
		
		System.out.printf("max probe size:%,d\n", maxProbe);

		idxAddr = allocator.allocate(idxSize * slotSize);
		this.keyCls = keyCls;
		this.valueCls = valueCls;
		keySerializer = SerializerFactory.get(keyCls);
		valueSerializer = SerializerFactory.get(valueCls);
		
	}
	
	private int getIndexSize(int size){
		int _idxSize = nextPower2(size, 65536);

		if (_idxSize * 1.0 / size < 1.5) {
			_idxSize *= 2;
		}

		return _idxSize - 1;
	}

	public void put(final K key, final V value) {
		check();
		if (key == null || value == null) {
			throw new NullPointerException("key or value should not be null.");
		}

		long[] addr = checkExist(key);

		int keySize = keySerializer.getOffheapSize(key);
		int valueSize = valueSerializer.getOffheapSize(value);
		long recordAddr = allocator.allocate(keySize + valueSize);
		keySerializer.write(recordAddr, key);
		valueSerializer.write(recordAddr + keySize, value);

		if (addr[0] != -1) {
			// exist, replace
			allocator.deallocate(addr[0]);
			allocator.putLong(addr[1] + 1, recordAddr);
		} else {
			// insert to current slot
			byte shortHash = (byte) ((getHash2(key) & 0xff) | BYTE_0x80);
			if(shortHash == 0) shortHash = 1;
			allocator.putByte(addr[1], shortHash);
			allocator.putLong(addr[1] + 1, recordAddr);
		}
	}

	public V remove(K key) {
		check();
		if (key == null) {
			throw new NullPointerException("key or value should not be null.");
		}
		V value = null;
		long[] addr = checkExist(key);
		if (addr[0] != -1) {
			// exist, remove
			value = valueSerializer.read(addr[0] + keySerializer.getOffheapSize(key), valueCls);
			
			// remove record
			allocator.deallocate(addr[0]);
			// clear slot
			allocator.putByte(addr[1], (byte) 0);//TODO; this is not safe, and might cause some key un-accessable
			
		}
		return value;
	}

	public boolean contains(K key) {
		check();
		if (key == null) {
			throw new NullPointerException("key should not be null.");
		}
		return checkExist(key)[0] != -1;
	}

	// return the record address if found, else return -1.
	private long[] checkExist(K key) {
		int slot = getHash1(key) & idxSize;

		long slotAddr = idxAddr + slot * slotSize;
		byte shortHash = (byte) ((getHash2(key) & 0xff) | BYTE_0x80);
		if(shortHash == 0) shortHash = 1;

		int probe = 0;
		byte shortHashStored;
		boolean empty;
		long recordAddr;
		while (probe < maxProbe) {
			shortHashStored = allocator.getByte(slotAddr);
			empty = (shortHashStored & BYTE_0x80) == 0;
			if (empty) {
				currentAddr[0] = -1;
				currentAddr[1] = slotAddr;							
				// currentAddr.probe = probe;
				return currentAddr;
			} else if (shortHashStored == shortHash) {
				// might found
				recordAddr = allocator.getLong(slotAddr + 1);
				K storedKey = keySerializer.read(recordAddr, keyCls);
				if (key.equals(storedKey)) {
					currentAddr[0] = recordAddr;
					currentAddr[1] = slotAddr;
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
		check();
		if (key == null) {
			throw new NullPointerException("key should not be null.");
		}

		long recordAddr = checkExist(key)[0];

		if (recordAddr == -1)
			return null;
		else {
			V v = valueSerializer.read(recordAddr + keySerializer.getOffheapSize(key), valueCls);
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
		check();
		long slotAddr, recordAddr;
		byte shortHashStored;
		boolean empty;
		
		long tmpIdxAddr = idxAddr;
		idxAddr = -1;
		
		for(int slot = 0; slot < idxSize; slot++){
			slotAddr = tmpIdxAddr + slot * slotSize;
			shortHashStored = allocator.getByte(slotAddr);
			empty = (shortHashStored & BYTE_0x80) == 0;
			if (!empty) {
				recordAddr = allocator.getLong(slotAddr + 1);
				allocator.deallocate(recordAddr);
			}
		}
		allocator.deallocate(tmpIdxAddr);
	}

	protected int getHash1(K key){
		return key.hashCode();
	}
	
	protected int getHash2(K key){
		return key.hashCode() / 971;
	}
	
	private void check(){
		if(idxAddr == -1) throw new java.lang.IllegalStateException(this.getClass().getName() + " is already destroyed.");
	}
}
