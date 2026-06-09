# Arquitetura do Sistema de Cadastro e Login

Este projeto implementa uma aplicação Spring Boot simples para cadastro, login, listagem e remoção de usuários.

A decisão principal desta versão é não usar Spring Security. Em vez disso, a aplicação tem um filtro manual, `AuthFilter`, que roda antes dos controllers e decide se uma requisição pode passar ou se deve ser bloqueada/redirecionada para login.

Também não há banco de dados. Os usuários e as sessões são salvos em arquivos CSV locais:

- `data/usuarios.csv`: usuários cadastrados.
- `data/sessoes.csv`: tokens de sessão ativos.

Esses arquivos são criados automaticamente em runtime quando a aplicação precisa ler ou gravar dados.

## Estrutura Geral

O fluxo principal da aplicação é:

```text
Requisição HTTP
 -> AuthFilter
 -> Controller
 -> Service
 -> Repository
 -> CSV / Model
 -> Service
 -> Controller
 -> Response HTTP
```

As camadas têm os seguintes papéis:

- `filters`: intercepta requisições antes dos controllers.
- `controller`: recebe HTTP requests e constrói HTTP responses.
- `service`: aplica regra de negócio.
- `repository`: lê e grava dados em CSV.
- `model`: representa objetos internos da aplicação.
- `dto`: representa dados de entrada e saída da API.
- `auth`: concentra utilitários de autenticação, como nome do cookie e hash de senha.

## Classes

### AtlantidastoreApplication

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/AtlantidastoreApplication.java`

Classe principal da aplicação.

Métodos:

- `main(String[] args)`: chama `SpringApplication.run(...)` e inicializa o Spring Boot.

Annotation:

- `@SpringBootApplication`: marca a classe como ponto inicial da aplicação. Ela combina configurações automáticas, varredura de componentes e configuração Spring.

### PasswordHasher

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/auth/PasswordHasher.java`

Responsável por gerar e validar hashes de senha sem usar Spring Security.

Esta classe usa APIs do próprio JDK:

- `PBKDF2WithHmacSHA256`;
- `SecureRandom`;
- `Base64`;
- `MessageDigest.isEqual(...)`.

Métodos:

- `hash(String senha)`: recebe uma senha em texto puro, gera um salt aleatório e retorna uma string no formato:

```text
pbkdf2$iterations$saltBase64$hashBase64
```

- `matches(String senhaDigitada, String senhaSalva)`: recebe a senha digitada no login e o hash salvo no CSV. Recalcula o hash usando o salt salvo e compara os bytes com `MessageDigest.isEqual(...)`.

- `pbkdf2(String senha, byte[] salt, int iterations)`: método privado que executa o algoritmo PBKDF2.

- `encode(byte[] bytes)`: converte bytes para Base64.

- `decode(String value)`: converte Base64 de volta para bytes.

Annotation:

- `@Component`: registra a classe como bean do Spring, permitindo injeção em `UsuarioService` e `AuthService`.

### SessionKey

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/auth/SessionKey.java`

Classe simples de constante.

Campos:

- `COOKIE_NAME`: nome do cookie de sessão usado pela aplicação: `ATLANTIDA_SESSION`.

Essa classe evita espalhar strings repetidas pelo código.

### Usuario

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/model/Usuario.java`

Modelo interno de usuário.

Campos:

- `id`: UUID do usuário.
- `nome`: nome do usuário.
- `email`: e-mail normalizado.
- `senhaHash`: hash da senha. A senha real nunca deve ser salva.
- `dataCriacao`: data e hora de criação.

Construtores:

- `Usuario(String nome, String email, String senhaHash)`: usado ao criar novo usuário. Gera `UUID.randomUUID()` e `LocalDateTime.now()`.
- `Usuario(UUID id, String nome, String email, String senhaHash, LocalDateTime dataCriacao)`: usado pelo repositório ao reconstruir um usuário lido do CSV.

Métodos:

