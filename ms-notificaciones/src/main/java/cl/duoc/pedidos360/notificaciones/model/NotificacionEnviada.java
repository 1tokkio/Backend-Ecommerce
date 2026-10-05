package cl.duoc.pedidos360.notificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class NotificacionEnviada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 150)
    private String asunto;

    private Long ordenId;

    private Integer total;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    public NotificacionEnviada() {
    }

    public NotificacionEnviada(String destinatario, String asunto, Long ordenId, Integer total, boolean enviada) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.ordenId = ordenId;
        this.total = total;
        this.estado = enviada ? "ENVIADO" : "FALLIDO";
        this.fechaEnvio = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public Integer getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
