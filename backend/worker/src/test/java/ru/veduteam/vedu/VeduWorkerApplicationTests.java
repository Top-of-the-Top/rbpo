package ru.veduteam.vedu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"REDIS_HOST=localhost",
		"REDIS_PORT=6379",
		"REDIS_PASSWORD=test"
})
class VeduWorkerApplicationTests {

	@Test
	void contextLoads() {
	}

}
