offheap-collection
==================

A Map/Array implement which stored at off-heap memory.

Do not use Object, Class... as the generic type, which will not work。
##Sample Usage:

####simple array:
    long size = 10 * 1024 * 1024;
    
    //init the array with specified size
    SimpleArray<String> stringArray = new SimpleArray<String>(String.class, size);
    
    //write to array by index
    for(int i = 0; i < size; i++){
		stringArray.set(i, "" + i);
	}
	
	//read from array
	for(int i = 0; i < size; i++){
		String s = stringArray.get(i);
	}
	
	//remove from array
	for(int i = 0; i < size; i++){
		stringArray.remove(i);
	}
	
	//de-allocate the array
	stringArray.destroy();
	
####simple linear-probe open-address hash map:
Causion: The current implementation of "remove" operation is not finished.

    int MAP_SIZE = 1 * 1024 * 1024;
    
    //init the map with expected max size
	OpenHashMap<String, String> map = new OpenHashMap<String, String>(
			String.class, String.class, MAP_SIZE);
			
	for (int i = 0; i < MAP_SIZE; i++) {
		String key = "" + i;
		String value = "" + i;
		
		//put a key
		map.put(key, value);
		
		//check existence
		boolean exist = map.contains(key);
		
		//get the value
		String v = map.get(key);
		
		//update the value
		map.put(key, value + i);
	}
	
	//destroy the map
	map.destroy();
	