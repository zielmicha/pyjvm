// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public abstract class NativeObj extends Obj { //!export NativeObj	
	public Obj getAttr(int name) {
		Type clazz = getType();
		Obj entry = clazz.getEntry(name);
		return entry.getObjectAttr(this);
	}
	
	
	// int length() //!export - __len__
}
