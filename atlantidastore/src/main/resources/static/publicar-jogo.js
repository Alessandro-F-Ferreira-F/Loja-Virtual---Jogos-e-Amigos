const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const jogoForm = document.getElementById("jogoForm");
const jogoNomeInput = document.getElementById("jogoNome");
const jogoDescricaoInput = document.getElementById("jogoDescricao");
const jogoPrecoInput = document.getElementById("jogoPreco");
const jogoTagsInput = document.getElementById("jogoTags");
const jogoImagemInput = document.getElementById("jogoImagem");
const imagemPreview = document.getElementById("imagemPreview");
const previewPlaceholder = document.getElementById("previewPlaceholder");
const logoutButton = document.getElementById("logoutButton");

let imagemCapa = null;

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("erro", erro);
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

function lerImagemComoDataUrl(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = () => reject(new Error("Não foi possível ler a imagem."));
        reader.readAsDataURL(file);
    });
}

jogoImagemInput.addEventListener("change", async () => {
    const file = jogoImagemInput.files[0];

    if (!file) {
        imagemCapa = null;
        imagemPreview.removeAttribute("src");
        previewPlaceholder.hidden = false;
        return;
    }

    try {
        imagemCapa = await lerImagemComoDataUrl(file);
        imagemPreview.src = imagemCapa;
        previewPlaceholder.hidden = true;
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

jogoForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
        await fetchJson("/api/jogos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nome: jogoNomeInput.value,
                descricao: jogoDescricaoInput.value,
                preco: Number(jogoPrecoInput.value),
                tags: jogoTagsInput.value,
                imagemCapa
            })
        });

        window.location.href = "/";
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

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
