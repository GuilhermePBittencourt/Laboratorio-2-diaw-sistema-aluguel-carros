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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Pedido de aluguel feito por um cliente e analisado por um agente. */
@Entity
@Table(name = "pedido_aluguel")
public class PedidoAluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "automovel_id", nullable = false)
    private Automovel automovel;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(length = 500)
    private String parecer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliado_por_id")
    private Agente avaliadoPor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PedidoAluguel() {
    }

    public PedidoAluguel(Cliente cliente, Automovel automovel, LocalDate dataInicio, LocalDate dataFim) {
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = StatusPedido.EM_ANALISE;
    }

    public void alterar(Automovel novoAutomovel, LocalDate novoInicio, LocalDate novoFim) {
        if (!status.aceitaAlteracao()) {
            throw new IllegalStateException("Só é possível modificar pedidos que estão em análise.");
        }
        this.automovel = novoAutomovel;
        this.dataInicio = novoInicio;
        this.dataFim = novoFim;
    }

    public void cancelar() {
        transitarPara(StatusPedido.CANCELADO);
    }

    public void avaliar(Agente agente, Decisao decisao, String parecerFinanceiro) {
        StatusPedido destino = decisao == Decisao.APROVAR ? StatusPedido.APROVADO : StatusPedido.REPROVADO;
        transitarPara(destino);
        this.avaliadoPor = agente;
        this.parecer = parecerFinanceiro;
    }

    public void marcarComoContratado() {
        transitarPara(StatusPedido.CONTRATADO);
    }

    private void transitarPara(StatusPedido destino) {
        if (!status.podeIrPara(destino)) {
            throw new IllegalStateException(
                    "Não é possível mudar o pedido de " + status + " para " + destino + ".");
        }
        this.status = destino;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Automovel getAutomovel() {
        return automovel;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getParecer() {
        return parecer;
    }

    public Agente getAvaliadoPor() {
        return avaliadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
