// Mensagem de feedback. Para erros da API, mostra também os problemas de cada campo.
function Alerta({ tipo = 'erro', erro, children }) {
  if (!erro && !children) return null;

  const campos = erro?.campos ? Object.entries(erro.campos) : [];

  return (
    <div className={`alerta alerta-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      <p>{erro ? erro.message : children}</p>
      {campos.length > 0 && (
        <ul>
          {campos.map(([campo, mensagem]) => (
            <li key={campo}>
              <strong>{campo.split('.').pop()}:</strong> {mensagem}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Alerta;
