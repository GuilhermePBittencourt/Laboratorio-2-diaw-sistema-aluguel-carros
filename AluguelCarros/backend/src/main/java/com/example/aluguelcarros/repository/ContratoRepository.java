package com.example.aluguelcarros.repository;

import com.example.aluguelcarros.model.Contrato;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    Optional<Contrato> findByPedidoId(Long pedidoId);

    List<Contrato> findByPedidoIdIn(Collection<Long> pedidoIds);
}
