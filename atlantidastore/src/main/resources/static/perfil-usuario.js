const nomeDesenvolvedor = document.getElementById("nomeDesenvolvedor");
const perfilResumo = document.getElementById("perfilResumo");
const fotoPerfilPublico = document.getElementById("fotoPerfilPublico");
const avatarPublico = document.getElementById("avatarPublico");
const totalPublicados = document.getElementById("totalPublicados");
const totalSeguidores = document.getElementById("totalSeguidores");
const totalSeguindo = document.getElementById("totalSeguindo");
const mensagem = document.getElementById("mensagem");
const jogosPublicados = document.getElementById("jogosPublicados");
const btnSeguir = document.getElementById("btnSeguir");
const logoutButton = document.getElementById("logoutButton");

let usuarioLogadoId = null;
let perfilUsuarioId = null;

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

function renderizarBotaoSeguir(status) {
    if (!status || (!status.seguindo && !status.solicitacaoPendente)) {
        btnSeguir.textContent = "Seguir";
        btnSeguir.className = "button";
        btnSeguir.dataset.acao = "seguir";
    } else if (status.seguindo) {
        btnSeguir.textContent = "Seguindo ✔";
        btnSeguir.className = "button secondary";
        btnSeguir.dataset.acao = "deixar";
    } else if (status.solicitacaoPendente) {
        btnSeguir.textContent = "Pendente ⏳";
        btnSeguir.className = "button secondary";
        btnSeguir.dataset.acao = "cancelar";
    }
}

function renderizarFotoPublica(perfil) {
    avatarPublico.textContent = (perfil.nome || "?").trim().charAt(0).toUpperCase() || "?";

    if (perfil.fotoPerfilUrl) {
        fotoPerfilPublico.src = perfil.fotoPerfilUrl;
        fotoPerfilPublico.hidden = false;
        avatarPublico.hidden = true;
        return;
    }

    fotoPerfilPublico.removeAttribute("src");
    fotoPerfilPublico.hidden = true;
    avatarPublico.hidden = false;
}

async function carregarInfoSocial() {
    const [seguidores, seguindo] = await Promise.all([
        fetchJson(`/api/usuarios/${perfilUsuarioId}/seguidores`).catch(() => []),
        fetchJson(`/api/usuarios/${perfilUsuarioId}/seguindo`).catch(() => [])
    ]);

    totalSeguidores.textContent = seguidores?.length ?? 0;
    totalSeguindo.textContent = seguindo?.length ?? 0;

    if (usuarioLogadoId && usuarioLogadoId !== perfilUsuarioId) {
        const status = await fetchJson(`/api/usuarios/${perfilUsuarioId}/seguir`).catch(() => null);
        renderizarBotaoSeguir(status);
        btnSeguir.hidden = false;
    } else {
        btnSeguir.hidden = true;
    }
}

async function carregarPerfilPublico() {
    perfilUsuarioId = new URLSearchParams(window.location.search).get("id");

    if (!perfilUsuarioId) {
        throw new Error("Perfil de usuário não informado.");
    }

    const [perfil, biblioteca, desejos, meData] = await Promise.all([
        fetchJson(`/api/usuarios/${perfilUsuarioId}/perfil-publico`),
        fetchJson("/api/biblioteca/me").catch(() => []),
        fetchJson("/api/lista-desejos").catch(() => []),
        fetchJson("/api/auth/me").catch(() => null)
    ]);

    if (!perfil) {
        return;
    }

    usuarioLogadoId = meData?.id ?? null;

    nomeDesenvolvedor.textContent = perfil.nome;
    renderizarFotoPublica(perfil);
    perfilResumo.textContent = `Publicando na plataforma desde ${formatarData(perfil.dataCriacao)}.`;
    totalPublicados.textContent = perfil.jogosPublicados?.length ?? 0;

    if (biblioteca && desejos) {
        renderizarJogos(perfil.jogosPublicados, biblioteca, desejos);
    }

    await carregarInfoSocial();
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

btnSeguir.addEventListener("click", async () => {
    const acao = btnSeguir.dataset.acao;
    btnSeguir.disabled = true;

    try {
        if (acao === "seguir") {
            const resultado = await fetchJson(`/api/usuarios/${perfilUsuarioId}/seguir`, { method: "POST" });
            if (resultado?.solicitacaoPendente) {
                mostrarMensagem("Solicitação de seguimento enviada.");
            } else {
                mostrarMensagem("Você agora segue este usuário.");
            }
        } else if (acao === "deixar") {
            await fetchJson(`/api/usuarios/${perfilUsuarioId}/seguir`, { method: "DELETE" });
            mostrarMensagem("Você deixou de seguir este usuário.");
        } else if (acao === "cancelar") {
            await fetchJson(`/api/usuarios/${perfilUsuarioId}/solicitacao`, { method: "DELETE" });
            mostrarMensagem("Solicitação cancelada.");
        }
        await carregarInfoSocial();
    } catch (error) {
        mostrarMensagem(error.message, true);
    } finally {
        btnSeguir.disabled = false;
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
