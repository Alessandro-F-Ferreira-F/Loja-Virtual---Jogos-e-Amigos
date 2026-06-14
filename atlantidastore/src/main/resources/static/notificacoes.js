const mensagem = document.getElementById("mensagem");
const notifResumo = document.getElementById("notifResumo");
const listaSolicitacoes = document.getElementById("listaSolicitacoes");
const listaNotificacoes = document.getElementById("listaNotificacoes");
const marcarTodasLidas = document.getElementById("marcarTodasLidas");
const badgeNav = document.getElementById("badgeNav");
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

function tempoRelativo(valor) {
    if (!valor) return "";

    const agora = new Date();
    const data = new Date(valor);
    const diffMs = agora - data;
    const diffMin = Math.floor(diffMs / 60000);
    const diffHoras = Math.floor(diffMin / 60);
    const diffDias = Math.floor(diffHoras / 24);

    if (diffMin < 1) return "agora mesmo";
    if (diffMin < 60) return `há ${diffMin} min`;
    if (diffHoras < 24) return `há ${diffHoras}h`;
    if (diffDias < 7) return `há ${diffDias} dia${diffDias > 1 ? "s" : ""}`;

    return formatarData(valor);
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

function descreverNotificacao(notif) {
    switch (notif.tipo) {
        case "SOLICITACAO_SEGUIMENTO":
            return `<strong>${escaparHtml(notif.atorNome)}</strong> quer seguir você`;
        case "NOVO_SEGUIDOR":
            return `<strong>${escaparHtml(notif.atorNome)}</strong> começou a seguir você`;
        case "SOLICITACAO_ACEITA":
            return `<strong>${escaparHtml(notif.atorNome)}</strong> aceitou seu pedido de seguimento`;
        default:
            return `Notificação de <strong>${escaparHtml(notif.atorNome)}</strong>`;
    }
}

function iconeNotificacao(tipo) {
    switch (tipo) {
        case "SOLICITACAO_SEGUIMENTO": return "📩";
        case "NOVO_SEGUIDOR": return "👤";
        case "SOLICITACAO_ACEITA": return "✅";
        default: return "🔔";
    }
}

function renderizarSolicitacoes(solicitacoes) {
    if (!solicitacoes || solicitacoes.length === 0) {
        listaSolicitacoes.innerHTML = '<p class="empty">Nenhuma solicitação pendente.</p>';
        return;
    }

    listaSolicitacoes.innerHTML = solicitacoes.map((s) => `
        <div class="notif-item notif-pendente" data-solicitacao-id="${s.id}">
            <div class="notif-avatar">${escaparHtml((s.solicitanteNome || "?").charAt(0).toUpperCase())}</div>
            <div class="notif-body">
                <p class="notif-text">
                    <strong>${escaparHtml(s.solicitanteNome)}</strong> quer seguir você
                </p>
                <span class="notif-time">${tempoRelativo(s.criadaEm)}</span>
            </div>
            <div class="notif-actions">
                <button type="button" class="small" data-aceitar="${s.id}">Aceitar</button>
                <button type="button" class="danger small" data-recusar="${s.id}">Recusar</button>
            </div>
        </div>
    `).join("");
}

function renderizarNotificacoes(notificacoes) {
    if (!notificacoes || notificacoes.length === 0) {
        listaNotificacoes.innerHTML = '<p class="empty">Nenhuma notificação.</p>';
        return;
    }

    listaNotificacoes.innerHTML = notificacoes.map((n) => `
        <div class="notif-item ${n.lida ? "" : "notif-nao-lida"}" data-notif-id="${n.id}">
            <div class="notif-icone">${iconeNotificacao(n.tipo)}</div>
            <div class="notif-body">
                <p class="notif-text">${descreverNotificacao(n)}</p>
                <span class="notif-time">${tempoRelativo(n.criadaEm)}</span>
            </div>
            ${!n.lida ? `<button type="button" class="small secondary" data-marcar-lida="${n.id}">Lida</button>` : '<span class="notif-lida-label">✔ Lida</span>'}
        </div>
    `).join("");
}

function atualizarBadge(count) {
    if (count > 0) {
        badgeNav.textContent = count > 99 ? "99+" : count;
        badgeNav.hidden = false;
    } else {
        badgeNav.hidden = true;
    }
}

async function carregarTudo() {
    const [notificacoes, solicitacoes, naoLidas] = await Promise.all([
        fetchJson("/api/notificacoes"),
        fetchJson("/api/solicitacoes"),
        fetchJson("/api/notificacoes/nao-lidas")
    ]);

    if (notificacoes !== null) {
        renderizarNotificacoes(notificacoes);
    }

    if (solicitacoes !== null) {
        renderizarSolicitacoes(solicitacoes);
    }

    const totalNaoLidas = naoLidas?.count ?? 0;
    const totalSolicitacoes = solicitacoes?.length ?? 0;
    atualizarBadge(totalNaoLidas);

    if (totalNaoLidas === 0 && totalSolicitacoes === 0) {
        notifResumo.textContent = "Você está em dia! Nenhuma notificação pendente.";
    } else {
        const partes = [];
        if (totalNaoLidas > 0) partes.push(`${totalNaoLidas} não lida${totalNaoLidas > 1 ? "s" : ""}`);
        if (totalSolicitacoes > 0) partes.push(`${totalSolicitacoes} solicitaç${totalSolicitacoes > 1 ? "ões" : "ão"} pendente${totalSolicitacoes > 1 ? "s" : ""}`);
        notifResumo.textContent = partes.join(" · ");
    }
}

async function aceitarSolicitacao(id) {
    await fetchJson(`/api/solicitacoes/${id}/aceitar`, { method: "POST" });
    mostrarMensagem("Solicitação aceita!");
    await carregarTudo();
}

async function recusarSolicitacao(id) {
    await fetchJson(`/api/solicitacoes/${id}/recusar`, { method: "POST" });
    mostrarMensagem("Solicitação recusada.");
    await carregarTudo();
}

async function marcarNotifLida(id) {
    await fetchJson(`/api/notificacoes/${id}/lida`, { method: "PATCH" });
    await carregarTudo();
}

async function marcarTodasComoLidas() {
    await fetchJson("/api/notificacoes/lidas", { method: "PATCH" });
    mostrarMensagem("Todas as notificações foram marcadas como lidas.");
    await carregarTudo();
}

listaSolicitacoes.addEventListener("click", async (event) => {
    const btnAceitar = event.target.closest("button[data-aceitar]");
    const btnRecusar = event.target.closest("button[data-recusar]");

    try {
        if (btnAceitar) {
            btnAceitar.disabled = true;
            await aceitarSolicitacao(btnAceitar.dataset.aceitar);
        } else if (btnRecusar) {
            btnRecusar.disabled = true;
            await recusarSolicitacao(btnRecusar.dataset.recusar);
        }
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

listaNotificacoes.addEventListener("click", async (event) => {
    const btnLida = event.target.closest("button[data-marcar-lida]");

    if (btnLida) {
        try {
            btnLida.disabled = true;
            await marcarNotifLida(btnLida.dataset.marcarLida);
        } catch (error) {
            mostrarMensagem(error.message, true);
        }
    }
});

marcarTodasLidas.addEventListener("click", async () => {
    try {
        marcarTodasLidas.disabled = true;
        await marcarTodasComoLidas();
    } catch (error) {
        mostrarMensagem(error.message, true);
    } finally {
        marcarTodasLidas.disabled = false;
    }
});

logoutButton.addEventListener("click", async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/login";
});

carregarTudo().catch((error) => {
    listaNotificacoes.innerHTML = '<p class="empty">Não foi possível carregar as notificações.</p>';
    listaSolicitacoes.innerHTML = '<p class="empty">Não foi possível carregar as solicitações.</p>';
    mostrarMensagem(error.message, true);
});