- `getId()`
- `getNome()`
- `getEmail()`
- `getSenhaHash()`
- `getDataCriacao()`

Esta classe não é uma entidade JPA. Ela não é gerenciada por banco de dados. É apenas um objeto Java comum.

### Sessao

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/model/Sessao.java`

Modelo interno de uma sessão autenticada.

Campos:

- `token`: valor aleatório gravado no cookie e no CSV.
- `usuarioId`: ID do usuário dono da sessão.
- `criadoEm`: data e hora de criação.
- `expiraEm`: data e hora em que a sessão deixa de valer.

Métodos:

- `getToken()`
- `getUsuarioId()`
- `getCriadoEm()`
- `getExpiraEm()`
- `isExpirada()`: retorna `true` quando `LocalDateTime.now()` passou de `expiraEm`.

### CadastroRequestDTO

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/dto/CadastroRequestDTO.java`

DTO de entrada para cadastro de usuário.

Campos:

- `nome`
- `email`
- `senha`

É usado pelo `UsuarioController` quando o frontend envia `POST /api/usuarios`.

### LoginRequest

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/dto/LoginRequest.java`

DTO de entrada para login.

Campos:

- `email`
- `senha`

É usado pelo `LoginController` quando o frontend envia `POST /api/auth/login`.

### UsuarioResponse

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/dto/UsuarioResponse.java`

DTO de saída usado para devolver dados de usuário ao frontend.

Campos:

- `id`
- `nome`
- `email`
- `dataCriacao`

Métodos:

- `from(Usuario usuario)`: converte um `Usuario` interno em `UsuarioResponse`.

Importante: `UsuarioResponse` não expõe `senhaHash`. Essa separação evita vazar dados sensíveis na API.

### ErroResponse

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/dto/ErroResponse.java`

DTO simples para respostas de erro.

Campo:

- `mensagem`

Exemplo de resposta:

```json
{
  "mensagem": "E-mail já cadastrado"
}
```

### UsuarioRepository

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/repository/UsuarioRepository.java`

Interface do repositório de usuários.

Métodos:

- `save(Usuario user)`: salva um usuário.
- `findByID(UUID id)`: busca usuário por ID.
- `findByEmail(String email)`: busca usuário por e-mail.
- `listAll()`: lista todos os usuários.
- `removeByID(UUID id)`: remove usuário por ID.
- `isEmailRegistered(String email)`: verifica se já existe usuário com esse e-mail.

### UsuarioRepositoryImp

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/repository/UsuarioRepositoryImp.java`

Implementação concreta do repositório de usuários usando CSV.

Arquivo usado:

```text
data/usuarios.csv
```

Formato:

```csv
id,nome,email,senhaHash,dataCriacao
```

Métodos públicos:

- `save(Usuario user)`: lê todos os usuários, remove uma versão antiga com o mesmo ID se existir, adiciona o usuário atual e regrava o CSV.
- `findByID(UUID id)`: lê o CSV e retorna o usuário com o ID informado.
- `findByEmail(String email)`: lê o CSV e compara e-mails ignorando maiúsculas/minúsculas.
- `listAll()`: lê todos os usuários e ordena pela data de criação.
- `removeByID(UUID id)`: remove o usuário do CSV.
- `isEmailRegistered(String email)`: usa `findByEmail(...)` para saber se o e-mail já existe.

Métodos privados:

- `readAll()`: lê o arquivo CSV e converte cada linha em `Usuario`.
- `writeAll(List<Usuario> users)`: regrava todo o CSV com header e usuários atualizados.
- `ensureFileExists()`: cria a pasta `data` e o arquivo `usuarios.csv` se ainda não existirem.
- `toCsv(Usuario user)`: transforma um usuário em linha CSV.
- `fromCsv(String line)`: transforma uma linha CSV em `Usuario`.
- `escape(String value)`: escapa valores com vírgula, aspas ou quebra de linha.
- `parseCsvLine(String line)`: interpreta uma linha CSV respeitando aspas.

Annotation:

- `@Repository`: registra a classe como bean de persistência. O Spring pode injetá-la onde a interface `UsuarioRepository` é exigida.

### SessaoRepository

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/repository/SessaoRepository.java`

