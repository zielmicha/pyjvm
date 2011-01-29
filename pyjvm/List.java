// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class List extends Obj {
	private Obj[] array;
	private int length;
	
	/** hack */
	private List(boolean dontInit) {}
	
	public List() {
		this(8);
	}
	
	public static List fromArrayUnsafe(Obj[] arr) {
		List list = new List(true);
		list.array = arr;
		list.length = arr.length;
		return list;
	}
	
	public List(int initialCapacity) {
		this.array = new Obj[initialCapacity];
		this.length = 0;
	}
	
	public final int length() {
		return this.length;
	}
	
	public final void append(Obj obj) {
		if(length + 1 == array.length)
			reallocate();
		array[length] = obj;
		length++;
	}

	private void reallocate() {
		Obj[] src = array;
		Obj[] dst = new Obj[src.length * 2];
		System.arraycopy(src, 0, dst, 0, src.length);
		this.array = dst;
	}
	
	public Obj getItem(int index) {
		if(index >= length)
			throw new ScriptError(ScriptError.IndexError, "bad index");
		return array[index];
	}
	
	public Obj getItem(Obj key) {
		return getItem(key.intValue());
	}
	
	public int[] toInternedStringArray() {
		int[] dest = new int[length];
		for(int i=0; i<length; i++) {
			dest[i] = array[i].stringValue().intern();
		}
		return dest;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		for(int i=0; i<length; i++) {
			b.append(array[i]);
			b.append(", ");
		}
		b.append("]");
		return b.toString();
	}
	
	// TODO: hashCode
	// TODO: equals
}
