package gov.cms.mat.patients.conversion.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.ResourceFileUtil;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class PatientConverterTest implements ResourceFileUtil {

        @SneakyThrows
    @Test
    void process() {
        String all = getStringFromResource("/cqm_patients.json");

            ObjectMapper objectMapper = new ObjectMapper();

        List<String> splits = Stream.of(all.split("\n"))
                .map(String::new)
                .collect(Collectors.toList());

            Set<String> types =   new TreeSet<>();

            for( String split:  splits ) {
          try {
              BonniePatient bonniePatient = objectMapper.readValue(split, BonniePatient.class);

         var elements=     bonniePatient.getQdmPatient().getDataElements();

         elements.forEach(d-> {
             types.add(  d.get_type());

//             if( d.getTargetOutcome() != null) {
//                 System.out.println(  bonniePatient.get_id());
//             }


//             if(CollectionUtils.isNotEmpty(d.getComponents())) {
//
//                 d.getComponents().forEach(c-> {
//                     if( c.getResult() != null) {
//                         System.out.println(  bonniePatient.get_id());
//                     }
//                 });
//
//             }
         });


              // System.out.println(split);
              // System.out.println(bonniePatient.get_id());
          } catch (Exception e) {
              System.out.println(split);
              e.printStackTrace();
              throw new IllegalArgumentException("shit");
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
        BonniePatient[] patients =   objectMapper.readValue(all, BonniePatient[].class);

        //   objectMapper.readValue()

    }


}