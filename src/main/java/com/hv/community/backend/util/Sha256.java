package com.hv.community.backend.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Sha256 {

  public static String encrypt(String planeText) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update((planeText + getSalt()).getBytes());
    byte[] byteData = md.digest();
    StringBuffer sb = new StringBuffer();
    for (byte i : byteData) {
      sb.append(Integer.toString((i & 0xff) + 0x100, 16).substring(1));
    }
    StringBuffer hexString = new StringBuffer();
    for (byte i : byteData) {
      String hex = Integer.toHexString(0xff & i);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String getSalt() {
    Random random = new Random();
    byte[] salt = new byte[8];
    random.nextBytes(salt);
    StringBuffer sb = new StringBuffer();
    for (byte i : salt) {
      sb.append(String.format("%02x", i));
    }
    return sb.toString();
  }
}
