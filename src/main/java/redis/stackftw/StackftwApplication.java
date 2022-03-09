package redis.stackftw;

import com.google.common.io.Files;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.tuple.Fields;
import io.redisearch.aggregation.SortedField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableRedisDocumentRepositories
public class StackftwApplication {


    @Bean
    CommandLineRunner commandLineRunner(
            @Value("file:///${HOME}/Desktop/data.csv") File dataFile,
            EntityStream stream,
            RedisModulesOperations<String, Object> redisModulesOperations,
            PersonRepository repository) {
        return args -> {


            redisModulesOperations.opsForSearch(Person.class.getName() + "Idx");
            repository.deleteAll();

            var data = Files
                    .readLines(dataFile, StandardCharsets.UTF_8)
                    .stream()
                    .map(l -> l.split(","))
                    .map(ar -> new Person(null, ar[0], ar[1], Integer.parseInt(ar[2]),
                            new Point(Double.parseDouble(ar[3].trim()), Double.parseDouble(ar[4].trim()))))
                    .toList();
            repository.saveAll(data);

            log(repository.findByFirstName("Brian"));
            log(repository.findByAgeBetween(35, 40));
            //
            log(repository.search("jo*"));
            log(repository.findByFirstNameAndLastName("Tam", "Koon"));
            log(repository.findByFirstNameAndLastName("Brian", "Sam*"));
            log(repository.findAll());
            log(repository.findByLocationNear(new Point(-122.124500, 47.640160), new Distance(1, Metrics.MILES)));
            log(repository.findByLocationNear(new Point(38.7205373, -9.148091), new Distance(1000, Metrics.MILES)));
            log(stream.of(Person.class).collect(Collectors.toList()));
            log(stream.of(Person.class).filter(Person$.LAST_NAME.eq("Long").or(Person$.LAST_NAME.eq("Koon"))).collect(Collectors.toList()));
            var results = stream
                    .of(Person.class)
                    .sorted(Person$.AGE, SortedField.SortOrder.DESC)
                    .map(Fields.of(Person$.FIRST_NAME, Person$.LAST_NAME, Person$.AGE))
                    .collect(Collectors.toList());
            log(results);


        };
    }

    private static void log(Iterable<?> people) {
        System.out.println("\uD83E\uDD2D");
        people.forEach(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(StackftwApplication.class, args);
    }
}

interface PersonRepository extends RedisDocumentRepository<Person, String> {


    Collection<Person> findByLocationNear(Point location, Distance distance);

    Collection<Person> search(String query);

    Collection<Person> findByAgeBetween(int min, int max);

    Collection<Person> findByFirstName(String name);

    Collection<Person> findByFirstNameAndLastName(String firstName, String lastName);

}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class Person {

    @Id
    @Indexed
    private String id;

    @Searchable
    private String firstName, lastName;

    @Indexed
    private Integer age;

    @Indexed
    private Point location;

}