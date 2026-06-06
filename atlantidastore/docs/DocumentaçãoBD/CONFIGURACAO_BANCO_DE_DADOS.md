# 🗄️ Configuração do Banco de Dados

## Estrutura das Tabelas

### **1. Tabela: usuarios**

```sql
CREATE TABLE usuarios (
    id TEXT PRIMARY KEY,          -- UUID em texto
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senhaHash VARCHAR(255) NOT NULL,
    dataCriacao DATETIME NOT NULL
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
```

### **2. Tabela: jogos**

```sql
CREATE TABLE jogos (
    id TEXT PRIMARY KEY,                    -- UUID em texto
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    publicadorId TEXT NOT NULL,             -- UUID em texto
    categorias TEXT,                        -- Delimitado por |
    dataCriacao DATETIME NOT NULL,
    downloadUrl VARCHAR(500) NOT NULL
);

CREATE INDEX idx_jogos_titulo ON jogos(titulo);
CREATE INDEX idx_jogos_dataCriacao ON jogos(dataCriacao);
CREATE INDEX idx_jogos_publicadorId ON jogos(publicadorId);
```

### **3. Tabela: sessoes**

```sql
CREATE TABLE sessoes (
    token VARCHAR(500) PRIMARY KEY,
    usuarioId TEXT NOT NULL,                -- UUID em texto
    criadoEm DATETIME NOT NULL,
    expiraEm DATETIME NOT NULL
);

CREATE INDEX idx_sessoes_usuarioId ON sessoes(usuarioId);
CREATE INDEX idx_sessoes_expiraEm ON sessoes(expiraEm);
```

---

## 🔑 Relacionamentos

```
┌─────────────────┐
│    USUARIOS     │
│                 │
│ id (PK)         │
│ nome            │
│ email (UNIQUE)  │
│ senhaHash       │
│ dataCriacao     │
└────────┬────────┘
         │
         │ (1 usuário → N sessões)
         │ (usuarioId referencia id)
         │
         ▼
┌─────────────────┐
│    SESSOES      │
│                 │
│ token (PK)      │
│ usuarioId (FK)  │
│ criadoEm        │
│ expiraEm        │
└─────────────────┘

┌─────────────────┐
│      JOGOS      │
│                 │
│ id (PK)         │
│ titulo          │
│ descricao       │
│ preco           │
│ publicadorId    │ (FK → usuarios.id)
│ categorias      │
│ dataCriacao     │
│ downloadUrl     │
└─────────────────┘
```

---

## 📝 Exemplos de Dados

### **Usuários**

```sql
INSERT INTO usuarios VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'João Silva',
    'joao@example.com',
    'hash_da_senha_criptografada',
    '2024-01-15 10:30:00'
);
```

### **Jogos**

```sql
INSERT INTO jogos VALUES (
    '660e8400-e29b-41d4-a716-446655440111',
    'The Legend of Zelda',
    'Um clássico jogo de aventura...',
    59.90,
    '550e8400-e29b-41d4-a716-446655440000',
    'RPG|Aventura|Ação',
    '2024-01-20 14:45:00',
    'https://download.example.com/zelda.zip'
);
```

### **Sessões**

```sql
INSERT INTO sessoes VALUES (
    'token_aleatorio_muito_longo_aqui_550e8400e29b41d4',
    '550e8400-e29b-41d4-a716-446655440000',
    '2024-01-20 15:00:00',
    '2024-01-21 15:00:00'  -- expira em 24h
);
```

---

## 🔍 Queries Úteis

### **Listar todos os usuários**

```sql
SELECT * FROM usuarios ORDER BY dataCriacao DESC;
```

### **Buscar usuário por email**

```sql
SELECT * FROM usuarios WHERE email = 'joao@example.com';
```

### **Listar jogos ordenados por data**

```sql
SELECT * FROM jogos ORDER BY dataCriacao DESC;
```

### **Listar jogos por categoria**

```sql
SELECT * FROM jogos WHERE categorias LIKE '%RPG%';
```

### **Verificar sessões válidas (não expiradas)**

```sql
SELECT * FROM sessoes WHERE expiraEm > datetime('now');
```

### **Listar sessões de um usuário específico**

```sql
SELECT * FROM sessoes
WHERE usuarioId = '550e8400-e29b-41d4-a716-446655440000'
AND expiraEm > datetime('now');
```

### **Listar sessões expiradas (para limpeza)**

```sql
SELECT * FROM sessoes WHERE expiraEm < datetime('now');
```

### **Listar jogos de um publicador**

```sql
SELECT j.* FROM jogos j
WHERE j.publicadorId = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY j.dataCriacao DESC;
```

### **Contar jogos totais**

```sql
SELECT COUNT(*) as total_jogos FROM jogos;
```

### **Listar price range dos jogos**

```sql
SELECT
    MIN(preco) as preco_minimo,
    MAX(preco) as preco_maximo,
    AVG(preco) as preco_medio
FROM jogos;
```

---

## ⚙️ Otimizações Implementadas

✅ **Índices em colunas frequentemente consultadas:**

- `email` em `usuarios`
- `titulo` em `jogos`
- `usuarioId` em `sessoes`
- `expiraEm` em `sessoes` (para limpeza automática)

✅ **Constraints:**

- `UNIQUE` em `email` para prevenir duplicatas
- `PRIMARY KEY` em todos

✅ **Performance:**

- Queries derivadas no JPA (sem N+1 queries)
- Batch processing automático (batch_size=20)
- Order inserts/updates para melhor performance

---

## 🔐 Segurança

### **Dados Sensíveis**

- ✅ Senhas são armazenadas como **hash** (use PasswordHasher)
- ✅ Sessões têm **tokens únicos**
- ✅ UUIDs são usados para identificadores (dificultam enumeration)

### **SQLite Specifics**

⚠️ **SQLite não tem:**

- Usuários/permissões nativas
- Criptografia nativa (considere usar em produção → PostgreSQL)
- SSL/TLS nativo

---

## 📈 Migração de Volume

| Tabela   | Registros | Espaço (aprox) |
| -------- | --------- | -------------- |
| usuarios | 100       | 20 KB          |
| jogos    | 1000      | 200 KB         |
| sessoes  | 500       | 50 KB          |

**Total esperado:** ~270 KB (muito leve!)

---

## 🔄 Backup & Restore

### **Backup**

```bash
# Copiar arquivo
cp atlantidastore.db atlantidastore.db.backup

# Ou exportar SQL
sqlite3 atlantidastore.db .dump > backup.sql
```

### **Restore**

```bash
# Do arquivo
cp atlantidastore.db.backup atlantidastore.db

# Do SQL
sqlite3 atlantidastore_novo.db < backup.sql
```

---

## 🚀 Para Produção

Se planeja usar em produção:

### **Option 1: Manter SQLite**

✅ Funciona bem  
✅ Sem setup externo  
✅ Backups fáceis  
❌ Sem replicação  
❌ Sem clustering

### **Option 2: Migrar para PostgreSQL**

1. Adicione dependência: `org.postgresql:postgresql`
2. Mude `spring.datasource.url`
3. Mude `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect`
4. Crie o banco no servidor PostgreSQL
5. Dump SQL e restore

---

**Banco configurado e pronto! 🎉**
