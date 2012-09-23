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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Obj implements Iterable<Obj> {

	public final boolean equals(Object other) {
		if(!(other instanceof Obj))
			return false;
		return equals((Obj)other);
	}

	public final boolean equals(Obj other) {
		SBool isEqual = isEqual((Obj)other);
		if(isEqual == null) {
			SBool isEqual2 = other.isEqual(this);
			if(isEqual2 == null)
				return false;
			else
				return isEqual2 == SBool.True;
		}
		return isEqual == SBool.True;
	}

	public SBool isEqual(Obj other) {
		/**
		 * Test for equality.
		 * Returns null where Python __eq__ would return NotImplemented.
		 */
		if(other == this)
			return SBool.True;
		return null;
	}

	public int intValue() {
		throw new ScriptError(ScriptError.TypeError, "Object unconvertable to int", this);
	}

	public SString stringValue() {
		throw new ScriptError(ScriptError.TypeError, "Object unconvertable to string", this);
	}

	public void dump() {
		System.err.println(this.repr());
	}

	public void dump(String name) {
		System.err.println(name + " " + this.repr());
	}

	public boolean boolValue() {
		return true;
	}

	public String toString() {
		return "<" + getClass().getSimpleName() + " at " + System.identityHashCode(this) + ">";
	}

	// repr

	public Obj repr() {
		try {
			return new SString(toString());
		} catch(StackOverflowError err) {
			return new SString("<recursive>");
		}
	}

	public Obj str() {
		return new SString(toString());
	}

	public static String repr(Obj object) {
		if(object == null)
			return "null";
		Obj repr = object.repr();
		if(repr == NotImplemented)
			return object.toString();
		return repr.stringValue().toString();
	}

	public static String typeRepr(Obj object) {
		try {
			Type t = object.getType();
			return repr(t);
		} catch(ScriptError ex) {
			return "<no type>";
		}
	}

	public static final NotImplemented NotImplemented = pyjvm.NotImplemented.NotImplemented;
	public static final None None = pyjvm.None.None;

	// operators

	public Obj add(Obj other) {
		return NotImplemented;
	}

	public Obj add(Frame frame, Obj b) {
		return this.iadd(b);
	}

	public Obj iadd(Obj other) {
		return this.add(other);
	}

	public Obj iadd(Frame frame, Obj b) {
		return this.iadd(b);
	}

	public Obj radd(Obj other) {
		return NotImplemented;
	}

	public Obj radd(Frame frame, Obj a) {
		return this.radd(a);
	}
	//
	public Obj sub(Frame frame, Obj b) {
		return sub(b);
	}

	public Obj sub(Obj b) {
		return NotImplemented;
	}

	public Obj isub(Frame frame, Obj b) {
		return isub(b);
	}

	public Obj isub(Obj b) {
		return this.sub(b);
	}

	public Obj rsub(Frame frame, Obj b) {
		return rsub(b);
	}

	public Obj rsub(Obj b) {
		return NotImplemented;
	}
	//
	public Obj mul(Frame frame, Obj b) {
		return mul(b);
	}

	public Obj mul(Obj b) {
		return NotImplemented;
	}

	public Obj rmul(Frame frame, Obj b) {
		return rmul(b);
	}

	public Obj rmul(Obj b) {
		return NotImplemented;
	}
	//
	public Obj floordiv(Frame frame, Obj b) {
		return floordiv(b);
	}

	public Obj floordiv(Obj b) {
		return NotImplemented;
	}

	public Obj rfloordiv(Frame frame, Obj b) {
		return rfloordiv(b);
	}

	public Obj rfloordiv(Obj b) {
		return NotImplemented;
	}
	//
	public Obj div(Frame frame, Obj b) {
		return div(b);
	}

	public Obj div(Obj b) {
		return NotImplemented;
	}

	public Obj rdiv(Frame frame, Obj b) {
		return rdiv(b);
	}

	public Obj rdiv(Obj b) {
		return NotImplemented;
	}
	//
	public Obj truediv(Frame frame, Obj b) {
		return truediv(b);
	}

	public Obj truediv(Obj b) {
		return NotImplemented;
	}

	public Obj rtruediv(Frame frame, Obj b) {
		return rtruediv(b);
	}

	public Obj rtruediv(Obj b) {
		return NotImplemented;
	}
	//
	public Obj mod(Frame frame, Obj b) {
		return mod(b);
	}

	public Obj mod(Obj b) {
		return NotImplemented;
	}

	public Obj rmod(Frame frame, Obj b) {
		return rmod(b);
	}

	public Obj rmod(Obj b) {
		return NotImplemented;
	}

	public Obj isEqual(Frame frame, Obj b) {
		return isEqual(b);
	}

	public int length() {
		throw new ScriptError(ScriptError.TypeError, "Object without length");
	}

	public UserFunction getUserFunction() {
		return null;
	}

	public Obj call(Obj[] args) {
		throw new ScriptError(ScriptError.TypeError, "Object not callable", this);
	}

	public Obj getItem(Frame frame, Obj key) {
		return getItem(key);
	}

	public Obj getItem(Obj key) {
		throw new ScriptError(ScriptError.TypeError, "Object not subscriptable", this);
	}

	public void setItem(Frame frame, Obj index, Obj item) {
		setItem(index, item);
	}

	public void setItem(Obj index, Obj item) {
		throw new ScriptError(ScriptError.TypeError, "Object not subscriptable", this);
	}

	public Obj getIter(Frame frame) {
		return getIter();
	}

	public Obj getIter() {
		throw new ScriptError(ScriptError.TypeError, "Object not iterable", this);
	}

	public Obj next(Frame frame) {
		return next();
	}

	public Obj next() {
		throw new ScriptError(ScriptError.TypeError, "Object is not an iterator", this);
	}

	public Obj getAttr(int name) {
		throw new ScriptError(ScriptError.TypeError, "Object without attributes", this);
	}

	public void delAttr(int name) {
		throw new ScriptError(ScriptError.TypeError, "Object without deletable attributes", this);
	}

	public Obj getObjectAttr(Obj instance) {
		// throw new ScriptError(ScriptError.TypeError, "Object without property protocol", this);
		return this;
	}

	public void setObjectAttr(Obj instance, Obj value) {
		throw new ScriptError(ScriptError.AttributeError, "Attribute declared in class", this);
	}

	public Obj getClassAttr() {
		// throw new ScriptError(ScriptError.TypeError, "Object without property protocol", this);
		return this;
	}

	public boolean callInFrame(Frame frame, Obj[] args) {
		return false;
	}

	public boolean callInFrame(Frame frame, Obj[] args, int[] kwargs) {
		return false;
	}

	public void setAttr(int name, Obj value) {
		throw new ScriptError(ScriptError.TypeError, "Object without writeable attributes", this);
	}

	public Type getType() {
		throw new ScriptError(ScriptError.InternalError, "Object without type (???)", this);
	}

	public static boolean isInstance(Obj type, Obj object) {
		Type t = (Type)type;
		Type objectType = object.getType();

		if(t == objectType)
			return true;

		Type[] bases = t.getBases();
		for(int i=0; i<bases.length; i++) {
			if(isInstance(bases[i], object))
				return true;
		}
		return false;
	}

	public Obj call(Obj[] args, int[] kwargs) {
		if(kwargs.length == 0)
			return call(args);
		throw new ScriptError(ScriptError.TypeError, "Object takes no keywords arguments or is not callable");
	}

	// comparators

	public Obj compare(Obj b) {
		return NotImplemented;
	}

	public Obj lessThan(Frame frame, Obj b) {
		return lessThan(b);
	}

	public Obj lessThan(Obj b) {
		Obj cmp = compare(b);
		if(cmp == NotImplemented) return NotImplemented;
		return cmp.intValue() < 0? SBool.True: SBool.False;
	}

	public Obj lessOrEqual(Frame frame, Obj b) {
		return lessOrEqual(b);
	}

	public Obj lessOrEqual(Obj b) {
		Obj cmp = compare(b);
		if(cmp == NotImplemented) return NotImplemented;
		return cmp.intValue() <= 0? SBool.True: SBool.False;
	}

	public Obj greaterThan(Frame frame, Obj b) {
		return greaterThan(b);
	}

	public Obj greaterThan(Obj b) {
		Obj cmp = compare(b);
		if(cmp == NotImplemented) return NotImplemented;
		return cmp.intValue() > 0? SBool.True: SBool.False;
	}

	public Obj greaterOrEqual(Frame frame, Obj b) {
		return greaterOrEqual(b);
	}

	public Obj greaterOrEqual(Obj b) {
		Obj cmp = compare(b);
		if(cmp == NotImplemented) return NotImplemented;
		return cmp.intValue() >= 0? SBool.True: SBool.False;
	}

	public Obj getSlice(Frame frame, Obj lower, Obj upper) {
		return getSlice(lower, upper);
	}

	public Obj getSlice(Obj lower, Obj upper) {
		throw new ScriptError(ScriptError.TypeError, "Object is not sliceable");
	}

	public Obj unarySub() {
		throw new ScriptError(ScriptError.TypeError, "bad operand type for unary -", this);
	}

	public Obj unarySub(Frame frame) {
		return unarySub();
	}

	public boolean contains(Frame frame, Obj b) {
		return contains(b);
	}

	public boolean contains(Obj b) {
		Obj iter = this.getIter();
		Obj current;
		while((current=iter.next()) != null) {
			if(current.equals(b))
				return true;
		}
		return false;
	}

	public Iterator<Obj> iterator() {
		final Obj iter = getIter();
		return new Iterator<Obj>() {
			boolean nextFetched = false;
			Obj next;

			void fetch() {
				if(!nextFetched) {
					next = iter.next();
					nextFetched = true;
				}
			}

			public boolean hasNext() {
				fetch();
				return next != null;
			}

			public Obj next() {
				fetch();
				if(next == null) throw new NoSuchElementException();
				nextFetched = false;
				return next;
			}

			public void remove() {
				throw new ScriptError(ScriptError.NotImplementedError, "removing not implemented in iterator");
			}

		};
	}

	public static final class IndexableIter extends Obj {
		private int pos;
		private int length;
		private Obj list;

		public IndexableIter(Obj list) {
			this.pos = 0;
			this.length = list.length();
			this.list = list;
		}

		public Obj next() {
			if(pos == length)
				return null;
			return list.getItem(SInt.get(pos++));
		}
	}
}
