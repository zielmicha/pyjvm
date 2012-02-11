# Copyright (C) 2011 by Michal Zielinski
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.


assert 1+2 == 3
assert 2*3 == 6
assert 10/2 == 5
assert 10//2 == 5
assert 5-2 == 3

def f1(a, b, c):
	assert a==1
	assert b==2
	assert c==3

f1(1,2,3)

a = 10
a -= 1
assert a == 9

a = 10
a += 1
assert a == 11

seq = [1,2,3]
seq2 = (1,2,3)

assert seq[0] == 1

def f2():
	return 4

assert f2() == 4

assert int(5) == 5

for i in seq:
	assert i == 1
	break

number = 0

for i in xrange(3):
	number += 1

assert number == 3

seq.append(9)
assert seq[3] == 9
assert seq.__len__() == 4
assert len(seq) == 4

try:
	non_existing
except: pass
else: assert False

assert True or False
assert True and True
assert not False

def f3():
	non_existing

try:
	f3()
except: pass
else: assert False

class F:
	f = 4

assert F.f == 4
F.g = 5
assert F.g == 5

instance = F()
assert instance.f == 4
assert instance.g == 5
alamakota = 0
class D:
	def __init__(self):
		self.i = 5
	
	def m1(self):
		return 12

d = D()
assert d.i == 5
assert d.m1() == 12

import test_import
assert test_import.value == 5

try:
	non_exising
except AttributeError: pass
else: assert False

try:
	non_exising
except TypeError: assert False
except AttributeError: pass

def f3(a, b=1):
	assert b == a

f3(1)
f3(2, 2)

try:
	f3()
except TypeError: pass
else: assert False

try:
	f3(2, 2, 2)
except TypeError: pass
else: assert False

def f4(a, b):
	assert a - 1 == b

#f4(b=1, a=2)

def f5(a, b, c, d=4, e=0):
	assert a == 1
	assert b == 2
	assert c == 3
	assert d == 4
	assert e == 5

f5(1, 2, c=3, e=5)

s = 'abc'
s += 'def'
assert s == 'abcdef'

assert ','.join(['1', '2', '3']) == '1,2,3'

from test_import import value

assert value == 5

assert (1 < 2 < 3)
assert not (3 < 2 < 3)
assert not (1 < 2 < 1)

assert 1<2
assert 2>1
assert 1<=2
assert 2>=2
assert 1<=1
assert 1>=1

f = 10
del f

try:
	f
except AttributeError: pass
else: assert False

class S: pass
s = S()
s.f = 5
del s.f

try:
	s.f
except AttributeError: pass
else: assert False

try:
	raise IOError()
except IOError: pass
else: assert False

try:
	raise IOError
except IOError: pass
else: assert False

try:
	raise IOError, 5
except IOError: pass
else: assert False

try:
	raise IOError(), 5
except TypeError: pass
else: assert False

try:
	try:
		raise IOError
	except:
		raise
except IOError: pass
else: assert False

a=1
assert a is a
assert a is not 2

assert -(1) == -1

from lib import StringIO
s = StringIO.StringIO()
s.write('123')
assert '123' == '123'
assert '123' == s.getvalue()

assert "ab" * 3 == "ababab"

import sys
assert sys.modules['sys'] == sys

try: sys.modules['aaaa']
except AttributeError: pass
else: assert False

d = {}
d['a'] = 1
assert d['a'] == 1

d = {'a': 1}
assert d == {'a': 1}
assert d != {'a': 2}
assert d != {'a': 2, 'b': 3}
assert d != {'b': 1}

d['a'] = 3
assert d['a'] == 3

print 'argv:', ' '.join(sys.argv)

assert type(7) == type(6)

type_dict = {}
type_dict['__init__'] = lambda self: None
type_dict['m'] = lambda self, x: (self, x)
assert 'm' in type_dict
assert '__init__' in type_dict

dyn_type = type('DynType', (), type_dict)
dyn_type.m
i = dyn_type()
assert i.m(1) == (i, 1)

assert '123' in '01234'
assert '123' not in '12'
assert '123' not in '124'
assert '123' in '123'

assert ('aa' + '12' * 1000).find('12') == 2
assert ('aa' + '12' * 1000).find('13') == -1
assert ('ab' * 1000).find('ab' * 100) == 0
assert ('ab' * 1000).find('ab' * 100 + 'c') == -1

#print sys.builtins

def deep_recur(i):
	if i != 0:
		return deep_recur(i - 1)

deep_recur(3000)

s = '123' * 3000
a = []
for i in xrange(10):
	a.append(s)
assert ''.join(a) == s * 10

import reflect

f = reflect.get_class('java.io.FileInputStream')

file = f.create('tests-inside.py')
assert file.read() == 35 # 35 is code of '#'

from lib import os

f = os.File('tests-inside.py')
assert f.read(20) == "# Copyright (C) 2011"

l = [1, 2, 3]
l2 = list(xrange(20))

assert max(2, 3, 1, 5, 3) == 5
assert max(2, 3, 1) == 3
assert max([1, 2, 3]) == 3

assert ''.join(os.File('example').readlines()) == os.File('example').read()

print l2[:-3], l2[-3:]
assert l2[:-3] + l2[-3:] == l2

assert '01234'[2:3] == '2'
assert '01234'[2:4] == '23'

StringIO.test()

