package org.sourcehot.runner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

public class JsonPropertySourceLoader implements PropertySourceLoader {

	public String[] getFileExtensions() {
		return new String[] { "json" };
	}

	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		if (resource == null || !resource.exists()) {
			return Collections.emptyList();
		}

		Map<String, Object> configs = JSON.parseObject(resource.getInputStream(), Map.class);

		return Collections.singletonList(new MapPropertySource(name, configs));
	}

}
