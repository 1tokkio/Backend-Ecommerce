# pedidos360-backend

Los tres microservicios del sistema Pedidos360, construidos con Spring Boot 3.2.5
sobre Java 17. Cada uno es un Resource Server de OAuth2: recibe el access token que
el frontend obtuvo desde Microsoft Entra ID y lo valida antes de responder.

| Servicio      | Puerto | Schema     | Ruta base            |
|---------------|--------|------------|----------------------|
| ms-usuarios   | 8081   | `usuarios` | `/api/v1/usuarios`   |
| ms-carrito    | 8082   | `carrito`  | `/api/v1/carrito`    |
| ms-pedidos    | 8083   | `pedidos`  | `/api/v1/pedidos`    |

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

## Endpoints

### ms-usuarios
- `GET /api/v1/usuarios/estado` - abierto, sin token.
- `GET /api/v1/usuarios/perfil` - registra al usuario a partir de los claims y devuelve su perfil.
- `GET /api/v1/usuarios` - listado completo, **solo rol Admin**.

### ms-carrito
- `GET /api/v1/carrito/estado` - abierto, sin token.
- `GET /api/v1/carrito/productos` - catalogo.
- `GET /api/v1/carrito` - carrito del usuario del token, con total calculado.
- `POST /api/v1/carrito/items` - cuerpo `{ "productoId": 1, "cantidad": 2 }`.
- `DELETE /api/v1/carrito/items/{id}` - quita un item propio.
- `DELETE /api/v1/carrito` - vacia el carrito.

### ms-pedidos
- `GET /api/v1/pedidos/estado` - abierto, sin token.
- `POST /api/v1/pedidos` - cuerpo `{ "items": [{ "nombreProducto": "...", "precioUnitario": 1000, "cantidad": 1 }] }`.
- `GET /api/v1/pedidos/mis-pedidos` - pedidos del usuario del token.
- `GET /api/v1/pedidos` - todos los pedidos, **solo rol Admin**.

## Como levantarlo

Primero hay que tener corriendo el stack de `pedidos360-data`, que crea la red
`pedidos360-network` y la base de datos.

    cp .env.example .env
    # completar los valores
    docker compose up -d --build

Para desarrollar un servicio suelto sin Docker, exportar las mismas variables y:

    cd ms-usuarios
    mvn spring-boot:run
