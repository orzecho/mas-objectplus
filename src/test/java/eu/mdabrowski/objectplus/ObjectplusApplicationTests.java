package eu.mdabrowski.objectplus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ObjectplusApplicationTests {

    @Autowired
    private ExtensionRepository extensionRepository;

    @Autowired
    private ObjectPlusService objectPlusService;

    @Test
    public void objectPlusTest() {
        //when
        Person person = new Person("Jan");

        //then
        assertThat(ObjectPlus.getExtensions()).containsKeys(Person.class);
        assertThat(ObjectPlus.getExtensions().get(Person.class)).contains(person);
    }

    @Test
    public void persistanceTest() {
        //given
        Person person = new Person("Jan");

        //when
        objectPlusService.saveAll();
        objectPlusService.reload();

        //then
        assertThat(extensionRepository.findAll()).isNotEmpty();
        log.info(extensionRepository.findAll().get(0).getKlass().getName());
        log.info(new String(extensionRepository.findAll().get(0).getExtensions()));
        assertThat(ObjectPlus.getExtensions()).containsKeys(Person.class);
        assertThat(ObjectPlus.getExtensions().get(Person.class)).contains(person);
    }

}
