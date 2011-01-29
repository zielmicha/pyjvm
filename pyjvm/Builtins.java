// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Builtins { //!export Builtins
	public static final Obj int_(Obj arg) { //!export
		return SInt.get(arg.intValue());
	}
}
