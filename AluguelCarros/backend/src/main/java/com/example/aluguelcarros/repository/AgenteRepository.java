package com.example.aluguelcarros.repository;

import com.example.aluguelcarros.model.Agente;
import com.example.aluguelcarros.model.TipoAgente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenteRepository extends JpaRepository<Agente, Long> {

    Optional<Agente> findByUsuarioId(Long usuarioId);

    List<Agente> findAllByOrderByNomeAsc();

    List<Agente> findByTipoOrderByNomeAsc(TipoAgente tipo);
}
