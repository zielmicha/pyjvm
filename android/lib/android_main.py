print 'importing main module...'

import main

try:
    _main = main.main
except AttributeError:
    pass
else:
    _main()
