package cl.duoc.pedidos360.ordenes.service;

import cl.duoc.pedidos360.ordenes.dto.LineaOrden;
import cl.duoc.pedidos360.ordenes.mensajeria.PublicadorOrdenes;
import cl.duoc.pedidos360.ordenes.model.DetalleOrden;
import cl.duoc.pedidos360.ordenes.model.Orden;
import cl.duoc.pedidos360.ordenes.repository.OrdenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdenService {

    private final OrdenRepository repositorio;
    private final PublicadorOrdenes publicador;

    public OrdenService(OrdenRepository repositorio, PublicadorOrdenes publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    /** Se publica pedido.creado recien despues de guardar, para no avisar de una orden que no quedo. */
    public Orden crear(String usuarioOid, String correo, List<LineaOrden> lineas) {
        Orden orden = new Orden(usuarioOid, correo);
        for (LineaOrden linea : lineas) {
            orden.agregarDetalle(new DetalleOrden(
                    linea.productoId(), linea.nombreProducto(), linea.precioUnitario(), linea.cantidad()));
        }
        orden = repositorio.save(orden);
        publicador.publicarOrdenCreada(orden, correo);
        return orden;
    }

    public List<Orden> misOrdenes(String usuarioOid) {
        return repositorio.findByUsuarioOidOrderByFechaCreacionDesc(usuarioOid);
    }

    public List<Orden> todos() {
        return repositorio.findAll();
    }
}
