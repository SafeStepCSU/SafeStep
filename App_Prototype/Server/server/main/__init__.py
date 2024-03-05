"""
application factory pattern - application is created inside a function
"""

from flask import Flask
from flask_pymongo import PyMongo
from dotenv import load_dotenv
from server.config import config
import os

# Initialize PyMongo to be used in the application
mongo = PyMongo()

def create_app():
    # load environment variables
    load_dotenv()

    app = Flask(__name__)

    # Apply configuration - can be development, production, default, this is just nice to have for different environments, if we ever do have a dedicated 'production' environment
    config_name = os.getenv('FLASK_ENV', 'default')
    app.config.from_object(config[config_name])  # load the configuration settings from the config.py file


    # Initialize PyMongo with app configuration
    mongo.init_app(app)

    # Register/initialize the routes for the app
    from .routes import init_app_routes
    init_app_routes(app)

    return app


