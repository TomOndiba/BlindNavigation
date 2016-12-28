# coding=utf-8

import json
from tornado.options import options
from tornado.web import RequestHandler
from tornado.web import HTTPError

DEFAULT_TYPE = []

class MilakuluJSONEncoder(json.JSONEncoder):

	def default(self, o):
		if isinstance(o, datetime):
			encoded_object = o.strftime(config.DATETIME_FORMAT)
		elif isinstance(o, ObjectId):
			encoded_object = str(o)
		else:
			encoded_object = super(MilakuluJSONEncoder, self).default(o)

		return encoded_object

def json_encode(o):
	return json.dumps(o, cls=MilakuluJSONEncoder)

def json_decode(o):
	return json.loads(o)

class ArgsError(HTTPError):

	def __init__(self, res_name, info=''):
		info = info or '输入的 %s 参数不存在或者格式不正确' % res_name
		super(ArgsError, self).__init__(400, info)
		self.arg_name = res_name
		self.code = 104

class BaseHandler(RequestHandler):
	@property
	def db(self):
		return self.settings['database'] #.cursor()

	@property
	def json_body(self):
		if not hasattr(self, '_json_body'):
			if hasattr(self.request, "body"):
				try:
					self._json_body = json_decode(self.request.body.decode('utf-8'))
				except ValueError:
					raise ArgsError("参数不是json格式！")

		return self._json_body

	def get_json_argument(self, name, default=DEFAULT_TYPE):
		if name in self.json_body:
			return self.json_body[name]
		elif isinstance(default, list) and len(default) == 0:
			raise ArgsError(name)
		else:
			return default

	def get_argument(self, name, default=DEFAULT_TYPE, strip=True):
		# 参数优先查询字符串里的参数
		if self.request.method in ('GET', 'DELETE'):
			rs = self.get_query_argument(name, default)
			if rs is DEFAULT_TYPE:
				raise ArgsError(name)
			return rs
		else:
			if name in self.json_body:
				rs = self.json_body[name]
				return rs
			elif default is DEFAULT_TYPE:
				raise ArgsError(name)
			else:
				return default
	
	def finish_success(self, **kwargs):
		rs = {
			'status': 'success'
		}

		rs.update(kwargs)
		self.finish(json_encode(rs))

	def finish_failed(self,**kwargs):
		rs = {
			'status': 'failed'
		}
		self.finish(json_encode(rs))
