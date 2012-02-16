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
