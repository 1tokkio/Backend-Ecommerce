package cl.duoc.pedidos360.pedidos.service;

import cl.duoc.pedidos360.pedidos.dto.LineaPedido;
import cl.duoc.pedidos360.pedidos.model.DetallePedido;
import cl.duoc.pedidos360.pedidos.model.Pedido;
import cl.duoc.pedidos360.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository repositorio;

    public PedidoService(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Pedido crear(String usuarioOid, String correo, List<LineaPedido> lineas) {
        Pedido pedido = new Pedido(usuarioOid, correo);
        for (LineaPedido linea : lineas) {
            pedido.agregarDetalle(new DetallePedido(
                    linea.nombreProducto(), linea.precioUnitario(), linea.cantidad()));
        }
        return repositorio.save(pedido);
    }

    public List<Pedido> misPedidos(String usuarioOid) {
        return repositorio.findByUsuarioOidOrderByFechaCreacionDesc(usuarioOid);
    }

    public List<Pedido> todos() {
        return repositorio.findAll();
    }
}
