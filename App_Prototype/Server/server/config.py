import os


class Config:
    """Base configuration class with default settings."""
    MONGO_URI = os.environ.get('MONGO_URI', 'mongodb://localhost:27017/SafeStep')
    SECRET_KEY = os.environ.get('SECRET_KEY', '123abc')


class DevelopmentConfig(Config):
    """Development configuration class. Inherits from Config."""
    DEBUG = True
    # no need to override MONGO_URI, it's the same as in Config

class ProductionConfig(Config):
    """Production configuration class. Inherits from Config."""
    DEBUG = False
    # Override MONGO_URI to use the production database
    MONGO_URI = os.environ.get('PROD_MONGO_URI', 'mongodb://mongo:27017/SafeStep')
    SECRET_KEY = os.environ.get('PROD_SECRET_KEY', 'orangeGorilla0_0')


config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'default': DevelopmentConfig
}
