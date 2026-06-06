# 🔧 Troubleshooting - Soluções para Problemas Comuns

## ❌ Erro ao Compilar

### **Erro: "cannot find symbol" ou imports não resolvem**

```
[ERROR] /Project/.../Usuario.java:[X]: error: cannot find symbol
[ERROR]   symbol:   class Entity
[ERROR]   location: package jakarta.persistence
```

**Solução:**

```bash
# Limpar cache e resolver dependências
mvn clean
mvn dependency:resolve
mvn compile
```

---

### **Erro: "Plugin execution not covered"**

```
[WARNING] Using platform encoding (UTF-8) to copy filtered resources...
[WARNING] maven-compiler-plugin does not support m2e incremental compilation
```

**Solução:** Apenas warning, pode ignorar. Se quiser remover:

1. No VS Code: `Ctrl+Shift+P` → "Maven: Add Dependencies"
2. Adicione: `m2e-core-ui`

---

## ❌ Erro ao Executar

### **Erro: "No suitable driver found for jdbc:sqlite"**

```
java.sql.SQLException: No suitable driver found for jdbc:sqlite:atlantidastore.db
```

**Solução:**

```bash
# Ensure SQLite JDBC is available
mvn dependency:tree | grep sqlite

# If missing, add:
mvn dependency:resolve

# Rebuild
mvn clean compile
```

---

### **Erro: "Table 'usuarios' already exists"**

```
org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "USUARIOS" already exists
```

**Solução:**

```bash
# Remover o banco existente
rm atlantidastore.db

# Executar novamente
mvn spring-boot:run
```

**Ou:**

```bash
# Mudar ddl-auto no application.properties
spring.jpa.hibernate.ddl-auto=drop-and-create  # Cria novo banco
```

---

### **Erro: "Cannot create table - Permission Denied"**

```
Error creating default for table: permission denied
```

**Solução:**

```bash
# Verificar permissões
ls -la atlantidastore.db

# Dar permissão
chmod 666 atlantidastore.db

# Ou remover e criar novo
rm atlantidastore.db
mvn spring-boot:run
```

---

## ⚠️ Problemas de Migração

### **Dados não foram migrados após inicialização**

**Verificar:**

1. CSVs existem em `data/`?

   ```bash
   ls -la data/
   # Deve ter: usuarios.csv, jogos.csv, sessoes.csv
   ```

2. Banco foi criado?

   ```bash
   ls -la atlantidastore.db
   ```

3. Tabelas estão vazias?
   ```bash
   sqlite3 atlantidastore.db "SELECT COUNT(*) FROM usuarios;"
   # Se retornar 0, a migração falhou
   ```

**Solução:**

- Verificar logs da `MigrationService`
- Se CSVs têm formato inválido, deletar e refazer:
  ```bash
  rm atlantidastore.db
  mvn spring-boot:run  # Refaz migração
  ```

---

### **"Migration service não executou"**

**Verificar:**

1. Spring está iniciando corretamente?

   ```
   Você deveria ver na log:
   [INFO] Iniciando migração de dados CSV para banco de dados...
   ```

2. Se não aparece, ativar logs:

   ```properties
   # application.properties
   logging.level.root=DEBUG
   logging.level.dev.osdiscretos.atlantidastore.service=DEBUG
   ```

3. Rodar novamente:
   ```bash
   mvn spring-boot:run 2>&1 | grep -i "migra\|error\|warn"
   ```

---

### **"Dados duplicados após vários starts"**

Se dados aparecem repetidos:

```bash
# Verificar dados
sqlite3 atlantidastore.db "SELECT COUNT(*) FROM usuarios;"

# Limpar banco
rm atlantidastore.db

# A próxima execução vai recriar e migrar corretamente
mvn spring-boot:run
```

---

## 🔍 Problemas de Performance

### **Aplicação está lenta**

**Verificar:**

