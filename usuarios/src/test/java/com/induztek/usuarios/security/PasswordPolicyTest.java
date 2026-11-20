package com.induztek.usuarios.security;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PasswordPolicyTest {
 @Test void aceptaClaveDePrueba(){assertTrue(PasswordPolicy.valida("Abc1234@"));}
 @Test void aceptaLetrasEspanolas(){assertTrue(PasswordPolicy.valida("Ñandú123@"));}
 @Test void rechazaSinSimbolo(){assertFalse(PasswordPolicy.valida("Abc12345"));}
 @Test void enyeNoCuentaComoSimbolo(){assertFalse(PasswordPolicy.valida("Ñandú1234"));}
 @Test void rechazaSinMayuscula(){assertFalse(PasswordPolicy.valida("abc1234@"));}
 @Test void rechazaEspacios(){assertFalse(PasswordPolicy.valida("Abc 1234@"));}
}
