package org.yong3.offheap.collection.array;

import org.yong3.offheap.serialization.MemoryAllocator;
import org.yong3.offheap.serialization.MemoryAllocatorFactory;
import org.yong3.offheap.serialization.Serializer;
import org.yong3.offheap.serialization.SerializerFactory;

/**
 * A simple fixed length array impl.
 * 
 * @author yc
 * 
 * @param <T>
 */
public class SimpleArray<T> implements Array<T> {
	private Class<T> type;
	private long size;
	private MemoryAllocator allocator;
	private Serializer<T> serializer;

	private long idxAddress;
	private int idxScale;

	public SimpleArray(Class<T> cls, long size) {
		this.type = cls;
		this.size = size;
		allocator = MemoryAllocatorFactory.get();
		serializer = SerializerFactory.get(cls);

		idxScale = allocator.fixSizeOf(long.class);
		long idxSize = size * idxScale;
		idxAddress = allocator.allocate(idxSize);
		for (int i = 0; i < size; i++) {
			allocator.putLong(idxAddress + i * idxScale, -1);
		}
	}

	@Override
	public void set(long idx, T obj) {
		long addr = allocator.allocate(serializer.getOffheapSize(obj));
		serializer.write(addr, obj);
		allocator.putLong(idxAddress + idx * idxScale, addr);
	}

	@Override
	public T get(long idx) {
		long addr = allocator.getLong(idxAddress + idx * idxScale);
		if (addr == -1) {
			return null;
		} else {
			return serializer.read(addr, type);
		}
	}

	@Override
	public void destroy() {
		long addr;
		for (int i = 0; i < size; i++) {
			addr = allocator.getLong(idxAddress + i * idxScale);
			if(addr != -1) allocator.deallocate(addr);
		}
		allocator.deallocate(idxAddress);
	}

}
