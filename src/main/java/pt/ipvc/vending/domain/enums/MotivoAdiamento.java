package pt.ipvc.vending.domain.enums;

public enum MotivoAdiamento {
    CLIENTE_AUSENTE,
    PROBLEMA_TECNICO,
    ACESSO_INDISPONIVEL,
    MAQUINA_NAO_ENTREGUE,
    OUTRO;

    public String getLabel() {
        return switch (this) {
            case CLIENTE_AUSENTE      -> "Cliente Ausente";
            case PROBLEMA_TECNICO     -> "Problema Técnico";
            case ACESSO_INDISPONIVEL  -> "Acesso Indisponível";
            case MAQUINA_NAO_ENTREGUE -> "Máquina Não Entregue";
            case OUTRO                -> "Outro";
        };
    }
}
