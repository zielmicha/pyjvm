import reflect

System = reflect.get_class('java.lang.System')

def clock():
    return System.currentTimeMillis() / 1000.

