package com.todoapp.todorails.util;


import nl.martijndwars.webpush.Utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GenerateVapid {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(256);
        KeyPair keyPair = keyGen.generateKeyPair();

        String publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(
                keyPair.getPublic().getEncoded()
        );

        String privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(
                keyPair.getPrivate().getEncoded()
        );

        System.out.println("Public Key: " + publicKey);
        System.out.println("Private Key: " + privateKey);
    }
}
