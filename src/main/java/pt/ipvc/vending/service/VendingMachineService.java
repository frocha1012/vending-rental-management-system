package pt.ipvc.vending.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipvc.vending.domain.entity.VendingMachine;
import pt.ipvc.vending.repository.ContratoRepository;
import pt.ipvc.vending.repository.PropostaRepository;
import pt.ipvc.vending.repository.VendingMachineRepository;
import pt.ipvc.vending.service.exception.EntidadeEmUsoException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendingMachineService {

    private final VendingMachineRepository vendingMachineRepository;
    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;

    public VendingMachineService(VendingMachineRepository vendingMachineRepository,
                                 ContratoRepository contratoRepository,
                                 PropostaRepository propostaRepository) {
        this.vendingMachineRepository = vendingMachineRepository;
        this.contratoRepository = contratoRepository;
        this.propostaRepository = propostaRepository;
    }

    public List<VendingMachine> listarTodas() {
        return vendingMachineRepository.findAll();
    }

    public Optional<VendingMachine> obterPorId(Long id) {
        return vendingMachineRepository.findById(id);
    }

    public VendingMachine guardar(VendingMachine vendingMachine) {
        return vendingMachineRepository.save(vendingMachine);
    }

    public void eliminar(Long id) {
        if (contratoRepository.existsByVendingMachineId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete machine because it is associated with active contracts.");
        }
        if (propostaRepository.existsByVendingMachineId(id)) {
            throw new EntidadeEmUsoException(
                    "Cannot delete machine because it is associated with proposals.");
        }
        vendingMachineRepository.deleteById(id);
    }
}
