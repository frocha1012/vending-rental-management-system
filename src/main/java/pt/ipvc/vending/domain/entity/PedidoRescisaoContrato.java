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
import pt.ipvc.vending.domain.enums.EstadoPedidoRescisao;
import pt.ipvc.vending.domain.enums.MotivoRescisao;

import java.time.LocalDate;

@Entity
@Table(name = "pedidos_rescisao")
public class PedidoRescisaoContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoRescisao motivo;

    private String descricao;

    @Column(nullable = false)
    private LocalDate dataPedido = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedidoRescisao estado = EstadoPedidoRescisao.PENDENTE;

    public PedidoRescisaoContrato() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contrato getContrato() { return contrato; }
    public void setContrato(Contrato contrato) { this.contrato = contrato; }

    public MotivoRescisao getMotivo() { return motivo; }
    public void setMotivo(MotivoRescisao motivo) { this.motivo = motivo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDate dataPedido) { this.dataPedido = dataPedido; }

    public EstadoPedidoRescisao getEstado() { return estado; }
    public void setEstado(EstadoPedidoRescisao estado) { this.estado = estado; }
}
