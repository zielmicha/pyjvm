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



def pop_many(list, n):
	result = []
	for i in xrange(n):
		result.append(list.pop())
	return result

class Struct(object):
	_defaults = None
	_ignore_in_repr = ''
	
	def __init__(self, **kwargs):
		if self._defaults:
			kwargs2 = self._defaults.copy()
			kwargs2.update(kwargs)
			kwargs = kwargs2
		for field in self._fields.split():
			if field not in kwargs:
				raise ValueError('Required argument (%s) not given' % field)
			setattr(self, field, kwargs[field])
			del kwargs[field]
		if kwargs:
			raise ValueError('Unexpected argument %s' % kwargs.keys()[0])
		self._init_struct()
	
	def _init_struct(self):
		pass
	
	def __repr__(self):
		attrs = ' '.join(
			'%s=%r' % (k, v)
			for k, v in self._attrs.items()
			if k not in self._ignore_in_repr.split()
		)
		return '<%s %s>' % (self.__class__.__name__, attrs) 
	
	@property
	def _attrs(self):
		return dict(
			(key, getattr(self, key))
			for key in self._fields.split()
		)
	
	def clone(self, **attrs):
		thisdict = self._attrs.copy()
		for k, v in attrs.items():
			assert k in self._fields
			thisdict[k] = v
		new = type(self)(**thisdict)
		return new

