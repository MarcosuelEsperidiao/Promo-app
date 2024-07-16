import sqlite3

# Conectar ao banco de dados (ou criar se não existir)
conn = sqlite3.connect('database.db')
cursor = conn.cursor()

# Criar a tabela Product
cursor.execute('''
CREATE TABLE IF NOT EXISTS Product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    location TEXT NOT NULL,
    locario TEXT NOT NULL,
    price REAL NOT NULL,
    image TEXT
)
''')

# Inserir um registro na tabela Product
cursor.execute('''
INSERT INTO Product (location, locario, price, image) 
VALUES (?, ?, ?, ?)
''', ('São Paulo', 'Centro', 150.0, 'http://example.com/image.jpg'))

# Confirmar a transação
conn.commit()

# Consultar dados da tabela Product
cursor.execute('SELECT * FROM Product')
products = cursor.fetchall()
for product in products:
    print(product)

# Fechar a conexão
conn.close()