Repositório de sessões usando CSV.

Arquivo usado:

```text
data/sessoes.csv
```

Formato:

```csv
token,usuarioId,criadoEm,expiraEm
```

Métodos públicos:

- `save(Sessao sessao)`: salva uma sessão no CSV.
- `findByToken(String token)`: busca a sessão pelo token vindo do cookie. Se estiver expirada, remove a sessão e retorna `null`.
- `removeByToken(String token)`: remove uma sessão específica.
- `removeByUsuarioId(UUID usuarioId)`: remove todas as sessões de um usuário. É usado quando um usuário é removido.
- `removeExpired()`: limpa sessões expiradas.

Métodos privados:

- `readAll()`: lê todas as sessões do CSV.
- `writeAll(List<Sessao> sessoes)`: regrava o CSV de sessões.
- `ensureFileExists()`: cria a pasta `data` e o arquivo `sessoes.csv` se necessário.
- `toCsv(Sessao sessao)`: converte sessão para linha CSV.
- `fromCsv(String line)`: converte linha CSV para `Sessao`.
- `escape(String value)`: escapa valores CSV.
- `parseCsvLine(String line)`: interpreta linha CSV.

Annotation:

- `@Repository`: registra a classe como bean de persistência.

### UsuarioService

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/service/UsuarioService.java`

Contém regras de negócio relacionadas a usuários.

Dependências:

- `UsuarioRepository`: persistência de usuários.
- `SessaoRepository`: remoção de sessões quando um usuário é excluído.
- `PasswordHasher`: hash de senha no cadastro.

Métodos:

- `register(CadastroRequestDTO request)`: valida nome, e-mail e senha; verifica e-mail duplicado; gera hash da senha; cria `Usuario`; salva no CSV; retorna `UsuarioResponse`.

Validações feitas:

- request não pode ser nulo.
- nome é obrigatório.
- e-mail é obrigatório.
- e-mail precisa conter `@`.
- senha precisa ter pelo menos 6 caracteres.
- e-mail não pode estar cadastrado.

- `listAll()`: busca todos os usuários no repositório, converte para `UsuarioResponse` e retorna.

- `remove(UUID id)`: verifica se o usuário existe; remove o usuário; remove também as sessões daquele usuário.

- `normalize(String value)`: método privado para tratar `null` e remover espaços antes/depois.

Annotation:

- `@Service`: registra a classe como bean de serviço.

### AuthService

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/service/AuthService.java`

Contém regras de autenticação e sessão.

Dependências:

- `UsuarioRepository`: busca usuário por e-mail ou ID.
- `SessaoRepository`: salva, busca e remove sessões.
- `PasswordHasher`: compara senha digitada com hash salvo.

Métodos:

- `login(LoginRequest request)`: valida o request, autentica o usuário, cria token aleatório, cria `Sessao`, limpa sessões expiradas, salva a nova sessão e retorna `LoginResult`.

- `authenticate(String email, String senhaDigitada)`: busca usuário por e-mail e compara senha digitada com o hash salvo. Se falhar, lança `IllegalArgumentException`.

- `findUserBySessionToken(String token)`: busca sessão pelo token do cookie. Se a sessão existir e não estiver expirada, busca o usuário dono da sessão. Se o usuário foi removido, a sessão também é removida.

- `logout(String token)`: remove a sessão do CSV.

- `sessionMaxAgeSeconds()`: retorna o tempo de vida da sessão em segundos. Atualmente é 30 minutos.

- `gerarToken()`: método privado que gera 32 bytes aleatórios e converte para Base64 URL-safe.

Record interno:

