package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.model.entity.Permiso;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.entity.UsuarioPermiso;
import co.edu.unbosque.backend.repository.PermisoRepository;
import co.edu.unbosque.backend.repository.RolPermisoRepository;
import co.edu.unbosque.backend.repository.UsuarioPermisoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;
    private final UsuarioRepository usuarioRepository;

    public PermisoService(PermisoRepository permisoRepository, RolPermisoRepository rolPermisoRepository,
                          UsuarioPermisoRepository usuarioPermisoRepository, UsuarioRepository usuarioRepository) {
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Permiso> listarTodos() {
        return permisoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Set<String> permisosEfectivos(Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new co.edu.unbosque.backend.exception.ResourceNotFoundException("No existe usuario " + idUsuario));
        String rol = u.getRol() != null ? u.getRol().trim().toUpperCase() : "";
        Set<String> base = rolPermisoRepository.findByRol(rol).stream()
                .map(rp -> rp.getPermiso().getClave())
                .collect(Collectors.toSet());
        // overrides por usuario
        List<UsuarioPermiso> overrides = usuarioPermisoRepository.findByUsuario_IdUsuario(idUsuario);
        for (UsuarioPermiso up : overrides) {
            String clave = up.getPermiso().getClave();
            if (Boolean.TRUE.equals(up.getConcedido())) base.add(clave);
            else base.remove(clave);
        }
        return base;
    }

    @Transactional(readOnly = true)
    public List<UsuarioPermiso> listarOverrides(Long idUsuario) {
        return usuarioPermisoRepository.findByUsuario_IdUsuario(idUsuario);
    }

    @Transactional
    public Set<String> guardarOverrides(Long idUsuario, Map<String, Boolean> permisosMap) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new co.edu.unbosque.backend.exception.ResourceNotFoundException("No existe usuario " + idUsuario));
        // borrar previos y recrear solo los que difieren del rol base
        usuarioPermisoRepository.deleteByUsuario_IdUsuario(idUsuario);
        Set<String> rolBase = rolPermisoRepository.findByRol(u.getRol().trim().toUpperCase()).stream()
                .map(rp -> rp.getPermiso().getClave()).collect(Collectors.toSet());
        for (Map.Entry<String, Boolean> e : permisosMap.entrySet()) {
            String clave = e.getKey();
            Boolean concedido = e.getValue();
            Permiso p = permisoRepository.findByClave(clave)
                    .orElseThrow(() -> new co.edu.unbosque.backend.exception.ResourceNotFoundException("Permiso no existe: " + clave));
            boolean enRol = rolBase.contains(clave);
            // solo guardar override si difiere del rol
            if (concedido != enRol) {
                UsuarioPermiso up = new UsuarioPermiso();
                up.setUsuario(u);
                up.setPermiso(p);
                up.setConcedido(concedido);
                usuarioPermisoRepository.save(up);
            }
        }
        return permisosEfectivos(idUsuario);
    }
}
