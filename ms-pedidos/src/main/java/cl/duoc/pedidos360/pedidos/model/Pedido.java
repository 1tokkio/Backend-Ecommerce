package cl.duoc.pedidos360.pedidos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(String usuarioOid, String correoUsuario) {
        this.usuarioOid = usuarioOid;
        this.correoUsuario = correoUsuario;
        this.estado = "CONFIRMADO";
        this.fechaCreacion = LocalDateTime.now();
        this.total = 0;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
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

    public List<DetallePedido> getDetalles() {
        return detalles;
    }
}
