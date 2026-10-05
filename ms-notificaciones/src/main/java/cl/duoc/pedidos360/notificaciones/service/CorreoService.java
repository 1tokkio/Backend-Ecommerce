package cl.duoc.pedidos360.notificaciones.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final boolean configurado;

    public CorreoService(JavaMailSender mailSender,
                          @Value("${correo.remitente}") String remitente,
                          @Value("${spring.mail.username:}") String usuarioSmtp) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.configurado = usuarioSmtp != null && !usuarioSmtp.isBlank();
    }

    /** Mismo asunto que arma el correo, expuesto para que quede registrado en la tabla. */
    public String asuntoPara(Long ordenId) {
        return ordenId == null
                ? "Pedidos360 - Correo de prueba"
                : "Pedidos360 - Confirmacion de tu orden #" + ordenId;
    }

    // Un fallo de envio no relanza excepcion: la orden ya existe, no hay nada que
    // reintentar. Sin SMTP_USER configurado, el correo queda en el log en vez de enviarse.
    public boolean enviarConfirmacion(String destinatario, Long ordenId, Integer total, List<String> detalleItems) {
        String asunto = asuntoPara(ordenId);
        String cuerpo = cuerpoHtml(ordenId, total, detalleItems);

        if (!configurado) {
            log.info("SMTP no configurado, correo no enviado de verdad. Para: {} | Asunto: {} | Cuerpo: {}",
                    destinatario, asunto, cuerpo);
            return true;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true);

            mailSender.send(mensaje);
            log.info("Correo enviado a {} por la orden {}", destinatario, ordenId);
            return true;
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de la orden {}: {}", ordenId, e.getMessage());
            return false;
        }
    }

    /** HTML armado a mano: para un correo de una pantalla no hace falta un motor de plantillas. */
    private String cuerpoHtml(Long ordenId, Integer total, List<String> detalleItems) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family: Arial, sans-serif; color: #222;\">");
        html.append("<h2>Pedidos360</h2>");

        if (ordenId == null) {
            html.append("<p>Este es un correo de prueba enviado desde ms-notificaciones.</p>");
        } else {
            html.append("<p>Tu orden <strong>#").append(ordenId).append("</strong> fue confirmada.</p>");
            if (detalleItems != null && !detalleItems.isEmpty()) {
                html.append("<ul>");
                for (String linea : detalleItems) {
                    html.append("<li>").append(linea).append("</li>");
                }
                html.append("</ul>");
            }
            html.append("<p><strong>Total: $").append(total).append("</strong></p>");
        }

        html.append("</div>");
        return html.toString();
    }
}
