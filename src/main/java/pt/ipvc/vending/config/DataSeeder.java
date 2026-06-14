package pt.ipvc.vending.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import pt.ipvc.vending.domain.entity.Cliente;
import pt.ipvc.vending.domain.enums.BackOfficeRole;
import pt.ipvc.vending.repository.BackOfficeUserRepository;
import pt.ipvc.vending.service.BackOfficeUserService;
import pt.ipvc.vending.domain.entity.Contrato;
import pt.ipvc.vending.domain.entity.Instalacao;
import pt.ipvc.vending.domain.entity.Proposta;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.domain.enums.EstadoCliente;
import pt.ipvc.vending.domain.enums.EstadoContrato;
import pt.ipvc.vending.domain.enums.EstadoInstalacao;
import pt.ipvc.vending.domain.enums.EstadoProposta;
import pt.ipvc.vending.domain.enums.EstadoVendingMachine;
import pt.ipvc.vending.repository.ClienteRepository;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.InstalacaoRepository;
import pt.ipvc.vending.repository.PropostaRepository;
import pt.ipvc.vending.repository.VendingMachineRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final VendingMachineRepository vendingMachineRepository;
    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BackOfficeUserService backOfficeUserService;

    public DataSeeder(ClienteRepository clienteRepository,
                      VendingMachineRepository vendingMachineRepository,
                      ContratoRepository contratoRepository,
                      PropostaRepository propostaRepository,
                      InstalacaoRepository instalacaoRepository,
                      BCryptPasswordEncoder passwordEncoder,
                      BackOfficeUserService backOfficeUserService) {
        this.clienteRepository = clienteRepository;
        this.vendingMachineRepository = vendingMachineRepository;
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.backOfficeUserService = backOfficeUserService;
    }

    @Override
    public void run(String... args) {
        seedBackOfficeUsers();
        if (clienteRepository.count() == 0) {
            seedClientData();
        }
    }

    private void seedBackOfficeUsers() {
        backOfficeUserService.seedIfAbsent("admin",         "admin123",         BackOfficeRole.ADMIN);
        backOfficeUserService.seedIfAbsent("gestor",        "gestor123",        BackOfficeRole.GESTOR);
        backOfficeUserService.seedIfAbsent("rececionista",  "rececionista123",  BackOfficeRole.RECECIONISTA);
        backOfficeUserService.seedIfAbsent("tecnico",       "tecnico123",       BackOfficeRole.TECNICO);
    }

    private void seedClientData() {

        Cliente cliente1 = new Cliente();
        cliente1.setNome("Escola Secundaria de Viseu");
        cliente1.setEmail("geral@escolaviseu.pt");
        cliente1.setTelefone("232000111");
        cliente1.setNif("500100200");
        cliente1.setEstado(EstadoCliente.ATIVO);
        cliente1.setDataRegisto(LocalDate.of(2024, 1, 15));
        cliente1.setUsername("escola");
        cliente1.setPassword(passwordEncoder.encode("1234"));
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNome("Ginasio FitViseu");
        cliente2.setEmail("contacto@fitviseu.pt");
        cliente2.setTelefone("232000222");
        cliente2.setNif("500100201");
        cliente2.setEstado(EstadoCliente.ATIVO);
        cliente2.setDataRegisto(LocalDate.of(2024, 3, 10));
        cliente2.setUsername("ginasio");
        cliente2.setPassword(passwordEncoder.encode("1234"));
        cliente2 = clienteRepository.save(cliente2);

        VendingMachine vm1 = new VendingMachine();
        vm1.setCodigo("VM-001");
        vm1.setModelo("SnackPro 300");
        vm1.setLocalizacao("Armazem Central");
        vm1.setEstado(EstadoVendingMachine.DISPONIVEL);
        vm1.setPrecoAluguerMensal(new BigDecimal("150.00"));
        vm1 = vendingMachineRepository.save(vm1);

        VendingMachine vm2 = new VendingMachine();
        vm2.setCodigo("VM-002");
        vm2.setModelo("BebidasMax 200");
        vm2.setLocalizacao("Ginasio FitViseu");
        vm2.setEstado(EstadoVendingMachine.ALUGADA);
        vm2.setPrecoAluguerMensal(new BigDecimal("180.00"));
        vm2 = vendingMachineRepository.save(vm2);

        VendingMachine vm3 = new VendingMachine();
        vm3.setCodigo("VM-003");
        vm3.setModelo("ComboFresh 150");
        vm3.setLocalizacao("Armazem Central");
        vm3.setEstado(EstadoVendingMachine.MANUTENCAO);
        vm3.setPrecoAluguerMensal(new BigDecimal("120.00"));
        vm3 = vendingMachineRepository.save(vm3);

        Contrato contrato1 = new Contrato();
        contrato1.setCliente(cliente2);
        contrato1.setVendingMachine(vm2);
        contrato1.setDataInicio(LocalDate.of(2024, 4, 1));
        contrato1.setDataFim(LocalDate.of(2025, 3, 31));
        contrato1.setValorMensal(new BigDecimal("180.00"));
        contrato1.setEstado(EstadoContrato.ATIVO);
        contrato1 = contratoRepository.save(contrato1);

        Proposta proposta1 = new Proposta();
        proposta1.setCliente(cliente1);
        proposta1.setVendingMachine(vm1);
        proposta1.setDataProposta(LocalDate.of(2024, 5, 20));
        proposta1.setValorProposto(new BigDecimal("145.00"));
        proposta1.setEstado(EstadoProposta.PENDENTE);
        proposta1.setObservacoes("Proposta para cantina da escola");
        propostaRepository.save(proposta1);

        Instalacao instalacao1 = new Instalacao();
        instalacao1.setContrato(contrato1);
        instalacao1.setDataInstalacao(LocalDate.of(2024, 3, 28));
        instalacao1.setLocalInstalacao("Rececao do Ginasio FitViseu");
        instalacao1.setEstado(EstadoInstalacao.CONCLUIDA);
        instalacao1.setObservacoes("Instalacao concluida sem incidentes");
        instalacaoRepository.save(instalacao1);
    }
}
