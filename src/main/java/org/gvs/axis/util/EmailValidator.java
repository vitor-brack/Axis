package org.gvs.axis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.gvs.axis.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author vitor
 */
@Component
@RequiredArgsConstructor
public class EmailValidator {

    private final UsuarioRepository usuarioRepository;

    private static final String EMAIL_PATTERN
            = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final int MAX_EMAIL_LENGTH = 100;
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public void validar(String email) {
        List<String> errors = new ArrayList<>();

        if (email == null) {
            throw new IllegalArgumentException("O email não pode ser nulo");
        }

        String trimmedEmail = email.trim();

        if (trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("O email não pode estar vazio");
        }

        if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("O email não pode ter mais que " + MAX_EMAIL_LENGTH + " caracteres");
        }

        if (!pattern.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Formato de email inválido");
        }

        if (trimmedEmail.contains("..")) {
            throw new IllegalArgumentException("O email não pode conter pontos consecutivos");
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }
}
