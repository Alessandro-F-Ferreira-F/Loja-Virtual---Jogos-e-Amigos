const mensagem = document.getElementById("mensagem");
const bibliotecaLista = document.getElementById("bibliotecaLista");
const logoutButton = document.getElementById("logoutButton");
const adminNavLink = document.getElementById("adminNavLink");

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("erro", erro);
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

function renderizarBiblioteca(jogos) {
    if (!jogos || jogos.length === 0) {
        bibliotecaLista.innerHTML = '<p class="empty">Nenhum jogo na biblioteca.</p>';
        return;
    }

    bibliotecaLista.innerHTML = jogos.map((jogo) => `
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
                <div class="game-actions">
                    <a class="button small" href="/api/jogos/${jogo.id}/download">Baixar jogo</a>
                    <button class="danger" type="button" data-remove-game-id="${jogo.id}">Remover da biblioteca</button>
                </div>
            </div>
        </article>
    `).join("");
}

async function carregarBiblioteca() {
    const jogos = await fetchJson("/api/biblioteca/me");

    if (jogos) {
        renderizarBiblioteca(jogos);
    }
}

async function removerJogo(id) {
    await fetchJson(`/api/biblioteca/${id}`, {
        method: "DELETE"
    });

    mostrarMensagem("Jogo removido da biblioteca.");
    await carregarBiblioteca();
}

bibliotecaLista.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-remove-game-id]")) {
        return;
    }

    try {
        await removerJogo(event.target.dataset.removeGameId);
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

async function carregarSessao() {
    const usuario = await fetchJson("/api/auth/me");
    if (usuario && adminNavLink && usuario.administrador) {
        adminNavLink.hidden = false;
    }
}

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
carregarBiblioteca().catch((error) => {
    bibliotecaLista.innerHTML = '<p class="empty">Não foi possível carregar sua biblioteca.</p>';
    mostrarMensagem(error.message, true);
});
