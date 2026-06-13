# Integracao da feature de jogos

Este documento descreve a integracao das implementacoes da branch `features-felipe` na branch `features-caio`.

## Objetivo da integracao

A branch `features-caio` contem a implementacao mais atual de usuarios, cadastro, login, cookies de sessao, filtro manual de autenticacao e persistencia em CSV.

A branch `features-felipe` adicionou a feature de jogos, mas estava mais proxima da `main` e usava uma abordagem diferente:

- `Jogo` como entidade JPA.
- `JogoRepository` estendendo `JpaRepository`.
- dependencias de JPA/H2 no `pom.xml`.
- campos do jogo ainda incompletos para o modelo desejado.

O objetivo foi absorver a feature de jogos sem substituir a arquitetura atual de login/cadastro.

Por isso, o merge foi tratado como integracao seletiva: a implementacao de usuario/login/sessao de `features-caio` foi preservada, e a feature de jogos foi adaptada ao padrao atual do projeto.

## O que foi preservado de features-caio

Foram mantidos:

- login manual sem Spring Security;
- `AuthFilter`;
- `AuthService`;
- `LoginController`;
- `PaginaController`;
- `UsuarioController`;
- persistencia de usuarios em `data/usuarios.csv`;
- persistencia de sessoes em `data/sessoes.csv`;
- telas estaticas de login, cadastro e area autenticada.

Isso era necessario porque o merge bruto de `features-felipe` removeria varias dessas classes e voltaria a aplicacao para um estado anterior.

## O que foi aproveitado de features-felipe

Foram incorporados os conceitos principais:

- modelo `Jogo`;
- controller REST de jogos;
- service de jogos;
- repository de jogos;
- DTO de cadastro de jogo;
- DTO de resposta de jogo.

Entretanto, a implementacao foi refatorada para funcionar sem banco de dados e sem JPA.

## Arquivos adicionados

### `model/Jogo.java`

Representa um jogo publicado na plataforma.

Campos:

- `UUID id`: identificador unico do jogo.
- `String titulo`: titulo publico do jogo.
- `String descricao`: descricao textual.
- `BigDecimal preco`: preco do jogo. Foi usado `BigDecimal` em vez de `double` para evitar problemas de precisao com dinheiro.
- `UUID publicadorId`: ID do usuario que publicou o jogo.
- `List<String> categorias`: tags de categoria, como `RPG`, `Aventura`, `Primeira-pessoa`.
- `LocalDateTime dataCriacao`: data e hora de publicacao.
- `String downloadUrl`: campo reservado para futura instalacao/download.

O model nao usa `@Entity`, porque o projeto continua sem banco de dados.

### `dto/CadastrarJogoRequestDTO.java`

DTO recebido em `POST /api/jogos`.

Campos:

- `titulo`
- `descricao`
- `preco`
- `categorias`
- `downloadUrl`

### `dto/JogoResponse.java`

DTO devolvido pela API.

Ele evita expor qualquer estrutura interna desnecessaria e centraliza a conversao:

```java
public static JogoResponse from(Jogo jogo)
```

### `repository/JogoRepository.java`

Repository simples baseado em CSV.

Arquivo usado:

```text
data/jogos.csv
```

Formato:

```csv
id,titulo,descricao,preco,publicadorId,categorias,dataCriacao,downloadUrl
```

Metodos principais:

- `save(Jogo jogo)`: salva ou substitui um jogo pelo ID.
- `findById(UUID id)`: busca um jogo pelo ID.
- `findAll()`: lista todos os jogos ordenados por data de criacao.
- `existsByTituloIgnoreCase(String titulo)`: impede cadastro duplicado pelo titulo.
- `deleteById(UUID id)`: remove um jogo.

### `service/JogoService.java`

Camada de regra de negocio dos jogos.

Metodos:

- `cadastrar(CadastrarJogoRequestDTO request, Usuario publicador)`: valida dados, vincula o jogo ao usuario logado e salva no CSV.
- `listar()`: retorna todos os jogos como `JogoResponse`.
- `remover(UUID id)`: remove jogo pelo ID.

Validacoes atuais:

- titulo obrigatorio;
- preco obrigatorio;
- preco nao pode ser negativo;
- titulo nao pode estar duplicado.

### `controller/JogoController.java`

Controller REST dos jogos.

Base path:

```text
/api/jogos
```

Endpoints:

```text
POST   /api/jogos
GET    /api/jogos
DELETE /api/jogos/{id}
```

O metodo de cadastro recebe o usuario logado com:

```java
@RequestAttribute("usuarioLogado") Usuario usuarioLogado
```

Esse atributo e colocado pelo `AuthFilter` quando o cookie de sessao e valido.

## Arquivos modificados

### `pom.xml`

Foi removida a dependencia local de SQLite.

Motivo:

- o projeto nao esta usando banco de dados;
- os usuarios, sessoes e jogos estao em arquivos CSV;
- manter dependencia de banco criaria confusao sobre a arquitetura atual.

### `static/index.html`

A tela autenticada passou a ter:

- cabecalho da plataforma;
- formulario para publicar jogo;
- tabela da biblioteca central de jogos;
- tabela de usuarios cadastrados.

