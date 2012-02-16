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
	
	public static final Obj chr(int val) { //!export
		if(val < 0 || val > 255) 
			throw new ScriptError(ScriptError.ValueError, "chr() args not in range(256)");
		byte b = (byte)val;
		return new SString(new byte[]{b});
	}
	
	public static final int ord(Obj val) { //!export
		SString s = val.stringValue();
		if(s.length() != 1)
			throw new ScriptError(ScriptError.ValueError, "ord() arg not string of length 1");
		return (int)s.bytes[0];
	}
	
	public static final Obj int_(Obj arg) { //!export
		return SInt.get(arg.intValue());
	}
	
	public static final int hash(Obj args) { //!export
		return args.hashCode();
	}
	
	public static final int len(Obj arg) { //!export
		return arg.length();
	}
	
	public static final Obj repr(Obj arg) { //!export
		return arg.repr();
	}
	
	public static final Obj string_dict() { //!export
		return new StringDict();
	}
	
	public static final Obj bytearray(int length) { //!export
		return new ByteArray(length);
	}
	
	public static final Obj type(Obj[] args) { //!export direct
		if(args.length == 1) {
			return args[0].getType();
		} else if(args.length == 3) {
			SString name = args[0].stringValue();
			Tuple bases = (Tuple)args[1];
			StringDict dict = new StringDict(args[2]);
			return UserType.create(name.intern(), bases, dict);
		} else {
			throw new ScriptError(ScriptError.TypeError, "requires 1 or 3 arguments");
		}
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
	
	public static final Obj max(Obj[] args) { //!export direct
		if(args.length == 0) {
			throw new ScriptError(ScriptError.TypeError, "max() requires at least 1 argument");
		}
		Obj iter;
		if(args.length == 1) {
			iter = args[0].getIter();
		} else {
			iter = List.fromArrayUnsafe(args).getIter();
		}
		Obj max = null;
		Obj current;
		
		while((current=iter.next()) != null) {
			if(max == null || current.compare(max).intValue() > 0)
				max = current;
		}
		
		if(max == null)
			throw new ScriptError(ScriptError.TypeError, "max() arg is an empty sequence");
		
		return max;
	}
	
	public static final Obj min(Obj[] args) { //!export direct
		if(args.length == 0) {
			throw new ScriptError(ScriptError.TypeError, "min() requires at least 1 argument");
		}
		Obj iter;
		if(args.length == 1) {
			iter = args[0].getIter();
		} else {
			iter = List.fromArrayUnsafe(args).getIter();
		}
		Obj max = null;
		Obj current;
		
		while((current=iter.next()) != null) {
			if(max == null || current.compare(max).intValue() < 0)
				max = current;
		}
		
		if(max == null)
			throw new ScriptError(ScriptError.TypeError, "min() arg is an empty sequence");
		
		return max;
	}
	
	public static final Obj range(Obj[] args) { //!export direct
		List l = new List();
		l.iadd(xrange(args));
		return l;
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
	
	public static void importBuiltins() {
		Module m = Importer.importModule("builtins");
		dict.put("map", m.dict.get("map"));
	}
	
	public static final StringDict dict;
	
	static {
		dict = BuiltinsClass.dict;
		
		for(int i=0; i<ScriptError.names.length; i++) {
			dict.put(ScriptError.names[i], ScriptError.excClasses[i]);
		}
		
		dict.put("str", SStringClass.instance);
		dict.put("list", ListClass.instance);
	}
}
