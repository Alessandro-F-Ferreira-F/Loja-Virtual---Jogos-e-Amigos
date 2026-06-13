const form = document.getElementById("loginForm");
const emailInput = document.getElementById("email");
const senhaInput = document.getElementById("senha");
const mensagem = document.getElementById("mensagem");

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("erro", erro);
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
        const resposta = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: emailInput.value,
                senha: senhaInput.value
            })
        });

        if (!resposta.ok) {
            const erro = await resposta.json();
            throw new Error(erro.mensagem || "Não foi possível entrar.");
        }

        window.location.href = "/";
    } catch (error) {
        mostrarMensagem(error.message, true);
    }
});

if (new URLSearchParams(window.location.search).has("cadastroSucesso")) {
    mostrarMensagem("Cadastro realizado. Faça login para continuar.");
}
