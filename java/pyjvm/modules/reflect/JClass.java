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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import pyjvm.*;

public class JClass extends Obj {
	public Class java;
	static final int str_new = SString.intern("new");
	
	public JClass(Class java) {
		this.java = java;
	}

	public Obj getAttr(int name) {
		if(name == str_new)
			return new Obj() {
				public Obj call(Obj[] args) {
					return create(args);
				}
			};
		
		Obj result = getStaticField(SString.unintern(name).toString());
		if(result == null) {
			return getStaticMethod(SString.unintern(name).toString());
		} else {
			return result;
		}
	}
	
	public Obj create(Obj[] args) {
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
		return new Type.EmptyType("JClassType");
	}

	private Obj getStaticMethod(String name) {
		Method[] methods = java.getMethods();
		int count = 0;
		for(Method m:methods)
			if(Modifier.isStatic(m.getModifiers()) && m.getName().equals(name))
				count ++;

		Method[] good_methods = new Method[count];
		Class[][] defs = new Class[count][];

		int j = 0;
		for(Method m:methods) {
			if(m.getName().equals(name)) {
				good_methods[j] = m;
				defs[j] = m.getParameterTypes();
				j++;
			}
		}
		
		if(good_methods.length == 0)
			throw new ScriptError(ScriptError.AttributeError, java + " has no static method/field named " + name);
		
		return new JInstance.JMethod(null, name, good_methods, defs);
	}

	private Obj getStaticField(String name) {
		Field field;
		Object ret;
		try {
			field = java.getField(name);
			ret = field.get(this.java);
		} catch (NoSuchFieldException ex) {
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return Reflect.fromJava(ret);
	}
}
