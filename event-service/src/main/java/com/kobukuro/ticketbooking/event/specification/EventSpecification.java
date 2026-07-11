package com.kobukuro.ticketbooking.event.specification;

import com.kobukuro.ticketbooking.event.entity.EventCategory;
import com.kobukuro.ticketbooking.event.entity.EventStatus;
import com.kobukuro.ticketbooking.event.entity.Event;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class EventSpecification {

    private EventSpecification() {}

    public static Specification<Event> hasCategory(EventCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Event> hasCity(String city) {
        return (root, query, cb) -> city == null ? null :
                cb.equal(root.join("venue", JoinType.INNER).get("city"), city);
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("eventDateTime"), from);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("eventDateTime"), to);
    }
}
