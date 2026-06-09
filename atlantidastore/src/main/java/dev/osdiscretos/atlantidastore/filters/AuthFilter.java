package dev.osdiscretos.atlantidastore.filters;

import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String path = normalizarPath(request);

        if (isPublicRequest(request.getMethod(), path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Usuario usuario = authService.findUserBySessionToken(readSessionToken(request));

        if (usuario == null) {
            rejectUnauthenticatedRequest(request, response, path);
            return;
        }

        request.setAttribute("usuarioLogado", usuario);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicRequest(String method, String path) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)) {
            return isStaticAsset(path)
                || path.equals("/login")
                || path.equals("/login.html")
                || path.equals("/cadastro")
                || path.equals("/cadastro.html")
                || path.equals("/api/jogos/feed")
                || path.matches("/api/jogos/[0-9a-fA-F\\-]{36}")
                || path.matches("/api/usuarios/[0-9a-fA-F\\-]{36}/perfil-publico")
                || path.equals("/error");
        }

        if ("POST".equalsIgnoreCase(method)) {
            return path.equals("/api/auth/login")
                || path.equals("/api/auth/logout")
                || path.equals("/api/usuarios");
        }

        return false;
    }

    private boolean isStaticAsset(String path) {
        return path.equals("/favicon.ico")
            || path.endsWith(".css")
            || path.endsWith(".js")
            || path.endsWith(".png")
            || path.endsWith(".jpg")
            || path.endsWith(".jpeg")
            || path.endsWith(".gif")
            || path.endsWith(".svg")
            || path.endsWith(".webp");
    }

    private String readSessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (SessionKey.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void rejectUnauthenticatedRequest(
        HttpServletRequest request,
        HttpServletResponse response,
        String path
    ) throws IOException {
        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"mensagem\":\"Login obrigatório\"}");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private String normalizarPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();

        if (contextPath == null || contextPath.isBlank()) {
            return uri;
        }

        return uri.substring(contextPath.length());
    }
}
