const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const listaDesejosContainer = document.getElementById("listaDesejos");
const logoutButton = document.getElementById("logoutButton");

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("erro", erro);
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
        const texto = await resposta.text().catch(() => "");
        let erro = {};
        try { erro = texto ? JSON.parse(texto) : {}; } catch { erro = { mensagem: texto }; }
        throw new Error(erro.mensagem || "Não foi possível concluir a operação.");
    }

    if (resposta.status === 204) {
        return null;
    }

    const textoCorpo = await resposta.text();
    return textoCorpo ? JSON.parse(textoCorpo) : null;
}

async function carregarSessao() {
    const usuario = await fetchJson("/api/auth/me");
    if (usuario) {
        usuarioLogado.textContent = `Logado como ${usuario.nome} (${usuario.email})`;
    }
}

function renderizarLista(jogos, biblioteca) {
    if (!Array.isArray(jogos)) throw new Error("Resposta inválida da API.");

    if (jogos.length === 0) {
        listaDesejosContainer.innerHTML = '<p class="empty">Sua lista de desejos está vazia. Volte ao Feed para adicionar jogos!</p>';
        return;
    }

    const idsBiblioteca = new Set(biblioteca.map((j) => j.id || j.jogoId));

    listaDesejosContainer.innerHTML = jogos.map((jogo) => `
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
                </div>
                ${idsBiblioteca.has(jogo.id) ? '<span class="muted">✔ Já na biblioteca</span>' : `<button type="button" data-add-game-id="${jogo.id}">Adicionar à biblioteca</button>`}
                <button type="button" class="danger" data-remove-wishlist-id="${jogo.id}">Remover da lista</button>
            </div>
        </article>
    `).join("");
}

async function carregarListaDesejos() {
    const [jogos, biblioteca] = await Promise.all([
        fetchJson("/api/lista-desejos"),
        fetchJson("/api/biblioteca").catch(() => []) // Ignora erro caso a biblioteca falhe
    ]);

    if (jogos && biblioteca) {
        renderizarLista(jogos, biblioteca);
    }
}

async function removerDaLista(id) {
    await fetchJson(`/api/lista-desejos/${id}`, { method: "DELETE" });
    mostrarMensagem("Jogo removido da sua lista.");
    await carregarListaDesejos(); // Recarrega a tela atualizada sem o jogo removido
}

async function adicionarBiblioteca(id) {
    await fetchJson(`/api/biblioteca/${id}`, { method: "POST" });
    mostrarMensagem("Jogo adicionado à biblioteca com sucesso.");
    await carregarListaDesejos(); // Recarrega a tela para ocultar o botão
}

listaDesejosContainer.addEventListener("click", async (event) => {
    try {
        if (event.target.matches("button[data-remove-wishlist-id]")) {
            await removerDaLista(event.target.dataset.removeWishlistId);
        } else if (event.target.matches("button[data-add-game-id]")) {
            await adicionarBiblioteca(event.target.dataset.addGameId);
        }
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/login";
});

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
carregarListaDesejos().catch((error) => {
    listaDesejosContainer.innerHTML = '<p class="empty">Não foi possível carregar a lista de desejos.</p>';
    mostrarMensagem(error.message, true);
});