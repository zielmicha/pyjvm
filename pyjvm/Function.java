// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Function extends Obj {
	
	private final int[] loadArgs;
	public final Instr body;
	private final int pushArgsStart;
	private final int pushArgsCount;
	private StringDict globals;
	private StringDict builtins;
	
	public Function(int[] loadArgs, int pushArgsStart, int pushArgsCount,
			Instr body, StringDict globals, StringDict builtins) {
		this.loadArgs = loadArgs;
		this.pushArgsStart = pushArgsStart;
		this.pushArgsCount = pushArgsCount;
		this.body = body;
		this.globals = globals;
		this.builtins = builtins;
	}
	
	public final void prepareFrame(Frame frame, Obj[] args) {
		frame.globals = this.globals;
		frame.builtins = this.builtins;
		
		for(int i=0; i<loadArgs.length; i++)
			frame.reg[loadArgs[i]] = args[i];
		for(int i=0; i<pushArgsCount; i++)
			frame.reg[i] = args[i + pushArgsStart];
	}
	
}
