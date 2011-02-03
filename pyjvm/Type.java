// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public abstract class Type extends Obj {
	public static final class EmptyType extends Type {
		private String name;
		
		public EmptyType(String name) {
			this.name = name;
		}
		
		public String toString() {
			return "<type '" + this.name + "'>";
		}
		
		public Obj getEntry(int name) {
			throw new ScriptError(ScriptError.TypeError, "Class without attributes");
		}

	}

	public abstract Obj getEntry(int name);
	
	public Type[] getBases() {
		if(this == NativeObjClass.instance)
			return new Type[0];
		else
			return DEFUALT_BASES;
	}
	
	public static Type[] DEFUALT_BASES = new Type[]{
		NativeObjClass.instance
	};
}
