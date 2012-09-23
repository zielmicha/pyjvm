package pyjvm.modules.reflect;

import pyjvm.Obj;

public class ArrayWrapper extends Obj {

	private Object[] array;

	public ArrayWrapper(Object[] array) {
		this.array = array;
	}

	public int length() {
		return array.length;
	}

	public Obj getItem(Obj key) {
		return Reflect.fromJava(array[key.intValue()]);
	}

	public Obj getIter() {
		return new IndexableIter(this);
	}
}
