package cl.duoc.pedidos360.ordenes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String usuarioOid;

    @Column(nullable = false, length = 150)
    private String correoUsuario;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // EAGER porque open-in-view esta en false: sin esto, Jackson intenta
    // serializar los detalles despues de cerrada la sesion de Hibernate y
    // falla con LazyInitializationException.
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleOrden> detalles = new ArrayList<>();

    public Orden() {
    }

    public Orden(String usuarioOid, String correoUsuario) {
        this.usuarioOid = usuarioOid;
        this.correoUsuario = correoUsuario;
        this.estado = "CONFIRMADO";
        this.fechaCreacion = LocalDateTime.now();
        this.total = 0;
    }

    public void agregarDetalle(DetalleOrden detalle) {
        detalle.setOrden(this);
        this.detalles.add(detalle);
        this.total += detalle.getSubtotal();
    }

    public Long getId() {
        return id;
    }

    public String getUsuarioOid() {
        return usuarioOid;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public Integer getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public List<DetalleOrden> getDetalles() {
        return detalles;
    }
}
