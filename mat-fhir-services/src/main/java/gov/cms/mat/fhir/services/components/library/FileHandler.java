package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.fhir.services.exceptions.ConfigException;
import gov.cms.mat.fhir.services.exceptions.LibraryFileNotFoundException;
import org.eclipse.jetty.io.RuntimeIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface FileHandler {

    default Path checkAndCreatePath(String directoryName) {
        Path path = Paths.get(directoryName);

        if (!path.toFile().exists()) {
            throw new ConfigException(directoryName, "does not exist");
        }

        if (!path.toFile().isDirectory()) {
            throw new ConfigException(directoryName, "is not directory");
        }

        if (!path.toFile().canWrite()) {
            throw new ConfigException("Path " + directoryName, "is not writeable");
        }

        return path;
    }

    default List<String> findAll(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            return walk.map(Path::toFile)
                    .map(File::getName)
                    .filter(n -> n.endsWith(".cql"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }


    default String findCqlInFile(Path path, String name) {
        Path cqlPath = path.resolve(name);

        if (cqlPath.toFile().exists()) {
            try {
                return new String(Files.readAllBytes(cqlPath));
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        } else {
            throw new LibraryFileNotFoundException(name);
        }
    }


}
