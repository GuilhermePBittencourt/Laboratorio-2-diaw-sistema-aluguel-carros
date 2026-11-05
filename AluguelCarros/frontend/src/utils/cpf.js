// Mesma regra de validação do back-end (dígitos verificadores do CPF).

function digitoVerificador(cpf, quantidade) {
  let soma = 0;
  for (let i = 0; i < quantidade; i += 1) {
    soma += Number(cpf[i]) * (quantidade + 1 - i);
  }
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

export function cpfValido(texto) {
  const cpf = (texto ?? '').replace(/\D/g, '');
  if (cpf.length !== 11 || new Set(cpf).size === 1) {
    return false;
  }
  return digitoVerificador(cpf, 9) === Number(cpf[9]) && digitoVerificador(cpf, 10) === Number(cpf[10]);
}
