// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class UserFunction extends Obj implements CallInExistingFrame {

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
	
	public String toString() {
		return "<UserFunction lineno=" + this.func.body.next.lineno + ">";
	}
	
	public boolean callInFrame(Frame parentFrame, Obj[] args) {
		Frame frame = new Frame(parentFrame);
		parentFrame.setFrame = frame;
		
		parentFrame.setInstr = callInExistingFrame(frame, args);
		return true;
	}
	
	public Instr callInExistingFrame(Frame frame, Obj[] args) {
		Obj[] finalArgs = args;
		if(args.length != expectedCount) {
			// TODO: implement defaults
			finalArgs = new Obj[expectedCount];
			
			throw new ScriptError(ScriptError.NotImplementedError, "Bad number of arguments (not supported)");
		}
		func.prepareFrame(frame, finalArgs);
		return func.body;
	}
	
	public Obj call(Obj[] args) {
		return Frame.call(this, args);
	}

	public Obj getObjectAttr(Obj instance) {
		return new BoundMethod(instance);
	}
	
	class BoundMethod extends Obj {
		final Obj instance;
		public BoundMethod(Obj instance) {
			this.instance = instance;
		}

		public Obj call(Obj[] args) {
			Obj[] allArgs = new Obj[args.length + 1];
			allArgs[0] = instance;
			System.arraycopy(args, 0, allArgs, 1, args.length);
			return UserFunction.this.call(allArgs);
		}
		
		public boolean callInFrame(Frame parentFrame, Obj[] args) {
			Frame frame = new Frame(parentFrame);
			parentFrame.setFrame = frame;
			
			callInExistingFrame(frame, args);
			
			parentFrame.setInstr = func.body;
			return true;
		}
		
		public void callInExistingFrame(Frame frame, Obj[] args) {
			Obj[] allArgs = new Obj[args.length + 1];
			allArgs[0] = this.instance;
			System.arraycopy(args, 0, allArgs, 1, args.length);
			
			func.prepareFrame(frame, allArgs);
		}
		
		public String toString() {
			return "<bound method of " + instance + ", function " + UserFunction.this + ">";
		}
	}
}
