package pt.ipvc.vending.domain.entity;

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
import jakarta.persistence.Table;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.domain.enums.MotivoAdiamento;

import java.time.LocalDate;

@Entity
@Table(name = "instalacoes")
public class Instalacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    private LocalDate dataInstalacao;

    @Column(nullable = false)
    private String localInstalacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInstalacao estado = EstadoInstalacao.AGENDADA;

    private String observacoes;

    /** Set automatically when estado transitions to CONCLUIDA. */
    private LocalDate dataConclusao;

    /** Reason for postponement — set when estado transitions to ADIADA. */
    @Enumerated(EnumType.STRING)
    private MotivoAdiamento motivoAdiamento;

    /** New scheduled date — set when estado transitions to ADIADA. */
    private LocalDate novaDataAgendada;

    public Instalacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public LocalDate getDataInstalacao() {
        return dataInstalacao;
    }

    public void setDataInstalacao(LocalDate dataInstalacao) {
        this.dataInstalacao = dataInstalacao;
    }

    public String getLocalInstalacao() {
        return localInstalacao;
    }

    public void setLocalInstalacao(String localInstalacao) {
        this.localInstalacao = localInstalacao;
    }

    public EstadoInstalacao getEstado() {
        return estado;
    }

    public void setEstado(EstadoInstalacao estado) {
        this.estado = estado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDate dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public MotivoAdiamento getMotivoAdiamento() {
        return motivoAdiamento;
    }

    public void setMotivoAdiamento(MotivoAdiamento motivoAdiamento) {
        this.motivoAdiamento = motivoAdiamento;
    }

    public LocalDate getNovaDataAgendada() {
        return novaDataAgendada;
    }

    public void setNovaDataAgendada(LocalDate novaDataAgendada) {
        this.novaDataAgendada = novaDataAgendada;
    }
}
