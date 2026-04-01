package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.CrearUsuarioRequest;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Servicio de aplicación para gestión de usuarios internos.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea un nuevo usuario validando unicidad del username.
     *
     * @param request datos del nuevo usuario
     * @return usuario persistido
     */
    @Transactional
    public Usuario crearUsuario(CrearUsuarioRequest request) {
        String usernameNormalizado = request.username().trim();
        if (usuarioRepository.existsByUsernameIgnoreCase(usernameNormalizado)) {
            throw new BusinessException("Ya existe un usuario con username " + request.username());
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usernameNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(request.passwordHash().trim()));
        usuario.setNombreCompleto(normalizarTexto(request.nombreCompleto()));
        usuario.setRol(request.rol().trim());
        usuario.setEstado(normalizarEstado(request.estado()));

        return usuarioRepository.save(usuario);
    }

    /**
     * Recupera un usuario por identificador.
     *
     * @param usuarioId id del usuario
     * @return usuario encontrado
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + usuarioId));
    }

    /**
     * Lista usuarios con filtros opcionales por estado y rol.
     *
     * @param q busqueda libre opcional por id, username o nombre
     * @param estado estado opcional
     * @param rol rol opcional
     * @return usuarios encontrados
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios(String q, String estado, String rol) {
        if (q != null && !q.isBlank()) {
            String termino = q.trim();
            if (termino.matches("\\d+")) {
                return usuarioRepository.findById(Long.parseLong(termino))
                        .map(List::of)
                        .orElse(Collections.emptyList());
            }
            return usuarioRepository.buscarPorUsernameONombre(termino);
        }
        if (estado != null && !estado.isBlank()) {
            return usuarioRepository.findByEstadoOrderByNombreCompletoAsc(estado.trim());
        }
        if (rol != null && !rol.isBlank()) {
            return usuarioRepository.findByRolOrderByNombreCompletoAsc(rol.trim());
        }
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "nombreCompleto"));
    }

    /**
     * Cambia el estado funcional de un usuario.
     *
     * @param usuarioId id del usuario
     * @param estado nuevo estado
     * @return usuario actualizado
     */
    @Transactional
    public Usuario actualizarEstado(Long usuarioId, String estado) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        usuario.setEstado(normalizarEstado(estado));
        return usuarioRepository.save(usuario);
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "ACTIVO";
        }
        return estado.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ");
    }
}