- `LoginResult(Usuario usuario, Sessao sessao)`: retorna juntos o usuário autenticado e a sessão criada.

Annotation:

- `@Service`: registra a classe como bean de serviço.

### AuthFilter

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/filters/AuthFilter.java`

Filtro manual de autenticação.

Essa classe substitui, de forma simples, a parte de filtragem de requisições que normalmente seria feita pelo Spring Security.

Ela roda antes dos controllers.

Dependência:

- `AuthService`: usado para validar o token do cookie.

Métodos:

- `doFilterInternal(...)`: método principal do filtro. Ele normaliza a URL, verifica se a rota é pública, lê o cookie, valida a sessão e decide se a requisição continua ou é bloqueada.

- `isPublicRequest(String method, String path)`: define manualmente quais rotas são públicas.

Rotas públicas GET:

```text
/login
/login.html
/cadastro
/cadastro.html
/styles.css
/login.js
/cadastro.js
/favicon.ico
/error
```

Rotas públicas POST:

```text
/api/auth/login
/api/auth/logout
/api/usuarios
```

Observação: `POST /api/usuarios` é público para permitir cadastro inicial de usuários.

- `readSessionToken(HttpServletRequest request)`: percorre os cookies da requisição e encontra o cookie `ATLANTIDA_SESSION`.

- `rejectUnauthenticatedRequest(...)`: se a rota é de API, responde `401 Unauthorized` com JSON. Se é página HTML, redireciona para `/login`.

- `normalizarPath(HttpServletRequest request)`: remove o context path da URL para comparar somente o caminho real da aplicação.

Annotation:

- `@Component`: registra o filtro como bean. Por estender `OncePerRequestFilter`, o Spring Web registra esse filtro na cadeia de filtros da aplicação.

### LoginController

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/controller/LoginController.java`

Controller REST para login, logout e consulta da sessão atual.

Base path:

```text
/api/auth
```

Métodos:

- `login(LoginRequest request, HttpServletResponse response)`: atende `POST /api/auth/login`. Chama `AuthService.login(...)`, recebe a sessão criada, adiciona o cookie `ATLANTIDA_SESSION` na resposta e retorna o usuário autenticado.

- `logout(String token, HttpServletResponse response)`: atende `POST /api/auth/logout`. Remove a sessão do CSV e envia um cookie expirado para apagar o cookie do navegador.

- `me(String token)`: atende `GET /api/auth/me`. Lê o cookie, busca o usuário da sessão e retorna `UsuarioResponse`. Se não houver sessão válida, retorna `401 Unauthorized`.

- `tratarRequisicaoInvalida(IllegalArgumentException exception)`: converte erros de login em `400 Bad Request` com `ErroResponse`.

- `sessionCookie(String token)`: método privado que cria o cookie de sessão.

- `expiredSessionCookie()`: método privado que cria um cookie expirado para logout.

Annotations:

- `@RestController`: indica que os retornos dos métodos são escritos diretamente no corpo da resposta HTTP.
- `@RequestMapping("/api/auth")`: define o prefixo das rotas.
- `@PostMapping`: mapeia requisições POST.
- `@GetMapping`: mapeia requisições GET.
- `@RequestBody`: converte JSON da requisição em `LoginRequest`.
- `@CookieValue`: lê o valor do cookie da requisição.
- `@ExceptionHandler`: trata exceções lançadas dentro do controller.

