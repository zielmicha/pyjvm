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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
	
	// to Java authors: it was so hard to make type Pair???
	static Map<Type, Map<Class, ToJava>> toJavaConverters = new HashMap<Type, Map<Class, ToJava>>();
	static Map<Class, FromJava> fromJavaConverters = new HashMap<Class, FromJava>();
	
	static int match(Class[][] types, Obj[] args) {
		int j = 0;
		
		typeLoop:
		for(Class[] typeArray: types) {
			if(typeArray.length == args.length) {
				for(int i=0; i<args.length; i++) {
					Class c = typeArray[i];
					Obj obj = args[i];
					Type type = obj.getType();
					Map<Class, ToJava> s = toJavaConverters.get(type);
					if(s == null || s.get(c) == null)
						continue typeLoop;
				}
				// Success! We have matched Python args with Java types!!!
				return j;
			}
			j++;
		}
		
		String errmsg = "Failed to match parameters " + List.fromArrayUnsafe(args) + ", to definitions: ";
		for(Class[] typeArray: types)
			errmsg += Arrays.toString(typeArray) + " ";
		
		throw new ScriptError(ScriptError.TypeError, errmsg);
	}
	
	static Object[] argsToJava(Class[] types, Obj[] args) {
		Object[] result = new Object[args.length];
		for(int i=0; i<args.length; i++) {
			Class c = types[i];
			Obj obj = args[i];
			Type type = obj.getType();
			ToJava s = toJavaConverters.get(type).get(c);
			result[i] = s.convert(args[i]);
		}
		return result;
	}
	
	static Obj fromJava(Object obj) {
		Class c = obj.getClass();
		if(fromJavaConverters.containsKey(c)) {
			return fromJavaConverters.get(c).convert(obj);
		} else {
			return new JInstance(obj);
		}
	}
	
	static void addToJavaConverter(Class c, Type t, ToJava func) {
		Map<Class, ToJava> m = toJavaConverters.get(t);
		if(m == null) {
			toJavaConverters.put(t, m = new HashMap<Class, ToJava>());
		}
		m.put(c, func);
		
	}
	
	static void addFromJavaConverter(Class c, FromJava func) {
		fromJavaConverters.put(c, func);
	}
	
	static {
		dict = ReflectClass.dict;
		dict.put("__name__", SString.fromJavaString("reflect"));
		
		addToJavaConverter(String.class, SStringClass.instance, new ToJava() {
			@Override
			public Object convert(Obj o) {
				return o.stringValue().toString();
			}
		});
		
		addToJavaConverter(byte[].class, ByteArrayClass.instance, new ToJava() {
			@Override
			public Object convert(Obj o) {
				return ((ByteArray)o).bytes;
			}
		});
		
		addToJavaConverter(int.class, SIntClass.instance, new ToJava() {
			@Override
			public Object convert(Obj o) {
				return Integer.valueOf(o.intValue());
			}
		});
		
		addFromJavaConverter(Integer.class, new FromJava() {
			@Override
			public Obj convert(Object obj) {
				return SInt.get(((Integer)obj).intValue());
			}
		});
		
		addFromJavaConverter(String.class, new FromJava() {
			@Override
			public Obj convert(Object obj) {
				return SString.fromJavaString((String)obj);
			}
		});
		
		addFromJavaConverter(Long.class, new FromJava() {

			public Obj convert(Object obj) {
				return SInt.get(((Long)obj).longValue());
			}
		});
	}
	
	public interface ToJava {
		Object convert(Obj o);
	}
	
	public interface FromJava {
		Obj convert(Object obj);
	}
}
