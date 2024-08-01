from flask import Flask, request, jsonify
import sqlite3
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)

def get_products_db_connection():
    conn = sqlite3.connect('database1.db')
    conn.row_factory = sqlite3.Row
    return conn

def get_users_db_connection():
    conn = sqlite3.connect('database2.db')
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/products', methods=['POST'])
def add_product():
    data = request.get_json()
    if not data:
        return jsonify({'message': 'No JSON data received'}), 400

    location = data.get('location')
    locario = data.get('locario')
    price = data.get('price')
    image = data.get('image')

    if not all([location, locario, price]):
        return jsonify({'message': 'Missing required fields'}), 400

    try:
        with sqlite3.connect('database.db') as conn:
            cursor = conn.cursor()
            cursor.execute('''
            INSERT INTO Product (location, locario, price, image) 
            VALUES (?, ?, ?, ?)
            ''', (location, locario, price, image))
            conn.commit()
    except sqlite3.Error as e:
        return jsonify({'message': str(e)}), 500

    return jsonify({'message': 'Product added successfully'})

@app.route('/products', methods=['GET'])
def get_products():
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Product ORDER BY id DESC')
        products = cursor.fetchall()

    product_list = []
    for product in products:
        product_dict = {
            'id': product[0],
            'location': product[1],
            'locario': product[2],
            'price': product[3],
            'image': product[4]
        }
        product_list.append(product_dict)

    return jsonify(product_list)

@app.route('/products/<int:product_id>', methods=['DELETE'])
def delete_product(product_id):
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('DELETE FROM Product WHERE id = ?', (product_id,))
        conn.commit()
        if cursor.rowcount == 0:
            return jsonify({'message': 'Product not found'}), 404

    return jsonify({'message': 'Product deleted successfully'})

#################################################

@app.route('/users', methods=['POST'])
def add_user():
    data = request.get_json()
    password = data['password']
    password = generate_password_hash(password, method='pbkdf2:sha256')
    if not data:
        return jsonify({'message': 'No JSON data received'}), 400

    name = data.get('name')
    phone = data.get('phone')
    password = data.get('password')

    if not all([name, phone, password]):
        return jsonify({'message': 'Missing required fields'}), 400
    
    password = generate_password_hash(password, method='pbkdf2:sha256')

    try:
        with sqlite3.connect('database.db') as conn:
            cursor = conn.cursor()
            cursor.execute('''
            INSERT INTO User (name, phone, password) 
            VALUES (?, ?, ?)
            ''', (name, phone, password))
            conn.commit()
    except sqlite3.IntegrityError:
        return jsonify({'message': 'Phone number already registered'}), 400
    except sqlite3.Error as e:
        return jsonify({'message': str(e)}), 500

    return jsonify({'message': 'User added successfully'})

@app.route('/users', methods=['GET'])
def get_users():
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM User ORDER BY id DESC')
        users = cursor.fetchall()

    user_list = []
    for user in users:
        user_dict = {
            'id': user[0],
            'name': user[1],
            'phone': user[2],
            'password': user[3]
        }
        user_list.append(user_dict)

    return jsonify(user_list)


@app.route('/users/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('DELETE FROM User WHERE id = ?', (user_id,))
        conn.commit()
        if cursor.rowcount == 0:
            return jsonify({'message': 'User not found'}), 404

    return jsonify({'message': 'User deleted successfully'})


##########################################

@app.route('/login', methods=['POST'])
def login_user():
    data = request.get_json()
    if not data:
        return jsonify({'message': 'No JSON data received'}), 400

    phone = data.get('phone')
    password = data.get('password')

    if not all([phone, password]):
        return jsonify({'message': 'Missing required fields'}), 400

    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM User WHERE phone = ?', (phone,))
        user = cursor.fetchone()

    if user is None or not check_password_hash(user[3], password):
        return jsonify({'message': 'Invalid phone or password'}), 401

    return jsonify({'message': 'Login successful', 'name': user[1]})



if __name__ == '__main__':
    app.run(port=5000, debug=True)
