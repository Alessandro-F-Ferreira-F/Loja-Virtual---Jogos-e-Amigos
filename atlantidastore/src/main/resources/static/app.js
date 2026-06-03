const form = document.getElementById("usuarioForm");
const nomeInput = document.getElementById("nome");
const emailInput = document.getElementById("email");
const mensagem = document.getElementById("mensagem");
const tabela = document.getElementById("usuariosTabela");
const atualizarButton = document.getElementById("atualizarButton");
const imprimirButton = document.getElementById("imprimirButton");

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

async function carregarUsuarios() {
    const resposta = await fetch("/api/usuarios");

    if (!resposta.ok) {
        throw new Error("Não foi possível carregar os usuários.");
    }

    const usuarios = await resposta.json();
    renderizarUsuarios(usuarios);
}

async function cadastrarUsuario(event) {
    event.preventDefault();

    const usuario = {
        nome: nomeInput.value,
        email: emailInput.value
    };

    const resposta = await fetch("/api/usuarios", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(usuario)
    });

    if (!resposta.ok) {
        const erro = await resposta.json();
        throw new Error(erro.mensagem || "Não foi possível cadastrar o usuário.");
    }

    form.reset();
    mostrarMensagem("Usuário cadastrado com sucesso.");
    await carregarUsuarios();
}

async function removerUsuario(id) {
    const resposta = await fetch(`/api/usuarios/${id}`, {
        method: "DELETE"
    });

    if (!resposta.ok) {
        const erro = await resposta.json();
        throw new Error(erro.mensagem || "Não foi possível remover o usuário.");
    }

    mostrarMensagem("Usuário removido com sucesso.");
    await carregarUsuarios();
}

form.addEventListener("submit", async (event) => {
    try {
        await cadastrarUsuario(event);
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

atualizarButton.addEventListener("click", async () => {
    try {
        await carregarUsuarios();
        mostrarMensagem("Lista atualizada.");
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

imprimirButton.addEventListener("click", () => {
    window.print();
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

carregarUsuarios().catch((error) => {
    mostrarMensagem(error.message, true);
});
