const nomeDesenvolvedor = document.getElementById("nomeDesenvolvedor");
const perfilResumo = document.getElementById("perfilResumo");
const totalPublicados = document.getElementById("totalPublicados");
const mensagem = document.getElementById("mensagem");
const jogosPublicados = document.getElementById("jogosPublicados");
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

function renderizarJogos(jogos) {
    if (!jogos || jogos.length === 0) {
        jogosPublicados.innerHTML = '<p class="empty">Este usuário ainda não publicou jogos.</p>';
        return;
    }

    jogosPublicados.innerHTML = jogos.map((jogo) => `
        <article class="game-card">
            ${jogo.imagemCapa ? `<img class="game-cover" src="${escaparHtml(jogo.imagemCapa)}" alt="Capa de ${escaparHtml(jogo.nome)}">` : '<div class="game-cover placeholder">Sem capa</div>'}
            <div class="game-body">
                <div class="game-heading">
                    <h3>${escaparHtml(jogo.nome)}</h3>
                    <strong>${formatarPreco(jogo.preco)}</strong>
                </div>
                <p>${escaparHtml(jogo.descricao)}</p>
                <div class="game-meta">
                    <span>${escaparHtml(jogo.tags || "Sem tags")}</span>
                    <span>${formatarData(jogo.dataPublicacao)}</span>
                </div>
                <button type="button" data-add-game-id="${jogo.id}">Adicionar à biblioteca</button>
            </div>
        </article>
    `).join("");
}

async function carregarPerfilPublico() {
    const usuarioId = new URLSearchParams(window.location.search).get("id");

    if (!usuarioId) {
        throw new Error("Perfil de usuário não informado.");
    }

    const perfil = await fetchJson(`/api/usuarios/${usuarioId}/perfil-publico`);

    if (!perfil) {
        return;
    }

    nomeDesenvolvedor.textContent = perfil.nome;
    perfilResumo.textContent = `Publicando na plataforma desde ${formatarData(perfil.dataCriacao)}.`;
    totalPublicados.textContent = perfil.jogosPublicados?.length ?? 0;
    renderizarJogos(perfil.jogosPublicados);
}

async function adicionarBiblioteca(id) {
    await fetchJson(`/api/biblioteca/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à biblioteca.");
}

jogosPublicados.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-add-game-id]")) {
        return;
    }

    try {
        await adicionarBiblioteca(event.target.dataset.addGameId);
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

carregarPerfilPublico().catch((error) => mostrarMensagem(error.message, true));
