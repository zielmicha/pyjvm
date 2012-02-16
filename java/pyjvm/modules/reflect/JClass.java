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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pyjvm.*;

public class JClass extends NativeObj { //!export modules.reflect.JClass
	Class java;
	
	public JClass(Class java) {
		this.java = java;
	}
	
	public Obj create(Obj[] args) { //!export direct
		Constructor[] constructors = this.java.getConstructors();
		Class[][] defs = new Class[constructors.length][];
		for(int i=0; i<constructors.length; i++)
			defs[i] = constructors[i].getParameterTypes();
		int match = Reflect.match(defs, args);
		Object[] converted = Reflect.argsToJava(defs[match], args);
		
		Object result;
		try {
			result = constructors[match].newInstance(converted);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		return Reflect.fromJava(result);
	}
	
	public Type getType() {
		return JClassClass.instance;
	}
}
