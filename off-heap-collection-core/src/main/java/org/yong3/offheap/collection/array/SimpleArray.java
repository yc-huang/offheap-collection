package org.yong3.offheap.collection.array;

import org.yong3.offheap.serialization.MemoryAllocator;
import org.yong3.offheap.serialization.MemoryAllocatorFactory;
import org.yong3.offheap.serialization.OffheapUtils;
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
	private final static MemoryAllocator allocator = MemoryAllocatorFactory
			.get();
	private final static int idxScale = allocator.fixSizeOf(long.class);

	private Serializer<T> serializer;
	private Class<T> type;
	private long size;
	private long idxAddress;

	public SimpleArray(Class<T> cls, long size) {
		this.type = cls;
		this.size = size;
		serializer = SerializerFactory.get(cls);

		long idxSize = size * idxScale;
		idxAddress = allocator.allocate(idxSize);
		// for (int i = 0; i < size; i++) {
		// allocator.putLong(idxAddress + i * idxScale, 0);
		// }
		OffheapUtils.unsafe.setMemory(idxAddress, idxSize, (byte) 0);
	}

	@Override
	public void set(long idx, T obj) {
		check(idx);
		long addr = allocator.allocate(serializer.getOffheapSize(obj));
		serializer.write(addr, obj);
		allocator.putLong(idxAddress + idx * idxScale, addr);
	}

	@Override
	public T get(long idx) {
		check(idx);
		long addr = allocator.getLong(idxAddress + idx * idxScale);
		if (addr == 0) {
			return null;
		} else {
			return serializer.read(addr, type);
		}
	}
	

	@Override
	public void remove(long idx) {
		check(idx);
		long addr = allocator.getLong(idxAddress + idx * idxScale);
		if (addr != 0) {
			allocator.deallocate(addr);
			allocator.putLong(idxAddress + idx * idxScale, 0);
		}
	}

	@Override
	public void destroy() {
		check(0);
		long addr;
		for (int i = 0; i < size; i++) {
			addr = allocator.getLong(idxAddress + i * idxScale);
			if (addr != 0)
				allocator.deallocate(addr);
		}
		allocator.deallocate(idxAddress);
		idxAddress = -1;
	}

	
	private void check(long idx){
		if(idx < 0 || idx >= size) throw new ArrayIndexOutOfBoundsException("Array index out of range: " + idx);
		if(idxAddress == -1) throw new java.lang.IllegalStateException(this.getClass().getName() + " is already destroyed.");
	}

}
