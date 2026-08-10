package cn.nobeta.bbs.common.util;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.enums.AuditLogType;
import cn.nobeta.bbs.common.model.AuditLogEvent;
import cn.nobeta.bbs.security.util.SecurityUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogUtil {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final ObjectMapper objectMapper;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public String resolveOperator() {

        Long userId = SecurityUtil.getLoginUserId();

        if (userId != null) {
            return "user<" + userId + ">";
        }

        /**
         * ? knowledge
         * RequestContextHolder 是 Spring Web 工具类，用于全局获取当前 HTTP 请求上下文
         * getRequestAttributes() 获取封装请求、会话信息的RequestAttributes 对象
         * 如果返回结果为 null，则：当前线程 **不是 HTTP 请求线程**，更可能是定时任务、异步线程等非 web 场景
         */
        return RequestContextHolder.getRequestAttributes() == null ? "system" : "anonymous";

    }

    public void printLog(
        Method method, Object[] args, AuditLog auditLog,
        AuditLogType type, String module, Throwable failure
    ) {

        try {
            Object data = resolveData(method, args, auditLog.data());

            if (failure != null) {
                Map<String, Object> errorData = new LinkedHashMap<>();

                if (data != null) {
                    errorData.put("data", data);
                }

                errorData.put("exception", failure.getClass().getSimpleName());
                errorData.put("eMsg", failure.getMessage());

                data = errorData;
                type = AuditLogType.WARN;
            }

            AuditLogEvent event = AuditLogEvent.builder()
                    .TIME(LocalDateTime.now(ZoneId.of("+8")).toString())
                    .TYPE(type)
                    .OPERATOR(resolveOperator())
                    .MODULE(module)
                    .MESSAGE(auditLog.message())
                    .DATA(data)
                    .build();

            printByLombok(event);

        } catch (Exception ex) {
            log.warn("Aduit log write failed.", ex);
        }

    }

    private Object resolveData(Method method, Object[] args, String expressions) {
        
        if (!StringUtils.hasText(expressions)) {
            return null;
        }

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(new Object(), method, args, parameterNameDiscoverer);

        return parser.parseExpression(expressions).getValue(context);
        
    }


    private void printByLombok(AuditLogEvent event) throws JsonProcessingException {
        AuditLogType type = event.getTYPE();

        // 构造 lombok 日志消息
        StringBuilder sb = new StringBuilder()
                .append(event.getOPERATOR())
                .append("|")
                .append(event.getMODULE());

        if (StringUtils.hasText(event.getMESSAGE())) {
            sb.append(" -m \"").append(event.getMESSAGE()).append("\"");
        }

        if (event.getDATA() != null) {
            sb.append(" : ").append(objectMapper.writeValueAsString(event.getDATA()));
        }

        String message = sb.toString();

        switch (type) {
            case INFO -> log.info("{}", message);
            case WARN -> log.warn("{}", message);
            case ERROR -> log.error("{}", message);
        }
    }
}