### `static/app.js`

Foi atualizado para chamar os novos endpoints:

- `GET /api/jogos`;
- `POST /api/jogos`;
- `DELETE /api/jogos/{id}`.

Tambem continua chamando:

- `GET /api/auth/me`;
- `GET /api/usuarios`;
- `DELETE /api/usuarios/{id}`;
- `POST /api/auth/logout`.

### `static/styles.css`

Foi ajustado para suportar:

- `textarea`;
- formulario de jogo;
- textos auxiliares;
- blocos que nao aparecem na impressao.

## Fluxo de publicacao de jogo

```text
Usuario autenticado
 -> preenche formulario de jogo
 -> app.js envia POST /api/jogos
 -> AuthFilter valida cookie
 -> AuthFilter coloca usuarioLogado na request
 -> JogoController recebe request e usuarioLogado
 -> JogoService valida os dados
 -> JogoService cria Jogo com publicadorId do usuario
 -> JogoRepository grava em data/jogos.csv
 -> JogoResponse volta como JSON
 -> frontend recarrega a biblioteca central
```

## Fluxo de listagem de jogos

```text
Frontend
 -> GET /api/jogos
 -> AuthFilter valida cookie
 -> JogoController.listar()
 -> JogoService.listar()
 -> JogoRepository.findAll()
 -> data/jogos.csv
 -> lista de JogoResponse
 -> JSON para o frontend
```

## Fluxo de remocao de jogo

```text
Frontend
 -> DELETE /api/jogos/{id}
 -> AuthFilter valida cookie
 -> JogoController.remover(id)
 -> JogoService.remover(id)
 -> JogoRepository.deleteById(id)
 -> HTTP 204 No Content
```

## Sobre a biblioteca central e bibliotecas privadas

Nesta etapa foi implementada apenas a biblioteca central:

- todos os usuarios autenticados podem publicar jogos;
- todos os usuarios autenticados podem listar os jogos publicados;
- os jogos ficam registrados em `data/jogos.csv`;
- cada jogo sabe quem publicou por meio de `publicadorId`.

A biblioteca privada do usuario ainda nao foi implementada.

Para implementar depois, o caminho simples seria criar um novo CSV:

```text
data/bibliotecas_usuarios.csv
```

Formato possivel:

```csv
usuarioId,jogoId,dataAquisicao
```

Endpoints futuros possiveis:

```text
POST /api/biblioteca/{jogoId}/adquirir
GET  /api/biblioteca
```

Assim, o jogo continua existindo na biblioteca central, e a biblioteca privada vira apenas uma relacao entre usuario e jogo.

## Campo downloadUrl

O campo `downloadUrl` foi adicionado ao model `Jogo`, ao DTO de cadastro e ao DTO de resposta.

Ele ainda nao e usado para instalar jogos.

Por enquanto, ele apenas guarda uma referencia para um futuro arquivo de instalacao, por exemplo:

```text
https://cdn.exemplo.com/jogos/meu-jogo/instalador.exe
```

## Como implementar download/instalacao depois

Uma implementacao mais robusta deveria evitar simplesmente mandar o frontend abrir qualquer URL arbitraria.

Sugestao de desenho futuro:

1. Criar um endpoint autenticado:

```text
GET /api/jogos/{id}/download
```

2. O backend valida:

- usuario esta autenticado;
- jogo existe;
- usuario possui o jogo na biblioteca privada;
- `downloadUrl` esta configurado.

3. O backend pode responder de duas formas:

- redirecionar para a URL real do arquivo;
- ou gerar uma URL temporaria de download.

4. Adicionar metadados ao jogo ou a uma tabela/CSV de builds:

```text
jogoId,versao,plataforma,downloadUrl,tamanhoBytes,sha256
```

5. O cliente local poderia verificar:

- sistema operacional;
- hash SHA-256 do arquivo baixado;
- versao instalada;
- pasta de instalacao.

6. Se a aplicacao virar um launcher, o ideal e separar:

- servidor web: autentica, lista biblioteca e autoriza download;
- cliente desktop: baixa, valida e executa instalador/jogo local.

## Pontos de atencao

- `data/jogos.csv` ainda e um armazenamento simples. Funciona para aprendizado, mas nao e ideal para alta concorrencia.
- Ainda nao existe autorizacao por dono do jogo; qualquer usuario autenticado pode remover qualquer jogo.
- Ainda nao existe biblioteca privada.
- Ainda nao existe compra/aquisicao.
- Ainda nao existe validacao forte da `downloadUrl`.
- Ainda nao existe upload de arquivo do jogo.

## Proximos passos recomendados

1. Criar biblioteca privada do usuario com um CSV de relacao `usuarioId,jogoId`.
2. Adicionar endpoint para adquirir jogo.
3. Impedir remocao de jogos por usuarios que nao publicaram o jogo.
4. Adicionar campo `publicadorNome` no `JogoResponse`, se a tela precisar mostrar o autor.
5. Criar endpoint de download autenticado.
6. Adicionar metadados de arquivo: tamanho, plataforma e hash.
7. Validar `downloadUrl` contra formatos permitidos.
8. Evoluir CSV para banco de dados quando o dominio estabilizar.
