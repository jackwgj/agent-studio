package com.openjiuwen.studio.agent.space.common.validator;

import com.alibaba.fastjson2.JSON;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * json字串校验器
 */
public class ValidJsonStringValidator implements ConstraintValidator<ValidJsonString, String> {
    private static final Pattern VALID_CONTENT = Pattern.compile(
        "^[0-9a-zA-Z\\u4E00-\\u9FFF\\s" + "!@#$%^&*()_+\\-–=\\[\\]{}|;:'\",.<>/?\\\\"
            + "！@#￥%……&*（）——+={}【】、；：‘’“”，。《》？·`~" + "≈≠≤≥±×÷∫∑∏√∞∠∥⊥∪∩∈∉⊆⊇∅∀∃∴∵∝πτℵ∂∇¬∧∨⊕⊗→←↑↓²³" + "]+$");

    @Override
    public void initialize(ValidJsonString constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        try {
            JSON.parseObject(value, Object.class);
            return VALID_CONTENT.matcher(value).matches();
        } catch (Exception e) {
            return false;
        }
    }
}
