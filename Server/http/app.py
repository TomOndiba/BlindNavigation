# coding=utf-8

import tornado.httpserver
import tornado.ioloop
import tornado.options
import tornado.web
from tornado.options import options, define
from tornado_mysql import pools
from tornado_mysql.cursors import DictCursor
from routes import handlers
import MySQLdb
#import torndb

define("port", default=9555, help="本地监听端口", type=int)
define("db_host", default="127.0.0.1", help="数据库地址", type=str)
define("db_port", default=3306, help="数据库port", type=int)
define("db_name", default="edison", help="数据库名字", type=str)
define("db_user", default="root", help="数据库名字", type=str)
define("db_pass", default="momo0506", help="数据库密码", type=str)
tornado.options.parse_command_line()

application = tornado.web.Application(
	handlers = handlers,
	#database = MySQLdb.connect(host=options.db_host,db=options.db_name,user=options.db_user,passwd=options.db_pass)
	#database = torndb.Connection(options.db_host,options.db_name,options.db_user,options.db_pass)
	database=pools.Pool(
		dict(host=options.db_host, port=options.db_port, user=options.db_user,
			passwd=options.db_pass, db=options.db_name, cursorclass=DictCursor, charset='utf8'
			),
		max_idle_connections=2,
		max_recycle_sec=3,
		max_open_connections=2,
   )
)

application.listen(options.port)
ioloop = tornado.ioloop.IOLoop.current()

ioloop.start()