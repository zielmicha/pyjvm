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

public final class UserType extends Type {
	private final int name;
	private final Tuple bases;
	final StringDict dict;

	private UserType(int name, Tuple bases, StringDict dict) {
		this.name = name;
		this.bases = bases;
		this.dict = UserObjClass.dict.copy();
		this.dict.update(dict);
	}

	public static Obj create(int name, Tuple bases, StringDict dict) {
		return new UserType(name, bases, dict);
	}

	public Obj getEntry(int name) {
		return dict.get(name);
	}

	public Obj call(Obj[] args) {
		// TODO: call __new__
		UserObj instance = new UserObj(this);
		
		Obj init = instance.getAttr(StringConst.__init__);
		Obj returned = init.call(args);
		if(returned != None)
			throw new ScriptError(ScriptError.TypeError, "__init__() should return None, not " + SString.repr(returned));
		
		return instance;
	}
	
	public Obj getAttr(int name) {
		Obj classAttr = dict.get(name);
		return classAttr; // TODO: think how does it work in cpython (or check docs)
	}
	
	public void setAttr(int name, Obj value) {
		dict.put(name, value);
	}

	public SString getName() {
		return SString.uninternQuiet(name);
	}
	
}
