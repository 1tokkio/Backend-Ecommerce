package cl.duoc.pedidos360.pedidos.dto;

import java.util.List;

public record NuevoPedido(List<LineaPedido> items) {
}
