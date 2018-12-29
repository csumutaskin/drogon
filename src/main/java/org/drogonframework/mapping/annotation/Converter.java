package org.drogonframework.mapping.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.drogonframework.mapping.entity.ConvertibleDTO;

/**
 * Class level annotation that indicates the converter entity for a given {@link ConvertibleDTO} object
 * @author UMUT
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Converter 
{
	public String id();
	public String converterEntity();
}
