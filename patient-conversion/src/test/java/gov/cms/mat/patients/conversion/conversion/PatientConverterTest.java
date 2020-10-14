package gov.cms.mat.patients.conversion.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.ResourceFileUtil;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.service.PatientService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class PatientConverterTest implements ResourceFileUtil {

    @Autowired
    PatientService patientService;

    @SneakyThrows
    @Test
    void process() {
        String all = getStringFromResource("/cqm_patients.json");

        ObjectMapper objectMapper = new ObjectMapper();

        List<String> splits = Stream.of(all.split("\n"))
                .map(String::new)
                .collect(Collectors.toList());

        Set<String> types = new TreeSet<>();
        BonniePatient bonniePatient = null;
        for (String split : splits) {

            try {
                bonniePatient = objectMapper.readValue(split, BonniePatient.class);
                patientService.processOne(bonniePatient);

            } catch (Exception e) {
                System.out.println(bonniePatient.get_id());
                System.out.println(split);
                e.printStackTrace();
                // throw new IllegalArgumentException("shit");
            }

        }

        System.out.println(types);


//        String json = "[" + String.join(",\n", splits) + "]";
//
//        Path path = Paths.get("/tmp/all.json");
//        byte[] strToBytes = json.getBytes();

        //     Files.write(path, strToBytes);
    }

    @SneakyThrows
    @Test
    public void load() {

        String all = getStringFromResource("/patients_all_QDM_PROD_as_array.json");
        ObjectMapper objectMapper = new ObjectMapper();
        BonniePatient[] patients = objectMapper.readValue(all, BonniePatient[].class);

        //   objectMapper.readValue()

    }


}