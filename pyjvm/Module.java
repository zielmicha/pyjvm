// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Module extends Obj {
	private StringDict dict;

	public Module() {
		dict = new StringDict();
	}
	
	public static Module create(Instr mainInstr) {
		Frame frame = new Frame(null);
		frame.builtins = BuiltinsClass.dict;
		frame.module = new Module();
		frame.globals = frame.module.dict;
		
		Frame.execute(frame, mainInstr);
		
		return frame.module;
	}

	public void done(Obj doc) {
		// TODO: useless	
	}
	
	public Obj getAttr(int name) {
		return dict.get(name);
	}
}
