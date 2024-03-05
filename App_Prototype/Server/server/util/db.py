from flask_pymongo import PyMongo

mongo = PyMongo()

def get_db():
    return mongo.db

def init_db(app):
    mongo.init_app(app)
