package cl.duoc.pedidos360.ordenes.dto;

import java.util.List;

// correo es respaldo: se usa solo si el token no trae preferred_username ni email
// (pasa con el access token de Cognito, que no lleva claims de perfil).
public record NuevaOrden(List<LineaOrden> items, String correo) {
}
