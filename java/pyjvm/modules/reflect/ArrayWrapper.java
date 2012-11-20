package pyjvm.modules.reflect;

import pyjvm.Obj;
import pyjvm.SFloat;

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

	public static final class ForFloat extends Obj {
		private float[] array;

		public ForFloat(float[] array) {
			this.array = array;
		}

		public int length() {
			return array.length;
		}

		public Obj getItem(Obj key) {
			return SFloat.get(array[key.intValue()]);
		}

		public Obj getIter() {
			return new IndexableIter(this);
		}
	}
}
