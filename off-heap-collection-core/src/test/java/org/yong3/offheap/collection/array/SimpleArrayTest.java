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
		SimpleArray<String> stringArray = new SimpleArray<String>(String.class, size);
		
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
	}

}
