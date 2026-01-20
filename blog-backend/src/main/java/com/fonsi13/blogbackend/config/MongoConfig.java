package com.fonsi13.blogbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        // Esto ayuda a Spring a entender cómo convertir tipos complejos,
        // pero por ahora lo dejamos para configurar el MappingConverter después.
        return new MongoCustomConversions(Collections.emptyList());
    }

    // Método para remover el campo _class
    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory,
                                                       MongoMappingContext context,
                                                       MongoCustomConversions conversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
        converter.setCustomConversions(conversions);

        // AQUÍ ESTÁ EL TRUCO: Seteamos el TypeMapper a "null" para que no añada el campo _class
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return converter;
    }
}
