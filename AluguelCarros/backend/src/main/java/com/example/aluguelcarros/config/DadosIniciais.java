package com.example.aluguelcarros.config;

import com.example.aluguelcarros.model.Agente;
import com.example.aluguelcarros.model.Automovel;
import com.example.aluguelcarros.model.Perfil;
import com.example.aluguelcarros.model.TipoAgente;
import com.example.aluguelcarros.model.TipoProprietario;
import com.example.aluguelcarros.model.Usuario;
import com.example.aluguelcarros.repository.AgenteRepository;
import com.example.aluguelcarros.repository.AutomovelRepository;
import com.example.aluguelcarros.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria, se ainda não existirem, dois agentes de demonstração (uma empresa e um banco) e alguns automóveis.
 * Agentes não têm cadastro público, então é por aqui que o primeiro agente passa a existir.
 * Desligue com SEED_ENABLED=false em produção.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioRepository usuarioRepository;
    private final AgenteRepository agenteRepository;
    private final AutomovelRepository automovelRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaAgentes;

    public DadosIniciais(UsuarioRepository usuarioRepository, AgenteRepository agenteRepository,
                         AutomovelRepository automovelRepository, PasswordEncoder passwordEncoder,
                         @Value("${app.seed.senha-agentes:}") String senhaAgentes) {
        this.usuarioRepository = usuarioRepository;
        this.agenteRepository = agenteRepository;
        this.automovelRepository = automovelRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaAgentes = senhaAgentes;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarAgentes();
        criarAutomoveis();
    }

    private void criarAgentes() {
        if (senhaAgentes.isBlank()) {
            log.warn("SEED_SENHA_AGENTES não foi definida: os agentes de demonstração não serão criados.");
            return;
        }
        criarAgente("agente.empresa", "Locadora Estrada Livre", TipoAgente.EMPRESA);
        criarAgente("agente.banco", "Banco Horizonte", TipoAgente.BANCO);
    }

    private void criarAgente(String login, String nome, TipoAgente tipo) {
        if (usuarioRepository.existsByLogin(login)) {
            return;
        }
        Usuario usuario = usuarioRepository.save(new Usuario(login, passwordEncoder.encode(senhaAgentes), Perfil.AGENTE));
        agenteRepository.save(new Agente(usuario, nome, tipo));
        log.info("Agente de demonstração criado: {}", login);
    }

    private void criarAutomoveis() {
        if (automovelRepository.count() > 0) {
            return;
        }
        automovelRepository.save(new Automovel("MAT-0001", 2023, "Fiat", "Argo", "ABC1D23", TipoProprietario.EMPRESA));
        automovelRepository.save(new Automovel("MAT-0002", 2022, "Volkswagen", "Polo", "BRA2E19", TipoProprietario.EMPRESA));
        automovelRepository.save(new Automovel("MAT-0003", 2024, "Chevrolet", "Onix", "QWE3R45", TipoProprietario.BANCO));
        automovelRepository.save(new Automovel("MAT-0004", 2021, "Toyota", "Corolla", "JKL4M56", TipoProprietario.BANCO));
        automovelRepository.save(new Automovel("MAT-0005", 2023, "Hyundai", "HB20", "XYZ5N67", TipoProprietario.EMPRESA));
    }
}
