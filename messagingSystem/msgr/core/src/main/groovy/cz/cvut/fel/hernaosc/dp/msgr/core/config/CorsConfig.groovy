package cz.cvut.fel.hernaosc.dp.msgr.core.config

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Slf4j
class CorsConfig {
    @Value('${cors.pattern:/**}')
    String cors

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            void addCorsMappings(CorsRegistry registry) {
                log.info "CORS policy set up as '$cors'"
                registry.addMapping(cors)
                        .allowedMethods("GET", "POST", "PUT", "HEAD", "DELETE", "OPTIONS")
            }
        }
    }
}
