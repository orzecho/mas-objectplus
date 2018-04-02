package eu.mdabrowski.objectplus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObjectPlusService {

    private final ExtensionRepository extensionRepository;
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final Try<ObjectOutputStream> objectOutputStream =
            Try.of(() -> new ObjectOutputStream(byteArrayOutputStream));

    public void saveAll() {
        ObjectPlus.getExtensions()
                .entrySet().stream()
                .map(e -> new Extension(e.getKey(), objectOutputStream.andThenTry(o -> o.writeObject(e.getValue()))
                        .andThenTry(byteArrayOutputStream::flush)
                        .map(o -> byteArrayOutputStream.toByteArray())
                        .get()))
                .forEach(extensionRepository::save);
    }

    public void loadAll() {
        extensionRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Extension::getKlass,
                        e -> (Vector) getObject(e)));
    }

    private Object getObject(Extension e) {
        return Try.of(() -> new ObjectInputStream(new ByteArrayInputStream(e.getExtensions())).readObject()).get();
    }
}
