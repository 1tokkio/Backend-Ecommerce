package cl.duoc.pedidos360.auditoria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String routingKey;

    @Lob
    @Column(nullable = false)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public EventoAuditoria() {
    }

    public EventoAuditoria(String routingKey, String contenido) {
        this.routingKey = routingKey;
        this.contenido = contenido;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