### UsuarioController

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/controller/UsuarioController.java`

Controller REST para cadastro, listagem e remoção de usuários.

Base path:

```text
/api/usuarios
```

Métodos:

- `register(CadastroRequestDTO request)`: atende `POST /api/usuarios`. Cadastra usuário e retorna `201 Created`.

- `listUsers()`: atende `GET /api/usuarios`. Lista todos os usuários cadastrados. Essa rota é privada porque o filtro não a libera como pública.

- `remove(UUID id)`: atende `DELETE /api/usuarios/{id}`. Remove usuário por ID e retorna `204 No Content`.

- `tratarRequisicaoInvalida(IllegalArgumentException exception)`: transforma erros de validação em `400 Bad Request`.

- `tratarUsuarioNaoEncontrado(NoSuchElementException exception)`: transforma usuário não encontrado em `404 Not Found`.

Annotations:

- `@RestController`
- `@RequestMapping("/api/usuarios")`
- `@PostMapping`
- `@GetMapping`
- `@DeleteMapping`
- `@RequestBody`
- `@PathVariable`
- `@ExceptionHandler`

### PaginaController

Arquivo: `src/main/java/dev/osdiscretos/atlantidastore/controller/PaginaController.java`

Controller simples para expor páginas estáticas em URLs amigáveis.

Métodos:

- `usuarios()`: atende `GET /` e `GET /usuarios`, encaminhando internamente para `/index.html`.
- `login()`: atende `GET /login`, encaminhando para `/login.html`.
- `cadastro()`: atende `GET /cadastro`, encaminhando para `/cadastro.html`.

Ele usa `forward:` em vez de template engine. Isso significa que o Spring encaminha a requisição para o arquivo estático dentro de `src/main/resources/static`.

Annotations:

- `@Controller`: indica controller MVC tradicional, usado aqui para retornar forward de páginas.
- `@GetMapping`: mapeia URLs GET.

## Frontend Estático

Arquivos:

- `src/main/resources/static/login.html`
- `src/main/resources/static/login.js`
- `src/main/resources/static/cadastro.html`
- `src/main/resources/static/cadastro.js`
- `src/main/resources/static/index.html`
- `src/main/resources/static/app.js`
- `src/main/resources/static/styles.css`

### login.html / login.js

Tela pública de login.

Fluxo:

1. Usuário informa e-mail e senha.
2. `login.js` envia `POST /api/auth/login`.
3. Se login for válido, o backend envia o cookie `ATLANTIDA_SESSION`.
4. O frontend redireciona para `/`.

### cadastro.html / cadastro.js

Tela pública de cadastro.

Fluxo:

1. Usuário informa nome, e-mail e senha.
2. `cadastro.js` envia `POST /api/usuarios`.
3. O backend cria o usuário e salva no CSV.
4. O frontend redireciona para `/login?cadastroSucesso`.

### index.html / app.js

Tela privada de listagem de usuários.

Fluxo:

1. O filtro exige cookie válido antes de servir `/`.
2. `app.js` chama `GET /api/auth/me` para mostrar o usuário logado.
3. `app.js` chama `GET /api/usuarios` para carregar a lista.
4. O botão remover chama `DELETE /api/usuarios/{id}`.
5. O botão sair chama `POST /api/auth/logout`.

## Annotations Utilizadas

### @SpringBootApplication

Usada na classe principal. Ativa a configuração automática do Spring Boot e a busca por componentes no pacote base.

### @Component

Usada em classes genéricas que devem virar beans do Spring.

No projeto:

- `PasswordHasher`
- `AuthFilter`

### @Repository

Especialização de `@Component` para classes de persistência.

No projeto:

- `UsuarioRepositoryImp`
- `SessaoRepository`

### @Service

Especialização de `@Component` para classes de regra de negócio.

No projeto:

- `UsuarioService`
- `AuthService`

### @Controller

Usada em controllers MVC que retornam páginas, forwards ou views.

No projeto:

- `PaginaController`

### @RestController

Combina `@Controller` com `@ResponseBody`. O retorno dos métodos vira corpo HTTP, normalmente JSON.

No projeto:

- `LoginController`
- `UsuarioController`

### @RequestMapping

Define prefixo de rota para uma classe ou rota específica.

Exemplo:

```java
@RequestMapping("/api/usuarios")
```

### @GetMapping

Mapeia requisições HTTP GET.

### @PostMapping

Mapeia requisições HTTP POST.

### @DeleteMapping

Mapeia requisições HTTP DELETE.

### @RequestBody

Diz ao Spring para converter o corpo JSON da requisição em objeto Java.

Exemplo:

```java
public ResponseEntity<UsuarioResponse> register(@RequestBody CadastroRequestDTO request)
```

### @PathVariable

Lê valores da URL.

Exemplo:

```java
DELETE /api/usuarios/{id}
```

O valor `{id}` vira parâmetro Java:

```java
@PathVariable UUID id
```

### @CookieValue

Lê um cookie da requisição.

No projeto, é usado para ler:

```text
ATLANTIDA_SESSION
```

### @ExceptionHandler

Define métodos que tratam exceções de um controller.

Exemplo:

- `IllegalArgumentException` vira `400 Bad Request`.
- `NoSuchElementException` vira `404 Not Found`.

### @Override

Annotation Java usada quando um método sobrescreve método da superclasse.

No projeto:

- `AuthFilter#doFilterInternal(...)`
- métodos da implementação `UsuarioRepositoryImp`

