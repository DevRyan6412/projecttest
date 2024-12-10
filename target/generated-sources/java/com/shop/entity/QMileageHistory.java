package com.shop.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMileageHistory is a Querydsl query type for MileageHistory
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMileageHistory extends EntityPathBase<MileageHistory> {

    private static final long serialVersionUID = -1607294582L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMileageHistory mileageHistory = new QMileageHistory("mileageHistory");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    public final StringPath type = createString("type");

    public QMileageHistory(String variable) {
        this(MileageHistory.class, forVariable(variable), INITS);
    }

    public QMileageHistory(Path<? extends MileageHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMileageHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMileageHistory(PathMetadata metadata, PathInits inits) {
        this(MileageHistory.class, metadata, inits);
    }

    public QMileageHistory(Class<? extends MileageHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

