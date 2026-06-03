package dev.osdiscretos.atlantidastore.controller;


import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.LoginRequest;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.model.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token,
        HttpServletResponse response
    ) {
        authService.logout(token);
        response.addCookie(expiredSessionCookie());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
        @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        AuthService.LoginResult login = authService.login(request);
        response.addCookie(sessionCookie(login.sessao().getToken()));

        return ResponseEntity.ok(UsuarioResponse.from(login.usuario()));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = authService.findUserBySessionToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErroResponse(exception.getMessage()));
    }

    private Cookie sessionCookie(String token) {
        Cookie cookie = new Cookie(SessionKey.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(authService.sessionMaxAgeSeconds());
        return cookie;
    }

    private Cookie expiredSessionCookie() {
        Cookie cookie = new Cookie(SessionKey.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
