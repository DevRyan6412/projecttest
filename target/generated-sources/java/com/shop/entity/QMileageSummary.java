package com.shop.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMileageSummary is a Querydsl query type for MileageSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMileageSummary extends EntityPathBase<MileageSummary> {

    private static final long serialVersionUID = -96901988L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMileageSummary mileageSummary = new QMileageSummary("mileageSummary");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUpdated = createDateTime("lastUpdated", java.time.LocalDateTime.class);

    public final QMember member;

    public final NumberPath<Integer> totalEarned = createNumber("totalEarned", Integer.class);

    public final NumberPath<Integer> totalMileage = createNumber("totalMileage", Integer.class);

    public final NumberPath<Integer> totalUsed = createNumber("totalUsed", Integer.class);

    public QMileageSummary(String variable) {
        this(MileageSummary.class, forVariable(variable), INITS);
    }

    public QMileageSummary(Path<? extends MileageSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMileageSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMileageSummary(PathMetadata metadata, PathInits inits) {
        this(MileageSummary.class, metadata, inits);
    }

    public QMileageSummary(Class<? extends MileageSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

