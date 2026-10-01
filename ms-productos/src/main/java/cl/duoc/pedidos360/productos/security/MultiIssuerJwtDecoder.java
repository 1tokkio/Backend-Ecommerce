package cl.duoc.pedidos360.productos.security;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.nimbusds.jwt.SignedJWT;

public class MultiIssuerJwtDecoder implements JwtDecoder {

    private final Map<String, JwtDecoder> decodersPorEmisor;

    public MultiIssuerJwtDecoder(Map<String, JwtDecoder> decodersPorEmisor) {
        this.decodersPorEmisor = new LinkedHashMap<>(decodersPorEmisor);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        String emisor = leerEmisor(token);

        JwtDecoder decoder = decodersPorEmisor.get(emisor);
        if (decoder == null) {
            throw new JwtException("El token no fue emitido por un proveedor registrado. iss recibido: " + emisor);
        }

        return decoder.decode(token);
    }

    private String leerEmisor(String token) {
        try {
            // Se lee el claim crudo porque segun la version de Nimbus "iss" se
            // expone como String o como lista de String.
            Object iss = SignedJWT.parse(token).getJWTClaimsSet().getClaim("iss");

            if (iss instanceof String emisor) {
                return emisor;
            }
            if (iss instanceof List<?> emisores && !emisores.isEmpty()) {
                return String.valueOf(emisores.get(0));
            }
            throw new JwtException("El token no contiene el claim iss");
        } catch (ParseException e) {
            throw new JwtException("El token no es un JWT valido", e);
        }
    }
}
