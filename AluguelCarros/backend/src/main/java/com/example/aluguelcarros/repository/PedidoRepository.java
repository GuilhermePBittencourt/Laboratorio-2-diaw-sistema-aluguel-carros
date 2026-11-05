package com.example.aluguelcarros.repository;

import com.example.aluguelcarros.model.PedidoAluguel;
import com.example.aluguelcarros.model.StatusPedido;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<PedidoAluguel, Long> {

    List<PedidoAluguel> findAllByOrderByCriadoEmDesc();

    List<PedidoAluguel> findByStatusOrderByCriadoEmDesc(StatusPedido status);

    List<PedidoAluguel> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    List<PedidoAluguel> findByClienteIdAndStatusOrderByCriadoEmDesc(Long clienteId, StatusPedido status);

    boolean existsByClienteIdAndStatusIn(Long clienteId, Collection<StatusPedido> status);

    void deleteByClienteId(Long clienteId);

    /**
     * Há outro pedido que ocupa o mesmo automóvel em algum dia do período?
     * "ignorarId" permite desconsiderar o próprio pedido ao modificá-lo (use 0 quando não houver).
     */
    @Query("""
            select count(p) > 0 from PedidoAluguel p
            where p.automovel.id = :automovelId
              and p.status in :status
              and p.dataInicio <= :fim
              and p.dataFim >= :inicio
              and p.id <> :ignorarId
            """)
    boolean existeConflitoDePeriodo(@Param("automovelId") Long automovelId,
                                    @Param("inicio") LocalDate inicio,
                                    @Param("fim") LocalDate fim,
                                    @Param("status") Collection<StatusPedido> status,
                                    @Param("ignorarId") Long ignorarId);
}
