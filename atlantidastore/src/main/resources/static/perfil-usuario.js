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

    const textoCorpo = await resposta.text();
    return textoCorpo ? JSON.parse(textoCorpo) : null;
}

function renderizarJogos(jogos, biblioteca, desejos) {
    if (!jogos || jogos.length === 0) {
        jogosPublicados.innerHTML = '<p class="empty">Este usuário ainda não publicou jogos.</p>';
        return;
    }

    const idsBiblioteca = new Set(biblioteca.map((j) => j.id || j.jogoId));
    const idsDesejos = new Set(desejos.map((j) => j.id || j.jogoId));

    jogosPublicados.innerHTML = jogos.map((jogo) => `
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
                    <span>${formatarData(jogo.dataPublicacao)}</span>
                </div>
                ${idsBiblioteca.has(jogo.id) ? '<span class="muted">✔ Na biblioteca</span>' : `<button type="button" data-add-game-id="${jogo.id}">Adicionar à biblioteca</button>`}
                ${idsDesejos.has(jogo.id) ? '<span class="muted">🤍 Na lista de desejos</span>' : `<button type="button" data-wishlist-game-id="${jogo.id}">Adicionar à lista de desejos</button>`}
            </div>
        </article>
    `).join("");
}

async function carregarPerfilPublico() {
    const usuarioId = new URLSearchParams(window.location.search).get("id");

    if (!usuarioId) {
        throw new Error("Perfil de usuário não informado.");
    }

    const [perfil, biblioteca, desejos] = await Promise.all([
        fetchJson(`/api/usuarios/${usuarioId}/perfil-publico`),
        fetchJson("/api/biblioteca").catch(() => []),
        fetchJson("/api/lista-desejos").catch(() => [])
    ]);

    if (!perfil) {
        return;
    }

    nomeDesenvolvedor.textContent = perfil.nome;
    perfilResumo.textContent = `Publicando na plataforma desde ${formatarData(perfil.dataCriacao)}.`;
    totalPublicados.textContent = perfil.jogosPublicados?.length ?? 0;
    
    if (biblioteca && desejos) {
        renderizarJogos(perfil.jogosPublicados, biblioteca, desejos);
    }
}

async function adicionarBiblioteca(id) {
    await fetchJson(`/api/biblioteca/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à biblioteca.");
    await carregarPerfilPublico();
}

async function adicionarListaDesejos(id) {
    await fetchJson(`/api/lista-desejos/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à lista de desejos.");
    await carregarPerfilPublico();
}

jogosPublicados.addEventListener("click", async (event) => {
    if (event.target.matches("button[data-add-game-id]")) {
        try {
            await adicionarBiblioteca(event.target.dataset.addGameId);
        } catch (error) {
            mostrarMensagem(error.message, true);
        }
    } else if (event.target.matches("button[data-wishlist-game-id]")) {
        try {
            await adicionarListaDesejos(event.target.dataset.wishlistGameId);
        } catch (error) {
            mostrarMensagem(error.message, true);
        }
    }
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

carregarPerfilPublico().catch((error) => {
    jogosPublicados.innerHTML = '<p class="empty">Não foi possível carregar este perfil.</p>';
    mostrarMensagem(error.message, true);
});
