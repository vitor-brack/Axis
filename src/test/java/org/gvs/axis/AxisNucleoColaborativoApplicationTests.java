package org.gvs.axis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

// Sobe a aplicação inteira num H2 em memória: o teste não depende de MySQL, nem no CI
@SpringBootTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@AutoConfigureTestDatabase
class AxisNucleoColaborativoApplicationTests {

	@Test
	void contextLoads() {
	}

}
