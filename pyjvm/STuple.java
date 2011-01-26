package pyjvm;

public class STuple extends SObject {
	public SObject[] items;
	
	public STuple(SObject[] items) {
		this.items = items;
	}
	
	public final SObject get(int i) {
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
	
	public static final STuple Empty = new STuple(new SObject[0]);
	
	// TODO: isEqual
	// TODO: hashCode
	
	public final String toString() {
		StringBuilder builder = new StringBuilder("(");
		for(int i=0; i<items.length; i++) {
			builder.append(SObject.repr(items[i]) + ", ");
		}
		builder.append(")");
		return builder.toString();
	}

	public static String toString(SObject[] data) {
		return new STuple(data).toString();
	}

	public static STuple tuple1(SObject arg0) {
		return new STuple(new SObject[]{arg0});
	}
}