## Fluxos HTTP

### GET /login

Objetivo: mostrar tela de login.

Fluxo:

```text
Browser -> AuthFilter -> PaginaController -> login.html -> Response HTML
```

Detalhes:

1. O browser acessa `/login`.
2. `AuthFilter` verifica que `GET /login` é público.
3. A requisição passa.
4. `PaginaController.login()` retorna `forward:/login.html`.
5. Spring entrega o arquivo estático.

### POST /api/auth/login

Objetivo: autenticar usuário e criar cookie de sessão.

Fluxo:

```text
Browser
 -> AuthFilter
 -> LoginController
 -> AuthService
 -> UsuarioRepositoryImp
 -> PasswordHasher
 -> SessaoRepository
 -> LoginController
 -> Response com Set-Cookie
```

Detalhes:

1. `login.js` envia JSON com e-mail e senha.
2. `AuthFilter` permite `POST /api/auth/login` por ser rota pública.
3. `LoginController.login(...)` recebe o JSON em `LoginRequest`.
4. `AuthService.login(...)` chama `authenticate(...)`.
5. `UsuarioRepositoryImp.findByEmail(...)` busca usuário em `data/usuarios.csv`.
6. `PasswordHasher.matches(...)` compara senha digitada com hash salvo.
7. Se estiver correto, `AuthService` cria um token aleatório.
8. `SessaoRepository.save(...)` salva a sessão em `data/sessoes.csv`.
9. `LoginController` cria o cookie `ATLANTIDA_SESSION`.
10. A resposta HTTP contém o header `Set-Cookie`.
11. O navegador salva o cookie.

### POST /api/auth/logout

Objetivo: encerrar sessão.

Fluxo:

```text
Browser
 -> AuthFilter
 -> LoginController
 -> AuthService
 -> SessaoRepository
 -> Response com cookie expirado
```

Detalhes:

1. `app.js` envia `POST /api/auth/logout`.
2. O controller lê o cookie com `@CookieValue`.
3. `AuthService.logout(...)` remove a sessão pelo token.
4. O controller envia um cookie com `Max-Age=0`.
5. O navegador apaga o cookie.

### GET /

Objetivo: mostrar tela privada de usuários.

Fluxo autenticado:

```text
Browser
 -> AuthFilter
 -> AuthService
 -> SessaoRepository
 -> UsuarioRepositoryImp
 -> PaginaController
 -> index.html
 -> Response HTML
```

Detalhes:

1. Browser acessa `/`.
2. `AuthFilter` vê que `/` não é rota pública.
3. O filtro procura o cookie `ATLANTIDA_SESSION`.
4. `AuthService.findUserBySessionToken(...)` valida a sessão.
5. Se a sessão for válida, a requisição continua.
6. `PaginaController.usuarios()` encaminha para `index.html`.

Fluxo não autenticado:

```text
Browser -> AuthFilter -> redirect /login
```

### GET /api/auth/me

Objetivo: retornar o usuário logado.

