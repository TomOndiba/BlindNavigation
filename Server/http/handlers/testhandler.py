# coding=utf-8

from handlers.base import BaseHandler
from tornado.gen import coroutine


class TestHandler(BaseHandler):
	@coroutine
	def get(self):
		yield self.db.execute("insert into routes(points,crossings,lights,startName,endName,startPos,endPos,middlePos,radius) "
			"values(%s,%s,%s,%s,%s,%s,%s,%s,%s) ",
			("points","crossings","lights","startName","endName","startPos","endPos","middlePos",123.12345678910)
		)
		self.finish_success(info="Hello,worldttt")

