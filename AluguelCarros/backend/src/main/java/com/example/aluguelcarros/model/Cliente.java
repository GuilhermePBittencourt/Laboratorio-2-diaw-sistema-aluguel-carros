package com.example.aluguelcarros.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Contratante do aluguel: dados de identificação, profissão e até 3 rendimentos. */
@Entity
@Table(name = "cliente")
public class Cliente {

    public static final int MAX_RENDIMENTOS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 255)
    private String endereco;

    @Column(nullable = false, length = 80)
    private String profissao;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cliente_rendimento", joinColumns = @JoinColumn(name = "cliente_id"))
    private List<Rendimento> rendimentos = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(Usuario usuario, String rg, String cpf, String nome, String endereco, String profissao,
                   List<Rendimento> rendimentos) {
        this.usuario = usuario;
        atualizarDados(rg, cpf, nome, endereco, profissao, rendimentos);
    }

    public void atualizarDados(String rg, String cpf, String nome, String endereco, String profissao,
                               List<Rendimento> rendimentos) {
        this.rg = rg;
        this.cpf = cpf;
        this.nome = nome;
        this.endereco = endereco;
        this.profissao = profissao;
        definirRendimentos(rendimentos);
    }

    private void definirRendimentos(List<Rendimento> novos) {
        if (novos.size() > MAX_RENDIMENTOS) {
            throw new IllegalArgumentException("Um cliente pode ter no máximo " + MAX_RENDIMENTOS + " rendimentos.");
        }
        this.rendimentos.clear();
        this.rendimentos.addAll(novos);
    }

    public Long getId() {
        return id;
    }

    public String getRg() {
        return rg;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getProfissao() {
        return profissao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<Rendimento> getRendimentos() {
        return Collections.unmodifiableList(rendimentos);
    }
}
