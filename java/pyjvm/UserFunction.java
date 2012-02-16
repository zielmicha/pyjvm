// Copyright (C) 2011 by Michal Zielinski
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


package pyjvm;

import java.util.Arrays;

public final class UserFunction extends Obj implements CallInExistingFrame {

	private final boolean varargs;
	private final boolean kwargs;
	private final Obj[] defaults;
	private final int[] argnames;
	private final Function func;
	private final int expectedCount;

	public UserFunction(Function func, Tuple defaults, boolean varargs, boolean kwargs,
			int[] argnames) {
		this.varargs = varargs;
		this.kwargs = kwargs;
		this.defaults = defaults.items;
		this.argnames = argnames;
		this.func = func;
		
		this.expectedCount = this.argnames.length;
	}
	
	public String toString() {
		int lineno = -1;
		if(this.func!=null && this.func.body!=null && this.func.body!=null && this.func.body.next!=null)
			lineno = this.func.body.next.lineno;
		return "<UserFunction lineno=" + lineno + ">";
	}
	
	public boolean callInFrame(Frame parentFrame, Obj[] args) {
		Frame frame = new Frame(parentFrame);
		parentFrame.setFrame = frame;
		
		parentFrame.setInstr = callInExistingFrame(frame, args);
		return true;
	}
	
	public boolean callInFrame(Frame parentFrame, Obj[] args, int[] kwargs) {
		Frame frame = new Frame(parentFrame);
		parentFrame.setFrame = frame;
		
		parentFrame.setInstr = callInExistingFrame(frame, args, kwargs);
		return true;
	}
	
	public Instr callInExistingFrame(Frame frame, Obj[] args) {
		Obj[] finalArgs = args;

		if(args.length != expectedCount) {
			// TODO: implement ...
			finalArgs = new Obj[expectedCount];
			
			int defaultsCount = expectedCount - args.length;
			int startDefaults = defaults.length - defaultsCount;
			
			if(defaultsCount > defaults.length || defaultsCount < 0)
				throw new ScriptError(ScriptError.TypeError, "expected " + expectedCount + " arguments (" + 
					defaults.length + " defaults), got " + args.length + " instead.");

			System.arraycopy(args, 0, finalArgs, 0, args.length);
			System.arraycopy(defaults, startDefaults, finalArgs, args.length, defaultsCount);
		}
		func.prepareFrame(frame, finalArgs);
		return func.body;
	}
	
	public Instr callInExistingFrame(Frame frame, Obj[] args, int[] kwargs) {
		Obj[] finalArgs = args;
		if(args.length != expectedCount || kwargs.length != 0) {
			finalArgs = new Obj[expectedCount];
			boolean[] usedArgs = new boolean[expectedCount];
			
			main:
			for(int i=0; i<kwargs.length; i++) {
				int kwargName = kwargs[i];
				for(int j=0; j<argnames.length; j++) {
					int argName = argnames[j];
					if(argName == kwargName) {
						usedArgs[j] = true;
						finalArgs[j] = args[args.length - kwargs.length + i];
						continue main;
					}
				}
				throw new ScriptError(ScriptError.TypeError, "got unexpected keyword argument " 
						+ SString.uninternQuiet(kwargName));
			}
			 
			int normalArgsCount = args.length - kwargs.length;
			for(int i=0; i<normalArgsCount; i++) {
				if(usedArgs[i])
					throw new ScriptError(ScriptError.TypeError, "got multiple values for keyword argument "
							+ SString.uninternQuiet(argnames[i]));
				usedArgs[i] = true;
				finalArgs[i] = args[i];
			}
			
			int firstWithDefault = expectedCount - defaults.length;
			for(int i=0; i<usedArgs.length; i++) {
				if(!usedArgs[i]) {
					if(i >= firstWithDefault)
						finalArgs[i] = defaults[i - firstWithDefault];
					else
						throw new ScriptError(ScriptError.TypeError, "got no value for keyword argument " + SString.uninternQuiet(argnames[i]));
				}
			}
			
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
			
			//func.prepareFrame(frame, allArgs);
			UserFunction.this.callInExistingFrame(frame, allArgs);
		}
		
		public String toString() {
			return "<bound method of " + instance + ", function " + UserFunction.this + ">";
		}
	}
}
