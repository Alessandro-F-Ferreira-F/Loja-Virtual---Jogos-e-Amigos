const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const feedLista = document.getElementById("feedLista");
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

function renderizarFeed(jogos) {
    if (!Array.isArray(jogos)) {
        throw new Error("Resposta inválida ao carregar o feed.");
    }

    if (jogos.length === 0) {
        feedLista.innerHTML = '<p class="empty">Nenhum jogo publicado.</p>';
        return;
    }

    feedLista.innerHTML = jogos.map((jogo) => `
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
                <button type="button" data-add-game-id="${jogo.id}">Adicionar à biblioteca</button>
            </div>
        </article>
    `).join("");
}

async function fetchJson(url, options = {}) {
    const resposta = await fetch(url, options);

    if (resposta.status === 401) {
        window.location.href = "/login";
        return null;
    }

    if (!resposta.ok) {
        const texto = await resposta.text().catch(() => "");
        let erro = {};

        try {
            erro = texto ? JSON.parse(texto) : {};
        } catch {
            erro = { mensagem: texto };
        }

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

async function carregarFeed() {
    const jogos = await fetchJson("/api/jogos/feed");

    if (jogos) {
        renderizarFeed(jogos);
    }
}

async function adicionarBiblioteca(id) {
    await fetchJson(`/api/biblioteca/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à biblioteca.");
}

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

feedLista.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-add-game-id]")) {
        return;
    }

    try {
        await adicionarBiblioteca(event.target.dataset.addGameId);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
carregarFeed().catch((error) => {
    feedLista.innerHTML = '<p class="empty">Não foi possível carregar os jogos.</p>';
    mostrarMensagem(error.message, true);
});
