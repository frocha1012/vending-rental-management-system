package pt.ipvc.vending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ipvc.vending.domain.entity.AccountRequest;
import pt.ipvc.vending.domain.enums.EstadoAccountRequest;

import java.util.List;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {

    List<AccountRequest> findByEstadoOrderByDataPedidoDesc(EstadoAccountRequest estado);

    List<AccountRequest> findAllByOrderByDataPedidoDesc();

    boolean existsByUsernameRequestedAndEstado(String usernameRequested, EstadoAccountRequest estado);

    boolean existsByNifAndEstado(String nif, EstadoAccountRequest estado);

    boolean existsByEmailAndEstado(String email, EstadoAccountRequest estado);
}
