package org.yong3.offheap.collection.array;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleArrayTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		long size = 10 * 1024 * 1024;
		long start = System.currentTimeMillis();
		SimpleArray<String> stringArray = new SimpleArray<String>(String.class, size);
		System.err.println("init cost:" + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		
		for(int i = 0; i < size; i++){
			assertNull(stringArray.get(i));
		}
		
		for(int i = 0; i < size; i++){
			stringArray.set(i, "" + i);
		}
		
		for(int i = 0; i < size; i++){
			assertEquals("" + i, stringArray.get(i));
		}
		
		for(int i = 0; i < size; i++){
			stringArray.set(i, "" + (i+1));
		}
		
		for(int i = 0; i < size; i++){
			assertEquals("" + (i+1), stringArray.get(i));
		}
		
		for(int i = 0; i < size; i++){
			stringArray.remove(i);
		}
		
		for(int i = 0; i < size; i++){
			assertEquals(null, stringArray.get(i));
		}
		
		System.err.printf("cost:%d", System.currentTimeMillis() - start);
		
//		SimpleArray<SimpleArray<String>> wrapper = new SimpleArray<SimpleArray<String>>((Class<SimpleArray<String>>) stringArray.getClass(), 1);
//		wrapper.set(0, stringArray);
//		
//		SimpleArray<String> read = wrapper.get(0);
//		for(int i = 0; i < size; i++){
//			assertEquals("" + (i+1), read.get(i));
//		}
		
		stringArray.destroy();
	}

}
