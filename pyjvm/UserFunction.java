package pyjvm;

public final class UserFunction extends Obj {

	private final boolean varargs;
	private final boolean kwargs;
	private final int defaults;
	private final int[] argnames;
	private final Function func;
	private final int expectedCount;

	public UserFunction(Function func, int defaults, boolean varargs, boolean kwargs,
			int[] argnames) {
		this.varargs = varargs;
		this.kwargs = kwargs;
		this.defaults = defaults;
		this.argnames = argnames;
		this.func = func;
		
		this.expectedCount = this.argnames.length;
	}

	public Instr callInFrame(Frame frame, Obj[] args) {
		Obj[] finalArgs = args;
		if(args.length != expectedCount) {
			finalArgs = new Obj[expectedCount];
			
			throw new ScriptError(ScriptError.NotImplementedError, "Bad number of arguments (not supported)");
		}
		func.prepareFrame(frame, finalArgs);
		return func.body;
	}
	
	public UserFunction getUserFunction() {
		return this;
	}
}
