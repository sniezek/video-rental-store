package film.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DatabasesConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.film")
    public DataSourceProperties filmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dsFilm")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.film")
    public DataSource filmDataSource() {
        return filmDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.event")
    public DataSourceProperties eventDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dsEvent")
    @ConfigurationProperties(prefix = "spring.datasource.event")
    public DataSource eventDataSource() {
        return eventDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsEvent") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
