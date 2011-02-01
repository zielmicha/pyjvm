
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
except:
	pass
else:
	assert False

assert True or False
assert True and True
assert not False

def f3():
	non_existing

try:
	f3()
except:
	pass
else:
	assert False

class F:
	f = 4

assert F.f == 4
F.g = 5
assert F.g == 5

instance = F()
assert instance.f == 4
assert instance.g == 5

class D:
	def m1(self):
		return self

d = D()
assert d.m1() == d

import test_import
assert test_import.value == 5