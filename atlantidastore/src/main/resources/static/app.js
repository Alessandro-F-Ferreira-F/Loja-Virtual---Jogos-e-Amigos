const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const tabela = document.getElementById("usuariosTabela");
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
        tabela.innerHTML = '<tr><td colspan="4">Nenhum usuário cadastrado.</td></tr>';
        return;
    }

    tabela.innerHTML = usuarios.map((usuario) => `
        <tr>
            <td>${escaparHtml(usuario.nome)}</td>
            <td>${escaparHtml(usuario.email)}</td>
            <td>${formatarData(usuario.dataCriacao)}</td>
            <td class="acoes">
                <button class="danger" type="button" data-id="${usuario.id}">Remover</button>
            </td>
        </tr>
    `).join("");
}

async function carregarSessao() {
    const resposta = await fetch("/api/auth/me");

    if (resposta.status === 401) {
        window.location.href = "/login";
        return;
    }

    if (!resposta.ok) {
        throw new Error("Não foi possível carregar a sessão.");
    }

    const usuario = await resposta.json();
    usuarioLogado.textContent = `Logado como ${usuario.nome} (${usuario.email})`;
}

async function carregarUsuarios() {
    const resposta = await fetch("/api/usuarios");

    if (resposta.status === 401) {
        window.location.href = "/login";
        return;
    }

    if (!resposta.ok) {
        throw new Error("Não foi possível carregar os usuários.");
    }

    const usuarios = await resposta.json();
    renderizarUsuarios(usuarios);
}

async function removerUsuario(id) {
    const resposta = await fetch(`/api/usuarios/${id}`, {
        method: "DELETE"
    });

    if (resposta.status === 401) {
        window.location.href = "/login";
        return;
    }

    if (!resposta.ok) {
        const erro = await resposta.json();
        throw new Error(erro.mensagem || "Não foi possível remover o usuário.");
    }

    mostrarMensagem("Usuário removido com sucesso.");
    await carregarUsuarios();
}

imprimirButton.addEventListener("click", () => {
    window.print();
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

tabela.addEventListener("click", async (event) => {
    if (!event.target.matches("button[data-id]")) {
        return;
    }

    try {
        await removerUsuario(event.target.dataset.id);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

Promise.all([
    carregarSessao(),
    carregarUsuarios()
]).catch((error) => mostrarMensagem(error.message, true));
