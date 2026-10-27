package org.gvs.axis.service;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.gvs.axis.model.Usuario;
import org.gvs.axis.repository.UsuarioRepository;
import org.gvs.axis.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author vitor
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new CustomUserDetails(
                usuario,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
