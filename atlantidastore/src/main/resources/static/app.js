const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const jogoForm = document.getElementById("jogoForm");
const jogoTituloInput = document.getElementById("jogoTitulo");
const jogoDescricaoInput = document.getElementById("jogoDescricao");
const jogoPrecoInput = document.getElementById("jogoPreco");
const jogoCategoriasSelect = document.getElementById("jogoCategoriasSelect");
const jogoCategoriasGatilho = jogoCategoriasSelect?.querySelector(".multi-select-trigger");
const jogoCategoriasValor = jogoCategoriasSelect?.querySelector(".multi-select-value");
const jogoCategoriasDropdown = jogoCategoriasSelect?.querySelector(".multi-select-dropdown");
const jogoDownloadUrlInput = document.getElementById("jogoDownloadUrl");
const feedLista = document.getElementById("feedLista");
const logoutButton = document.getElementById("logoutButton");
const adminNavLink = document.getElementById("adminNavLink");

let digitosCentavos = "";

function atualizarExibicaoPreco() {
    if (!digitosCentavos) {
        jogoPrecoInput.value = "";
        return;
    }
    const centavos = parseInt(digitosCentavos, 10);
    jogoPrecoInput.value = new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    }).format(centavos / 100);
}

if (jogoPrecoInput) {
    jogoPrecoInput.addEventListener("keydown", (event) => {
        if (event.ctrlKey || event.metaKey || event.altKey) return;
        if (/^\d$/.test(event.key)) {
            event.preventDefault();
            if (digitosCentavos.length < 10) {
                digitosCentavos += event.key;
            }
            atualizarExibicaoPreco();
        } else if (event.key === "Backspace") {
            event.preventDefault();
            digitosCentavos = digitosCentavos.slice(0, -1);
            atualizarExibicaoPreco();
        } else if (event.key !== "Tab" && event.key !== "Enter") {
            event.preventDefault();
        }
    });

    jogoPrecoInput.addEventListener("paste", (event) => {
        event.preventDefault();
        const colado = (event.clipboardData || window.clipboardData).getData("text");
        for (const c of colado.replace(/\D/g, "")) {
            if (digitosCentavos.length < 10) digitosCentavos += c;
        }
        atualizarExibicaoPreco();
    });
}

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

function renderizarFeed(jogos, biblioteca, desejos) {
    if (!Array.isArray(jogos)) {
        throw new Error("Resposta inválida ao carregar o feed.");
    }

    if (jogos.length === 0) {
        feedLista.innerHTML = '<p class="empty">Nenhum jogo publicado.</p>';
        return;
    }

    const idsBiblioteca = new Set(biblioteca.map((j) => j.id || j.jogoId));
    const idsDesejos = new Set(desejos.map((j) => j.id || j.jogoId));

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
                ${idsBiblioteca.has(jogo.id) ? '<span class="muted">✔ Na biblioteca</span>' : `<button type="button" data-add-game-id="${jogo.id}">Adicionar à biblioteca</button>`}
                ${idsDesejos.has(jogo.id) ? '<span class="muted">🤍 Na lista de desejos</span>' : `<button type="button" data-wishlist-game-id="${jogo.id}">Adicionar à lista de desejos</button>`}
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

    const textoCorpo = await resposta.text();
    return textoCorpo ? JSON.parse(textoCorpo) : null;
}

async function carregarSessao() {
    const usuario = await fetchJson("/api/auth/me");

    if (usuario) {
        usuarioLogado.textContent = `Logado como ${usuario.nome} (${usuario.email})`;

        if (adminNavLink && usuario.administrador) {
            adminNavLink.hidden = false;
        }
    }
}

async function carregarFeed() {
    const [jogos, biblioteca, desejos] = await Promise.all([
        fetchJson("/api/jogos/feed"),
        fetchJson("/api/biblioteca/me").catch(() => []),
        fetchJson("/api/lista-desejos").catch(() => [])
    ]);

    if (jogos && biblioteca && desejos) {
        renderizarFeed(jogos, biblioteca, desejos);
    }
}

function getCategoriasSelected() {
    return Array.from(jogoCategoriasDropdown.querySelectorAll("input[type=checkbox]:checked"))
        .map((cb) => cb.value);
}

function atualizarLabelCategorias() {
    const selecionadas = getCategoriasSelected();
    if (selecionadas.length === 0) {
        jogoCategoriasValor.textContent = "Selecione as categorias";
        jogoCategoriasValor.classList.remove("has-selection");
    } else {
        jogoCategoriasValor.textContent = selecionadas.join(", ");
        jogoCategoriasValor.classList.add("has-selection");
    }
}

function fecharCategorias() {
    if (jogoCategoriasSelect) {
        jogoCategoriasSelect.classList.remove("open");
        jogoCategoriasGatilho.setAttribute("aria-expanded", "false");
    }
}

if (jogoCategoriasGatilho) {
    jogoCategoriasGatilho.addEventListener("click", (event) => {
        event.stopPropagation();
        const aberto = jogoCategoriasSelect.classList.toggle("open");
        jogoCategoriasGatilho.setAttribute("aria-expanded", String(aberto));
    });
}

if (jogoCategoriasDropdown) {
    jogoCategoriasDropdown.addEventListener("click", (event) => {
        const opcao = event.target.closest(".multi-select-option");
        if (!opcao) return;
        const checkbox = opcao.querySelector("input[type=checkbox]");
        if (event.target !== checkbox) {
            checkbox.checked = !checkbox.checked;
        }
        atualizarLabelCategorias();
        event.stopPropagation();
    });
}

document.addEventListener("click", fecharCategorias);

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") fecharCategorias();
});

async function publicarJogo(event) {
    event.preventDefault();

    const categorias = getCategoriasSelected();

    await fetchJson("/api/jogos", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            nome: jogoTituloInput.value,
            descricao: jogoDescricaoInput.value,
            preco: parseInt(digitosCentavos || "0", 10) / 100,
            tags: categorias.join("|")
        })
    });

    jogoForm.reset();
    digitosCentavos = "";
    jogoCategoriasDropdown.querySelectorAll("input[type=checkbox]").forEach((cb) => { cb.checked = false; });
    atualizarLabelCategorias();
    mostrarMensagem("Jogo publicado com sucesso.");
    await carregarFeed();
}

if (jogoForm) {
    jogoForm.addEventListener("submit", publicarJogo);
}

async function adicionarBiblioteca(id) {
    await fetchJson(`/api/biblioteca/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à biblioteca.");
    await carregarFeed();
}

async function adicionarListaDesejos(id) {
    await fetchJson(`/api/lista-desejos/${id}`, {
        method: "POST"
    });

    mostrarMensagem("Jogo adicionado à lista de desejos.");
    await carregarFeed();
}

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

feedLista.addEventListener("click", async (event) => {
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

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
carregarFeed().catch((error) => {
    feedLista.innerHTML = '<p class="empty">Não foi possível carregar os jogos.</p>';
    mostrarMensagem(error.message, true);
});