Fluxo:

```text
Browser
 -> AuthFilter
 -> LoginController
 -> AuthService
 -> SessaoRepository
 -> UsuarioRepositoryImp
 -> Response JSON
```

Detalhes:

1. `app.js` chama `/api/auth/me`.
2. O filtro exige sessão válida.
3. O controller lê o cookie.
4. O service busca o usuário da sessão.
5. Retorna `UsuarioResponse`.

### GET /cadastro

Objetivo: mostrar tela pública de cadastro.

Fluxo:

```text
Browser -> AuthFilter -> PaginaController -> cadastro.html -> Response HTML
```

### POST /api/usuarios

Objetivo: cadastrar usuário.

Fluxo:

```text
Browser
 -> AuthFilter
 -> UsuarioController
 -> UsuarioService
 -> PasswordHasher
 -> UsuarioRepositoryImp
 -> Response JSON
```

Detalhes:

1. `cadastro.js` envia JSON com nome, e-mail e senha.
2. `AuthFilter` permite `POST /api/usuarios` por ser rota pública.
3. `UsuarioController.register(...)` recebe `CadastroRequestDTO`.
4. `UsuarioService.register(...)` valida dados.
5. `PasswordHasher.hash(...)` gera hash da senha.
6. `UsuarioRepositoryImp.save(...)` grava em `data/usuarios.csv`.
7. Controller retorna `201 Created` com `UsuarioResponse`.

### GET /api/usuarios

Objetivo: listar usuários.

Fluxo:

```text
Browser
 -> AuthFilter
 -> UsuarioController
 -> UsuarioService
 -> UsuarioRepositoryImp
 -> Response JSON
```

Detalhes:

1. `app.js` chama `GET /api/usuarios`.
2. `AuthFilter` exige cookie válido.
3. `UsuarioController.listUsers()` chama `UsuarioService.listAll()`.
4. O service busca usuários no CSV.
5. Cada `Usuario` vira `UsuarioResponse`.
6. A lista é retornada como JSON.

### DELETE /api/usuarios/{id}

Objetivo: remover usuário.

Fluxo:

```text
Browser
 -> AuthFilter
 -> UsuarioController
 -> UsuarioService
 -> UsuarioRepositoryImp
 -> SessaoRepository
 -> Response 204
```

Detalhes:

1. `app.js` chama `DELETE /api/usuarios/{id}`.
2. `AuthFilter` exige cookie válido.
3. `UsuarioController.remove(...)` recebe o ID da URL.
4. `UsuarioService.remove(...)` verifica se o usuário existe.
5. `UsuarioRepositoryImp.removeByID(...)` remove do CSV.
6. `SessaoRepository.removeByUsuarioId(...)` remove sessões desse usuário.
7. Controller retorna `204 No Content`.

## Como Funcionam Session Cookies

Cookie é um pequeno dado que o servidor pede para o navegador guardar.

Quando o backend responde login com:

```text
Set-Cookie: ATLANTIDA_SESSION=token; Path=/; HttpOnly; Max-Age=1800
```

o navegador salva esse cookie.

Depois, em novas requisições para a mesma aplicação, o navegador envia automaticamente:

```text
Cookie: ATLANTIDA_SESSION=token
```

O usuário não precisa reenviar e-mail e senha a cada request. O backend passa a identificar o usuário pelo token de sessão.

### Como o projeto cria session cookie

1. Usuário faz login em `POST /api/auth/login`.
2. `AuthService.login(...)` autentica e cria uma `Sessao`.
3. A sessão tem:

```text
token
usuarioId
criadoEm
expiraEm
```

4. `SessaoRepository.save(...)` grava a sessão em `data/sessoes.csv`.
5. `LoginController.sessionCookie(...)` cria um cookie:

```java
Cookie cookie = new Cookie(SessionKey.COOKIE_NAME, token);
cookie.setHttpOnly(true);
cookie.setPath("/");
cookie.setMaxAge(authService.sessionMaxAgeSeconds());
```

