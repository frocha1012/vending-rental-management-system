package pt.ipvc.vending.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.ipvc.vending.domain.enums.EstadoAccountRequest;

import java.time.LocalDate;

@Entity
@Table(name = "account_requests")
public class AccountRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String nif;

    @Column(nullable = false)
    private String email;

    private String telefone;

    private String morada;

    @Column(nullable = false, unique = true)
    private String usernameRequested;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAccountRequest estado = EstadoAccountRequest.PENDENTE;

    @Column(nullable = false)
    private LocalDate dataPedido = LocalDate.now();

    private String observacoes;

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

    public String getNome()                          { return nome; }
    public void setNome(String nome)                 { this.nome = nome; }

    public String getNif()                           { return nif; }
    public void setNif(String nif)                   { this.nif = nif; }

    public String getEmail()                         { return email; }
    public void setEmail(String email)               { this.email = email; }

    public String getTelefone()                      { return telefone; }
    public void setTelefone(String telefone)         { this.telefone = telefone; }

    public String getMorada()                        { return morada; }
    public void setMorada(String morada)             { this.morada = morada; }

    public String getUsernameRequested()                       { return usernameRequested; }
    public void setUsernameRequested(String usernameRequested) { this.usernameRequested = usernameRequested; }

    public String getPasswordHash()                  { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public EstadoAccountRequest getEstado()          { return estado; }
    public void setEstado(EstadoAccountRequest e)    { this.estado = e; }

    public LocalDate getDataPedido()                 { return dataPedido; }
    public void setDataPedido(LocalDate d)           { this.dataPedido = d; }

    public String getObservacoes()                   { return observacoes; }
    public void setObservacoes(String observacoes)   { this.observacoes = observacoes; }
}
