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
import pt.ipvc.vending.domain.enums.EstadoProposta;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vending_machine_id", nullable = false)
    private VendingMachine vendingMachine;

    @Column(nullable = false)
    private LocalDate dataProposta = LocalDate.now();

    @Column(nullable = false)
    private BigDecimal valorProposto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProposta estado = EstadoProposta.PENDENTE;

    private String observacoes;

    private BigDecimal valorGestor;

    private String observacoesGestor;

    private Integer duracaoAnos;

    public Proposta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public VendingMachine getVendingMachine() {
        return vendingMachine;
    }

    public void setVendingMachine(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    public LocalDate getDataProposta() {
        return dataProposta;
    }

    public void setDataProposta(LocalDate dataProposta) {
        this.dataProposta = dataProposta;
    }

    public BigDecimal getValorProposto() {
        return valorProposto;
    }

    public void setValorProposto(BigDecimal valorProposto) {
        this.valorProposto = valorProposto;
    }

    public EstadoProposta getEstado() {
        return estado;
    }

    public void setEstado(EstadoProposta estado) {
        this.estado = estado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getValorGestor() {
        return valorGestor;
    }

    public void setValorGestor(BigDecimal valorGestor) {
        this.valorGestor = valorGestor;
    }

    public String getObservacoesGestor() {
        return observacoesGestor;
    }

    public void setObservacoesGestor(String observacoesGestor) {
        this.observacoesGestor = observacoesGestor;
    }

    public Integer getDuracaoAnos() {
        return duracaoAnos;
    }

    public void setDuracaoAnos(Integer duracaoAnos) {
        this.duracaoAnos = duracaoAnos;
    }
}
