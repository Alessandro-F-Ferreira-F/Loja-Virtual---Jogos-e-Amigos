const usuarioLogado = document.getElementById("usuarioLogado");
const mensagem = document.getElementById("mensagem");
const jogoForm = document.getElementById("jogoForm");
const jogoNomeInput = document.getElementById("jogoNome");
const jogoDescricaoInput = document.getElementById("jogoDescricao");
const jogoPrecoInput = document.getElementById("jogoPreco");
const jogoImagemInput = document.getElementById("jogoImagem");
const imagemPreview = document.getElementById("imagemPreview");
const previewPlaceholder = document.getElementById("previewPlaceholder");
const logoutButton = document.getElementById("logoutButton");

const jogoTagsSelect = document.getElementById("jogoTagsSelect");
const jogoTagsTrigger = document.getElementById("jogoTagsTrigger");
const jogoTagsValue = document.getElementById("jogoTagsValue");
const jogoTagsDropdown = document.getElementById("jogoTagsDropdown");
const tagsCheckboxes = jogoTagsDropdown ? jogoTagsDropdown.querySelectorAll('input[type="checkbox"]') : [];

const MAX_IMAGE_BYTES = 2 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = new Set(["image/png", "image/jpeg", "image/gif", "image/webp"]);
let imagemCapa = null;

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

    if (!ALLOWED_IMAGE_TYPES.has(file.type)) {
        jogoImagemInput.value = "";
        imagemCapa = null;
        imagemPreview.removeAttribute("src");
        previewPlaceholder.hidden = false;
        mostrarMensagem("A capa deve ser PNG, JPEG, GIF ou WebP.", true);
        return;
    }

    if (file.size > MAX_IMAGE_BYTES) {
        jogoImagemInput.value = "";
        imagemCapa = null;
        imagemPreview.removeAttribute("src");
        previewPlaceholder.hidden = false;
        mostrarMensagem("A capa deve ter no máximo 2 MB.", true);
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

if (jogoTagsTrigger) {
    jogoTagsTrigger.addEventListener("click", () => {
        jogoTagsSelect.classList.toggle("open");
    });

    document.addEventListener("click", (event) => {
        if (!jogoTagsSelect.contains(event.target)) {
            jogoTagsSelect.classList.remove("open");
        }
    });

    tagsCheckboxes.forEach(cb => {
        cb.addEventListener("change", () => {
            const selecionadas = Array.from(tagsCheckboxes)
                .filter(c => c.checked)
                .map(c => c.value);

            if (selecionadas.length === 0) {
                jogoTagsValue.textContent = "Selecione as categorias...";
                jogoTagsValue.classList.remove("has-selection");
            } else {
                jogoTagsValue.textContent = selecionadas.join(", ");
                jogoTagsValue.classList.add("has-selection");
            }
        });
    });
}

// Máscara de moeda para o input de preço
jogoPrecoInput.addEventListener("input", (e) => {
    let valorTexto = e.target.value.replace(/\D/g, ""); // Remove tudo que não for número
    
    if (!valorTexto) {
        e.target.value = "";
        return;
    }
    
    let valorNumerico = parseInt(valorTexto, 10) / 100;
    e.target.value = valorNumerico.toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
});

jogoForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const tagsSelecionadas = Array.from(tagsCheckboxes)
        .filter(cb => cb.checked)
        .map(cb => cb.value)
        .join(", ");

    // Converte o valor "R$ 15,99" de volta para número decimal "15.99" pro backend entender
    const precoNumerico = Number(jogoPrecoInput.value.replace(/\D/g, "")) / 100;

    try {
        await fetchJson("/api/jogos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nome: jogoNomeInput.value,
                descricao: jogoDescricaoInput.value,
                preco: precoNumerico,
                tags: tagsSelecionadas,
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
