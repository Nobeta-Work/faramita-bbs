package online.faramita.bbs.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import online.faramita.bbs.common.enums.AuditLogType;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogEvent {

    private final String TIME;

    private final AuditLogType TYPE;

    private final String OPERATOR;

    private final String MODULE;

    private final String MESSAGE;

    private final Object DATA;
}
