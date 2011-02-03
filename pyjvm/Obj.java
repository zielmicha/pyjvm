// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public class Obj {
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
		return false;
	}
	
	public String toString() {
		return "<" + getClass().getSimpleName() + " at " + System.identityHashCode(this) + ">";
	}

	// repr
	
	public Obj repr() {
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
		return this.add(b);
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

}
