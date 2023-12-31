package code.config;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.MissingProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Map;

public class CustomPropertyUtils extends PropertyUtils {
    @Override
    public Property getProperty(Class<? extends Object> type, String name, BeanAccess bAccess) {
        Map<String, Property> properties = this.getPropertiesMap(type, bAccess);
        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (handle(key).equals(handle(name))) {
                return entry.getValue();
            }
        }
        return new MissingProperty(name);
    }

    private String handle(String name) {
        String replace = StringUtils.replace(name, "-", "");
        replace = StringUtils.replace(replace, "_", "");
        return replace.toLowerCase();
    }
}
