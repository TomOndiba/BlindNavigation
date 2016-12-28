# coding=utf-8

from handlers.testhandler import TestHandler
from handlers.upload_route import UploadRouteHandler
from handlers.get_route import GetRouteHandler

handlers=[
	(r'/test',TestHandler),
	(r'/upload_route',UploadRouteHandler),
	(r'/get_route',GetRouteHandler)
]