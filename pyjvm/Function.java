package pyjvm;

public final class Function extends SObject {
	
	private final int loadArgs;
	public final Instr body;
	private final int pushArgsStart;
	private final int pushArgsCount;
	private StringDict globals;
	
	public Function(int loadArgs, int pushArgsStart, int pushArgsCount,
			Instr body, StringDict globals) {
		this.loadArgs = loadArgs;
		this.pushArgsStart = pushArgsStart;
		this.pushArgsCount = pushArgsCount;
		this.body = body;
		this.globals = globals;
	}
	
	public final void prepareFrame(Frame frame, SObject[] args) {
		frame.globals = this.globals;
		for(int i=0; i<loadArgs; i++)
			frame.reg[i] = args[i];
		for(int i=0; i<pushArgsCount; i++)
			frame.reg[i] = args[i + pushArgsStart];
	}
	
}
