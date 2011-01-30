// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public abstract class Method extends Obj {
	public abstract Obj callMethod(Obj self, Obj[] args);
	
	public Obj getObjectAttr(Obj instance) {
		return new BoundMethod(instance);
	}
	
	private class BoundMethod extends Obj {
		private final Obj instance;
		public BoundMethod(Obj instance) {
			this.instance = instance;
		}

		public Obj call(Obj[] args) {
			return callMethod(instance, args);
		}
	}
}