6. `HttpServletResponse.addCookie(...)` adiciona esse cookie na resposta.
7. O navegador passa a enviar esse cookie nas próximas requisições.

### Como o projeto salva sessões

As sessões são salvas em:

```text
data/sessoes.csv
```

Cada linha contém:

```text
token,usuarioId,criadoEm,expiraEm
```

Isso permite que a sessão continue existindo mesmo se a aplicação for reiniciada, desde que:

- o arquivo CSV continue existindo;
- a sessão ainda não tenha expirado;
- o usuário dono da sessão ainda exista.

### Como o projeto recupera sessão em nova requisição

1. Browser acessa uma rota privada, por exemplo `/`.
2. Browser envia automaticamente o cookie `ATLANTIDA_SESSION`.
3. `AuthFilter` roda antes do controller.
4. O filtro chama `readSessionToken(...)`.
5. O filtro chama `AuthService.findUserBySessionToken(token)`.
6. `AuthService` chama `SessaoRepository.findByToken(token)`.
7. O repositório procura o token em `data/sessoes.csv`.
8. Se a sessão estiver expirada, remove e retorna `null`.
9. Se a sessão for válida, o service busca o usuário em `UsuarioRepository`.
10. Se o usuário existir, a requisição passa para o controller.
11. Se não existir, a sessão é removida e a requisição é bloqueada.

### Como o projeto remove sessão no logout

1. Frontend chama `POST /api/auth/logout`.
2. `LoginController.logout(...)` lê o cookie com `@CookieValue`.
3. `AuthService.logout(token)` remove a sessão do CSV.
4. O controller envia outro cookie com o mesmo nome, mas `Max-Age=0`.
5. O navegador apaga o cookie.

### Por que o cookie é HttpOnly

O cookie é criado com:

```java
cookie.setHttpOnly(true);
```

Isso impede que JavaScript leia o valor do cookie com `document.cookie`.

O navegador ainda envia o cookie automaticamente nas requisições, mas o token fica menos exposto a scripts da página.

## Sumário do que foi implementado nesta refatoração

- Remoção da dependência de Spring Security.
- Hash de senha com `PasswordHasher` usando JDK.
- Cadastro real com nome, e-mail e senha.
- Login real com validação de senha.
- Cookie de sessão próprio.
- Persistência de usuários em CSV.
- Persistência de sessões em CSV.
- Filtro manual de rotas públicas e privadas.
- Tela pública de login.
- Tela pública de cadastro.
- Tela privada de listagem, remoção, impressão e logout.

## Próximos Passos Para Robustez

1. Adicionar CSRF token para proteger ações com cookie, principalmente `POST` e `DELETE`.

2. Usar HTTPS em produção e marcar o cookie como `Secure`.

3. Adicionar `SameSite=Lax` ou `SameSite=Strict` no cookie.

4. Criar controle de papéis/perfis, por exemplo `ADMIN` e `USER`.

5. Impedir que um usuário comum remova outros usuários.

6. Criar confirmação antes de remover usuário no frontend.

7. Criar endpoint de alteração de senha.

8. Criar recuperação de senha.

9. Implementar rate limit para login, evitando tentativa ilimitada de senha.

10. Melhorar validação de e-mail.

11. Criar testes automatizados para services, repositories e controllers.

12. Separar melhor erros de autenticação (`401`) e autorização (`403`).

13. Migrar de CSV para banco de dados quando a aplicação precisar suportar mais dados ou concorrência real.

14. Adicionar logs de login, logout e falhas de autenticação.

15. Remover sessões antigas periodicamente com uma tarefa agendada.

16. Criar layout mais completo para mensagens de erro e sucesso.

17. Adicionar paginação na listagem de usuários.

18. Revisar a estratégia de armazenamento de CSV caso múltiplas instâncias da aplicação sejam executadas ao mesmo tempo.