1. Ativar query logging:

   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   logging.level.org.hibernate.SQL=DEBUG
   ```

2. Procurar por N+1 queries (muitas queries para 1 operação)

3. Índices adicionados?
   ```sql
   sqlite3 atlantidastore.db ".indices"
   ```

**Solução:**

- Adicionar índices manualmente:
  ```sql
  CREATE INDEX idx_usuarios_email ON usuarios(email);
  CREATE INDEX idx_jogos_titulo ON jogos(titulo);
  ```

---

## 🔐 Problemas de Dados

### **UUID não está sendo salvo corretamente**

```
sqlite3 atlantidastore.db "SELECT id FROM usuarios LIMIT 1;"
# Retorna NULL?
```

**Solução:** Verificar que UUID é armazenado como TEXT:

```java
@Id
@Column(columnDefinition = "TEXT")  // ← Importante!
private UUID id;
```

---

### **Categorias dos jogos aparecem vazias**

```
jogo.getCategorias()  // Retorna []
```

**Solução:** Verificar como está armazenado no banco:

```bash
sqlite3 atlantidastore.db "SELECT categorias FROM jogos LIMIT 1;"
# Deve mostrar: "RPG|Ação|Aventura"
```

Se estiver vazio, refazer migração:

```bash
rm atlantidastore.db
mvn spring-boot:run
```

---

## 🌐 Problemas de Conexão

### **Erro de conexão com banco após migração**

```
SQLException: cannot change the database connection - connection is locked
```

**Solução:**

1. Fechar todos os SQLite clients:

   ```bash
   pkill sqlite3
   ```

2. Se em VS Code, fechar terminal com banco aberto

3. Remover lock:

   ```bash
   rm atlantidastore.db-shm
   rm atlantidastore.db-wal
   ```

4. Reiniciar:
   ```bash
   mvn spring-boot:run
   ```

---

## 🔄 Reverter para CSV (Se Necessário)

Se quiser voltar aos CSVs:

### **Passo 1: Remover anotações JPA**

```java
// Usuario.java
- @Entity
- @Table(name = "usuarios")
  public class Usuario {
-   @Id
      private UUID id;
```

### **Passo 2: Restaurar RepositoryImp original**

```bash
git checkout HEAD -- src/main/java/dev/osdiscretos/atlantidastore/repository/UsuarioRepositoryImp.java
```

### **Passo 3: Remover dependências**

```xml
<!-- Remover do pom.xml -->
- <dependency>spring-boot-starter-data-jpa</dependency>
- <dependency>hibernate-community-dialect</dependency>
```

### **Passo 4: Remover banco**

```bash
rm atlantidastore.db
rm atlantidastore.db-shm  # SQLite temp files
rm atlantidastore.db-wal
```

### **Passo 5: Recompilar**

```bash
mvn clean compile
mvn spring-boot:run
```

---

## 📝 Adicionar Debug

### **Ver todas as queries SQL**

```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true

# Mais detalhes
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Resultado:**

```
Hibernate:
    select
        u1_0.id,
        u1_0.data_criacao,
        u1_0.email,
        u1_0.nome,
        u1_0.senha_hash
    from
        usuarios u1_0
```

---

### **Ver estatísticas de Hibernate**

```properties
# application.properties
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

---

## ✅ Checklist de Verificação

Quando algo não funciona, verificar na ordem:

- [ ] `mvn clean` foi executado?
- [ ] `atlantidastore.db` foi deletado?
- [ ] CSVs existem em `data/`?
- [ ] Dependências foram baixadas? (`mvn dependency:resolve`)
- [ ] Imports estão corretos? (jakarta.persistence, não javax.persistence)
- [ ] Anotações @Entity estão nos modelos?
- [ ] JPA Repositories foram criados?
- [ ] application.properties tem config SQLite?
- [ ] MigrationService está sendo chamado?
- [ ] Sem processo SQLite travado? (`pkill sqlite3`)

---

## 📞 Se Nada Funcionar

### **Option 1: Começar do Zero**

```bash
# Remover tudo
mvn clean
rm atlantidastore.db
rm -rf target/
rm -rf .mvn/

# Recomeçar
mvn clean compile
mvn spring-boot:run
```

### **Option 2: Ver Logs Completos**

```bash
# Redirecionar para arquivo
mvn spring-boot:run 2>&1 | tee build.log

# Procurar por erros
grep -i "error\|exception\|failed" build.log
```

### **Option 3: Verificar Arquivo Corrompido**

```bash
# Banco corrompido?
sqlite3 atlantidastore.db "PRAGMA integrity_check;"

# Se retornar "ok", banco está OK
# Se não, remover e recriar:
rm atlantidastore.db
mvn spring-boot:run
```

---

## 🎯 Resumo Rápido

| Problema              | Solução Rápida                             |
| --------------------- | ------------------------------------------ |
| Não compila           | `mvn clean compile`                        |
| Driver não encontrado | `mvn dependency:resolve`                   |
| Tabela já existe      | `rm atlantidastore.db`                     |
| Dados não migraram    | Verificar `data/` e logs                   |
| Lento                 | Ativar SQL debug, adicionar índices        |
| Trava                 | `pkill sqlite3`, remover `.db-shm/.db-wal` |

---

**Problema não listado? Procure na documentação ou veja os logs! 🔍**
