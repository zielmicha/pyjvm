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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import pyjvm.*;
import java.lang.reflect.Method;

public class JInstance extends Obj { //!export
	final Object obj;

	public JInstance(Object obj) {
		this.obj = obj;
	}

	public Type getType() {
		return InstanceClass.instance;
	}

	public String toString() {
		return "<wrapper " + obj + " type=" + obj.getClass() + ">";
	}

	public Obj getAttr(int i) {
		String name = SString.unintern(i).toString();

		Method[] methods = obj.getClass().getMethods();
		int count = 0;
		for(Method m:methods)
			if(m.getName().equals(name))
				count ++;

		Method[] good_methods = new Method[count];
		Class[][] defs = new Class[count][];

		int j = 0;
		for(Method m:methods) {
			if(m.getName().equals(name)) {
				good_methods[j] = m;
				defs[j] = m.getParameterTypes();
				// Java is stupid; if class is protected it doesn't
				// allow access to _any_ methods via reflection
				m.setAccessible(true);
				j++;
			}
		}

		if(good_methods.length == 0)
			throw new ScriptError(ScriptError.AttributeError, obj.getClass() + " has no method named " + name);

		return new JMethod(obj, name, good_methods, defs);
	}

	public static final class InstanceClass extends Type {
		private InstanceClass() {}

		public static final InstanceClass instance = new InstanceClass();

		public final Obj getEntry(int name) {
			return null;
		}
	}

	public static final class JMethod extends Obj {
		Object obj;
		String name;
		Method[] methods;
		Class[][] defs;
		public static Type type = new Type.EmptyType("JMethodType");

		public JMethod(Object obj, String name, Method[] methods, Class[][] defs) {
			this.obj = obj;
			this.name = name;
			this.methods = methods;
			this.defs = defs;
		}

		public Obj call(Obj[] args) {
			int match = Reflect.match(defs, args);
			Object[] jargs = Reflect.argsToJava(defs[match], args);
			Object result;
			try {
				result = methods[match].invoke(obj, jargs);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return Reflect.fromJava(result);
		}

		public Type getType() {
			return type;
		}
	}
}
