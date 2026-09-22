package cl.duoc.pedidos360.ordenes.dto;

public record LineaOrden(Long productoId, String nombreProducto, Integer precioUnitario, Integer cantidad) {
}
