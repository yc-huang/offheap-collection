package org.yong3.offheap.collection.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
		
		System.err.printf("cost:%d\n", System.currentTimeMillis() - start);
		
//		SimpleArray<SimpleArray<String>> wrapper = new SimpleArray<SimpleArray<String>>((Class<SimpleArray<String>>) stringArray.getClass(), 1);
//		wrapper.set(0, stringArray);
//		
//		SimpleArray<String> read = wrapper.get(0);
//		for(int i = 0; i < size; i++){
//			assertEquals("" + (i+1), read.get(i));
//		}
		
		stringArray.destroy();
	}

	
	@Test
	public void testRawStringPerf() {
		long size = 10 * 1024 * 1024;		
		
		//warmup
		//this.testRawStringKey();
		
		SimpleArray<String> stringArray = new SimpleArray<String>(String.class, size);

		
		long start = System.nanoTime();
		for (int i = 0; i < size; i++) {
			stringArray.get(i);
		}
		System.err.printf("get empty array cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		for (int i = 0; i < size; i++) {
			stringArray.set(i, String.valueOf(i));
		}

		System.err.printf("set cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		
		for (int i = 0; i < size; i++) {
			stringArray.get(i);
		}
		System.err.printf("get full array cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		
		for (int i = 0; i < size; i++) {
			stringArray.set(i, String.valueOf(i+1));
		}
		
		System.err.printf("update cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();

		for (int i = 0; i < size; i++) {
			stringArray.remove(i);
		}
		
		System.err.printf("remove cost:%d ns/op\n", (System.nanoTime() - start)/size);
		
		start = System.nanoTime();
		stringArray.destroy();
		System.err.printf("destroy cost:%d ns/op\n", (System.nanoTime() - start));
	}

	@Test
	public void testPojoPerf() {
		long size = 10 * 1024 * 1024;		
		
		//warmup
		//this.testRawStringKey();
		
		SimpleArray<Pojo> stringArray = new SimpleArray<Pojo>(Pojo.class, size);

		
		long start = System.nanoTime();
		for (int i = 0; i < size; i++) {
			stringArray.get(i);
		}
		System.err.printf("get empty array cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		for (int i = 0; i < size; i++) {
			stringArray.set(i, new Pojo(i, String.valueOf(i)));
		}

		System.err.printf("set cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		
		for (int i = 0; i < size; i++) {
			stringArray.get(i);
		}
		System.err.printf("get full array cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();
		
		for (int i = 0; i < size; i++) {
			stringArray.set(i, new Pojo(i, String.valueOf(i+1)));
		}
		
		System.err.printf("update cost:%d ns/op\n", (System.nanoTime() - start)/size);

		start = System.nanoTime();

		for (int i = 0; i < size; i++) {
			stringArray.remove(i);
		}
		
		System.err.printf("remove cost:%d ns/op\n", (System.nanoTime() - start)/size);
		
		start = System.nanoTime();
		stringArray.destroy();
		System.err.printf("destroy cost:%d ns/op\n", (System.nanoTime() - start));
	}
	
	class Pojo{
		int idx, dix2;
		String desc;
		
		Pojo(int i, String s){
			idx = i;
			desc = s;
		}
	}
}
