from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
db = SQLAlchemy(app)

class Product(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    location = db.Column(db.String(100), nullable=False)
    locario = db.Column(db.String(100), nullable=False)
    price = db.Column(db.Float, nullable=False)
    image = db.Column(db.String(1000), nullable=True)  # Assuming storing image paths or URLs

@app.route('/products', methods=['POST'])
def add_product():
    data = request.get_json()
    new_product = Product(
        location=data['location'],
        locario=data['locario'],
        price=data['price'],
        image=data['image']  # You may need to handle image uploads properly
    )
    db.session.add(new_product)
    db.session.commit()
    return jsonify({'message': 'Product added successfully'})

if __name__ == '__main__':
    app.run(debug=True)
