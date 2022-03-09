package redis.stackftw;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.domain.geo.Metrics;

import java.util.Arrays;
import java.util.Collection;

@SpringBootApplication
@EnableRedisDocumentRepositories
public class StackftwApplication {


    @Bean
    CommandLineRunner commandLineRunner(
            RedisModulesOperations<String, Object> redisModulesOperations,
            PersonRepository personRepository) {
        return args -> {
            redisModulesOperations.opsForSearch(Person.class.getName() + "Idx");
            personRepository.deleteAll();
            var data = Arrays.stream("""
                            Brian,Sam-Bodden,40,-122.066540, 37.377690
                            Tam,Koon,36,-122.124500, 47.640160
                            Josh,Long,38,38.7635877,-9.2018309
                            Jose,Lat,49,38.7205373,-9.148091
                            Jose,Lat,35,37.0990749,-8.6868258
                            Joshua,Long,30,37.0990749,-8.6868258
                            """
                            .split(System.lineSeparator()))
                    .toList()
                    .stream()
                    .map(l -> l.split(","))
                    .map(ar -> new Person(null, ar[0], ar[1], Integer.parseInt(ar[2]),
                            new Point(Double.parseDouble(ar[3].trim()), Double.parseDouble(ar[4].trim()))))
                    .toList();
            personRepository.saveAll(data);
            log(personRepository.findByFirstName("Brian"));
            log(personRepository.findByAgeBetween(35, 40));
            log(personRepository.search("jo*"));
            log(personRepository.findByFirstNameAndLastName("Tam", "Koon"));
            log(personRepository.findByFirstNameAndLastName("Brian", "Sam*"));
            log(personRepository.findAll());
            log(personRepository.findByLocationNear(new Point(-122.124500, 47.640160), new Distance(1, Metrics.MILES)));
            log(personRepository.findByLocationNear(new Point(38.7205373, -9.148091), new Distance(1000, Metrics.MILES)));
        };
    }

    private static void log(Iterable<Person> people) {
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