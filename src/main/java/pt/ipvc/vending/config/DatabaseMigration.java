package pt.ipvc.vending.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Drops and recreates check constraints that Hibernate generates on initial schema
 * creation but never updates when enum values are added later (ddl-auto=update).
 *
 * Order(1) ensures this runs before DataSeeder (Order(2) by default).
 */
@Component
@Order(1)
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DatabaseMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        fixPropostasEstadoCheck();
        fixInstalacoesEstadoCheck();
        fixAuditLogsActionCheck();
    }

    private void fixInstalacoesEstadoCheck() {
        jdbc.execute(
            "ALTER TABLE instalacoes DROP CONSTRAINT IF EXISTS instalacoes_estado_check"
        );
        jdbc.execute(
            "ALTER TABLE instalacoes ADD CONSTRAINT instalacoes_estado_check " +
            "CHECK (estado IN (" +
            "'AGENDADA', 'EM_CURSO', 'CONCLUIDA', 'CANCELADA', 'ADIADA'" +
            "))"
        );
    }

    private void fixAuditLogsActionCheck() {
        jdbc.execute(
            "ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_action_check"
        );
        jdbc.execute(
            "ALTER TABLE audit_logs ADD CONSTRAINT audit_logs_action_check " +
            "CHECK (action IN (" +
            "'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', " +
            "'LOGIN', 'LOGIN_FAILED', 'LOGOUT', " +
            "'ACCEPT', 'REJECT', 'COUNTER_PROPOSAL', " +
            "'TERMINATION_REQUEST', 'INSTALLATION_COMPLETED', 'INSTALLATION_DELAYED', " +
            "'ACCOUNT_REQUEST_SUBMITTED', 'ACCOUNT_REQUEST_APPROVED', 'ACCOUNT_REQUEST_REJECTED', " +
            "'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET_COMPLETED', 'PASSWORD_RESET_FAILED'" +
            "))"
        );
    }

    private void fixPropostasEstadoCheck() {
        // Drop the old constraint (safe — IF EXISTS prevents failure on first run)
        jdbc.execute(
            "ALTER TABLE propostas DROP CONSTRAINT IF EXISTS propostas_estado_check"
        );

        // Recreate it with all current EstadoProposta enum values
        jdbc.execute(
            "ALTER TABLE propostas ADD CONSTRAINT propostas_estado_check " +
            "CHECK (estado IN (" +
            "'PENDENTE', 'EM_ANALISE', 'ENVIADA_CLIENTE', " +
            "'ACEITE', 'REJEITADA', 'CONTRAPROPOSTA', 'EXPIRADA'" +
            "))"
        );
    }
}
