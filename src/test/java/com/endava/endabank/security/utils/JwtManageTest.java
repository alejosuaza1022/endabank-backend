package com.endava.endabank.security.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.endava.endabank.exceptions.customExceptions.BadDataException;
import com.endava.endabank.model.User;
import com.endava.endabank.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtManageTest {

    private final String secret_dummy = "ZHVtbXkgdmFsdWUK";

    @Test
    void generateValidTokenTest() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(),
                user.getEmail(), secret_dummy);
        assertNotNull(token);
        assertNotEquals("", token);
    }

    @Test
    void generateInvalidTokenTest() {
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(null, "a@a.com", secret_dummy));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, null, secret_dummy));
        assertThrows(BadDataException.class, () ->
                JwtManage.generateToken(1, "", secret_dummy));
    }

    @Test
    void verifyValidToken() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        int idUser = JwtManage.verifyToken("Bearer " + token, secret_dummy);
        assertEquals(idUser, user.getId());
    }

    @Test
    void verifyInvalidToken() throws BadDataException {
        User user = TestUtils.getUserAdmin();
        String token = JwtManage.generateToken(user.getId(), user.getEmail(), secret_dummy);
        assertThrows(JWTVerificationException.class, () ->
                JwtManage.verifyToken("Bearer " + token + "asd", secret_dummy)
        );
        assertThrows(JWTVerificationException.class, () ->
                JwtManage.verifyToken("Bearer ", secret_dummy)
        );

    }
}