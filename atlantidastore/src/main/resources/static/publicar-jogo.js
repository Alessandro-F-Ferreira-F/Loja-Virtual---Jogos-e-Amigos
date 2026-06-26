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
    const [session, setSession] = React.useState(null); // Inicia como nulo para evitar o erro

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
const MAX_IMAGE_BYTES = 2 * 1024 * 1024;
const MAX_GAME_FILE_BYTES = 500 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = new Set(["image/png", "image/jpeg", "image/gif", "image/webp"]);
const TAGS_DISPONIVEIS = ["Ação", "Aventura", "RPG", "Estratégia", "Simulação", "Esportes", "Corrida", "Puzzle", "Terror", "Luta", "Plataforma", "Souls-like"];

function validarArquivoJogo(file) {
    if (!file) {
        mostrarMensagem("Anexe um arquivo ZIP do jogo.", true);
        return false;
    }

    if (!file.name.toLowerCase().endsWith(".zip")) {
        mostrarMensagem("O arquivo do jogo deve ser um .zip.", true);
        return false;
    }

    if (file.size <= 0) {
        mostrarMensagem("O arquivo ZIP não pode estar vazio.", true);
        return false;
    }

    if (file.size > MAX_GAME_FILE_BYTES) {
        mostrarMensagem("O arquivo ZIP deve ter no máximo 500 MB.", true);
        return false;
    }

    return true;
}

function formatarTamanho(bytes) {
    if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function PublishGamePage() {
    const [nome, setNome] = React.useState('');
    const [preco, setPreco] = React.useState('');
    const [tags, setTags] = React.useState([]);
    const [imagemCapa, setImagemCapa] = React.useState(null); // Data URL
    const [imagemPreview, setImagemPreview] = React.useState('');
    const [arquivo, setArquivo] = React.useState(null); // File object
    const [descricao, setDescricao] = React.useState('');
    const [isSubmitting, setIsSubmitting] = React.useState(false);
    const session = window.userSession;

    // React.useEffect(() => {
    //     carregarSessao();
    // }, []);

    const handleImageChange = async (e) => {
        const file = e.target.files[0];
        if (!file) {
            setImagemCapa(null);
            setImagemPreview('');
            return;
        }
        if (!ALLOWED_IMAGE_TYPES.has(file.type) || file.size > MAX_IMAGE_BYTES) {
            mostrarMensagem("Imagem inválida (deve ser PNG, JPG, etc. e ter no máximo 2MB).", true);
            e.target.value = '';
            return;
        }
        const dataUrl = await new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
        setImagemCapa(dataUrl);
        setImagemPreview(dataUrl);
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file && !validarArquivoJogo(file)) {
            e.target.value = '';
            setArquivo(null);
            return;
        }
        setArquivo(file);
    };

    const handlePriceChange = (e) => {
        let valorTexto = e.target.value.replace(/\D/g, "");
        if (!valorTexto) {
            setPreco("");
            return;
        }
        let valorNumerico = parseInt(valorTexto, 10) / 100;
        setPreco(valorNumerico.toLocaleString("pt-BR", { style: "currency", currency: "BRL" }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validarArquivoJogo(arquivo)) return;

        setIsSubmitting(true);
        try {
            mostrarMensagem("Cadastrando metadados do jogo...");
            const precoNumerico = Number(preco.replace(/\D/g, "")) / 100;

            const jogoCriado = await fetchJson("/api/jogos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ nome, descricao, preco: precoNumerico, tags: tags.join(", "), imagemCapa })
            });

            if (!jogoCriado || !jogoCriado.id) throw new Error("O servidor não retornou o ID do jogo criado.");

            mostrarMensagem("Enviando arquivo ZIP do jogo...");
            const formData = new FormData();
            formData.append("arquivo", arquivo);

            await fetch(`/api/jogos/${jogoCriado.id}/arquivo`, { method: "POST", body: formData });

            mostrarMensagem("Jogo publicado com sucesso!");
            setTimeout(() => window.location.href = "/", 2000);

        } catch (error) {
            mostrarMensagem(error.message, true);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="container">
            <section className="page-title">
                <div>
                    <h1>Publicar Jogo</h1>
                    {session && <p>Logado como {session.nome} ({session.email})</p>}
                </div>
            </section>

            <section className="panel">
                <form className="form publish-form" onSubmit={handleSubmit}>
                    <div>
                        <label>
                            Nome do Jogo
                            <input type="text" value={nome} onChange={e => setNome(e.target.value)} required />
                        </label>
                    </div>
                    <div>
                        <label>
                            Preço
                            <input type="text" value={preco} onChange={handlePriceChange} placeholder="R$ 0,00" required />
                        </label>
                    </div>
                    <div className="span-2">
                        <label>
                            Descrição
                            <textarea value={descricao} onChange={e => setDescricao(e.target.value)} rows="5" required></textarea>
                        </label>
                    </div>
                    <div className="span-2">
                        <div>
                            <label>
                                Tags
                                <TagsSelector selectedTags={tags} onChange={setTags} />
                            </label>
                        </div>
                    </div>
                    <div className="span-2">
                        <label>
                            Capa do Jogo (PNG, JPG, GIF, WebP - máx 2MB)
                            <input type="file" accept="image/*" onChange={handleImageChange} />
                        </label>
                        <div className="cover-preview">
                            {imagemPreview ? <img src={imagemPreview} alt="Preview da capa" /> : <p>Preview da capa</p>}
                        </div>
                    </div>
                    <div className="span-2">
                        <label>
                            Arquivo do Jogo (.zip - máx 500MB)
                            <input type="file" accept=".zip" onChange={handleFileChange} required />
                            {arquivo && <p className="field-hint">Arquivo selecionado: {arquivo.name} ({formatarTamanho(arquivo.size)})</p>}
                        </label>
                    </div>
                    <div className="form-actions span-2">
                        <button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? "Publicando..." : "Publicar Jogo"}
                        </button>
                    </div>
                </form>
            </section>
        </main>
    );
}

function TagsSelector({ selectedTags, onChange }) {
    const [isOpen, setIsOpen] = React.useState(false);
    const ref = React.useRef(null);

    React.useEffect(() => {
        const handleClickOutside = (event) => {
            if (ref.current && !ref.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [ref]);

    const handleToggle = () => setIsOpen(!isOpen);

    const handleTagChange = (tag) => {
        const newTags = selectedTags.includes(tag)
            ? selectedTags.filter(t => t !== tag)
            : [...selectedTags, tag];
        onChange(newTags);
    };

    return (
        <div className={`multi-select ${isOpen ? 'open' : ''}`} ref={ref}>
            <button type="button" className="multi-select-trigger" onClick={handleToggle}>
                <span className={`multi-select-value ${selectedTags.length > 0 ? 'has-selection' : ''}`}>
                    {selectedTags.length > 0 ? selectedTags.join(', ') : 'Selecione as categorias...'}
                </span>
                <span className="multi-select-arrow">▼</span>
            </button>
            <div className="multi-select-dropdown">
                {TAGS_DISPONIVEIS.map(tag => (
                    <div key={tag} className="multi-select-option" onClick={() => handleTagChange(tag)}>
                        <input type="checkbox" checked={selectedTags.includes(tag)} readOnly />
                        <span>{tag}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

function App() {
    return (
        <>
            <Header />
            <PublishGamePage />
            <div id="toast-container"></div>
        </>
    );
}

const root = ReactDOM.createRoot(document.getElementById("app-root"));

carregarSessao().then(() => {
    root.render(<App />);
});
