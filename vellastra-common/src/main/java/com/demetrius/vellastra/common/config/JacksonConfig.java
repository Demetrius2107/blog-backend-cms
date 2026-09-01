package com.demetrius.vellastra.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.jackson2.autoconfigure.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * <h3>Jackson 全局序列化配置</h3>
 *
 * <p>统一配置 JSON 序列化/反序列化规则，解决以下常见问题：</p>
 * <ul>
 *   <li><b>Long 精度丢失</b> — 将 Long 类型序列化为 String，防止前端 JS 精度丢失</li>
 *   <li><b>日期格式统一</b> — {@link LocalDateTime} 格式化为 {@code yyyy-MM-dd HH:mm:ss}</li>
 *   <li><b>时区统一</b> — 设定为 {@code Asia/Shanghai}</li>
 *   <li><b>空对象不抛异常</b> — 禁止 {@code SerializationFeature.FAIL_ON_EMPTY_BEANS}</li>
 * </ul>
 *
 * @author wanqiu
 * @since 1.1
 * @since 2026-07-18
 */
@Configuration
public class JacksonConfig {

    /** 日期时间格式 */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 日期格式 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long → String 序列化，防止前端 JS 精度丢失
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // LocalDateTime 序列化/反序列化
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            // LocalDate 序列化/反序列化
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // 时区设为 Asia/Shanghai
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // 禁止空对象抛异常
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        };
    }
}