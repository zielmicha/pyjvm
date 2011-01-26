package pyjvm;

public class FunctionConst extends SObject {

	private final int loadArgs;
	public final Instr body;
	private final int pushArgsStart;
	private final int pushArgsCount;

	public FunctionConst(int argcount, int loadargs, int varcount, Instr body) {
		this.loadArgs = loadargs;
		this.pushArgsStart = varcount + loadargs;
		this.pushArgsCount = argcount - loadargs;
		this.body = body;
	}
	
	public Function createInstance(Frame frame) {
		return new Function(loadArgs, pushArgsStart, pushArgsCount, body, frame.globals);
	}
	
}
