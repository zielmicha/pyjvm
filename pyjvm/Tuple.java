package pyjvm;

public class Tuple extends Obj {
	public Obj[] items;
	
	public Tuple(Obj[] items) {
		this.items = items;
	}
	
	public final Obj get(int i) {
		return items[i];
	}
	
	public final int getInt(int i) {
		return items[i].intValue();
	}
	
	public final int[] toIntArray() {
		int[] array = new int[this.length()];
		for(int i=0; i<this.length(); i++) {
			array[i] = getInt(i);
		}
		return array;
	}
	
	public final int length() {
		return this.items.length;
	}
	
	public static final Tuple Empty = new Tuple(new Obj[0]);
	
	// TODO: isEqual
	// TODO: hashCode
	
	public final String toString() {
		StringBuilder builder = new StringBuilder("(");
		for(int i=0; i<items.length; i++) {
			builder.append(Obj.repr(items[i]) + ", ");
		}
		builder.append(")");
		return builder.toString();
	}

	public static String toString(Obj[] data) {
		return new Tuple(data).toString();
	}

	public static Tuple tuple1(Obj arg0) {
		return new Tuple(new Obj[]{arg0});
	}

	public static Obj create(int[] in) {
		// TODO: create type IntTuple
		Obj[] arr = new Obj[in.length];
		for(int i=0; i<in.length; i++)
			arr[i] = SInt.get(in[i]);
		return new Tuple(arr);
	}
}
