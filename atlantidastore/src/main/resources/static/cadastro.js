function mostrarMensagem(texto, erro = false) {
    const container = document.getElementById("toast-container");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${erro ? 'erro' : ''}`;
    toast.textContent = texto;
    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 100);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode === container) {
                container.removeChild(toast);
            }
        }, 500);
    }, 5000);
}

async function extrairMensagemErro(resposta, fallback) {
    const texto = await resposta.text().catch(() => "");
    if (!texto) return fallback;
    try {
        return JSON.parse(texto).mensagem || fallback;
    } catch {
        return texto;
    }
}

async function fetchJson(url, options = {}) {
    const resposta = await fetch(url, options);

    // Se receber 401 (Não Autorizado) e não estivermos em uma página pública (login/cadastro), redireciona.
    const isPublicPage = window.location.pathname.endsWith('/login.html') || window.location.pathname.endsWith('/cadastro.html');
    if (resposta.status === 401 && !isPublicPage) {
        window.location.href = "/login.html";
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

function formatarData(valor) {
    if (!valor) return "-";
    return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(valor));
}

function formatarPreco(valor) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(Number(valor ?? 0));
}

function escaparHtml(valor) {
    return String(valor ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function carregarSessao() {
    try {
        window.userSession = await fetchJson("/api/auth/me");
    } catch (error) {
        window.userSession = null;
    }
}

// Componente Header reutilizável
function Header() {
    const [session, setSession] = React.useState(null);

    // Este useEffect agora busca a sessão do jeito certo, após a renderização inicial.
    React.useEffect(() => {
        // A sessão já foi carregada por carregarSessao() antes do render.
        setSession(window.userSession);
    }, []); // Executa apenas uma vez, após a montagem do componente.

    const handleLogout = async () => {
        try {
            await fetchJson("/api/auth/logout", { method: "POST" });
            window.location.href = "/login.html";
        } catch (error) {
            mostrarMensagem(error.message, true);
        }
    };

    return (
        <header className="topbar">
            <div className="topbar-inner">
                <a className="brand" href="/">Atlantida Store</a>
                <nav className="nav">
                    <a href="/">Feed</a>
                    <a href="/biblioteca.html">Minha Biblioteca</a>
                    <a href="/lista-desejos.html">Lista de desejos</a>
                    <a className="button small" href="/publicar-jogo.html">Publicar Jogo</a>
                    {session ? <button className="danger small" onClick={handleLogout} type="button">Sair</button> : <a href="/login.html">Login</a>}
                </nav>
            </div>
        </header>
    );
}

// Componente GameCard reutilizável
function GameCard({ jogo, children }) {
    return (
        <article className="game-card">
            {jogo.imagemCapaUrl ? <img className="game-cover" src={jogo.imagemCapaUrl} alt={`Capa de ${jogo.nome}`} /> : <div className="game-cover placeholder">Sem capa</div>}
            <div className="game-body">
                <div className="game-heading">
                    <h3>{jogo.nome}</h3>
                    <strong>{formatarPreco(jogo.preco)}</strong>
                </div>
                <p>{jogo.descricao}</p>
                <div className="game-meta">
                    <span>{jogo.tags || "Sem tags"}</span>
                    <span className="publisher-link">{jogo.desenvolvedorNome || "Desenvolvedor"}</span>
                    <span>{formatarData(jogo.dataPublicacao)}</span>
                </div>
                <div className="game-actions">
                    {children}
                </div>
            </div>
        </article>
    );
}

// Componente reutilizável para input de senha com toggle de visibilidade
function PasswordInput({ value, onChange, required = false, minLength = 0, autoComplete = "off" }) {
    const [isPasswordVisible, setIsPasswordVisible] = React.useState(false);

    const iconOlho = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`;
    const iconOlhoFechado = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`;

    return (
        <div className="senha-wrapper">
            <input
                type={isPasswordVisible ? "text" : "password"}
                value={value}
                onChange={onChange}
                autoComplete={autoComplete}
                required={required}
                minLength={minLength}
            />
            <button type="button" className="toggle-senha" onClick={() => setIsPasswordVisible(!isPasswordVisible)} aria-label={isPasswordVisible ? "Ocultar senha" : "Mostrar senha"} dangerouslySetInnerHTML={{ __html: isPasswordVisible ? iconOlhoFechado : iconOlho }} />
        </div>
    );
}
function CadastroPage() {
    const [nome, setNome] = React.useState('');
    const [email, setEmail] = React.useState('');
    const [senha, setSenha] = React.useState('');
    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await fetchJson("/api/usuarios", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ nome, email, senha })
            });
            window.location.href = "/login.html?cadastroSucesso";
        } catch (error) {
            mostrarMensagem(error.message, true);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="container narrow">
            <section className="panel">
                <div className="header">
                    <div>
                        <h1>Cadastro</h1>
                        <p>Crie uma conta para acessar a plataforma.</p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="form single">
                    <label>
                        Nome
                        <input value={nome} onChange={e => setNome(e.target.value)} type="text" autoComplete="name" required />
                    </label>

                    <label>
                        E-mail
                        <input value={email} onChange={e => setEmail(e.target.value)} type="email" autoComplete="email" required />
                    </label>

                    <label>
                        Senha
                        <PasswordInput value={senha} onChange={e => setSenha(e.target.value)} required minLength="6" autoComplete="new-password" />
                    </label>

                    <button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "Cadastrando..." : "Cadastrar"}
                    </button>
                </form>

                <p className="footer-link">
                    Já tem conta? <a href="/login.html">Entrar</a>
                </p>
            </section>
        </main>
    );
}

function App() {
    return (
        <>
            <Header />
            <CadastroPage />
            <div id="toast-container"></div>
        </>
    );
}

const root = ReactDOM.createRoot(document.getElementById("app-root"));

// Carrega a sessão antes de renderizar para que o Header em outras páginas funcione
carregarSessao().then(() => {
    root.render(<App />);
});
