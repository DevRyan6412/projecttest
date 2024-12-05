package com.shop.validator;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BusinessRegistrationNumberValidator implements ConstraintValidator<ValidBusinessRegistrationNumber, MemberFormDto> {

    @Override
    public boolean isValid(MemberFormDto memberFormDto, ConstraintValidatorContext context) {
        if (Role.MANAGER.name().equals(memberFormDto.getRole())) {
            // 사업자 번호가 비어 있거나 10자리가 아닐 경우 오류
            return memberFormDto.getBusinessRegistrationNumber() != null &&
                    memberFormDto.getBusinessRegistrationNumber().length() == 10;
        }
        // 다른 Role은 검증하지 않음
        return true;
    }
}
