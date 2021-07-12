package me.kuku.simbot.annotation;

import kotlin.annotation.MustBeDocumented;
import love.forte.common.utils.annotation.AnnotateMapping;
import love.forte.simbot.annotation.ListenGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@MustBeDocumented
@AnnotateMapping(value = ListenGroup.class)
@ListenGroup(value = "", append = false)
public @interface SkipListenGroup {
}
