package com.shop.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = 728765766L;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final StringPath couponCode = createString("couponCode");

    public final StringPath description = createString("description");

    public final NumberPath<Double> discountAmount = createNumber("discountAmount", Double.class);

    public final NumberPath<Double> discountRate = createNumber("discountRate", Double.class);

    public final StringPath discountType = createString("discountType");

    public QCoupon(String variable) {
        super(Coupon.class, forVariable(variable));
    }

    public QCoupon(Path<? extends Coupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoupon(PathMetadata metadata) {
        super(Coupon.class, metadata);
    }

}

