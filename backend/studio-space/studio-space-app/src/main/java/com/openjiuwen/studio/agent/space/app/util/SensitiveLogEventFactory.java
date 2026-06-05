package com.openjiuwen.studio.agent.space.app.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveLogEventFactory implements LogEventFactory {
    private static final String sensitiveReg = "((?<=http://.{4})(.*?)(?=/eureka))|((?<=password:)([\\d\\w]+))";

    private static final String maskReg = "[\\d\\w!@#%&_.:]+";

    private static final SensitiveLogEventFactory instance = new SensitiveLogEventFactory();

    private final Pattern sensitivePattern = Pattern.compile(sensitiveReg);

    public static SensitiveLogEventFactory getInstance() {
        return instance;
    }


    @Override
    public LogEvent createEvent(String loggerName, Marker marker, String fqcn, Level level, Message message,
                                List<Property> properties, Throwable throwable) {
        String formattedMessage = message.getFormattedMessage();
        String obfuscateMsg = obfuscateIfNecessary(sensitivePattern, formattedMessage);
        SimpleMessage msg = new SimpleMessage(obfuscateMsg);
        return new Log4jLogEvent(loggerName, marker, fqcn, level, msg, properties, throwable);
    }

    private String obfuscateIfNecessary(Pattern sensitivePattern, String formattedMessage) {
        Matcher matcher = sensitivePattern.matcher(formattedMessage);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            if (!Objects.isNull(group)) {
                String obfuscated = group.replaceAll(maskReg, "***********");
                matcher.appendReplacement(stringBuffer, obfuscated);
            }
        }
        if (stringBuffer.length() != 0) {
            matcher.appendTail(stringBuffer);
            return stringBuffer.toString();
        }
        return formattedMessage;
    }
}
