package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Almacenamiento filesystem para imágenes de proveedores.
 * SQLite: solo guarda nombre en proveedor.imagen_url (TEXT).
 * Archivos: uploads/proveedores/proveedor-{id}.ext
 */
@Service
public class ProveedorImagenStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path directorioBase;

    public ProveedorImagenStorageService(
            @Value("${farmaros.proveedor.imagenes.dir:uploads/proveedores}") String dir
    ) {
        this.directorioBase = resolverDirectorioBase(dir);
        try {
            Files.createDirectories(this.directorioBase);
        } catch (IOException e) {
            throw new BusinessException("No se pudo crear el directorio de imágenes de proveedores: " + this.directorioBase);
        }
    }

    private static Path resolverDirectorioBase(String dir) {
        Path configurado = Paths.get(dir);
        if (configurado.isAbsolute()) return configurado.normalize();
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path raiz = userDir;
        if (raiz.getFileName() != null && "backend".equalsIgnoreCase(raiz.getFileName().toString())) {
            Path parent = raiz.getParent();
            if (parent != null) raiz = parent;
        }
        if (raiz.getFileName() != null && "frontend".equalsIgnoreCase(raiz.getFileName().toString())) {
            Path parent = raiz.getParent();
            if (parent != null) raiz = parent;
        }
        return raiz.resolve(configurado).normalize();
    }

    public String guardar(Long proveedorId, MultipartFile archivo) {
        validar(archivo);
        String extension = extraerExtension(archivo.getOriginalFilename(), archivo.getContentType());
        String nombreArchivo = "proveedor-" + proveedorId + "." + extension;
        Path destino = directorioBase.resolve(nombreArchivo).normalize();
        eliminarVariantes(proveedorId, nombreArchivo);
        try {
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen del proveedor");
        }
        return nombreArchivo;
    }

    public Resource cargarComoResource(String nombreArchivo) {
        try {
            Path archivo = directorioBase.resolve(nombreArchivo).normalize();
            Resource resource = new UrlResource(archivo.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            Path fallback = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize()
                    .resolve("uploads/proveedores").resolve(nombreArchivo).normalize();
            if (!fallback.equals(archivo)) {
                Resource fb = new UrlResource(fallback.toUri());
                if (fb.exists() && fb.isReadable()) return fb;
            }
            Path legacy = Paths.get("backend").toAbsolutePath().resolve("uploads/proveedores").resolve(nombreArchivo);
            if (!legacy.equals(archivo) && !legacy.equals(fallback)) {
                Resource lg = new UrlResource(legacy.toUri());
                if (lg.exists() && lg.isReadable()) return lg;
            }
            throw new ResourceNotFoundException("Imagen no encontrada: " + nombreArchivo);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Imagen no encontrada: " + nombreArchivo);
        }
    }

    public void eliminar(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return;
        try {
            Path archivo = directorioBase.resolve(nombreArchivo).normalize();
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {}
    }

    public String detectarContentType(String nombreArchivo) {
        String ext = extraerExtension(nombreArchivo, null).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "image/jpeg";
        };
    }

    private void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new BusinessException("Debe enviar un archivo de imagen");
        if (archivo.getSize() > MAX_BYTES) throw new BusinessException("La imagen no puede superar 5MB");
        String ct = archivo.getContentType();
        if (ct != null && !ct.isBlank() && !MIME_PERMITIDOS.contains(ct.toLowerCase())) {
            if (!ct.toLowerCase().startsWith("image/")) throw new BusinessException("Solo se permiten imágenes (JPG, PNG, WEBP, GIF)");
        }
        String ext = extraerExtension(archivo.getOriginalFilename(), ct);
        if (!EXTENSIONES_PERMITIDAS.contains(ext.toLowerCase())) throw new BusinessException("Extensión no permitida. Use: jpg, jpeg, png, webp, gif");
    }

    private String extraerExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).trim().toLowerCase();
            if (!ext.isBlank()) return ext;
        }
        if (contentType != null) {
            if (contentType.equalsIgnoreCase("image/png")) return "png";
            if (contentType.equalsIgnoreCase("image/webp")) return "webp";
            if (contentType.equalsIgnoreCase("image/gif")) return "gif";
        }
        return "jpg";
    }

    private void eliminarVariantes(Long proveedorId, String conservar) {
        for (String ext : EXTENSIONES_PERMITIDAS) {
            String candidato = "proveedor-" + proveedorId + "." + ext;
            if (candidato.equals(conservar)) continue;
            try { Files.deleteIfExists(directorioBase.resolve(candidato)); } catch (IOException ignored) {}
        }
        if (!conservar.equals("proveedor-" + proveedorId + ".jpeg")) {
            try { Files.deleteIfExists(directorioBase.resolve("proveedor-" + proveedorId + ".jpeg")); } catch (IOException ignored) {}
        }
        if (!conservar.equals("proveedor-" + proveedorId + ".jpg")) {
            try { Files.deleteIfExists(directorioBase.resolve("proveedor-" + proveedorId + ".jpg")); } catch (IOException ignored) {}
        }
    }

    public Path getDirectorioBase() { return directorioBase; }
}
