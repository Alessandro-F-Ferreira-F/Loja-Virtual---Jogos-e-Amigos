# 🚀 Quick Start - Ativar Banco de Dados

## ⚡ 5 Passos Rápidos

### **1️⃣ Compilar o Projeto**

```bash
cd atlantidastore
mvn clean compile
```

### **2️⃣ Executar a Aplicação**

```bash
mvn spring-boot:run
```

**O que acontece automaticamente:**

- Hibernate cria as tabelas do banco
- MigrationService lê seus CSVs
- Dados são migrados para SQLite
- Arquivo `atlantidastore.db` é criado

### **3️⃣ Verificar a Migração**

```bash
# Abra outro terminal
sqlite3 atlantidastore.db

# Dentro do sqlite3
SELECT count(*) FROM usuarios;
SELECT count(*) FROM jogos;
SELECT count(*) FROM sessoes;
.exit
```

### **4️⃣ Testar a Aplicação**

```bash
# A aplicação continua na http://localhost:8080
# Tudo funciona igual!
# Agora os dados estão no banco de dados
```

### **5️⃣ (Opcional) Remover CSVs Antigos**

```bash
# Se quiser deletar os arquivos CSV antigos:
rm data/usuarios.csv data/jogos.csv data/sessoes.csv
# Mas é bom manter como backup!
```

---

## ✅ Pronto!

Sua aplicação agora usa:

- ✅ Spring Data JPA
- ✅ Hibernate
- ✅ SQLite Database
- ✅ Transações automáticas
- ✅ Queries otimizadas

---

## 📊 Arquitetura Atual

```
Requisição HTTP
      ↓
  Controller
      ↓
  UsuarioRepository (Interface)
      ↓
  UsuarioRepositoryImp (Implementação)
      ↓
  JpaUsuarioRepository (JPA)
      ↓
  Hibernate ORM
      ↓
  SQLite (atlantidastore.db)
```

---

## 🔍 Verificar Dados no Banco

### **Ver todas as tabelas**

```bash
sqlite3 atlantidastore.db .tables
```

### **Ver estrutura de uma tabela**

```bash
sqlite3 atlantidastore.db ".schema usuarios"
```

### **Executar query customizada**

```bash
sqlite3 atlantidastore.db "SELECT * FROM usuarios LIMIT 5;"
```

---

## 🎯 Mudanças para Desenvolvedores

### **Controllers - SEM MUDANÇAS**

Continuam igual:

```java
@Autowired
private UsuarioRepository usuarioRepository;

public void criarUsuario(Usuario usuario) {
    usuarioRepository.save(usuario);  // ✅ Funciona igual!
}
```

### **Services - SEM MUDANÇAS**

Continuam igual:

```java
@Service
public class MinhaService {
    @Autowired
    private JogoRepository jogoRepository;

    public void criarJogo(Jogo jogo) {
        jogoRepository.save(jogo);  // ✅ Funciona igual!
    }
}
```

### **Apenas o Repository é diferente**

Agora usa JPA:

```java
// Novo UsuarioRepositoryImp.java
@Repository
public class UsuarioRepositoryImp implements UsuarioRepository {
    @Autowired
    private JpaUsuarioRepository jpaRepository;

    @Override
    public Usuario save(Usuario user) {
        return jpaRepository.save(user);  // ✅ Via JPA
    }
}
```

---

## 📚 Documentação Completa

Para mais detalhes, veja:

- **[MIGRACAO_CSV_PARA_JPA.md](./MIGRACAO_CSV_PARA_JPA.md)** - Guia completo
- **[CONFIGURACAO_BANCO_DE_DADOS.md](./CONFIGURACAO_BANCO_DE_DADOS.md)** - Schema SQL e queries

---

## ⚠️ Troubleshooting Rápido

### Erro: "Table 'usuarios' already exists"

→ Remova `atlantidastore.db` e execute novamente

### Erro: "No suitable driver"

→ Execute: `mvn dependency:resolve`

### Dados não migraram

→ Verifique que CSVs existem em `data/`
→ Veja logs da MigrationService

### Quero mais detalhes

→ Ative logs em `application.properties`:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

---

## 🎉 Migração Concluída!

Seu projeto agora está com:

- ✅ Banco de dados relacional
- ✅ ORM moderna (Hibernate)
- ✅ Sem mais parsing manual de CSV
- ✅ Transações automáticas
- ✅ Melhor performance

**Aproveite! 🚀**
