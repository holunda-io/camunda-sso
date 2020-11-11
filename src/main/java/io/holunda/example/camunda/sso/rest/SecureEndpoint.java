package io.holunda.example.camunda.sso.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(REST.REST_PREFIX)
public class SecureEndpoint {

    @GetMapping(value = "/info", produces = "application/json")
    public ResponseEntity<Info> getSecuredInfo() {
        final Info info = new Info();
        info.setValue("Hello secret world");
        return ResponseEntity.ok(info);
    }


    public static class Info {
        private String value;
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
