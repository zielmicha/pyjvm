package pyjvm;

public class SObject {
	public final boolean equals(Object other) {
		if(!(other instanceof SObject))
			return false;
		return equals((SObject)other);
	}
	
	public final boolean equals(SObject other) {
		SBool isEqual = isEqual((SObject)other);
		if(isEqual == null) {
			SBool isEqual2 = other.isEqual(this);
			if(isEqual2 == null)
				return false;
			else
				return isEqual2 == SBool.True;
		}
		return isEqual == SBool.True;
	}
	
	public SBool isEqual(SObject other) {
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
		throw new ScriptError(ScriptError.TypeError, "Object unconvertable to bool", this);
	}
	
	public String toString() {
		return "<" + getClass().getSimpleName() + " at " + System.identityHashCode(this) + ">";
	}

	// repr
	
	public SObject repr() {
		return new SString(toString());
	}
	
	public static String repr(SObject object) {
		if(object == null)
			return "null";
		SObject repr = object.repr();
		if(repr == NotImplemented)
			return object.toString();
		return repr.toString();
	}
	
	public static final NotImplemented NotImplemented = pyjvm.NotImplemented.NotImplemented; 
	public static final None None = pyjvm.None.None;
	
	// operators
	
	public SObject add(SObject other) {
		return NotImplemented;
	}

	public SObject add(Frame frame, SObject b) {
		return this.add(b);
	}
	
	public SObject radd(SObject other) {
		return NotImplemented;
	}

	public SObject radd(Frame frame, SObject a) {
		return this.radd(a);
	}
	//
	public SObject sub(Frame frame, SObject b) {
		return sub(b);
	}
	
	public SObject sub(SObject b) {
		return NotImplemented;
	}
	
	public SObject rsub(Frame frame, SObject b) {
		return rsub(b);
	}
	
	public SObject rsub(SObject b) {
		return NotImplemented;
	}
	//
	public SObject mul(Frame frame, SObject b) {
		return mul(b);
	}
	
	public SObject mul(SObject b) {
		return NotImplemented;
	}
	
	public SObject rmul(Frame frame, SObject b) {
		return rmul(b);
	}
	
	public SObject rmul(SObject b) {
		return NotImplemented;
	}
	//
	public SObject floordiv(Frame frame, SObject b) {
		return floordiv(b);
	}
	
	public SObject floordiv(SObject b) {
		return NotImplemented;
	}
	
	public SObject rfloordiv(Frame frame, SObject b) {
		return rfloordiv(b);
	}
	
	public SObject rfloordiv(SObject b) {
		return NotImplemented;
	}

	public SObject isEqual(Frame frame, SObject b) {
		return isEqual(b);
	}

	public int length() {
		throw new ScriptError(ScriptError.TypeError, "object without length");
	}

	public UserFunction getUserFunction() {
		return null;
	}

	public SObject call(SObject[] args) {
		throw new ScriptError(ScriptError.TypeError, "Object not callable", this);
	}
}
