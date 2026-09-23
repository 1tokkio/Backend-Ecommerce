# pedidos360-backend

Los seis microservicios del sistema Pedidos360, construidos con Spring Boot 3.2.5
sobre Java 17. Cada uno es un Resource Server de OAuth2: recibe el access token que
el frontend obtuvo desde Microsoft Entra ID y lo valida antes de responder.

| Servicio           | Puerto | Schema           | Ruta base                 |
|---------------------|--------|------------------|----------------------------|
| ms-usuarios         | 8081   | `usuarios`       | `/api/v1/usuarios`         |
| ms-productos        | 8082   | `productos`      | `/api/v1/productos`        |
| ms-carrito          | 8083   | `carrito`        | `/api/v1/carrito`          |
| ms-ordenes          | 8084   | `ordenes`        | `/api/v1/ordenes`          |
| ms-notificaciones   | 8085   | `notificaciones` | `/api/v1/notificaciones`   |
| ms-auditoria        | 8086   | `auditoria`      | `/api/v1/auditoria`        |

## Validacion del token

`SecurityConfig` comprueba cuatro cosas antes de dejar pasar una peticion:

1. **Firma** - contra las claves publicas (JWKS) que publica el tenant.
2. **Emisor** - el claim `iss` tiene que ser el del tenant configurado.
3. **Audiencia** - el claim `aud` tiene que apuntar a esta API.
4. **Vigencia** - el token no puede estar expirado ni ser todavia futuro.

Ademas, los app roles que vienen en el claim `roles` se traducen a autoridades de
Spring con el prefijo `ROLE_`, lo que permite proteger endpoints con
`@PreAuthorize("hasRole('Admin')")`. Un token valido pero sin ese rol recibe **403**,
mientras que una peticion sin token recibe **401**.

## Mensajeria

Un exchange `topic` llamado `pedidos360`. `ms-ordenes` publica `pedido.creado` al
crear una orden; `ms-productos`, `ms-notificaciones` y `ms-auditoria` lo consumen
cada uno desde su propia cola. Las colas, routing keys y el formato del mensaje
estan detallados en la seccion "Endpoints" de este archivo y en el codigo de
cada `RabbitConfig`.

## Endpoints

### ms-usuarios
- `GET /api/v1/usuarios/estado` - abierto, sin token.
- `GET /api/v1/usuarios/perfil` - registra al usuario a partir de los claims y devuelve su perfil.
- `GET /api/v1/usuarios` - listado completo, **solo rol Admin**.

### ms-productos
- `GET /api/v1/productos/estado` - abierto, sin token.
- `GET /api/v1/productos` - catalogo con stock.
- `GET /api/v1/productos/{id}` - detalle de un producto.
- `POST /api/v1/productos` - cuerpo `{ "nombre": "...", "descripcion": "...", "precio": 1000, "categoria": "...", "stock": 10 }`, **solo rol Admin**.
- Consume `pedido.creado` y descuenta el stock de cada item.

### ms-carrito
- `GET /api/v1/carrito/estado` - abierto, sin token.
- `GET /api/v1/carrito` - carrito del usuario del token, con total calculado.
- `POST /api/v1/carrito/items` - cuerpo `{ "productoId": 1, "nombreProducto": "...", "precioUnitario": 1000, "cantidad": 2 }`.
- `DELETE /api/v1/carrito/items/{id}` - quita un item propio.
- `DELETE /api/v1/carrito` - vacia el carrito.

### ms-ordenes
- `GET /api/v1/ordenes/estado` - abierto, sin token.
- `POST /api/v1/ordenes` - cuerpo `{ "items": [{ "productoId": 1, "nombreProducto": "...", "precioUnitario": 1000, "cantidad": 1 }] }`.
  Publica `pedido.creado` una vez que la orden queda guardada.
- `GET /api/v1/ordenes/mis-ordenes` - ordenes del usuario del token.
- `GET /api/v1/ordenes` - todas las ordenes, **solo rol Admin**.

### ms-notificaciones
- `GET /api/v1/notificaciones/estado` - abierto, sin token.
- `POST /api/v1/notificaciones/enviar` - cuerpo `{ "destinatario": "..." }`, envia un correo de prueba sin pasar por RabbitMQ.
- `GET /api/v1/notificaciones` - historial de notificaciones enviadas, **solo rol Admin**.
- Consume `pedido.creado`, envia el correo de confirmacion y deja el intento registrado.

### ms-auditoria
- `GET /api/v1/auditoria/estado` - abierto, sin token.
- `GET /api/v1/auditoria` - listado de eventos registrados, **solo rol Admin**.
- Consume cualquier evento del exchange (`#`) y lo deja registrado.

## Como levantarlo

Primero hay que tener corriendo el stack de `Data-Ecommerce`, que crea la base y
RabbitMQ.

    cp .env.example .env
    # completar los valores
    docker compose up -d --build

En local, si ambos repositorios corren en la misma maquina, `DATASOURCE_URL` y
`RABBITMQ_HOST` usan el nombre de los contenedores porque comparten la red
`pedidos360-network`. En AWS, cada repositorio vive en su propia instancia y esos
valores pasan a ser la IP privada de la instancia de datos.

Para desarrollar un servicio suelto sin Docker, exportar las mismas variables y:

    cd ms-usuarios
    mvn spring-boot:run
