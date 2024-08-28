import sqlite3

# Conectar ao banco de dados (ou criar se não existir)
conn = sqlite3.connect('database1.db')
cursor = conn.cursor()

# Criar a tabela Product com as novas colunas description e timestamp
cursor.execute('''
CREATE TABLE IF NOT EXISTS Product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    location TEXT NOT NULL,
    locario TEXT NOT NULL,
    price REAL NOT NULL,
    image TEXT,
    userName TEXT,         -- Coluna para armazenar o nome de usuário
    profileImage TEXT,     -- Coluna para armazenar a imagem de perfil
    description TEXT,      -- Nova coluna para armazenar a descrição do produto
    timestamp TEXT         -- Coluna para armazenar o timestamp
)
''')

# Confirmar a transação
conn.commit()

# Inserir um registro na tabela Product com a nova coluna description
cursor.execute('''
INSERT INTO Product (location, locario, price, image, userName, profileImage, description, timestamp) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
''', (
    'São Paulo', 
    'Centro', 
    150.0, 
    'http://example.com/image.jpg', 
    'Usuario1', 
    'http://example.com/profile.jpg', 
    'Descrição do produto', 
    '12:30 28-08-2024'  
))

# Confirmar a transação
conn.commit()

# Consultar dados da tabela Product
cursor.execute('SELECT * FROM Product')
products = cursor.fetchall()
for product in products:
    print(product)

# Fechar a conexão
conn.close()
