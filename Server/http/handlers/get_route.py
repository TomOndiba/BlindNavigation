# coding=utf-8

from handlers.base import BaseHandler
from tornado.gen import coroutine

class GetRouteHandler(BaseHandler):
	@coroutine
	def post(self):
		startName = self.get_argument("startName")
		endName = self.get_argument("endName")
		route_cur = yield self.db.execute(
			"select * from routes where startName=%s and endName=%s",
			(startName,endName)
			)
		route = route_cur.fetchone()
		if not route:
			self.finish_failed()
		else:
			self.finish_success(**route)

