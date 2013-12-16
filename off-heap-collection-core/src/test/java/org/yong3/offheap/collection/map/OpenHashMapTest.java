package org.yong3.offheap.collection.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.yong3.offheap.serialization.MemoryAllocator;
import org.yong3.offheap.serialization.MemoryAllocatorFactory;
import org.yong3.offheap.serialization.Serializer;
import org.yong3.offheap.serialization.SerializerFactory;
import org.yong3.offheap.serialization.serializer.StringSerializer;

import sun.misc.Hashing;

public class OpenHashMapTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRawStringKey() {
		MemoryAllocator allocator = MemoryAllocatorFactory.get();
		Serializer<String> keySerializer = SerializerFactory.get(String.class);
		Serializer<String> valueSerializer = SerializerFactory
				.get(String.class);
		assertTrue(valueSerializer.getClass() == StringSerializer.class);

		int MAP_SIZE = 1 * 1024 * 1024;
		OpenHashMap<String, String> map = new OpenHashMap<String, String>(
				String.class, String.class, MAP_SIZE);

		for (int i = 0; i < MAP_SIZE; i++) {
			assertFalse(map.contains(new String("" + i)));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			long addr = allocator.allocate(keySerializer.getOffheapSize(key));
			keySerializer.write(addr, key);
			assertEquals(key, keySerializer.read(addr, String.class));
			allocator.deallocate(addr);

			addr = allocator.allocate(valueSerializer.getOffheapSize(key));
			valueSerializer.write(addr, key);
			assertEquals(key, valueSerializer.read(addr, String.class));
			allocator.deallocate(addr);

			// assertTrue((key.shortHash() & (byte)0x80) != 0);
			map.put(key, key);
			try {
				assertTrue(map.contains(key));
				assertEquals(key, map.get(key));
			} catch (AssertionError e) {
				System.err.println(i);
				throw e;
			}
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			assertTrue(map.contains(key));
			assertEquals(key, map.get(key));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.put(k, v);
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			assertTrue(map.contains(k));
			assertEquals(v, map.get(k));
		}

	}

	@Test
	public void testRawStringKeyAgain() {
		MemoryAllocator allocator = MemoryAllocatorFactory.get();
		Serializer<String> keySerializer = SerializerFactory.get(String.class);
		Serializer<String> valueSerializer = SerializerFactory
				.get(String.class);
		assertTrue(valueSerializer.getClass() == StringSerializer.class);

		int MAP_SIZE = 1 * 1024 * 1024;
		OpenHashMap<String, String> map = new OpenHashMap<String, String>(
				String.class, String.class, MAP_SIZE);

		for (int i = 0; i < MAP_SIZE; i++) {
			assertFalse(map.contains(new String("" + i)));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			long addr = allocator.allocate(keySerializer.getOffheapSize(key));
			keySerializer.write(addr, key);
			assertEquals(key, keySerializer.read(addr, String.class));
			allocator.deallocate(addr);

			addr = allocator.allocate(valueSerializer.getOffheapSize(key));
			valueSerializer.write(addr, key);
			assertEquals(key, valueSerializer.read(addr, String.class));
			allocator.deallocate(addr);

			// assertTrue((key.shortHash() & (byte)0x80) != 0);
			map.put(key, key);
			try {
				assertTrue(map.contains(key));
				assertEquals(key, map.get(key));
			} catch (AssertionError e) {
				System.err.println(i);
				throw e;
			}
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			assertTrue(map.contains(key));
			assertEquals(key, map.get(key));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.put(k, v);
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			assertTrue(map.contains(k));
			assertEquals(v, map.get(k));
		}

	}

	@Test
	public void testStringKey() {
		MemoryAllocator allocator = MemoryAllocatorFactory.get();
		Serializer<StringHashable> keySerializer = SerializerFactory
				.get(StringHashable.class);
		Serializer<String> valueSerializer = SerializerFactory
				.get(String.class);
		assertTrue(valueSerializer.getClass() == StringSerializer.class);

		int MAP_SIZE = 10 * 1024 * 1024;
		OpenHashMap<StringHashable, String> map = new OpenHashMap<StringHashable, String>(
				StringHashable.class, String.class, MAP_SIZE);

		for (int i = 0; i < MAP_SIZE; i++) {
			assertFalse(map.contains(new StringHashable("" + i)));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String v = "" + i;
			StringHashable key = new StringHashable(v);
			long addr = allocator.allocate(keySerializer.getOffheapSize(key));
			keySerializer.write(addr, key);
			assertEquals(key, keySerializer.read(addr, StringHashable.class));
			allocator.deallocate(addr);

			addr = allocator.allocate(valueSerializer.getOffheapSize(v));
			valueSerializer.write(addr, v);
			assertEquals(v, valueSerializer.read(addr, String.class));
			allocator.deallocate(addr);

			// assertTrue((key.shortHash() & (byte)0x80) != 0);
			map.put(key, v);
			try {
				assertTrue(map.contains(key));
				assertEquals(v, map.get(key));
			} catch (AssertionError e) {
				System.err.println(i);
				throw e;
			}
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String v = "" + i;
			StringHashable key = new StringHashable(v);
			assertTrue(map.contains(key));
			assertEquals(v, map.get(key));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.put(new StringHashable(k), v);
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			StringHashable key = new StringHashable(k);
			assertTrue(map.contains(key));
			assertEquals(v, map.get(key));
		}

	}

	@Test
	public void testIntKey() {
		MemoryAllocator allocator = MemoryAllocatorFactory.get();
		Serializer<IntHashable> keySerializer = SerializerFactory
				.get(IntHashable.class);
		Serializer<Integer> valueSerializer = SerializerFactory
				.get(Integer.class);

		int MAP_SIZE = 10 * 1024 * 1024;
		OpenHashMap<IntHashable, Integer> map = new OpenHashMap<IntHashable, Integer>(
				IntHashable.class, Integer.class, MAP_SIZE);

		for (int i = 0; i < MAP_SIZE; i++) {
			assertFalse(map.contains(new IntHashable(i)));
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			IntHashable key = new IntHashable(i);
			long addr = allocator.allocate(keySerializer.getOffheapSize(key));
			keySerializer.write(addr, key);
			assertEquals(key, keySerializer.read(addr, IntHashable.class));
			allocator.deallocate(addr);

			addr = allocator.allocate(valueSerializer.getOffheapSize(i));
			valueSerializer.write(addr, i);
			assertEquals(i, valueSerializer.read(addr, Integer.class)
					.intValue());
			allocator.deallocate(addr);

			// assertTrue((key.shortHash() & (byte)0x80) != 0);
			map.put(key, i);
			try {
				assertTrue(map.contains(key));
				assertEquals(i, map.get(key).intValue());
			} catch (AssertionError e) {
				System.err.println(i);
				throw e;
			}
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			IntHashable key = new IntHashable(i);
			assertTrue(map.contains(key));
			assertEquals(i, map.get(key).intValue());
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			map.put(new IntHashable(i), i + 1);
		}

		for (int i = 0; i < MAP_SIZE; i++) {
			IntHashable key = new IntHashable(i);
			assertTrue(map.contains(key));
			assertEquals(i + 1, map.get(key).intValue());
		}

	}

	class IntHashable implements Hashable {
		int v;

		public IntHashable(int v) {
			this.v = v;
		}

		@Override
		public int longHash() {
			return v;
		}

		@Override
		public int shortHash() {
			return v / 123;
		}

		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof IntHashable
					&& ((IntHashable) o).v == this.v;
		}

		@Override
		public int hashCode() {
			return v;
		}

	}

	class StringHashable implements Hashable {
		char[] chars;

		public StringHashable(String data) {
			this.chars = data.toCharArray();
		}

		int shortHash, longHash;

		@Override
		public int longHash() {
			if (longHash == 0) {
				long hash = 2166136261L;
				if (chars != null) {
					int k;
					int len = chars.length;
					for (int i = 1; i < len; i += 2) {
						k = chars[i - 1] | chars[i] << 8;
						hash = (hash ^ k) * 16777619;
					}

					if ((len & 1) == 1) {
						hash = (hash ^ chars[len - 1]) * 16777619;
					}
				}
				longHash = (int) (hash & 0xffffffff);
			}
			return longHash;
		}

		@Override
		public int shortHash() {
			if (shortHash == 0) {
				int hash = 5381;
				if (chars != null) {
					int k;
					int len = chars.length;
					for (int i = 1; i < len; i += 2) {
						k = chars[i - 1] | chars[i] << 8;
						hash = ((hash << 5) + hash) + k;
					}

					if ((len & 1) == 1) {
						hash = ((hash << 5) + hash) + chars[len - 1];
					}
				}
				shortHash = hash;
			}
			return shortHash;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof StringHashable) {
				StringHashable other = (StringHashable) o;
				if (other.chars.length == this.chars.length) {
					for (int i = 0; i < this.chars.length; i++) {
						if (this.chars[i] != other.chars[i])
							return false;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return new String(chars);
		}

		@Override
		public int hashCode() {
			return Hashing.murmur3_32(chars);
			//return longHash();
		}
	}
}
