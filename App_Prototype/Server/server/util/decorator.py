from flask import request, jsonify, current_app
from functools import wraps
import jwt
from server.main import mongo

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        # Check if Authorization header is in the request
        if 'Authorization' in request.headers:
            # Attempt to split the header to remove "Bearer " prefix
            auth_header = request.headers['Authorization']
            token_parts = auth_header.split(' ')  # Split by space
            if len(token_parts) == 2 and token_parts[0].lower() == "bearer":
                token = token_parts[1]  # Take the token part
            else:
                return jsonify({'message': 'No token, no permission.'}), 401

        if not token:
            return jsonify({'message': 'No token, no permission.'}), 401

        try:
            # Decode the token using the secret key
            data = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=["HS256"])
            # Get the user from the database using the username from the token
            current_user = mongo.db.users.find_one({'username': data['username']})
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Token has expired.'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Token is invalid.'}), 401

        return f(current_user=current_user, *args, **kwargs)

    return decorated
