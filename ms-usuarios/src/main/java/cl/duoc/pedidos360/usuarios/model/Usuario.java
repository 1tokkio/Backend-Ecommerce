package cl.duoc.pedidos360.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador estable en su proveedor: "oid" en Azure, "sub" en Cognito. */
    @Column(nullable = false, unique = true, length = 64)
    private String identificadorExterno;

    /** De donde vino el login: "azure" o "cognito". */
    @Column(nullable = false, length = 20)
    private String proveedor;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(length = 150)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public Usuario(String identificadorExterno, String proveedor, String correo, String nombre, String rol) {
        this.identificadorExterno = identificadorExterno;
        this.proveedor = proveedor;
        this.correo = correo;
        this.nombre = nombre;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getIdentificadorExterno() {
        return identificadorExterno;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
