import sys

try:
    c = string_dict()
except NameError:
    print 'using normal dict'
    c = {}

for i in [1, 2, 3]:
    assert i in (1,2,3)

def func():
    s = [] 
    
    for i in xrange(5000, 7):
        s.append(str(i))
    
    s += [ str(i) for i in xrange(5000, 3) ]
    
    for s1 in s:
        c[s1] = s1 + 'c'
    
    for s1 in s:
        assert c[s1] == s1 + 'c'

func()
c = {}
func()
print 'ok!'
