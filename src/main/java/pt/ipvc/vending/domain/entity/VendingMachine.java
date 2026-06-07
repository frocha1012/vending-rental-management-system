package pt.ipvc.vending.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.ipvc.vending.domain.enums.EstadoVendingMachine;

import java.math.BigDecimal;

@Entity
@Table(name = "vending_machines")
public class VendingMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String modelo;

    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVendingMachine estado = EstadoVendingMachine.DISPONIVEL;

    @Column(nullable = false)
    private BigDecimal precoAluguerMensal;

    public VendingMachine() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public EstadoVendingMachine getEstado() {
        return estado;
    }

    public void setEstado(EstadoVendingMachine estado) {
        this.estado = estado;
    }

    public BigDecimal getPrecoAluguerMensal() {
        return precoAluguerMensal;
    }

    public void setPrecoAluguerMensal(BigDecimal precoAluguerMensal) {
        this.precoAluguerMensal = precoAluguerMensal;
    }
}
