// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public class FunctionConst extends Obj {

	private final int[] loadArgs;
	public final Instr body;
	private final int pushArgsStart;
	private final int pushArgsCount;

	public FunctionConst(int argcount, int[] loadargs, int varcount, Instr body) {
		this.loadArgs = loadargs;
		this.pushArgsStart = varcount + loadargs.length;
		this.pushArgsCount = argcount - loadargs.length;
		this.body = body;
	}
	
	public Function createInstance(Frame frame) {
		return new Function(loadArgs, pushArgsStart, pushArgsCount, body, frame.globals, frame.builtins);
	}
	
}
