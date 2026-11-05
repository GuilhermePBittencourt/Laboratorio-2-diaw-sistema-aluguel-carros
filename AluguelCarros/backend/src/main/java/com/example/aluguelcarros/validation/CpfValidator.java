package com.example.aluguelcarros.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        // Campo vazio é responsabilidade do @NotBlank
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return validar(valor);
    }

    public static boolean validar(String texto) {
        String cpf = somenteDigitos(texto);
        if (cpf.length() != 11 || todosIguais(cpf)) {
            return false;
        }
        int primeiro = digitoVerificador(cpf, 9);
        int segundo = digitoVerificador(cpf, 10);
        return primeiro == Character.getNumericValue(cpf.charAt(9))
                && segundo == Character.getNumericValue(cpf.charAt(10));
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private static boolean todosIguais(String cpf) {
        return cpf.chars().distinct().count() == 1;
    }

    /** Calcula o dígito verificador usando os primeiros "quantidade" dígitos. */
    private static int digitoVerificador(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
