package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.VendingMachine;

public interface VendingMachineRepository extends JpaRepository<VendingMachine, Long> {
}
