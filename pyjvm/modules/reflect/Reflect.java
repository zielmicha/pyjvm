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
package pyjvm.modules.reflect;

import pyjvm.*;

public class Reflect { //!export modules.reflect.Reflect
	public static final StringDict dict;
	
	public static Obj get_class(Obj name) { //!export
		try {
			String n = name.stringValue().toString();
			return new JClass(Class.forName(n));
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	static int match(Class[][] types, Obj[] args) {
		throw new ScriptError(ScriptError.TypeError, "Failed to match parameters " + List.fromArrayUnsafe(args));
	}
	
	static Object[] translate(Class[] types, Obj[] args) {
		return null;
	}
	
	static Obj convert(Object obj) {
		return null;
	}
	
	static {
		dict = ReflectClass.dict;
		dict.put("__name__", SString.fromJavaString("reflect"));
	}
}
