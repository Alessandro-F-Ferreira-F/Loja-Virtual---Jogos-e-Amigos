const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const usuariosTabela = document.getElementById("usuariosTabela");
const jogosTabela = document.getElementById("jogosTabela");
const jogoForm = document.getElementById("jogoForm");
const jogoTituloInput = document.getElementById("jogoTitulo");
const jogoDescricaoInput = document.getElementById("jogoDescricao");
const jogoPrecoInput = document.getElementById("jogoPreco");
const jogoCategoriasSelect = document.getElementById("jogoCategoriasSelect");
const jogoCategoriasGatilho = jogoCategoriasSelect.querySelector(".multi-select-trigger");
const jogoCategoriasValor = jogoCategoriasSelect.querySelector(".multi-select-value");
const jogoCategoriasDropdown = jogoCategoriasSelect.querySelector(".multi-select-dropdown");
const jogoDownloadUrlInput = document.getElementById("jogoDownloadUrl");
const imprimirButton = document.getElementById("imprimirButton");
const logoutButton = document.getElementById("logoutButton");

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
    jogoCategoriasSelect.classList.remove("open");
    jogoCategoriasGatilho.setAttribute("aria-expanded", "false");
}

jogoCategoriasGatilho.addEventListener("click", (event) => {
    event.stopPropagation();
    const aberto = jogoCategoriasSelect.classList.toggle("open");
    jogoCategoriasGatilho.setAttribute("aria-expanded", String(aberto));
});

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
            titulo: jogoTituloInput.value,
            descricao: jogoDescricaoInput.value,
            preco: parseInt(digitosCentavos || "0", 10) / 100,
            categorias,
            downloadUrl: jogoDownloadUrlInput.value
        })
    });

    jogoForm.reset();
    digitosCentavos = "";
    jogoCategoriasDropdown.querySelectorAll("input[type=checkbox]").forEach((cb) => { cb.checked = false; });
    atualizarLabelCategorias();
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
