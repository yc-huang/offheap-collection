package org.yong3.offheap.collection.map;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NestedOpenHashMapTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testRawStringKey() {
		int MAP_SIZE = 1 * 1024 * 1024;		
		
		NestedOpenHashMap<String, String> map = new NestedOpenHashMap<String, String>(
				String.class, String.class, MAP_SIZE);

	
		for (int i = 0; i < MAP_SIZE; i++) {
			assertFalse(map.contains(new String("" + i)));
		}
	
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			map.put(key, key);
		}
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			assertTrue(map.contains(key));
		}
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
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
		}
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			assertEquals(v, map.get(k));
		}
		map.destroy();
	}

	
	@Test
	public void testRawStringKeyPerf() {
		int MAP_SIZE = 10 * 1024 * 1024;		
		
		//warmup
		//this.testRawStringKey();
		
		NestedOpenHashMap<String, String> map = new NestedOpenHashMap<String, String>(
				String.class, String.class, MAP_SIZE);

		
		long start = System.nanoTime();
		for (int i = 0; i < MAP_SIZE; i++) {
			map.contains(new String("" + i));
		}
		System.err.printf("existence check cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			map.put(key, key);
		}

		System.err.printf("put cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			map.contains(key);
		}
		System.err.printf("existence check cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String key = "" + i;
			map.get(key);
		}
		
		System.err.printf("get cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();

		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.put(k, v);
		}
		
		System.err.printf("update cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();


		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.contains(k);
		}
		System.err.printf("existence check cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();
		
		for (int i = 0; i < MAP_SIZE; i++) {
			String k = "" + i;
			String v = "" + (i + 1);
			map.get(k);
		}
		System.err.printf("get cost:%d ns/op\n", (System.nanoTime() - start)/MAP_SIZE);

		start = System.nanoTime();
		map.destroy();
		System.err.printf("destroy cost:%d us\n", (System.nanoTime() - start)/1000);
	}

	

}
