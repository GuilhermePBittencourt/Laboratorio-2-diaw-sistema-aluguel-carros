package com.example.aluguelcarros.service;

import com.example.aluguelcarros.dto.AutomovelRequest;
import com.example.aluguelcarros.dto.AutomovelResponse;
import com.example.aluguelcarros.exception.ConflitoException;
import com.example.aluguelcarros.model.Automovel;
import com.example.aluguelcarros.repository.AutomovelRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomovelService {

    private final AutomovelRepository automovelRepository;

    public AutomovelService(AutomovelRepository automovelRepository) {
        this.automovelRepository = automovelRepository;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automovelRepository.findAllByOrderByMarcaAscModeloAsc().stream().map(AutomovelResponse::de).toList();
    }

    @Transactional
    public AutomovelResponse cadastrar(AutomovelRequest dados) {
        String placa = dados.placa().replace("-", "").toUpperCase(Locale.ROOT);
        String matricula = dados.matricula().trim();

        if (automovelRepository.existsByPlaca(placa)) {
            throw new ConflitoException("Já existe um automóvel com esta placa.");
        }
        if (automovelRepository.existsByMatricula(matricula)) {
            throw new ConflitoException("Já existe um automóvel com esta matrícula.");
        }

        Automovel automovel = new Automovel(matricula, dados.ano(), dados.marca().trim(), dados.modelo().trim(),
                placa, dados.proprietario());
        return AutomovelResponse.de(automovelRepository.save(automovel));
    }
}
