/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.annotation.DeterminableImports;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * Variant of {@link AutoConfigurationImportSelector} for
 * {@link ImportAutoConfiguration @ImportAutoConfiguration}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
	class ImportAutoConfigurationImportSelector extends AutoConfigurationImportSelector implements DeterminableImports {

	/**
	 * 注解名称集合
	 */
	private static final Set<String> ANNOTATION_NAMES;

	static {
		Set<String> names = new LinkedHashSet<>();
		names.add(ImportAutoConfiguration.class.getName());
		names.add("org.springframework.boot.autoconfigure.test.ImportAutoConfiguration");
		ANNOTATION_NAMES = Collections.unmodifiableSet(names);
	}

	@Override
	public Set<Object> determineImports(AnnotationMetadata metadata) {
		// 候选配置类集合
		List<String> candidateConfigurations = getCandidateConfigurations(metadata, null);
		// 去重配置类集合
		Set<String> result = new LinkedHashSet<>(candidateConfigurations);
		// 移除需要排除的类
		result.removeAll(getExclusions(metadata, null));
		return Collections.unmodifiableSet(result);
	}

	@Override
	protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
		return null;
	}

	@Override
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> candidates = new ArrayList<>();
		// 获取类和注解集合之间的映射
		Map<Class<?>, List<Annotation>> annotations = getAnnotations(metadata);
		// 通过collectCandidateConfigurations方法采集候选类
		annotations.forEach(
				(source, sourceAnnotations) -> collectCandidateConfigurations(source, sourceAnnotations, candidates));
		return candidates;
	}

	private void collectCandidateConfigurations(Class<?> source, List<Annotation> annotations,
			List<String> candidates) {
		// 循环处理注解
		for (Annotation annotation : annotations) {
			// 通过getConfigurationsForAnnotation方法获取候选值
			candidates.addAll(getConfigurationsForAnnotation(source, annotation));
		}
	}

	private Collection<String> getConfigurationsForAnnotation(Class<?> source, Annotation annotation) {
		// 获取注解的classes数据
		String[] classes = (String[]) AnnotationUtils.getAnnotationAttributes(annotation, true).get("classes");
		if (classes.length > 0) {
			return Arrays.asList(classes);
		}
		// 在spring.factories中寻找
		return loadFactoryNames(source);
	}

	protected Collection<String> loadFactoryNames(Class<?> source) {
		return SpringFactoriesLoader.loadFactoryNames(source, getBeanClassLoader());
	}

	@Override
	protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		// 创建排除类集合
		Set<String> exclusions = new LinkedHashSet<>();
		// 获取注解类
		Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);
		// 循环注解名称集合
		for (String annotationName : ANNOTATION_NAMES) {
			// 合并注解的数据
			AnnotationAttributes merged = AnnotatedElementUtils.getMergedAnnotationAttributes(source, annotationName);
			// 获取合并注解后的排除类
			Class<?>[] exclude = (merged != null) ? merged.getClassArray("exclude") : null;
			if (exclude != null) {
				for (Class<?> excludeClass : exclude) {
					exclusions.add(excludeClass.getName());
				}
			}
		}
		// 循环处理注解元数据中的注解列表
		for (List<Annotation> annotations : getAnnotations(metadata).values()) {
			for (Annotation annotation : annotations) {
				// 获取注解中的排除类集合
				String[] exclude = (String[]) AnnotationUtils.getAnnotationAttributes(annotation, true).get("exclude");
				if (!ObjectUtils.isEmpty(exclude)) {
					exclusions.addAll(Arrays.asList(exclude));
				}
			}
		}
		// 通过getExcludeAutoConfigurationsProperty方法获取排除类加入到排除类集合中
		exclusions.addAll(getExcludeAutoConfigurationsProperty());
		return exclusions;
	}

	protected final Map<Class<?>, List<Annotation>> getAnnotations(AnnotationMetadata metadata) {
		// 创建返回值集合
		MultiValueMap<Class<?>, Annotation> annotations = new LinkedMultiValueMap<>();
		// 获取注解类
		Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);
		// 采集注解相关数据
		collectAnnotations(source, annotations, new HashSet<>());
		return Collections.unmodifiableMap(annotations);
	}

	private void collectAnnotations(Class<?> source, MultiValueMap<Class<?>, Annotation> annotations,
			HashSet<Class<?>> seen) {

		if (source != null && seen.add(source)) {
			// 循环处理source的注解列表
			for (Annotation annotation : source.getDeclaredAnnotations()) {
				// 不是java.lang.annotation的处理
				if (!AnnotationUtils.isInJavaLangAnnotationPackage(annotation)) {
					// 在 ANNOTATION_NAMES 中存在放入到结果集合中
					if (ANNOTATION_NAMES.contains(annotation.annotationType().getName())) {
						annotations.add(source, annotation);
					}
					collectAnnotations(annotation.annotationType(), annotations, seen);
				}
			}
			collectAnnotations(source.getSuperclass(), annotations, seen);
		}
	}

	@Override
	public int getOrder() {
		return super.getOrder() - 1;
	}

	@Override
	protected void handleInvalidExcludes(List<String> invalidExcludes) {
		// Ignore for test
	}

}
