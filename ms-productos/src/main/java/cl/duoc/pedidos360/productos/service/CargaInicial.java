package cl.duoc.pedidos360.productos.service;

import cl.duoc.pedidos360.productos.model.Producto;
import cl.duoc.pedidos360.productos.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deja el catalogo con datos la primera vez que arranca el servicio.
 * Sin esto la tienda parte vacia y no se puede demostrar el flujo completo.
 */
@Component
public class CargaInicial implements CommandLineRunner {

    private final ProductoRepository productos;

    public CargaInicial(ProductoRepository productos) {
        this.productos = productos;
    }

    @Override
    public void run(String... args) {
        if (productos.count() > 0) {
            return;
        }

        productos.saveAll(List.of(
                new Producto("Teclado mecanico TKL", "Switches rojos, retroiluminado", 45990, "Perifericos", 20),
                new Producto("Mouse inalambrico", "Sensor optico de 12000 dpi", 24990, "Perifericos", 35),
                new Producto("Monitor 24 pulgadas", "Panel IPS 75 Hz, 1080p", 129990, "Pantallas", 10),
                new Producto("Audifonos con microfono", "Diadema acolchada, conexion USB", 32990, "Audio", 25),
                new Producto("Webcam Full HD", "1080p a 30 cuadros por segundo", 27990, "Video", 15),
                new Producto("Disco SSD 1 TB", "NVMe, lectura de 3500 MB por segundo", 68990, "Almacenamiento", 30)
        ));
    }
}
