# 📚 Guia de Migração: CSV → Hibernate + JPA

## Resumo da Migração

Este documento descreve como o projeto **AtlantidaStore** foi migrado de um sistema baseado em arquivos CSV para um banco de dados relacional usando **Spring Data JPA** e **Hibernate**.

---

## 🔄 O que foi alterado

### **Antes (CSV)**

- Dados salvos em arquivos de texto: `data/usuarios.csv`, `data/jogos.csv`, `data/sessoes.csv`
- Leitura/escrita manual com parsing de CSV
- Sincronização manual com `synchronized`
- Sem índices ou otimizações

### **Depois (Banco de Dados)**

- Dados persisted em **SQLite** (`atlantidastore.db`)
- Acesso via **JPA/Hibernate**
- Transações automáticas
- Índices no banco
- Queries otimizadas

---

## 📁 Arquivos Modificados/Criados

### **Dependências (pom.xml)**

```xml
✅ spring-boot-starter-data-jpa  ← Principal! (JPA + Hibernate)
✅ hibernate-community-dialect   ← Suporte SQLite nativo
✅ sqlite-jdbc                   ← Driver SQLite (já estava)
```

### **Configuração (application.properties)**

```properties
# Novo!
spring.datasource.url=jdbc:sqlite:atlantidastore.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=validate
```

### **Modelos → Entidades JPA**

```java
✅ Usuario.java        → @Entity @Table("usuarios")
✅ Jogo.java          → @Entity @Table("jogos")
✅ Sessao.java        → @Entity @Table("sessoes")
```

### **Novos Repositories JPA**

```java
✅ JpaUsuarioRepository.java    extends JpaRepository<Usuario, UUID>
✅ JpaJogoRepository.java       extends JpaRepository<Jogo, UUID>
✅ JpaSessaoRepository.java     extends JpaRepository<Sessao, String>
```

### **Implementações Atualizadas**

```java
✅ UsuarioRepositoryImp.java   ← Agora usa JpaUsuarioRepository
✅ JogoRepository.java          ← Agora usa JpaJogoRepository
✅ SessaoRepository.java        ← Agora usa JpaSessaoRepository
```

### **Serviço de Migração (Novo)**

```java
✅ MigrationService.java
   - Implementa ApplicationRunner
   - Executa na inicialização
   - Lê CSVs e migra para BD
   - Executa apenas 1x (verifica se dados já existem)
```

---

## 🚀 Como Usar

### **1. Primeira Inicialização**

```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run
```

**O que acontece:**

1. Spring Boot inicia
2. Hibernate cria as tabelas automaticamente
3. `MigrationService` detecta CSVs e migra os dados
4. Banco `atlantidastore.db` é criado
5. Aplicação já usa o novo banco!

### **2. Acessar o Banco (Opcional)**

Se quiser verificar os dados:

```bash
# Instalar SQLite CLI (caso não tenha)
# Windows: winget install sqlite
# Linux: sudo apt install sqlite3
# macOS: brew install sqlite

# Conectar ao banco
sqlite3 atlantidastore.db

# Ver tabelas
.tables

# Ver dados (exemplo)
SELECT * FROM usuarios;
SELECT * FROM jogos;
SELECT * FROM sessoes;
```

### **3. Controllers (Sem mudanças necessárias!)**

Os Controllers **continuam funcionando igual**:

```java
@Autowired
private UsuarioRepository usuarioRepository;  // ✅ Continua funcionando
```

Internamente, `UsuarioRepository` agora chama `JpaUsuarioRepository`.

---

## 🔧 Detalhes Técnicos

### **Mapeamento de Tipos Java ↔ SQLite**

| Java                        | SQLite     | Coluna                      |
| --------------------------- | ---------- | --------------------------- |
| `UUID`                      | `TEXT`     | `columnDefinition = "TEXT"` |
| `String`                    | `TEXT`     | `length = 255`              |
| `LocalDateTime`             | `DATETIME` | Automático                  |
| `BigDecimal`                | `DECIMAL`  | `precision=10, scale=2`     |
| `List<String>` (categorias) | `TEXT`     | Delimitado por `\|`         |

### **Tratamento de Categorias**

No **modelo Jogo**:

```java
// Armazenado no BD como: "RPG|Ação|Aventura"
private String categorias;

// Retorna como: ["RPG", "Ação", "Aventura"]
public List<String> getCategorias() {
    return Arrays.stream(categorias.split("\\|"))
        .map(String::trim)
        .collect(Collectors.toList());
}
```

### **Queries Customizadas**

Os JPA Repositories têm queries derivadas:

```java
// JpaUsuarioRepository
Optional<Usuario> findByEmailIgnoreCase(String email);
boolean existsByEmailIgnoreCase(String email);

// JpaJogoRepository
boolean existsByTituloIgnoreCase(String titulo);
List<Jogo> findAllByOrderByDataCriacaoAsc();

// JpaSessaoRepository
Optional<Sessao> findByToken(String token);
void deleteByUsuarioId(UUID usuarioId);
int deleteByExpiraEmBefore(LocalDateTime dataLimite);
```

---

## 📊 Diagrama de Estrutura

```
┌─────────────────────────────┐
│        Controllers          │
│  (Sem mudanças)             │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ Repository Interfaces       │
│ (UsuarioRepository, etc)    │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ Repository Implementations  │
│ (UsuarioRepositoryImp, etc) │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ JPA Repositories            │
│ (JpaUsuarioRepository, etc) │
│ extends JpaRepository       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│    Hibernate/JPA            │
│    (Entidades JPA)          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ SQLite Database             │
│ (atlantidastore.db)         │
└─────────────────────────────┘
```

---

## ✅ Checklist de Verificação

- [x] Dependências adicionadas ao pom.xml
- [x] Modelos transformados em @Entity
- [x] JPA Repositories criados
- [x] Implementações de Repository atualizadas
- [x] MigrationService criado e funcional
- [x] application.properties configurado
- [x] Controllers continuam funcionando
- [x] CSVs ainda existem (para backup/referência)

---

## 🐛 Troubleshooting

### **Erro: "Table 'usuarios' already exists"**

→ Significa que o banco foi criado. Remova `atlantidastore.db` e tente novamente.

### **Erro: "No suitable driver found for jdbc:sqlite"**

→ Falta o driver SQLite. Rode: `mvn dependency:resolve`

### **Dados não apareceram após migração**

→ Verifique que os CSVs existem em `data/`.
→ Veja os logs da aplicação para erros na MigrationService.

### **Quero voltar aos CSVs**

→ Restaure os arquivos do git/backup.
→ Remova as anotações `@Entity` dos modelos.
→ Reverta os repositories para usar leitura de arquivo.

---

## 📝 Próximos Passos Opcionais

1. **Adicionar mais índices** no banco se performance degradar
2. **Criar scripts de backup** do SQLite
3. **Adicionar versionamento** com Flyway/Liquibase
4. **Implementar auditing** (quem criou/modificou)
5. **Adicionar cascade** para deletar sessões quando usuário é removido

---

## 📞 Dúvidas?

Se encontrar problemas:

1. Verifique os logs da aplicação
2. Rode `sqlite3 atlantidastore.db .schema` para ver a estrutura
3. Verifique que os CSVs têm formato correto

---

**Migração concluída com sucesso! 🎉**
