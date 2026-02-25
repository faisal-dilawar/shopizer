package com.salesmanager.test.utils;



import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.business.utils.CacheUtils;
import com.salesmanager.core.model.common.Address;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.modules.utils.Encryption;
import com.salesmanager.core.modules.utils.GeoLocation;
import com.salesmanager.test.configuration.ConfigurationTest;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ConfigurationTest.class})
public class UtilsTestCase  {
	
	
	@Inject
	private CountryService countryService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private Encryption encryption;
	
	@Inject
	private CacheUtils cache;
	
	@Inject
	private GeoLocation geoLoaction;
	

	
	//@Test
	@Ignore
	public void testCache() throws Exception {
		

		
		@SuppressWarnings("rawtypes")
		List countries = countryService.list();

		//CacheUtils cache = CacheUtils.getInstance();
		cache.putInCache(countries, "COUNTRIES");
		
		@SuppressWarnings("rawtypes")
		List objects = (List) cache.getFromCache("COUNTRIES");
		
		Assert.assertNotNull(objects);
		
	}
	
	@Test
	public void testCurrency_javaDefaultSymbolUsedWhenNoOverride() throws Exception {
		Currency currency = currencyService.getByCode("INR");
		Assert.assertNotNull("INR currency must exist in the database", currency);
		// No override is set in the seeded data, so Java's default "Rs." must be returned
		Assert.assertEquals("Rs.", currency.getSymbol());
	}

	@Test
	public void testCurrency_overrideSymbolTakesPrecedence() throws Exception {
		Currency currency = currencyService.getByCode("INR");
		Assert.assertNotNull(currency);

		currency.setSymbolOverride("₹");
		Assert.assertEquals("₹", currency.getSymbol());

		// Clearing the override must fall back to Java default
		currency.setSymbolOverride(null);
		Assert.assertEquals("Rs.", currency.getSymbol());
	}
	
	@Test
	public void testGeoLocation() throws Exception {
		
		Address address = geoLoaction.getAddress("96.21.132.0");
		if(address!=null) {
			System.out.println(address.getCountry());
		}
		
	}
	

}
