package com.example.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Contrato executado por um agente a partir de um pedido aprovado.
 * Pode estar associado a um crédito concedido por um banco.
 */
@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoAluguel pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProprietario proprietario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_credito_id")
    private Agente bancoCredito;

    @Column(name = "valor_credito", precision = 12, scale = 2)
    private BigDecimal valorCredito;

    @Column(name = "data_contrato", nullable = false)
    private Instant dataContrato;

    protected Contrato() {
    }

    public Contrato(PedidoAluguel pedido, TipoProprietario proprietario, Agente agente,
                    Agente bancoCredito, BigDecimal valorCredito) {
        this.pedido = pedido;
        this.proprietario = proprietario;
        this.agente = agente;
        this.bancoCredito = bancoCredito;
        this.valorCredito = valorCredito;
        this.dataContrato = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public PedidoAluguel getPedido() {
        return pedido;
    }

    public TipoProprietario getProprietario() {
        return proprietario;
    }

    public Agente getAgente() {
        return agente;
    }

    public Agente getBancoCredito() {
        return bancoCredito;
    }

    public BigDecimal getValorCredito() {
        return valorCredito;
    }

    public Instant getDataContrato() {
        return dataContrato;
    }
}
