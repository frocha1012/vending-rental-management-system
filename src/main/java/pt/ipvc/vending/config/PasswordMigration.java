package pt.ipvc.vending.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.repository.ClienteRepository;

import java.util.List;

/**
 * Runs before DataSeeder (@Order(2)) to ensure any pre-existing plaintext
 * passwords are hashed. Safe to run repeatedly: already-hashed values
 * (recognised by the "$2" prefix) are skipped.
 */
@Component
@Order(1)
public class PasswordMigration implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordMigration(ClienteRepository clienteRepository,
                             BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Cliente> clientes = clienteRepository.findAll();
        for (Cliente cliente : clientes) {
            String pwd = cliente.getPassword();
            if (pwd != null && !pwd.startsWith("$2")) {
                cliente.setPassword(passwordEncoder.encode(pwd));
                clienteRepository.save(cliente);
            }
        }
    }
}
