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
     * El registro no lo hace un formulario: el usuario ya existe en su proveedor
     * de identidad. La primera vez que entra con su token lo guardamos en nuestra
     * base a partir de los claims, y en las siguientes visitas solo actualizamos
     * nombre, correo y rol.
     *
     * esAdmin viene ya resuelto por SecurityConfig (via el rol de la autenticacion),
     * no se vuelve a leer el claim "roles" aca: asi el default-role de Cognito
     * tambien queda reflejado sin duplicar esa logica.
     */
    public Usuario sincronizar(Jwt token, boolean esAdmin) {
        String proveedor = proveedorDe(token);
        String identificador = identificadorDe(token, proveedor);
        String correo = correoDesde(token);
        String nombre = token.getClaimAsString("name");
        String rol = esAdmin ? "Admin" : "Cliente";

        return repositorio.findByIdentificadorExterno(identificador)
                .map(existente -> {
                    existente.setCorreo(correo);
                    existente.setNombre(nombre);
                    existente.setRol(rol);
                    return repositorio.save(existente);
                })
                .orElseGet(() -> repositorio.save(new Usuario(identificador, proveedor, correo, nombre, rol)));
    }

    public List<Usuario> listarTodos() {
        return repositorio.findAll();
    }

    /** El issuer de Cognito siempre trae "cognito-idp" en el dominio; el de Azure no. */
    private String proveedorDe(Jwt token) {
        String issuer = token.getIssuer() == null ? "" : token.getIssuer().toString();
        return issuer.contains("cognito-idp") ? "cognito" : "azure";
    }

    /** El identificador estable es "oid" en Azure y "sub" en Cognito. */
    private String identificadorDe(Jwt token, String proveedor) {
        return "cognito".equals(proveedor) ? token.getSubject() : token.getClaimAsString("oid");
    }

    /** Segun el proveedor y el tipo de cuenta el correo puede venir en distintos claims. */
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
}
