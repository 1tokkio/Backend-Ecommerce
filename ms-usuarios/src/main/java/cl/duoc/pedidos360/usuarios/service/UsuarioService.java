package cl.duoc.pedidos360.usuarios.service;

import cl.duoc.pedidos360.usuarios.model.Usuario;
import cl.duoc.pedidos360.usuarios.repository.UsuarioRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repositorio;

    public UsuarioService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * El registro no lo hace un formulario: el usuario ya existe en el tenant.
     * La primera vez que entra con su token lo guardamos en nuestra base a partir
     * de los claims, y en las siguientes visitas solo actualizamos nombre y correo.
     */
    public Usuario sincronizar(Jwt token) {
        String oid = token.getClaimAsString("oid");
        String correo = correoDesde(token);
        String nombre = token.getClaimAsString("name");
        String rol = esAdministrador(token) ? "ADMIN" : "CLIENTE";

        return repositorio.findByAzureOid(oid)
                .map(existente -> {
                    existente.setCorreo(correo);
                    existente.setNombre(nombre);
                    existente.setRol(rol);
                    return repositorio.save(existente);
                })
                .orElseGet(() -> repositorio.save(new Usuario(oid, correo, nombre, rol)));
    }

    public List<Usuario> listarTodos() {
        return repositorio.findAll();
    }

    /** Segun el tipo de cuenta el correo puede venir en "preferred_username", en "email" o en "upn". */
    private String correoDesde(Jwt token) {
        String correo = token.getClaimAsString("preferred_username");
        if (correo == null) {
            correo = token.getClaimAsString("email");
        }
        if (correo == null) {
            correo = token.getClaimAsString("upn");
        }
        return correo == null ? "sin-correo" : correo;
    }

    private boolean esAdministrador(Jwt token) {
        List<String> roles = token.getClaimAsStringList("roles");
        return roles != null && roles.contains("Admin");
    }
}
