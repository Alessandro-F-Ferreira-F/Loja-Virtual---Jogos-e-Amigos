const form = document.getElementById("cadastroForm");
const nomeInput = document.getElementById("nome");
const emailInput = document.getElementById("email");
const senhaInput = document.getElementById("senha");
const mensagem = document.getElementById("mensagem");

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

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
        const resposta = await fetch("/api/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nome: nomeInput.value,
                email: emailInput.value,
                senha: senhaInput.value
            })
        });

        if (!resposta.ok) {
            throw new Error(await extrairMensagemErro(resposta, "Não foi possível cadastrar."));
        }

        window.location.href = "/login?cadastroSucesso";
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});
