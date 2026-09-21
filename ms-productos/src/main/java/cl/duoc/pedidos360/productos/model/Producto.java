package cl.duoc.pedidos360.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false)
    private Integer stock;

    public Producto() {
    }

    public Producto(String nombre, String descripcion, Integer precio, String categoria, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock;
    }

    /** El stock nunca baja de cero: si se vendio mas de lo que habia, queda en cero. */
    public void descontar(int cantidad) {
        this.stock = Math.max(0, this.stock - cantidad);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public Integer getStock() {
        return stock;
    }
}
