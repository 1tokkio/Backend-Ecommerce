package cl.duoc.pedidos360.notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoService(JavaMailSender mailSender, @Value("${correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    /**
     * Un correo que falla no puede tumbar el consumo del mensaje: la orden ya existe
     * y el stock ya se descuenta por su cuenta. Si el envio falla solo queda en el log.
     */
    public void enviarConfirmacion(String destinatario, Long ordenId, Integer total) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Pedidos360 - Confirmacion de tu orden #" + ordenId);
        mensaje.setText("Tu orden #" + ordenId + " fue confirmada. Total: $" + total);

        try {
            mailSender.send(mensaje);
            log.info("Correo de confirmacion enviado a {} por la orden {}", destinatario, ordenId);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de la orden {}: {}", ordenId, e.getMessage());
        }
    }
}
