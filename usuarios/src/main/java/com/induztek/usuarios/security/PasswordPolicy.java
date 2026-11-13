package com.induztek.usuarios.security;
import java.util.regex.Pattern;
public final class PasswordPolicy {
 private PasswordPolicy(){}
 // Acepta letras Unicode (incluida ñ/Ñ), números y símbolos explícitos.
 private static final Pattern POLICY = Pattern.compile("^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*[0-9])(?=.*[!@#$%&*()_+\\-=\\[\\]{};':\\\\|,.<>/?¿¡~^]).{8,72}$");
 public static boolean valida(String password){ return password != null && !password.chars().anyMatch(Character::isWhitespace) && POLICY.matcher(password).matches(); }
 public static void exigir(String password){
   if(!valida(password)) throw new IllegalArgumentException("La contraseña debe tener 8 a 72 caracteres, mayúscula, minúscula, número y un símbolo permitido.");
 }
}
