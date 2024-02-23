package com.muralis.minhasfinancas.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.muralis.minhasfinancas.service.impl.CsvServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class CsvServiceTest {

	@SpyBean
	CsvServiceImpl service;
	
	@Test
	public void deveConverterCsvDtoEMLancamento() {
		
	}
	
}
