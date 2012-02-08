// Copyright (C) 2011 by Michal Zielinski
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


package pyjvm;

public class Tuple extends Obj { //!export
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
	
	public final int[] toInternedStringArray() {
		int[] array = new int[this.length()];
		for(int i=0; i<this.length(); i++) {
			array[i] = items[i].stringValue().intern();
		}
		return array;
	}
	
	public final int length() {
		return this.items.length;
	}
	
	public static final Tuple Empty = new Tuple(new Obj[0]);
	
	// TODO: hashCode
	
	public SBool isEqual(Obj obj) {
		if(obj instanceof Tuple) {
			Tuple t = (Tuple)obj;
			if(t.items.length != items.length)
				return SBool.False;
			for(int i=0; i<items.length; i++)
				if(! t.items[i].equals(items[i]) )
					return SBool.False;
			return SBool.True;
		} else {
			return null;
		}
	}
	
	public int hashCode() {
		int code = 0;
		for(int i=0; i<items.length; i++) {
			code += items[i].hashCode();
			code *= 13;
		}
		return code;
	}
	
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
	
	public Obj getIter() {
		return new TupleIter();
	}
	
	class TupleIter extends Obj {
		int pos = 0;
		
		public Obj next() {
			if(pos == items.length)
				return null;
			return items[pos++];
		}
	}
}
