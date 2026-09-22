package cl.duoc.pedidos360.ordenes.dto;

import java.util.List;

public record NuevaOrden(List<LineaOrden> items) {
}
