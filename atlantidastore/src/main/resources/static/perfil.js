const perfilResumo = document.getElementById("perfilResumo");
const mensagem = document.getElementById("mensagem");
const avatarUsuario = document.getElementById("avatarUsuario");
const fotoPerfil = document.getElementById("fotoPerfil");
const fotoPerfilForm = document.getElementById("fotoPerfilForm");
const fotoPerfilInput = document.getElementById("fotoPerfilInput");
const nomeUsuario = document.getElementById("nomeUsuario");
const emailUsuario = document.getElementById("emailUsuario");
const totalPublicados = document.getElementById("totalPublicados");
const totalBiblioteca = document.getElementById("totalBiblioteca");
const dadosUsuario = document.getElementById("dadosUsuario");
const jogosPublicados = document.getElementById("jogosPublicados");
const bibliotecaResumo = document.getElementById("bibliotecaResumo");
const privacidadeToggle = document.getElementById("privacidadeToggle");
const logoutButton = document.getElementById("logoutButton");
const adminNavLink = document.getElementById("adminNavLink");

const MAX_PROFILE_IMAGE_BYTES = 2 * 1024 * 1024;
const ALLOWED_PROFILE_IMAGE_TYPES = new Set(["image/png", "image/jpeg", "image/gif", "image/webp"]);

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

    return resposta.json();
}

function renderizarFotoPerfil(perfil) {
    const inicial = (perfil.nome || "?").trim().charAt(0).toUpperCase();
    avatarUsuario.textContent = inicial || "?";

    if (perfil.fotoPerfilUrl) {
        fotoPerfil.src = perfil.fotoPerfilUrl;
        fotoPerfil.hidden = false;
        avatarUsuario.hidden = true;
        return;
    }

    fotoPerfil.removeAttribute("src");
    fotoPerfil.hidden = true;
    avatarUsuario.hidden = false;
}

function renderizarLista(elemento, jogos, vazio) {
    if (!jogos || jogos.length === 0) {
        elemento.innerHTML = `<p class="empty">${vazio}</p>`;
        return;
    }

    elemento.innerHTML = jogos.map((jogo) => `
        <article class="compact-item">
            ${jogo.imagemCapaUrl ? `<img src="${escaparHtml(jogo.imagemCapaUrl)}" alt="Capa de ${escaparHtml(jogo.nome)}">` : '<div class="mini-cover">Sem capa</div>'}
            <div>
                <h3>${escaparHtml(jogo.nome)}</h3>
                <p>${formatarPreco(jogo.preco)} · ${escaparHtml(jogo.tags || "Sem tags")}</p>
            </div>
        </article>
    `).join("");
}

async function carregarPerfil() {
    const perfil = await fetchJson("/api/usuarios/me/perfil");

    if (!perfil) {
        return;
    }

    perfilResumo.textContent = `${perfil.nome} (${perfil.email})`;
    renderizarFotoPerfil(perfil);
    nomeUsuario.textContent = perfil.nome;
    emailUsuario.textContent = perfil.email;
    totalPublicados.textContent = perfil.jogosPublicados?.length ?? 0;
    totalBiblioteca.textContent = perfil.biblioteca?.length ?? 0;
    dadosUsuario.innerHTML = `
        <div>
            <dt>ID</dt>
            <dd>${escaparHtml(perfil.id)}</dd>
        </div>
        <div>
            <dt>Criado em</dt>
            <dd>${formatarData(perfil.dataCriacao)}</dd>
        </div>
    `;

    renderizarLista(jogosPublicados, perfil.jogosPublicados, "Nenhum jogo publicado.");
    renderizarLista(bibliotecaResumo, perfil.biblioteca, "Nenhum jogo na biblioteca.");

    privacidadeToggle.checked = perfil.perfilPrivado;
}

privacidadeToggle.addEventListener("change", async () => {
    try {
        await fetchJson("/api/usuarios/me/privacidade", {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ perfilPrivado: privacidadeToggle.checked })
        });
        mostrarMensagem(privacidadeToggle.checked ? "Perfil tornado privado." : "Perfil tornado público.");
    } catch (error) {
        privacidadeToggle.checked = !privacidadeToggle.checked;
        mostrarMensagem(error.message, true);
    }
});

fotoPerfilInput.addEventListener("change", () => {
    const file = fotoPerfilInput.files[0];

    if (!file) {
        return;
    }

    if (!validarFotoPerfil(file)) {
        fotoPerfilInput.value = "";
        return;
    }

    fotoPerfil.src = URL.createObjectURL(file);
    fotoPerfil.hidden = false;
    avatarUsuario.hidden = true;
});

fotoPerfilForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const file = fotoPerfilInput.files[0];

    if (!validarFotoPerfil(file)) {
        return;
    }

    const botao = fotoPerfilForm.querySelector('button[type="submit"]');
    botao.disabled = true;

    try {
        const formData = new FormData();
        formData.append("foto", file);

        await fetchJson("/api/usuarios/me/foto", {
            method: "POST",
            body: formData
        });

        fotoPerfilForm.reset();
        mostrarMensagem("Foto de perfil atualizada.");
        await carregarPerfil();
    } catch (error) {
        mostrarMensagem(error.message, true);
    } finally {
        botao.disabled = false;
    }
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", {
        method: "POST"
    });

    window.location.href = "/login";
});

async function carregarSessao() {
    const usuario = await fetchJson("/api/auth/me");
    if (usuario && adminNavLink && usuario.administrador) {
        adminNavLink.hidden = false;
    }
}

carregarSessao().catch((error) => mostrarMensagem(error.message, true));
carregarPerfil().catch((error) => mostrarMensagem(error.message, true));

function validarFotoPerfil(file) {
    if (!file) {
        mostrarMensagem("Selecione uma foto de perfil.", true);
        return false;
    }

    if (!ALLOWED_PROFILE_IMAGE_TYPES.has(file.type)) {
        mostrarMensagem("A foto deve ser PNG, JPEG, GIF ou WebP.", true);
        return false;
    }

    if (file.size > MAX_PROFILE_IMAGE_BYTES) {
        mostrarMensagem("A foto deve ter no máximo 2 MB.", true);
        return false;
    }

    return true;
}
