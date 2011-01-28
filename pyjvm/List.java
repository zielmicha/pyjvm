package pyjvm;

public class List extends Obj {
	private Obj[] array;
	private int length;
	
	public List() {
		this(8);
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

	public int[] toInternedStringArray() {
		int[] dest = new int[length];
		for(int i=0; i<length; i++) {
			dest[i] = array[i].stringValue().intern();
		}
		return dest;
	}
	
	// TODO: hashCode
	// TODO: equals
}
