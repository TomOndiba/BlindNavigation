# coding=utf-8

from handlers.base import BaseHandler
from tornado.gen import coroutine

class UploadRouteHandler(BaseHandler):
	@coroutine
	def post(self):
		points = self.get_argument("points")
		crossings = self.get_argument("crossings")
		lights = self.get_argument("lights")
		startName = self.get_argument("startName")
		endName = self.get_argument("endName")
		startPos = self.get_argument("startPos")
		endPos = self.get_argument("endPos")
		middlePos = self.get_argument("middlePos")
		radius = self.get_argument("radius")
		yield self.db.execute("insert into routes(points,crossings,lights,startName,endName,startPos,endPos,middlePos,radius) "
			"values(%s,%s,%s,%s,%s,%s,%s,%s,%s) ",
			(points,crossings,lights,startName,endName,startPos,endPos,middlePos,radius)
		)
		self.finish_success()