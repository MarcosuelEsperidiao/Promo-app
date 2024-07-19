from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

def init_db():
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('''
        CREATE TABLE IF NOT EXISTS Product (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            location TEXT NOT NULL,
            locario TEXT NOT NULL,
            price REAL NOT NULL,
            image TEXT
        )
        ''')
        conn.commit()

@app.route('/products', methods=['POST'])
def add_product():
    data = request.get_json()
    if not data:
        return jsonify({'message': 'No JSON data received'}), 400

    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('''
        INSERT INTO Product (location, locario, price, image) 
        VALUES (?, ?, ?, ?)
        ''', (data.get('location'), data.get('locario'), data.get('price'), data.get('image')))
        conn.commit()

    return jsonify({'message': 'Product added successfully'})

@app.route('/products', methods=['GET'])
def get_products():
    with sqlite3.connect('database.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Product ORDER BY id DESC')  # Ordenar pelo ID de forma decrescente
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


if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
