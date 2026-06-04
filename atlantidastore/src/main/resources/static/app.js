const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const usuariosTabela = document.getElementById("usuariosTabela");
const jogosTabela = document.getElementById("jogosTabela");
const jogoForm = document.getElementById("jogoForm");
const jogoTituloInput = document.getElementById("jogoTitulo");
const jogoDescricaoInput = document.getElementById("jogoDescricao");
const jogoPrecoInput = document.getElementById("jogoPreco");
const jogoCategoriasInput = document.getElementById("jogoCategorias");
const jogoDownloadUrlInput = document.getElementById("jogoDownloadUrl");
const imprimirButton = document.getElementById("imprimirButton");
const logoutButton = document.getElementById("logoutButton");

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("erro", erro);
}

function formatarData(valor) {
    if (!valor) {
        return "-";
    }

    return new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(valor));
}

function formatarPreco(valor) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    }).format(Number(valor ?? 0));
}

function escaparHtml(valor) {
    return String(valor ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function renderizarUsuarios(usuarios) {
    if (usuarios.length === 0) {
        usuariosTabela.innerHTML = '<tr><td colspan="4">Nenhum usuário cadastrado.</td></tr>';
        return;
    }

    usuariosTabela.innerHTML = usuarios.map((usuario) => `
        <tr>
            <td>${escaparHtml(usuario.nome)}</td>
            <td>${escaparHtml(usuario.email)}</td>
            <td>${formatarData(usuario.dataCriacao)}</td>
            <td class="acoes">
                <button class="danger" type="button" data-user-id="${usuario.id}">Remover</button>
            </td>
        </tr>
    `).join("");
}

function renderizarJogos(jogos) {
    if (jogos.length === 0) {
        jogosTabela.innerHTML = '<tr><td colspan="5">Nenhum jogo publicado.</td></tr>';
        return;
    }

    jogosTabela.innerHTML = jogos.map((jogo) => `
        <tr>
            <td>
                <strong>${escaparHtml(jogo.titulo)}</strong>
                <div class="muted">${escaparHtml(jogo.descricao)}</div>
            </td>
            <td>${formatarPreco(jogo.preco)}</td>
            <td>${escaparHtml((jogo.categorias || []).join(", "))}</td>
            <td>${formatarData(jogo.dataCriacao)}</td>
            <td class="acoes">
                <button class="danger" type="button" data-game-id="${jogo.id}">Remover</button>
            </td>
        </tr>
    `).join("");
}

async function fetchJson(url, options = {}) {
    const resposta = await fetch(url, options);

    if (resposta.status === 401) {
        window.location.href = "/login";
        return null;
    }

    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(erro.mensagem || "Não foi possível concluir a operação.");
    }

    if (resposta.status === 204) {
        return null;
    }

    return resposta.json();
}

async function carregarSessao() {
    const usuario = await fetchJson("/api/auth/me");

    if (usuario) {
        usuarioLogado.textContent = `Logado como ${usuario.nome} (${usuario.email})`;
    }
}

async function carregarUsuarios() {
    const usuarios = await fetchJson("/api/usuarios");

    if (usuarios) {
        renderizarUsuarios(usuarios);
    }
}

async function carregarJogos() {
    const jogos = await fetchJson("/api/jogos");

    if (jogos) {
        renderizarJogos(jogos);
    }
}

async function publicarJogo(event) {
    event.preventDefault();

    const categorias = jogoCategoriasInput.value
        .split(",")
        .map((categoria) => categoria.trim())
        .filter(Boolean);

    await fetchJson("/api/jogos", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            titulo: jogoTituloInput.value,
            descricao: jogoDescricaoInput.value,
            preco: jogoPrecoInput.value,
            categorias,
            downloadUrl: jogoDownloadUrlInput.value
        })
    });

    jogoForm.reset();
    mostrarMensagem("Jogo publicado com sucesso.");
    await carregarJogos();
}

async function removerUsuario(id) {
    await fetchJson(`/api/usuarios/${id}`, {
        method: "DELETE"
    });

    mostrarMensagem("Usuário removido com sucesso.");
    await Promise.all([carregarUsuarios(), carregarJogos()]);
}

async function removerJogo(id) {
    await fetchJson(`/api/jogos/${id}`, {
        method: "DELETE"
    });

    mostrarMensagem("Jogo removido com sucesso.");
    await carregarJogos();
}

jogoForm.addEventListener("submit", async (event) => {
    try {
        await publicarJogo(event);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

imprimirButton.addEventListener("click", () => {
    window.print();
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

usuariosTabela.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-user-id]")) {
        return;
    }

    try {
        await removerUsuario(event.target.dataset.userId);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

jogosTabela.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-game-id]")) {
        return;
    }

    try {
        await removerJogo(event.target.dataset.gameId);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

Promise.all([
    carregarSessao(),
    carregarUsuarios(),
    carregarJogos()
]).catch((error) => mostrarMensagem(error.message, true));
