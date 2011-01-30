// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class Builtins { //!export Builtins

	public static final Obj int_(Obj arg) { //!export
		return SInt.get(arg.intValue());
	}
	
	public static final int len(Obj arg) { //!export
		return arg.length();
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

}
