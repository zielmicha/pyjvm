from _reflect import get_class, create_invocation_handler

Proxy = get_class('java.lang.reflect.Proxy')
Boolean = get_class('java.lang.Boolean')

def get_class_loader():
    return Boolean.TRUE.getClass().getClassLoader()

def implement(interface, methods):
    def handler(name, args):
        func = methods[name]
        return func(*list(args))

    return Proxy.newProxyInstance(get_class_loader(), [interface], create_invocation_handler(handler))
