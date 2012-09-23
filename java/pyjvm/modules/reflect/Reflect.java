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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

	public static Obj create_invocation_handler(Obj callable) { //!export
		final Obj callable0 = callable;
		return fromJava(new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				Obj result = callable0.call(new Obj[] {
					SString.fromJavaString(method.getName()),
					fromJava(args)
				});
				return toJava(result);
			}
		});
	}

	static Map<Type, Map<Class, ToJava>> toJavaConverters = new HashMap<Type, Map<Class, ToJava>>();
	static Map<Type, ToJava> toJavaDefaultConverters = new HashMap<Type, ToJava>();
	static Map<Class, FromJava> fromJavaConverters = new HashMap<Class, FromJava>();

	static int match(Class[][] types, Obj[] args) {
		int j = 0;

		typeLoop:
		for(Class[] typeArray: types) {
			if(typeArray.length == args.length) {
				for(int i=0; i<args.length; i++) {
					Class c = typeArray[i];
					Obj obj = args[i];
					if(!matchArg(c, obj)) {
						j++;
						continue typeLoop;
					}
				}
				// Success! We have matched Python args with Java types!!!
				return j;
			} else {
				j++;
			}
		}

		String errmsg = "Failed to match parameters " + List.fromArrayUnsafe(args) + ", to definitions: ";
		for(Class[] typeArray: types)
			errmsg += Arrays.toString(typeArray) + " ";

		throw new ScriptError(ScriptError.TypeError, errmsg);
	}

	static Object[] argsToJava(Class[] types, Obj[] args) {
		if(types.length != args.length) {
			throw new ScriptError(ScriptError.InternalError, "not matching length of " + Arrays.toString(types) + " and " + Arrays.toString(args));
		}
		Object[] result = new Object[args.length];
		for(int i=0; i<args.length; i++) {
			Class c = types[i];
			Obj obj = args[i];

			result[i] = argToJava(c, obj);
		}
		return result;
	}

	static boolean matchArg(Class expected, Obj obj) {
		Type type = obj.getType();
		if(obj instanceof JInstance) {
			if(!expected.isInstance(((JInstance)obj).obj)) {
				return false;
			} else {
				return true;
			}
		} else {
			Map<Class, ToJava> s = toJavaConverters.get(type);
			if(s == null) return false;
			for(Class c: getClassParents(expected)) {
				if(s.get(c) != null) {
					return true;
				}
			}
			return false;
		}
	}

	static Object argToJava(Class expected, Obj obj) {
		if(obj instanceof JInstance) {
			return ((JInstance) obj).obj;
		} else {
			Type type = obj.getType();
			Map<Class, ToJava> hash = toJavaConverters.get(type);
			if(hash == null)
				throw new ScriptError(ScriptError.InternalError, "no map for " + type);
			for(Class c: getClassParents(expected)) {
				ToJava s = hash.get(c);
				if(s == null)
					continue;
				return s.convert(obj, expected);
			}
			throw new ScriptError(ScriptError.InternalError, "no def for " + type + ", " + expected);
		}
	}

	static Obj fromJava(Object obj) {
		if(obj == null) return Obj.None;
		Class clazz = obj.getClass();
		for(Class c: getClassParents(clazz)) {
			if(fromJavaConverters.containsKey(c)) {
				return fromJavaConverters.get(c).convert(obj);
			}
		}
		return new JInstance(obj);
	}

	static java.util.List<Class> getClassParents(Class s) {
		// TODO: interfaces
		ArrayList<Class> l = new ArrayList<Class>();
		Class current = s;
		while(current != null) {
			l.add(current);
			current = current.getSuperclass();
		}
		if(s.isArray())
			l.add(Object[].class);
		return l;
	}

	static Object toJava(Obj o) {
		Type t = o.getType();
		if(toJavaDefaultConverters.containsKey(t)) {
			return toJavaDefaultConverters.get(t).convert(o, null);
		} else {
			return o;
		}
	}

	static void addToJavaConverter(Class c, Type t, ToJava func) {
		Map<Class, ToJava> m = toJavaConverters.get(t);
		if(m == null) {
			toJavaConverters.put(t, m = new HashMap<Class, ToJava>());
		}
		m.put(c, func);
		toJavaDefaultConverters.put(t, func);

	}

	static void addFromJavaConverter(Class c, FromJava func) {
		fromJavaConverters.put(c, func);
	}

	static {
		dict = ReflectClass.dict;
		dict.put("__name__", SString.fromJavaString("reflect"));

		addToJavaConverter(Object.class, Obj.None.getType(), new ToJava() {
			public Object convert(Obj o, Class expected) {
				return null;
			}
		});

		addToJavaConverter(Object[].class, ListClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				Class component = expected.getComponentType();
				Object[] arr = (Object[]) Array.newInstance(component, o.length());
				int i = 0;
				for(Obj item: o) {
					arr[i++] = argToJava(component, item);
				}
				return arr;
			}
		});

		addToJavaConverter(String.class, SStringClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return o.stringValue().toString();
			}
		});

		addToJavaConverter(byte[].class, ByteArrayClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return ((ByteArray)o).bytes;
			}
		});

		addToJavaConverter(byte[].class, SStringClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return ((SString)o).bytes;
			}
		});

		addToJavaConverter(int.class, SIntClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return Integer.valueOf(o.intValue());
			}
		});

		addToJavaConverter(boolean.class, SIntClass.instance, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return o.boolValue();
			}
		});

		addToJavaConverter(Class.class, JClass.type, new ToJava() {
			public Object convert(Obj o, Class expected) {
				return ((JClass)o).java;
			}
		});

		addFromJavaConverter(Integer.class, new FromJava() {
			public Obj convert(Object obj) {
				return SInt.get(((Integer)obj).intValue());
			}
		});

		addFromJavaConverter(String.class, new FromJava() {
			public Obj convert(Object obj) {
				return SString.fromJavaString((String)obj);
			}
		});

		addFromJavaConverter(Long.class, new FromJava() {
			public Obj convert(Object obj) {
				return SInt.get(((Long)obj).longValue());
			}
		});

		addFromJavaConverter(Object[].class, new FromJava() {
			public Obj convert(Object obj) {
				return new ArrayWrapper((Object[])obj);
			}
		});
	}

	public interface ToJava {
		Object convert(Obj o, Class expected);
	}

	public interface FromJava {
		Obj convert(Object obj);
	}
}
