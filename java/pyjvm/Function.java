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
	
	public boolean callInFrame(Frame parentFrame, Obj[] args) {
		Frame frame = new Frame(parentFrame);
		parentFrame.setFrame = frame;
		
		prepareFrame(frame, args);
		parentFrame.setInstr = body;
		return true;
	}
}
