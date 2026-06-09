package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.AuditAction;
import pt.ipvc.vending.repository.ClienteRepository;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.PropostaRepository;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final AuditLogService auditLogService;

    public ClienteService(ClienteRepository clienteRepository,
                          ContratoRepository contratoRepository,
                          PropostaRepository propostaRepository,
                          AuditLogService auditLogService) {
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
        this.auditLogService = auditLogService;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obterPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente guardar(Cliente cliente) {
        boolean isNew = cliente.getId() == null;
        Cliente saved = clienteRepository.save(cliente);
        if (isNew) {
            auditLogService.logCreate("Cliente", saved.getId(),
                    "Cliente criado: " + saved.getNome() + " (NIF: " + saved.getNif() + ")");
        } else {
            auditLogService.logUpdate("Cliente", saved.getId(),
                    "Cliente atualizado: " + saved.getNome(), null, null);
        }
        return saved;
    }

    public void eliminar(Long id) {
        if (contratoRepository.existsByClienteId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete client because it has active contracts.");
        }
        if (propostaRepository.existsByClienteId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete client because it has active proposals.");
        }
        Cliente c = clienteRepository.findById(id).orElse(null);
        clienteRepository.deleteById(id);
        auditLogService.logDelete("Cliente", id,
                "Cliente eliminado: " + (c != null ? c.getNome() : id));
    }

    /**
     * Lets a logged-in client update only their own contact data.
     * nome, NIF, estado, username and dataRegisto are never touched.
     * Password is only updated when a non-blank value is provided.
     */
    public Cliente atualizarDadosProprios(Long id, String email, String telefone,
                                          String morada, String novaPassword) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email é obrigatório.");
        }

        String oldEmail = cliente.getEmail();
        cliente.setEmail(email.trim());
        cliente.setTelefone(telefone != null && !telefone.isBlank() ? telefone.trim() : null);
        cliente.setMorada(morada != null && !morada.isBlank() ? morada.trim() : null);

        if (novaPassword != null && !novaPassword.isBlank()) {
            cliente.setPassword(novaPassword.trim());
        }

        Cliente saved = clienteRepository.save(cliente);
        auditLogService.logUpdate("Cliente", saved.getId(),
                "Cliente atualizou os seus dados de contacto",
                "email=" + oldEmail, "email=" + email.trim());
        return saved;
    }
}
