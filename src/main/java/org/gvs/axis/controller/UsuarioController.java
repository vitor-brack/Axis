package org.gvs.axis.controller;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.gvs.axis.dto.AtualizarReservaDTO;
import org.gvs.axis.dto.UsuarioDTO;
import org.gvs.axis.dto.response.ReservaResponse;
import org.gvs.axis.service.ReservaService;
import org.gvs.axis.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author vitor
 */
@Controller
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final ReservaService reservaService;

    @GetMapping("/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioDTO());
        return "usuario/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrarUsuario(@Valid UsuarioDTO usuarioDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "usuario/cadastro";
        }

        try {
            usuarioService.cadastrar(usuarioDTO);
            redirectAttributes.addFlashAttribute("mensagem", "Usuário cadastrado com sucesso!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar usuário: " + e.getMessage());
            return "redirect:/usuario/cadastro";
        }
    }

    @GetMapping("/login")
    public String mostrarPaginaLogin() {
        return "/usuario/login";
    }

    @GetMapping("/status")
    public ResponseEntity verificarStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isLogado = auth != null
                && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser");

        ArrayList<Object> status = new ArrayList<>();

        status.add(isLogado);
        status.add(isLogado ? auth.getName() : null);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/minha-conta")
    public String mostrarPaginaMinhaConta(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            try {
                UsuarioDTO usuario = usuarioService.buscarDTOPorEmail(auth.getName());
                model.addAttribute("user", usuario);
            } catch (Exception e) {
                model.addAttribute("erro", "Erro ao carregar dados do usuário");
            }
        }

        return "usuario/minha-conta";
    }

    @ResponseBody
    @PostMapping("/minha-conta/atualizar")
    public ResponseEntity<?> atualizarPerfil(@RequestBody Map<String, String> dados,
            Authentication auth) {
        try {
            UsuarioDTO usuarioDTO = new UsuarioDTO();
            usuarioDTO.setEmail(auth.getName()); // Email do usuário logado
            usuarioDTO.setNome(dados.get("nome"));
            usuarioDTO.setTelefone(dados.get("telefone"));
            usuarioDTO.setSenha(dados.get("senha")); // Será null se não fornecida

            UsuarioDTO usuarioAtualizado = usuarioService.atualizar(usuarioDTO);
            return ResponseEntity.ok(usuarioAtualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getPerfilUsuario(@PathVariable Long id) {
        try {
            UsuarioDTO usuario = usuarioService.buscarDTOPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-reservas")
    public String mostrarHistoricoReservas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            try {
                List<ReservaResponse> historico = reservaService.buscarHistoricoPorEmail(auth.getName());
                model.addAttribute("reservas", historico);
            } catch (Exception e) {
                model.addAttribute("erro", "Erro ao carregar histórico de reservas");
            }
        }

        return "usuario/historico-reservas";
    }

    @GetMapping("/minhas-reservas")
    public String mostrarMinhasReservas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            try {
                List<ReservaResponse> reservasAtivas = reservaService.buscarReservasAtivas(auth.getName());
                model.addAttribute("reservas", reservasAtivas);
            } catch (Exception e) {
                model.addAttribute("erro", "Erro ao carregar reservas ativas");
            }
        }

        return "usuario/minhas-reservas";
    }

    @ResponseBody
    @PostMapping("/minhas-reservas/{id}/alterar")
    public ResponseEntity<?> alterarReserva(
            @PathVariable Long id,
            @RequestBody AtualizarReservaDTO dto,
            Authentication auth) {
        try {
            ReservaResponse reserva = reservaService.alterarReserva(id, dto, auth.getName());
            return ResponseEntity.ok(reserva);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("erro", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("erro", "Erro ao alterar reserva"));
        }
    }

    @PostMapping("/minhas-reservas/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Authentication auth) {
        try {
            ReservaResponse reserva = reservaService.cancelarReserva(id, auth.getName());
            return ResponseEntity.ok(reserva);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("erro", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("erro", e.getMessage()));
        }
    }
}
