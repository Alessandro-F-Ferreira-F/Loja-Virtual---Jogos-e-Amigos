const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const usuarioForm = document.getElementById("usuarioForm");
const novoNomeInput = document.getElementById("novoNome");
const novoEmailInput = document.getElementById("novoEmail");
const novaSenhaInput = document.getElementById("novaSenha");
const usuariosTabela = document.getElementById("usuariosTabela");
const jogosLista = document.getElementById("jogosLista");
const logoutButton = document.getElementById("logoutButton");

let usuarioAtual = null;

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

async function extrairMensagemErro(resposta, fallback) {
    const texto = await resposta.text().catch(() => "");

    if (!texto) {
        return fallback;
    }

    try {
        return JSON.parse(texto).mensagem || fallback;
    } catch {
        return texto;
    }
}

async function fetchJson(url, options = {}) {
    const resposta = await fetch(url, options);

    if (resposta.status === 401) {
        window.location.href = "/login";
        return null;
    }

    if (!resposta.ok) {
        throw new Error(await extrairMensagemErro(resposta, "Não foi possível concluir a operação."));
    }

    if (resposta.status === 204) {
        return null;
    }

    return resposta.json();
}

async function carregarSessao() {
    usuarioAtual = await fetchJson("/api/auth/me");

    if (!usuarioAtual) {
        return;
    }

    if (!usuarioAtual.administrador) {
        mostrarMensagem("Acesso administrativo obrigatório.", true);
        document.querySelectorAll("button, input").forEach((elemento) => {
            elemento.disabled = true;
        });
        return;
    }

    usuarioLogado.textContent = `Administrador: ${usuarioAtual.nome} (${usuarioAtual.email})`;
}

function renderizarUsuarios(usuarios) {
    if (!Array.isArray(usuarios) || usuarios.length === 0) {
        usuariosTabela.innerHTML = '<tr><td colspan="5">Nenhum usuário cadastrado.</td></tr>';
        return;
    }

    usuariosTabela.innerHTML = usuarios.map((usuario) => `
        <tr>
            <td>${escaparHtml(usuario.nome)}</td>
            <td>${escaparHtml(usuario.email)}</td>
            <td>${usuario.administrador ? "Administrador" : "Usuário"}</td>
            <td>${formatarData(usuario.dataCriacao)}</td>
            <td class="acoes">
                ${usuarioAtual && usuario.id === usuarioAtual.id
                    ? '<span class="muted">Conta atual</span>'
                    : `<button class="danger small" type="button" data-remove-user-id="${usuario.id}">Remover</button>`}
            </td>
        </tr>
    `).join("");
}

function renderizarJogos(jogos) {
    if (!Array.isArray(jogos) || jogos.length === 0) {
        jogosLista.innerHTML = '<p class="empty">Nenhum jogo publicado.</p>';
        return;
    }

    jogosLista.innerHTML = jogos.map((jogo) => `
        <article class="game-card">
            ${jogo.imagemCapaUrl ? `<img class="game-cover" src="${escaparHtml(jogo.imagemCapaUrl)}" alt="Capa de ${escaparHtml(jogo.nome)}">` : '<div class="game-cover placeholder">Sem capa</div>'}
            <div class="game-body">
                <div class="game-heading">
                    <h3>${escaparHtml(jogo.nome)}</h3>
                    <strong>${formatarPreco(jogo.preco)}</strong>
                </div>
                <p>${escaparHtml(jogo.descricao)}</p>
                <div class="game-meta">
                    <span>${escaparHtml(jogo.tags || "Sem tags")}</span>
                    <a class="publisher-link" href="/perfil-usuario?id=${jogo.desenvolvedorId}">${escaparHtml(jogo.desenvolvedorNome || "Desenvolvedor")}</a>
                    <span>${formatarData(jogo.dataPublicacao)}</span>
                </div>
            </div>
        </article>
    `).join("");
}

async function carregarAdmin() {
    const [usuarios, jogos] = await Promise.all([
        fetchJson("/api/admin/usuarios"),
        fetchJson("/api/admin/jogos")
    ]);

    renderizarUsuarios(usuarios);
    renderizarJogos(jogos);
}

async function criarUsuario(event) {
    event.preventDefault();

    const botao = usuarioForm.querySelector('button[type="submit"]');
    botao.disabled = true;

    try {
        await fetchJson("/api/admin/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nome: novoNomeInput.value,
                email: novoEmailInput.value,
                senha: novaSenhaInput.value
            })
        });

        usuarioForm.reset();
        mostrarMensagem("Usuário criado.");
        await carregarAdmin();
    } catch (error) {
        mostrarMensagem(error.message, true);
    } finally {
        botao.disabled = false;
    }
}

async function removerUsuario(id) {
    await fetchJson(`/api/admin/usuarios/${id}`, {
        method: "DELETE"
    });

    mostrarMensagem("Usuário removido.");
    await carregarAdmin();
}

usuarioForm.addEventListener("submit", criarUsuario);

usuariosTabela.addEventListener("click", async (event) => {
    const botao = event.target.closest("button[data-remove-user-id]");

    if (!botao) {
        return;
    }

    try {
        await removerUsuario(botao.dataset.removeUserId);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

carregarSessao()
    .then(() => {
        if (usuarioAtual && usuarioAtual.administrador) {
            return carregarAdmin();
        }
        return null;
    })
    .catch((error) => {
        usuariosTabela.innerHTML = '<tr><td colspan="5">Não foi possível carregar os usuários.</td></tr>';
        jogosLista.innerHTML = '<p class="empty">Não foi possível carregar os jogos.</p>';
        mostrarMensagem(error.message, true);
    });
