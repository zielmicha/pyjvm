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

public final class List extends NativeObj { //!export List
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
	
	public final void append(Obj obj) { //!export
		if(length == array.length)
			reallocate();
		array[length] = obj;
		length++;
	}

	private void reallocate() {
		Obj[] src = array;
		Obj[] dst = new Obj[src.length==0?1:src.length * 2];
		System.arraycopy(src, 0, dst, 0, src.length);
		this.array = dst;
	}
	
	public Obj getItem(int index) {
		int nindex = index;
		if(index < 0)
			nindex = length + index;
		if(nindex >= length)
			throw new ScriptError(ScriptError.IndexError, "bad index " + index);
		return array[nindex];
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
			b.append(Obj.repr(array[i]));
			b.append(", ");
		}
		b.append("]");
		return b.toString();
	}
	
	public Obj getIter() {
		return new ListIterator();
	}
	
	public class ListIterator extends Obj {
		private int index = 0;
		
		public Obj next() {
			if(index == length)
				return null;
			return array[index++];
		}
	}

	public Type getType() {
		return ListClass.instance;
	}
	
	public Obj iadd(Obj other) {
		Obj iter = other.getIter();
		Obj current;
		while((current=iter.next()) != null) {
			append(current);
		}
		return this;
	}
	
	public Obj getSlice(Obj lower, Obj upper) {
		int len = this.length();
		int l = (lower == None)? 0 : lower.intValue();
		int u = (upper == None)? len : upper.intValue();
		if(l < 0) {
			l = len + l;
			if(l < 0) l = 0;
		} else if(l >= len) {
			l = len;
		}
		if(u < 0) {
			u = len + u;
			if(u < 0) u = 0;
		} else if(u >= len) {
			u = len;
		}
		//System.err.println("len=" + len + "; creating range " + l + " to " + u + ", from " + this);
		List list = new List();
		for(int i=l; i < u; i++) {
			list.append(array[i]);
		}
		//System.err.println("result: " + list);
		return list;
	}
	
	public static Obj construct(Obj[] args) { //!export direct <new>
		List l = new List();
		if(args.length == 1) {
			l.iadd(args[0]);
		} else if(args.length != 0) {
			throw new ScriptError(ScriptError.TypeError, "int() takes at most 1 argument");
		}
		return l;
	}
	
	public SBool isEqual(Obj other) {
		if(other instanceof List) {
			List l = (List)other;
			if(l.length != length)
				return SBool.False;
			for(int i=0; i<length; i++) 
				if(! l.array[i].equals(array[i]))
					return SBool.False;
			return SBool.True;
		}
		return null;
	}
	
	// TODO: hashCode
}
