package com.openjiuwen.studio.agent.space.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 通用字符串校验器
 */
public class ValidChatStringValidator implements ConstraintValidator<ValidChatString, String> {
    private static final Pattern VALID_CONTENT = Pattern.compile(
        "^[0-9a-zA-Z\\u4E00-\\u9FFF\\s" + "!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?\\\\"
            + "！@#￥%……&*（）——+={}【】、；：‘’“”，。《》？·`~" + "≈≠≤≥±×÷∫∑∏√∞∠∥⊥∪∩∈∉⊆⊇∅∀∃∴∵∝πτℵ∂∇¬∧∨⊕⊗→←↑↓²³" + "]+$");

    @Override
    public void initialize(ValidChatString constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return VALID_CONTENT.matcher(value).matches();
    }
}
