// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Module extends Obj {
	public final StringDict dict;

	public Module() {
		dict = new StringDict();
	}

	public void done(Obj doc) {
		// TODO: useless	
	}
	
	public Obj getAttr(int name) {
		return dict.get(name);
	}
	
	public void setAttr(int name, Obj val) {
		dict.put(name, val);
	}
}
