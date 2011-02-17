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

public final class Builtins { //!export Builtins

	public static final Obj int_(Obj arg) { //!export
		return SInt.get(arg.intValue());
	}
	
	public static final int len(Obj arg) { //!export
		return arg.length();
	}
	
	public static final boolean isinstance(Obj obj, Obj type) { //!export
		if(type instanceof Tuple) {
			Obj tupleIter = type.getIter();
			Obj item;
			while((item=tupleIter.next()) != null) {
				if(!Obj.isInstance(item, obj))
					return false;
			}
			return true;
		}
		return Obj.isInstance(type, obj);
	}
	
	public static final Obj xrange(Obj[] args) { //!export direct
		int start = 0;
		int step = 1;
		int end = 0;
		if(args.length == 1) {
			end = args[0].intValue(); 
		} else if(args.length == 2) {
			start = args[0].intValue();
			end = args[1].intValue();
		} else if(args.length == 3) {
			start = args[0].intValue();
			end = args[1].intValue();
			step = args[2].intValue();
		} else {
			throw new ScriptError(ScriptError.TypeError, "requires 1-3 int arguments");
		}
		return new XRange(start, step, end);
	}
	
	public static final class XRange extends Obj {
		private final int start, step, end;
		
		public XRange(int start, int step, int end) {
			this.start = start;
			this.step = step;
			this.end = end;
		}

		public Obj getIter() {
			return new XRangeIter(start, step, end);
		}
	}

	public static final class XRangeIter extends Obj {
		private final int step, end;
		private int index;
		
		public XRangeIter(int start, int step, int end) {
			this.index = start;
			this.step = step;
			this.end = end;
		}

		public Obj next() {
			if(this.index >= this.end)
				return null;
			int val = this.index;
			this.index += this.step;
			return SInt.get(val);
		}
	}
	
	public static final StringDict dict;
	
	static {
		dict = BuiltinsClass.dict;
		
		for(int i=0; i<ScriptError.names.length; i++) {
			dict.put(ScriptError.names[i], ScriptError.excClasses[i]);
		}
		
		dict.put("str", SStringClass.instance);
	}
}
