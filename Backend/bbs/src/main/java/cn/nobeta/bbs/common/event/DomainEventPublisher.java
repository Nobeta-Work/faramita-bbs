package cn.nobeta.bbs.common.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
